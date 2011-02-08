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
		case MAX_EFFICIENCY:
			doMaxEfficiency(ras);
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
	/**
	 * Compute the allocations based on the maximum efficiency.
	 * @author Andras Kovacs, MTA SZTAKI
	 * @param ras the resource allocation settings
	 */
	void doMaxEfficiency(ResourceAllocationSettings ras) {
		double nWorkerFree = -ras.availableWorkers;
		double dEnergyFree = 0;		// Energy already produced by PPs, but not used by Fs
		double dEnergyOpen = 0;		// Energy that can be obtained with allocating more workers, but without switching on a new PP 
		double dEnergyPrice = 0;	// Energy price expressed in nWorker
		
		List<BuildingAW> factoriesList = new ArrayList<BuildingAW>();
		List<BuildingAW> powerplantsList = new ArrayList<BuildingAW>();
		
		for (BuildingAW b : ras.buildings) {
			if (b.producesEnergy) {
				powerplantsList.add(b);
			} else {
				factoriesList.add(b);
			}
		}
		/**
		 * the building simplification for this algorithm. 
		 * @author Andras Kovacs, MTA SZTAKI
		 */
		final class B {
			BuildingAW link;
			double wReq;
			double eReq;
//			double level;
			double wAss;
			double eAss;
			/**
			 * Constructor.
			 * @param link the link back to the original object.
			 */
			public B(BuildingAW link) {
				this.link = link;
			}
		};
		B[] factories = new B[factoriesList.size()];
		for (int i = 0; i < factories.length; i++) {
			factories[i] = new B(factoriesList.get(i));
			factories[i].wReq = -factoriesList.get(i).workerDemand;
			factories[i].eReq = -factoriesList.get(i).energyDemand;
		}
		B[] powerplants = new B[powerplantsList.size()];
		for (int i = 0; i < powerplants.length; i++) {
			powerplants[i] = new B(powerplantsList.get(i));
			powerplants[i].wReq = -powerplantsList.get(i).workerDemand;
			powerplants[i].eReq = powerplantsList.get(i).energyDemand;
		}
		
		// If there is free energy available, use it
		// If new PP must be switched on, switch it on
		// If more stuff needed, then add more
		
		while (nWorkerFree > 0) {
			if (dEnergyFree > 0) {
				// Choose factory only
				int iBestF = -1;
				double dBestValF = -1;
				for (int i = 0; i < factories.length; i++) {
					if ((factories[i].wReq + factories[i].eReq * dEnergyPrice < dBestValF || dBestValF == -1) 
							&& (factories[i].wReq > factories[i].wAss)) {
						iBestF = i;
						dBestValF = factories[i].wReq + factories[i].eReq * dEnergyPrice;
					}
				}
				if (iBestF < 0) {
					break;
				}
				// Calculate the limit;
				// 1. Assume limit is factory
				double wF = factories[iBestF].wReq - factories[iBestF].wAss;
				double eF = factories[iBestF].eReq - factories[iBestF].eAss;
				// 2. Assume limit is dEnergyFree
				if (eF > dEnergyFree) {
					eF = dEnergyFree;
					wF = eF * factories[iBestF].wReq / factories[iBestF].eReq;
				}
				// 3. Assume limit is worker pool
				if (wF > nWorkerFree) {
					wF = nWorkerFree;
					eF = wF * factories[iBestF].eReq / factories[iBestF].wReq;
				}
				
				factories[iBestF].wAss += wF;
				factories[iBestF].eAss += eF;
				nWorkerFree -= wF;
				dEnergyFree -= eF;
			} else 
			if (dEnergyOpen <= 0) {
				int iBestPP = -1;
				double dBestValPP = -1;
				for (int i = 0; i < powerplants.length; i++) {
					if ((powerplants[i].wReq / powerplants[i].eReq < dBestValPP || dBestValPP == -1) 
							&& (powerplants[i].wAss == 0)) {
						iBestPP = i;
						dBestValPP = powerplants[i].wReq / powerplants[i].eReq;
					}
				}
				if (iBestPP < 0) {
					break;
				}
				// Calculate the limit;
				// 1. Assume limit is factory
				double wPP = powerplants[iBestPP].wReq / 2;
				// 2. Assume limit is worker pool
				if (wPP > nWorkerFree) {
					break;
				}
				powerplants[iBestPP].wAss = wPP;
				powerplants[iBestPP].eAss = powerplants[iBestPP].eReq / 2;
				nWorkerFree += -wPP;
				dEnergyPrice = dBestValPP;
				dEnergyFree = powerplants[iBestPP].eReq / 2;
				dEnergyOpen = powerplants[iBestPP].eReq / 2;
			} else {
				// Choose power plant and factory
				// Choose a power plant to populate
				int iBestPP = -1;
				double dBestValPP = -1;
				for (int i = 0; i < powerplants.length; i++) {
					if ((powerplants[i].wReq  / powerplants[i].eReq < dBestValPP || dBestValPP == -1) 
							&& (powerplants[i].wReq > powerplants[i].wAss)) {
						iBestPP = i;
						dBestValPP = powerplants[i].wReq / powerplants[i].eReq;
					}
				}
				if (iBestPP < 0) {
					break;
				}
				// Choose a factory to populate
				int iBestF = -1;
				double dBestValF = -1;
				for (int i = 0; i < factories.length; i++) {
					if ((factories[i].wReq + factories[i].eReq * dBestValPP < dBestValF || dBestValF == -1) 
							&& (factories[i].wReq > factories[i].wAss)) {
						iBestF = i;
						dBestValF = factories[i].wReq + factories[i].eReq * dBestValPP;
					}
				}
				if (iBestF < 0) {
					break;
				}
				// Calculate the limit;
				// 1. Assume limit is factory
				double ratio = factories[iBestF].eReq * powerplants[iBestPP].wReq / powerplants[iBestPP].eReq / factories[iBestF].wReq;
				double wF = factories[iBestF].wReq - factories[iBestF].wAss;
				double wPP =  wF * ratio;
				
				// 2. Assume limit is power plant
				if (wPP > powerplants[iBestPP].wReq - powerplants[iBestPP].wAss) {
					wPP = powerplants[iBestPP].wReq - powerplants[iBestPP].wAss;
					wF = wPP / ratio;
				}
				// 3. Assume limit is worker pool
				if (wF + wF * ratio > nWorkerFree) {
					wF = nWorkerFree / (1 + ratio);
					wPP =  wF * ratio;
				}
				
				double e = wPP / dBestValPP;
				
				powerplants[iBestPP].wAss += wPP;
				powerplants[iBestPP].eAss += e;
				factories[iBestF].wAss += wF;
				factories[iBestF].eAss += e;
				nWorkerFree += -wPP - wF;
				dEnergyPrice = dBestValPP;
				dEnergyOpen -= e;
			}
		}
		
		for (int i = 0; i < powerplants.length; i++) {
//			System.out.println("PP[" + i + "]:  W" + powerplants[i].wAss + "  E" + powerplants[i].eAss + "  --> " + powerplants[i].wAss / powerplants[i].wReq);
			powerplants[i].link.workerAllocated = -(int)powerplants[i].wAss;
		}
//		System.out.println("----------------------------");			
		for (int i = 0; i < factories.length; i++) {
//			System.out.println("F[" + i + "]:  W" + factories[i].wAss + "  E" + factories[i].eAss + "  --> " + 
//					Math.min(factories[i].wAss / factories[i].wReq, factories[i].eAss / factories[i].eReq));	
			factories[i].link.workerAllocated = -(int)factories[i].wAss;
			factories[i].link.energyAllocated = -(int)factories[i].eAss;
		}
//		System.out.println("----------------------------");			

// Post-processing:
		// Go through factories, switch off under-stuffed ones (maybe several). 
		// As soon as saved workers & energy is sufficient to switch on the next under-stuffed one, assign all.  
		
	}
}
