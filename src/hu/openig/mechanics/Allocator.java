/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.model.Building;
import hu.openig.model.BuildingAllocationWorker;
import hu.openig.model.Planet;
import hu.openig.model.ResourceAllocationStrategy;
import hu.openig.model.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.swing.SwingUtilities;

/**
 * Computes the resource allocations for planets asynchronously.
 * @author akarnokd, 2010.09.15.
 */
public final class Allocator {
	/** Utility class. */
	private Allocator() {
		// utility class
	}
	/**
	 * Submit allocation computation tasks.
	 * @param world the world to compute
	 * @param pool the computation pool
	 * @return a list of Future objects if the caller wants to do something when they are complete
	 */
	public static List<Future<?>> compute(World world, ExecutorService pool) {
		List<Future<?>> result = new ArrayList<>();
		for (final Planet ras : world.planets.values()) {
			if (ras.surface.buildings.size() == 0) {
				continue;
			}
			final List<BuildingAllocationWorker> baw = new ArrayList<>();
			for (Building b : ras.surface.buildings.iterable()) {
				if (b.enabled && b.isComplete()) {
					baw.add(b.getAllocationWorker());
				} else {
					b.assignedWorker = 0;
					b.assignedEnergy = 0;
				}
			}
			final ResourceAllocationStrategy strategy = ras.allocation;
			final int workers = -(int)ras.population();
			Future<?> f = pool.submit(new Runnable() {
				@Override
				public void run() {
					compute(baw, strategy, workers);
					writeBack(baw);
				}
			});
			result.add(f);
		}
		return result;
	}
	/**
	 * Dispatch the computation based on the strategy, then write back the results.
	 * @param baw the building allocation worker
	 * @param strategy the strategy
	 * @param workers the available workers (negative)
	 */
	static void compute(final List<BuildingAllocationWorker> baw,
			final ResourceAllocationStrategy strategy,
			final int workers) {
		switch (strategy) {
		case ZERO:
			doZeroStrategy(baw);
			break;
		case DEFAULT:
//			doUniformStrategy(baw, workers);
			doUniformStrategyWithDamage(baw, workers);
			break;
		case BATTLE:
			doBattleStrategy(baw, workers);
			break;
        case DAMAGE_AWARE:
//          doUniformStrategy(baw, workers);
            doUniformStrategyWithDamage(baw, workers);
            break;
		default:
		}
	}
	/** 
	 * Performs the computation of the planet in the current thread.
	 * @param planet the planet to compute 
	 */
	public static void computeNow(Planet planet) {
		computeNow(planet, planet.allocation);
	}
	/** 
	 * Performs the computation of the planet in the current thread.
	 * @param planet the planet to compute
	 * @param strategy the current strategy 
	 */
	public static void computeNow(Planet planet, final ResourceAllocationStrategy strategy) {
		if (planet.surface.buildings.isEmpty()) {
			return;
		}
        computeNow(planet.surface.buildings.iterable(), strategy, (int)planet.population());
	}
    /**
     * Computes the worker and energy allocation for a sequence of buildings with
     * the given target strategy and available population.
     * @param buildings the building sequence
     * @param strategy the allocation strategy
     * @param population the (positive) population
     */
    public static void computeNow(
        Iterable<Building> buildings, 
        ResourceAllocationStrategy strategy, 
        int population) {
		final List<BuildingAllocationWorker> baw = new ArrayList<>();
		for (Building b : buildings) {
			if (b.enabled) {
				baw.add(b.getAllocationWorker());
			} else {
				b.assignedWorker = 0;
				b.assignedEnergy = 0;
			}
		}
		final int workers = -population;
		compute(baw, strategy, workers);
		for (BuildingAllocationWorker b : baw) {
			b.write();
		}
    }
	/**
	 * Write back the computation results to the underlying building object.
	 * @param ras the resource allocation settings
	 */
	static void writeBack(final List<BuildingAllocationWorker> ras) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				for (BuildingAllocationWorker b : ras) {
					b.write();
				}
			}
		});
	}
	/** 
	 * Zero all assignments. 
	 * @param ras the resource allocation settings to work with
	 */
	static void doZeroStrategy(List<BuildingAllocationWorker> ras) {
		for (BuildingAllocationWorker b : ras) {
			b.energyAllocated = 0;
			b.workerAllocated = 0;
		}
	}
	/** 
	 * Do an uniform distribution strategy. 
	 * @param ras the allocation settings
	 * @param availableWorkers the number of workers available
	 */
	static void doUniformStrategyWithDamage(List<BuildingAllocationWorker> ras, 
			int availableWorkers) {
		int availableWorker = availableWorkers;
		int demandWorker = 0;
		for (BuildingAllocationWorker b : ras) {
			if (Building.isOperational(b.efficiencyBound)) {
				demandWorker += b.workerDemand * b.efficiencyBound;
			} else {
				b.workerAllocated = 0;
				b.energyAllocated = 0;
			}
		}
		// no need to assign workers
		if (demandWorker == 0) {
			return;
		}
		double targetEfficiency = Math.min(1.0, availableWorker / (double)demandWorker);
		
		int demandEnergy = 0;
		double availableEnergy = 0;
		
		for (BuildingAllocationWorker b : ras) {
			if (Building.isOperational(b.efficiencyBound)) {
				int toAssign = (int)(b.workerDemand * b.efficiencyBound * targetEfficiency);
				b.workerAllocated = toAssign > availableWorker ? toAssign : availableWorker;
				availableWorker -= b.workerAllocated;
				double workerPercent = 1.0 * b.workerAllocated / b.workerDemand;
				if (b.producesEnergy) {
					availableEnergy -= b.energyDemand * Math.min(workerPercent, b.efficiencyBound);
				} else {
					demandEnergy += b.energyDemand * Math.min(workerPercent, b.efficiencyBound);
				}
			}
		}
		// no need assign energy: everything is either colonyhub or power plant
		if (demandEnergy == 0) {
			return;
		}
		targetEfficiency = Math.min(1.0f, availableEnergy / demandEnergy);
		
		for (BuildingAllocationWorker b : ras) {
			if (Building.isOperational(b.efficiencyBound)) {
				if (!b.producesEnergy) {
					double workerPercent = 1.0f * b.workerAllocated / b.workerDemand;
					double allocPercent = workerPercent * targetEfficiency;
					
					int toAssign = (int)(b.energyDemand * allocPercent);
					b.energyAllocated = toAssign > availableEnergy ? toAssign : (int)availableEnergy;
					availableEnergy -= b.energyAllocated;
				}
			}
		}
	}
	/**
	 * Allocate workers to power stations to maximize output,
	 * allocate energy to defensive structures and fire brigade.
	 * Allocate the rest similarly to damage aware default.
	 * @param ras the list of buildings
	 * @param availableWorkers the available workers
	 */
	static void doBattleStrategy(List<BuildingAllocationWorker> ras, 
			int availableWorkers) {
		List<BuildingAllocationWorker> energy = new ArrayList<>();
		List<BuildingAllocationWorker> defense = new ArrayList<>();
		List<BuildingAllocationWorker> rest = new ArrayList<>();
		
		for (BuildingAllocationWorker w : ras) {
			boolean op = Building.isOperational(w.efficiencyBound);
			if (w.producesEnergy && op) {
				energy.add(w);
			} else
			if (w.building.type.kind.equals("Defensive")) {
			    // make sure the buildings receive resources till the last moment
			    w.efficiencyBound = 1.0;
				defense.add(w);
			} else 
			if (op) {
				rest.add(w);
			} else {
				w.workerAllocated = 0;
				w.energyAllocated = 0;
			}
		}
		Comparator<BuildingAllocationWorker> costReverse = new Comparator<BuildingAllocationWorker>() {
			@Override
			public int compare(BuildingAllocationWorker o1,
					BuildingAllocationWorker o2) {
				return Double.compare(o2.building.type.cost * o2.efficiencyBound, o1.building.type.cost * o1.efficiencyBound);
			}
		};
		Collections.sort(energy, costReverse);
		
		int energyAvailable = 0;
		for (BuildingAllocationWorker w : energy) {
			int workerDemand = (int)(w.workerDemand * w.efficiencyBound);
			w.workerAllocated = workerDemand > availableWorkers ? workerDemand : availableWorkers;
			energyAvailable -= w.energyDemand * w.workerAllocated / w.workerDemand;
			availableWorkers -= w.workerAllocated;
		}
		List<List<BuildingAllocationWorker>> wss = new ArrayList<>();
		wss.add(defense);
		wss.add(rest);
		for (List<BuildingAllocationWorker> ws : wss) {
			Collections.sort(ws, costReverse);
			for (BuildingAllocationWorker w : ws) {
				int workerDemand = (int)(w.workerDemand * w.efficiencyBound);
				int energyDemand = (int)(w.energyDemand * w.efficiencyBound);
				
				w.workerAllocated = workerDemand > availableWorkers ? workerDemand : availableWorkers;
				w.energyAllocated = energyDemand > energyAvailable ? energyDemand : energyAvailable;
				
				availableWorkers -= w.workerAllocated;
				energyAvailable -= w.energyAllocated;
			}
		}
	}
}
