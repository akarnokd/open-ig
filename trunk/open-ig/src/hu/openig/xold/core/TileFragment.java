/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.xold.core;

/**
 * Immutable record class which has a tile fragment index and the actual tile provider.
 * Each Tile has strips of its image to work with the Z-order rendering.
 * This class holds the fragment index into this strips to use for
 * rendering at a particular location.
 * For renderers, if the fragment index points beyond the provided tile's strip count
 * it can be wrapped around.
 * @author karnokd
 */
public final class TileFragment {
	/** The tile fragment index. -1 indicates no tile should be used. */
	public final int fragment;
	/** The tile provider which will supply the actual tile object. */
	public final TileProvider provider;
	/** The tile fragment represents a road. */
	public final boolean isRoad;
	/**
	 * Constructor. Initializes the final fields
	 * @param fragment the fragment index, -1 indicates no tile to use
	 * @param provider the tile provider, not null
	 * @param isRoad the fragment represents a road
	 */
	private TileFragment(int fragment, TileProvider provider, boolean isRoad) {
		this.fragment = fragment;
		if (provider == null) {
			throw new IllegalArgumentException("provider null");
		}
		this.provider = provider;
		this.isRoad = isRoad;
	}
	/**
	 * Returns a tile fragment with the supplied settings.
	 * @param fragment The tile fragment index. -1 indicates no tile should be used
	 * @param provider The tile provider which will supply the actual tile object
	 * @param isRoad the fragment represents a road
	 * @return the tile fragment object
	 */
	public static TileFragment of(int fragment, TileProvider provider, boolean isRoad) {
		return new TileFragment(fragment, provider, isRoad);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TileFragment) {
			TileFragment tf = (TileFragment)obj;
			return this.fragment == tf.fragment && this.provider.equals(tf.provider);
		}
		return false;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return 31 * (fragment + 17) + provider.hashCode();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "{" + fragment + ", " + provider + "}";
	}
}
