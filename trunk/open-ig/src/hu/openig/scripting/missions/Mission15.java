/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Action0;
import hu.openig.model.ObjectiveState;

/**
 * Mission 15: see spy, talk to Colonel, talk to kelly.
 * @author akarnokd, 2012.01.18.
 */
public class Mission15 extends Mission {
	@Override
	public void onTime() {
		if (world.level != 2) {
			return;
		}
		if (helper.canStart("Mission-15")) {
			helper.clearMissionTime("Mission-15");
			helper.showObjective("Mission-15");
			world.env.playVideo("interlude/spy_on_johnson", new Action0() {
				@Override
				public void invoke() {
					world.currentTalk = "kelly";
				}
			});
		}
		if (world.recordWatched) {
			helper.send("Douglas-Report-Spy").visible = true;
			helper.setMissionTime("Mission-15-Timeout", helper.now() + 24);
		}
		if (helper.isMissionTime("Mission-15-Timeout")) {
			helper.send("Douglas-Report-Spy").visible = false;
			helper.clearMissionTime("Mission-15-Timeout");
			helper.setObjectiveState("Mission-15", ObjectiveState.FAILURE);
			helper.setTimeout("Mission-15-Hide", 13000);
		}
		if (helper.isMissionTime("Mission-15-Hide")) {
			helper.objective("Mission-15").visible = false;
		}
	}
	@Override
	public void onMessageSeen(String id) {
		if (world.level == 2) {
			if ("Douglas-Report-Spy".equals(id)) {
				helper.clearMissionTime("Mission-15-Timeout");
				helper.setTimeout("Mission-15-Hide", 13000);
			}
		}
	}
	@Override
	public void onTalkCompleted() {
		if (world.level == 2) {
			helper.setObjectiveState("Mission-15", ObjectiveState.SUCCESS);
			world.currentTalk = null;
			world.allowRecordMessage = true;
			helper.clearMissionTime("Mission-15-Timeout");
			helper.setTimeout("Mission-15-Hide", 13000);
		}
	}
}
