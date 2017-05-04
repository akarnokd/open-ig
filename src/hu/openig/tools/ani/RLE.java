/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools.ani;

import java.util.Arrays;

/**
 * Decompression utility to decompress custom RLE (Run Length Encoding) formatted 
 * 8 bit per pixel images into RGBA format images.
 * Data streams. It provides two algorithms for this.
 * @author karnokd, 2009.01.11.
 * @version $Revision 1.0$
 */
public final class RLE {
	/** Private constructor. */
	private RLE() {
		throw new AssertionError("Utility class!");
	}
	/**
	 * Decompresses the given data stream into an RGBA image using one type of
	 * RLE algorithm. In this algorithm, the codes 0..127 mean literal value,
	 * 128..191 means skip instruction, 192..255 means repeat a color.
	 * @param data the input data array
	 * @param src the source index to read from
	 * @param out the output image array
	 * @param dst the output index to write to
	 * @param pal the palette decoder
	 * @return the destination index pointer after the decompression completed
	 */
	public static int decompress1(byte[] data, int src, int[] out, 
			int dst, PaletteDecoder pal) {
		while (src < data.length && dst < out.length) {
			int c = data[src++] & 0xFF;
			if (c >= 0xC0) {
				if (c == 0xC0) {
					int x = (data[src++] & 0xFF) | (data[src++] & 0xFF) << 8;
					c = data[src++] & 0xFF;
					Arrays.fill(out, dst, dst + x, pal.getColor(c));
					dst += x;
				} else {
					int x = c & 0x3F;
					c = data[src++] & 0xFF;
					Arrays.fill(out, dst, dst + x, pal.getColor(c));
					dst += x;
				}
			} else
			if (c >= 0x80) {
				if (c == 0x80) {
					int x = (data[src++] & 0xFF) | (data[src++] & 0xFF) << 8;
					dst += + x;
				} else {
					dst += c & 0x3F;
				}
			} else {
				out[dst++] = pal.getColor(c);
			}
		}
		return dst;
	}
	/**
	 * Decompresses the given data stream into an RGBA image using one type of
	 * RLE algorithm. In this algorithm, the codes 0..127 means copy N following 
	 * bytes as literal,
	 * 128..191 means skip instruction, 192..255 means repeat a color.
	 * @param data the input data array
	 * @param src the source index to read from
	 * @param out the output image array
	 * @param dst the output index to write to
	 * @param pal the palette decoder
	 * @return the destination index pointer after the decompression completed
	 */
	public static int decompress2(byte[] data, int src, int[] out, 
			int dst, PaletteDecoder pal) {
		while (src < data.length && dst < out.length) {
			int c = data[src++] & 0xFF;
			if (c >= 0xC0) {
				if (c == 0xC0) {
					int x = (data[src++] & 0xFF) | (data[src++] & 0xFF) << 8;
					c = data[src++] & 0xFF;
					Arrays.fill(out, dst, dst + x, pal.getColor(c));
					dst += x;
				} else {
					int x = c & 0x3F;
					c = data[src++] & 0xFF;
					Arrays.fill(out, dst, dst + x, pal.getColor(c));
					dst += x;
				}
			} else
			if (c >= 0x80) {
				if (c == 0x80) {
					int x = (data[src++] & 0xFF) | (data[src++] & 0xFF) << 8;
					dst += x;
				} else {
					dst += c & 0x3F;
				}
			} else
			if (c == 0) {
				int x = (data[src++] & 0xFF) | (data[src++] & 0xFF) << 8;
				for (int i = 0; i < x; i++) {
					c = data[src++] & 0xFF;
					out[dst++] = pal.getColor(c);
				}
			} else {
				int x = c;
				for (int i = 0; i < x; i++) {
					c = data[src++] & 0xFF;
					out[dst++] = pal.getColor(c);
				}
			}
		}
		return dst;
	}
	/**
	 * Decompresses the given data stream into an indexed image using one type of
	 * RLE algorithm. In this algorithm, the codes 0..127 mean literal value,
	 * 128..191 means skip instruction, 192..255 means repeat a color.
	 * @param data the input data array
	 * @param src the source index to read from
	 * @param out the output image array
	 * @param dst the output index to write to
	 * @return the destination index pointer after the decompression completed
	 */
	public static int decompress1(byte[] data, int src, byte[] out, 
			int dst) {
		while (src < data.length && dst < out.length) {
			int c = data[src++] & 0xFF;
			if (c >= 0xC0) {
				if (c == 0xC0) {
					int x = (data[src++] & 0xFF) | (data[src++] & 0xFF) << 8;
					c = data[src++] & 0xFF;
					Arrays.fill(out, dst, dst + x, (byte)c);
					dst += x;
				} else {
					int x = c & 0x3F;
					c = data[src++] & 0xFF;
					Arrays.fill(out, dst, dst + x, (byte)c);
					dst += x;
				}
			} else
			if (c >= 0x80) {
				if (c == 0x80) {
					int x = (data[src++] & 0xFF) | (data[src++] & 0xFF) << 8;
					dst += + x;
				} else {
					dst += c & 0x3F;
				}
			} else {
				out[dst++] = (byte)c;
			}
		}
		return dst;
	}
	/**
	 * Decompresses the given data stream into an indexed image using one type of
	 * RLE algorithm. In this algorithm, the codes 0..127 means copy N following 
	 * bytes as literal,
	 * 128..191 means skip instruction, 192..255 means repeat a color.
	 * @param data the input data array
	 * @param src the source index to read from
	 * @param out the output image array
	 * @param dst the output index to write to
	 * @return the destination index pointer after the decompression completed
	 */
	public static int decompress2(byte[] data, int src, byte[] out, 
			int dst) {
		while (src < data.length && dst < out.length) {
			int c = data[src++] & 0xFF;
			if (c >= 0xC0) {
				if (c == 0xC0) {
					int x = (data[src++] & 0xFF) | (data[src++] & 0xFF) << 8;
					c = data[src++] & 0xFF;
					Arrays.fill(out, dst, dst + x, (byte)c);
					dst += x;
				} else {
					int x = c & 0x3F;
					c = data[src++] & 0xFF;
					Arrays.fill(out, dst, dst + x, (byte)c);
					dst += x;
				}
			} else
			if (c >= 0x80) {
				if (c == 0x80) {
					int x = (data[src++] & 0xFF) | (data[src++] & 0xFF) << 8;
					dst += x;
				} else {
					dst += c & 0x3F;
				}
			} else
			if (c == 0) {
				int x = (data[src++] & 0xFF) | (data[src++] & 0xFF) << 8;
				for (int i = 0; i < x; i++) {
					c = data[src++] & 0xFF;
					out[dst++] = (byte)c;
				}
			} else {
				int x = c;
				for (int i = 0; i < x; i++) {
					c = data[src++] & 0xFF;
					out[dst++] = (byte)c;
				}
			}
		}
		return dst;
	}
}
