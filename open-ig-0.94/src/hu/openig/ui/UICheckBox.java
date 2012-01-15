/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ui;

import hu.openig.core.Action0;
import hu.openig.render.TextRenderer;
import hu.openig.ui.UIMouse.Type;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * A checkbox component with text.
 * @author akarnokd, Apr 18, 2011
 */
public class UICheckBox extends UIComponent {
	/** The text to display. */
	protected String text;
	/** The text and checkbox size. */
	protected int size;
	/** The text renderer. */
	protected final TextRenderer tr;
	/** The check mark. */
	protected final BufferedImage check;
	/** The normal color. */
	protected int color = TextRenderer.GREEN;
	/** The disabled color. */
	protected int disabledColor = TextRenderer.GRAY;
	/** The selection state. */
	protected boolean selected;
	/** The press indicator. */
	protected boolean down;
	/** The change handler. */
	public Action0 onChange;
	/**
	 * Construct an UICheckBox.
	 * @param text the text label
	 * @param size the size
	 * @param check the check image
	 * @param tr the text renderer
	 */
	public UICheckBox(String text, int size, BufferedImage check, TextRenderer tr) {
		this.text = text;
		this.size = size;
		this.height = size;
		this.width = tr.getTextWidth(size, text) + size + 6;
		this.check = check;
		this.tr = tr;
	}
	/** @return the selected state. */
	public boolean selected() {
		return selected;
	}
	/**
	 * Change the selection state.
	 * @param state the new state
	 * @return this
	 */
	public UICheckBox selected(boolean state) {
		this.selected = state;
		return this;
	}
	@Override
	public void draw(Graphics2D g2) {
		g2.setColor(Color.WHITE);
		g2.drawRect(0, 0, size - 1, size - 1);
		g2.drawRect(1, 1, size - 3, size - 3);
		
		if (selected) {
			g2.drawImage(check, 0, size - check.getHeight(), null);
		}
		
		tr.paintTo(g2, size + 6, 0, size, enabled() ? color : disabledColor, text);
	}
	@Override
	public boolean mouse(UIMouse e) {
		if (e.has(Type.DOWN)) {
			if (!selected) {
				selected = true;
				if (onChange != null) {
					onChange.invoke();
				}
				down = true;
				return true;
			}
		} else
		if (e.has(Type.UP)) {
			if (selected && !down) {
				selected = false;
				if (onChange != null) {
					onChange.invoke();
				}
				return true;
			}
			down = false;
		}
		return super.mouse(e);
	}
	/**
	 * Change the text.
	 * @param newText the new text
	 * @param resize resize to the new text?
	 * @return this
	 */
	public UICheckBox text(String newText, boolean resize) {
		this.text = newText;
		if (resize) {
			width = size + 2 + tr.getTextWidth(size, newText);
		}
		return this;
	}
}
