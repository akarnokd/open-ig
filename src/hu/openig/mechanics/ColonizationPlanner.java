/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.core.Pair;
import hu.openig.core.Pred1;
import hu.openig.model.AIBuilding;
import hu.openig.model.AIControls;
import hu.openig.model.AIFleet;
import hu.openig.model.AIPlanet;
import hu.openig.model.AIWorld;
import hu.openig.model.BuildingType;
import hu.openig.model.Fleet;
import hu.openig.model.FleetTask;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.Player;
import hu.openig.model.ResearchType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Colonization planner.
 * @author akarnokd, 2012.08.20.
 */
public class ColonizationPlanner extends Planner {
    /** Indicator to allow actions those spend money. */
    boolean maySpendMoney;
    /** Colonize only planets from the colonizationTargets set? */
    public boolean explicitMode;
    /** Check colony ship function. */
    final Pred1<AIFleet> hasColonyShip = new Pred1<AIFleet>() {
        @Override
        public Boolean invoke(AIFleet value) {
            return value.hasInventory("ColonyShip");
        }
    };
    /**
     * Constructs the planner.
     * @param world the world
     * @param controls the controls
     */
    public ColonizationPlanner(AIWorld world, AIControls controls) {
        super(world, controls);
    }
    @Override
    protected void plan() {
        //if low on money and planets, plan for conquest
        maySpendMoney = (world.money >= world.autoBuildLimit + 100000 || world.global.planetCount > 2);

        if (explicitMode) {
            planExplicitConquest();
        } else {
            boolean rq = world.researchRequiresColonization;
            boolean exp = checkEnemyExpansion();
            boolean col = checkColonizersReady();
            boolean techOverfill = world.global.labs.count() >= world.global.planetCount * 3;
            if (rq || exp || col || techOverfill) {
                conquerMorePlanets();
            }
        }
    }
    /**
     * Check if we have colonizers in inventory or in fleet.
     * @return true if colonizers can be activated
     */
    boolean checkColonizersReady() {
        return world.inventoryCount("ColonyShip").first > 0
                || !findFleetsFor(FleetTask.COLONIZE, hasColonyShip).isEmpty()
                || !findFleetsWithTask(FleetTask.COLONIZE, hasColonyShip).isEmpty();
    }
    /**
     * Check if enemies have more planets then we have.
     * @return true if expansion is needed
     */
    boolean checkEnemyExpansion() {
        Map<Player, Integer> counts = new HashMap<>();
        for (AIPlanet p : world.enemyPlanets) {
            if (p.owner != null) {
                Integer i = counts.get(p.owner);
                counts.put(p.owner, i != null ? i + 1 : 1);
            }
        }
        if (!counts.isEmpty()) {
            int max = Collections.max(counts.values());
            return max > world.ownPlanets.size() && !world.unknownPlanets.isEmpty();
        }
        return false;
    }
    /**
     * Check if more planets can be conquered.
     * @return true if action taken
     */
    boolean conquerMorePlanets() {
        if (world.mayConquer
                && (world.colonizationLimit < 0
                || world.colonizationLimit > world.statistics.planetsColonized.value)) {
            // TODO this is more complicated
            planConquest();
            return true;
        }
        return false;
    }
    /**
     * Assign existing fleets to colonization tasks.
     */
    void colonizeWithExistingFleets() {
        List<AIFleet> fleets = world.ownFleets;
        List<AIFleet> candidates = new ArrayList<>();
        List<String> cts = new ArrayList<>(world.colonizationTargets);
        List<String> remaining = new ArrayList<>();
        for (final String p : cts) {
            AIPlanet aip = world.planetMap.get(p);
            if (aip != null && aip.owner == null) {
                boolean targeted = false;
                for (AIFleet f : fleets) {
                    if (f.targetPlanet != null && f.targetPlanet.id.equals(p) && f.task == FleetTask.COLONIZE) {
                        targeted = true;
                    } else
                    if (f.task == FleetTask.IDLE && f.hasInventory("ColonyShip")) {
                        candidates.add(f);
                    }
                }
                if (!targeted) {
                    remaining.add(p);
                }
            } else {
                // remove the now occupied planet from the targets
                add(new Action0() {
                    @Override
                    public void invoke() {
                        log("ColonizationPlanner, Lost sight of planet " + p);
                        ColonizationPlanner.this.p.colonizationTargets.remove(p);
                    }
                });
            }
        }
        for (final String p : remaining) {
            if (!candidates.isEmpty()) {
                final AIPlanet aip = world.planetMap.get(p);
                Collections.sort(candidates, new Comparator<AIFleet>() {
                    @Override
                    public int compare(AIFleet o1, AIFleet o2) {
                        return Double.compare(aip.distance(o1), aip.distance(o2));
                    }
                });
                final AIFleet f = candidates.remove(0);
                final Fleet f0 = f.fleet;
                final Planet p0 = aip.planet;
                f.task = FleetTask.COLONIZE;
                add(new Action0() {
                    @Override
                    public void invoke() {
                        if (f0.task == FleetTask.IDLE) {
                            f0.moveTo(p0);
                            f0.task = FleetTask.COLONIZE;
                            f0.owner.colonizationTargets.add(p0.id);
                            log("MoveToColonize, Fleet = %s (%d), Planet = %s (%s)", f0.name(), f0.id, p0.name(), p0.id);
                        }
                    }
                });
            }
        }
    }
    /** Plan to colonize the colonizationTarget planets. */
    void planExplicitConquest() {
        colonizeWithExistingFleets();
        cancelColonizersIfNecessary();
        if (checkColonizersReachedPlanet()) {
            return;
        }
        List<AIPlanet> ps = new ArrayList<>();
        outer1:
        for (String ct : world.colonizationTargets) {
            AIPlanet p = world.planetMap.get(ct);
            // check if not owned or lost track
            if (p == null || p.owner != null || p.knowledge.compareTo(PlanetKnowledge.OWNER) < 0) {
                continue;
            }
            // check if not target of one of our fleets
            for (AIFleet f : world.ownFleets) {
                if (f.targetPlanet == p.planet && f.task == FleetTask.COLONIZE) {
                    continue outer1;
                }
            }
            ps.add(p);
        }
        // if none, exit
        if (ps.isEmpty()) {
            return;
        }
        // compute how many colony ships can we produce
        int costs = 0;
        if (!world.global.hasMilitarySpaceport) {
            costs += findBuilding("MilitarySpaceport").cost;
        }
        ResearchType rt = world.isAvailable("ColonyShip");
        if (rt == null) {
            return;
        }
        if (rt.has(ResearchType.PARAMETER_NEEDS_ORBITAL_FACTORY)) {
            ResearchType rto = world.isAvailable("OrbitalFactory");
            if (rto == null) {
                if (world.global.orbitalFactory == 0) {
                    return;
                }
                rto = w.researches.get("OrbitalFactory");
            }
            if (world.global.orbitalFactory == 0) {
                costs += rto.productionCost;
            }
        }

        long avail = /* world.money - */ world.autoBuildLimit - costs;

        int pcount = (int)Math.max(1, avail / rt.productionCost);

        planFleetOrProduction(ps, pcount);
    }
    /**
     * Plan fleet deployment or production.
     * @param ps the list of target planets
     * @param maxProduction the maximum production
     */
    void planFleetOrProduction(List<AIPlanet> ps, int maxProduction) {
        if (assignFleetsToColonization(ps)) {
            return;
        }

        final Pair<Boolean, AIPlanet> spaceport = findMilitarySpaceport();
        // if no planet has military spaceport, build one somewhere
        if (!spaceport.first) {
            return;
        }
        if (maySpendMoney && spaceport.second == null) {
            if (world.isAvailable("ColonyShip") != null && world.isAvailable("OrbitalFactory") != null) {
                buildMilitarySpaceport();
            }
        }
        if (spaceport.second != null) {
            if (deployInventoryColonyShip(spaceport.second)) {
                return;
            }
            if (maySpendMoney) {
                if (checkOrbitalFactory()) {
                    return;
                }
                final ResearchType cs = world.isAvailable("ColonyShip");
                if (cs != null) {
                    placeProductionOrder(cs, maxProduction, true);
                }
            }
        }
    }
    /**
     * Plan for conquest.
     */
    void planConquest() {
        cancelColonizersIfNecessary();
        if (checkColonizersReachedPlanet()) {
            return;
        }
        List<AIPlanet> ps = findColonizablePlanets();
        // if none, exit
        if (ps.isEmpty()) {
            return;
        }
        planFleetOrProduction(ps, 1);
    }
    /**
     * Check if the colonization fleets' targets are still marked for colonization
     * or still known/empty.
     */
    void cancelColonizersIfNecessary() {
        List<AIFleet> colonizers = findFleetsWithTask(FleetTask.COLONIZE, hasColonyShip);
        for (final AIFleet fleet : colonizers) {
            if (fleet.targetPlanet != null) {
                AIPlanet pl = world.planetMap.get(fleet.targetPlanet);
                boolean cancel = false;
                String reasonStr = "";
                if (pl == null) {
                    cancel = true;
                    reasonStr = "Planet not found";
                } else if (pl.owner != null) {
                    cancel = true;
                    reasonStr = "Planet already colonized by " + pl.owner.name;
                } else if (!explicitMode && pl.knowledge.compareTo(PlanetKnowledge.OWNER) < 0) {
                    cancel = true;
                    reasonStr = "Lost sight of owner (implicit mode)";
                } else if (explicitMode && !world.colonizationTargets.contains(pl.planet.id)) {
                    cancel = true;
                    reasonStr = "Player cancelled";
                }
                if (cancel) {
                    final String fReasonStr = reasonStr;
                    // stop the fleet
                    add(new Action0() {
                        @Override
                        public void invoke() {
                            String targetPlanet = fleet.fleet.targetPlanet() != null ? fleet.fleet.targetPlanet().name() : "?";
                            log("CancelColonization, Fleet = %s (%d), Planet = %s, Reason = %s",
                                    fleet.fleet.name(), fleet.fleet.id, targetPlanet, fReasonStr);
                            fleet.fleet.stop();
                        }
                    });
                    // mark it as stopped here as well
                    fleet.targetPlanet = null;
                    fleet.task = FleetTask.IDLE;
                }
            }
        }
    }
    /**
     * Check if the colonizers have actually reached their planet.
     * @return true if action taken
     */
    boolean checkColonizersReachedPlanet() {
        boolean result = false;
        List<AIFleet> colonizers = findFleetsWithTask(FleetTask.COLONIZE, hasColonyShip);
        for (AIFleet fleet : colonizers) {
            if (!fleet.isMoving()
                    && fleet.statistics.planet != null) {
                final Fleet f0 = fleet.fleet;
                final Planet p0 = fleet.statistics.planet;
                fleet.task = FleetTask.IDLE;
                add(new Action0() {
                    @Override
                    public void invoke() {
                        p.colonizationTargets.remove(p0.id);
                        if (p0.owner == null) {
                            controls.actionColonizePlanet(f0, p0);
                            log("ColonizePlanetFall, Fleet = %s (%d), Planet = %s (%s)", f0.name(), f0.id, p0.name(), p0.id);
                        } else {
                            log("ColonizePlanetFall, Fleet = %s (%d), Planet = %s (%s), FAILED = no longer empty", f0.name(), f0.id, p0.name(), p0.id);
                        }
                        f0.task = FleetTask.IDLE;
                    }
                });
                result = true;
                world.colonizationTargets.remove(p0.id);
            }
        }
        // if our colonizers are under way
        if (colonizers.size() > 0) {
            result = true;
        }
        return result;
    }
    /**
     * @return list of colonizable planets not already targeted
     */
    List<AIPlanet> findColonizablePlanets() {
        // locate knownly colonizable planets
        List<AIPlanet> ps = new ArrayList<>();
        outer1:
        for (AIPlanet p : world.enemyPlanets) {
            if (p.owner == null && world.withinLimits(p.planet.x, p.planet.y)) {
                // check if no one targets this planet already
                for (AIFleet f : world.ownFleets) {
                    if (f.targetPlanet == p.planet && f.task == FleetTask.COLONIZE) {
                        continue outer1;
                    }
                }
                ps.add(p);
            }
        }
        return ps;
    }
    /**
     * Assign available fleets to colonization task.
     * @param ps the target planet
     * @return true if action taken
     */
    boolean assignFleetsToColonization(List<AIPlanet> ps) {
        boolean result = false;
        // bring one fleet to the target planet
        for (final AIFleet fleet : findFleetsFor(FleetTask.COLONIZE, hasColonyShip)) {
            final AIPlanet p0 = Collections.min(ps, new Comparator<AIPlanet>() {
                @Override
                public int compare(AIPlanet o1, AIPlanet o2) {
                    double d1 = Math.hypot(fleet.x - o1.planet.x, fleet.y - o1.planet.y);
                    double d2 = Math.hypot(fleet.x - o2.planet.x, fleet.y - o2.planet.y);
                    return d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
                }
            });
            fleet.task = FleetTask.COLONIZE;
            add(new Action0() {
                @Override
                public void invoke() {
                    if (fleet.fleet.task != FleetTask.SCRIPT) {
                        controls.actionMoveFleet(fleet.fleet, p0.planet);
                        fleet.fleet.task = FleetTask.COLONIZE;
                        fleet.fleet.owner.colonizationTargets.add(p0.planet.id);
                        log("MoveToColonize2, Fleet = %s (%d), Planet = %s (%s)", fleet.fleet.name(), fleet.fleet.id, p0.planet.name(), p0.planet.id);
                    }
                }
            });
            result = true;
        }
        return result;
    }
    /**
     * Find a military spaceport.
     * @return (false, null) if a military spaceport is being constructed,
     * (true, null) no military spaceport found, (true, X) found at planet X
     */
    Pair<Boolean, AIPlanet> findMilitarySpaceport() {
        AIPlanet sp = null;
        for (AIPlanet pl : world.ownPlanets) {
            if (pl.statistics.hasMilitarySpaceport) {
                sp = pl;
                break;
            }
            // if constructing here, return
            for (AIBuilding b : pl.buildings) {
                if (b.type.id.equals("MilitarySpaceport") && pl.statistics.constructing) {
                    return Pair.of(false, null);
                }
            }
        }
        return Pair.of(true, sp);
    }
    /**
     * Build a military spaceport at the best planet.
     */
    void buildMilitarySpaceport() {
        final BuildingType bt = findBuilding("MilitarySpaceport");
        planCategory(new Pred1<AIPlanet>() {
            @Override
            public Boolean invoke(AIPlanet value) {
                return true;
            }
        }, BEST_PLANET, new BuildingSelector() {
            @Override
            public boolean accept(AIPlanet planet, AIBuilding building) {
                return false;
            }
            @Override
            public boolean accept(AIPlanet planet, BuildingType buildingType) {
                return buildingType == bt && count(planet, bt) < 1;
            }
        }, costOrder, false);
    }
    /**
     * Deploy a colony ship from inventory.
     * @param spaceport the target planet
     * @return true if action taken
     */
    boolean deployInventoryColonyShip(final AIPlanet spaceport) {
        // check if we have colony ships in the inventory
        final Pair<Integer, ResearchType> csi = world.inventoryCount("ColonyShip");
        if (csi.first > 0) {
            world.addInventoryCount(csi.second, -1);
            add(new Action0() {
                @Override
                public void invoke() {
                    if (p.inventoryCount(csi.second) > 0) {
                        Fleet f = controls.actionCreateFleet(format("colonizer_fleet", p.shortName), spaceport.planet);
                        f.deployItem(csi.second, p, 1);
                    }
                }
            });
            return true;
        }
        return false;
    }

}
