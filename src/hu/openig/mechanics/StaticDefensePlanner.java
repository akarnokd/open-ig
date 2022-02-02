/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hu.openig.core.Action0;
import hu.openig.core.Difficulty;
import hu.openig.core.Pred0;
import hu.openig.model.AIBuilding;
import hu.openig.model.AIControls;
import hu.openig.model.AIInventoryItem;
import hu.openig.model.AIPlanet;
import hu.openig.model.AIWorld;
import hu.openig.model.BuildingType;
import hu.openig.model.EquipmentSlot;
import hu.openig.model.InventorySlot;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.Production;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;
import hu.openig.model.Tile;
import hu.openig.model.VehiclePlan;
import hu.openig.utils.U;

/**
 * Planner for building ground defenses and space stations.
 * @author akarnokd, 2012.01.04.
 */
public class StaticDefensePlanner extends Planner {
    /** The new construction money limit. */
    protected static final int MONEY_LIMIT = 150000;
    /** Let the defense planner build an orbital factory? */
    public boolean allowBuildOrbitalFactory = true;
    /** Allow producing vehicles or stations? */
    public boolean allowProduction = true;
    /** Indicator that production was issued for a planet. */
    boolean productionInProgress;
    /**
     * Constructor. Initializes the fields.
     * @param world the world object
     * @param controls the controls
     */
    public StaticDefensePlanner(AIWorld world, AIControls controls) {
        super(world, controls);
    }

