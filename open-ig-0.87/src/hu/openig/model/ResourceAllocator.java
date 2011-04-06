/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Act;
import hu.openig.utils.JavaUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Computes the resource allocations for planets asynchronously.
 * @author akarnokd, 2010.09.15.
 */
public class ResourceAllocator {
	/** The thread pool used for computations. */
	final ExecutorService pool;
	/** The world. */
	final List<Planet> world;
	/** The periodic timer to compute the allocation. */
	final Timer timer;
	/**
	 * Constructor.
	 * @param pool the thread pool to use
	 * @param world the world to compute
	 */
	public ResourceAllocator(ExecutorService pool, List<Planet> world) {
		this.pool = pool;
		this.world = world;
		timer = new Timer(1000, new Act() {
			@Override
			public void act() {
				compute();
			}
		});
		timer.setInitialDelay(0);
	}
	/**
	 * Submit allocation computation tasks.
	 * @return a list of Future objects if the caller wants to do something when they are complete
	 */
	public List<Future<?>> compute() {
		return compute(world);
	}	
	/**
	 * Submit allocation computation tasks.
	 * @param planets the collection of distinct building groups
	 * @return a list of Future objects if the caller wants to do something when they are complete
	 */
	public List<Future<?>> compute(Collection<Planet> planets) {
		List<Future<?>> result = new ArrayList<Future<?>>();
		for (final Planet ras : planets) {
			if (ras.surface.buildings.size() == 0) {
				continue;
			}
			final List<BuildingAllocationWorker> baw = JavaUtils.newArrayList();
			for (Building b : ras.surface.buildings) {
				if (b.enabled && b.isComplete()) {
					baw.add(b.getAllocationWorker());
				} else {
					b.assignedWorker = 0;
					b.assignedEnergy = 0;
				}
			}
			final ResourceAllocationStrategy strategy = ras.allocation;
			final int workers = -ras.population;
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
	void compute(final List<BuildingAllocationWorker> baw,
			final ResourceAllocationStrategy strategy,
			final int workers) {
		switch (strategy) {
		case ZERO:
			doZeroStrategy(baw);
			break;
		case DEFAULT:
			doUniformStrategy(baw, workers);
			break;
		case DAMAGE_AWARE:
			doUniformStrategyWithDamage(baw, workers);
			break;
		default:
		}
	}
	/** 
	 * Performs the computation of the planet in the current thread.
	 * @param planet the planet to compute 
	 */
	public void computeNow(Planet planet) {
		if (planet.surface.buildings.size() == 0) {
			return;
		}
		final List<BuildingAllocationWorker> baw = JavaUtils.newArrayList();
		for (Building b : planet.surface.buildings) {
			if (b.enabled) {
				baw.add(b.getAllocationWorker());
			} else {
				b.assignedWorker = 0;
				b.assignedEnergy = 0;
			}
		}
		final ResourceAllocationStrategy strategy = planet.allocation;
		final int workers = -planet.population;
		compute(baw, strategy, workers);
		for (BuildingAllocationWorker b : baw) {
			b.write();
		}
	}
	/**
	 * Write back the computation results to the underlying building object.
	 * @param ras the resource allocation settings
	 */
	void writeBack(final List<BuildingAllocationWorker> ras) {
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
	void doZeroStrategy(List<BuildingAllocationWorker> ras) {
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
	void doUniformStrategy(List<BuildingAllocationWorker> ras, int availableWorkers) {
		int availableWorker = availableWorkers;
		int demandWorker = 0;
		int demandEnergy = 0;
		for (BuildingAllocationWorker b : ras) {
			demandWorker += b.workerDemand;
			demandEnergy += b.producesEnergy ? 0 : b.energyDemand;
		}
		// no need to assign workers
		if (demandWorker == 0) {
			return;
		}
		float targetEfficiency = Math.min(1.0f, availableWorker / (float)demandWorker);
		float availableEnergy = 0;
		for (BuildingAllocationWorker b : ras) {
			int toAssign = Math.round(b.workerDemand * targetEfficiency);
			b.workerAllocated = toAssign > availableWorker ? toAssign : availableWorker;
			availableWorker -= b.workerAllocated;
			if (b.producesEnergy) {
				availableEnergy -= b.energyDemand * b.getEfficiency();
			}
		}
		// no need assign energy: everything is either colonyhub or power plant
		if (demandEnergy == 0) {
			return;
		}
		targetEfficiency = Math.min(1.0f, availableEnergy / demandEnergy);
		
		for (BuildingAllocationWorker b : ras) {
			if (!b.producesEnergy) {
				int toAssign = (int)(b.energyDemand * targetEfficiency);
				b.energyAllocated = toAssign > availableEnergy ? toAssign : (int)availableEnergy;
				availableEnergy -= b.energyAllocated;
			}
		}
	}
	/** 
	 * Do an uniform distribution strategy. 
	 * @param ras the allocation settings
	 * @param availableWorkers the number of workers available
	 */
	void doUniformStrategyWithDamage(List<BuildingAllocationWorker> ras, int availableWorkers) {
		int availableWorker = availableWorkers;
		int demandWorker = 0;
		int demandEnergy = 0;
		for (BuildingAllocationWorker b : ras) {
			demandWorker += b.workerDemand * b.efficiencyBound;
			demandEnergy += b.producesEnergy ? 0 : b.energyDemand * b.efficiencyBound;
		}
		// no need to assign workers
		if (demandWorker == 0) {
			return;
		}
		float targetEfficiency = Math.min(1.0f, availableWorker / (float)demandWorker);
		float availableEnergy = 0;
		for (BuildingAllocationWorker b : ras) {
			int toAssign = (int)(b.workerDemand * Math.min(targetEfficiency, b.efficiencyBound));
			b.workerAllocated = toAssign > availableWorker ? toAssign : availableWorker;
			availableWorker -= b.workerAllocated;
			if (b.producesEnergy) {
				availableEnergy -= b.energyDemand * b.getEfficiency();
			}
		}
		// no need assign energy: everything is either colonyhub or power plant
		if (demandEnergy == 0) {
			return;
		}
		targetEfficiency = Math.min(1.0f, availableEnergy / demandEnergy);
		
		for (BuildingAllocationWorker b : ras) {
			if (!b.producesEnergy) {
				int toAssign = (int)(b.energyDemand * Math.min(targetEfficiency, b.efficiencyBound));
				b.energyAllocated = toAssign > availableEnergy ? toAssign : (int)availableEnergy;
				availableEnergy -= b.energyAllocated;
			}
		}
	}
	/**
	 * Start the timer.
	 */
	public void start() {
		timer.start();
	}
	/**
	 * Stop the timer.
	 */
	public void stop() {
		timer.stop();
	}
}
