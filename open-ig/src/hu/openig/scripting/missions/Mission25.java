/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Action0;
import hu.openig.model.DiplomaticRelation;
import hu.openig.model.Fleet;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
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
			showObjective("Mission-25");
			stage = M25.RUNNING;
		}
		if (stage == M25.RUNNING) {
			if (player("Dargslan").statistics.planetsOwned.value == 0) {
				stage = M25.DONE;
				setObjectiveState("Mission-25", ObjectiveState.SUCCESS);
				addTimeout("Win", 6000);
			}
		}
		if (checkTimeout("Win")) {
			objective("Mission-25").visible = false;
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
	@Override
	public void onLevelChanged() {
		if (world.level == 5) {
			String a = "achievement.grand_admiral";
			world.achievement(a);
			
			// make sure the dargslan hate the player the most.
			DiplomaticRelation dr = world.getRelation(player, player("Dargslan"));
			dr.value = 1;
		}
	}
	/** Check if the main ship is still operational. */
	void checkMainShip() {
		Fleet ft = findTaggedFleet("CampaignMainShip4", player);
		if (ft == null) {
			if (!hasTimeout("MainShip-Lost")) {
				addTimeout("MainShip-Lost", 3000);
			}
			if (checkTimeout("MainShip-Lost")) {
				gameover();
				loseGameMovie("lose/destroyed_level_3");
			}
		}
	}
	@Override
	public void save(XElement xmission) {
		super.save(xmission);
		xmission.set("stage", stage);
	}
	@Override
	public void load(XElement xmission) {
		super.load(xmission);
		stage = M25.valueOf(xmission.get("stage", M25.NONE.toString()));
	}
	@Override
	public void reset() {
		stage = M25.NONE;
	}
	@Override
	public void onPlanetInfected(Planet planet) {
		if (planet.owner == player) {
			String msgId = planet.id + "-Virus";
			if (hasReceive(msgId)) {
				incomingMessage(msgId, (Action0)null);
			}
		}
	}
}
