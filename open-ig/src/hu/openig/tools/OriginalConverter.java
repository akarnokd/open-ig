/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.tools.ani.Framerates;
import hu.openig.tools.ani.Framerates.Rates;
import hu.openig.tools.ani.SpidyAniFile;
import hu.openig.tools.ani.SpidyAniFile.Block;
import hu.openig.tools.ani.SpidyAniFile.Sound;
import hu.openig.utils.IOUtils;
import hu.openig.utils.PCXImage;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Takes an original game install and extracts all language-specific resource, then names
 * them correctly.
 * @author akarnokd, 2012.10.20.
 */
public final class OriginalConverter {
	/** Utility class. */
	private OriginalConverter() { }
	/** The source directory. */
	static String source;
	/** The destination directory. */
	static String destination;
	/** The conversion instructions. */
	static XElement instructions;
	/** The target language. */
	static String language;
	/**
	 * Convert an SMP file into a WAV file.
	 * @param src the source file
	 * @param dst the destination WAV
	 * @throws IOException on error
	 */
	static void convertSMP(File src, File dst) throws IOException {
		dst.getParentFile().mkdirs();
		
		byte[] sample = IOUtils.load(src);
		if (sample.length == 0) {
			return;
		}
		
		writeWav(dst, sample);
	}
	/**
	 * Writes the byte data as a wav file.
	 * @param dst the destination file
	 * @param sample the sample
	 * @throws IOException on error
	 */
	protected static void writeWav(File dst, byte[] sample)
			throws IOException {

		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dst), 1024 * 1024));
		try {
			writeWav(sample, dout);
		} finally {
			dout.close();
		}
	}
	/**
	 * Write the wav into an output stream.
	 * @param sample the sample
	 * @param dout the stream
	 * @throws IOException on error
	 */
	public static void writeWav(byte[] sample, DataOutputStream dout)
			throws IOException {
		int dataLen = sample.length + (sample.length % 2 == 0 ? 0 : 1);
		// HEADER
		dout.write("RIFF".getBytes("ISO-8859-1"));
		dout.writeInt(Integer.reverseBytes(36 + dataLen)); // chunk size
		dout.write("WAVE".getBytes("ISO-8859-1"));
		
		// FORMAT
		dout.write("fmt ".getBytes("ISO-8859-1"));
		dout.writeInt(Integer.reverseBytes(16)); // chunk size
		dout.writeShort(Short.reverseBytes((short)1)); // Format: PCM = 1
		dout.writeShort(Short.reverseBytes((short)1)); // Channels = 1
		dout.writeInt(Integer.reverseBytes(22050)); // Sample Rate = 22050
		dout.writeInt(Integer.reverseBytes(22050)); // Byte Rate = 22050
		dout.writeShort(Short.reverseBytes((short)1)); // Block alignment = 1
		dout.writeShort(Short.reverseBytes((short)8)); // Bytes per sample = 8

		// DATA
		dout.write("data".getBytes("ISO-8859-1"));
		dout.writeInt(Integer.reverseBytes(dataLen));
		for (int i = 0; i < sample.length; i++) {
			dout.write(sample[i] + 128);
		}
		for (int i = sample.length; i < dataLen; i++) {
			dout.write(0x80);
		}
		dout.flush();
	}
	/**
	 * Create a renamed copy of the source file.
	 * @param src the source
	 * @param dst the destination
	 */
	static void copyFile(File src, File dst) {
		dst.getParentFile().mkdirs();
		byte[] srcData = IOUtils.load(src);
		if (srcData.length > 0) {
			IOUtils.save(dst, srcData);
		}
	}
	/**
	 * Extract the sound from an original ANI file.
	 * @param src the source file
	 * @param dst the destination file
	 */
	static void convertANISound(File src, File dst) {
		byte[] sample = extractWav(src);
		dst.getParentFile().mkdirs();
		try {
			writeWav(dst, sample);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Extract the wav from the given ANI file.
	 * @param src the source file
	 * @return the byte data
	 */
	public static byte[] extractWav(File src) {
		List<byte[]> data = U.newArrayList();
		int len = 0;
		
		final SpidyAniFile saf = new SpidyAniFile();
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(src), 1024 * 1024);
			try {
				saf.open(in);
				saf.load();
				
				Framerates fr = new Framerates();
				Rates r = fr.getRates(src.getName().toUpperCase(), saf.getLanguageCode());
				int delay = (int)(r.delay / r.fps * 22050);
				
				byte[] silence = new byte[delay];
				for (int i = 0; i < silence.length; i++) {
					silence[i] = 0;
				}
				data.add(silence);
				len += delay;
				
				while (true) {
					Block b = saf.next();
					if (b instanceof Sound) {
						data.add(b.data.clone());
						len += b.data.length;
					}
				}
				
			} finally {
				in.close();
			}
		} catch (IOException ex) {
			// ignored
		}
		if (len % 2 == 1) {
			byte[] pad = new byte[] { 0 };
			data.add(pad);
			len++;
		}
		byte[] sample = new byte[len];
		len = 0;
		for (byte[] d : data) {
			System.arraycopy(d, 0, sample, len, d.length);
			len += d.length;
		}
		return sample;
	}
	/**
	 * Extract a subimage from the given image and save it as PNG.
	 * @param img the image
	 * @param x the X
	 * @param y the Y
	 * @param width the width
	 * @param height the height
	 * @param dst the destination filename
	 */
	static void extractImage(BufferedImage img, int x, int y, int width, int height, File dst) {
		dst.getParentFile().mkdirs();
		try {
			ImageIO.write(img.getSubimage(x, y, width, height), "png", dst);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * 
	 * @param args no arguments
	 * @throws Exception ignored.
	 */
	public static void main(String[] args) throws Exception {
		
		instructions = XElement.parseXML(OriginalConverter.class.getResource("originalconverter.xml"));
		
		source = "c:/games/igfr/";
		destination = "c:/games/open_igfr/";
		language = "fr";
		
		for (XElement xsmp : instructions.childrenWithName("smp")) {
			String src = xsmp.get("src");
			String dst = xsmp.get("dst");
			System.out.printf("SMP: %s -> %s%n", src, dst);
			convertSMP(new File(source + src), createDestination("audio", dst));
		}
		for (XElement xcopy : instructions.childrenWithName("copy-audio")) {
			String src = xcopy.get("src");
			String dst = xcopy.get("dst");
			System.out.printf("COPY: %s -> %s%n", src, dst);
			copyFile(new File(source + src), createDestination(xcopy.get("type"), dst));
		}
		for (XElement xaa : instructions.childrenWithName("ani-sound")) {
			String src = xaa.get("src");
			String dst = xaa.get("dst");
			File srcFile = new File(source + src);
			if (srcFile.canRead()) {
				System.out.printf("ANI-SOUND: %s -> %s%n", src, dst);
				convertANISound(srcFile, createDestination("audio", dst));
			} else {
				System.err.printf("ANI-SOUND: %s missing%n", src, dst);
				System.err.flush();
			}
		}
		for (XElement ximg : instructions.childrenWithName("image")) {
			String src = ximg.get("src");
			BufferedImage img = null;
			File f = new File(source + src);
			if (f.canRead()) {
				if (src.toLowerCase().endsWith(".pcx")) {
					img = PCXImage.from(f, -1);
				} else {
					img = ImageIO.read(f);
				}
				for (XElement area : ximg.childrenWithName("area")) {
					String[] coords = U.split(area.get("coords"), ",");
					String dst = area.get("dst");
					
					extractImage(img, 
							Integer.parseInt(coords[0]),
							Integer.parseInt(coords[1]),
							Integer.parseInt(coords[2]),
							Integer.parseInt(coords[3]),
							createDestination("images", dst)
					);
				}
			} else {
				System.out.printf("IMAGE: %s not found%n", f.getAbsolutePath());
			}
		}
		
	}
	/**
	 * Creates the destination filename.
	 * @param type the data type
	 * @param dst the relative file
	 * @return the file
	 */
	private static File createDestination(String type, String dst) {
		return new File(destination + type + "/" + language + "/" + dst);
	}

}
