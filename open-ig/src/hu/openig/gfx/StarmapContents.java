/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.gfx;

import java.awt.Color;
import java.awt.image.BufferedImage;

/** 
 * Record to store the inner window parts of the starmap display. 
 * @author karnokd
 */
public class StarmapContents {
	/** Bottom left image. */
	public BufferedImage bottomLeft;
	/** Bottom right image. */
	public BufferedImage bottomRight;
	/** Bottom filler image. */
	public BufferedImage bottomFiller;
	/** Right top image. */
	public BufferedImage rightTop;
	/** Right bottom image. */
	public BufferedImage rightBottom;
	/** Right filler image. */
	public BufferedImage rightFiller;
	/** Ship controls images. */
	public BufferedImage shipControls;
	/** Horizontal scrollbar left. */
	public BufferedImage hscrollLeft;
	/** Horizontal scrollbar filler. */
	public BufferedImage hscrollFiller;
	/** Horizontal scrollbar right. */
	public BufferedImage hscrollRight;
	/** Vertical scrollbar top. */
	public BufferedImage vscrollTop;
	/** Vertical scrollbar filler. */
	public BufferedImage vscrollFiller;
	/** Vertical scrollbar bottom. */
	public BufferedImage vscrollBottom;
	/** The full map background. */
	public BufferedImage fullMap;
	/** The color of the map background. */
	public Color mapBackground;
}
