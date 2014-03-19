/*
 * Copyright 2008-2014, David Karnok 
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
	/** The base tax income per day. */
	public double taxBase = 0.0;
	/** The tax income scaling. */
	public double taxScale = 1.0;
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CustomGameDefinition other = (CustomGameDefinition) obj;
		if (galaxy == null) {
			if (other.galaxy != null) {
				return false;
			}
		} else if (!galaxy.equals(other.galaxy)) {
			return false;
		}
		if (galaxyCustomPlanets != other.galaxyCustomPlanets) {
			return false;
		}
		if (galaxyPlanetCount != other.galaxyPlanetCount) {
			return false;
		}
		if (galaxyRandomLayout != other.galaxyRandomLayout) {
			return false;
		}
		if (galaxyRandomSurface != other.galaxyRandomSurface) {
			return false;
		}
		if (grantColonyShip != other.grantColonyShip) {
			return false;
		}
		if (grantOrbitalFactory != other.grantOrbitalFactory) {
			return false;
		}
		if (initialColonyShips != other.initialColonyShips) {
			return false;
		}
		if (initialDifficulty != other.initialDifficulty) {
			return false;
		}
		if (initialDiplomaticRelation != other.initialDiplomaticRelation) {
			return false;
		}
		if (initialMoney != other.initialMoney) {
			return false;
		}
		if (initialOrbitalFactories != other.initialOrbitalFactories) {
			return false;
		}
		if (initialPlanets != other.initialPlanets) {
			return false;
		}
		if (initialPopulation != other.initialPopulation) {
			return false;
		}
		if (maxLevel != other.maxLevel) {
			return false;
		}
		if (noFactoryLimit != other.noFactoryLimit) {
			return false;
		}
		if (noLabLimit != other.noLabLimit) {
			return false;
		}
		if (placeColonyHubs != other.placeColonyHubs) {
			return false;
		}
		if (race == null) {
			if (other.race != null) {
				return false;
			}
		} else if (!race.equals(other.race)) {
			return false;
		}
		if (startLevel != other.startLevel) {
			return false;
		}
		if (Double.doubleToLongBits(taxBase) != Double
				.doubleToLongBits(other.taxBase)) {
			return false;
		}
		if (Double.doubleToLongBits(taxScale) != Double
				.doubleToLongBits(other.taxScale)) {
			return false;
		}
		if (tech == null) {
			if (other.tech != null) {
				return false;
			}
		} else if (!tech.equals(other.tech)) {
			return false;
		}
		if (victoryConquest != other.victoryConquest) {
			return false;
		}
		if (victoryEconomic != other.victoryEconomic) {
			return false;
		}
		if (victoryEconomicMoney != other.victoryEconomicMoney) {
			return false;
		}
		if (victoryOccupation != other.victoryOccupation) {
			return false;
		}
		if (victoryOccupationPercent != other.victoryOccupationPercent) {
			return false;
		}
		if (victoryOccupationTime != other.victoryOccupationTime) {
			return false;
		}
		if (victorySocial != other.victorySocial) {
			return false;
		}
		if (victorySocialMorale != other.victorySocialMorale) {
			return false;
		}
		if (victorySocialPlanets != other.victorySocialPlanets) {
			return false;
		}
		if (victoryTechnology != other.victoryTechnology) {
			return false;
		}
		return true;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((galaxy == null) ? 0 : galaxy.hashCode());
		result = prime * result + (galaxyCustomPlanets ? 1231 : 1237);
		result = prime * result + galaxyPlanetCount;
		result = prime * result + (galaxyRandomLayout ? 1231 : 1237);
		result = prime * result + (galaxyRandomSurface ? 1231 : 1237);
		result = prime * result + (grantColonyShip ? 1231 : 1237);
		result = prime * result + (grantOrbitalFactory ? 1231 : 1237);
		result = prime * result + initialColonyShips;
		result = prime
				* result
				+ ((initialDifficulty == null) ? 0 : initialDifficulty
						.hashCode());
		result = prime
				* result
				+ ((initialDiplomaticRelation == null) ? 0
						: initialDiplomaticRelation.hashCode());
		result = prime * result + initialMoney;
		result = prime * result + initialOrbitalFactories;
		result = prime * result + initialPlanets;
		result = prime * result + initialPopulation;
		result = prime * result + maxLevel;
		result = prime * result + (noFactoryLimit ? 1231 : 1237);
		result = prime * result + (noLabLimit ? 1231 : 1237);
		result = prime * result + (placeColonyHubs ? 1231 : 1237);
		result = prime * result + ((race == null) ? 0 : race.hashCode());
		result = prime * result + startLevel;
		long temp;
		temp = Double.doubleToLongBits(taxBase);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(taxScale);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((tech == null) ? 0 : tech.hashCode());
		result = prime * result + (victoryConquest ? 1231 : 1237);
		result = prime * result + (victoryEconomic ? 1231 : 1237);
		result = prime * result + victoryEconomicMoney;
		result = prime * result + (victoryOccupation ? 1231 : 1237);
		result = prime * result + victoryOccupationPercent;
		result = prime * result + victoryOccupationTime;
		result = prime * result + (victorySocial ? 1231 : 1237);
		result = prime * result + victorySocialMorale;
		result = prime * result + victorySocialPlanets;
		result = prime * result + (victoryTechnology ? 1231 : 1237);
		return result;
	}
	
	
}