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
import hu.openig.model.BattleInfo;
import hu.openig.model.Fleet;
import hu.openig.model.FleetMode;
import hu.openig.model.FleetTask;
import hu.openig.model.InventoryItem;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarWorld;

import java.awt.geom.Point2D;

/**
 * Mission 9: deal with the with the San Sterling smuggler.
 * @author akarnokd, Jan 22, 2012
 */
public class Mission9 extends Mission {
	@Override
	public void onTime() {
		if (world.level != 2) {
			return;
		}
		Objective m7t1 = helper.objective("Mission-7-Task-1");
		Objective m9 = helper.objective("Mission-9");
		if (m7t1.state != ObjectiveState.ACTIVE
				&& !m9.visible && m9.state == ObjectiveState.ACTIVE
				&& !helper.hasTimeout("Mission-9-Message")
				&& !helper.hasMissionTime("Mission-9-Interlude")
				&& !helper.hasTimeout("Mission-9-Objective")
				&& !helper.hasMissionTime("Mission-9")) {
			helper.setMissionTime("Mission-9", helper.now() + 3 * 24);
		}
		if (checkMission("Mission-9")) {
			helper.setMissionTime("Mission-9-Interlude", helper.now() + 1);
			world.env.stopMusic();
			world.env.playVideo("interlude/merchant_in", new Action0() {
				@Override
				public void invoke() {
					world.env.playMusic();
					helper.setTimeout("Mission-9-Message", 1000);
					helper.clearMissionTime("Mission-9-Interlude");
					world.env.speed1();
				}
			});
		}
		if (checkTimeout("Mission-9-Message")) {
			incomingMessage("San Sterling-Smuggler");
		}
		if (checkTimeout("Mission-9-Objective")) {
			helper.showObjective("Mission-9");
			createSmuggler();
		}
		if (checkTimeout("Mission-9-Slipped")) {
			helper.receive("San Sterling-Smuggler").visible = false;
			helper.setObjectiveState("Mission-9", ObjectiveState.FAILURE);
			incomingMessage("San Sterling-Smuggler-Escaped");
			helper.setTimeout("Mission-9-Hide", 30000);
		}
		if (checkTimeout("Mission-9-Innocent")) {
			helper.receive("San Sterling-Smuggler").visible = false;
			helper.setObjectiveState("Mission-9", ObjectiveState.FAILURE);
			helper.gameover();
			loseGameMessageAndMovie("San Sterling-Smuggler-Killed-Innocent", "loose/fired_level_2");
		}
		if (checkTimeout("Mission-9-Killed")) {
			helper.receive("San Sterling-Smuggler").visible = false;
			helper.setObjectiveState("Mission-9", ObjectiveState.SUCCESS);
			incomingMessage("San Sterling-Smuggler-Killed");
		}
		if (checkTimeout("Mission-9-Success")) {
			helper.receive("San Sterling-Smuggler").visible = false;
			helper.setObjectiveState("Mission-9", ObjectiveState.SUCCESS);
			helper.setTimeout("Mission-9-Hide", 13000);
			helper.setMissionTime("Mission-9-Hide-Fleet", helper.now() + 12);
		}
		if (checkTimeout("Mission-9-Hide")) {
			helper.objective("Mission-9").visible = false;
			helper.receive("San Sterling-Smuggler-Escaped").visible = false;
			helper.receive("San Sterling-Smuggler-Killed-Innocent").visible = false;
			helper.receive("San Sterling-Smuggler-Killed").visible = false;
		}
		if (checkMission("Mission-9-Hide-Fleet")) {
			Pair<Fleet, InventoryItem> smg = findTaggedFleet("Mission-9-Smuggler", player("Traders"));
			if (smg != null) {
				world.removeFleet(smg.first);
				helper.scriptedFleets().remove(smg.first.id);
			}
		}
	}
	/** Create the smuggler's ship. */
	void createSmuggler() {
		Planet sst = planet("San Sterling");
		// create simple pirate fleet
		Fleet pf = createFleet(label("mission-9.trader_name"), 
				player("Traders"), sst.x + 80, sst.y + 80);
		pf.task = FleetTask.SCRIPT;
		int n = world.random().nextInt(2) + 1;
		// ----------------------------------------------------------------
		pf.addInventory(research("TradersFreight" + n), 1);
		// ----------------------------------------------------------------
		for (InventoryItem ii : pf.inventory) {
			ii.tag = "Mission-9-Smuggler";
		}
		pf.mode = FleetMode.MOVE;
		pf.targetPlanet(sst);

		helper.scriptedFleets().add(pf.id);
	}
	@Override
	public void onMessageSeen(String id) {
		if (world.level != 2) {
			return;
		}
		if ("San Sterling-Smuggler".equals(id)) {
			if (findTaggedFleet("Mission-9-Smuggler", player("Traders")) == null) {
				helper.setTimeout("Mission-9-Objective", 1000);
			}
		}
		if (helper.objective("Mission-9").visible) {
			if ("San Sterling-Smuggler-Escaped".equals(id)) {
				helper.setTimeout("Mission-9-Hide", 3000);
			} else
			if ("San Sterling-Smuggler-Killed".equals(id)) {
				helper.setTimeout("Mission-9-Hide", 3000);
			}
		}		
	}
	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		if (world.level != 2) {
			return;
		}
		if (helper.isActive("Mission-9") 
				&& war.battle().attacker.owner == player 
				&& war.battle().targetFleet != null) {
			Player traders = player("Traders");
			// check for losses
			boolean smugglerKilled = false;
			boolean innocentKilled = false;
			boolean diverted = false;
			boolean slipped = false;
			
			for (SpacewarStructure s : war.battle().spaceLosses) {
				if (s.item != null && "Mission-9-Smuggler".equals(s.item.tag)) {
					smugglerKilled = true;
				} else
				if (s.owner == traders) {
					innocentKilled = true;
				}
			}
			
			Pair<Fleet, InventoryItem> smg = findTaggedFleet("Mission-9-Smuggler", traders);
			if (smg != null) {
				InventoryItem ii = smg.second;
				if (ii.hp * 2 < world.getHitpoints(ii.type)) {
					diverted = true;
				} else {
					slipped = true;
				}
			}
			
			Planet sst = planet("San Sterling");
			if (smugglerKilled) {
				helper.setTimeout("Mission-9-Killed", 3000);
			} else
			if (innocentKilled) {
				helper.setTimeout("Mission-9-Innocent", 3000);
			} else
			if (diverted) {
				helper.setTimeout("Mission-9-Success", 3000);
				war.battle().messageText = label("battlefinish.mission-9.17");
				smg.first.targetPlanet(null);
				smg.first.waypoints.clear();
				smg.first.waypoints.add(new Point2D.Double(sst.x + 80, sst.y + 80));
				smg.first.task = FleetTask.SCRIPT;
			} else
			if (slipped) {
				// resume flight
				smg.first.targetPlanet(sst);
				smg.first.task = FleetTask.SCRIPT;
			}
		}
	}
	@Override
	public void onFleetAt(Fleet fleet, Planet planet) {
		if (world.level != 2) {
			return;
		}
		if (helper.isActive("Mission-9")) {
			if (planet.id.equals("San Sterling") 
					&& hasTag(fleet, "Mission-9-Smuggler")) {
				world.removeFleet(fleet);
				helper.scriptedFleets().remove(fleet.id);
				helper.setObjectiveState("Mission-9", ObjectiveState.FAILURE);
				helper.setTimeout("Mission-9-Slipped", 13000);
			}
		}
	}
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		if (world.level != 2) {
			return;
		}
		if (isMissionSpacewar(battle, "Mission-9")) {
			Player traders = player("Traders");
			if (battle.targetFleet != null && battle.targetFleet.owner == traders) {
				Pair<Fleet, InventoryItem> smg = findTaggedFleet("Mission-9-Smuggler", 
						traders);
				if (smg == null) {
					helper.setTimeout("Mission-9-Killed", 3000);
				} else {
					helper.setTimeout("Mission-9-Innocent", 3000);
				}
			}
		}
	}
}
