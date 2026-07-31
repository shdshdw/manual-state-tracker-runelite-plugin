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
package com.sordanow.manualstatetracker.data;

import com.sordanow.manualstatetracker.image.BuiltInImage;
import com.sordanow.manualstatetracker.image.GameIconType;
import java.awt.Color;
import java.awt.event.KeyEvent;
import lombok.Data;
import net.runelite.client.config.Keybind;

/**
 * A single state inside a {@link StateSet}: a name, the picture to show while it is active,
 * and the hotkey that activates it.
 *
 * <p>Instances are persisted as JSON, so every field has a default that {@link #normalise()}
 * restores whenever the stored value is not one this plugin can use.</p>
 */
@Data
public class TrackedState
{
	private static final String DEFAULT_NAME = "New state";
	private static final StateImageSource DEFAULT_IMAGE_SOURCE = StateImageSource.TEXT;
	private static final String DEFAULT_TEXT = "1";
	private static final String NO_FILE = "";
	private static final int DEFAULT_COLOR_RGB = Color.WHITE.getRGB();
	private static final int DEFAULT_OUTLINE_COLOR_RGB = Color.BLACK.getRGB();
	private static final GameIconType DEFAULT_GAME_ICON_TYPE = GameIconType.ITEM;

	/** {@link #gameIconId} value meaning the user has not chosen an icon yet. */
	public static final int NO_GAME_ICON = -1;

	private String name = DEFAULT_NAME;

	private StateImageSource imageSource = DEFAULT_IMAGE_SOURCE;

	/** Free text, used when {@link #imageSource} is {@link StateImageSource#TEXT}. */
	private String text = DEFAULT_TEXT;

	/**
	 * {@link BuiltInImage} enum constant name, used when {@link #imageSource} is
	 * {@link StateImageSource#BUILT_IN}. Stored as a string rather than as the enum itself so an
	 * unrecognised value can be reset instead of failing to parse.
	 */
	private String builtInImage = BuiltInImage.DEFAULT.name();

	/**
	 * File name (not a full path) inside the plugin's image directory, used when
	 * {@link #imageSource} is {@link StateImageSource#CUSTOM_FILE}.
	 */
	private String customFileName = NO_FILE;

	/**
	 * Which kind of game icon is chosen, used when {@link #imageSource} is
	 * {@link StateImageSource#GAME_ICON}.
	 */
	private GameIconType gameIconType = DEFAULT_GAME_ICON_TYPE;

	/** Item id or sprite id of the chosen game icon, or {@link #NO_GAME_ICON} if none is chosen. */
	private int gameIconId = NO_GAME_ICON;

	/**
	 * Name of the chosen game icon. Held alongside the id purely so the panel can label the choice
	 * without the game cache being loaded, since item names only exist once it is.
	 */
	private String gameIconName = "";

	/**
	 * Packed ARGB. Serves as the swatch colour when {@link #imageSource} is
	 * {@link StateImageSource#COLOR} and as the text colour when it is
	 * {@link StateImageSource#TEXT}, so switching between the two keeps the chosen colour.
	 */
	private int colorRgb = DEFAULT_COLOR_RGB;

	/**
	 * Packed ARGB of the outline drawn around the text, used when {@link #imageSource} is
	 * {@link StateImageSource#TEXT}. A fully transparent value leaves the text unoutlined.
	 */
	private int outlineColorRgb = DEFAULT_OUTLINE_COLOR_RGB;

	/**
	 * Hotkey, stored as its two primitive parts because {@link Keybind} has no no-args constructor
	 * and its internal shape is not part of the plugin API.
	 */
	private int keyCode = KeyEvent.VK_UNDEFINED;
	private int keyModifiers = 0;

	/**
	 * Resets every setting this plugin cannot use back to its default, so the rest of the code can
	 * treat a state as well formed no matter where its stored form came from.
	 *
	 * <p>A value this plugin does not recognise, whether it was hand-edited or written by a
	 * different version, deserialises to null for an object field and is therefore caught here.
	 * This has to run before anything switches on {@link #imageSource}.</p>
	 */
	void normalise()
	{
		if (name == null)
		{
			name = DEFAULT_NAME;
		}

		if (imageSource == null)
		{
			imageSource = DEFAULT_IMAGE_SOURCE;
		}

		if (text == null)
		{
			text = DEFAULT_TEXT;
		}

		if (customFileName == null)
		{
			customFileName = NO_FILE;
		}

		if (gameIconType == null)
		{
			gameIconType = DEFAULT_GAME_ICON_TYPE;
		}

		if (gameIconName == null)
		{
			gameIconName = "";
		}

		if (gameIconId < 0)
		{
			gameIconId = NO_GAME_ICON;
		}

		// Resolving and writing the name back replaces an unknown image with the default
		builtInImage = BuiltInImage.byName(builtInImage).name();
	}

	/**
	 * @return the built-in image for this state, or the default if the stored name is unknown
	 */
	public BuiltInImage resolveBuiltInImage()
	{
		return BuiltInImage.byName(builtInImage);
	}

	public Keybind getKeybind()
	{
		return new Keybind(keyCode, keyModifiers);
	}

	public void setKeybind(Keybind keybind)
	{
		final Keybind effective = keybind == null ? Keybind.NOT_SET : keybind;
		keyCode = effective.getKeyCode();
		keyModifiers = effective.getModifiers();
	}

	public boolean hasKeybind()
	{
		return !Keybind.NOT_SET.equals(getKeybind());
	}

	public Color getColor()
	{
		return new Color(colorRgb, true);
	}

	public void setColor(Color color)
	{
		colorRgb = color.getRGB();
	}

	public Color getOutlineColor()
	{
		return new Color(outlineColorRgb, true);
	}

	public void setOutlineColor(Color color)
	{
		outlineColorRgb = color.getRGB();
	}

	/**
	 * Identifies the picture this state draws, ignoring its name and hotkey. Two states that
	 * resolve to the same key can share one cached, scaled image.
	 */
	public String imageCacheKey()
	{
		switch (imageSource)
		{
			case CUSTOM_FILE:
				return "file:" + customFileName;
			case COLOR:
				return "color:" + colorRgb;
			case BUILT_IN:
				return "builtin:" + resolveBuiltInImage().name() + ':' + colorRgb + ':' + outlineColorRgb;
			case GAME_ICON:
				return "gameicon:" + gameIconType + ':' + gameIconId;
			case TEXT:
			default:
				return "text:" + colorRgb + ':' + outlineColorRgb + ':' + text;
		}
	}
}
