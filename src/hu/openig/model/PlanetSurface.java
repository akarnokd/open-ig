/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

import hu.openig.core.Func0;
import hu.openig.core.Location;
import hu.openig.core.RoadType;
import hu.openig.core.Sides;
import hu.openig.utils.Exceptions;
import hu.openig.utils.XElement;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The container class for the planetary surface objects,

 * including the base surface map,
 * Buildings and roads.
 * @author akarnokd
 */
public class PlanetSurface {
    /** The surface variant. */
    public int variant;
    /**

     * The map's width in cells.

     * The width is defined as a slightly descending horizontal dimension of the map,

     * but in coordinate terms it is equal to the sequence (0, 0) (1,-1) (2,-2) etc.
     * Note that the rendering coordinate system is different from the original IG's map definition.

     */
    public int width;
    /** The height with in cells. The width is defined as a vertical dimension of the map,
     *  but in coordinate terms it is equal to the sequence (0, 0) (-1,-1) (-2,-2) etc. */
    public int height;
    /**
     * The base map of the surface. Kept separate from the building maps for the case the user
     * demolishes a building.
     */
    public Map<Location, SurfaceEntity> basemap = new HashMap<>();
    /**
     * The buildings and roads map.
     */
    public final Map<Location, SurfaceEntity> buildingmap = new HashMap<>();
    /** The pre-computed locations where the angular rendering should start. */
    public final List<Location> renderingOrigins = new ArrayList<>();
    /** The pre-computed locations where the angular rendering should end. */
    public final List<Integer> renderingLength = new ArrayList<>();
    /** The base X offset to shift the (0,0) cell horizontally. */
    public int baseXOffset;
    /** The base Y offset to shift the (0,0) vertically. */
    public int baseYOffset;
    /** The bounding rectangle that fits all cells of this map. */
    public Rectangle boundingRectangle;
    /** The list of building instances. */
    public final Buildings buildings = new Buildings();
    /** The list of base surface features which have multi-tile geometry. */
    public List<SurfaceFeature> features = new ArrayList<>();
    /** Set of locations that should be excluded from groundwar deployments. */
    public Set<Location> deploymentExclusions = new HashSet<>();
    /** Locations that have been paved and thus now buildable/passable. */
    public Set<Location> pavements = new HashSet<>();
    /**
     * If true, the features list will contain all tiles,
     * if false, only the non 1x1 tiles will be added.
     */
    public static boolean isEditorMode;
    /** The placement helper. */
    public PlacementHelper placement = new PlacementHelper() {
        @Override
        protected Map<Location, SurfaceEntity> basemap() {
            return basemap;
        }
        @Override
        protected Map<Location, SurfaceEntity> buildingmap() {
            return buildingmap;
        }
        @Override
        protected boolean cellInMap(int x, int y) {
            return PlanetSurface.this.cellInMap(x, y);
        }
        @Override
        protected boolean hasPavement(Location loc) {
            return pavements.contains(loc);
        }
        @Override
        protected int height() {
            return height;
        }
        @Override
        protected int width() {
            return width;
        }

        @Override
        protected Buildings buildings() {
            return buildings;
        }
    };
    /** Compute the rendering start-stop locations. */
    public void computeRenderingLocations() {
        renderingOrigins.clear();
        renderingLength.clear();
        // y -> x
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < height; i++) {
            map.put(-i, -i);
        }
        for (int j = 1; j < width; j++) {
            map.put(- (height - 1) - j, -(height - 1) + j);
        }
        for (int i = 0; i < width; i++) {
            int x = i;
            int y = -i;
            renderingOrigins.add(Location.of(i, -i));
            renderingLength.add(x - map.get(y) + 1);
        }
        for (int j = 1; j < height; j++) {
            int x = (width - 1) - j;
            int y = - (width - 1) - j;
            renderingOrigins.add(Location.of(x, y));
            renderingLength.add(x - map.get(y) + 1);
        }
        baseYOffset = 0;
        baseXOffset = -Tile.toScreenX(-height, -height);
        int x0 = 0;
        int y0 = 0;
        int x1 = Tile.toScreenX(width - 1, -width + 1) + baseXOffset;
        Location loc = renderingOrigins.get(renderingOrigins.size() - 1);

