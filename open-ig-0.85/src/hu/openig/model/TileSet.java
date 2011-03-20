/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

import hu.openig.core.Tile;

/** The tileset for complete buildings. */
public class TileSet {
	/** The normal building tile. */
	public Tile normal;
	/** The no-light version of the tile. */
	public Tile nolight;
	/** The damaged building tile. */
	public Tile damaged;
}
