/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

import hu.openig.core.BuildingLookup;
import hu.openig.core.SurfaceType;
import hu.openig.core.Tile;
import hu.openig.utils.XML;
import hu.openig.utils.XML.XmlProcessor;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
	public static List<GameBuildingPrototype> parse(String resource, final BuildingLookup lookup) {
		List<GameBuildingPrototype> planet = XML.parseResource(resource, new XmlProcessor<List<GameBuildingPrototype>>() {
			@Override
			public List<GameBuildingPrototype> process(Document doc) {
				return GameBuildingPrototype.process(doc, lookup);
			}
		});
		return planet != null ? planet : new ArrayList<GameBuildingPrototype>();
	}
	/**
	 * Processes a buildings.xml document.
	 * @param root the document 
	 * @param lookup building images lookup
	 * @return the list of buildings
	 */
	private static List<GameBuildingPrototype> process(Document root, BuildingLookup lookup) {
		List<GameBuildingPrototype> result = new ArrayList<GameBuildingPrototype>();
		// query for all building phases.
		Map<String, List<Tile>> buildingPhases = lookup.getBuildingPhases();
		// loop on each building entry
		for (Element e : XML.childrenWithName(root.getDocumentElement(), "building")) {
			GameBuildingPrototype b = new GameBuildingPrototype();
			b.id = e.getAttribute("id");
			b.index = Integer.parseInt(e.getAttribute("index"));
			b.name = lookup.getNameLabel(b.index);
			b.description = lookup.getDescriptionLabels(b.index);
			for (Element e1 : XML.children(e)) {
				if ("cost".equals(e1.getLocalName())) {
					b.cost = Integer.parseInt(e1.getTextContent());
				} else
				if ("worker".equals(e1.getLocalName())) {
					b.workers = Integer.parseInt(e1.getTextContent());
				} else
				if ("energy".equals(e1.getLocalName())) {
					b.energy = Integer.parseInt(e1.getTextContent());
				} else
				if ("hp".equals(e1.getLocalName())) {
					b.health = Integer.parseInt(e1.getTextContent());
				} else
				if ("tile".equals(e1.getLocalName())) {
					String techid = e1.getAttribute("techid");
					boolean multi = "true".equals(e1.getAttribute("b"));
					int x = Integer.parseInt(e1.getAttribute("x"));
					int y = Integer.parseInt(e1.getAttribute("y"));
					BuildingImages bi = new BuildingImages();
					bi.thumbnail = lookup.getThumbnail(techid, b.index);
					bi.regularTile = new Tile();
					bi.regularTile.width = x;
					bi.regularTile.height = y;
					bi.regularTile.rawImage = lookup.getBuildingTile(techid, b.index, multi, false);
					bi.damagedTile = new Tile();
					bi.damagedTile.width = x;
					bi.damagedTile.height = y;
					bi.damagedTile.rawImage = lookup.getBuildingTile(techid, b.index, multi, true);
					bi.buildPhases = buildingPhases.get(techid);
				} else {
					b.properties.put(e.getLocalName(), e.getTextContent());
				}
			}
			result.add(b);
		}
		return result;
	}

}
