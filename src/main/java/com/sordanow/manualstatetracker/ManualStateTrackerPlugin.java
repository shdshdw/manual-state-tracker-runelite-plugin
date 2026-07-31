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

import com.google.inject.Provides;
import com.sordanow.manualstatetracker.data.StateSet;
import com.sordanow.manualstatetracker.data.TrackedState;
import com.sordanow.manualstatetracker.data.TrackerData;
import com.sordanow.manualstatetracker.data.TrackerDataStore;
import com.sordanow.manualstatetracker.image.GameIconCatalogue;
import com.sordanow.manualstatetracker.image.ImageFactory;
import com.sordanow.manualstatetracker.image.StateImageProvider;
import com.sordanow.manualstatetracker.ui.ManualStateTrackerPanel;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarClientID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;

/**
 * Shows a single, freely positionable overlay image that the user switches between themselves with
 * hotkeys.
 *
 * <p>The plugin owns the currently active state; the side panel edits the configuration, the
 * overlay renders the result, and a key listener moves between states. Nothing about the game world
 * is inspected: which state is showing is entirely the user's decision.</p>
 */
@Slf4j
@PluginDescriptor(
	name = "Manual State Tracker",
	description = "Shows a movable overlay image that you switch yourself with hotkeys",
	tags = {"overlay", "image", "icon", "hotkey", "keybind", "manual", "state", "phase", "step", "rotation", "marker", "prayer", "arrow"}
)
public class ManualStateTrackerPlugin extends Plugin
{
	/** Value of {@link #activeStateIndex} meaning "nothing is showing". */
	public static final int NO_STATE = -1;

	/** Size the panel draws its little state previews at. */
	public static final int PREVIEW_SIZE = 24;

	private static final int MIN_OVERLAY_SIZE = 16;
	private static final int MAX_OVERLAY_SIZE = 512;

	@Inject
	private Client client;

	@Inject
	private ManualStateTrackerConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ManualStateTrackerOverlay overlay;

	@Inject
	private KeyManager keyManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private TrackerDataStore dataStore;

	@Inject
	private StateImageProvider imageProvider;

	@Inject
	private GameIconCatalogue iconCatalogue;

	@Inject
	private ManualStateTrackerPanel panel;

	/** Everything the user has configured. Mutated by the panel on the Swing thread. */
	@Getter
	private TrackerData data = new TrackerData();

	/**
	 * Index into the active set's states, or {@link #NO_STATE}. Written from the AWT key dispatch
	 * thread and read by the overlay on the client thread, hence volatile.
	 */
	private volatile int activeStateIndex = NO_STATE;

	private NavigationButton navigationButton;
	private StateKeyListener keyListener;
	private Runnable imageReadyListener;

