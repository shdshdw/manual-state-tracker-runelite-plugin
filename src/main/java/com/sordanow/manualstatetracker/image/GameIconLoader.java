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

import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.AsyncBufferedImage;

/**
 * Fetches the picture behind a {@link GameIcon} at its natural size.
 *
 * <p>Both underlying managers already hop to the client thread themselves and keep retrying until
 * the game cache is available, so this can be called from anywhere. The callback fires once the
 * image exists, which may be well after the call and, for items, on the client thread.</p>
 */
@Singleton
public class GameIconLoader
{
	private final SpriteManager spriteManager;
	private final ItemManager itemManager;

	@Inject
	private GameIconLoader(SpriteManager spriteManager, ItemManager itemManager)
	{
		this.spriteManager = spriteManager;
		this.itemManager = itemManager;
	}

	/**
	 * @param onLoaded receives the unscaled image; never called if the icon cannot be produced
	 */
	public void load(GameIconType type, int id, Consumer<BufferedImage> onLoaded)
	{
		if (type == GameIconType.ITEM)
		{
			// The returned image starts blank and fills in later, so wait for it rather than
			// handing a blank canvas straight to the caller
			final AsyncBufferedImage image = itemManager.getImage(id);

			if (image != null)
			{
				image.onLoaded(() -> onLoaded.accept(image));
			}

			return;
		}

		spriteManager.getSpriteAsync(id, 0, onLoaded::accept);
	}
}
