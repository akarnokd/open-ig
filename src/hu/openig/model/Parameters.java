/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Func0;
import hu.openig.utils.Exceptions;
import hu.openig.utils.U;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Contains game simulation related parameters.
 * @author akarnokd, 2011.12.20.
 */
public class Parameters {
	/** The speed factor callback. */
	protected final Func0<Integer> speed;
	/** The multiplier for radar-range in pixels for ground radars. */
	@LoadField
	protected int groundRadarUnitSize = 35;
	/** The base radar range if there is no radar on the fleet. */
	@LoadField
	protected double fleetRadarlessMultiplier = 0.5;
	/** The multiplier for radar-range in pixels for fleet radars. */
	@LoadField
	protected int fleetRadarUnitSize = 25;
	/** The research speed in terms of money / simulation step. */
	@LoadField
	protected int researchSpeed = 8;
	/** The money spent on producing an item with 1 unit of capacity under 1 ingame minute. The larger the faster the production becomes. */
	@LoadField
	protected double productionUnit = 0.004;
	/** The construction points per simulation step. */
	@LoadField
	protected int constructionSpeed = 20;
	/** The construction cost per simulation step. Not used now. */
	@LoadField
	protected int constructionCost = 20;
	/** The hitpoints improved per simulation step. */
	@LoadField
	protected int repairSpeed = 10;
	/** The repair cost per simulation step. */
	@LoadField
	protected int repairCost = 2;
	/** The denominator to compute hitpoints from costs in battles. */
	@LoadField
	protected int costToHitpoints = 10;
	/** What is considered a planet-nearby. */
	@LoadField
	protected double nearbyDistance = 10;
	/** The ratio between the fleet movement timer and speed() minute simulation time. */
	@LoadField
	protected int simulationRatio = 10;
	/** The fleet speed in pixels per game minute per hyperdrive level. */
	@LoadField
	protected double fleetSpeed = 0.016;
	/** The maximum allowed station count per planet. */
	@LoadField
	protected int stationLimit = 3;
	/** The maximum battleship limit per fleet. */
	@LoadField
	protected int battleshipLimit = 3;
	/** The maximum fighter limit per type in a fleet. */
	@LoadField
	protected int fighterLimit = 30;
	/** The maximum number of cruisers and destroyers per fleet. */
	@LoadField
	protected int mediumshipLimit = 25;
	/** The radar share limit. */
	@LoadField
	protected int radarShareLimit = 80;
	/** The black market restock time in minutes. */
	@LoadField
	protected int blackMarketRestockTime = 3 * 24 * 60;
	/**
	 * Constructor. Initializes the speed factor callback field.
	 * @param speed the speed callback
	 */
	public Parameters(Func0<Integer> speed) {
		this.speed = speed;
	}
	/**
	 * Load the property values from the supplied mapping.
	 * @param parameters the map of key-value pairs.
	 */
	public void load(Map<String, String> parameters) {
		for (Field f : getClass().getDeclaredFields()) {
			if (f.isAnnotationPresent(LoadField.class)) {
				try {
					if (f.getType() == Integer.TYPE) {
						f.set(this, U.getInt(parameters, f.getName(), (Integer)f.get(this)));
					} else
					if (f.getType() == Double.TYPE) {
						f.set(this, U.getDouble(parameters, f.getName(), (Double)f.get(this)));
					}
				} catch (IllegalAccessException ex) {
					Exceptions.add(ex);
				}
			}
		}
	}
	/** @return the speed multiplier. */
	public int speed() {
		return speed.invoke();
	}
	/**
	 * @return the multiplier for radar-range in pixels for ground radars
	 */
	public int groundRadarUnitSize() {
		return groundRadarUnitSize;
	}
	/** @return the multiplier for radarless fleets */
	public double fleetRadarlessMultiplier() {
		return fleetRadarlessMultiplier;
	}
	/**
	 * @return the multiplier for radar-range in pixels for fleet radars
	 */
	public int fleetRadarUnitSize() {
		return fleetRadarUnitSize; // DEFAULT: 25
	}
	/** @return the research speed in terms of money / simulation step. */
	public int researchSpeed() {
		return researchSpeed * speed(); // DEFAULT: 40
	}
	/** @return The money spent on producing an item with 1 unit of capacity under 1 ingame minute. The larger the faster the production becomes. */
	public double productionUnit() {
		return productionUnit * speed(); // DEFAULT: 0.004 cr / min
	}
	/** @return the construction points per simulation step. */
	public int constructionSpeed() {
		return constructionSpeed * speed(); // DEFAULT: 200
	}
	/** @return the construction cost per simulation step. Not used now. */
	public int constructionCost() {
		return constructionCost * speed(); // DEFAULT: 200
	}
	/** @return the hitpoints improved per simulation step. */
	public int repairSpeed() {
		return repairSpeed * speed(); // DEFAULT: 50
	}
	/** @return the repair cost per simulation step. */
	public int repairCost() {
		return repairCost * speed(); // DEFAULT: 20
	}
	/** @return the denominator to compute hitpoints from costs in battles. */
	public int costToHitpoints() {
		return costToHitpoints; // DEFAULT: 25
	}
	/**
	 * What is considered a planet-nearby.
	 * @return the distance
	 */
	public double nearbyDistance() {
		return nearbyDistance;
	}
	/**
	 * @return the ratio between the fleet movement timer and speed() minute simulation time.
	 */
	public int simulationRatio() {
		return simulationRatio;
	}
	/**
	 * @return The fleet speed in pixels per game minute per hyperdrive level.
	 */
	public double fleetSpeed() {
		return fleetSpeed * speed(); 
	}
	/**
	 * The maximum allowed station count per planet.
	 * @return the count
	 */
	public int stationLimit() {
		return stationLimit;
	}
	/**
	 * @return the maximum battleship limit per fleet
	 */
	public int battleshipLimit() {
		return battleshipLimit;
	}
	/**
	 * @return the maximum fighter limit per type in a fleet
	 */
	public int fighterLimit() {
		return fighterLimit;
	}
	/**
	 * @return the maximum number of cruisers and destroyers per fleet
	 */
	public int mediumshipLimit() {
		return mediumshipLimit;
	}
	/** @return the radar share limit between allies. */
	public int radarShareLimit() {
		return radarShareLimit;
	}
	/**
	 * @return the black market restock time in minutes
	 */
	public int blackMarketRestockTime() {
		return blackMarketRestockTime;
	}
}
