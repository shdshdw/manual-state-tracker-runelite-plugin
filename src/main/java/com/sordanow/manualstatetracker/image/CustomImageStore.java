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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;

/**
 * Owns {@code .runelite/manual-state-tracker/images}, the one directory this plugin reads user
 * images from. Users can either drop PNGs in there themselves or import them through the panel.
 *
 * <p>All methods touch the disk and must therefore be called off the client thread.</p>
 */
@Slf4j
@Singleton
public class CustomImageStore
{
	private static final String PLUGIN_DIR_NAME = "manual-state-tracker";
	private static final String IMAGE_DIR_NAME = "images";
	private static final String PNG_EXTENSION = ".png";

	/** Directory images are read from, whether or not it currently exists on disk. */
	public Path getImageDirectory()
	{
		return RuneLite.RUNELITE_DIR.toPath().resolve(PLUGIN_DIR_NAME).resolve(IMAGE_DIR_NAME);
	}

	/**
	 * Creates the image directory if it is missing.
	 *
	 * @return the directory, or null if it could not be created
	 */
	@Nullable
	public Path ensureImageDirectory()
	{
		final Path dir = getImageDirectory();

		try
		{
			Files.createDirectories(dir);
			return dir;
		}
		catch (IOException e)
		{
			log.warn("Could not create image directory {}", dir, e);
			return null;
		}
	}

	/**
	 * @return the names of every PNG in the image directory, alphabetically, or an empty list if
	 * the directory does not exist yet
	 */
	public List<String> listImageNames()
	{
		final Path dir = getImageDirectory();

		if (!Files.isDirectory(dir))
		{
			return Collections.emptyList();
		}

		try (Stream<Path> entries = Files.list(dir))
		{
			final List<String> names = new ArrayList<>();

			entries.filter(Files::isRegularFile)
				.map(path -> path.getFileName().toString())
				.filter(name -> name.toLowerCase(Locale.ROOT).endsWith(PNG_EXTENSION))
				.forEach(names::add);

			names.sort(String.CASE_INSENSITIVE_ORDER);
			return names;
		}
		catch (IOException e)
		{
			log.warn("Could not list image directory {}", dir, e);
			return Collections.emptyList();
		}
	}

	/**
	 * Resolves a stored file name against the image directory.
	 *
	 * @return the file, or null if the name is empty or would escape the image directory
	 */
	@Nullable
	public Path resolve(String fileName)
	{
		if (fileName == null || fileName.isEmpty())
		{
			return null;
		}

		final Path dir = getImageDirectory().toAbsolutePath().normalize();
		final Path resolved = dir.resolve(fileName).toAbsolutePath().normalize();

		// A hand-edited config could contain "../..", which must not be allowed to read arbitrary files
		if (!resolved.startsWith(dir))
		{
			log.warn("Refusing to read image outside of the plugin directory: {}", fileName);
			return null;
		}

		return resolved;
	}

	/**
	 * Copies a PNG chosen by the user into the image directory, without overwriting an existing
	 * file of the same name.
	 *
	 * @return the file name it ended up under, or null if the copy failed
	 */
	@Nullable
	public String importImage(File source)
	{
		final Path dir = ensureImageDirectory();

		if (dir == null)
		{
			return null;
		}

		final String baseName = stripExtension(source.getName());
		Path destination = dir.resolve(baseName + PNG_EXTENSION);

		for (int suffix = 2; Files.exists(destination); suffix++)
		{
			destination = dir.resolve(baseName + " (" + suffix + ")" + PNG_EXTENSION);
		}

		try
		{
			Files.copy(source.toPath(), destination, StandardCopyOption.COPY_ATTRIBUTES);
			log.debug("Imported {} as {}", source, destination.getFileName());
			return destination.getFileName().toString();
		}
		catch (IOException e)
		{
			log.warn("Could not import image {}", source, e);
			return null;
		}
	}

	private static String stripExtension(String fileName)
	{
		final int dot = fileName.lastIndexOf('.');
		return dot > 0 ? fileName.substring(0, dot) : fileName;
	}
}
