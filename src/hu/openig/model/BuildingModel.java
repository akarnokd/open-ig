/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

import hu.openig.core.ResourceType;
import hu.openig.core.RoadType;
import hu.openig.model.ResourceLocator.ResourcePlace;
import hu.openig.utils.ImageUtils;
import hu.openig.utils.U;
import hu.openig.utils.WipPort;
import hu.openig.utils.XElement;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * The building models.
 * @author akarnokd
 */
public class BuildingModel {
    /** The list of all building types. Maps from building ID to building type definition. */
    public final Map<String, BuildingType> buildings = new LinkedHashMap<>();
    /** The road tile map from tech id to road type to tile. */
    public final Map<String, Map<RoadType, Tile>> roadTiles = new HashMap<>();
    /** The road tile to road type reverse lookup table. */
    public final Map<String, Map<Tile, RoadType>> tileRoads = new HashMap<>();
    /** The configuration. */
    protected final Configuration config;
    /** Are the buildings read in skirmish-mode? */
    protected final boolean skirmishMode;
    /** Set of races to load buildings for. */
    protected Set<String> races;
    /**
     * Constructor. Set the configuration.
     * @param config the configuration
     * @param skirmishMode indicate skirmish mode
     * @param races the races to include
     */
    public BuildingModel(Configuration config, boolean skirmishMode, Set<String> races) {
        this.config = config;
        this.skirmishMode = skirmishMode;
        this.races = races;
    }
    /**
     * Process the contents of the buildings definition.
     * @param rl the resource locator
     * @param data the buildings definition
     * @param researches the map of researches
     * @param labels the labels

     * @param exec the executor service for the parallel processing
     * @param wip the wip counter
     */
    public void processBuildings(final ResourceLocator rl,

            String data,

            Map<String, ResearchType> researches,
            final Labels labels,
            final ExecutorService exec,

            final WipPort wip) {
        wip.inc();
        try {
            BufferedImage solidTile = rl.getImage("colony/tile_1x1");

            BuildingMinimapTiles bmt = new BuildingMinimapTiles();
            bmt.normal = newTile(1, 1, ImageUtils.recolor(solidTile, 0xFF00C000), null);
            bmt.damaged = newTile(1, 1, ImageUtils.recolor(solidTile, 0xFFFF0000), null);
            bmt.inoperable = newTile(1, 1, ImageUtils.recolor(solidTile, 0xFFFFFF00), null);
            bmt.destroyed = newTile(1, 1, ImageUtils.recolor(solidTile, 0xFF202020), null);
            bmt.constructing = newTile(1, 1, ImageUtils.recolor(solidTile, 0xFF0040FF), null);
            bmt.constructingDamaged = newTile(1, 1, ImageUtils.recolor(solidTile, 0xFFFF40FF), null);

            XElement buildings = rl.getXML(data);

            XElement scaff = buildings.childElement("scaffolding");
            XElement scaffGraph = scaff.childElement("graphics");
            String scaffBase = scaffGraph.get("base");
            Map<String, Scaffolding> scaffoldings = new HashMap<>();
            for (XElement scaffTech : scaffGraph.childrenWithName("tech")) {
                String id = scaffTech.get("id");
                final Scaffolding scaffolding = new Scaffolding();
                scaffoldings.put(id, scaffolding);

                XElement norm = scaffTech.childElement("normal");

                String nbase = norm.get("base");
                final int nstart = Integer.parseInt(norm.get("from"));
                final int nend = Integer.parseInt(norm.get("to"));

                for (int i = nstart; i <= nend; i++) {
                    scaffolding.normal.add(null);
                }

                for (int i = nstart; i <= nend; i++) {
                    final int fi = i;
                    final String normalImg = String.format(scaffBase, id, String.format(nbase, i));
                    final String normalLight = normalImg + "_lights";

                    wip.inc();
                    exec.execute(U.checked(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                BufferedImage lightMap = null;
                                ResourcePlace rp = rl.get(normalLight, ResourceType.IMAGE);
                                if (rp != null) {
                                    lightMap = rl.getImage(normalLight);
                                }
                                BufferedImage image = rl.getImage(normalImg);

                                Tile t = newTile(1, 1, image, lightMap);
                                synchronized (scaffolding.normal) {
                                    scaffolding.normal.set(fi - nstart, t);
                                }
                            } finally {
                                wip.dec();
                            }
                        }
                    }));
                }

                XElement dam = scaffTech.childElement("damaged");
                String dbase = dam.get("base");
                final int dstart = Integer.parseInt(dam.get("from"));
                final int dend = Integer.parseInt(dam.get("to"));
                for (int i = dstart; i <= dend; i++) {
                    scaffolding.damaged.add(null);
                }

