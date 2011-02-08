/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.xold.ani;

import hu.openig.xold.ani.SpidyAniDecoder.SpidyAniCallback;
import hu.openig.xold.core.BtnAction;
import hu.openig.core.SwappableRenderer;
import hu.openig.sound.AudioThread;
import hu.openig.utils.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.locks.LockSupport;

import javax.swing.SwingUtilities;

/**
 * The animation player.
 * @author karnokd, 2009.02.08.
 * @version $Revision 1.0$
 */
public class SpidyAniPlayer {
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
	/** Disable audio playback. */
	private boolean noAudio;
	/** Fetch the entire animation file into memory? */
	private boolean memoryPlayback;
	/** The frame rates object. */
	//private Framerates framerates;
	/**
	 * Constructor.
	 * @param surface the target surface to render to
	 */
	public SpidyAniPlayer(SwappableRenderer surface) {
		this.surface = surface;
		//framerates = new Framerates();
	}
	/** The playback loop. */
	private void play() {
		ByteArrayInputStream mem0 = null;
		if (memoryPlayback) {
			mem0 = new ByteArrayInputStream(IOUtils.load(filename));
		}
		final ByteArrayInputStream mem = mem0;
		while (!stop && !Thread.currentThread().isInterrupted()) {
			if (!noAudio) {
				ad = new AudioThread();
				ad.start();
				ad.setMasterGain(masterGain);
				ad.setMute(mute);
			}
			SpidyAniCallback callback = new SpidyAniCallback() {
				/** Time calculation for proper frame delay. */
				double starttime;
				/** The current frame number. */
				int frameCount;
				/** The audio frame delay. */
				int frameDelay;
				/** Frame width. */
				int width;
				/** Frame height. */
				int height;
				/** The frame/second. */
				double fps;
				@Override
				public void audioData(byte[] data) {
					if (ad != null) {
						ad.submit(data);
					}
				}

				@Override
				public void fatal(Throwable t) {
					t.printStackTrace();
					stopped();
				}

				@Override
				public void finished() {
					if (ad != null) {
						ad.stopPlayback();
						// wait for the audio thread to finish
						try {
							ad.join();
						} catch (InterruptedException e) {
							// ignored here
						}
					}
				}

				@Override
				public String getFileName() {
					return SpidyAniPlayer.this.getFilename();
				}

				@Override
				public InputStream getNewInputStream() {
					if (memoryPlayback) {
						mem.reset();
						return mem;
					} else {
						try {
							return new FileInputStream(getFilename());
						} catch (FileNotFoundException ex) {
							throw new RuntimeException("Missing file? " + getFilename());
						}
					}
				}

				@Override
				public void imageData(int[] image) {
					if (frameCount == 0) {
						starttime = System.currentTimeMillis();
					}
					if (frameCount++ == frameDelay) {
						if (ad != null) {
							ad.startPlaybackNow();
						}
					}
					surface.getBackbuffer().setRGB(0, 0, width, height, image, 0, width);
					surface.swap();
					// wait the frame/sec
					starttime += (1000.0 / fps);
           			LockSupport.parkNanos((long)(Math.max(0, starttime - System.currentTimeMillis()) * 1000000));
				}

				@Override
				public void initialize(int width, int height, int frames,
						int languageCode, double fps, int audioDelay) {
					this.frameDelay = audioDelay;
					this.width = width;
					this.height = height;
					this.fps = fps;
					surface.init(width, height);
//					 clear backbuffer
					surface.getBackbuffer().setRGB(0, 0, width, height, new int[width * height], 0, width);
					surface.swap();
				}

				@Override
				public boolean isPaused() {
					return false;
				}

				@Override
				public boolean isStopped() {
					return stop;
				}

				@Override
				public void stopped() {
					if (ad != null) {
						ad.stopPlaybackNow();
						try {
							ad.join();
						} catch (InterruptedException e) {
							// ignored here
						}
					}
				}
				
			};
			SpidyAniDecoder.decodeLoop(callback);
			if (!isLoop()) {
				break;
			}
		}
		playback = null;
		ad = null;
		if (onCompleted != null) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override 
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
			@Override 
			public void run() {
				play();
			}
		}, "Video: " + filename);
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
	/**
	 * Disable audio playback.
	 * @param noAudio the noAudio to set
	 */
	public void setNoAudio(boolean noAudio) {
		this.noAudio = noAudio;
	}
	/**
	 * Disable audio playback.
	 * @return the noAudio
	 */
	public boolean isNoAudio() {
		return noAudio;
	}
	/**
	 * Fetch the entire animation file into memory?
	 * @param memoryPlayback the memoryPlayback to set
	 */
	public void setMemoryPlayback(boolean memoryPlayback) {
		this.memoryPlayback = memoryPlayback;
	}
	/**
	 * Fetch the entire animation file into memory?
	 * @return the memoryPlayback
	 */
	public boolean isMemoryPlayback() {
		return memoryPlayback;
	}
}
