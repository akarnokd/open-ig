/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

import java.awt.Graphics2D;

/**
 * A base class for common Imperium Galactica.
 * @author karnok, 2010.01.14.
 * @version $Revision 1.0$
 */
public abstract class Button {
	/** Is the button enabled. */
	public boolean enabled = true;
	/** Is the button visible. */
	public boolean visible = true;
	/** Flag to indicate whether the mouse is over this button. */
	public boolean mouseOver;
	/** Flag to indicate whether the mouse is pressed over this button. */
	public boolean pressed;
	/** The X coordinate. */
	public int x;
	/** The Y coordinate. */
	public int y;
	/**
	 * Paint the button relative to the given reference point.
	 * @param g2 the graphics object
	 * @param x0 the reference X coordinate
	 * @param y0 the reference Y coordinate
	 */
	public abstract void paintTo(Graphics2D g2, int x0, int y0);
	/**
	 * @return the button width
	 */
	public abstract int getWidth();
	/**
	 * @return the button height
	 */
	public abstract int getHeight();
	/**
	 * Test if the mouse is within this button.
	 * @param mx the mouse X coordinate
	 * @param my the mouse Y coordinate
	 * @param x0 the reference X coordinate
	 * @param y0 the reference Y coordinate
	 * @return true if within
	 */
	public boolean test(int mx, int my, int x0, int y0) {
		return enabled && visible && mx >= x0 + x && mx < x0 + x + getWidth()
		&& my >= y0 + y && my < y0 + y + getHeight();
	}
	/**
	 * Called when the mouse enters this button.
	 */
	public abstract void onEnter();
	/**
	 * Called when the mouse leaves this button.
	 */
	public abstract void onLeave();
	/** Called when the left mouse is pressed on this button. */
	public abstract void onPressed();
	/** Called when the left mouse is released on this button. */
	public abstract void onReleased();
}
