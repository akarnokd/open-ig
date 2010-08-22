/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.model;

import hu.openig.v1.core.PlanetType;

/**
 * A planet.
 * @author karnokd, 2010.01.07.
 * @version $Revision 1.0$
 */
public class Planet {
	/** The planet's display name. */
	public String name;
	/** The X coordinate on the unscaled starmap. */
	public int x;
	/** The Y coordinate on the unscaled starmap. */
	public int y;
	/** The planet's type. */
	public PlanetType type;
	/** The surface map. */
	public byte[] surface;
	/** The owner. */
	public Player owner;
	/** The inhabitant race. */
	public Race race;
	/** The current population. */
	public int population;
	/** The rendered rotation phase. */
	public int rotationPhase;
	/** The rotation direction. */
	public boolean rotationDirection;
	/** The radar radius. */
	public int radar;
}
