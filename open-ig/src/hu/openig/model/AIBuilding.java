/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The AI building representation.
 * @author akarnokd, 2011.12.28.
 */
public class AIBuilding extends Building {
	/** The backing building. */
	public final Building building;
	/**
	 * Copy constructor.
	 * @param b the other building
	 */
	public AIBuilding(Building b) {
		super(b.id, b.type, b.race);
		this.building = b;
		this.location = b.location;
		this.assignedEnergy = b.assignedEnergy;
		this.assignedWorker = b.assignedWorker;
		this.buildProgress = b.buildProgress;
		this.hitpoints = b.hitpoints;
		this.currentUpgrade = b.currentUpgrade;
		this.upgradeLevel = b.upgradeLevel;
		this.enabled = b.enabled;
		this.repairing = b.repairing;
	}
}
