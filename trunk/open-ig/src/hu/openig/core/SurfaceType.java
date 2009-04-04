/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration for various surface types.
 * @author karnokd
 */
public enum SurfaceType {
	DESERT(1, "BSIV", "Desert"),
	ICE(2, "BJEG", "Frozen"),
	CRATER(3, "BKRA", "Cratered"),
	ROCKY(4, "BSZI", "Rocky"),
	LIQUID(5, "BVIZ", "Liquid"),
	EARTH(6, "BFOL", "Earth"),
	NECTOPLASM(7, "BALI", "Neptoplasm")
	;
	/** Surface type index for the planet surface rendering. */
	public final int surfaceIndex;
	/** Surface type for starmap planet animation rendering. */
	public final String planetString;
	/** Textual representation in the planets.xml file. */
	public final String planetXmlString;
	/** Mapping from planet xml to surface type enums. */
	public static final Map<String, SurfaceType> planetXmlMap;
	/**
	 * Constructor. Sets the fields
	 * @param surfaceIndex
	 * @param planetString
	 * @param planetXmlString
	 */
	SurfaceType(int surfaceIndex, String planetString, String planetXmlString) {
		this.surfaceIndex = surfaceIndex;
		this.planetString = planetString;
		this.planetXmlString = planetXmlString;
	}
	static {
		// map the values to enum
		Map<String, SurfaceType> planetXmlMapL = new HashMap<String, SurfaceType>();
		for (SurfaceType v : values()) {
			planetXmlMapL.put(v.planetXmlString, v);
		}
		planetXmlMap = Collections.unmodifiableMap(planetXmlMapL);
	}
}
