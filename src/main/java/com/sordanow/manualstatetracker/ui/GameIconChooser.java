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

import com.sordanow.manualstatetracker.image.GameIcon;
import com.sordanow.manualstatetracker.image.GameIconCatalogue;
import com.sordanow.manualstatetracker.image.GameIconLoader;
import com.sordanow.manualstatetracker.image.GameIconType;
import com.sordanow.manualstatetracker.image.ImageFactory;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.IconTextField;

/**
 * Modal search dialog for picking an item, prayer or spell icon.
 *
 * <p>Kept as a dialog rather than inline in the side panel so the results list has room, and so a
 * set with many states does not become unusably tall.</p>
 */
class GameIconChooser extends JDialog
{
	/** Size the result rows draw their icons at. */
	private static final int ROW_ICON_SIZE = 28;

	/** Cap on results, since a short query can match thousands of items. */
	private static final int MAX_RESULTS = 300;

	/** Idle time after a keystroke before searching, so typing does not search on every character. */
	private static final int SEARCH_DELAY_MS = 150;

	private static final String ANY_TYPE = "Any type";

	private final GameIconCatalogue catalogue;
	private final GameIconLoader iconLoader;

	private final IconTextField searchField = new IconTextField();
	private final JComboBox<Object> typeFilter = new JComboBox<>();
	private final DefaultListModel<GameIcon> resultModel = new DefaultListModel<>();
	private final JList<GameIcon> resultList = new JList<>(resultModel);
	private final JLabel statusLabel = new JLabel();
	private final JButton selectButton = new JButton("Use icon");

	/** Row icons, keyed by type and id, loaded lazily as rows are rendered. */
	private final Map<String, BufferedImage> rowIcons = new HashMap<>();

	/** Keys already requested, so a repainting list does not queue the same load repeatedly. */
	private final Map<String, Boolean> requestedIcons = new HashMap<>();

	private final Timer searchTimer;

	GameIconChooser(Window owner, GameIconCatalogue catalogue, GameIconLoader iconLoader,
		@Nullable GameIconType initialType, Consumer<GameIcon> onChosen)
	{
		super(owner, "Choose a game icon", ModalityType.APPLICATION_MODAL);

		this.catalogue = catalogue;
		this.iconLoader = iconLoader;

		searchTimer = new Timer(SEARCH_DELAY_MS, event -> runSearch());
		searchTimer.setRepeats(false);

		setLayout(new BorderLayout(0, 6));
		final JPanel content = new JPanel(new BorderLayout(0, 6));
		content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		content.add(buildSearchRow(initialType), BorderLayout.NORTH);
		content.add(buildResults(), BorderLayout.CENTER);
		content.add(buildButtons(onChosen), BorderLayout.SOUTH);

		add(content, BorderLayout.CENTER);

		// Item names come from the game cache, so make sure the index is being built
		catalogue.startItemIndex();

		// Opens showing every prayer and spell, so they can be browsed without knowing a name
		runSearch();

		setSize(340, 460);
		setLocationRelativeTo(owner);
		SwingUtilities.invokeLater(searchField::requestFocusInWindow);
	}

