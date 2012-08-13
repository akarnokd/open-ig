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
import hu.openig.core.Pair;
import hu.openig.mechanics.AchievementManager;
import hu.openig.model.Screens;
import hu.openig.model.SoundType;
import hu.openig.model.World;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.ScreenBase;
import hu.openig.screen.panels.ClickLabel;
import hu.openig.ui.UICheckBox;
import hu.openig.ui.UIGenericButton;
import hu.openig.ui.UIImage;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;
import hu.openig.utils.U;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	public final List<AchievementEntry> achievementList = new ArrayList<AchievementEntry>();
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
	final Rectangle base = new Rectangle(0, 0, 640, 442);
	/** The listing rectangle of the achievements. */
	final Rectangle listRectAch = new Rectangle();
	/** The listing rectangle of the statistics. */
	final Rectangle listRectStat = new Rectangle();
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
	/** Production button. */
	UIImage noProduction;
	/** Research button. */
	UIImage noResearch;
	/** Diplomacy button. */
	UIImage noDiplomacy;
	/** Information button. */
	UIImageButton info;
	/** Statistics label. */
	ClickLabel statisticsLabel;
	/** Achievements label. */
	ClickLabel achievementLabel;
	/** The current display mode. */
	public Screens mode = Screens.STATISTICS;
	/** Achievements label. */
	UIGenericButton backLabel;
	/** Number of achieved / total. */
	UILabel counts;
	/** Show earned. */
	UICheckBox earned;
	/** Show unearned. */
	UICheckBox unearned;
	@Override
	public void onResize() {
		scaleResize(base, margin());

		listRectAch.setBounds(base.x + 10, base.y + 45, base.width - 50, 325);
		achievementCount = listRectAch.height / 50;

		listRectStat.setBounds(base.x + 10, base.y + 20, base.width - 50, 350);
		statisticsCount = listRectStat.height / 20;
		
		scrollUpButton.x = base.x + listRectStat.width + 12;
		scrollUpButton.y = base.y + 10 + (listRectStat.height / 2 - scrollUpButton.height) / 2;
		
		scrollDownButton.x = scrollUpButton.x;
		scrollDownButton.y = base.y + 10 + listRectStat.height / 2 + (listRectStat.height / 2 - scrollDownButton.height) / 2;

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
		statisticsLabel.y = base.y - achievementLabel.height / 2 + 1;

		if (world() != null) {
			achievementLabel.x = base.x + base.width / 2 + (base.width / 2 - achievementLabel.width) / 2;
		} else {
			achievementLabel.x = base.x + (base.width - achievementLabel.width) / 2;
		}
		achievementLabel.y = base.y - achievementLabel.height / 2 + 1;

		noProduction.location(production.location());
		noResearch.location(research.location());
		noDiplomacy.location(diplomacy.location());
		
		backLabel.x = base.x + (base.width - backLabel.width) / 2;
		backLabel.y = base.y + base.height - backLabel.height - 5;

	}

	@Override
	public void onFinish() {
	}

	@Override
	public void onInitialize() {
		
		base.setSize(commons.common().infoEmpty.getWidth(), commons.common().infoEmpty.getHeight());
		achievementList.clear();
		statistics.clear();
		
		scrollUpButton = new UIImageButton(commons.database().arrowUp);
		scrollUpButton.setHoldDelay(100);
		scrollUpButton.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_HIGH_2);
				doScrollUp();
			}
		};
		
		scrollDownButton = new UIImageButton(commons.database().arrowDown);
		scrollDownButton.setHoldDelay(100);
		scrollDownButton.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_HIGH_2);
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
		equimpent.onClick = new Action0() {
			@Override
			public void invoke() {
				displaySecondary(Screens.EQUIPMENT);
			}
		};

		production = new UIImageButton(commons.info().production);
		production.onClick = new Action0() {
			@Override
			public void invoke() {
				displaySecondary(Screens.PRODUCTION);
			}
		};
		
		noProduction = new UIImage(commons.common().emptyButton);
		noResearch = new UIImage(commons.common().emptyButton);
		noDiplomacy = new UIImage(commons.common().emptyButton);

		
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
				buttonSound(SoundType.CLICK_MEDIUM_2);
				mode = Screens.ACHIEVEMENTS;
				adjustLabels();
				adjustScrollButtons();
			}
		};
		
		statisticsLabel = new ClickLabel("statistics", 14, commons);
		statisticsLabel.onPressed = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				mode = Screens.STATISTICS;
				adjustLabels();
				adjustScrollButtons();
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
				hideSecondary();
			}
		};

		counts = new UILabel("", 14, commons.text());
		earned = new UICheckBox(get("achievements.show_earned"), 14, commons.common().checkmark, commons.text());
		earned.selected(true);
		earned.onChange = new Action0() {
			@Override
			public void invoke() {
				adjustScrollButtons();				
			}
		};
		unearned = new UICheckBox(get("achievements.show_unearned"), 14, commons.common().checkmark, commons.text());
		unearned.selected(true);
		unearned.onChange = new Action0() {
			@Override
			public void invoke() {
				adjustScrollButtons();				
			}
		};

		addThis();
		// FIXME find other ways to populate the achievement list
		createTestEntries();
	}

	@Override
	public boolean mouse(UIMouse e) {
		scaleMouse(e, base, margin());
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
			Rectangle r = mode == Screens.ACHIEVEMENTS ? listRectAch : listRectStat;
			if (r.contains(e.x, e.y)) {
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
			List<AchievementEntry> achList = achievements();
			if (achList.size() > achievementCount) {
				int oldIndex = achievementIndex;
				achievementIndex = Math.min(achievementIndex + 1, achList.size() - achievementCount);
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
			achievementIndex = Math.min(achievementIndex, achievements().size() - achievementCount);
			scrollUpButton.visible(achievementIndex > 0);
			List<AchievementEntry> achList = achievements();
			scrollDownButton.visible(achievementIndex < achList.size() - achievementCount);
		}		
		askRepaint();
	}
	@Override
	public void onEnter(Screens mode) {
		this.mode = mode == null ? Screens.STATISTICS : mode;
		onResize();
		adjustScrollButtons();
		adjustLabels();
		
		World w = world();
		
		production.visible(w != null && w.level > 1);
		research.visible(w != null && w.level > 2);
		
		noProduction.visible(w != null && !production.visible());
		noResearch.visible(w != null && !research.visible());
		/*
		diplomacy.visible(world().level > 3);
		noDiplomacy.visible(!diplomacy.visible());
		*/
		diplomacy.visible(false);
		noDiplomacy.visible(false);
		
		starmap.visible(w != null);
		colony.visible(w != null);
		equimpent.visible(w != null);
		info.visible(w != null);
		statisticsLabel.visible(w != null);
		backLabel.visible(w == null);
	}
	/** Adjust label selection. */
	void adjustLabels() {
		achievementLabel.selected = mode == Screens.ACHIEVEMENTS;
		statisticsLabel.selected = mode == Screens.STATISTICS;
	}
	@Override
	public void onLeave() {

	}

	@Override
	public void draw(Graphics2D g2) {
		AffineTransform savea = scaleDraw(g2, base, margin());
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);
		
		g2.drawImage(commons.common().infoEmpty, base.x, base.y, null);
		
		Shape save0 = g2.getClip();
		adjustLabels();
		boolean ach = mode == Screens.ACHIEVEMENTS;
		Rectangle r = ach ? listRectAch : listRectStat;
		g2.clipRect(r.x, r.y, r.width, r.height);

		
		counts.visible(ach);
		earned.visible(ach);
		unearned.visible(ach);
		
		if (ach) {
			List<AchievementEntry> achList = achievements();
			int total = achievementList.size();
			int act = 0;
			for (AchievementEntry e : achievementList) {
				if (e.enabled()) {
					act++;
				}
			}
			counts.text(format("achievements.counts", act, total, act * 100d / total), true);
			counts.location(r.x, r.y - 25);
			earned.location(counts.x + counts.width + 25, counts.y);
			unearned.location(earned.x + earned.width + 25, earned.y);
			
			int y = r.y;
			for (int i = achievementIndex; i < achList.size() && i < achievementIndex + achievementCount; i++) {
				AchievementEntry ae = achList.get(i);
				String desc = get(ae.description);
				int tw = r.width - commons.common().achievement.getWidth() - 10;
				List<String> lines = new ArrayList<String>();
				commons.text().wrapText(desc, tw, 10, lines);
				BufferedImage img = commons.common().achievement;
				int color = 0xFF00FF00;
				if (!ae.enabled()) {
					img = commons.common().achievementGrayed;
					color = 0xFFC0C0C0;
				}
				g2.drawImage(img, r.x, y, null);
				commons.text().paintTo(g2, r.x + commons.common().achievement.getWidth() + 10, y, 14, color, get(ae.title));
				int y1 = y + 20;
				
				for (int j = 0; j < lines.size(); j++) {
					commons.text().paintTo(g2, r.x + commons.common().achievement.getWidth() + 10, y1, 10, color, lines.get(j));
					y1 += 12;
				}
				y += 50;
			}
		} else
		if (mode == Screens.STATISTICS) {
			int y = r.y;
			int h = 14;
			for (int i = statisticsIndex; i < statistics.size() && i < statisticsIndex + statisticsCount; i++) {
				StatisticsEntry se = statistics.get(i);
				int w1 = commons.text().getTextWidth(h, get(se.label));
				commons.text().paintTo(g2, r.x, y, h, TextRenderer.GREEN, get(se.label));
				String s = se.value.invoke(null);
				int w2 = commons.text().getTextWidth(h, s);
				
				g2.setColor(Color.GRAY);
				
				g2.drawLine(r.x + w1 + 5, y + 10, r.x + r.width - w2 - 10, y + 10);
				
				commons.text().paintTo(g2, r.x + r.width - w2 - 5, y, h, TextRenderer.YELLOW, s);
				
				y += 20;
			}
		}
		g2.setClip(save0);
		g2.setTransform(savea);
		super.draw(g2);
	}
	/** Create the test achievements. */
	void createTestEntries() {
		for (String ac : AchievementManager.achievements()) {
			achievementList.add(new AchievementEntry(ac, ac + ".desc"));
		}
		Collections.sort(achievementList, new Comparator<AchievementEntry>() {
			@Override
			public int compare(AchievementEntry o1, AchievementEntry o2) {
				return get(o1.title).compareTo(get(o2.title));
			}
		});
		
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
		statistics.add(new StatisticsEntry("statistics.planet_lost", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.planetsLost);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.planet_lost_alien", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.planetsLostAlien);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.planet_revolted", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.planetsRevolted);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.planet_died", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.planetsDied);
			}
		}
		));

		// ------------------------------------------------------------------
		statistics.add(new StatisticsEntry("statistics.space_battles", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.spaceBattles);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.space_wins", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.spaceWins);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.space_loses", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.spaceLoses);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.space_retreats", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.spaceRetreats);
			}
		}
		));

		statistics.add(new StatisticsEntry("statistics.ground_battles", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.groundBattles);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.ground_wins", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.spaceWins);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.ground_loses", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.spaceLoses);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.fleets_created", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.fleetsCreated);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.fleets_lost", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.fleetsLost);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.ships_destroyed", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.shipsDestroyed);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.ships_destroyed_cost", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.shipsDestroyedCost) + " cr";
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.ships_lost", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.shipsLost);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.ships_lost_cost", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.shipsLostCost) + " cr";
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.buildings_destroyed", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.buildingsDestroyed);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.buildings_destroyed_cost", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.buildingsDestroyedCost) + " cr";
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.buildings_lost", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.buildingsLost);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.buildings_lost_cost", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.buildingsLostCost) + " cr";
			}
		}
		));

		statistics.add(new StatisticsEntry("statistics.vehicles_destroyed", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.vehiclesDestroyed);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.vehicles_destroyed_cost", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.vehiclesDestroyedCost) + " cr";
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.vehicles_lost", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.vehiclesLost);
			}
		}
		));
		statistics.add(new StatisticsEntry("statistics.vehicles_lost_cost", 
				new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return df.format(player().statistics.vehiclesLostCost) + " cr";
			}
		}
		));

		// ------------------------------------------------------------------

		
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
	@Override
	public boolean keyboard(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE && world() == null) {
			hideSecondary();
			e.consume();
			return true;
		}
		return false;
	}
	@Override
	protected int margin() {
		return 11;
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
	/**
	 * Get a potentially filtered list of achievements.
	 * @return the list of achievements to display
	 */
	protected List<AchievementEntry> achievements() {
		List<AchievementEntry> result = U.newArrayList();
		for (AchievementEntry e : achievementList) {
			if ((e.enabled() && earned.selected()) || (!e.enabled() && unearned.selected())) {
				result.add(e);
			}
		}
		return result;
	}
}
