/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.screens;

import hu.openig.utils.XML;
import hu.openig.v1.core.Act;
import hu.openig.v1.core.Button;
import hu.openig.v1.core.Difficulty;
import hu.openig.v1.model.GameDefinition;

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

import org.w3c.dom.Element;


/**
 * The single player screen with campaign selection.
 * @author karnokd, 2010.01.11.
 * @version $Revision 1.0$
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
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#doResize()
	 */
	@Override
	public void doResize() {
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
	public void finish() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#initialize()
	 */
	@Override
	public void initialize() {
		buttons.clear();
		playLabel = new ClickLabel();
		playLabel.visible = true;
		playLabel.commons = commons;
		playLabel.size = 20;
		playLabel.label = "singleplayer.start_game";
		playLabel.onLeave = new Act() { public void act() { playLabel.selected = false; } };
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
		backLabel.onLeave = new Act() { public void act() { backLabel.selected = false; } };
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
		difficultyLeft.onPress = new Act() { public void act() { doDifficultyLess(); } };
		difficultyLeft.onRelease = new Act() { public void act() { adjustDifficultyButtons(); } };
		difficultyLeft.onLeave = new Act() { public void act() { adjustDifficultyButtons(); } };
		
		difficultyRight = new ImageButton();
		difficultyRight.commons = commons;
		difficultyRight.enabled = true;
		difficultyRight.visible = true;
		difficultyRight.normalImage = commons.moveRight[0];
		difficultyRight.selectedImage = commons.moveRight[0];
		difficultyRight.pressedImage = commons.moveRight[1];
		difficultyRight.onPress = new Act() { public void act() { doDifficultyMore(); } };
		difficultyRight.onRelease = new Act() { public void act() { adjustDifficultyButtons(); } };
		difficultyRight.onLeave = new Act() { public void act() { adjustDifficultyButtons(); } };
		
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
			commons.control.playVideos(new Act() {
				@Override
				public void act() {
					commons.control.displayPrimary(commons.screens.bridge);
				}
			}, selectedDefinition.intro);
		}
	}
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#keyTyped(int, int)
	 */
	@Override
	public void keyTyped(int key, int modifiers) {
		if (key == KeyEvent.VK_ESCAPE) {
			playLabel.selected = false;
			backLabel.selected = false;
			commons.control.displayPrimary(commons.screens.mainmenu);
		}
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mouseMoved(int, int, int, int)
	 */
	@Override
	public void mouseMoved(int button, int x, int y, int modifiers) {
		for (Button btn : buttons) {
			if (btn.test(x, y, origin.x, origin.y)) {
				if (!btn.mouseOver) {
					btn.mouseOver = true;
					btn.onEnter();
					requestRepaint();
				}
			} else
			if (btn.mouseOver || btn.pressed) {
				btn.mouseOver = false;
				btn.pressed = false;
				btn.onLeave();
				requestRepaint();
			}
		}

	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mousePressed(int, int, int, int)
	 */
	@Override
	public void mousePressed(int button, int x, int y, int modifiers) {
		for (Button btn : buttons) {
			if (btn.test(x, y, origin.x, origin.y)) {
				btn.pressed = true;
				btn.onPressed();
				requestRepaint();
			}
		}
		if (campaignList.contains(x, y)) {
			int idx = (y - campaignList.y) / 20;
			if (idx < campaigns.size()) {
				selectedDefinition = campaigns.get(idx);
				requestRepaint();
			}
		}
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mouseReleased(int, int, int, int)
	 */
	@Override
	public void mouseReleased(int button, int x, int y, int modifiers) {
		for (Button btn : buttons) {
			if (btn.pressed) {
				btn.pressed = false;
				if (btn.test(x, y, origin.x, origin.y)) {
					btn.onReleased();
				}
				requestRepaint();
			}
		}
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mouseScrolled(int, int, int, int)
	 */
	@Override
	public void mouseScrolled(int direction, int x, int y, int modifiers) {
		// TODO Auto-generated method stub

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
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#onEnter()
	 */
	@Override
	public void onEnter() {
		background = commons.background.difficulty[rnd.nextInt(commons.background.difficulty.length)];
		selectedDefinition = null;
		campaigns.clear();
		for (String name : commons.rl.listDirectories(commons.config.language, "campaign/")) {
			GameDefinition gd = parseDefinition("campaign/" + name);
			campaigns.add(gd);
		}
		for (String name : commons.rl.listDirectories(commons.config.language, "skirmish/")) {
			GameDefinition gd = parseDefinition("skirmish/" + name);
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
		g2.drawImage(background, origin.x, origin.y, null);
		
		Composite cp = g2.getComposite();
		g2.setComposite(AlphaComposite.SrcOver.derive(0.75f));
		
		g2.fill(campaignList);
		
		g2.fill(descriptionRect);
		
		g2.fill(difficultyRect);
		
		g2.setComposite(cp);
		
		int y = campaignList.y;
		commons.text.paintTo(g2, campaignList.x + 1, campaignList.y - 24, 20, 0xFF000000, commons.labels.get("singleplayer.campaigns"));
		commons.text.paintTo(g2, campaignList.x, campaignList.y - 25, 20, 0xFFFFFF00, commons.labels.get("singleplayer.campaigns"));
		for (GameDefinition gd : campaigns) {
			int color = selectedDefinition == gd ? 0xFFFFCC00 : 0xFF80FF80;
			commons.text.paintTo(g2, campaignList.x + 10, y + 2, 14, color, gd.title);
			y += 20;
		}
		g2.drawImage(commons.database.pictureEdge[0], pictureRect.x, pictureRect.y, null);
		g2.drawImage(commons.database.pictureEdge[1], pictureRect.x + pictureRect.width - commons.database.pictureEdge[1].getWidth(), pictureRect.y, null);
		g2.drawImage(commons.database.pictureEdge[2], pictureRect.x, pictureRect.y + pictureRect.height - commons.database.pictureEdge[2].getHeight(), null);
		g2.drawImage(commons.database.pictureEdge[3], pictureRect.x + pictureRect.width - commons.database.pictureEdge[3].getWidth(), pictureRect.y + pictureRect.height - commons.database.pictureEdge[3].getHeight(), null);
		
		commons.text.paintTo(g2, descriptionRect.x + 1, descriptionRect.y - 24, 20, 0xFF000000, commons.labels.get("singleplayer.description"));
		commons.text.paintTo(g2, descriptionRect.x, descriptionRect.y - 25, 20, 0xFFFFFF00, commons.labels.get("singleplayer.description"));
		
		commons.text.paintTo(g2, descriptionRect.x + 1, descriptionRect.y + descriptionRect.height + 6, 20, 0xFF000000, commons.labels.get("singleplayer.difficulty"));
		commons.text.paintTo(g2, descriptionRect.x, descriptionRect.y + descriptionRect.height + 5, 20, 0xFFFFFF00, commons.labels.get("singleplayer.difficulty"));
		
		if (selectedDefinition != null) {
			List<String> lines = new ArrayList<String>();
			commons.text.wrapText(selectedDefinition.description, descriptionRect.width - 20, 14, lines);
			y = descriptionRect.y + 2;
			for (String s : lines) {
				commons.text.paintTo(g2, descriptionRect.x + 10, y, 14, 0xFF00FF00, s);
				y += 20;
			}
			g2.drawImage(selectedDefinition.image, pictureRect.x + (pictureRect.width - selectedDefinition.image.getWidth()) / 2,
					pictureRect.y + (pictureRect.height - selectedDefinition.image.getHeight()) / 2, null);
		}
		
		String diff = commons.labels.get(Difficulty.values()[difficulty].label);
		int diffw = commons.text.getTextWidth(14, diff);
		commons.text.paintTo(g2, difficultyRect.x + (difficultyRect.width - diffw) / 2, 
				difficultyRect.y + (difficultyLeft.getHeight() - difficultyRect.height) / 2, 14, 0xFF00FFFF, diff);
		
		for (Button btn : buttons) {
			btn.paintTo(g2, origin.x, origin.y);
		}
	}
	/**
	 * Parse the game definition from.
	 * @param name the definition/game name
	 * @return the parsed definition.
	 */
	public GameDefinition parseDefinition(String name) {
		GameDefinition result = new GameDefinition();
		result.name = name;
		Element root = commons.rl.getXML(commons.config.language, name + "/definition");
		for (Element texts : XML.childrenWithName(root, "texts")) {
			if (commons.config.language.equals(texts.getAttribute("language"))) {
				result.title = XML.childValue(texts, "title");
				result.description = XML.childValue(texts, "description");
				break;
			}
		}
		result.intro = XML.childValue(root, "intro");
		result.image = commons.rl.getImage(commons.config.language, XML.childValue(root, "image"));
		return result;
	}

}
