/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.model.BattleInfo;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.SpacewarWorld;
import hu.openig.utils.XElement;

/**
 * Mission 12: New Caroline virus infection.
 * @author akarnokd, Feb 4, 2012
 */
public class Mission12 extends Mission {
	/** The stages. */
	public enum M12Stages {
		/** Not started yet. */
		NONE,
		/** Waiting for the first action. */
		INITIAL_DELAY,
		/** Incoming message. */
		FIRST_MESSAGE,
		/** First rundown. */
		FIRST_RUNDOWN,
		/** Waiting for subsequent infections. */
		SUBSEQUENT_DELAY,
		/** Infection rundown. */
		SUBSEQUENT_MESSAGE,
		/** Subsequent rundown. */
		SUBSEQUENT_RUNDOWN,
		/** Mission done. */
		DONE
	}
	/** The mission stages. */
	protected M12Stages stage = M12Stages.NONE;
	/** If traders were killed. */
	protected boolean tradersLost;
	@Override
	public void onTime() {
		Objective m11t1 = helper.objective("Mission-11");
		if (m11t1.state != ObjectiveState.ACTIVE) {
			if (stage == M12Stages.NONE) {
				helper.setMissionTime("Mission-12", helper.now() + 12);
				stage = M12Stages.INITIAL_DELAY;
			}
		}
		if (checkMission("Mission-12")) {
			world.env.speed1();
			incomingMessage("New Caroline-Garthog-Virus");
			stage = M12Stages.FIRST_MESSAGE;
			planet("New Caroline").quarantineTTL = Planet.DEFAULT_QUARANTINE_TTL;
		}
		Objective m12 = helper.objective("Mission-12");
		if (checkMission("Mission-12-Subsequent") 
				&& m12.state == ObjectiveState.ACTIVE) {
			Objective m14 = helper.objective("Mission-14");
			if (!helper.hasMissionTime("Mission-14")
					&& (!m14.visible && m14.state == ObjectiveState.ACTIVE)) {
				world.env.speed1();
				incomingMessage("New Caroline-Garthog-Virus-Again");
				stage = M12Stages.SUBSEQUENT_MESSAGE;
				planet("New Caroline").quarantineTTL = Planet.DEFAULT_QUARANTINE_TTL;
			}
		}
		if (checkMission("Mission-12-Hide")) {
			helper.objective("Mission-12").visible = false;
			helper.receive("New Caroline-Garthog-Virus").visible = false;
			helper.receive("New Caroline-Garthog-Virus-Again").visible = false;
			helper.receive("New Caroline-Garthog-Virus-Resolved").visible = false;
			helper.receive("New Caroline-Garthog-Virus-Again-Deaths").visible = false;
		}
		if (checkTimeout("Mission-12-Task-6-Hide")) {
			helper.objective("Mission-12-Task-6").visible = false;
		}
		if (checkMission("Mission-12-TaskSuccess")) {
			completeActiveTask();
		}
		if (stage == M12Stages.FIRST_MESSAGE
				|| stage == M12Stages.SUBSEQUENT_MESSAGE
				|| stage == M12Stages.FIRST_RUNDOWN
				|| stage == M12Stages.SUBSEQUENT_RUNDOWN
		) {
			helper.receive("New Caroline-Virus").visible = false;
		}
	}
	@Override
	public void onMessageSeen(String id) {
		if (stage == M12Stages.FIRST_MESSAGE) {
			if ("New Caroline-Garthog-Virus".equals(id)) {
				helper.showObjective("Mission-12");
				helper.showObjective("Mission-12-Task-1");
				stage = M12Stages.FIRST_RUNDOWN;
			}
		}
		if (stage == M12Stages.SUBSEQUENT_MESSAGE) {
			if ("New Caroline-Garthog-Virus-Again".equals(id)) {
				helper.showObjective("Mission-12");
				for (int i = 2; i < 6; i++) {
					Objective o = helper.objective("Mission-12-Task-" + i);
					if (!o.visible && o.state == ObjectiveState.ACTIVE) {
						helper.showObjective("Mission-12-Task-" + i);
						break;
					}
				}				
				stage = M12Stages.SUBSEQUENT_RUNDOWN;
			}
		}
		if ("New Caroline-Garthog-Virus-Resolved".equals(id)
				|| "New Caroline-Garthog-Virus-Again-Deaths".equals(id)) {
			completeActiveTask();				
		}
		if ("Douglas-Report-Viruses".equals(id)) {
			helper.setMissionTime("Mission-14", helper.now() + 8);
			helper.setObjectiveState("Mission-12-Task-6", ObjectiveState.SUCCESS);
			helper.setTimeout("Mission-12-Task-6-Hide", 13000);
		}
	}
	/**
	 * Complete the active task.
	 */
	void completeActiveTask() {
		for (int i = 5; i >= 1; i--) {
			Objective o = helper.objective("Mission-12-Task-" + i);
			if (o.visible && o.state == ObjectiveState.ACTIVE) {
				helper.setObjectiveState("Mission-12-Task-" + i, ObjectiveState.SUCCESS);
				if (helper.hasMissionTime("Mission-12-TaskSuccess")) {
					helper.clearMissionTime("Mission-12-TaskSuccess");
				}
				if (i >= 2) {
					helper.send("Douglas-Report-Viruses").visible = true;
				}
				if (i == 5) {
					helper.showObjective("Mission-12-Task-6");
				}
				break;
			}
		}
	}
	@Override
	public void onLost(Planet planet) {
		for (int i = 1; i < 6; i++) {
			Objective o = helper.objective("Mission-12-Task-" + i);
			if (o.visible && o.state == ObjectiveState.ACTIVE) {
				helper.setObjectiveState(o, ObjectiveState.FAILURE);
			}
			helper.setObjectiveState("Mission-12", ObjectiveState.FAILURE);
		}
		helper.gameover();
		loseGameMessageAndMovie("Douglas-Fire-Lost-Planet-2", "loose/fired_level_2");
	}
	@Override
	public void onPlanetCured(Planet planet) {
		if (stage == M12Stages.FIRST_RUNDOWN) {
			if (planet.id.equals("New Caroline")) {
				helper.receive("New Caroline-Garthog-Virus").visible = false;
				incomingMessage("New Caroline-Garthog-Virus-Resolved");
				helper.setMissionTime("Mission-12-Subsequent", helper.now() + 24);
				helper.setMissionTime("Mission-12-TaskSuccess", helper.now() + 2);
				stage = M12Stages.SUBSEQUENT_DELAY;
				helper.setMissionTime("Mission-12-Hide", helper.now() + 3);
			}
		}
		if (stage == M12Stages.SUBSEQUENT_RUNDOWN) {
			if (planet.id.equals("New Caroline")) {
				helper.receive("New Caroline-Garthog-Virus-Again").visible = false;
				if (tradersLost) {
					incomingMessage("New Caroline-Garthog-Virus-Again-Deaths");
				} else {
					incomingMessage("New Caroline-Garthog-Virus-Resolved");
				}
				for (int i = 5; i >= 2; i--) {
					Objective o = helper.objective("Mission-12-Task-" + i);
					if (o.visible && o.state == ObjectiveState.ACTIVE) {
						if (i < 5) {
							helper.setMissionTime("Mission-12-Subsequent", helper.now() + 24);
							stage = M12Stages.SUBSEQUENT_DELAY;
						} else {
							stage = M12Stages.DONE;
						}
						helper.setMissionTime("Mission-12-TaskSuccess", helper.now() + 2);
						break;
					}
				}				
				helper.setMissionTime("Mission-12-Hide", helper.now() + 3);
			}			
		}
	}
	@Override
	public void onPlanetInfected(Planet planet) {
		if (stage == M12Stages.FIRST_RUNDOWN 
				|| stage == M12Stages.SUBSEQUENT_RUNDOWN) {
			if (!planet.id.equals("New Caroline")) {
				helper.gameover();
				loseGameMessageAndMovie("New Caroline-Garthog-Virus-Breached", "loose/fired_level_2");
			}
		}
	}
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		if (stage == M12Stages.FIRST_RUNDOWN 
				|| stage == M12Stages.SUBSEQUENT_RUNDOWN) {
			if (battle.targetFleet != null 
					&& battle.targetFleet.owner == player("Traders")
					&& battle.targetFleet.inventory.isEmpty()) {
				tradersLost = true;
			}
		}
	}
	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		onAutobattleFinish(war.battle());
	}
	@Override
	public boolean applicable() {
		return world.level == 2;
	}
	@Override
	public void load(XElement xmission) {
		stage = M12Stages.valueOf(xmission.get("stage"));
		tradersLost = xmission.getBoolean("traders-lost");
	}
	@Override
	public void save(XElement xmission) {
		xmission.set("stage", stage);
		xmission.set("traders-lost", tradersLost);
	}
	@Override
	public void reset() {
		tradersLost = false;
		stage = M12Stages.NONE;
		super.reset();
	}
}
