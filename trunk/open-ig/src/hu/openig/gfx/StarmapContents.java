/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.gfx;

import java.awt.Color;
import java.awt.image.BufferedImage;

/** Record to store the inner window parts of the starmap display. */
public class StarmapContents {
	public BufferedImage bottomLeft;
	public BufferedImage bottomRight;
	public BufferedImage bottomFiller;
	public BufferedImage rightTop;
	public BufferedImage rightBottom;
	public BufferedImage rightFiller;
	public BufferedImage shipControls;
	public BufferedImage hscrollLeft;
	public BufferedImage hscrollFiller;
	public BufferedImage hscrollRight;
	public BufferedImage vscrollTop;
	public BufferedImage vscrollFiller;
	public BufferedImage vscrollBottom;
	/** The bottom right minimap background. */
	public BufferedImage minimap;
	/** The full map background. */
	public BufferedImage fullMap;
	/** The color of the map background. */
	public Color mapBackground;
}