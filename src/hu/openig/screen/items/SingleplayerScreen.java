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
import hu.openig.core.Difficulty;
import hu.openig.core.Pair;
import hu.openig.model.GameDefinition;
import hu.openig.model.Labels;
import hu.openig.model.Screens;
import hu.openig.model.SoundType;
import hu.openig.model.Traits;
import hu.openig.model.World;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.UIGenericButton;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIMouse;
import hu.openig.utils.U;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

import javax.swing.SwingUtilities;


/**
 * The single player screen with campaign selection.
 * @author akarnokd, 2010.01.11.
 */
public class SingleplayerScreen extends ScreenBase {
	/** The random used for background selection. */
	final Random rnd = new Random();
	/** The background image. */
	BufferedImage background;
	/** The list of campaigns. */
	final List<GameDefinition> campaigns = new ArrayList<>();
	/** The currently selected definition. */
	GameDefinition selectedDefinition;
	/** The campaign list. */
	final Rectangle campaignList = new Rectangle();
	/** The definition. */
	final Rectangle descriptionRect = new Rectangle();
	/** The video playback completion waiter. */
	volatile Thread videoWaiter;
	/** The load waiter. */
	volatile Thread loadWaiter;
	/** The reference frame. */
	final Rectangle base = new Rectangle();
	/** Play label. */
	UIGenericButton playLabel;
	/** Custom game. */
	UIGenericButton customLabel;
	/** Achievements label. */
	UIGenericButton backLabel;
	/** The left difficulty button. */
	UIImageButton difficultyLeft;
	/** The right difficulty button. */
	UIImageButton difficultyRight;
	/** The picture rectangle. */
	final Rectangle pictureRect = new Rectangle();
	/** The difficulty rectangle. */
	final Rectangle difficultyRect = new Rectangle();
	/** The difficulty index. */
	int difficulty;
	@Override
	public void onResize() {
		scaleResize(base, margin());

		int w = base.width / 2;

		playLabel.x = base.x + w + (w - playLabel.width) / 2;
		playLabel.y = base.y + base.height - playLabel.height - 5;
		
		customLabel.y = playLabel.y;
		customLabel.x = base.x + (base.width - customLabel.width) / 2; 
		
		backLabel.x = base.x + (w - backLabel.width) / 2;
		backLabel.y = base.y + base.height - backLabel.height - 5;
	
		campaignList.setBounds(base.x + 10, base.y + 30, base.width / 2 - 30, 100);
		descriptionRect.setBounds(campaignList.x, campaignList.y + campaignList.height + 30, 
				campaignList.width, 200);
		pictureRect.setBounds(base.x + base.width / 2, base.y + 30, 320, 400);
		
		difficultyLeft.x = base.x + 10;
		difficultyLeft.y = base.y + descriptionRect.y - base.y + descriptionRect.height + 30;
		
		difficultyRight.x = base.x + 10 + campaignList.width - difficultyRight.width;
		difficultyRight.y = difficultyLeft.y;
		
		difficultyRect.setBounds(
				difficultyLeft.x + difficultyLeft.width + 5, 
				difficultyLeft.y + (difficultyLeft.height - 22) / 2, 				
				difficultyRight.x - difficultyLeft.x - difficultyLeft.width - 10, 
				22);
	}

	@Override
	public void onFinish() {
		if (videoWaiter != null) {
			videoWaiter.interrupt();
			videoWaiter = null;
		}
		if (loadWaiter != null) {
			loadWaiter.interrupt();
			loadWaiter = null;
		}
	}

