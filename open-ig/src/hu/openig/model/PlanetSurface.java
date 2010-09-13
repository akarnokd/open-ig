/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

import hu.openig.core.Location;
import hu.openig.core.Tile;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The container class for the planetary surface objects, including the base surface map,
 * builings, roads and vehicles.
 * @author karnokd
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
	public List<Building> buildings = new ArrayList<Building>();
	/** Compute the rendering start-stop locations. */
	public void computeRenderingLocations() {
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
}
