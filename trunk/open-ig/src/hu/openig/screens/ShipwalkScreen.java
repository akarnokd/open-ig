/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import hu.openig.core.SwappableRenderer;
import hu.openig.core.Act;
import hu.openig.model.WalkPosition;
import hu.openig.model.WalkTransition;
import hu.openig.render.TextRenderer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author karnokd, 2010.01.11.
 * @version $Revision 1.0$
 */
public class ShipwalkScreen extends ScreenBase implements SwappableRenderer {
	/** The current position. */
	public WalkPosition position;
	/** The next position after the transition. Might be null because of a special output. */
	public WalkPosition next;
	/** Is video playing. */
	boolean videoMode;
	/** The video front buffer. */
	BufferedImage frontBuffer;
	/** The video back buffer. */
	BufferedImage backBuffer;
	/** The buffer swap lock. */
	final Lock swapLock = new ReentrantLock();
	/** The transition the mouse is pointing at. */
	WalkTransition pointerTransition;
	/** The transition video player. */
	MediaPlayer video;
	/** The rendering origin. */
	final Rectangle origin = new Rectangle();
	/** The action to call when the transition ends. */
	public Act onCompleted;
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#doResize()
	 */
	@Override
	public void doResize() {
		origin.setBounds((parent.getWidth() - 640) / 2, 20 + (parent.getHeight() - 38 - 442) / 2, 640, 442);
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#finish()
	 */
	@Override
	public void finish() {
		if (video != null) {
			video.stop();
		}
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

	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mouseMoved(int, int, int, int)
	 */
	@Override
	public void mouseMoved(int button, int x, int y, int modifiers) {
		WalkTransition prev = pointerTransition;
		pointerTransition = null;
		if (position != null) {
			for (WalkTransition wt : position.transitions) {
				if (wt.area.contains(x - origin.x, y - origin.y)) {
					pointerTransition = wt;
					break;
				}
			}
		}
		if (prev != pointerTransition) {
			repaint();
		}
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mousePressed(int, int, int, int)
	 */
	@Override
	public void mousePressed(int button, int x, int y, int modifiers) {
		if (button == MouseEvent.BUTTON1) {
			if (videoMode) {
				video.stop();
				setNextPosition();
				requestRepaint();
			} else {
				if (position != null) {
					for (WalkTransition wt : position.transitions) {
						if (wt.area.contains(x - origin.x, y - origin.y)) {
							next = commons.world.getShip().positions.get(wt.to);
							if (!wt.media.isEmpty()) {
								startTransition(wt.media);
							} else {
								setNextPosition();
								requestRepaint();
							}
							break;
						}
					}
				}
			}
		}

	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mouseReleased(int, int, int, int)
	 */
	@Override
	public void mouseReleased(int button, int x, int y, int modifiers) {
		// TODO Auto-generated method stub

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
		doResize();
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
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, parent.getWidth(), parent.getHeight());
		if (position != null) {
			g2.translate(origin.x, origin.y);
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
				g2.drawImage(position.picture, 0, 0, null);
				
				g2.setComposite(AlphaComposite.SrcOver.derive(0.5f));
				g2.setColor(Color.WHITE);
				for (WalkTransition wt : position.transitions) {
					g2.setColor(Color.WHITE);
					g2.fill(wt.area);
					Rectangle rect = wt.area.getBounds();
					
					if (pointerTransition == wt) {
						g2.setColor(Color.BLACK);
						int w = commons.text.getTextWidth(14, pointerTransition.label);
						commons.text.paintTo(g2, rect.x + (rect.width - w) / 2, rect.y + (rect.height - 14) / 2, 14, TextRenderer.RED, pointerTransition.label);
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

	@Override
	public void mouseDoubleClicked(int button, int x, int y, int modifiers) {
	}
	/**
	 * Start a transition playback video.
	 * @param video the transition video
	 */
	protected void startTransition(final String video) {
		videoMode = true;
		this.video = new MediaPlayer(commons, video, this);
		this.video.onComplete = new Act() {
			@Override
			public void act() {
				setNextPosition();
				videoMode = false;
				if (onCompleted != null) {
					onCompleted.act();
				}
				repaint();
			}
		};
		this.video.start();
	}
	/**
	 * Set the current position to the next position.
	 */
	protected void setNextPosition() {
		position = next;
		if (position != null && position.id.startsWith("*")) {
			commons.switchScreen(position.id);
		}
	}
}
