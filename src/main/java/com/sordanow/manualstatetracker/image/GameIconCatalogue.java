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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.client.callback.ClientThread;

/**
 * The searchable set of game icons: the fixed prayer and spell tables, plus every item in the game
 * cache.
 *
 * <p>Item names only exist in the cache, so they have to be read on the client thread. Reading tens
 * of thousands of them in one go would stall a frame, so {@link #startItemIndex()} walks the item
 * ids a slice per client tick and publishes the finished index in one go. Searching before it
 * finishes simply returns no items rather than blocking.</p>
 */
@Slf4j
@Singleton
public class GameIconCatalogue
{
	/** Item ids read per client tick. Small enough to be invisible, large enough to finish quickly. */
	private static final int SCAN_CHUNK = 2000;

	/** Shortest query that searches items, which are far too numerous to match on one character. */
	private static final int MIN_ITEM_QUERY_LENGTH = 2;

	private final Client client;
	private final ClientThread clientThread;

	/**
	 * Item ids and names as parallel arrays rather than objects. There are tens of thousands of
	 * them and only the handful that match a search need to become a {@link GameIcon}.
	 */
	private volatile int[] itemIds = new int[0];
	private volatile String[] itemNames = new String[0];

	private boolean scanScheduled;

	/** Set while a queued scan should abandon itself, so it does not outlive the plugin. */
	private volatile boolean scanCancelled;

	@Inject
	private GameIconCatalogue(Client client, ClientThread clientThread)
	{
		this.client = client;
		this.clientThread = clientThread;
	}

	/**
	 * Begins indexing item names if that has not already happened. Safe to call repeatedly and from
	 * any thread.
	 */
	public synchronized void startItemIndex()
	{
		if (scanScheduled)
		{
			return;
		}

		scanScheduled = true;
		scanCancelled = false;
		clientThread.invokeLater(new ItemIndexScan());
	}

	/**
	 * Abandons an unfinished scan so it does not keep running once the plugin is disabled. A later
	 * {@link #startItemIndex()} begins again; a scan that already finished is left alone, since its
	 * index stays valid.
	 */
	public synchronized void stopItemIndex()
	{
		if (isItemIndexReady())
		{
			return;
		}

		scanCancelled = true;
		scanScheduled = false;
	}

	/** Whether item names are available yet. Prayers and spells are always available. */
	public boolean isItemIndexReady()
	{
		return itemIds.length > 0;
	}

	/**
	 * Finds icons whose name contains the query, best matches first.
	 *
	 * <p>An empty query lists every prayer and spell in spellbook order, since there are few enough
	 * of them to browse. Items are left out until something is typed; there are tens of thousands
	 * and an unfiltered list would be useless.</p>
	 *
	 * @param type restrict to one kind of icon, or null for all of them
	 * @param limit maximum number of results
	 */
	public List<GameIcon> search(String query, @Nullable GameIconType type, int limit)
	{
		final String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
		final boolean browsing = needle.isEmpty();
		final List<GameIcon> matches = new ArrayList<>();

		for (GameIcon icon : GameIconTables.all())
		{
			if (matchesType(icon.getType(), type) && (browsing || containsIgnoreCase(icon.getName(), needle)))
			{
				matches.add(icon);
			}
		}

		if (!browsing && matchesType(GameIconType.ITEM, type) && needle.length() >= MIN_ITEM_QUERY_LENGTH)
		{
			// Read both arrays once; the scan publishes them together but it could finish mid-search
			final int[] ids = itemIds;
			final String[] names = itemNames;

			for (int i = 0; i < Math.min(ids.length, names.length); i++)
			{
				if (containsIgnoreCase(names[i], needle))
				{
					matches.add(new GameIcon(GameIconType.ITEM, ids[i], names[i]));
				}
			}
		}

		if (!browsing)
		{
			// While browsing, the tables' own order groups prayers and each spellbook together,
			// which reads better than any ranking
			matches.sort(relevance(needle));
		}

		return matches.size() > limit ? matches.subList(0, limit) : matches;
	}

