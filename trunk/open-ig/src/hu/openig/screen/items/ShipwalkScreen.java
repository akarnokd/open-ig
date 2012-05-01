/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.core.SwappableRenderer;
import hu.openig.model.Screens;
import hu.openig.model.SoundType;
import hu.openig.model.WalkPosition;
import hu.openig.model.WalkTransition;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.MediaPlayer;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Button;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * The ship walk-around screen.
 * @author akarnokd, 2010.01.11.
 */
public class ShipwalkScreen extends ScreenBase implements SwappableRenderer {
	/** The current position. */
	private WalkPosition position;
	/** The next position after the transition. Might be null because of a special output. */
	public WalkPosition next;
	/** The next position's id. */
	public String nextId;
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
	final Rectangle origin = new Rectangle(0, 0, 640, 442);
	/** The action to call when the transition ends. */
	public Action0 onCompleted;
	/** The position picture. */
	BufferedImage picture;
	@Override
	public void onResize() {
		RenderTools.centerScreen(origin, width, height, true);
	}

	@Override
	public void onFinish() {
		onEndGame();
		if (video != null) {
			video.stop();
			video = null;
		}
	}

	@Override
	public void onInitialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean keyboard(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			if (videoMode) {
				video.stop();
				setNextPosition();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean mouse(UIMouse e) {
		boolean rep = false;
		switch (e.type) {
		case MOVE:
		case DRAG:
			if (!videoMode) {
				WalkTransition prev = pointerTransition;
				pointerTransition = null;
				if (position != null) {
					for (WalkTransition wt : position.transitions) {
						if (wt.area.contains(e.x - origin.x, e.y - origin.y)) {
							pointerTransition = wt;
							break;
						}
					}
				}
				if (prev != pointerTransition) {
					rep = true;
				}
			}
			break;
		case DOWN:
			if (e.has(Button.LEFT)) {
				if (videoMode) {
					video.stop();
					setNextPosition();
					rep = true;
				} else {
					if (position != null) {
						for (WalkTransition wt : position.transitions) {
							if (wt.area.contains(e.x - origin.x, e.y - origin.y)) {
								next = position.ship.positions.get(wt.to);
								nextId = wt.to;
								if (wt.media != null && !wt.media.isEmpty()) {
									startTransition(wt.media);
								} else {
									setNextPosition();
									rep = true;
								}
								break;
							}
						}
					}
				}
			}
			break;
		default:
		}
		return rep;
	}

	@Override
	public void onEnter(Screens mode) {
		resize();
	}

	@Override
	public void onLeave() {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(Graphics2D g2) {
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, getInnerWidth(), getInnerHeight());
		if (position != null) {
			g2.translate(origin.x, origin.y);
		}
		if (videoMode) {
			swapLock.lock();
			try {
				if (frontBuffer != null && position != null && picture != null) {
					g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					g2.drawImage(frontBuffer, 0, 0, picture.getWidth(), picture.getHeight(), null);
				} else 
				if (position != null && picture != null) {
					g2.drawImage(picture, 0, 0, null);
				}
			} finally {
				swapLock.unlock();
			}
			
		} else {
			if (position != null && picture != null) {
				g2.drawImage(picture, 0, 0, null);
				
				if (pointerTransition != null) {
					String gotoLocation = get(pointerTransition.label);
					Rectangle r = pointerTransition.area.getBounds();
					
					int tw = commons.text().getTextWidth(14, gotoLocation) + 10;
					int th = 20;
					
					
					int tx = r.x + (r.width - tw) / 2;
					int ty = r.y + (r.height - th) / 2;
					// do not let the text slide out of the viewport
					if (tx < 0) {
						tx = 0;
					}
					if (tx + tw >= origin.width) {
						tx = origin.width - tw;
					}
					if (ty < 0) {
						ty = 0;
					}
					if (ty + th >= origin.height) {
						ty = origin.height - th;
					}
					
					Composite cp = g2.getComposite();
					g2.setComposite(AlphaComposite.SrcOver.derive(0.85f));
					g2.setColor(Color.BLACK);
					g2.fillRect(tx, ty, tw, th);
					g2.setComposite(cp);
					commons.text().paintTo(g2, tx + 5, ty + 3, 14, TextRenderer.YELLOW, gotoLocation);
				}
			}
		}
		if (position != null) {
			g2.translate(-origin.x, -origin.y);
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
		askRepaint();
	}

	/**
	 * Start a transition playback video.
	 * @param video the transition video
	 */
	protected void startTransition(final String video) {
		videoMode = true;
		this.video = new MediaPlayer(commons, video, this);
		this.video.onComplete = new Action0() {
			@Override
			public void invoke() {
				videoMode = false;
				try {
					setNextPosition();
					
					if (onCompleted != null) {
						onCompleted.invoke();
					}
				} finally {
					askRepaint();
				}
			}
		};
		swapLock.lock();
		try {
			backBuffer = null;
			frontBuffer = null;
		} finally {
			swapLock.unlock();
		}
		this.video.start();
	}
	/**
	 * Set the current position to the next position.
	 */
	protected void setNextPosition() {
		if (nextId.startsWith("*")) {
			commons.switchScreen(nextId);
		} else {
			setPosition(next);
			pointerTransition = null;
			commons.control().moveMouse();
			// simple sound hack for cabin
			if (position != null && position.id.equals("cabin")) {
				screenSound(SoundType.CABIN);
			}
		}
	}
	@Override
	public Screens screen() {
		return Screens.SHIPWALK;
	}
	@Override
	public void onEndGame() {
		next = null;
		position = null;
		picture = null;
		pointerTransition = null;
	}
	/**
	 * Set the position to the given new state.
	 * @param pos the new position
	 */
	public void setPosition(WalkPosition pos) {
		this.position = pos;
		if (pos != null) {
			picture = rl.getImage(pos.pictureName);
		} else {
			picture = null;
		}
	}
}
