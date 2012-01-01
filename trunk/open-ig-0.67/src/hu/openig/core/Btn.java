/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.core;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * The button position and state.
 * @author karnokd
 */
public class Btn {
	/** The location rectangle. */
	public final Rectangle rect;
	/** The button is disabled. */
	public boolean disabled;
	/** The button is pressed. */
	public boolean down;
	/** Button visibility indicator. */
	public boolean visible = true;
	/** The action to perform when the button is clicked. */
	public BtnAction onClick;
	/**
	 * Constructor.
	 */
	public Btn() {
		rect = new Rectangle();
	}
	/**
	 * Constructor. Sets the onClick action.
	 * @param onClickAction the action
	 */
	public Btn(BtnAction onClickAction) {
		this();
		onClick = onClickAction;
	}
	/**
	 * Perform the click operation action.
	 */
	public void click() {
		if (onClick != null) {
			onClick.invoke();
		}
	}
	/** 
	 * Tests if the point is within the visible, enabled rectangle of this button.
	 * @param p the point to test
	 * @return true if the point is within the button's boundaries
	 */
	public boolean test(Point p) {
		return visible && !disabled && rect.contains(p);
	}
	/**
	 * Set the bounds.
	 * @param r the bounding rectangle
	 */
	public void setBounds(Rectangle r) {
		rect.setBounds(r);
	}
	/**
	 * Set the bounds from coordinates.
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param w the width
	 * @param h the height
	 */
	public void setBounds(int x, int y, int w, int h) {
		rect.setBounds(x, y, w, h);
	}
}
