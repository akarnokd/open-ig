/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

import hu.openig.core.BuildLimit;
import hu.openig.core.BuildingLookup;
import hu.openig.core.SurfaceType;
import hu.openig.core.Tile;
import hu.openig.utils.XML;
import hu.openig.utils.XML.XmlProcessor;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
		/** The damaged building phases of the building. The original game had one set of images for each tech id, therefore, this list could be shared. */
		public List<Tile> damagedPhases;
		/** The technology id. */
		public String techId;
		/** 
		 * If the building is a planetary defense building, this list contains the 360 degrees 
		 * rotated small images of the building to display it on the space battle screen.
		 * Null indicates, this building is not for planetary defense.
		 */
		public List<BufferedImage> planetaryDefense;
	}
	/** Map from technology id to building images. */
	public final Map<String, BuildingImages> images = new HashMap<String, BuildingImages>();
	/** The building health. */
	public int health;
	/** The universal building identifier. */
	public String id;
	/** The building kind used for grouping. */
	public String kind;
	/** The building index in the resource files. */
	public int index;
	/** The name. */
	public String name;
	/** On which surfaces cannot be built. */
	public final Set<SurfaceType> notBuildableSurfaces = new HashSet<SurfaceType>();
	/** The textual description lines. */
	public String[] description = new String[3];
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
	public final Map<String, Object> properties = new HashMap<String, Object>();
	/** The set of product types which should be listed in the building properties page. */
	public static final Set<String> PRODUCT_TYPES = new HashSet<String>(
		Arrays.<String>asList("living-space", "energy", "food", "hospital", "credit", "credit-dup")
	);
	/** The main building kind to check if a building could be built on a planet. */
	public static final String MAIN_BUILDING = "MainBuilding";
	/** 
	 * Indicates the technology to research before this building can be built by the player.
	 * Not all buildable objects are required to have a research technology associated. 
	 */
	public ResearchTech researchTech;
	/**
	 * Parses and processes a building XML.
	 * @param resource the name of the resource
	 * @param lookup the building images lookup
	 * @return list of planets
	 */
	public static Map<String, GameBuildingPrototype> parse(String resource, final BuildingLookup lookup) {
		Map<String, GameBuildingPrototype> planet = XML.parseResource(resource, new XmlProcessor<Map<String, GameBuildingPrototype>>() {
			@Override
			public Map<String, GameBuildingPrototype> process(Document doc) {
				return GameBuildingPrototype.process(doc, lookup);
			}
		});
		return planet != null ? planet : new HashMap<String, GameBuildingPrototype>();
	}
	/**
	 * Processes a buildings.xml document.
	 * @param root the document 
	 * @param lookup building images lookup
	 * @return the list of buildings
	 */
	private static Map<String, GameBuildingPrototype> process(Document root, BuildingLookup lookup) {
		Map<String, GameBuildingPrototype> result = new LinkedHashMap<String, GameBuildingPrototype>();
		// query for all building phases.
		Map<String, List<Tile>> buildingPhases = lookup.getBuildingPhases();
		Map<String, List<Tile>> damagedPhases = lookup.getDamagedBuildingPhases();
		// loop on each building entry
		for (Element e : XML.childrenWithName(root.getDocumentElement(), "building")) {
			GameBuildingPrototype b = new GameBuildingPrototype();
			b.id = e.getAttribute("id");
			b.index = Integer.parseInt(e.getAttribute("index"));
			b.name = lookup.getNameLabel(b.index);
			b.description = lookup.getDescriptionLabels(b.index);
			for (Element e1 : XML.children(e)) {
				if ("cost".equals(e1.getNodeName())) {
					b.cost = Integer.parseInt(e1.getTextContent());
				} else
				if ("worker".equals(e1.getNodeName())) {
					b.workers = Integer.parseInt(e1.getTextContent());
				} else
				if ("energy".equals(e1.getNodeName())) {
					b.energy = Integer.parseInt(e1.getTextContent());
					if (b.energy > 0) {
						b.properties.put("energy", e1.getTextContent());
					}
				} else
				if ("hp".equals(e1.getNodeName())) {
					b.health = Integer.parseInt(e1.getTextContent());
				} else
				if ("nobuild".equals(e1.getNodeName())) {
					String nobuild = e1.getTextContent();
					if (nobuild.length() > 0) {
						String[] st = nobuild.split("\\s*,\\s*");
						for (String s : st) {
							b.notBuildableSurfaces.add(SurfaceType.MAP.get(s));
						}
					}
				} else
				if ("kind".equals(e1.getNodeName())) {
					b.kind = e1.getTextContent();
				} else
				if ("limit".equals(e1.getNodeName())) {
					String limit = e1.getTextContent();
					if ("*".equals(limit)) {
						b.limitType = BuildLimit.UNLIMITED;
						b.limitValue = Integer.MAX_VALUE;
					} else
					if (limit.startsWith("-")) {
						b.limitType = BuildLimit.FIXED_KIND_PER_PLANET;
						b.limitValue = -Integer.parseInt(limit);
					} else {
						b.limitType = BuildLimit.FIXED_NUMBER_PER_PLANET;
						b.limitValue = Integer.parseInt(limit);
					}
				} else
				if ("tile".equals(e1.getNodeName())) {
					String techid = e1.getAttribute("techid");
					int y = Integer.parseInt(e1.getAttribute("width"));
					int x = Integer.parseInt(e1.getAttribute("height"));
					BuildingImages bi = new BuildingImages();
					bi.techId = techid;
					bi.thumbnail = lookup.getThumbnail(techid, b.index);
					bi.regularTile = new Tile();
					bi.regularTile.width = x;
					bi.regularTile.height = y;
					bi.regularTile.rawImage = lookup.getBuildingTile(techid, b.index, false);
					bi.damagedTile = new Tile();
					bi.damagedTile.width = x;
					bi.damagedTile.height = y;
					bi.damagedTile.rawImage = lookup.getBuildingTile(techid, b.index, true);
					bi.buildPhases = buildingPhases.get(techid);
					bi.damagedPhases = damagedPhases.get(techid);
					String offset = e1.getAttribute("offset");
					if (offset != null && offset.length() > 0) {
						bi.regularTile.heightCorrection = Integer.parseInt(offset);
						bi.damagedTile.heightCorrection = Integer.parseInt(offset);
					}
					b.images.put(techid, bi);
				} else {
					b.properties.put(e1.getNodeName(), e1.getTextContent());
				}
			}
			result.put(b.id, b);
		}
		return result;
	}
	/** Comparator by index of the building prototype. */
	public static final Comparator<GameBuildingPrototype> BY_INDEX = new Comparator<GameBuildingPrototype>() {
		@Override
		public int compare(GameBuildingPrototype o1, GameBuildingPrototype o2) {
			return o1.index - o2.index;
		}
	};
	/**
	 * @return true if this building consumes energy (colony hubs don't require energy, power plants produce energy)
	 */
	public boolean isEnergyConsumer() {
		return energy < 0;
	}
}
