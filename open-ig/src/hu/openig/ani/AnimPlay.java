/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.ani;

import hu.openig.ani.SpidyAniFile.Algorithm;
import hu.openig.ani.SpidyAniFile.Block;
import hu.openig.ani.SpidyAniFile.Data;
import hu.openig.ani.SpidyAniFile.Palette;
import hu.openig.ani.SpidyAniFile.Sound;
import hu.openig.compress.LZSS;
import hu.openig.compress.RLE;
import hu.openig.core.PaletteDecoder;

import java.awt.Container;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 * Standalone ANI file player of Imperium Galactica's
 * various .ANI formats.
 * @author karnokd, 2009.01.11.
 * @version $Revision 1.0$
 */
public class AnimPlay {
	/** The frame form the images. */
	private static JFrame frame;
	/** The label for the player. */
	private static JLabel imageLabel;
	/**
	 * Execute the SwingUtilities.invokeAndWait() method but strip of
	 * the exceptions. The exceptions will be ignored.
	 * @param r the runnable to pass along
	 */
	private static void swingInvokeAndWait(final Runnable r) {
		try {
			SwingUtilities.invokeAndWait(r);
		} catch (InterruptedException ex) {
			
		} catch (InvocationTargetException ex) {
			
		}
	}
	/**
	 * Create the player's frame.
	 * @param f the file that will be played
	 */
	private static void createFrame(final File f) {
		swingInvokeAndWait(new Runnable() {
			@Override
			public void run() {
				frame = new JFrame(String.format("Playing: %s", f));
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				Container c = frame.getContentPane();
				imageLabel = new JLabel();
				c.add(imageLabel);
				frame.pack();
				frame.setResizable(false);
				frame.setVisible(true);
				
			}
		});
	}
	/**
	 * Creates the audio output stream. The returned output
	 * stream is opened but not started.
	 * @return the created audio output stream or null if no audio support
	 * is available.
	 */
	private static SourceDataLine createAudioOutput() {
		AudioFormat af = new AudioFormat(22050, 8, 1, true, false);
		SourceDataLine sdl = null;
		DataLine.Info dli = new DataLine.Info(SourceDataLine.class, af);
		if (!AudioSystem.isLineSupported(dli)) {
			return null;
		}
		try {
			sdl = (SourceDataLine)AudioSystem.getLine(dli);
			sdl.open(af);
			return sdl;
		} catch (LineUnavailableException ex) {
			return null;
		}
	}
	/**
	 * Asynchronous thread to play audio data in parallel of the
	 * rendered images. Send an empty array or interrupt to
	 * close this thread. Will start and stop the playback automatically
	 * @author karnokd, 2009.01.11.
	 * @version $Revision 1.0$
	 */
	public static class AudioThread extends Thread {
		/** The queue for asynchronus music play. */
		private final BlockingQueue<byte[]> queue;
		/** The output audio line. */
		private final SourceDataLine sdl;
		/**
		 * Constructor. Sets the private fields.
		 * @param queue the data queue to use.
		 * @param sdl
		 */
		public AudioThread(BlockingQueue<byte[]> queue, SourceDataLine sdl) {
			this.queue = queue;
			this.sdl = sdl;
		}
		/**
		 * The main loop to enqueue and play audio.
		 */
		@Override
		public void run() {
			try {
				boolean first = true;
				while (!isInterrupted()) {
					byte[] data = queue.take();
					if (data.length == 0) {
						break;
					}
					if (first) {
						sdl.start();
						first = false;
					}
					sdl.write(data, 0, data.length);
				}
			} catch (InterruptedException ex) {
				// time to quit;
				interrupt();
			} finally {
				sdl.stop();
				sdl.drain();
				sdl.close();
			}
		}
	}
	/**
	 * Main program. Accepts 1 argument: the file name to play.
	 * @param args the arguments
	 */
	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			System.out.printf("Usage:%n AnimPlay filename%n");
			return;
		}
		// the number of images per second
		final float FPS = 16.0f;
		File f = new File(args[0]);
		FileInputStream rf = new FileInputStream(f);
		try {
			SpidyAniFile saf = new SpidyAniFile();
			saf.open(new DataInputStream(rf));
			saf.load();
			createFrame(f);
			
			BlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>();
			
			AudioThread ad = new AudioThread(queue, createAudioOutput());
			ad.start();
			
			PaletteDecoder palette = null;
			BufferedImage bimg = new BufferedImage(saf.getWidth(), saf.getHeight(), BufferedImage.TYPE_INT_ARGB);
			int[] rawImage = new int[saf.getWidth() * saf.getHeight()];
			int imageHeight = 0;
			int dst = 0;
			int audioCount = 0;
			Algorithm alg = saf.getAlgorithm();
			try {
		   		long starttime = System.currentTimeMillis();  // notice the start time
		   		boolean firstFrame = true;
		   		while (true) {
					Block b = saf.next();
					if (b instanceof Palette) {
						palette = (Palette)b;
					} else
					if (b instanceof Sound) {
						queue.offer(b.data);
						audioCount++;
					} else
					if (b instanceof Data) {
						Data d = (Data)b;
						imageHeight += d.height;
						// decompress the image
						byte[] rleInput = d.data;
						if (saf.isLZSS() && !d.specialFrame) {
							rleInput = new byte[d.bufferSize];
							LZSS.decompress(d.data, 0, rleInput, 0);
						}
						switch (alg) {
						case RLE_TYPE_1:
							int newDst = RLE.decompress1(rleInput, 0, rawImage, dst, palette);
							dst = newDst;
							break;
						case RLE_TYPE_2:
							newDst = RLE.decompress2(rleInput, 0, rawImage, dst, palette);
							dst = newDst;
							break;
						}
						// we reached the number of subimages per frame?
						if (imageHeight >= saf.getHeight()) {
							bimg.setRGB(0, 0, saf.getWidth(), saf.getHeight(), rawImage, 0, saf.getWidth());
							final ImageIcon imgIcon = new ImageIcon(bimg);
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									imageLabel.setIcon(imgIcon);
									frame.pack();
								}
							});
							if (firstFrame) {
								starttime = System.currentTimeMillis();
								firstFrame = false;
							}
							imageHeight = 0;
							dst = 0;
							try {
			           			starttime += (int)(1000 / FPS); // compute the destination time
			           			// if destination time isn't reached --> sleep
			           			Thread.sleep(Math.max(0,starttime - System.currentTimeMillis())); 
			           		} catch (InterruptedException ex) {
								
							}
						}
					}
				}
			} catch (EOFException ex) {
				// we reached the end of file
			}
			ad.queue.offer(new byte[0]);
			ad.interrupt();
			System.out.printf("%.2f", audioCount * 0x4F6 / 22050f / saf.getFrameCount());
		} finally {
			rf.close();
		}
	}

}
