/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.screens;

import hu.openig.v1.core.Act;
import hu.openig.v1.core.Button;

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
 * The achievements and statistics listing screen.
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
	/** The statistics entry. */
	class StatisticsEntry {
		/** The label of the statistics. */
		public String label;
		/** The value of the statistics. */
		public String value;
		/**
		 * Constructor.
		 * @param label the label
		 * @param value the value
		 */
		public StatisticsEntry(String label, String value) {
			this.label = label;
			this.value = value;
		}
	}
	/** The list of achievements. */
	public final List<AchievementEntry> achievements = new ArrayList<AchievementEntry>();
	/** The list of statistics. */
	public final List<StatisticsEntry> statistics = new ArrayList<StatisticsEntry>();
	/** The saved statistics index. */
	int statisticsIndex;
	/** The saved statistics count. */
	int statisticsCount;
	/** The saved achievements index. */
	int achievementIndex;
	/** The saved achievements count. */
	int achievementCount;
	/** The screen origin. */
	final Rectangle origin = new Rectangle();
	/** The listing rectangle. */
	final Rectangle listRect = new Rectangle();
	/** Scroll up button. */
	ImageButton scrollUpButton;
	/** Scroll down button. */
	ImageButton scrollDownButton;
	/** Scroll buttons. */
	final List<Button> buttons = new ArrayList<Button>();
	/** The timer for the continuous scroll down. */
	Timer scrollDownTimer;
	/** The timer for the continuous scroll up. */
	Timer scrollUpTimer;
	/** Bridge button. */
	ImageButton bridge;
	/** Starmap button. */
	ImageButton starmap;
	/** Colony button. */
	ImageButton colony;
	/** Equipment button. */
	ImageButton equimpent;
	/** Production button. */
	ImageButton production;
	/** Research button. */
	ImageButton research;
	/** Diplomacy button. */
	ImageButton diplomacy;
	/** Information button. */
	ImageButton info;
	/** Statistics label. */
	ClickLabel statisticsLabel;
	/** Achievements label. */
	ClickLabel achievementLabel;
	/** The rendering mode. */
	public enum Mode {
		/** Statistics screen. */
		STATISTICS,
		/** Achievements screen. */
		ACHIEVEMENTS
	}
	/** The current display mode. */
	public Mode mode = Mode.STATISTICS;
	@Override
	public void doResize() {
		origin.setBounds(
			(parent.getWidth() - commons.infoEmpty.getWidth()) / 2,
			20 + (parent.getHeight() - commons.infoEmpty.getHeight() - 38) / 2,
			commons.infoEmpty.getWidth(),
			commons.infoEmpty.getHeight()
		);
		listRect.setBounds(origin.x + 10, origin.y + 20, origin.width - 50, 350);
		achievementCount = listRect.height / 50;
		statisticsCount = listRect.height / 20;
		
		scrollUpButton.x = listRect.width + 12;
		scrollUpButton.y = 10 + (listRect.height / 2 - scrollUpButton.normalImage.getHeight()) / 2;
		
		scrollDownButton.x = scrollUpButton.x;
		scrollDownButton.y = 10 + listRect.height / 2 + (listRect.height / 2 - scrollDownButton.normalImage.getHeight()) / 2;

		bridge.x = 4 - bridge.normalImage.getWidth();
		bridge.y = origin.height - 2 - bridge.normalImage.getHeight();
		bridge.visible = false;
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
		diplomacy.visible = false;

		statisticsLabel.commons = commons;
		statisticsLabel.x = (origin.width / 2 - achievementLabel.getWidth()) / 2;
		statisticsLabel.y = - achievementLabel.getHeight() / 2;
		
		achievementLabel.commons = commons;
		achievementLabel.x = origin.width / 2 + (origin.width / 2 - statisticsLabel.getWidth()) / 2;
		achievementLabel.y = - statisticsLabel.getHeight() / 2;
		
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
		
		buttons.clear();
		achievements.clear();
		statistics.clear();
		
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
		scrollUpButton.onLeave = new Act() {
			@Override
			public void act() {
				scrollUpTimer.stop();
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
		scrollDownButton.onLeave = new Act() {
			@Override
			public void act() {
				scrollDownTimer.stop();
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
		
		achievementLabel = new ClickLabel();
		achievementLabel.commons = commons;
		achievementLabel.size = 14;
		achievementLabel.label = "achievements";
		achievementLabel.onPressed = new Act() {
			@Override
			public void act() {
				mode = Mode.ACHIEVEMENTS;
				adjustLabels();
				adjustScrollButtons();
			}
		};
		buttons.add(achievementLabel);
		
		statisticsLabel = new ClickLabel();
		statisticsLabel.commons = commons;
		statisticsLabel.size = 14;
		statisticsLabel.label = "statistics";
		statisticsLabel.onPressed = new Act() {
			@Override
			public void act() {
				mode = Mode.STATISTICS;
				adjustLabels();
				adjustScrollButtons();
			}
		};
		buttons.add(statisticsLabel);
		
		// FIXME find other ways to populate the achievement list
		createTestEntries();
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
		if (mode == Mode.STATISTICS) {
			int oldIndex = statisticsIndex;
			statisticsIndex = Math.max(0, statisticsIndex - 1);
			if (oldIndex != statisticsIndex) {
				adjustScrollButtons();
			}
		} else
		if (mode == Mode.ACHIEVEMENTS) {
			int oldIndex = achievementIndex;
			achievementIndex = Math.max(0, achievementIndex - 1);
			if (oldIndex != achievementIndex) {
				adjustScrollButtons();
			}
		}
	}
	/** Scroll the list down. */
	void doScrollDown() {
		if (mode == Mode.STATISTICS) {
			if (statistics.size() > statisticsCount) {
				int oldIndex = statisticsIndex;
				statisticsIndex = Math.min(statisticsIndex + 1, statistics.size() - statisticsCount);
				if (oldIndex != statisticsIndex) {
					adjustScrollButtons();
				}
			}
		} else
		if (mode == Mode.ACHIEVEMENTS) {
			if (achievements.size() > achievementCount) {
				int oldIndex = achievementIndex;
				achievementIndex = Math.min(achievementIndex + 1, achievements.size() - achievementCount);
				if (oldIndex != achievementIndex) {
					adjustScrollButtons();
				}
			}
		}
	}
	/** Adjust the visibility of the scroll buttons. */
	void adjustScrollButtons() {
		if (mode == Mode.STATISTICS) {
			scrollUpButton.visible = statisticsIndex > 0;
			scrollDownButton.visible = statisticsIndex < statistics.size() - statisticsCount;
		} else
		if (mode == Mode.ACHIEVEMENTS) {
			scrollUpButton.visible = achievementIndex > 0;
			scrollDownButton.visible = achievementIndex < achievements.size() - achievementCount;
		}		
		if (!scrollUpButton.visible) {
			scrollUpTimer.stop();
		}
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
		onResize();
		adjustScrollButtons();
		adjustLabels();
	}
	/** Adjust label selection. */
	void adjustLabels() {
		achievementLabel.selected = mode == Mode.ACHIEVEMENTS;
		statisticsLabel.selected = mode == Mode.STATISTICS;
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
		
		Shape sp = g2.getClip();
		g2.setClip(listRect);
		if (mode == Mode.ACHIEVEMENTS) {
			int y = listRect.y;
			for (int i = achievementIndex; i < achievements.size() && i < achievementIndex + achievementCount; i++) {
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
		} else
		if (mode == Mode.STATISTICS) {
			int y = listRect.y;
			int h = 10;
			for (int i = statisticsIndex; i < statistics.size() && i < statisticsIndex + statisticsCount; i++) {
				StatisticsEntry se = statistics.get(i);
				commons.text.paintTo(g2, listRect.x, y, h, 0xFF80FF80, commons.labels.get(se.label));
				int w2 = commons.text.getTextWidth(h, se.value);
				commons.text.paintTo(g2, listRect.x + listRect.width - w2 - 5, y, h, 0xFF8080FF, se.value);
				
				y += 20;
			}
		}
		g2.setClip(sp);
		
		for (Button btn : buttons) {
			btn.paintTo(g2, origin.x, origin.y);
		}
		
	}
	/** Create the test achievements. */
	void createTestEntries() {
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
		
		statistics.add(new StatisticsEntry("statistics.total_gametime", "1 23:50.00"));
		statistics.add(new StatisticsEntry("statistics.total_ingame_time", "50-03-01 12:50.00"));
		statistics.add(new StatisticsEntry("statistics.money_aquired", "32.000"));
		statistics.add(new StatisticsEntry("statistics.money_aquired_trade", "0"));
		statistics.add(new StatisticsEntry("statistics.money_spent", "0 (0%)"));
		statistics.add(new StatisticsEntry("statistics.money_spent_building", "0 (0%)"));
		statistics.add(new StatisticsEntry("statistics.money_spent_production", "0 (0%)"));
		statistics.add(new StatisticsEntry("statistics.money_spent_research", "0 (0%)"));
		statistics.add(new StatisticsEntry("statistics.equipment_sold_money", "0 (0%)"));
		statistics.add(new StatisticsEntry("statistics.planet_discovered", "0 (0%)"));
		statistics.add(new StatisticsEntry("statistics.planet_own", "3 (3%)"));
		statistics.add(new StatisticsEntry("statistics.planet_colonized", "0 (0%)"));
		statistics.add(new StatisticsEntry("statistics.races_discovered", "1 (9%)"));
		statistics.add(new StatisticsEntry("statistics.produced_items", "0"));
		statistics.add(new StatisticsEntry("statistics.sold_items", "0"));
		statistics.add(new StatisticsEntry("statistics.research_count", "5 (2%)"));
		statistics.add(new StatisticsEntry("statistics.research_acquired", "0 (0%)"));
		statistics.add(new StatisticsEntry("statistics.total_population", "350.000"));
		statistics.add(new StatisticsEntry("statistics.total_houses", "170.000"));
		statistics.add(new StatisticsEntry("statistics.total_energy", "121.000"));
		statistics.add(new StatisticsEntry("statistics.actual_energy", "121.000 (100%)"));
		statistics.add(new StatisticsEntry("statistics.total_energy_demand", "100.000 (89%)"));
		statistics.add(new StatisticsEntry("statistics.total_buildings", "62"));
		statistics.add(new StatisticsEntry("statistics.actual_buildings", "47 (75%)"));
		statistics.add(new StatisticsEntry("statistics.total_food_production", "85.000"));
		statistics.add(new StatisticsEntry("statistics.total_hospital", "130.000"));
		statistics.add(new StatisticsEntry("statistics.total_worker", "45.000"));
		statistics.add(new StatisticsEntry("statistics.total_police", "150.000"));
		statistics.add(new StatisticsEntry("statistics.total_fleet", "6"));
		statistics.add(new StatisticsEntry("statistics.total_own_firepower", "4.025"));
		statistics.add(new StatisticsEntry("statistics.total_enemy_firepower", "19.200"));
		statistics.add(new StatisticsEntry("statistics.ships_destroyed", "60"));
		statistics.add(new StatisticsEntry("statistics.ships_destroyed_value", "125.000"));
		statistics.add(new StatisticsEntry("statistics.galaxy_money_acquired", "900.000"));
		statistics.add(new StatisticsEntry("statistics.galaxy_money_spent", "420.000"));
		statistics.add(new StatisticsEntry("statistics.galaxy_total_population", "1.000.000"));
		statistics.add(new StatisticsEntry("statistics.galaxy_total_buildings", "120"));
		statistics.add(new StatisticsEntry("statistics.galaxy_total_houses", "1.200.000 (120%)"));
		statistics.add(new StatisticsEntry("statistics.galaxy_total_energy", "1.000.000"));
		statistics.add(new StatisticsEntry("statistics.galaxy_total_energy_demand", "1.000.000 (100%)"));
		statistics.add(new StatisticsEntry("statistics.galaxy_total_worker", "700.000 (70%)"));
		statistics.add(new StatisticsEntry("statistics.galaxy_total_food", "1.100.000 (110%)"));
		statistics.add(new StatisticsEntry("statistics.galaxy_total_hospital", "1.150.000 (115%)"));
		statistics.add(new StatisticsEntry("statistics.galaxy_total_police", "1.200.000 (120%)"));
//		statistics.add(new StatisticsEntry("", ""));
	}
	@Override
	public void mouseDoubleClicked(int button, int x, int y, int modifiers) {
		// TODO Auto-generated method stub
		
	}
}
