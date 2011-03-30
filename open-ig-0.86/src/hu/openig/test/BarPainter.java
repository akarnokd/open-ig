/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.test;

import hu.openig.core.SwappableRenderer;
import hu.openig.sound.AudioThread;
import hu.openig.core.Labels;
import hu.openig.core.ResourceLocator;
import hu.openig.core.ResourceType;
import hu.openig.core.SubtitleManager;
import hu.openig.core.ResourceLocator.ResourcePlace;
import hu.openig.model.TalkPerson;
import hu.openig.model.TalkSpeech;
import hu.openig.model.TalkState;
import hu.openig.render.TextRenderer;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * The test surface for a bar painting.
 * @author akarnokd, 2009.10.10.
 */
public class BarPainter extends JComponent implements SwappableRenderer {
	/** */
	private static final long serialVersionUID = 1470655113225844893L;
	/** The current talk person. */
	TalkPerson person;
	/** The current talk state. */
	TalkState state;
	/** The next talk state. */
	TalkState next;
	/** Is the media playback active? */
	boolean mediaPlayback;
	/** The video thread. */
	Thread videoThread;
	/** The audio playback thread. */
	Thread audioThread;
	/** The audio playback channel. */
	volatile SourceDataLine sdl;
	/** The current label to display. */
	String label;
	/** Stop movie playback. */
	volatile boolean stop;
	/** The resource locator. */
	ResourceLocator rl;
	/** The language. */
	String lang;
	/** The text renderer. */
	TextRenderer txt;
	/** The image swap lock. */
	Lock swapLock;
	/** The front buffer. */
	BufferedImage frontBuffer;
	/** The back buffer. */
	BufferedImage backBuffer;
	/** The highlighted speech. */
	TalkSpeech highlight;
	/** The audio length in milliseconds. */
	volatile int audioLen;
	/** The current subtitle manager. */
	SubtitleManager subtitle;
	/** The label manager. */
	Labels labels;
	/** The list of choices. */
	final List<Choice> choices = new ArrayList<Choice>();
	/** A talk choice. */
	class Choice {
		/** The target rendering rectangle. */
		final Rectangle rect = new Rectangle();
		/** The choice text rows. */
		final List<String> rows = new ArrayList<String>();
		/**
		 * Paint the rows.
		 * @param g2 the graphics
		 * @param x0 the paint origin
		 * @param y0 the paint origin
		 * @param color the rendering color.
		 */
		public void paintTo(Graphics2D g2, int x0, int y0, int color) {
			int y = y0 + rect.y;
			for (int i = 0; i < rows.size(); i++) {
				txt.paintTo(g2, x0 + rect.x, y + 3, 14, color, rows.get(i));
				y += 20;
			}
		}
		/**
		 * Test if the mouse position in the rectangle.
		 * @param x0 the paint origin
		 * @param y0 the paint origin
		 * @param mouse the mouse coordinate
		 * @return true if in the rectangle
		 */
		public boolean test(int x0, int y0, Point mouse) {
			return rect.contains(mouse.x - x0, mouse.y - y0);
		}
	}
	/**
	 * Constructor.
	 * @param rl the resource locator
	 * @param lang the language
	 * @param labels the labels
	 */
	public BarPainter(ResourceLocator rl, String lang, Labels labels) {
		this.rl = rl;
		this.lang = lang;
		this.labels = labels;
		txt = new TextRenderer(rl);
		swapLock = new ReentrantLock();
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				doMouseClick(e);
			}
		});
		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				doMouseMove(e.getPoint());
			}
		});
	}
	/**
	 * Set the next talk state. 
	 * @param state the state
	 */
	void setState(TalkState state) {
		this.state = state;
		if (state != null) {
			int h0 = state.picture.getHeight();
			int w0 = state.picture.getWidth();
			int h = 0;
			choices.clear();
			int maxTw = 0;
			for (TalkSpeech ts : state.speeches) {
				Choice c = new Choice();
				int tw = txt.wrapText(labels.get(ts.text), w0, 14, c.rows);
				maxTw = Math.max(maxTw, tw);
				int dh = c.rows.size() * 20;
				c.rect.setBounds(0, h, tw, dh);
				h += dh + 20;
				choices.add(c);
			}
			// center them all
			int h1 = (h0 - h + 20) / 2;
			int w1 = (w0 - maxTw) / 2;
			for (Choice c : choices) {
				c.rect.x += w1;
				c.rect.y += h1;
			}
		}
	}
	/**
	 * On mouse clicked.
	 * @param e the event
	 */
	protected void doMouseClick(MouseEvent e) {
		if (state == null) {
			return;
		}
		if (mediaPlayback) {
			stop = true;
			sdl.stop();
			mediaPlayback = false;
			setState(next);
			frontBuffer = null;
			label = null;
			doMouseMove(relativize(MouseInfo.getPointerInfo().getLocation()));
			repaint();
		} else {
			int x0 = (getWidth() - state.picture.getWidth()) / 2;
			int y0 = (getHeight() - state.picture.getHeight()) / 2;
			int idx = 0;
			for (Choice c : choices) {
				if (c.test(x0, y0, e.getPoint())) {
					TalkSpeech ts = state.speeches.get(idx);
					next = person.states.get(ts.to);
					mediaPlayback = true;
					ts.spoken = true;
					startPlayback(ts.media);
					break;
				}
				idx++;
			}
			repaint();
		}
	}
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
	 * Start playback.
	 * @param media the media
	 */
	protected void startPlayback(final String media) {
		final CyclicBarrier barrier = new CyclicBarrier(2);
		final ResourcePlace audio = rl.get(lang, media, ResourceType.AUDIO);
		final ResourcePlace video = rl.get(lang, media, ResourceType.VIDEO);
		ResourcePlace sub = rl.get(lang, media, ResourceType.SUBTITLE);
		subtitle = new SubtitleManager(sub.open());
		stop = false;
		audioThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					AudioInputStream in = AudioSystem.getAudioInputStream(new BufferedInputStream(
							audio.open(), 256 * 1024));
					try {
						byte[] buffer = new byte[in.available()];
						in.read(buffer);
						byte[] buffer2 = AudioThread.split16To8(AudioThread.movingAverage(upscale8To16AndSignify(buffer), 1));
						try {
							AudioFormat streamFormat = new AudioFormat(22050, 16, 1, true, false);
							DataLine.Info clipInfo = new DataLine.Info(SourceDataLine.class, streamFormat);
							sdl = (SourceDataLine) AudioSystem.getLine(clipInfo);
							sdl.open();
							audioLen = buffer.length;
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
				}
			}
		});
		videoThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(video.open(), 1024 * 1024), 1024 * 1024));
					try {
						int w = Integer.reverseBytes(in.readInt());
						int h = Integer.reverseBytes(in.readInt());
						final int frames = Integer.reverseBytes(in.readInt());
						double fps = Integer.reverseBytes(in.readInt()) / 1000.0;
						
						init(w, h);
						
						int[] palette = new int[256];
						byte[] bytebuffer = new byte[w * h];
						int[] currentImage = new int[w * h];
						int frameCount = 0;
						long starttime = 0;
						int frames2 = frames;
						while (!stop) {
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
								if (frameCount == 0) {
									try {
										barrier.await();
									} catch (InterruptedException ex) {
										
									} catch (BrokenBarrierException ex) {
										
									}
									frames2 = (int)Math.ceil(audioLen * fps / 22050.0);
									starttime = System.nanoTime();
								}
								getBackbuffer().setRGB(0, 0, w, h, currentImage, 0, w);
								swap();
								setPosition(fps, frameCount);
								// wait the frame/sec
								starttime += (1000000000.0 / fps);
				       			LockSupport.parkNanos((Math.max(0, starttime - System.nanoTime())));
				       			frameCount++;
							}
						}
						// continue to emit reposition events
						if (frames2 > frames && !stop) {
							for (int i = frames; i < frames2 && !stop; i++) {
								setPosition(fps, i);
								// wait the frame/sec
								starttime += (1000000000.0 / fps);
				       			LockSupport.parkNanos((Math.max(0, starttime - System.nanoTime())));
							}
						}
					} finally {
						try { in.close(); } catch (IOException ex) {  }
					}
				} catch (IOException ex) {
					// TODO log
					ex.printStackTrace();
				} finally {
					SwingUtilities.invokeLater(new Runnable() {
						@Override 
						public void run() {
							mediaPlayback = false;
							setState(next);
							frontBuffer = null;
							backBuffer = null;
							label = null;
							doMouseMove(relativize(MouseInfo.getPointerInfo().getLocation()));
							repaint();
						}
					});
				}
			}
		});
		audioThread.start();
		videoThread.start();
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
				long time = (long)(frameCount * 1000 / fps);
				label = subtitle.get(time);
				repaint();
			}
		});
	}
	/**
	 * If the mouse moves.
	 * @param e the event
	 */
	protected void doMouseMove(Point e) {
		if (state == null) {
			return;
		}
		if (!mediaPlayback) {
			int x0 = (getWidth() - state.picture.getWidth()) / 2;
			int y0 = (getHeight() - state.picture.getHeight()) / 2;
			int idx = 0;
			highlight = null;
			for (Choice c : choices) {
				if (c.test(x0, y0, e.getLocation())) {
					highlight = state.speeches.get(idx);
					break;
				}
				idx++;
			}
			
			repaint();
		}
	}
	/**
	 * Relativize the given screen coordinate.
	 * @param pt the point
	 * @return the relativized point
	 */
	protected Point relativize(Point pt) {
		Point sp = getLocationOnScreen();
		return new Point(pt.x - sp.x, pt.y - sp.y);
	}
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		if (mediaPlayback && frontBuffer != null) {
			swapLock.lock();
			try {
				if (state.picture != null) {
					g2.translate((getWidth() - state.picture.getWidth()) / 2, (getHeight() - state.picture.getHeight()) / 2);
					g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					g2.drawImage(frontBuffer, 0, 
							0, 
							state.picture.getWidth(), state.picture.getHeight(), null);
					if (label != null) {
						paintLabel(g2, state.picture.getWidth(), state.picture.getHeight());
					}
				}				
			} finally {
				swapLock.unlock();
			}
		} else {
			if (state != null && state.picture != null) {
				int x0 = (getWidth() - state.picture.getWidth()) / 2;
				int y0 = (getHeight() - state.picture.getHeight()) / 2;

				g2.drawImage(state.picture, x0, y0, null);
				int idx = 0;
				for (Choice c : choices) {
					TalkSpeech ts = state.speeches.get(idx);
					int color = ts == highlight ? TextRenderer.WHITE : (ts.spoken ? TextRenderer.GRAY : TextRenderer.YELLOW);
					c.paintTo(g2, x0, y0, color);
					idx++;
				}
			}
		}
	}
	/**
	 * Paint a word-wrapped label.
	 * @param g2 the graphics context.
	 * @param width the draw width
	 * @param height the draw height
	 */
	public void paintLabel(Graphics2D g2, int width, int height) {
		String[] words = label.trim().split("\\s+");
		int i = 0;
		StringBuilder line = new StringBuilder();
		List<String> lines = new ArrayList<String>();
		while (i < words.length) {
			line.setLength(0);
			for (; i < words.length; i++) {
				if (txt.getTextWidth(14, line + " " + words[i]) >= state.picture.getWidth()) {
					lines.add(line.toString());
					break;
				} else {
					if (line.length() > 0) {
						line.append(" ");
					}
					line.append(words[i]);
				}
			}
		}
		if (line.length() > 0) {
			lines.add(line.toString());
		}
		int y = height - lines.size() * 21 - 7;
		for (String s : lines) {
			int tw = txt.getTextWidth(14, s);
			int x = (width - tw) / 2;
			txt.paintTo(g2, x, y, 14, TextRenderer.WHITE, s);
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
	
}
