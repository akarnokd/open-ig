/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.model.BattleProjectile.Mode;
import hu.openig.utils.Exceptions;
import hu.openig.utils.U;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * A fleet.
 * @author akarnokd, 2010.01.07.
 */
public class Fleet implements Named, Owned, HasInventory, HasPosition {
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
    private String name;
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
    public void setTargetPlanet(Planet p) {
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
        return name != null ? name : "";
    }
    /**
     * Set a new fleet name.
     * @param newName the new name, non-null
     */
    public void name(String newName) {
        if (newName == null) {
            throw new NullPointerException();
        }
        this.name = newName;
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

        int rus = owner.world.params().fleetRadarUnitSize();
        if (!inventory.isEmpty() && radar == 0) {
            radar = (int)(rus

                    * owner.world.params().fleetRadarlessMultiplier());
        } else {
            radar *= rus;
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
     * @return the currently allowed
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
                return Integer.compare(o2.type.productionCost, o1.type.productionCost);
            }
        });
        // walk
        for (InventoryItem ii : iis) {
            if (ii.type.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS
                    || ii.type.category == ResearchSubCategory.SPACESHIPS_CRUISERS) {
                ii.upgradeSlots();
            }
//            ii.shield = Math.max(0, ii.shieldMax());
        }

        for (ResearchType rt : owner.available()) {
            if (rt.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
                int count = Math.min(owner.world.params().fighterLimit(), owner.inventoryCount(rt));
                if (count > 0) {
                    deployItem(rt, owner, count);
                }
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
        plan.fillAvailableVehicleTech(owner.available(), owner.world.battle);
        // expected composition
        int tankDemand = plan.special.isEmpty() ? vehicleMax * 2 / 3 : vehicleMax / 2;
        int onePerKindDemand = plan.onePerKind.size();
        int vehicleDemand = vehicleMax - tankDemand - onePerKindDemand;
        int specialDemand = plan.special.isEmpty() ? 0 : vehicleDemand / 2;
        int sledDemand = vehicleDemand - specialDemand;

        // try to fill all the demands
        vehicleMax -= fillWithVehicleType(plan.onePerKind, onePerKindDemand);
        vehicleMax -= fillWithVehicleType(plan.tanks, tankDemand);
        vehicleMax -= fillWithVehicleType(plan.sleds, sledDemand);
        vehicleMax -= fillWithVehicleType(plan.special, specialDemand);
        // try to fill the remaining space with tanks and sleds
        if (vehicleMax > 0) {
            List<ResearchType> availableTanksAndSleds  = new ArrayList<>();
            availableTanksAndSleds.addAll(plan.tanks);
            availableTanksAndSleds.addAll(plan.sleds);
            fillWithVehicleType(availableTanksAndSleds, vehicleMax);
        }
    }
    /**
     * Helper function that fills up fleet inventory from a list of vehicle types by the give number of units.
     * @param vehicleTypes the vehicle types to use from inventory
     * @param demand the number of units to fill from inventory
     * @return the number of all the vehicles filled from the given inventory list
     */
    int fillWithVehicleType(List<ResearchType> vehicleTypes, int demand) {
        int fulfilledDemand = 0;
        for (ResearchType vehicleType : vehicleTypes) {
            int count = Math.min(demand, owner.inventoryCount(vehicleType));
            if (count > 0) {
                deployItem(vehicleType, owner, count);
                fulfilledDemand += count;
                demand -= count;
                if (demand == 0) {
                    break;
                }
            }
        }
        return fulfilledDemand;
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
                if (ii.count < owner.world.params().fighterLimit() && owner.inventoryCount(ii.type) > 0) {
                    return true;
                }
            }
        }
        for (ResearchType rt : owner.available()) {
            if (rt.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
                if (owner.inventoryCount(rt) > 0 && inventoryCount(rt) == 0) {
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
            deployItem(rt, owner, 1);
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
        if (owner.isStrongAlliance(otherFleet.owner)) {
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
        inventory.remove(getInventoryItem(cs));
        // remove empty fleet
        if (inventory.isEmpty()) {
            w.removeFleet(this);
        }

        return p.colonize(owner);
    }
    /**
     * Creates a new fleet near this fleet.
     * @return the new fleet.
     */
    public Fleet newFleet() {
        Fleet f = new Fleet(owner);
        f.name = owner.world.labels.get("newfleet.name");
        if (f.name == null) {
            f.name = "";
        }

        double r = Math.max(0, ModelUtils.random() * owner.world.params().nearbyDistance() - 1);
        double k = ModelUtils.random() * 2 * Math.PI;
        f.x = (x + Math.cos(k) * r);
        f.y = (y + Math.sin(k) * r);

        return f;
    }
    @Override
    public List<InventoryItem> deployItem(ResearchType rt, Player owner,
            int count) {
        if (owner != this.owner) {
            throw new IllegalArgumentException("You can only deploy items for the same owner as the fleet.");
        }
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
                ii.count = 1;
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
            sell(ii, count);
        }
    }
    @Override
    public void sell(InventoryItem ii, int count) {
        ii.sell(count);
        if (ii.count <= 0) {
            inventory.remove(ii);
        }
    }
    @Override
    public boolean contains(int itemId) {
        return inventory.contains(itemId);
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
     * Transfer a specific inventory item to the other fleet.
     * @param other the other fleet
     * @param itemId the inventory item in this fleet
     * @param mode the transfer mode
     */
    public void transferTo(Fleet other, int itemId, FleetTransferMode mode) {
        int vehicleCap = getStatistics().vehicleMax;

        transfer(other, itemId, mode);
        // move vehicles
        int vehicleCap2 = getStatistics().vehicleMax;
        int toMove = vehicleCap - vehicleCap2;
        if (toMove > 0) {
            Queue<InventoryItem> viis = new LinkedList<>();
            for (InventoryItem vi : inventory.iterable()) {
                if (vi.type.category == ResearchSubCategory.WEAPONS_TANKS
                        || vi.type.category == ResearchSubCategory.WEAPONS_VEHICLES) {
                    viis.add(vi);
                }
            }
            while (toMove > 0 && !viis.isEmpty()) {
                InventoryItem vi = viis.peek();
                transfer(other, vi.id, FleetTransferMode.ONE);
                toMove--;
                if (vi.count <= 0) {
                    viis.remove();
                }
            }
        }
    }
    /**
     * Transfers the inventory item(s) from this fleet to the other fleet.
     * @param other the other fleet
     * @param itemId the source inventory id
     * @param mode the transfer mode
     */
    private void transfer(Fleet other, int itemId, FleetTransferMode mode) {
        InventoryItem ii = inventory.findById(itemId);
        if (ii == null) {
            throw new IllegalArgumentException("Unknown inventory item: " + itemId);
        }
        if (ii.count <= 0) {
            throw new IllegalStateException("Inventory has count of " + ii.count);
        }

        int count;

        InventoryItem ii2;
        if (ii.type.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS

                || ii.type.category == ResearchSubCategory.SPACESHIPS_CRUISERS) {
            List<InventoryItem> iis = new ArrayList<>(inventory.findByType(ii.type.id));
            int idx = iis.indexOf(ii);
            switch (mode) {
            case ONE:
                count = 1;
                break;
            case HALF:
                count = iis.size() / 2 + 1;
                break;
            case ALL:
                count = iis.size();
                break;
            default:
                throw new IllegalArgumentException("Unsupported transfer type: " + mode);
            }
            int limit = other.getAddLimit(ii.type);
            count = Math.min(limit, count);

            int i = idx;
            while (count > 0) {
                InventoryItem ii3 = iis.get(i);

                other.inventory.add(ii3);
                inventory.remove(ii3);

                i = (i + 1) % iis.size();
                count--;
            }
        } else {
            switch (mode) {
            case ONE:
                count = 1;
                break;
            case HALF:
                count = ii.count / 2 + 1;
                break;
            case ALL:
                count = ii.count;
                break;
            default:
                throw new IllegalArgumentException("Unsupported transfer type: " + mode);
            }

            ii2 = other.getInventoryItem(ii.type);
            if (ii2 == null) {
                ii2 = new InventoryItem(owner.world.newId(), other.owner, ii.type);
                ii2.init();
                int toAdd = Math.min(other.getAddLimit(ii.type), count);
                ii2.count = toAdd;
                other.inventory.add(ii2);
                ii.count -= toAdd;
            } else {
                int toAdd = Math.min(other.getAddLimit(ii.type), count);
                ii2.count += toAdd;
                ii.count -= toAdd;
            }
            if (ii.count <= 0) {
                inventory.remove(ii);
            }
        }
    }
    @Override
    public double x() {
        return x;
    }
    @Override
    public double y() {
        return y;
    }
}
