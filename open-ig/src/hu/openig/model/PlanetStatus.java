/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * Planet status record containing various properties.
 * @author karnokd
 */
public class PlanetStatus {
	/** The current population. */
	public int population;
	/** Total energy demand. */
	public int energyDemand;
	/** Total energy production. */
	public int energyProduction;
	/** Living space. */
	public int livingSpace;
	/** Hospital. */
	public int hospital;
	/** Worker demands. */
	public int workerDemand;
	/** Food. */
	public int food;
	/** The current radar radius. */
	public int radar;
}
