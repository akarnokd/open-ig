/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.test;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;

/**
 * @author akarnokd, 2011.02.25.
 */
public class UIComponent {
	/** The relative X placement to the parent. */
	public int x;
	/** The relative Y placement to the parent. */
	public int y;
	/** The current width. */
	public int width;
	/** The current height. */
	public int height;
	/** The Z ordering helper to distinguish between overlapping components. */
	public int z;
	/** The parent UI component (e.g., container) or null if this is the top level component. */
	public UIComponent parent;
	/** The visibility indicator (e.g., no drawing and no events). */
	public boolean visible = true;
	/** Enabledness indicator (e.g., no events). */
	public boolean enabled = true;
	/** The mouse is over this component, managed by the container. */
	public boolean over;
	/**
	 * Paint this UI component to the given graphics context.
	 * @param g2 the graphics context
	 */
	public void draw(Graphics2D g2) {
		
	}
	/**
	 * @return the components own size
	 */
	public Dimension size() {
		return new Dimension(width, height);
	}
	/**
	 * Set both with and height to the specified dimension value.
	 * Some component might have fixed size, and
	 * setting a size is a no-op.
	 * @param d the new dimension
	 */
	public void size(Dimension d) {
		width = d.width;
		height = d.height;
	}
	/**
	 * Set both with and height to the specified values.
	 * Some component might have fixed size, and
	 * setting a size is a no-op.
	 * @param width the new width
	 * @param height the new height
	 */
	public void size(int width, int height) {
		this.width = width;
		this.height = height;
	}
	/**
	 * The generic entry point for a mouse event.
	 * @param e the mouse event object
	 * @return true if the component requests a repaint
	 */
	public boolean mouse(UIMouse e) {
		return false;
	}
	/**
	 * The generic entry point for keyboard event.
	 * TODO might not be needed
	 * @param e the keyboard event
	 * @return true if the component requests a repaint
	 */
	public boolean keyboard(KeyEvent e) {
		return false;
	}
	/**
	 * Returns the absolute location within the
	 * container hierarchy.
	 * @return the absolute location (x, y)
	 */
	public Point absLocation() {
		int px = x;
		int py = y;
		UIComponent p = parent;
		while (p != null) {
			px += p.x;
			py += p.y;
			p = p.parent;
		}
		return new Point(px, py);
	}
	/**
	 * @return the (x, y) coordinate pair
	 */
	public Point location() {
		return new Point(x, y);
	}
	/**
	 * Set the container-relative coordinate.
	 * @param p the location as Point
	 */
	public void location(Point p) {
		this.x = p.x;
		this.y = p.y;
	}
	/**
	 * Set the container-relative coordinate.
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 */
	public void location(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
