/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Action0;
import hu.openig.mechanics.AITrader;
import hu.openig.model.BattleInfo;
import hu.openig.model.Fleet;
import hu.openig.model.FleetTask;
import hu.openig.model.InventoryItem;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarWorld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Activities of Mission 2.
 * @author akarnokd, 2012.01.14.
 */
public class Mission2 extends Mission {
	/** The pirate tag. */
	private static final String MISSION_2_PIRATE = "Mission-2-Pirate";
	/** The trader tag. */
	private static final String MISSION_2_TRADER = "Mission-2-Trader";
	/** The money reward per task. */
	final int[] moneyReward = { 0, 1000, 2500, 5000 };
	/** The custom battle finish image. */
	final String[] imageReward = { null, "battlefinish/mission_1_8b", "battlefinish/mission_1_8b", "battlefinish/mission_1_8b" };
	/** The attack is mission-related. */
	boolean missionAttack;
	/**
	 * Check the starting conditions of Mission 2 and make it available.
	 */
	void checkMission2Start() {
		if (checkMission("Mission-2")) {
			world.env.stopMusic();
			world.env.playVideo("interlude/merchant_in", new Action0() {
				@Override
				public void invoke() {
					world.env.playMusic();
					showObjective("Mission-2");
					addMission("Mission-2-Task-1", 24);
				}
			});

		}
	}
	/**
	 * Check the starting conditions for mission 2 tasks.
	 */
	void checkMission2Tasks() {
		Objective m2 = objective("Mission-2");
		if (!m2.visible) {
			return;
		}
		if (m2.isActive()) {
			for (int i = 1; i <= 3; i++) {
				String m2tio = String.format("Mission-2-Task-%d-Timeout", i);
				String m2ti = String.format("Mission-2-Task-%d", i);
				if (checkMission(m2tio)) {
					setObjectiveState(m2ti, ObjectiveState.FAILURE);
					scheduleNextTask(i);
					cleanupShips();
				}
				if (checkMission(m2ti)) {
					List<Fleet> fs = findVisibleFleets(player, false, player("Traders"), 8);
					fs = filterByRange(fs, world.params().groundRadarUnitSize() - 2, 
							"Naxos", "San Sterling", "Achilles");
					if (!fs.isEmpty()) {
						Fleet f = world.random(fs);

						int fidx = ((AITrader)f.owner.ai).fleetIndex(f);
						
						int traderMessage = 1 + fidx % 7;
						
						receive("Douglas-Pirates").visible = false;
						incomingMessage("Merchant-Under-Attack-" + traderMessage, m2ti);
						
						world.env.speed1();
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
						
						tagFleet(pf, MISSION_2_PIRATE);
						tagFleet(f, MISSION_2_TRADER);
						
						addScripted(f);
						addScripted(pf);
						
						// set failure timeout
						addMission(m2tio, 48);
					} else {
						addMission(m2ti, 0);
					}
					
				}
			}
			int done = 0;
			int success = 0;
			for (int i = 1; i <= 3; i++) {
				String m2ti = String.format("Mission-2-Task-%d", i);
				Objective o = objective(m2ti);
				if (o.state != ObjectiveState.ACTIVE) {
					done++;
					if (o.state == ObjectiveState.SUCCESS) {
						success++;
					}
				}
			}
			if (done == 3) {
				if (success == 0) {
					setObjectiveState("Mission-2", ObjectiveState.FAILURE);
					addTimeout("Mission-2-Failed", 13000);
				} else 
				if (success == 3) {
					setObjectiveState("Mission-2", ObjectiveState.SUCCESS);
					addTimeout("Mission-2-Success", 13000);
				} else {
					setObjectiveState("Mission-2", ObjectiveState.SUCCESS);
					addTimeout("Mission-2-Success-But", 13000);
					incomingMessage("Douglas-Pirates");
				}
			}
			for (int i = 1; i <= 3; i++) {
				if (checkTimeout("Mission-2-Task-" + i + "-Failed")) {
					setObjectiveState("Mission-2-Task-" + i, ObjectiveState.FAILURE);
					scheduleNextTask(i);
				} else
				if (checkTimeout("Mission-2-Task-" + i + "-Success")) {
					setObjectiveState("Mission-2-Task-" + i, ObjectiveState.SUCCESS);
					
					// Reward
					int m = moneyReward[i];
					player.money += m;
					player.statistics.moneyIncome += m;
					world.statistics.moneyIncome += m;
					
					scheduleNextTask(i);

				}
			}
		}
		
		if (checkTimeout("Mission-2-Failed")) {
			objective("Mission-2").visible = false;
			loseGameMessageAndMovie("Douglas-Fire-Lost-Merchants", "loose/fired_level_1");
		} else
		if (checkTimeout("Mission-2-Success")) {
			objective("Mission-2").visible = false;
		} else
		if (checkTimeout("Mission-2-Success-But")) {
			objective("Mission-2").visible = false;
		}
	}
	/**
	 * Remove the trader and the pirate fleet.
	 */
	void cleanupShips() {
		Fleet tf = findTaggedFleet(MISSION_2_TRADER, player("Traders"));
		if (tf != null) {
			removeScripted(tf);
			world.removeFleet(tf);
		}
		Fleet pf = findTaggedFleet(MISSION_2_PIRATE, player("Pirates")); 
		if (pf != null) {
			removeScripted(pf);
			world.removeFleet(pf);
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
			addMission(String.format("Mission-2-Task-%d", currentTask + 1), 
					(4 + world.random().nextInt(3)) * 24);
		}
	}
	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		for (int i = 1; i <= 3; i++) {
			spacewarFinishTraderVsPirate(war, i);
		}
	}
	@Override
	public void onSpacewarStart(SpacewarWorld war) {
		for (int i = 1; i <= 3; i++) {
			spacewarStartTraderVsPirate(war, i);
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

			// which trader message to show
			int textIndex = 0;
			for (int i = 1; i <= 7; i++) {
				if (receive("Merchant-Under-Attack-" + i).visible) {
					textIndex = i - 1;
					break;
				}
			}
			textIndex %= 6;

			
			boolean traderSurvived = !sts.isEmpty();
			completeTaskN(traderSurvived, task);
			if (traderSurvived) {
				war.battle().messageText = format("battlefinish.mission-2.merchant" + (textIndex + 1), moneyReward[task]);
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
		if (!traderSurvived) {
			addTimeout("Mission-2-Task-" + task + "-Failed", 3000);
			clearMission("Mission-2-Task-" + task + "-Timeout");
		} else {
			addTimeout("Mission-2-Task-" + task + "-Success", 3000);
			clearMission("Mission-2-Task-" + task + "-Timeout");
		}

		Fleet tf = findTaggedFleet(MISSION_2_TRADER, player("Traders"));
		if (tf != null) {
			for (InventoryItem ii : tf.inventory) {
				ii.owner = tf.owner;
				ii.tag = null;
			}
			tf.task = FleetTask.IDLE;
			tf.moveTo(world.random(Arrays.asList(planet("Achilles"), planet("Naxos"), planet("San Sterling"))));
		}
		Fleet pf = findTaggedFleet(MISSION_2_PIRATE, player("Pirates")); 
		if (pf != null) {
			removeScripted(pf);
			world.removeFleet(pf);
		}

		
		cleanupScriptedFleets();
		// hide message
		for (int i = 1; i <= 7; i++) {
			receive("Merchant-Under-Attack-" + i).visible = false;
		}
		missionAttack = false;
	}
	/**
	 * Handle the spacewar between a trader and a pirate. 
	 * @param war the war context
	 * @param task the task index
	 */
	void spacewarStartTraderVsPirate(SpacewarWorld war, int task) {
		if (isMissionSpacewar(war.battle(), "Mission-2-Task-" + task)) {
			if (startJointSpaceBattle(war, MISSION_2_TRADER, player("Traders"), MISSION_2_PIRATE, player("Pirates"))) {
				Player tr = player("Traders");
				int tidx = ((AITrader)tr.ai).fleetIndex(findTaggedFleet(MISSION_2_TRADER, tr));
				war.battle().chat = "chat.mission-2.defend.merchant" + (1 + tidx % 6);
				
				missionAttack = true;
			}
		}
	}
	@Override
	public void onTime() {
		checkMission2Start();
		checkMission2Tasks();
	}
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		for (int task = 1; task <= 3; task++) {
			if (isMissionSpacewar(battle, "Mission-2-Task-" + task) && missionAttack) {
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
		for (int task = 1; task <= 3; task++) {
			if (isMissionSpacewar(battle, "Mission-2-Task-" + task)) {

				if (startJointAutoSpaceBattle(battle, 
						MISSION_2_TRADER, player("Traders"), MISSION_2_PIRATE, player("Pirates"))) {
					missionAttack = true;
				}
			}
		}
	}
	@Override
	public boolean applicable() {
		return world.level == 1;
	}
	@Override
	public boolean fleetBlink(Fleet f) {
		if (f.task == FleetTask.SCRIPT && f.owner == player("Traders")) {
			return true;
		}
		return false;
	}
}
