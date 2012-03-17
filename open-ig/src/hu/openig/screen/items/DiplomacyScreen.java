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
import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;



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
		// TODO Auto-generated method stub
		if (projectorAnim != null) {
			projectorAnim.stop();
			projectorAnim = null;
			projectorFront = null;
			projectorBack = null;
			onProjectorComplete = null;
		}
		if (raceUpdater != null) {
			U.close(raceUpdater);
			raceUpdater = null;
		}
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
					showPanelLabel = e.within(base.x, base.y, base.width, 300)
							&& pointerTransition == null;

					if (prev != pointerTransition || spl != showPanelLabel) {
						askRepaint();
					}
				} else {
					showPanelLabel = false;
					pointerTransition = null;
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
					if (e.within(base.x, base.y, base.width, 300)) {
						showProjector();
						return true;
					}
				} else
				if (projectorOpen && !projectorClosing && !openCloseAnimating && !e.within(base.x + stanceMatrix.x, base.y + stanceMatrix.y, stanceMatrix.width, stanceMatrix.height)) {
					hideProjector();
					return true;
				}

				
				return super.mouse(e);
			}
			return super.mouse(e);
		}
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

		
		if (pointerTransition != null) {
			ScreenUtils.drawTransitionLabel(g2, pointerTransition, base, commons);
		}
		if (showPanelLabel) {
			String s = get("diplomacy.show_panel");
			int tw = commons.text().getTextWidth(14, s);
			
			g2.setColor(new Color(0, 0, 0, 255 * 85 / 100));

			int ax = base.x + (base.width - tw) / 2;
			int ay = base.y + 150;
			
			g2.fillRect(ax - 5, ay - 5, tw + 10, 14 + 10);
			
			commons.text().paintTo(g2, ax, ay, 14, TextRenderer.YELLOW, s);
		}

		super.draw(g2);
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
		races.visible(false);
		stances.visible(false);
		if (index < races.items.size() - 1) {
			stanceMatrix.visible(false);
			playProjectorClose();
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
}
