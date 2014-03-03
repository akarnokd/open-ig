/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Action0;
import hu.openig.model.BattleInfo;
import hu.openig.model.GroundwarWorld;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchType;
import hu.openig.model.SpacewarWorld;
import hu.openig.utils.XElement;

/**
 * Mission 24: First contact with the Dargslan.
 * @author akarnokd, 2012.03.02.
 */
public class Mission24 extends Mission {
	/** Mission stages. */
	enum M24 {
		/** Not started. */
		NONE,
		/** Running. */
		RUN,
		/** Done. */
		DONE
	}
	/** The mission stage. */
	M24 stage = M24.NONE;
	@Override
	public boolean applicable() {
		return world.level == 4;
	}
	@Override
	public void onTime() {
		if (stage == M24.NONE && checkMission("Mission-24")) {
			Objective m24 = objective("Mission-24");
			if (m24.state == ObjectiveState.ACTIVE) {
				showObjective(m24);
				stage = M24.RUN;
			}
		}
		if (checkTimeout("Mission-24-Promote")) {
			objective("Mission-24").visible = false;
			objective("Mission-22").visible = false;
			world.env.stopMusic();
			world.env.playVideo("interlude/level_5_intro", new Action0() {
				@Override
				public void invoke() {
					promote();
				}
			});
		}
	}
	/**
	 * Promotion action.
	 */
	void promote() {
		world.level = 5;
		world.env.playMusic();
	}
	@Override
	public void save(XElement xmission) {
		super.save(xmission);
		xmission.set("stage", stage);
	}
	@Override
	public void load(XElement xmission) {
		super.load(xmission);
		stage = M24.valueOf(xmission.get("stage", M24.NONE.toString()));
	}
	@Override
	public void reset() {
		stage = M24.NONE;
	}
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		Player dargslan = player("Dargslan");
		Player a = battle.attacker.owner;
		Player b = null;
		if (battle.getFleet() != null) {
			b = battle.getFleet().owner;
		}
		if (battle.getPlanet() != null) {
			b = battle.getPlanet().owner;
		}
		if ((a == player && b == dargslan) || (a == dargslan && b == player)) {
			missionSuccess();
		}
	}
	/**
	 * Mission success actions.
	 */
	void missionSuccess() {
		Objective o = objective("Mission-24");
		if (setObjectiveState("Mission-24", ObjectiveState.SUCCESS)) {
			o.visible = true;
			addTimeout("Mission-24-Promote", 13000);
			
			stage = M24.DONE;
		}
	}
	@Override
	public void onConquered(Planet planet, Player previousOwner) {
		if (planet.owner == player && previousOwner == player("Dargslan")) {
			missionSuccess();
		}
	}
	@Override
	public void onDeploySatellite(Planet target, Player player,
			ResearchType satellite) {
		if (target.owner != null && target.owner == player("Dargslan") && player == this.player) {
			missionSuccess();
		}
	}
	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		onAutobattleFinish(war.battle());
	}
	@Override
	public void onGroundwarFinish(GroundwarWorld war) {
		onAutobattleFinish(war.battle());
	}
}