        int y1 = Tile.toScreenY(loc.x, loc.y);
        boundingRectangle = new Rectangle(x0, y0, x1 - x0 + 57, y1 - y0 + 28);

    }
    /**
     * Check if the given cell coordinates fall into the bounds of the surface map's visible (and rendered) zone.
     * @param x the X coordinate
     * @param y the Y coordinate
     * @return true if the cell is within the map bounds
     */
    public boolean cellInMap(int x, int y) {
        if (y > 0 || y < -(width + height - 2)) {
            return false;
        }
        return !(x > renderingOrigins.get(-y).x || x < renderingOrigins.get(-y).x - renderingLength.get(-y) + 1);
    }
    /**
     * Place a building tile onto the current surface map.
     * Does not check for overlapping.
     * @param tile the tile
     * @param x the tile's leftmost coordinate
     * @param y the tile's topmost coordinate
     * @param building the building object to assign
     */
    public void placeBuilding(Tile tile, int x, int y, Building building) {
        for (int a = x; a < x + tile.width; a++) {
            for (int b = y; b > y - tile.height; b--) {
                SurfaceEntity se = new SurfaceEntity();
                se.type = SurfaceEntityType.BUILDING;
                se.virtualRow = y - b;
                se.virtualColumn = a - x;
                se.tile = tile;
                se.building = building;
                buildingmap.put(Location.of(a, b), se);
            }
        }
        buildings.add(building);
    }
    /**
     * Place a tile onto the current surface map.
     * Does not check for overlapping.
     * @param tile the tile
     * @param x the tile's leftmost coordinate
     * @param y the tile's topmost coordinate
     * @param id the base tile id
     * @param surface the surface type
     */
    public void placeBase(Tile tile, int x, int y, int id, String surface) {
        for (int a = x; a < x + tile.width; a++) {
            for (int b = y; b > y - tile.height; b--) {
                SurfaceEntity se = new SurfaceEntity();
                se.type = SurfaceEntityType.BASE;
                se.virtualRow = y - b;
                se.virtualColumn = a - x;
                se.tile = tile;
                basemap.put(Location.of(a, b), se);
            }
        }
        if (isEditorMode || (tile.width > 1 || tile.height > 1)) {
            SurfaceFeature sf = new SurfaceFeature();
            sf.id = id;
            sf.type = surface;
            sf.tile = tile;
            sf.location = Location.of(x, y);
            features.add(sf);
        }
    }
    /**
     * Set the size of the surface map.
     * @param width the width (direction +1, -1)
     * @param height the height (direction -1, -1)
     */
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        computeRenderingLocations();
    }
    /**
     * Parse a map definition XML.
     * @param map the map
     * @param gm the galaxy model, null if no surface needs to be loaded
     * @param bm the building model, null if no building needs to be loaded
     * @param newId the function that generates new ids
     */
    public void parseMap(XElement map,

            GalaxyModel gm,

            BuildingModel bm, Func0<Integer> newId) {
        if (gm != null) {
            XElement surface = map.childElement("surface");
            if (surface != null) {
                this.features.clear();
                this.basemap.clear();
                this.deploymentExclusions.clear();
                int width = Integer.parseInt(surface.get("width"));
                int height = Integer.parseInt(surface.get("height"));
                setSize(width, height);
                for (XElement tile : surface.childrenWithName("tile")) {
                    String type = tile.get("type");
                    int id = Integer.parseInt(tile.get("id"));
                    int x = Integer.parseInt(tile.get("x"));
                    int y = Integer.parseInt(tile.get("y"));
                    PlanetType pt = gm.planetTypes.get(type);
                    if (pt == null) {
                        Exceptions.add(new AssertionError("Missing planet type: " + type));
                    } else {
                        Tile t = pt.tiles.get(id);
                        if (t == null) {
                            Exceptions.add(new AssertionError("Missing tile: " + id + " on planet type " + type));
                        } else {
                            placeBase(t, x, y, id, type);
                        }
                    }
                }
                for (XElement exclude : surface.childrenWithName("exclude")) {
                    int x = Integer.parseInt(exclude.get("x"));
                    int y = Integer.parseInt(exclude.get("y"));
                    deploymentExclusions.add(Location.of(x, y));
                }
            }
        }
        if (bm != null) {
            XElement buildings = map.childElement("buildings");
            if (buildings != null) {
                setBuildings(bm, buildings, newId);
            }
        }
    }
    /**
     * Set the surface buildings from the given XElement.
     * @param bm the building model
     * @param buildings the buildings XElement
     * @param newId the function that generates new ids
     */
    void setBuildings(BuildingModel bm, XElement buildings, Func0<Integer> newId) {
        this.buildings.clear();
        this.buildingmap.clear();

        for (XElement tile : buildings.childrenWithName("building")) {
            int id = tile.getInt("id", -1);
            if (id < 0) {
                id = newId.invoke();
            }
            String type = tile.get("type");
            String race = tile.get("race");

            BuildingType bt = bm.buildings.get(type);

            Building b = new Building(id, bt, race);
            int x = tile.getInt("x");
            int y = tile.getInt("y");

            b.location = Location.of(x, y);

            String bp = tile.get("build", null);
            if (bp == null || bp.isEmpty()) {
                b.buildProgress = b.type.hitpoints;
            } else {
                b.buildProgress = Math.min(Integer.parseInt(bp), b.type.hitpoints);
            }
            String hp = tile.get("hp", null);
            if (hp == null || hp.isEmpty()) {
                b.hitpoints = b.type.hitpoints;
            } else {
                b.hitpoints = Math.min(Integer.parseInt(hp), b.type.hitpoints);
            }
            b.setLevel(Math.min(Integer.parseInt(tile.get("level")), b.type.upgrades.size())
            );
            b.assignedEnergy = Integer.parseInt(tile.get("energy"));
            b.assignedWorker = Integer.parseInt(tile.get("worker"));
            b.enabled = "true".equals(tile.get("enabled"));
            b.repairing = "true".equals(tile.get("repairing"));

            if (b.tileset != null) {
                placeBuilding(b.tileset.normal, x, y, b);
            } else {
                System.out.println("Warning: No tileset found for building " + type + " of race " + race);
            }
        }
        placeRoads(getTechnology(), bm);
    }
    /**
     * Store the map elements under the given XElement.
     * @param map the map to store the surface and/or buildings
     * @param withSurface store the surface
     * @param withBuildings store the buildings?
     */
    public void storeMap(XElement map, boolean withSurface, boolean withBuildings) {
        if (withSurface) {
            XElement surfaces = map.add("surface");
            surfaces.set("width", width);
            surfaces.set("height", height);
            for (SurfaceFeature sf : features) {
                XElement tile = surfaces.add("tile");
                tile.set("type", sf.type);
                tile.set("id", sf.id);
                tile.set("x", sf.location.x);
                tile.set("y", sf.location.y);
            }
        }
        if (withBuildings) {
            XElement xbuildings = map.add("buildings");
            for (Building b : buildings.iterable()) {
                XElement xb = xbuildings.add("building");
                xb.set("id", b.id);
                xb.set("type", b.type.id);
                xb.set("race", b.race);
                xb.set("x", b.location.x);
                xb.set("y", b.location.y);
                xb.set("build", b.buildProgress);
                xb.set("hp", b.hitpoints);
                xb.set("level", b.upgradeLevel);
                xb.set("worker", b.assignedWorker);
                xb.set("energy", b.assignedEnergy);
                xb.set("enabled", b.enabled);
                xb.set("repairing", b.repairing);
            }
        }
    }
    /**
     * @return the technology id of the last building placed on the planet, null if no buildings present
     */
    public String getTechnology() {
        if (!buildings.isEmpty()) {
            return buildings.iterable().iterator().next().race;
        }
        return null;
    }
    /**
     * Create a deep copy of the surface by sharing the basemap but
     * copying the buildings.
     * @param newId the function that generates new ids.
     * @return the planet surface copy
     */
    public PlanetSurface copy(Func0<Integer> newId) {
        PlanetSurface result = new PlanetSurface();

        result.setSize(width, height);
        result.variant = variant;

        for (Building b : buildings.iterable()) {
            Building bc = b.copy(newId.invoke());
            result.placeBuilding(bc.tileset.normal, bc.location.x, bc.location.y, bc);
        }
        // share basemap
        result.basemap = basemap;
        result.features = features;

        for (Map.Entry<Location, SurfaceEntity> se : buildingmap.entrySet()) {
            if (se.getValue().type == SurfaceEntityType.ROAD) {
                result.buildingmap.put(se.getKey(), se.getValue());
            }
        }
        result.deploymentExclusions = deploymentExclusions;
        return result;
    }
    /**
     * Place roads around buildings for the given race.
     * @param raceId the race who builds the roads
     * @param bm the building model for the roads
     */
    public void placeRoads(String raceId, BuildingModel bm) {
        Map<RoadType, Tile> rts = bm.roadTiles.get(raceId);
        Map<Tile, RoadType> trs = bm.tileRoads.get(raceId);
        // remove all roads
        Iterator<SurfaceEntity> it = buildingmap.values().iterator();
        while (it.hasNext()) {
            SurfaceEntity se = it.next();
            if (se.type == SurfaceEntityType.ROAD) {
                it.remove();
            }
        }

        Set<Location> corners = new HashSet<>();
        for (Building bld : buildings.iterable()) {
            Rectangle rect = new Rectangle(bld.location.x - 1, bld.location.y + 1, bld.tileset.normal.width + 2, bld.tileset.normal.height + 2);
            addRoadAround(rts, rect, corners);
        }
        SurfaceEntity[] neighbors = new SurfaceEntity[9];
        for (Location l : corners) {
            SurfaceEntity se = buildingmap.get(l);
            if (se == null || se.type != SurfaceEntityType.ROAD) {
                continue;
            }
            setNeighbors(l.x, l.y, buildingmap, neighbors);
            int pattern = 0;

            RoadType rt1 = null;
            if (neighbors[1] != null && neighbors[1].type == SurfaceEntityType.ROAD) {
                pattern |= Sides.TOP;
                rt1 = trs.get(neighbors[1].tile);
            }
            RoadType rt3 = null;
            if (neighbors[3] != null && neighbors[3].type == SurfaceEntityType.ROAD) {
                pattern |= Sides.LEFT;
                rt3 = trs.get(neighbors[3].tile);
            }
            RoadType rt5 = null;
            if (neighbors[5] != null && neighbors[5].type == SurfaceEntityType.ROAD) {
                pattern |= Sides.RIGHT;
                rt5 = trs.get(neighbors[5].tile);
            }
            RoadType rt7 = null;
            if (neighbors[7] != null && neighbors[7].type == SurfaceEntityType.ROAD) {
                pattern |= Sides.BOTTOM;
                rt7 = trs.get(neighbors[7].tile);
            }
            RoadType rt = RoadType.get(pattern);
            // place the new tile fragment onto the map
            // oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
            se = createRoadEntity(rts.get(rt));
            buildingmap.put(l, se);
            // alter the four neighboring tiles to contain road back to this
            if (rt1 != null) {
                rt1 = RoadType.get(rt1.pattern | Sides.BOTTOM);
                buildingmap.put(l.delta(0, 1), createRoadEntity(rts.get(rt1)));
            }
            if (rt3 != null) {
                rt3 = RoadType.get(rt3.pattern | Sides.RIGHT);
                buildingmap.put(l.delta(-1, 0), createRoadEntity(rts.get(rt3)));
            }
            if (rt5 != null) {
                rt5 = RoadType.get(rt5.pattern | Sides.LEFT);
                buildingmap.put(l.delta(1, 0), createRoadEntity(rts.get(rt5)));
            }
            if (rt7 != null) {
                rt7 = RoadType.get(rt7.pattern | Sides.TOP);
                buildingmap.put(l.delta(0, -1), createRoadEntity(rts.get(rt7)));
            }

        }
    }
    /**
     * Create a road entity for the tile.
     * @param tile the tile
     * @return the entity
     */
    SurfaceEntity createRoadEntity(Tile tile) {
        SurfaceEntity result = new SurfaceEntity();
        result.tile = tile;
        result.type = SurfaceEntityType.ROAD;
        return result;
    }
    /**
     * Fills the fragment array of the 3x3 rectangle centered around x and y.
     * @param x the x coordinate
     * @param y the y coordinate
     * @param map the map
     * @param fragments the fragments
     */
    void setNeighbors(int x, int y, Map<Location, SurfaceEntity> map, SurfaceEntity[] fragments) {
        fragments[0] = map.get(Location.of(x - 1, y + 1));
        fragments[1] = map.get(Location.of(x, y + 1));
        fragments[2] = map.get(Location.of(x + 1, y + 1));

        fragments[3] = map.get(Location.of(x - 1, y));
        fragments[4] = map.get(Location.of(x, y));
        fragments[5] = map.get(Location.of(x + 1, y));

        fragments[6] = map.get(Location.of(x - 1, y - 1));
        fragments[7] = map.get(Location.of(x, y - 1));
        fragments[8] = map.get(Location.of(x + 1, y - 1));
    }
    /**
     * Places a road frame around the tilesToHighlight rectangle.
     * @param rts the road to tile map for a concrete race
     * @param rect the rectangle to use
     * @param corners where to place the created corners
     */
    void addRoadAround(Map<RoadType, Tile> rts, Rectangle rect, Collection<Location> corners) {
        Location la = Location.of(rect.x, rect.y);
        Location lb = Location.of(rect.x + rect.width - 1, rect.y);
        Location lc = Location.of(rect.x, rect.y - rect.height + 1);
        Location ld = Location.of(rect.x + rect.width - 1, rect.y - rect.height + 1);

        corners.add(la);
        corners.add(lb);
        corners.add(lc);
        corners.add(ld);

        buildingmap.put(la, createRoadEntity(rts.get(RoadType.RIGHT_TO_BOTTOM)));
        buildingmap.put(lb, createRoadEntity(rts.get(RoadType.LEFT_TO_BOTTOM)));
        buildingmap.put(lc, createRoadEntity(rts.get(RoadType.TOP_TO_RIGHT)));
        buildingmap.put(ld, createRoadEntity(rts.get(RoadType.TOP_TO_LEFT)));
        // add linear segments

        Tile ht = rts.get(RoadType.HORIZONTAL);
        for (int i = rect.x + 1; i < rect.x + rect.width - 1; i++) {
            buildingmap.put(Location.of(i, rect.y), createRoadEntity(ht));
            buildingmap.put(Location.of(i, rect.y - rect.height + 1), createRoadEntity(ht));
        }
        Tile vt = rts.get(RoadType.VERTICAL);
        for (int i = rect.y - 1; i > rect.y - rect.height + 1; i--) {
            buildingmap.put(Location.of(rect.x, i), createRoadEntity(vt));
            buildingmap.put(Location.of(rect.x + rect.width - 1, i), createRoadEntity(vt));
        }
    }
    /**
     * Removes the given building from the map.
     * @param building the building to remove
     * @return true if the building was successfully removed
     */
    public boolean removeBuilding(Building building) {
        building.hitpoints = 0;
        if (buildings.remove(building)) {
            for (int a = building.location.x; a < building.location.x + building.tileset.normal.width; a++) {
                for (int b = building.location.y; b > building.location.y - building.tileset.normal.height; b--) {
                    buildingmap.remove(Location.of(a, b));
                }
            }
            return true;
        }
        return false;
    }
    /**
     * The placement helper class to determine if a specific region size is available on the surface.
     * @author akarnokd, 2011.12.27.
     */
    public abstract static class PlacementHelper {
        /** @return the map's horizontal width. */
        protected abstract int width();
        /** @return the map's vertical height. */
        protected abstract int height();
        /**
         * Test if the given coordinate is within the map.
         * @param x the X coordinate
         * @param y the Y coordinate
         * @return true if within the map
         */
        protected abstract boolean cellInMap(int x, int y);
        /** @return the building map */
        protected abstract Map<Location, SurfaceEntity> buildingmap();
        /** @return the base map. */
        protected abstract Map<Location, SurfaceEntity> basemap();
        /** @return the existing buildings. */
        protected abstract Buildings buildings();
        /** @return true if the location is paved. */
        protected abstract boolean hasPavement(Location loc);
        /**
         * Test if the given rectangular region is eligible for building placement, e.g.:
         * all cells are within the map's boundary, no other buildings are present within the given bounds,
         * no multi-tile surface object is present at the location.
         * @param rect the surface rectangle
         * @return true if the building can be placed
         */
        public boolean canPlaceBuilding(Rectangle rect) {
            return canPlaceBuilding(rect.x, rect.y, rect.width, rect.height);
        }
        /**
         * Test if the given rectangular region is eligible for building placement, e.g.:
         * all cells are within the map's boundary, no other buildings are present within the given bounds,
         * no multi-tile surface object is present at the location.
         * @param x the left coordinate
         * @param y the top coordinate
         * @param width the width into +X direction
         * @param height the height into -Y direction
         * @return true if the building can be placed
         */
        public boolean canPlaceBuilding(int x, int y, int width, int height) {
            for (int i = x; i < x + width; i++) {
                for (int j = y; j > y - height; j--) {
                    if (!canPlaceBuilding(i, j)) {
                        return false;
                    }
                }
            }
            return true;
        }
        /**
         * Test if the coordinates are suitable for building placement.
         * @param x the X coordinate
         * @param y the Y coordinate
         * @return true if placement is allowed
         */
        public boolean canPlaceBuilding(int x, int y) {
            if (!cellInMap(x, y)) {
                return false;
            }
            SurfaceEntity se = buildingmap().get(Location.of(x, y));
            if (se != null && se.type == SurfaceEntityType.BUILDING) {
                return false;
            }
            Location loc = Location.of(x, y);
            if (hasPavement(loc)) {
                return true;
            }
            se = basemap().get(loc);
            return !(se != null && (se.tile.width > 1 || se.tile.height > 1));
        }
        /**
         * Find a location for the given dimensions.
         * <p>Note: the dimensions should incorporate 1+1 road on both axis.</p>
         * @param dim the dimensions
         * @return the location or null if not found
         */
        public Point findLocation(Dimension dim) {
            return findLocation(dim.width, dim.height);
        }

        /**
         * Find a location for the given dimensions, starting from a specific point on the surface.
         * <p>Note: the dimensions should incorporate 1+1 road on both axis.</p>
         * @param dim the dimensions
         * @param preferredLocation the point from which the location search starts from
         * @return the location or null if not found
         */
        public Point findLocation(Dimension dim, Location preferredLocation) {
            return findLocation(dim.width, dim.height, preferredLocation);
        }
        /**
         * Find a location on the surface which can support a building (and surrounding roads)
         * with the given size. The location search starts of from the center of the map
         * @param width should be the building tile width + 2
         * @param height should be the building tile height + 2
         * @return the top-left point where this building could be built, null indicates that
         * no suitable location is present
         */
        public Point findLocation(int width, int height) {
            int cx = this.width() / 2 - this.height() / 2 - width / 2;
            int cy = -this.width() / 2 - this.height() / 2 + height / 2;
            return findLocation(width, height, Location.of(cx, cy));
        }

        /**
         * Find a location on the surface which can support a building (and surrounding roads)
         * with the given size. The location search starts from the specified point on the surface.
         * @param width should be the building tile width + 2
         * @param height should be the building tile height + 2
         * @param preferredLocation the point from which the location search starts from
         * @return the top-left point where this building could be built, null indicates that
         * no suitable location is present
         */
        public Point findLocation(int width, int height, Location preferredLocation) {
            int startX = preferredLocation.x;
            int startY = preferredLocation.y;

            int rx1 = Math.abs(this.width() - startX);
            int rx2 = Math.abs(-this.height() - startX);
            int ry1 = Math.abs(this.width() + this.height() + startY);
            int ry2 = Math.abs(startY);
            int maxr = Math.max(Math.max(rx1, rx2), Math.max(ry1, ry2));
            // the square size
            List<PlaceCandidate> candidates = new ArrayList<>();
            for (int i = 0; i < maxr; i++) {
                int len = i * 2 + 1;
                int size = len > 1 ? len * 2 + (len - 2) * 2 : 1;
                int[] xs = new int[size];
                int[] ys = new int[size];
                clockwise(xs, ys, len);
                for (int k = 0; k < size; k++) {
                    int x0 = startX + xs[k] - i;
                    int y0 = startY - ys[k] + i;
                    if (canPlaceBuilding(x0, y0, width, height)) {
                        int d = (startX - x0) * (startX - x0) + (startY - y0) * (startY - y0);
                        PlaceCandidate pc = createCandidate(x0, y0, width, height, d);
                        if (pc != null) {
                            candidates.add(pc);
                        }
                    }
                }
                // if only check for placement is running
                if (buildings().isEmpty() && !candidates.isEmpty()) {
                    PlaceCandidate pc = Collections.max(candidates);
                    return new Point(pc.x, pc.y);
                }
            }
            if (candidates.size() > 0) {
                PlaceCandidate pc = Collections.max(candidates);
                return new Point(pc.x, pc.y);
            }

            return null;
        }
        /**
         * Create a candidate place for the location.
         * @param x0 coordinates
         * @param y0 coordinates
         * @param width placement width
         * @param height placement height
         * @param d distance to center
         * @return the candidate or null if not near a road
         */
        PlaceCandidate createCandidate(int x0, int y0, int width, int height, int d) {
            // no buildings at all
            if (buildingmap().isEmpty()) {
                return new PlaceCandidate(x0, y0, 1, 0, d);
            }
            int roads = 1;
            int edges = 0;
            for (int k = x0; k < x0 + width; k++) {
                for (int j = y0; j > y0 - height; j--) {
                    if (isRoad(k, j)) {
                        roads++;
                        if (isCrossRoad(k, j)) {
                            edges++;
                        }
                    }
                }
            }
            return new PlaceCandidate(x0, y0, roads, edges, d);
        }
        /**
         * Fill in a clockwise coordinate pair of a given length rectangle.
         * @param xs the xs
         * @param ys the ys
         * @param len the rectangle length
         */
        void clockwise(int[] xs, int[] ys, int len) {
            int j = 0;
            for (int i = 0; i < len; i++) {
                xs[j] = i;
                ys[j] = 0;
                j++;
            }
            for (int i = 1; i < len; i++) {
                xs[j] = len - 1;
                ys[j] = i;
                j++;
            }
            for (int i = len - 2; i >= 0; i--) {
                xs[j] = i;
                ys[j] = len - 1;
                j++;
            }
            for (int i = len - 2; i >= 1; i--) {
                xs[j] = 0;
                ys[j] = i;
                j++;
            }
        }
        /**
         * Check if the cell is the edge of a building road.
         * @param x the center x
         * @param y the center y
         * @return true if edge road
         */
        boolean isCrossRoad(int x, int y) {
            if (isEdge(x - 1, y - 1) && isRoad(x, y - 1) && isRoad(x - 1, y)) {
                return true;
            }
            if (isEdge(x + 1, y - 1) && isRoad(x, y - 1) && isRoad(x + 1, y)) {
                return true;
            }
            if (isEdge(x - 1, y + 1) && isRoad(x - 1, y) && isRoad(x, y + 1)) {
                return true;
            }
            return isEdge(x + 1, y + 1) && isRoad(x, y + 1) && isRoad(x + 1, y);
        }
        /**
         * Check if the given location is a road.
         * @param x the X coordinate
         * @param y the Y coordinate
         * @return true if road
         */
        boolean isRoad(int x, int y) {
            SurfaceEntity e = buildingmap().get(Location.of(x, y));
            return e != null && e.type == SurfaceEntityType.ROAD;
        }
        /**
         * Check if the given location is a building edge.
         * @param x the X coordinate
         * @param y the Y coordinate
         * @return true if road
         */
        boolean isEdge(int x, int y) {
            SurfaceEntity e = buildingmap().get(Location.of(x, y));
            if (e != null && e.building != null) {
                if (e.virtualColumn == 0 && e.virtualRow == 0) {
                    return true;
                }
                if (e.virtualColumn == 0 && e.virtualRow == e.building.tileset.normal.height - 1) {
                    return true;
                }
                if (e.virtualColumn == e.building.tileset.normal.width - 1 && e.virtualRow == e.building.tileset.normal.height - 1) {
                    return true;
                }
                if (e.virtualColumn == e.building.tileset.normal.width - 1 && e.virtualRow == 0) {
                    return true;
                }
            }
            return false;
        }
        /**
         * A building place candidate.
         * @author akarnokd, 2011.12.30.
         */
        public static class PlaceCandidate implements Comparable<PlaceCandidate> {
            /** Location X. */
            public final int x;
            /** Location Y. */
            public final int y;
            /** Number of contacting building cells. */
            public final int contact;
            /** Number of connected buildings. */
            public final int edges;
            /** Distance to center. */
            public final int distance;
            /**
             * Constructor. Initializes the fields.
             * @param x location X
             * @param y location Y
             * @param contact Number of contacting building cells
             * @param edges number of edges
             * @param distance distance to center
             */
            public PlaceCandidate(int x, int y, int contact, int edges, int distance) {
                this.x = x;
                this.y = y;
                this.contact = contact;
                this.edges = edges;
                this.distance = distance;
            }
            @Override
            public int compareTo(PlaceCandidate o) {
                double v1 = (contact + edges * 4.0) / (distance + 1);
                double v2 = (o.contact + o.edges * 4.0) / (o.distance + 1);
                return v1 < v2 ? -1 : (v1 > v2 ? 1 : 0);
            }
        }
    }
    /**
     * Return a building instance at the specified location.
     * @param loc the location
     * @return the building object or null
     */
    public Building getBuildingAt(Location loc) {
        SurfaceEntity se = buildingmap.get(loc);
        if (se != null && se.type == SurfaceEntityType.BUILDING) {
            return se.building;
        }
        return null;
    }
    /**
     * Compute the bounding rectangle of the rendered building object.
     * @param loc the location to look for a building.
     * @return the bounding rectangle or null if the target does not contain a building
     */
    public Rectangle getBoundingRect(Location loc) {
        SurfaceEntity se = buildingmap.get(loc);
        if (se != null && se.type == SurfaceEntityType.BUILDING) {
            int a0 = loc.x - se.virtualColumn;
            int b0 = loc.y + se.virtualRow;

            int x = baseXOffset + Tile.toScreenX(a0, b0);
            int y = baseYOffset + Tile.toScreenY(a0, b0 - se.tile.height + 1) + 27;

            return new Rectangle(x, y - se.tile.imageHeight, se.tile.imageWidth, se.tile.imageHeight);
        }
        return null;
    }
    /**
     * Returns the bounding rectangle of the building in non-scaled screen coordinates.
     * @param b the building to test
     * @return the bounding rectangle
     */
    public Rectangle buildingRectangle(Building b) {
        Rectangle r = b.rectangle();
        r.x += baseXOffset;
        r.y += baseYOffset;
        return r;
    }
    /**
     * Returns the bounding rectangle of the given surface feature in non-scaled screen coordinates.
     * @param f the surface feature
     * @return the bounding rectangle
     */
    public Rectangle featureRectangle(SurfaceFeature f) {
        int a0 = f.location.x;
        int b0 = f.location.y;
        int x = baseXOffset + Tile.toScreenX(a0, b0);
        int y = baseYOffset + Tile.toScreenY(a0, b0 - f.tile.height + 1) + 27;
        return new Rectangle(x, y - f.tile.imageHeight, f.tile.imageWidth, f.tile.imageHeight);
    }
    /**
     * Returns the screen coordinates of the center of the given location (without offset).
     * @param loc the surface coordinates
     * @return the screen coordinates
     */
    public Point center(Location loc) {
        return new Point(Tile.toScreenX(loc.x, loc.y) + 28,

                Tile.toScreenY(loc.x, loc.y) + 14);
    }
    /**
     * Returns the screen coordinates of the center of the given location (without offset).
     * @param x the surface X coordinate
     * @param y the surface Y coordinate
     * @return the screen coordinates
     */
    public Point center(double x, double y) {
        return new Point((int)(Tile.toScreenX(x, y) + 28),

                (int)(Tile.toScreenY(x, y) + 14));
    }
    /**
     * Returns the screen coordinates of the center of the given location (with offset).
     * @param loc the surface coordinates
     * @return the screen coordinates
     */
    public Point centerOffset(Location loc) {
        return new Point(baseXOffset + Tile.toScreenX(loc.x, loc.y) + 28,

                baseYOffset + Tile.toScreenY(loc.x, loc.y) + 14);

    }
    /**
     * Returns the screen coordinates of the center of the given location (with offset).
     * @param x the surface X coordinate
     * @param y the surface Y coordinate
     * @return the screen coordinates
     */
    public Point centerOffset(double x, double y) {
        return new Point((int)(baseXOffset + Tile.toScreenX(x, y) + 28),

                (int)(baseYOffset + Tile.toScreenY(x, y) + 14));
    }
}
