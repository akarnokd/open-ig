/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

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

import hu.openig.core.Action0;
import hu.openig.core.Func1;
import hu.openig.core.Pair;
import hu.openig.mechanics.AchievementManager;
import hu.openig.model.AchievementProgress;
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
    public final List<AchievementEntry> achievementList = new ArrayList<>();
    /** The list of statistics. */
    public final List<StatisticsEntry> statistics = new ArrayList<>();
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
    UIImageButton equipment;
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

        listRectAch.setBounds(base.x + 10, base.y + 45, base.width - 50, 335);
        achievementCount = listRectAch.height / 66;

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
        equipment.x = colony.x + colony.width;
        equipment.y = bridge.y;
        production.x = equipment.x + equipment.width;
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

        base.setSize(
                commons.common().infoEmptyTop.getWidth(),

                commons.common().infoEmptyLeft.getHeight());
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

        equipment = new UIImageButton(commons.research().equipmentButton);
        equipment.onClick = new Action0() {
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

    /** Scroll the list up. */
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
                statisticsIndex = Math.max(0, Math.min(statisticsIndex + 1, statistics.size() - statisticsCount));
                if (oldIndex != statisticsIndex) {
                    adjustScrollButtons();
                }
            }
        } else
        if (mode == Screens.ACHIEVEMENTS) {
            List<AchievementEntry> achList = achievements();
            if (achList.size() > achievementCount) {
                int oldIndex = achievementIndex;
                achievementIndex = Math.max(Math.min(achievementIndex + 1, achList.size() - achievementCount), 0);
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
            achievementIndex = Math.max(0, Math.min(achievementIndex, achievements().size() - achievementCount));
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

        statisticsIndex = Math.max(0, statisticsIndex);
        achievementIndex = Math.max(0, achievementIndex);

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
        equipment.visible(w != null);
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

        commons.common().drawInfoEmpty(g2, base.x, base.y);

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

            int iconWidth = commons.common().achievement.getWidth();
            int y = r.y;
            for (int i = achievementIndex; i < achList.size() && i < achievementIndex + achievementCount; i++) {
                AchievementEntry ae = achList.get(i);
                String desc = get(ae.description);
                int tw = r.width - iconWidth - 10;
                List<String> lines = new ArrayList<>();
                commons.text().wrapText(desc, tw, 10, lines);
                BufferedImage img = commons.common().achievement;
                int color = 0xFF00FF00;
                if (!ae.enabled()) {
                    img = commons.common().achievementGrayed;
                    color = 0xFFC0C0C0;
                }
                g2.drawImage(img, r.x, y, null);
                commons.text().paintTo(g2, r.x + iconWidth + 10, y, 14, color, get(ae.title));
                int y1 = y + 20;

                for (String line : lines) {
                    commons.text().paintTo(g2, r.x + iconWidth + 10, y1, 10, color, line);
                    y1 += 12;
                }

                AchievementProgress ap = commons.profile().getProgress(ae.title);
                if (ap != null && ap.displayProgress && ap.max > 0) {
                    y1++;
                    int pw = tw - 20;
                    g2.setColor(Color.GRAY);
                    g2.fillRect(r.x + iconWidth + 20, y1, (int)(pw * Math.min(ap.max, ap.progress) / ap.max), 14);
                    g2.setColor(Color.GRAY);
                    g2.drawRect(r.x + iconWidth + 20, y1, pw, 14);

                    String progressStr = String.format("%,.0f / %,.0f  (%.1f%%)", ap.progress, ap.max, ap.progress * 100 / ap.max);
                    int progressWidth = commons.text().getTextWidth(10, progressStr);
                    commons.text().paintTo(g2, r.x + iconWidth + 20 + (pw - progressWidth) / 2, y1 + 2, 10, 0xFFFFFFFF, progressStr);
                    y1 += 16;
                }

                y += 66;
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

        super.draw(g2);

        g2.setTransform(savea);

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
                return toTime(world().statistics.playTime.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.total_ingame_time",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return toTime(world().statistics.simulationTime.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.total_pause_time",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return toTime(world().statistics.playTime.value - world().statistics.simulationTime.value);
            }
        }
        ));

        final DecimalFormat df = new DecimalFormat("#,###");

        statistics.add(new StatisticsEntry("statistics.money_aquired",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.moneyIncome.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.money_tax_income",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.moneyTaxIncome.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.money_trade_income",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.moneyTradeIncome.value);
            }
        }
        ));

        statistics.add(new StatisticsEntry("statistics.money_demolish_income",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.moneyDemolishIncome.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.money_sell_income",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.moneySellIncome.value);
            }
        }
        ));

        statistics.add(new StatisticsEntry("statistics.money_spent",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.moneySpent.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.money_spent_building",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.moneyBuilding.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.money_spent_repair",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.moneyRepair.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.money_spent_production",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.moneyProduction.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.money_spent_research",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.moneyResearch.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.money_spent_upgade",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.moneyUpgrade.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.build_count",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.buildCount.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.demolish_count",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.demolishCount.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.sell_count",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.sellCount.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.production_count",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.productionCount.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.research_count",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.researchCount.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.upgrade_count",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.upgradeCount.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.planet_own",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.planetsOwned.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.planet_discovered",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.planetsDiscovered.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.planet_colonized",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.planetsColonized.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.planet_conquered",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.planetsConquered.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.planet_lost",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.planetsLost.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.planet_lost_alien",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.planetsLostAlien.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.planet_revolted",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.planetsRevolted.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.planet_died",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.planetsDied.value);
            }
        }
        ));

        // ------------------------------------------------------------------
        statistics.add(new StatisticsEntry("statistics.space_battles",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.spaceBattles.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.space_wins",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.spaceWins.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.space_loses",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.spaceLoses.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.space_retreats",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.spaceRetreats.value);
            }
        }
        ));

        statistics.add(new StatisticsEntry("statistics.ground_battles",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.groundBattles.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.ground_wins",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.spaceWins.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.ground_loses",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.spaceLoses.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.fleets_created",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.fleetsCreated.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.fleets_lost",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.fleetsLost.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.ships_destroyed",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.shipsDestroyed.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.ships_destroyed_cost",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.shipsDestroyedCost.value) + " cr";
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.ships_lost",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.shipsLost.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.ships_lost_cost",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.shipsLostCost.value) + " cr";
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.buildings_destroyed",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.buildingsDestroyed.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.buildings_destroyed_cost",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.buildingsDestroyedCost.value) + " cr";
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.buildings_lost",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.buildingsLost.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.buildings_lost_cost",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.buildingsLostCost.value) + " cr";
            }
        }
        ));

        statistics.add(new StatisticsEntry("statistics.vehicles_destroyed",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.vehiclesDestroyed.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.vehicles_destroyed_cost",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.vehiclesDestroyedCost.value) + " cr";
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.vehicles_lost",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.vehiclesLost.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.vehicles_lost_cost",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.vehiclesLostCost.value) + " cr";
            }
        }
        ));

        // ------------------------------------------------------------------

        statistics.add(new StatisticsEntry("statistics.total_buildings",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.totalBuilding.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.actual_buildings",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.totalAvailableBuilding.value);
            }
        }
        ));

        statistics.add(new StatisticsEntry("statistics.total_population",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.totalPopulation.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.total_houses",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.totalAvailableHouse.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.total_worker",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.totalWorkerDemand.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.total_energy",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.totalAvailableEnergy.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.total_energy_demand",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.totalEnergyDemand.value);
            }
        }
        ));

        statistics.add(new StatisticsEntry("statistics.total_food_production",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.totalAvailableFood.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.total_hospital",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.totalAvailableHospital.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.total_police",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(player().statistics.totalAvailablePolice.value);
            }
        }
        ));

        // ************************************************************************

        statistics.add(new StatisticsEntry("statistics.galaxy_money_aquired",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.moneyIncome.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.galaxy_money_tax_income",

                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.moneyTaxIncome.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.galaxy_money_trade_income",
                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.moneyTradeIncome.value);
            }
        }
        ));

        statistics.add(new StatisticsEntry("statistics.galaxy_money_demolish_income",
                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.moneyDemolishIncome.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.galaxy_money_sell_income",
                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.moneySellIncome.value);
            }
        }
        ));

        statistics.add(new StatisticsEntry("statistics.galaxy_money_spent",
                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.moneySpent.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.galaxy_money_spent_building",
                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.moneyBuilding.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.galaxy_money_spent_repair",
                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.moneyRepair.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.galaxy_money_spent_production",
                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.moneyProduction.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.galaxy_money_spent_research",
                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.moneyResearch.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.galaxy_money_spent_upgade",
                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.moneyUpgrade.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.galaxy_build_count",
                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.buildCount.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.galaxy_demolish_count",
                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.demolishCount.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.galaxy_sell_count",
                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.sellCount.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.galaxy_production_count",
                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.productionCount.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.galaxy_research_count",
                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.researchCount.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.galaxy_upgrade_count",
                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.upgradeCount.value);
            }
        }
        ));

        statistics.add(new StatisticsEntry("statistics.galaxy_total_buildings",
                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.totalBuilding.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.galaxy_actual_buildings",
                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.totalAvailableBuilding.value);
            }
        }
        ));

        statistics.add(new StatisticsEntry("statistics.galaxy_total_population",
                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.totalPopulation.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.galaxy_total_houses",
                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.totalAvailableHouse.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.galaxy_total_worker",
                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.totalWorkerDemand.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.galaxy_total_energy",
                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.totalAvailableEnergy.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.galaxy_total_energy_demand",
                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.totalEnergyDemand.value);
            }
        }
        ));

        statistics.add(new StatisticsEntry("statistics.galaxy_total_food_production",
                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.totalAvailableFood.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.galaxy_total_hospital",
                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.totalAvailableHospital.value);
            }
        }
        ));
        statistics.add(new StatisticsEntry("statistics.galaxy_total_police",
                new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return df.format(world().statistics.totalAvailablePolice.value);
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
        List<AchievementEntry> result = new ArrayList<>();
        for (AchievementEntry e : achievementList) {
            if ((e.enabled() && earned.selected()) || (!e.enabled() && unearned.selected())) {
                result.add(e);
            }
        }
        return result;
    }
}
