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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Draws the images the plugin generates itself, and scales the ones it does not.
 *
 * <p>Everything is produced at the exact overlay size rather than drawn once and stretched, so
 * arrows and numbers stay sharp no matter how large the overlay is configured. All results are
 * cached by {@link StateImageProvider}; nothing here runs per frame.</p>
 */
public final class ImageFactory
{
	/** Fraction of the canvas a generated glyph or arrow is allowed to occupy. */
	private static final double CONTENT_FILL = 0.80;

	/**
	 * How many times taller a shape is than its outline. Tuned against a single character, which
	 * fills the canvas vertically and so carries the heaviest outline of anything drawn here.
	 */
	private static final double OUTLINE_HEIGHT_RATIO = 11.2;


	private ImageFactory()
	{
	}

	/**
	 * Scales an image to fit inside a {@code size} by {@code size} canvas, preserving its aspect
	 * ratio and centring it. This is what makes differently sized user PNGs all render at the one
	 * configured overlay size.
	 */
	public static BufferedImage fitToSquare(BufferedImage source, int size)
	{
		final BufferedImage canvas = newCanvas(size);
		final Graphics2D g = createGraphics(canvas);

		try
		{
			final double scale = Math.min(
				(double) size / source.getWidth(),
				(double) size / source.getHeight());

			final int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
			final int height = Math.max(1, (int) Math.round(source.getHeight() * scale));

			g.drawImage(source, (size - width) / 2, (size - height) / 2, width, height, null);
		}
		finally
		{
			g.dispose();
		}

		return canvas;
	}

	/** A flat swatch of the given colour, filling the whole canvas. */
	public static BufferedImage colorSwatch(int size, Color color)
	{
		final BufferedImage canvas = newCanvas(size);
		final Graphics2D g = createGraphics(canvas);

		try
		{
			g.setColor(color);
			g.fillRect(0, 0, size, size);
		}
		finally
		{
			g.dispose();
		}

		return canvas;
	}

	/**
	 * One of the plugin's own shapes, scaled to fill the canvas and centred.
	 *
	 * @param quarterTurns clockwise quarter turns away from the shape's natural orientation
	 * @param outlineColor a fully transparent value leaves the shape unoutlined
	 */
	public static BufferedImage shape(int size, BuiltInImage.Shape shape, int quarterTurns, Color fill,
		Color outlineColor)
	{
		final BufferedImage canvas = newCanvas(size);
		final Graphics2D g = createGraphics(canvas);

		try
		{
			paintOutlined(g, fitted(rotate(unitShape(shape), quarterTurns), size), fill, outlineColor);
		}
		finally
		{
			g.dispose();
		}

		return canvas;
	}

	/**
	 * Text drawn as large as it will fit, centred, in the given colours.
	 *
	 * @param outlineColor colour of the outline around the glyphs; a fully transparent value leaves
	 * the text unoutlined
	 * @return the rendered text, or an empty canvas if the text is blank
	 */
	public static BufferedImage text(int size, String text, Color color, Color outlineColor)
	{
		final BufferedImage canvas = newCanvas(size);

		if (text == null || text.trim().isEmpty())
		{
			return canvas;
		}

		final Graphics2D g = createGraphics(canvas);

		try
		{
			final Shape glyph = centredGlyph(g, text, size);

			if (glyph != null)
			{
				paintOutlined(g, glyph, color, outlineColor);
			}
		}
		finally
		{
			g.dispose();
		}

		return canvas;
	}

	/**
	 * The side panel's toolbar icon: a stack of three bars with the middle one highlighted, echoing
	 * a list of states with one of them active.
	 */
	public static BufferedImage navigationIcon()
	{
		final int size = 16;
		final BufferedImage canvas = newCanvas(size);
		final Graphics2D g = createGraphics(canvas);

		try
		{
			final Color[] barColors = {new Color(0x9F, 0x9F, 0x9F), new Color(0xFF, 0x98, 0x1F), new Color(0x9F, 0x9F, 0x9F)};

			for (int row = 0; row < barColors.length; row++)
			{
				g.setColor(barColors[row]);
				g.fill(new RoundRectangle2D.Double(1.5, 1.5 + row * 5.0, 13.0, 3.5, 2.0, 2.0));
			}
		}
		finally
		{
			g.dispose();
		}

		return canvas;
	}

