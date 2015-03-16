/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

import hu.openig.core.Location;

/**
 * The surface feature.
 * @author akarnokd
 */
public class SurfaceFeature {
	/** The location of the surface feature. */
	public Location location;
	/** The type of the surface feature, e.g., rocky, earth, etc. */
	public String type;
	/** The tile identifier. */
	public int id;
	/** The tile object. */
	public Tile tile;
	/**
	 * @return Create a copy of this feature.
	 */
	public SurfaceFeature copy() {
		SurfaceFeature r = new SurfaceFeature();
		r.location = location;
		r.type = type;
		r.id = id;
		r.tile = tile;
		return r;
	}
	/**
	 * Tests wether the given location is within the base footprint of this placed building.
	 * @param a the X coordinate
	 * @param b the Y coordinate
	 * @return does the (a,b) fall into this building?
	 */
	public boolean containsLocation(int a, int b) {
		return a >= location.x && b <= location.y && a < location.x + tile.width && b > location.y - tile.height; 
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SurfaceFeature) {
			SurfaceFeature sf = (SurfaceFeature)obj;
			return id == sf.id && type.equals(sf.type);
		}
		return false;
	}
	@Override
	public int hashCode() {
		return 17 + (id * 31) + type.hashCode();
	}
}
