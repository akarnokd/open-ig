/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

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
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.SoundType;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarWorld;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.EnumSet;

/**
 * Mission 3: Escort carrier.
 * @author akarnokd, 2012.01.14.
 */
public class Mission3 extends Mission {
	@Override
	public void onTime() {
		if (world.level == 1) {
			Objective m2t1 = helper.objective("Mission-2-Task-1");
			Objective m3 = helper.objective("Mission-3");
			if (!m3.visible && m3.state == ObjectiveState.ACTIVE
					&& m2t1.state != ObjectiveState.ACTIVE
					&& !helper.hasMissionTime("Mission-3")) {
				helper.setMissionTime("Mission-3", helper.now() + 48);
			}
			if (helper.canStart("Mission-3")) {
				world.env.speed1();
				helper.setTimeout("Mission-3-Message", 3000);
				incomingMessage("Douglas-Carrier");
				helper.clearMissionTime("Mission-3");
			}
			if (helper.isTimeout("Mission-3-Message")) {
				helper.clearTimeout("Mission-3-Message");
				helper.showObjective("Mission-3");
				createCarrierTask();
			}
			if (m3.visible && m3.state == ObjectiveState.ACTIVE) {
				checkCarrierLocation();
			}
			if (helper.isMissionTime("Mission-3-Timeout")) {
				helper.clearMissionTime("Mission-3-Timeout");
				helper.setObjectiveState("Mission-3", ObjectiveState.FAILURE);
				helper.setTimeout("Mission-3-Timeout", 13000);
				
				helper.receive("Douglas-Carrier").visible = false;
				
				removeFleets();
			}
			if (helper.isTimeout("Mission-3-Timeout")) {
				loseGameMessageAndMovie("Douglas-Fire-Escort-Failed", "loose/fired_level_1");
				helper.clearTimeout("Mission-3-Timeout");
			}
			if (helper.isTimeout("Mission-3-Failed")) {
				helper.receive("Douglas-Carrier").visible = false;
				helper.setObjectiveState("Mission-3", ObjectiveState.FAILURE);
				helper.clearTimeout("Mission-3-Failed");
				incomingMessage("Douglas-Carrier-Lost");
				helper.setTimeout("Mission-3-Done", 13000);
			}
			if (helper.isMissionTime("Mission-3-Success")) {
				helper.receive("Douglas-Carrier").visible = false;
				helper.setObjectiveState("Mission-3", ObjectiveState.SUCCESS);
				helper.clearMissionTime("Mission-3-Success");
				player.changeInventoryCount(world.researches.get("Shield1"), 1);
				helper.setTimeout("Mission-3-Done", 13000);
				removeFleets();
			}
			if (helper.isTimeout("Mission-3-Done")) {
				helper.objective("Mission-3").visible = false;
				helper.clearTimeout("Mission-3-Done");
			}
		}
	}
	/** Remove the scripted fleets. */
	void removeFleets() {
		Pair<Fleet, InventoryItem> fi = findTaggedFleet("Mission-3-Pirates", player("Pirates"));
		if (fi != null) {
			world.removeFleet(fi.first);
			helper.scriptedFleets().remove(fi.first.id);
		}
		fi = findTaggedFleet("Mission-3-Carrier", player);
		if (fi != null) {
			world.removeFleet(fi.first);
			helper.scriptedFleets().remove(fi.first.id);
		}
		cleanupScriptedFleets();
	}
	/**
	 * Create a carrier moving across the screen.
	 */
	void createCarrierTask() {
		Planet naxos = planet("Naxos");
		Fleet f = createFleet(label("mission-3.escort_carrier.name"), player, naxos.x + 20, naxos.y + 40);
		f.addInventory(world.researches.get("TradersFreight1"), 1);
		for (InventoryItem ii : f.inventory) {
			ii.tag = "Mission-3-Carrier";
		}
		f.task = FleetTask.SCRIPT;
		moveToDestination(f);
		helper.scriptedFleets().add(f.id);
	}
	/**
	 * Set the target for the carrier fleet.
	 * @param f the fleet
	 */
	void moveToDestination(Fleet f) {
		Planet sansterling = planet("San Sterling");
		f.waypoints.clear();
		f.mode = FleetMode.MOVE;
		f.task = FleetTask.SCRIPT;
		f.waypoints.add(new Point2D.Double(sansterling.x - 20, sansterling.y - 40));
	}
	/**
	 * Check the carrier's location and attack it with pirate.
	 */
	void checkCarrierLocation() {
		Objective m3 = helper.objective("Mission-3");
		if (m3.state == ObjectiveState.ACTIVE 
				&& !helper.hasMissionTime("Mission-3-Timeout")
				&& !helper.hasMissionTime("Mission-3-Success")
				&& !helper.hasTimeout("Mission-3-Failed")) {
			Pair<Fleet, InventoryItem> fi = findTaggedFleet("Mission-3-Carrier", player);
			Planet sansterling = planet("San Sterling");
			
			double d = Math.hypot(fi.first.x - sansterling.x, fi.first.y - sansterling.y);
			
			if (d < 15) {
				world.env.speed1();
				world.env.computerSound(SoundType.CARRIER_UNDER_ATTACK);
				helper.setMissionTime("Mission-3-Timeout", helper.now() + 24);
				
				Fleet pf = createFleet(label("pirates.fleet_name"), 
						player("Pirates"), fi.first.x + 1, fi.first.y + 1);
				
				pf.addInventory(world.researches.get("PirateFighter"), 3);
				for (InventoryItem ii : pf.inventory) {
					ii.tag = "Mission-3-Pirates";
				}
//				pf.addInventory(world.researches.get("PirateDestroyer"), 1);
				
				helper.scriptedFleets().add(fi.first.id);
				helper.scriptedFleets().add(pf.id);
				fi.first.waypoints.clear();
			}
		}
	}
	/**
	 * Issue the specific mission changes once task is completed.
	 * @param traderSurvived did the trader survive?
	 */
	void completeMission(boolean traderSurvived) {
		Player pirates = player("Pirates");
		if (!traderSurvived) {
			helper.setTimeout("Mission-3-Failed", 3000);
			helper.clearMissionTime("Mission-3-Timeout");
		} else {
			helper.setMissionTime("Mission-3-Success", helper.now() + 9);
			helper.clearMissionTime("Mission-3-Timeout");
		}
		for (int i : new ArrayList<Integer>(helper.scriptedFleets())) {
			Fleet f = fleet(i);
			if (f != null) {
				if (f.owner == player) {
					moveToDestination(f);
				} else
				if (f.owner == pirates) {
					world.removeFleet(f);
					helper.scriptedFleets().remove(i);
				}
			} else {
				helper.scriptedFleets().remove(i);
			}
		}
	}
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		if (world.level == 1) {
			if (isMissionSpacewar(battle, "Mission-3")) {
				boolean traderSurvived = false;
				for (InventoryItem ii : new ArrayList<InventoryItem>(battle.attacker.inventory)) {
					if ("Mission-3-Carrier".equals(ii.tag)) {
						traderSurvived = true;
						battle.attacker.inventory.remove(ii);
					}
				}
				completeMission(traderSurvived);
			}
		}
	}
	@Override
	public void onAutobattleStart(BattleInfo battle) {
		if (world.level == 1) {
			if (isMissionSpacewar(battle, "Mission-3")) {
				Player pirates = player("Pirates");
				Fleet tf = null;
				Fleet pf = null;
				for (int i : helper.scriptedFleets()) {
					Fleet f = fleet(i);
					if (f.owner == player) {
						tf = f;
					} else
					if (f.owner == pirates) {
						pf = f;
					}
				}
				if (battle.targetFleet == tf) {
					battle.targetFleet = pf;
				}
				battle.attacker.inventory.addAll(tf.inventory);
			}
		}
	}
	@Override
	public void onSpacewarStart(SpacewarWorld war) {
		if (isMissionSpacewar(war.battle(), "Mission-3")) {
			BattleInfo battle = war.battle();
			Player pirates = player("Pirates");
			Fleet tf = null;
			Fleet pf = null;
			for (int i : helper.scriptedFleets()) {
				Fleet f = fleet(i);
				if (f.owner == player) {
					tf = f;
				} else
				if (f.owner == pirates) {
					pf = f;
				}
			}
			// attack on the trader
			// trader attacked
			if (battle.targetFleet == tf) {
				war.includeFleet(pf, pf.owner);
				// fix target fleet
				battle.targetFleet = pf;
			} else {
				// pirate attacked
				war.addStructures(tf.inventory, EnumSet.of(
						ResearchSubCategory.SPACESHIPS_BATTLESHIPS,
						ResearchSubCategory.SPACESHIPS_CRUISERS,
						ResearchSubCategory.SPACESHIPS_FIGHTERS));
			}
			// center trader
			Dimension d = war.space();
			for (SpacewarStructure s : war.structures(player)) {
				if (s.item != null && "Mission-3-Carrier".equals(s.item.tag)) {
					s.x = d.width / 2;
					s.y = d.height / 2;
					s.angle = 0.0;
				}
			}
			battle.allowRetreat = false;
		}
	}
	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		if (isMissionSpacewar(war.battle(), "Mission-3")) {
			// find the status of the trader ship
			boolean traderSurvived = false;
			for (SpacewarStructure s : war.structures(player)) {
				if (s.item != null && "Mission-3-Carrier".equals(s.item.tag)) {
					traderSurvived = true;
					break;
				}
			}
			completeMission(traderSurvived);
			if (traderSurvived) {
				war.battle().rewardText = label("mission-3.reward");
				war.battle().messageText = label("battlefinish.mission-3.11");
			}
//			war.battle().rewardImage = imageReward[task];
		}
	}
}
