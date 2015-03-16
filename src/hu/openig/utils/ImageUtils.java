/*
 * Copyright 2008-2014, David Karnok 
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
	/**
	 * Split the image into equally sized sub-images.
	 * @param img the image to split
	 * @param width the split width in pixels
	 * @return the array of images
	 */
	public static BufferedImage[] splitByWidth(BufferedImage img, int width) {
		if (img.getWidth() > 0) {
			BufferedImage[] result = new BufferedImage[(img.getWidth() + width - 1) / width];
			int x = 0;
			for (int i = 0; i < result.length; i++) {
				int x2 = Math.min(x + width - 1, img.getWidth() - 1);
				result[i] = newSubimage(img, x, 0, x2 - x + 1, img.getHeight());
				x += width;
			}
			return result;
		}
		return new BufferedImage[0];
	}
	/**
	 * Split the image into equally sized sub-images.
	 * @param img the image to split
	 * @param width the split width in pixels
	 * @param height the split height in pixels
	 * @return the array of array images, the first dimension are the image rows
	 */
	public static BufferedImage[][] split(BufferedImage img, int width, int height) {
		if (img.getWidth() > 0) {
			int d0 = (img.getHeight() + height - 1) / height;
			int d1 = (img.getWidth() + width - 1) / width;
			BufferedImage[][] result = new BufferedImage[d0][d1];
			int y = 0;
			for (BufferedImage[] ba : result) {
				int x = 0;
				int y2 = Math.min(y + height - 1, img.getHeight() - 1);
				for (int i = 0; i < ba.length; i++) {
					int x2 = Math.min(x + width - 1, img.getWidth() - 1);
					ba[i] = newSubimage(img, x, y, x2 - x + 1, y2 - y + 1);
					x += width;
				}
				y += height;
			}
			return result;
		}
		return new BufferedImage[0][];
	}
	/**
	 * Remove transparent border around an image.
	 * @param src the source image
	 * @return the cut new image
	 */
	public static BufferedImage cutTransparentBorder(BufferedImage src) {
		int top = 0;
		int left = 0;
		int bottom = 0;
		int right = 0;
		
		topcheck:
		for (int y = 0; y < src.getHeight(); y++) {
			for (int x = 0; x < src.getWidth(); x++) {
				int c = src.getRGB(x, y);
				if ((c & 0xFF000000) != 0) {
					break topcheck;
				}
			}
			top++;
		}
		leftcheck:
		for (int x = 0; x < src.getWidth(); x++) {
			for (int y = 0; y < src.getHeight(); y++) {
				int c = src.getRGB(x, y);
				if ((c & 0xFF000000) != 0) {
					break leftcheck;
				}
			}
			left++;
		}
		bottomcheck:
		for (int y = src.getHeight() - 1; y >= top; y--) {
			for (int x = 0; x < src.getWidth(); x++) {
				int c = src.getRGB(x, y);
				if ((c & 0xFF000000) != 0) {
					break bottomcheck;
				}
			}
			bottom++;
		}
		rightcheck:
		for (int x = src.getWidth() - 1; x >= left; x--) {
			for (int y = 0; y < src.getHeight(); y++) {
				int c = src.getRGB(x, y);
				if ((c & 0xFF000000) != 0) {
					break rightcheck;
				}
			}
			right++;
		}
		
		return subimage(src, left, top, src.getWidth() - right - left, src.getHeight() - bottom - top);
	}
}
