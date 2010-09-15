/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.model.BuildingAW;
import hu.openig.model.ResourceAllocationSettings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

/**
 * Computes the resource allocations for planets asynchronously.
 * @author karnok, 2010.09.15.
 * @version $Revision 1.0$
 */
public class ResourceAllocator {
	/** The thread pool used for computations. */
	ExecutorService service;
	/**
	 * Constructor. Creates a thread pool with one less than the total core count or 1.
	 */
	public ResourceAllocator() {
		service = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
		((ThreadPoolExecutor)service).setKeepAliveTime(5, TimeUnit.SECONDS);
		((ThreadPoolExecutor)service).allowCoreThreadTimeOut(true);
	}
	/**
	 * Constructor.
	 * @param service use the supplied executor service
	 */
	public ResourceAllocator(ExecutorService service) {
		this.service = service;
	}
	/**
	 * Submit allocation computation tasks.
	 * @param planets the collection of distinct building groups
	 * @return a list of Future objects if the caller wants to do something when they are complete
	 */
	public List<Future<?>> compute(Collection<ResourceAllocationSettings> planets) {
		List<Future<?>> result = new ArrayList<Future<?>>();
		for (final ResourceAllocationSettings ras : planets) {
			result.add(service.submit(new Runnable() {
				@Override
				public void run() {
					doComputationFor(ras);
				}
			}));
		}
		return result;
	}
	/**
	 * Perform the computation for the given settings.
	 * @param ras the resource allocation settings
	 */
	protected void doComputationFor(ResourceAllocationSettings ras) {
		switch (ras.strategy) {
		case ZERO_STRATEGY:
			doZeroStrategy(ras);
			break;
		case DEFAULT_STRATEGY:
			doUniformStrategy(ras);
			break;
		case DAMAGE_AWARE_DEFAULT_STRATEGY:
			doUniformStrategyWithDamage(ras);
			break;
		default:
		}
		writeBack(ras);
	}
	/**
	 * Write back the computation results to the underlying building object.
	 * @param ras the resource allocation settings
	 */
	void writeBack(final ResourceAllocationSettings ras) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				for (BuildingAW b : ras) {
					b.write();
				}
			}
		});
	}
	/** 
	 * Zero all assignments. 
	 * @param ras the resource allocation settings to work with
	 */
	void doZeroStrategy(ResourceAllocationSettings ras) {
		for (BuildingAW b : ras) {
			b.energyAllocated = 0;
			b.workerAllocated = 0;
		}
	}
	/** 
	 * Do an uniform distribution strategy. 
	 * @param ras the allocation settings
	 */
	void doUniformStrategy(ResourceAllocationSettings ras) {
		int availableWorker = ras.availableWorkers;
		int demandWorker = 0;
		int demandEnergy = 0;
		for (BuildingAW b : ras) {
			demandWorker += b.workerDemand;
			demandEnergy += b.producesEnergy ? 0 : b.energyDemand;
		}
		// no need to assign workers
		if (demandWorker == 0) {
			return;
		}
		float targetEfficiency = Math.min(1.0f, availableWorker / (float)demandWorker);
		float availableEnergy = 0;
		for (BuildingAW b : ras) {
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
		
		for (BuildingAW b : ras) {
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
	 */
	void doUniformStrategyWithDamage(ResourceAllocationSettings ras) {
		int availableWorker = ras.availableWorkers;
		int demandWorker = 0;
		int demandEnergy = 0;
		for (BuildingAW b : ras) {
			demandWorker += b.workerDemand * b.efficiencyBound;
			demandEnergy += b.producesEnergy ? 0 : b.energyDemand * b.efficiencyBound;
		}
		// no need to assign workers
		if (demandWorker == 0) {
			return;
		}
		float targetEfficiency = Math.min(1.0f, availableWorker / (float)demandWorker);
		float availableEnergy = 0;
		for (BuildingAW b : ras) {
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
		
		for (BuildingAW b : ras) {
			if (!b.producesEnergy) {
				int toAssign = (int)(b.energyDemand * Math.min(targetEfficiency, b.efficiencyBound));
				b.energyAllocated = toAssign > availableEnergy ? toAssign : (int)availableEnergy;
				availableEnergy -= b.energyAllocated;
			}
		}
	}
}
