/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.model;

import hu.openig.v1.core.Tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A building prototype.
 * @author karnokd, 2010.01.07.
 * @version $Revision 1.0$
 */
public class BuildingType {
	/** The tileset for various races. */
	public static class TileSet {
		/** The normal building tile. */
		public Tile normal;
		/** The damaged building tile. */
		public Tile damaged;
	}
	/**
	 * The required or produced resource definition.
	 * @author karnokd, 2010.01.07.
	 * @version $Revision 1.0$
	 */
	public static class Resource {
		/** The resource type. */
		public String type;
		/** The resource amount. */
		public float amount;
	}
	/**
	 * The upgrades for the building type.
	 * @author karnokd, 2010.01.07.
	 * @version $Revision 1.0$
	 */
	public static class Upgrade {
		/** The upgrade description. */
		public String description;
		/** The resources associated with this upgrade. */
		public final Map<String, Resource> resources = new HashMap<String, Resource>();
	}
	/** The identifier. */
	public String id;
	/** The label. */
	public String label;
	/** The description label. */
	public String description;
	/** The tile set for various race (more like techraces). */
	public final Map<String, TileSet> tileset = new HashMap<String, TileSet>();
	/** The hit point amount. */
	public int hitpoints;
	/** The build cost. */
	public int cost;
	/** The building kind. */
	public String kind;
	/** The build limit: negative value represents a per-kind build limit. */
	public int limit;
	/** The planet type (surface) exception set. */
	public final Set<String> except = new HashSet<String>();
	/** The required research to be available. */
	public String research;
	/** Is the operation level dependent on the supplied energy and worker amounts? */
	public boolean percentable;
	/** The primary resource to display in the info panel. */
	public Resource primary;
	/** The resources associated with this building type. */
	public final Map<String, Resource> resources = new HashMap<String, Resource>();
	/** The ordered list of upgrades for this building, if any. */
	public final List<Upgrade> upgrades = new ArrayList<Upgrade>();
}