	/**
	 * A small solid triangle for the panel's reorder buttons. Generated rather than typed as a
	 * unicode arrow so it renders identically whatever fonts the user has installed.
	 */
	public static BufferedImage triangleIcon(int size, boolean pointingUp, Color color)
	{
		final BufferedImage canvas = newCanvas(size);
		final Graphics2D g = createGraphics(canvas);

		try
		{
			final Path2D.Double path = new Path2D.Double();

			if (pointingUp)
			{
				path.moveTo(size / 2.0, size * 0.22);
				path.lineTo(size * 0.85, size * 0.74);
				path.lineTo(size * 0.15, size * 0.74);
			}
			else
			{
				path.moveTo(size / 2.0, size * 0.78);
				path.lineTo(size * 0.85, size * 0.26);
				path.lineTo(size * 0.15, size * 0.26);
			}

			path.closePath();

			g.setColor(color);
			g.fill(path);
		}
		finally
		{
			g.dispose();
		}

		return canvas;
	}

	/** A small cross for the panel's delete buttons. */
	public static BufferedImage crossIcon(int size, Color color)
	{
		final BufferedImage canvas = newCanvas(size);
		final Graphics2D g = createGraphics(canvas);

		try
		{
			final double inset = size * 0.24;

			g.setColor(color);
			g.setStroke(new BasicStroke(Math.max(1.4f, size / 7f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g.draw(new Line2D.Double(inset, inset, size - inset, size - inset));
			g.draw(new Line2D.Double(size - inset, inset, inset, size - inset));
		}
		finally
		{
			g.dispose();
		}

		return canvas;
	}

	/**
	 * Each shape drawn in a 0..1 unit square, to be rotated and fitted onto the canvas afterwards.
	 * Sizes here only matter relative to each other, since {@link #fitted} rescales the result.
	 */
	private static Shape unitShape(BuiltInImage.Shape shape)
	{
		switch (shape)
		{
			case CROSS:
				return rotate(barsShape(), 0.5);
			case PLUS:
				return barsShape();
			case LOOP:
				return loopShape();
			case ARROW:
			default:
				return arrowShape();
		}
	}

	private static Shape arrowShape()
	{
		final Path2D.Double path = new Path2D.Double();
		path.moveTo(0.50, 0.10);
		path.lineTo(0.90, 0.52);
		path.lineTo(0.67, 0.52);
		path.lineTo(0.67, 0.90);
		path.lineTo(0.33, 0.90);
		path.lineTo(0.33, 0.52);
		path.lineTo(0.10, 0.52);
		path.closePath();
		return path;
	}

	/** Two crossed bars, upright as a plus and rotated a half turn of 90 degrees as a cross. */
	private static Shape barsShape()
	{
		final double half = 0.17;
		final double reach = 0.45;
		final double centre = 0.5;

		final Path2D.Double path = new Path2D.Double();
		path.moveTo(centre - half, centre - reach);
		path.lineTo(centre + half, centre - reach);
		path.lineTo(centre + half, centre - half);
		path.lineTo(centre + reach, centre - half);
		path.lineTo(centre + reach, centre + half);
		path.lineTo(centre + half, centre + half);
		path.lineTo(centre + half, centre + reach);
		path.lineTo(centre - half, centre + reach);
		path.lineTo(centre - half, centre + half);
		path.lineTo(centre - reach, centre + half);
		path.lineTo(centre - reach, centre - half);
		path.lineTo(centre - half, centre - half);
		path.closePath();
		return path;
	}

	/** A ring broken by a gap, with an arrowhead on one end, for "repeat" or "back round again". */
	private static Shape loopShape()
	{
		final double centre = 0.5;
		final double radius = 0.33;
		final double thickness = 0.15;
		final double startDegrees = 60.0;

		final Arc2D arc = new Arc2D.Double(
			centre - radius, centre - radius, radius * 2, radius * 2, startDegrees, 285.0, Arc2D.OPEN);

		final Area area = new Area(new BasicStroke((float) thickness, BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER).createStrokedShape(arc));

		// Arrowhead sitting on the arc's starting end, pointing the way the arc is travelled
		final double angle = Math.toRadians(startDegrees);
		final double tipX = centre + radius * Math.cos(angle);
		final double tipY = centre - radius * Math.sin(angle);
		final double alongX = Math.sin(angle);
		final double alongY = Math.cos(angle);
		final double acrossX = -alongY;
		final double acrossY = alongX;
		final double reach = 0.20;
		final double halfWidth = 0.17;

		final Path2D.Double head = new Path2D.Double();
		head.moveTo(tipX + alongX * reach, tipY + alongY * reach);
		head.lineTo(tipX + acrossX * halfWidth, tipY + acrossY * halfWidth);
		head.lineTo(tipX - acrossX * halfWidth, tipY - acrossY * halfWidth);
		head.closePath();

		area.add(new Area(head));
		return area;
	}

	private static Shape rotate(Shape shape, double quarterTurns)
	{
		if (quarterTurns == 0)
		{
			return shape;
		}

		return AffineTransform.getRotateInstance(Math.toRadians(90.0 * quarterTurns), 0.5, 0.5)
			.createTransformedShape(shape);
	}

	/**
	 * Scales a shape about the canvas centre so it fills the canvas without touching the edge.
	 *
	 * <p>Applied to everything drawn here, so a plus, a cross, an arrow and a character all take up
	 * the same amount of the overlay however their raw geometry differs.</p>
	 */
	private static Shape fitted(Shape shape, int size)
	{
		final Rectangle2D bounds = shape.getBounds2D();

		if (bounds.getWidth() <= 0 || bounds.getHeight() <= 0)
		{
			return null;
		}

		final double target = size * CONTENT_FILL;
		final double scale = Math.min(target / bounds.getWidth(), target / bounds.getHeight());

		final AffineTransform transform = new AffineTransform();
		transform.translate(size / 2.0, size / 2.0);
		transform.scale(scale, scale);
		transform.translate(-bounds.getCenterX(), -bounds.getCenterY());
		return transform.createTransformedShape(shape);
	}

	/**
	 * @return the outline of {@code text}, scaled and translated so it is centred in the canvas, or
	 * null if the text produced no glyphs
	 */
	private static Shape centredGlyph(Graphics2D g, String text, int size)
	{
		final Font font = new Font(Font.SANS_SERIF, Font.BOLD, 100);
		return fitted(new TextLayout(text, font, g.getFontRenderContext()).getOutline(null), size);
	}

	/**
	 * Strokes then fills a shape, so it reads clearly against both the light and dark parts of the
	 * game world.
	 */
	private static void paintOutlined(Graphics2D g, Shape shape, Color fill, Color outline)
	{
		g.setStroke(new BasicStroke(outlineWidth(shape), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.setColor(outline);
		g.draw(shape);
		g.setColor(fill);
		g.fill(shape);
	}

	/**
	 * Outline weight, taken from how tall the shape actually ends up rather than from the canvas.
	 *
	 * <p>Longer text is scaled down to fit the overlay, so an outline pinned to the canvas size
	 * would grow heavier relative to the letters the more of them there are. Measuring the shape
	 * keeps the weight looking the same at every length.</p>
	 */
	private static float outlineWidth(Shape shape)
	{
		return (float) Math.max(1.0, shape.getBounds2D().getHeight() / OUTLINE_HEIGHT_RATIO);
	}

	private static BufferedImage newCanvas(int size)
	{
		return new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
	}

	private static Graphics2D createGraphics(BufferedImage image)
	{
		final Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		return g;
	}
}
