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
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;
import hu.openig.utils.U;

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
	@Override
	public void onInitialize() {
		base.setBounds(0, 0, 
				commons.diplomacy().base.getWidth(), commons.diplomacy().base.getHeight());
		
		races = new OptionList();
		stances = new OptionList();
		stances.before = 3;
		stances.after = 4 + 3;
		stances.textsize = 7;
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
		
		addThis();
	}

	@Override
	public void onEnter(Screens mode) {
		// TODO Auto-generated method stub
		showProjector();
		
		raceUpdater = commons.register(1000, new Action0() {
			@Override
			public void invoke() {
				updateRaces();
			}
		});
		updateRaces();
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
		
		races.location(base.x + 155, base.y + 14);
		stances.location(base.x + 450, base.y + 20);
	}
	
	@Override
	public boolean mouse(UIMouse e) {
		if (!base.contains(e.x, e.y) && e.has(Type.UP)) {
			hideSecondary();
			return true;
		} else {
			if (e.has(Type.MOVE) || e.has(Type.DRAG)) {
				WalkTransition prev = pointerTransition;
				pointerTransition = null;
				WalkPosition position = ScreenUtils.getWalk("*diplomacy", world());
				for (WalkTransition wt : position.transitions) {
					if (wt.area.contains(e.x - base.x, e.y - base.y)) {
						pointerTransition = wt;
						break;
					}
				}
				if (prev != pointerTransition) {
					askRepaint();
				}
				return super.mouse(e);
			} else
			if (e.has(Type.DOWN)) {
				WalkPosition position = ScreenUtils.getWalk("*diplomacy", world());
				for (WalkTransition wt : position.transitions) {
					if (wt.area.contains(e.x - base.x, e.y - base.y)) {
						ScreenUtils.doTransition(position, wt, commons);
						return false;
					}
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
				int color = TextRenderer.YELLOW;
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
				hover(idx, e.has(Type.DRAG));
				return true;
			} else
			if (e.has(Type.DOWN)) {
				hover(idx, true);
				return true;
			} else
			if (e.has(Type.UP)) {
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
				oi1.label = p2.shortName;
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
		
		commons.control().moveMouse();
		askRepaint();
	}
	/**
	 * Action when a race is selected.
	 * @param index the index
	 */
	void onSelectRace(int index) {
		races.visible(false);
		stances.visible(false);
		playProjectorClose();

		// TODO implement rest
	}
	/**
	 * Show the projector.
	 */
	void showProjector() {
		races.visible(false);
		stances.visible(false);
		onProjectorComplete = new Action0() {
			@Override
			public void invoke() {
				races.visible(true);
				stances.visible(true);
				commons.control().moveMouse();
			}
		};
		playProjectorOpen();
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
}
