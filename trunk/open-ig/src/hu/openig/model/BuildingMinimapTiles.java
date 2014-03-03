/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;


/**
 * Record for storing building state dependant filled and colored tiles. 
 * @author akarnokd
 */
public class BuildingMinimapTiles {
	/** The normal state tile. */
	public Tile normal;
	/** The damaged state tile. */
	public Tile damaged;
	/** The inoperable tile. */
	public Tile inoperable;
	/** The destroyed tile. */
	public Tile destroyed;
	/** The constructing tile. */
	public Tile constructing;
	/** The damaged constructing tile. */
	public Tile constructingDamaged;
}
