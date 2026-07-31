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
package com.sordanow.manualstatetracker;

import com.sordanow.manualstatetracker.data.TrackedState;
import com.sordanow.manualstatetracker.image.StateImageProvider;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TextComponent;

/**
 * Draws the image of the currently active state.
 *
 * <p>The overlay is detached and movable, so RuneLite lets the user drag it anywhere and remembers
 * where they put it. Rendering is deliberately trivial: the image was already produced and scaled
 * on a background thread by {@link StateImageProvider}, so this only blits and, optionally, labels
 * it.</p>
 */
class ManualStateTrackerOverlay extends Overlay
{
	/** Space between the image and the panel edge when the background is enabled. */
	private static final int BACKGROUND_PADDING = 4;

	/** Space between the image and the state name. */
	private static final int LABEL_GAP = 2;

	private static final Color BORDER_COLOR = new Color(0x38, 0x38, 0x38);

	private final ManualStateTrackerPlugin plugin;
	private final ManualStateTrackerConfig config;
	private final StateImageProvider imageProvider;

	private final TextComponent label = new TextComponent();

	@Inject
	private ManualStateTrackerOverlay(ManualStateTrackerPlugin plugin, ManualStateTrackerConfig config,
		StateImageProvider imageProvider)
	{
		super(plugin);

		this.plugin = plugin;
		this.config = config;
		this.imageProvider = imageProvider;

		// DYNAMIC plus movable means RuneLite does not anchor the overlay anywhere: the user drags
		// it where they want and the client remembers the position
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(PRIORITY_HIGH);
		setMovable(true);
		setSnappable(true);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		final TrackedState state = plugin.getActiveState();

		if (state == null)
		{
			return null;
		}

		final int size = plugin.getOverlaySize();
		final BufferedImage image = imageProvider.get(state, size);

		if (image == null)
		{
			// Still being loaded or scaled; skip this frame rather than block
			return null;
		}

		final boolean showBackground = config.showBackground();
		final int padding = showBackground ? BACKGROUND_PADDING : 0;

		final StateNamePosition namePosition = config.stateNamePosition();
		final Font labelFont = FontManager.getRunescapeSmallFont();
		final FontMetrics metrics = graphics.getFontMetrics(labelFont);

		// A state with a blank name would otherwise reserve height for nothing
		final String labelText = namePosition.isVisible() && !state.getName().trim().isEmpty()
			? state.getName()
			: null;

		final boolean nameAbove = namePosition == StateNamePosition.ABOVE;
		final int labelWidth = labelText == null ? 0 : metrics.stringWidth(labelText);
		final int labelHeight = labelText == null ? 0 : LABEL_GAP + metrics.getAscent() + metrics.getDescent();

		final int contentWidth = Math.max(size, labelWidth);
		final int width = contentWidth + padding * 2;
		final int height = size + labelHeight + padding * 2;

		// The gap always sits between the two, so both placements are laid out the same way
		final int imageY = padding + (nameAbove ? labelHeight : 0);
		final int labelBaseline = nameAbove
			? padding + metrics.getAscent()
			: padding + size + LABEL_GAP + metrics.getAscent();

		final Composite originalComposite = graphics.getComposite();
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity()));

		try
		{
			if (showBackground)
			{
				graphics.setColor(config.backgroundColor());
				graphics.fillRect(0, 0, width, height);
				graphics.setColor(BORDER_COLOR);
				graphics.drawRect(0, 0, width - 1, height - 1);
			}

			graphics.drawImage(image, padding + (contentWidth - size) / 2, imageY, null);

			if (labelText != null)
			{
				label.setFont(labelFont);
				label.setText(labelText);
				label.setPosition(new Point(padding + (contentWidth - labelWidth) / 2, labelBaseline));
				label.render(graphics);
			}
		}
		finally
		{
			graphics.setComposite(originalComposite);
		}

		return new Dimension(width, height);
	}

	private float opacity()
	{
		return Math.min(100, Math.max(5, config.overlayOpacity())) / 100f;
	}

}
