/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.core.Action1;
import hu.openig.core.ResourceType;
import hu.openig.core.SwappableRenderer;
import hu.openig.model.Player;
import hu.openig.model.Screens;
import hu.openig.model.VideoAudio;
import hu.openig.model.WalkPosition;
import hu.openig.model.WalkTransition;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.MediaPlayer;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;
import hu.openig.utils.U;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;

import javax.swing.SwingUtilities;



/**
 * The diplomacy screen.
 * @author akarnokd, 2010.01.11.
 */
public class DiplomacyScreen extends ScreenBase {
	/** The panel base rectangle. */
	final Rectangle base = new Rectangle();
	/** The transition the mouse is pointing at. */
	WalkTransition pointerTransition;
	/** The projector rectangle. */
	final Rectangle projectorRect = new Rectangle();
	/** The projector front buffer. */
	BufferedImage projectorFront;
	/** The projector back buffer. */
	BufferedImage projectorBack;
	/** The projector lock. */
	final Lock projectorLock = new ReentrantLock();
	/** The projector animator. */
	volatile MediaPlayer projectorAnim;
	/** The action to invoke when the projector reached its end of animation. */
	Action0 onProjectorComplete;
	/** Is the projector open? */
	boolean projectorOpen;
	/** The projector is closing. */
	boolean projectorClosing;
	/** The opening/closing animation is in progress. */
	boolean openCloseAnimating;
	/** The list of races in the projector. */
	OptionList races;
	/** The list of stances in the porjector. */
	OptionList stances;
	/** The list of options once a race has been selected. */
	OptionList options;
	/** Update the race listing periodically. */
	Closeable raceUpdater;
	/** The stance matrix. */
	StanceMatrix stanceMatrix;
	/** Show the panel label? */
	boolean showPanelLabel;
	/** Show the close label? */
	boolean showCloseLabel;
	/** The head animation. */
	HeadAnimation headAnimation;
	/** To close the animation. */
	Closeable headAnimationClose;
	/** The current darkening index. */
	int darkeningIndex;
	/** The darkening steps. */
	int darkeningMax = 10;
	/** The target alpha value. */
	float darkeningAlpha = 0.5f;
	/** The (un)darkening animation. */
	Closeable darkening;
	/** The other player. */
	Player other;
	@Override
	public void onInitialize() {
		base.setBounds(0, 0, 
				commons.diplomacy().base.getWidth(), commons.diplomacy().base.getHeight());
		
		races = new OptionList();
		stances = new OptionList();
		stances.before = 2;
		stances.after = 2 + 3;
		stances.textsize = 10;
		options = new OptionList();
		options.visible(false);
		
		races.onSelect = new Action1<Integer>() {
			@Override
			public void invoke(Integer value) {
				onSelectRace(value);
			}
		};
		races.onHighlight = new Action1<Integer>() {
			@Override
			public void invoke(Integer value) {
				onRaceHighlight(value, races.items.get(value).hover);
			}
		};
		stances.onHighlight = new Action1<Integer>() {
			@Override
			public void invoke(Integer value) {
				onRaceHighlight(value, stances.items.get(value).hover);
			}
		};
		stances.onSelect = new Action1<Integer>() {
			@Override
			public void invoke(Integer value) {
				onSelectRace(value);
			}
		};
		
		stanceMatrix = new StanceMatrix();
		
		addThis();
	}

	@Override
	public void onEnter(Screens mode) {
		raceUpdater = commons.register(1000, new Action0() {
			@Override
			public void invoke() {
				updateRaces();
			}
		});
		updateRaces();
		races.visible(false);
		stances.visible(false);
		stanceMatrix.visible(false);
	}

	@Override
	public void onLeave() {
		openCloseAnimating = false;
		projectorOpen = false;
		projectorClosing = false;
		showCloseLabel = false;
		showPanelLabel = false;
		
		if (projectorAnim != null) {
			projectorAnim.stop();
			projectorAnim = null;
			projectorFront = null;
			projectorBack = null;
			onProjectorComplete = null;
		}
		if (raceUpdater != null) {
			close0(raceUpdater);
			raceUpdater = null;
		}
		if (headAnimationClose != null) {
			close0(headAnimationClose);
			headAnimation = null;
		}
		if (darkening != null) {
			close0(darkening);
		}
		other = null;
	}

