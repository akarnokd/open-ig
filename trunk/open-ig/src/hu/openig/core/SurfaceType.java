/*
 * Copyright 2008-2009, David Karnok 
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
 * Enumeration for various surface types and their resource representations.
 * @author karnokd
 */
public enum SurfaceType {
	/** Desert surface. */
	DESERT(1, "BSIV", "Desert"),
	/** Icy surface. */
	ICE(2, "BJEG", "Frozen"),
	/** Cratered surface. */
	CRATER(3, "BKRA", "Cratered"),
	/** Rocky surface. */
	ROCKY(4, "BSZI", "Rocky"),
	/** Water surface. */
	LIQUID(5, "BVIZ", "Liquid"),
	/** Earth-type surface. */
	EARTH(6, "BFOL", "Earth"),
	/** Neptoplasm surface. */
	NEPTOPLASM(7, "BALI", "Neptoplasm")
	;
	/** Surface type index for the planet surface rendering. */
	public final int surfaceIndex;
	/** Surface type for starmap planet animation rendering. */
	public final String planetString;
	/** Textual representation in the planets.xml file. */
	public final String planetXmlString;
	/** Mapping from planet xml to surface type enums. */
	public static final Map<String, SurfaceType> MAP;
	/**
	 * Constructor. Sets the fields
	 * @param surfaceIndex the surface index in FELSZINx.PAC
	 * @param planetString the starmap-planet animation filename prefix
	 * @param planetXmlString the planet type referenced in the resource xmls
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
		MAP = Collections.unmodifiableMap(planetXmlMapL);
	}
}
