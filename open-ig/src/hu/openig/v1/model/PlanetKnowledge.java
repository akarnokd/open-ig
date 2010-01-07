/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.model;

/**
 * Represents a knowledge status about a planet.
 * @author karnokd, 2010.01.07.
 * @version $Revision 1.0$
 */
public enum PlanetKnowledge {
	/** The planet has been discovered, the name is not displayed on the starmap and the surface does not show any buildings. */
	DISCOVERED,
	/** The planet name is known and displayed, but in gray color. */
	NAMED,
	/** The owner of the planet is known and its colored label indicates this. Buildings are outlined as black cells. */
	OWNED,
	/** The buildings are also drawn. */
	BUILDINGS,
	/** All statistics are available about the planet. Player owned planets have this flag. */
	FULL
}
