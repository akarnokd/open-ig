/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Class representing the world for an AI player, copying the world state to allow a thread-safe evaluation.
 * @author akarnokd, 2011.12.08.
 */
public class AIWorld extends AIObject {
	/** The backing world. */
	public World world;
	/** The player of this world. */
	public Player player;
	/** The player's current money. */
	public long money;
	/** The precomputed planet statistics. */
	public final Map<Planet, PlanetStatistics> planetStatistics = new HashMap<Planet, PlanetStatistics>();
	/** The precomputed planet statistics. */
	public final Map<Fleet, FleetStatistics> fleetStatistics = new HashMap<Fleet, FleetStatistics>();
	/** The active productions. */
	public final List<Production> productions = new LinkedList<Production>();
	/** The active researches. */
	public final List<Research> researches = new LinkedList<Research>(); 
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
	public final List<AIPlanet> colonizePlanets = new LinkedList<AIPlanet>();
	/**
	 * Assign the values to this world from the real world.
	 * @param world the world
	 * @param player the player
	 */
	public void assign(World world, Player player) {
		this.world = world;
		this.player = player;
		money = player.money;
		
		for (Map<ResearchType, Production> prods : player.production.values()) {
			for (Production prod : prods.values()) {
				productions.add(prod.copy());
			}
		}
		for (Research res : player.research.values()) {
			researches.add(res.copy());
		}
		
		for (Player p2 : world.players.values()) {
			// if self or ignorables
			if (p2 != player && world.aiAccept(p2)) {
				players.add(p2);
			}
		}
		
		for (Fleet f : player.fleets.keySet()) {
			if (f.owner == player) {
				AIFleet aif = new AIFleet();
				aif.assign(f, this);
				ownFleets.add(aif);
			} else
			if (world.aiAccept(f.owner)) {
				AIFleet aif = new AIFleet();
				aif.assign(f, this);
				enemyFleets.add(aif);
			}
		}
		for (Planet pl : player.planets.keySet()) {
			if (pl.owner == null || player.knowledge(pl, PlanetKnowledge.OWNER) < 0) {
				AIPlanet aip = new AIPlanet();
				aip.assign(pl, this);
				colonizePlanets.add(aip);
			} else
			if (pl.owner == player) {
				AIPlanet aip = new AIPlanet();
				aip.assign(pl, this);
				ownPlanets.add(aip);
			} else {
				AIPlanet aip = new AIPlanet();
				aip.assign(pl, this);
				enemyPlanets.add(aip);
			}
		}
	}
	@Override
	public void assign(AITaskCandidate tc) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public double taskTime(AITask task) {
		// TODO Auto-generated method stub
		return Double.POSITIVE_INFINITY;
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
			fs = f.getStatistics(world.battle);
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
}
