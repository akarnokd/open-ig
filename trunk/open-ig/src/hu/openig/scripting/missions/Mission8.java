/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Action0;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.SoundType;

/**
 * Mission 6: Defend Achilles.
 * @author akarnokd, 2012.01.18.
 */
public class Mission8 extends Mission {
	@Override
	public void onLevelChanged() {
		if (world.level == 2) {
			Objective m8 = helper.objective("Mission-8");
			if (!m8.visible && m8.state == ObjectiveState.ACTIVE) {
				helper.setMissionTime("Mission-8", helper.now() + 3 * 24);
				world.testNeeded = false;
				world.testCompleted = false;
			}
		}
	}
	@Override
	public void onTime() {
		if (world.level != 2) {
			return;
		}
		Objective m8 = helper.objective("Mission-8");
		if (helper.canStart("Mission-8")) {
			helper.clearMissionTime("Mission-8");
			helper.setTimeout("Mission-8-Announce", 5000);
			world.env.computerSound(SoundType.PHSYCHOLOGIST_WAITING);
		}
		if (helper.isTimeout("Mission-8-Announce")) {
			helper.clearTimeout("Mission-8-Announce");
			helper.showObjective(m8);
			world.testNeeded = true;
			world.testCompleted = false;
		}
		if (world.testCompleted && m8.state == ObjectiveState.ACTIVE) {
			helper.setObjectiveState(m8, ObjectiveState.SUCCESS);
			helper.setTimeout("Mission-8-Hide", 13000);
			if (world.testScore() * 2 < world.testMax()) {
				helper.setMissionTime("Mission-8-Fire", helper.now() + 48);
			} else {
				helper.setMissionTime("Mission-8-Visions", helper.now() + 30 * 24);
			}
		}
		if (checkTimeout("Mission-8-Hide")) {
			m8.visible = false;
		}
		if (checkMission("Mission-8-Fire")) {
			helper.gameover();
			loseGameMessageAndMovie("Douglas-Fire-Test", "loose/fired_level_2");
		}
		if (checkMission("Mission-8-Visions")) {
			
			helper.setMissionTime("Mission-15", helper.now() + 15 * 24);
			world.env.stopMusic();
			world.env.playVideo("interlude/dream_1", new Action0() {
				@Override
				public void invoke() {
					world.currentTalk = "phsychologist";
					helper.showObjective("Mission-8-Task-1");
					helper.setMissionTime("Mission-8-Task-1-Timeout", helper.now() + 2 * 24);
					world.env.playMusic();
				}
			});
		}
		if (checkMission("Mission-8-Task-1-Timeout")) {
			helper.setObjectiveState("Mission-8-Task-1", ObjectiveState.FAILURE);
			helper.setTimeout("Mission-8-Task-1-Hide", 13000);
			world.currentTalk = null;
		}
		if (checkTimeout("Mission-8-Task-1-Hide")) {
			helper.objective("Mission-8-Task-1").visible = false;
			world.currentTalk = null;
		}
	}
	@Override
	public void onTalkCompleted() {
		if ("phsychologist".equals(world.currentTalk)) {
			helper.clearMissionTime("Mission-8-Task-1-Timeout");
			helper.setObjectiveState("Mission-8-Task-1", ObjectiveState.SUCCESS);
			helper.setTimeout("Mission-8-Task-1-Hide", 13000);
		}
	}
}