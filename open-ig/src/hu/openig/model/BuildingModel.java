/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

import hu.openig.core.ResourceLocator;
import hu.openig.core.ResourceLocator.ResourcePlace;
import hu.openig.core.ResourceType;
import hu.openig.core.RoadType;
import hu.openig.core.Tile;
import hu.openig.utils.ImageUtils;
import hu.openig.utils.JavaUtils;
import hu.openig.utils.WipPort;
import hu.openig.utils.XElement;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * The building models.
 * @author akarnokd
 */
public class BuildingModel {
	/** The list of all building types. Maps from building ID to building type definition. */
	public final Map<String, BuildingType> buildings = new LinkedHashMap<String, BuildingType>();
	/** The road tile map from tech id to road type to tile. */
	public final Map<String, Map<RoadType, Tile>> roadTiles = new HashMap<String, Map<RoadType, Tile>>();
	/** The road tile to road type reverse lookup table. */
	public final Map<String, Map<Tile, RoadType>> tileRoads = new HashMap<String, Map<Tile, RoadType>>();
	/**
	 * Process the contents of the buildings definition.
	 * @param data the buildings definition
	 * @param rl the resource locator
	 * @param language the language
	 * @param exec the executor service for the parallel processing
	 * @param wip the wip counter
	 */
	public void processBuildings(final ResourceLocator rl, 
			final String language, String data, 
			final ExecutorService exec, final WipPort wip) {
		wip.inc();
		try {
			BufferedImage solidTile = rl.getImage(language, "colony/tile_1x1");
			
			BuildingMinimapTiles bmt = new BuildingMinimapTiles();
			bmt.normal = new Tile(1, 1, ImageUtils.recolor(solidTile, 0xFF00C000), null);
			bmt.damaged = new Tile(1, 1, ImageUtils.recolor(solidTile, 0xFFFF0000), null);
			bmt.inoperable = new Tile(1, 1, ImageUtils.recolor(solidTile, 0xFFFFFF00), null);
			bmt.destroyed = new Tile(1, 1, ImageUtils.recolor(solidTile, 0xFF202020), null);
			bmt.constructing = new Tile(1, 1, ImageUtils.recolor(solidTile, 0xFF0040FF), null);
			bmt.constructingDamaged = new Tile(1, 1, ImageUtils.recolor(solidTile, 0xFFFF40FF), null);
			
			XElement buildings = rl.getXML(language, data);
			
			XElement scaff = buildings.childElement("scaffolding");
			XElement scaffGraph = scaff.childElement("graphics");
			String scaffBase = scaffGraph.get("base");
			Map<String, Scaffolding> scaffoldings = new HashMap<String, Scaffolding>();
			for (XElement scaffTech : scaffGraph.childrenWithName("tech")) {
				String id = scaffTech.get("id");
				final Scaffolding scaffolding = new Scaffolding();
				scaffoldings.put(id, scaffolding);
	
				XElement norm = scaffTech.childElement("normal");
				
				String nbase = norm.get("base");
				int nstart = Integer.parseInt(norm.get("from"));
				int nend = Integer.parseInt(norm.get("to"));
				
				for (int i = nstart; i <= nend; i++) {
					final String normalImg = String.format(scaffBase, id, String.format(nbase, i));
					final String normalLight = normalImg + "_lights";
					
					wip.inc();
					exec.execute(JavaUtils.checked(new Runnable() {
						@Override
						public void run() {
							try {
								BufferedImage lightMap = null;
								ResourcePlace rp = rl.get(language, normalLight, ResourceType.IMAGE);
								if (rp != null) {
									lightMap = rl.getImage(language, normalLight);
								}
								BufferedImage image = rl.getImage(language, normalImg);
								
								synchronized (scaffolding.normal) {
									scaffolding.normal.add(new Tile(1, 1, image, lightMap));
								}
							} finally {
								wip.dec();
							}
						}
					}));
				}
				
				XElement dam = scaffTech.childElement("damaged");
				String dbase = dam.get("base");
				int dstart = Integer.parseInt(dam.get("from"));
				int dend = Integer.parseInt(dam.get("to"));
				
				for (int i = dstart; i <= dend; i++) {
					final String normalImg = String.format(scaffBase, id, String.format(dbase, i));
					final String normalLight = normalImg + "_lights";
					
					wip.inc();
					exec.execute(JavaUtils.checked(new Runnable() {
						@Override
						public void run() {
							try {
								BufferedImage lightMap = null;
								ResourcePlace rp = rl.get(language, normalLight, ResourceType.IMAGE);
								if (rp != null) {
									lightMap = rl.getImage(language, normalLight);
								}
								BufferedImage image = rl.getImage(language, normalImg);
								
								synchronized (scaffolding.damaged) {
									scaffolding.damaged.add(new Tile(1, 1, image, lightMap));
								}
							} finally {
								wip.dec();
							}
						}
					}));
				}
				
			}
			
			for (XElement building : buildings.childrenWithName("building")) {
				final BuildingType b = new BuildingType();
				b.scaffoldings = scaffoldings;
				b.id = building.get("id");
				b.label = building.get("label");
				b.description = b.label + ".desc";
				b.minimapTiles = bmt;
				
				XElement gfx = building.childElement("graphics");
				String pattern = gfx.get("base");
				for (XElement r : gfx.childrenWithName("tech")) {
					final TileSet ts = new TileSet();
					
					final String rid = r.get("id");
					final int width = Integer.parseInt(r.get("width"));
					final int height = Integer.parseInt(r.get("height"));
					
					final String normalImg = String.format(pattern, rid);
					final String normalLight = normalImg + "_lights";
					final String damagedImg = normalImg + "_damaged";
					final String previewImg = normalImg + "_mini";
					
					wip.inc();
					exec.execute(JavaUtils.checked(new Runnable() {
						@Override
						public void run() {
							try {
								BufferedImage lightMap = null;
								ResourcePlace rp = rl.get(language, normalLight, ResourceType.IMAGE);
								if (rp != null) {
									lightMap = rl.getImage(language, normalLight);
								}
								BufferedImage image = rl.getImage(language, normalImg);
								ts.normal = new Tile(width, height, image, lightMap);
								ts.nolight = new Tile(width, height, image, null);
								ts.damaged = new Tile(width, height, rl.getImage(language, damagedImg), null); // no lightmap for damaged building
								ts.preview = rl.getImage(language, previewImg);
								synchronized (b.tileset) {
									b.tileset.put(rid, ts);
								}
							} finally {
								wip.dec();
							}
						}
					}));
				}
				XElement bld = building.childElement("build");
				b.cost = Integer.parseInt(bld.get("cost"));
				b.hitpoints = b.cost; // TODO cost == hitpoints???
				b.kind = bld.get("kind");
				String limit = bld.get("limit");
				if ("*".equals(limit)) {
					b.limit = Integer.MAX_VALUE;
				} else {
					b.limit = Integer.parseInt(limit);
				}
				b.research = bld.get("research");
				String except = bld.get("except");
				if (except != null && !except.isEmpty()) {
					b.except.addAll(Arrays.asList(except.split("\\s*,\\s*")));
				}
				XElement op = building.childElement("operation");
				b.percentable = "true".equals(op.get("percent"));
				for (XElement re : op.childrenWithName("resource")) {
					Resource res = new Resource();
					res.type = re.get("type");
					res.amount = Float.parseFloat(re.content);
					b.resources.put(res.type, res);
					if ("true".equals(re.get("display"))) {
						b.primary = res.type;
					}
				}
				
				XElement ug = building.childElement("upgrades");
				for (XElement u : ug.childrenWithName("upgrade")) {
					Upgrade upg = new Upgrade();
					upg.description = u.get("desc");
					for (XElement re : u.childrenWithName("resource")) {
						Resource res = new Resource();
						res.type = re.get("type");
						res.amount = Float.parseFloat(re.content);
						upg.resources.put(res.type, res);
					}
					b.upgrades.add(upg);
				}
				
				this.buildings.put(b.id, b);
			}
			XElement roads = buildings.childElement("roads");
			XElement graph = roads.childElement("graphics");
			String roadBase = graph.get("base");
			List<String> techs = new ArrayList<String>();
			for (XElement e : graph.childrenWithName("tech")) {
				techs.add(e.get("id"));
			}
			for (XElement e : roads.childrenWithName("layout")) {
				final int index = Integer.parseInt(e.get("index"));
				String id = e.get("id");
				for (final String rid : techs) {
					final String normalImg = String.format(roadBase, rid, id);
					final String normalLight = normalImg + "_lights";
					
					wip.inc();
	
					exec.execute(JavaUtils.checked(new Runnable() {
						@Override
						public void run() {
							try {
								BufferedImage lightMap = null;
								ResourcePlace rp = rl.get(language, normalLight, ResourceType.IMAGE);
								if (rp != null) {
									lightMap = rl.getImage(language, normalLight);
								}
								Tile t = new Tile(1, 1, rl.getImage(language, normalImg), lightMap);
								RoadType rt = RoadType.getByIndex(index);
								addRoadType(rid, rt, t);
							} finally {
								wip.dec();
							}
						}
					}));
					
				}
			}
		} finally {
			wip.dec();
		}
	}
	/**
	 * Add a road type - tile entry.
	 * @param rid the race id
	 * @param rt the road type
	 * @param tile the tile entry
	 */
	synchronized void addRoadType(String rid, RoadType rt, Tile tile) {
		Map<RoadType, Tile> tiles = roadTiles.get(rid);
		if (tiles == null) {
			tiles = new HashMap<RoadType, Tile>();
			roadTiles.put(rid, tiles);
		}
		tiles.put(rt, tile);
		
		Map<Tile, RoadType> roads = tileRoads.get(rid);
		if (roads == null) {
			roads = new HashMap<Tile, RoadType>();
			tileRoads.put(rid, roads);
		}
		roads.put(tile, rt);
	}
}
