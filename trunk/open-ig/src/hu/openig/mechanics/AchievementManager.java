/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Pred2;
import hu.openig.model.Building;
import hu.openig.model.DiplomaticRelation;
import hu.openig.model.ExplorationMap;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchType;
import hu.openig.model.World;
import hu.openig.utils.U;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

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
	protected static final Pred2<World, Player> CONQUEROR = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return u.statistics.planetsConquered > 0;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> MILLIONAIRE = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return u.money >= 1000000;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> PIRATE_BAY = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			Player p = t.players.get("Pirates");
			if (p != null && p.statistics.planetsOwned > 0) {
				return true;
			}
			return false;
		}
	};
	/** Test for achievement. */
	/*
	@ID
	protected static final Pred2<World, Player> STUDENT_OF_BOKROS = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return false;
		}
	};
	*/
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> DARGSLAYER = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			Player p = t.players.get("Dargslan");
			if (p != null && p.statistics.planetsOwned == 0) {
				return true;
			}
			return false;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> ENERGIZER = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return u.statistics.totalAvailableEnergy >= 10000000;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> DEATH_STAR = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return u.statistics.planetsDied > 0;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> RESEARCH_ASSISTANT = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return u.statistics.researchCount >= 5;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> SCIENTIST = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return u.statistics.researchCount >= 15;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> NOBEL_PRIZE = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			int total = 0;
			int available = 0;
			for (ResearchType rt : t.researches.values()) {
				if (rt.race.contains(u.race) && rt.level < 6) {
					total++;
				}
				if (u.isAvailable(rt)) {
					available++;
				}
			}
			return available >= total;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> POPULAR = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			int popular = 0;
			for (Planet p : u.planets.keySet()) {
				if (p.owner == u && p.morale >= 60) {
					popular++;
				}
			}
			return popular >= u.statistics.planetsOwned && u.statistics.planetsOwned >= 3;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> APEH = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			int popular = 0;
			for (Planet p : u.planets.keySet()) {
				if (p.owner == u && p.morale >= 95) {
					popular++;
				}
			}
			return popular >= u.statistics.planetsOwned && u.statistics.planetsOwned >= 3;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> ULTIMATE_LEADER = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			int popular = 0;
			for (Planet p : u.planets.keySet()) {
				if (p.owner == u && p.morale >= 80) {
					popular++;
				}
			}
			return popular >= u.statistics.planetsOwned && u.statistics.planetsOwned >= 3;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> REVOLUTIONER = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return u.statistics.planetsRevolted > 0;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> MASS_EFFECT = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return u.statistics.totalPopulation >= 1000000;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> DEFENDER = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return false;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> EMBARGO = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return false;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> COLUMBUS = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return u.statistics.planetsColonized > 0;
		}
	};

	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> QUARTER = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return u.statistics.planetsOwned >= 25;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> MANUFACTURER = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return u.statistics.productionCount >= 1000;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> SALVAGE = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return false; // TODO count!!!
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> LIVING_SPACE = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return u.statistics.planetsDiscovered > 0;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> FOOD = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return u.statistics.totalAvailableFood > 1000000;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> DECADE = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return t.time.getTimeInMillis() - t.initialDate.getTime() >= 10 * 365.2425 * 24 * 60 * 60 * 1000;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> OLDEST_MAN = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return t.time.getTimeInMillis() - t.initialDate.getTime() >= 100 * 365.2425 * 24 * 60 * 60 * 1000;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> ALL_YOUR_BASE = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return u.statistics.planetsLostAlien > 0;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> ET = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			for (DiplomaticRelation dr : t.relations) {
				Player p = dr.second;
				if (dr.first == u && !p.race.equals(u.race) 
						&& !p.race.equals("traders") 
						&& !p.race.equals("pirates")) {
					return true;
				}
			}
			return false;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> COFFEE_BREAK = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return t.statistics.playTime - t.statistics.simulationTime >= 30 * 60;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> DEFENSE_CONTRACT = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			for (Planet p : u.planets.keySet()) {
				if (p.owner == u) {
					int shieldCount = 0;
					int gunCount = 0;
					for (Building b : p.surface.buildings) {
						if (b.isOperational() && b.type.kind.equals("Gun")) {
							gunCount++;
						} else
						if (b.isOperational() && b.type.kind.equals("Shield")) {
							shieldCount++;
						}
					}
					if (shieldCount < 1 || gunCount < 3) {
						return false;
					}
				}
			}
			return u.statistics.planetsOwned >= 3;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> ALL_SEEING_EYE = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			if (t.level >= 5) {
				ExplorationMap xp = new ExplorationMap(u);
				int area0 = xp.map.size();
				for (Planet p : u.planets.keySet()) {
					if (p.owner == u) {
						xp.removeCoverage(p.x, p.y, p.radar);
					}
				}
				return xp.map.size() <= area0 / 5;
			}
			return false;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> NEWBIE = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return true;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> COMMANDER = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return false; /* t.level >= 3; */
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> CAPTAIN = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return false; /* t.level >= 2; */
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> ADMIRAL = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return false; /* t.level >= 4; */
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> GRAND_ADMIRAL = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return false;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> INFLUENZA = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return false;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> I_ROBOT = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return false;
		}
	};
	/** Test for achievement. */
	@ID
	protected static final Pred2<World, Player> DO_YOU_CHAT = new Pred2<World, Player>() {
		@Override
		public Boolean invoke(World t, Player u) {
			return false;
		}
	};
	// -------------------------------------------------------------------------------	
	/** Utility class. */
	private AchievementManager() {
		// utility class
	}
	/** The functions map for achievements. */
	protected static final Map<String, Pred2<World, Player>> FUNCTIONS = U.newHashMap();
	static {
		for (Field f : AchievementManager.class.getDeclaredFields()) {
			if (Pred2.class.isAssignableFrom(f.getType()) && f.isAnnotationPresent(ID.class)) {
				try {
					@SuppressWarnings("unchecked") Pred2<World, Player> pred2 = (Pred2<World, Player>)f.get(null);
					FUNCTIONS.put("achievement." + f.getName().toLowerCase(), pred2);
				} catch (IllegalAccessException ex) {
					ex.printStackTrace();
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
	public static Pred2<World, Player> get(String id) {
		return FUNCTIONS.get(id);
	}
}
