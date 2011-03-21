/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import hu.openig.core.Act;
import hu.openig.render.RenderTools;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The achievements and statistics listing screen.
 * @author akarnokd, 2010.01.11.
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
	final Rectangle base = new Rectangle();
	/** The listing rectangle. */
	final Rectangle listRect = new Rectangle();
	/** Scroll up button. */
	UIImageButton scrollUpButton;
	/** Scroll down button. */
	UIImageButton scrollDownButton;
	/** Bridge button. */
	UIImageButton bridge;
	/** Starmap button. */
	UIImageButton starmap;
	/** Colony button. */
	UIImageButton colony;
	/** Equipment button. */
	UIImageButton equimpent;
	/** Production button. */
	UIImageButton production;
	/** Research button. */
	UIImageButton research;
	/** Diplomacy button. */
	UIImageButton diplomacy;
	/** Information button. */
	UIImageButton info;
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
	public void onResize() {
		RenderTools.centerScreen(base, getInnerWidth(), getInnerHeight(), true);

		listRect.setBounds(base.x + 10, base.y + 20, base.width - 50, 350);
		achievementCount = listRect.height / 50;
		statisticsCount = listRect.height / 20;
		
		scrollUpButton.x = base.x + listRect.width + 12;
		scrollUpButton.y = base.y + 10 + (listRect.height / 2 - scrollUpButton.height) / 2;
		
		scrollDownButton.x = scrollUpButton.x;
		scrollDownButton.y = base.y + 10 + listRect.height / 2 + (listRect.height / 2 - scrollDownButton.height) / 2;

		bridge.x = base.x + 4 - bridge.width;
		bridge.y = base.y + base.height - 2 - bridge.height;
		starmap.x = bridge.x + bridge.width;
		starmap.y = bridge.y;
		colony.x = starmap.x + starmap.width;
		colony.y = bridge.y;
		equimpent.x = colony.x + colony.width;
		equimpent.y = bridge.y;
		production.x = equimpent.x + equimpent.width;
		production.y = bridge.y;
		research.x = production.x + production.width;
		research.y = bridge.y;
		info.x = research.x + research.width;
		info.y = production.y;
		diplomacy.x = info.x + info.width;
		diplomacy.y = production.y;

		statisticsLabel.x = base.x + (base.width / 2 - achievementLabel.width) / 2;
		statisticsLabel.y = base.y - achievementLabel.height / 2;
		
		achievementLabel.x = base.x + base.width / 2 + (base.width / 2 - statisticsLabel.width) / 2;
		achievementLabel.y = base.y - statisticsLabel.height / 2;
		
	}

	@Override
	public void onFinish() {
	}

	@Override
	public void onInitialize() {
		
		base.setSize(commons.common().infoEmpty.getWidth(), commons.common().infoEmpty.getHeight());
		achievements.clear();
		statistics.clear();
		
		scrollUpButton = new UIImageButton(commons.database().arrowUp);
		scrollUpButton.setHoldDelay(100);
		scrollUpButton.onClick = new Act() {
			@Override
			public void act() {
				doScrollUp();
			}
		};
		
		scrollDownButton = new UIImageButton(commons.database().arrowDown);
		scrollDownButton.setHoldDelay(100);
		scrollDownButton.onClick = new Act() {
			@Override
			public void act() {
				doScrollDown();
			}
		};
		
		// create buttons for the main screens.
		
		bridge = new UIImageButton(commons.common().bridgeButton);
		bridge.onClick = new Act() {
			@Override
			public void act() {
				commons.control.displayPrimary(Screens.BRIDGE);
			}
		};
		bridge.visible(false);
		starmap = new UIImageButton(commons.info().starmap);
		starmap.onClick = new Act() {
			@Override
			public void act() {
				commons.control.displayPrimary(Screens.STARMAP);
			}
		};
		colony = new UIImageButton(commons.info().colony);
		colony.onClick = new Act() {
			@Override
			public void act() {
				commons.control.displayPrimary(Screens.COLONY);
			}
		};
		
		equimpent = new UIImageButton(commons.research().equipmentButton);
		colony.onClick = new Act() {
			@Override
			public void act() {
				commons.control.displayPrimary(Screens.COLONY);
			}
		};

		production = new UIImageButton(commons.info().production);
		production.onClick = new Act() {
			@Override
			public void act() {
				commons.control.displaySecondary(Screens.PRODUCTION);
			}
		};
		
		research = new UIImageButton(commons.info().research);
		research.onClick = new Act() {
			@Override
			public void act() {
				commons.control.displaySecondary(Screens.RESEARCH);
			}
		};

		info = new UIImageButton(commons.common().infoButton);
		info.onClick = new Act() {
			@Override
			public void act() {
				commons.control.displaySecondary(Screens.INFORMATION);
			}
		};

		diplomacy = new UIImageButton(commons.info().diplomacy);
		diplomacy.onClick = new Act() {
			@Override
			public void act() {
				commons.control.displaySecondary(Screens.DIPLOMACY);
			}
		};
		diplomacy.visible(false);
		
		achievementLabel = new ClickLabel("achievements", 14, commons);
		achievementLabel.onPressed = new Act() {
			@Override
			public void act() {
				mode = Mode.ACHIEVEMENTS;
				adjustLabels();
				adjustScrollButtons();
			}
		};
		
		statisticsLabel = new ClickLabel("statistics", 14, commons);
		statisticsLabel.onPressed = new Act() {
			@Override
			public void act() {
				mode = Mode.STATISTICS;
				adjustLabels();
				adjustScrollButtons();
			}
		};
		

		addThis();
		// FIXME find other ways to populate the achievement list
		createTestEntries();
	}

	@Override
	public boolean mouse(UIMouse e) {
		if (!base.contains(e.x, e.y) && e.has(Type.UP)) {
			commons.control.hideSecondary();
			return true;
		}
		boolean result = false;
		switch (e.type) {
		case WHEEL:
			if (listRect.contains(e.x, e.y)) {
				if (e.z < 0) {
					doScrollUp();
				} else {
					doScrollDown();
				}
			}
			break;
		default:
			super.mouse(e);
		}
		return result;
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
			scrollUpButton.visible(statisticsIndex > 0);
			scrollDownButton.visible(statisticsIndex < statistics.size() - statisticsCount);
		} else
		if (mode == Mode.ACHIEVEMENTS) {
			scrollUpButton.visible(achievementIndex > 0);
			scrollDownButton.visible(achievementIndex < achievements.size() - achievementCount);
		}		
		askRepaint();
	}
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#onEnter()
	 */
	@Override
	public void onEnter(Object mode) {
		this.mode = mode == null ? Mode.STATISTICS : (Mode)mode;
		onResize();
		adjustScrollButtons();
		adjustLabels();
	}
	/** Adjust label selection. */
	void adjustLabels() {
		achievementLabel.selected = mode == Mode.ACHIEVEMENTS;
		statisticsLabel.selected = mode == Mode.STATISTICS;
	}
	@Override
	public void onLeave() {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(Graphics2D g2) {
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);
		
		g2.drawImage(commons.common().infoEmpty, base.x, base.y, null);
		super.draw(g2);
		
		Shape sp = g2.getClip();
		g2.setClip(listRect);
		adjustLabels();
		if (mode == Mode.ACHIEVEMENTS) {
			int y = listRect.y;
			for (int i = achievementIndex; i < achievements.size() && i < achievementIndex + achievementCount; i++) {
				AchievementEntry ae = achievements.get(i);
				String desc = commons.labels().get(ae.description);
				int tw = listRect.width - commons.common().achievement.getWidth() - 10;
				List<String> lines = new ArrayList<String>();
				commons.text().wrapText(desc, tw, 10, lines);
				BufferedImage img = commons.common().achievement;
				int color = 0xFF00FF00;
				if (!ae.enabled) {
					img = commons.common().achievementGrayed;
					color = 0xFFC0C0C0;
				}
				g2.drawImage(img, listRect.x, y, null);
				commons.text().paintTo(g2, listRect.x + commons.common().achievement.getWidth() + 10, y, 14, color, commons.labels().get(ae.title));
				int y1 = y + 20;
				for (int j = 0; j < lines.size(); j++) {
					commons.text().paintTo(g2, listRect.x + commons.common().achievement.getWidth() + 10, y1, 10, color, lines.get(j));
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
				commons.text().paintTo(g2, listRect.x, y, h, 0xFF80FF80, commons.labels().get(se.label));
				int w2 = commons.text().getTextWidth(h, se.value);
				commons.text().paintTo(g2, listRect.x + listRect.width - w2 - 5, y, h, 0xFF8080FF, se.value);
				
				y += 20;
			}
		}
		g2.setClip(sp);
		
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
}
