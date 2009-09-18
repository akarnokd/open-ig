/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ani;

import hu.openig.ani.SpidyAniDecoder.SpidyAniCallback;
import hu.openig.utils.WipPort;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Transcoder program to convert the images of the original ANI file to a more modern and smaller files.
 * @author karnokd, 2009.09.16.
 * @version $Revision 1.0$
 */
public final class Transcoder {
	/** Private constructor. */
	private Transcoder() {
		// utility program
	}
	/** Transcoding mode. */
	enum Mode {
		/** Raw. */
		RAW,
		/** ZIP. */
		ZIP,
		/** GZIP. */
		GZIP
	}
	/**
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		final AtomicLong originalSize = new AtomicLong();
		final AtomicLong rawSize = new AtomicLong();
		final AtomicLong newSize = new AtomicLong();
		final ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		expandToRawAni(originalSize, rawSize, newSize, exec);
		exec.shutdown();
	}
	/**
	 * Expand to raw animation file.
	 * @param originalSize original size counter
	 * @param rawSize raw size counter
	 * @param newSize new size counter
	 * @param exec the executor service
	 * @throws InterruptedException on interruption
	 */
	private static void expandToRawAni(final AtomicLong originalSize,
			final AtomicLong rawSize, final AtomicLong newSize,
			final ExecutorService exec) throws InterruptedException {
		final WipPort wp = new WipPort();
		final AtomicLong ignore = new AtomicLong();
		Files.walkFileTree(Paths.get("c:\\games\\IGHU\\ANI"), 
			new SimpleFileVisitor<Path>() {
				public FileVisitResult visitFile(final Path file, BasicFileAttributes attrs) {
					if (file.toString().toLowerCase().endsWith(".ani")) {
						wp.inc();
						exec.submit(new Runnable() {
							public void run() {
								try {
									File stage2 = new File("d:\\games\\IGHU\\youtube\\" + file.getName().toString() + ".rar");
									if (!stage2.exists()) {
										SpidyAniDecoder.decodeLoop(createCallback(new File(file.toString()), Mode.RAW, originalSize, rawSize, newSize));
									}
									if (false) {
										// recode
										File fout = new File(stage2.getAbsolutePath() + ".2009.gz");
										DataOutputStream gout = new DataOutputStream(new BufferedOutputStream(
												new GZIPOutputStream(new FileOutputStream(fout)), 1024 * 1024));
										convertWithPalette(stage2.getAbsolutePath(), gout);
										gout.close();
										
										// statistics
										long orig = new File(file.toString()).length();
										long nw = fout.length();
										newSize.addAndGet(nw);
										System.out.printf("%s done | Original: %d -> New: %d (%.2f%%)%n", fout.toString().toLowerCase(),
											orig, nw, (nw * 100.0 / orig) 
										);
									}
								} catch (Throwable t) {
									t.printStackTrace();
								} finally {
									wp.dec();
								}
							}
						});
					}
					return FileVisitResult.CONTINUE;
				};
			}
		);
		wp.await();
		System.out.printf("Done | Raw: %d -> Original: %d (%.2f%%) -> New: %d (%.2f%%, %.2f%%)%n", 
				rawSize.get(), originalSize.get(), (originalSize.get() * 100.0 / rawSize.get()), newSize.get(), 
				(newSize.get() * 100.0 / rawSize.get()), (newSize.get() * 100.0 / originalSize.get()));
	}
	/**
	 * Create a transcoder callack.
	 * @param in the input file
	 * @param mode use zip or gzip compression
	 * @param originalSize the original file size 
	 * @param rawSize the uncompressed size
	 * @param newSize the compressed size
	 * @return the callback
	 */
	private static SpidyAniCallback createCallback(final File in, final Mode mode, 
			final AtomicLong originalSize, final AtomicLong rawSize, final AtomicLong newSize) {
		return new SpidyAniCallback() {
			int w, h;
			int[] lastImage;
			int frame;
			DataOutputStream out;
			File outFile;
			int storeMode = 0;
			@Override
			public void stopped() {
				
			}
			
			@Override
			public boolean isStopped() {
				return false;
			}
			
			@Override
			public boolean isPaused() {
				return false;
			}
			
			@Override
			public void initialize(int width, int height, int frames, int languageCode,
					double fps, int audioDelay) {
				w = width;
				h = height;
				lastImage = new int[w * h];
				try {
					OutputStream zoutp = null;
					if (mode == Mode.ZIP) {
						outFile = new File("d:\\games\\IGHU\\youtube\\" + in.getName().toLowerCase() + ".zip");
						ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(outFile));
						zout.putNextEntry(new ZipEntry(in.getName()));
						zout.setLevel(9);
						zoutp = zout;
					} else
					if (mode == Mode.GZIP) {
						outFile = new File("d:\\games\\IGHU\\youtube\\" + in.getName().toLowerCase() + ".gz");
						GZIPOutputStream zout = new GZIPOutputStream(new FileOutputStream(outFile));
						zoutp = zout;
					} else {
						outFile = new File("d:\\games\\IGHU\\youtube\\" + in.getName().toLowerCase() + ".raw");
						zoutp = new FileOutputStream(outFile);
					}
					
					out = new DataOutputStream(new BufferedOutputStream(zoutp, 16 *  1024 * 1024));
					out.writeShort(width);
					out.writeShort(height);
					out.writeShort(frames);
					out.writeShort((short)(fps * 1000));
				} catch (IOException ex) {
					throw new RuntimeException(in.toString(), ex);
				}
			}
			@Override
			public void imageData(int[] image) {
				try {
					int[] copy = image.clone();
					if (storeMode == 0) {
						storeImageDirect(copy);
					} else
					if (storeMode == 1) {
						storeImageDirect3(copy);
					} else 
					if (storeMode == 2) {
						storeImagePlains(copy);
					}
					lastImage = copy;
					frame++;
				} catch (IOException ex) {
					throw new RuntimeException(in.toString(), ex);
				}
			}
			/**
			 * Store the image directly
			 * @param copy
			 * @throws IOException
			 */
			void storeImageDirect(int[] copy) throws IOException {
				for (int i = 0; i < copy.length; i++) {
					if (copy[i] == lastImage[i]) {
						out.writeInt(0);
					} else {
						out.writeInt(copy[i]);
					}
				}
			}
			/**
			 * Store the image separated into RGB plains
			 * @param copy the copy
			 * @throws IOException on error
			 */
			void storeImagePlains(int[] copy) throws IOException {
				for (int j : new int[] {0, 8, 16 }) {
					for (int i = 0; i < copy.length; i++) {
						if (copy[i] == lastImage[i]) {
							out.write(00);
						} else {
							if (copy[i] == 0xFF000000) {
								out.write(01);
							} else {
								out.write((copy[i] >> j) & 0xFF);
							}
						}
					}
				}
			}
			/**
			 * Store the image directly
			 * @param copy
			 * @throws IOException
			 */
			void storeImageDirect3(int[] copy) throws IOException {
				for (int i = 0; i < copy.length; i++) {
					if (copy[i] == lastImage[i]) {
							write3(out, 0xFF00000);
					} else {
						if (copy[i] == 0xFF000000) {
								write3(out, 0xFF010101);
						} else {
								write3(out, copy[i]);
						}
					}
				}
			}
			
			@Override
			public InputStream getNewInputStream() {
				try {
					return new FileInputStream(in);
				} catch (IOException ex) {
					throw new RuntimeException(in.toString(), ex);
				}
			}
			
			@Override
			public String getFileName() {
				return in.getAbsolutePath();
			}
			
			@Override
			public void finished() {
				try {
					out.flush();
					out.close();
					long raw = (w * h * 4) * frame + 8;
					long orig = in.length();
					long nw = outFile.length();
					originalSize.addAndGet(orig);
					rawSize.addAndGet(raw);
					newSize.addAndGet(nw);
					System.out.printf("%s done | Raw: %d -> Original: %d (%.2f%%) -> New: %d (%.2f%%, %.2f%%)%n", in.toString().toLowerCase(),
						raw, orig, (orig * 100.0 / raw), nw, (nw * 100.0 / raw), (nw * 100.0 / orig) 
					);
				} catch (IOException ex) {
					throw new RuntimeException(in.toString(), ex);
				}
			}
			
			@Override
			public void fatal(Throwable t) {
				t.printStackTrace();
			}
			
			@Override
			public void audioData(byte[] data) {
				
			}
		};
	}
	/**
	 * Write 3 bytes of RGB color to the output stream.
	 * @param out the output stream
	 * @param c the output
	 * @throws IOException if 
	 */
	static void write3(OutputStream out, int c) throws IOException {
		out.write((c >> 16) & 0xFF);
		out.write((c >> 8) & 0xFF);
		out.write((c >> 0) & 0xFF);
	}
	/**
	 * Index pixels.
	 * @param pixels the array of pixels
	 * @return the map of pixels
	 */
	static Map<Integer, Integer> indexPixels(int[] pixels) {
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		int idx = 0;
		for (int i = 0; i < pixels.length; i++) {
			int c = pixels[i];
			if ((c & 0xFF000000) != 0 && !result.containsKey(c)) {
				result.put(c, idx);
				idx++;
			}
		}
		return result;
	}
	/**
	 * Convert the input RAW file to a palettized version.
	 * @param inputFile the input file
	 * @param output the output stream
	 * @throws IOException on error
	 */
	static void convertWithPalette(String inputFile, DataOutputStream output) throws IOException {
		RawAni in = new RawAni(inputFile);
		int[] image = new int[in.width * in.height];
		int idx = 0;
		output.writeShort(in.width);
		output.writeShort(in.height);
		output.writeShort(in.frames);
		output.writeShort((int)(in.fps * 1000));
		while (idx < in.frames) {
			Map<Integer, Integer> refMap = new HashMap<Integer, Integer>(256);
			// look ahead for new colors until the color
			int endIndex = idx;
			for (endIndex = idx; endIndex < in.frames; endIndex++) {
				Arrays.fill(image, 0);
				in.readFrame(idx, image);
				Map<Integer, Integer> map = indexPixels(image);
				Map<Integer, Integer> newMap = new HashMap<Integer, Integer>(refMap);
				newMap.putAll(map);
				if (newMap.size() > 255) {
					break;
				}
				// remove existing colors
				map.keySet().removeAll(refMap.keySet());
				// add new colors to the end of the refmap
				for (Integer c : map.keySet()) {
					refMap.put(c, refMap.size());
				}
			}
			// store the palette
			output.write('P');
			output.write(refMap.size());
			// reverse the refmap
			int[] refMapInv = new int[refMap.size()];
			for (Map.Entry<Integer, Integer> e : refMap.entrySet()) {
				refMapInv[e.getValue()] = e.getKey();
			}
			for (int c : refMapInv) {
				output.write((c >> 16) & 0xFF);
				output.write((c >> 8) & 0xFF);
				output.write((c >> 0) & 0xFF);
			}
			// store the palettized image sequences
			for (int i = idx; i < endIndex; i++) {
				Arrays.fill(image, 0);
				in.readFrame(idx, image);
				writePixels(image, refMap, output);
			}
			
			idx = endIndex;
		}
		output.flush();
	}
	/**
	 * Remap RGBA image to indexes and write it to the output stream.
	 * @param pixels the pixels
	 * @param colorMap the color index map
	 * @param out the output
	 * @throws IOException on error
	 */
	static void writePixels(int[] pixels, Map<Integer, Integer> colorMap, OutputStream out) throws IOException {
		out.write('I');
		for (int c : pixels) {
			if (c == 0) {
				out.write(255);
			} else {
				out.write(colorMap.get(c));
			}
		}
	}
}
