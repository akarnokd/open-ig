/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import hu.openig.core.Act;
import hu.openig.core.Difficulty;
import hu.openig.core.Labels;
import hu.openig.model.GameDefinition;
import hu.openig.model.World;
import hu.openig.render.RenderTools;
import hu.openig.ui.UIGenericButton;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIMouse;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
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
	final List<GameDefinition> campaigns = new ArrayList<GameDefinition>();
	/** The list of campaigns. */
	final List<GameDefinition> skirmishes = new ArrayList<GameDefinition>();
	/** The currently selected definition. */
	GameDefinition selectedDefinition;
	/** The campaign list. */
	final Rectangle campaignList = new Rectangle();
	/** The definition. */
	final Rectangle descriptionRect = new Rectangle();
	/** The video playback completion waiter. */
	private Thread videoWaiter;
	/** The reference frame. */
	final Rectangle origin = new Rectangle();
	/** Statistics label. */
	UIGenericButton playLabel;
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
		RenderTools.centerScreen(origin, getInnerWidth(), getInnerHeight(), true);

		int w = origin.width / 2;

		playLabel.x = origin.x + w + (w - playLabel.width) / 2;
		playLabel.y = origin.y + origin.height - playLabel.height - 5;
		
		backLabel.x = origin.x + (w - backLabel.width) / 2;
		backLabel.y = origin.y + origin.height - backLabel.height - 5;
	
		campaignList.setBounds(origin.x + 10, origin.y + 30, origin.width / 2 - 30, 100);
		descriptionRect.setBounds(campaignList.x, campaignList.y + campaignList.height + 30, 
				campaignList.width, 200);
		pictureRect.setBounds(origin.x + origin.width / 2, origin.y + 30, 320, 400);
		
		difficultyLeft.x = origin.x + 10;
		difficultyLeft.y = origin.y + descriptionRect.y - origin.y + descriptionRect.height + 30;
		
		difficultyRight.x = origin.x + 10 + campaignList.width - difficultyRight.width;
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
		}
	}

	@Override
	public void onInitialize() {
		origin.setSize(commons.background().difficulty[0].getWidth(), 
				commons.background().difficulty[0].getHeight());
		
		playLabel = new UIGenericButton(
				get("singleplayer.start_game"),
				fontMetrics(16),
				commons.common().mediumButton,
				commons.common().mediumButtonPressed
				);
		playLabel.onClick = new Act() {
			@Override
			public void act() {
				doStartGame();
			}
		};
		
		backLabel = new UIGenericButton(
			get("singleplayer.back"),
			fontMetrics(16),
			commons.common().mediumButton,
			commons.common().mediumButtonPressed
		);
		backLabel.onClick = new Act() {
			@Override
			public void act() {
				displayPrimary(Screens.MAIN);
			}
		};
		
		difficultyLeft = new UIImageButton(commons.common().moveLeft);
		difficultyLeft.onClick = new Act() { @Override public void act() { doDifficultyLess(); adjustDifficultyButtons(); } };
		difficultyLeft.setDisabledPattern(commons.common().disabledPattern);
		
		difficultyRight = new UIImageButton(commons.common().moveRight);
		difficultyRight.onClick = new Act() { @Override public void act() { doDifficultyMore(); adjustDifficultyButtons(); } };
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
			// display the loading screen.
			commons.control().displaySecondary(Screens.LOADING);
			final Semaphore barrier = new Semaphore(-1);
			videoWaiter = new Thread("Start Game Video Waiter") {
				@Override 
				public void run() {
					try {
						barrier.acquire();
						SwingUtilities.invokeLater(new Runnable() {
							@Override 
							public void run() {
								commons.world().start();
								commons.control().displayPrimary(Screens.BRIDGE);
								commons.control().displayStatusbar();
							};
						});
					} catch (InterruptedException ex) {
						
					}
				};
			};
			videoWaiter.setPriority(Thread.MIN_PRIORITY);
			videoWaiter.start();
			commons.world(null);
			commons.worldLoading = true;
			// the asynchronous loading
			Thread t1 = new Thread("Start Game Loading") {
				@Override 
				public void run() {
					final World world = new World(commons.pool, commons.control());
					world.definition = selectedDefinition;
					world.difficulty = Difficulty.values()[difficulty];
					final Labels labels = new Labels(); 
					labels.load(commons.rl, selectedDefinition.labels);
					world.labels = labels;
					world.load(commons.rl, selectedDefinition.name);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							commons.labels0().replaceWith(labels);
							commons.world(world);
							commons.worldLoading = false;
							barrier.release();
						}
					});
				};
			};
			t1.setPriority(Thread.MIN_PRIORITY);
			t1.start();
			// the video playback
			playVideos(new Act() {
				@Override
				public void act() {
					barrier.release();
				}
			}, selectedDefinition.intro);
		}
	}
	@Override
	public boolean keyboard(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			displayPrimary(Screens.MAIN);
			displayStatusbar();
		}
		return false;
	}

	@Override
	public boolean mouse(UIMouse e) {
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
			GameDefinition gd = GameDefinition.parse(commons, "campaign/" + name);
			campaigns.add(gd);
		}
		for (String name : commons.rl.listDirectories(commons.config.language, "skirmish/")) {
			GameDefinition gd = GameDefinition.parse(commons, "skirmish/" + name);
			skirmishes.add(gd);
		}
		
		selectedDefinition = campaigns.size() > 0 ? campaigns.get(0) : (skirmishes.size() > 0 ? skirmishes.get(0) : null);
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
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, getInnerWidth(), getInnerHeight());
		g2.drawImage(background, origin.x, origin.y, null);
		
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
			commons.text().paintTo(g2, campaignList.x + 10, y + 2, 14, color, gd.title);
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
			List<String> lines = new ArrayList<String>();
			commons.text().wrapText(selectedDefinition.description, descriptionRect.width - 20, 14, lines);
			y = descriptionRect.y + 2;
			for (String s : lines) {
				commons.text().paintTo(g2, descriptionRect.x + 10, y, 14, 0xFF00FF00, s);
				y += 20;
			}
			g2.drawImage(selectedDefinition.image, pictureRect.x + (pictureRect.width - selectedDefinition.image.getWidth()) / 2,
					pictureRect.y + (pictureRect.height - selectedDefinition.image.getHeight()) / 2, null);
		}
		
		String diff = get(Difficulty.values()[difficulty].label);
		int diffw = commons.text().getTextWidth(14, diff);
		commons.text().paintTo(g2, difficultyRect.x + (difficultyRect.width - diffw) / 2, 
				difficultyRect.y + (difficultyLeft.height - difficultyRect.height) / 2, 14, 0xFF00FFFF, diff);

		super.draw(g2);
	}
	@Override
	public Screens screen() {
		return Screens.SINGLEPLAYER;
	}
	@Override
	public void onEndGame() {
		// TODO Auto-generated method stub
		
	}
}
