/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Difficulty;
import hu.openig.core.Location;
import hu.openig.model.BattleProjectile.Mode;
import hu.openig.utils.Exceptions;
import hu.openig.utils.U;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A fleet.
 * @author akarnokd, 2010.01.07.
 */
public class Fleet implements Named, Owned, HasInventory {
	/** The unique fleet identifier. */
	public final int id;
	/** The owner of the fleet. */
	public final Player owner;
	/** The X coordinate. */
	public double x;
	/** The Y coordinate. */
	public double y;
	/** The radar radius. */
	public int radar;
	/** The fleet name. */
	public String name;
	/** The fleet inventory: ships and tanks. */
	public final InventoryItems inventory = new InventoryItems();
	/** The current list of movement waypoints. */
	public final List<Point2D.Double> waypoints = new ArrayList<>();
	/** If the fleet should follow the other fleet. */
	public Fleet targetFleet;
	/** If the fleet should move to the planet. */
	private Planet targetPlanet;
	/** If the fleet was moved to a planet. */
	public Planet arrivedAt;
	/** The fleet movement mode. */
	public FleetMode mode;
	/** The current task. */
	public FleetTask task = FleetTask.IDLE;
	/** Refill once. */
	public boolean refillOnce;
	/** The formation index. */
	public int formation;
	/**
	 * Create a fleet with a specific ID and owner.
	 * @param id the identifier
	 * @param owner the owner
	 */
	public Fleet(int id, Player owner) {
		this.id = id;
		this.owner = owner;
		this.owner.fleets.put(this, FleetKnowledge.FULL);
		this.owner.world.fleets.put(id, this);
		owner.statistics.fleetsCreated.value++;
	}
	/**
	 * Create a new fleet for the specific player and automatic ID.
	 * @param owner the owner
	 */
	public Fleet(Player owner) {
		this(owner.world.newId(), owner);
	}
	/**
	 * Set the new target planet and save the current target into {@code arrivedAt}.
	 * @param p the new target planet
	 */
	public void targetPlanet(Planet p) {
		if (p == null) {
			arrivedAt = targetPlanet;
		} else {
			arrivedAt = null;
		}
		targetPlanet = p;
	}
	/**
	 * Returns the current target planet.
	 * @return the current target planet
	 */
	public Planet targetPlanet() {
		return targetPlanet;
	}
	@Override
	public String name() {
		return name;
	}
	@Override
	public Player owner() {
		return owner;
	}
	/**
	 * Returns the number of items of the give research type.
	 * @param rt the research type to count
	 * @return the count
	 */
	public int inventoryCount(ResearchType rt) {
		int count = 0;
		for (InventoryItem pii : inventory.findByType(rt.id)) {
			count += pii.count;
		}
		return count;
	}
	/**
	 * Returns the number of items of the give category of the given owner.
	 * @param cat the research sub-category
	 * @return the count
	 */
	public int inventoryCount(ResearchSubCategory cat) {
		int count = 0;
		for (InventoryItem pii : inventory.iterable()) {
			if (pii.type.category == cat) {
				count += pii.count;
			}
		}
		return count;
	}
	/**
	 * @return calculate the fleet statistics. 
	 */
	public FleetStatistics getStatistics() {
		FleetStatistics result = new FleetStatistics();

		int baseSpeed = 14;
		Trait t = owner.traits.trait(TraitKind.PRE_WARP);
		if (t != null) {
			baseSpeed = (int)t.value;
		}
		
		result.speed = Integer.MAX_VALUE;
		int radar = 0;
		for (InventoryItem fii : inventory.iterable()) {
			boolean checkHyperdrive = false;
			boolean checkFirepower = false;
			boolean checkRadar = false;
			if (fii.type.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS) {
				result.battleshipCount += fii.count;
				checkHyperdrive = true;
				checkFirepower = true;
				checkRadar = true;
			} else
			if (fii.type.category == ResearchSubCategory.SPACESHIPS_CRUISERS) {
				result.cruiserCount += fii.count;
				checkHyperdrive = true;
				checkFirepower = true;
				checkRadar = true;
			} else
			if (fii.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
				result.fighterCount += fii.count;
				checkFirepower = true;
			} else
			if (fii.type.category == ResearchSubCategory.WEAPONS_TANKS
					|| fii.type.category == ResearchSubCategory.WEAPONS_VEHICLES
			) {
				result.vehicleCount += fii.count;
				
				BattleGroundVehicle v = owner.world.battle.groundEntities.get(fii.type.id);
				if (v != null) {
					result.groundFirepower += v.damage(fii.owner);
				}
				
			}
			
			if (fii.type.has(ResearchType.PARAMETER_VEHICLES)) {
				result.vehicleMax += fii.type.getInt(ResearchType.PARAMETER_VEHICLES); 
			}
			boolean speedFound = false;
			for (InventorySlot slot : fii.slots.values()) {
				if (slot.type != null && slot.count > 0) {
					if (checkRadar && slot.type.has(ResearchType.PARAMETER_RADAR)) {
						radar = Math.max(radar, slot.type.getInt(ResearchType.PARAMETER_RADAR)); 
					}
					if (slot.type.has(ResearchType.PARAMETER_VEHICLES)) {
						result.vehicleMax += slot.type.getInt(ResearchType.PARAMETER_VEHICLES); 
					}
					if (checkHyperdrive && slot.type.has(ResearchType.PARAMETER_SPEED)) {
						speedFound = true;
						result.speed = Math.min(slot.type.getInt(ResearchType.PARAMETER_SPEED), result.speed);
					}
					if (checkFirepower && slot.type.has(ResearchType.PARAMETER_PROJECTILE)) {
						BattleProjectile bp = owner.world.battle.projectiles.get(slot.type.get(ResearchType.PARAMETER_PROJECTILE));
						if (bp != null && bp.mode == Mode.BEAM) {
							double dmg = bp.damage(owner);
							result.firepower += slot.count * dmg * fii.count;
						}
					}
				}
			}
			if (checkHyperdrive && !speedFound) {
				result.speed = baseSpeed;
			}
		}
		
		if (result.speed == Integer.MAX_VALUE) {
			result.speed = baseSpeed;
		}
		
		result.planet = nearbyPlanet();
		
		if (!inventory.isEmpty() && radar == 0) {
			radar = 12;
		} else {
			radar *= owner.world.params().fleetRadarUnitSize();
		}
		this.radar = radar;
		
		return result;
	}
	/**
	 * Calculate the speed value of the fleet.
	 * @return the speed
	 */
	public int getSpeed() {
		int baseSpeed = 14;
		Trait t = owner.traits.trait(TraitKind.PRE_WARP);
		if (t != null) {
			baseSpeed = (int)t.value;
		}
		int speed = Integer.MAX_VALUE;
		for (InventoryItem fii : inventory.iterable()) {
			boolean checkHyperdrive = fii.type.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS 
					|| fii.type.category == ResearchSubCategory.SPACESHIPS_CRUISERS;
			if (checkHyperdrive) {
				boolean found = false;
				for (InventorySlot is : fii.slots.values()) {
					if (is.type != null && is.type.has(ResearchType.PARAMETER_SPEED)) {
						found = true;
						speed = Math.min(speed, is.type.getInt(ResearchType.PARAMETER_SPEED));
					}
				}
				if (!found) {
					speed = baseSpeed;
				}
			}
		}
		if (speed == Integer.MAX_VALUE) {
			speed = baseSpeed;
		}
		return speed;
	}
	/**
	 * @return Returns the nearest planet or null if out of range. 
	 */
	public Planet nearbyPlanet() {
		double dmin = Integer.MAX_VALUE; 
		Planet pmin = null;
		for (Planet p : owner.planets.keySet()) {
			double d = Math.hypot(p.x - x, p.y - y);
			if (d < dmin && d <= owner.world.params().nearbyDistance()) {
				dmin = d;
				pmin = p;
			}
		}
		if (pmin != null) {
			return pmin;
		}
		return null;
	}
	/** @return the non-fighter and non-vehicular inventory items. */
	public List<InventoryItem> getSingleItems() {
		List<InventoryItem> result = new ArrayList<>();
		for (InventoryItem ii : inventory.iterable()) {
			if (ii.type.category != ResearchSubCategory.SPACESHIPS_FIGHTERS
					&& ii.type.category != ResearchSubCategory.WEAPONS_TANKS
					&& ii.type.category != ResearchSubCategory.WEAPONS_VEHICLES
				) {
				result.add(ii);
			}
		}
		return result;
	}
	/**
	 * Retrieve the first inventory item with the given type.
	 * @param rt the type
	 * @return the inventory item or null if not present
	 */
	public InventoryItem getInventoryItem(ResearchType rt) {
		for (InventoryItem ii : inventory.findByType(rt.id)) {
			return ii;
		}
		return null;
	}
	/**
	 * Compute how many of the supplied items can be added without violating the limit constraints. 
	 * @param rt the item type
	 * @return the currently alloved
	 */
	public int getAddLimit(ResearchType rt) {
		FleetStatistics fs = getStatistics();
		switch (rt.category) {
		case SPACESHIPS_BATTLESHIPS:
			return owner.world.params().battleshipLimit() - fs.battleshipCount;
		case SPACESHIPS_CRUISERS:
			return owner.world.params().mediumshipLimit() - fs.cruiserCount;
		case SPACESHIPS_FIGHTERS:
			return owner.world.params().fighterLimit() - inventoryCount(rt);
		case WEAPONS_TANKS:
		case WEAPONS_VEHICLES:
			return fs.vehicleMax - fs.vehicleCount;
		default:
			return 0;
		}
	}
	/** 
	 * Returns a list of same-owned fleets within the given radius.
	 * @param limit the radius
	 * @return the list of nearby fleets
	 */
	public List<Fleet> fleetsInRange(float limit) {
		List<Fleet> result = new ArrayList<>();
		for (Fleet f : owner.fleets.keySet()) {
			if (f.owner == owner && f != this) {
				double dist = (x - f.x) * (x - f.x) + (y - f.y) * (y - f.y);
				if (dist <= limit * limit) {
					result.add(f);
				}
			}
		}
		return result;
	}
	@Override
	public InventoryItems inventory() {
		return inventory;
	}
	/**
	 * Lose vehicles due limited capacity.
	 * @param to the other player who was responsible for the losses
	 * @return the total number of vehicles lost
	 */
	public int loseVehicles(Player to) {
		int vehicleCount = 0;
		int vehicleMax = 0;
		int result = 0;
		List<InventoryItem> veh = new ArrayList<>();
		for (InventoryItem fii : inventory.iterable()) {
			if (fii.type.category == ResearchSubCategory.WEAPONS_TANKS
					|| fii.type.category == ResearchSubCategory.WEAPONS_VEHICLES) {
				vehicleCount += fii.count;
				veh.add(fii);
			}
			if (fii.type.has(ResearchType.PARAMETER_VEHICLES)) {
				vehicleMax += fii.type.getInt(ResearchType.PARAMETER_VEHICLES); 
			}
			for (InventorySlot slot : fii.slots.values()) {
				if (slot.type != null) {
					if (slot.type.has(ResearchType.PARAMETER_VEHICLES)) {
						vehicleMax += slot.type.getInt(ResearchType.PARAMETER_VEHICLES); 
					}
				}
			}
		}
		while (vehicleCount > vehicleMax && veh.size() > 0) {
			InventoryItem fii = ModelUtils.random(veh);
			if (fii.count > 0) {
				fii.count--;
				vehicleCount--;
				result++;
				if (fii.count <= 0) {
					inventory.remove(fii);
					veh.remove(fii);
				}
				
				fii.owner.statistics.vehiclesLost.value++;
				fii.owner.statistics.vehiclesLostCost.value += fii.type.productionCost;
				
				to.statistics.vehiclesDestroyed.value++;
				to.statistics.vehiclesDestroyedCost.value += fii.type.productionCost;
			}
		}
		return result;
	}
	/**
	 * Upgrade equipments according to the best available from the owner's inventory.
	 * Fill in fighters and tanks as well
	 * <p>Ensure that the fleet is over a military spaceport.</p>
	 */
	public void upgradeAll() {
		strip();
		List<InventoryItem> iis = inventory.list();
		Collections.sort(iis, new Comparator<InventoryItem>() {
			@Override
			public int compare(InventoryItem o1, InventoryItem o2) {
				return U.compare(o2.type.productionCost, o1.type.productionCost);
			}
		});
		// walk
		for (InventoryItem ii : iis) {
			if (ii.type.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS
					|| ii.type.category == ResearchSubCategory.SPACESHIPS_CRUISERS) {
				ii.upgradeSlots();
			}
//			ii.shield = Math.max(0, ii.shieldMax());
		}
		
		for (ResearchType rt : owner.available().keySet()) {
			if (rt.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
				int count = Math.min(30, owner.inventoryCount(rt));
				deployItem(rt, count);
			}
		}
		upgradeVehicles(getStatistics().vehicleMax);
	}
	/**
	 * Upgrade the vehicles based on the given vehicle limit.
	 * The current vehicle count should be zero
	 * @param vehicleMax the vehicle maximum
	 */
	public void upgradeVehicles(int vehicleMax) {
		VehiclePlan plan = new VehiclePlan();
		plan.calculate(owner.available().keySet(), 
				owner.world.battle, 
				vehicleMax, 
				owner == owner.world.player ? Difficulty.HARD : owner.world.difficulty);
		// fill in best
		for (Map.Entry<ResearchType, Integer> e : plan.demand.entrySet()) {
			int demand = e.getValue();
			ResearchType rt = e.getKey();
			int count = Math.min(demand, owner.inventoryCount(rt));
			deployItem(rt, count);
			vehicleMax -= count;
		}
		if (vehicleMax > 0) {
			List<ResearchType> remaining = new ArrayList<>();
			int max = Math.max(plan.tanks.size(), plan.sleds.size());
			for (int i = 1; i < max; i++) {
				if (i < plan.tanks.size()) {
					remaining.add(plan.tanks.get(i));
				}
				if (i < plan.sleds.size()) {
					remaining.add(plan.sleds.get(i));
				}
			}
			// fill in remaining slots
			for (ResearchType rt : remaining) {
				if (vehicleMax <= 0) {
					break;
				}
				int add = Math.min(vehicleMax, owner.inventoryCount(rt));
				deployItem(rt, add);
				vehicleMax -= add;
			}
		}
	}
	/**
	 * Remove all tanks and vehicles and place them back into the owner's inventory.
	 */
	public void stripVehicles() {
		for (InventoryItem ii : inventory.list()) {
			if (ii.type.category == ResearchSubCategory.WEAPONS_TANKS
					|| ii.type.category == ResearchSubCategory.WEAPONS_VEHICLES) {
				owner.changeInventoryCount(ii.type, ii.count);
				inventory.remove(ii);
			}
		}
	}
	/**
	 * Returns true if there are options available to upgrade the fleet.
	 * @return true if there are options available to upgrade the fleet.
	 */
	public boolean canUpgrade() {
		for (InventoryItem ii : inventory.iterable()) {
			if (ii.type.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS
					|| ii.type.category == ResearchSubCategory.SPACESHIPS_CRUISERS) {
				if (ii.checkSlots()) { 
					return true;
				}
			} else
			if (ii.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
				if (ii.count < 30 && owner.inventoryCount(ii.type) > 0) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Unload tanks, fighters, equipment.
	 */
	public void strip() {
		for (InventoryItem ii : inventory.list()) {
			if (ii.type.category == ResearchSubCategory.WEAPONS_TANKS
					|| ii.type.category == ResearchSubCategory.WEAPONS_VEHICLES
					|| ii.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
				owner.changeInventoryCount(ii.type, ii.count);
				inventory.remove(ii);
			} else {
				ii.strip();
			}
		}
		if (inventory.size() == 0) {
			owner.world.removeFleet(this);
		}
	}
	/**
	 * Sell all inventory item.
	 */
	public void sell() {
		for (InventoryItem ii : inventory.iterable()) {
			ii.sell(ii.count);
		}
		
		inventory.clear();
		owner.world.removeFleet(this);
	}
	/**
	 * Stop the fleet.
	 */
	public void stop() {
		clearWaypoints();
		targetFleet = null;
		targetPlanet = null;
		arrivedAt = null;
		mode = null;
		task = FleetTask.IDLE;
	}
	/** Remove inventory items with zero counts. */
	public void cleanup() {
		inventory.removeIf(InventoryItem.CLEANUP);
	}
	/**
	 * Replace a medium or large ship from inventory.
	 * @param rt the best ship technology
	 * @param limit the limit of the ship category
	 */
	public void replaceWithShip(ResearchType rt, int limit) {
		// the category count
		int current = inventoryCount(rt.category);
		// current available inventory
		int invGlobal = owner.inventoryCount(rt);
		// already in inventory
		int invLocal = inventoryCount(rt);
		// how many can we add?
		int toAdd = Math.min(limit - invLocal, invGlobal);
		
		while (toAdd > 0) {
			deployItem(rt, 1);
			current++;
			toAdd--;
		}
		// lets remove excess
		if (current > limit) {
			List<InventoryItem> iis = U.sort(inventory.list(), new Comparator<InventoryItem>() {
				@Override
				public int compare(InventoryItem o1, InventoryItem o2) {
					return ResearchType.CHEAPEST_FIRST.compare(o1.type, o2.type);
				}
			});
			for (InventoryItem ii : iis) {
				if (current <= limit) {
					break;
				}
				if (ii.type.category == rt.category && ii.type != rt) {
					ii.strip();
					inventory.remove(ii);
					current--;
				}
			}
		}
	}
	@Override
	public String toString() {
		return String.format("Fleet { Name = %s (%d) , Inventory = %d: %s }", name, id, inventory.size(), inventory);
	}
	/**
	 * Issue a move order to the target location.
	 * @param x the target X
	 * @param y the target Y
	 */
	public void moveTo(double x, double y) {
		stop();
		addWaypoint(x, y);
		mode = FleetMode.MOVE;
		task = FleetTask.MOVE;
	}
	/**
	 * Issue a move order to the target location.
	 * @param p the target point
	 */
	public void moveTo(Point2D.Double p) {
		stop();
		addWaypoint(p.x, p.y);
		mode = FleetMode.MOVE;
		task = FleetTask.MOVE;
	}
	/**
	 * Remove all waypoints.
	 */
	public void clearWaypoints() {
		waypoints.clear();
	}
	/**
	 * Add a new waypoint.
	 * @param x the waypoint X
	 * @param y the waypoint Y
	 */
	public void addWaypoint(double x, double y) {
		waypoints.add(new Point2D.Double(x, y));
	}
	/**
	 * Switch to move mode and either move to
	 * the target point or add a new waypoint.
	 * @param x the new point
	 * @param y the new point
	 */
	public void moveNext(double x, double y) {
		if (mode != FleetMode.MOVE || targetFleet != null || targetPlanet != null) {
			// if a planet is currently targeted, make it a waypoint
			if (targetPlanet != null) {
				double px = targetPlanet.x;
				double py = targetPlanet.y;
				moveTo(px, py);
				addWaypoint(x, y);
			} else {
				moveTo(x, y);
			}
		} else {
			addWaypoint(x, y);
		}
	}
	/**
	 * Switch to move mode and either move to
	 * the target point or add a new waypoint
	 * if already moving.
	 * @param p the point
	 */
	public void moveNext(Point2D.Double p) {
		moveNext(p.x, p.y);
	}
	/**
	 * Add a new waypoint.
	 * @param p the point
	 */
	public void addWaypoint(Point2D.Double p) {
		addWaypoint(p.x, p.y);
	}
	/**
	 * Start following the other fleet.
	 * @param otherFleet the fleet to follow
	 */
	public void follow(Fleet otherFleet) {
		if (this == otherFleet) {
			Exceptions.add(new AssertionError("Can't follow self!"));
			return;
		}
		stop();
		targetFleet = otherFleet;
		mode = FleetMode.MOVE;
		task = FleetTask.MOVE;
	}
	/**
	 * Attack the other fleet.
	 * @param otherFleet the other fleet
	 */
	public void attack(Fleet otherFleet) {
		if (this == otherFleet) {
			Exceptions.add(new AssertionError("Can't attack self!"));
			return;
		} else
		if (this.owner == otherFleet.owner) {
			Exceptions.add(new AssertionError("Can't attack friendly!"));
			return;
		} else
		if (!owner.isStrongAlliance(otherFleet.owner)) {
			Exceptions.add(new AssertionError("Strong alliance!"));
			return;
		}
		stop();
		targetFleet = otherFleet;
		mode = FleetMode.ATTACK;
		task = FleetTask.ATTACK;
	}
	/**
	 * Attack the target planet.
	 * @param planet the planet to attack
	 */
	public void attack(Planet planet) {
		if (owner == planet.owner) {
			Exceptions.add(new AssertionError("Can't attack friendly planet!"));
			return;
		}
		if (planet.owner == null) {
			Exceptions.add(new AssertionError("Can't attack empty planet!"));
			return;
		}
		if (owner.isStrongAlliance(planet.owner)) {
			Exceptions.add(new AssertionError("Can't attack ally planet!"));
			return;
		}
		stop();
		targetPlanet = planet;
		mode = FleetMode.ATTACK;
		task = FleetTask.ATTACK;
	}
	/**
	 * Attack the target planet.
	 * @param planet the planet to attack
	 */
	public void moveTo(Planet planet) {
		stop();
		targetPlanet = planet;
		arrivedAt = null;
		mode = FleetMode.MOVE;
		task = FleetTask.MOVE;
	}
	/**
	 * Put excess fighters and vehicles back into the inventory.
	 */
	public void removeExcess() {
		InventoryItem.removeExcessFighters(inventory);
		FleetStatistics fs = getStatistics();
		InventoryItem.removeExcessTanks(inventory, owner, fs.vehicleCount, fs.vehicleMax);
	}
	/**
	 * Fills in the equipment of the fleet's members.
	 */
	public void refillEquipment() {
		if (owner == owner.world.player) {
			if (owner.world.env.config().reequipBombs) {
				for (InventoryItem ii : inventory.iterable()) {
					for (InventorySlot is : ii.slots.values()) {
						if (is.getCategory() == ResearchSubCategory.WEAPONS_PROJECTILES 
								&& !is.isFilled()) {
							is.refill(owner);
						}
					}
				}
			}
			if (owner.world.env.config().reequipTanks) {
				FleetStatistics fs = getStatistics();
				if (fs.vehicleCount < fs.vehicleMax) {
					stripVehicles();
					upgradeVehicles(fs.vehicleMax);
				}
			}
		}
	}
	/**
	 * @return Check if the fleet still exists in the world.
	 */
	public boolean exists() {
		return owner.fleets.containsKey(this);
	}
	/**
	 * Returns a fleet status object.
	 * @return the status object
	 */
	public FleetStatus toFleetStatus() {
		FleetStatus result = new FleetStatus();
		
		result.id = id;
		result.knowledge = owner.fleets.get(this);
		result.owner = owner.id;
		result.x = x;
		result.y = y;
		result.name = name;

		if (targetFleet != null) {
			result.targetFleet = targetFleet.id;
		}
		if (targetPlanet != null) {
			result.targetPlanet = targetPlanet.id;
		}
		if (arrivedAt != null) {
			result.arrivedAt = arrivedAt.id;
		}
		result.mode = mode;
		result.task = task;
		result.refillOnce = refillOnce;
		result.formation = formation;
		result.infectedBy = owner.world.infectedFleets.get(id);
		
		for (Point2D.Double wp : waypoints) {
			result.waypoints.add(new Point2D.Double(wp.x, wp.y));
		}
		
		for (InventoryItem ii : inventory.iterable()) {
			result.inventory.add(ii.toInventoryItemStatus());
		}
		
		return result;
	}
	/**
	 * Load values from the fleet status object.
	 * @param fs the fleet status object
	 */
	public void fromFleetStatus(FleetStatus fs) {
		// we don't allow owner changes this way?
		owner.fleets.put(this, fs.knowledge);
		x = fs.x;
		y = fs.y;
		name = fs.name;

		for (Point2D.Double wp : fs.waypoints) {
			waypoints.add(new Point2D.Double(wp.x, wp.y));
		}
		
		if (fs.targetFleet == null) {
			targetFleet = null;
		} else {
			targetFleet = owner.fleet(fs.targetFleet);
		}
		
		if (fs.targetPlanet == null) {
			targetPlanet = null;
		} else {
			targetPlanet = owner.world.planet(fs.targetPlanet);
		}
		
		if (fs.arrivedAt == null) {
			arrivedAt = null;
		} else {
			arrivedAt = owner.world.planet(fs.targetPlanet);
		}
		
		mode = fs.mode;
		task = fs.task;
		refillOnce = fs.refillOnce;
		if (fs.infectedBy != null) {
			owner.world.infectedFleets.put(id, fs.infectedBy);
		} else {
			owner.world.infectedFleets.remove(id);
		}
		
		Set<Integer> current = new HashSet<>();
		for (InventoryItemStatus iis : fs.inventory) {
			InventoryItem ii = inventory.findById(iis.id);
			if (ii == null) {
				ii = new InventoryItem(iis.id, owner.world.player(iis.owner), owner.world.research(iis.type));
				ii.init();
				inventory.add(ii);
			}
			ii.fromInventoryItemStatus(iis, owner.world);
			current.add(ii.id);
		}
		inventory.removeById(current);
	}
	/**
	 * Colonize the nearby planet if possible.
	 * @param p the planet
	 * @return true if colonization successful
	 */
	public boolean colonize(Planet p) {
		if (p.owner != null) {
			Exceptions.add(new AssertionError("Planet occupied: " + p.id));
			return false;
		}
		if (p != nearbyPlanet()) {
			Exceptions.add(new AssertionError("Planet too far: " + p.id));
		}
		World w = owner.world;
		ResearchType cs = w.research("ColonyShip");
		if (inventoryCount(cs) == 0) {
			Exceptions.add(new AssertionError("No colony ships available"));
			return false;
		}
		for (BuildingType bt : w.buildingModel.buildings.values()) {
			if ("MainBuilding".equals(bt.kind)) {
				TileSet ts = bt.tileset.get(owner.race);
				if (ts != null) {
					Point pt = p.surface.placement.findLocation(ts.normal.width + 2, ts.normal.height + 2);
					if (pt != null) {
						// remove colony ship from fleet
						inventory.remove(getInventoryItem(cs));
						
						// remove empty fleet
						if (inventory.isEmpty()) {
							w.removeFleet(this);
							List<Fleet> of = owner.ownFleets();
							if (of.size() > 0) {
								owner.currentFleet = of.iterator().next();
							} else {
								owner.currentFleet = null;
								owner.selectionMode = SelectionMode.PLANET;
							}
						}
						// place building
						Building b = new Building(owner.world.newId(), bt, owner.race);
						p.owner = owner;
						p.race = owner.race;
						p.population = 5000;
						p.morale = 50;
						p.lastMorale = 50;
						p.lastPopulation = 5000;
						b.location = Location.of(pt.x + 1, pt.y - 1);
						
						p.surface.placeBuilding(ts.normal, b.location.x, b.location.y, b);
						p.rebuildRoads();
						
						p.owner.planets.put(p, PlanetKnowledge.BUILDING);
						p.owner.currentPlanet = p;
						
						p.owner.statistics.planetsColonized.value++;
						
						p.owner.colonizationTargets.remove(p.id);
						
						// uninstall satellites
						p.removeOwnerSatellites();
						
						return true;
					}
					Exceptions.add(new AssertionError(
							String.format("Could not colonize planet %s, not enough initial space for colony hub of race %s.", p.id, owner.race)));
				}
			}
		}
		return false;
	}
	/**
	 * Creates a new fleat near this fleet.
	 * @return the new fleet.
	 */
	public Fleet newFleet() {
		Fleet f = new Fleet(owner);
		f.name = owner.world.labels.get("newfleet.name");
		
		double r = Math.max(0, ModelUtils.random() * owner.world.params().nearbyDistance() - 1);
		double k = ModelUtils.random() * 2 * Math.PI;
		f.x = (x + Math.cos(k) * r);
		f.y = (y + Math.sin(k) * r);

		return f;
	}
	@Override
	public List<InventoryItem> deployItem(ResearchType rt, int count) {
		if (count <= 0) {
			throw new IllegalArgumentException("count > 0 required");
		}
		int fleetLimit = getAddLimit(rt);
		boolean single = false;
		switch (rt.category) {
		case WEAPONS_TANKS:
		case WEAPONS_VEHICLES:
		case SPACESHIPS_FIGHTERS:
			single = true;
			break;
		default:
		}

		int invAvail = owner.inventoryCount(rt);
		
		int toDeploy = Math.min(count, Math.min(fleetLimit, invAvail));
		if (toDeploy > 0) {
			owner.changeInventoryCount(rt, -toDeploy);

			if (single) {
				InventoryItem ii = getInventoryItem(rt);
				if (ii == null) {
					ii = new InventoryItem(owner.world.newId(), owner, rt);
					ii.init();
					inventory.add(ii);
				}
				ii.count += toDeploy;
				return Collections.singletonList(ii);
			}
			List<InventoryItem> r = new ArrayList<>();
			for (int i = 0; i < toDeploy; i++) {
				InventoryItem ii = new InventoryItem(owner.world.newId(), owner, rt);
				ii.init();
				inventory.add(ii);
				r.add(ii);
			}
			return r;
		}
		return Collections.emptyList();
	}
	@Override
	public void sell(int itemId, int count) {
		InventoryItem ii = inventory.findById(itemId);
		if (ii != null) {
			ii.sell(count);
			if (ii.count <= 0) {
				inventory.remove(ii);
			}
		}
	}
	/**
	 * Sell a given amount of the given technology from this fleet.
	 * <p>For example, selling 2 cruisers will remove both from
	 * the inventory collection.</p>
	 * @param type the technology type
	 * @param count the number of items to sell.
	 */
	public void sell(ResearchType type, int count) {
		for (InventoryItem ii : new ArrayList<>(inventory.findByType(type.id))) {
			int n = ii.count;
			int s = Math.min(n, count);
			ii.sell(s);
			if (ii.count <= 0) {
				inventory.remove(ii);
			}
			count -= s;
			if (count <= 0) {
				break;
			}
		}
	}
	@Override
	public void undeployItem(int itemId, int count) {
		InventoryItem ii = inventory.findById(itemId);
		if (ii != null) {
			if (canUndeploy(ii.type)) {
				int n = Math.min(count, ii.count);
				ii.count -= n;
				owner.changeInventoryCount(ii.type, n);
				if (ii.count <= 0) {
					inventory.remove(ii);
				}
			} else {
				throw new IllegalArgumentException("inventory item can't be undeployed: " + ii.type);
			}
		} else {
			throw new IllegalArgumentException("inventory item not found: " + itemId);
		}
	}
	/**
	 * Undeploys the given amount of the technology from the fleet's
	 * inventory.
	 * @param rt the technology
	 * @param count the number of items to undeploy
	 */
	public void undeployItem(ResearchType rt, int count) {
		for (InventoryItem ii : inventory.findByType(rt.id)) {
			undeployItem(ii.id, count);
			return;
		}
	}
	@Override
	public boolean canUndeploy(ResearchType type) {
		return type.category == ResearchSubCategory.WEAPONS_TANKS
				|| type.category == ResearchSubCategory.WEAPONS_VEHICLES
				|| type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS;
	}
	@Override
	public boolean canDeploy(ResearchType type) {
		return type.category == ResearchSubCategory.WEAPONS_TANKS
				|| type.category == ResearchSubCategory.WEAPONS_VEHICLES
				|| type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS
				|| type.category == ResearchSubCategory.SPACESHIPS_CRUISERS
				|| type.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS;
	}
	/**
	 * Transfer the given amount of technology to the other fleet.
	 * @param other the other fleet
	 * @param type the technology to transfer
	 * @param count the number of items to transfer
	 */
	public void transferTo(Fleet other, ResearchType type, int count) {
		if (count <= 0) {
			throw new IllegalArgumentException("count > 0 required");
		}
		if (canUndeploy(type)) {
			InventoryItem ii = getInventoryItem(type);
			int n = Math.min(ii.count, count);
			ii.count -= n;
			if (ii.count <= 0) {
				inventory.remove(ii);
			}
			
			InventoryItem ii2 = other.getInventoryItem(type);
			if (ii2 == null) {
				ii2 = new InventoryItem(owner.world.newId(), other.owner, type);
				ii2.init();
				other.inventory.add(ii2);
			}
			ii2.count += n;
		} else
		if (canDeploy(type)) {
			for (InventoryItem ii : new ArrayList<>(inventory.findByType(type.id))) {
				inventory.remove(ii);
				other.inventory.add(ii);
				count -= ii.count;
				if (count <= 0) {
					break;
				}
			}
		} else {
			throw new IllegalArgumentException("Can't transfer items: " + type);
		}
		
	}
}
