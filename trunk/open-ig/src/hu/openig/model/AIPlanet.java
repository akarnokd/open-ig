/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Location;
import hu.openig.model.PlanetSurface.PlacementHelper;
import hu.openig.utils.JavaUtils;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
	/** The inventory items of the planet. */
	public final List<AIInventoryItem> inventory = JavaUtils.newArrayList();
	/** Set of locations where no buildings may be placed. */
	public final Map<Location, SurfaceEntity> nonbuildable = JavaUtils.newHashMap();
	/** Building list. */
	public final List<AIBuilding> buildings = new ArrayList<AIBuilding>();
	/** The placement helper. */
	public PlacementHelper placement;
	/** The current morale. */
	public int morale;
	/** The last morale. */
	public int lastMorale;
	/** The tax level. */
	public TaxLevel tax;
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
		this.population = planet.population;
		this.morale = planet.morale;
		this.lastMorale = planet.lastMorale;
		this.tax = planet.tax;
		
		for (InventoryItem ii : planet.inventory) {
			inventory.add(new AIInventoryItem(ii));
		}
		nonbuildable.putAll(planet.surface.buildingmap);
		
		final int width = planet.surface.width;
		final int height = planet.surface.height;

		for (Building b : planet.surface.buildings) {
			buildings.add(new AIBuilding(b));
		}
		
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
			protected boolean cellInMap(int x, int y) {
				return planet.surface.cellInMap(x, y);
			}

			@Override
			protected Map<Location, SurfaceEntity> buildingmap() {
				return nonbuildable;
			}

			@Override
			protected Map<Location, SurfaceEntity> basemap() {
				return planet.surface.basemap;
			}
			@Override
			protected List<Building> buildings() {
				// prevents gravity adjust, not really necessary for placement tests
				return Collections.emptyList();
			}
		};
	}
	/**
	 * Try to find a suitable location for the given building type.
	 * @param bt the building type
	 * @return the point or null if none
	 */
	public Point findLocation(BuildingType bt) {
		return placement.findLocation(planet.getPlacementDimensions(bt));
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
		return Planet.canBuild(planet, buildings, world.availableResearch, bt);
	}
}