	/**
	 * Exact matches first, then names starting with the query, then everything else alphabetically.
	 * Puts "Fire" above "Fire giant" above "Ring of fire" when searching for "fire".
	 */
	private static Comparator<GameIcon> relevance(String needle)
	{
		return Comparator
			.comparingInt((GameIcon icon) -> rank(icon.getName(), needle))
			.thenComparingInt(icon -> icon.getName().length())
			.thenComparing(GameIcon::getName, String.CASE_INSENSITIVE_ORDER);
	}

	private static int rank(String name, String needle)
	{
		if (name.equalsIgnoreCase(needle))
		{
			return 0;
		}

		return name.regionMatches(true, 0, needle, 0, needle.length()) ? 1 : 2;
	}

	private static boolean matchesType(GameIconType candidate, @Nullable GameIconType filter)
	{
		return filter == null || filter == candidate;
	}

	/** Allocation-free case-insensitive containment, given an already lowercased needle. */
	private static boolean containsIgnoreCase(String haystack, String needle)
	{
		final int last = haystack.length() - needle.length();

		for (int offset = 0; offset <= last; offset++)
		{
			if (haystack.regionMatches(true, offset, needle, 0, needle.length()))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Walks every item id a slice at a time. Returning false leaves the task queued, so the client
	 * runs the next slice on its next tick.
	 */
	private class ItemIndexScan implements BooleanSupplier
	{
		/**
		 * Best id found so far for each lowercased name. Alternate game modes reuse a name for their
		 * own copy of an item, so the cache holds several ids that all render the same picture. The
		 * Armadyl godsword, for instance, exists as the real item plus beta and Deadman variants.
		 */
		private final Map<String, Candidate> byName = new LinkedHashMap<>();

		private int nextId;

		@Override
		public boolean getAsBoolean()
		{
			if (scanCancelled)
			{
				// Returning true drops this task from the client's queue
				return true;
			}

			if (client.getGameState().getState() < GameState.LOGIN_SCREEN.getState())
			{
				return false;
			}

			final int total = client.getItemCount();

			if (total <= 0)
			{
				return false;
			}

			final int end = Math.min(total, nextId + SCAN_CHUNK);

			for (; nextId < end; nextId++)
			{
				collect(nextId);
			}

			if (nextId < total)
			{
				return false;
			}

			publish();
			return true;
		}

		private void collect(int itemId)
		{
			final ItemComposition composition = client.getItemDefinition(itemId);

			// A noted or placeholder item shows the note or placeholder graphic and repeats a name
			// that is already in the index from the real item
			if (composition.getNote() != -1 || composition.getPlaceholderTemplateId() != -1)
			{
				return;
			}

			final String name = composition.getName();

			if (name == null || name.isEmpty() || "null".equals(name))
			{
				return;
			}

			final Candidate candidate = new Candidate(itemId, name, composition.isTradeable());
			byName.merge(name.toLowerCase(Locale.ROOT), candidate, Candidate::preferred);
		}

		private void publish()
		{
			final int count = byName.size();
			final int[] publishedIds = new int[count];
			final String[] publishedNames = new String[count];

			int index = 0;

			for (Candidate candidate : byName.values())
			{
				publishedIds[index] = candidate.id;
				publishedNames[index] = candidate.name;
				index++;
			}

			itemNames = publishedNames;
			itemIds = publishedIds;

			log.debug("Indexed {} item icons from {} item ids", count, nextId);
		}
	}

	/** One item id competing to represent its name in the index. */
	private static class Candidate
	{
		private final int id;
		private final String name;
		private final boolean tradeable;

		private Candidate(int id, String name, boolean tradeable)
		{
			this.id = id;
			this.name = name;
			this.tradeable = tradeable;
		}

		/**
		 * Picks which of two same-named items to keep: the tradeable one, since game mode copies are
		 * untradeable, and otherwise the lower id, since those copies were added later.
		 */
		private static Candidate preferred(Candidate existing, Candidate replacement)
		{
			if (existing.tradeable != replacement.tradeable)
			{
				return existing.tradeable ? existing : replacement;
			}

			return existing.id <= replacement.id ? existing : replacement;
		}
	}
}