                for (int i = dstart; i <= dend; i++) {
                    final int fi = i;
                    final String normalImg = String.format(scaffBase, id, String.format(dbase, i));
                    final String normalLight = normalImg + "_lights";

                    wip.inc();
                    exec.execute(U.checked(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                BufferedImage lightMap = null;
                                ResourcePlace rp = rl.get(normalLight, ResourceType.IMAGE);
                                if (rp != null) {
                                    lightMap = rl.getImage(normalLight);
                                }
                                BufferedImage image = rl.getImage(normalImg);

                                Tile t = newTile(1, 1, image, lightMap);
                                synchronized (scaffolding.damaged) {
                                    scaffolding.damaged.set(fi - dstart, t);
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
                XElement bld = building.childElement("build");
                String research = bld.get("research", null);
                b.research = researches.get(research);
                if (research != null && b.research == null) {
                    //throw new AssertionError("Missing research: Building = " + b.id + ", Research = " + research);
                    // no research available with the current set of races.
                    continue;
                }
                b.scaffoldings = scaffoldings;
                b.id = building.get("id");
                b.name = labels.get(building.get("name"));
                b.description = labels.get(building.get("name") + ".desc");
                b.minimapTiles = bmt;

                final XElement gfx = building.childElement("graphics");
                wip.inc();
                exec.execute(U.checked(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Map<String, TileSet> tsCache = new HashMap<>();
                            for (XElement r : gfx.childrenWithName("tech")) {
                                final String rid = r.get("id");
                                if (races != null && !races.contains(rid)) {
                                    continue;
                                }
                                boolean skirmishOnly = r.getBoolean("skirmish-only", false);
                                // load this tile only if skirmish mode is on and the building is marked as skirmish
                                if (!skirmishMode && skirmishOnly) {
                                    continue;
                                }

                                TileSet ts = null;

                                final int width = Integer.parseInt(r.get("width"));
                                final int height = Integer.parseInt(r.get("height"));

                                final String normalImg = r.get("image");
                                TileSet tsCached = tsCache.get(normalImg);
                                if (tsCached != null) {
                                    ts = tsCached;
                                } else {
                                    ts = new TileSet();
                                    tsCache.put(normalImg, ts);

                                    final String normalLight = normalImg + "_lights";
                                    final String damagedImg = normalImg + "_damaged";
                                    final String previewImg = normalImg + "_mini";
                                    BufferedImage lightMap = null;
                                    ResourcePlace rp = rl.get(normalLight, ResourceType.IMAGE);
                                    if (rp != null) {
                                        lightMap = rl.getImage(normalLight);
                                    }
                                    BufferedImage image = rl.getImage(normalImg);

                                    ts.normal = newTile(width, height, image, lightMap);
                                    ts.nolight = newTile(width, height, image, null);

                                    BufferedImage dmgImage = rl.getImage(damagedImg);
                                    ts.damaged = newTile(width, height, dmgImage, null); // no lightmap for damaged building

                                    ts.preview = rl.getImage(previewImg);

                                }
                                synchronized (b.tileset) {
                                    b.tileset.put(rid, ts);
                                }

                                String except = r.get("except", null);
                                if (except != null && !except.isEmpty()) {
                                    synchronized (b.raceExcept) {
                                        b.raceExcept.put(rid, new HashSet<String>(Arrays.asList(except.split("\\s*,\\s*"))));
                                    }
                                }
                            }
                        } finally {
                            wip.dec();
                        }
                    }
                }));
                b.cost = Integer.parseInt(bld.get("cost"));
                b.hitpoints = b.cost; // TODO cost == hitpoints???
                b.kind = bld.get("kind");
                String limit = bld.get("limit");
                if ("*".equals(limit)) {
                    b.limit = Integer.MAX_VALUE;
                } else {
                    b.limit = Integer.parseInt(limit);
                }
                b.skirmishHardLimit = bld.getBoolean("skirmish-hard-limit", false);

                String except = bld.get("except", null);
                if (except != null && !except.isEmpty()) {
                    b.except.addAll(Arrays.asList(except.split("\\s*,\\s*")));
                }
                XElement op = building.childElement("operation");
                for (XElement re : op.childrenWithName("resource")) {
                    Resource res = new Resource();
                    res.type = re.get("type");
                    res.amount = Float.parseFloat(re.content);
                    b.resources.put(res.type, res);
                    if ("true".equals(re.get("display", "false"))) {
                        b.primary = res.type;
                    }
                }

                XElement ug = building.childElement("upgrades");
                for (XElement u : ug.childrenWithName("upgrade")) {
                    Upgrade upg = new Upgrade();
                    upg.description = labels.get(u.get("desc"));
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
            List<String> techs = new ArrayList<>();
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

                    exec.execute(U.checked(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                BufferedImage lightMap = null;
                                ResourcePlace rp = rl.get(normalLight, ResourceType.IMAGE);
                                if (rp != null) {
                                    lightMap = rl.getImage(normalLight);
                                }
                                Tile t = newTile(1, 1, rl.getImage(normalImg), lightMap);
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
            tiles = new HashMap<>();
            roadTiles.put(rid, tiles);
        }
        tiles.put(rt, tile);

        Map<Tile, RoadType> roads = tileRoads.get(rid);
        if (roads == null) {
            roads = new HashMap<>();
            tileRoads.put(rid, roads);
        }
        roads.put(tile, rt);
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
        if (config.tileCacheSize > 0 && config.tileCacheBuildingLimit != 0) {
            int limit = Math.abs(config.tileCacheBuildingLimit);
            if (config.tileCacheBuildingLimit > 0) {
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
     * Find a specific kind of building.
     * @param kind the kind
     * @return the definition or null if not found
     */
    public BuildingType find(String kind) {
        for (BuildingType bt : buildings.values()) {
            if (bt.kind.equals(kind)) {
                return bt;
            }
        }
        return null;
    }
    /**
     * Returns the building definition or null if not found.
     * @param id the building type id
     * @return the definition or null if not found
     */
    public BuildingType get(String id) {
        return buildings.get(id);
    }
}
