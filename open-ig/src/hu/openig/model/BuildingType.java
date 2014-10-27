/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A building prototype.
 * @author akarnokd, 2010.01.07.
 */
public class BuildingType {
	/** The identifier. */
	public String id;
	/** The display name. */
	public String name;
	/** The display description. */
	public String description;
	/** The tile set for various race (more like techraces). */
	public final Map<String, TileSet> tileset = new HashMap<>();
	/** The hit point amount. */
	public int hitpoints;
	/** The build cost. */
	public int cost;
	/** The building kind. */
	public String kind;
	/** The build limit: negative value represents a per-kind build limit. */
	public int limit;
	/** In skirmish, use the default limit if the unlimited option is chosen. */
	public boolean skirmishHardLimit;
	/** The planet type (surface) exception set. */
	public final Set<String> except = new HashSet<>();
	/** The required research to be available. */
	public ResearchType research;
	/** The primary resource to display in the info panel. */
	public String primary;
	/** The resources associated with this building type. */
	public final Map<String, Resource> resources = new HashMap<>();
	/** The ordered list of upgrades for this building, if any. */
	public final List<Upgrade> upgrades = new ArrayList<>();
	/** The common scaffolding map. */
	public Map<String, Scaffolding> scaffoldings;
	/** The common minimap tiles for various building states. */
	public BuildingMinimapTiles minimapTiles;
	/** The vehicle resource type. */
	public static final String RESOURCE_VEHICLES = "vehicles";
	/** Morale resource type. */
	public static final String RESOURCE_MORALE = "morale";
	/** Population growth resource type. */
	public static final String RESOURCE_POPULATION_GROWTH = "population-growth";
	/** Radar resource. */
	public static final String RESOURCE_RADAR = "radar";
	/** Credit resource. */
	public static final String RESOURCE_CREDIT = "credit";
	/** Multiply resource. */
	public static final String RESOURCE_MULTIPLY = "multiply";
	/** Police resource. */
	public static final String RESOURCE_POLICE = "police";
	/** Hospital resource. */
	public static final String RESOURCE_HOSPITAL = "hospital";
	/** Kind constant. */
	public static final String KIND_SOCIAL = "Social";
	/** Kind constant. */
	public static final String KIND_DEFENSIVE = "Defensive";
	/** Kind constant. */
	public static final String KIND_GUN = "Gun";
	/** Kind constant. */
	public static final String KIND_MAIN_BUILDING = "MainBuilding";
	/** Kind constant. */
	public static final String KIND_FACTORY = "Factory";
	/** Comparator for ascending cost. */
	public static final Comparator<BuildingType> COST = new Comparator<BuildingType>() {
		@Override
		public int compare(BuildingType o1, BuildingType o2) {
			return Integer.compare(o1.cost, o2.cost);
		}
	};
	/**
	 * Check if a given resource is present.
	 * @param resource the resource name
	 * @return true if present
	 */
	public boolean hasResource(String resource) {
		return resources.containsKey(resource);
	}
	/**
	 * Returns the resource amount.
	 * <p>Does not check for the existence of the resource.</p>
	 * @param resource the resource name
	 * @return the amount
	 */
	public double getResource(String resource) {
		Resource r = resources.get(resource);
		return r.amount;
	}
	@Override
	public String toString() {
	    return id + " (kind = " + kind + ", cost = " + cost + ")";
	}
}
