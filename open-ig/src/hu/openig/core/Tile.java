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
 * The tile coordinate system and screen coordinate system is assumed to
 * have the same origo, namely the 1x1 tile's bottom left position.
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
	//public int scanlines = 1;
	/** Tile image height correction, for those images which don't end at the last line of the image. */
	public int heightCorrection;
	/** The actual tile image. */
	public BufferedImage image;
	/** The vertical strips for larger tiles.*/
	public BufferedImage[] strips;
	/**
	 * Converts the tile coordinates to pixel coordinates, X component.
	 * @param x
	 * @param y
	 * @return
	 */
	public static int toScreenX(int x, int y) {
		return x * 30 - y * 28;
	}
	/**
	 * Converts the tile coordinates to pixel coordinates, Y component.
	 * @param x
	 * @param y
	 * @return
	 */
	public static int toScreenY(int x, int y) {
		return - 12 * x - 15 * y;
	}
	/**
	 * Converts the screen coordinates to tile coordinates, X component.
	 * @param x
	 * @param y
	 * @return
	 */
	public static float toTileX(int x, int y) {
		return (x + toTileY(x, y) * 28) / 30f;
	}
	/**
	 * Converts the screen coordinates to tile coordinates, Y comonent.
	 * @param x
	 * @param y
	 * @return
	 */
	public static float toTileY(int x, int y) {
		return -(30 * y + 12 * x) / 786f;
	}
}
