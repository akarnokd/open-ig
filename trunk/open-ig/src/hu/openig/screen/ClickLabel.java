/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen;

import hu.openig.core.Act;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIMouse;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * A clickable label printed by the original IG font.
 * Its size is computed from the contents.
 * @author akarnokd, 2010.01.15.
 */
public class ClickLabel extends UIComponent {
	/** The label. */
	private String label;
	/** The text size. */
	private int size;
	/** The action to invoke on press. */
	public Act onPressed;
	/** The action to invoke on release. */
	public Act onReleased;
	/** The action to perform on mouse enter. */
	public Act onEnter;
	/** The action to perform on mouse leave. */
	public Act onLeave;
	/** Is the label selected? */
	public boolean selected;
	/** The common resources. */
	private CommonResources commons;
	/**
	 * Constructor. Sets the label and size.
	 * @param label the label
	 * @param size the size
	 * @param commons the commons
	 */
	public ClickLabel(String label, int size, CommonResources commons) {
		this.label = label;
		this.size = size;
		this.commons = commons;
		resize();
	}
	/** Resize the component based on the content label. */
	void resize() {
		width = 10 + commons.text().getTextWidth(size, commons.labels0().get(label));
		height = size + 4;
	}
	@Override 
	public void draw(Graphics2D g2) {
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, width, height);
		int color = selected ? 0xFFFFCC00 : (over ? 0xFFFFEE00 : 0xFF00CC00);
		commons.text().paintTo(g2, 5, 2, size, color, commons.labels0().get(label));
	}
	@Override
	public boolean mouse(UIMouse e) {
		switch (e.type) {
		case ENTER:
			if (onEnter != null) {
				onEnter.act();
			}
			return true;
		case LEAVE:
			if (onLeave != null) {
				onLeave.act();
			}
			return true;
		case DOWN:
			if (onPressed != null) {
				onPressed.act();
			}
			return true;
		case UP:
			if (onReleased != null) {
				onReleased.act();
			}
			return true;
		default:
			return false;
		}
	}
	/**
	 * Set a new label.
	 * @param newLabel the new label
	 * @return this
	 */
	ClickLabel label(String newLabel) {
		this.label = newLabel;
		resize();
		return this;
	}
}
