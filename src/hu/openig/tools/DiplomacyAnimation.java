/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.utils.Exceptions;
import hu.openig.utils.U;
import hu.openig.utils.WipPort;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;

/**
 * Extract the animation from the diplomacy screen, excluding the background and the alien.
 * @author akarnokd, 2012.06.20.
 */
public final class DiplomacyAnimation {
	/** Utility class. */
	private DiplomacyAnimation() { }

	/**
	 * @param args no arguments.
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		int nfej = 8;
		
		final List<List<int[]>> allFrames = new ArrayList<>();
		for (int i = 1; i <= nfej; i++) {
			allFrames.add(getFrames("video/generic/diplomacy/dipfej" + i + ".ani.gz"));
		}
		
		// remove heads
		final ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		sequentialDiff(allFrames, exec);
		averaging(allFrames, exec);
		
		// compose image sequence
		compose(allFrames, exec);

		exec.shutdown();
		exec.awaitTermination(1, TimeUnit.HOURS);
	}

	/**
	 * Compose the images.
	 * @param allFrames the all frames
	 * @param exec the executor
	 * @throws IOException on error
	 * @throws InterruptedException on interrupt
	 */
	public static void compose(final List<List<int[]>> allFrames,
			final ExecutorService exec) throws IOException,
			InterruptedException {
		final BufferedImage garthog = ImageIO.read(new File("e:/downloads/garthog_head.png"));
		final BufferedImage img0 = ImageIO.read(new File("e:/temp/dipfej_000.png")); 
		
		final int w = img0.getWidth();
		final int h = img0.getHeight();
		final int iy = 84;
		final int ix = (w - garthog.getWidth()) / 2;
		
		final WipPort wip = new WipPort(1);
		
		final int frameCount = allFrames.get(0).size();
		for (int i1 = 0; i1 < frameCount; i1++) {
			final int i = i1;
			wip.inc();
			exec.execute(new Runnable() {
				@Override
				public void run() {
					try {
						BufferedImage img = ImageIO.read(new File(String.format("e:/temp/dipfej_%03d.png", i)));
						BufferedImage out = new BufferedImage(img0.getWidth(), img0.getHeight(), BufferedImage.TYPE_INT_ARGB);
						Graphics2D g2 = out.createGraphics();
						// appear
						g2.drawImage(img0, 0, 0, null);
						Composite save0 = g2.getComposite();
						if (i >= 0 && i < 35) {
							g2.setComposite(AlphaComposite.SrcOver.derive(i / 34f));
							g2.drawImage(garthog, ix, iy, null);
						} else
						// stay
						if (i >= 30 && i < frameCount - 35) {
							g2.drawImage(garthog, ix, iy, null);
						} else
						// flash-out
						if (i >= frameCount - 35 && i < frameCount - 20) {
							int j = i - frameCount + 35;
							g2.setComposite(AlphaComposite.SrcOver.derive((15 - j) / 15f));
							g2.drawImage(garthog, ix, iy, null);
						}
						g2.setComposite(save0);
						g2.dispose();
						
						int[] imgb = img.getRGB(0, 0, w, h, null, 0, w);
						int[] imgc = out.getRGB(0, 0, w, h, null, 0, w);
						int[] argb0 = new int[4];
						int[] argb1 = new int[4];
						for (int k = 0; k < imgb.length; k++) {
							getargb(imgb[k], argb0);
							getargb(imgc[k], argb1);
//							if (argb1[2] > argb0[2]) {
//								argb1[2] = (int)(argb1[2] * 0.5 + argb0[2] * 0.5);
//							}
							argb1[2] = Math.max(argb1[2], argb0[2]);
						
							imgc[k] = getColor(argb1);
						}
						
						// reduce color space to 0-254 colors
						Map<Integer, Integer> colorCounts = new HashMap<>();
                        for (int anImgc : imgc) {
                            Integer c = colorCounts.get(anImgc & 0xFFFFFF);
                            colorCounts.put(anImgc & 0xFFFFFF, c != null ? c + 1 : 1);
                        }
						if (colorCounts.size() > 254) {
							List<Map.Entry<Integer, Integer>> lst = U.newArrayList(colorCounts.entrySet());
							Collections.sort(lst, new Comparator<Map.Entry<Integer, Integer>>() {
								@Override
								public int compare(Entry<Integer, Integer> o1,
										Entry<Integer, Integer> o2) {
									return Integer.compare(o2.getValue(), o1.getValue());
								}
							});
							
							Set<Integer> keptColors = new HashSet<>();
							Map<Integer, Integer> translate = new HashMap<>();
							for (int i = 0; i < 254; i++) {
								keptColors.add(lst.get(i).getKey());
							}
							for (int i = 254; i < lst.size(); i++) {
								final int c = lst.get(i).getKey();
								final int[] argb3 = new int[4];
								getargb(c, argb3);
								int cm = Collections.min(keptColors, new Comparator<Integer>() {
									int[] argb1 = new int[4];
									int[] argb2 = new int[4];
									@Override
									public int compare(Integer o1, Integer o2) {
										getargb(o1, argb1);
										getargb(o2, argb2);
										int sum1 = 0;
										int sum2 = 0;
										for (int m = 1; m < 4; m++) {
											int diff = argb1[m] - argb3[m];
											sum1 += diff * diff;
											diff = argb2[m] - argb3[m];
											sum2 += diff * diff;
										}
										return Double.compare(sum1, sum2);
									}
								});
								translate.put(c, cm);
							}
							
							for (int k = 0; k < imgc.length; k++) {
								int c = imgc[k] & 0xFFFFFF;
								if (translate.containsKey(c)) {
									imgc[k] = 0xFF000000 + translate.get(c);
								}
							}							
						}
						
						out.setRGB(0, 0, w, h, imgc, 0, w);

						
						ImageIO.write(out, "png", new File(String.format("e:/temp/dipfej9_%03d.png", i)));
					} catch (IOException ex) {
						Exceptions.add(ex);
					} finally {
						wip.dec();
					}
				}
			});
		}
		wip.dec();
		wip.await();
	}

