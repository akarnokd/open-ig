/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.utils;

import java.awt.image.BufferedImage;

/**
 * Image manipulation utilities.
 * @author karnokd
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
			BufferedImage bimg =  new BufferedImage(w, h, src.getType());
			int[] tmp = new int[w * h];
			src.getRGB(x, y, w, h, tmp, 0, w);
			bimg.setRGB(0, 0, w, h, tmp, 0, w);
			return bimg;
		}
		return src.getSubimage(x, y, w, h);
	}
}
