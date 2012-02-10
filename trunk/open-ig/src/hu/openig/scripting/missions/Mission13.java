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
import hu.openig.model.FleetTask;
import hu.openig.model.InventoryItem;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchType;
import hu.openig.model.SoundType;
import hu.openig.model.SpacewarWorld;
import hu.openig.utils.XElement;

/**
 * Mission 13: Escort Admiral Benson.
 * @author akarnokd, 2012.02.10.
 */
public class Mission13 extends Mission {
	/** The mission state enum. */
	public enum M13 {
		/** Not started yet. */
		NONE,
		/** Wait to start. */
		WAIT,
		/** Intro phase. */
		INTRO,
		/** Running. */
		RUN,
		/** Done. */
		DONE
	}
	/** The mission stage. */
	M13 stage = M13.NONE;
	@Override
	public boolean applicable() {
		return world.level == 2;
	}

	@Override
	public void onTime() {
		Objective m14 = helper.objective("Mission-14");
		final Objective m13 = helper.objective("Mission-13");
		if (m14.state == ObjectiveState.SUCCESS
				&& stage == M13.NONE) {
			stage = M13.WAIT;
			helper.setMissionTime("Mission-13", helper.now() + 4 * 24);
		}
		if (checkMission("Mission-13")) {
			stage = M13.INTRO;
			world.env.playVideo("interlude/flagship_arrival", new Action0() {
				@Override
				public void invoke() {
					helper.showObjective(m13);
					helper.setTimeout("Mission-13-Message", 3000);
				}
			});
		}
		if (checkTimeout("Mission-13-Message")) {
			stage = M13.RUN;
			incomingMessage("Douglas-Admiral-Benson");
			createBenson();
			helper.setMissionTime("Mission-13-Attack", helper.now() + 1);
		}
		if (checkMission("Mission-13-Attack")) {
			createGarthog();
		}
		if (checkMission("Mission-13-Timeout")) {
			removeFleets();
			helper.setObjectiveState(m13, ObjectiveState.FAILURE);
			
			helper.setTimeout("Mission-13-Fire", 13000);
			helper.setTimeout("Mission-13-Hide", 13000);
		}
		if (checkTimeout("Mission-13-Hide")) {
			stage = M13.DONE;
			m13.visible = false;
			helper.receive("Douglas-Admiral-Benson").visible = false;
		}
		if (checkTimeout("Mission-13-Fire")) {
			helper.gameover();
			loseGameMessageAndMovie("Douglas-Admiral-Benson-Failed", "loose/fired_level_2");
		}
		if (stage == M13.RUN) {
			checkFollowBenson();
		}
	}
	/**
	 * Remove the mission fleets.
	 */
	void removeFleets() {
		Pair<Fleet, InventoryItem> benson = findTaggedFleet("Mission-13-Benson", player);
		Player g = garthog();
		Pair<Fleet, InventoryItem> garthog = findTaggedFleet("Mission-13-Garthog", g);
		if (benson != null) {
			world.removeFleet(benson.first);
			helper.scriptedFleets().remove(benson.first.id);
		}
		if (garthog != null) {
			world.removeFleet(garthog.first);
			helper.scriptedFleets().remove(garthog.first.id);
		}
	}
	/**
	 * Create benson's fleet.
	 */
	void createBenson() {
		Planet a = planet("Achilles");
		Fleet f = createFleet(label("mission-13.benson_fleet"), player, a.x, a.y);
		
		//---------------------------------
		ResearchType bs1 = research("Battleship1");
		f.addInventory(bs1, 1);
		
		InventoryItem ii = f.getInventoryItem(bs1);
		
		setSlot(ii, "laser", "Laser1", 14);
		setSlot(ii, "rocket", "Rocket1", 3);
		setSlot(ii, "cannon", "IonCannon", 3);
		setSlot(ii, "shield", "Shield1", 1);
		
		//---------------------------------
		tagFleet(f, "Mission-13-Benson");
		
		Planet nc = planet("New Caroline");
		f.moveTo(nc);
		f.task = FleetTask.SCRIPT;
		helper.scriptedFleets().add(f.id);
	}
	/**
	 * Create the garthog fleet.
	 */
	void createGarthog() {
		Planet g1 = planet("Garthog 1");
		Player g = garthog();
		Fleet f = createFleet(label("Garthog.fleet"), g, g1.x, g1.y);

		//---------------------------------
		
		f.addInventory(research("GarthogFighter"), 3);
		f.addInventory(research("GarthogDestroyer"), 2);
		f.addInventory(research("GarthogBattleship"), 1);
		
		//---------------------------------
		
		tagFleet(f, "Mission-13-Garthog");
		
		f.task = FleetTask.SCRIPT;
		helper.scriptedFleets().add(f.id);
	}
	/**
	 * Reach benson.
	 */
	void checkFollowBenson() {
		Player g = garthog();
		Pair<Fleet, InventoryItem> benson = findTaggedFleet("Mission-13-Benson", player);
		Pair<Fleet, InventoryItem> garthog = findTaggedFleet("Mission-13-Garthog", g);
		
		if (benson != null && garthog != null) {
			double d = Math.hypot(benson.first.x - garthog.first.x, benson.first.y - garthog.first.y);
			if (d <= 5) {
				helper.setMissionTime("Mission-13-Timeout", helper.now() + 12);
				
				benson.first.stop();
				benson.first.task = FleetTask.SCRIPT;
				garthog.first.stop();
				garthog.first.task = FleetTask.SCRIPT;
				
				Fleet ff = getFollower(benson.first, player);
				if (ff != null) {
					ff.attack(garthog.first);
				}
			} else {
				garthog.first.moveTo(benson.first.x, benson.first.y);
				garthog.first.task = FleetTask.SCRIPT;
			}
		}
	}
	/**
	 * Conclude the space battle.
	 * @param battle the battle parameters
	 * @return success
	 */
	boolean concludeBattle(BattleInfo battle) {
		boolean result = false;
		helper.clearMissionTime("Mission-13-Timeout");
		Player g = garthog();
		Pair<Fleet, InventoryItem> benson = findTaggedFleet("Mission-13-Benson", player);
		Pair<Fleet, InventoryItem> garthog = findTaggedFleet("Mission-13-Garthog", g);

		if (benson != null) {
			Planet nc = planet("New Caroline");
			benson.first.moveTo(nc);
			benson.first.task = FleetTask.SCRIPT;
			result = true;
		} else {
			helper.setObjectiveState("Mission-13", ObjectiveState.FAILURE);
			helper.setTimeout("Mission-13-Fire", 13000);
		}
		// remove garthog anyway
		if (garthog != null) {
			world.removeFleet(garthog.first);
		}
		cleanupScriptedFleets();
		return result;
	}
	@Override
	public void onDiscovered(Player player, Fleet fleet) {
		if (stage == M13.RUN && player == this.player) {
			if (hasTag(fleet, "Mission-13-Garthog")) {
				if (world.env.config().slowOnEnemyAttack) {
					world.env.speed1();
				}
				world.env.computerSound(SoundType.ENEMY_FLEET_DETECTED);
			}
		}
	}
	@Override
	public void onFleetAt(Fleet fleet, Planet planet) {
		if (planet.id.equals("New Caroline") && hasTag(fleet, "Mission-13-Benson")) {
			helper.setObjectiveState("Mission-13", ObjectiveState.SUCCESS);
			helper.setMissionTime("Mission-13-Hide", 13000);
		}
	}
	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		BattleInfo battle = war.battle();
		if (isMissionSpacewar(battle, "Mission-13")) {
			if (concludeBattle(battle)) {
				battle.rewardImage = "battlefinish/mission_16.png";
				battle.messageText = label("battlefinish.mission-13.16");
			}
		}
	}
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		if (isMissionSpacewar(battle, "Mission-13")) {
			finishJointAutoSpaceBattle(battle, "Mission-13-Benson");
			concludeBattle(battle);
		}
	}
	@Override
	public void onSpacewarStart(SpacewarWorld war) {
		if (isMissionSpacewar(war.battle(), "Mission-13")) {
			Player g = garthog();
			startJointSpaceBattle(war, "Mission-13-Benson", player, "Mission-13-Garthog", g);
		}
	}
	/** @return the garthog player. */
	Player garthog() {
		return player("Garthog");
	}
	@Override
	public void onAutobattleStart(BattleInfo battle) {
		if (isMissionSpacewar(battle, "Mission-13")) {
			Player g = garthog();
			startJointAutoSpaceBattle(battle, "Mission-13-Benson", player, "Mission-13-Garthog", g);
		}
	}
	@Override
	public void reset() {
		stage = M13.NONE;
	}
	@Override
	public void load(XElement xmission) {
		stage = M13.valueOf(xmission.get("stage"));
	}
	@Override
	public void save(XElement xmission) {
		xmission.set("stage", stage);
	}
}
