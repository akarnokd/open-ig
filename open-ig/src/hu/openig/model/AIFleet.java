/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * Class representing a fleet for an AI player.
 * @author akarnokd, 2011.12.08.
 */
public class AIFleet {
	/** The original fleet object. */
	public Fleet fleet;
	/** The current fleet knowledge. */
	public FleetKnowledge knowledge;
	/** The current fleet statistics. */
	public FleetStatistics statistics;
	/**
	 * Assign the necessary properties from a fleet.
	 * @param fleet the target fleet
	 * @param world the world object
	 */
	public void assign(Fleet fleet, AIWorld world) {
		this.fleet = fleet;
		knowledge = world.knowledge(fleet);
		this.statistics = world.getStatistics(fleet);
	}
}
