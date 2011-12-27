/*
 * Copyright 2008-2012, David Karnok 
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
	 */
	void actionPlaceBuilding(Planet planet, BuildingType buildingType);
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

}
