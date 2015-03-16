/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The automatic building strategy.
 * FIXME: can't think other than civilian buildings
 * @author akarnokd, Mar 29, 2011
 */
public enum AutoBuild {
	/** The autobuild is offline. */
	OFF,
	/** 
	 * Build civilian buildings based on shortages: 
	 * Living space, food, hospital, police.
	 * If necessary, power plants are built too.
	 */
	CIVIL,
	/** Construct economic buildings and upgrade them. */
	ECONOMIC,
	/** Construct factory buildings and upgrade them. */
	FACTORY,
	/** Place one morale building per type. */
	SOCIAL,
	/** Upgrade buildings. */
	UPGRADE,
	/** Use the colony planner of the default AI. */
	AI,
}
