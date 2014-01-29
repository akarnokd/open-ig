/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Difficulty;

/**
 * Game definition with basic settings and no user details.
 * @author akarnokd, 2013.04.25.
 */
public class CustomGameDefinition {
	/** The game from where the galaxy and planet definitions should be taken. */
	public String galaxy;
	/** Randomize surface types. */
	public boolean galaxyRandomSurface;
	/** Randomize layout. */
	public boolean galaxyRandomLayout;
	/** Custom planet count. */
	public boolean galaxyCustomPlanets;
	/** The number of custom planets. */
	public int galaxyPlanetCount;
	/** The race definitions to use. */
	public String race;
	/** The technology definitions. */
	public String tech;
	/** The initial tech level. */
	public int startLevel;
	/** The maximum tech level. */
	public int maxLevel = 5;
	/** The initial money. */
	public int initialMoney;
	/** The initial planets per player. */
	public int initialPlanets;
	/** Place colony hubs on the planets? */
	public boolean placeColonyHubs;
	/** Initial population on planets. */
	public int initialPopulation;
	/** Grant colony ship technology. */
	public boolean grantColonyShip;
	/** Initial colony ship counts. */
	public int initialColonyShips;
	/** Grant orbital factory technology. */
	public boolean grantOrbitalFactory;
	/** Initial orbital factory counts. */
	public int initialOrbitalFactories;
	/** The initial diplomatic relation. */
	public SkirmishDiplomaticRelation initialDiplomaticRelation;
	/** The initial difficulty. */
	public Difficulty initialDifficulty;
	/** Conquest victory condition. */
	public boolean victoryConquest;
	/** Occupation victory conquest. */
	public boolean victoryOccupation;
	/** Occupation percent. */
	public int victoryOccupationPercent;
	/** Occupation duration. */
	public int victoryOccupationTime;
	/** Economic victory. */
	public boolean victoryEconomic;
	/** Economic victory money. */
	public int victoryEconomicMoney;
	/** Technological victory. */
	public boolean victoryTechnology;
	/** Social victory. */
	public boolean victorySocial;
	/** Social victory morale limit. */
	public int victorySocialMorale;
	/** Social victory planet limit. */
	public int victorySocialPlanets;
	/** Allow building more than 1 labs per planet. */
	public boolean noLabLimit;
	/** Allow building more than 1 factories per planet. */
	public boolean noFactoryLimit;
}