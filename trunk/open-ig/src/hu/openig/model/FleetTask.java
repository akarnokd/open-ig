/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * Represents the current task allocation and implicit priority of the fleet.
 * <p>Fleets with lower task priorities can be repurposed.</p>
 * @author akarnokd, 2011.12.30.
 */
public enum FleetTask {
	/** The fleet is executing a script. */
	SCRIPT,
	/** Defensive task. */
	DEFEND,
	/** Attacking. */
	ATTACK,
	/** Colonizing. */
	COLONIZE,
	/** Deployment of new units. */
	DEPLOY,
	/** Upgrade equipment. */
	UPGRADE,
	/** Explore the map. */
	EXPLORE,
	/** Patrol. */
	PATROL,
	/** Simply move somewhere. */
	MOVE,
	/** Nothing. */
	IDLE
}
