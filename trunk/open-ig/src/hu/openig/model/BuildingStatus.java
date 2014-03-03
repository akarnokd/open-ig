/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.net.MessageObject;

/**
 * Status of a concrete building.
 * @author akarnokd, 2013.04.27.
 */
public class BuildingStatus implements MessageObjectIO {
	/** The object name. */
	public static final String OBJECT_NAME = "BUILDING";
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
	@Override
	public void fromMessage(MessageObject mo) {
		id = mo.getInt("id");
		type = mo.getString("type");
		race = mo.getString("race");
		x = mo.getInt("x");
		y = mo.getInt("y");
		assignedEnergy = mo.getInt("energy");
		assignedWorker = mo.getInt("worker");
		buildProgress = mo.getInt("progress");
		hitpoints = mo.getInt("hp");
		upgradeLevel = mo.getInt("level");
		enabled = mo.getBoolean("enabled");
		repairing = mo.getBoolean("repairing");
	}
	@Override
	public MessageObject toMessage() {
		MessageObject mo = new MessageObject(objectName());
		mo.set("id", id)
		.set("type", type)
		.set("race", race)
		.set("x", x)
		.set("y", y)
		.set("energy", assignedEnergy)
		.set("worker", assignedWorker)
		.set("hp", hitpoints)
		.set("level", upgradeLevel)
		.set("enabled", enabled)
		.set("repairing", repairing)
		;
		return mo;
	}
	@Override
	public String objectName() {
		return OBJECT_NAME;
	}
	/**
	 * Remove information that doesn't concern an enemy player.
	 * Note that the game should provide BUILDING level knowledge 
	 * to the player when on ground battle.
	 */
	public void clearEnemyInfo() {
		// TODO should this happen?
	}
}
