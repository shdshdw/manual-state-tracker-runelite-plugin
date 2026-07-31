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
package com.sordanow.manualstatetracker.ui;

import com.sordanow.manualstatetracker.ManualStateTrackerPlugin;
import com.sordanow.manualstatetracker.data.StateSet;
import com.sordanow.manualstatetracker.data.TrackedState;
import com.sordanow.manualstatetracker.data.TrackerData;
import com.sordanow.manualstatetracker.image.CustomImageStore;
import com.sordanow.manualstatetracker.image.GameIconCatalogue;
import com.sordanow.manualstatetracker.image.GameIconLoader;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.util.LinkBrowser;

/**
 * The plugin's side panel: pick the active state set, edit the sets themselves, and edit the states
 * inside the active set.
 *
 * <p>The panel is the only place state sets can be edited, because RuneLite's config panel cannot
 * express a dynamic list of sets each containing a dynamic list of states. Every edit is written
 * straight through to the plugin, which persists it.</p>
 *
 * <p>All methods run on the Swing thread unless noted; disk work is handed to the executor.</p>
 */
@Singleton
public class ManualStateTrackerPanel extends PluginPanel
{
	private final CustomImageStore imageStore;
	private final ScheduledExecutorService executor;
	private final GameIconCatalogue iconCatalogue;
	private final GameIconLoader iconLoader;

	@Getter
	private final ColorPickerManager colorPickerManager;

	/** Set once by {@link #init}; the plugin cannot be injected here because it injects this panel. */
	private ManualStateTrackerPlugin plugin;

	private final JComboBox<String> setSelector = new JComboBox<>();
	private final JPanel statesContainer = new JPanel(new DynamicGridLayout(0, 1, 0, 6));
	private final List<StateEditorPanel> editors = new ArrayList<>();
	private final JButton addStateButton = new JButton("Add state");

	/** PNGs found in the plugin's image folder, refreshed off the Swing thread. */
	private List<String> customImageNames = Collections.emptyList();

	/** Suppresses the selector's action listener while its items are being replaced. */
	private boolean populatingSelector;

	/**
	 * Pixels the wheel moves per unit. A scroll bar defaults to one pixel per unit, or three pixels
	 * a notch, which crawls once a set holds more than a few states. State rows are around a
	 * hundred pixels tall, so this puts a row within a couple of notches.
	 */
	private static final int SCROLL_UNIT_INCREMENT = 24;

	@Inject
	private ManualStateTrackerPanel(CustomImageStore imageStore, ColorPickerManager colorPickerManager,
		ScheduledExecutorService executor, GameIconCatalogue iconCatalogue, GameIconLoader iconLoader)
	{
		this.imageStore = imageStore;
		this.colorPickerManager = colorPickerManager;
		this.executor = executor;
		this.iconCatalogue = iconCatalogue;
		this.iconLoader = iconLoader;

		add(buildTitle());
		add(buildSetControls());
		add(statesContainer);
		add(addStateButton);
		add(buildFolderHint());

		statesContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

		addStateButton.setToolTipText("Add a new state to the selected set");
		addStateButton.addActionListener(event -> addState());

		// Set here rather than relying on the look and feel, which only speeds the wheel up when its
		// own scroll bar delegate is installed
		getScrollPane().getVerticalScrollBar().setUnitIncrement(SCROLL_UNIT_INCREMENT);
	}

	/**
	 * Connects the panel to the plugin. Safe to call from the client thread.
	 */
	public void init(ManualStateTrackerPlugin plugin)
	{
		this.plugin = plugin;
		SwingUtilities.invokeLater(this::rebuild);
		reloadCustomImages();
	}

	/** Redraws the state previews, for example once a background image load completes. */
	public void refreshPreviews()
	{
		editors.forEach(StateEditorPanel::refreshPreview);
	}

	/** Moves the "currently showing" highlight to whichever state is now active. */
	public void refreshActiveState()
	{
		final int active = plugin == null ? ManualStateTrackerPlugin.NO_STATE : plugin.getActiveStateIndex();

		for (int i = 0; i < editors.size(); i++)
		{
			editors.get(i).setActive(i == active);
		}

		repaint();
	}

	// ------------------------------------------------------------------
	// Called by StateEditorPanel
	// ------------------------------------------------------------------

	/** Persists an in-place edit to a state, without rebuilding the editors. */
	void stateChanged()
	{
		if (plugin == null)
		{
			return;
		}

		plugin.onDataChanged();
		refreshPreviews();
	}

	void moveState(int index, int delta)
	{
		final List<TrackedState> states = activeStates();
		final int target = index + delta;

		if (states == null || index < 0 || index >= states.size() || target < 0 || target >= states.size())
		{
			return;
		}

		Collections.swap(states, index, target);

		// Keep the overlay showing the same state it was showing before the reorder
		final int active = plugin.getActiveStateIndex();

		if (active == index)
		{
			plugin.setActiveStateIndex(target);
		}
		else if (active == target)
		{
			plugin.setActiveStateIndex(index);
		}

		plugin.onDataChanged();
		rebuild();
	}

