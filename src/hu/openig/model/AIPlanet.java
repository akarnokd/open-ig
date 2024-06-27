/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.*;
import java.util.*;
import java.util.List;

import hu.openig.core.Location;
import hu.openig.model.PlanetSurface.PlacementHelper;

/**
 * Class representing a planet for the AI player.
 * @author akarnokd, 2011.12.08.
 */
public class AIPlanet {
    /** The parent AI world. */
    public AIWorld world;
    /** The original planet. */
    public Planet planet;
    /** The owner. */
    public Player owner;
    /** The knowledge level about the planet. */
    public PlanetKnowledge knowledge;
    /** The planet statistics. */
    public PlanetStatistics statistics;
    /** The radar range. */
    public int radar;
    /** The population. */
    public int population;
    /** The last trade income. */
    public int tradeIncome;
    /** The inventory items of the planet. */
    public final List<AIInventoryItem> inventory = new ArrayList<>();
    /** Building list. */
    public final List<AIBuilding> buildings = new ArrayList<>();
    /** The building counts per type, including under construction. */
    public final Map<BuildingType, Integer> buildingCounts = new HashMap<>();
    /** Copy of the object containing basic surface information for placements. */
    public PlanetSurface.SurfaceCellArray surfaceCells;
    /** The placement helper. */
    public PlacementHelper placement;
    /** The current morale. */
    public double morale;
    /** The last morale. */
    public double lastMorale;
    /** The tax level. */
    public TaxLevel tax;
    /** The auto build state. */
    public AutoBuild autoBuild;
    /** Has any satellite of the current evaluating AI? */
    public boolean hasSatelliteOfAI;
    /** The current race on the planet. */
    public String race;
    /**
     * Assign the necessary properties from a planet.
     * @param planet the target fleet
     * @param world the world object
     */
    public void assign(final Planet planet, final AIWorld world) {
        this.world = world;
        this.planet = planet;
        this.owner = planet.owner;
        this.knowledge = world.knowledge(planet);
        this.statistics = world.getStatistics(planet);
        this.radar = planet.radar;
        this.population = (int)planet.population();
        this.tradeIncome = (int)planet.tradeIncome();
        this.morale = planet.morale();
        this.lastMorale = planet.lastMorale();
        this.tax = planet.tax;
        this.autoBuild = planet.autoBuild;
        this.race = planet.race;

        for (InventoryItem ii : planet.inventory.iterable()) {
            inventory.add(new AIInventoryItem(ii));
        }

        surfaceCells = planet.surface.surfaceCells.copy();

        final int width = planet.surface.width;
        final int height = planet.surface.height;

        for (Building b : planet.surface.buildings.iterable()) {
            buildings.add(new AIBuilding(b));

            Integer c = buildingCounts.get(b.type);
            buildingCounts.put(b.type, c != null ? c + 1 : 1);
        }

        hasSatelliteOfAI = hasSatelliteOf(world.player);

        placement = new PlacementHelper() {
            @Override
            protected int width() {
                return width;
            }

            @Override
            protected int height() {
                return height;
            }

            @Override
            protected Map<Location, SurfaceEntity> basemap() {
                return planet.surface.basemap;
            }
            @Override
            protected Buildings buildings() {
                // prevents gravity adjust, not really necessary for placement tests
                return new Buildings();
            }
            @Override
            protected PlanetSurface.SurfaceCellArray surfaceCellArray() {
                return surfaceCells;
            }
        };
    }
    /**
     * Try to find a suitable location for the given building type.
     * @param bt the building type
     * @return the point or null if none
     */
    public Point findLocation(BuildingType bt) {
        Dimension dim = planet.getPlacementDimensions(bt);
        if (dim != null) {
            return placement.findLocation(dim);
        }
        return null;
    }
    /**
     * Check if the given technology is in the inventory.
     * @param rt the technology
     * @return true if present
     */
    public boolean hasInventory(ResearchType rt) {
        for (AIInventoryItem ii : inventory) {
            if (ii.type == rt && ii.count > 0) {
                return true;
            }
        }
        return false;
    }
    /**
     * Test if another instance of the building type can be built on this planet.
     * It checks for the building limits and surface type.
     * @param bt the building type to test
     * @return can be built here?
     */
    public boolean canBuild(BuildingType bt) {
        return Planet.canBuild(planet, buildings, world.availableResearch, bt, true);
    }
    /**
     * Test if another instance of the building type can be built on this planet
     * as replacement.
     * It checks for the building limits and surface type.
     * @param bt the building type to test
     * @return can be built here?
     */
    public boolean canBuildReplacement(BuildingType bt) {
        return Planet.canBuild(planet, buildings, world.availableResearch, bt, false);
    }
    /**
     * Returns the inventory count of the specified technology.
     * @param rt the technology
     * @return the count
     */
    public int inventoryCount(ResearchType rt) {
        int result = 0;
        for (AIInventoryItem ii : inventory) {
            if (ii.type == rt) {
                result += ii.count;
            }
        }
        return result;
    }
    /**
     * Returns the inventory count of the specified technology.
     * @param rt the technology
     * @param owner the owner
     * @return the count
     */
    public int inventoryCount(ResearchType rt, Player owner) {
        int result = 0;
        for (AIInventoryItem ii : inventory) {
            if (ii.type == rt && owner == ii.owner) {
                result += ii.count;
            }
        }
        return result;
    }
    /**
     * Add a specific amount to the current inventory level.
     * @param type the type
     * @param owner the owner
     * @param count the count change
     */
    public void addInventoryCount(ResearchType type, Player owner, int count) {
        Iterator<AIInventoryItem> it = inventory.iterator();
        while (it.hasNext()) {
            AIInventoryItem ii = it.next();
            if (ii.type == type && ii.owner == owner) {
                int cnt1 = ii.count + count;
                if (cnt1 <= 0) {
                    it.remove();
                } else {
                    ii.count = cnt1;
                }
                break;
            }
        }
        if (count > 0) {
            AIInventoryItem ii = new AIInventoryItem();
            ii.count = count;
            ii.owner = owner;
            ii.type = type;
            inventory.add(ii);
        }
    }
    /**
     * Returns the distance from the fleet.
     * @param fleet the fleet
     * @return the distance value
     */
    public double distance(AIFleet fleet) {
        return Math.hypot(fleet.x - planet.x, fleet.y - planet.y);
    }
    @Override
    public String toString() {
        return planet.id;
    }
    /**
     * Checks if the planet has satellites of the given player.
     * @param p the other player
     * @return true if satellite present
     */
    public boolean hasSatelliteOf(Player p) {
        for (AIInventoryItem ii : inventory) {
            if (ii.owner == p && ii.type.has(ResearchType.PARAMETER_DETECTOR)) {
                return true;
            }
        }
        return false;
    }
}
