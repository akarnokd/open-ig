/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.ani;

import hu.openig.ani.Framerates.Rates;
import hu.openig.utils.IOUtils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;


/**
 * Image compression test program.
 * @author karnokd
 */
public final class ImgCompress {
	/** Private constructor. */
	private ImgCompress() {
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
			if (!result.containsKey(i)) {
				result.put(i, idx++);
			}
		}
		
		return result;
	}
	/**
	 * Remaps the raw image int array to raw image byte array using the color map.
	 * Note that colorMap should have &lt;= 256 entries
	 * @param rawImage
	 * @param colorMap
	 * @return
	 */
	static byte[] remapToBytes(int[] rawImage, Map<Integer, Integer> colorMap) {
		byte[] result = new byte[rawImage.length];
		for (int i = 0; i < rawImage.length; i++) {
			result[i] = colorMap.get(rawImage[i]).byteValue();
		}
		return result;
	}
	/**
	 * Reorder bytes as they were a subsequent of square * square segments of the original image.
	 * @param raw
	 * @param scan the scan line length in bytes
	 * @param squareSize
	 * @return
	 */
	static byte[] reorderSquared(byte[] raw, int scan, int squareSize) {
		byte[] result = new byte[raw.length];
		int scanBuckets = scan / squareSize; // the buckect count
		for (int i = 0; i < raw.length; i++) {
			int x = i % scan;
			int y = i / scan;
			
			int xbucket = x / squareSize;
			int xrel = x % squareSize;
			int ybucket = y / squareSize;
			int yrel = y % squareSize;
			int bucketAddr = (ybucket * scanBuckets + xbucket) * squareSize * squareSize;
			result[bucketAddr + yrel * squareSize + xrel] = raw[i];
			
		}
		
		return result;
	}
	/**
	 * Unreorder the square * square segments.
	 * @param raw the reordered image
	 * @param scan the original scan length
	 * @param squareSize the size of the square segments
	 * @return
	 */
	static byte[] unreorderSquared(byte[] raw, int scan, int squareSize) {
		byte[] result = new byte[raw.length];
		int scanBuckets = scan / squareSize; // the buckect count
		for (int i = 0; i < raw.length; i++) {
			int segIdx = i / (squareSize * squareSize);
			int segOffs = i % (squareSize * squareSize);
			int xrel = segOffs % squareSize;
			int yrel = segOffs / squareSize;
			
			int segX = segIdx % scanBuckets;
			int segY = segIdx / scanBuckets;
			
			int addr = (segX * squareSize + xrel) + (segY * squareSize + yrel) * scan;
			result[addr] = raw[i];
		}
		return result;
	}
	/**
	 * Transcode into a differential format.
	 * @param raw the raw data
	 * @return the differentiated value
	 */
	static byte[] differentiate(byte[] raw) {
		byte[] result = new byte[raw.length];
		
		byte last = 0;
		for (int i = 0; i < raw.length; i++) {
			result[i] = (byte)(last - raw[i]);
			last = raw[i];
		}
		
		return result;
	}
	static byte[] undifferentiate(byte[] raw) {
		byte[] result = new byte[raw.length];
		
		byte last = 0;
		for (int i = 0; i < raw.length; i++) {
			result[i] = (byte)(last - raw[i]);
			last = result[i];
		}
		
		return result;
	}
	/** Test various compression methods on one image. */
	static void testMethods() throws IOException {
		File src = new File("c:/games/ighu/youtube/digi060e.ani/digi060e.ani-00000a.png");
		
		BufferedImage img = ImageIO.read(src);
		
		
		int[] raw = new int[img.getWidth() * img.getHeight()];
		
		System.out.printf("Image: %d x %d, %d bytes compressed, %d bytes uncompressed%n", 
				img.getWidth(), img.getHeight(), src.length(), raw.length
				);
		
		img.getRGB(0, 0, img.getWidth(), img.getHeight(), raw, 0, img.getWidth());
		
		Map<Integer, Integer> colors = indexColors(raw);
		
		System.out.printf("  Distinct colors: %d%n", colors.size());
		
		byte[] raw8 = remapToBytes(raw, colors);
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream(raw8.length);
		GZIPOutputStream gout = new GZIPOutputStream(bout);

		gout.write(raw8);
		gout.close();
		
		System.out.printf("Linear%n");
		
		System.out.printf("  GZipped: %d bytes (%.3f %% | %.3f %%)%n", 
				bout.size(), bout.size() * 100f / src.length(),
				bout.size() * 100f / raw8.length);
		
		bout = new ByteArrayOutputStream(raw8.length);
		ZipOutputStream zout = new ZipOutputStream(bout);
		zout.setLevel(9);
		zout.putNextEntry(new ZipEntry(src.getName()));
		zout.write(raw8);
		zout.close();
		
		System.out.printf("  ZIPped: %d bytes (%.3f %% | %.3f %%)%n", 
				bout.size(), bout.size() * 100f / src.length(),
				bout.size() * 100f / raw8.length);
		
		// --------------------------------------------------------------------
		byte[] r1 = reorderSquared(raw8, img.getWidth(), 8);
		byte[] r2 = unreorderSquared(r1, img.getWidth(), 8);
		
		System.out.printf("Reordered: %s%n", Arrays.equals(raw8, r2));
		
		bout = new ByteArrayOutputStream(raw8.length);
		gout = new GZIPOutputStream(bout);

		gout.write(r1);
		gout.close();
		
		System.out.printf("  GZipped: %d bytes (%.3f %% | %.3f %%)%n", 
				bout.size(), bout.size() * 100f / src.length(),
				bout.size() * 100f / r1.length);
		
		bout = new ByteArrayOutputStream(raw8.length);
		zout = new ZipOutputStream(bout);
		zout.setLevel(9);
		zout.putNextEntry(new ZipEntry(src.getName()));
		zout.write(r1);
		zout.close();
		
		System.out.printf("  ZIPped: %d bytes (%.3f %% | %.3f %%)%n", 
				bout.size(), bout.size() * 100f / src.length(),
				bout.size() * 100f / r1.length);
		
		// --------------------------------------------------------------------
		r1 = differentiate(raw8);
		r2 = undifferentiate(r1);
		
		System.out.printf("Differentiated: %s%n", Arrays.equals(raw8, r2));
		
		bout = new ByteArrayOutputStream(raw8.length);
		gout = new GZIPOutputStream(bout);

		gout.write(r1);
		gout.close();
		
		System.out.printf("  GZipped: %d bytes (%.3f %% | %.3f %%)%n", 
				bout.size(), bout.size() * 100f / src.length(),
				bout.size() * 100f / r1.length);
		
		bout = new ByteArrayOutputStream(raw8.length);
		zout = new ZipOutputStream(bout);
		zout.setLevel(9);
		zout.putNextEntry(new ZipEntry(src.getName()));
		zout.write(r1);
		zout.close();
		
		System.out.printf("  ZIPped: %d bytes (%.3f %% | %.3f %%)%n", 
				bout.size(), bout.size() * 100f / src.length(),
				bout.size() * 100f / r1.length);
		
		// --------------------------------------------------------------------
		r1 = differentiate(differentiate(raw8));
		r2 = undifferentiate(undifferentiate(r1));
		
		System.out.printf("Differentiated x 2: %s%n", Arrays.equals(raw8, r2));
		
		bout = new ByteArrayOutputStream(raw8.length);
		gout = new GZIPOutputStream(bout);

		gout.write(r1);
		gout.close();
		
		System.out.printf("  GZipped: %d bytes (%.3f %% | %.3f %%)%n", 
				bout.size(), bout.size() * 100f / src.length(),
				bout.size() * 100f / r1.length);
		
		bout = new ByteArrayOutputStream(raw8.length);
		zout = new ZipOutputStream(bout);
		zout.setLevel(9);
		zout.putNextEntry(new ZipEntry(src.getName()));
		zout.write(r1);
		zout.close();
		
		System.out.printf("  ZIPped: %d bytes (%.3f %% | %.3f %%)%n", 
				bout.size(), bout.size() * 100f / src.length(),
				bout.size() * 100f / r1.length);
		// --------------------------------------------------------------------
	}
	static void doDifferentialAniCoding(String imageSeqName, int numLength, String imageSeqExt) throws IOException {
		System.out.printf("%s-%d%s%n", imageSeqName, numLength, imageSeqExt);
		int i = 0;
		int w = 0;
		int h = 0;
		long originalSizes = 0;
		Map<Integer, Integer> palette = new HashMap<Integer, Integer>();
		Map<Integer, Integer> paletteIndex = new HashMap<Integer, Integer>();
		while (true) {
			File src = new File(String.format("%s-%0" + numLength + "d%s", imageSeqName, i, imageSeqExt));
			if (!src.canRead()) {
				break;
			}
			originalSizes += src.length();
			BufferedImage img = ImageIO.read(src);
			w = img.getWidth();
			h = img.getHeight();
			int[] raw = new int[img.getWidth() * img.getHeight()];
			img.getRGB(0, 0, img.getWidth(), img.getHeight(), raw, 0, img.getWidth());
			int cidx = 0;
			for (int j = 0; j < raw.length; j++) {
				if (!palette.containsKey(raw[j])) {
					palette.put(raw[j], cidx);
					paletteIndex.put(cidx, raw[j]);
					cidx++;
				}
			}
			i++;
			if (i % 100 == 0) {
				System.out.println("|");
			} else
			if (i % 10 == 0) {
				System.out.print("*");
			} else {
				System.out.print(".");
			}
		}
		int cnt = i;
		System.out.printf("%nColors: %d%n", palette.size());
		// store common palette
		File dst = new File(imageSeqName);
		String n = dst.getName();
		dst = dst.getParentFile().getParentFile();
		dst = new File(dst, n + "2009.GZ");
		OutputStream out = new GZIPOutputStream(new FileOutputStream(dst), 1024 * 1024);
		writeLEInt(out, w);
		writeLEInt(out, h);
		writeLEInt(out, cnt);
		Rates r = new Framerates().getRates(new File(imageSeqName).getName(), 1);
		writeLEInt(out, (int)(r.fps * 1000)); // number of frames per second * 1000
		for (int j = 0; j < paletteIndex.size(); j++) {
			int c = paletteIndex.get(j);
			out.write((c & 0xFF0000) >> 16);
			out.write((c & 0xFF00) >> 8);
			out.write((c & 0xFF) >> 0);
		}
		File audio = new File(imageSeqName + ".wav");
		includeAudio(out, audio);
		// begin differential store
		byte[] lastRaw8 = null;
		i = 0;
		while (true) {
			File src = new File(String.format("%s-%0" + numLength + "d%s", imageSeqName, i, imageSeqExt));
			if (!src.canRead()) {
				break;
			}
			BufferedImage img = ImageIO.read(src);
			w = img.getWidth();
			h = img.getHeight();
			int[] raw = new int[img.getWidth() * img.getHeight()];
			img.getRGB(0, 0, img.getWidth(), img.getHeight(), raw, 0, img.getWidth());
			byte[] raw8 = remapToBytes(raw, palette);
			if (lastRaw8 == null) {
				out.write(raw8);
			} else {
				byte[] altered = raw8.clone();
				// replace the pixels which are the same on both images with 255
				for (int j = 0; j < raw8.length; j++) {
					if (raw8[j] == lastRaw8[j]) {
						altered[j] = -1;
					}
				}
				out.write(altered);
			}
			lastRaw8 = raw8;
			i++;
			if (i % 100 == 0) {
				System.out.println("|");
			} else
			if (i % 10 == 0) {
				System.out.print("*");
			} else
			System.out.print(".");
		}		
		out.close();
		System.out.printf("%nOriginal: %d, Compressed: %d, Ratio: %.3f%n", originalSizes, dst.length(), (dst.length() * 100d / originalSizes));
	}
	/**
	 * Adds the given raw audio data into the output stream starting with a length int then the raw data
	 * @param out
	 * @param audio
	 */
	private static void includeAudio(OutputStream out, File audio) throws IOException {
		System.out.printf("%s", audio);
		if (audio.canRead()) {
			byte[] data = IOUtils.load(audio);
			int len = 0;
			int offset = 0;
			// locate the 'data' segment
			for (int i = 0; i < data.length - 4; i++) {
				if (data[i] == 'd' && data[i + 1] == 'a' && data[i + 2] == 't' && data[i + 3] == 'a') {
					for (int j = 0; j < 4; j++) {
						len |= ((data[i + j + 4] & 0xFF) << (j * 8));
					}
					offset = i + 8;
					break;
				}
			}
			if (len > 0) {
				System.out.printf(": Audio: %d - %d%n", offset, len);
				writeLEInt(out, len);
				out.write(data, offset, len);
				return;
			}
		}
		System.out.println(": No audio");
		out.write(0);
		out.write(0);
		out.write(0);
		out.write(0);
	}
	static void writeLEInt(OutputStream out, int value) throws IOException {
		out.write((value & 0xFF) >> 0);
		out.write((value & 0xFF00) >> 8);
		out.write((value & 0xFF0000) >> 16);
		out.write((value & 0xFF000000) >> 24);
	}
	/**
	 * Main program.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
//		testMethods();
		File[] files = new File("c:/games/ighu/youtube").listFiles();
		if (files != null) {
			ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			for (File f : files) {
				final String name = f.getAbsolutePath() + "/" + f.getName();
				exec.submit(new Runnable() {
					@Override
					public void run() {
						try {
							doDifferentialAniCoding(name, 5, ".png");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			}
			exec.shutdown();
		}
	}

}
