/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import hu.openig.core.Pred3;
import hu.openig.model.AchievementProgress;
import hu.openig.model.Building;
import hu.openig.model.DiplomaticRelation;
import hu.openig.model.ExplorationMap;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchType;
import hu.openig.model.World;
import hu.openig.utils.Exceptions;

/**
 * The achievement condition tester functions repository.
 * @author akarnokd, 2011.12.31.
 */
public final class AchievementManager {
    /** The ID of the achievement. */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ID {
    }
    // -------------------------------------------------------------------------------
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> CONQUEROR = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            ap.displayProgress = false;
            ap.max = 1;
            ap.progress = u.statistics.planetsConquered.value;
            return ap.isComplete();
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> MILLIONAIRE = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            if (!t.isSkirmish()) {
                ap.displayProgress = true;
                ap.max = 1000 * 1000;
                ap.progress = u.money();
                return ap.isComplete();
            }
            return false;
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> PIRATE_BAY = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            if (!t.isSkirmish()) {
                ap.displayProgress = false;
                ap.max = 1;
                Player p = t.players.get("Pirates");
                if (p != null) {
                    ap.progress = p.statistics.planetsOwned.value;
                }
                return ap.isComplete();
            }
            return false;
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> DARGSLAYER = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            if (!t.isSkirmish()) {
                Player p = t.players.get("Dargslan");
                ap.displayProgress = false;
                ap.max = 1;
                ap.progress = p.statistics.planetsOwned.value == 0 ? 1 : 0;
                return ap.isComplete();
            }
            return false;
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> ENERGIZER = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            ap.displayProgress = true;
            ap.max = 10L * 1000 * 1000;
            ap.progress = u.statistics.totalAvailableEnergy.value;
            return ap.isComplete();
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> DEATH_STAR = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            ap.displayProgress = false;
            ap.max = 1;
            ap.progress = u.statistics.planetsDied.value;
            return ap.isComplete();
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> RESEARCH_ASSISTANT = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            ap.displayProgress = true;
            ap.max = 5;
            ap.progress = u.statistics.researchCount.value;
            return ap.isComplete();
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> SCIENTIST = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            ap.displayProgress = true;
            ap.max = 15;
            ap.progress = u.statistics.researchCount.value;
            return ap.isComplete();
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> NOBEL_PRIZE = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            int total = 0;
            int available = 0;
            for (ResearchType rt : t.researches.values()) {
                if (rt.race.contains(u.race) && rt.level < 6) {
                    total++;
                }
                if (rt.level < 6 && u.isAvailable(rt)) {
                    available++;
                }
            }
            ap.displayProgress = true;
            ap.max = total;
            ap.progress = available;
            return ap.isComplete();
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> POPULAR = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            int popular = 0;
            int owned = 0;
            for (Planet p : u.planets.keySet()) {
                if (p.owner == u) {
                    if (p.morale() >= 60) {
                        popular++;
                    }
                    owned++;
                }
            }
            ap.displayProgress = true;
            ap.max = Math.max(3, owned);
            ap.progress = popular;
            return ap.isComplete();
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> APEH = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            int popular = 0;
            int owned = 0;
            for (Planet p : u.planets.keySet()) {
                if (p.owner == u) {
                    if (p.morale() >= 95) {
                        popular++;
                    }
                    owned++;
                }
            }
            ap.displayProgress = true;
            ap.max = Math.max(3, owned);
            ap.progress = popular;
            return ap.isComplete();
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> ULTIMATE_LEADER = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            int popular = 0;
            int owned = 0;
            for (Planet p : u.planets.keySet()) {
                if (p.owner == u) {
                    if (p.morale() >= 80) {
                        popular++;
                    }
                    owned++;
                }
            }
            ap.displayProgress = true;
            ap.max = Math.max(3, owned);
            ap.progress = popular;
            return ap.isComplete();
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> REVOLUTIONER = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            ap.displayProgress = false;
            ap.max = 1;
            ap.progress = u.statistics.planetsRevolted.value;
            return ap.isComplete();
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> MASS_EFFECT = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            ap.displayProgress = true;
            ap.max = 1000 * 1000;
            ap.progress = u.statistics.totalPopulation.value;
            return ap.isComplete();
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> DEFENDER = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            return false; // Main campaign, awarded via script.
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> EMBARGO = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            return false; // Main campaign, awarded via script.
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> COLUMBUS = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            ap.displayProgress = false;
            ap.max = 1;
            ap.progress = u.statistics.planetsColonized.value;
            return ap.isComplete();
        }
    };

    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> QUARTER = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            if (!t.isSkirmish()) {
                ap.displayProgress = true;
                ap.max = 25;
                ap.progress = u.statistics.planetsOwned.value;
                return ap.isComplete();
            }
            return false;
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> MANUFACTURER = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            ap.displayProgress = true;
            ap.max = 1000;
            ap.progress = u.statistics.productionCount.value;
            return ap.isComplete();
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> SALVAGE = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            ap.displayProgress = true;
            ap.max = 1000;
            ap.progress = u.statistics.shipsDestroyed.value;
            return ap.isComplete();
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> LIVING_SPACE = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            if (!t.isSkirmish()) {
                ap.displayProgress = false;
                ap.max = 1;
                ap.progress = u.statistics.planetsDiscovered.value;
                return ap.isComplete();
            }
            return false;
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> FOOD = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            ap.displayProgress = true;
            ap.max = 1000 * 1000;
            ap.progress = u.statistics.totalAvailableFood.value;
            return ap.isComplete();
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> DECADE = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            return false; // managed by Simulator
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> OLDEST_MAN = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            return false; // managed by Simulator
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> ALL_YOUR_BASE = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            ap.displayProgress = false;
            ap.max = 1;
            ap.progress = u.statistics.planetsLostAlien.value;
            return ap.isComplete();
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> ET = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            if (t.isSkirmish()) {
                return false;
            }
            ap.displayProgress = false;
            ap.max = 1;
            for (DiplomaticRelation dr : t.relations) {
                if (dr.first.equals(u.id) || dr.second.equals(u.id)) {
                    Player otherPlayer = dr.first.equals(u.id) ? t.player(dr.second) : t.player(dr.first);
                    if (dr.full
                            && !otherPlayer.race.equals(u.race)
                            && !otherPlayer.race.equals("traders")
                            && !otherPlayer.race.equals("pirates")) {
                        ap.progress = 1;
                        return true;
                    }
                }
            }
            return false;
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> COFFEE_BREAK = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            ap.displayProgress = true;
            ap.max = 30;
            ap.progress = (t.statistics.playTime.value - t.statistics.simulationTime.value) / 60d;
            return ap.isComplete();
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> DEFENSE_CONTRACT = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            ap.displayProgress = true;
            ap.max = Math.max(3, u.statistics.planetsOwned.value);
            int good = 0;
            for (Planet p : u.planets.keySet()) {
                if (p.owner == u) {
                    int shieldCount = 0;
                    int gunCount = 0;
                    for (Building b : p.surface.buildings.iterable()) {
                        if (b.isOperational() && b.type.kind.equals("Gun")) {
                            gunCount++;
                        } else
                        if (b.isOperational() && b.type.kind.equals("Shield")) {
                            shieldCount++;
                        }
                    }
                    if (shieldCount >= 1 && gunCount >= 3) {
                        good++;
                    }
                }
            }
            ap.progress = good;
            return ap.isComplete();
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> ALL_SEEING_EYE = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            if (t.level >= 5 && !t.isSkirmish()) {
                ap.displayProgress = true;
                ap.max = 80;
                ExplorationMap xp = new ExplorationMap(u);
                int area0 = xp.map.size();
                for (Planet p : u.planets.keySet()) {
                    if (p.owner == u) {
                        xp.removeCoverage(p.x, p.y, p.radar);
                    }
                }
                ap.progress = 100 - 100 * xp.map.size() / area0;
                return ap.isComplete();
            }
            return false;
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> NEWBIE = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            if (!t.isSkirmish()) {
                ap.displayProgress = false;
                ap.max = 1;
                ap.progress = 1;
                return ap.isComplete();
            }
            return false;
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> COMMANDER = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            return false; /* t.level >= 3; */ // Main campaign, awarded via script.
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> CAPTAIN = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            return false; /* t.level >= 2; */ // Main campaign, awarded via script.
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> ADMIRAL = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            return false; /* t.level >= 4; */ // Main campaign, awarded via script.
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> GRAND_ADMIRAL = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            return false; // Main campaign, awarded via script.
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> INFLUENZA = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            return false; // Main campaign, awarded via script.
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> I_ROBOT = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            return false; // Main campaign, awarded via script.
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> DO_YOU_CHAT = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            return false; // Main campaign, awarded via script.
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> A_PIRATE_IN_NEED = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            return false; // Main campaign, awarded via script.
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> BELA_THE_4TH = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            return false; // Main campaign, awarded via script.
        }
    };
    /** Test for achievement. */
    @ID
    static final Pred3<World, Player, AchievementProgress> WELL_RESPECTED_FEATURE = new Pred3<World, Player, AchievementProgress>() {
        @Override
        public boolean invoke(World t, Player u, AchievementProgress ap) {
            ap.displayProgress = true;
            ap.max = 100;
            ap.progress = u.statistics.chats.value;
            return ap.isComplete();
        }
    };
    // -------------------------------------------------------------------------------

    /** Utility class. */
    private AchievementManager() {
        // utility class
    }
    /** The functions map for achievements. */
    static final Map<String, Pred3<World, Player, AchievementProgress>> FUNCTIONS = new HashMap<>();
    /** Locate and add achievement-tester functions. */
    static {
        for (Field f : AchievementManager.class.getDeclaredFields()) {
            if (Pred3.class.isAssignableFrom(f.getType()) && f.isAnnotationPresent(ID.class)) {
                try {
                    @SuppressWarnings("unchecked")
                    Pred3<World, Player, AchievementProgress> pred2 = (Pred3<World, Player, AchievementProgress>)f.get(null);
                    FUNCTIONS.put("achievement." + f.getName().toLowerCase(Locale.ENGLISH), pred2);
                } catch (IllegalAccessException ex) {
                    Exceptions.add(ex);
                }
            }
        }
    }
    /**
     * Returns the set of supported achievement ids.
     * @return the set of achievement ids
     */
    public static Set<String> achievements() {
        return FUNCTIONS.keySet();
    }
    /**
     * Retrieve the condition checking function for an achievement.
     * @param id the achievement id
     * @return the function
     */
    public static Pred3<World, Player, AchievementProgress> get(String id) {
        return FUNCTIONS.get(id);
    }
}
