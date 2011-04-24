/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

import hu.openig.core.Location;
import hu.openig.core.PlanetType;
import hu.openig.core.RoadType;
import hu.openig.core.Sides;
import hu.openig.core.Tile;
import hu.openig.utils.XElement;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The container class for the planetary surface objects, including the base surface map,
 * builings, roads and vehicles.
 * @author akarnokd
 */
public class PlanetSurface {
	/** 
	 * The map's width in cells. 
	 * The width is defined as a slightly descending horizontal dimension of the map, 
	 * but in coordinate terms it is equal to the sequence 0,0 1,-1, 2,-2 etc.
	 * Note that the rendering coordinate system is different from the original IG's map definition. 
	 */
	public int width;
	/** The height with in cells. The width is defined as a vertical dimension of the map,
	 *  but in coordinate terms it is equal to the sequence 0,0 -1,-1, -2,-2 etc. */
	public int height;
	/**
	 * The accessible rectangle of the surface defined in pixels. The accessible origin is encoded relative to the top-left corner of where the Location(0,0) is rendered.
	 */
	public final Rectangle accessibleRect = new Rectangle();
	/**
	 * The base map of the surface. Kept separate from the building maps for the case the user
	 * demolishes a building.
	 */
	public final Map<Location, SurfaceEntity> basemap = new HashMap<Location, SurfaceEntity>();
	/**
	 * The buildings and roads map.
	 */
	public final Map<Location, SurfaceEntity> buildingmap = new HashMap<Location, SurfaceEntity>();
	/** The pre-computed locations where the angular rendering should start. */
	public final List<Location> renderingOrigins = new ArrayList<Location>();
	/** The pre-computed locations where the angular rendering should end. */
	public final List<Integer> renderingLength = new ArrayList<Integer>();
	/** The base X offset to shift the (0,0) cell horizontally. */
	public int baseXOffset;
	/** The base Y offset to shift the (0,0) vertically. */
	public int baseYOffset;
	/** The bounding rectangle that fits all cells of this map. */
	public Rectangle boundingRectangle;
	/** The list of building instances. */
	public final List<Building> buildings = new ArrayList<Building>();
	/** The list of surface features. */
	public final List<SurfaceFeature> features = new ArrayList<SurfaceFeature>();
	/** Compute the rendering start-stop locations. */
	public void computeRenderingLocations() {
		renderingOrigins.clear();
		renderingLength.clear();
		// y -> x
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
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
		if (x > renderingOrigins.get(-y).x || x < renderingOrigins.get(-y).x - renderingLength.get(-y) + 1) {
			return false;
		}		
		return true;
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
		SurfaceFeature sf = new SurfaceFeature();
		sf.id = id;
		sf.type = surface;
		sf.tile = tile;
		sf.location = Location.of(x, y);
		features.add(sf);
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
	 */
	public void parseMap(XElement map, GalaxyModel gm, BuildingModel bm) {
		if (gm != null) {
			XElement surface = map.childElement("surface");
			if (surface != null) {
				this.features.clear();
				this.basemap.clear();
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
						System.err.println("Missing planet type: " + type);
					}
					Tile t = pt.tiles.get(id);
					if (t == null) {
						System.err.println("Missing tile: " + id + " on planet type " + type);
					}
					
					placeBase(t, x, y, id, type);
				}
			}
		}
		if (bm != null) {
			XElement buildings = map.childElement("buildings");
			if (buildings != null) {
				setBuildings(bm, buildings);
			}
		}
	}
	/**
	 * Set the surface buildings from the given XElement.
	 * @param bm the building model
	 * @param buildings the buildings XElement
	 */
	void setBuildings(BuildingModel bm, XElement buildings) {
		this.buildings.clear();
		this.buildingmap.clear();
		
		for (XElement tile : buildings.childrenWithName("building")) {
			String id = tile.get("id");
			String tech = tile.get("tech");
			
			Building b = new Building(bm.buildings.get(id), tech);
			int x = Integer.parseInt(tile.get("x"));
			int y = Integer.parseInt(tile.get("y"));
		
			b.location = Location.of(x, y);
			
			String bp = tile.get("build");
			if (bp == null || bp.isEmpty()) {
				b.buildProgress = b.type.hitpoints;
			} else {
				b.buildProgress = Integer.parseInt(bp);
			}
			String hp = tile.get("hp");
			if (hp == null || hp.isEmpty()) {
				b.hitpoints = b.type.hitpoints;
			} else {
				b.hitpoints = Integer.parseInt(hp);
			}
			b.setLevel(Math.min(Integer.parseInt(tile.get("level")), b.type.upgrades.size())
			);
			b.assignedEnergy = Integer.parseInt(tile.get("energy"));
			b.assignedWorker = Integer.parseInt(tile.get("worker"));
			b.enabled = "true".equals(tile.get("enabled"));
			b.repairing = "true".equals(tile.get("repairing"));
			
			placeBuilding(b.tileset.normal, x, y, b);
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
			for (Building b : buildings) {
				XElement xb = xbuildings.add("building");
				xb.set("id", b.type.id);
				xb.set("tech", b.techId);
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
		if (buildings.size() > 0) {
			return buildings.get(buildings.size() - 1).techId;
		}
		return null;
	}
	/**
	 * Create a deep copy of the surface by sharing the basemap but
	 * copying the buildings.
	 * @return the planet surface copy
	 */
	public PlanetSurface copy() {
		PlanetSurface result = new PlanetSurface();
		
		result.setSize(width, height);
		
		for (Building b : buildings) {
			Building bc = b.copy();
			result.placeBuilding(bc.tileset.normal, bc.location.x, bc.location.y, bc);
		}
		for (SurfaceFeature f : features) {
			result.placeBase(f.tile, f.location.x, f.location.y, f.id, f.type);
		}
		for (Map.Entry<Location, SurfaceEntity> se : buildingmap.entrySet()) {
			if (se.getValue().type == SurfaceEntityType.ROAD) {
				result.buildingmap.put(se.getKey(), se.getValue());
			}
		}
		
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
		
		Set<Location> corners = new HashSet<Location>();
		for (Building bld : buildings) {
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
	 */
	public void removeBuilding(Building building) {
		if (buildings.remove(building)) {
			for (int a = building.location.x; a < building.location.x + building.tileset.normal.width; a++) {
				for (int b = building.location.y; b > building.location.y - building.tileset.normal.height; b--) {
					buildingmap.remove(Location.of(a, b));
				}
			}
		}
	}
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
		} else {
			SurfaceEntity se = buildingmap.get(Location.of(x, y));
			if (se != null && se.type == SurfaceEntityType.BUILDING) {
				return false;
			} else {
				se = basemap.get(Location.of(x, y));
				if (se != null && (se.tile.width > 1 || se.tile.height > 1)) {
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * Find a location on the surface which can support a building (and surrounding roads)
	 * with the given size. The location search starts of from the center of the map
	 * @param width should be the building tile width + 2
	 * @param height should be the builindg tile height + 2
	 * @return the top-left point where this building could be built, null indicates that
	 * no suitable location is present
	 */
	public Point findLocation(int width, int height) {
		int cx = this.width / 2 - this.height / 2 - width / 2;
		int cy = -this.width / 2 - this.height / 2 + height / 2;
		
		// the square size
		Point pt = null;
		long dist = -1L; 
		for (int i = 0; i < Math.abs(cy); i++) {
			for (int j = cx - i; j <= cx + i; j++) {
				for (int k = cy + i; k >= cy - i; k--) {
					if (
						j < cx - i + 1 || j > cx + i - 1
						|| k > cy + i - 1 || k < cy - i + 1
					) { 
						if (canPlaceBuilding(j, k, width, height)) {
							Point pt1 = new Point(j, k);
							long d1 = (j - cx) * (j - cx) + (k - cy) * (k - cy);
							if (dist < 0 || d1 <= dist) {
								pt = pt1;
								dist = d1;
							}
						}
					}
				}
			}
			if (pt != null) {
				break;
			}
		}
		
		
		
//		for (int j = cx; j >= cx - i; j--) {
//			if (canPlaceBuilding(j, cy + i, width, height)) {
//				return new Point(j, cy + i);
//			}
//			if (canPlaceBuilding(j, cy - i, width, height)) {
//				return new Point(j, cy - i);
//			}
//		}
//		for (int j = cx; j <= cx + i; j++) {
//			if (canPlaceBuilding(j, cy + i, width, height)) {
//				return new Point(j, cy + i);
//			}
//			if (canPlaceBuilding(j, cy - i, width, height)) {
//				return new Point(j, cy - i);
//			}
//		}
//		for (int k = cy; k <= cy + i; k++) {
//			if (canPlaceBuilding(cx - i, k, width, height)) {
//				return new Point(cx - i, k);
//			}
//			if (canPlaceBuilding(cx + i, k, width, height)) {
//				return new Point(cx + i, k);
//			}
//		}
//		for (int k = cy; k >= cy - i; k--) {
//			if (canPlaceBuilding(cx - i, k, width, height)) {
//				return new Point(cx - i, k);
//			}
//			if (canPlaceBuilding(cx + i, k, width, height)) {
//				return new Point(cx + i, k);
//			}
//		}
		
		return pt;
	}
}