	private JPanel buildSearchRow(@Nullable GameIconType initialType)
	{
		final JPanel panel = new JPanel(new BorderLayout(0, 6));

		searchField.setIcon(IconTextField.Icon.SEARCH);
		searchField.setPreferredSize(new Dimension(0, 26));
		searchField.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(KeyEvent event)
			{
				searchTimer.restart();
			}
		});
		searchField.addClearListener(this::runSearch);
		panel.add(searchField, BorderLayout.NORTH);

		typeFilter.addItem(ANY_TYPE);

		for (GameIconType type : GameIconType.values())
		{
			typeFilter.addItem(type);
		}

		typeFilter.setSelectedItem(initialType == null ? ANY_TYPE : initialType);
		typeFilter.addActionListener(event -> runSearch());
		panel.add(typeFilter, BorderLayout.CENTER);

		return panel;
	}

	private JScrollPane buildResults()
	{
		resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultList.setFixedCellHeight(ROW_ICON_SIZE + 6);
		resultList.setCellRenderer(new IconRowRenderer());
		resultList.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent event)
			{
				if (event.getClickCount() == 2)
				{
					selectButton.doClick();
				}
			}
		});

		final JScrollPane scrollPane = new JScrollPane(resultList);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		return scrollPane;
	}

	private JPanel buildButtons(Consumer<GameIcon> onChosen)
	{
		final JPanel panel = new JPanel(new BorderLayout(6, 0));

		statusLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		panel.add(statusLabel, BorderLayout.NORTH);

		final JPanel buttons = new JPanel(new BorderLayout(6, 0));

		selectButton.addActionListener((ActionEvent event) ->
		{
			final GameIcon chosen = resultList.getSelectedValue();

			if (chosen != null)
			{
				onChosen.accept(chosen);
				dispose();
			}
		});
		buttons.add(selectButton, BorderLayout.CENTER);

		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(event -> dispose());
		buttons.add(cancelButton, BorderLayout.EAST);

		panel.add(buttons, BorderLayout.SOUTH);
		return panel;
	}

	private void runSearch()
	{
		final List<GameIcon> results = catalogue.search(searchField.getText(), selectedType(), MAX_RESULTS);

		resultModel.clear();
		results.forEach(resultModel::addElement);

		if (!results.isEmpty())
		{
			resultList.setSelectedIndex(0);
		}

		updateStatus();
	}

	private void updateStatus()
	{
		final int shown = resultModel.getSize();
		final boolean browsing = searchField.getText().trim().isEmpty();
		final boolean itemsWanted = itemsIncluded();

		// Saying "the first" rather than a bare count, so a capped list does not read as the whole of it
		final String count = shown >= MAX_RESULTS ? "the first " + shown : String.valueOf(shown);
		final StringBuilder status = new StringBuilder("Currently showing ")
			.append(count)
			.append(shown == 1 ? " icon" : " icons");

		// Items are the one kind not listed up front, and only worth mentioning if they are wanted
		if (browsing && itemsWanted)
		{
			status.append(", type to search items");
		}

		status.append('.');

		if (itemsWanted && !catalogue.isItemIndexReady())
		{
			status.append(" Item names are still loading.");
		}

		statusLabel.setText(status.toString());
	}

	@Nullable
	private GameIconType selectedType()
	{
		final Object selected = typeFilter.getSelectedItem();
		return selected instanceof GameIconType ? (GameIconType) selected : null;
	}

	/** Whether the type filter lets items through, and so whether mentioning them is any use. */
	private boolean itemsIncluded()
	{
		final GameIconType type = selectedType();
		return type == null || type == GameIconType.ITEM;
	}

	/**
	 * Requests a row's icon the first time it is drawn and repaints once it arrives, so opening the
	 * dialog does not load hundreds of images up front.
	 */
	@Nullable
	private BufferedImage rowIcon(GameIcon icon)
	{
		final String key = icon.getType() + ":" + icon.getId();
		final BufferedImage cached = rowIcons.get(key);

		if (cached != null)
		{
			return cached;
		}

		if (requestedIcons.putIfAbsent(key, Boolean.TRUE) == null)
		{
			iconLoader.load(icon.getType(), icon.getId(), image ->
			{
				final BufferedImage scaled = ImageFactory.fitToSquare(image, ROW_ICON_SIZE);
				SwingUtilities.invokeLater(() ->
				{
					rowIcons.put(key, scaled);
					resultList.repaint();
				});
			});
		}

		return null;
	}

	private class IconRowRenderer extends DefaultListCellRenderer
	{
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,
			boolean isSelected, boolean cellHasFocus)
		{
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			final GameIcon icon = (GameIcon) value;
			final BufferedImage image = rowIcon(icon);

			setIcon(image == null ? null : new ImageIcon(image));
			setText(icon.getName() + "  (" + icon.getType() + ")");
			setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
			return this;
		}
	}
}
