/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.utils.XElement;

import java.util.List;

/**
 * Base interface to manage AI related operations.
 * @author akarnokd, 2011.12.08.
 */
public interface AIManager {
	/**
	 * Initialize the AI manager by showing the world and the player object it is representing.
	 * @param p the player object
	 */
	void init(Player p);
	/**
	 * Prepare the world for AI processing outside the EDT, e.g., copy relevant world state into private data.
	 */
	void prepare();
	/**
	 * Manage the world.
	 */
	void manage();
	/**
	 * Apply the changes from the world management.
	 */
	void apply();
	/**
	 * Calculates the diplomatic response to an offer coming from another player.
	 * @param other the other player
	 * @param about the topic about to negotiate
	 * @param approach the style of the offer
	 * @param argument the custom object associated with the negotiation (i.e., money amount, the common enemy, etc.)
	 * @return the response
	 */
	ResponseMode diplomacy(Player other, NegotiateType about, ApproachType approach, Object argument);
	/**
	 * Initialize the space battle.
	 * <p>Called before the first battle simulation step.</p>
	 * @param world the space war world.
	 */
	void spaceBattleInit(SpacewarWorld world);
	/**
	 * Handle some aspects of a space battle.
	 * @param world the world
	 * @param idles the list of objects which have completed their current attack objectives and awaiting new commands
	 * @return the global action
	 */
	SpacewarAction spaceBattle(SpacewarWorld world, List<SpacewarStructure> idles);
	/**
	 * Called after the space battle has been concluded and losses applied.
	 * @param world the world object
	 */
	void spaceBattleDone(SpacewarWorld world);
	/**
	 * Initialize the ground battle.
	 * <p>Called before the first battle simulation step.</p>
	 * @param war the ground war world
	 */
	void groundBattleInit(GroundwarWorld war);
	/**
	 * Handle some aspects of a ground battle.
	 * @param war the ground war world
	 */
	void groundBattle(GroundwarWorld war);
	/**
	 * Called after the ground battle has been concluded and losses applied.
	 * @param war the ground war world
	 */
	void groundBattleDone(GroundwarWorld war);
	/**
	 * Save the state of this AI manager from a save file.
	 * @param out the output XElement
	 */
	void save(XElement out);
	/**
	 * Load the state of this AI manager from a save file.
	 * @param in the input XElement
	 */
	void load(XElement in);
	/**
	 * Notification if a research has changed its state to completed, money or lab.
	 * @param rt the research
	 * @param state the state
	 */
	void onResearchStateChange(ResearchType rt, ResearchState state);
	/**
	 * Notification if a production (batch) is complete.
	 * @param rt the research
	 */
	void onProductionComplete(ResearchType rt);
	/**
	 * Notification if a new planet has been discovered.
	 * @param planet the planet
	 */
	void onDiscoverPlanet(Planet planet);
	/**
	 * Notification if a fleet has been discovered.
	 * @param fleet the fleet
	 */
	void onDiscoverFleet(Fleet fleet);
	/**
	 * Notification if a new player has been discovered.
	 * @param player the player
	 */
	void onDiscoverPlayer(Player player);
	/**
	 * Notification if a fleet arrived at a specific point in space.
	 * @param fleet the fleet
	 * @param x the X space coordinate
	 * @param y the Y space coordinate
	 */
	void onFleetArrivedAtPoint(Fleet fleet, double x, double y);
	/**
	 * Notification if a fleet arrived at a planet.
	 * @param fleet the fleet
	 * @param planet the planet
	 */
	void onFleetArrivedAtPlanet(Fleet fleet, Planet planet);
	/**
	 * Notification if a fleet arrived at another fleet.
	 * @param fleet the fleet
	 * @param other the other fleet
	 */
	void onFleetArrivedAtFleet(Fleet fleet, Fleet other);
	/**
	 * Notification if a building is completed.
	 * @param planet the planet
	 * @param building the building
	 */
	void onBuildingComplete(Planet planet, Building building);
	/**
	 * Notification about a fleet disappearing from radar.
	 * @param fleet the fleet we lost sight of
	 */
	void onLostSight(Fleet fleet);
	/**
	 * Notification about a fleet loosing its target due radar coverage, destruction or disassembly.
	 * @param fleet the fleet
	 * @param target the target fleet
	 */
	void onLostTarget(Fleet fleet, Fleet target);
	/** Notification if a new day arrived. */
	void onNewDay();
	/**
	 * Notification if a satellite was destroyed.
	 * @param planet the planet
	 * @param ii the inventory item
	 */
	void onSatelliteDestroyed(Planet planet, InventoryItem ii);
	/**
	 * Notification if a planet died.
	 * <p>Called before the <code>planet.die()</code> method in the simulator</p>
	 * @param planet the target planet
	 */
	void onPlanetDied(Planet planet);
	/**
	 * Notification if a planet is in revolt state.
	 * @param planet the target planet
	 */
	void onPlanetRevolt(Planet planet);
	/**
	 * Notification about a planet conquer.
	 * @param planet the planet
	 * @param lastOwner the last owner
	 */
	void onPlanetConquered(Planet planet, Player lastOwner);
	/**
	 * Notification about a planet just colonized.
	 * @param planet the target planet
	 */
	void onPlanetColonized(Planet planet);
	/**
	 * Notification about a planet lost.
	 * @param planet the planet lost (showing the current owner)
	 */
	void onPlanetLost(Planet planet);
	/**
	 * Notification about the radar sweep completion.
	 */
	void onRadar();
	/** 
	 * Notification about the auto-battle start.
	 * @param battle the battle configuration 
	 * @param str the strength
	 */
	void onAutoSpacewarStart(BattleInfo battle, SpaceStrengths str);
	/** 
	 * Notification about the auto-battle start.
	 * @param battle the battle configuration 
	 * @param attacker the attacker's strength
	 * @param defender the defender's strength
	 */
	void onAutoGroundwarStart(BattleInfo battle, AttackDefense attacker, AttackDefense defender);
	/** 
	 * Notification about the auto-battle finish. Called after losses are applied. 
	 * @param battle the battle configuration 
	 */
	void onAutobattleFinish(BattleInfo battle);
}
