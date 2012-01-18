/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;

/**
 * Mission 4: Resolve pirate battle.
 * @author akarnokd, 2012.01.18.
 */
public class Mission5 extends Mission {
	@Override
	public void onTime() {
		if (world.level == 1) {
			Objective m2t1 = helper.objective("Mission-2-Task-3");
			Objective m5 = helper.objective("Mission-5");
			if (!m5.visible && m5.state == ObjectiveState.ACTIVE
					&& m2t1.state != ObjectiveState.ACTIVE
					&& !helper.hasMissionTime("Mission-5")) {
				helper.setMissionTime("Mission-5", helper.now() + 72);
			}
			if (helper.canStart("Mission-5")) {
				world.env.speed1();
				helper.setTimeout("Mission-5-Message", 2000);
				helper.clearMissionTime("Mission-5");
				helper.setMissionTime("Mission-5-Timeout-1", helper.now() + 24);
			}
			if (helper.isTimeout("Mission-5-Message")) {
				incomingMessage("Douglas-Admiral-Benson");
				helper.clearTimeout("Mission-5-Message");
				helper.showObjective("Mission-5");
			}
		}
	}
}
