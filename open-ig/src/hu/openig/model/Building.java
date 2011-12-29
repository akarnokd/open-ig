/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Location;

/**
 * A building instance.
 * @author akarnokd, 2010.01.07.
 */
public class Building {
	/** The building type definition. */
	public final BuildingType type;
	/** The technology ID for selecting a Tileset from the building type. */
	public final String techId;
	/** The tileset used when rendering the building. */
	public final TileSet tileset;
	/** The scaffolding used when rendering the building build. */
	public final Scaffolding scaffolding;
	/** The building's placement. */
	public Location location;
	/** The energy assigned to this building. */
	public int assignedEnergy;
	/** The worker assigned to this building. */
	public int assignedWorker;
	/** The buildup progress up to the top hit point. */
	public int buildProgress;
	/** The hitpoints of this building. */
	public int hitpoints;
	/** The current upgrade. Can be null for plain buildings. */
	public Upgrade currentUpgrade;
	/** The current upgrade level. 0 means no upgrades. */
	public int upgradeLevel;
	/** Is the building enabled. */
	public boolean enabled = true;
	/** Is the building under repair. */
	public boolean repairing;
	/** The allocation worker object. */
	public final BuildingAllocationWorker allocationWorker;
	/**
	 * Constructs a building instance and assigns the prototype model.
	 * @param type the building type
	 * @param techId the technology id
	 */
	public Building(BuildingType type, String techId) {
		this.type = type;
		this.techId = techId;
		this.tileset = type.tileset.get(techId);
		this.scaffolding = type.scaffoldings.get(techId);
		this.allocationWorker = new BuildingAllocationWorker(this);
	}
	/**
	 * Tests wether the given location is within the base footprint of this placed building.
	 * @param a the X coordinate
	 * @param b the Y coordinate
	 * @return does the (a,b) fall into this building?
	 */
	public boolean containsLocation(int a, int b) {
		return a >= location.x && b <= location.y && a < location.x + tileset.normal.width && b > location.y - tileset.normal.height; 
	}
	/**
	 * @return Returns the required worker amount for this building, taking the upgrade level into account.
	 */
	public int getWorkers() {
		return (int)getResource("worker");
	}
	/**
	 * @return Returns the required (&lt;0) or produced (&gt;0) energy amount, taking the upgrade level into account
	 */
	public int getEnergy() {
		int e = (int)getResource("energy");
		if (e >= 0) {
			e = (int)(e * getEfficiency());
		}
		return e;
	}
	/**
	 * @return the operational efficiency
	 */
	public float getEfficiency() {
		if (!enabled) {
			return 0.0f;
		}
		// if the building is incomplete or is more than 50% damaged
		if (buildProgress < type.hitpoints || hitpoints * 2 < type.hitpoints) {
			return 0.0f;
		}
		int workerDemand = getWorkers();
		int energyDemand = (int)getResource("energy");

//		Efficiency doesn't drop to 0 when less than half workers    		
//		if (assignedWorker * 2 > workerDemand) {
//			return 0.0f;
//		}

		// if the building doesn't need energy
		if (energyDemand >= 0) {
			return Math.min(assignedWorker / (float)workerDemand, hitpoints / (float)type.hitpoints);
		}
		if (assignedEnergy * 2 > energyDemand) {
			return 0.0f;
		}
		return Math.min(
				Math.min(assignedEnergy / (float)energyDemand, assignedWorker / (float)workerDemand), 
				hitpoints / (float)type.hitpoints);
	}
	/**
	 * @return test if the building is operational
	 */
	public boolean isOperational() {
		return isOperational(getEfficiency());
	}
	/**
	 * @param efficiency the efficiency value 0..1 
	 * @return tell if an efficiency value indicates an operational building. 
	 */
	public static boolean isOperational(float efficiency) {
		return efficiency > 0.0;
	}
	/**
	 * @return is the building in construction phase?
	 */
	public boolean isConstructing() {
		return buildProgress < type.hitpoints;
	}
	/** @return test if the building is damaged. */
	public boolean isDamaged() {
		return (isConstructing() ? buildProgress : type.hitpoints)  > hitpoints;
	}
	/**
	 * @return is the building or construction severly damaged?
	 */
	public boolean isSeverlyDamaged() {
		return hitpoints * 2 < (isConstructing() ? buildProgress : type.hitpoints);
	}
	/** @return the current damage ratio. */
	public double health() {
		return 1.0 * hitpoints / (isConstructing() ? (buildProgress > 0 ? buildProgress : 1) : type.hitpoints);
	}
	/**
	 * @return is the building destroyed?
	 */
	public boolean isDestroyed() {
		return hitpoints == 0 && buildProgress > 0;
	}
	/** Make the building fully built. */
	public void makeFullyBuilt() {
		buildProgress = type.hitpoints;
		hitpoints = type.hitpoints;
	}
	/**
	 * @return is there an energy shortage situation?
	 */
	public boolean isEnergyShortage() {
		return !isConstructing() && assignedEnergy * 2 > getEnergy() && enabled;
	}
	/**
	 * @return is there a worker shortage situation?
	 */
	public boolean isWorkerShortage() {
		return !isConstructing() && assignedWorker * 2 > getWorkers() && enabled;
	}
	/**
	 * Test if a given resource is present at this building.
	 * @param name the resource name
	 * @return the resource is present?
	 */
	public boolean hasResource(String name) {
		return type.resources.containsKey(name);
	}
	/**
	 * Retrieve a resource for this building and apply any upgrade settings.
	 * The resource must exist, test with hasResource() before using this
	 * @param name the resource name
	 * @return the resource amount
	 */
	public float getResource(String name) {
		Resource res = type.resources.get(name);
		if (currentUpgrade != null) {
			Resource ru = currentUpgrade.getType(name);
			if (ru != null) {
				return res.amount * ru.amount;
			}
		}
		return res.amount;
	}
	/** @return the primary resource adjusted by the efficiency level. */
	public float getPrimary() {
		float res = getResource(type.primary);
		return res * getEfficiency();
	}
	/**
	 * Set the upgrade level.
	 * @param level the upgrade level
	 */
	public void setLevel(int level) {
		this.upgradeLevel = level;
		if (level > 0) {
			this.currentUpgrade = type.upgrades.get(level - 1);
		} else {
			this.currentUpgrade = null;
		}
	}
	/**
	 * @return is the building ready to receive workers and energy, e.g. is enabled, not under construction and is not severly damaged?
	 */
	public boolean isReady() {
		return enabled && !isConstructing() && !isSeverlyDamaged();
	}
	/**
	 * @return returns an allocation worker object for this building
	 */
	public BuildingAllocationWorker getAllocationWorker() {
		allocationWorker.read();
		return allocationWorker;
	}
	/**
	 * @return Create a copy of this building.
	 */
	public Building copy() {
		Building result = new Building(type, techId);
		result.buildProgress = buildProgress;
		result.hitpoints = hitpoints;
		result.setLevel(upgradeLevel);
		result.enabled = enabled;
		result.repairing = repairing;
		result.location = location;
		
		return result;
	}
	/** @return is the building completed? */
	public boolean isComplete() {
		return buildProgress == type.hitpoints;
	}
	/** @return true if this building can be further upgraded */
	public boolean canUpgrade() {
		return upgradeLevel < type.upgrades.size();
	}
}
