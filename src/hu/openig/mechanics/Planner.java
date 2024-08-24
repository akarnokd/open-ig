/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.core.Func1;
import hu.openig.core.Pair;
import hu.openig.core.Pred1;
import hu.openig.model.AIBuilding;
import hu.openig.model.AIControls;
import hu.openig.model.AIFleet;
import hu.openig.model.AIInventoryItem;
import hu.openig.model.AIPlanet;
import hu.openig.model.AIResult;
import hu.openig.model.AIWorld;
import hu.openig.model.BattleGroundVehicle;
import hu.openig.model.Building;
import hu.openig.model.BuildingType;
import hu.openig.model.FleetTask;
import hu.openig.model.GroundwarUnitType;
import hu.openig.model.ModelUtils;
import hu.openig.model.Planet;
import hu.openig.model.PlanetProblems;
import hu.openig.model.Player;
import hu.openig.model.Production;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;
import hu.openig.model.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * The base class for planners.
 * @author akarnokd, 2011.12.28.
 */
public abstract class Planner {
    /**
     * Interface for selecting a building or building type.
     * @author akarnokd, 2011.12.28.
     */
    public interface BuildingSelector {
        /**
         * Accept the given building for upgrading.
         * @param planet the planet in question
         * @param building the building
         * @return true if accept
         */
        boolean accept(AIPlanet planet, AIBuilding building);
        /**
         * Accept the given building type for construction.
         * @param planet the planet in question
         * @param buildingType the building type
         * @return true if accept
         */
        boolean accept(AIPlanet planet, BuildingType buildingType);
    }
    /**
     * Interface for comparing values of buildings.

     * @author akarnokd, 2011.12.28.
     */
    public interface BuildingOrder {
        /**
         * Compare the values of two concrete buildings.
         * @param b1 the first building
         * @param b2 the second building
         * @return -1, 0 or 1
         */
        int compare(AIBuilding b1, AIBuilding b2);
        /**
         * Compare the values of two building types.
         * @param b1 the first building type
         * @param b2 the second building type
         * @return -1, 0 or 1
         */
        int compare(BuildingType b1, BuildingType b2);
    }
    /**
     * Create a comparator from the building order object for the building type.
     * @param o the order
     * @return the comparator
     */
    Comparator<BuildingType> fromOrderType(final BuildingOrder o) {
        return new Comparator<BuildingType>() {
            @Override
            public int compare(BuildingType o1, BuildingType o2) {
                return o.compare(o1, o2);
            }
        };
    }
    /**
     * Create a comparator from the building order object for the buildings.
     * @param o the order
     * @return the comparator
     */
    Comparator<AIBuilding> fromOrder(final BuildingOrder o) {
        return new Comparator<AIBuilding>() {
            @Override
            public int compare(AIBuilding o1, AIBuilding o2) {
                return o.compare(o1, o2);
            }
        };
    }
    /** Default incremental cost-order. */
    final BuildingOrder costOrder = new BuildingOrder() {
        @Override
        public int compare(AIBuilding o1, AIBuilding o2) {
            int c = BuildingType.COST.compare(o1.type, o2.type);
            if (c == 0) {
                c = Integer.compare(o1.upgradeLevel, o2.upgradeLevel);
            }
            return c;
        }
        @Override
        public int compare(BuildingType o1, BuildingType o2) {
            return Integer.compare(o1.cost, o2.cost);
        }
    };
    /** Default incremental cost-order. */
    final BuildingOrder costOrderReverse = new BuildingOrder() {
        @Override
        public int compare(AIBuilding o2, AIBuilding o1) {
            int c = BuildingType.COST.compare(o1.type, o2.type);
            if (c == 0) {
                c = Integer.compare(o1.upgradeLevel, o2.upgradeLevel);
            }
            return c;
        }
        @Override
        public int compare(BuildingType o2, BuildingType o1) {
            return Integer.compare(o1.cost, o2.cost);
        }
    };
    /** Compares planets and chooses the worst overall condition. */
    public static final Comparator<AIPlanet> WORST_PLANET = new Comparator<AIPlanet>() {
        @Override
        public int compare(AIPlanet o1, AIPlanet o2) {
            return Integer.compare(status(o2), status(o1));
        }
        /**

         * Computes the health status.
         * @param p the planet
         * @return the status number
         */
        int status(AIPlanet p) {
            int value = 0;
            value += required(p.morale, 50) * 1000;
            value += required(p.buildings.size(), 3) * 1000;
            if (p.statistics.hasProblem(PlanetProblems.COLONY_HUB) || p.statistics.hasWarning(PlanetProblems.COLONY_HUB)) {
                value += 20000;
            }
            value += required(p.statistics.houseAvailable, p.population) * 2;
            value += required(p.statistics.energyAvailable, p.statistics.energyDemand) * 2;
            value += required(p.statistics.foodAvailable, p.population);
            value += required(p.statistics.hospitalAvailable, p.population);
            value += required(p.statistics.policeAvailable, p.population);
            return value;
        }
        /**
         * If available is less than the demand, return the difference, return zero otherwise
         * @param available the available amount
         * @param demand the demand amount
         * @return the required
         */
        int required(int available, int demand) {
            if (available < demand) {
                return demand - available;
            }
            return 0;
        }
        /**
         * If available is less than the demand, return the difference, return zero otherwise
         * @param available the available amount
         * @param demand the demand amount
         * @return the required
         */
        double required(double available, double demand) {
            if (available < demand) {
                return demand - available;
            }
            return 0;
        }
    };
    /** The best planet comparator. */
    public static final Comparator<AIPlanet> BEST_PLANET = new Comparator<AIPlanet>() {
        @Override
        public int compare(AIPlanet o1, AIPlanet o2) {
            return WORST_PLANET.compare(o2, o1);
        }
    };
    /** The world copy. */
    final AIWorld world;
    /** The original world object. */
    final World w;
    /** The player. */
    final Player p;
    /** The actions to perform. */
    final List<Action0> applyActions;
    /** The controls to affect the world in actions. */
    final AIControls controls;
    /**
     * Constructor. Initializes the fields.
     * @param world the world object
     * @param controls the controls to affect the world in actions
     */
    public Planner(AIWorld world, AIControls controls) {
        this.world = world;
        this.controls = controls;
        this.p = world.player;
        this.w = p.world;
        this.applyActions = new ArrayList<>();
    }
    /**
     * Execute the planning and return the action list.
     * @return the action list
     */
    public final List<Action0> run() {
        plan();
        return applyActions;
    }
    /** Perform the planning. */
    protected abstract void plan();
    /**
     * Add the given action to the output.
     * @param action the action to add
     */
    final void add(Action0 action) {
        applyActions.add(action);
    }
//    /**
//     * Add an empty action.
//     */
//    final void addEmpty() {
//        applyActions.add(new Action0() {
//            @Override
//            public void invoke() {
//

//            }
//        });
//    }
    /**
     * Display the action log.
     * @param message the message
     * @param values the message parameters
     */
    final void log(String message, Object... values) {
        /*
        // FIXME filter out irrelevant information for now
        if (!message.contains("Coloniz")) {
            return;
        }
        */
        System.out.printf("AI:%s:", p.id);
        System.out.printf(message, values);
        System.out.println();
    }
    /**
     * Find a type the building kind.
     * @param kind the kind
     * @return the building type
     */
    protected BuildingType findBuildingKind(String kind) {
        for (BuildingType bt : w.buildingModel.buildings.values()) {
            if (bt.kind.equals(kind)) {
                return bt;
            }
        }
        return null;
    }
    /**
     * Find a type the building id.
     * @param id the building type id
     * @return the building type
     */
    protected BuildingType findBuilding(String id) {
        for (BuildingType bt : w.buildingModel.buildings.values()) {
            if (bt.id.equals(id)) {
                return bt;
            }
        }
        return null;
    }
    /**
     * Plan a specific category of building actions.
     * @param condition the condition to check for the planet
     * @param planetOrder the order in which the planets should be visited.
     * @param selector the selector of candidate buildings
     * @param order the order between alternative buildings
     * @param upgradeFirst try upgrading first?
     * @return true if action taken
     */
    public final boolean planCategory(
            Func1<AIPlanet, Boolean> condition,
            Comparator<AIPlanet> planetOrder,
            BuildingSelector selector,

            BuildingOrder order, boolean upgradeFirst) {
        // try to upgrade or build a new power plant
        List<AIPlanet> planets = new ArrayList<>(world.ownPlanets);
        Collections.sort(planets, planetOrder);
        for (final AIPlanet planet : planets) {
            if (planet.statistics.constructing) {
                continue;
            }
            if (condition.invoke(planet)) {
                if (manageBuildings(planet, selector, order, upgradeFirst)) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * Shuffle the given collection randomly.
     * @param <T> the element type

     * @param collection the collection
     * @return the shuffled collection
     */
    <T> List<T> shuffle(Collection<? extends T> collection) {
        List<T> result = new ArrayList<>(collection);
        ModelUtils.shuffle(result);
        return result;
    }
    /**
     * Count the number of elements in the collection which are
     * allowed by the filter and number associated by the counter.
     * @param <T> the element type
     * @param src the source sequence
     * @param where the filter
     * @param counter the counter
     * @return the count
     */
    <T> int count(Iterable<T> src, Func1<? super T, Boolean> where, Func1<? super T, Integer> counter) {
        int result = 0;
        for (T ii : src) {
            if (where.invoke(ii)) {
                result += counter.invoke(ii);
            }
        }
        return result;
    }
    /**
     * Build or upgrade a power plant on the planet.
     * @param planet the target planet
     * @param selector to select the building type
     * @param order the comparator for order
     * @param upgradeFirst try upgrading first?
     * @return true if action taken

     */
    public final boolean manageBuildings(final AIPlanet planet,
            final BuildingSelector selector,
            final BuildingOrder order,
            boolean upgradeFirst) {
        upgradeFirst &= this.world.allowBuildingUpgrades;
        if (upgradeFirst) {
            if (manageUpgrade(planet, selector, order)) {
                return true;
            }
            AIResult result = manageConstruction(planet, selector, order);
            if (result == AIResult.SUCCESS) {
                return true;
            }
        } else {
            AIResult result = manageConstruction(planet, selector, order);
            if (result == AIResult.SUCCESS) {
                return true;
            }
            if (result == AIResult.NO_ROOM || result == AIResult.NO_AVAIL) {
                if (manageUpgrade(planet, selector, order)) {
                    return true;
                }
            }
        }
        return manageRepair(planet, selector, order);
    }

    /**
     * See if there are disabled buildings of the target type and try enabling them instead of
     * building more or upgrading.
     * @param planet the target planet
     * @param selector the building selector
     * @return true if there were disabled buildings
     */
    public final boolean tryEnableBuilding(final AIPlanet planet, final BuildingSelector selector) {
        for (final AIBuilding b : planet.buildings) {
            if (!b.enabled && selector.accept(planet, b)) {
                if (planet.population > planet.statistics.workerDemand - b.getWorkers()) {
                    add(new Action0() {
                        @Override
                        public void invoke() {
                            controls.actionEnableBuilding(planet.planet, b.building, true);
                        }
                    });
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Try to choose an upgrade option.
     * @param planet the target planet
     * @param selector the building selector
     * @param order the building order
     * @return true if action taken
     */
    public final boolean manageUpgrade(final AIPlanet planet,
            final BuildingSelector selector,
            final BuildingOrder order) {
        if (tryEnableBuilding(planet, selector)) {
            return true;
        }
        // scan for the most affordable upgrade
        AIBuilding upgrade = null;
        for (final AIBuilding b : planet.buildings) {
            if (selector.accept(planet, b) && b.canUpgrade() && !b.isDamaged()
                    && b.type.cost <= world.money) {
                if (upgrade == null || order.compare(b, upgrade) < 0) {
                    upgrade = b;
                }
            }
        }
        if (upgrade != null) {
            final Building fupgrade = upgrade.building;
            world.money -= fupgrade.type.cost;
            add(new Action0() {
                @Override
                public void invoke() {
                    controls.actionUpgradeBuilding(planet.planet, fupgrade, fupgrade.upgradeLevel + 1);
                }
            });
            return true;
        }
        return false;
    }
    /**
     * Try to choose a construction option but vary the number of buildings of the same type.
     * @param planet the target planet
     * @param selector the building selector
     * @param order the building order
     * @return the result of the operation
     */
    public final AIResult manageConstruction(final AIPlanet planet,
            final BuildingSelector selector,
            final BuildingOrder order) {
        if (tryEnableBuilding(planet, selector)) {
            return AIResult.SUCCESS;
        }
        // try building a new one
        List<BuildingType> createCandidates = new ArrayList<>();
        int moneyFor = 0;
        int locationFor = 0;
        for (final BuildingType bt : w.buildingModel.buildings.values()) {
            if (selector.accept(planet, bt) && planet.canBuild(bt)) {
                if (bt.cost <= world.money) {
                    moneyFor++;
                    if (planet.findLocation(bt) != null) {
                        locationFor++;
                        createCandidates.add(bt);
                    }
                }
            }
        }
        Collections.sort(createCandidates, fromOrderType(order));
        if (!createCandidates.isEmpty()) {
            // build all types round-robin
//            int maxOfType = Collections.max(builtCounts(createCandidates, planet));
//            for (final BuildingType bt : createCandidates) {
//                if (count(planet, bt) < maxOfType) {
//                    build(planet, bt);
//

//                    return AIResult.SUCCESS;
//                }
//            }
            // if all building counts are equal, just build the most expensive one
            final BuildingType bt = Collections.max(createCandidates, fromOrderType(costOrder));
            build(planet, bt);

            return AIResult.SUCCESS;
        }

        if (locationFor == 0) {
            return AIResult.NO_ROOM;
        } else
        if (moneyFor == 0) {
            return AIResult.NO_MONEY;
        }
        return AIResult.NO_AVAIL;
    }
    /**
     * Calculate the number of buildings built given by the types.
     * @param bts the building types to count
     * @param planet the target planet
     * @return the counts, in the same order as bts
     */
    List<Integer> builtCounts(Iterable<BuildingType> bts, AIPlanet planet) {
        List<Integer> result = new ArrayList<>();
        for (BuildingType bt : bts) {
            result.add(count(planet, bt));
        }
        return result;
    }
    /**
     * Try to choose a repair option.
     * @param planet the target planet
     * @param selector the building selector
     * @param order the building order
     * @return true if action taken
     */
    boolean manageRepair(final AIPlanet planet,

            final BuildingSelector selector,
            final BuildingOrder order) {
        // try repairing existing
        for (final AIBuilding b : planet.buildings) {
            if (!b.repairing && b.isDamaged() && selector.accept(planet, b)) {
                add(new Action0() {
                    @Override
                    public void invoke() {
                        controls.actionRepairBuilding(planet.planet, b.building, true);
                    }
                });
                return true;
            }
        }
        return false;
    }
    /**
     * Limit the production of items based on the current production capacity.
     * @param rt the technology
     * @param produce the number of items expected to be produced
     * @return the adjusted production count
     */
    public int limitProduction(ResearchType rt, int produce) {
        int pc = world.global.production.forResearch(rt);
        if (rt.productionCost * produce >= pc) {
            produce = pc / rt.productionCost + 1;  // FIXME don't produce too much
        }
        return produce;
    }
    /**
     * Place a production order of the given count,
     * if not enough slots available, the cheapest and/or finished production will be replaced.
     * @param rt the research type
     * @param count the minimum production amount when starting a new production
     * @param highPriority indicates that the production has high priority
     * @return true if new production order was placed
     */
    public boolean placeProductionOrder(final ResearchType rt, final int count, final boolean highPriority) {
        if (world.level <= 1) {
            return false;
        }
        Production prod = world.productions.get(rt);
        if (prod != null && prod.count > 0) {
            return false;
        } else
        if (prod != null) {
            issueProductionOrder(rt, count, highPriority);
            return true;
        }
        int prodCnt = 0;
        ResearchType cheapestRunning = null;
        ResearchType cheapestFinished = null;
        for (ResearchType rt0 : world.productions.keySet()) {
            prod = world.productions.get(rt0);
            if (rt.category.main == rt0.category.main) {
                prodCnt++;
                if (prod.count == 0) {
                    if (cheapestFinished == null || cheapestFinished.productionCost > rt0.productionCost) {
                        cheapestFinished = rt0;
                    }
                } else
                if (cheapestRunning == null || cheapestRunning.productionCost > rt0.productionCost) {
                    cheapestRunning = rt0;
                }
            }
        }
        if (prodCnt < 5) {
            issueProductionOrder(rt, count, highPriority);
            return true;
        }
        if (cheapestFinished != null) {
            final ResearchType cp = cheapestFinished;
            add(new Action0() {
                @Override
                public void invoke() {
                    controls.actionRemoveProduction(cp);
                }
            });
            return true;
        } else
        if (cheapestRunning != null && cheapestRunning.productionCost < rt.productionCost) {
            final ResearchType cp = cheapestRunning;
            add(new Action0() {
                @Override
                public void invoke() {
                    controls.actionRemoveProduction(cp);
                }
            });
            return true;
        }
        return false;
    }
    /**
     * Issue a production action for the given technology.
     * @param rt the technology to produce
     * @param count the minimum amount to produce
     * @param highPriority indicates that the production has high priority
     */
    void issueProductionOrder(final ResearchType rt, final int count, final boolean highPriority) {
        final int count0 = count;

        add(new Action0() {
            @Override
            public void invoke() {
                controls.actionStartProduction(rt, count0, highPriority ? 100 : 5);
            }
        });
    }
    /**
     * Check the current building count.
     * @param planet the target planet
     * @param bt the building type
     * @return the building count
     */
    public int count(AIPlanet planet, BuildingType bt) {
         int count = 0;
         for (AIBuilding b : planet.buildings) {
             if (b.type == bt) {
                 count++;
             }
         }
         return count;
    }
    /**
     * Returns the given label.
     * @param value the label
     * @return the translation
     */
    public String label(String value) {
        return w.env.labels().get(value);
    }
    /**
     * Returns the given formatted label.
     * @param value the label
     * @param params the parameters
     * @return the translation
     */
    public String format(String value, Object... params) {
        return w.env.labels().format(value, params);
    }
    /**
     * Find fleets for the specific tasks.
     * @param forTask the target task, any lower priority fleets will be found
     * @param filter optional filter for fleet properties
     * @return the list of fleets
     */
    List<AIFleet> findFleetsFor(FleetTask forTask, Func1<AIFleet, Boolean> filter) {
        List<AIFleet> result = new ArrayList<>();
        for (AIFleet f : world.ownFleets) {
            if (f.task.ordinal() > forTask.ordinal()) {
                if (filter == null || filter.invoke(f)) {
                    result.add(f);
                }
            }
        }
        return result;
    }
    /**
     * Find fleets with the specified task assignment.
     * @param withTask the current task
     * @param filter optional filter for fleet properties
     * @return the list of fleets
     */
    List<AIFleet> findFleetsWithTask(FleetTask withTask, Func1<AIFleet, Boolean> filter) {
        List<AIFleet> result = new ArrayList<>();
        for (AIFleet f : world.ownFleets) {
            if (f.task == withTask) {
                if (filter == null || filter.invoke(f)) {
                    result.add(f);
                }
            }
        }
        return result;
    }
    /**
     * Check for the orbital factory.
     * @return true
     */
    boolean checkOrbitalFactory() {
        if (world.isAvailable("OrbitalFactory") == null) {
            return true;
        }
        if (world.global.orbitalFactory == 0) {
            // check if we have orbital factory in inventory, deploy it
            final Pair<Integer, ResearchType> orbital = world.inventoryCount("OrbitalFactory");
            if (orbital.first > 0) {
                List<AIPlanet> planets = new ArrayList<>(world.ownPlanets);
                Collections.sort(planets, BEST_PLANET);

                if (deployOrbitalFactory(orbital.second, planets)) {
                    return true;
                }
                // if no room, make
                if (sellStations(planets)) {
                    return true;
                }
            }

            // if researched, build one
            if (produceOrbitalFactory()) {
                return true;
            }
        }
        return false;
    }
    /**
     * Create a military spaceport if necessary.
     * @param force force the check on the space port
     * @return true if action taken
     */
    boolean checkMilitarySpaceport(boolean force) {
        // if there is at least one operational we are done
        if (world.global.hasMilitarySpaceport) {
            return false;
        }
        // do not build below this money
        if (world.money < 85000) {
            return true;
        }
        if (world.isAvailable("ColonyShip") == null
                || world.isAvailable("OrbitalFactory") == null) {
            return true;
        }
        if (!force && !checkPlanetPreparedness()) {
            return true;
        }
        // check if there is a spaceport which we could get operational
        for (final AIPlanet planet : world.ownPlanets) {
            for (final AIBuilding b : planet.buildings) {
                if (b.type.id.equals("MilitarySpaceport")) {
                    if (b.isDamaged() && !b.repairing) {
                        add(new Action0() {
                            @Override
                            public void invoke() {
                                controls.actionRepairBuilding(planet.planet, b.building, true);
                            }
                        });
                    }
                    // found and wait for it to become available
                    return true;
                }
            }
        }
        // build one somewhere
        final BuildingType ms = findBuilding("MilitarySpaceport");
        // check if we can afford it
        if (ms.cost <= world.money) {
            List<AIPlanet> planets = new ArrayList<>(world.ownPlanets);
            Collections.shuffle(planets);
            // try building one somewhere randomly
            for (final AIPlanet planet : planets) {
                if (/* !planet.statistics.constructing && */

                        planet.canBuild(ms)
                        && planet.findLocation(ms) != null) {
                    build(planet, ms);
                    return true;
                }
            }
            // there was no room, so demolish a trader's spaceport somewhere
            for (final AIPlanet planet : planets) {
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
        // can't seem to do much now
        return true;
    }
    /**
     * Sell the cheapest deployed station.
     * @param planets the list of planets
     * @return true if action taken
     */
    boolean sellStations(List<AIPlanet> planets) {
        Pair<AIPlanet, ResearchType> toSell = null;
        for (AIPlanet p : planets) {
            for (AIInventoryItem ii : p.inventory) {
                if (ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
                    if (toSell == null || toSell.second.productionCost > ii.type.productionCost) {
                        toSell = Pair.of(p, ii.type);
                    }
                }
            }
        }
        if (toSell != null) {
            final Pair<AIPlanet, ResearchType> fsell = toSell;
            add(new Action0() {
                @Override
                public void invoke() {
                    controls.actionSellSatellite(fsell.first.planet, fsell.second, 1);
                }
            });
            return true;
        }
        return false;
    }
    /**
     * Deploy orbital factory.
     * @param rt the factory tech
     * @param planets the planets to check
     * @return true if action taken
     */
    boolean deployOrbitalFactory(final ResearchType rt, List<AIPlanet> planets) {
        for (final AIPlanet p2 : planets) {
            int sats = count(p2.inventory, new Pred1<AIInventoryItem>() {
                @Override
                public Boolean invoke(AIInventoryItem value) {
                    return value.type.category == ResearchSubCategory.SPACESHIPS_STATIONS;
                }
            }, new Func1<AIInventoryItem, Integer>() {
                @Override
                public Integer invoke(AIInventoryItem value) {
                    return value.count;
                }
            });
            if (sats < 3) {
                add(new Action0() {
                    @Override
                    public void invoke() {
                        controls.actionDeploySatellite(p2.planet, rt);
                    }
                });
                return true;
            }
        }
        return false;
    }
    /**
     * Produce an orbital factory.
     * @return action taken
     */
    boolean produceOrbitalFactory() {
        final ResearchType of = world.isAvailable("OrbitalFactory");
        if (of != null) {
            placeProductionOrder(of, 1, true);
            return true;
        }
        return false;
    }
    /**
     * Check if there is any ongoing production of the given list of technologies.
     * @param rts the list of technologies
     * @return true if any of it is in production
     */
    protected boolean isAnyProduction(List<ResearchType> rts) {
        for (ResearchType rt : rts) {
            Production pr = world.productions.get(rt);
            if (pr != null && pr.count > 0) {
                return true;
            }
        }
        return false;
    }
    /**
     * Check if there are better tanks or rocket sleds than in the inventory.
     * @author akarnokd, 2012.01.09.
     */
    public class TankChecker {
        /** The best tank technology. */
        ResearchType bestTank = null;
        /** The best rocket sled technology. */
        ResearchType bestSled = null;
        /** The current tank in inventory. */
        final NavigableSet<ResearchType> currentTank = new TreeSet<>(ResearchType.EXPENSIVE_FIRST);
        /** The current sled in inventory. */
        final NavigableSet<ResearchType> currentSled = new TreeSet<>(ResearchType.EXPENSIVE_FIRST);
        /**
         * Check if we have better tanks or vehicles.
         * @param inv the inventory
         * @return true if better available
         */
        public boolean check(Iterable<AIInventoryItem> inv) {
            for (ResearchType rt : world.availableResearch) {
                if (rt.category == ResearchSubCategory.WEAPONS_TANKS) {
                    if (bestTank == null || bestTank.productionCost < rt.productionCost) {
                        bestTank = rt;
                    }
                }
                if (rt.category == ResearchSubCategory.WEAPONS_VEHICLES) {
                    BattleGroundVehicle veh = w.battle.groundEntities.get(rt.id);
                    if (veh != null && veh.type == GroundwarUnitType.ROCKET_SLED) {
                        if (bestSled == null || bestSled.productionCost < rt.productionCost) {
                            bestSled = rt;
                        }
                    }
                }
            }
            for (AIInventoryItem ii : inv) {
                ResearchType rt = ii.type;
                if (rt.category == ResearchSubCategory.WEAPONS_TANKS) {
                    currentTank.add(rt);
                }
                if (rt.category == ResearchSubCategory.WEAPONS_VEHICLES) {
                    BattleGroundVehicle veh = w.battle.groundEntities.get(rt.id);
                    if (veh != null && veh.type == GroundwarUnitType.ROCKET_SLED) {
                        currentSled.add(rt);
                    }
                }
            }
            if (bestTank != null && !currentTank.contains(bestTank)) {
                return true;
            }
            if (bestSled != null && !currentSled.contains(bestSled)) {
                return true;
            }
            if (bestTank != null

                    && (currentTank.size() > 1
                    || currentTank.first().productionCost < bestTank.productionCost)) {
                return true;
            }
            return bestSled != null
                    && (currentSled.size() > 1
                    || currentSled.first().productionCost < bestSled.productionCost);
        }
    }
    /**
     * Find the best military spaceport.
     * @return the planet with the spaceport
     */
    public AIPlanet findBestMilitarySpaceport() {
        List<AIPlanet> planets = new ArrayList<>(world.ownPlanets);
        Collections.sort(planets, BEST_PLANET);
        for (AIPlanet p : planets) {
            if (p.statistics.hasMilitarySpaceport) {
                return p;
            }
        }
        return null;
    }
    /**
     * Find the best military spaceport close to the given location.
     * @param x the coordinate
     * @param y the coordinate
     * @return the planet with the spaceport
     */
    public AIPlanet findClosestMilitarySpaceport(final double x, final double y) {
        List<AIPlanet> planets = new ArrayList<>(world.ownPlanets);
        Collections.sort(planets, new Comparator<AIPlanet>() {
            @Override
            public int compare(AIPlanet o1, AIPlanet o2) {
                double d1 = Math.hypot(o1.planet.x - x, o1.planet.y - y);
                double d2 = Math.hypot(o2.planet.x - x, o2.planet.y - y);
                return d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
            }
        });
        for (AIPlanet p : planets) {
            if (p.statistics.hasMilitarySpaceport) {
                return p;
            }
        }
        return null;
    }
    /**
     * Returns the list of available technologies matching the given set of categories.
     * @param categories the category set
     * @return the list of available research
     */
    public List<ResearchType> availableResearchOf(EnumSet<ResearchSubCategory> categories) {
        List<ResearchType> result = new ArrayList<>();
        for (ResearchType rt : world.availableResearch) {
            if (categories.contains(rt.category)) {
                result.add(rt);
            }
        }
        return result;
    }
    /**
     * Check if all planets are ready for building factories and labs.
     * @return true if all planets are prepared
     */
    public boolean checkPlanetPreparedness() {
        boolean checkMoraleBuilding = true;
        BuildingType btc = w.building("Church");
        if (btc == null || !btc.tileset.containsKey(p.race)) {
            checkMoraleBuilding = false;
        }
        int isPrepared = 0;
        for (AIPlanet p : world.ownPlanets) {
            if (p.statistics.energyAvailable * 4 < p.statistics.energyDemand * 3
                    || p.statistics.energyAvailable == 0) {
                continue;
            }
            boolean moraleOrPolice = false;
            if (checkMoraleBuilding) {
                for (AIBuilding b : p.buildings) {
                    if (!b.type.kind.equals(BuildingType.KIND_MAIN_BUILDING)) {
                        if (b.hasResource(BuildingType.RESOURCE_MORALE)
                                || b.hasResource(BuildingType.RESOURCE_POLICE)) {
                            moraleOrPolice = true;
                            break;
                        }
                    }
                }
                moraleOrPolice |= p.morale > 45;
            } else {
                moraleOrPolice = p.morale > 35;
            }
            if (moraleOrPolice) {
                isPrepared++;
            }
        }
        int planetCount = world.ownPlanets.size();
        return isPrepared * 3 >= planetCount * 2;
    }
    /**
     * Setup the construction action for the given planet and building.
     * @param planet the target planet
     * @param bt the building type to construct
     */
    public void build(final AIPlanet planet, final BuildingType bt) {
        world.money -= bt.cost;
        planet.statistics.constructing = true;

        AIBuilding b = new AIBuilding(new Building(-1, bt, planet.race));
        planet.buildings.add(b);

        Planet.updateStatistics(planet.statistics, planet.owner, b.building);
        Planet.updateStatistics(world.global, planet.owner, b.building);

        add(new Action0() {
            @Override
            public void invoke() {
                controls.actionPlaceBuilding(planet.planet, bt);
            }
        });
    }
}