	void deleteState(int index)
	{
		final List<TrackedState> states = activeStates();

		if (states == null || index < 0 || index >= states.size())
		{
			return;
		}

		final int choice = JOptionPane.showConfirmDialog(this,
			"Delete the state \"" + states.get(index).getName() + "\"?",
			"Delete state", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		if (choice != JOptionPane.YES_OPTION)
		{
			return;
		}

		states.remove(index);

		final int active = plugin.getActiveStateIndex();

		if (active == index)
		{
			plugin.setActiveStateIndex(ManualStateTrackerPlugin.NO_STATE);
		}
		else if (active > index)
		{
			plugin.setActiveStateIndex(active - 1);
		}

		plugin.onDataChanged();
		rebuild();
	}

	/** Shows a state on the overlay, or hides the overlay if it was already showing. */
	void activateState(int index)
	{
		if (plugin == null)
		{
			return;
		}

		plugin.setActiveStateIndex(
			plugin.getActiveStateIndex() == index ? ManualStateTrackerPlugin.NO_STATE : index);
	}

	List<String> getCustomImageNames()
	{
		return customImageNames;
	}

	@Nullable
	BufferedImage getPreview(TrackedState state)
	{
		return plugin == null ? null : plugin.getImageProvider().get(state, ManualStateTrackerPlugin.PREVIEW_SIZE);
	}

	/** Re-reads the plugin's image folder on the executor, then rebuilds the editors. */
	void reloadCustomImages()
	{
		executor.execute(() ->
		{
			imageStore.ensureImageDirectory();
			final List<String> names = imageStore.listImageNames();

			SwingUtilities.invokeLater(() ->
			{
				customImageNames = names;
				rebuild();
			});
		});
	}

	/**
	 * Opens the icon search dialog and assigns whatever the user picks to the state.
	 *
	 * @param editor the row that asked, so only its label needs rebuilding
	 */
	void chooseGameIcon(TrackedState state, StateEditorPanel editor)
	{
		final GameIconChooser chooser = new GameIconChooser(
			SwingUtilities.getWindowAncestor(this), iconCatalogue, iconLoader, state.getGameIconType(),
			icon ->
			{
				state.setGameIconType(icon.getType());
				state.setGameIconId(icon.getId());
				state.setGameIconName(icon.getName());
				plugin.onDataChanged();
				editor.refreshValueControl();
				refreshPreviews();
			});

		chooser.setVisible(true);
	}

	/** Asks the user for a PNG, copies it into the plugin's image folder and assigns it. */
	void importImageFor(TrackedState state)
	{
		final JFileChooser chooser = new JFileChooser(imageStore.getImageDirectory().toFile());
		chooser.setDialogTitle("Import a PNG");
		chooser.setFileFilter(new FileNameExtensionFilter("PNG images", "png"));
		chooser.setAcceptAllFileFilterUsed(false);

		if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
		{
			return;
		}

		final File source = chooser.getSelectedFile();

		executor.execute(() ->
		{
			final String importedName = imageStore.importImage(source);
			final List<String> names = imageStore.listImageNames();

			SwingUtilities.invokeLater(() ->
			{
				customImageNames = names;

				if (importedName == null)
				{
					JOptionPane.showMessageDialog(this,
						"Could not copy that file into the plugin's image folder.",
						"Import failed", JOptionPane.ERROR_MESSAGE);
					return;
				}

				state.setCustomFileName(importedName);
				plugin.onDataChanged();
				rebuild();
			});
		});
	}

	// ------------------------------------------------------------------
	// Set management
	// ------------------------------------------------------------------

	private void createSet()
	{
		final String name = askForName("Name for the new set:", "New state set", "");

		if (name == null)
		{
			return;
		}

		final TrackerData data = plugin.getData();
		final StateSet set = new StateSet();
		set.setName(name);
		data.getSets().add(set);

		plugin.selectSet(data.getSets().size() - 1);
		rebuild();
	}

	private void renameSet()
	{
		final StateSet set = plugin.getData().getActiveSet();

		if (set == null)
		{
			return;
		}

		final String name = askForName("New name for this set:", "Rename state set", set.getName());

		if (name == null)
		{
			return;
		}

		set.setName(name);
		plugin.onDataChanged();
		rebuild();
	}

	private void deleteSet()
	{
		final TrackerData data = plugin.getData();
		final StateSet set = data.getActiveSet();

		if (set == null)
		{
			return;
		}

		final int choice = JOptionPane.showConfirmDialog(this,
			"Delete the set \"" + set.getName() + "\" and all of its states?",
			"Delete state set", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		if (choice != JOptionPane.YES_OPTION)
		{
			return;
		}

		final int index = data.getActiveSetIndex();
		data.getSets().remove(index);

		plugin.selectSet(index);
		rebuild();
	}

	private void addState()
	{
		final List<TrackedState> states = activeStates();

		if (states == null)
		{
			JOptionPane.showMessageDialog(this, "Create a state set first.", "No set selected",
				JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		states.add(new TrackedState());
		plugin.onDataChanged();
		rebuild();
	}

	// ------------------------------------------------------------------
	// Building
	// ------------------------------------------------------------------

	private void rebuild()
	{
		if (plugin == null)
		{
			return;
		}

		rebuildSetSelector();
		rebuildStateEditors();
	}

	private void rebuildSetSelector()
	{
		final TrackerData data = plugin.getData();

		populatingSelector = true;

		try
		{
			setSelector.removeAllItems();
			data.getSets().forEach(set -> setSelector.addItem(set.getName()));

			if (!data.getSets().isEmpty())
			{
				setSelector.setSelectedIndex(data.getActiveSetIndex());
			}
		}
		finally
		{
			populatingSelector = false;
		}

		setSelector.setEnabled(!data.getSets().isEmpty());
	}

	private void rebuildStateEditors()
	{
		statesContainer.removeAll();
		editors.clear();

		final StateSet set = plugin.getData().getActiveSet();

		if (set == null)
		{
			statesContainer.add(hint("Create a state set to get started."));
		}
		else if (set.getStates().isEmpty())
		{
			statesContainer.add(hint("This set has no states yet."));
		}
		else
		{
			final List<TrackedState> states = set.getStates();
			final int active = plugin.getActiveStateIndex();

			for (int i = 0; i < states.size(); i++)
			{
				final StateEditorPanel editor = new StateEditorPanel(this, states.get(i), i, states.size(), i == active);
				editors.add(editor);
				statesContainer.add(editor);
			}
		}

		addStateButton.setEnabled(set != null);

		statesContainer.revalidate();
		statesContainer.repaint();
	}

	private JLabel buildTitle()
	{
		final JLabel title = new JLabel("Manual State Tracker");
		title.setFont(title.getFont().deriveFont(Font.BOLD));
		title.setForeground(ColorScheme.BRAND_ORANGE);
		return title;
	}

	private JPanel buildSetControls()
	{
		final JPanel panel = new JPanel(new DynamicGridLayout(0, 1, 0, 4));
		panel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		final JLabel label = new JLabel("Active state set");
		label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		panel.add(label);

		setSelector.setToolTipText("The set the overlay and hotkeys use");
		setSelector.addActionListener(event ->
		{
			if (populatingSelector || plugin == null)
			{
				return;
			}

			final int index = setSelector.getSelectedIndex();

			if (index >= 0 && index != plugin.getData().getActiveSetIndex())
			{
				plugin.selectSet(index);
				rebuildStateEditors();
			}
		});
		panel.add(setSelector);

		final JPanel buttons = new JPanel(new GridLayout(1, 3, 4, 0));
		buttons.setBackground(ColorScheme.DARK_GRAY_COLOR);
		buttons.add(button("New", "Create a new state set", this::createSet));
		buttons.add(button("Rename", "Rename the selected state set", this::renameSet));
		buttons.add(button("Delete", "Delete the selected state set", this::deleteSet));
		panel.add(buttons);

		return panel;
	}

	private JPanel buildFolderHint()
	{
		final JPanel panel = new JPanel(new DynamicGridLayout(0, 1, 0, 4));
		panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

		final JLabel hint = hint("Custom images are PNGs in the plugin's image folder. Overlay size, opacity and the cycle hotkeys live in the plugin's config.");
		panel.add(hint);

		final JButton openFolder = button("Open image folder", imageStore.getImageDirectory().toString(), () ->
			executor.execute(() ->
			{
				final java.nio.file.Path directory = imageStore.ensureImageDirectory();

				if (directory != null)
				{
					LinkBrowser.open(directory.toString());
				}
			}));
		panel.add(openFolder);

		return panel;
	}

	private static JButton button(String text, String tooltip, Runnable action)
	{
		final JButton button = new JButton(text);
		button.setToolTipText(tooltip);
		button.setFocusable(false);
		button.addActionListener(event -> action.run());
		return button;
	}

	private static JLabel hint(String text)
	{
		final JLabel label = new JLabel("<html><body style='width:170px'>" + text + "</body></html>");
		label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		return label;
	}

	/**
	 * @return a trimmed, non-empty name, or null if the user cancelled or entered only whitespace
	 */
	@Nullable
	private String askForName(String message, String title, String initialValue)
	{
		final Object answer = JOptionPane.showInputDialog(this, message, title,
			JOptionPane.PLAIN_MESSAGE, null, null, initialValue);

		if (answer == null)
		{
			return null;
		}

		final String name = answer.toString().trim();
		return name.isEmpty() ? null : name;
	}

	@Nullable
	private List<TrackedState> activeStates()
	{
		if (plugin == null)
		{
			return null;
		}

		final StateSet set = plugin.getData().getActiveSet();
		return set == null ? null : set.getStates();
	}
}
