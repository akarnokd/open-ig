/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.core;

import java.awt.image.BufferedImage;

/**
 * Contains tile image and correction values.
 * @author karnokd
 */
public class Tile {
	/** Tile base area width in tile units. */
	public int width = 1;
	/** Tile base area height in tile units. */
	public int height = 1;
	/** Correctional value for bottom edge of the tile. */
	//public int offset = 0;
	/** Number of scan lines spanning this tile's base over. Is equal to width + height - 1. */
	public int scanlines = 1;
	/** Tile image height correction, for those images which don't end at the last line of the image. */
	public int heightCorrection;
	/** The actual tile image. */
	public BufferedImage image;
}
