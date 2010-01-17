/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.screens;

import hu.openig.core.SwappableRenderer;
import hu.openig.sound.AudioThread;
import hu.openig.v1.core.Act;
import hu.openig.v1.core.ResourceType;
import hu.openig.v1.core.SubtitleManager;
import hu.openig.v1.core.ResourceLocator.ResourcePlace;
import hu.openig.v1.render.TextRenderer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.SwingUtilities;

/**
 * The movie screen used for full screen video playback with sound and subtitles.
 * @author karnokd, 2010.01.08.
 * @version $Revision 1.0$
 */
public class MovieScreen extends ScreenBase implements SwappableRenderer {
	/** The center region for the movie frames. */
	Rectangle movieRect = new Rectangle();
	/** The video thread. */
	volatile VideoRenderer videoThread;
	/** The audio playback thread. */
	Thread audioThread;
	/** The audio playback channel. */
	volatile SourceDataLine sdl;
	/** The current label to display. */
	String label;
	/** Stop movie playback. */
	volatile boolean stop;
	/** The audio length in bytes. */
	volatile int audioLen;
	/** The image swap lock. */
	Lock swapLock = new ReentrantLock();
	/** The front buffer. */
	BufferedImage frontBuffer;
	/** The back buffer. */
	BufferedImage backBuffer;
	/** The current subtitle manager. */
	SubtitleManager subtitle;
	/** The media queue to play videos after each other. */
	public final Queue<String> mediaQueue = new LinkedList<String>();
	/** The action to invoke when the playback has finished. */
	public Act playbackFinished;
	/**
	 * Upscale the 8 bit signed values to 16 bit signed values.
	 * @param data the data to upscale
	 * @return the upscaled data
	 */
	public static short[] upscale8To16AndSignify(byte[] data) {
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
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (subtitle != null) {
					long time = (long)(frameCount * 1000 / fps);
					label = subtitle.get(time);
					repaint();
				}
			}
		});
	}
	/**
	 * Start playback.
	 * @param media the media
	 */
	protected void startPlayback(final String media) {
		final ResourcePlace audio = rl.get(commons.config.language, media, ResourceType.AUDIO);
		final ResourcePlace video = rl.get(commons.config.language, media, ResourceType.VIDEO);
		final CyclicBarrier barrier = new CyclicBarrier(audio != null ? 2 : 1);
		final CyclicBarrier continuation = new CyclicBarrier(barrier.getParties() + 1);
		ResourcePlace sub = rl.get(commons.config.language, media, ResourceType.SUBTITLE);
		if (sub != null) {
			subtitle = new SubtitleManager(sub.open());
		} else {
			subtitle = null;
		}
		stop = false;
		final int audioSmooth = commons.config.videoFilter;
		final int audioVolume = commons.config.videoVolume;
		audioThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					AudioInputStream in = AudioSystem.getAudioInputStream(new BufferedInputStream(
							audio.open(), 256 * 1024));
					try {
						byte[] buffer = new byte[in.available()];
						in.read(buffer);
						byte[] buffer2 = AudioThread.split16To8(AudioThread.movingAverage(upscale8To16AndSignify(buffer), audioSmooth));
						try {
							AudioFormat streamFormat = new AudioFormat(22050, 16, 1, true, false);
							DataLine.Info clipInfo = new DataLine.Info(SourceDataLine.class, streamFormat);
							sdl = (SourceDataLine) AudioSystem.getLine(clipInfo);
							sdl.open();
							FloatControl fc = (FloatControl)sdl.getControl(FloatControl.Type.MASTER_GAIN);
							if (fc != null) {
								double minLinear = Math.pow(10, fc.getMinimum() / 20);
								double maxLinear = Math.pow(10, fc.getMaximum() / 20);
								fc.setValue((float)(20 * Math.log10(minLinear + audioVolume * (maxLinear - minLinear) / 100)));
							}
							videoThread.setAudioLength(buffer.length);
							try {
								barrier.await();
							} catch (InterruptedException ex) {
								
							} catch (BrokenBarrierException ex) {
								
							}
							sdl.start();
							sdl.write(buffer2, 0, buffer2.length);
							sdl.drain();
							sdl.stop();
							sdl.close();
						} catch (LineUnavailableException ex) {
							// TODO log
						}
					} finally {
						in.close();
					}
				} catch (UnsupportedAudioFileException ex) {
					// TODO log
					ex.printStackTrace();
				} catch (IOException ex) {
					// TODO log
					ex.printStackTrace();
				} finally {
					try {
						continuation.await();
					} catch (InterruptedException ex) {
						
					} catch (BrokenBarrierException ex) {
						
					}
				}
			}
		}, "Movie Audio");
		videoThread = new VideoRenderer(barrier, continuation, this, video, "Movie Video") {
			@Override
			public void onFrame(double fps, int frameIndex) {
				setPosition(fps, frameIndex);
			}
		};
		
		Thread continueThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					continuation.await();
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							frontBuffer = null;
							playNext();
						}
					});
				} catch (InterruptedException ex) {
					
				} catch (BrokenBarrierException ex) {
					
				}
			}
		}, "Movie Completion Waiter");
		continueThread.start();
		if (audio != null) {
			audioThread.start();
		}
		videoThread.start();
	}
	/**
	 * 
	 */
	protected void playNext() {
		String nextMedia = mediaQueue.poll();
		if (nextMedia != null) {
			startPlayback(nextMedia);
		} else 
		if (playbackFinished != null) {
			playbackFinished.act();
		}
	}
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#doResize()
	 */
	@Override
	public void doResize() {
		movieRect.setBounds((parent.getWidth() - 640) / 2, (parent.getHeight() - 480) / 2, 640, 480);
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#finish()
	 */
	@Override
	public void finish() {
		mediaQueue.clear();
		stopPlayback();
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#initialize()
	 */
	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#keyTyped(int, int)
	 */
	@Override
	public void keyTyped(int key, int modifiers) {
		// TODO Auto-generated method stub
		if (key == KeyEvent.VK_ESCAPE) {
			stopPlayback();
		}
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mouseMoved(int, int, int, int)
	 */
	@Override
	public void mouseMoved(int button, int x, int y, int modifiers) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mousePressed(int, int, int, int)
	 */
	@Override
	public void mousePressed(int button, int x, int y, int modifiers) {
	}
	/** Stop the current playback. */
	protected void stopPlayback() {
		stop = true;
		if (sdl != null) {
			sdl.stop();
		}
		if (videoThread != null) {
			videoThread.stopPlayback();
		}
//		frontBuffer = null;
		label = null;
		repaint();
	}
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mouseReleased(int, int, int, int)
	 */
	@Override
	public void mouseReleased(int button, int x, int y, int modifiers) {
		if (button == 1) {
			stopPlayback();
		}
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mouseScrolled(int, int, int, int)
	 */
	@Override
	public void mouseScrolled(int direction, int x, int y, int modifiers) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#onEnter()
	 */
	@Override
	public void onEnter() {
		playNext();
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#onLeave()
	 */
	@Override
	public void onLeave() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#paintTo(java.awt.Graphics2D)
	 */
	@Override
	public void paintTo(Graphics2D g2) {
		onResize();
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, parent.getWidth(), parent.getHeight());
		if (frontBuffer != null) {
			swapLock.lock();
			try {
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2.drawImage(frontBuffer, movieRect.x, movieRect.y, 
						movieRect.width, movieRect.height, null);
				if (label != null) {
					paintLabel(g2, movieRect.x, movieRect.y, movieRect.width, movieRect.height);
				}
			} finally {
				swapLock.unlock();
			}
		}	
	}

	/**
	 * Paint a word-wrapped label.
	 * @param g2 the graphics context.
	 * @param x0 the X coordinate
	 * @param y0 the Y coordinate
	 * @param width the draw width
	 * @param height the draw height
	 */
	public void paintLabel(Graphics2D g2, int x0, int y0, int width, int height) {
		List<String> lines = new ArrayList<String>();
		int maxWidth = commons.text.wrapText(label, width, 14, lines);
		int y = height - lines.size() * 21 - 7;
		Composite cp = g2.getComposite();
		g2.setComposite(AlphaComposite.SrcOver.derive(0.8f));
		g2.fillRect(x0 + (width - maxWidth) / 2 - 3, y0 + y - 3, maxWidth + 6, lines.size() * 21 + 6);
		g2.setComposite(cp);
		for (String s : lines) {
			int tw = commons.text.getTextWidth(14, s);
			int x = (width - tw) / 2;
			commons.text.paintTo(g2, x0 + x, y0 + y, 14, TextRenderer.WHITE, s);
			y += 21;
		}
	}
	@Override
	public BufferedImage getBackbuffer() {
		return backBuffer;
	}

	@Override
	public void init(int width, int height) {
		swapLock.lock();
		try {
			backBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			frontBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		} finally {
			swapLock.unlock();
		}
	}

	@Override
	public void swap() {
		swapLock.lock();
		try {
			BufferedImage temp = backBuffer;
			backBuffer = frontBuffer;
			frontBuffer = temp;
		} finally {
			swapLock.unlock();
		}
		repaint();
	}

	@Override
	public void mouseDoubleClicked(int button, int x, int y, int modifiers) {
		// TODO Auto-generated method stub
		
	}
}
