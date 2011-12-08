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
public class AIFleet extends AIObject {
	/** The original fleet object. */
	public Fleet fleet;
	/**
	 * Assign the necessary properties from a fleet.
	 * @param fleet the target fleet
	 * @param world the world object
	 */
	public void assign(Fleet fleet, World world) {
		this.fleet = fleet;
	}
	/**
	 * Apply the fleet properties.
	 * @param world the world object
	 */
	public void apply(World world) {
		
	}
	@Override
	public void assign(AITaskCandidate tc) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public double taskTime(AITask task) {
		// TODO Auto-generated method stub
		return Double.POSITIVE_INFINITY;
	}
}
