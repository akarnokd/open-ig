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

import java.util.List;
import java.util.Map;

/**
 * Class representing a planet for the AI player.
 * @author akarnokd, 2011.12.08.
 */
public class AIPlanet {
	/** The original planet. */
	public Planet planet;
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
	/** The placement helper. */
	public PlacementHelper placement;
	/**
	 * Assign the necessary properties from a planet.
	 * @param planet the target fleet
	 * @param world the world object
	 */
	public void assign(final Planet planet, AIWorld world) {
		this.planet = planet;
		this.knowledge = world.knowledge(planet);
		this.statistics = world.getStatistics(planet);
		this.radar = planet.radar;
		this.population = planet.population;
		for (InventoryItem ii : planet.inventory) {
			inventory.add(new AIInventoryItem(ii));
		}
		nonbuildable.putAll(planet.surface.buildingmap);
		
		final int width = planet.surface.width;
		final int height = planet.surface.height;
		
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
			
		};
	}
}
