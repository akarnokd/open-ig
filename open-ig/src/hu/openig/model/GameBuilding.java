/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Location;
import hu.openig.core.Tile;
import hu.openig.core.TileProvider;

/**
 * Actual building instance.
 * @author karnokd
 */
public class GameBuilding implements TileProvider {
	/** The prototype building. */
	public GameBuildingPrototype prototype;
	/** Shortcut for building images for the actual tech id. */
	public GameBuildingPrototype.BuildingImages images;
	/** The technology id. */
	public String techId; // TODO do we need this here?
	/** The tile X coordinate of the left edge. */
	public int x;
	/** The tile Y coordinate of the top edge. */
	public int y;
	/** The current damage level. */
	public int health; // FIXME damage percent or hitpoints?
	/** The current build progress percent: 0 to 100. */
	public int progress;
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tile getTile(Location location) {
		if (progress == 100) {
			if (health < 50) { // FIXME health level to switch to damaged tile
				return images.damagedTile;
			}
			return images.regularTile;
		}
		// FIXME: maybe the returned tile should depend on the internal location, to have non-uniformly built structure visual effect
		return images.buildPhases.get(images.buildPhases.size() * progress / 100); 
	}
}
