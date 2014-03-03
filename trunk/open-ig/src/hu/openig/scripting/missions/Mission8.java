/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Action0;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.SoundTarget;
import hu.openig.model.SoundType;
import hu.openig.utils.XElement;

/**
 * Mission 8: Test and visions.
 * @author akarnokd, 2012.01.18.
 */
public class Mission8 extends Mission {
	/** Re-execute the waiting? */
	boolean runWaiting;
	@Override
	public boolean applicable() {
		return world.level == 2;
	}
	@Override
	public void onLevelChanged() {
		if (world.level == 2) {
			addMission("Mission-8", 3 * 24);
			world.testNeeded = false;
			world.testCompleted = false;
		}
	}
	@Override
	public void onTime() {
		final Objective m8 = objective("Mission-8");
		if (checkMission("Mission-8")) {
			runWaiting = true;
			world.env.playSound(SoundTarget.COMPUTER, SoundType.PHSYCHOLOGIST_WAITING, createWaiting(m8));
		}
		if (world.testCompleted && m8.state == ObjectiveState.ACTIVE) {
			setObjectiveState(m8, ObjectiveState.SUCCESS);
			addTimeout("Mission-8-Hide", 13000);
			if (world.testScore() * 2 < world.testMax()) {
				addMission("Mission-8-Fire", 48);
			} else {
				addMission("Mission-8-Visions", 10 * 24);
			}
		}
		if (checkTimeout("Mission-8-Hide")) {
			m8.visible = false;
		}
		if (checkMission("Mission-8-Fire")) {
			gameover();
			loseGameMessageAndMovie("Douglas-Fire-Test", "lose/fired_level_2");
		}
		if (checkMission("Mission-8-Visions")) {
			world.env.stopMusic();
			world.env.playVideo("interlude/dream_1", new Action0() {
				@Override
				public void invoke() {
//					addMission("Mission-8-Visions-2", 5 * 24);
					world.env.playMusic();
				}
			});
		}
		if (checkMission("Mission-8-Visions-2")) {
			addMission("Mission-15", 4 * 24);
			world.env.stopMusic();
			world.env.playVideo("interlude/dream_3", new Action0() {
				@Override
				public void invoke() {
					world.currentTalk = "phsychologist";
					showObjective("Mission-8-Task-1");
					addMission("Mission-8-Task-1-Timeout", 2 * 24);
					world.env.playMusic();
				}
			});
		}
		if (checkMission("Mission-8-Task-1-Timeout")) {
			setObjectiveState("Mission-8-Task-1", ObjectiveState.FAILURE);
			addTimeout("Mission-8-Task-1-Hide", 13000);
			world.currentTalk = null;
		}
		if (checkTimeout("Mission-8-Task-1-Hide")) {
			objective("Mission-8-Task-1").visible = false;
			world.currentTalk = null;
		}
	}
	/**
	 * Create the action to execute after the announcement of the phsychologist.
	 * @param m8 the mission 8
	 * @return the action
	 */
	private Action0 createWaiting(final Objective m8) {
		return new Action0() {
			@Override
			public void invoke() {
				runWaiting = false;
				showObjective(m8);
				world.testNeeded = true;
				world.testCompleted = false;
			}
		};
	}
	@Override
	public void onTalkCompleted() {
		if ("phsychologist".equals(world.currentTalk)) {
			if (setObjectiveState("Mission-8-Task-1", ObjectiveState.SUCCESS)) {
				clearMission("Mission-8-Task-1-Timeout");
				addTimeout("Mission-8-Task-1-Hide", 13000);
			}
		}
	}
	@Override
	public void load(XElement xmission) {
		super.load(xmission);
		runWaiting = xmission.getBoolean("run-waiting", false);
	}
	@Override
	public void save(XElement xmission) {
		super.save(xmission);
		xmission.set("run-waiting", runWaiting);
	}
	@Override
	public void onLoaded() {
		super.onLoaded();
		if (runWaiting) {
			createWaiting(objective("Mission-8")).invoke();
		}
	}
}
