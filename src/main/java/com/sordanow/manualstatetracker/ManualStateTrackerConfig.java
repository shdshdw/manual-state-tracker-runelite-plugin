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

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;
import net.runelite.client.ui.overlay.components.ComponentConstants;

/**
 * Global settings for the plugin. The state sets themselves are not here: RuneLite's config panel
 * cannot express nested dynamic lists, so they live in the side panel and are persisted as JSON
 * under the hidden {@link #KEY_SETS_DATA} key.
 */
@ConfigGroup(ManualStateTrackerConfig.GROUP)
public interface ManualStateTrackerConfig extends Config
{
	String GROUP = "manual-state-tracker";

	/** Hidden key holding the serialised {@code TrackerData}. */
	String KEY_SETS_DATA = "setsData";

	String KEY_OVERLAY_SIZE = "overlaySize";

	@ConfigSection(
		name = "Overlay",
		description = "How the state overlay is drawn",
		position = 0
	)
	String overlaySection = "overlaySection";

	@ConfigSection(
		name = "Hotkeys",
		description = "Hotkeys that apply to the whole set; per-state hotkeys are set in the side panel",
		position = 1
	)
	String hotkeySection = "hotkeySection";

	@ConfigItem(
		keyName = KEY_OVERLAY_SIZE,
		name = "Size",
		description = "Width and height of the overlay in pixels. Every image is scaled to fit this box, whatever its own size is.",
		section = overlaySection,
		position = 0
	)
	@Range(min = 16, max = 512)
	@Units(Units.PIXELS)
	default int overlaySize()
	{
		return 64;
	}

	@ConfigItem(
		keyName = "overlayOpacity",
		name = "Opacity",
		description = "How opaque the overlay is drawn",
		section = overlaySection,
		position = 1
	)
	@Range(min = 5, max = 100)
	@Units(Units.PERCENT)
	default int overlayOpacity()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "showBackground",
		name = "Show background",
		description = "Draw a translucent panel and border behind the image",
		section = overlaySection,
		position = 2
	)
	default boolean showBackground()
	{
		return false;
	}

	@ConfigItem(
		keyName = "backgroundColor",
		name = "Background colour",
		description = "Colour of the panel drawn behind the image, when the background is shown",
		section = overlaySection,
		position = 3
	)
	@Alpha
	default Color backgroundColor()
	{
		return ComponentConstants.STANDARD_BACKGROUND_COLOR;
	}

	@ConfigItem(
		keyName = "stateNamePosition",
		name = "State name",
		description = "Whether to draw the name of the active state, and on which side of the image",
		section = overlaySection,
		position = 4
	)
	default StateNamePosition stateNamePosition()
	{
		return StateNamePosition.HIDDEN;
	}

	@ConfigItem(
		keyName = "cycleForwardHotkey",
		name = "Next state",
		description = "Steps forward through the states of the active set, wrapping around at the end",
		section = hotkeySection,
		position = 0
	)
	default Keybind cycleForwardHotkey()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		keyName = "cycleBackwardHotkey",
		name = "Previous state",
		description = "Steps backward through the states of the active set, wrapping around at the start",
		section = hotkeySection,
		position = 1
	)
	default Keybind cycleBackwardHotkey()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		keyName = "clearStateHotkey",
		name = "Hide overlay",
		description = "Clears the active state so the overlay disappears",
		section = hotkeySection,
		position = 2
	)
	default Keybind clearStateHotkey()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		keyName = "passHotkeysToGame",
		name = "Pass hotkeys to the game",
		description = "Let a key that matches a hotkey also reach the game, so one key can do both things."
			+ " Only use this with F-keys or modifier combinations: an ordinary letter or digit would be"
			+ " typed into the chatbox, and hotkeys are ignored while there is text in the chatbox.",
		section = hotkeySection,
		position = 3
	)
	default boolean passHotkeysToGame()
	{
		return false;
	}

	@ConfigItem(
		keyName = KEY_SETS_DATA,
		name = "State sets",
		description = "The state sets, as JSON. Edited through the side panel, not here.",
		hidden = true
	)
	default String setsData()
	{
		return "";
	}
}
