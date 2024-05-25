/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.model.Building;
import hu.openig.model.DiplomaticRelation;
import hu.openig.model.Fleet;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.InventoryItem;
import hu.openig.model.InventorySlot;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.Player;
import hu.openig.model.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The algorithm that adjusts the knowledge level of planets and fleets for all players.
 * @author akarnokd, 2011.04.05.
 */
public final class Radar {
    /** Utility class. */
    private Radar() {
    }
    /**
     * Compute for all players.
     * @param world the world
     */
    public static void compute(World world) {
        if (world.env.isBattle()) {
            return;
        }
        final int rrg = world.params().groundRadarUnitSize();
        final int rrf = world.params().fleetRadarUnitSize();
        final double radaless = world.params().fleetRadarlessMultiplier();
        // clear knowledge
        for (Player player : world.players.values()) {
            // reset known planets to discovered state
            for (Map.Entry<Planet, PlanetKnowledge> p : player.planets.entrySet()) {
                if (p.getKey().owner != player) {
                    if (p.getValue().ordinal() > PlanetKnowledge.NAME.ordinal()) {
                        p.setValue(PlanetKnowledge.NAME);
                    }
                }
            }
            // change back to visible only
            Set<Fleet> visibleEnemySet = new HashSet<>();
            for (Map.Entry<Fleet, FleetKnowledge> f : player.fleets.entrySet()) {
                if (f.getKey().owner != player) {
                    f.setValue(FleetKnowledge.VISIBLE);
                    visibleEnemySet.add(f.getKey());
                } else {
                    f.setValue(FleetKnowledge.FULL);
                }
            }
            // run fleet-radar detection
            for (Fleet f : new ArrayList<>(player.fleets.keySet())) {
                if (f.owner == player) {
                    // find the max radar
                    double radar = radaless;
                    for (InventoryItem fi : f.inventory.iterable()) {
                        for (InventorySlot fis : fi.slots.values()) {
                            if (fis.type != null) {
                                radar = Math.max(radar, fis.type.getInt("radar", 0));
                            }
                        }
                    }
                    if (radar > 0) {
                        for (Planet q : findPlanetsInRange(world, f.x, f.y, radar * rrf)) {
                            if (radar < 1f) {
                                updateKnowledge(world, player, q, PlanetKnowledge.VISIBLE);
                            } else
                            if (radar < 1.1f) {
                                updateKnowledge(world, player, q, PlanetKnowledge.NAME);
                            } else
                            if (radar < 2.1f) {
                                updateKnowledge(world, player, q, PlanetKnowledge.OWNER);
                            } else
                            if (radar < 3.1f) {
                                updateKnowledge(world, player, q, PlanetKnowledge.OWNER);
                            }
                        }
                        for (Fleet f1 : findFleetsInRange(world, f.x, f.y, radar * rrf)) {
                            visibleEnemySet.remove(f1);
                            if (radar < 1f) {
                                updateKnowledge(world, player, f1, FleetKnowledge.VISIBLE);
                            } else
                            if (radar < 1.1) {
                                updateKnowledge(world, player, f1, FleetKnowledge.COMPOSITION);
                            } else
                            if (radar < 2.1) {
                                updateKnowledge(world, player, f1, FleetKnowledge.COMPOSITION);
                            } else
                            if (radar < 3.1) {
                                updateKnowledge(world, player, f1, FleetKnowledge.COMPOSITION);
                            }
                        }
                    }
                }
            }

            for (Planet p : player.ownPlanets()) {
                updateKnowledge(world, p.owner, p, PlanetKnowledge.BUILDING);
                int radar = 0;
                for (InventoryItem pii : p.inventory.iterable()) {
                    radar = pii.type.getInt("radar", 0);
                    if (radar > 0) {
                        for (Planet q : findPlanetsInRange(world, p.x, p.y, radar * rrg)) {
                            updateKnowledge(world, pii.owner, q, PlanetKnowledge.OWNER);
                        }
                        for (Fleet f : findFleetsInRange(world, p.x, p.y, radar * rrg)) {
                            visibleEnemySet.remove(f);
                            updateKnowledge(world, pii.owner, f, FleetKnowledge.COMPOSITION);
                        }
                    }
                }
                if (radar == 0) {
                    for (Building b : p.surface.buildings.iterable()) {
                        if (b.isOperational()) {
                            if (b.hasResource("radar")) {
                                radar = Math.max(radar, (int)b.getResource("radar"));
                            }
                        }
                    }
                    if (radar > 0) {
                        for (Planet q : findPlanetsInRange(world, p.x, p.y, radar * rrg)) {
                            updateKnowledge(world, p.owner, q, radar == 1 ? PlanetKnowledge.NAME : PlanetKnowledge.OWNER);
                        }
                        for (Fleet f1 : findFleetsInRange(world, p.x, p.y, radar * rrg)) {
                            visibleEnemySet.remove(f1);
                            updateKnowledge(world, p.owner, f1, FleetKnowledge.COMPOSITION);
                        }
                    }
                }
            }

            for (Fleet f0 : visibleEnemySet) {
                player.ai.onLostSight(f0);
                world.scripting.onLostSight(player, f0);
            }
            player.fleets.keySet().removeAll(visibleEnemySet);
        }
        // spy satellites
        for (Planet p : world.planets.values()) {
            for (InventoryItem pii : p.inventory.iterable()) {
                int detectorType = pii.type.getInt("detector", 0);
                if (detectorType == 1) {
                    updateKnowledge(world, pii.owner, p, PlanetKnowledge.OWNER);
                }
                if (detectorType == 2) {
                    updateKnowledge(world, pii.owner, p, PlanetKnowledge.STATIONS);
                }
                if (detectorType == 3) {
                    updateKnowledge(world, pii.owner, p, PlanetKnowledge.BUILDING);
                }
            }

        }
        // share radar knowledge
        for (Player p : world.players.values()) {
            for (DiplomaticRelation dr : world.relations) {
                if ((dr.first == p || dr.second == p)
                        && dr.value >= world.params().radarShareLimit()
                        && !dr.alliancesAgainst.isEmpty()) {
                    Player p2 = (dr.first == p ? dr.second : dr.first);

                    for (Map.Entry<Fleet, FleetKnowledge> fe : p.fleets.entrySet()) {
                        updateKnowledge(world, p2, fe.getKey(), fe.getValue());
                    }

                    for (Map.Entry<Planet, PlanetKnowledge> pk : p.planets.entrySet()) {
                        updateKnowledge(world, p2, pk.getKey(), pk.getValue());
                    }
                }
            }
        }
        // notify players about the radar sweep completed
        for (Player p : world.players.values()) {
            p.ai.onRadar();
        }
    }
    /**
     * Update the planet knowledge by only increasing it in the given mapping.
     * @param world the world
     * @param player the player object
     * @param planet the planet
     * @param k the new knowledge
     */
    static void updateKnowledge(World world, Player player, Planet planet, PlanetKnowledge k) {
        PlanetKnowledge k0 = player.planets.get(planet);
        if (k0 == null && planet.owner != player) {
            player.statistics.planetsDiscovered.value++;

            player.ai.onDiscoverPlanet(planet);
            world.scripting.onDiscovered(player, planet);
        }
        if (k0 == null || k0.ordinal() < k.ordinal()) {
            player.planets.put(planet, k);
        }
        if (planet.owner != null

                && planet.owner != player

                && !player.knows(planet.owner)

                && k.ordinal() >= PlanetKnowledge.OWNER.ordinal()) {
            player.setStance(planet.owner, player.initialStance);
            player.ai.onDiscoverPlayer(planet.owner);
            world.scripting.onDiscovered(player, planet.owner);
        }
    }
    /**
     * Update the planet knowledge by only increasing it in the given mapping.
     * @param world the world
     * @param player the player
     * @param fleet the fleet
     * @param k the new knowledge
     */
    static void updateKnowledge(World world, Player player, Fleet fleet, FleetKnowledge k) {
        FleetKnowledge k0 = player.fleets.get(fleet);
        if (k0 == null || k0.ordinal() < k.ordinal()) {
            player.fleets.put(fleet, k);
        }
        if (k0 == null) {
            player.ai.onDiscoverFleet(fleet);
            world.scripting.onDiscovered(player, fleet);
        }
        if (fleet.owner != null && fleet.owner != player && !player.knows(fleet.owner)) {
            player.setStance(fleet.owner, player.initialStance);
            player.ai.onDiscoverPlayer(fleet.owner);
            world.scripting.onDiscovered(player, fleet.owner);
        }
    }
    /**
     * Locate planets within the given circle.
     * @param world the game world
     * @param x the center coordinate
     * @param y the center coordinate
     * @param range the range
     * @return the list of planets in range
     */
    static List<Planet> findPlanetsInRange(World world, double x, double y, double range) {
        List<Planet> result = new ArrayList<>();
        for (Planet p : world.planets.values()) {
            if ((x - p.x) * (x - p.x) + (y - p.y) * (y - p.y) < range * range) {
                result.add(p);
            }
        }

        return result;
    }
    /**
     * Locate fleets within the given circle.
     * @param world the game world
     * @param x the center coordinate
     * @param y the center coordinate
     * @param range the range
     * @return the list of planets in range
     */
    static List<Fleet> findFleetsInRange(World world, double x, double y, double range) {
        List<Fleet> result = new ArrayList<>();
        for (Player p : world.players.values()) {
            for (Fleet f : p.fleets.keySet()) {
                if ((x - f.x) * (x - f.x) + (y - f.y) * (y - f.y) < range * range) {
                    result.add(f);
                }
            }
        }
        return result;
    }
}
