/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

/**
 * Class representing the world for an AI player.
 * @author akarnokd, 2011.12.08.
 */
public class AIWorld extends AIObject {
	/** The backing world. */
	public World world;
	/** The player of this world. */
	public Player player;
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
		if (SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException("Should run in EDT!");
		}
		this.world = world;
		this.player = player;
		for (Player p2 : world.players.values()) {
			// if self or ignorables
			if (p2 != player && world.aiAccept(p2)) {
				players.add(p2);
			}
		}
		
		for (Fleet f : player.fleets.keySet()) {
			if (f.owner == player) {
				AIFleet aif = new AIFleet();
				aif.assign(f, world);
				ownFleets.add(aif);
			} else
			if (world.aiAccept(f.owner)) {
				AIFleet aif = new AIFleet();
				aif.assign(f, world);
				enemyFleets.add(aif);
			}
		}
		for (Planet pl : player.planets.keySet()) {
			if (pl.owner == null || player.knowledge(pl, PlanetKnowledge.OWNER) < 0) {
				AIPlanet aip = new AIPlanet();
				aip.assign(pl, world);
				colonizePlanets.add(aip);
			} else
			if (pl.owner == player) {
				AIPlanet aip = new AIPlanet();
				aip.assign(pl, world);
				ownPlanets.add(aip);
			} else {
				AIPlanet aip = new AIPlanet();
				aip.assign(pl, world);
				enemyPlanets.add(aip);
			}
		}
	}
	/**
	 * Apply the results of the AI actions back to the real world.
	 */
	public void apply() {
		if (SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException("Should run in EDT!");
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
}
