/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import hu.openig.utils.XML;
import hu.openig.core.PlanetType;
import hu.openig.core.ResourceLocator;
import hu.openig.core.Tile;

import org.w3c.dom.Element;

/**
 * The Galaxy Model describing the planet types and surface tiles.
 * @author karnokd
 */
public class GalaxyModel {
	/** The default starmap. */
	public BufferedImage map;
	/** The minimum scaling level. */
	public float minScale;
	/** The maximum scaling level. */
	public float maxScale;
	/** The planet types. */
	public final Map<String, PlanetType> planetTypes = new HashMap<String, PlanetType>();
	/**
	 * Process the contents of the galaxy data.
	 * @param rl the resource locator
	 * @param language the current language
	 * @param data the galaxy data file
	 */
	public void processGalaxy(ResourceLocator rl, String language, String data) {
		Element galaxy = rl.getXML(language, data);
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
					String ws = te.getAttribute("width");
					int width = ws != null && !ws.isEmpty() ? Integer.parseInt(ws) : 1;
					String hs = te.getAttribute("height");
					int height = hs != null && !hs.isEmpty() ? Integer.parseInt(hs) : 1;
					for (int id = start; id <= end; id++) {
						Tile tile = new Tile(width, height, rl.getImage(language, String.format(tilePattern, id)), null);
						planetType.tiles.put(id, tile);
					}
				} else
				if (te.getNodeName().equals("tile")) {
					int id = Integer.parseInt(te.getAttribute("id"));
					String ws = te.getAttribute("width");
					int width = ws != null && !ws.isEmpty() ? Integer.parseInt(ws) : 1;
					String hs = te.getAttribute("height");
					int height = hs != null && !hs.isEmpty() ? Integer.parseInt(hs) : 1;
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

}
