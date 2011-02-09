/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

import hu.openig.core.PlanetType;
import hu.openig.core.ResourceLocator;
import hu.openig.core.Tile;
import hu.openig.utils.WipPort;
import hu.openig.utils.XElement;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

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
	 * @param exec the executor for parallel processing
	 * @param wip the wip counter
	 */
	public void processGalaxy(final ResourceLocator rl, final String language, 
			final String data, ExecutorService exec, final WipPort wip) {
		wip.inc();
		XElement galaxy = rl.getXML(language, data);
		XElement background = galaxy.childElement("background");
		map = rl.getImage(language, background.get("image"));
		minScale = Float.parseFloat(background.get("min-scale"));
		maxScale = Float.parseFloat(background.get("max-scale"));
		
		XElement planets = galaxy.childElement("planets");
		for (final XElement planet : planets.childrenWithName("planet")) {
			wip.inc();
			exec.submit(new Runnable() {
				@Override
				public void run() {
					PlanetType planetType = new PlanetType();
					planetType.type = planet.get("type");
					planetType.label = planet.get("label");
					
					XElement bodyElement = planet.childElement("body");
					planetType.body = rl.getAnimation(language, bodyElement.content, -1, 64);
					XElement tileset = planet.childElement("tileset");
					String tilePattern = tileset.get("pattern");
					
					for (XElement te : tileset) {
						if (te.name.equals("tile-range")) {
							int start = Integer.parseInt(te.get("start"));
							int end = Integer.parseInt(te.get("end"));
							String ws = te.get("width");
							int width = ws != null && !ws.isEmpty() ? Integer.parseInt(ws) : 1;
							String hs = te.get("height");
							int height = hs != null && !hs.isEmpty() ? Integer.parseInt(hs) : 1;
							for (int id = start; id <= end; id++) {
								Tile tile = new Tile(width, height, rl.getImage(language, String.format(tilePattern, id)), null);
								planetType.tiles.put(id, tile);
							}
						} else
						if (te.name.equals("tile")) {
							int id = Integer.parseInt(te.get("id"));
							String ws = te.get("width");
							int width = ws != null && !ws.isEmpty() ? Integer.parseInt(ws) : 1;
							String hs = te.get("height");
							int height = hs != null && !hs.isEmpty() ? Integer.parseInt(hs) : 1;
							Tile tile = new Tile(width, height, rl.getImage(language, String.format(tilePattern, id)), null);
							planetType.tiles.put(id, tile);
						}
					}
					
					XElement map = planet.childElement("map");
					String mapPattern = map.get("pattern");
					int start = Integer.parseInt(map.get("start"));
					int end = Integer.parseInt(map.get("end"));
					for (int i = start; i <= end; i++) {
						planetType.surfaces.put(i, rl.getData(language, String.format(mapPattern, i)));
					}
					synchronized (planetTypes) {
						planetTypes.put(planetType.type, planetType);
					}
					wip.dec();
				}
			});
		}
		wip.dec();
	}

}
