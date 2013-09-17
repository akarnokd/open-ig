/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

/**
 * Immutable class/record representing an X,Y integral coordinate.
 * @author akarnokd
 */
public final class Location {
	/** The X coordinate. */
	public final int x;
	/** The Y coordinate. */
	public final int y;
	/** The cached hash code. */
	private final int hc;
	/** The location cache, where the first dimension is the X coordinate + 80, 
	 * the second is Y coordinate + 159. */
	private static final Location[][] CACHE;
	static {
		CACHE = new Location[160][160];
		for (int i = 0; i < CACHE.length; i++) {
			for (int j = 0; j < CACHE[i].length; j++) {
				CACHE[i][j] = new Location(i - 80, -j);
			}
		}
	}
	/**
	 * Constructor. Initializes the fields.
	 * @param x the X coordinate.
	 * @param y the Y coordinate
	 */
	private Location(int x, int y) {
		this.x = x;
		this.y = y;
		this.hc = 31 * (x + 17) + y;
	}
	/**
	 * Returns a location object with
	 * the given coordinates.
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @return the location object
	 */
	public static Location of(int x, int y) {
		if (x < -80 || x >= 80 || y < -159 || y > 0) {
			return new Location(x, y);
		}
		return CACHE[x + 80][-y];
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof Location) {
			Location l = (Location)obj;
			return this.x == l.x && this.y == l.y;
		}
		return false;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return hc;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
	/**
	 * Returns a location relative to this location.
	 * @param dx the delta x
	 * @param dy the delta y
	 * @return the new location
	 */
	public Location delta(int dx, int dy) {
		return Location.of(x + dx, y + dy);
	}
}