    @Override
    protected void plan() {
        if (world.global.militarySpaceportCount == 0) {
            // do not build if there is any planet without power plant.
            for (AIPlanet p : world.ownPlanets) {
                if (p.statistics.energyAvailable == 0) {
                    return;
                }
            }
            if (checkMilitarySpaceport(true)) {
                return;
            }
        }
        productionInProgress = false;
        List<AIPlanet> planets = new ArrayList<>(world.ownPlanets);
        Collections.sort(planets, BEST_PLANET);
        for (AIPlanet planet : planets) {
            if (managePlanet(planet)) {
                if (world.mainPlayer == p && world.money < world.autoBuildLimit) {
                    return;
                }
//                if (productionInProgress) {
//                    return;
//                }
            }
        }
        if (!planets.isEmpty() && allowBuildOrbitalFactory) {
            ResearchType ort = world.isAvailable("OrbitalFactory");
            if (world.global.orbitalFactory == 0

                    && ort != null
                    && world.money >= ort.productionCost * 3 / 4 + world.autoBuildLimit) {
                for (ResearchType rt : world.availableResearch) {
                    if (rt.has("needsOrbitalFactory")) {
                        checkOrbitalFactory();
                        break;
                    }
                }
            }
        }
    }
    /**
     * Manage a planet. Used by the AutoBuilder.
     * @param planet the target planet
     */
    public void managePlanet(Planet planet) {
        for (AIPlanet p : world.ownPlanets) {
            if (p.planet == planet) {
                managePlanet(p);
                return;
            }
        }
    }
    /**
     * Manage a concrete planet.
     * @param planet the target planet
     * @return true if action taken
     */
    public boolean managePlanet(final AIPlanet planet) {
        if (!world.mayImproveDefenses) {
            return false;
        }

        if (world.autobuildEconomyFirst

                && p == world.mainPlayer

                && !isEconomyBuilt(planet)) {
            return false;
        }

        List<Pred0> actions = new ArrayList<>();

        // FIX ME how many barracks to build per difficulty
        int defenseLimit = 1;
        if (world.difficulty == Difficulty.NORMAL) {
            defenseLimit = 3;
        }
        if (world.difficulty == Difficulty.HARD || p == world.mainPlayer) {
            defenseLimit = 5;
        }
        final int fdefenseLimit = defenseLimit;

        if (world.money >= 150000 && planet.population >= planet.statistics.workerDemand * 1.1) {
            actions.add(new Pred0() {
                @Override
                public Boolean invoke() {
                    return checkBuildingKind(planet, "Gun", fdefenseLimit);
                }
            });
            actions.add(new Pred0() {
                @Override
                public Boolean invoke() {
                    // find the best available shield technology
                    return checkBuildingKind(planet, "Shield", Integer.MAX_VALUE);
                }
            });
            actions.add(new Pred0() {
                @Override
                public Boolean invoke() {
                    // find bunker
                    return checkBuildingKind(planet, "Bunker", Integer.MAX_VALUE);
                }
            });
            actions.add(new Pred0() {
                @Override
                public Boolean invoke() {
                    // find barracks..strongholds
                    if (planet.statistics.constructing) {
                        return false;
                    }
                    return checkBuildingKind(planet, "Defensive", fdefenseLimit);
                }
            });
            actions.add(new Pred0() {
                @Override
                public Boolean invoke() {
                    // check for military spaceport
                    if (planet.statistics.constructing) {
                        return false;
                    }
                    return checkMilitarySpaceport(planet);
                }
            });
        }
        if (world.money >= MONEY_LIMIT) {
            if (world.level > 1) {
                actions.add(new Pred0() {
                    @Override
                    public Boolean invoke() {
                        // find the space stations
                        return checkStations(planet);
                    }
                });
                actions.add(new Pred0() {
                    @Override
                    public Boolean invoke() {
                        // find the space stations
                        return checkRockets(planet);
                    }
                });
            }
            if (world.level > 2) {
                actions.add(new Pred0() {
                    @Override
                    public Boolean invoke() {
                        // find the space stations
                        return checkFighters(planet);
                    }
                });
            }

            if (world.level > 1) {
                actions.add(new Pred0() {
                    @Override
                    public Boolean invoke() {
                        // check for military spaceport
                        return checkTanks(planet);
                    }
                });
            }
        }

        Collections.shuffle(actions);

        boolean result = false;
        for (Pred0 p : actions) {
            if (p.invoke()) {
                if (world.mainPlayer != this.p || world.money < world.autoBuildLimit) {
                    return true;
                }
                result = true;
            }
        }

        return result;
    }
    /**
     * Check if all economic buildings have been built.
     * @param planet the target planet
     * @return true if all economic buildings built
     */
    boolean isEconomyBuilt(AIPlanet planet) {
        boolean hasMultiply = !buildingResourceSupported(planet, "credit");
        boolean hasCredit = !buildingResourceSupported(planet, "multiply");
        boolean hasTrade = world.isAvailable("TradeCenter") == null;
        boolean hasRadar = false;
        boolean hasSocial = false;
        boolean hasPolice = planet.population < 5000;
        boolean hasHospital = planet.population < 5000;
        for (AIInventoryItem ii : planet.inventory) {
            if (ii.type.has(ResearchType.PARAMETER_RADAR) && ii.owner == planet.owner) {
                hasRadar = true;
                break;
            }
        }

        for (AIBuilding b : planet.buildings) {
            if (b.isComplete()) {
                hasMultiply |= b.hasResource(BuildingType.RESOURCE_MULTIPLY);
                hasCredit |= b.hasResource(BuildingType.RESOURCE_CREDIT);
                hasRadar |= b.hasResource(BuildingType.RESOURCE_RADAR);
                hasSocial |= b.hasResource(BuildingType.RESOURCE_MORALE);
                hasPolice |= b.hasResource(BuildingType.RESOURCE_POLICE);
                hasHospital |= b.hasResource(BuildingType.RESOURCE_HOSPITAL);
                hasTrade |= b.type.id.equals("TradeCenter");
            }
        }
        Set<String> rf = getRequiredFactories();
        return hasMultiply && hasCredit
                && (planet.statistics.hasTradersSpaceport || !buildingSupported(planet, "TradersSpaceport"))
                && (planet.statistics.activeProduction.weapons > 0 || !rf.contains("weapon"))
                && (planet.statistics.activeProduction.equipment > 0 || !rf.contains("equipment"))
                && (planet.statistics.activeProduction.spaceship > 0 || !rf.contains("spaceship"))
                && hasRadar
                && hasSocial
                && hasPolice
                && hasHospital
                && hasTrade;
    }
    /**
     * Collects the factory types to be built based on technological requirements.
     * @return the set of factory types
     */
    Set<String> getRequiredFactories() {
        Set<String> result = new HashSet<>();
        for (ResearchType rt : w.researches.values()) {
            if (rt.race.contains(p.race) && rt.level <= w.level) {
                result.add(rt.factory);
            }
        }
        return result;
    }
    /**
     * Check if there is a building that supports the given resource on the planet.
     * @param planet the target planet
     * @param resource the resource
     * @return true if supported
     */
    boolean buildingResourceSupported(AIPlanet planet, String resource) {
        for (BuildingType bt : w.buildingModel.buildings.values()) {
            if (planet.canBuild(bt)) {
                if (bt.hasResource(resource)) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * Check if there is a building that supports the given resource on the planet.
     * @param planet the target planet
     * @param buildingId the building identifier
     * @return true if supported
     */
    boolean buildingSupported(AIPlanet planet, String buildingId) {
        for (BuildingType bt : w.buildingModel.buildings.values()) {
            if (planet.canBuild(bt)) {
                if (bt.id.equals(buildingId)) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * Check the tanks.
     * @param planet the target planet
     * @return true if action taken
     */
    boolean checkTanks(final AIPlanet planet) {
        final Player expectedOwner = p;

        int vehicleMax = planet.statistics.vehicleMax;

        if (world.difficulty == Difficulty.EASY && p != world.mainPlayer) {
            vehicleMax = vehicleMax * 2 / 3;
        }

        final VehiclePlan plan = new VehiclePlan();
        plan.calculate(world.availableResearch, w.battle,
                vehicleMax,
                p == world.mainPlayer ? Difficulty.HARD : world.difficulty);

        if (vehicleMax < planet.statistics.vehicleCount) {
            List<ResearchType> removeCandidates = new ArrayList<>();
            removeCandidates.addAll(plan.tanks);
            removeCandidates.addAll(plan.sleds);
            Collections.shuffle(removeCandidates);
            if (!removeCandidates.isEmpty()) {
                final ResearchType rt = removeCandidates.get(0);
                planet.addInventoryCount(rt, planet.owner, -1);
                world.addInventoryCount(rt, 1);
                add(new Action0() {
                    @Override
                    public void invoke() {
                        if (planet.planet.owner == expectedOwner) {
                            planet.planet.changeInventory(rt, planet.owner, -1);
                            planet.planet.owner.changeInventoryCount(rt, 1);
                            log("UndeployExcessCapacityTanks, Planet = %s, Type = %s, Count = %s", planet.planet.id, rt, 1);
                        }
                    }
                });
                return true;
            }
        }

        if (planet.owner.money() >= MONEY_LIMIT) {
            boolean productionPlaced = false;
            // issue production order for the difference
            for (Map.Entry<ResearchType, Integer> prod : plan.demand.entrySet()) {
                ResearchType rt = prod.getKey();
                int count = prod.getValue();
                int inventoryGlobal = world.inventoryCount(rt);
                int inventoryLocal = planet.inventoryCount(rt);

                int localDemand = count - inventoryLocal;
                if (localDemand > 0 && localDemand > inventoryGlobal) {
                    // if in production wait
                    if (world.productionCount(rt) == 0 && activeProductionLanes("weapon") < 5) {
                        if (allowProduction && !productionInProgress) {
                            placeProductionOrder(rt, limitProduction(rt, localDemand - inventoryGlobal), false);
                            productionInProgress = true;
                        }
                        productionPlaced = true;
                    }
                }
            }
            if (productionPlaced) {
                return true;
            }
        }
        // deploy new equipment

        for (Map.Entry<ResearchType, Integer> e : plan.demand.entrySet()) {
            final ResearchType rt = e.getKey();
            final int planCount = e.getValue();
            final int inventoryLocal = planet.inventoryCount(rt);
            final int inventoryGlobal = world.inventoryCount(rt);
            final int remaining = Math.max(0, planCount - inventoryLocal);

            // if the current mixture does not match the intended mixture, undeploy the excess
            if (inventoryLocal > planCount) {
                final int excess = inventoryLocal - planCount;
                planet.addInventoryCount(rt, p, -excess);
                world.addInventoryCount(rt, excess);
                planet.statistics.vehicleCount -= excess;
                add(new Action0() {
                    @Override
                    public void invoke() {
                        if (planet.planet.owner == expectedOwner) {
                            int currentInventoryLocal = planet.planet.inventoryCount(rt, p);
                            if (currentInventoryLocal > planCount) {
                                int currentExcess = currentInventoryLocal - planCount;
                                planet.planet.changeInventory(rt, planet.owner, -currentExcess);
                                planet.owner.changeInventoryCount(rt, currentExcess);
                                log("UndeployExcessTanks, Planet = %s, Type = %s, Count = %s, Current = %d, Plan = %d", planet.planet.id, rt, currentExcess, currentInventoryLocal, planCount);
                            }
                        }
                    }
                });
                return true;
            }
            if (remaining > 0 && inventoryGlobal >= remaining) {
                // undeploy old technology only when the new technology is fully ready
                for (final AIInventoryItem ii : planet.inventory) {
                    if (!plan.demand.containsKey(ii.type)) {
                        if ((plan.tanks.contains(ii.type) && plan.tanks.contains(rt))
                                || (plan.sleds.contains(ii.type) && plan.sleds.contains(rt))) {
                            int cnt1 = planet.inventoryCount(ii.type, p);
                            world.addInventoryCount(ii.type, cnt1);
                            planet.addInventoryCount(ii.type, p, -cnt1);
                            planet.statistics.vehicleCount -= cnt1;
                            add(new Action0() {
                                @Override
                                public void invoke() {
                                    if (planet.planet.owner == expectedOwner) {
                                        int cnt = planet.planet.inventoryCount(ii.type, ii.owner);
                                        planet.planet.changeInventory(ii.type, planet.owner, -cnt);
                                        planet.owner.changeInventoryCount(ii.type, cnt);
                                        log("UndeployTanks, Planet = %s, Type = %s, Count = %s", planet.planet.id, ii.type, cnt);
                                    }
                                }
                            });
                            return true;
                        }
                    }
                }
                final int expectedVehicleCount = planet.statistics.vehicleCount;
                final int expectedVehicleMax = vehicleMax;
                if (expectedVehicleCount <= expectedVehicleMax) {
                    world.addInventoryCount(rt, -remaining);
                    planet.addInventoryCount(rt, expectedOwner, remaining);
                    add(new Action0() {
                        @Override
                        public void invoke() {
                            if (planet.planet.owner == expectedOwner) {
                                int currentVehicleCount = planet.planet.getStatistics().vehicleCount;
                                int currentVehicleMax = planet.planet.getStatistics().vehicleMax;
                                int currentInventoryLocal = planet.planet.inventoryCount(rt, expectedOwner);
                                // make sure the vehicle amount wasn't changed by the player
                                if (currentVehicleCount == expectedVehicleCount
                                        && currentVehicleMax == expectedVehicleMax
                                        && currentInventoryLocal == inventoryLocal) {
                                    int maxDeploy = Math.min(remaining, Math.max(0, currentVehicleMax - currentVehicleCount));
                                    if (p.inventoryCount(rt) >= maxDeploy && maxDeploy > 0) {
                                        planet.planet.changeInventory(rt, planet.owner, maxDeploy);
                                        p.changeInventoryCount(rt, -remaining);
                                        log("DeployTanks, Planet = %s, Type = %s, Count = %s, Current = %d, Max = %d", planet.planet.id, rt, maxDeploy, currentInventoryLocal, currentVehicleMax);
                                    }
                                }
                            }
                        }
                    });
                    planet.statistics.vehicleCount += remaining;
                    return true;
                }
                log("DeployTanks, Planet = %s, Type = %s, Count = %s, Current = %d, Max = %d, FAILED = At max vehicle capacity", planet.planet.id, rt, remaining, expectedVehicleCount, expectedVehicleMax);
            }
        }

        return false;
    }
    /**
     * Counts the number of active production lanes in the given factory.
     * @param factory the factory
     * @return the number
     */
    int activeProductionLanes(String factory) {
        int count = 0;
        for (Production pr : world.productions.values()) {
            if (pr.type.factory.equals(factory) && pr.count > 0) {
                count++;
            }
        }
        return count;
    }
    /**
     * Try constructing planets / 2 + 1 spaceports.
     * @param planet the target planet
     * @return true if action taken
     */
    boolean checkMilitarySpaceport(final AIPlanet planet) {
        if (world.money > 100000

                && world.money >= world.autoBuildLimit
                && checkPlanetPreparedness()) {
            final BuildingType bt = findBuilding("MilitarySpaceport");
            int spaceportLimit = Math.min(world.global.planetCount, (int)(world.money / (world.global.planetCount * 1.5 * bt.cost + world.autoBuildLimit) + 1));
            if (planet.statistics.militarySpaceportCount == 0

                    && spaceportLimit > world.global.militarySpaceportCount) {
                Point pt = planet.findLocation(bt);
                if (pt != null) {
                    build(planet, bt);
                    return true;
                }
                // if no room, make it by demolishing a traders spaceport
                for (final AIBuilding b : planet.buildings) {
                    if (b.type.id.equals("TradersSpaceport")) {
                        planet.buildings.remove(b);
                        add(new Action0() {
                            @Override
                            public void invoke() {
                                controls.actionDemolishBuilding(planet.planet, b.building);
                            }
                        });
                        return true;
                    }
                }
            }
        }
        return false;
    }
    /**
     * Check if stations can be placed/replaced.
     * @param planet the target planet
     * @return if action taken
     */
    boolean checkStations(final AIPlanet planet) {
        // find best station
        ResearchType station = null;
        for (ResearchType rt : world.availableResearch) {
            if (rt.category == ResearchSubCategory.SPACESHIPS_STATIONS && !rt.id.equals("OrbitalFactory")) {
                if (station == null || station.productionCost < rt.productionCost) {
                    station = rt;
                }
            }
        }

        if (station != null) {
            // count stations and find cheapest one
            int stationCount = 0;
            AIInventoryItem cheapest = null;
            for (AIInventoryItem ii : planet.inventory) {
                if (ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
                    stationCount++;
                    if (!ii.type.id.equals("OrbitalFactory")) {
                        if (cheapest == null || cheapest.type.productionCost > ii.type.productionCost) {
                            cheapest = ii;
                        }
                    }
                }
            }
            // if not enough, place one
            if (stationCount < world.stationLimit) {
                // if not available in inventory, construct one
                if (world.inventoryCount(station) == 0) {
                    if (allowProduction && !productionInProgress) {
                        placeProductionOrder(station, 1, false);
                        productionInProgress = true;
                    }
                    return true;
                }
                world.addInventoryCount(station, -1);
                //deploy satellite
                final ResearchType fstation = station;
                add(new Action0() {
                    @Override
                    public void invoke() {
                        controls.actionDeploySatellite(planet.planet, fstation);
                    }
                });
                return true;
            }
            // if the cheapest is cheaper than the best station, sell it
            if (cheapest != null && cheapest.type.productionCost < station.productionCost) {
                final AIInventoryItem fcheapest = cheapest;
                add(new Action0() {
                    @Override
                    public void invoke() {
                        controls.actionSellSatellite(planet.planet, fcheapest.type, 1);
                    }
                });
                return true;
            }
        }
        return false;
    }
    /**
     * Check the availability of the given building kinds and replace
     * cheaper ones.
     * @param planet the target planet
     * @param kind the building kind
     * @param limit constrain the building count even further
     * @return true if action taken
     */
    boolean checkBuildingKind(final AIPlanet planet, String kind, int limit) {
        // find the best available gun technology
        BuildingType bt = null;
        for (BuildingType bt0 : p.world.buildingModel.buildings.values()) {
            if (planet.canBuildReplacement(bt0) && bt0.kind.equals(kind)) {
                if (bt == null || bt.cost < bt0.cost) {
                    bt = bt0;
                }
            }
        }
        if (bt != null) {
            int gunCount = 0;
            // count guns
            for (AIBuilding b : planet.buildings) {
                if (b.type.kind.equals(kind)) {
                    gunCount++;
                }
            }
            // if room, build one
            boolean hasRoom = planet.findLocation(bt) != null;
            if (gunCount < Math.abs(bt.limit) && gunCount < limit) {
                if (hasRoom) {
                    build(planet, bt);
                    return true;
                }
            } else {

                // the current defense level and demolish lesser guns
                for (final AIBuilding b : planet.buildings) {
                    if (b.type.kind.equals(kind) && b.type.cost < bt.cost) {

                        // check if there would be room for the upgraded version
                        Tile bts = bt.tileset.get(planet.planet.race).normal;
                        Tile bs = b.tileset.normal;

                        if (hasRoom || (bs.width >= bts.width && bs.height >= bts.height)) {
                            planet.buildings.remove(b);
                            add(new Action0() {
                                @Override
                                public void invoke() {
                                    controls.actionDemolishBuilding(planet.planet, b.building);
                                }
                            });
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    /**

     * Returns the actions to perform.
     * @return the actions to perform
     */
    public List<Action0> actions() {
        return applyActions;
    }
    /**
     * Check if space station rockets are equipped.
     * @param planet the target planet
     * @return true if action performed
     */
    boolean checkRockets(AIPlanet planet) {
        Map<ResearchType, Integer> demand = new HashMap<>();
        boolean result = false;
        // collect rocket demands
        for (final AIInventoryItem ii : planet.inventory) {
            if (ii.owner == p && ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
                for (InventorySlot is : ii.slots) {
                    // find best available technology
                    ResearchType rt1 = null;
                    for (ResearchType rt0 : is.slot.items) {
                        if (world.isAvailable(rt0)) {
                            rt1 = rt0;
                        }
                    }
                    if (rt1 != null && rt1.category == ResearchSubCategory.WEAPONS_PROJECTILES) {
                        int d = is.slot.max;
                        if (is.type == rt1) {
                            d = Math.max(is.slot.max - is.count, 0);
                        }

                        if (d > 0) {
                            // check if inventory is available
                            if (d <= world.inventoryCount(rt1)) {
                                world.addInventoryCount(rt1, -d);
                                final int fd = d;
                                final ResearchType frt1 = rt1;
                                final EquipmentSlot fes = is.slot;
                                // deploy into slot
                                add(new Action0() {
                                    @Override
                                    public void invoke() {
                                        if (p.inventoryCount(frt1) >= fd) {

                                            if (ii.parent != null) {
                                                for (InventorySlot is : ii.parent.slots.values()) {
                                                    if (is.slot == fes) {
                                                        // unequip previous tech
                                                        if (is.type != null && is.type != frt1) {
                                                            p.changeInventoryCount(is.type, is.count);
                                                            is.count = 0;
                                                            is.type = frt1;
                                                        }
                                                        // Make sure count doesn't exceed the limit upfront
                                                        is.count = Math.min(Math.max(0, is.count), is.slot.max);
                                                        int newCount = Math.min(is.slot.max, is.count + fd);
                                                        // since the player could have deployed, we only deploy the remaining
                                                        int actualDeploy = Math.max(0, newCount - is.count);
                                                        is.count = newCount;
                                                        p.changeInventoryCount(frt1, -actualDeploy);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                });
                                result = true;
                            } else {
                                Integer id = demand.get(rt1);
                                demand.put(rt1, id != null ? id + d : d);
                            }
                        }
                    }
                }
            }
        }
        if (allowProduction && !productionInProgress) {
            // place production order for the difference
            for (Map.Entry<ResearchType, Integer> de : demand.entrySet()) {
                int di = de.getValue();
                int ic = world.inventoryCount(de.getKey());
                if (di > ic) {
                    if (placeProductionOrder(de.getKey(), limitProduction(de.getKey(), di - ic), false)) {
                        result = true;
                    }
                    productionInProgress = true;
                }
            }
        }
        return result;
    }
    /**
     * Check if enough fighters are placed into orbit.
     * @param planet the planet
     * @return true if action performed
     */
    boolean checkFighters(final AIPlanet planet) {
        final List<ResearchType> fighters = U.sort2(availableResearchOf(EnumSet.of(ResearchSubCategory.SPACESHIPS_FIGHTERS)), ResearchType.EXPENSIVE_FIRST);
        boolean result = false;
        for (final ResearchType rt : fighters) {
            int ic = planet.inventoryCount(rt, planet.owner);
            if (ic < world.fighterLimit) {
                int gic = world.inventoryCount(rt);
                final int needed = Math.max(0, world.fighterLimit - ic);
                if (gic >= needed) {
                    world.addInventoryCount(rt, -needed);
                    add(new Action0() {
                        @Override
                        public void invoke() {
                            DefaultAIControls.actionDeployFighters(planet.owner, planet.planet, rt, needed);
                        }
                    });
                    result = true;
                } else {
                    if (allowProduction && !productionInProgress) {
                        int toproduce = Math.max(10, needed - gic);
                        if (toproduce > 0) {
                            if (placeProductionOrder(rt, limitProduction(rt, toproduce), false)) {
                                result = true;
                            }
                            productionInProgress = true;
                        }
                    }
                }
            }
        }

        return result;
    }

}