	/**
	 * Create the sequential difference between subsequent frames by turning of pixels.
	 * @param allFrames all the frames
	 * @param exec the executor
	 */
	static void sequentialDiff(List<List<int[]>> allFrames, ExecutorService exec) {
		final WipPort wip = new WipPort(1);
		for (final List<int[]> frames : allFrames) {
			wip.inc();
			exec.execute(new Runnable() {
				@Override
				public void run() {
					try {
						int[] argb0 = new int[4];
						int[] argb1 = new int[4];
						int[] f0 = frames.get(0);
						for (int i = 1; i < frames.size(); i++) {
							int[] f1 = frames.get(i);
							for (int k = 0; k < f0.length; k++) {
								getargb(f0[k], argb0);
								getargb(f1[k], argb1);
								
//								int max = Math.max(argb0[1], Math.max(argb0[2], argb0[3]));
//								for (int m = 1; m < 4; m++) {
//									argb1[m] = Math.min(255, Math.max(0, argb1[m] - argb0[m]));
//								}
								if (argb1[2] <= argb0[2]) {
									argb1[0] = 0;
								} else {
									int max = Math.max(argb0[1], Math.max(argb0[2], argb0[3]));
									argb1[1] = 0;
									argb1[2] = max + argb1[2] - argb0[2];
									argb1[3] = 0;
								}
								f1[k] = getColor(argb1);
							}
						}
					} finally {
						wip.dec();
					}
				}
			});
		}
		wip.dec();
		try {
			wip.await();
		} catch (InterruptedException ex) {
			// ignored
		}
	}
	/**
	 * Convert the a, r, g, b array into a composite integer color.
	 * @param argb the input color components
	 * @return the int color
	 */
	static int getColor(int[] argb) {
		return (argb[0] << 24) + (argb[1] << 16) + (argb[2] << 8) + (argb[3]);
	}
	/**
	 * Extract components of an ARGB integer color.
	 * @param color the color
	 * @param argb the output array
	 */
	static void getargb(int color, int[] argb) {
		argb[0] = (color >> 24) & 0xFF;
		argb[1] = (color >> 16) & 0xFF;
		argb[2] = (color >> 8) & 0xFF;
		argb[3] = (color) & 0xFF;
	}
	/**
	 * Convert an ARGB color to HSV.
	 * @param argb the ARGB color
	 * @return array of alpha, h, s, v
	 */
	static float[] argbToHsv(int argb) {
		float[] result = new float[4];
		
		result[0] = ((argb & 0xFF000000) >> 24) / 255f;

		int r = (argb >> 16) & 0xFF;
		int g = (argb >> 8) & 0xFF;
		int b = (argb) & 0xFF;
		
		int min = Math.min(r, Math.min(g, b));
		int max = Math.max(r, Math.max(g, b));
		int delta = max - min;
		
		result[3] = max;
		
		if (max != 0) {
			result[2] = delta * 1f / max;
		} else {
			result[2] = 0;
			result[1] = -1;
			return result;
		}
		
		if (r == max) {
			result[1] = (g - b) * 1f / delta;
		} else
		if (g == max) {
			result[1] = 2 + (b - r) * 1f / delta;
		} else {
			result[1] = 4 + (r - g) * 1f / delta;
		}
		result[1] *= 60;
		if (result[1] < 0) {
			result[1] += 360;
		}
		result[1] /= 360;
		
		return result;
	}
	/**
	 * Average the frames across image sequences.
	 * @param allFrames the frames
	 * @param exec the executor service
	 */
	public static void averaging(final List<List<int[]>> allFrames, final ExecutorService exec) {
		final WipPort wip = new WipPort(1);
		final int length = allFrames.get(0).get(0).length;

		for (int i = 0; i < allFrames.get(0).size(); i++) {
			wip.inc();
			final int fi = i;
			exec.execute(new Runnable() {
				@Override
				public void run() {
					try {
						int[] frame = new int[length];
						for (int k = 0; k < frame.length; k++) {
							int a1 = 0;
							int r1 = 0;
							int g1 = 0;
							int b1 = 0;
                            for (List<int[]> allFrame : allFrames) {
                                int[] f2 = allFrame.get(fi);
                                a1 += (f2[k] & 0xFF000000) >> 24;
                                r1 += (f2[k] & 0xFF0000) >> 16;
                                g1 += (f2[k] & 0xFF00) >> 8;
                                b1 += (f2[k] & 0xFF);
                            }
							a1 /= allFrames.size();
							r1 /= allFrames.size();
							g1 /= allFrames.size();
							b1 /= allFrames.size();
							
							int c2 = (a1 << 24) + (r1 << 16) + (g1 << 8) + b1;
							frame[k] = c2;
						}
						
						int h = frame.length / 640;
						BufferedImage bimg = new BufferedImage(640, h, BufferedImage.TYPE_INT_ARGB);
						bimg.setRGB(0, 0, 640, h, frame, 0, 640);
						
						try {
							ImageIO.write(bimg, "png", new File(String.format("e:/temp/dipfej_%03d.png", fi)));
						} catch (IOException ex) {
							Exceptions.add(ex);
						}
					} finally {
						wip.dec();
					}
				}
			});
		}
		wip.dec();
		try {
			wip.await();
		} catch (InterruptedException ex) {
			// ignored
		}
	}
	/**
	 * Returns the frames of a concrete ani.gz video file.
	 * @param videoFile the file
	 * @return the list of frames
	 * @throws IOException on error
	 */
	static List<int[]> getFrames(String videoFile) throws IOException {
		List<int[]> result = new ArrayList<>();
		try (DataInputStream in = new DataInputStream(
				new BufferedInputStream(new GZIPInputStream(new FileInputStream(videoFile), 1024 * 1024), 1024 * 1024))) {
			int w = Integer.reverseBytes(in.readInt());
			int h = Integer.reverseBytes(in.readInt());
			if (in.skipBytes(8) != 8) {
                throw new IOException("File structure error!");
            }
			
			int[] palette = new int[256];
			byte[] bytebuffer = new byte[w * h];
			int[] currentImage = new int[w * h];
			
			while (!Thread.currentThread().isInterrupted()) {
				int c = in.read();
				if (c < 0 || c == 'X') {
					break;
				} else
				if (c == 'P') {
					int len = in.read();
					for (int j = 0; j < len; j++) {
						int r = in.read() & 0xFF;
						int g = in.read() & 0xFF;
						int b = in.read() & 0xFF;
						palette[j] = 0xFF000000 | (r << 16) | (g << 8) | b;
					}
				} else
				if (c == 'I') {
					in.readFully(bytebuffer);
					for (int i = 0; i < bytebuffer.length; i++) {
						int c0 = palette[bytebuffer[i] & 0xFF];
						if (c0 != 0) {
							currentImage[i] = c0;
						}
					}
					result.add(currentImage.clone());
				}
			}
		}
		return result;
	}

}
