/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	 * @param eavail the available energy
	 * @param edemand the demanded energy
	 * @return true if the allocation did change properties of the buildings
	 */
	public static boolean uniformEnergyAllocation(GamePlanet planet, int eavail, int edemand) {
		boolean result = false;
		float perc = eavail / (float)edemand;
		for (GameBuilding b : planet.buildings) {
			int currEnergy = b.energy;
			b.energy = (int)(b.getEnergyDemand() * perc);
			// capture value change
			result |= currEnergy != b.energy;
		}			
		return result;
	}
	/**
	 * Allocate the workers uniformly.
	 * @param planet the planet
	 * @param wavail the avaliable worker amount
	 * @param wdemand the total worker demand
	 * @return true if the allocation did change properties of the buildings
	 */
	public static boolean uniformWorkerAllocation(GamePlanet planet, int wavail, int wdemand) {
		boolean result = false;
		float perc = wavail / (float)wdemand;
		// set the worker amount to the proportional
		for (GameBuilding b : planet.buildings) {
			int currWorkers = b.workers;
			b.workers = (int)(b.getWorkerDemand() * perc);
			// capture value change
			result |= currWorkers != b.workers;
		}
		return result;
	}
	/**
	 * Allocates workers to the buildings based on the worker-demand/utility factor.
	 * @param planet the planet
	 * @param util the utility callback
	 * @return true if the allocation did change properties of the buildings
	 */
	public static boolean utilityWorkerAllocation(GamePlanet planet, BuildingUtility util) {
		List<Tuple2<GameBuilding, Float>> buildings = new ArrayList<Tuple2<GameBuilding, Float>>();
		boolean result = false;
		for (GameBuilding b : planet.buildings) {
			int w = b.getWorkerDemand();
			if (w > 0) {
				buildings.add(new Tuple2<GameBuilding, Float>(b, util.getUtility(b)));
			}
		}
		Collections.sort(buildings, new Comparator<Tuple2<GameBuilding, Float>>() {
			@Override
			public int compare(Tuple2<GameBuilding, Float> o1,
					Tuple2<GameBuilding, Float> o2) {
				return o1.second.compareTo(o2.second);
			}
		});
		int workerAvail = planet.population;
		for (Tuple2<GameBuilding, Float> e : buildings) {
			GameBuilding b = e.first;
			int w = b.getWorkerDemand();
			if (workerAvail > w) {
				result |= b.workers != w;
				b.workers = w;
				workerAvail -= w;
			} else {
				result |= b.workers != workerAvail;
				b.workers = workerAvail;
				workerAvail = 0;
			}
		}
		return result;
	}
	/**
	 * Allocates energy to the buildings based on the utility factor.
	 * @param planet the planet
	 * @param util the utility callback
	 * @param eavail the available energy
	 * @return true if the allocation did change properties of the buildings
	 */
	public static boolean utilityEnergyAllocation(GamePlanet planet, BuildingUtility util, int eavail) {
		List<Tuple2<GameBuilding, Float>> buildings = new ArrayList<Tuple2<GameBuilding, Float>>();
		boolean result = false;
		for (GameBuilding b : planet.buildings) {
			int w = b.getEnergyDemand();
			if (w > 0) {
				buildings.add(new Tuple2<GameBuilding, Float>(b, util.getUtility(b)));
			}
		}
		Collections.sort(buildings, new Comparator<Tuple2<GameBuilding, Float>>() {
			@Override
			public int compare(Tuple2<GameBuilding, Float> o1,
					Tuple2<GameBuilding, Float> o2) {
				return o2.second.compareTo(o1.second);
			}
		});
		int energyAvail = eavail;
		for (Tuple2<GameBuilding, Float> t : buildings) {
			GameBuilding b = t.first;
			int e = b.getEnergyDemand();
			if (energyAvail > e) {
				result |= b.energy != e;
				b.energy = e;
				energyAvail -= e;
			} else {
				result |= b.energy != energyAvail;
				b.energy = energyAvail;
				energyAvail = 0;
			}
		}
		return result;
	}
	/**
	 * Utility based on the power consumption value.
	 */
	public static final BuildingUtility POWER_WEIGHTED_UTILITY = new BuildingUtility() {
		@Override
		public float getUtility(GameBuilding building) {
			return building.prototype.energy;
		}
	};
	/**
	 * Utility based the worker consumption value.
	 */
	public static final BuildingUtility WORKER_WEIGHTED_UTILITY = new BuildingUtility() {
		@Override
		public float getUtility(GameBuilding building) {
			return building.prototype.workers;
		}
	};
	/**
	 * Utility based on kind, prefers living conditions enhancing buildins.
	 */
	public static final BuildingUtility LIVING_CONDITIONS_UTILITY = new BuildingUtility() {
		private final Set<String> preferences = new HashSet<String>(
				Arrays.asList("Energy", "House", "Food", "Social"));
		@Override
		public float getUtility(GameBuilding building) {
			if (preferences.contains(building.prototype.kind)) {
				return 1;
			}
			return 0;
		}
	};
	/**
	 * Utility based on kind, prefers production related buildings.
	 */
	public static final BuildingUtility PRODUCTION_UTILITY = new BuildingUtility() {
		private final Set<String> preferences = new HashSet<String>(
				Arrays.asList("Energy", "Factory"));
		@Override
		public float getUtility(GameBuilding building) {
			if (preferences.contains(building.prototype.kind)) {
				return 1;
			}
			return 0;
		}
	};
	/**
	 * Utility based on kind, prefers scientific buildings.
	 */
	public static final BuildingUtility SCIENCE_UTILITY = new BuildingUtility() {
		private final Set<String> preferences = new HashSet<String>(
				Arrays.asList("Energy", "Science"));
		@Override
		public float getUtility(GameBuilding building) {
			if (preferences.contains(building.prototype.kind)) {
				return 1;
			}
			return 0;
		}
	};
	/**
	 * Utility based on kind, prefers scientific buildings.
	 */
	public static final BuildingUtility ECONOMIC_UTILITY = new BuildingUtility() {
		private final Set<String> preferences = new HashSet<String>(
				Arrays.asList("Energy", "Economic"));
		@Override
		public float getUtility(GameBuilding building) {
			if (preferences.contains(building.prototype.kind)) {
				return 1;
			}
			return 0;
		}
	};
	/**
	 * Utility based on kind, prefers scientific buildings.
	 */
	public static final BuildingUtility MILITARY_UTILITY = new BuildingUtility() {
		private final Set<String> preferences = new HashSet<String>(
				Arrays.asList("Energy", "Military", "Radar", "Gun", "Shield", "Defensive"));
		@Override
		public float getUtility(GameBuilding building) {
			if (preferences.contains(building.prototype.kind)) {
				return 1;
			}
			return 0;
		}
	};
	/**
	 * Utility based on kind, prefers scientific buildings.
	 */
	public static final BuildingUtility ENERGY_OPERATIONAL_UTILITY = new BuildingUtility() {
		@Override
		public float getUtility(GameBuilding building) {
			float result = 0;
			int e = building.getEnergyDemand();
			if (e > 0) {
				result = 1.0f / e;
			}
			return result;
		}
	};
	/**
	 * Utility based on kind, prefers scientific buildings.
	 */
	public static final BuildingUtility WORKER_OPERATIONAL_UTILITY = new BuildingUtility() {
		@Override
		public float getUtility(GameBuilding building) {
			float result = 1;
			int w = building.getWorkerDemand();
			if (!building.isProductionDependantOnOperationLevel()) {
				w = w / 2 + 1; // half energy is enough for that
			}
			if (w > 0) {
				result /= w;
			}
			return result;
		}
	};
	/**
	 * Allocate workers based on the planet's allocation preference.
	 * @param planet the planet
	 * @return true if allocation has changed values on the buildings
	 */
	public static boolean allocateWorkers(GamePlanet planet) {
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
			switch (planet.workerAllocation) {
			case UNIFORM:
				result = uniformWorkerAllocation(planet, wavail, wdemand);
				break;
			case LIVING_CONDITIONS:
				result = utilityWorkerAllocation(planet, LIVING_CONDITIONS_UTILITY);
				break;
			case ECONOMIC:
				result = utilityWorkerAllocation(planet, ECONOMIC_UTILITY);
				break;
			case MILITARY:
				result = utilityWorkerAllocation(planet, MILITARY_UTILITY);
				break;
			case OPERATIONAL:
				result = utilityWorkerAllocation(planet, WORKER_OPERATIONAL_UTILITY);
				break;
			case PRODUCTION:
				result = utilityWorkerAllocation(planet, PRODUCTION_UTILITY);
				break;
			case SCIENCE:
				result = utilityWorkerAllocation(planet, SCIENCE_UTILITY);
				break;
			default:
				// do nothing
			}
		}
		return result;
	}
	/**
	 * Allocate energy based on the planet's allocation preference.
	 * @param planet the planet
	 * @return true if allocation has changed values on the buildings
	 */
	public static boolean allocateEnergy(GamePlanet planet) {
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
		switch (planet.energyAllocation) {
			case UNIFORM:
				result = uniformEnergyAllocation(planet, eavail, edemand);
				break;
			case LIVING_CONDITIONS:
				result = utilityEnergyAllocation(planet, LIVING_CONDITIONS_UTILITY, eavail);
				break;
			case ECONOMIC:
				result = utilityEnergyAllocation(planet, ECONOMIC_UTILITY, eavail);
				break;
			case MILITARY:
				result = utilityEnergyAllocation(planet, MILITARY_UTILITY, eavail);
				break;
			case OPERATIONAL:
				result = utilityEnergyAllocation(planet, ENERGY_OPERATIONAL_UTILITY, eavail);
				break;
			case PRODUCTION:
				result = utilityEnergyAllocation(planet, PRODUCTION_UTILITY, eavail);
				break;
			case SCIENCE:
				result = utilityEnergyAllocation(planet, SCIENCE_UTILITY, eavail);
				break;
			default:
				// do nothing
		}
		}
		return result;
	}
}
