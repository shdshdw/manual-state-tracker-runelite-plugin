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
import com.sordanow.manualstatetracker.data.StateImageSource;
import com.sordanow.manualstatetracker.data.TrackedState;
import com.sordanow.manualstatetracker.image.BuiltInImage;
import com.sordanow.manualstatetracker.image.ImageFactory;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.components.ColorJButton;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;

/**
 * Editor for one {@link TrackedState}: its name, the image it shows, its hotkey, and the buttons
 * that reorder or delete it.
 */
class StateEditorPanel extends JPanel
{
	private static final int PREVIEW_SIZE = ManualStateTrackerPlugin.PREVIEW_SIZE;
	private static final int ICON_SIZE = 10;
	private static final int LABEL_WIDTH = 48;
	private static final int ACTIVE_STRIPE_WIDTH = 3;

	private static final Icon UP_ICON = new ImageIcon(ImageFactory.triangleIcon(ICON_SIZE, true, ColorScheme.LIGHT_GRAY_COLOR));
	private static final Icon DOWN_ICON = new ImageIcon(ImageFactory.triangleIcon(ICON_SIZE, false, ColorScheme.LIGHT_GRAY_COLOR));
	private static final Icon DELETE_ICON = new ImageIcon(ImageFactory.crossIcon(ICON_SIZE, ColorScheme.PROGRESS_ERROR_COLOR));

	private final ManualStateTrackerPanel parent;
	private final TrackedState state;
	private final int index;

	private final JLabel previewLabel = new JLabel();

	/** Holds whichever control the currently selected image source needs. */
	private final JPanel valueHolder = new JPanel(new BorderLayout());

	StateEditorPanel(ManualStateTrackerPanel parent, TrackedState state, int index, int stateCount, boolean active)
	{
		this.parent = parent;
		this.state = state;
		this.index = index;

		setLayout(new DynamicGridLayout(0, 1, 0, 4));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setActive(active);

		valueHolder.setOpaque(false);

		add(buildHeader(stateCount));
		add(labelled("Image", buildSourceSelector()));
		add(labelled("", valueHolder));
		add(labelled("Hotkey", buildHotkeyButton()));

		rebuildValueControl();
		refreshPreview();
	}

	/** Highlights this state while it is the one the overlay is showing. */
	void setActive(boolean active)
	{
		setBorder(new CompoundBorder(
			new MatteBorder(0, ACTIVE_STRIPE_WIDTH, 0, 0,
				active ? ColorScheme.BRAND_ORANGE : ColorScheme.DARKER_GRAY_COLOR),
			BorderFactory.createEmptyBorder(6, 6, 6, 6)));
	}

	/** Picks up an image that finished loading after this editor was built. */
	void refreshPreview()
	{
		final BufferedImage preview = parent.getPreview(state);
		previewLabel.setIcon(preview == null ? null : new ImageIcon(preview));
	}

