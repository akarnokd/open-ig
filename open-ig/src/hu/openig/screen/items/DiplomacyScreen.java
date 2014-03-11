/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.core.Action1;
import hu.openig.core.Pair;
import hu.openig.core.SwappableRenderer;
import hu.openig.model.ApproachType;
import hu.openig.model.CallType;
import hu.openig.model.Diplomacy;
import hu.openig.model.Diplomacy.Approach;
import hu.openig.model.Diplomacy.Call;
import hu.openig.model.Diplomacy.Negotiate;
import hu.openig.model.DiplomaticOffer;
import hu.openig.model.DiplomaticRelation;
import hu.openig.model.Fleet;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.InventoryItem;
import hu.openig.model.ModelUtils;
import hu.openig.model.NegotiateType;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResponseMode;
import hu.openig.model.Screens;
import hu.openig.model.SoundType;
import hu.openig.model.VideoAudio;
import hu.openig.model.WalkPosition;
import hu.openig.model.WalkTransition;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.render.TextRenderer.TextSegment;
import hu.openig.screen.MediaPlayer;
import hu.openig.screen.RawAnimation;
import hu.openig.screen.ScreenBase;
import hu.openig.screen.panels.OptionList;
import hu.openig.screen.panels.OptionList.OptionItem;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIGenericButton;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Button;
import hu.openig.ui.UIMouse.Type;
import hu.openig.utils.Exceptions;
import hu.openig.utils.Parallels;
import hu.openig.utils.U;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	/** The listing of approaches. */
	OptionList approachList;
	/** The money list. */
	OptionList moneyList;
	/** List of enemies. */
	OptionList enemies;
	/** The stance matrix. */
	StanceMatrix stanceMatrix;
	/** Show the panel label? */
	boolean showPanelLabel;
	/** Show the close label? */
	boolean showCloseLabel;
	/** The head animation. */
	RawAnimation headAnimation;
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
	/** Are we in call mode? */
	boolean inCall;
	/** The label that displays the selected negotiation for the approach. */
	UILabel negotiationTitle;
	/** The offer text. */
	UILabel offerText;
	/** The response text. */
	UILabel responseText;
	/** Click/SPACE to continue. */
	UILabel continueLabel;
	/** Indicate that the message exchange is in progress. 0 = no message, 1 send, 2 response */
	int messagePhase;
	/** The negotiation memory. */
	final Set<Negotiate> mentioned = new HashSet<>();
	/** The relation when the conversation started. */
	double initialRelation;
	/** Quit talking. */
	boolean quitTalking;
	/** The change to introduce after clicking the first message phase. */
	double change;
	/** Accept the current offer. */
	UIGenericButton acceptOffer;
	/** Decline the current offer. */
	UIGenericButton declineOffer;
	/** The last projector image. */
	BufferedImage projectorLast;
	@Override
	public void onInitialize() {
		base.setBounds(0, 0, 
				commons.diplomacy().base.getWidth(), commons.diplomacy().base.getHeight());
		
		races = new OptionList(commons.text());
		stances = new OptionList(commons.text());
		stances.before = 2;
		stances.after = 2 + 3;
		stances.textsize = 10;
		options = new OptionList(commons.text());
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
		
		options.onSelect = new Action1<Integer>() {
			@Override
			public void invoke(Integer value) {
				doOption(value);
			}
		};
		
		moneyList = new OptionList(commons.text());
		moneyList.onSelect = new Action1<Integer>() {
			@Override
			public void invoke(Integer value) {
				doMoney(value);
			}
		};
		
		
		approachList = new OptionList(commons.text());
		approachList.onSelect = new Action1<Integer>() {
			@Override
			public void invoke(Integer value) {
				doApproach(value);
			}
		};
		
		negotiationTitle = new UILabel("", 14, commons.text());
		negotiationTitle.color(TextRenderer.YELLOW);
		
		enemies = new OptionList(commons.text());
		enemies.onSelect = new Action1<Integer>() {
			@Override
			public void invoke(Integer value) {
				doEnemies(value);
			}
		};
		
		offerText = new UILabel("", 14, commons.text());
		offerText.wrap(true).visible(false);
		
		responseText = new UILabel("", 14, commons.text());
		responseText.wrap(true).visible(false);
		continueLabel = new UILabel(get("diplomacy.click_continue"), 7, commons.text());
		continueLabel.color(TextRenderer.YELLOW).visible(false);
		
		acceptOffer = new UIGenericButton(get("diplomacy.accept_offer"), fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		declineOffer = new UIGenericButton(get("diplomacy.decline_offer"), fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		declineOffer.onClick = new Action0() {
			@Override
			public void invoke() {
				returnToOptions();
			}
		};
		
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
		options.visible(false);
		moneyList.visible(false);
		enemies.visible(false);
		approachList.visible(false);
		offerText.visible(false);
		responseText.visible(false);
		continueLabel.visible(false);
		negotiationTitle.visible(false);
		acceptOffer.visible(false);
		acceptOffer.onClick = null;
		declineOffer.visible(false);
		mentioned.clear();
		
		messagePhase = 0;
		change = 0;
		
		darkeningIndex = 0;
		inCall = false;
	}

	@Override
	public void onLeave() {
		cleanup();
	}

	/**
	 * Cleanup allocated resources.
	 */
	void cleanup() {
		openCloseAnimating = false;
		projectorOpen = false;
		projectorClosing = false;
		showCloseLabel = false;
		showPanelLabel = false;
		
		if (projectorAnim != null) {
			projectorAnim.stop();
			projectorAnim = null;
			clearProjectorSurface();
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
		options.items.clear();
		races.items.clear();
		stances.items.clear();
	}
	/** Clear the projector surface. */
	void clearProjectorSurface() {
		projectorLock.lock();
		try {
			projectorLast = null;
			projectorFront = null;
			projectorBack = null;
		} finally {
			projectorLock.unlock();
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
		scaleResize(base, margin());
		
		projectorRect.setBounds(base.x + (base.width - 524) / 2 - 10, base.y, 524, 258);
		
		races.location(base.x + 155, base.y + 10);
		stances.location(base.x + 445, base.y + 10);
		
		stanceMatrix.bounds(base.x + 157, base.y + 10, 320, 239);
		
		options.location(base.x + 10, base.y + base.height - options.height - 10);
		
		approachList.location(base.x + 10, base.y + base.height - approachList.height - 10);
		moneyList.location(base.x + 10, base.y + base.height - moneyList.height - 10);
		enemies.location(base.x + 10, base.y + base.height - enemies.height - 10);

		continueLabel.location(base.x + 10, base.y + base.height - 30);
		acceptOffer.location(continueLabel.x, continueLabel.y);
		declineOffer.location(acceptOffer.x + acceptOffer.width + 20, acceptOffer.y);
		
		offerText.location(base.x + 10, continueLabel.y - 20 - offerText.height);
		responseText.location(base.x + 10, continueLabel.y - 20 - responseText.height);

		if (approachList.visible()) {
			negotiationTitle.location(base.x + 10, approachList.y - 20);
		} else
		if (moneyList.visible()) {
			negotiationTitle.location(base.x + 10, moneyList.y - 20);
		} else
		if (enemies.visible()) {
			negotiationTitle.location(base.x + 10, enemies.y - 20);
		} else
		if (offerText.visible()) {
			negotiationTitle.location(base.x + 10, offerText.y - 20);
		} else
		if (responseText.visible()) {
			negotiationTitle.location(base.x + 10, responseText.y - 20);
		}
		
	}
	
	@Override
	public boolean mouse(UIMouse e) {
		scaleMouse(e, base, margin());
		if (!base.contains(e.x, e.y) && e.has(Type.UP)) {
			hideSecondary();
			return true;
		} else 
		if (!inCall) {
			if (e.has(Type.MOVE) || e.has(Type.DRAG)) {
				if (!projectorOpen && !projectorClosing && !openCloseAnimating) {
					WalkTransition prev = pointerTransition;
					pointerTransition = null;
					WalkPosition position = ScreenUtils.getWalk("*diplomacy", world());
					if (position != null) {
						for (WalkTransition wt : position.transitions) {
							if (wt.area.contains(e.x - base.x, e.y - base.y)) {
								pointerTransition = wt;
								break;
							}
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
				
			} else
			if (e.has(Type.DOWN)) {
				if (!projectorOpen && !projectorClosing && !openCloseAnimating) {
					WalkPosition position = ScreenUtils.getWalk("*diplomacy", world());
					if (position != null) {
						for (WalkTransition wt : position.transitions) {
							if (wt.area.contains(e.x - base.x, e.y - base.y)) {
								ScreenUtils.doTransition(position, wt, commons, e.has(Button.RIGHT));
								return false;
							}
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
			}
		} else
		if (e.has(Type.DOWN) && messagePhase > 0 && messagePhase < 4) {
			handleMessagePhases();
			return true;
		}
		return super.mouse(e);
	}
	/** Handle the message phase-related displays. */
	void handleMessagePhases() {
		if (messagePhase == 1) {
			offerText.visible(false);
			responseText.visible(true);
			onResize();
			messagePhase = 2;
			
			DiplomaticRelation dr = world().getRelation(player(), other);
			dr.value = Math.max(0, Math.min(100, dr.value + change));
		} else
		if (messagePhase == 2) {
			returnToOptions();
		}
	}
	/**
	 * Return to options panel.
	 */
	void returnToOptions() {
		responseText.visible(false);
		continueLabel.visible(false);
		negotiationTitle.visible(false);
		acceptOffer.visible(false);
		declineOffer.visible(false);

		DiplomaticRelation dr = world().getRelation(player(), other);
		if (dr.wontTalk() || quitTalking) {
			headAnimation.loop = false;
		} else {
			options.visible(true);
			updateOptions();
		}
		
		messagePhase = 0;
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
		
		AffineTransform savea = scaleDraw(g2, base, margin());
		
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);
		g2.drawImage(commons.diplomacy().base, base.x, base.y, null);

		
		projectorLock.lock();
		try {
			if (projectorLast != null) {
				g2.drawImage(projectorLast, projectorRect.x, projectorRect.y, null);
			} else
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

		if (!openCloseAnimating && !inCall) {
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
		}
		if (inCall && !openCloseAnimating) {
			commons.text().paintTo(g2, base.x + 5, base.y + 5, 14, other.color, other.name);
			double r = world().getRelation(player(), other).value;
			double delta = r - initialRelation;
			int c;
			String fill;
			if (delta < 0) {
				c = TextRenderer.RED;
				fill = "";
			} else {
				c = TextRenderer.GREEN;
				fill = "+";
			}
			int stanceColor = TextRenderer.GREEN;
			if (r >= 0) {
				if (r < 30) {
					stanceColor = TextRenderer.RED;
				} else
				if (r < 40) {
					stanceColor = TextRenderer.YELLOW;
				} else
				if (r > 80) {
					stanceColor = TextRenderer.LIGHT_BLUE;
				} else
				if (r > 60) {
					stanceColor = TextRenderer.LIGHT_GREEN;
				}
			}
			List<TextSegment> tss = new ArrayList<>();
			tss.add(new TextSegment(String.format("%.1f", r), stanceColor));
			tss.add(new TextSegment(" (", TextRenderer.GREEN));
			tss.add(new TextSegment(fill, c));
			tss.add(new TextSegment(String.format("%.1f", delta), c));
			tss.add(new TextSegment(")", TextRenderer.GREEN));
			commons.text().paintTo(g2, base.x + 5, base.y + 24, 10, tss);

			darkenUnder(g2, options);
			darkenUnder(g2, moneyList);
			darkenUnder(g2, approachList);
			darkenUnder(g2, offerText);
			darkenUnder(g2, responseText);
			darkenUnder(g2, continueLabel);
		}
		super.draw(g2);
		
		g2.setTransform(savea);
	}
	/**
	 * Darken the area under the given component if it is visible.
	 * @param g2 the graphics context.
	 * @param c the component
	 */
	void darkenUnder(Graphics2D g2, UIComponent c) {
		if (c.visible()) {
			g2.setColor(new Color(0, 0, 0, 160));
			g2.fillRect(c.x - 5, c.y - 5, c.width + 10, c.height + 10);
		}
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
		cleanup();
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
				projectorLock.lock();
				try {
					projectorLast = projectorFront;
					projectorFront = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
					projectorFront.setAccelerationPriority(0);
					projectorBack = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
					projectorBack.setAccelerationPriority(0);
				} finally {
					projectorLock.unlock();
				}
			}
			@Override
			public void swap() {
				projectorLock.lock();
				try {
					projectorLast = null;
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
				projectorLock.lock();
				try {
					projectorLast = projectorFront;
					projectorFront = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
					projectorFront.setAccelerationPriority(0);
					projectorBack = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
					projectorBack.setAccelerationPriority(0);
				} finally {
					projectorLock.unlock();
				}
			}
			@Override
			public void swap() {
				projectorLock.lock();
				try {
					projectorLast = null;
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

		long now = world().time.getTimeInMillis();
		
		for (Map.Entry<Player, DiplomaticRelation> pi : player().knownPlayers().entrySet()) {
			Player p2 = pi.getKey();
			DiplomaticRelation rel = pi.getValue();

			long last = rel.lastContact != null ? rel.lastContact.getTime() : 0;
			long limit = rel.wontTalk() ? 24L * 60 * 60 * 1000 : 7L * 24 * 60 * 60 * 1000; 
			
			if (!p2.noDiplomacy) {
				OptionItem oi1 = new OptionItem();
				boolean active = !p2.isDefeated();
				if (player().offers.containsKey(p2.id) && active) {
					oi1.label = "!" + p2.shortName;
				} else {
					oi1.label = " " + p2.shortName;
				}
				oi1.userObject = p2;
				
				oi1.enabled = rel.full && last < now - limit 
						&& active;

				if (oi1.enabled && rel.wontTalk()) {
					rel.wontTalk(false);
				}
				
				races.items.add(oi1);
				
				OptionItem oi2 = new OptionItem();
				oi2.label = Integer.toString((int)rel.value);
				oi2.enabled = oi1.enabled;
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
		world().env.pause();
		headAnimation = null;
		races.visible(false);
		stances.visible(false);
		
		if (index < races.items.size() - 1) {
			other = (Player)races.items.get(index).userObject;
			
			initialRelation = world().getRelation(player(), other).value;
			quitTalking = false;
			contactRaceAnim();
			
		} else {
			stanceMatrix.visible(true);
		}
	}

	/**
	 * Close the projector and display the race head. 
	 */
	protected void contactRaceAnim() {
		inCall = true;
		mentioned.clear();
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

		loadHeadAnimAsync(wip);
	}

	/**
	 * Load the head animation asynchronously.
	 * @param wip the wip port
	 */
	protected void loadHeadAnimAsync(final AtomicInteger wip) {
		commons.pool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					// load head
					final RawAnimation ha = RawAnimation.load(commons.rl, other.diplomacyHead);
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
					Exceptions.add(t);
				}
			}
		});
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
			List<Player> players = new ArrayList<>();
			players.add(player());
			for (Player p : player().knownPlayers().keySet()) {
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
	 * Start the head animation.
	 */
	void doStartHead() {
		DiplomaticRelation dr = world().getRelation(player(), other);
		dr.lastContact = world().time.getTime();
		
		DiplomaticOffer na = player().offers.get(other.id);
		if (na != null) {
			doIncomingMessage(na);
		} else {
			options.visible(true);
		}
		updateOptions();
		if (headAnimation.frames > 0) {
			
			headAnimation.active = true;
			headAnimation.loop = true;
			headAnimation.startLoop = 35;
			headAnimation.endLoop = headAnimation.frames - 35;
			
			effectSound(SoundType.HOLOGRAM_ON);
			
			int delay = (((int)(1000 / headAnimation.fps)) / 25) * 25; // round frames down
			headAnimationClose = commons.register(delay, new Action0() {
				@Override
				public void invoke() {
					boolean wr = headAnimation.moveNext();
					if (headAnimation.index() == headAnimation.endLoop + 1) {
						effectSound(SoundType.HOLOGRAM_OFF);
					}
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
					commons.control().moveMouse();
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
				darkeningIndex--;
				if (darkeningIndex == 0) {
					close0(darkening);
					inCall = false;
					commons.control().moveMouse();
					other = null;
				}
				askRepaint();
			}
		});
	}
	/**
	 * Update the list of options based on the current talking partner.
	 */
	void updateOptions() {
		DiplomaticRelation dr = world().getRelation(player(), other);
		Diplomacy de = world().diplomacy.get(other.id);
		options.items.clear();
		if (de != null) {
			for (Negotiate neg : de.negotiations) {
				OptionItem opt = new OptionItem();
				opt.label = get("diplomacy.type." + neg.type.toString());
				opt.userObject = neg;
				opt.enabled = !mentioned.contains(neg);
				
				if (neg.type == NegotiateType.TRADE && dr.tradeAgreement) {
					opt.enabled = false;
				}
				
				options.items.add(opt);
			}
		}
		OptionItem opt = new OptionItem();
		opt.label = get("diplomacy.done");
		options.items.add(opt);
		
		options.fit();
		options.y = base.y + base.height - options.height - 10;
		
		// update approaches
	}
	/**
	 * Activate talk option.
	 * @param index the index
	 */
	void doOption(int index) {
		options.visible(false);
		if (index == options.items.size() - 1) {
			
			// quit talking
			Diplomacy de = world().diplomacy.get(other.id);
			
			responseText.width = base.width - 20;
			responseText.text(get(de != null ? de.terminateLabel : "diplomacy.generic.endtalk"));
			responseText.height = responseText.getWrappedHeight();
			
			responseText.visible(true);
			continueLabel.visible(true);
			messagePhase = 2;
			change = 0;
			
			quitTalking = true;
			
			onResize();
			
		} else {
			OptionItem cancel = new OptionItem();
			cancel.label = get("diplomacy.cancel");

			Negotiate neg = (Negotiate)options.items.get(index).userObject;

			negotiationTitle.text(get("diplomacy.type." + neg.type), true).visible(true);

			approachList.items.clear();
			moneyList.items.clear();
			enemies.items.clear();
			
			if (neg.type == NegotiateType.MONEY) {
				int[] money = { 10000, 20000, 30000, 40000, 50000, 100000, 250000, 500000 };
				for (int m : money) {
					OptionItem item = new OptionItem();
					item.label = String.format("%,8d", m);
					item.userObject = Pair.of(neg, m);
					item.enabled = m <= player().money();
					moneyList.items.add(item);
				}
				moneyList.items.add(cancel);
				
				moneyList.fit();
				moneyList.visible(true);
				onResize();
			} else 
			if (neg.type == NegotiateType.ALLY) {
				negotiationTitle.text(get("diplomacy.type." + neg.type), true).visible(true);

				Set<Player> others = new LinkedHashSet<>(player().knownPlayers().keySet());
				others.remove(other);
				others.retainAll(other.knownPlayers().keySet());

				for (Player p : others) {
					if (!p.isDefeated()) {
						OptionItem item = new OptionItem();
						item.label = " " + p.name + " (" + player().getStance(p) + ", " + other.getStance(p) + ")";
						item.userObject = Pair.of(neg, p);
						enemies.items.add(item);
					}
					
				}
				enemies.items.add(cancel);
				
				enemies.fit();
				enemies.visible(true);
				onResize();
			} else {
				if (neg.approaches.size() > 1) {
					
					Set<ApproachType> at = new HashSet<>();
					for (Approach a : neg.approaches) {
						at.add(a.type);
					}
					for (ApproachType a : at) {
						OptionItem item = new OptionItem();
						item.label = " " + get("diplomacy." + a);
						item.userObject = Pair.of(neg, a);
						approachList.items.add(item);
					}
					approachList.items.add(cancel);

					approachList.fit();
					approachList.visible(true);
					onResize();
				} else {
					doGeneric(neg);
				}
			}
		}
	}
	@Override
	public boolean keyboard(KeyEvent e) {
		if (e.getKeyChar() == ' ' && messagePhase > 0 && messagePhase < 4) {
			handleMessagePhases();
			e.consume();
			return true;
		}
		return false;
	}
	/**
	 * Display the negotiation text results and update the relation table.
	 * @param n the negotiation instance.
	 * @param parameter the negotiation parameter
	 * @param at the approach type
	 * @param m the response type
	 */
	protected void displayResults(Negotiate n, Object parameter, 
			ApproachType at,
			ResponseMode m) {
		
		mentioned.add(n);
		
		Diplomacy.Response r0 = ModelUtils.random(n.responseFor(at, m));
		
		DiplomaticRelation dr = world().getRelation(player(), other);
		
		dr.wontTalk(r0.notalk);
		change = r0.change / 10d;
		
		String sOffer = format(ModelUtils.random(n.approachFor(at)).label, parameter);
		String sResponse = format(r0.label, parameter);
		setupTexts(sOffer, sResponse);

		offerText.visible(true);
		continueLabel.visible(true);
		messagePhase = 1;
		onResize();
	}
	/**
	 * The action when an approach is selected.
	 * @param index the index
	 */
	void doApproach(int index) {
		if (index == approachList.items.size() - 1) {
			options.visible(true);
			approachList.visible(false);
			negotiationTitle.visible(false);
		} else {
			@SuppressWarnings("unchecked")
			Pair<Negotiate, ApproachType> a = (Pair<Negotiate, ApproachType>)approachList.items.get(index).userObject;
			
			approachList.visible(false);
			
			ResponseMode m = other.ai.diplomacy(player(), 
					a.first.type, 
					a.second, 
					a.second);
			
			displayResults(a.first, "", a.second, m);

		}
	}
	/**
	 * Action when a money amount is selected.
	 * @param index the index
	 */
	void doMoney(int index) {
		if (index == moneyList.items.size() - 1) {
			options.visible(true);
			moneyList.visible(false);
			negotiationTitle.visible(false);
		} else {
			@SuppressWarnings("unchecked")
			Pair<Negotiate, Integer> a = (Pair<Negotiate, Integer>)moneyList.items.get(index).userObject;
			moneyList.visible(false);
			
			ApproachType at = ApproachType.NEUTRAL;
			
			ResponseMode m = other.ai.diplomacy(player(), 
					a.first.type, 
					at, 
					a.second);
			

			if (m == ResponseMode.YES && a.second >= player().money()) {
				player().addMoney(-a.second);
				player().statistics.moneySpent.value += a.second;
				
				other.addMoney(a.second);
				other.statistics.moneyIncome.value += a.second;
			} else {
				m = ResponseMode.NO;
			}

			displayResults(a.first, a.second, at, m);
			
		}
	}
	/**
	 * The action when an approach is selected.
	 * @param index the index
	 */
	void doEnemies(int index) {
		if (index == enemies.items.size() - 1) {
			options.visible(true);
			enemies.visible(false);
			negotiationTitle.visible(false);
		} else {
			@SuppressWarnings("unchecked")
			Pair<Negotiate, Player> a = (Pair<Negotiate, Player>)enemies.items.get(index).userObject;
			enemies.visible(false);
			
			ApproachType at = ApproachType.NEUTRAL;
			
			ResponseMode m = other.ai.diplomacy(player(), 
					a.first.type, 
					at, 
					a.second);
			
			displayResults(a.first, a.second.name, at, m);
			
			if (m == ResponseMode.YES) {
				setAlliance(a.second);
			}
		}
	}
	/**
	 * Setup alliance against the given common enemy.
	 * @param enemy the common enemy player
	 */
	protected void setAlliance(Player enemy) {
		DiplomaticRelation dr = world().getRelation(player(), other);
		dr.alliancesAgainst.add(enemy.id);
		
		dr = world().getRelation(player(), enemy);
		dr.alliancesAgainst.remove(other.id);
		
		dr = world().getRelation(other, enemy);
		dr.alliancesAgainst.remove(player().id);
		
		dr.value = Math.min(dr.value, 30);
	}
	/**
	 * Perform generic topic negotiation.
	 * @param neg the negotiation base.
	 */
	protected void doGeneric(Negotiate neg) {
		ApproachType at = ApproachType.NEUTRAL;

		ResponseMode m = other.ai.diplomacy(player(), 
				neg.type, 
				at, 
				null);
		
		if (m == ResponseMode.YES) {
			if (neg.type == NegotiateType.SURRENDER) {
				alienSurrender();
			} else
			if (neg.type == NegotiateType.DARGSLAN) {
				setAlliance(world().players.get("Dargslan"));
			} else
			if (neg.type == NegotiateType.TRADE) {
				setTradeAgreement();
			}
		}		
		displayResults(neg, "", at, m);
	}
	/**
	 * Set up the trade agreement.
	 */
	void setTradeAgreement() {
		DiplomaticRelation dr = world().getRelation(player(), other);
		dr.tradeAgreement = true;
	}
	/**
	 * Aliens surrender.
	 */
	protected void alienSurrender() {
		// take over planets
		for (Planet p : U.newArrayList(other.ownPlanets())) {
			p.takeover(player());
		}
		// FIXME keep their fleets as own?
		for (Fleet f : other.ownFleets()) {
			
			world().removeFleet(f);
			Fleet f0 = new Fleet(f.id, player());
			for (InventoryItem ii : f.inventory.iterable()) {
				InventoryItem ii0 = new InventoryItem(ii.id, player(), ii.type);
				ii0.assign(ii);
				f0.inventory.add(ii0);
			}
			player().fleets.put(f0, FleetKnowledge.FULL);
		}
	}
	/**
	 * Setup the text labels.
	 * @param offer the offer
	 * @param response the response
	 */
	void setupTexts(String offer, String response) {
		offerText.width = base.width - 20;
		offerText.text(offer);
		offerText.height = offerText.getWrappedHeight();
		
		responseText.width = base.width - 20;
		responseText.text(response);
		responseText.height = responseText.getWrappedHeight();
		
		onResize();
	}
	/**
	 * Display the incoming message.
	 * @param na the negotiation and approach type.
	 */
	void doIncomingMessage(final DiplomaticOffer na) {


		String label = null;
		final Diplomacy diplomacy = world().diplomacy.get(other.id);
		if (diplomacy != null) {
			outer:
			for (Call c : diplomacy.calls) {
				if (c.type == na.callType) {
					for (Approach a : c.approaches) {
						if (a.type == na.approach) {
							label = a.label;
							break outer;
						}
					}
				}
			}
			
			responseText.width = base.width - 20;
			if (na.callType == CallType.MONEY) {
				responseText.text(format(label, na.value()));
			} else {
				responseText.text(get(label));
			}
			responseText.height = responseText.getWrappedHeight();
			
			responseText.visible(true);
			continueLabel.visible(false);
			if (na.callType == CallType.ALLIANCE || na.callType == CallType.RESIGN
					|| na.callType == CallType.MONEY) {
				messagePhase = 4;
				acceptOffer.visible(true);
				declineOffer.visible(true);
				
				acceptOffer.onClick = new Action0() {
					@Override
					public void invoke() {
						acceptOffer.onClick = null;
						DiplomaticRelation dr = world().getRelation(world().player, other);
						if (na.callType == CallType.ALLIANCE) {
							dr.value = 91;
						} else
						if (na.callType == CallType.MONEY) {
							long mny = ((Number)na.value()).longValue();
							if (player().money() >= mny) {
								player().addMoney(-mny);
								other.addMoney(mny);
								other.statistics.moneyIncome.value += mny;
								player().statistics.moneySpent.value += mny;
								if (dr.value < 95) {
									dr.value += 5;
								}
							} else {
								commons.control().displayError(format("diplomacy.not_enough_money", other.name));
							}
						} else
						if (na.callType == CallType.RESIGN) {
							world().env.loseGame();
							return;
						}
						returnToOptions();
					}
				};
			} else {
				messagePhase = 2;
				continueLabel.visible(true);
			}
			change = 0;
			
			onResize();
		} else {
			returnToOptions();
			inCall = false;
		}
		player().offers.remove(other.id);

		DiplomaticRelation dr = world().getRelation(player(), other);
		
		if (na.callType == CallType.SURRENDER) {
			alienSurrender();
			dr.wontTalk(true);
		} else {
			dr.wontTalk(false);
		}
	}
	/**
	 * Automatically receive a diplomatic call.
	 */
	public void receive() {
		for (Map.Entry<String, DiplomaticOffer> de : player().offers.entrySet()) {
			if (openCloseAnimating) {
				receiveAgain();
			} else			
			if (other != null) {
				offerText.visible(false);
				responseText.visible(false);
				continueLabel.visible(false);
				options.visible(false);
				approachList.visible(false);
				moneyList.visible(false);
				enemies.visible(false);
				negotiationTitle.visible(false);
				
				if (!other.id.equals(de.getKey())) {
					headAnimation.loop = false;
					receiveAgain();
				} else {
					doIncomingMessage(de.getValue());
				}
			} else {
				other = world().players.get(de.getKey());
				if (projectorOpen) {
					contactRaceAnim();
				} else {
					AtomicInteger wip = new AtomicInteger(1);
					inCall = true;
					mentioned.clear();
					stanceMatrix.visible(false);
					loadHeadAnimAsync(wip);
				}
			}
			break;
		}
	}
	/** Receive again later. */
	void receiveAgain() {
		Parallels.runDelayedInEDT(250, new Runnable() {
			@Override
			public void run() {
				receive();
			}
		});
	}
	@Override
	protected Point scaleBase(int mx, int my) {
		UIMouse m = new UIMouse();
		m.x = mx;
		m.y = my;
		scaleMouse(m, base, margin()); 
		return new Point(m.x, m.y);
	}
	@Override
	protected Pair<Point, Double> scale() {
		Pair<Point, Double> s = scale(base, margin());
		return Pair.of(new Point(base.x, base.y), s.second);
	}
}
