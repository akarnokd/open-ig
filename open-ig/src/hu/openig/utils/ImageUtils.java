/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.utils;

import java.awt.image.BufferedImage;

/**
 * Image manipulation utilities.
 * @author akarnokd
 */
public final class ImageUtils {
	/** Use subimage or separate image. */
	private static final boolean SUBIMAGE = true; 
	/** Private constructor. */
	private ImageUtils() {
		// private class
	}
	/**
	 * Returns a subimage of the given main image. If the sub-image
	 * tends to be smaller in area then the original image, a new buffered image is returned instead of the
	 * shared sub image
	 * @param src the source image.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param w the width
	 * @param h the height
	 * @return the extracted sub-image
	 */
	public static BufferedImage subimage(BufferedImage src, int x, int y, int w, int h) {
		if (!SUBIMAGE /* && w * h * 16 < src.getWidth() * src.getHeight() */) {
			return newSubimage(src, x, y, w, h);
		}
		return src.getSubimage(x, y, w, h);
	}
	/**
	 * Returns an independent subimage of the given main image. copying data from the original image.
	 * @param src the source image.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param w the width
	 * @param h the height
	 * @return the extracted sub-image
	 */
	public static BufferedImage newSubimage(BufferedImage src, int x, int y, int w, int h) {
		BufferedImage bimg =  new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		int[] tmp = new int[w * h];
		src.getRGB(x, y, w, h, tmp, 0, w);
		bimg.setRGB(0, 0, w, h, tmp, 0, w);
		return bimg;
	}
	/**
	 * Recolor a given default tile image.
	 * @param img the original image.
	 * @param newColor the new RGBA color.
	 * @return the new RGBA image
	 */
	public static BufferedImage recolor(BufferedImage img, int newColor) {
		int[] pixels = new int[img.getWidth() * img.getHeight()];
		img.getRGB(0, 0, img.getWidth(), img.getHeight(), pixels, 0, img.getWidth());
		for (int i = 0; i < pixels.length; i++) {
			int c = pixels[i];
			if (c == 0xFF000000) {
				pixels[i] = newColor;
			}
		}
		BufferedImage result = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		result.setRGB(0, 0, img.getWidth(), img.getHeight(), pixels, 0, img.getWidth());
		return result;
	}
}
