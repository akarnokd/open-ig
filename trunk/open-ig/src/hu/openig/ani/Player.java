/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ani;

import hu.openig.ani.Framerates.Rates;
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

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.locks.LockSupport;

import javax.swing.SwingUtilities;

/**
 * The animation player.
 * @author karnokd, 2009.02.08.
 * @version $Revision 1.0$
 */
public class Player {
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
	private volatile BtnAction onCompleted;
	/** The current audio thread. */
	private volatile AudioThread ad;
	/** The master gain. */
	private volatile float masterGain;
	/** The mute value. */
	private volatile boolean mute;
	/** The frame rates object. */
	//private Framerates framerates;
	/**
	 * Constructor.
	 * @param surface the target surface to render to
	 */
	public Player(SwappableRenderer surface) {
		this.surface = surface;
		//framerates = new Framerates();
	}
	/** The playback loop. */
	private void play() {
		try {
			while (!stop && !Thread.currentThread().isInterrupted()) {
				FileInputStream rf = new FileInputStream(getFilename());
				ad = new AudioThread();
				ad.start();
				ad.setMasterGain(masterGain);
				ad.setMute(mute);
				try {
					final SpidyAniFile saf = new SpidyAniFile();
					saf.open(rf);
					saf.load();

					Framerates fr = new Framerates();
					
					Rates r = fr.getRates(getFilename(), saf.getLanguageCode());
					double fps = r.fps;
					int delay = r.delay;
					
					
					PaletteDecoder palette = null;
					int[] rawImage = new int[saf.getWidth() * saf.getHeight()];
					// clear any previous things from the buffer
					surface.init(saf.getWidth(), saf.getHeight());
					surface.getBackbuffer().setRGB(0, 0, saf.getWidth(), saf.getHeight(), rawImage, 0, saf.getWidth());
					surface.swap();
					int imageHeight = 0;
					int dst = 0;
					int audioCount = 0;
					Algorithm alg = saf.getAlgorithm();

			   		double starttime = System.currentTimeMillis();  // notice the start time
			   		try {
						// add audio delay
				   		int framecounter = 0;
				   		boolean firstframe = true;
				   		while (!stop) {
							Block b = saf.next();
							if (b instanceof Palette) {
								palette = (Palette)b;
							} else
							if (b instanceof Sound) {
								ad.submit(b.data);
								audioCount++;
							} else
							if (b instanceof Data) {
								if (firstframe) {
									starttime = System.currentTimeMillis();
									firstframe = false;
								}
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
								default:
								}
								// we reached the number of subimages per frame?
								if (imageHeight >= saf.getHeight()) {
									surface.getBackbuffer().setRGB(0, 0, saf.getWidth(), saf.getHeight(), rawImage, 0, saf.getWidth());
									surface.swap();
									if (framecounter == delay) {
										ad.startPlaybackNow();
									}
									imageHeight = 0;
									dst = 0;
									starttime += (1000.0 / fps);
				           			LockSupport.parkNanos((long)(Math.max(0, starttime - System.currentTimeMillis()) * 1000000));
								}
							}
						}
					} catch (EOFException ex) {
						ad.submit(new byte[0]);
					}
				} finally {
					if (stop) {
						ad.stopPlaybackNow();
						ad.interrupt();
					}
					try {
						ad.join();
					} catch (InterruptedException e) {
					}
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
		ad = null;
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
	/**
	 * Set the master gain on the source data line.
	 * @param gain the master gain in decibels, typically -80 to 0.
	 */
	public void setMasterGain(float gain) {
		AudioThread au = ad;
		if (au != null) {
			au.setMasterGain(gain);
		}
		masterGain = gain;
	}
	/**
	 * Mute or unmute the current playback.
	 * @param mute the mute status
	 */
	public void setMute(boolean mute) {
		AudioThread au = ad;
		if (au != null) {
			au.setMute(mute);
		}
		this.mute = mute;
	}
}
