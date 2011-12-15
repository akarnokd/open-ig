/*
 * Copyright 2008-2012, David Karnok 
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
	 * @param w the world object
	 * @param p the player object
	 */
	void init(World w, Player p);
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
	 * @param world the world
	 * @param we the target player
	 * @param other the other player
	 * @param offer the kind of offer/request
	 * @return the response
	 */
	ResponseMode diplomacy(World world, Player we, Player other, DiplomaticInteraction offer);
	/**
	 * Handle some aspects of a space battle.
	 * @param world the world
	 * @param player the AI's player object
	 * @param idles the list of objects which have completed their current attack objectives and awaiting new commands
	 * @return the global action
	 */
	SpacewarAction spaceBattle(SpacewarWorld world, Player player, List<SpacewarStructure> idles);
	/**
	 * Handle some aspects of a ground battle.
	 * TODO output?
	 * @param world the world
	 * @param we the target player
	 * @param battle the battle information
	 */
	void groundBattle(World world, Player we, BattleInfo battle);
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
	 * Notification if a research has completed.
	 * @param rt the research
	 */
	void onResearchComplete(ResearchType rt);
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
}
