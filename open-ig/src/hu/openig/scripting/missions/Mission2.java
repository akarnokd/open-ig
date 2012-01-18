/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.model.BattleInfo;
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
 * Activities of Mission 2.
 * @author akarnokd, 2012.01.14.
 */
public class Mission2 extends Mission {
	/** The money reward per task. */
	final int[] moneyReward = { 0, 1000, 2500, 5000 };
	/** The custom battle finish image. */
	final String[] imageReward = { null, null, null, null };
	/**
	 * Check the starting conditions of Mission 2 and make it available.
	 */
	void checkMission2Start() {
		if (helper.canStart("Mission-2")) {
			helper.showObjective("Mission-2");
			helper.clearMissionTime("Mission-2");
			helper.setMissionTime("Mission-2-Task-1", helper.now() + 24);
		}
	}
	/**
	 * Check the starting conditions for mission 2 tasks.
	 */
	void checkMission2Tasks() {
		if (!helper.objective("Mission-2").visible) {
			return;
		}
		if (helper.isActive("Mission-2")) {
			for (int i = 1; i <= 3; i++) {
				String m2tio = String.format("Mission-2-Task-%d-Timeout", i);
				String m2ti = String.format("Mission-2-Task-%d", i);
				if (helper.isMissionTime(m2tio)) {
					helper.setObjectiveState(m2ti, ObjectiveState.FAILURE);
					helper.clearMissionTime(m2tio);
					scheduleNextTask(i);
				}
				if (helper.canStart(m2ti)) {
					List<Fleet> fs = findVisibleFleets(player, false, player("Traders"), 4);
					fs = filterByRange(fs, world.params().groundRadarUnitSize(), 
							"Naxos", "San Sterling", "Achilles");
					if (!fs.isEmpty()) {
						int traderMessage = world.random().nextInt(7) + 1;
						
						helper.receive("Douglas-Pirates").visible = false;
						incomingMessage("Merchant-Under-Attack-" + traderMessage);
						helper.send("Douglas-Reinforcements-Denied").visible = true;
						helper.showObjective(m2ti);
						helper.clearMissionTime(m2ti);
						world.env.speed1();
						Fleet f = world.random(fs);
						f.stop();
						f.task = FleetTask.SCRIPT;
						
						// create simple pirate fleet
						Fleet pf = createFleet(label("pirates.fleet_name"), player("Pirates"), f.x + 1, f.y + 1);
						
						if (i == 1) {
							pf.addInventory(world.researches.get("PirateFighter"), 2);
						} else
						if (i == 2) {
							pf.addInventory(world.researches.get("PirateFighter"), 3);
						} else {
							pf.addInventory(world.researches.get("PirateFighter"), 3);
							pf.addInventory(world.researches.get("PirateDestroyer"), 1);
						}
						
						helper.scriptedFleets().add(f.id);
						helper.scriptedFleets().add(pf.id);
						
						// set failure timeout
						helper.setMissionTime(m2tio, helper.now() + 48);
					}
					
				}
			}
			int done = 0;
			int success = 0;
			for (int i = 1; i <= 3; i++) {
				String m2ti = String.format("Mission-2-Task-%d", i);
				Objective o = helper.objective(m2ti);
				if (o.state != ObjectiveState.ACTIVE) {
					done++;
					if (o.state == ObjectiveState.SUCCESS) {
						success++;
					}
				}
			}
			if (done == 3) {
				helper.send("Douglas-Reinforcements-Denied").visible = false;
				if (success == 0) {
					helper.setObjectiveState("Mission-2", ObjectiveState.FAILURE);
					helper.setTimeout("Mission-2-Failed", 13000);
				} else 
				if (success == 3) {
					helper.setObjectiveState("Mission-2", ObjectiveState.SUCCESS);
					helper.setTimeout("Mission-2-Success", 13000);
				} else {
					helper.setObjectiveState("Mission-2", ObjectiveState.SUCCESS);
					helper.setTimeout("Mission-2-Success-But", 13000);
					incomingMessage("Douglas-Pirates");
				}
			}
			for (int i = 1; i <= 3; i++) {
				if (helper.isTimeout("Mission-2-Task-" + i + "-Failed")) {
					helper.setObjectiveState(helper.objective("Mission-2-Task-" + i), ObjectiveState.FAILURE);
					helper.clearTimeout("Mission-2-Task-" + i + "-Failed");
					scheduleNextTask(i);
				} else
				if (helper.isTimeout("Mission-2-Task-" + i + "-Success")) {
					helper.setObjectiveState(helper.objective("Mission-2-Task-" + i), ObjectiveState.SUCCESS);
					
					// Reward
					int m = moneyReward[i];
					player.money += m;
					player.statistics.moneyIncome += m;
					world.statistics.moneyIncome += m;
					helper.clearTimeout("Mission-2-Task-" + i + "-Success");
					
					scheduleNextTask(i);

				}
			}
		}
		
		if (helper.isTimeout("Mission-2-Failed")) {
			helper.clearTimeout("Mission-2-Failed");
			helper.objective("Mission-2").visible = false;
			loseGameMessageAndMovie("Douglas-Fire-Lost-Merchants", "loose/fired_level_1");
		} else
		if (helper.isTimeout("Mission-2-Success")) {
			helper.clearTimeout("Mission-2-Success");
			helper.objective("Mission-2").visible = false;
		} else
		if (helper.isTimeout("Mission-2-Success-But")) {
			helper.clearTimeout("Mission-2-Success-But");
			helper.objective("Mission-2").visible = false;
		}
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
	 * Schedule the next task.
	 * @param currentTask the current task
	 */
	void scheduleNextTask(int currentTask) {
		if (currentTask < 3) {
			helper.setMissionTime(String.format("Mission-2-Task-%d", currentTask + 1), 
					helper.now() + (4 + world.random().nextInt(3)) * 24);
		}
	}
	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		if (world.level == 1) {
			for (int i = 1; i <= 3; i++) {
				spacewarFinishTraderVsPirate(war, i);
			}
		}
	}
	@Override
	public void onSpacewarStart(SpacewarWorld war) {
		if (world.level == 1) {
			for (int i = 1; i <= 3; i++) {
				spacewarStartTraderVsPirate(war, i);
			}
		}
	}
	/**
	 * Finish the war for the trader vs pirate missions.
	 * @param war the war context
	 * @param task the task index
	 */
	void spacewarFinishTraderVsPirate(SpacewarWorld war, int task) {
		if (isMissionSpacewar(war.battle(), "Mission-2-Task-" + task)) {
			// find the status of the trader ship
			Player traders = player("Traders");
			List<SpacewarStructure> sts = war.structures(traders);
			boolean traderSurvived = !sts.isEmpty();
			completeTaskN(traderSurvived, task);
			if (traderSurvived) {
				war.battle().rewardText = format("mission-2.save_trader.reward", moneyReward[task]);
				war.battle().rewardImage = imageReward[task];
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
		Player pirates = player("Pirates");
		if (!traderSurvived) {
			helper.setTimeout("Mission-2-Task-" + task + "-Failed", 3000);
			helper.clearMissionTime("Mission-2-Task-" + task + "-Timeout");
		} else {
			helper.setTimeout("Mission-2-Task-" + task + "-Success", 3000);
			helper.clearMissionTime("Mission-2-Task-" + task + "-Timeout");
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
					}
					f.targetPlanet(world.random(Arrays.asList(planet("Achilles"), planet("Naxos"), planet("San Sterling"))));
					helper.scriptedFleets().remove(f.id);
				} else
				if (f.owner == pirates) {
					world.removeFleet(f);
					helper.scriptedFleets().remove(f.id);
				}
			}
		}
		// hide message
		for (int i = 1; i <= 7; i++) {
			helper.receive("Merchant-Under-Attack-" + i).visible = false;
		}
	}
	/**
	 * Handle the spacewar between a trader and a pirate. 
	 * @param war the war context
	 * @param task the task index
	 */
	void spacewarStartTraderVsPirate(SpacewarWorld war, int task) {
		if (isMissionSpacewar(war.battle(), "Mission-2-Task-" + task)) {
			BattleInfo battle = war.battle();
			Player traders = player("Traders");
			Player pirates = player("Pirates");
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
			for (SpacewarStructure s : war.structures(tf.owner)) {
				s.x = d.width / 2;
				s.y = d.height / 2;
				s.angle = 0.0;
			}
			battle.attackerAllies.add(traders);
			battle.allowRetreat = false;
		}
	}
	@Override
	public void onTime() {
		checkMission2Start();
		checkMission2Tasks();
	}
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		if (world.level == 1) {
			for (int task = 1; task <= 3; task++) {
				if (isMissionSpacewar(battle, "Mission-2-Task-" + task)) {
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
	}
	@Override
	public void onAutobattleStart(BattleInfo battle) {
		if (world.level == 1) {
			for (int task = 1; task <= 3; task++) {
				if (isMissionSpacewar(battle, "Mission-2-Task-" + task)) {
					Player traders = player("Traders");
					Player pirates = player("Pirates");
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
}
