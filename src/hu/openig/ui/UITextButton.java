/*
 * Copyright 2008-2014, David Karnok 
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

/**
 * A textual button surrounded by the purple IG themed color.
 * @author akarnokd, 2012.05.28.
 */
public class UITextButton extends UIComponent {
	/** The text renderer. */
	protected final TextRenderer tr;
	/** The text to display. */
	protected String text;
	/** The text color. */
	protected int color = 0xFFC0C0C0;
	/** The normal background. */
	protected int background = 0xFF65486D;
	/** Background color when hovered. */
	protected int backgroundHover = 0xFF685697;
	/** Background color when pressed. */
	protected int backgroundPressed = 0xFF7A3C5D;
	/** The text size. */
	protected int size;
	/** The action to perform on click. */
	public Action0 onClick;
	/** The hover indicator. */
	protected boolean hover;
	/** The pressed indicator. */
	protected boolean pressed;
	/** The horizontal margin. */
	protected int marginWidth = 6;
	/** The vertical margin. */
	protected int marginHeight = 5;
	/**
	 * Constructs a new textbutton with the given contents and default look.
	 * @param text the text to display
	 * @param size the text height
	 * @param tr the text renderer
	 */
	public UITextButton(String text, int size, TextRenderer tr) {
		this.text = text;
		this.size = size;
		this.tr = tr;
		this.width = tr.getTextWidth(size, text) + 2 * marginWidth;
		this.height = size + 2 * marginHeight;
	}
	@Override
	public boolean mouse(UIMouse e) {
		boolean result = false;
		if (e.has(Type.DOWN)) {
			pressed = true;
			if (onClick != null) {
				onClick.invoke();
				result = true;
			}
		}
		if (e.has(Type.UP)) {
			if (pressed) {
				pressed = false;
				result = true;
			}
		}
		if (e.has(Type.ENTER)) {
			hover = true;
			result = true;
		} else
		if (e.has(Type.LEAVE)) {
			hover = false;
			pressed = false;
			result = true;
		}
		return result;
	}
	@Override
	public void draw(Graphics2D g2) {
		if (enabled) {
			if (pressed) {
				g2.setColor(new Color(this.backgroundPressed));
			} else
			if (hover) {
				g2.setColor(new Color(this.backgroundHover));
			} else {
				g2.setColor(new Color(this.background));
			}
		} else {
			g2.setColor(Color.GRAY);
		}
		g2.fillRect(0, 0, width, height);
		g2.setColor(Color.BLACK);
		g2.drawRoundRect(1, 1, width - 3, height - 3, 3, 3);
		tr.paintTo(g2, marginWidth, marginHeight, size, color, text);
	}
	@Override
	public UIComponent enabled(boolean state) {
		hover &= state;
		pressed &= state;
		return super.enabled(state);
	}
	@Override
	public UIComponent visible(boolean state) {
		hover &= state;
		pressed &= state;
		return super.visible(state);
	}
}
