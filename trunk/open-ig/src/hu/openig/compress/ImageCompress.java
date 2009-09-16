/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.compress;

import hu.openig.utils.BitInputStream;
import hu.openig.utils.BitOutputStream;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Special optimized image storage format.
 * @author karnok, 2009.09.15.
 * @version $Revision 1.0$
 */
public final class ImageCompress {
	/** Private constructor. */
	private ImageCompress() {
		// utility class
	}
	/**
	 * Compress the RGBA pixels provided into the output stream.
	 * @param pixels the pixels
	 * @param width image width
	 * @param height image height
	 * @param pout the output stream
	 * @throws IOException if used IO operations throw this
	 */
	public static void compressImage(int[] pixels, int width, int height, OutputStream pout) throws IOException {
		ByteArrayOutputStream moutP = new ByteArrayOutputStream(1024);
		ByteArrayOutputStream moutNP = new ByteArrayOutputStream(1024);
		ByteArrayOutputStream moutP8 = new ByteArrayOutputStream(1024);
		ByteArrayOutputStream moutNP8 = new ByteArrayOutputStream(1024);
		
		// analysis phase
		Map<Integer, Integer> colorToIndex = new HashMap<Integer, Integer>();
		Map<Integer, Integer> indexToColor = new HashMap<Integer, Integer>();
		
		int colorIndex = 0;
		boolean transparency = false;
		boolean morealpha = false;
		for (int i = 0; i < pixels.length; i++) {
			int c = pixels[i];
			int a = (c >> 24) & 0xFF;
			if (a < 255) {
				transparency = true;
				if (a > 0) {
					morealpha = true;
					if (!colorToIndex.containsKey(c)) {
						colorToIndex.put(c, colorIndex);
						indexToColor.put(colorIndex, c);
						colorIndex++;
					}
				}
			} else {
				if (!colorToIndex.containsKey(c)) {
					colorToIndex.put(c, colorIndex);
					indexToColor.put(colorIndex, c);
					colorIndex++;
				}
			}
		}
		if (transparency && !morealpha) {
			// put the full transparent color as the last color
			colorToIndex.put(0, colorIndex);
			colorIndex++;
		}
		// the bits per pixel
		int bpp = 32 - Integer.numberOfLeadingZeros(colorIndex);

		writeImageData(width, height, moutP, colorToIndex, indexToColor, pixels,
				colorIndex, transparency, morealpha, bpp, true);
		writeImageData(width, height, moutNP, colorToIndex, indexToColor, pixels,
				colorIndex, transparency, morealpha, bpp, false);
		
		if (bpp <= 8) {
			writeImageData(width, height, moutP8, colorToIndex, indexToColor, pixels,
					colorIndex, transparency, morealpha, 8, true);
			writeImageData(width, height, moutNP8, colorToIndex, indexToColor, pixels,
					colorIndex, transparency, morealpha, 8, false);
		}
		
		char type = 'R';
		ByteArrayOutputStream mout = null;
		for (ByteArrayOutputStream raw : new ByteArrayOutputStream[] { moutP, moutP8, moutNP, moutNP8 }) {
			if (raw.size() > 0) {
				if (mout == null || mout.size() > raw.size()) {
					type = 'R';
					mout = raw;
				}
				ByteArrayOutputStream gz = new ByteArrayOutputStream(1024);
				GZIPOutputStream gzout = new GZIPOutputStream(gz);
				raw.writeTo(gzout);
				gzout.close();
				if (mout.size() > gz.size()) {
					mout = gz;
					type = 'G';
				}
				
				ByteArrayOutputStream zip = new ByteArrayOutputStream(1024);
				ZipOutputStream zout  = new ZipOutputStream(zip);
				zout.setLevel(9);
				zout.putNextEntry(new ZipEntry("raw.img"));
				raw.writeTo(zout);
				zout.close();
				if (mout.size() > zip.size()) {
					mout = zip;
					type = 'Z';
				}
			}
		}
		
		pout.write(type);
		mout.writeTo(pout);
		pout.flush();
	}
	/**
	 * Compress the image data.
	 * @param img the image
	 * @param pout the output stream
	 * @throws IOException on error
	 */
	public static void compressImage(BufferedImage img, OutputStream pout) throws IOException {
		// estimate the data size based on wether a palette + indexes or direct colors are used
		int[] pixels = new int[img.getWidth() * img.getHeight()];
		img.getRGB(0, 0, img.getWidth(), img.getHeight(), pixels, 0, img.getWidth());
		compressImage(pixels, img.getWidth(), img.getHeight(), pout);
	}
	/**
	 * Encode and write the raw image data to the output stream.
	 * @param width the width
	 * @param height the height
	 * @param out the output stream
	 * @param colorToIndex the color to index map
	 * @param indexToColor the index to color map
	 * @param pixels the raw RGBA pixels
	 * @param colorIndex the number of colors
	 * @param transparency the original image has transparency
	 * @param morealpha the original image has more than two alpha value (e.g. not 0 and 255)
	 * @param bpp the target bits per pixel for the image data
	 * @param isWithPalette use palette + index or use 24/32 bit pixels
	 * @throws IOException if an IO error occurs
	 */
	private static void writeImageData(int width, int height, OutputStream out,
			Map<Integer, Integer> colorToIndex,
			Map<Integer, Integer> indexToColor, int[] pixels, int colorIndex,
			boolean transparency, boolean morealpha, int bpp,
			boolean isWithPalette) throws IOException {
		BitOutputStream bout = new BitOutputStream(out);
		// the largest index is the tranparent pixel
		bout.writeBit(transparency ? 1 : 0);
		// there are alpha blended colors
		bout.writeBit(morealpha ? 1 : 0); 
		// use 3 byte width and height, instead of 8 byte
		bout.writeBit(isWithPalette ? 1 : 0);
		// the number of bits per pixel minus one
		bout.writeBits(bpp - 1, 5);
		bout.writeBits(width, 12);
		bout.writeBits(height, 12);
		
		if (isWithPalette) {
			int max = transparency && !morealpha ? colorIndex - 1 : colorIndex;
			// write the number of palette entries
			bout.writeBits(max - 1, bpp);
			// write 
			for (int i = 0; i < max; i++) {
				if (morealpha) {
					bout.writeBits(indexToColor.get(i), 32);
				} else {
					bout.writeBits(indexToColor.get(i), 24);
				}
			}
			for (int i = 0; i < pixels.length; i++) {
				int c = pixels[i];
				int a = (c >> 24) & 0xFF;
				if (a == 0 && !morealpha) {
					bout.writeBits(colorIndex - 1, bpp);
				} else {
					bout.writeBits(colorToIndex.get(c), bpp);
				}
			}
		} else {
			if (transparency && !morealpha) {
				// find an unused color to use as transparency
				TreeSet<Integer> ts = new TreeSet<Integer>();
				for (int i = 0; i < pixels.length; i++) {
					ts.add(pixels[i]);
				}
				int transparentColor = ts.last() + 1;
				Integer last = ts.first();
				for (Integer curr : ts) {
					if (curr - last > 1) {
						transparentColor = last + 1;
						break;
					}
				}
				bout.writeBits(transparentColor, 24);
				// write transparent pixel color
				for (int i = 0; i < pixels.length; i++) {
					int c = pixels[i];
					if  ((c & 0xFF000000) == 0) {
						c = transparentColor;
					}
					bout.writeBits(c, 24);
				}
			} else {
				// no transparency at all, write raw data
				for (int i = 0; i < pixels.length; i++) {
					bout.writeBits(pixels[i], morealpha ? 32 : 24);
				}
			}
		}
		bout.close();
	}
	/**
	 * Decompress the image from the input stream.
	 * @param in the input stream
	 * @param width the one element array to store the width
	 * @param height the one element array to store the height
	 * @return the image pixels in RGBA format
	 * @throws IOException if any IO operation throws it
	 */
	public static int[] decompressImage(InputStream in, int[] width, int[] height) throws IOException {
		char type = (char)in.read();
		if (type == 'G') {
			in = new GZIPInputStream(in);
		} else
		if (type == 'Z') {
			ZipInputStream zin = new ZipInputStream(in);
			zin.getNextEntry();
			in = zin;
		}
		BitInputStream bin = new BitInputStream(in);
		
		
		boolean transparency = bin.readBit() > 0;
		boolean morealpha = bin.readBit() > 0;
		boolean isWithPalette = bin.readBit() > 0;
		int bpp = bin.readBits(5) + 1;
		int w = bin.readBits(12);
		int h = bin.readBits(12);
		
		int[] pixels = new int[w * h];
		Map<Integer, Integer> indexToColor = new HashMap<Integer, Integer>();
		if (isWithPalette) {
			int colorIndex = bin.readBits(bpp) + 1;
			for (int i = 0; i < colorIndex; i++) {
				indexToColor.put(i, bin.readBits(morealpha ? 32 : 24));
			}
			for (int i = 0; i < pixels.length; i++) {
				int c = bin.readBits(bpp);
				if (!(c == colorIndex && (transparency && !morealpha))) {
					int c2 = indexToColor.get(c);
					if (morealpha) {
						pixels[i] = c2;
					} else {
						int c3 = 0xFF000000 | c2;
						pixels[i] = c3;
					}
				}
			}
		} else {
			if (transparency && !morealpha) {
				int transparentColor = bin.readBits(24);
				for (int i = 0; i < pixels.length; i++) {
					int c2 = bin.readBits(24);
					if (c2 != transparentColor) {
						int c3 = 0xFF000000 | c2;
						pixels[i] = c3;
					}
				}				
			} else {
				for (int i = 0; i < pixels.length; i++) {
					int c2 = bin.readBits(morealpha ? 32 : 24);
					if (morealpha) {
						pixels[i] = c2;
					} else {
						int c3 = 0xFF000000 | c2;
						pixels[i] = c3;
					}
				}
			}
		}
		width[0] = w;
		height[0] = h;
		return pixels;
	}
	/**
	 * Decompress image data.
	 * @param in the input stream
	 * @return the ARGB buffered image
	 * @throws IOException on error
	 */
	public static BufferedImage decompressImage(InputStream in) throws IOException {
		int[] w = { 0 }, h = { 0 };
		int[] pixels = decompressImage(in, w, h);
		BufferedImage bimg = new BufferedImage(w[0], h[0], BufferedImage.TYPE_INT_ARGB);
		bimg.setRGB(0, 0, w[0], h[0], pixels, 0, w[0]);
		return bimg;
	}
}
