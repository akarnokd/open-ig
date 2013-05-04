/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * Status of a concrete building.
 * @author akarnokd, 2013.04.27.
 */
public class BuildingStatus {
	/** The building's unique id. */
	public int id;
	/** The building's type. */
	public String type;
	/** The building's race. */
	public String race;
	/** The built location. */
	public int x;
	/** The built location. */
	public int y;
	/** The energy assigned to this building. */
	public int assignedEnergy;
	/** The worker assigned to this building. */
	public int assignedWorker;
	/** The buildup progress up to the top hit point. */
	public int buildProgress;
	/** The hitpoints of this building. */
	public int hitpoints;
	/** The current upgrade level. 0 means no upgrades. */
	public int upgradeLevel;
	/** Is the building enabled. */
	public boolean enabled = true;
	/** Is the building under repair. */
	public boolean repairing;
}
