/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Action0;
import hu.openig.model.Fleet;
import hu.openig.model.FleetMode;
import hu.openig.model.FleetTask;
import hu.openig.model.InventoryItem;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;

/**
 * Mission 10: Escort centronom governor.
 * @author akarnokd, Jan 22, 2012
 */
public class Mission10 extends Mission {
	@Override
	public void onTime() {
		if (world.level != 2) {
			return;
		}
		Objective m9 = helper.objective("Mission-9");
		Objective m10 = helper.objective("Mission-10");
		if (m9.state != ObjectiveState.ACTIVE
				&& m10.state == ObjectiveState.ACTIVE
				&& !helper.hasMissionTime("Mission-10-Timeout")
				&& !helper.hasMissionTime("Mission-10")) {
			helper.setMissionTime("Mission-10", helper.now() + 3 * 24);
		}
		if (checkMission("Mission-10")) {
			incomingMessage("Douglas-Centronom-Governor");
			helper.setMissionTime("Mission-10-Timeout", helper.now() + 24);
		}
		if (checkTimeout("Mission-10-Hide")) {
			helper.objective("Mission-10").visible = false;
		}
		if (checkTimeout("Mission-10-Objective")) {
			world.env.stopMusic();
			world.env.playVideo("interlude/colony_ship_arrival", new Action0() {
				@Override
				public void invoke() {
					world.env.playMusic();
					helper.showObjective("Mission-10");
					helper.clearMissionTime("Mission-10-Timeout");
					world.env.speed1();
					createGovernor();
				}
			});
		}
	}
	/**
	 * Create the governor's fleet.
	 */
	void createGovernor() {
		Planet sst = planet("San Sterling");
		Planet nax = planet("Naxos");
		// create simple pirate fleet
		Fleet pf = createFleet(label("mission-10.governor_name"), 
				player, sst.x, sst.y);
		pf.task = FleetTask.SCRIPT;
		// ----------------------------------------------------------------
		pf.addInventory(research("ColonyShip"), 1);
		pf.addInventory(research("TradersFreight1"), 1);
		// ----------------------------------------------------------------
		for (InventoryItem ii : pf.inventory) {
			ii.tag = "Mission-10-Governor";
		}
		pf.mode = FleetMode.MOVE;
		pf.targetPlanet(nax);

		helper.scriptedFleets().add(pf.id);

	}
	@Override
	public void onFleetAt(Fleet fleet, Planet planet) {
		if (world.level != 2) {
			return;
		}
		if (helper.isActive("Mission-10")) {
			if (planet.id.equals("Naxos") 
					&& hasTag(fleet, "Mission-10-Governor")) {
				world.removeFleet(fleet);
				helper.scriptedFleets().remove(fleet.id);
				helper.setObjectiveState("Mission-10", ObjectiveState.SUCCESS);
				helper.setTimeout("Mission-10-Hide", 13000);
				helper.receive("Douglas-Centronom-Governor").visible = false;
			}
		}
	}
	@Override
	public void onMessageSeen(String id) {
		if (world.level != 2) {
			return;
		}
		if ("Douglas-Centronom-Governor".equals(id)) {
			if (findTaggedFleet("Mission-10-Governor", player) == null) {
				helper.setTimeout("Mission-10-Objective", 1000);
			}
		}
	}
}
