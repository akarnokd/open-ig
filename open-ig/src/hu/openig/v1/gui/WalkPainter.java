/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.gui;

import hu.openig.core.SwappableRenderer;
import hu.openig.v1.ResourceLocator;
import hu.openig.v1.ResourceType;
import hu.openig.v1.ResourceLocator.ResourcePlace;
import hu.openig.v1.core.WalkPosition;
import hu.openig.v1.core.WalkShip;
import hu.openig.v1.core.WalkTransition;
import hu.openig.v1.render.TextRenderer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * The ship walk painter.
 * @author karnok, 2009.10.09.
 * @version $Revision 1.0$
 */
public class WalkPainter extends JComponent implements SwappableRenderer {
	/** */
	private static final long serialVersionUID = -8967352094580553501L;
	/** The current ship. */
	WalkShip ship;
	/** The current position. */
	WalkPosition position;
	/** The next position after the transition. Might be null because of a special output. */
	WalkPosition next;
	/** Is video playing. */
	boolean videoMode;
	/** The video front buffer. */
	BufferedImage frontBuffer;
	/** The video back buffer. */
	BufferedImage backBuffer;
	/** The buffer swap lock. */
	Lock swapLock;
	/** The current movie thread. */
	Thread movieThread;
	/** Stop movie playback. */
	volatile boolean stop;
	/** The resource locator. */
	ResourceLocator rl;
	/** The language. */
	String lang;
	/** The transition the mouse is pointing at. */
	WalkTransition pointerTransition;
	/** The text renderer. */
	protected TextRenderer txt;
	/**
	 * Constructor.
	 * @param rl the resource locator
	 * @param lang the current language
	 */
	public WalkPainter(ResourceLocator rl, String lang) {
		super();
		txt = new TextRenderer(rl);
		swapLock = new ReentrantLock();
		this.rl = rl;
		this.lang = lang;
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				doClick(e);
			}
		});
		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				doMouseMove(e);
			}
		});
	}
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		if (position != null) {
			g2.translate((getWidth() - position.picture.getWidth()) / 2,
					(getHeight() - position.picture.getHeight()) / 2);
		}
		if (videoMode) {
			swapLock.lock();
			try {
				if (frontBuffer != null) {
					g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					g2.drawImage(frontBuffer, 0, 0, position.picture.getWidth(), position.picture.getHeight(), null);
				}
			} finally {
				swapLock.unlock();
			}
			
		} else {
			if (position != null) {
				g.drawImage(position.picture, 0, 0, null);
				
				g2.setComposite(AlphaComposite.SrcOver.derive(0.5f));
				g2.setColor(Color.WHITE);
				for (WalkTransition wt : position.transitions) {
					g2.setColor(Color.WHITE);
					g2.fill(wt.area);
					Rectangle rect = wt.area.getBounds();
					
					if (pointerTransition == wt) {
						g2.setColor(Color.BLACK);
						int w = txt.getTextWidth(14, pointerTransition.label);
						txt.paintTo(g2, rect.x + (rect.width - w) / 2, rect.y + (rect.height - 14) / 2, 14, TextRenderer.RED, pointerTransition.label);
					}
				}
			}
		}
	}
	@Override
	public BufferedImage getBackbuffer() {
		swapLock.lock();
		try {
			return backBuffer;
		} finally {
			swapLock.unlock();
		}
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
			BufferedImage tmp = backBuffer;
			backBuffer = frontBuffer;
			frontBuffer = tmp;
		} finally {
			swapLock.unlock();
		}
		repaint();
	}
	/**
	 * If clicked, perform the transition of that area.
	 * @param e the mouse event
	 */
	protected void doClick(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
			if (videoMode) {
				stop = true;
				position = next;
			} else {
				if (position != null) {
					for (WalkTransition wt : position.transitions) {
						if (wt.area.contains(e.getX() - (getWidth() - position.picture.getWidth()) / 2, 
								e.getY() - (getHeight() - position.picture.getHeight()) / 2)) {
							next = ship.positions.get(wt.to);
							if (!wt.media.isEmpty()) {
								startTransition(rl.get(lang, wt.media, ResourceType.VIDEO));
							} else {
								position = next;
							}
							break;
						}
					}
				}
			}
		}
	}
	/**
	 * React to mouse movement.
	 * @param e the event
	 */
	protected void doMouseMove(MouseEvent e) {
		pointerTransition = null;
		if (position != null) {
			for (WalkTransition wt : position.transitions) {
				if (wt.area.contains(e.getX() - (getWidth() - position.picture.getWidth()) / 2, 
						e.getY() - (getHeight() - position.picture.getHeight()) / 2)) {
					pointerTransition = wt;
					break;
				}
			}
		}
		repaint();
	}
	/**
	 * Start a transition playback video.
	 * @param video the transition video
	 */
	protected void startTransition(final ResourcePlace video) {
		stop = false;
		videoMode = true;
		movieThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(video.open(), 256 * 1024), 256 * 1024));
					try {
						int w = Integer.reverseBytes(in.readInt());
						int h = Integer.reverseBytes(in.readInt());
						in.readInt();
						double fps = Integer.reverseBytes(in.readInt()) / 1000.0;
						
						init(w, h);
						
						int[] palette = new int[256];
						byte[] bytebuffer = new byte[w * h];
						int[] currentImage = new int[w * h];
						int frameCount = 0;
						long starttime = 0;
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
								in.read(bytebuffer);
								for (int i = 0; i < bytebuffer.length; i++) {
									int c0 = palette[bytebuffer[i] & 0xFF];
									if (c0 != 0) {
										currentImage[i] = c0;
									}
								}
								if (frameCount == 0) {
									starttime = System.nanoTime();
								}
								getBackbuffer().setRGB(0, 0, w, h, currentImage, 0, w);
								swap();
								// wait the frame/sec
								starttime += (1000000000.0 / fps);
				       			LockSupport.parkNanos((Math.max(0, starttime - System.nanoTime())));
				       			frameCount++;
							}
						}
					} finally {
						try { in.close(); } catch (IOException ex) {
							// TODO log
							ex.printStackTrace();
						}
					}
				} catch (IOException ex) {
					// TODO log
					ex.printStackTrace();
				}
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						videoMode = false;
						position = next;
						repaint();
					}
				});
			}
		});
		movieThread.start();
	}
}
