/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Difficulty;
import hu.openig.core.Pair;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class representing the world for an AI player, copying the world state to allow a thread-safe evaluation.
 * @author akarnokd, 2011.12.08.
 */
public class AIWorld {
	/** The player of this world. */
	public Player player;
	/** The player's current money. */
	public long money;
	/** The global planet statistics. */
	public PlanetStatistics global;
	/** The precomputed planet statistics. */
	public final Map<Planet, PlanetStatistics> planetStatistics = new HashMap<>();
	/** The precomputed planet statistics. */
	public final Map<Fleet, FleetStatistics> fleetStatistics = new HashMap<>();
	/** The active productions. */
	public final Map<ResearchType, Production> productions = new HashMap<>();
	/** The active researches. */
	public final Map<ResearchType, Research> researches = new HashMap<>();
	/** The currently running research. */
	public ResearchType runningResearch;
	/** The current inventory level. */
	public final Map<ResearchType, Integer> inventory = new HashMap<>();
	/** The set of available researches. */
	public final Set<ResearchType> availableResearch = new HashSet<>();
	/** The set of remaining research. */
	public final Set<ResearchType> remainingResearch = new HashSet<>();
	/** The set of researches which can be developed in the current level. */
	public final Set<ResearchType> furtherResearch = new HashSet<>();
	/** The list of known other players. */
	public final List<Player> players = new LinkedList<>();
	/** The list of own fleets. */
	public final List<AIFleet> ownFleets = new LinkedList<>();
	/** The list of own planets. */
	public final List<AIPlanet> ownPlanets = new LinkedList<>();
	/** List of known, non-player fleets. */
	public final List<AIFleet> enemyFleets = new LinkedList<>();
	/** List of known, non-player planets. */
	public final List<AIPlanet> enemyPlanets = new LinkedList<>();
	/** The list of maybe colonizable planets. */
	public final List<AIPlanet> unknownPlanets = new LinkedList<>();
	/** The map from regular planet or planet ID string to AI planet. */
	public final Map<Object, AIPlanet> planetMap = new HashMap<>();
	/** The inner limit. */
	public Rectangle explorationInnerLimit;
	/** The outer limit. */
	public Rectangle explorationOuterLimit;
	/** Check if the main player has conquered its first planet. */
	public boolean mayConquer;
	/** The player statistics. */
	public PlayerStatistics statistics;
	/** The colonization limit of the player. */
	public int colonizationLimit;
	/** The next attack date or null if never. */
	public Date nextAttack;
	/** The current date. */
	public Date now;
	/** The current difficulty. */
	public Difficulty difficulty;
	/** The current level. */
	public int level;
	/** The relations with other players. */
	public final Map<Player, DiplomaticRelation> relations = new HashMap<>();
	/** Players with active offers. */
	public final Set<Player> activeOffer = new HashSet<>();
	/** The main player of the game. */
	public Player mainPlayer;
	/** The current auto-build limit. */
	public long autoBuildLimit;
	/** Build economy first. */
	public boolean autobuildEconomyFirst;
	/** The limit of fighters. */
	public int fighterLimit;
	/** The limit of cruisers. */
	public int cruiserLimit;
	/** The limit of battleships. */
	public int battleshipLimit;
	/** The limit of space stations. */
	public int stationLimit;
	/** Indicator that the research requires new planets. */
	public boolean researchRequiresColonization;
	/** The player's current colonization targets. */
	public final Set<String> colonizationTargets = new LinkedHashSet<>();
	/** Does the user have diplomacy room? */
	public boolean hasDiplomacyRoom;
	/** Allow the deployment of more defenses? */
	public boolean mayImproveDefenses;
	/** No limit on planet building. */
	public boolean noLabLimit;
	/**
	 * Assign the values to this world from the real world.
	 * @param player the player
	 */
	public void assign(Player player) {
		this.player = player;
		this.mainPlayer = player.world.player;
		
		difficulty = player.difficulty;
		level = player.world.level;
		noLabLimit = player.world.noLabLimit();
		
		explorationInnerLimit = player.explorationInnerLimit;
		explorationOuterLimit = player.explorationOuterLimit;
		money = player.money();
		statistics = player.statistics.copy();
		this.colonizationLimit = player.colonizationLimit;

        mayConquer = player == player.world.player || player.world.player.statistics.planetsColonized.value > 0 || player.world.scripting.mayPlayerAttack(player);
		mayImproveDefenses = player.world.scripting.mayPlayerImproveDefenses(player);
		
		inventory.putAll(player.inventory);
		
		for (ResearchType rt : player.world.researches.values()) {
			if (player.isAvailable(rt)) {
				if (!rt.nobuild) {
					availableResearch.add(rt);
				}
			} else
			if (rt.race.contains(player.race)) {
				if (player.canResearch(rt)) {
					remainingResearch.add(rt);
				} else
				if (rt.level <= player.world.level) {
					furtherResearch.add(rt);
				}
			}
		}
		runningResearch = player.runningResearch();
		
		for (Map<ResearchType, Production> prods : player.production.values()) {
			for (Production prod : prods.values()) {
				productions.put(prod.type, prod.copy());
			}
		}
		for (Research res : player.researches.values()) {
			researches.put(res.type, res.copy());
		}
		
		for (Player p2 : player.world.players.values()) {
			// if self or ignorables
			if (p2 != player && player.world.aiAccept(p2)) {
				players.add(p2);
			}
		}
		
		for (Fleet f : player.fleets.keySet()) {
			if (f.owner == player) {
				if (f.task != FleetTask.SCRIPT) {
					AIFleet aif = new AIFleet();
					aif.assign(f, this);
					ownFleets.add(aif);
				}
			} else
			if (player.world.aiAccept(f.owner)) {
				AIFleet aif = new AIFleet();
				aif.assign(f, this);
				enemyFleets.add(aif);
			}
		}
		global = new PlanetStatistics();
		for (Planet pl : player.planets.keySet()) {
			AIPlanet aip = new AIPlanet();
			aip.assign(pl, this);
			planetMap.put(pl, aip);
			planetMap.put(pl.id, aip);
			if (player.knowledge(pl, PlanetKnowledge.OWNER) < 0) {
				unknownPlanets.add(aip);
			} else
			if (pl.owner == player) {
				ownPlanets.add(aip);
				global.add(aip.statistics);
			} else {
				enemyPlanets.add(aip);
			}
		}
		now = player.world.time.getTime();
		
		for (DiplomaticRelation dr : player.world.relations) {
			if (dr.first.equals(player.id)) {
				relations.put(player.world.players.get(dr.second), dr);
			} else
			if (dr.second.equals(player.id)) {
				relations.put(player.world.players.get(dr.first), dr);
			}
		}
		for (Player p : player.world.players.values()) {
			if (p.offers.containsKey(player.id)) {
				activeOffer.add(p);
			}
		}
		
		if (player == mainPlayer) {
			autoBuildLimit = player.world.config.autoBuildLimit;
			autobuildEconomyFirst = player.world.config.autoBuildEconomyFirst;
		}
		fighterLimit = player.world.params().fighterLimit();
		cruiserLimit = player.world.params().mediumshipLimit();
		battleshipLimit = player.world.params().battleshipLimit();
		stationLimit = player.world.params().stationLimit();
		if (level == 3 && player != mainPlayer) {
			switch (difficulty) {
			case EASY:
				fighterLimit = Math.max(1, fighterLimit * 2 / 3);
				cruiserLimit = Math.max(1, cruiserLimit * 2 / 3);
				battleshipLimit = Math.max(1, battleshipLimit * 1 / 3);
				stationLimit = Math.max(1, battleshipLimit * 2 / 3);
				break;
			case NORMAL:
				fighterLimit = Math.max(1, fighterLimit * 5 / 6);
				cruiserLimit = Math.max(1, cruiserLimit * 5 / 6);
				battleshipLimit = Math.max(1, battleshipLimit * 2 / 3);
				break;
			default:
			}
		}
		colonizationTargets.addAll(player.colonizationTargets);
		hasDiplomacyRoom = player.world.getShip().positions.containsKey("*diplomacy");
	}
	/**
	 * Returns or calculates the planet statistics.
	 * @param p the planet
	 * @return the statistics
	 */
	public PlanetStatistics getStatistics(Planet p) {
		PlanetStatistics ps = planetStatistics.get(p);
		if (ps == null) {
			ps = p.getStatistics();
			planetStatistics.put(p, ps);
		}
		return ps;
	}
	/**
	 * Returns or calculates the fleet statistics.
	 * @param f the fleet
	 * @return the statistics
	 */
	public FleetStatistics getStatistics(Fleet f) {
		FleetStatistics fs = fleetStatistics.get(f);
		if (fs == null) {
			fs = f.getStatistics();
			fleetStatistics.put(f, fs);
		}
		return fs;
	}
	/**
	 * Returns the player's knowledge level about a fleet.
	 * @param f the fleet
	 * @return the knowledge level
	 */
	public FleetKnowledge knowledge(Fleet f) {
		return player.fleets.get(f);
	}
	/**
	 * Returns the player's knowledge level about a planet.
	 * @param p the planet
	 * @return the knowledge
	 */
	public PlanetKnowledge knowledge(Planet p) {
		return player.planets.get(p);
	}
	/**
	 * The inventory count of the given research type.
	 * @param rt the technology
	 * @return the count
	 */
	public int inventoryCount(ResearchType rt) {
		Integer i = inventory.get(rt);
		return i != null ? i : 0;
	}
	/**
	 * Add a specific amount to the current inventory level.
	 * @param type the type
	 * @param count the count change
	 */
	public void addInventoryCount(ResearchType type, int count) {
		Integer c = inventory.get(type);
		int ci = c != null ? c : 0;
		int ci2 = ci + count;
		if (ci2 <= 0) {
			inventory.remove(type);
		} else {
			inventory.put(type, ci2);
		}
	}
	/**
	 * The inventory count of the given research type.
	 * @param id the identifier
	 * @return the count
	 */
	public Pair<Integer, ResearchType> inventoryCount(String id) {
		for (Map.Entry<ResearchType, Integer> e : inventory.entrySet()) {
			if (e.getKey().id.equals(id)) {
				return Pair.of(e.getValue(), e.getKey());
			}
		}
		return Pair.of(0, null);
	}
	/**
	 * Check if the given research is available.
	 * @param rt the research
	 * @return true if available
	 */
	public boolean isAvailable(ResearchType rt) {
		return availableResearch.contains(rt);
	}
	/**
	 * Check if the given research ID is available and
	 * return the research object.
	 * @param id the research id
	 * @return the research or null if not available
	 */
	public ResearchType isAvailable(String id) {
		for (ResearchType rt : availableResearch) {
			if (rt.id.equals(id)) {
				return rt;
			}
		}
		return null;
	}
	/**
	 * Check if the given coordinate is within the allowed exploration zones.
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @return true if within the limits
	 */
	public boolean withinLimits(int x, int y) {
		if (explorationInnerLimit != null && explorationInnerLimit.contains(x, y)) {
			return false;
		}
		return explorationOuterLimit == null || explorationOuterLimit.contains(x, y);
	}
	/**
	 * Computes the center location of all of our planets.
	 * @return the center location
	 */
	public Point2D.Double center() {
		double cx = 0;
		double cy = 0;
		for (AIPlanet p : ownPlanets) {
			cx += p.planet.x;
			cy += p.planet.y;
		}
		cx /= ownPlanets.size();
		cy /= ownPlanets.size();

		return new Point2D.Double(cx, cy);
	}
	/**
	 * The number of remaining production or zero if not producing.
	 * @param rt the research type
	 * @return the remaining count
	 */
	public int productionCount(ResearchType rt) {
		Production prod = productions.get(rt);
		if (prod != null) {
			return prod.count;
		}
		return 0;
	}
	/**
	 * Returns the list of known planets of the given player.
	 * @param player the target player
	 * @return the list of planets
	 */
	public List<AIPlanet> planetsOf(String player) {
		List<AIPlanet> result = new ArrayList<>();
		for (Map.Entry<Object, AIPlanet> e : planetMap.entrySet()) {
			if (e.getKey() instanceof String) {
				AIPlanet value = e.getValue();
				if (value.owner.id.equals(player)) {
					result.add(value);
				}
			}
		}
		return result;
	}
}
