/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Location;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.Comparator;

/**
 * A building instance.
 * @author akarnokd, 2010.01.07.
 */
public class Building implements HasLocation {
	/** The building's unique id. */
	public final int id;
	/** The building type definition. */
	public final BuildingType type;
	/** The race of the building. */
	public final String race;
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
	 * @param id the building's unique id
	 * @param type the building type
	 * @param race the technology id
	 */
	public Building(int id, BuildingType type, String race) {
		this.id = id;
		this.type = type;
		this.race = race;
		this.tileset = type.tileset.get(race);
		this.scaffolding = type.scaffoldings.get(race);
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
	public double getEfficiency() {
		if (!enabled) {
			return 0.0;
		}
		// if the building is incomplete or is more than 50% damaged
		if (hitpoints * 2 < type.hitpoints) {
			return 0.0;
		}
		return getSpacewarEfficiency();
	}
	/**
	 * Calculate the spacewar efficiency.
	 * @return the efficiency value
	 */
	public double getSpacewarEfficiency() {
		if (buildProgress < type.hitpoints) {
			return 0d;
		}
		int workerDemand = getWorkers();
		int energyDemand = (int)getResource("energy");

		// if the building doesn't need energy
		if (energyDemand >= 0) {
			return Math.min(assignedWorker / (double)workerDemand, hitpoints / (double)type.hitpoints);
		}
		if (assignedEnergy * 2 > energyDemand) {
			return 0.0f;
		}
		return Math.min(
				Math.min(assignedEnergy / (double)energyDemand, assignedWorker / (double)workerDemand), 
				hitpoints / (double)type.hitpoints);
	}
	/**
	 * @return test if the building is operational
	 */
	public boolean isOperational() {
		return isOperational(getEfficiency());
	}
	/**
	 * Check if the building is spacewar-ready.
	 * @return true if spacewar-ready
	 */
	public boolean isSpacewarOperational() {
		return isOperational(getSpacewarEfficiency());
	}
	/**
	 * @param efficiency the efficiency value 0..1 
	 * @return tell if an efficiency value indicates an operational building. 
	 */
	public static boolean isOperational(double efficiency) {
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
		return isConstructing() ? 1 : 1.0 * hitpoints / type.hitpoints;
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
	public double getResource(String name) {
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
	public double getPrimary() {
		double res = getResource(type.primary);
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
	 * Create a copy of this building.
	 * @param id the unique id of the new building
	 * @return the new building object
	 */
	public Building copy(int id) {
		Building result = new Building(id, type, race);

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
		int maxUpgrades = type.upgrades.size();
		return upgradeLevel < maxUpgrades;
	}
	/**
	 * Returns true if the given upgrade level is accessible to this building. 
	 * @param newLevel the upgrade level
	 * @return true if this building can be further upgraded 
	 */
	public boolean canUpgrade(int newLevel) {
		int maxUpgrades = type.upgrades.size();
		return upgradeLevel < maxUpgrades && newLevel <= maxUpgrades;
	}
	/**
	 * Returns the cost of upgrading to the given new level.
	 * @param newLevel the new level
	 * @return the cost
	 */
	public int upgradeCost(int newLevel) {
		int diff = newLevel - upgradeLevel;
		return type.cost * diff;
	}
	@Override
	public String toString() {
		
		return type.id + " (hp = " + hitpoints + ")";
	}
	/** @return the instance width */
	public int width() {
		return tileset.normal.width;
	}
	/** @return the instance height. */
	public int height() {
		return tileset.normal.height;
	}
	@Override
	public Double exactLocation() {
		return new Point2D.Double(location.x + width() / 2d, location.y - height() / 2d);
	}
	@Override
	public Location location() {
		return location;
	}
	/**
	 * Returns the building status record.
	 * @return the building status record
	 */
	public BuildingStatus toBuildingStatus() {
		BuildingStatus result = new BuildingStatus();
		
		result.id = id;
		result.type = type.id;
		result.race = race;
		result.x = location.x;
		result.y = location.y;
		result.assignedEnergy = assignedEnergy;
		result.assignedWorker = assignedWorker;
		result.buildProgress = buildProgress;
		result.hitpoints = hitpoints;
		result.upgradeLevel = upgradeLevel;
		result.enabled = enabled;
		result.repairing = repairing;
		
		return result;
	}
	/**
	 * Assigns values from the building status record.
	 * Note that this method can't create a new building.
	 * @param st the status record
	 */
	public void fromBuildingStatus(BuildingStatus st) {
		location = Location.of(st.x, st.y);
		assignedEnergy = st.assignedEnergy;
		assignedWorker = st.assignedWorker;
		buildProgress = st.buildProgress;
		hitpoints = st.hitpoints;
		upgradeLevel = st.upgradeLevel;
		enabled = st.enabled;
		repairing = st.repairing;
	}
	/**
	 * Returns the bounding rectangle of this building (without offset).
	 * @return the bounding rectangle 
	 */
	public Rectangle rectangle() {
		int a0 = location.x;
		int b0 = location.y;
		int x = Tile.toScreenX(a0, b0);
		int y = Tile.toScreenY(a0, b0 - tileset.normal.height + 1) + 27;
		
		return new Rectangle(x, y - tileset.normal.imageHeight, 
				tileset.normal.imageWidth, tileset.normal.imageHeight);

	}
	/**
	 * Returns the center of the footprint's bounding rectangle (without offset).
	 * @return the point
	 */
	public Point center() {
		double cx = location.x + width() / 2d;
		double cy = location.y - height() / 2d;
		
		return new Point((int)(Tile.toScreenX(cx, cy) + 28), 
				(int)(Tile.toScreenY(cx, cy) + 14));
	}
	/**
	 * Compare the levels of two buildings.
	 */
	public static final Comparator<Building> COMPARE_LEVEL = new Comparator<Building>() {
		@Override
		public int compare(Building o1, Building o2) {
			return Integer.compare(o1.upgradeLevel, o2.upgradeLevel);
		}
	};
	/**
	 * Compare the build costs of two buildings.
	 */
	public static final Comparator<Building> COMPARE_COST = new Comparator<Building>() {
		@Override
		public int compare(Building o1, Building o2) {
			return Integer.compare(o1.type.cost, o2.type.cost);
		}
	};
}