	@Provides
	ManualStateTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ManualStateTrackerConfig.class);
	}

	@Override
	protected void startUp()
	{
		data = dataStore.load();
		activeStateIndex = NO_STATE;

		panel.init(this);

		navigationButton = NavigationButton.builder()
			.tooltip("Manual State Tracker")
			.icon(ImageFactory.navigationIcon())
			.priority(6)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navigationButton);
		overlayManager.add(overlay);

		keyListener = new StateKeyListener();
		keyManager.registerKeyListener(keyListener);

		// Repaint the panel previews as their images finish loading in the background
		imageReadyListener = () -> SwingUtilities.invokeLater(panel::refreshPreviews);
		imageProvider.addReadyListener(imageReadyListener);

		// Item names are only in the game cache, so indexing them is spread over client ticks
		iconCatalogue.startItemIndex();

		refreshImages();
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		keyManager.unregisterKeyListener(keyListener);
		clientToolbar.removeNavigation(navigationButton);
		imageProvider.removeReadyListener(imageReadyListener);
		imageProvider.clear();
		iconCatalogue.stopItemIndex();

		keyListener = null;
		navigationButton = null;
		imageReadyListener = null;
		activeStateIndex = NO_STATE;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!ManualStateTrackerConfig.GROUP.equals(event.getGroup()))
		{
			return;
		}

		if (ManualStateTrackerConfig.KEY_OVERLAY_SIZE.equals(event.getKey()))
		{
			// Images are cached per size, so the old ones are dead weight
			imageProvider.clear();
			refreshImages();
		}
	}

	/**
	 * @return the state the overlay should draw, or null if none is active
	 */
	@Nullable
	public TrackedState getActiveState()
	{
		final StateSet set = data.getActiveSet();

		if (set == null || activeStateIndex < 0 || activeStateIndex >= set.getStates().size())
		{
			return null;
		}

		return set.getStates().get(activeStateIndex);
	}

	public int getActiveStateIndex()
	{
		return activeStateIndex;
	}

	/** The configured overlay size, clamped to the range the config panel allows. */
	public int getOverlaySize()
	{
		return Math.min(MAX_OVERLAY_SIZE, Math.max(MIN_OVERLAY_SIZE, config.overlaySize()));
	}

	public StateImageProvider getImageProvider()
	{
		return imageProvider;
	}

	/**
	 * Activates a state by index, or {@link #NO_STATE} to hide the overlay. Called both by hotkeys
	 * and by clicking a state in the panel.
	 */
	public void setActiveStateIndex(int index)
	{
		final StateSet set = data.getActiveSet();
		final int clamped = set != null && index >= 0 && index < set.getStates().size() ? index : NO_STATE;

		if (clamped == activeStateIndex)
		{
			return;
		}

		activeStateIndex = clamped;
		log.debug("Active state is now {}", clamped);
		SwingUtilities.invokeLater(panel::refreshActiveState);
	}

	/**
	 * Persists the configuration and brings derived state back in line with it. The panel calls
	 * this after every edit.
	 */
	public void onDataChanged()
	{
		data.normalise();
		dataStore.save(data);

		final StateSet set = data.getActiveSet();

		if (set == null || activeStateIndex >= set.getStates().size())
		{
			activeStateIndex = NO_STATE;
		}

		refreshImages();
	}

	/**
	 * Switches which set the overlay and hotkeys operate on, clearing the active state because
	 * indices do not carry over between sets. The index is clamped, so the panel can call this
	 * straight after adding or removing a set.
	 */
	public void selectSet(int index)
	{
		final int count = data.getSets().size();

		data.setActiveSetIndex(count == 0 ? 0 : Math.max(0, Math.min(index, count - 1)));
		activeStateIndex = NO_STATE;
		onDataChanged();
	}

	/** Schedules the images the panel and overlay need, and drops the ones they no longer do. */
	private void refreshImages()
	{
		final StateSet set = data.getActiveSet();
		final List<TrackedState> states = set == null ? Collections.emptyList() : set.getStates();
		imageProvider.sync(states, getOverlaySize(), PREVIEW_SIZE);
	}

	private void cycle(int direction)
	{
		final StateSet set = data.getActiveSet();

		if (set == null || set.getStates().isEmpty())
		{
			return;
		}

		final int size = set.getStates().size();

		if (activeStateIndex == NO_STATE)
		{
			setActiveStateIndex(direction > 0 ? 0 : size - 1);
		}
		else
		{
			setActiveStateIndex(Math.floorMod(activeStateIndex + direction, size));
		}
	}

	/**
	 * @return true if the key should be left alone because the user is entering text
	 */
	private boolean isTextInputActive()
	{
		// Covers RuneLite's own text fields and in-game search boxes
		if (client.getFocusedInputFieldWidget() != null)
		{
			return true;
		}

		// Covers the "Enter amount" / "Enter name" style modal inputs
		if (client.getVarcIntValue(VarClientID.MESLAYERMODE) != 0)
		{
			return true;
		}

		// Covers a partially typed chat message; the chatbox is always ready to accept input, so
		// there is nothing better to test than whether the user has actually started typing.
		// This only stays accurate because matched hotkeys are consumed and so never land here
		// themselves; see StateKeyListener.
		final String typed = client.getVarcStrValue(VarClientID.CHATINPUT);
		return typed != null && !typed.isEmpty();
	}

	/**
	 * Applies whichever hotkey the event matches.
	 *
	 * @return true if the event matched one of the plugin's hotkeys
	 */
	private boolean handleHotkey(KeyEvent event)
	{
		if (config.clearStateHotkey().matches(event))
		{
			setActiveStateIndex(NO_STATE);
			return true;
		}

		if (config.cycleForwardHotkey().matches(event))
		{
			cycle(1);
			return true;
		}

		if (config.cycleBackwardHotkey().matches(event))
		{
			cycle(-1);
			return true;
		}

		final StateSet set = data.getActiveSet();

		if (set == null)
		{
			return false;
		}

		final List<TrackedState> states = set.getStates();

		for (int i = 0; i < states.size(); i++)
		{
			final TrackedState state = states.get(i);

			if (state.hasKeybind() && state.getKeybind().matches(event))
			{
				// Pressing the hotkey of the state that is already showing hides the overlay
				setActiveStateIndex(activeStateIndex == i ? NO_STATE : i);
				return true;
			}
		}

		return false;
	}

	/**
	 * Listens for the plugin's hotkeys.
	 *
	 * <p>Only the first press of a key is acted on, so holding a key down does not make a toggle
	 * flicker at the OS key repeat rate.</p>
	 *
	 * <p>A key that matched a hotkey is consumed unless the user opted out. That is not just tidy:
	 * an unconsumed printable key goes on to be typed into the chatbox, which would then make
	 * {@link #isTextInputActive()} suppress every following press.</p>
	 */
	private class StateKeyListener implements KeyListener
	{
		private final Set<Integer> heldKeys = new HashSet<>();

		/** Key codes whose press was consumed, so their repeats and release go the same way. */
		private final Set<Integer> consumedKeys = new HashSet<>();

		/**
		 * Whether characters produced by the keyboard still belong to a press this plugin consumed.
		 *
		 * <p>Key typed events carry no key code, so there is nothing to correlate them with except
		 * the press that came before. Matching on the character instead does not work for a dead
		 * key: it produces no character on the press that triggers the hotkey, and then emits the
		 * accumulated accents on a later press, which would otherwise reach the chatbox.</p>
		 */
		private boolean suppressTyped;

		@Override
		public void keyTyped(KeyEvent event)
		{
			if (suppressTyped)
			{
				event.consume();
			}
		}

		@Override
		public void keyPressed(KeyEvent event)
		{
			final int keyCode = event.getKeyCode();

			if (!heldKeys.add(keyCode))
			{
				// Key repeat: do not act again, but keep it away from the game if the first press
				// was consumed
				if (consumedKeys.contains(keyCode))
				{
					event.consume();
				}

				return;
			}

			if (isTextInputActive() || !handleHotkey(event) || config.passHotkeysToGame())
			{
				// This press belongs to the game, and so does anything it goes on to type. Clearing
				// the flag here is safe because a press always arrives before its own typed event.
				suppressTyped = false;
				return;
			}

			event.consume();
			consumedKeys.add(keyCode);
			suppressTyped = true;
		}

		@Override
		public void keyReleased(KeyEvent event)
		{
			final int keyCode = event.getKeyCode();
			heldKeys.remove(keyCode);

			if (consumedKeys.remove(keyCode))
			{
				event.consume();
			}
		}

		@Override
		public void focusLost()
		{
			heldKeys.clear();
			consumedKeys.clear();
			suppressTyped = false;
		}
	}
}
