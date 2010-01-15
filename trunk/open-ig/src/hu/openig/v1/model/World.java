/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.model;

import hu.openig.utils.XML;
import hu.openig.v1.core.PlanetType;
import hu.openig.v1.core.ResourceLocator;
import hu.openig.v1.core.ResourceType;
import hu.openig.v1.core.Talks;
import hu.openig.v1.core.Tile;
import hu.openig.v1.core.Walks;
import hu.openig.v1.core.ResourceLocator.ResourcePlace;
import hu.openig.v1.model.BuildingType.Resource;
import hu.openig.v1.model.BuildingType.TileSet;
import hu.openig.v1.model.BuildingType.Upgrade;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.w3c.dom.Element;

/**
 * The world object.
 * @author karnok, 2009.10.25.
 * @version $Revision 1.0$
 */
public class World {
	/** The current world level. */
	public int level;
	/** The current player. */
	public Player player;
	/** The default starmap. */
	public BufferedImage map;
	/** The minimum scaling level. */
	public float minScale;
	/** The maximum scaling level. */
	public float maxScale;
	/** The list of all players. */
	public final List<Player> players = new ArrayList<Player>();
	/** The time. */
	public final GregorianCalendar time = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
	/** All planets on the starmap. */
	public final List<Planet> planets = new ArrayList<Planet>();
	/** All fleets on the starmap. */
	public final List<Fleet> fleets = new ArrayList<Fleet>();
	/** The list of available researches. */
	public final List<ResearchType> researches = new ArrayList<ResearchType>();
	/** The list of all building types. */
	public final Map<String, BuildingType> buildings = new HashMap<String, BuildingType>();
	/** Achievements. */
	public final List<Achievement> achievements = new ArrayList<Achievement>();
	/** The planet types. */
	public final Map<String, PlanetType> planetTypes = new HashMap<String, PlanetType>();
	/** The available crew-talks. */
	public Talks talks;
	/** The ship-walk definitions. */
	public Walks walks;
	/**
	 * Load the game world's resources.
	 * @param rl the resource locator
	 * @param language the current language
	 * @param game the game directory
	 */
	public void load(ResourceLocator rl, String language, String game) {
		Element def = rl.getXML(language, game + "/def");
		Element galaxy = rl.getXML(language, game + "/galaxy");
		processGalaxy(galaxy, rl, language);
		Element races = rl.getXML(language, game + "/races");
		Element tech = rl.getXML(language, game + "/tech");
		Element buildings = rl.getXML(language, game + "/buildings");
		processBuildings(buildings, rl, language);
		Element planets = rl.getXML(language, game + "/planets");
		talks = new Talks();
		talks.load(rl, language, game);
		walks = new Walks();
		walks.load(rl, language, game);
	}
	/**
	 * Process the contents of the galaxy data.
	 * @param galaxy the galaxy
	 * @param rl the resource locator
	 * @param language the current language
	 */
	protected void processGalaxy(Element galaxy, ResourceLocator rl, String language) {
		Element background = XML.childElement(galaxy, "background");
		map = rl.getImage(language, background.getAttribute("image"));
		minScale = Float.parseFloat(background.getAttribute("min-scale"));
		maxScale = Float.parseFloat(background.getAttribute("max-scale"));
		
		Element planets = XML.childElement(galaxy, "planets");
		for (Element planet : XML.childrenWithName(planets, "planet")) {
			PlanetType planetType = new PlanetType();
			planetType.type = planet.getAttribute("type");
			planetType.label = planet.getAttribute("label");
			
			Element bodyElement = XML.childElement(planet, "body");
			planetType.body = rl.getAnimation(language, bodyElement.getTextContent(), -1, 64);
			Element tileset = XML.childElement(planet, "tileset");
			String tilePattern = tileset.getAttribute("pattern");
			
			for (Element te : XML.children(tileset)) {
				if (te.getNodeName().equals("tile-range")) {
					int start = Integer.parseInt(te.getAttribute("start"));
					int end = Integer.parseInt(te.getAttribute("end"));
					int width = Integer.parseInt(te.getAttribute("width"));
					int height = Integer.parseInt(te.getAttribute("height"));
					for (int id = start; id <= end; id++) {
						Tile tile = new Tile(width, height, rl.getImage(language, String.format(tilePattern, id)), null);
						planetType.tiles.put(id, tile);
					}
				} else
				if (te.getNodeName().equals("tile")) {
					int id = Integer.parseInt(te.getAttribute("id"));
					int width = Integer.parseInt(te.getAttribute("width"));
					int height = Integer.parseInt(te.getAttribute("height"));
					Tile tile = new Tile(width, height, rl.getImage(language, String.format(tilePattern, id)), null);
					planetType.tiles.put(id, tile);
				}
			}
			
			Element map = XML.childElement(planet, "map");
			String mapPattern = map.getAttribute("pattern");
			int start = Integer.parseInt(map.getAttribute("start"));
			int end = Integer.parseInt(map.getAttribute("end"));
			for (int i = start; i <= end; i++) {
				planetType.surfaces.put(i, rl.getData(language, String.format(mapPattern, i)));
			}
			planetTypes.put(planetType.type, planetType);
		}
	}
	/**
	 * Process the contents of the buildings definition.
	 * @param buildings the buildings definition
	 * @param rl the resource locator
	 * @param language the language
	 */
	protected void processBuildings(Element buildings, ResourceLocator rl, String language) {
		for (Element building : XML.childrenWithName(buildings, "building")) {
			BuildingType b = new BuildingType();
			
			b.id = building.getAttribute("id");
			b.label = building.getAttribute("label");
			b.description = b.label + ".desc";
			
			Element gfx = XML.childElement(building, "graphics");
			String pattern = gfx.getAttribute("base");
			for (Element r : XML.childrenWithName(gfx, "tech")) {
				TileSet ts = new TileSet();
				
				String rid = r.getAttribute("id");
				int width = Integer.parseInt(r.getAttribute("width"));
				int height = Integer.parseInt(r.getAttribute("height"));
				
				String normalImg = String.format(pattern, rid);
				String normalLight = normalImg + "_lights";
				String damagedImg = normalImg + "_damaged";
				
				BufferedImage lightMap = null;
				ResourcePlace rp = rl.get(language, normalLight, ResourceType.IMAGE);
				if (rp != null) {
					lightMap = rl.getImage(language, normalLight);
				}
				ts.normal = new Tile(width, height, rl.getImage(language, normalImg), lightMap);
				ts.damaged = new Tile(width, height, rl.getImage(language, damagedImg), null); // no lightmap for damaged building
				b.tileset.put(rid, ts);
			}
			Element bld = XML.childElement(building, "build");
			b.cost = Integer.parseInt(bld.getAttribute("cost"));
			b.kind = bld.getAttribute("kind");
			String limit = bld.getAttribute("limit");
			if ("*".equals(limit)) {
				b.limit = Integer.MAX_VALUE;
			} else {
				b.limit = Integer.parseInt(limit);
			}
			b.research = bld.getAttribute("research");
			String except = bld.getAttribute("except");
			if (except != null && !except.isEmpty()) {
				b.except.addAll(Arrays.asList(except.split("\\s*,\\s*")));
			}
			Element op = XML.childElement(building, "operation");
			b.percentable = "true".equals(op.getAttribute("percent"));
			for (Element re : XML.childrenWithName(op, "resource")) {
				Resource res = new Resource();
				res.type = re.getAttribute("type");
				res.amount = Float.parseFloat(re.getTextContent());
				b.resources.put(res.type, res);
				if ("true".equals(re.getAttribute("display"))) {
					b.primary = res;
				}
			}
			
			Element ug = XML.childElement(building , "upgrades");
			for (Element u : XML.childrenWithName(ug, "upgrade")) {
				Upgrade upg = new Upgrade();
				upg.description = u.getAttribute("desc");
				for (Element re : XML.childrenWithName(op, "resource")) {
					Resource res = new Resource();
					res.type = re.getAttribute("type");
					res.amount = Float.parseFloat(re.getTextContent());
					upg.resources.put(res.type, res);
				}
				b.upgrades.add(upg);
			}
			
			this.buildings.put(b.id, b);
		}
	}
}
