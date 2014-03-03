/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;


/**
 * Interface to issue actions.
 * @author akarnokd, 2011.12.27.
 */
public interface AIControls {
	/**
	 * Start the production of the given technology with the amount and priority.
	 * @param rt the technology
	 * @param count number of items to produce
	 * @param priority the production priority
	 */
	void actionStartProduction(ResearchType rt, int count, int priority);
	/**
	 * Action to start the research.
	 * @param rt the research type
	 * @param moneyFactor the money multiplier for the research
	 */
	void actionStartResearch(ResearchType rt, double moneyFactor);
	/**
	 * Place a building onto the planet (via the autobuild location mechanism).
	 * @param planet the target planet
	 * @param buildingType the building type
	 * @return the action result
	 */
	AIResult actionPlaceBuilding(Planet planet, BuildingType buildingType);
	/**
	 * Demolish a building.
	 * @param planet the target planet
	 * @param b the building
	 */
	void actionDemolishBuilding(Planet planet, Building b);
	/**
	 * Upgrade a building on the given planet to the given level.
	 * @param planet the target planet
	 * @param building the building
	 * @param newLevel the new level
	 */
	void actionUpgradeBuilding(Planet planet, Building building, int newLevel);
	/**
	 * Set the repair state on the given building.
	 * @param planet the target planet
	 * @param building the target building
	 * @param repair set the repair flag to this
	 */
	void actionRepairBuilding(Planet planet, Building building, boolean repair);
	/**
	 * Send the fleet to colonize the target planet.
	 * @param fleet the fleet
	 * @param planet the planet
	 */
	void actionColonizePlanet(Fleet fleet, Planet planet);
	/**
	 * Move the fleet to the designated coordinates.
	 * @param fleet the fleet
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 */
	void actionMoveFleet(Fleet fleet, double x, double y);
	/**
	 * Move the fleet to the given planet.
	 * @param fleet the fleet
	 * @param planet the target planet
	 */
	void actionMoveFleet(Fleet fleet, Planet planet);
	/**
	 * Use the fleet to attack the enemy fleet.
	 * @param fleet the fleet
	 * @param enemy the enemy fleet
	 * @param defense indicate if the attack is to stop an advancing enemy
	 */
	void actionAttackFleet(Fleet fleet, Fleet enemy, boolean defense);
	/**
	 * Use the fleet to attack the planet.
	 * @param fleet the fleet
	 * @param planet the target planet
	 * @param mode the aim of the attack
	 */
	void actionAttackPlanet(Fleet fleet, Planet planet, AIAttackMode mode);
	/**
	 * Deploy units such as tanks, fighters and stations into a planet.
	 * @param loadFactor percentage of 0..1 about how many units to place into slots.
	 * @param powerFactor percentage of 0..1 about how well the units should be equipped
	 * @param planet the target planet
	 * @param items the item types to deploy
	 */
	void actionDeployUnits(double loadFactor, double powerFactor, Planet planet, Iterable<ResearchType> items);
	/**
	 * Pause a production by setting its priority to zero.
	 * @param rt the research type to pause
	 */
	void actionPauseProduction(ResearchType rt);
	/**
	 * Deploy a fleet from the inventory.
	 * @param location the planet where the fleet will emerge
	 * @param loadFactor percentage of 0..1 about how many units to place into fighter/cruiser/battleship slots.
	 * @param powerFactor percentage of 0..1 about how well the units should be equipped
	 * @param items the sequence of items to choose from when filling in the slots
	 */
	void actionDeployFleet(
			Planet location,
			double loadFactor, 
			double powerFactor, 
			Iterable<ResearchType> items);
	/**
	 * Deploy one unit of the satellite to the target planet.
	 * @param planet the planet
	 * @param satellite the satellite type
	 */
	void actionDeploySatellite(Planet planet, ResearchType satellite);
	/**
	 * Remove the production from the production lines.
	 * @param rt the technology
	 */
	void actionRemoveProduction(ResearchType rt);
	/**
	 * Create a new empty fleet with the given name.
	 * @param name the optional name
	 * @param location the planet where the fleet should be created
	 * @return the created fleet
	 */
	Fleet actionCreateFleet(String name, Planet location);
	/**
	 * Stops the given research.
	 * @param rt the research to stop
	 */
	void actionStopResearch(ResearchType rt);
	/**
	 * Change the enabled-state of the given building.
	 * @param planet the target planet
	 * @param building the building
	 * @param enabled the new state
	 */
	void actionEnableBuilding(Planet planet, Building building, boolean enabled);
	/**
	 * Set the taxation level of the given planet.
	 * @param planet the target planet
	 * @param newLevel the new tax level
	 */
	void actionSetTaxation(Planet planet, TaxLevel newLevel);
	/**
	 * Sell the given amount from the satellites of the planet.
	 * @param planet the planet
	 * @param satellite the satellite
	 * @param count the amount to sell
	 */
	void actionSellSatellite(Planet planet, ResearchType satellite, int count);
}
