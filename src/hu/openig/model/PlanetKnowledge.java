/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * Represents a knowledge status about a planet.
 * @author akarnokd, 2010.01.07.
 */
public enum PlanetKnowledge {
	/** The planet has been discovered, the name is not displayed on the starmap and the surface does not show any buildings. */
	VISIBLE,
	/** The planet name is known and displayed, but in gray color. */
	NAME,
	/** 
	 * The owner of the planet is known and its colored label indicates this. 
	 * Buildings are outlined as initial construction cells. 
	 */
	OWNER,
	/** Display space stations and population number. */
	STATIONS,
	/** The buildings are also drawn. */
	BUILDING,
}
