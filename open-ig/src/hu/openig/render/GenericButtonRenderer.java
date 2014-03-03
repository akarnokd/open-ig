/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.render;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * The base interface for small, medium and large generic button renderers.
 * @author akarnokd
 */
public interface GenericButtonRenderer {
	/**
	 * Draw the generic button graphics to the target canvas.
	 * @param g2 the target graphics2d canvas
	 * @param x the leftmost coordinate
	 * @param y the rightmost coordinate
	 * @param width the full width of the button, must be at least 103
	 * @param height the full height of the button, must be at least 28
	 * @param down should compensate for the pressed-down state?
	 * @param text the display text of the button
	 */
	void paintTo(Graphics2D g2, int x, int y, 
			int width, int height, boolean down, String text);
	/**
	 * Get the preferred size of the rendering given the font metrics and content text.
	 * @param fm the font metrics
	 * @param text the text
	 * @return the preferred size
	 */
	Dimension getPreferredSize(FontMetrics fm, String text);
}
