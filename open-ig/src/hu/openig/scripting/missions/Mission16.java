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
import hu.openig.model.SoundType;
import hu.openig.model.SpacewarWorld;
import hu.openig.utils.XElement;


/**
 * Mission 16: Money carrier.
 * @author akarnokd, 2012.02.10.
 */
public class Mission16 extends Mission {
	/** The mission state enum. */
	public enum M16 {
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
	M16 stage = M16.NONE;
	/** Reinforcements called. */
	boolean reinforcements;
	@Override
	public boolean applicable() {
		return world.level == 2;
	}
	@Override
	public void reset() {
		stage = M16.NONE;
	}
	@Override
	public void load(XElement xmission) {
		stage = M16.valueOf(xmission.get("stage"));
		reinforcements = xmission.getBoolean("reinforcements");
	}
	@Override
	public void save(XElement xmission) {
		xmission.set("stage", stage);
		xmission.set("reinforcements", reinforcements);
	}
	/** The ally tag. */
	protected static final String ALLY = "Mission-16-Carrier";
	/** The enemy tag. */
	protected static final String ENEMY = "Mission-16-Garthog";
	@Override
	public void onTime() {
		Objective m13 = helper.objective("Mission-13");
		final Objective m16 = helper.objective("Mission-16");
		if (m13.state == ObjectiveState.SUCCESS
				&& stage == M16.NONE) {
			stage = M16.WAIT;
			addMission("Mission-16", 4 * 24);
		}
		if (checkMission("Mission-16")) {
			stage = M16.INTRO;
			world.env.playVideo("interlude/colony_ship_arrival_2", new Action0() {
				@Override
				public void invoke() {
					helper.showObjective(m16);
					addTimeout("Mission-16-Message", 3000);
				}
			});
		}
		if (checkTimeout("Mission-16-Message")) {
			stage = M16.RUN;
			incomingMessage("Douglas-Money");
			createCarrier();
			helper.send("Douglas-Reinforcements-Approved").visible = true;
			helper.send("Douglas-Reinforcements-Denied").visible = false;
			helper.send("Douglas-Reinforcements-Denied-2").visible = false;
		}
		if (checkMission("Mission-16-Timeout")) {
			removeFleets();
			helper.setObjectiveState(m16, ObjectiveState.FAILURE);
			
			addTimeout("Mission-16-Fire", 13000);
			addTimeout("Mission-16-Hide", 13000);
		}
		if (checkTimeout("Mission-16-Hide")) {
			stage = M16.DONE;
			m16.visible = false;
			
			helper.send("Douglas-Reinforcements-Approved").visible = false;
			helper.send("Douglas-Reinforcements-Denied").visible = true;
			helper.send("Douglas-Reinforcements-Denied-2").visible = false;
			helper.receive("Douglas-Money").visible = false;
		}
		if (checkTimeout("Mission-16-Fire")) {
			helper.gameover();
			loseGameMessageAndMovie("Douglas-Fire-Escort-Failed", "loose/fired_level_2");
		}
	}
	/** @return the garthog player. */
	Player garthog() {
		return player("Garthog");
	}
	/**
	 * Remove the mission fleets.
	 */
	void removeFleets() {
		Pair<Fleet, InventoryItem> benson = findTaggedFleet(ALLY, player);
		Player g = garthog();
		Pair<Fleet, InventoryItem> garthog = findTaggedFleet(ENEMY, g);
		if (benson != null) {
			world.removeFleet(benson.first);
			removeScripted(benson);
		}
		if (garthog != null) {
			world.removeFleet(garthog.first);
			removeScripted(garthog);
		}
	}
	@Override
	public void onMessageSeen(String id) {
		if ("Douglas-Reinforcements-Approved".equals(id) && !reinforcements) {
			reinforcements = true;
			addReinforcements();
		}
	}
	/** Create the carrier. */
	void createCarrier() {
		Fleet f = createFleet(label("mission-16.fleet"), player, player.explorationOuterLimit.x, player.explorationOuterLimit.y);
		
		f.addInventory(research("TradersFreight1"), 1);
		
		tagFleet(f, ALLY);
		
		Planet sst = planet("San Sterling");
		f.moveTo(sst);
		f.task = FleetTask.SCRIPT;
		
		addScripted(f);
	}
	/**
	 * Add reinforcements to your fleet.
	 */
	void addReinforcements() {
		Pair<Fleet, InventoryItem> f = findTaggedFleet("CampaignMainFleet2", player);
		if (f != null) {
			// --------------------------------------------
			
			f.first.addInventory(research("Fighter1"), 8);
			f.first.addInventory(research("Fighter2"), 4);
			
			// --------------------------------------------
			
			world.env.computerSound(SoundType.REINFORCEMENT_ARRIVED_2);
		}
	}
	@Override
	public void onFleetAt(Fleet fleet, Planet planet) {
		if (hasTag(fleet, ALLY) && planet.id.equals("San Sterling")) {
			garthogAttack();
		}
	}
	
	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		BattleInfo battle = war.battle();
		if (isMissionSpacewar(battle, "Mission-16")) {
			if (concludeBattle(battle)) {
				battle.rewardImage = "battlefinish/mission_20";
				battle.messageText = label("battlefinish.mission-16.20");
			}
		}
	}
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		if (isMissionSpacewar(battle, "Mission-16")) {
			finishJointAutoSpaceBattle(battle, ALLY);
			concludeBattle(battle);
		}
	}
	/**
	 * Conclude the space battle.
	 * @param battle the battle parameters
	 * @return success
	 */
	boolean concludeBattle(BattleInfo battle) {
		boolean result = false;
		helper.clearMissionTime("Mission-16-Timeout");
		Pair<Fleet, InventoryItem> carrier = findTaggedFleet(ALLY, player);

		if (carrier != null) {
			helper.setObjectiveState("Mission-16", ObjectiveState.SUCCESS);
			addTimeout("Mission-16-Hide", 13000);
			result = true;
		} else {
			helper.setObjectiveState("Mission-16", ObjectiveState.FAILURE);
			addTimeout("Mission-16-Fire", 13000);
		}
		removeFleets();
		cleanupScriptedFleets();
		return result;
	}
	@Override
	public void onSpacewarStart(SpacewarWorld war) {
		if (isMissionSpacewar(war.battle(), "Mission-16")) {
			Player g = garthog();
			startJointSpaceBattle(war, ALLY, player, ENEMY, g);
		}
	}
	@Override
	public void onAutobattleStart(BattleInfo battle) {
		if (isMissionSpacewar(battle, "Mission-16")) {
			Player g = garthog();
			startJointAutoSpaceBattle(battle, ALLY, player, ENEMY, g);
		}
	}
	/**
	 * Create the garthog fleet.
	 */
	void createGarthog() {
		Planet g1 = planet("San Sterling");
		Player g = garthog();
		Fleet f = createFleet(label("Garthog.fleet"), g, g1.x - 3, g1.y - 3);

		//---------------------------------
		
		f.addInventory(research("GarthogFighter"), 4);
		f.addInventory(research("GarthogDestroyer"), 2);
		
		//---------------------------------
		
		tagFleet(f, ENEMY);
		
		f.task = FleetTask.SCRIPT;
		helper.scriptedFleets().add(f.id);
	}
	/** Create the garthog attack. */
	void garthogAttack() {
		createGarthog();
		world.env.computerSound(SoundType.CARRIER_UNDER_ATTACK);
		
		world.env.speed1();
		
		addMission("Mission-16-Timeout", 12);

		Pair<Fleet, InventoryItem> carrier = findTaggedFleet(ALLY, player);
		Fleet ff = getFollower(carrier.first, player);
		if (ff != null) {
			Pair<Fleet, InventoryItem> garthog = findTaggedFleet(ENEMY, garthog());
			ff.attack(garthog.first);
		}

	}
}
