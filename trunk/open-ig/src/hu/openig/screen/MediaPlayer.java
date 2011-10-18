/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen;

import hu.openig.core.Act;
import hu.openig.core.Action1;
import hu.openig.core.ResourceLocator.ResourcePlace;
import hu.openig.core.ResourceType;
import hu.openig.core.SubtitleManager;
import hu.openig.core.SwappableRenderer;
import hu.openig.model.VideoAudio;
import hu.openig.sound.AudioThread;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.SwingUtilities;

/**
 * A media player for playing video and audio.
 * @author akarnokd, 2010.01.17.
 */
public class MediaPlayer {
	/** The label event. */
	public interface LabelEvent {
		/** 
		 * Set the new label.
		 * @param text the textual labels 
		 */
		void label(String text);
	}
	/** The video thread. */
	protected VideoRenderer videoThread;
	/** The audio playback thread. */
	protected Thread audioThread;
	/** The audio playback channel. */
	protected volatile SourceDataLine sdl;
	/** The current subtitle manager. */
	protected SubtitleManager subtitle;
	/** Stop movie playback. */
	protected volatile boolean stop;
	/** Is the playback terminated. */
	protected volatile boolean terminated;
	/** The event to invoke when completed. */
	public volatile Act onComplete;
	/** The label event. */
	public volatile LabelEvent onLabel;
	/** The indicator to continue a phase. */
	private AtomicInteger continuation;
	/** The indicator to continue a phase. */
	private AtomicInteger audioFinish;
	/**
	 * Constructor.
	 * @param commons the commons resources
	 * @param media the media to play
	 * @param surface the rendering surface
	 */
	public MediaPlayer(final CommonResources commons, final String media, SwappableRenderer surface) {
		init(commons, new VideoAudio(media, media), surface);
	}
	/**
	 * Constructor.
	 * @param commons the commons resources
	 * @param media the media to play
	 * @param surface the rendering surface
	 */
	public MediaPlayer(final CommonResources commons, final VideoAudio media, SwappableRenderer surface) {
		init(commons, media, surface);
	}
	
	/**
	 * Initialize the playback threads.
	 * @param commons the common resources
	 * @param media the media record
	 * @param surface the surface to render to
	 */
	private void init(final CommonResources commons, final VideoAudio media,
			SwappableRenderer surface) {
		final ResourcePlace audio = commons.audio(media.audio);
		final ResourcePlace video = commons.video(media.video);

		final CyclicBarrier barrier = new CyclicBarrier(audio != null ? 2 : 1);
		
		continuation = new AtomicInteger(barrier.getParties());
		audioFinish = new AtomicInteger(1);
		
		ResourcePlace sub = commons.rl.get(media.video, ResourceType.SUBTITLE);
		if (sub != null) {
			subtitle = new SubtitleManager(sub.open());
		} else {
			subtitle = null;
		}
		stop = false;
//		final int audioSmooth = commons.config.videoFilter;
		final int audioVolume = commons.config.videoVolume;
		audioThread = new Thread(new Runnable() {
			@Override
			public void run() {
				if (audio == null) {
					return;
				}
				try {
					AudioInputStream in = AudioSystem.getAudioInputStream(new BufferedInputStream(
							audio.open(), 256 * 1024));
					try {
						byte[] buffer = new byte[in.available()];
						in.read(buffer);
						try {
							AudioFormat af = in.getFormat();
							byte[] buffer2 = null;
							if (af.getSampleSizeInBits() == 8) {
								if (af.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED) {
									for (int i = 0; i < buffer.length; i++) {
										buffer[i] = (byte)((buffer[i] & 0xFF) - 128);
									}
								}
								buffer2 = AudioThread.convert8To16(buffer);
								af = new AudioFormat(af.getFrameRate(), 16, af.getChannels(), true, false);
							} else {
								buffer2 = buffer;
							}
							AudioFormat streamFormat = new AudioFormat(in.getFormat().getSampleRate(), 16, 1, true, false);
							
							DataLine.Info clipInfo = new DataLine.Info(SourceDataLine.class, streamFormat);
							sdl = (SourceDataLine) AudioSystem.getLine(clipInfo);
							sdl.open();
							AudioThread.setVolume(sdl, audioVolume);
							videoThread.setAudioLength(buffer2.length / 2);
							try {
								barrier.await();
								if (!stop) {
									sdl.start();
									try {
										sdl.write(buffer2, 0, buffer2.length);
										sdl.drain();
									} finally {
										sdl.stop();
									}
								}
							} catch (InterruptedException ex) {
								
							} catch (BrokenBarrierException ex) {
								ex.printStackTrace();
							} finally {
								sdl.close();
							}
						} catch (LineUnavailableException ex) {
							ex.printStackTrace();
						}
					} finally {
						in.close();
					}
				} catch (UnsupportedAudioFileException ex) {
					ex.printStackTrace();
				} catch (IOException ex) {
					ex.printStackTrace();
				} finally {
					doCompleteAudio();
				}
			}
		}, "Movie Audio");
		
		videoThread = new VideoRenderer(barrier, surface, video, "Movie Video",
			new Action1<Void>() {
				@Override
				public void invoke(Void value) {
					if (continuation.decrementAndGet() == 0) {
						invokeComplete();
					}
				}
			}
		) {
			@Override
			public void onFrame(double fps, int frameIndex) {
				setPosition(fps, frameIndex);
			}
		};
	}
	/** Invoke the completion action. */
	void invokeComplete() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (onComplete != null && !terminated) {
					onComplete.act();
				}
			}
		});
	}
	/**
	 * Upscale the 8 bit signed values to 16 bit signed values.
	 * @param data the data to upscale
	 * @return the upscaled data
	 */
	short[] upscale8To16AndSignify(byte[] data) {
		short[] result = new short[data.length];
		for (int i = 0; i < data.length; i++) {
			result[i] = (short)(((data[i] & 0xFF) - 128) * 256);
		}
		return result;
	}
	/**
	 * Set the label based on the current playback location.
	 * @param fps the frames per second
	 * @param frameCount the current frame count
	 */
	protected void setPosition(final double fps, final int frameCount) {
		if (onLabel != null && subtitle != null) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					long time = (long)(frameCount * 1000 / fps);
					onLabel.label(subtitle.get(time));
				}
			});
		}
	}
	/**
	 * Start the media playback.
	 */
	public void start() {
		if (audioThread != null) {
			audioThread.start();
		}
		videoThread.start();
	}
	/**
	 * Stop the media playback.
	 */
	public void stop() {
		stop = true;
		if (videoThread != null) {
			videoThread.stopPlayback();
		}
		if (sdl != null) {
			doCompleteAudio();
			audioThread.interrupt();
			sdl.stop();
		}
	}
	/**
	 * Terminate the media playback without invoking the completion handlers.
	 */
	public void terminate() {
		terminated = true;
		videoThread.terminatePlayback();
		stop();
	}
	/**
	 * If the audio hasn't finished yet, complete it and if all parties have finished, invoke the completion handler. 
	 */
	void doCompleteAudio() {
		if (audioFinish.decrementAndGet() == 0) {
			if (continuation.decrementAndGet() == 0) {
				invokeComplete();
			}
		}
	}
}
