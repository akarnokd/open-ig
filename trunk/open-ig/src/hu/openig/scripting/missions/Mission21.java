/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Action0;
import hu.openig.core.Difficulty;
import hu.openig.core.Pair;
import hu.openig.model.BattleInfo;
import hu.openig.model.Fleet;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.FleetTask;
import hu.openig.model.InventoryItem;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchType;
import hu.openig.model.SpacewarWorld;
import hu.openig.utils.XElement;

/**
 * Mission 21: Escort prototype Destroyer 2.
 * @author akarnokd, 2012.02.23.
 */
public class Mission21 extends Mission {
	/** Stages. */
	enum M21 {
		/** Not started yet. */
		NONE,
		/** Wait. */
		WAIT,
		/** Message. */
		MESSAGE,
		/** Meet with the prototype. */
		MEET,
		/** Running. */
		RUN,
		/** Attack happened. */
		ATTACK,
		/** Done. */
		DONE
	}
	/** The current stage. */
	M21 stage = M21.NONE;
	/** The ally tag. */
	protected static final String ALLY = "Mission-21-Prototype";
	/** The enemy tag. */
	protected static final String ENEMY = "Mission-21-Garthog";
	@Override
	public boolean applicable() {
		return world.level == 3;
	}
	@Override
	public void save(XElement xmission) {
		xmission.set("stage", stage);
	}
	@Override
	public void load(XElement xmission) {
		stage = M21.valueOf(xmission.get("stage", M21.NONE.toString()));
	}
	@Override
	public void reset() {
		stage = M21.NONE;
	}
	@Override
	public void onTime() {
		if (stage == M21.NONE && helper.objective("Mission-19").state != ObjectiveState.ACTIVE) {
			stage = M21.WAIT;
			addMission("Mission-21", 14 * 24);
		}
		if (checkMission("Mission-21")) {
			incomingMessage("Douglas-Prototype-2");
			addTimeout("Mission-21-Objectives", 3000);
			stage = M21.MESSAGE;
		}
		if (checkTimeout("Mission-21-Objectives")) {
			helper.showObjective("Mission-21");
			stage = M21.MEET;
			createPrototype();
		}
		if (stage == M21.MEET) {
			if (checkNearStart()) {
				world.env.stopMusic();
				world.env.playVideo("interlude/escort", new Action0() {
					@Override
					public void invoke() {
						world.env.playMusic();
						movePrototype();
						stage = M21.RUN;
					}
				});
			}
		}
		if (stage == M21.RUN) {
			if (checkNearCentronom()) {
				createGarthog();
				stage = M21.ATTACK;
				addMission("Mission-21-Timeout", 12);
			}
		}
		if (checkMission("Mission-21-Timeout")) {
			removeFleets();
			helper.setObjectiveState("Mission-21", ObjectiveState.FAILURE);
			
			addTimeout("Mission-21-Hide", 13000);
			addTimeout("Mission-21-FailureMessage", 3000);
			addMission("Mission-21-Failure", 24);
		}
		if (checkTimeout("Mission-21-FailureMessage")) {
			incomingMessage("Douglas-Prototype-2-Failed");
		}
		if (checkMission("Mission-21-Failure")) {
			helper.send("Douglas-Prototype-2-Failed").visible = false;
		}
		if (checkTimeout("Mission-21-Hide")) {
			helper.receive("Douglas-Prototype-2").visible = false;
			helper.objective("Mission-21").visible = false;
		}
		if (checkTimeout("Mission-21-Thanks")) {
			incomingMessage("Prototype-Thanks");
			addMission("Mission-21-Thanks-Hide", 24); 
		}
		if (checkMission("Mission-21-Thanks-Hide")) {
			helper.receive("Prototype-Thanks").visible = false;
		}
	}
	/**
	 * Remove the mission fleets.
	 */
	void removeFleets() {
		Pair<Fleet, InventoryItem> benson = findTaggedFleet(ALLY, player);
		if (benson != null) {
			world.removeFleet(benson.first);
			removeScripted(benson);
		}
		removeGarthog();
	}
	/**
	 * Removes the garthog fleet.
	 */
	void removeGarthog() {
		Player g = garthog();
		Pair<Fleet, InventoryItem> garthog = findTaggedFleet(ENEMY, g);
		if (garthog != null) {
			world.removeFleet(garthog.first);
			removeScripted(garthog);
		}
	}
	/** @return Check if the fleet is close to centronom. */
	boolean checkNearCentronom() {
		Planet cent = planet("Centronom");
		Pair<Fleet, InventoryItem> f = findTaggedFleet("Mission-21-Prototype", player);
		if (f != null && Math.hypot(cent.x - f.first.x, cent.y - f.first.y) < 20) {
			return true;
		}
		return false;
	}
	/**
	 * Create the garthog attacker fleet.
	 */
	void createGarthog() {
		Pair<Fleet, InventoryItem> f = findTaggedFleet(ALLY, player);
		f.first.stop();
		f.first.task = FleetTask.SCRIPT;
		
		Player g = player("Garthog");
		
		Fleet gf = createFleet(label("Garthog.fleet"), g, f.first.x + 3, f.first.y + 3);
		
		// ----------------------------

		if (world.difficulty == Difficulty.EASY) {
			gf.addInventory(research("GarthogBattleship"), 1);
			gf.addInventory(research("GarthogDestroyer"), 5);
			gf.addInventory(research("GarthogFighter"), 15);
		} else {
			gf.addInventory(research("GarthogBattleship"), 1);
			gf.addInventory(research("GarthogDestroyer"), 10);
			gf.addInventory(research("GarthogFighter"), 30);
		}
		
		
		// ----------------------------
		tagFleet(gf, ENEMY);
		
		gf.task = FleetTask.SCRIPT;
		addScripted(gf);
		
		Pair<Fleet, InventoryItem> carrier = findTaggedFleet(ALLY, player);
		Fleet ff = getFollower(carrier.first, player);
		if (ff != null) {
			Pair<Fleet, InventoryItem> garthog = findTaggedFleet(ENEMY, g);
			ff.attack(garthog.first);
			player.fleets.put(garthog.first, FleetKnowledge.VISIBLE);
		}

	}
	
