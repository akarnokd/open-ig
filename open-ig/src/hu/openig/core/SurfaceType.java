/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.core;

/**
 * Enumeration for various surface types.
 * @author karnokd
 */
public enum SurfaceType {
	DESERT(1, "BSIV"),
	ICE(2, "BJEG"),
	CRATER(3, "BKRA"),
	ROCKY(4, "BSZI"),
	LIQUID(5, "BVIZ"),
	EARTH(6, "BFOL"),
	NECTOPLASM(7, "BALI")
	;
	/** Surface type index for the planet surface rendering. */
	public final int surfaceIndex;
	/** Surface type for starmap planet animation rendering. */
	public final String planetString;
	/**
	 * Constructor. Sets the fields
	 * @param surfaceIndex
	 * @param planetString
	 */
	SurfaceType(int surfaceIndex, String planetString) {
		this.surfaceIndex = surfaceIndex;
		this.planetString = planetString;
	}
}
