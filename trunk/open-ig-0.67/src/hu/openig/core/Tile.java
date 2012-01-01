/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.core;

import hu.openig.utils.ImageUtils;
import hu.openig.utils.PCXImage;

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
	/** The original image. */
	public PCXImage rawImage;
	/** The actual tile image. */
	public BufferedImage image;
	/** The vertical strips for larger tiles. Null for 1x1 tiles. */
	public BufferedImage[] strips;
	/** The current alpha value. */
	public float alpha;
	/**
	 * Converts the tile coordinates to pixel coordinates, X component.
	 * @param x the X tile coordinate
	 * @param y the Y tile coordinate
	 * @return the screen coordinate
	 */
	public static int toScreenX(int x, int y) {
		return x * 30 - y * 28;
	}
	/**
	 * Converts the tile coordinates to pixel coordinates, Y component.
	 * @param x the X tile coordinate
	 * @param y the Y tile coordinate
	 * @return the screen Y coordinate
	 */
	public static int toScreenY(int x, int y) {
		return - 12 * x - 15 * y;
	}
	/**
	 * Converts the screen coordinates to tile coordinates, X component.
	 * @param x the X screen coordinate
	 * @param y the Y screen coordinate
	 * @return the tile X coordinate
	 */
	public static float toTileX(int x, int y) {
		return (x + toTileY(x, y) * 28) / 30f;
	}
	/**
	 * Converts the screen coordinates to tile coordinates, Y comonent.
	 * @param x the screen X coordinate
	 * @param y the screen Y coordinate
	 * @return the tile Y coordinate
	 */
	public static float toTileY(int x, int y) {
		return -(30 * y + 12 * x) / 786f;
	}
	/**
	 * Creates an image using the given alpha value modified palette and its strips.
	 * @param alpha the darkness factor 0=full dark, 1=normal
	 */
	public void createImage(float alpha) {
		// ignore alpha change below 0.01
		if (Math.abs(this.alpha - alpha) <= 0.01) {
			return;
		}
		this.alpha = alpha;
		image = rawImage.toBufferedImage(-2, createPalette(rawImage.getPalette(), alpha));
		//t.scanlines = width + height - 1;
		if (strips != null) {
			// create strips
			for (int i = 0; i < strips.length; i++) {
				int x0 = i >= width ? Tile.toScreenX(i, 0) : Tile.toScreenX(0, -i);
				int w0 = Math.min(57, image.getWidth() - x0);
				strips[i] = ImageUtils.subimage(image, x0, 0, w0, image.getHeight());
			}
		}
	}
	/**
	 * Alters the given palette by reducing the colors by the alpha, but
	 * leaves some palette entries intact. This leads to the effect of
	 * lights on buildings.
	 * @param currentPalette the current RGB palette
	 * @param alpha the alpha level.
	 * @return the modified palette
	 */
	private byte[] createPalette(byte[] currentPalette, float alpha) {
		// change palette only if it is not at 100%
		if (Math.abs(alpha - 1.0) > 0.01) {
			// scale down the palette colors by the given factor
			for (int i = 0; i < currentPalette.length; i += 3) {
				currentPalette[i] = (byte)ranged(0, 255, (int)((currentPalette[i] & 0xFF) * alpha));
				currentPalette[i + 1] = (byte)ranged(0, 255, (int)((currentPalette[i + 1] & 0xFF) * alpha));
				currentPalette[i + 2] = (byte)ranged(0, 255, (int)((currentPalette[i + 2] & 0xFF) * alpha));
			}
		}
		return currentPalette;
	}
	/**
	 * Returns a value between the given range.
	 * @param min the minimum allowed value
	 * @param max the maximum allowed value
	 * @param value the current value
	 * @return the bounded value
	 */
	private static float ranged(int min, int max, int value) {
		if (min > value) {
			return min;
		} else
		if (max < value) {
			return max;
		}
		return value;
	}
}
