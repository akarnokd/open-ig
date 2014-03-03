/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.screen.panels;

import hu.openig.render.RenderTools;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** A two phase toggle button. */
public class TwoPhaseButton {
	/** The X coordinate. */
	public int x;
	/** The Y coordinate. */
	public int y;
	/** The pressed state. */
	public boolean pressed;
	/** The phases. */
	BufferedImage[] phases;
	/** Is this button visible. */
	public boolean visible;
	/** Is the button enabled? */
	public boolean enabled = true;
	/** The disabled pattern. */
	BufferedImage disabledPattern;
	/**
	 * Constructor.
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param phases the two phases
	 * @param disabledPattern the disabled pattern
	 */
	public TwoPhaseButton(int x, int y, BufferedImage[] phases, BufferedImage disabledPattern) {
		this.x = x;
		this.y = y;
		this.phases = phases;
		this.disabledPattern = disabledPattern;
	}
	/**
	 * Paint the button.
	 * @param g2 the graphics
	 */
	public void paintTo(Graphics2D g2) {
		if (visible) {
			if (!enabled) {
				g2.drawImage(phases[0], x, y, null);
				RenderTools.fill(g2, x, y, phases[0].getWidth(), phases[0].getHeight(), disabledPattern);
			} else
			if (pressed) {
				g2.drawImage(phases[1], x, y, null);
			} else {
				g2.drawImage(phases[0], x, y, null);
			}
		}
	}
	/**
	 * Test if the mouse is within this button.
	 * @param mx the mouse X coordinate
	 * @param my the mouse Y coordinate
	 * @return true if within the button
	 */
	public boolean test(int mx, int my) {
		return enabled && visible && mx >= x && my >= y && mx < x + phases[0].getWidth() && my < y + phases[0].getHeight();
	}
}
