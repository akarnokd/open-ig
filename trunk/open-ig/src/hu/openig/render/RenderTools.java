/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.render;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Utility class for common rendering tasks and algorithms. 
 * @author karnokd
 */
public final class RenderTools {
	/** Utility class. */
	private RenderTools() {
	}
	/** The top status bar height constant. */
	public static final int STATUS_BAR_TOP = 20;
	/** The bottom status bar height constant. */
	public static final int STATUS_BAR_BOTTOM = 18;
	/**
	 * Set the rectangle (X, Y) so that the rectangle is centered on the screen
	 * denoted by the screen width and height.
	 * The centering may consider the effect of the top and bottom status bar
	 * @param current the current rectangle
	 * @param screenWidth the screen width
	 * @param screenHeight the screen height
	 * @param considerStatusbars consider, that the status bars take 20px top and 18 pixels bottom?
	 * @return the updated current rectangle
	 */
	public static Rectangle centerScreen(Rectangle current, int screenWidth, int screenHeight,
			boolean considerStatusbars) {
		if (current == null) {
			throw new IllegalArgumentException("current is null");
		}
		if (current.width == 0) {
			throw new IllegalArgumentException("current.width is zero");
		}
		if (current.height == 0) {
			throw new IllegalArgumentException("current.height is zero");
		}
		current.x = (screenWidth - current.width) / 2;
		if (considerStatusbars) {
			current.y = STATUS_BAR_TOP + (screenHeight - STATUS_BAR_TOP - STATUS_BAR_BOTTOM - current.height) / 2;
		} else {
			current.y = (screenHeight - current.height) / 2;
		}
		
		return current;
	}
	/**
	 * Paint a semi-transparent area around the supplied panel.
	 * @param panel the panel to paint around
	 * @param screenWidth the target screen width
	 * @param screenHeight the target screen height
	 * @param g2 the target graphics object
	 * @param alpha the transparency level
	 * @param considerStatusbars consider, that the status bars take 20px top and 18 pixels bottom?
	 */
	public static void darkenAround(
			Rectangle panel, int screenWidth, int screenHeight, Graphics2D g2, float alpha,
			boolean considerStatusbars) {
		Composite c = g2.getComposite();
		g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
		g2.setColor(Color.BLACK);
		
		if (considerStatusbars) {
			fillRectAbsolute(0, STATUS_BAR_TOP, screenWidth - 1, panel.y - 1, g2);
			fillRectAbsolute(0, panel.y + panel.height, screenWidth - 1, screenHeight - 1 - STATUS_BAR_BOTTOM, g2);
		} else {
			fillRectAbsolute(0, 0, screenWidth - 1, panel.y - 1, g2);
			fillRectAbsolute(0, panel.y + panel.height, screenWidth - 1, screenHeight - 1, g2);
		}
		
		fillRectAbsolute(0, panel.y, panel.x - 1, panel.y + panel.height - 1, g2);
		fillRectAbsolute(panel.x + panel.width, panel.y, screenWidth - 1, panel.y + panel.height - 1, g2);
		
		g2.setComposite(c);
	}
	/**
	 * Fill the rectangle given by absolute coordinates.
	 * @param x the left start of the filling inclusive
	 * @param y the top start of the filling inclusive
	 * @param x2 the right end of the filling inclusive
	 * @param y2 the bottom end of the filling inclusive
	 * @param g2 the graphics context
	 */
	public static void fillRectAbsolute(int x, int y, int x2, int y2, Graphics2D g2) {
		if (x >= x2 || y >= y2) {
			return;
		}
		g2.fillRect(x, y, x2 - x + 1, y2 - y + 1);
	}
}
