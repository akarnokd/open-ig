/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.model.Building;
import hu.openig.model.Fleet;
import hu.openig.model.Message;
import hu.openig.model.ModelUtils;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.SoundType;

import java.util.HashSet;
import java.util.Set;


/**
 * Activities of Mission 1.
 * @author akarnokd, 2012.01.14.
 */
public class Mission1 extends Mission {
	@Override
	public void onTime() {
		send("Naxos-Check").visible = true;
		send("San Sterling-Check").visible = true;
		checkMission1Start();
		checkMission1Task1();
		checkMission1Task2();
		checkMission1Complete();
		checkMainShip();
	}
	/** Check if the colony hub was completed. */
	void checkMission1Task1() {
		Objective o = objective("Mission-1-Task-1");
		if (o.isActive()) {
			for (Building b : planet("Achilles").surface.buildings.findByKind("MainBuilding")) {
				if (b.isComplete()) {
					setObjectiveState(o, ObjectiveState.SUCCESS);
				}
			}
		}		
	}
	/**
	 * Check if Achilles contains the required types of undamaged, operational buildings for Mission 1 Task 2.
	 */
	void checkMission1Task2() {
		Objective m1t2 = objective("Mission-1-Task-2");
		if (m1t2.state != ObjectiveState.ACTIVE) {
			return;
		}
		Planet p = planet("Achilles");
		String[][] buildingSets = {
				{ "PrefabHousing", "ApartmentBlock", "Arcology" },
				{ "NuclearPlant", "FusionPlant", "SolarPlant" },
				{ "CivilDevCenter", "MechanicalDevCenter", "ComputerDevCenter", "AIDevCenter", "MilitaryDevCenter" },
				{ "PoliceStation" },
				{ "FireBrigade" }, 
				{ "MilitarySpaceport" },
				{ "RadarTelescope", "FieldTelescope", "PhasedTelescope" }, 
		};
		boolean okay = true;
		Set<String> buildingTypes = new HashSet<>();
		for (Building b : p.surface.buildings.iterable()) {
			if (b.isOperational() /* && !b.isDamaged() */) {
				buildingTypes.add(b.type.id);
			}
		}
		if (okay) {
			for (String[] bts : buildingSets) {
				boolean found = false;
				for (String bt : bts) {
					if (buildingTypes.contains(bt)) {
						found = true;
						break;
					}
				}
				if (!found) {
					okay = false;
					break;
				}
			}
			if (okay) {
				m1t2.visible = true;
				setObjectiveState(m1t2, ObjectiveState.SUCCESS);
			}
		}
	}

	/**
	 * Check if either Naxos or San Sterling was lost.
	 * @param planet the event planet
	 */
	void checkMission1Task3Failure(Planet planet) {
		if (planet.id.equals("Naxos") || planet.id.equals("San Sterling")) {
			Objective o = objective("Mission-1-Task-3");
			if (o.visible) {
				if (setObjectiveState(o, ObjectiveState.FAILURE)) {
					gameover();
					addTimeout("Mission-1-Failure", 5000);
				}
			}
		}
	}
	/**
	 * Check if either Naxos or San Sterling was lost.
	 * @param planet the event planet
	 */
	void checkMission1Task4Failure(Planet planet) {
		if (planet.id.equals("Achilles")) {
			Objective o = objective("Mission-1-Task-4");
			if (o.visible) {
				if (setObjectiveState(o, ObjectiveState.FAILURE)) {
					gameover();
					addTimeout("Mission-1-Failure", 5000);
				}
			}
		}
	}
	/**
	 * Check Mission 1 start condition and start the mission.
	 */
	void checkMission1Start() {
		if (checkTimeout("Mission-1-Init")) {
			objective("Mission-1-Task-1").visible = true;
			objective("Mission-1-Task-2").visible = true;
			showObjective("Mission-1");

			addMission("Mission-1-Task-3", 72);
			
			int mission2Time = 3 * (ModelUtils.randomInt(2) + 3);
			addMission("Mission-2", mission2Time);
			addTimeout("Welcome", 1500);
		}
		if (checkTimeout("Welcome")) {
			Message msg = new Message();
			msg.priority = 100;
			msg.gametime = world.time.getTimeInMillis();
			msg.timestamp = System.currentTimeMillis();
			msg.sound = SoundType.WELCOME;
			msg.text = "welcome";
			
			player.addMessage(msg);
		}
		if (checkMission("Mission-1-Task-3")) {
			showObjective("Mission-1-Task-3");
		}
	}
	/**
	 * Check if mission 1 was completed.
	 */
	void checkMission1Complete() {
		final Objective o0 = objective("Mission-1");
		final Objective o1 = objective("Mission-1-Task-1");
		final Objective o2 = objective("Mission-1-Task-2");
		
		if (o0.visible && o1.state == ObjectiveState.SUCCESS && o2.state == ObjectiveState.SUCCESS) {
			if (o0.state == ObjectiveState.ACTIVE) {
				setObjectiveState(o0, ObjectiveState.SUCCESS);
				addTimeout("Mission-1-Success", 13000);
				world.achievement("achievement.bela_the_4th");
			}
			
		}
		if (checkTimeout("Mission-1-Success")) {
			o0.visible = false;
			o1.visible = false;
			o2.visible = false;
			showObjective("Mission-1-Task-4");
			send("San Sterling-Check").visible = true;
			send("Achilles-Check").visible = true;
			
		} else
		if (checkTimeout("Mission-1-Failure")) {
			loseGameMessageAndMovie("Douglas-Fire-Lost-Planet", "lose/fired_level_1");
		}
	}
	/**
	 * Check if Achilles was lost.
	 * @param planet the planet
	 */
	void checkMission1Failure(Planet planet) {
		if (planet.id.equals("Achilles")) {
			Objective o = objective("Mission-1");
			if (o.visible) {
				if (setObjectiveState(o, ObjectiveState.FAILURE)) {
					gameover();
					addTimeout("Mission-1-Failure", 5000);
				}
			}
		}
	}
	@Override
	public void onConquered(Planet planet, Player previousOwner) {
		if (previousOwner == player) {
			checkMission1Failure(planet);
			checkMission1Task3Failure(planet);
			checkMission1Task4Failure(planet);
		}
	}
	@Override
	public void onLost(Planet planet) {
		checkMission1Failure(planet);
		checkMission1Task3Failure(planet);
		checkMission1Task4Failure(planet);
	}
	/**
	 * Check if the main ship still exists.
	 */
	void checkMainShip() {
		Fleet ft = findTaggedFleet("CampaignMainShip1", player);
		if (ft == null) {
			if (!hasTimeout("MainShip-Lost")) {
				addTimeout("MainShip-Lost", 0);
			}
			if (checkTimeout("MainShip-Lost")) {
				gameover();
				loseGameMovie("lose/destroyed_level_1");
			}
		}
	}
	@Override
	public void onLevelChanged() {
		// start over
		if (world.level == 1) {
			removeMissions(1, 25);
			addTimeout("Mission-1-Init", 2000); // 8000
			send("Douglas-Reinforcements-Denied").visible = true;
		}
	}
	@Override
	public boolean applicable() {
		return world.level == 1;
	}
}
