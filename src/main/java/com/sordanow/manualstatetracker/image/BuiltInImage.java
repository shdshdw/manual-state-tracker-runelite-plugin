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

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The images the plugin draws itself.
 *
 * <p>They are produced with Java2D by {@link StateImageProvider} at whatever size the overlay is
 * set to, so they stay crisp however large it gets and cost the repository nothing. Icons taken from
 * the game live in {@link GameIcon} instead.</p>
 */
@AllArgsConstructor
@Getter
public enum BuiltInImage
{
	ARROW_UP("Arrow: up", Shape.ARROW, 0),
	ARROW_RIGHT("Arrow: right", Shape.ARROW, 1),
	ARROW_DOWN("Arrow: down", Shape.ARROW, 2),
	ARROW_LEFT("Arrow: left", Shape.ARROW, 3),

	LOOPING_ARROW("Looping arrow", Shape.LOOP, 0),

	CROSS("Cross", Shape.CROSS, 0),
	PLUS("Plus", Shape.PLUS, 0);

	/** Used when a stored image name is not one of these constants. */
	public static final BuiltInImage DEFAULT = ARROW_UP;

	/** Which outline to draw; the shape itself is produced by {@link ImageFactory}. */
	public enum Shape
	{
		ARROW,
		LOOP,
		CROSS,
		PLUS
	}

	private final String displayName;
	private final Shape shape;

	/** Clockwise quarter turns away from the shape's natural orientation. */
	private final int quarterTurns;

	/**
	 * @param name an enum constant name, possibly null or from a newer/older plugin version
	 * @return the matching constant, or {@link #DEFAULT} if there is none
	 */
	public static BuiltInImage byName(String name)
	{
		if (name != null)
		{
			for (BuiltInImage image : values())
			{
				if (image.name().equals(name))
				{
					return image;
				}
			}
		}

		return DEFAULT;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
