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

import com.sordanow.manualstatetracker.image.GameIconType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import lombok.Data;
import net.runelite.api.gameval.SpriteID;

/**
 * Root of everything the user configures in the side panel. Serialised to a single hidden config
 * key as JSON, because RuneLite's {@code @ConfigItem} system cannot express nested dynamic lists.
 */
@Data
public class TrackerData
{
	private List<StateSet> sets = new ArrayList<>();

	/** Index into {@link #sets} of the set the overlay and hotkeys currently operate on. */
	private int activeSetIndex = 0;

	/**
	 * Resets anything unusable in the stored payload back to a default, so callers never have to
	 * null-check the object graph or guard against values this plugin does not understand.
	 */
	public void normalise()
	{
		if (sets == null)
		{
			sets = new ArrayList<>();
		}

		sets.removeIf(Objects::isNull);
		sets.forEach(StateSet::normalise);

		if (sets.isEmpty())
		{
			activeSetIndex = 0;
		}
		else
		{
			activeSetIndex = Math.max(0, Math.min(activeSetIndex, sets.size() - 1));
		}
	}

	@Nullable
	public StateSet getActiveSet()
	{
		return activeSetIndex >= 0 && activeSetIndex < sets.size() ? sets.get(activeSetIndex) : null;
	}

	/**
	 * @return a data object holding one example set, used the first time the plugin starts so the
	 * panel is not empty
	 */
	public static TrackerData createDefault()
	{
		final StateSet set = new StateSet();
		set.setName("Protection prayers");
		set.getStates().add(prayerState("Melee", SpriteID.Prayeron.PROTECT_FROM_MELEE, "Protect from Melee"));
		set.getStates().add(prayerState("Ranged", SpriteID.Prayeron.PROTECT_FROM_MISSILES, "Protect from Missiles"));
		set.getStates().add(prayerState("Magic", SpriteID.Prayeron.PROTECT_FROM_MAGIC, "Protect from Magic"));

		final TrackerData data = new TrackerData();
		data.getSets().add(set);
		return data;
	}

	private static TrackedState prayerState(String name, int spriteId, String iconName)
	{
		final TrackedState state = new TrackedState();
		state.setName(name);
		state.setImageSource(StateImageSource.GAME_ICON);
		state.setGameIconType(GameIconType.PRAYER);
		state.setGameIconId(spriteId);
		state.setGameIconName(iconName);
		return state;
	}
}
