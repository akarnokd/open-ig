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
	 * Constructor. Sets the allocation strategies
	 */
	private ResourceAllocator() {
	}
	/**
	 * Allocate energy uniformly.
	 * @param planet the planet
	 */
	public static void uniformEnergyAllocation(GamePlanet planet) {
		int eavail = planet.getEnergyProduction();
		int eneed = planet.getEnergyDemand();
		// if there is enough energy, give everyone its demand
		if (eneed <= eavail) {
			for (GameBuilding b : planet.buildings) {
				// for enerergy consuming buildings
				if (b.requiresEnergy()) {
					b.energy = b.getEnergy();
				}
			}
		} else {
			// count the energy consuming buildings
			int count = 0;
			for (GameBuilding b : planet.buildings) {
				// for enerergy consuming buildings
				if (b.requiresEnergy()) {
					count++;
				}
			}
			// if there was at least one energy demanding building
			if (count > 0) {
				int e = eavail / count;
				for (GameBuilding b : planet.buildings) {
					// for enerergy consuming buildings
					if (b.requiresEnergy()) {
						b.energy = e;
					}
				}
			}
		}
	}
	/**
	 * Allocate the workers uniformly.
	 * @param planet the planet
	 */
	public static void uniformWorkerAllocation(GamePlanet planet) {
		int wavail = planet.population;
		int wneed = planet.getWorkerDemand();
		if (wavail >= wneed) {
			for (GameBuilding b : planet.buildings) {
				if (b.requiresWorkers()) {
					b.workers = b.getWorkerDemand();
				}
			}
		} else {
			int count = 0;
			for (GameBuilding b : planet.buildings) {
				// for enerergy consuming buildings
				if (b.requiresWorkers()) {
					count++;
				}
			}
			// if there was at least one energy demanding building
			if (count > 0) {
				int w = wavail / count;
				for (GameBuilding b : planet.buildings) {
					// for enerergy consuming buildings
					if (b.requiresWorkers()) {
						b.workers = w;
					}
				}
			}
		}
	}
}