	@Override
	public void onInitialize() {
		base.setSize(commons.background().difficulty[0].getWidth(), 
				commons.background().difficulty[0].getHeight());
		
		playLabel = new UIGenericButton(
				get("singleplayer.start_game"),
				fontMetrics(16),
				commons.common().mediumButton,
				commons.common().mediumButtonPressed
				);
		playLabel.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
				doStartGame();
			}
		};
		customLabel = new UIGenericButton(
				get("singleplayer.customize_race"),
				fontMetrics(16),
				commons.common().mediumButton,
				commons.common().mediumButtonPressed
				);
		customLabel.disabledPattern(commons.common().disabledPattern);
		customLabel.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
				doCustomGame();
			}
		};
		
		backLabel = new UIGenericButton(
			get("singleplayer.back"),
			fontMetrics(16),
			commons.common().mediumButton,
			commons.common().mediumButtonPressed
		);
		backLabel.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
				displayPrimary(Screens.MAIN);
			}
		};
		
		difficultyLeft = new UIImageButton(commons.common().moveLeft);
		difficultyLeft.onClick = new Action0() { @Override public void invoke() { 
			buttonSound(SoundType.CLICK_HIGH_2);
			doDifficultyLess(); 
			adjustDifficultyButtons(); 
		} };
		difficultyLeft.setDisabledPattern(commons.common().disabledPattern);
		
		difficultyRight = new UIImageButton(commons.common().moveRight);
		difficultyRight.onClick = new Action0() { @Override public void invoke() { 
			buttonSound(SoundType.CLICK_HIGH_2);
			doDifficultyMore(); 
			adjustDifficultyButtons(); 
		} };
		difficultyRight.setDisabledPattern(commons.common().disabledPattern);
		
		addThis();
	}
	/** Less difficulty. */
	void doDifficultyLess() {
		difficulty = Math.max(0, difficulty - 1);
	}
	/** More difficulty. */
	void doDifficultyMore() {
		difficulty = Math.min(difficulty + 1, Difficulty.values().length);
	}
	/** Start the selected game. */
	void doStartGame() {
		if (selectedDefinition != null) {
			commons.world(null);
			commons.worldLoading = true;
			// display the loading screen.
			commons.control().displaySecondary(Screens.LOADING);
			final Semaphore barrier = new Semaphore(-1);
			startVideoWaiter(barrier);
			// the asynchronous loading
			startLoadWaiter(barrier);
			// the video playback
			commons.control().playVideos(new Action0() {
				@Override
				public void invoke() {
					barrier.release();
				}
			}, selectedDefinition.intro);
		}
	}

	/**
	 * Start the load waiter thread.
	 * @param barrier the notification barrier
	 */
	void startLoadWaiter(final Semaphore barrier) {
		loadWaiter = new Thread("Start Game Loading") {
			@Override 
			public void run() {
				try {
					final World world = new World(commons);
					world.definition = selectedDefinition;
					world.difficulty = Difficulty.values()[difficulty];
					
					final Labels labels = new Labels();
					labels.load(commons.rl, U.startWith(selectedDefinition.labels, "labels"));
					world.labels = labels;
					
					world.loadCampaign(commons.rl);
					
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							commons.labels().replaceWith(labels);
							commons.world(world);
							commons.worldLoading = false;
							commons.nongame = false;
							barrier.release();
						}
					});
				} finally {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							loadWaiter = null;	
						}
					});
				}
			}
        };
		loadWaiter.setPriority(Thread.MIN_PRIORITY);
		loadWaiter.start();
	}

	/**
	 * Start the video playback waiter.
	 * @param barrier the notification barrier
	 */
	void startVideoWaiter(final Semaphore barrier) {
		videoWaiter = new Thread("Start Game Video Waiter") {
			@Override 
			public void run() {
				try {
					barrier.acquire();
					SwingUtilities.invokeLater(new Runnable() {
						@Override 
						public void run() {
							enterGame();
						}
					});
				} catch (InterruptedException ex) {
					
				} finally {
					videoWaiter = null;
				}
			}
        };
		videoWaiter.setPriority(Thread.MIN_PRIORITY);
		videoWaiter.start();
	}
	/**
	 * Enter the game.
	 */
	void enterGame() {
		world().scripting.onNewGame();
		final boolean csw = config.computerVoiceScreen;
		config.computerVoiceScreen = false;
		commons.start(true);
		commons.control().displayPrimary(Screens.BRIDGE);
		
		config.computerVoiceScreen = csw;
		commons.control().displayStatusbar();
	}

    @Override
	public boolean keyboard(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			displayPrimary(Screens.MAIN);
			return true;
		}
		return false;
	}

	@Override
	public boolean mouse(UIMouse e) {
		scaleMouse(e, base, margin());
		boolean rep = false;
		switch (e.type) {
		case DOWN:
			if (campaignList.contains(e.x, e.y)) {
				int idx = (e.y - campaignList.y) / 20;
				if (idx < campaigns.size()) {
					selectedDefinition = campaigns.get(idx);
					rep = true;
				}
			} else {
				rep = super.mouse(e);
			}
			break;
		case DOUBLE_CLICK:
			if (campaignList.contains(e.x, e.y)) {
				int idx = (e.y - campaignList.y) / 20;
				if (idx < campaigns.size()) {
					selectedDefinition = campaigns.get(idx);
					doStartGame();
					rep = true;
				}
			} else {
				rep = super.mouse(e);
			}
			break;
		default:
			rep = super.mouse(e);
		}
		return rep;
	}
	@Override
	public void onEnter(Screens mode) {
		background = commons.background().difficulty[rnd.nextInt(commons.background().difficulty.length)];
		selectedDefinition = null;
		campaigns.clear();
		for (String name : commons.rl.listDirectories(commons.config.language, "campaign/")) {
			GameDefinition gd = GameDefinition.parse(commons.rl, "campaign/" + name);
			campaigns.add(gd);
		}
		Collections.sort(campaigns, new Comparator<GameDefinition>() {
			@Override
			public int compare(GameDefinition o1, GameDefinition o2) {
				return o1.name.compareToIgnoreCase(o2.name);
			}
		});
		for (String name : commons.rl.listDirectories(commons.config.language, "skirmish/")) {
			GameDefinition gd = GameDefinition.parse(commons.rl, "skirmish/" + name);
			campaigns.add(gd); // FIXME for now
		}
		
		
		selectedDefinition = campaigns.size() > 0 ? campaigns.get(0) : null;
		difficulty = Difficulty.values().length / 2;
		adjustDifficultyButtons();
		onResize();
	}
	/** Adjust difficulty buttons. */
	void adjustDifficultyButtons() {
		difficultyLeft.enabled(difficulty > 0);
		difficultyRight.enabled(difficulty < Difficulty.values().length - 1);
	}
	@Override
	public void onLeave() {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(Graphics2D g2) {
		AffineTransform savea = scaleDraw(g2, base, margin());
		
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, getInnerWidth(), getInnerHeight());
		g2.drawImage(background, base.x, base.y, null);
		
		Composite cp = g2.getComposite();
		g2.setComposite(AlphaComposite.SrcOver.derive(0.75f));
		
		g2.fill(campaignList);
		
		g2.fill(descriptionRect);
		
		g2.fill(difficultyRect);
		
		g2.setComposite(cp);
		
		int y = campaignList.y;
		commons.text().paintTo(g2, campaignList.x + 1, campaignList.y - 24, 20, 0xFF000000, get("singleplayer.campaigns"));
		commons.text().paintTo(g2, campaignList.x, campaignList.y - 25, 20, 0xFFFFFF00, get("singleplayer.campaigns"));
		for (GameDefinition gd : campaigns) {
			int color = selectedDefinition == gd ? 0xFFFFCC00 : 0xFF80FF80;
			commons.text().paintTo(g2, campaignList.x + 10, y + 2, 14, color, gd.getTitle(rl.language));
			y += 20;
		}
		g2.drawImage(commons.database().pictureEdge[0], pictureRect.x, pictureRect.y, null);
		g2.drawImage(commons.database().pictureEdge[1], pictureRect.x + pictureRect.width - commons.database().pictureEdge[1].getWidth(), pictureRect.y, null);
		g2.drawImage(commons.database().pictureEdge[2], pictureRect.x, pictureRect.y + pictureRect.height - commons.database().pictureEdge[2].getHeight(), null);
		g2.drawImage(commons.database().pictureEdge[3], pictureRect.x + pictureRect.width - commons.database().pictureEdge[3].getWidth(), pictureRect.y + pictureRect.height - commons.database().pictureEdge[3].getHeight(), null);
		
		commons.text().paintTo(g2, descriptionRect.x + 1, descriptionRect.y - 24, 20, 0xFF000000, get("singleplayer.description"));
		commons.text().paintTo(g2, descriptionRect.x, descriptionRect.y - 25, 20, 0xFFFFFF00, get("singleplayer.description"));
		
		commons.text().paintTo(g2, descriptionRect.x + 1, descriptionRect.y + descriptionRect.height + 6, 20, 0xFF000000, get("singleplayer.difficulty"));
		commons.text().paintTo(g2, descriptionRect.x, descriptionRect.y + descriptionRect.height + 5, 20, 0xFFFFFF00, get("singleplayer.difficulty"));
		
		if (selectedDefinition != null) {
			List<String> lines = new ArrayList<>();
			commons.text().wrapText(selectedDefinition.getDescription(rl.language), descriptionRect.width - 20, 14, lines);
			y = descriptionRect.y + 2;
			for (String s : lines) {
				commons.text().paintTo(g2, descriptionRect.x + 10, y, 14, 0xFF00FF00, s);
				y += 20;
			}
			if (selectedDefinition.image != null) {
				g2.drawImage(selectedDefinition.image, pictureRect.x + (pictureRect.width - selectedDefinition.image.getWidth()) / 2,
						pictureRect.y + (pictureRect.height - selectedDefinition.image.getHeight()) / 2, null);
			}
		}
		
		String diff = get(Difficulty.values()[difficulty].label);
		int diffw = commons.text().getTextWidth(14, diff);
		commons.text().paintTo(g2, difficultyRect.x + (difficultyRect.width - diffw) / 2, 
				difficultyRect.y + (difficultyLeft.height - difficultyRect.height) / 2, 14, 0xFF00FFFF, diff);

		super.draw(g2);
		
		g2.setTransform(savea);
	}
	@Override
	public Screens screen() {
		return Screens.SINGLEPLAYER;
	}
	@Override
	public void onEndGame() {
		
	}
	/** Switch to custom game. */
	void doCustomGame() {
		TraitScreen ts = (TraitScreen)displaySecondary(Screens.TRAITS);
		ts.updateTraits(selectedDefinition.traits);
		ts.onComplete = new Action1<Traits>() {
			@Override
			public void invoke(Traits value) {
				if (value != null) {
					selectedDefinition.traits = value;
				}
			}
		};
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

