/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

import hu.openig.core.Pair;
import hu.openig.utils.U;
import hu.openig.utils.WipPort;
import hu.openig.utils.XElement;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * The Galaxy Model describing the planet types and surface tiles.
 * @author akarnokd
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
	/** The configuration. */
	protected final Configuration config;
	/** The population growth map. */
	protected final Map<Pair<String, String>, Double> populationGrowth = U.newHashMap();
	/**
	 * Constructor. Set the configuration.
	 * @param config the configuration
	 */
	public GalaxyModel(Configuration config) {
		this.config = config;
	}
	/**
	 * Process the contents of the galaxy data.
	 * @param rl the resource locator
	 * @param data the galaxy data file
	 * @param exec the executor for parallel processing
	 * @param wip the wip counter
	 */
	public void processGalaxy(final ResourceLocator rl, 
			final String data, ExecutorService exec, final WipPort wip) {
		wip.inc();
		try {
			XElement galaxy = rl.getXML(data);
			XElement background = galaxy.childElement("background");
			map = rl.getImage(background.get("image"));
			minScale = Float.parseFloat(background.get("min-scale"));
			maxScale = Float.parseFloat(background.get("max-scale"));
			
			XElement planets = galaxy.childElement("planets");
			for (final XElement planet : planets.childrenWithName("planet")) {
				wip.inc();
				exec.submit(U.checked(new Runnable() {
					@Override
					public void run() {
						try {
							PlanetType planetType = new PlanetType();
							planetType.type = planet.get("type");
							planetType.label = planet.get("label");
							
							XElement bodyElement = planet.childElement("body");
							planetType.body = rl.getAnimation(bodyElement.content, -1, 64);
							XElement tileset = planet.childElement("tileset");
							String tilePattern = tileset.get("pattern");
							
							XElement xequipment = planet.childElement("equipment");
							planetType.equipment = rl.getImage(xequipment.content);
							
							XElement xspacewar = planet.childElement("spacewar");
							planetType.spacewar = rl.getImage(xspacewar.content);
							
							for (XElement te : tileset.children()) {
								if (te.name.equals("tile-range")) {
									int start = Integer.parseInt(te.get("start"));
									int end = Integer.parseInt(te.get("end"));
									String ws = te.get("width", null);
									int width = ws != null && !ws.isEmpty() ? Integer.parseInt(ws) : 1;
									String hs = te.get("height", null);
									int height = hs != null && !hs.isEmpty() ? Integer.parseInt(hs) : 1;
									for (int id = start; id <= end; id++) {
										Tile tile = newTile(width, height, rl.getImage(String.format(tilePattern, id)), null);
										planetType.tiles.put(id, tile);
									}
								} else
								if (te.name.equals("tile")) {
									int id = Integer.parseInt(te.get("id"));
									String ws = te.get("width", null);
									int width = ws != null && !ws.isEmpty() ? Integer.parseInt(ws) : 1;
									String hs = te.get("height", null);
									int height = hs != null && !hs.isEmpty() ? Integer.parseInt(hs) : 1;
									Tile tile = newTile(width, height, rl.getImage(String.format(tilePattern, id)), null);
									planetType.tiles.put(id, tile);
								}
							}
							
							XElement map = planet.childElement("map");
							planetType.pattern = map.get("pattern");
							planetType.start = Integer.parseInt(map.get("start"));
							planetType.end = Integer.parseInt(map.get("end"));
							
							XElement xweather = planet.childElement("weather");
							if (xweather != null) {
								planetType.weatherFrequency = xweather.getInt("frequency");
								planetType.weatherDuration = xweather.getInt("duration");
								String dropType = xweather.get("drop-type", null);
								planetType.weatherDrop = WeatherType.valueOf(dropType);
							}
							
							synchronized (planetTypes) {
								planetTypes.put(planetType.type, planetType);
							}
						} finally {
							wip.dec();
						}
					}
				}));
			}
			XElement xgr = galaxy.childElement("population-growths");
			for (XElement xg : xgr.childrenWithName("growth")) {
				Pair<String, String> key = Pair.of(xg.get("type"), xg.get("race"));
				populationGrowth.put(key, xg.getDouble("value"));
			}
		} finally {
			wip.dec();
		}
	}
	/**
	 * Construct a new tile with the given parameters.
	 * @param width the width
	 * @param height the height
	 * @param image the base image
	 * @param lightMap the optional light map
	 * @return the tile
	 */
	protected Tile newTile(int width, int height, BufferedImage image,
			BufferedImage lightMap) {
		if (config.tileCacheSize > 0 && config.tileCacheBaseLimit != 0) {
			int limit = Math.abs(config.tileCacheBaseLimit);
			if (config.tileCacheBaseLimit > 0) {
				if (width >= limit && height >= limit) {
					return new TileCached(width, height, image, lightMap, config.tileCacheSize);
				}
			} else {
				if (width <= limit && height <= limit) {
					return new TileCached(width, height, image, lightMap, config.tileCacheSize);
				}
			}
		}
		return new Tile(width, height, image, lightMap);
	}
	/**
	 * Returns the population growth modifier for the given surface type and race type.
	 * @param surfaceType the surface type from galaxy.xml
	 * @param raceType the race type from players.xml
	 * @return the growth
	 */
	public double getGrowth(String surfaceType, String raceType) {
		Pair<String, String> key = Pair.of(surfaceType, raceType);
		Double g = populationGrowth.get(key);
		if (g != null) {
			return g;
		}
		return 1.0;
	}
}
