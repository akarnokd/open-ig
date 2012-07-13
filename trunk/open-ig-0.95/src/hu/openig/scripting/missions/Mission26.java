/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.model.BattleInfo;
import hu.openig.model.SpacewarWorld;

/**
 * This mission manages the wife videos and various generic achievements.
 * @author akarnokd, Feb 4, 2012
 */
public class Mission26 extends Mission {
	
	@Override
	public void onTime() {
		if (checkMission("Mission-26-Wife-1")) {
			incomingMessage("Wife-1");
		}
		if (checkMission("Mission-26-Wife-2")) {
			incomingMessage("Wife-4");
		}
		if (checkMission("Mission-26-Wife-3")) {
			incomingMessage("Wife-3");
		}
		if (checkMission("Mission-26-Wife-4")) {
			incomingMessage("Wife-2");
		}
		if (checkTimeout("Mission-26-Wife-Hide")) {
			receive("Wife-1").visible = false;
			receive("Wife-2").visible = false;
			receive("Wife-3").visible = false;
			receive("Wife-4").visible = false;
		}
	}
	@Override
	public void onLevelChanged() {
		if (world.level == 2) {
			addMission("Mission-26-Wife-2", 24);
		}
		if (world.level == 3) {
			addMission("Mission-26-Wife-3", 16 * 24);
		}
		if (world.level == 4) {
			addMission("Mission-26-Wife-4", 10 * 24);
		}
	}
	@Override
	public void onMessageSeen(String id) {
		if (id.startsWith("Wife-")) {
			addTimeout("Mission-26-Wife-Hide", 30000);
		}
	}
	@Override
	public boolean applicable() {
		return true;
	}
	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		onAutobattleFinish(war.battle());
	}
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		if (battle.attacker.owner == player
				&& battle.targetFleet != null 
				&& battle.targetFleet.owner == player("Traders") 
				&& battle.targetFleet.inventory.isEmpty()) {
			world.achievement("achievement.embargo");
		}
	}
}
