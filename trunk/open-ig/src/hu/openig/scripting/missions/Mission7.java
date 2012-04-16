/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Difficulty;
import hu.openig.core.Pair;
import hu.openig.model.BattleInfo;
import hu.openig.model.DiplomaticRelation;
import hu.openig.model.Fleet;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Mission 7: defend 2 traders from Garthog pirates.
 * @author akarnokd, 2012.01.19.
 *
 */
public class Mission7 extends Mission {
	@Override
	public boolean applicable() {
		return world.level == 2;
	}
	@Override
	public void onTime() {
		// a week after the initial garthog attack
		Objective m6 = helper.objective("Mission-6");
		Objective m7 = helper.objective("Mission-7");
		Objective m7t1 = helper.objective("Mission-7-Task-1");
		Objective m7t2 = helper.objective("Mission-7-Task-2");
		if (m6.state != ObjectiveState.ACTIVE
				&& !m7.visible && m7.state == ObjectiveState.ACTIVE
				&& !m7t1.visible && m7t1.state == ObjectiveState.ACTIVE
				&& !helper.hasMissionTime("Mission-7")) {
			helper.setMissionTime("Mission-7", helper.now() + 7 * 24);
		}
		// a week after the first virus attack
		Objective m12t1 = helper.objective("Mission-12-Task-1");
		if (m12t1.state != ObjectiveState.ACTIVE
				&& !m7t2.visible
				&& !helper.hasMissionTime("Mission-7-Task-2")) {
			helper.setMissionTime("Mission-7-Task-2", helper.now() + 7 * 24);
		}
		
		if (checkMission("Mission-7")) {
			helper.showObjective("Mission-7");
			helper.setMissionTime("Mission-7-Task-1", helper.now() + 24);
		}
		if (helper.canStart("Mission-7-Task-1")) {
			checkStartTask(1);
		}
		if (helper.canStart("Mission-7-Task-2")) {
			checkStartTask(2);
		}
		for (int i = 1; i <= 2; i++) {
			if (checkTimeout("Mission-7-Task-" + i + "-Success")) {
				helper.setObjectiveState("Mission-7-Task-" + i, ObjectiveState.SUCCESS);
				if (i == 2) {
					helper.setObjectiveState("Mission-7", ObjectiveState.SUCCESS);
					DiplomaticRelation dr = world.establishRelation(player, player("FreeTraders"));
					dr.value = 75;
				} else {
					int reward = 5000;
					player.money += reward;
					player.statistics.moneyIncome += reward;
				}
				helper.setTimeout("Mission-7-Hide", 13000);
			}
			if (checkTimeout("Mission-7-Task-" + i + "-Failed")) {
				helper.setObjectiveState("Mission-7-Task-" + i, ObjectiveState.FAILURE);
				helper.setObjectiveState("Mission-7", ObjectiveState.FAILURE);
				helper.setTimeout("Mission-7-Hide", 13000);
				helper.setTimeout("Mission-7-Fire", 16000);
				removeFleets();
			}
			if (checkMission("Mission-7-Task-" + i + "-Timeout")) {
				helper.setObjectiveState("Mission-7-Task-" + i, ObjectiveState.FAILURE);
				helper.setObjectiveState("Mission-7", ObjectiveState.FAILURE);
				helper.setTimeout("Mission-7-Hide", 13000);
				helper.setTimeout("Mission-7-Fire", 16000);
				removeFleets();
			}
		}
		if (checkTimeout("Mission-7-Hide")) {
			helper.objective("Mission-7").visible = false;
		}
		if (checkTimeout("Mission-7-Fire")) {
			helper.gameover();
			loseGameMessageAndMovie("Douglas-Fire-Lost-Merchants", "loose/fired_level_2");
		}
	}
	/** Remove the attacker fleets. */
	void removeFleets() {
		Pair<Fleet, InventoryItem> merchant = findTaggedFleet("Mission-7-Trader", player("Traders"));
		if (merchant != null) {
			removeScripted(merchant.first);
			world.removeFleet(merchant.first);
		}
		Pair<Fleet, InventoryItem> g = findTaggedFleet("Mission-7-Garthog", player("Garthog"));
		if (g != null) {
			removeScripted(g.first);
			world.removeFleet(g.first);
		}
	}
	/**
	 * Check if the traders are in position to start the task. 
	 * @param task the task index
	 * @return true if can start
	 */
	boolean checkStartTask(int task) {
		List<Fleet> fs = findVisibleFleets(player, false, player("Traders"), 4);
		fs = filterByRange(fs, world.params().groundRadarUnitSize() - 2, 
				"Naxos", "San Sterling", "Achilles", "Centronom", "New Caroline");
		String m7ti = "Mission-7-Task-" + task;
		String m7tio = "Mission-7-Task-" + task + "-Timeout";
		if (!fs.isEmpty()) {
			incomingMessage("Merchant-Under-Attack-Garthog");
			helper.objective("Mission-7").visible = true;
			helper.showObjective(m7ti);
			helper.clearMissionTime(m7ti);
			world.env.speed1();
			Fleet f = world.random(fs);
			f.stop();
			f.task = FleetTask.SCRIPT;
			for (InventoryItem ii : f.inventory) {
				ii.tag = "Mission-7-Trader";
			}
			
			// create simple pirate fleet
			Fleet pf = createFleet(label("Garthog.pirates"), player("Garthog"), f.x + 1, f.y + 1);
			
			// ----------------------------------------------------------------
			// adjust fleet strenth here
			if (task == 1) {
				if (world.difficulty == Difficulty.HARD) {
					pf.addInventory(world.researches.get("GarthogFighter"), 8);
					pf.addInventory(world.researches.get("GarthogDestroyer"), 2);
				} else
				if (world.difficulty == Difficulty.NORMAL) {
					pf.addInventory(world.researches.get("GarthogFighter"), 10);
					pf.addInventory(world.researches.get("GarthogDestroyer"), 1);
				} else {
					pf.addInventory(world.researches.get("GarthogFighter"), 5);
					pf.addInventory(world.researches.get("GarthogDestroyer"), 1);
				}
			} else {
				if (world.difficulty == Difficulty.HARD) {
					pf.addInventory(world.researches.get("GarthogFighter"), 15);
					pf.addInventory(world.researches.get("GarthogDestroyer"), 3);
				} else
				if (world.difficulty == Difficulty.NORMAL) {
					pf.addInventory(world.researches.get("GarthogFighter"), 12);
					pf.addInventory(world.researches.get("GarthogDestroyer"), 2);

				} else {
					pf.addInventory(world.researches.get("GarthogFighter"), 8);
					pf.addInventory(world.researches.get("GarthogDestroyer"), 2);
				}
			}
			for (InventoryItem ii : pf.inventory) {
				ii.tag = "Mission-7-Garthog";
			}

			// ----------------------------------------------------------------

			helper.scriptedFleets().add(f.id);
			helper.scriptedFleets().add(pf.id);
			
			// set failure timeout
			helper.setMissionTime(m7tio, helper.now() + 24);
			return true;
		}
		return false;
	}
	/**
	 * Ensure that the given list of fleets contains only elements
	 * which are within the given radar range of the listed planet ids.
	 * @param fs the fleet list
	 * @param r the radar range in pixels
	 * @param planets the planets to check
	 * @return fs parameter
	 */
	List<Fleet> filterByRange(List<Fleet> fs, int r,
			String... planets) {
		outer:
		for (int i = fs.size() - 1; i >= 0; i--) {
			Fleet f = fs.get(i);
			for (String p : planets) {
				Planet op = planet(p);
				double d = Math.hypot(f.x - op.x, f.y - op.y);
				if (d <= r) {
					continue outer;
				}
			}
			fs.remove(i);
		}
		return fs;
	}
	/**
	 * Handle the spacewar between a trader and a pirate. 
	 * @param war the war context
	 * @param task the task index
	 */
	void spacewarStartTraderVsPirate(SpacewarWorld war, int task) {
		if (isMissionSpacewar(war.battle(), "Mission-7-Task-" + task)) {
			BattleInfo battle = war.battle();
			Player traders = player("Traders");
			Player pirates = player("Garthog");
			
			Pair<Fleet, InventoryItem> trader = findTaggedFleet("Mission-7-Trader", traders);
			Pair<Fleet, InventoryItem> garthog = findTaggedFleet("Mission-7-Garthog", pirates);
			if (trader == null) {
				new AssertionError("Mission-7-Trader not found").printStackTrace();
			}
			if (garthog == null) {
				new AssertionError("Mission-7-Garthog not found").printStackTrace();
			}
			// attack on the trader
			// trader attacked
			if (battle.targetFleet == trader.first) {
				war.includeFleet(garthog.first, garthog.first.owner);
				// fix target fleet
				battle.targetFleet = garthog.first;
			} else {
				// pirate attacked
				war.addStructures(trader.first.inventory, EnumSet.of(
						ResearchSubCategory.SPACESHIPS_BATTLESHIPS,
						ResearchSubCategory.SPACESHIPS_CRUISERS,
						ResearchSubCategory.SPACESHIPS_FIGHTERS));
			}
			// center trader
			Dimension d = war.space();
			for (SpacewarStructure s : war.structures()) {
				if (s.item != null && "Mission-7-Trader".equals(s.item.tag)) {
					s.x = d.width / 2;
					s.y = d.height / 2;
					s.angle = 0.0;
				}
			}
			battle.attackerAllies.add(traders);
			battle.allowRetreat = false;
			
			battle.chat = "chat.mission-7.defend.merchant" + task;
		}
	}
	/**
	 * Finish the war for the trader vs pirate missions.
	 * @param war the war context
	 * @param task the task index
	 */
	void spacewarFinishTraderVsPirate(SpacewarWorld war, int task) {
		if (isMissionSpacewar(war.battle(), "Mission-7-Task-" + task)) {
			// find the status of the trader ship
			Player traders = player("Traders");
			List<SpacewarStructure> sts = war.structures(traders);
			boolean traderSurvived = !sts.isEmpty();
			completeTaskN(traderSurvived, task);
			if (traderSurvived) {
				if (task == 1) {
					war.battle().messageText = label("battlefinish.mission-7.merchant7");
					war.battle().rewardText = format("mission-7.save_trader.reward", 5000);
					war.battle().rewardImage = "battlefinish/mission_1_8d";
				} else {
					war.battle().messageText = label("battlefinish.mission-7_task2.merchant8");
					war.battle().rewardText = label("battlefinish.mission-7_task2.merchant8_bonus");
				}
			}
		}
	}
	/**
	 * Issue the specific mission changes once task is completed.
	 * @param traderSurvived did the trader survive?
	 * @param task the current task id
	 */
	void completeTaskN(boolean traderSurvived, int task) {
		Player traders = player("Traders");
		Player pirates = player("Garthog");
		if (!traderSurvived) {
			helper.setTimeout("Mission-7-Task-" + task + "-Failed", 3000);
			helper.clearMissionTime("Mission-7-Task-" + task + "-Timeout");
		} else {
			helper.setTimeout("Mission-7-Task-" + task + "-Success", 3000);
			helper.clearMissionTime("Mission-7-Task-" + task + "-Timeout");
		}
		cleanupScriptedFleets();
		for (int i : new ArrayList<Integer>(helper.scriptedFleets())) {
			Fleet f = fleet(i);
			if (f != null) {
				if (f.owner == traders) {
					f.task = FleetTask.IDLE;
					// fix owner
					for (InventoryItem ii : f.inventory) {
						ii.owner = f.owner;
						ii.tag = null;
					}
					f.targetPlanet(world.random(Arrays.asList(
							planet("Achilles"), planet("Naxos"), planet("San Sterling"),
							planet("Centronom"), planet("New Caroline"))));
					helper.scriptedFleets().remove(f.id);
				} else
				if (f.owner == pirates) {
					world.removeFleet(f);
					helper.scriptedFleets().remove(f.id);
				}
			}
		}
		helper.receive("Merchant-Under-Attack-Garthog").visible = false;
	}
	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		for (int i = 1; i <= 2; i++) {
			spacewarFinishTraderVsPirate(war, i);
		}
	}
	@Override
	public void onSpacewarStart(SpacewarWorld war) {
		for (int i = 1; i <= 2; i++) {
			spacewarStartTraderVsPirate(war, i);
		}
	}
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		for (int task = 1; task <= 2; task++) {
			if (isMissionSpacewar(battle, "Mission-7-Task-" + task)) {
				Player trader = player("Traders");
				boolean traderSurvived = false;
				for (InventoryItem ii : new ArrayList<InventoryItem>(battle.attacker.inventory)) {
					if (ii.owner == trader) {
						traderSurvived = true;
						battle.attacker.inventory.remove(ii);
					}
				}
				completeTaskN(traderSurvived, task);
			}
		}
	}
	@Override
	public void onAutobattleStart(BattleInfo battle) {
		for (int task = 1; task <= 2; task++) {
			if (isMissionSpacewar(battle, "Mission-7-Task-" + task)) {
				Player traders = player("Traders");
				Player pirates = player("Garthog");
				Fleet tf = null;
				Fleet pf = null;
				for (int i : helper.scriptedFleets()) {
					Fleet f = fleet(i);
					if (f.owner == traders) {
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
}