	@Override
	public void onFinish() {
		if (projectorAnim != null) {
			projectorAnim.terminate();
			projectorAnim = null;
		}
	}

	@Override
	public void onResize() {
		RenderTools.centerScreen(base, width, height, true);
		
		projectorRect.setBounds(base.x + (base.width - 524) / 2 - 10, base.y, 524, 258);
		
		races.location(base.x + 155, base.y + 10);
		stances.location(base.x + 445, base.y + 10);
		
		stanceMatrix.bounds(base.x + 157, base.y + 10, 320, 239);
	}
	
	@Override
	public boolean mouse(UIMouse e) {
		if (!base.contains(e.x, e.y) && e.has(Type.UP)) {
			hideSecondary();
			return true;
		} else {
			if (e.has(Type.MOVE) || e.has(Type.DRAG)) {
				if (!projectorOpen && !projectorClosing && !openCloseAnimating) {
					WalkTransition prev = pointerTransition;
					pointerTransition = null;
					WalkPosition position = ScreenUtils.getWalk("*diplomacy", world());
					for (WalkTransition wt : position.transitions) {
						if (wt.area.contains(e.x - base.x, e.y - base.y)) {
							pointerTransition = wt;
							break;
						}
					}
					boolean spl = showPanelLabel;
					showPanelLabel = e.within(base.x, base.y, base.width, 350)
							&& pointerTransition == null;

					if (prev != pointerTransition || spl != showPanelLabel) {
						askRepaint();
					}
				} else {
					showPanelLabel = false;
					pointerTransition = null;
				}
				// show close label
				boolean b0 = showCloseLabel;
				
				showCloseLabel = projectorOpen && !projectorClosing 
						&& !openCloseAnimating 
						&& !isInsidePanel(e)
						&& e.within(base.x, base.y, base.width, base.height);
				if (b0 != showCloseLabel) {
					askRepaint();
				}
				
				return super.mouse(e);
			} else
			if (e.has(Type.DOWN)) {
				if (!projectorOpen && !projectorClosing && !openCloseAnimating) {
					WalkPosition position = ScreenUtils.getWalk("*diplomacy", world());
					for (WalkTransition wt : position.transitions) {
						if (wt.area.contains(e.x - base.x, e.y - base.y)) {
							ScreenUtils.doTransition(position, wt, commons);
							return false;
						}
					}
					if (e.within(base.x, base.y, base.width, 350)) {
						showPanelLabel = false;
						showProjector();
						return true;
					}
				} else
				if (projectorOpen && !projectorClosing && !openCloseAnimating 
						&& !isInsidePanel(e)) {
					showCloseLabel = false;
					hideProjector();
					return true;
				}

				
				return super.mouse(e);
			}
			return super.mouse(e);
		}
	}
	/**
	 * Check if the mouse is inside the panel body.
	 * @param e the mouse
	 * @return true if inside
	 */
	private boolean isInsidePanel(UIMouse e) {
		return e.within(stanceMatrix.x, stanceMatrix.y, stanceMatrix.width, stanceMatrix.height);
	}
	@Override
	public void draw(Graphics2D g2) {
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);
		g2.drawImage(commons.diplomacy().base, base.x, base.y, null);

		
		projectorLock.lock();
		try {
			if (projectorFront != null) {
				g2.drawImage(projectorFront, projectorRect.x, projectorRect.y, null);
			}
		} finally {
			projectorLock.unlock();
		}

		if (darkeningIndex > 0) {
			float alpha = darkeningAlpha * darkeningIndex / darkeningMax;
			g2.setColor(new Color(0f, 0f, 0f, alpha));
			g2.fill(base);
		}
		
		if (headAnimation != null && headAnimation.images.size() > 0 && headAnimation.active) {
			g2.drawImage(headAnimation.get(), base.x, base.y, null);
		}

		
		if (pointerTransition != null) {
			ScreenUtils.drawTransitionLabel(g2, pointerTransition, base, commons);
		}
		if (showPanelLabel) {
			String s = get("diplomacy.show_panel");
			int tw = commons.text().getTextWidth(14, s);
			
			int dy = 150;
			
			centerLabel(g2, s, tw, dy);
		}
		if (showCloseLabel) {
			String s = get("diplomacy.close_panel");
			int tw = commons.text().getTextWidth(14, s);
			
			int dy = 300;
			
			centerLabel(g2, s, tw, dy);
		}

