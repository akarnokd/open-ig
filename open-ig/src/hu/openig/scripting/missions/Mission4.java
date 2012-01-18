/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Pair;
import hu.openig.model.AIMode;
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
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarWorld;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Mission 4: Resolve pirate battle.
 * @author akarnokd, 2012.01.18.
 */
public class Mission4 extends Mission {
	@Override
	public void onTime() {
		if (world.level == 1) {
			Objective m2t1 = helper.objective("Mission-2-Task-2");
			Objective m4 = helper.objective("Mission-4");
			if (!m4.visible && m4.state == ObjectiveState.ACTIVE
					&& m2t1.state != ObjectiveState.ACTIVE
					&& !helper.hasMissionTime("Mission-4")) {
				helper.setMissionTime("Mission-4", helper.now() + 48);
			}
			if (helper.canStart("Mission-4")) {
				world.env.speed1();
				helper.setTimeout("Mission-4-Message", 3000);
				helper.clearMissionTime("Mission-4");
				helper.setMissionTime("Mission-4-Timeout", helper.now() + 24);
				helper.send("Naxos-Not-Under-Attack").visible = false;
				incomingMessage("Naxos-Unknown-Ships");
			}
			if (helper.isTimeout("Mission-4-Message")) {
				helper.clearTimeout("Mission-4-Message");
				helper.showObjective("Mission-4");
				createPirateTask();
			}
			
			if (helper.isMissionTime("Mission-4-Timeout")) {
				helper.clearMissionTime("Mission-4-Timeout");

				helper.send("Naxos-Not-Under-Attack").visible = true;
				helper.receive("Naxos-Unknown-Ships").visible = false;
				
				helper.setObjectiveState("Mission-4", ObjectiveState.FAILURE);
				removeFleets();
				helper.setTimeout("Mission-4-Fire", 13000);
			}
			if (helper.isTimeout("Mission-4-Fire")) {
				helper.clearTimeout("Mission-4-Fire");
				helper.gameover();
				loseGameMessageAndMovie("Douglas-Fire-No-Order", "loose/fired_level_1");
			}
			if (helper.isTimeout("Mission-4-Success")) {
				helper.clearTimeout("Mission-4-Success");

				helper.send("Naxos-Not-Under-Attack").visible = true;
				helper.receive("Naxos-Unknown-Ships").visible = false;
				
				helper.setObjectiveState("Mission-4", ObjectiveState.SUCCESS);
				helper.setTimeout("Mission-4-Done", 13000);
			}
			if (helper.isTimeout("Mission-4-Done")) {
				helper.objective("Mission-4").visible = false;
				helper.clearTimeout("Mission-4-Done");
			}
		}
	}
	/**
	 * Remove the fleets involved in the situation.
	 */
	void removeFleets() {
		Player pirates = player("Pirates");
		Pair<Fleet, InventoryItem> fi = findTaggedFleet("Mission-4-Pirates-1", pirates);
		if (fi != null) {
			world.removeFleet(fi.first);
			helper.scriptedFleets().remove(fi.first.id);
		}

		fi = findTaggedFleet("Mission-4-Pirates-2", pirates);
		if (fi != null) {
			world.removeFleet(fi.first);
			helper.scriptedFleets().remove(fi.first.id);
		}
		cleanupScriptedFleets();
	}
	/**
	 * Create a carrier moving across the screen.
	 */
	void createPirateTask() {
		Planet naxos = planet("Naxos");
		Player pirate = player("Pirates");
		Fleet f = createFleet(label("pirates.fleet_name"), pirate, naxos.x + 15, naxos.y - 5);
		f.addInventory(world.researches.get("PirateFighter2"), 1);
		for (InventoryItem ii : f.inventory) {
			ii.tag = "Mission-4-Pirates-1";
		}
		f.task = FleetTask.SCRIPT;
		helper.scriptedFleets().add(f.id);
		
		f = createFleet(label("pirates.fleet_name"), pirate, naxos.x + 17, naxos.y - 3);
		f.addInventory(world.researches.get("PirateFighter"), 2);
		for (InventoryItem ii : f.inventory) {
			ii.tag = "Mission-4-Pirates-2";
		}
		f.task = FleetTask.SCRIPT;
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
	 * Issue the specific mission changes once task is completed.
	 * @param pirateSurvived did the pirate survive?
	 */
	void completeMission(boolean pirateSurvived) {
		helper.setTimeout("Mission-4-Success", 3000);
		helper.clearMissionTime("Mission-4-Timeout");
		if (pirateSurvived) {
			// indicate that we helped the pirate
			helper.setMissionTime("Mission-4-Helped", helper.now() + 1);
		}
		removeFleets();

	}
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		if (world.level == 1) {
			if (isMissionSpacewar(battle, "Mission-4")) {
				boolean pirateSurvived = false;
				for (InventoryItem ii : new ArrayList<InventoryItem>(battle.attacker.inventory)) {
					if ("Mission-4-Pirates-1".equals(ii.tag)) {
						pirateSurvived = true;
						battle.attacker.inventory.remove(ii);
					}
				}
				completeMission(pirateSurvived);
			}
		}
	}
	@Override
	public void onAutobattleStart(BattleInfo battle) {
		if (world.level == 1) {
			if (isMissionSpacewar(battle, "Mission-4")) {
				Player pirates = player("Pirates");
				Pair<Fleet, InventoryItem> f1 = findTaggedFleet("Mission-4-Pirates-1", pirates);
				Pair<Fleet, InventoryItem> f2 = findTaggedFleet("Mission-4-Pirates-2", pirates);
				if (battle.targetFleet == f1.first) {
					battle.targetFleet = f2.first;
				}
				// move into player
				f1.second.owner = player;
				battle.attacker.inventory.addAll(f1.first.inventory);
			}
		}
	}
	@Override
	public void onSpacewarStart(SpacewarWorld war) {
		if (isMissionSpacewar(war.battle(), "Mission-4")) {
			BattleInfo battle = war.battle();
			Player pirates = player("Pirates");
			Pair<Fleet, InventoryItem> f1 = findTaggedFleet("Mission-4-Pirates-1", pirates);
			Pair<Fleet, InventoryItem> f2 = findTaggedFleet("Mission-4-Pirates-2", pirates);

			// pirate 1 attacked
			if (battle.targetFleet == f1.first) {
				war.includeFleet(f2.first, f2.first.owner);
				battle.targetFleet = f2.first;
			} else {
				// pirate 2 attacked
				war.addStructures(f1.first.inventory, EnumSet.of(
						ResearchSubCategory.SPACESHIPS_BATTLESHIPS,
						ResearchSubCategory.SPACESHIPS_CRUISERS,
						ResearchSubCategory.SPACESHIPS_FIGHTERS));
			}
			
			patchPlayer(f1.first);
			battle.attackerAllies.add(f1.first.owner);
			// center pirate
			Dimension d = war.space();
			List<SpacewarStructure> structures = war.structures();
			SpacewarStructure a = null;
			for (SpacewarStructure s : structures) {
				if (s.item != null && "Mission-4-Pirates-1".equals(s.item.tag)) {
					s.x = d.width / 2;
					s.y = d.height / 2;
					s.angle = 0.0;
					s.owner = f1.first.owner;
					a = s;
					s.guard = true;
				}
			}
			for (SpacewarStructure s : war.structures(pirates)) {
				s.attack = a;
			}
			battle.allowRetreat = false;
		}
	}
	/**
	 * Duplicate the player as player2.
	 * @param f the fleet
	 */
	void patchPlayer(Fleet f) {
		Player owner = f.owner;
		
		Player newOwner = new Player(owner.world, owner.id + "2");
		newOwner.name = owner.name;
		newOwner.color = owner.color;
		newOwner.shortName = owner.shortName;
		newOwner.fleetIcon = owner.fleetIcon;
		newOwner.race = owner.race;
		newOwner.aiMode = AIMode.PIRATES;
		newOwner.ai = world.env.getAI(newOwner);
		newOwner.noDatabase = owner.noDatabase;
		newOwner.noDiplomacy = owner.noDiplomacy;
		
		
		f.owner = newOwner;
	}
	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		if (isMissionSpacewar(war.battle(), "Mission-4")) {
			// find the status of the trader ship
			boolean pirateSurvived = false;
			for (SpacewarStructure s : war.structures()) {
				if (s.item != null && "Mission-4-Pirates-1".equals(s.item.tag)) {
					Player pirates = player("Pirates");
					// restore owner
					s.fleet.owner = pirates;
					pirateSurvived = true;
					break;
				}
			}
			completeMission(pirateSurvived);
			if (pirateSurvived) {
				war.battle().messageText = label("battlefinish.mission-4.10_bonus");
			} else {
				war.battle().messageText = label("battlefinish.mission-4.10");
			}
		}
	}
}
