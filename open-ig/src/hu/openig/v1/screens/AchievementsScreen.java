/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.screens;

import hu.openig.v1.Act;
import hu.openig.v1.ScreenBase;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.Timer;

/**
 * @author karnokd, 2010.01.11.
 * @version $Revision 1.0$
 */
public class AchievementsScreen extends ScreenBase {
	/** An achievement entry. */
	class AchievementEntry {
		/** Is this achieved? */
		public boolean enabled;
		/** The achievement title label. */
		public String title;
		/** The achievement description. */
		public String description;
		/**
		 * Constructor. 
		 * @param title the title label
		 * @param description the description label
		 * @param enabled is achieved?
		 */
		public AchievementEntry(String title, String description, boolean enabled) {
			this.enabled = enabled;
			this.title = title;
			this.description = description;
		}
	}
	/** The list of achievements. */
	public final List<AchievementEntry> achievements = new ArrayList<AchievementEntry>();
	/** The top index used when scrolling. */
	int topIndex;
	/** The visible achievement count. */
	int visibleCount;
	/** The screen origin. */
	final Rectangle origin = new Rectangle();
	/** The listing rectangle. */
	final Rectangle listRect = new Rectangle();
	/** A scroll button. */
	class ImageButton {
		/** The relative coordinate. */
		public int x;
		/** The relative coordinate. */
		public int y;
		/** Selected. */
		public boolean selected;
		/** Visible. */
		public boolean visible;
		/** Pressed. */
		public boolean pressed;
		/** The selected image. */
		BufferedImage selectedImage;
		/** The normal image. */
		BufferedImage normalImage;
		/** The pressed image. */
		BufferedImage pressedImage;
		/** The action to invoke when pressed. */
		Act onPress;
		/** The action to invoke when released. */
		Act onRelease;
		/**
		 * Test if the mouse is within this button.
		 * @param mx the mouse x
		 * @param my the mouse y
		 * @param x0 the reference x
		 * @param y0 the reference y
		 * @return true if in
		 */
		public boolean test(int mx, int my, int x0, int y0) {
			return visible && mx >= x0 + x && mx < x0 + x + normalImage.getWidth()
			&& my >= y0 + y && my < y0 + y + normalImage.getHeight();
		}
		/**
		 * Paint the button.
		 * @param g2 the graphics target
		 * @param x0 the reference
		 * @param y0 the reference
		 */
		public void paintTo(Graphics2D g2, int x0, int y0) {
			if (visible) {
				if (pressed) {
					g2.drawImage(pressedImage, x0 + x, y0 + y, null);
				} else
				if (selected) {
					g2.drawImage(selectedImage, x0 + x, y0 + y, null);
				} else {
					g2.drawImage(normalImage, x0 + x, y0 + y, null);
				}
			}
		}
		/**
		 * Invoke the associated action on press.
		 */
		public void invokePress() {
			if (onPress != null) {
				onPress.act();
			}
		}
		/** Invoke the associated action on release. */
		public void invokeRelease() {
			if (onRelease != null) {
				onRelease.act();
			}
		}
	}
	/** Scroll up button. */
	ImageButton scrollUpButton;
	/** Scroll down button. */
	ImageButton scrollDownButton;
	/** Scroll buttons. */
	final List<ImageButton> buttons = new ArrayList<ImageButton>();
	/** The timer for the continuous scroll down. */
	Timer scrollDownTimer;
	/** The timer for the continuous scroll up. */
	Timer scrollUpTimer;
	/** Bridge button. */
	private ImageButton bridge;
	/** Starmap button. */
	private ImageButton starmap;
	/** Colony button. */
	private ImageButton colony;
	/** Equipment button. */
	private ImageButton equimpent;
	/** Production button. */
	private ImageButton production;
	/** Research button. */
	private ImageButton research;
	/** Diplomacy button. */
	private ImageButton diplomacy;
	/** Information button. */
	private ImageButton info;
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#doResize()
	 */
	@Override
	public void doResize() {
		// TODO Auto-generated method stub
		origin.setBounds(
			(parent.getWidth() - commons.infoEmpty.getWidth()) / 2,
			20 + (parent.getHeight() - commons.infoEmpty.getHeight() - 38) / 2,
			commons.infoEmpty.getWidth(),
			commons.infoEmpty.getHeight()
		);
		listRect.setBounds(origin.x + 10, origin.y + 20, origin.width - 50, 350);
		visibleCount = listRect.height / 50;

		scrollUpButton.x = listRect.width + 12;
		scrollUpButton.y = 10 + (listRect.height / 2 - scrollUpButton.normalImage.getHeight()) / 2;
		
		scrollDownButton.x = scrollUpButton.x;
		scrollDownButton.y = 10 + listRect.height / 2 + (listRect.height / 2 - scrollDownButton.normalImage.getHeight()) / 2;

		bridge.x = 4 - bridge.normalImage.getWidth();
		bridge.y = origin.height - 2 - bridge.normalImage.getHeight();
		starmap.x = bridge.x + bridge.normalImage.getWidth();
		starmap.y = bridge.y;
		colony.x = starmap.x + starmap.normalImage.getWidth();
		colony.y = bridge.y;
		equimpent.x = colony.x + colony.normalImage.getWidth();
		equimpent.y = bridge.y;
		production.x = equimpent.x + equimpent.normalImage.getWidth();
		production.y = bridge.y;
		research.x = production.x + production.normalImage.getWidth();
		research.y = bridge.y;
		info.x = research.x + research.normalImage.getWidth();
		info.y = production.y;
		diplomacy.x = info.x + info.normalImage.getWidth();
		diplomacy.y = production.y;

	
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#finish()
	 */
	@Override
	public void finish() {
		scrollDownTimer.stop();
		scrollUpTimer.stop();
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#initialize()
	 */
	@Override
	public void initialize() {
		
		scrollDownTimer = new Timer(500, new Act() {
			@Override
			public void act() {
				doScrollDown();
			}
		});
		scrollDownTimer.setDelay(100);
		scrollUpTimer = new Timer(500, new Act() {
			@Override
			public void act() {
				doScrollUp();
			}
		});
		scrollUpTimer.setDelay(100);
		
		scrollUpButton = new ImageButton();
		scrollUpButton.normalImage = commons.database.arrowUp[0];
		scrollUpButton.selectedImage = commons.database.arrowUp[1];
		scrollUpButton.pressedImage = commons.database.arrowUp[2];
		
		scrollUpButton.onPress = new Act() {
			@Override
			public void act() {
				doScrollUp();
				scrollUpTimer.start();
			}
		};
		scrollUpButton.onRelease = new Act() {
			@Override
			public void act() {
				scrollUpTimer.stop();
			}
		};
		
		scrollDownButton = new ImageButton();
		scrollDownButton.normalImage = commons.database.arrowDown[0];
		scrollDownButton.selectedImage = commons.database.arrowDown[1];
		scrollDownButton.pressedImage = commons.database.arrowDown[2];
		scrollDownButton.onPress = new Act() {
			@Override
			public void act() {
				doScrollDown();
				scrollDownTimer.start();
			}
		};
		scrollDownButton.onRelease = new Act() {
			@Override
			public void act() {
				scrollDownTimer.stop();
			}
		};
		
		buttons.add(scrollUpButton);
		buttons.add(scrollDownButton);
		
		// create buttons for the main screens.
		
		bridge = new ImageButton();
//		bridge.visible = true;
		bridge.normalImage = commons.research.bridge[0];
		bridge.selectedImage = commons.research.bridge[0];
		bridge.pressedImage = commons.research.bridge[1];
		
		starmap = new ImageButton();
		starmap.visible = true;
		starmap.normalImage = commons.info.starmap[0];
		starmap.selectedImage = commons.info.starmap[0];
		starmap.pressedImage = commons.info.starmap[1];
		
		colony = new ImageButton();
		colony.visible = true;
		colony.normalImage = commons.info.colony[0];
		colony.selectedImage = commons.info.colony[0];
		colony.pressedImage = commons.info.colony[1];
		
		equimpent = new ImageButton();
		equimpent.visible = true;
		equimpent.normalImage = commons.research.equipmentButton[0];
		equimpent.selectedImage = commons.research.equipmentButton[0];
		equimpent.pressedImage = commons.research.equipmentButton[1];
		
		production = new ImageButton();
		production.visible = true;
		production.normalImage = commons.info.production[0];
		production.selectedImage = commons.info.production[0];
		production.pressedImage = commons.info.production[1];
		
		research = new ImageButton();
		research.visible = true;
		research.normalImage = commons.info.research[0];
		research.selectedImage = commons.info.research[0];
		research.pressedImage = commons.info.research[1];

		info = new ImageButton();
		info.visible = true;
		info.normalImage = commons.starmap.info[0];
		info.selectedImage = commons.starmap.info[0];
		info.pressedImage = commons.starmap.info[1];

		diplomacy = new ImageButton();
//		diplomacy.visible = true;
		diplomacy.normalImage = commons.info.diplomacy[0];
		diplomacy.selectedImage = commons.info.diplomacy[0];
		diplomacy.pressedImage = commons.info.diplomacy[1];
		
		buttons.add(bridge);
		buttons.add(starmap);
		buttons.add(colony);
		buttons.add(equimpent);
		buttons.add(production);
		buttons.add(research);
		buttons.add(info);
		buttons.add(diplomacy);
		
		// FIXME find other ways to populate the achievement list
		createTestAchievements();
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
		for (ImageButton btn : buttons) {
			if (btn.test(x, y, origin.x, origin.y)) {
				if (!btn.selected) {
					btn.selected = true;
					requestRepaint();
				}
			} else
			if (btn.selected || btn.pressed) {
				btn.selected = false;
				btn.pressed = false;
				if (btn == scrollUpButton) {
					scrollUpTimer.stop();
				} else
				if (btn == scrollDownButton) {
					scrollDownTimer.stop();
				}
				requestRepaint();
			}
		}
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mousePressed(int, int, int, int)
	 */
	@Override
	public void mousePressed(int button, int x, int y, int modifiers) {
		for (ImageButton btn : buttons) {
			if (btn.test(x, y, origin.x, origin.y)) {
				btn.pressed = true;
				btn.invokePress();
				requestRepaint();
			}
		}
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mouseReleased(int, int, int, int)
	 */
	@Override
	public void mouseReleased(int button, int x, int y, int modifiers) {
		for (ImageButton btn : buttons) {
			if (btn.pressed) {
				btn.pressed = false;
				if (btn.test(x, y, origin.x, origin.y)) {
					btn.invokeRelease();
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
		if (listRect.contains(x, y)) {
			if (direction < 0) {
				doScrollUp();
			} else {
				doScrollDown();
			}
		}
	}
	/** Scoll the list up. */
	void doScrollUp() {
		int oldIndex = topIndex;
		topIndex = Math.max(0, topIndex - 1);
		if (oldIndex != topIndex) {
			adjustScrollButtons();
		}
	}
	/** Scroll the list down. */
	void doScrollDown() {
		int oldIndex = topIndex;
		topIndex = Math.min(topIndex + 1, achievements.size() - visibleCount);
		if (oldIndex != topIndex) {
			adjustScrollButtons();
		}
	}
	/** Adjust the visibility of the scroll buttons. */
	void adjustScrollButtons() {
		scrollUpButton.visible = topIndex > 0;
		if (!scrollUpButton.visible) {
			scrollUpTimer.stop();
		}
		scrollDownButton.visible = topIndex < achievements.size() - visibleCount;
		if (!scrollDownButton.visible) {
			scrollDownTimer.stop();
		}
		repaint();
	}
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#onEnter()
	 */
	@Override
	public void onEnter() {
		adjustScrollButtons();
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
		Composite cp = g2.getComposite();
		g2.setComposite(AlphaComposite.SrcOver.derive(0.8f));
		g2.fillRect(0, 0, parent.getWidth(), parent.getHeight());
		g2.setComposite(cp);
		
		g2.drawImage(commons.infoEmpty, origin.x, origin.y, null);
		
		String t = commons.labels.get("statistics");
		int tw0 = commons.text.getTextWidth(14, t);
		g2.fillRect(origin.x + (origin.width / 2 - tw0) / 2 - 5, origin.y - 9, tw0 + 10, 18);
		commons.text.paintTo(g2, origin.x + (origin.width / 2 - tw0) / 2, origin.y - 7, 14, 0xFF00CC00, t);

		t = commons.labels.get("achievements");
		tw0 = commons.text.getTextWidth(14, t);
		g2.fillRect(origin.x + origin.width / 2 + (origin.width / 2 - tw0) / 2 - 5, origin.y - 9, tw0 + 10, 18);
		commons.text.paintTo(g2, origin.x + origin.width / 2 + (origin.width / 2 - tw0) / 2, origin.y - 7, 14, 0xFFFFCC00, t);

		Shape sp = g2.getClip();
		g2.setClip(listRect);
		int y = listRect.y;
		for (int i = topIndex; i < achievements.size() && i < topIndex + visibleCount + 1; i++) {
			AchievementEntry ae = achievements.get(i);
			String desc = commons.labels.get(ae.description);
			int tw = listRect.width - commons.achievement.getWidth() - 10;
			List<String> lines = new ArrayList<String>();
			commons.text.wrapText(desc, tw, 10, lines);
			BufferedImage img = commons.achievement;
			int color = 0xFF00FF00;
			if (!ae.enabled) {
				img = commons.achievementGrayed;
				color = 0xFFC0C0C0;
			}
			g2.drawImage(img, listRect.x, y, null);
			commons.text.paintTo(g2, listRect.x + commons.achievement.getWidth() + 10, y, 14, color, commons.labels.get(ae.title));
			int y1 = y + 20;
			for (int j = 0; j < lines.size(); j++) {
				commons.text.paintTo(g2, listRect.x + commons.achievement.getWidth() + 10, y1, 10, color, lines.get(j));
				y1 += 12;
			}
			y += 50;
		}
		
		g2.setClip(sp);
		
		for (ImageButton btn : buttons) {
			btn.paintTo(g2, origin.x, origin.y);
		}
		
	}
	/** Create the test achievements. */
	void createTestAchievements() {
		achievements.add(new AchievementEntry("achievement.conqueror", "achievement.conqueror.desc", false));
		achievements.add(new AchievementEntry("achievement.millionaire", "achievement.millionaire.desc", false));
		achievements.add(new AchievementEntry("achievement.student_of_bokros", "achievement.student_of_bokros.desc", false));
		achievements.add(new AchievementEntry("achievement.pirate_bay", "achievement.pirate_bay.desc", false));
		achievements.add(new AchievementEntry("achievement.dargslayer", "achievement.dargslayer.desc", false));
		achievements.add(new AchievementEntry("achievement.energizer", "achievement.energizer.desc", false));
		achievements.add(new AchievementEntry("achievement.death_star", "achievement.death_star.desc", false));
		achievements.add(new AchievementEntry("achievement.research_assistant", "achievement.research_assistant.desc", false));
		achievements.add(new AchievementEntry("achievement.scientist", "achievement.scientist.desc", false));
		achievements.add(new AchievementEntry("achievement.nobel_prize", "achievement.nobel_prize.desc", false));
		achievements.add(new AchievementEntry("achievement.popular", "achievement.popular.desc", false));
		achievements.add(new AchievementEntry("achievement.apeh", "achievement.apeh.desc", false));
		achievements.add(new AchievementEntry("achievement.ultimate_leader", "achievement.ultimate_leader.desc", false));
		achievements.add(new AchievementEntry("achievement.revolutioner", "achievement.revolutioner.desc", false));
		achievements.add(new AchievementEntry("achievement.mass_effect", "achievement.mass_effect.desc", false));
		achievements.add(new AchievementEntry("achievement.defender", "achievement.defender.desc", false));
		achievements.add(new AchievementEntry("achievement.embargo", "achievement.embargo.desc", false));
		achievements.add(new AchievementEntry("achievement.colombus", "achievement.colombus.desc", false));
		achievements.add(new AchievementEntry("achievement.quarter", "achievement.quarter.desc", false));
		achievements.add(new AchievementEntry("achievement.manufacturer", "achievement.manufacturer.desc", false));
		achievements.add(new AchievementEntry("achievement.salvage", "achievement.salvage.desc", false));
		achievements.add(new AchievementEntry("achievement.living_space", "achievement.living_space.desc", false));
		achievements.add(new AchievementEntry("achievement.food", "achievement.food.desc", false));
		achievements.add(new AchievementEntry("achievement.decade", "achievement.decade.desc", false));
		achievements.add(new AchievementEntry("achievement.oldest_man", "achievement.oldest_man.desc", false));
		achievements.add(new AchievementEntry("achievement.all_your_base", "achievement.all_your_base.desc", false));
		achievements.add(new AchievementEntry("achievement.et", "achievement.et.desc", false));
		achievements.add(new AchievementEntry("achievement.defense_contract", "achievement.defense_contract.desc", false));
		achievements.add(new AchievementEntry("achievement.coffee_break", "achievement.coffee_break.desc", false));
		achievements.add(new AchievementEntry("achievement.all_seeing_eye", "achievement.all_seeing_eye.desc", false));
		achievements.add(new AchievementEntry("achievement.newbie", "achievement.newbie.desc", false));
		achievements.add(new AchievementEntry("achievement.commander", "achievement.commander.desc", false));
		achievements.add(new AchievementEntry("achievement.admiral", "achievement.admiral.desc", false));
		achievements.add(new AchievementEntry("achievement.grand_admiral", "achievement.grand_admiral.desc", false));
		achievements.add(new AchievementEntry("achievement.influenza", "achievement.influenza.desc", false));
		
		Random rnd = new Random();
		for (AchievementEntry ae : achievements) {
			ae.enabled = rnd.nextBoolean();
		}
	}
}
