/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Pair;

import java.util.HashMap;
import java.util.HashSet;
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
	public final Map<Planet, PlanetStatistics> planetStatistics = new HashMap<Planet, PlanetStatistics>();
	/** The precomputed planet statistics. */
	public final Map<Fleet, FleetStatistics> fleetStatistics = new HashMap<Fleet, FleetStatistics>();
	/** The active productions. */
	public final Map<ResearchType, Production> productions = new HashMap<ResearchType, Production>();
	/** The active researches. */
	public final Map<ResearchType, Research> researches = new HashMap<ResearchType, Research>();
	/** The currently running research. */
	public ResearchType runningResearch;
	/** The current inventory level. */
	public final Map<ResearchType, Integer> inventory = new HashMap<ResearchType, Integer>();
	/** The set of available researches. */
	public final Set<ResearchType> availableResearch = new HashSet<ResearchType>();
	/** The set of remaining research. */
	public final Set<ResearchType> remainingResearch = new HashSet<ResearchType>();
	/** The set of researches which can be developed in the current level. */
	public final Set<ResearchType> furtherResearch = new HashSet<ResearchType>();
	/** The list of known other players. */
	public final List<Player> players = new LinkedList<Player>();
	/** The list of own fleets. */
	public final List<AIFleet> ownFleets = new LinkedList<AIFleet>();
	/** The list of own planets. */
	public final List<AIPlanet> ownPlanets = new LinkedList<AIPlanet>();
	/** List of known, non-player fleets. */
	public final List<AIFleet> enemyFleets = new LinkedList<AIFleet>();
	/** List of known, non-player planets. */
	public final List<AIPlanet> enemyPlanets = new LinkedList<AIPlanet>();
	/** The list of maybe colonizable planets. */
	public final List<AIPlanet> unknownPlanets = new LinkedList<AIPlanet>();
	/**
	 * Assign the values to this world from the real world.
	 * @param player the player
	 */
	public void assign(Player player) {
		this.player = player;
		money = player.money;
		
		inventory.putAll(player.inventory);
		
		for (ResearchType rt : player.world.researches.values()) {
			if (player.isAvailable(rt)) {
				availableResearch.add(rt);
			} else
			if (rt.race.contains(player.race)) {
				if (player.world.canResearch(rt)) {
					remainingResearch.add(rt);
				} else
				if (rt.level <= player.world.level) {
					furtherResearch.add(rt);
				}
			}
		}
		runningResearch = player.runningResearch;
		
		for (Map<ResearchType, Production> prods : player.production.values()) {
			for (Production prod : prods.values()) {
				productions.put(prod.type, prod.copy());
			}
		}
		for (Research res : player.research.values()) {
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
				AIFleet aif = new AIFleet();
				aif.assign(f, this);
				ownFleets.add(aif);
			} else
			if (player.world.aiAccept(f.owner)) {
				AIFleet aif = new AIFleet();
				aif.assign(f, this);
				enemyFleets.add(aif);
			}
		}
		global = new PlanetStatistics();
		for (Planet pl : player.planets.keySet()) {
			if (player.knowledge(pl, PlanetKnowledge.OWNER) < 0) {
				AIPlanet aip = new AIPlanet();
				aip.assign(pl, this);
				unknownPlanets.add(aip);
			} else
			if (pl.owner == player) {
				AIPlanet aip = new AIPlanet();
				aip.assign(pl, this);
				ownPlanets.add(aip);
				global.add(aip.statistics);
			} else {
				AIPlanet aip = new AIPlanet();
				aip.assign(pl, this);
				enemyPlanets.add(aip);
			}
		}
		
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
		return i != null ? i.intValue() : 0;
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
}
