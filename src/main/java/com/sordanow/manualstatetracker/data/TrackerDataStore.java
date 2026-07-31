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

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sordanow.manualstatetracker.ManualStateTrackerConfig;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

/**
 * Reads and writes the user's state sets as JSON under a single hidden config key.
 */
@Slf4j
@Singleton
public class TrackerDataStore
{
	private final ConfigManager configManager;
	private final Gson gson;

	@Inject
	private TrackerDataStore(ConfigManager configManager, Gson gson)
	{
		this.configManager = configManager;
		this.gson = gson;
	}

	/**
	 * @return the stored sets, an example set on first run, or the example set again if the stored
	 * JSON turned out to be unreadable
	 */
	public TrackerData load()
	{
		final String json = configManager.getConfiguration(
			ManualStateTrackerConfig.GROUP, ManualStateTrackerConfig.KEY_SETS_DATA);

		if (json == null || json.trim().isEmpty())
		{
			log.debug("No stored state sets, starting from the default set");
			return TrackerData.createDefault();
		}

		try
		{
			final TrackerData data = gson.fromJson(json, TrackerData.class);

			if (data == null)
			{
				return TrackerData.createDefault();
			}

			data.normalise();
			return data;
		}
		catch (JsonParseException e)
		{
			// Keeping the broken value in the config means the user can still recover it by hand
			log.warn("Stored state sets could not be parsed, falling back to the default set", e);
			return TrackerData.createDefault();
		}
	}

	public void save(TrackerData data)
	{
		configManager.setConfiguration(
			ManualStateTrackerConfig.GROUP, ManualStateTrackerConfig.KEY_SETS_DATA, gson.toJson(data));
	}
}