		super.draw(g2);
	}

	/**
	 * Center a label on the screen.
	 * @param g2 the graphics context
	 * @param s the string to display
	 * @param tw the text width
	 * @param dy the y offset 
	 */
	void centerLabel(Graphics2D g2, String s, int tw, int dy) {
		g2.setColor(new Color(0, 0, 0, 255 * 85 / 100));

		int ax = base.x + (base.width - tw) / 2;
		int ay = base.y + dy;
		
		g2.fillRect(ax - 5, ay - 5, tw + 10, 14 + 10);
		
		commons.text().paintTo(g2, ax, ay, 14, TextRenderer.YELLOW, s);
	}
	@Override
	public Screens screen() {
		return Screens.DIPLOMACY;
	}
	@Override
	public void onEndGame() {
		// TODO Auto-generated method stub
		
	}
	/** An option item to display. */
	class OptionItem {
		/** The display label. */
		public String label;
		/** Is enabled? */
		public boolean enabled = true;
		/** Mouse over? */
		public boolean hover;
		/** Selected? */
		public boolean selected;
		/** The associated user object. */
		public Object userObject;
		/**
		 * Create a copy of this object.
		 * @return the copy
		 */
		public OptionItem copy() {
			OptionItem result = new OptionItem();
			result.label = label;
			result.hover = hover;
			result.enabled = enabled;
			result.selected = selected;
			return result;
		}
	}
	/**
	 * Generic list for options. 
	 * @author akarnokd, 2012.03.17.
	 */
	public class OptionList extends UIContainer {
		/** The text size. */
		public int textsize = 14;
		/** The distance after the text. */
		public int after = 3;
		/** The distance before the text. */
		public int before = 0;
		/** The list of items. */
		public final List<OptionItem> items = U.newArrayList();
		/** The action to invoke when a menu item is selected. */
		public Action1<Integer> onSelect;
		/** Called when the given item is highlight changes. */
		public Action1<Integer> onHighlight;
		/** If mouse pressed. */
		boolean mouseDown;
		/** Fit the control's width to accomodate all labels. */
		public void fit() {
			int w = 0;
			for (OptionItem oi : items) {
				w = Math.max(w, commons.text().getTextWidth(textsize, oi.label));
			}
			this.width = w + 10;
			this.height = 10 + items.size() * (textsize + before + after);
			if (!items.isEmpty()) {
				this.height -= after;
			}
		}
		@Override
		public void draw(Graphics2D g2) {
			int dy = 5;
			for (OptionItem oi : items) {
				dy += before;
				int color = TextRenderer.GREEN;
				if (!oi.enabled) {
					color = TextRenderer.GRAY;
				} else
				if (oi.selected) {
					color = TextRenderer.ORANGE;
				} else
				if (oi.hover) {
					color = TextRenderer.WHITE;
				}
				
				commons.text().paintTo(g2, 5, dy, textsize, color, oi.label);
				
				dy += after + textsize;
			}
		}
		@Override
		public boolean mouse(UIMouse e) {
			int idx = (e.y - 5) / (textsize + before + after);
			if (e.has(Type.MOVE) || e.has(Type.DRAG)) {
				hover(idx, e.has(Type.DRAG) || mouseDown);
				return true;
			} else
			if (e.has(Type.DOWN)) {
				hover(idx, true);
				mouseDown = true;
				return true;
			} else
			if (e.has(Type.UP)) {
				mouseDown = false;
				if (idx >= 0 && idx < items.size()) {
					OptionItem oi = items.get(idx);
					if (oi.enabled && oi.selected && onSelect != null) {
						onSelect.invoke(idx);
						return true;
					}
				}
			} else
			if (e.has(Type.LEAVE)) {
				hover(-1, false);
				mouseDown = false;
			}
			
			return super.mouse(e);
		}
		/**
		 * Hover or select a given indexth item.
		 * @param idx the index
		 * @param select should the item be selected
		 */
		public void hover(int idx, boolean select) {
			int i = 0;
			for (OptionItem oi : items) {
				
				boolean oldHover = oi.hover;
				
				oi.hover = oi.enabled & i == idx;
				oi.selected = oi.hover & select;
				
				if (oldHover != oi.hover && onHighlight != null) {
					onHighlight.invoke(i);
				}
				
				i++;
			}
		}
	}
	/** Play message panel closing. */
	void playProjectorOpen() {
		openCloseAnimating = true;
		VideoAudio va = new VideoAudio();
		va.audio = "ui/comm_deploy";
		va.video = "diplomacy/diplomacy_projector_extend";
		projectorAnim = new MediaPlayer(commons, va, new SwappableRenderer() {
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
					askRepaint();
				}
			}
		});
		projectorAnim.onComplete = new Action0() {
			@Override
			public void invoke() {
				projectorOpen = true;
				openCloseAnimating = false;
				if (onProjectorComplete != null) {
					onProjectorComplete.invoke();
					onProjectorComplete = null;
				}
				askRepaint();
			}
		};
		projectorAnim.start();
	}
	/** Play message panel closing. */
	void playProjectorClose() {
		openCloseAnimating = true;
		projectorClosing = true;
		VideoAudio va = new VideoAudio();
		va.audio = "ui/comm_deploy";
		va.video = "diplomacy/diplomacy_projector_retract";
		projectorAnim = new MediaPlayer(commons, va, new SwappableRenderer() {
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
					askRepaint();
				}
			}
		});
		projectorAnim.onComplete = new Action0() {
			@Override
			public void invoke() {
				projectorOpen = false;
				openCloseAnimating = false;
				projectorClosing = false;
				if (onProjectorComplete != null) {
					onProjectorComplete.invoke();
					onProjectorComplete = null;
				}
				commons.control().moveMouse();
				askRepaint();
			}
		};
		projectorAnim.start();
	}
	/**
	 * Change the race display values.
	 */
	void updateRaces() {
		races.items.clear();
		stances.items.clear();
		
		for (Map.Entry<Player, Integer> pi : player().knownPlayers.entrySet()) {
			Player p2 = pi.getKey();
			int rel = pi.getValue();
			if (!p2.noDiplomacy) {
				OptionItem oi1 = new OptionItem();
				oi1.label = " " + p2.shortName;
				oi1.userObject = p2;
				races.items.add(oi1);
				
				OptionItem oi2 = new OptionItem();
				oi2.label = Integer.toString(rel);
				stances.items.add(oi2);
			}
		}
		
		OptionItem oirel = new OptionItem();
		oirel.label = get("diplomacy.relations");
		races.items.add(oirel);
		
		races.fit();
		stances.fit();
		
		if (races.visible()) {
			commons.control().moveMouse();
			askRepaint();
		}
	}
	/**
	 * Action when a race is selected.
	 * @param index the index
	 */
	void onSelectRace(int index) {
		headAnimation = null;
		races.visible(false);
		stances.visible(false);
		if (index < races.items.size() - 1) {
			
			final Player p2 = (Player)races.items.get(index).userObject;
			stanceMatrix.visible(false);

			final AtomicInteger wip = new AtomicInteger(2);
			onProjectorComplete = new Action0() {
				@Override
				public void invoke() {
					if (wip.decrementAndGet() == 0) {
						doDarken();
					}
				}
			};
			
			playProjectorClose();

			commons.pool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						// load head
						final HeadAnimation ha = loadHeadAnimation(p2.diplomacyHead);
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								headAnimation = ha;
								if (wip.decrementAndGet() == 0) {
									doDarken();
								}
							}
						});
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			});
			
		} else {
			stanceMatrix.visible(true);
		}
		// TODO implement rest
	}
	/** Hide the projector. */
	void hideProjector() {
		races.visible(false);
		stances.visible(false);
		stanceMatrix.visible(false);
		playProjectorClose();
	}
	
	/**
	 * Show the projector.
	 */
	void showProjector() {
		races.visible(false);
		stances.visible(false);
		stanceMatrix.visible(false);
		onProjectorComplete = new Action0() {
			@Override
			public void invoke() {
				races.visible(true);
				stances.visible(true);
				commons.control().moveMouse();
			}
		};
		playProjectorOpen();
		commons.control().moveMouse();
	}
	/** Hide the stance matrix. */
	void hideStanceMatrix() {
		races.visible(true);
		stances.visible(true);
		stanceMatrix.visible(false);
	}
	/**
	 * Event to highlight a row.
	 * @param idx the row index.
	 * @param value the highlight value
	 */
	void onRaceHighlight(int idx, boolean value) {
		if (races.items.size() > idx) {
			races.items.get(idx).hover = value;
		}
		if (stances.items.size() > idx) {
			stances.items.get(idx).hover = value;
		}
	}
	/**
	 * Displays the matrix of relations. 
	 * @author akarnokd, 2012.03.17.
	 */
	class StanceMatrix extends UIComponent {
		/** If mouse pressed. */
		boolean mouseDown;
		@Override
		public void draw(Graphics2D g2) {
			
//			g2.setColor(Color.GRAY);
//			g2.fillRect(0, 0, width, height);
			
			int textSize = 7;
			int cellSize = 18;
			
			// filter diplomatic races
			List<Player> players = U.newArrayList();
			players.add(player());
			for (Player p : player().knownPlayers.keySet()) {
				if (!p.noDiplomacy) {
					players.add(p);
				}
			}
			
			// paint stance matrix participants
			int ox = 0;
			int oy = 0;
			int dw = players.size() * cellSize;
			int dh = players.size() * cellSize;
			for (int i = 1; i <= players.size(); i++) {
				String n = Integer.toString(i);
				int tw = commons.text().getTextWidth(textSize, n);
				
				int dx = ox + (i - 1) * cellSize + (cellSize - tw) / 2;
				
				Player p = players.get(i - 1);
				
				commons.text().paintTo(g2, dx, oy, textSize, p.color, n);
				
				
				int ty = oy + (i - 1) * cellSize + (cellSize - textSize) / 2 + textSize + 3;
				
				commons.text().paintTo(g2, ox + dw + 5, ty, textSize, p.color, n + " - " + p.shortName);
			}
			g2.setColor(new Color(0xFF087B73));
			
			for (int i = 0; i <= players.size(); i++) {
				g2.drawLine(ox, oy + i * cellSize + textSize + 3, ox + dw, oy + i * cellSize + textSize + 3);
				g2.drawLine(ox + i * cellSize, oy + textSize + 3, ox + i * cellSize, oy + dh + textSize + 3);
			}
			
			// draw stance valus
			
			int stanceHeight = 7;
			for (int i = 0; i < players.size(); i++) {
				Player row = players.get(i);
				for (int j = 0; j < players.size(); j++) {
					Player col = players.get(j);
					
					String stance = "-";
					int st = -1;
					if (i != j && row.knows(col)) {
						st = row.getStance(col);
						stance = Integer.toString(st);
					}
					int stanceColor = TextRenderer.GREEN;
					if (st >= 0) {
						if (st < 30) {
							stanceColor = TextRenderer.RED;
						} else
						if (st < 40) {
							stanceColor = TextRenderer.YELLOW;
						} else
						if (st > 80) {
							stanceColor = TextRenderer.LIGHT_BLUE;
						} else
						if (st > 60) {
							stanceColor = TextRenderer.LIGHT_GREEN;
						}
					}
					
					int sw = commons.text().getTextWidth(stanceHeight, stance);
					commons.text().paintTo(g2, 
							ox + j * cellSize + (cellSize - sw) / 2,
							oy + i * cellSize + (cellSize - stanceHeight) / 2 + textSize + 3,
							stanceHeight,
							stanceColor,
							stance
					);
				}				
			}

			commons.text().paintTo(g2, ox + 5, height - 11, 7, TextRenderer.YELLOW, get("diplomacy.click_to_exit"));
			
		}
		@Override
		public boolean mouse(UIMouse e) {
			if (e.has(Type.DOWN)) {
				mouseDown = true;
			} else
			if (e.has(Type.UP) && mouseDown) {
				mouseDown = false;
				hideStanceMatrix();
				return true;
			} else
			if (e.has(Type.LEAVE)) {
				mouseDown = false;
			}
			return false;
		}
	}
	/**
	 * A head animation record. 
	 * @author akarnokd, 2012.03.17.
	 */
	public static class HeadAnimation {
		/** The list of image frames. */
		public List<byte[]> images = U.newArrayList();
		/** The palettes. */
		public List<int[]> palettes = U.newArrayList();
		/** The animation frames per second. */
		public double fps;
		/** The total number of frames. */
		public int frames;
		/** The current frame. */
		int index;
		/** The image width. */
		int w;
		/** The image height. */
		int h;
		/** The current image memory. */
		int[] currentImage;
		/** The current image at the start of the looping. */
		int[] keyframeLoopStart;
		/** The cached image. */
		BufferedImage cache;
		/** The current cached index. */
		int cacheIndex;
		/** The start loop index. */
		public int startLoop;
		/** The end loop index. */
		public int endLoop;
		/** Are we in loop mode? */
		public boolean loop;
		/** Animation is active? */
		public boolean active;
		/**
		 * @return the current image for the current frame.
		 */
		public BufferedImage get() {
			if (cache != null && cacheIndex == index) {
				return cache;
			}
			if (currentImage == null) {
				currentImage = new int[w * h];
			}
			byte[] data = images.get(index);
			int[] palette = palettes.get(index);
			for (int i = 0; i < data.length; i++) {
				int c0 = palette[data[i] & 0xFF];
				if (c0 != 0) {
					currentImage[i] = c0;
				}
			}
			if (index == startLoop && keyframeLoopStart == null) {
				keyframeLoopStart = currentImage.clone();
			}
			cache = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			cache.setRGB(0, 0, w, h, currentImage, 0, w);
			cacheIndex = index;
			return cache;
			
		}
		/**
		 * Move to the next frame.
		 * @return true if wrapped over
		 */
		public boolean moveNext() {
			index++;
			if (loop) {
				if (index >= endLoop) {
					index = startLoop;
					System.arraycopy(keyframeLoopStart, 0, currentImage, 0, keyframeLoopStart.length);
					return true;
				}
			} else
			if (index >= images.size()) {
				index = 0;
				return true;
			}
			return false;
		}
	}
	/**
	 * Load a video resource into list of images.
	 * @param resource the resource name
	 * @return the animation record with the frames
	 */
	HeadAnimation loadHeadAnimation(String resource) {
		HeadAnimation ha = new HeadAnimation();
		if (resource == null) {
			return ha;
		}
		try {
			DataInputStream in = new DataInputStream(
					new BufferedInputStream(
							new GZIPInputStream(rl.get(resource, ResourceType.VIDEO).open(), 1024 * 1024), 1024 * 1024));
			try {
				ha.w = Integer.reverseBytes(in.readInt());
				ha.h = Integer.reverseBytes(in.readInt());
				ha.frames = Integer.reverseBytes(in.readInt());
				ha.fps = Integer.reverseBytes(in.readInt()) / 1000.0;
				int[] palette = new int[256];
				int frameCount = 0;
				while (frameCount < ha.frames) {
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
						byte[] bytebuffer = new byte[ha.w * ha.h];
						in.readFully(bytebuffer);
						
						ha.images.add(bytebuffer);
						ha.palettes.add(palette.clone());
						
						
		       			frameCount++;
					}
				}
			} finally {
				try { in.close(); } catch (IOException ex) {  }
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return ha;
	}
	/**
	 * Start the head animation.
	 */
	void doStartHead() {
		if (headAnimation.frames > 0) {
			
			headAnimation.active = true;
			headAnimation.loop = true;
			headAnimation.startLoop = 35;
			headAnimation.endLoop = headAnimation.frames - 35;
			
			int delay = (((int)(1000 / headAnimation.fps)) / 25) * 25; // round frames down
			headAnimationClose = commons.register(delay, new Action0() {
				@Override
				public void invoke() {
					boolean wr = headAnimation.moveNext();
					if (wr && !headAnimation.loop) {
						headAnimation.active = false;
						close0(headAnimationClose);
						doUndarken();
					}
					askRepaint();
				}
			});
		}
	}
	/**
	 * Animate the darkening then start the animation.
	 */
	void doDarken() {
		int delay = (((int)(1000 / headAnimation.fps)) / 25) * 25; // round frames down
		darkeningIndex = 0;
		darkening = commons.register(delay, new Action0() {
			@Override
			public void invoke() {
				darkeningIndex++;
				if (darkeningIndex > darkeningMax) {
					doStartHead();
					darkeningIndex = 0;
					close0(darkening);
				}
				askRepaint();
			}
		});
	}
	/**
	 * Animate undarkening.
	 */
	void doUndarken() {
		int delay = (((int)(1000 / headAnimation.fps)) / 25) * 25; // round frames down
		darkeningIndex = darkeningMax;
		darkening = commons.register(delay, new Action0() {
			@Override
			public void invoke() {
				darkeningIndex++;
				if (darkeningIndex == 0) {
					close0(darkening);
				}
				askRepaint();
			}
		});
	}
}
