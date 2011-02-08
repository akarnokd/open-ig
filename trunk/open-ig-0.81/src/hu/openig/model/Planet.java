/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.PlanetType;

import java.util.ArrayList;
import java.util.List;

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
	/** The diameter in pixels up to 30 for the maximum zoom. */
	public int diameter;
	/** The current list of problems. */
	public List<PlanetProblems> problems = new ArrayList<PlanetProblems>();
	/** The planet is under quarantine: display red frame. */
	public boolean quarantine;
	/** The contents of the planet. */
	public PlanetSurface surface;
}
