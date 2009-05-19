/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

import hu.openig.core.SurfaceType;
import hu.openig.core.Tile;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Building model prototype. Actual building instances are derive data
 * from these instances
 * @author karnokd
 */
public class GameBuildingPrototype {
	/**
	 * Record to store various building images. 
	 * @author karnokd
	 */
	public static class BuildingImages {
		/** The building's thumbnail for build list and building information. */
		public BufferedImage thumbnail;
		/** The regular building tile to use. */
		public Tile regularTile;
		/** The damaged building tile to use. */
		public Tile damagedTile;
		/** The building phases of the building. The original game had one set of images for each tech id, therefore, this list could be shared. */
		public List<Tile> buildPhases;
		/** 
		 * If the building is a planetary defense building, this list contains the 360 degrees 
		 * rotated small images of the building to display it on the space battle screen.
		 * Null indicates, this building is not for planetary defense.
		 */
		public List<BufferedImage> planetaryDefense;
	}
	/** Map from technology id to building images. */
	public Map<String, BuildingImages> images;
	/** The building health. */
	public int health;
	/** The universal building identifier. */
	public String id;
	/** The building index in the resource files. */
	public int index;
	/** The name. */
	public String name;
	/** On which surfaces cannot be built. */
	public final Set<SurfaceType> notBuildableSurfaces = new HashSet<SurfaceType>();
	/** The textual description lines. */
	public final String[] description = new String[3];
	/** Build cost. */
	public int cost;
	/** Energy consumption/production. */
	public int energy;
	/** Worker requirements/productions. */
	public int workers;
	/** The build limit per planet. */
	public BuildLimit limitType;
	/** If the limit type is FIXED_NUMBER_PER_PLANET, this field contains the actual limit value. */
	public int limitValue;
	/** Map of custom properties with various data types (probably Integer or String). */
	public final Map<String, ?> properties = new HashMap<String, Object>();
	/** 
	 * Indicates the technology to research before this building can be built by the player.
	 * All buildable objects are required to have a research technology associated. 
	 */
	public ResearchTech researchTech;
}
