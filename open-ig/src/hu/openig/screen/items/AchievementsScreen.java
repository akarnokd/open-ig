/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.core.Func1;
import hu.openig.model.Screens;
import hu.openig.model.SoundType;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.ClickLabel;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * The achievements and statistics listing screen.
 * @author akarnokd, 2010.01.11.
 */
public class AchievementsScreen extends ScreenBase {
	/** An achievement entry. */
	public class AchievementEntry {
		/** The achievement title label. */
		public String title;
		/** The achievement description. */
		public String description;
		/**
		 * Constructor. 
		 * @param title the title label and the achievement ID
		 * @param description the description label
		 */
		public AchievementEntry(String title, String description) {
			this.title = title;
			this.description = description;
		}
		/** @return Is the achievement available? */
		public boolean enabled() {
			return commons.profile.hasAchievement(title);
		}
	}
	/** The statistics entry. */
	public static class StatisticsEntry {
		/** The label of the statistics. */
		public String label;
		/** The value of the statistics. */
		public Func1<Void, String> value;
		/**
		 * Constructor.
		 * @param label the label
		 * @param value the value
		 */
		public StatisticsEntry(String label, Func1<Void, String> value) {
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
	/** The current display mode. */
	public Screens mode = Screens.STATISTICS;
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
		scrollUpButton.onClick = new Action0() {
			@Override
			public void invoke() {
				sound(SoundType.CLICK_HIGH_2);
				doScrollUp();
			}
		};
		
		scrollDownButton = new UIImageButton(commons.database().arrowDown);
		scrollDownButton.setHoldDelay(100);
		scrollDownButton.onClick = new Action0() {
			@Override
			public void invoke() {
				sound(SoundType.CLICK_HIGH_2);
				doScrollDown();
			}
		};
		
		// create buttons for the main screens.
		
		bridge = new UIImageButton(commons.common().bridgeButton);
		bridge.onClick = new Action0() {
			@Override
			public void invoke() {
				displayPrimary(Screens.BRIDGE);
			}
		};
		bridge.visible(false);
		starmap = new UIImageButton(commons.info().starmap);
		starmap.onClick = new Action0() {
			@Override
			public void invoke() {
				displayPrimary(Screens.STARMAP);
			}
		};
		colony = new UIImageButton(commons.info().colony);
		colony.onClick = new Action0() {
			@Override
			public void invoke() {
				displayPrimary(Screens.COLONY);
			}
		};
		
		equimpent = new UIImageButton(commons.research().equipmentButton);
		colony.onClick = new Action0() {
			@Override
			public void invoke() {
				displayPrimary(Screens.COLONY);
			}
		};

		production = new UIImageButton(commons.info().production);
		production.onClick = new Action0() {
			@Override
			public void invoke() {
				displaySecondary(Screens.PRODUCTION);
			}
		};
		
		research = new UIImageButton(commons.info().research);
		research.onClick = new Action0() {
			@Override
			public void invoke() {
				displaySecondary(Screens.RESEARCH);
			}
		};

		info = new UIImageButton(commons.common().infoButton);
		info.onClick = new Action0() {
			@Override
			public void invoke() {
				displaySecondary(Screens.INFORMATION_COLONY);
			}
		};

		diplomacy = new UIImageButton(commons.info().diplomacy);
		diplomacy.onClick = new Action0() {
			@Override
			public void invoke() {
				displaySecondary(Screens.DIPLOMACY);
			}
		};
		diplomacy.visible(false);
		
		achievementLabel = new ClickLabel("achievements", 14, commons);
		achievementLabel.onPressed = new Action0() {
			@Override
			public void invoke() {
				sound(SoundType.CLICK_MEDIUM_2);
				mode = Screens.ACHIEVEMENTS;
				adjustLabels();
				adjustScrollButtons();
			}
		};
		
		statisticsLabel = new ClickLabel("statistics", 14, commons);
		statisticsLabel.onPressed = new Action0() {
			@Override
			public void invoke() {
				sound(SoundType.CLICK_MEDIUM_2);
				mode = Screens.STATISTICS;
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
		if (!base.contains(e.x, e.y) && e.has(Type.DOWN)) {
			if (!e.within(statisticsLabel.x, statisticsLabel.y, statisticsLabel.width, statisticsLabel.height)
					&& !e.within(achievementLabel.x, achievementLabel.y, achievementLabel.width, achievementLabel.height)) {
				hideSecondary();
				return true;
			}
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
		if (mode == Screens.STATISTICS) {
			int oldIndex = statisticsIndex;
			statisticsIndex = Math.max(0, statisticsIndex - 1);
			if (oldIndex != statisticsIndex) {
				adjustScrollButtons();
			}
		} else
		if (mode == Screens.ACHIEVEMENTS) {
			int oldIndex = achievementIndex;
			achievementIndex = Math.max(0, achievementIndex - 1);
			if (oldIndex != achievementIndex) {
				adjustScrollButtons();
			}
		}
	}
	/** Scroll the list down. */
	void doScrollDown() {
		if (mode == Screens.STATISTICS) {
			if (statistics.size() > statisticsCount) {
				int oldIndex = statisticsIndex;
				statisticsIndex = Math.min(statisticsIndex + 1, statistics.size() - statisticsCount);
				if (oldIndex != statisticsIndex) {
					adjustScrollButtons();
				}
			}
		} else
		if (mode == Screens.ACHIEVEMENTS) {
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
		if (mode == Screens.STATISTICS) {
			scrollUpButton.visible(statisticsIndex > 0);
			scrollDownButton.visible(statisticsIndex < statistics.size() - statisticsCount);
		} else
		if (mode == Screens.ACHIEVEMENTS) {
			scrollUpButton.visible(achievementIndex > 0);
			scrollDownButton.visible(achievementIndex < achievements.size() - achievementCount);
		}		
		askRepaint();
	}
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#onEnter()
	 */
	@Override
	public void onEnter(Screens mode) {
		this.mode = mode == null ? Screens.STATISTICS : mode;
		onResize();
		adjustScrollButtons();
		adjustLabels();
	}
	/** Adjust label selection. */
	void adjustLabels() {
		achievementLabel.selected = mode == Screens.ACHIEVEMENTS;
		statisticsLabel.selected = mode == Screens.STATISTICS;
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
		
		Shape save0 = g2.getClip();
		g2.clipRect(listRect.x, listRect.y, listRect.width, listRect.height);
		adjustLabels();
		if (mode == Screens.ACHIEVEMENTS) {
			int y = listRect.y;
			for (int i = achievementIndex; i < achievements.size() && i < achievementIndex + achievementCount; i++) {
				AchievementEntry ae = achievements.get(i);
				String desc = get(ae.description);
				int tw = listRect.width - commons.common().achievement.getWidth() - 10;
				List<String> lines = new ArrayList<String>();
				commons.text().wrapText(desc, tw, 10, lines);
				BufferedImage img = commons.common().achievement;
				int color = 0xFF00FF00;
				if (!ae.enabled()) {
					img = commons.common().achievementGrayed;
					color = 0xFFC0C0C0;
				}
				g2.drawImage(img, listRect.x, y, null);
				commons.text().paintTo(g2, listRect.x + commons.common().achievement.getWidth() + 10, y, 14, color, get(ae.title));
				int y1 = y + 20;
				
				for (int j = 0; j < lines.size(); j++) {
					commons.text().paintTo(g2, listRect.x + commons.common().achievement.getWidth() + 10, y1, 10, color, lines.get(j));
					y1 += 12;
				}
				y += 50;
			}
		} else
		if (mode == Screens.STATISTICS) {
			int y = listRect.y;
			int h = 14;
			for (int i = statisticsIndex; i < statistics.size() && i < statisticsIndex + statisticsCount; i++) {
				StatisticsEntry se = statistics.get(i);
				int w1 = commons.text().getTextWidth(h, get(se.label));
				commons.text().paintTo(g2, listRect.x, y, h, TextRenderer.GREEN, get(se.label));
				String s = se.value.invoke(null);
				int w2 = commons.text().getTextWidth(h, s);
				
				g2.setColor(Color.GRAY);
				
				g2.drawLine(listRect.x + w1 + 5, y + 10, listRect.x + listRect.width - w2 - 10, y + 10);
				
				commons.text().paintTo(g2, listRect.x + listRect.width - w2 - 5, y, h, TextRenderer.YELLOW, s);
				
				y += 20;
			}
		}
		g2.setClip(save0);
		
	}
	/** Create the test achievements. */
	void createTestEntries() {
		achievements.add(new AchievementEntry("achievement.conqueror", "achievement.conqueror.desc"));
		achievements.add(new AchievementEntry("achievement.millionaire", "achievement.millionaire.desc"));
		achievements.add(new AchievementEntry("achievement.student_of_bokros", "achievement.student_of_bokros.desc"));
		achievements.add(new AchievementEntry("achievement.pirate_bay", "achievement.pirate_bay.desc"));
		achievements.add(new AchievementEntry("achievement.dargslayer", "achievement.dargslayer.desc"));
		achievements.add(new AchievementEntry("achievement.energizer", "achievement.energizer.desc"));
		achievements.add(new AchievementEntry("achievement.death_star", "achievement.death_star.desc"));
		achievements.add(new AchievementEntry("achievement.research_assistant", "achievement.research_assistant.desc"));
		achievements.add(new AchievementEntry("achievement.scientist", "achievement.scientist.desc"));
		achievements.add(new AchievementEntry("achievement.nobel_prize", "achievement.nobel_prize.desc"));
		achievements.add(new AchievementEntry("achievement.popular", "achievement.popular.desc"));
		achievements.add(new AchievementEntry("achievement.apeh", "achievement.apeh.desc"));
		achievements.add(new AchievementEntry("achievement.ultimate_leader", "achievement.ultimate_leader.desc"));
		achievements.add(new AchievementEntry("achievement.revolutioner", "achievement.revolutioner.desc"));
		achievements.add(new AchievementEntry("achievement.mass_effect", "achievement.mass_effect.desc"));
		achievements.add(new AchievementEntry("achievement.defender", "achievement.defender.desc"));
		achievements.add(new AchievementEntry("achievement.embargo", "achievement.embargo.desc"));
		achievements.add(new AchievementEntry("achievement.colombus", "achievement.colombus.desc"));
		achievements.add(new AchievementEntry("achievement.quarter", "achievement.quarter.desc"));
		achievements.add(new AchievementEntry("achievement.manufacturer", "achievement.manufacturer.desc"));
		achievements.add(new AchievementEntry("achievement.salvage", "achievement.salvage.desc"));
		achievements.add(new AchievementEntry("achievement.living_space", "achievement.living_space.desc"));
		achievements.add(new AchievementEntry("achievement.food", "achievement.food.desc"));
		achievements.add(new AchievementEntry("achievement.decade", "achievement.decade.desc"));
		achievements.add(new AchievementEntry("achievement.oldest_man", "achievement.oldest_man.desc"));
		achievements.add(new AchievementEntry("achievement.all_your_base", "achievement.all_your_base.desc"));
		achievements.add(new AchievementEntry("achievement.et", "achievement.et.desc"));
		achievements.add(new AchievementEntry("achievement.defense_contract", "achievement.defense_contract.desc"));
		achievements.add(new AchievementEntry("achievement.coffee_break", "achievement.coffee_break.desc"));
		achievements.add(new AchievementEntry("achievement.all_seeing_eye", "achievement.all_seeing_eye.desc"));
		achievements.add(new AchievementEntry("achievement.newbie", "achievement.newbie.desc"));
		achievements.add(new AchievementEntry("achievement.commander", "achievement.commander.desc"));
		achievements.add(new AchievementEntry("achievement.admiral", "achievement.admiral.desc"));
		achievements.add(new AchievementEntry("achievement.grand_admiral", "achievement.grand_admiral.desc"));
		achievements.add(new AchievementEntry("achievement.influenza", "achievement.influenza.desc"));
		
		statistics.add(new StatisticsEntry("statistics.total_gametime",
		new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return toTime(world().statistics.playTime);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.total_ingame_time", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return toTime(world().statistics.simulationTime);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.total_pause_time", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return toTime(world().statistics.playTime - world().statistics.simulationTime);
			}
		}
		));
		
		final DecimalFormat df = new DecimalFormat("#,###"); 
		
		statistics.add(new StatisticsEntry("statistics.money_aquired", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.moneyIncome);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.money_tax_income", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.moneyTaxIncome);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.money_trade_income", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.moneyTradeIncome);
			}
		}
		));

