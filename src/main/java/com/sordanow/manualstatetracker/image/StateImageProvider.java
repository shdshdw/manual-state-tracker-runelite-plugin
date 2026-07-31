/*
 * Copyright (c) 2026, Sordanow
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.sordanow.manualstatetracker.image;

import com.sordanow.manualstatetracker.data.TrackedState;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * Turns {@link TrackedState}s into ready-to-draw images and keeps them cached at the sizes the
 * plugin actually renders.
 *
 * <p>Producing an image can mean reading a PNG off disk or waiting for the sprite cache, neither of
 * which may happen on the client thread. So the work is split in two: {@link #sync} schedules
 * whatever is missing onto the background executor, and {@link #get} is a pure cache lookup that
 * the overlay can call every frame. A state whose image is not ready yet simply does not draw.</p>
 */
@Slf4j
@Singleton
public class StateImageProvider
{
	private final GameIconLoader iconLoader;
	private final CustomImageStore imageStore;
	private final ScheduledExecutorService executor;

	/** Cache key is {@code <image identity>@<size>}; see {@link TrackedState#imageCacheKey()}. */
	private final Map<String, BufferedImage> cache = new ConcurrentHashMap<>();

	/** Keys with work already scheduled, so a slow load is not requested again every sync. */
	private final Set<String> pending = ConcurrentHashMap.newKeySet();

	/** Notified whenever a new image lands in the cache, so the UI can repaint. */
	private final CopyOnWriteArrayList<Runnable> readyListeners = new CopyOnWriteArrayList<>();

	@Inject
	private StateImageProvider(GameIconLoader iconLoader, CustomImageStore imageStore, ScheduledExecutorService executor)
	{
		this.iconLoader = iconLoader;
		this.imageStore = imageStore;
		this.executor = executor;
	}

	/**
	 * Looks up an already-rendered image. Safe to call from the client thread and from Swing.
	 *
	 * @return the image, or null if it has not been produced yet
	 */
	@Nullable
	public BufferedImage get(TrackedState state, int size)
	{
		return cache.get(cacheKey(state, size));
	}

	/**
	 * Brings the cache in line with the states that are currently configured: schedules anything
	 * missing and drops anything no longer referenced.
	 *
	 * @param states every state that may need to be drawn
	 * @param sizes the sizes those states are drawn at, typically the overlay size and the panel
	 * preview size
	 */
	public void sync(Collection<TrackedState> states, int... sizes)
	{
		final Set<String> live = new HashSet<>();

		for (TrackedState state : states)
		{
			for (int size : sizes)
			{
				live.add(cacheKey(state, size));
			}
		}

		cache.keySet().retainAll(live);
		pending.retainAll(live);

		for (TrackedState state : states)
		{
			for (int size : sizes)
			{
				request(state, size);
			}
		}
	}

	/** Drops every cached image, for example after the configured overlay size changed. */
	public void clear()
	{
		cache.clear();
		pending.clear();
	}

	public void addReadyListener(Runnable listener)
	{
		readyListeners.add(listener);
	}

	public void removeReadyListener(Runnable listener)
	{
		readyListeners.remove(listener);
	}

	private void request(TrackedState state, int size)
	{
		final String key = cacheKey(state, size);

		if (size <= 0 || cache.containsKey(key) || !pending.add(key))
		{
			return;
		}

		switch (state.getImageSource())
		{
			case COLOR:
				final Color swatchColor = state.getColor();
				executor.execute(() -> complete(key, ImageFactory.colorSwatch(size, swatchColor)));
				break;

			case CUSTOM_FILE:
				final String fileName = state.getCustomFileName();
				executor.execute(() -> complete(key, loadCustomImage(fileName, size)));
				break;

			case BUILT_IN:
				final BuiltInImage builtIn = state.resolveBuiltInImage();
				final Color shapeFill = state.getColor();
				final Color shapeOutline = state.getOutlineColor();
				executor.execute(() -> complete(key, ImageFactory.shape(
					size, builtIn.getShape(), builtIn.getQuarterTurns(), shapeFill, shapeOutline)));
				break;

			case GAME_ICON:
				requestGameIcon(key, state.getGameIconType(), state.getGameIconId(), size);
				break;

			case TEXT:
			default:
				final String text = state.getText();
				final Color textColor = state.getColor();
				final Color outlineColor = state.getOutlineColor();
				executor.execute(() -> complete(key, ImageFactory.text(size, text, textColor, outlineColor)));
				break;
		}
	}

	private void requestGameIcon(String key, GameIconType type, int id, int size)
	{
		if (id < 0)
		{
			// No icon chosen yet; nothing to draw
			pending.remove(key);
			return;
		}

		// The loader hops to the client thread itself and retries until the game cache is available;
		// we hop straight back off it so no scaling happens on the client thread
		iconLoader.load(type, id, image ->
			executor.execute(() -> complete(key, ImageFactory.fitToSquare(image, size))));
	}

	@Nullable
	private BufferedImage loadCustomImage(String fileName, int size)
	{
		final Path path = imageStore.resolve(fileName);

		if (path == null || !Files.isRegularFile(path))
		{
			log.debug("Custom image {} is missing", fileName);
			return null;
		}

		try
		{
			final BufferedImage source = ImageIO.read(path.toFile());

			if (source == null)
			{
				log.debug("Custom image {} is not a readable image", fileName);
				return null;
			}

			return ImageFactory.fitToSquare(source, size);
		}
		catch (IOException | RuntimeException e)
		{
			// ImageIO throws unchecked exceptions on some malformed files as well as IOException
			log.debug("Could not read custom image {}", fileName, e);
			return null;
		}
	}

	private void complete(String key, @Nullable BufferedImage image)
	{
		pending.remove(key);

		if (image == null)
		{
			return;
		}

		cache.put(key, image);
		readyListeners.forEach(Runnable::run);
	}

	private static String cacheKey(TrackedState state, int size)
	{
		return state.imageCacheKey() + '@' + size;
	}
}
