/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.xold.ani;

import hu.openig.xold.ani.SpidyAniDecoder.SpidyAniCallback;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;


/**
 * Image compression test program.
 * @author karnokd
 */
public final class VideoCompress {
	/** Private constructor. */
	private VideoCompress() {
		// utility program
	}
	/**
	 * Indexes the colors on the raw image.
	 * @param rawImage the raw RGBA image
	 * @return a map from RGB int to color index.
	 */
	static Map<Integer, Integer> indexColors(int[] rawImage) {
		Map<Integer, Integer> result = new HashMap<Integer, Integer>(256);
		int idx = 0;
		for (int i : rawImage) {
			if ((i & 0xFF000000) == 0xFF000000 && !result.containsKey(i)) {
				result.put(i, idx++);
			}
		}
		
		return result;
	}
	/**
	 * Remaps the raw image int array to raw image byte array using the color map.
	 * Note that colorMap should have &lt;= 256 entries
	 * @param rawImage the raw RGB array
	 * @param colorMap the color remapper from color to index
	 * @return the index map
	 */
	static byte[] remapToBytes(int[] rawImage, Map<Integer, Integer> colorMap) {
		byte[] result = new byte[rawImage.length];
		for (int i = 0; i < rawImage.length; i++) {
			if (rawImage[i] != 0) {
				result[i] = colorMap.get(rawImage[i]).byteValue();
			} else {
				result[i] = -1;
			}
		}
		return result;
	}
	/**
	 * Writes an integer value in little endian order into the output stream.
	 * @param out the output stream
	 * @param value the value
	 * @throws IOException if an error occurs
	 */
	static void writeLEInt(OutputStream out, int value) throws IOException {
		out.write((value & 0xFF) >> 0);
		out.write((value & 0xFF00) >> 8);
		out.write((value & 0xFF0000) >> 16);
		out.write((value & 0xFF000000) >> 24);
	}
	/**
	 * Differential encode the given raw file filename.
	 * @param filename the path and name prefix of the images
	 * @throws IOException if an I/O error occurs
	 */
	public static void doDifferentialAniCoding(String filename) throws IOException {
		RawAni ra = new RawAni(filename);
		int w = ra.width;
		int h = ra.height;
		long originalSizes = ra.raf.length();
		// store common palette
		File dst = new File(filename + ".gz");
		OutputStream out = new GZIPOutputStream(new FileOutputStream(dst), 1024 * 1024);
		writeLEInt(out, w);
		writeLEInt(out, h);
		writeLEInt(out, ra.frames);
		writeLEInt(out, (int)(ra.fps * 1000)); // number of frames per second * 1000
		
		// begin differential store
		int[] lastRaw = null;
		Map<Integer, Integer> currentMap = new HashMap<Integer, Integer>(256);
		for (int i = 0; i < ra.frames; i++) {
			int[] raw = new int[w * h];
			ra.readFrame(i, raw);
			
			Map<Integer, Integer> imagePal = indexColors(raw);
			if (!imagePal.keySet().equals(currentMap.keySet())) {
				out.write('P'); // indication for palette segment
				if (imagePal.size() > 255) {
					// find the least used color, then find the closest color, then replace it
					int[] occurs = new int[imagePal.size()];
					int[] ocolor = new int[imagePal.size()];
					for (int j = 0; j < raw.length; j++) {
						occurs[imagePal.get(raw[i])]++;
						ocolor[imagePal.get(raw[i])] = raw[i];
					}
					int min = Integer.MAX_VALUE;
					int minColor = 0;
					for (int j = 0; j < occurs.length; j++) {
						if (occurs[j] < min) {
							min = occurs[j];
							minColor = ocolor[j];
						}
					}
					long mind = Long.MAX_VALUE;
					int minc = 0;
					for (int j = 0; j < ocolor.length; j++) {
						long d = distance(minColor, ocolor[j]);
						if (d > 0 && mind > d) {
							mind = d;
							minc = ocolor[j];
						}
					}
					for (int j = 0; j < raw.length; j++) {
						if (raw[j] == minColor) {
							raw[j] = minc;
						}
					}
					imagePal = indexColors(raw);
					System.err.println("Too much color: " + imagePal.size() + " @ " + filename);
					System.err.printf("%d | Color %08X replaced with %08X%n", i, minColor, minc);
//					break;
				}
				out.write(imagePal.size());
				Map<Integer, Integer> reverse = new HashMap<Integer, Integer>(imagePal.size() + 1);
				for (Map.Entry<Integer, Integer> e : imagePal.entrySet()) {
					reverse.put(e.getValue(), e.getKey());
				}
				for (int j = 0; j < reverse.size(); j++) {
					int c = reverse.get(j);
					out.write((c & 0xFF0000) >> 16);
					out.write((c & 0xFF00) >> 8);
					out.write((c & 0xFF) >> 0);
				}
				currentMap = imagePal;
			}
			out.write('I'); // indication for image segment
			
			if (lastRaw == null) {
				out.write(remapToBytes(raw, currentMap));
			} else {
				int[] altered = raw.clone();
				// replace the pixels which are the same on both images with 255
				for (int j = 0; j < raw.length; j++) {
					if (raw[j] == lastRaw[j]) {
						altered[j] = 0;
					}
				}
				out.write(remapToBytes(altered, currentMap));
			}
			lastRaw = raw;
		}
		out.write('X');
		out.close();
		System.out.printf("%n%s | Original: %d, Compressed: %d, Ratio: %.3f%n", dst.getName(), originalSizes, dst.length(), (dst.length() * 100d / originalSizes));
	}
	/**
	 * Compute the distance square between two RGBA colors.
	 * @param rgb1 the first color
	 * @param rgb2 the second color
	 * @return the distance
	 */
	static long distance(int rgb1, int rgb2) {
		int r1 = (rgb1 & 0xFF0000) >> 16;
		int g1 = (rgb1 & 0xFF00) >> 8;
		int b1 = (rgb1 & 0xFF) >> 0;
		int r2 = (rgb1 & 0xFF0000) >> 16;
		int g2 = (rgb1 & 0xFF00) >> 8;
		int b2 = (rgb1 & 0xFF) >> 0;
		return (r1 * r1 + b1 * b1 + g1 * g1 - (r2 * r2 + b2 * b2 + g2 * g2));
	}
	/**
	 * Decompress the image to the raw format again.
	 * @param in the input stream
	 * @param out the output stream 
	 * @throws IOException on IO error
	 */
	public static void decompressToRaw(DataInputStream in, DataOutputStream out) throws IOException {
		int w = Integer.reverseBytes(in.readInt());
		int h = Integer.reverseBytes(in.readInt());
		int frames = Integer.reverseBytes(in.readInt());
		int fps = Integer.reverseBytes(in.readInt());
		
		out.writeShort(w);
		out.writeShort(h);
		out.writeShort(frames);
		out.writeShort(fps);
		
		int[] palette = new int[256];
//		int[] currimage = new int[w * h];
		byte[] bytebuffer = new byte[w * h];
		
		int c = 0;
		while ((c = in.read()) != -1) {
			if (c == 'X') {
				break;
			} else
			if (c == 'P') {
				int len = in.read();
				for (int j = 0; j < len; j++) {
					palette[j] = 0xFF;
					for (int k = 0; k < 3; k++) {
						palette[j] = (palette[j] << 8) | in.read();
					}
				}
			} else
			if (c == 'I') {
				in.read(bytebuffer);
				for (int i = 0; i < bytebuffer.length; i++) {
					int c0 = palette[bytebuffer[i] & 0xFF];
//					if (c0 == 0) {
//						currimage[i] = lastimage[i];
//					} else {
//						currimage[i] = c0;
//					}
//					out.writeInt(currimage[i]);
					out.writeInt(c0);
				}
				
			}
		}
		in.close();
		out.close();
	}
	/**
	 * Main program.
	 * @param args the arguments
	 * @throws Exception on error
	 */
	public static void main(String[] args) throws Exception {
		transcode("c:/games/ig/anim/1_HID.ANI", "c:/games/1_hid.ani");
		transcode("c:/games/ig/anim/2_HID.ANI", "c:/games/2_hid.ani");
		transcode("c:/games/ig/anim/3_HID.ANI", "c:/games/3_hid.ani");
		transcode("c:/games/ig/anim/1_HID0.ANI", "c:/games/1_hid0.ani");
		transcode("c:/games/ig/anim/2_HID0.ANI", "c:/games/2_hid0.ani");
		transcode("c:/games/ig/anim/3_HID0.ANI", "c:/games/3_hid0.ani");
		transcode("c:/games/ig/anim/1_HIDF.ANI", "c:/games/1_hidf.ani");
		transcode("c:/games/ig/anim/2_HIDF.ANI", "c:/games/2_hidf.ani");
		transcode("c:/games/ig/anim/3_HIDF.ANI", "c:/games/3_hidf.ani");
	}
	/**
	 * Transcodes the given original ANI into the new video format.
	 * @param filename the input ANI
	 * @param outFile the output new ANI
	 * @throws IOException on error
	 */
	private static void transcode(final String filename, final String outFile)
			throws IOException {
		SpidyAniDecoder.decodeLoop(new SpidyAniCallback() {
			RandomAccessFile raf;
			byte[] buffer;
			@Override
			public void audioData(byte[] data) {
				
			}

			@Override
			public void fatal(Throwable t) {
				
			}

			@Override
			public void finished() {
				try {
					raf.close();
				} catch (IOException ex) {
					
				}
			}

			@Override
			public String getFileName() {
				return filename;
			}

			@Override
			public InputStream getNewInputStream() {
				try {
					return new FileInputStream(filename);
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}

			@Override
			public void imageData(int[] image) {
				try {
					for (int i = 0; i < image.length; i++) {
						int c = image[i];
						buffer[i * 4 + 0] = (byte)((c & 0xFF000000) >> 24); 
						buffer[i * 4 + 1] = (byte)((c & 0xFF0000) >> 16); 
						buffer[i * 4 + 2] = (byte)((c & 0xFF00) >> 8); 
						buffer[i * 4 + 3] = (byte)((c & 0xFF) >> 0); 
					}
					raf.write(buffer);
				} catch (IOException ex) {
					
				}
			}

			@Override
			public void initialize(int width, int height, int frames,
					int languageCode, double fps, int audioDelay) {
				try {
					raf = new RandomAccessFile(outFile, "rw");
					raf.writeShort(width);
					raf.writeShort(height);
					raf.writeShort(frames);
					raf.writeShort((short)(fps * 1000));
					buffer = new byte[4 * width * height];
				} catch (IOException ex) {
					
				}
			}

			@Override
			public boolean isPaused() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isStopped() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void stopped() {
				// TODO Auto-generated method stub
				
			}
			
		});
		doDifferentialAniCoding(outFile);
	}

}
