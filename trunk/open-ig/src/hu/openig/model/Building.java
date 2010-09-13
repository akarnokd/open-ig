/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Location;
import hu.openig.model.BuildingType.TileSet;

/**
 * A building instance.
 * @author karnokd, 2010.01.07.
 * @version $Revision 1.0$
 */
public class Building {
	/** The building type definition. */
	public BuildingType type;
	/** The tileset used when rendering the building. */
	public TileSet tileset;
	/** The building's placement. */
	public Location location;
	/**
	 * Tests wether the given location is within the base footprint of this placed building.
	 * @param a the X coordinate
	 * @param b the Y coordinate
	 * @return does the (a,b) fall into this building?
	 */
	public boolean containsLocation(int a, int b) {
		return a >= location.x && b <= location.y && a < location.x + tileset.normal.width && b > location.y - tileset.normal.height; 
	}
}
