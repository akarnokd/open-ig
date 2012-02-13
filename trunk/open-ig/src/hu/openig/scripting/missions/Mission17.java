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
import hu.openig.model.SpacewarScriptResult;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarWorld;
import hu.openig.utils.XElement;

/**
 * Destroy the prototype.
 * @author akarnokd, 2012.02.13.
 */
public class Mission17 extends Mission {
	/** Stages. */
	enum M17 {
		/** Not yet started. */
		NONE,
		/** Wait for init. */
		WAIT,
		/** Intro. */
		INTRO,
		/** Message. */
		MESSAGE,
		/** Running. */
		RUN,
		/** Done. */
		DONE
	}
	/** The stage. */
	protected M17 stage = M17.NONE;
	/** The target planet. */
	protected final String target = "Garthog 2";
	@Override
	public void onTime() {
		if (stage == M17.NONE && helper.objective("Mission-16").state != ObjectiveState.ACTIVE) {
			stage = M17.WAIT;
			addMission("Mission-17", 7 * 24);
		}
		if (checkMission("Mission-17")) {
			stage = M17.INTRO;
			world.env.stopMusic();
			world.env.playVideo("interlude/retake_prototype", new Action0() {
				@Override
				public void invoke() {
					world.env.speed1();
					createGarthog();
					incomingMessage("Douglas-Prototype");
					addTimeout("Mission-17-Objective", 3000);
					world.env.playMusic();
					stage = M17.RUN;
				}
			});
		}
		if (checkTimeout("Mission-17-Objective")) {
			helper.showObjective("Mission-17");
		}
		if (checkTimeout("Mission-17-Failed")) {
			stage = M17.DONE;
			helper.gameover();
			helper.receive("Douglas-Prototype").visible = false;
			loseGameMessageAndMovie("Douglas-Fire-Prototype-Lost", "loose/fired_level_2");
		}
		if (checkTimeout("Mission-17-Success")) {
			helper.receive("Douglas-Prototype").visible = false;
			helper.objective("Mission-17").visible = false;
			stage = M17.DONE;
			incomingMessage("Douglas-Prototype-Success"); // TODO
		}
	}
	@Override
	public void onMessageSeen(String id) {
		if ("Douglas-Prototype-Success".equals(id)) {
			world.env.stopMusic();
			world.env.playVideo("interlude/level_3_intro", new Action0() {
				@Override
				public void invoke() {
					promote();
					helper.receive("Douglas-Prototype-Success").visible = false;
				}
			});
		}
	}
	/**
	 * Promotion action.
	 */
	void promote() {
		world.level = 3;
		world.env.playMusic();
	}
	/** Create the garthog fleet. */
	void createGarthog() {
		Player garthog = player("Garthog");
		
		Fleet f = createFleet(label("mission-17.prototype"), garthog, player.explorationOuterLimit.x, player.explorationOuterLimit.y);
		
		// ------------------------------------
		
		f.addInventory(research("GarthogBattleship"), 2);
		f.addInventory(research("GarthogDestroyer"), 2);
		f.addInventory(research("GarthogFighter"), 3);
		f.addInventory(research("Destroyer2"), 1);

		InventoryItem ii = f.getInventoryItem(research("Destroyer2"));
		
		setSlot(ii, "laser1", "Laser1", 6);
		setSlot(ii, "laser2", "Laser1", 6);
		
		//-------------------------------------
		
		tagFleet(f, "Mission-17-Garthog");
		ii.tag = "Mission-17-Prototype";
		
		Planet g2 = planet(target);
		f.moveTo(g2);
		f.task = FleetTask.SCRIPT;
		addScripted(f);
	}
	
	@Override
	public void onFleetAt(Fleet fleet, Planet planet) {
		if (planet.id.equals(target) && hasTag(fleet, "Mission-17-Prototype")) {
			helper.setObjectiveState("Mission-17", ObjectiveState.FAILURE);
			addTimeout("Mission-17-Failed", 13000);
			
			world.removeFleet(fleet);
			removeScripted(fleet);
		}
	}
	
	@Override
	public SpacewarScriptResult onSpacewarStep(SpacewarWorld war) {
		if (!isMissionSpacewar(war.battle(), "Mission-17")) {
			return null;
		}
		Player g = player("Garthog");
		for (SpacewarStructure s : war.structures(g)) {
			if (s.item != null && "Mission-17-Prototype".equals(s.item.tag)) {
				return SpacewarScriptResult.CONTINUE;
			}
		}
		for (SpacewarStructure s : war.structures(g)) {
			war.stop(s);
			war.move(s, war.space().width - 10, s.y);
		}
		
		return SpacewarScriptResult.PLAYER_WIN;
	}
	
	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		onAutobattleFinish(war.battle());
	}
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		if (!isMissionSpacewar(battle, "Mission-17")) {
			return;
		}
		Player garthog = player("Garthog");
		Pair<Fleet, InventoryItem> proto = findTaggedFleet("Mission-17-Prototype", garthog);
		if (proto == null) {
			
			battle.rewardImage = "battlefinish/mission_21";
			battle.messageText = label("battlefinish.mission-17.21");
			battle.rewardText = label("battlefinish.mission-17.21_bonus");
			
			Pair<Fleet, InventoryItem> gf = findTaggedFleet("Mission-17-Garthog", garthog);
			if (gf != null) {
				gf.first.inventory.clear();
				removeScripted(gf.first);
				world.removeFleet(gf.first);
			}
			
			helper.setObjectiveState("Mission-17", ObjectiveState.SUCCESS);
			addTimeout("Mission-17-Success", 13000);
			cleanupScriptedFleets();
		}
	}
	
	@Override
	public boolean applicable() {
		return world.level == 2;
	}
	@Override
	public void save(XElement xmission) {
		xmission.set("stage", stage);
	}
	@Override
	public void load(XElement xmission) {
		stage = M17.valueOf(xmission.get("stage"));
	}
	@Override
	public void reset() {
		stage = M17.NONE;
	}
}
