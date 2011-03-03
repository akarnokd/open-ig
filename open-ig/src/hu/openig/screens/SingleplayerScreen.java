/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import hu.openig.core.Act;
import hu.openig.core.Button;
import hu.openig.core.Difficulty;
import hu.openig.core.Labels;
import hu.openig.model.GameDefinition;
import hu.openig.model.World;
import hu.openig.ui.UIMouse;
import hu.openig.utils.XElement;

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
	/** The reference frame. */
	final Rectangle origin = new Rectangle();
	/** Scroll buttons. */
	final List<Button> buttons = new ArrayList<Button>();
	/** Statistics label. */
	ClickLabel playLabel;
	/** Achievements label. */
	ClickLabel backLabel;
	/** The left difficulty button. */
	ImageButton difficultyLeft;
	/** The right difficulty button. */
	ImageButton difficultyRight;
	/** The picture rectangle. */
	final Rectangle pictureRect = new Rectangle();
	/** The difficulty rectangle. */
	final Rectangle difficultyRect = new Rectangle();
	/** The difficulty index. */
	int difficulty;
	@Override
	public void onResize() {
		origin.setBounds((parent.getWidth() - 640) / 2, (parent.getHeight() - 480) / 2, 640, 480);

		int w = origin.width / 2;

		playLabel.x = w + (w - playLabel.getWidth()) / 2;
		playLabel.y = origin.height - 30;
		
		backLabel.x = (w - backLabel.getWidth()) / 2;
		backLabel.y = origin.height - 30;
	
		campaignList.setBounds(origin.x + 10, origin.y + 30, origin.width / 2 - 30, 100);
		descriptionRect.setBounds(campaignList.x, campaignList.y + campaignList.height + 30, 
				campaignList.width, 200);
		pictureRect.setBounds(origin.x + origin.width / 2, origin.y + 30, 320, 400);
		
		difficultyLeft.x = 10;
		difficultyLeft.y = descriptionRect.y - origin.y + descriptionRect.height + 30;
		
		difficultyRight.x = 10 + campaignList.width - difficultyRight.getWidth();
		difficultyRight.y = difficultyLeft.y;
		
		difficultyRect.setBounds(origin.x + difficultyLeft.x + difficultyLeft.getWidth() + 5, origin.y + difficultyLeft.y + (difficultyLeft.getHeight() - 22) / 2, 				
				difficultyRight.x - difficultyLeft.x - difficultyLeft.getWidth() - 10, 22);
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#finish()
	 */
	@Override
	public void onFinish() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#initialize()
	 */
	@Override
	public void onInitialize() {
		buttons.clear();
		playLabel = new ClickLabel();
		playLabel.visible = true;
		playLabel.commons = commons;
		playLabel.size = 20;
		playLabel.label = "singleplayer.start_game";
		playLabel.onLeave = new Act() { @Override public void act() { playLabel.selected = false; } };
		playLabel.onReleased = new Act() {
			@Override
			public void act() {
				playLabel.selected = false;
				doStartGame();
			}
		};
		playLabel.onPressed = new Act() {
			@Override
			public void act() {
				playLabel.selected = true;
			}
		};
		
		backLabel = new ClickLabel();
		backLabel.visible = true;
		backLabel.commons = commons;
		backLabel.size = 20;
		backLabel.label = "singleplayer.back";
		backLabel.onLeave = new Act() { @Override public void act() { backLabel.selected = false; } };
		backLabel.onReleased = new Act() {
			@Override
			public void act() {
				backLabel.selected = false;
				commons.control.displayPrimary(commons.screens.mainmenu);
			}
		};
		backLabel.onPressed = new Act() {
			@Override
			public void act() {
				backLabel.selected = true;
			}
		};
		
		difficultyLeft = new ImageButton();
		difficultyLeft.commons = commons;
		difficultyLeft.enabled = true;
		difficultyLeft.visible = true;
		difficultyLeft.normalImage = commons.moveLeft[0];
		difficultyLeft.selectedImage = commons.moveLeft[0];
		difficultyLeft.pressedImage = commons.moveLeft[1];
		difficultyLeft.onPress = new Act() { @Override public void act() { doDifficultyLess(); } };
		difficultyLeft.onRelease = new Act() { @Override public void act() { adjustDifficultyButtons(); } };
		difficultyLeft.onLeave = new Act() { @Override public void act() { adjustDifficultyButtons(); } };
		
		difficultyRight = new ImageButton();
		difficultyRight.commons = commons;
		difficultyRight.enabled = true;
		difficultyRight.visible = true;
		difficultyRight.normalImage = commons.moveRight[0];
		difficultyRight.selectedImage = commons.moveRight[0];
		difficultyRight.pressedImage = commons.moveRight[1];
		difficultyRight.onPress = new Act() { @Override public void act() { doDifficultyMore(); } };
		difficultyRight.onRelease = new Act() { @Override public void act() { adjustDifficultyButtons(); } };
		difficultyRight.onLeave = new Act() { @Override public void act() { adjustDifficultyButtons(); } };
		
		buttons.add(playLabel);
		buttons.add(backLabel);
		buttons.add(difficultyLeft);
		buttons.add(difficultyRight);

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
			commons.control.displaySecondary(commons.screens.loading);
			final Semaphore barrier = new Semaphore(-1);
			// the completion waiter thread
			Thread t0 = new Thread("Start Game Video Waiter") {
				@Override 
				public void run() {
					try {
						barrier.acquire();
						SwingUtilities.invokeLater(new Runnable() {
							@Override 
							public void run() {
								commons.control.displayPrimary(commons.screens.bridge);
							};
						});
					} catch (InterruptedException ex) {
						
					}
				};
			};
			t0.setPriority(Thread.MIN_PRIORITY);
			t0.start();
			commons.world = null;
			commons.worldLoading = true;
			// the asynchronous loading
			Thread t1 = new Thread("Start Game Loading") {
				@Override 
				public void run() {
					final World world = new World();
					world.definition = selectedDefinition;
					world.difficulty = Difficulty.values()[difficulty];
					final Labels labels = new Labels(); 
					labels.load(commons.rl, commons.language(), selectedDefinition.labels);
					world.load(commons.rl, commons.language(), selectedDefinition.name);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							commons.labels().replaceWith(labels);
							commons.world = world;
							commons.worldLoading = false;
							barrier.release();
						}
					});
				};
			};
			t1.setPriority(Thread.MIN_PRIORITY);
			t1.start();
			// the video playback
			commons.control.playVideos(new Act() {
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
			playLabel.selected = false;
			backLabel.selected = false;
			commons.control.displayPrimary(commons.screens.mainmenu);
		}
		return false;
	}

	@Override
	public boolean mouse(UIMouse e) {
		boolean rep = false;
		switch (e.type) {
		case MOVE:
			for (Button btn : buttons) {
				if (btn.test(e.x, e.y, origin.x, origin.y)) {
					if (!btn.mouseOver) {
						btn.mouseOver = true;
						btn.onEnter();
						rep = true;
					}
				} else
				if (btn.mouseOver || btn.pressed) {
					btn.mouseOver = false;
					btn.pressed = false;
					btn.onLeave();
					rep = true;
				}
			}
			break;
		case DOWN:
			for (Button btn : buttons) {
				if (btn.test(e.x, e.y, origin.x, origin.y)) {
					btn.pressed = true;
					btn.onPressed();
					rep = true;
				}
			}
			if (campaignList.contains(e.x, e.y)) {
				int idx = (e.y - campaignList.y) / 20;
				if (idx < campaigns.size()) {
					selectedDefinition = campaigns.get(idx);
					rep = true;
				}
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
			}
			break;
		case UP:
			for (Button btn : buttons) {
				if (btn.pressed) {
					btn.pressed = false;
					if (btn.test(e.x, e.y, origin.x, origin.y)) {
						btn.onReleased();
					}
					rep = true;
				}
			}
			break;
		default:
		}
		return rep;
	}
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
	@Override
	public void onEnter() {
		background = commons.background().difficulty[rnd.nextInt(commons.background().difficulty.length)];
		selectedDefinition = null;
		campaigns.clear();
		for (String name : commons.rl.listDirectories(commons.config.language, "campaign/")) {
			GameDefinition gd = parseDefinition(commons, "campaign/" + name);
			campaigns.add(gd);
		}
		for (String name : commons.rl.listDirectories(commons.config.language, "skirmish/")) {
			GameDefinition gd = parseDefinition(commons, "skirmish/" + name);
			skirmishes.add(gd);
		}
		difficulty = Difficulty.values().length / 2;
		adjustDifficultyButtons();
		onResize();
	}
	/** Adjust difficulty buttons. */
	void adjustDifficultyButtons() {
		difficultyLeft.enabled = difficulty > 0;
		difficultyRight.enabled = difficulty < Difficulty.values().length - 1;
	}
	@Override
	public void onLeave() {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(Graphics2D g2) {
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, parent.getWidth(), parent.getHeight());
		g2.drawImage(background, origin.x, origin.y, null);
		
		Composite cp = g2.getComposite();
		g2.setComposite(AlphaComposite.SrcOver.derive(0.75f));
		
		g2.fill(campaignList);
		
		g2.fill(descriptionRect);
		
		g2.fill(difficultyRect);
		
		g2.setComposite(cp);
		
		int y = campaignList.y;
		commons.text().paintTo(g2, campaignList.x + 1, campaignList.y - 24, 20, 0xFF000000, commons.labels().get("singleplayer.campaigns"));
		commons.text().paintTo(g2, campaignList.x, campaignList.y - 25, 20, 0xFFFFFF00, commons.labels().get("singleplayer.campaigns"));
		for (GameDefinition gd : campaigns) {
			int color = selectedDefinition == gd ? 0xFFFFCC00 : 0xFF80FF80;
			commons.text().paintTo(g2, campaignList.x + 10, y + 2, 14, color, gd.title);
			y += 20;
		}
		g2.drawImage(commons.database().pictureEdge[0], pictureRect.x, pictureRect.y, null);
		g2.drawImage(commons.database().pictureEdge[1], pictureRect.x + pictureRect.width - commons.database().pictureEdge[1].getWidth(), pictureRect.y, null);
		g2.drawImage(commons.database().pictureEdge[2], pictureRect.x, pictureRect.y + pictureRect.height - commons.database().pictureEdge[2].getHeight(), null);
		g2.drawImage(commons.database().pictureEdge[3], pictureRect.x + pictureRect.width - commons.database().pictureEdge[3].getWidth(), pictureRect.y + pictureRect.height - commons.database().pictureEdge[3].getHeight(), null);
		
		commons.text().paintTo(g2, descriptionRect.x + 1, descriptionRect.y - 24, 20, 0xFF000000, commons.labels().get("singleplayer.description"));
		commons.text().paintTo(g2, descriptionRect.x, descriptionRect.y - 25, 20, 0xFFFFFF00, commons.labels().get("singleplayer.description"));
		
		commons.text().paintTo(g2, descriptionRect.x + 1, descriptionRect.y + descriptionRect.height + 6, 20, 0xFF000000, commons.labels().get("singleplayer.difficulty"));
		commons.text().paintTo(g2, descriptionRect.x, descriptionRect.y + descriptionRect.height + 5, 20, 0xFFFFFF00, commons.labels().get("singleplayer.difficulty"));
		
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
		
		String diff = commons.labels().get(Difficulty.values()[difficulty].label);
		int diffw = commons.text().getTextWidth(14, diff);
		commons.text().paintTo(g2, difficultyRect.x + (difficultyRect.width - diffw) / 2, 
				difficultyRect.y + (difficultyLeft.getHeight() - difficultyRect.height) / 2, 14, 0xFF00FFFF, diff);
		
		for (Button btn : buttons) {
			btn.paintTo(g2, origin.x, origin.y);
		}
	}
	/**
	 * Parse the game definition from.
	 * @param commons the common resources
	 * @param name the definition/game name
	 * @return the parsed definition.
	 */
	public static GameDefinition parseDefinition(CommonResources commons, String name) {
		GameDefinition result = new GameDefinition();
		result.name = name;
		XElement root = commons.rl.getXML(commons.config.language, name + "/definition");
		for (XElement texts : root.childrenWithName("texts")) {
			if (commons.config.language.equals(texts.get("language"))) {
				result.title = texts.childValue("title");
				result.description = texts.childValue("description");
				break;
			}
		}
		result.intro = root.childValue("intro");
		result.image = commons.rl.getImage(commons.config.language, root.childValue("image"));
		result.startingLevel = Integer.parseInt(root.childValue("level"));
		result.labels = root.childValue("labels");
		result.galaxy = root.childValue("galaxy");
		result.races = root.childValue("races");
		result.tech = root.childValue("tech");
		result.build = root.childValue("build");
		result.planets = root.childValue("planets");
		result.bridge = root.childValue("bridge");
		result.walk = root.childValue("walk");
		result.talk = root.childValue("talk");
		
		return result;
	}

}