	/**
	 * Check if any of the player fleet is close to the prototype.
	 * @return true if a fleet is nearby
	 */
	boolean checkNearStart() {
		Pair<Fleet, InventoryItem> f = findTaggedFleet(ALLY, player);
		if (f != null) {
			for (Fleet f0 : player.ownFleets()) {
				if (f0 != f.first && Math.hypot(f0.x - f.first.x, f0.y - f.first.y) < 5) {
					return true;
				}
			}
		}	
		return false;
	}
	/**
	 * Create the prototype fleet.
	 */
	void createPrototype() {
		Planet z = planet("Zeuson");
		
		Fleet f = createFleet(label("mission-21.prototype2.name"), player, z.x, z.y);
		
		// -----------------------------------
		
		ResearchType d = research("Destroyer2");
		f.addInventory(d, 1);
		f.addInventory(research("Fighter2"), 4);
		
		InventoryItem ii = f.getInventoryItem(d);
		
		setSlot(ii, "laser1", "Laser1", 6);
		setSlot(ii, "laser2", "Laser2", 4);
		setSlot(ii, "shield", "Shield1", 1);
		
		// -----------------------------------
		
		tagFleet(f, ALLY);
		
		f.task = FleetTask.SCRIPT;
		addScripted(f);
	}
	/**
	 * Move the prototype to its destination.
	 */
	void movePrototype() {
		Planet cent = planet("Centronom");
		Pair<Fleet, InventoryItem> f = findTaggedFleet(ALLY, player);
		if (f != null) {
			f.first.moveTo(cent);
		}
	}
	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		BattleInfo battle = war.battle();
		if (stage == M21.ATTACK && isMissionSpacewar(battle, "Mission-21")) {
			if (concludeBattle(battle)) {
				battle.rewardImage = "battlefinish/mission_32";
				battle.messageText = label("battlefinish.mission-21.32");
				battle.rewardText = label("battlefinish.mission-21.32_bonus");
				player.setAvailable(research("Destroyer2"));
			}
		}
	}
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		if (stage == M21.ATTACK && isMissionSpacewar(battle, "Mission-21")) {
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
		helper.clearMissionTime("Mission-21-Timeout");
		Pair<Fleet, InventoryItem> carrier = findTaggedFleet(ALLY, player);

		if (carrier != null) {
			helper.setObjectiveState("Mission-21", ObjectiveState.SUCCESS);
			addTimeout("Mission-21-Hide", 13000);
			addTimeout("Mission-21-Thanks", 5000);
			result = true;
			movePrototype();
		} else {
			helper.setObjectiveState("Mission-21", ObjectiveState.FAILURE);
			
			addTimeout("Mission-21-Hide", 13000);
			addTimeout("Mission-21-FailureMessage", 3000);
			addMission("Mission-21-Failure", 24);
		}
		removeGarthog();
		cleanupScriptedFleets();
		stage = M21.DONE;
		return result;
	}
	@Override
	public void onFleetAt(Fleet fleet, Planet planet) {
		if (planet.id.equals("Centronom") 
				&& fleet.owner == player && hasTag(fleet, ALLY)) {
			world.removeFleet(fleet);
			removeScripted(fleet);
		}
	}
	@Override
	public void onSpacewarStart(SpacewarWorld war) {
		if (stage == M21.ATTACK && isMissionSpacewar(war.battle(), "Mission-21")) {
			Player g = garthog();
			startJointSpaceBattle(war, ALLY, player, ENEMY, g);
		}
	}
	@Override
	public void onAutobattleStart(BattleInfo battle) {
		if (stage == M21.ATTACK && isMissionSpacewar(battle, "Mission-21")) {
			Player g = garthog();
			startJointAutoSpaceBattle(battle, ALLY, player, ENEMY, g);
		}
	}
	/** @return the garthog player. */
	Player garthog() {
		return player("Garthog");
	}
}
