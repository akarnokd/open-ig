/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The building allocation worker object to offload resource allocation to a different thread.
 * @author akarnokd, 2010.09.15.
 */
public final class BuildingAllocationWorker {
    /** The back reference to the building. */
    public final Building building;
    /** The nominal energy demand. */
    public int energyDemand;
    /** The nominal worker demand. */
    public int workerDemand;
    /** Does this building produce energy instead of taking it? */
    public boolean producesEnergy;
    /** The upper bound for efficiency: the health ratio. */
    public double efficiencyBound;
    /** The energy allocated for this building. */
    public int energyAllocated;
    /** The worker allocated for this building. */
    public int workerAllocated;
    /**
     * Constructor. Sets the building link
     * @param building the associated building
     */
    public BuildingAllocationWorker(Building building) {
        this.building = building;
    }
    /** Read the settings from the building object. */
    public void read() {
        this.energyDemand = (int)building.getResource("energy");
        this.workerDemand = (int)building.getResource("worker");
        double bound = building.hitpoints / (double)building.type.hitpoints;
        this.efficiencyBound = bound >= 0.5 ? bound : 0.0;
        this.producesEnergy = energyDemand >= 0;
    }
    /** Apply the changes back on the building. */
    public void write() {
        this.building.assignedEnergy = energyAllocated;
        this.building.assignedWorker = workerAllocated;
    }
    /**
     * @return the efficiency level for the current allocation settings
     */
    public double getEfficiency() {
        if (producesEnergy) {
            return Math.min(efficiencyBound, workerAllocated / (double)workerDemand);
        }
        return Math.min(efficiencyBound, Math.min(workerAllocated / (double)workerDemand, energyAllocated / (double)energyDemand));
    }
}
