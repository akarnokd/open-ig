/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;


import java.util.ArrayList;
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
	public ResearchType research;
	/** Is the operation level dependent on the supplied energy and worker amounts? */
	public boolean percentable;
	/** The primary resource to display in the info panel. */
	public String primary;
	/** The resources associated with this building type. */
	public final Map<String, Resource> resources = new HashMap<String, Resource>();
	/** The ordered list of upgrades for this building, if any. */
	public final List<Upgrade> upgrades = new ArrayList<Upgrade>();
	/** The common scaffolding map. */
	public Map<String, Scaffolding> scaffoldings;
	/** The common minimap tiles for various building states. */
	public BuildingMinimapTiles minimapTiles;
	
}
