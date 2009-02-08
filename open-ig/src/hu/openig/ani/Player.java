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
import hu.openig.core.BtnAction;
import hu.openig.core.PaletteDecoder;
import hu.openig.core.SwappableRenderer;
import hu.openig.sound.AudioThread;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.SwingUtilities;

/**
 * The animation player.
 * @author karnokd, 2009.02.08.
 * @version $Revision 1.0$
 */
public class Player {
	/** The general playback framerate. */
	private final float FPS = 15.9f;
	/** Loop the playback? */
	private boolean loop;
	/** Stop the playback? */
	private volatile boolean stop;
	/** The playback target surfacce. */
	private SwappableRenderer surface;
	/** The filename to play. */
	private String filename;
	/** The playback thread. */
	private volatile Thread playback;
	/** The action to be invoked when the playback completes normally. */
	private BtnAction onCompleted;
	/**
	 * Constructor.
	 */
	public Player(SwappableRenderer surface) {
		this.surface = surface;
	}
	/** The playback loop. */
	private void play() {
		try {
			while (!stop && !Thread.currentThread().isInterrupted()) {
				FileInputStream rf = new FileInputStream(getFilename());
				AudioThread ad = new AudioThread();
				ad.start();
				try {
					final SpidyAniFile saf = new SpidyAniFile();
					saf.open(new DataInputStream(rf));
					saf.load();
					
					PaletteDecoder palette = null;
					int[] rawImage = new int[saf.getWidth() * saf.getHeight()];
					surface.init(saf.getWidth(), saf.getHeight());
					int imageHeight = 0;
					int dst = 0;
					int audioCount = 0;
					Algorithm alg = saf.getAlgorithm();

			   		long starttime = System.currentTimeMillis();  // notice the start time
			   		boolean firstFrame = true;
			   		try {
				   		while (!stop) {
							if (firstFrame) {
								starttime = System.currentTimeMillis();
							}
							Block b = saf.next();
							if (b instanceof Palette) {
								palette = (Palette)b;
							} else
							if (b instanceof Sound) {
								ad.submit(b.data);
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
									surface.getBackbuffer().setRGB(0, 0, saf.getWidth(), saf.getHeight(), rawImage, 0, saf.getWidth());
									surface.swap();
									if (firstFrame) {
										ad.startPlaybackNow();
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
				} finally {
					ad.submit(new byte[0]);
					ad.interrupt();
					ad.stopPlaybackNow();
				}
				if (!isLoop()) {
					break;
				}
			}
		} catch (IOException ex) {
			// won't do much about the general case
			ex.printStackTrace();
		}
		playback = null;
		if (onCompleted != null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					onCompleted.invoke();
				}
			});
		}
	}
	/**
	 * Start the playback.
	 */
	public void startPlayback() {
		Thread t = playback;
		if (t != null) {
			stopPlayback();
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		stop = false;
		t = new Thread(new Runnable() {
			public void run() {
				play();
			}
		});
		playback = t;
		t.start();
	}
	/** 
	 * Stop the playback.
	 */
	public void stopPlayback() {
		stop = true;
	}
	/** Stop and wait for termination. */
	public void stopAndWait() {
		Thread t = playback;
		stopPlayback();
		if (t != null) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}
	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}
	/**
	 * @param loop the loop to set
	 */
	public void setLoop(boolean loop) {
		this.loop = loop;
	}
	/**
	 * @return the loop
	 */
	public boolean isLoop() {
		return loop;
	}
	/**
	 * @param onCompleted the onCompleted to set
	 */
	public void setOnCompleted(BtnAction onCompleted) {
		this.onCompleted = onCompleted;
	}
	/**
	 * @return the onCompleted
	 */
	public BtnAction getOnCompleted() {
		return onCompleted;
	}
	/**
	 * Returns true if there is a playback going on.
	 * @return true if there is a playback going on
	 */
	public boolean isPlayback() {
		return playback != null;
	}
}
