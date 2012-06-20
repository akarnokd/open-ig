/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.core.Pair;
import hu.openig.utils.U;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;

/**
 * Utility class to take a sequence of PNG files and compose an
 * ani2009 type video file.
 */
public final class PNGToVideo {
	/** Utility class. */
	private PNGToVideo() { }
	/**
	 * Main program.
	 * @param args no arguments
	 * @throws Exception on error
	 */
	public static void main(String[] args) throws Exception {
//		int count = 41;
//		double fps = 17.89;
//		
//		String[] mode = { "appear", "open", "close" };
//		String[] mode2 = { "0", "", "F" };
//		
//		for (int l = 1; l < 4; l++) {
//			for (int m = 0; m < 3; m++) {
//				String filename = "c:/Games/IGDE/1/" + l + "_HID" + mode2[m] + ".ANI-%05d.PNG";
//				String output = "video/de/bridge/messages_" + mode[m] + "_level_" + l + ".ani.gz";
//				
//				convert(filename, count, fps, output);
//			}
//		}
		convert("e:/temp/dipfej9_%03d.png", 0, 122, 17.89, "video/generic/diplomacy/dipfej9.ani.gz");
	}
	/**
	 * Convert a sequence of PNG files into a video.
	 * @param filename the filename pattern
	 * @param start the start index
	 * @param count the number of frames
	 * @param fps the framerate
	 * @param output the output file
	 * @throws IOException on error
	 */
	public static void convert(String filename, int start, int count, double fps,
			String output) throws IOException {
		DataOutputStream bout = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(output))));
		try {
			BufferedImage i0 = get(filename, 0);
			
			// header
			bout.writeInt(Integer.reverseBytes(i0.getWidth()));
			bout.writeInt(Integer.reverseBytes(i0.getHeight()));
			bout.writeInt(Integer.reverseBytes(count));
			bout.writeInt(Integer.reverseBytes((int)(fps * 1000)));

			Pair<int[], Map<Integer, Integer>> uc = uniqueColors(i0);
			writePalette(bout, uc.first, uc.second.size());

			bout.write('I');
			byte[] b0 = colorToPalette(i0, uc.second);
			bout.write(b0);
			
			if (count > 1) {
				int idx = start;
				do {
					System.out.printf("Frame: %d ", idx);
					BufferedImage i2 = get(filename, idx);
					if (i2 == null) {
						System.out.printf(" no more%n");
						break;
					}
					
					Pair<int[], Map<Integer, Integer>> uc2 = uniqueColors(i2);
					
					if (!Arrays.equals(uc.first, uc2.first)) {
						writePalette(bout, uc2.first, uc2.second.size());
					}

					byte[] b2 = colorToPalette(i2, uc2.second);
					
					bout.write('I');
					
					for (int i = 0; i < b2.length; i++) {
						int p0 = b0[i] & 0xFF;
						int p1 = b2[i] & 0xFF;
						
						int c0 = uc.first[p0];
						int c1 = uc2.first[p1];
						if (c0 == c1) {
							bout.writeByte(255);
						} else {
							bout.writeByte(p1);
						}
					}
					
					uc = uc2;
					b0 = b2;
					idx++;
					System.out.printf(" done.%n");
				} while (idx < count + start);
			}
			
			// end of data
			bout.write('X');
		} finally {
			bout.close();
		}
	}
	/**
	 * Convert the given RGBA image into an array of 8-bit palette-based image.
	 * @param img the image
	 * @param pal the palette
	 * @return the bytes
	 */
	static byte[] colorToPalette(BufferedImage img, Map<Integer, Integer> pal) {
		int[] pxs = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
		byte[] result = new byte[pxs.length];
		for (int i = 0; i < pxs.length; i++) {
			int c = pxs[i];
			int b = pal.get(c);
			result[i] = (byte)b;
		}
		return result;
	}
	/**
	 * Write the palette settings.
	 * @param bout the data output
	 * @param palette the RGBA palette 
	 * @param n the number of entries
	 * @throws IOException on error
	 */
	static void writePalette(DataOutput bout, int[] palette, int n) throws IOException {
		bout.write('P');
		bout.writeByte(n);
		for (int i = 0; i < n; i++) {
			int c = palette[i];
			
			bout.writeByte((c & 0xFF0000) >> 16);
			bout.writeByte((c & 0xFF00) >> 8);
			bout.writeByte((c & 0xFF));
		}
	}
	/**
	 * Extract the unique colors into a palette.
	 * @param img the image
	 * @return the palette and maximum color count
	 */
	static Pair<int[], Map<Integer, Integer>> uniqueColors(BufferedImage img) {
		int[] pxs = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
		Map<Integer, Integer> colors = U.newHashMap();
		int[] result = new int[256];
		
		for (int c : pxs) {
			if (!colors.containsKey(c)) {
				int idx = colors.size();
				colors.put(c, idx);
				result[idx] = c;
			}
		}
		return Pair.of(result, colors);
	}
	/**
	 * Read the given image file.
	 * @param format the file pattern.
	 * @param index the index
	 * @return the image
	 * @throws IOException on error
	 */
	static BufferedImage get(String format, int index) throws IOException {
		File f = new File(String.format(format, index));
		if (f.canRead()) {
			return ImageIO.read(f);
		}
		return null;
	}
}
