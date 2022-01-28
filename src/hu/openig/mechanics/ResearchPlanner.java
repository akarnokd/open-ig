/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.model.*;
import hu.openig.utils.Exceptions;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple research planner.
 * @author akarnokd, 2011.12.27.
 */
public class ResearchPlanner extends Planner {
    /** The set of resource names. */
    private static final Set<String> LAB_RESOURCE_NAMES =

            new HashSet<>(Arrays.asList("ai", "civil", "computer", "mechanical", "military"));
    /** Indicator to allow actions those spend money. */
    boolean maySpendMoney;
    /** Labs increasing in number of labs. */
    static final Comparator<AIPlanet> LABS_INCREASING = new Comparator<AIPlanet>() {
        @Override
        public int compare(AIPlanet o1, AIPlanet o2) {
            return Integer.compare(o1.statistics.labCount(), o2.statistics.labCount());
        }
    };
    /**
     * Constructor. Initializes the fields.
     * @param world the world object
     * @param controls the controls to affect the world in actions
     * @param exploration the exploration map
     */
    public ResearchPlanner(AIWorld world, AIControls controls, ExplorationMap exploration) {
        super(world, controls);
    }
    @Override
    public void plan() {
        if (world.level < 3) {
            return;
        }
        world.researchRequiresColonization = false;
        if (world.runningResearch != null) {
            // if not enough labs, stop research and let the other management tasks apply
            if (!world.runningResearch.hasEnoughLabs(world.global)) {
                add(new Action0() {
                    @Override
                    public void invoke() {
                        controls.actionStopResearch(world.runningResearch);
                    }
                });
            }
            return;
        }

        //if low on money and planets, plan for conquest
        maySpendMoney = (world.money >= 100000 || world.global.planetCount > 2)

                /* && world.global.militarySpaceportCount > 0 */;

        final Map<ResearchType, Integer> enablesCount = new HashMap<>();
        final Map<ResearchType, Integer> rebuildCount = new HashMap<>();
        List<ResearchType> candidatesImmediate = new ArrayList<>();
        List<ResearchType> candidatesReconstruct = new ArrayList<>();
        List<ResearchType> candidatesGetMorePlanets = new ArrayList<>();

        // prepare lab costs
        Map<String, Integer> labCosts = new HashMap<>();
        for (BuildingType bt : w.buildingModel.buildings.values()) {
            for (String s : LAB_RESOURCE_NAMES) {
                if (bt.resources.containsKey(s)) {
                    labCosts.put(s, bt.cost);
                    break;
                }
            }
        }
        for (ResearchType rt : world.researchable) {
            if (rt.hasEnoughLabs(world.global)) {
                candidatesImmediate.add(rt);
                setResearchEnables(rt, enablesCount);
            } else
            if ((rt.labCount() <= world.ownPlanets.size() || world.noLabLimit)) {
                candidatesReconstruct.add(rt);
                setResearchEnables(rt, enablesCount);
                rebuildCount.put(rt, rebuildCost(rt, labCosts));
            } else {
                candidatesGetMorePlanets.add(rt);
                setResearchEnables(rt, enablesCount);
            }
        }
        if (maySpendMoney) {
            // yield if one planet available and not enough money
            if (candidatesImmediate.size() > 0) {
                Collections.sort(candidatesImmediate, new CompareFromMap(enablesCount));
                final ResearchType rt = candidatesImmediate.get(0);
                double mf = 2.0;
                final double moneyFactor = mf; // TODO decision variable
                applyActions.add(new Action0() {
                    @Override
                    public void invoke() {
                        controls.actionStartResearch(rt, moneyFactor);
                    }
                });
                return;
            }
            if (candidatesReconstruct.size() > 0) {
                planReconstruction(rebuildCount, candidatesReconstruct);
                return;
            }
        }
        world.researchRequiresColonization = candidatesGetMorePlanets.size() > 0;
    }
    /**
     * Plan how the labs will be reconstructed to allow the next research.
     * @param rebuildCount the number of new buildings needed for each research
     * @param candidatesReconstruct the candidates for the research
     */
    void planReconstruction(
            final Map<ResearchType, Integer> rebuildCount,
            List<ResearchType> candidatesReconstruct) {
        // find the research that requires the fewest lab rebuilds
        Collections.sort(candidatesReconstruct, new CompareFromMap(rebuildCount));

        final ResearchType rt = candidatesReconstruct.get(0);

        boolean noroom = false;
        // find an empty planet
        List<AIPlanet> ps = new ArrayList<>(world.ownPlanets);
        // don't build if there is an energy shortage anywhere
        if (!checkPlanetPreparedness()) {
            return;
        }
        if (world.global.production.spaceship == 0
                || world.global.production.weapons == 0) {
            return;
        }
        if (world.noLabLimit) {
            Collections.sort(ps, LABS_INCREASING);
        }
        for (AIPlanet planet : ps) {
            if (planet.statistics.labCount() == 0 || world.noLabLimit) {
                AIResult r = buildOneLabFor(rt, planet);
                if (r == AIResult.SUCCESS || r == AIResult.NO_MONEY) {
                    return;
                } else
                if (r == AIResult.NO_ROOM) {
                    noroom = true;
                } else
                if (r == AIResult.NO_AVAIL) {
                    noroom = false;
                }
            }
        }
        // find a planet with excess labs.
        for (AIPlanet planet : world.ownPlanets) {
            if (!planet.statistics.constructing) {
                if (demolishOneLabFor(rt, planet)) {
                    return;
                }
            }
        }
        // if at least one empty planet failed to build the required lab
        // conquer more planets
        boolean enoughLabs = rt.hasEnoughLabs(world.global);
        if (noroom && !enoughLabs) {
            world.researchRequiresColonization = true;
        }
    }
    /**
     * Demolish one of the excess labs on the planet to make room.
     * @param rt the research type
     * @param planet the target planet
     * @return true if demolish added
     */
    boolean demolishOneLabFor(ResearchType rt, AIPlanet planet) {
        if (demolishOneLabIf(rt.aiLab, world.global.labs.ai, planet.statistics.labs.ai, planet, "ai")) {
            return true;
        }
        if (demolishOneLabIf(rt.civilLab, world.global.labs.civil, planet.statistics.labs.civil, planet, "civil")) {
            return true;
        }
        if (demolishOneLabIf(rt.compLab, world.global.labs.comp, planet.statistics.labs.comp, planet, "computer")) {
            return true;
        }
        if (demolishOneLabIf(rt.mechLab, world.global.labs.mech, planet.statistics.labs.mech, planet, "mechanical")) {
            return true;
        }
        return demolishOneLabIf(rt.milLab, world.global.labs.mil, planet.statistics.labs.mil, planet, "military");
    }
    /**
     * Demolish one lab of the given resource.
     * @param lab the required lab count
     * @param global the global lab count
     * @param local the local lab count
     * @param planet the planet
     * @param resource the lab resource name
     * @return true if action added
     */
    boolean demolishOneLabIf(int lab, int global, int local, final AIPlanet planet, final String resource) {
        if (lab < global && local > 0) {
            for (AIBuilding b : new ArrayList<>(planet.buildings)) {
                if (b.type.resources.containsKey(resource)) {
                    planet.buildings.remove(b);
                }
            }
            applyActions.add(new Action0() {
                @Override
                public void invoke() {
                    for (Building b : planet.planet.surface.buildings.iterable()) {
                        if (b.type.resources.containsKey(resource)) {
                            controls.actionDemolishBuilding(planet.planet, b);
                            return;
                        }
                    }
                }
            });
            return true;
        }
        return false;
    }
    /**
     * Build one of the required labs.
     * @param rt the research type
     * @param planet the target planet
     * @return the construction result
     */
    AIResult buildOneLabFor(final ResearchType rt, final AIPlanet planet) {
        int noroom = 0;
        AIResult r = buildOneLabIf(rt.aiLab, world.global.labs.ai, planet.statistics.labs.ai, planet, "ai");
        if (r != AIResult.NO_ROOM && r != AIResult.CONTINUE) {
            return r;
        }
        if (r == AIResult.NO_ROOM) {
            noroom++;
        }
        r = buildOneLabIf(rt.civilLab, world.global.labs.civil, planet.statistics.labs.civil, planet, "civil");
        if (r != AIResult.NO_ROOM && r != AIResult.CONTINUE) {
            return r;
        }
        if (r == AIResult.NO_ROOM) {
            noroom++;
        }
        r = buildOneLabIf(rt.compLab, world.global.labs.comp, planet.statistics.labs.comp, planet, "computer");
        if (r != AIResult.NO_ROOM && r != AIResult.CONTINUE) {
            return r;
        }
        if (r == AIResult.NO_ROOM) {
            noroom++;
        }
        r = buildOneLabIf(rt.mechLab, world.global.labs.mech, planet.statistics.labs.mech, planet, "mechanical");
        if (r != AIResult.NO_ROOM && r != AIResult.CONTINUE) {
            return r;
        }
        if (r == AIResult.NO_ROOM) {
            noroom++;
        }
        r = buildOneLabIf(rt.milLab, world.global.labs.mil, planet.statistics.labs.mil, planet, "military");
        if (r != AIResult.NO_ROOM && r != AIResult.CONTINUE) {
            return r;
        }
        if (r == AIResult.NO_ROOM) {
            noroom++;
        }
        if (noroom > 0) {
            return AIResult.NO_ROOM;
        }
        return AIResult.CONTINUE;
    }
    /**
     * Build one of the labs if the prerequisite counts match.
     * @param required the required count of lab
     * @param available the available count of lab
     * @param local the locally built count
     * @param planet the target planet
     * @param resource the building type identification resource
     * @return true if successful
     */
    AIResult buildOneLabIf(int required, int available, int local, final AIPlanet planet, final String resource) {
        if (required > available && (local == 0 || world.noLabLimit)) {
            if (!planet.statistics.canBuildAnything()) {
                return AIResult.NO_AVAIL;
            }
            final BuildingType bt = findBuildingType(resource);
            if (bt == null) {
                Exceptions.add(new AssertionError("Can't find building for resource " + resource));
                return AIResult.NO_AVAIL;
            }
            if (bt.cost <= world.money) {
                Point pt = planet.placement.findLocation(planet.planet.getPlacementDimensions(bt));
                if (pt != null) {
                    build(planet, bt);
                    return AIResult.SUCCESS;
                }
                return AIResult.NO_ROOM;
            }
            return AIResult.NO_MONEY;
        }
        return AIResult.CONTINUE;
    }
    /**
     * Find the first building who provides the given resource.
     * @param resource the resource name
     * @return the building type or null
     */
    BuildingType findBuildingType(String resource) {
        for (BuildingType bt : w.buildingModel.buildings.values()) {
            if (bt.resources.containsKey(resource)) {
                return bt;
            }
        }
        return null;
    }
    /**
     * Comparator which takes an value from the supplied map for comparison.

     * @author akarnokd, 2011.12.26.
     */
    class CompareFromMap implements Comparator<ResearchType> {
        /** The backing map. */
        final Map<ResearchType, Integer> map;
        /**
         * Constructor.
         * @param map the backing map to use
         */
        public CompareFromMap(Map<ResearchType, Integer> map) {
            this.map = map;
        }
        @Override
        public int compare(ResearchType o1, ResearchType o2) {
            Integer count1 = map.get(o1);
            Integer count2 = map.get(o2);
            int c = count1.compareTo(count2);
            if (c == 0) {
                c = Integer.compare(o1.researchCost(p.traits), o2.researchCost(p.traits));
            }
            if (c == 0) {
                c = o1.level - o2.level;
            }
            if (c == 0) {
                c = Integer.compare(o1.productionCost, o2.productionCost);
            }
            return c;
        }
    }
    /**
     * Count how many labs need to be built in addition to the current settings.
     * @param rt the research type
     * @param labCosts the cost of various labs
     * @return the total number of new buildings required
     */
    int rebuildCost(ResearchType rt, Map<String, Integer> labCosts) {
        return

                rebuildRequiredCount(rt.aiLab, world.global.labs.ai) * labCosts.get("ai")
                + rebuildRequiredCount(rt.civilLab, world.global.labs.civil) * labCosts.get("civil")
                + rebuildRequiredCount(rt.compLab, world.global.labs.comp) * labCosts.get("computer")
                + rebuildRequiredCount(rt.mechLab, world.global.labs.mech) * labCosts.get("mechanical")
                + rebuildRequiredCount(rt.milLab, world.global.labs.mil) * labCosts.get("military")
        ;
    }
    /**
     * If the lab count is greater than the active count, return the difference.
     * @param lab the research required lab count
     * @param active the active research counts
     * @return zero or the difference
     */
    int rebuildRequiredCount(int lab, int active) {
        if (lab > active) {
            return lab - active;
        }
        return 0;
    }
    /**
     * Counts how many further research becomes available when the research is completed.
     * @param rt the current research
     * @param map the map for research to count
     */
    void setResearchEnables(ResearchType rt, Map<ResearchType, Integer> map) {
//        int count = 0;
//        for (ResearchType rt2 : world.remainingResearch) {
//            if (rt2.prerequisites.contains(rt)) {
//                count++;
//            }
//        }
//        for (ResearchType rt2 : world.furtherResearch) {
//            if (rt2.prerequisites.contains(rt)) {
//                count++;
//            }
//        }
        map.put(rt, rt.researchCost(p.traits));
    }

}
