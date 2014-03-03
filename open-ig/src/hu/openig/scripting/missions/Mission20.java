/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Action0;
import hu.openig.model.ObjectiveState;
import hu.openig.utils.XElement;

/**
 * Mission 20: 2nd vision and talk to Brian.
 * @author akarnokd, 2012.02.23.
 */
public class Mission20 extends Mission {
	/** The mission stages. */
	enum M20 {
		/** Not started. */
		NONE,
		/** Wait for vision. */
		WAIT,
		/** Playing vision. */
		VISION,
		/** Talk is available. */
		TALK,
		/** Mission done. */
		DONE
	}
	/** The current stage. */
	M20 stage = M20.NONE;
	@Override
	public void onTime() {
		if (stage == M20.NONE && objective("Mission-19").state != ObjectiveState.ACTIVE) {
			stage = M20.WAIT;
			addMission("Mission-20", 7 * 12);
		}
		if (checkMission("Mission-20")) {
			stage = M20.VISION;
			world.env.stopMusic();
			world.env.playVideo("interlude/dream_2", new Action0() {
				@Override
				public void invoke() {
					stage = M20.TALK;
					world.currentTalk = "brian";
					showObjective("Mission-20");
					addMission("Mission-20-Timeout", 2 * 24);
					world.env.playMusic();

				}
			});
		}
		if (checkMission("Mission-20-Timeout")) {
			stage = M20.DONE;
			setObjectiveState("Mission-20", ObjectiveState.FAILURE);
			addTimeout("Mission-20-Hide", 13000);
			world.currentTalk = null;
		}
		if (checkTimeout("Mission-20-Hide")) {
			objective("Mission-20").visible = false;
		}
		if (checkMission("Mission-20-EndTalk")) {
			world.currentTalk = null;
		}
	}
	@Override
	public void onTalkCompleted() {
		if ("brian".equals(world.currentTalk) && stage == M20.TALK) {
			setObjectiveState("Mission-20", ObjectiveState.SUCCESS);
			clearMission("Mission-20-Timeout");
			stage = M20.DONE;
			addTimeout("Mission-20-Hide", 13000);
			addMission("Mission-20-EndTalk", 1 * 24);
		}
	}
	@Override
	public boolean applicable() {
		return world.level == 3;
	}
	@Override
	public void save(XElement xmission) {
		super.save(xmission);
		xmission.set("stage", stage);
	}
	@Override
	public void load(XElement xmission) {
		super.load(xmission);
		stage = M20.valueOf(xmission.get("stage"));
	}
	@Override
	public void reset() {
		stage = M20.NONE;
		world.currentTalk = null;
	}

}
