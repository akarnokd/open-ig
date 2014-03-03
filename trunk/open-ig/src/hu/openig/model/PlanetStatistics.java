/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Record to store the planet wide statistics used by the information screens.
 * @author akarnokd
 */
public class PlanetStatistics {
	/**
	 * The lab statistics record.
	 * @author akarnokd, 2012.08.18.
	 */
	public static class LabStatistics {
		/** The civil engineering laboratory. */
		public int civil;
		/** The mechanical engineering laboratory. */
		public int mech;
		/** The computer laboratory. */
		public int comp;
		/** The AI laboratory. */
		public int ai;
		/** The Military laboratory active count. */
		public int mil;
		/**
		 * Add values from the other lab statistics.
		 * @param other the other lab statistics
		 */
		public void add(LabStatistics other) {
			this.civil += other.civil;
			this.mech += other.mech;
			this.comp += other.comp;
			this.ai += other.ai;
			this.mil += other.mil;
		}
		/**
		 * @return The total lab counts.
		 */
		public int count() {
			return ai + civil + comp + mech + mil;
		}
	}
	/**
	 * Production statitistics record.
	 * @author akarnokd, 2012.08.18.
	 */
	public static class ProductionStatistics {
		/** The equipment factory capacity. */
		public int equipment;
		/** The weapons factory capacity. */
		public int weapons;
		/** The spaceship factory capacity. */
		public int spaceship;
		/**
		 * Add the values from the other production statistics.
		 * @param other the other statistics
		 */
		public void add(ProductionStatistics other) {
			this.equipment += other.equipment;
			this.weapons += other.weapons;
			this.spaceship += other.spaceship;
		}
	}
	/** The available houses. */
	public int houseAvailable;
	/** The worker demand. */
	public int workerDemand;
	/** The available hospitals. */
	public int hospitalAvailable;
	/** The available food. */
	public int foodAvailable;
	/** The available police. */
	public int policeAvailable;
	/** The energy demand. */
	public int energyDemand;
	/** The available energy. */
	public int energyAvailable;
	/** The active production values. */
	public final ProductionStatistics activeProduction = new ProductionStatistics();
	/** The total production values. */
	public final ProductionStatistics production = new ProductionStatistics();
	/** The active laboratories. */
	public final LabStatistics activeLabs = new LabStatistics();
	/** THe total laboratories. */
	public final LabStatistics labs = new LabStatistics();
	/** The current list of problems. */
	public final Set<PlanetProblems> problems = new LinkedHashSet<>();
	/** The current list of warnings. */
	public final Set<PlanetProblems> warnings = new LinkedHashSet<>();
	/** Free repair percent. */
	public double freeRepair = 0;
	/** Free repair efficiency. */
	public double freeRepairEff = 0;
	/** Has a functioning military spaceport. */
	public boolean hasMilitarySpaceport;
	/** Has a functioning trader's spaceport. */
	public boolean hasTradersSpaceport;
	/** Number of orbital factories. */
	public int orbitalFactory;
	/** The figher count. */
	public int fighterCount;
	/** The vehicle count. */
	public int vehicleCount;
	/** The maximum vehicles. */
	public int vehicleMax;
	/** A space station is deployed. */
	public boolean hasSpaceStation;
	/** Building in progress or complete but not yet resource-allocated. */
	public boolean constructing;
	/** The total planet count. */
	public int planetCount;
	/** The total military spaceport count. */
	public int militarySpaceportCount;
	/** The worker demand of all built buildings, whether they are enabled or not. */
	public int nativeWorkerDemand;
	/** The population growth modifier. */
	public double populationGrowthModifier;
	/**
	 * Add the other planet statistics to this one.
	 * @param other the other statistics
	 */
	public void add(PlanetStatistics other) {
		planetCount++;
		houseAvailable += other.houseAvailable;
		workerDemand += other.workerDemand;
		hospitalAvailable += other.hospitalAvailable;
		foodAvailable += other.foodAvailable;
		policeAvailable += other.policeAvailable;
		energyDemand += other.energyDemand;
		energyAvailable += other.energyAvailable;
		
		activeLabs.add(other.activeLabs);
		labs.add(other.labs);
		activeProduction.add(other.activeProduction);
		production.add(other.production);
		
		orbitalFactory += other.orbitalFactory;
		problems.addAll(other.problems);
		warnings.addAll(other.warnings);
		constructing |= other.constructing;
		hasMilitarySpaceport |= other.hasMilitarySpaceport;
		hasSpaceStation |= other.hasSpaceStation;
		hasTradersSpaceport |= other.hasTradersSpaceport;
		militarySpaceportCount += other.militarySpaceportCount;
		nativeWorkerDemand += other.nativeWorkerDemand;
		populationGrowthModifier += other.populationGrowthModifier;
	}
	/**
	 * The planet has the specified problem?
	 * @param probl the problem
	 * @return present
	 */
	public boolean hasProblem(PlanetProblems probl) {
		return problems.contains(probl);
	}
	/** 
	 * Add the given planet problem to the map.
	 * @param probl the planet problem
	 */
	public void addProblem(PlanetProblems probl) {
		problems.add(probl);
	}
	/**
	 * The planet has the specified problem?
	 * @param probl the problem
	 * @return present
	 */
	public boolean hasWarning(PlanetProblems probl) {
		return warnings.contains(probl);
	}
	/** 
	 * Add the given planet problem to the map.
	 * @param probl the planet problem
	 */
	public void addWarning(PlanetProblems probl) {
		warnings.add(probl);
	}
	/**
	 * @return the total number of built labs.
	 */
	public int labCount() {
		return labs.count();
	}
	/**
	 * @return the total number of active labs.
	 */
	public int activeLabCount() {
		return activeLabs.count();
	}
	/**
	 * Check if building is possible on this planet.
	 * @return true if building is possible
	 */
	public boolean canBuildAnything() {
		return !hasProblem(PlanetProblems.COLONY_HUB) && !hasWarning(PlanetProblems.COLONY_HUB);
	}
	/**
	 * Check if any level of the problem exists.
	 * @param prob the problem to check
	 * @return true if problem present
	 */
	public boolean hasAnyProblem(PlanetProblems prob) {
		return hasProblem(prob) || hasWarning(prob);
	}
}
