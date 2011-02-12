/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The building allocation worker object to offload resource allocation to a different thread.
 * @author karnok, 2010.09.15.
 * @version $Revision 1.0$
 */
public final class BuildingAW {
	/** The back reference to the building. */
	public final Building building;
	/** The nominal energy demand. */
	public int energyDemand;
	/** The nominal worker demand. */
	public int workerDemand;
	/** Does this building produce energy instead of taking it? */
	public boolean producesEnergy;
	/** The upper bound for efficiency: the health ratio. */
	public float efficiencyBound;
	/** The energy allocated for this building. */
	public int energyAllocated;
	/** The worker allocated for this building. */
	public int workerAllocated;
	/**
	 * Constructor. Sets the building link
	 * @param building the associated building
	 */
	public BuildingAW(Building building) {
		this.building = building;
	}
	/** Read the settings from the building object. */
	public void read() {
		this.energyDemand = building.getEnergy();
		this.workerDemand = building.getWorkers();
		this.producesEnergy = energyDemand >= 0;
		this.efficiencyBound = building.hitpoints / (float)building.type.hitpoints;
	}
	/** Apply the changes back on the building. */
	public void write() {
		this.building.assignedEnergy = energyAllocated;
		this.building.assignedWorker = workerAllocated;
	}
	/**
	 * @return the efficiency level for the current allocation settings
	 */
	public float getEfficiency() {
		if (producesEnergy) {
			return Math.min(efficiencyBound, workerAllocated / (float)workerDemand);
		}
		return Math.min(efficiencyBound, Math.min(workerAllocated / (float)workerDemand, energyAllocated / (float)energyDemand));
	}
}
