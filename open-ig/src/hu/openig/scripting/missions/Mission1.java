/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import java.util.Set;

import hu.openig.core.Pair;
import hu.openig.model.Building;
import hu.openig.model.Fleet;
import hu.openig.model.InventoryItem;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.utils.U;


/**
 * Activities of Mission 1.
 * @author akarnokd, 2012.01.14.
 */
public class Mission1 extends Mission {
	@Override
	public void onTime() {
		if (world.level == 1) {
			helper.send("Naxos-Check").visible = true;
			helper.send("San Sterling-Check").visible = true;
			checkMission1Start();
			checkMission1Task2();
			checkMission1Complete();
		}
	}
	@Override
	public void onBuildingComplete(Planet planet, Building building) {
		if (world.level == 1) {
			checkMission1Task1(planet, building);
		}
	}
	/**
	 * Check if the colony hub was built on Achilles.
	 * @param planet the event planet
	 * @param building the event building
	 */
	void checkMission1Task1(Planet planet, Building building) {
		// Mission 1, Task 1: Build a Colony Hub
		if (planet.id.equals("Achilles") && building.type.kind.equals("MainBuilding")) {
			Objective o = helper.objective("Mission-1-Task-1");
			if (o.visible) {
				helper.setObjectiveState(o, ObjectiveState.SUCCESS);
			}
		}
	}
	/**
	 * Check if Achilles contains the required types of undamaged, operational buildings for Mission 1 Task 2.
	 */
	void checkMission1Task2() {
		Objective m1t2 = helper.objective("Mission-1-Task-2");
		if (!m1t2.visible) {
			return;
		}
		Planet p = planet("Achilles");
		String[][] buildingSets = {
				{ "PrefabHousing", "ApartmentBlock", "Arcology" },
				{ "NuclearPlant", "FusionPlant", "SolarPlant" },
				{ "CivilDevCenter", "MechanicalDevCenter", "ComputerDevCenter", "AIDevCenter", "MilitaryDevCenter" },
				{ "Police" },
				{ "FireBrigade" }, 
				{ "MilitarySpaceport" },
				{ "Radar1", "Radar2", "Radar3" }, 
		};
		boolean okay = true;
		Set<String> buildingTypes = U.newHashSet();
		for (Building b : p.surface.buildings) {
			if (!b.isOperational() || b.isDamaged()) {
				okay = false;
				break;
			} else {
				buildingTypes.add(b.type.id);
			}
		}
		if (okay) {
			for (String[] bts : buildingSets) {
				boolean found = true;
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
				helper.setObjectiveState(m1t2, ObjectiveState.SUCCESS);
			}
		}
	}

	/**
	 * Check if either Naxos or San Sterling was lost.
	 * @param planet the event planet
	 */
	void checkMission1Task3Failure(Planet planet) {
		if (planet.id.equals("Naxos") || planet.id.equals("San Sterling")) {
			Objective o = helper.objective("Mission-1-Task-3");
			if (o.visible) {
				if (helper.setObjectiveState(o, ObjectiveState.FAILURE)) {
					helper.gameover();
					helper.setTimeout("Mission-1-Failure", 5000);
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
			Objective o = helper.objective("Mission-1-Task-4");
			if (o.visible) {
				if (helper.setObjectiveState(o, ObjectiveState.FAILURE)) {
					helper.gameover();
					helper.setTimeout("Mission-1-Failure", 5000);
				}
			}
		}
	}
	/**
	 * Check Mission 1 start condition and start the mission.
	 */
	void checkMission1Start() {
		Objective o0 = helper.objective("Mission-1");
		if (!o0.visible && o0.state == ObjectiveState.ACTIVE && helper.isTimeout("Mission-1-Init")) {
			helper.objective("Mission-1-Task-1").visible = true;
			helper.objective("Mission-1-Task-2").visible = true;
			helper.objective("Mission-1-Task-3").visible = true;
			helper.showObjective("Mission-1");
			helper.clearTimeout("Mission-1-Init");
			
			int mission2Time = 24 * (world.random().nextInt(2) + 7);
			helper.setMissionTime("Mission-2", mission2Time);
		}
	}
	/**
	 * Check if mission 1 was completed.
	 */
	void checkMission1Complete() {
		final Objective o0 = helper.objective("Mission-1");
		final Objective o1 = helper.objective("Mission-1-Task-1");
		final Objective o2 = helper.objective("Mission-1-Task-2");
		
		if (o0.visible && o1.state == ObjectiveState.SUCCESS && o2.state == ObjectiveState.SUCCESS) {
			if (o0.state == ObjectiveState.ACTIVE) {
				helper.setObjectiveState(o0, ObjectiveState.SUCCESS);
				helper.setTimeout("Mission-1-Success", 13000);
			}
			
		}
		if (o0.state == ObjectiveState.SUCCESS && helper.isTimeout("Mission-1-Success")) {
			o0.visible = false;
			o1.visible = false;
			o2.visible = false;
			helper.showObjective("Mission-1-Task-4");
			
			helper.clearTimeout("Mission-1-Success");
		} else
		if (o0.state == ObjectiveState.FAILURE && helper.isTimeout("Mission-1-Failure")) {
			loseGameMessageAndMovie("Douglas-Fire-Lost-Planet", "loose/fired_level_1");
			helper.clearTimeout("Mission-1-Failure");
		}
	}
	/**
	 * Check if Achilles was lost.
	 * @param planet the planet
	 */
	void checkMission1Failure(Planet planet) {
		if (planet.id.equals("Achilles")) {
			Objective o = helper.objective("Mission-1");
			if (o.visible) {
				if (helper.setObjectiveState(o, ObjectiveState.FAILURE)) {
					helper.gameover();
					helper.setTimeout("Mission-1-Failure", 5000);
				}
			}
		}
	}
	@Override
	public void onConquered(Planet planet, Player previousOwner) {
		if (world.level == 1 && previousOwner == player) {
			checkMission1Failure(planet);
			checkMission1Task3Failure(planet);
			checkMission1Task4Failure(planet);
		}
	}
	@Override
	public void onLost(Fleet fleet) {
		if (world.level == 1 && fleet.owner == player) {
			Pair<Fleet, InventoryItem> ft = findTaggedFleet("CampaignMainShip1", player);
			if (ft == null) {
				helper.gameover();
				loseGameMovie("loose/destroyed_level_1");
			}
		}
	}
	@Override
	public void onLost(Planet planet) {
		if (world.level == 1) {
			checkMission1Failure(planet);
			checkMission1Task3Failure(planet);
			checkMission1Task4Failure(planet);
		}
	}
	@Override
	public void onNewGame() {
		helper.setTimeout("Mission-1-Init", 8000);
	}
}
