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
import hu.openig.core.Tile;
import hu.openig.utils.XElement;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The container class for the planetary surface objects, including the base surface map,
 * builings, roads and vehicles.
 * @author akarnokd
 */
public class PlanetSurface {
	/** 
	 * The map's with in cells. 
	 * The width is defined as a slightly descending horizontal dimension of the map, 
	 * but in coordinate terms it is equal to the sequence 0,0 1,-1, 2,-2 etc.
	 * Note that the rendering coordinate system is different from the original IG's map definition. 
	 */
	public int width;
	/** The height with in cells. The width is defined as a vertical dimension of the map, but in coordinate terms it is equal to the sequence 0,0 -1,-1, -2,-2 etc. */
	public int height;
	/** The current lighting level. */
	public float alpha = 1.0f;
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
				se.tile.alpha = alpha;
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
				se.tile.alpha = alpha;
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
				String tech = null;
				for (XElement tile : buildings.childrenWithName("building")) {
					String id = tile.get("id");
					tech = tile.get("tech");
					
					Building b = new Building(bm.buildings.get(id), tech);
					int x = Integer.parseInt(tile.get("x"));
					int y = Integer.parseInt(tile.get("y"));
				
					b.location = Location.of(x, y);
					
					b.buildProgress = Integer.parseInt(tile.get("build"));
					b.hitpoints = Integer.parseInt(tile.get("hp"));
					b.setLevel(Integer.parseInt(tile.get("level")));
					b.assignedEnergy = Integer.parseInt(tile.get("energy"));
					b.assignedWorker = Integer.parseInt(tile.get("worker"));
					b.enabled = "true".equals(tile.get("enabled"));
					b.repairing = "true".equals(tile.get("repairing"));
					
					placeBuilding(b.tileset.normal, x, y, b);
				}
			}
		}
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
	 * Set the lighting level on the surface tiles.
	 * @param alpha the new lighting level
	 */
	public void setAlpha(float alpha) {
		this.alpha = alpha;
		for (SurfaceFeature sf : features) {
			sf.tile.alpha = alpha;
		}
		for (Building b : buildings) {
			b.tileset.normal.alpha = alpha;
		}
	}
}