	private JPanel buildHeader(int stateCount)
	{
		final JPanel header = new JPanel(new BorderLayout(4, 0));
		header.setOpaque(false);

		previewLabel.setPreferredSize(new Dimension(PREVIEW_SIZE, PREVIEW_SIZE));
		previewLabel.setToolTipText("Show this state on the overlay now");
		previewLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		previewLabel.setBorder(BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR));
		previewLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent event)
			{
				parent.activateState(index);
			}
		});
		header.add(previewLabel, BorderLayout.WEST);

		final JTextField nameField = new JTextField(state.getName());
		nameField.setToolTipText("Name of this state");
		bindTextField(nameField, state::setName);
		header.add(nameField, BorderLayout.CENTER);

		final JPanel actions = new JPanel(new GridLayout(1, 3, 2, 0));
		actions.setOpaque(false);
		actions.add(iconButton(UP_ICON, "Move up", index > 0, () -> parent.moveState(index, -1)));
		actions.add(iconButton(DOWN_ICON, "Move down", index < stateCount - 1, () -> parent.moveState(index, 1)));
		actions.add(iconButton(DELETE_ICON, "Delete this state", true, () -> parent.deleteState(index)));
		header.add(actions, BorderLayout.EAST);

		return header;
	}

	private JComboBox<StateImageSource> buildSourceSelector()
	{
		final JComboBox<StateImageSource> selector = new JComboBox<>(StateImageSource.values());
		selector.setSelectedItem(state.getImageSource());
		selector.setToolTipText("Where the image for this state comes from");
		selector.addActionListener(event ->
		{
			final StateImageSource selected = (StateImageSource) selector.getSelectedItem();

			if (selected == null || selected == state.getImageSource())
			{
				return;
			}

			state.setImageSource(selected);
			rebuildValueControl();
			parent.stateChanged();
		});

		return selector;
	}

	private KeybindCaptureButton buildHotkeyButton()
	{
		return new KeybindCaptureButton(state.getKeybind(), keybind ->
		{
			state.setKeybind(keybind);
			parent.stateChanged();
		});
	}

	/** Redraws the control under "Image", for example after the icon chooser changed the choice. */
	void refreshValueControl()
	{
		rebuildValueControl();
	}

	/** Swaps the control under "Image" to match the selected image source. */
	private void rebuildValueControl()
	{
		valueHolder.removeAll();

		switch (state.getImageSource())
		{
			case CUSTOM_FILE:
				valueHolder.add(buildCustomFileControl(), BorderLayout.CENTER);
				break;

			case COLOR:
				valueHolder.add(
					buildColorButton("Pick colour", "Fill the overlay with a flat colour for this state",
						state::getColor, state::setColor),
					BorderLayout.CENTER);
				break;

			case GAME_ICON:
				valueHolder.add(buildGameIconControl(), BorderLayout.CENTER);
				break;

			case BUILT_IN:
				valueHolder.add(buildBuiltInControl(), BorderLayout.CENTER);
				break;

			case TEXT:
			default:
				valueHolder.add(buildTextControl(), BorderLayout.CENTER);
				break;
		}

		valueHolder.revalidate();
		valueHolder.repaint();
	}

	private JPanel buildBuiltInControl()
	{
		final JPanel panel = new JPanel(new DynamicGridLayout(0, 1, 0, 3));
		panel.setOpaque(false);

		final JComboBox<BuiltInImage> selector = new JComboBox<>(BuiltInImage.values());
		selector.setSelectedItem(state.resolveBuiltInImage());
		selector.addActionListener(event ->
		{
			final BuiltInImage selected = (BuiltInImage) selector.getSelectedItem();

			if (selected != null && !selected.name().equals(state.getBuiltInImage()))
			{
				state.setBuiltInImage(selected.name());
				parent.stateChanged();
			}
		});
		panel.add(selector);

		// The same two colours the text source uses, so the shapes recolour the same way
		panel.add(buildColorButton("Shape colour", "Colour the shape is filled with",
			state::getColor, state::setColor));

		panel.add(buildColorButton("Outline colour", "Colour of the outline around the shape."
			+ " Set it fully transparent to draw no outline.",
			state::getOutlineColor, state::setOutlineColor));

		return panel;
	}

	private JPanel buildCustomFileControl()
	{
		final JPanel panel = new JPanel(new DynamicGridLayout(0, 1, 0, 3));
		panel.setOpaque(false);

		final List<String> fileNames = parent.getCustomImageNames();
		final JComboBox<String> selector = new JComboBox<>(fileNames.toArray(new String[0]));
		selector.setSelectedItem(state.getCustomFileName());

		if (fileNames.isEmpty())
		{
			selector.setEnabled(false);
			selector.setToolTipText("No PNGs found. Import one, or drop files into the plugin's image folder.");
		}
		else
		{
			selector.setToolTipText("PNG from the plugin's image folder");
		}

		selector.addActionListener(event ->
		{
			final String selected = (String) selector.getSelectedItem();

			if (selected != null && !selected.equals(state.getCustomFileName()))
			{
				state.setCustomFileName(selected);
				parent.stateChanged();
			}
		});
		panel.add(selector);

		final JPanel buttons = new JPanel(new GridLayout(1, 2, 3, 0));
		buttons.setOpaque(false);

		final JButton importButton = new JButton("Import...");
		importButton.setToolTipText("Copy a PNG into the plugin's image folder and use it for this state");
		importButton.addActionListener(event -> parent.importImageFor(state));
		buttons.add(importButton);

		final JButton refreshButton = new JButton("Refresh");
		refreshButton.setToolTipText("Re-read the plugin's image folder");
		refreshButton.addActionListener(event -> parent.reloadCustomImages());
		buttons.add(refreshButton);

		panel.add(buttons);
		return panel;
	}

	/**
	 * A button labelled with the chosen icon, which opens the search dialog. Kept to a single button
	 * so a set with many states stays compact in the side panel.
	 */
	private JButton buildGameIconControl()
	{
		final boolean chosen = state.getGameIconId() != TrackedState.NO_GAME_ICON
			&& !state.getGameIconName().isEmpty();

		final JButton button = new JButton(chosen ? state.getGameIconName() : "Choose icon...");
		button.setToolTipText(chosen
			? state.getGameIconName() + " (" + state.getGameIconType() + "). Click to change."
			: "Search the game's items, prayers and spells");
		button.setFocusable(false);
		button.addActionListener(event -> parent.chooseGameIcon(state, this));

		return button;
	}

	private JPanel buildTextControl()
	{
		final JPanel panel = new JPanel(new DynamicGridLayout(0, 1, 0, 3));
		panel.setOpaque(false);

		final JTextField textField = new JTextField(state.getText());
		textField.setToolTipText("Text to show. It is scaled to fill the overlay, so short text is largest.");
		bindTextField(textField, state::setText);
		panel.add(textField);

		panel.add(buildColorButton("Text colour", "Colour the text is drawn in",
			state::getColor, state::setColor));

		panel.add(buildColorButton("Outline colour", "Colour of the outline around the text."
			+ " Set it fully transparent to draw no outline.",
			state::getOutlineColor, state::setOutlineColor));

		return panel;
	}

	/**
	 * A swatch button that opens the RuneLite colour picker for one of the state's colours.
	 *
	 * @param label doubles as the title of the picker window
	 */
	private ColorJButton buildColorButton(String label, String tooltip, Supplier<Color> getter,
		Consumer<Color> setter)
	{
		final ColorJButton button = new ColorJButton(label, getter.get());
		button.setToolTipText(tooltip);
		button.setFocusable(false);
		button.addActionListener(event ->
		{
			final RuneliteColorPicker picker = parent.getColorPickerManager().create(
				SwingUtilities.getWindowAncestor(button), getter.get(), label, false);

			picker.setLocationRelativeTo(button);
			picker.setOnColorChange(button::setColor);
			picker.setOnClose(color ->
			{
				setter.accept(color);
				button.setColor(color);
				parent.stateChanged();
			});
			picker.setVisible(true);
		});

		return button;
	}

	/**
	 * Writes the field's contents into the model as the user types, but only persists once editing
	 * finishes, so a rename does not trigger a config write and an image re-render per keystroke.
	 */
	private void bindTextField(JTextField field, Consumer<String> setter)
	{
		field.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent event)
			{
				setter.accept(field.getText());
			}

			@Override
			public void removeUpdate(DocumentEvent event)
			{
				setter.accept(field.getText());
			}

			@Override
			public void changedUpdate(DocumentEvent event)
			{
				setter.accept(field.getText());
			}
		});

		field.addActionListener(event -> parent.stateChanged());
		field.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent event)
			{
				parent.stateChanged();
			}
		});
	}

	private JButton iconButton(Icon icon, String tooltip, boolean enabled, Runnable action)
	{
		final JButton button = new JButton(icon);
		button.setToolTipText(tooltip);
		button.setEnabled(enabled);
		button.setFocusable(false);
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setPreferredSize(new Dimension(20, 20));
		button.addActionListener(event -> action.run());
		return button;
	}

	private static JPanel labelled(String text, JComponent component)
	{
		final JPanel row = new JPanel(new BorderLayout(4, 0));
		row.setOpaque(false);

		final JLabel label = new JLabel(text);
		label.setForeground(Color.LIGHT_GRAY);
		label.setPreferredSize(new Dimension(LABEL_WIDTH, label.getPreferredSize().height));
		row.add(label, BorderLayout.WEST);
		row.add(component, BorderLayout.CENTER);

		return row;
	}
}
