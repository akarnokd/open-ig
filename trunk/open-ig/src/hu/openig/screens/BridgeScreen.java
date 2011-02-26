/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;


import hu.openig.core.Act;
import hu.openig.core.SwappableRenderer;
import hu.openig.model.WalkPosition;
import hu.openig.model.WalkShip;
import hu.openig.model.WalkTransition;
import hu.openig.render.TextRenderer;
import hu.openig.ui.UIMouse;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.SwingUtilities;

/**
 * The bridge rendering screen.
 * @author akarnokd, 2010.01.11.
 */
public class BridgeScreen extends ScreenBase {
	/** The screen origins. */
	final Rectangle origin = new Rectangle();
	/** The message panel open rectangle. */
	final Rectangle messageOpenRect = new Rectangle();
	/** The message list rectangle. */
	final Rectangle messageListRect = new Rectangle();
	/** The projector rectangle. */
	final Rectangle projectorRect = new Rectangle();
	/** The video rectangle. */
	final Rectangle videoRect = new Rectangle();
	/** The message rectangle. */
	final Rectangle messageRect = new Rectangle();
	/** The poligin to click on to close the projector. */
	Polygon closeProjector;
	/** The poligon to click on to close the message panel. */
	Polygon closeMessage;
	@Override
	public void onResize() {
		origin.setBounds((parent.getWidth() - 640) / 2, 20 + (parent.getHeight() - 38 - 442) / 2, 640, 442);
		messageOpenRect.setBounds(origin.x + 572, origin.y + 292, 68, 170);
		projectorRect.setBounds(origin.x + (origin.width - 524) / 2 - 4, origin.y, 524, 258);
		videoRect.setBounds(projectorRect.x + 99, projectorRect.y + 11, 320, 240);
		messageRect.setBounds(origin.x + origin.width - 298, origin.y + origin.height - 182, 298, 182);

		
		
		closeProjector = new Polygon(
			new int[] { 
					origin.x, projectorRect.x, 
					projectorRect.x, projectorRect.x + projectorRect.width, 
					projectorRect.x + projectorRect.width, origin.x + origin.width - 1, 
					origin.x + origin.width - 1, messageRect.x, 
					messageRect.x, origin.x
				},
			new int[] { 
					origin.y, origin.y, 
					projectorRect.y + projectorRect.height, projectorRect.y + projectorRect.height, 
					origin.y, origin.y, 
					messageRect.y, messageRect.y, 
					origin.y + origin.height - 1, origin.y + origin.height - 1
					
				},
			10
		);
		closeMessage = new Polygon(
			new int[] {
				origin.x, origin.x + origin.width - 1, origin.x + origin.width - 1, messageRect.x,
				messageRect.x, origin.x
			},
			new int[] {
				origin.y, origin.y, messageRect.y, messageRect.y, origin.y + origin.height - 1, 
				origin.y + origin.height - 1
			},
			6
		);
	}
	/** The message front buffer. */
	BufferedImage messageFront;
	/** The message back buffer. */
	BufferedImage messageBack;
	/** The message lock. */
	final Lock messageLock = new ReentrantLock();
	/** The projector front buffer. */
	BufferedImage projectorFront;
	/** The projector back buffer. */
	BufferedImage projectorBack;
	/** The projector lock. */
	final Lock projectorLock = new ReentrantLock();
	/** The video front buffer. */
	BufferedImage videoFront;
	/** The video back buffer. */
	BufferedImage videoBack;
	/** The video lock. */
	final Lock videoLock = new ReentrantLock();
	/** The video subtitle. */
	String videoSubtitle;
	/** The message panel video animator. */
	volatile MediaPlayer messageAnim;
	/** The projector animator. */
	volatile MediaPlayer projectorAnim;
	/** The video animator. */
	volatile MediaPlayer videoAnim;
	/** Is the message panel open? */
	boolean messageOpen;
	/** Is the projector open? */
	boolean projectorOpen;
	/** The opening/closing animation is in progress. */
	boolean openCloseAnimating;
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#finish()
	 */
	@Override
	public void onFinish() {
		if (messageAnim != null) {
			messageAnim.terminate();
		}
		if (projectorAnim != null) {
			projectorAnim.terminate();
		}
		if (videoAnim != null) {
			videoAnim.terminate();
		}
	}
	/**
	 * Play the video for the. 
	 */
	void playMessageAppear() {
		openCloseAnimating = true;
		messageAnim = new MediaPlayer(commons, commons.world.getCurrentLevel().messageAppear, new SwappableRenderer() {
			@Override
			public BufferedImage getBackbuffer() {
				return messageBack;
			}
			@Override
			public void init(int width, int height) {
				messageFront = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				messageFront.setAccelerationPriority(0);
				messageBack = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				messageBack.setAccelerationPriority(0);
			}
			@Override
			public void swap() {
				messageLock.lock();
				try {
					BufferedImage temp = messageFront;
					messageFront = messageBack;
					messageBack = temp;
				} finally {
					messageLock.unlock();
					repaint();
				}
			}
		});
		messageAnim.onComplete = new Act() {
			@Override
			public void act() {
				openCloseAnimating = false;
				repaint();
			}
		};
		messageAnim.start();
	}
	/**
	 * Play message panel opening.
	 */
	void playMessageOpen() {
		openCloseAnimating = true;
		messageAnim = new MediaPlayer(commons, commons.world.getCurrentLevel().messageOpen, new SwappableRenderer() {
			@Override
			public BufferedImage getBackbuffer() {
				return messageBack;
			}
			@Override
			public void init(int width, int height) {
				messageFront = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				messageFront.setAccelerationPriority(0);
				messageBack = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				messageBack.setAccelerationPriority(0);
			}
			@Override
			public void swap() {
				messageLock.lock();
				try {
					BufferedImage temp = messageFront;
					messageFront = messageBack;
					messageBack = temp;
				} finally {
					messageLock.unlock();
					repaint();
				}
			}
		});
		messageAnim.onComplete = new Act() {
			@Override
			public void act() {
				messageOpen = true;
				openCloseAnimating = false;
				repaint();
			}
		};
		messageAnim.start();
	}
	/** Play message panel closing. */
	void playMessageClose() {
		openCloseAnimating = true;
		messageAnim = new MediaPlayer(commons, commons.world.getCurrentLevel().messageClose, new SwappableRenderer() {
			@Override
			public BufferedImage getBackbuffer() {
				return messageBack;
			}
			@Override
			public void init(int width, int height) {
				messageFront = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				messageFront.setAccelerationPriority(0);
				messageBack = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				messageBack.setAccelerationPriority(0);
			}
			@Override
			public void swap() {
				messageLock.lock();
				try {
					BufferedImage temp = messageFront;
					messageFront = messageBack;
					messageBack = temp;
				} finally {
					messageLock.unlock();
					repaint();
				}
			}
		});
		messageAnim.onComplete = new Act() {
			@Override
			public void act() {
				messageOpen = false;
				openCloseAnimating = false;
				repaint();
			}
		};
		messageAnim.start();
	}
	/** Play message panel closing. */
	void playProjectorOpen() {
		openCloseAnimating = true;
		projectorAnim = new MediaPlayer(commons, commons.world.getCurrentLevel().projectorOpen, new SwappableRenderer() {
			@Override
			public BufferedImage getBackbuffer() {
				return projectorBack;
			}
			@Override
			public void init(int width, int height) {
				projectorFront = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				projectorFront.setAccelerationPriority(0);
				projectorBack = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				projectorBack.setAccelerationPriority(0);
			}
			@Override
			public void swap() {
				projectorLock.lock();
				try {
					BufferedImage temp = projectorFront;
					projectorFront = projectorBack;
					projectorBack = temp;
				} finally {
					projectorLock.unlock();
					repaint();
				}
			}
		});
		projectorAnim.onComplete = new Act() {
			@Override
			public void act() {
				projectorOpen = true;
				openCloseAnimating = false;
				repaint();
			}
		};
		projectorAnim.start();
	}
	/** Play message panel closing. */
	void playProjectorClose() {
		openCloseAnimating = true;
		projectorAnim = new MediaPlayer(commons, commons.world.getCurrentLevel().projectorClose, new SwappableRenderer() {
			@Override
			public BufferedImage getBackbuffer() {
				return projectorBack;
			}
			@Override
			public void init(int width, int height) {
				projectorFront = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				projectorFront.setAccelerationPriority(0);
				projectorBack = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				projectorBack.setAccelerationPriority(0);
			}
			@Override
			public void swap() {
				projectorLock.lock();
				try {
					BufferedImage temp = projectorFront;
					projectorFront = projectorBack;
					projectorBack = temp;
				} finally {
					projectorLock.unlock();
					repaint();
				}
			}
		});
		projectorAnim.onComplete = new Act() {
			@Override
			public void act() {
				projectorOpen = false;
				openCloseAnimating = false;
				repaint();
			}
		};
		projectorAnim.start();
	}
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#initialize()
	 */
	@Override
	public void onInitialize() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mouseReleased(int, int, int, int)
	 */
	@Override
	public boolean mouse(UIMouse e) {
		if (e.type == UIMouse.Type.UP) {
			if (!openCloseAnimating) {
				if (messageOpen && !projectorOpen) {
					if (closeMessage.contains(e.x - origin.x, e.y - origin.y)) {
						playMessageClose();
					}
				} else
				if (projectorOpen) {
					if (closeProjector.contains(e.x - origin.x, e.y - origin.y)) {
						if (videoAnim != null) {
							videoAnim.stop();
						}
						playProjectorClose();
					}
				} else 
				if (!messageOpen && !projectorOpen) {
					if (messageOpenRect.contains(e.x, e.y)) {
						playMessageOpen();
					} else {
						for (WalkTransition tr : commons.world.getCurrentLevel().walk.transitions) {
							if (tr.area.contains(e.x - origin.x, e.y - origin.y)) {
								final String to = tr.to; 
								if (to.startsWith("*") && (tr.media == null || tr.media.isEmpty())) {
									// move to the screen directly.
									commons.switchScreen(to);
								} else {
									final ShipwalkScreen sws = commons.screens.shipwalk;
									sws.position = commons.world.getCurrentLevel().walk;
									
									WalkShip ship = commons.world.getShip();
									sws.next = ship.positions.get(tr.to);
									sws.displayPrimary();
									final String media = tr.media;
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											sws.startTransition(media);
											sws.onCompleted = new Act() {
												@Override
												public void act() {
													commons.switchScreen(to);
													sws.onCompleted = null;
												}
											};
										}
									});
								}
							}
						}
					}
				}
			}
		}
		return false;
	}
	/** The level specific background. */
	BufferedImage background;
	@Override
	public void onEnter() {
		background = commons.world.bridge.levels.get(commons.world.level).image;
		onResize();
		playMessageAppear();
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#onLeave()
	 */
	@Override
	public void onLeave() {
		if (messageAnim != null) {
			messageAnim.stop();
		}
		if (projectorAnim != null) {
			projectorAnim.stop();
		}
		if (videoAnim != null) {
			videoAnim.stop();
		}
	}
	
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#paintTo(java.awt.Graphics2D)
	 */
	@Override
	public void draw(Graphics2D g2) {
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, parent.getWidth(), parent.getHeight());
		
		g2.drawImage(background, origin.x, origin.y, null);
		
		
		messageLock.lock();
		try {
			if (messageFront != null) {
				g2.drawImage(messageFront, messageRect.x, messageRect.y, null);
			}
		} finally {
			messageLock.unlock();
		}
		
		projectorLock.lock();
		try {
			if (projectorFront != null) {
				g2.drawImage(projectorFront, projectorRect.x, projectorRect.y, null);
			}
		} finally {
			projectorLock.unlock();
		}

		videoLock.lock();
		try {
			if (videoFront != null) {
				g2.drawImage(projectorFront, videoRect.x, videoRect.y, videoRect.width, videoRect.height, null);
				if (videoSubtitle != null) {
					paintLabel(g2, origin.x, videoRect.y + videoRect.height, origin.width);
				}
			}
		} finally {
			videoLock.unlock();
		}
		Composite cp = g2.getComposite();
		g2.setComposite(AlphaComposite.SrcOver.derive(0.6f));
		g2.setColor(Color.WHITE);
		if (projectorOpen) {
			g2.fillPolygon(closeProjector);
		} else
		if (messageOpen) {
			g2.fillPolygon(closeMessage);
		} else {
			WalkPosition wp = commons.world.getCurrentLevel().walk;
			AffineTransform tf = g2.getTransform();
			g2.translate(origin.x, origin.y);
			
			for (WalkTransition tr : wp.transitions) {
				g2.fillPolygon(tr.area);
			}
			
			g2.setTransform(tf);
		}
		g2.setComposite(cp);
	}
	/**
	 * Paint a word-wrapped label.
	 * @param g2 the graphics context.
	 * @param x0 the X coordinate
	 * @param y0 the Y coordinate
	 * @param width the draw width
	 */
	public void paintLabel(Graphics2D g2, int x0, int y0, int width) {
		List<String> lines = new ArrayList<String>();
		int maxWidth = commons.text.wrapText(videoSubtitle, width, 14, lines);
		int y = y0;
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

}
