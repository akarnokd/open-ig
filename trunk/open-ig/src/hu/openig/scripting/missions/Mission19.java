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
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchType;
import hu.openig.model.SoundType;
import hu.openig.model.SpacewarScriptResult;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarWorld;
import hu.openig.utils.XElement;

/**
 * Mission 19: blockade of Zeuson.
 * @author akarnokd, 2012.02.23.
 */
public class Mission19 extends Mission {
	/** Mission stages. */
	enum M19 {
		/** Not started yet. */
		NONE,
		/** Waiting for message. */
		INIT_WAIT,
		/** Message. */
		INTRO,
		/** Message. */
		MESSAGE,
		/** Wait for objective. */
		OBJECTIVE_WAIT,
		/** Fleet appear wait. */
		APPEAR_WAIT,
		/** Chase. */
		RUN,
		/** Done. */
		DONE
	}
	/** The governor's ship type. */
	protected final String governorShipType = "TradersFreight2";
	/** The mission stages. */
	protected M19 stage = M19.NONE;
	/** Initial hp of the governor fleet. */
	protected int initialHP;
	/** The original relation with the free traders. */
	protected double originalRelation;
	@Override
	public boolean applicable() {
		return world.level == 3;
	}
	@Override
	public void save(XElement xmission) {
		xmission.set("stage", stage);
		xmission.set("original-relation", originalRelation);
	}
	@Override
	public void load(XElement xmission) {
		stage = M19.valueOf(xmission.get("stage"));
		originalRelation = xmission.getDouble("original-relation", 50);
	}
	@Override
	public void reset() {
		stage = M19.NONE;
	}
	@Override
	public void onTime() {
		if (stage == M19.NONE) {
			stage = M19.INIT_WAIT;
			addMission("Mission-19", 7 * 24);
		}
		
		if (checkMission("Mission-19")) {
			stage = M19.INTRO;
			world.env.stopMusic();
			world.env.playVideo("interlude/take_prisoner", new Action0() {
				@Override
				public void invoke() {
					stage = M19.MESSAGE;
					world.env.playMusic();
				}
			});
		}
		if (stage == M19.MESSAGE) {
			incomingMessage("Douglas-Rebel-Governor");
			addTimeout("Mission-19-Objectives", 3000);
			stage = M19.OBJECTIVE_WAIT;
		}
		if (checkTimeout("Mission-19-Objectives")) {
			helper.showObjective("Mission-19");
			stage = M19.APPEAR_WAIT;
			addMission("Mission-19-Appear", 3 * 24);
		}
		if (checkMission("Mission-19-Appear")) {
			stage = M19.RUN;
			createGovernor();
		}
		if (stage == M19.RUN) {
			checkGovernorPosition();
		}
		if (checkTimeout("Mission-19-Hide")) {
			helper.objective("Mission-19").visible = false;
		}
		if (checkTimeout("Mission-19-Failure")) {
			stage = M19.DONE;
			helper.gameover();
			loseGameMessageAndMovie("Douglas-Fire-Battle", "loose/fired_level_3");
		}
	}
	/**
	 * Check if the governor left the sector.
	 */
	void checkGovernorPosition() {
		Pair<Fleet, InventoryItem> f = findTaggedFleet("Mission-19-Governor", freeTraders());
		if (f != null) {
			if (!player.explorationOuterLimit.contains(f.first.x, f.first.y)) {
				failMission(f);
			}
		}		
	}
	/**
	 * Create the governor's fleet.
	 */
	void createGovernor() {
		Player ft = freeTraders();
		Planet z = planet("Zeuson");
		Fleet f = createFleet(label("mission-19.unknown"), ft, z.x, z.y);
		// -----------------
		
		f.addInventory(research(governorShipType), 1);
		f.addInventory(research("Fighter1"), 2);
		
		// -----------------
		tagFleet(f, "Mission-19-Governor");
		
		f.moveTo(z.x + 300, z.y);
		f.task = FleetTask.SCRIPT;
		addScripted(f);
		
		world.env.speed1();
		world.env.effectSound(SoundType.UNKNOWN_SHIP);
		
		originalRelation = world.establishRelation(player, ft).value;
	}
	/** @return the free trader player. */
	private Player freeTraders() {
		return player("FreeTraders");
	}
	@Override
	public void onFleetAt(Fleet fleet, Planet planet) {
		if (planet.id.equals("Zeuson") && fleet.owner.id.equals("FreeTraders") 
				&& hasTag(fleet, "Mission-19-Governor")) {
			world.removeFleet(fleet);
			removeScripted(fleet);
		}
	}
	@Override
	public void onSpacewarStart(SpacewarWorld war) {
		if (stage == M19.RUN && isMissionSpacewar(war.battle(), "Mission-19")) {
			initialHP = 0;
			for (SpacewarStructure s : war.structures(freeTraders())) {
				initialHP += s.hp;
			}
			war.battle().chat = "chat.mission-19.rebel.Zeuson.governor";
		}
	}
	@Override
	public SpacewarScriptResult onSpacewarStep(SpacewarWorld war) {
		if (stage == M19.RUN && isMissionSpacewar(war.battle(), "Mission-19")) {
			int currentHP = 0;
			for (SpacewarStructure s : war.structures(freeTraders())) {
				currentHP += s.hp;
			}
			if (currentHP * 2 < initialHP) {
				for (SpacewarStructure s : war.structures(freeTraders())) {
					war.flee(s);
				}			
				war.battle().enemyFlee = true;
			} else {
				for (SpacewarStructure s : war.structures(freeTraders())) {
					war.move(s, Math.cos(s.angle) * 1000, s.y);
				}
			}
			return SpacewarScriptResult.CONTINUE;
		}
		return null;
	}
	
	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		BattleInfo battle = war.battle();
		if (stage == M19.RUN && isMissionSpacewar(battle, "Mission-19")) {
			battleFinish(battle);
		}
	}
	/**
	 * Battle finished.
	 * @param battle the battle
	 */
	void battleFinish(BattleInfo battle) {
		Pair<Fleet, InventoryItem> f = findTaggedFleet("Mission-19-Governor", freeTraders());
		if (f != null) {
			ResearchType gr = research(governorShipType);
			InventoryItem ii = f.first.getInventoryItem(gr);
			if (ii != null && battle.enemyFlee) {
				// governor survived
				f.first.moveTo(planet("Zeuson"));
				f.first.task = FleetTask.SCRIPT;
				
				helper.setObjectiveState("Mission-19", ObjectiveState.SUCCESS);
				helper.receive("Douglas-Rebel-Governor").visible = false;
				addTimeout("Mission-19-Hide", 13000);
				
				battle.messageText = label("battlefinish.mission-19.31");
				battle.rewardImage = "battlefinish/mission_31";
				
				world.establishRelation(player, ii.owner).value = originalRelation;
				
				return;
			} else
			if (ii == null) {
				failMission(f);
			}
		} else {
			failMission(f);
		}
		cleanupScriptedFleets();
	}
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		if (stage == M19.RUN && isMissionSpacewar(battle, "Mission-19")) {
			battleFinish(battle);
		}
	}
	
	/**
	 * Fail the mission.
	 * @param f the governor's fleet
	 */
	void failMission(Pair<Fleet, InventoryItem> f) {
		stage = M19.DONE;
		if (f != null) {
			world.removeFleet(f.first);
			removeScripted(f);
		}
		helper.setObjectiveState("Mission-19", ObjectiveState.FAILURE);
		helper.receive("Douglas-Rebel-Governor").visible = false;
		addTimeout("Mission-19-Failure", 13000);
	}
}
