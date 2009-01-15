/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.utils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * PCX Image decoder.<p>
 * Format:<br>
 * <pre>
 * </pre> 
 *
 */
public class PCXImage {
	/**
	 * Header size: 128 bytes 
	 */
	static class Header {
		public byte manufacturer; // 0x0A
		public byte version;
		public byte encoding; // 0x01
		public byte bitsperpixel;
		public int xmin; // word
		public int ymin; // word
		public int xmax; // word
		public int ymax; // word
		public int getWidth() { return xmax - xmin + 1; }
		public int getHeight() { return ymax - ymin + 1; }
		public int vertdpi;
		public byte[] palette = new byte[48];
		private Map<Integer, Color> colormap = new HashMap<Integer, Color>();
		public Color colorOf(int index) { 
			Color c = colormap.get(index);
			if (c == null) {
				c = new Color(palette[index * 3] & 0xFF, 
						palette[index * 3 + 1] & 0xFF, palette[index * 3 + 2] & 0xFF);
				colormap.put(index, c);
			}
			return c;  
		}
		public int rgb(int index) {
			return (palette[index * 3] & 0xFF) << 16 |
			(palette[index * 3 + 1] & 0xFF) << 8 | (palette[index * 3 + 2] & 0xFF) << 0;
		}
		//public byte reserved;
		public byte colorplanes;
		public int bytesperline; // word
		public int palettetype; // word
		public int hscrsize; // word
		public int vscrsize; // word
		public int getMaxNumberOfColors() { return 1 << (bitsperpixel * colorplanes); }
		public int getScanLineLength() { return colorplanes * bytesperline; }
		public int getLinePaddingSize() { return (getScanLineLength() * 8 / bitsperpixel - getWidth()); }
		//public byte[] filler = new byte[56]
		public static void parse(byte[] data, Header h) {
			h.version = data[1];
			h.bitsperpixel = data[3];
			h.xmin = (data[4] & 0xFF) | (data[5] & 0xFF) << 8;
			h.ymin = (data[6] & 0xFF) | (data[7] & 0xFF) << 8;
			h.xmax = (data[8] & 0xFF) | (data[9] & 0xFF) << 8;
			h.ymax = (data[10] & 0xFF) | (data[11] & 0xFF) << 8;
			System.arraycopy(data, 16, h.palette, 0, 48);
			h.colorplanes = data[65];
			h.bytesperline = (data[66] & 0xFF) | (data[67] & 0xFF) << 8;
			h.palettetype = (data[68] & 0xFF) | (data[69] & 0xFF) << 8;
		}
	}
	/**
	 * Parses the byte array and returns a buffered image containing the PCX image.
	 * @param data the data array
	 * @param transparentRGB the optional transparency color or -1 none, -2 for set transparency based on
	 * the original color index 0, -3 for index 1, etc.
	 * @return the buffered image
	 */
	public static BufferedImage parse(byte[] data, int transparentRGB) {
		Header h = new Header();
		Header.parse(data, h);
		int src = 128;
		int dst = 0;
		int xmax = h.getWidth();
		int ymax = h.getHeight();
		int[] image = new int[xmax * ymax];
		int sll = h.getScanLineLength();
		byte[] scan = new byte[sll * ymax];
		// check for 256 color VGA palette
		int y = 0;
		while (src < data.length && dst < scan.length) {
			while (dst < scan.length) {
				byte b = data[src++];
				byte cnt = 1;
				if ((b & 0xC0) == 0xC0) {
					cnt = (byte)(b & 0x3F);
					b = data[src++];
				}
				// process line only if it is still within range;
				if (y < ymax) {
					Arrays.fill(scan, dst, dst + cnt, b);
				}
				dst += cnt;
			}
			y++;
		}
		if (src == data.length - 769 && data[src] == (byte)0x0C) {
			// fix palette
			h.palette = new byte[768];
			System.arraycopy(data, src + 1, h.palette, 0, h.palette.length);
			src += 1 + h.palette.length;
		}
		boolean isrgb = h.colorplanes == 3 && h.bitsperpixel == 8;
		// decode every pixel data
		int c = 0;
		for (y = 0; y < ymax; y++) {
			for (int x = 0; x < xmax; x++) {
				switch (h.colorplanes) {
				case 1:
					switch (h.bitsperpixel) {
					case 1:
						c = scan[sll * y + x / 8] & (1 << (x % 8));
						break;
					case 2:
						c = scan[sll * y + x / 4] & (3 << ((x % 4) * 2));
						break;
					case 8:
						c = scan[sll * y + x] & 0xFF;
						break;
					}
					break;
				case 3:
					switch (h.bitsperpixel) {
//					case 1:
//						c = (scan[sll * y + x / 8] & (1 << (x % 8)))
//						| (scan[sll * y + xmax / 2 + x / 8] & (1 << (x % 8))) << 1
//						| (scan[sll * y + xmax + x / 8] & (1 << (x % 8))) << 2
//						;
//						break;
					case 8:
						c = (scan[sll * y + x] & 0xFF) << 16
						| (scan[sll * y + xmax + x] & 0xFF) << 8
						| (scan[sll * y + 2 * xmax + x] & 0xFF) << 0
						;
						break;
					}
					break;
//				case 4:
//					// assume 1 bpp
//					c = (scan[sll * 4 * y + x / 8] & (1 << (x % 8)))
//					| (scan[sll * (4 * y + 1) + x / 8] & (1 << (x % 8))) << 1
//					| (scan[sll * (4 * y + 2) + x / 8] & (1 << (x % 8))) << 2
//					| (scan[sll * (4 * y + 3) + x / 8] & (1 << (x % 8))) << 3
//					;
//					break;
				}
				if (isrgb) {
					if (c != transparentRGB) {
						image[y * xmax + x] = 0xFF000000 | c;
					}
				} else {
					int d = h.rgb(c);
					if (transparentRGB == -1 || (transparentRGB >= 0 && d != transparentRGB) || (transparentRGB < -1 && c != -transparentRGB - 2)) {
						image[y * xmax + x] = 0xFF000000 | d;
					}
				}
			}
		}
		BufferedImage bi = new BufferedImage(h.getWidth(), h.getHeight(), BufferedImage.TYPE_INT_ARGB);
		bi.setRGB(0, 0, xmax, ymax, image, 0, xmax);
		return bi;
	}
	/** Loads an image from the specified filename. */
	public static BufferedImage from(String f, int transparentRGB) {
		return parse(IOUtils.load(f), transparentRGB);
	}
	public static void main(String[] args) throws Exception {
		File f = new File("me.PCX");
		byte[] data = new byte[(int)f.length()];
		DataInputStream fin = new DataInputStream(new FileInputStream(f));
		fin.readFully(data);
		fin.close();
		JFrame fm = new JFrame("Image of " + f);
		fm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		fm.getContentPane().add(new JLabel(new ImageIcon(parse(data, -1))));
		fm.pack();
		fm.setVisible(true);
		
	}
}
