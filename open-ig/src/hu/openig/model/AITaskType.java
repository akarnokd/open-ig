/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * List of all task types an AI player needs to consider.
 * @author akarnokd, 2011.12.08.
 */
public enum AITaskType {
	/** Produce a new ship. */
	PRODUCE_SHIP(AITaskCategory.OFFENSIVE),
	/** Produce a new tank. */
	PRODUCE_TANK(AITaskCategory.OFFENSIVE),
	/** Produce a new satellite. */
	PRODUCE_SATELLITE(AITaskCategory.GENERAL),
	/** Produce a new station. */
	PRODUCE_STATION(AITaskCategory.DEFENSIVE),
	/** Build a military spaceport. */
	BUILD_MILITARY_SPACEPORT(AITaskCategory.GENERAL),
	/** Build a ground defense cannon. */
	BUILD_CANNON(AITaskCategory.DEFENSIVE),
	/** Build a defensive shield. */
	BUILD_SHIELD(AITaskCategory.DEFENSIVE),
	/** Deploy a fleet. */
	DEPLOY_FLEET(AITaskCategory.OFFENSIVE),
	/** Deploy a fleet capable of capturing a planet. */
	DEPLOY_ATTACK_FLEET(AITaskCategory.OFFENSIVE),
	/** Produce a colonization ship. */
	PRODUCE_COLONY_SHIP(AITaskCategory.GENERAL),
	/** Deploy the colony ship into a fleet. */
	DEPLOY_COLONY_SHIP_FLEET(AITaskCategory.GENERAL),
	/** Deploy a space station. */
	DEPLOY_STATION(AITaskCategory.DEFENSIVE),
	/** Explore the galaxy. */
	EXPLORE(AITaskCategory.GENERAL),
	/** Build social buildings. */
	BUILD_SOCIAL(AITaskCategory.SOCIAL),
	/** Build (defensive) military buildings. */
	BUILD_MILITARY(AITaskCategory.DEFENSIVE),
	/** Build factory buildings. */
	BUILD_FACTORY(AITaskCategory.GENERAL),
	/** Build research buildings. */
	BUILD_RESEARCH(AITaskCategory.GENERAL),
	/** Build buildings to raise the morale. */
	BUILD_MORALE(AITaskCategory.SOCIAL),
	/** Deploy tanks to planets. */
	DEPLOY_TANKS(AITaskCategory.DEFENSIVE),
	/** Deploy fighters to planets. */
	DEPLOY_FIGHTERS(AITaskCategory.DEFENSIVE),
	/** Attack an enemy fleet. */
	ATTACK_FLEET(AITaskCategory.OFFENSIVE),
	/** Attack an enemy planet. */
	ATTACK_PLANET(AITaskCategory.OFFENSIVE),
	/** Colonize a planet. */
	COLONIZE_PLANET(AITaskCategory.GENERAL),
	/** Move a fleet into defensive position above a planet. */
	DEFEND_PLANET(AITaskCategory.DEFENSIVE),
	/** Move a fleet over a planet which has military spaceport. */
	REPAIR_FLEET(AITaskCategory.GENERAL),
	/** Perform some diplomatic actions. */
	DIPLOMACY(AITaskCategory.SOCIAL)
	;
	/** The task budget category. */
	public final AITaskCategory category;
	/**
	 * Construct the enum with a category.
	 * @param category the target category
	 */
	AITaskType(AITaskCategory category) {
		this.category = category;
	}
}
