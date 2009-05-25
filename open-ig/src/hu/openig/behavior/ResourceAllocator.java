/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.behavior;

import hu.openig.model.GameBuilding;
import hu.openig.model.GamePlanet;

/**
 * Algorithm to allocate resources of a planet (workers, energy)
 * between varios builings using various algorithmms.
 * The available worker and energy will affect the building output.
 * @author karnokd
 */
public final class ResourceAllocator {
	/**
	 * Constructor.
	 */
	private ResourceAllocator() {
	}
	/**
	 * Allocate energy uniformly.
	 * @param planet the planet
	 * @return true if the allocation did change properties of the buildings
	 */
	public static boolean uniformEnergyAllocation(GamePlanet planet) {
		boolean result = false;
		int edemand = planet.getEnergyDemand();
		int eavail = planet.getEnergyProduction();
		if (eavail >= edemand || edemand == 0) {
			// use the demanded values everywhere
			for (GameBuilding b : planet.buildings) {
				int currEnergy = b.energy;
				b.energy = b.getEnergyDemand();
				// capture value change
				result |= currEnergy != b.energy;
			}
		} else {
			float perc = eavail / (float)edemand;
			for (GameBuilding b : planet.buildings) {
				int currEnergy = b.energy;
				b.energy = (int)(b.getEnergyDemand() * perc);
				// capture value change
				result |= currEnergy != b.energy;
			}			
		}
		return result;
	}
	/**
	 * Allocate the workers uniformly.
	 * @param planet the planet
	 * @return true if the allocation did change properties of the buildings
	 */
	public static boolean uniformWorkerAllocation(GamePlanet planet) {
		boolean result = false;
		int wdemand = planet.getWorkerDemand();
		int wavail = planet.population;
		// if available is more than the demand
		if (wavail >= wdemand || wdemand == 0) {
			// use the demanded values everywhere
			for (GameBuilding b : planet.buildings) {
				int currWorkers = b.workers;
				b.workers = b.getWorkerDemand();
				// capture value change
				result |= currWorkers != b.workers;
			}
		} else {
			float perc = wavail / (float)wdemand;
			// set the worker amount to the proportional
			for (GameBuilding b : planet.buildings) {
				int currWorkers = b.workers;
				b.workers = (int)(b.getWorkerDemand() * perc);
				// capture value change
				result |= currWorkers != b.workers;
			}
		}
		return result;
	}
}
