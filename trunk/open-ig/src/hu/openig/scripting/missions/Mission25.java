/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Action0;
import hu.openig.core.Pair;
import hu.openig.model.Fleet;
import hu.openig.model.InventoryItem;
import hu.openig.model.ObjectiveState;
import hu.openig.utils.XElement;

/**
 * Mission 25: Defeat the Dargslan.
 * @author akarnokd, 2012.03.02.
 */
public class Mission25 extends Mission {
	/** The mission stages. */
	enum M25 {
		/** Not started. */
		NONE,
		/** Running. */
		RUNNING,
		/** Done. */
		DONE
	}
	/** The mission stage. */
	M25 stage = M25.NONE;
	@Override
	public boolean applicable() {
		return world.level == 5;
	}
	@Override
	public void onTime() {
		if (stage == M25.NONE) {
			helper.showObjective("Mission-25");
			stage = M25.RUNNING;
		}
		if (stage == M25.RUNNING) {
			if (player("Dargslan").statistics.planetsOwned == 0) {
				stage = M25.DONE;
				helper.setObjectiveState("Mission-25", ObjectiveState.SUCCESS);
				addTimeout("Win", 6000);
			}
		}
		if (checkTimeout("Win")) {
			helper.objective("Mission-25").visible = false;
			world.env.stopMusic();
			world.env.pause();
			world.env.playVideo("win/win", new Action0() {
				@Override
				public void invoke() {
					world.env.winGame();
				}
			});
		}
		checkMainShip();
	}
	/** Check if the main ship is still operational. */
	void checkMainShip() {
		Pair<Fleet, InventoryItem> ft = findTaggedFleet("CampaignMainShip4", player);
		if (ft == null) {
			if (!helper.hasTimeout("MainShip-Lost")) {
				helper.setTimeout("MainShip-Lost", 3000);
			}
			if (helper.isTimeout("MainShip-Lost")) {
				helper.gameover();
				loseGameMovie("loose/destroyed_level_3");
			}
		}
	}
	@Override
	public void save(XElement xmission) {
		xmission.set("stage", stage);
	}
	@Override
	public void load(XElement xmission) {
		stage = M25.valueOf(xmission.get("stage", M25.NONE.toString()));
	}
	@Override
	public void reset() {
		stage = M25.NONE;
	}
}