		statistics.add(new StatisticsEntry("statistics.money_demolish_income", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.moneyDemolishIncome);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.money_sell_income", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.moneySellIncome);
			}
		}
		));

		statistics.add(new StatisticsEntry("statistics.money_spent", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.moneySpent);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.money_spent_building", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.moneyBuilding);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.money_spent_repair", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.moneyRepair);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.money_spent_production", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.moneyProduction);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.money_spent_research", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.moneyResearch);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.money_spent_upgade", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.moneyUpgrade);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.build_count", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.buildCount);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.demolish_count", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.demolishCount);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.sell_count", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.sellCount);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.production_count", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.productionCount);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.research_count", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.researchCount);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.upgrade_count", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.upgradeCount);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.planet_own", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.planetsOwned);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.planet_discovered", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.planetsDiscovered);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.planet_colonized", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.planetsColonized);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.planet_conquered", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.planetsConquered);
			}
		}
		));
		
		statistics.add(new StatisticsEntry("statistics.total_buildings", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.totalBuilding);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.actual_buildings", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.totalAvailableBuilding);
			}
		}
		));
		
		statistics.add(new StatisticsEntry("statistics.total_population", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.totalPopulation);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.total_houses", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.totalAvailableHouse);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.total_worker", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.totalWorkerDemand);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.total_energy", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.totalAvailableEnergy);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.total_energy_demand", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.totalEnergyDemand);
			}
		}
		));

		statistics.add(new StatisticsEntry("statistics.total_food_production", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.totalAvailableFood);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.total_hospital", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.totalAvailableHospital);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.total_police", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.totalAvailablePolice);
			}
		}
		));
		
		// ************************************************************************

		statistics.add(new StatisticsEntry("statistics.galaxy_money_aquired", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.moneyIncome);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.galaxy_money_tax_income", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.moneyTaxIncome);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.galaxy_money_trade_income", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.moneyTradeIncome);
			}
		}
		));

		statistics.add(new StatisticsEntry("statistics.galaxy_money_demolish_income", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.moneyDemolishIncome);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.galaxy_money_sell_income", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.moneySellIncome);
			}
		}
		));

		statistics.add(new StatisticsEntry("statistics.galaxy_money_spent", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.moneySpent);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.galaxy_money_spent_building", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.moneyBuilding);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.galaxy_money_spent_repair", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.moneyRepair);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.galaxy_money_spent_production", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.moneyProduction);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.galaxy_money_spent_research", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.moneyResearch);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.galaxy_money_spent_upgade", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.moneyUpgrade);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.galaxy_build_count", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.buildCount);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.galaxy_demolish_count", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.demolishCount);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.galaxy_sell_count", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.sellCount);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.galaxy_production_count", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.productionCount);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.galaxy_research_count", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.researchCount);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.galaxy_upgrade_count", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.upgradeCount);
			}
		}
		));
		
		statistics.add(new StatisticsEntry("statistics.galaxy_total_buildings", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.totalBuilding);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.galaxy_actual_buildings", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.totalAvailableBuilding);
			}
		}
		));
		
		statistics.add(new StatisticsEntry("statistics.galaxy_total_population", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.totalPopulation);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.galaxy_total_houses", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.totalAvailableHouse);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.galaxy_total_worker", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.totalWorkerDemand);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.galaxy_total_energy", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.totalAvailableEnergy);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.galaxy_total_energy_demand", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.totalEnergyDemand);
			}
		}
		));

		statistics.add(new StatisticsEntry("statistics.galaxy_total_food_production", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.totalAvailableFood);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.galaxy_total_hospital", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.totalAvailableHospital);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.galaxy_total_police", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(world().statistics.totalAvailablePolice);
			}
		}
		));

	}
	/**
	 * Convert to time string.
	 * @param seconds the number of seconds.
	 * @return the string
	 */
	String toTime(long seconds) {
		long secs = seconds % 60;
		long mins = (seconds / 60) % 60;
		long hours = (seconds / 60 / 60);
		return String.format("%02d:%02d:%02d", hours, mins, secs);
	}
	@Override
	public Screens screen() {
		return mode;
	}
	@Override
	public void onEndGame() {
		// TODO Auto-generated method stub
	}
}
