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

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.JButton;
import lombok.Getter;
import net.runelite.client.config.Keybind;

/**
 * A button that records whatever key combination is pressed while it has focus, and clears itself
 * when clicked.
 *
 * <p>RuneLite has an equivalent widget for its own config panel, but it is package private, so this
 * mirrors its behaviour: click to focus and clear, then press the combination to bind.</p>
 */
class KeybindCaptureButton extends JButton
{
	private static final String UNBOUND_TEXT = "Not set";

	@Getter
	private Keybind value;

	private final Consumer<Keybind> onChange;

	KeybindCaptureButton(Keybind initialValue, Consumer<Keybind> onChange)
	{
		this.onChange = onChange;

		// Otherwise tab moves focus instead of being bindable
		setFocusTraversalKeysEnabled(false);
		setToolTipText("Click, then press a key combination. Click again to clear.");
		applyValue(initialValue, false);

		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent event)
			{
				// A mouse adapter rather than an action listener, so space stays bindable
				if (event.getButton() == MouseEvent.BUTTON1)
				{
					applyValue(Keybind.NOT_SET, true);
				}
			}
		});

		addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent event)
			{
				applyValue(new Keybind(event), true);
			}
		});
	}

	private void applyValue(Keybind keybind, boolean notify)
	{
		value = keybind == null ? Keybind.NOT_SET : keybind;
		setText(Keybind.NOT_SET.equals(value) ? UNBOUND_TEXT : value.toString());

		if (notify)
		{
			onChange.accept(value);
		}
	}
}
