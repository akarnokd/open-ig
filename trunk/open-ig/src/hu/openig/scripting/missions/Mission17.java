/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Action0;
import hu.openig.model.BattleInfo;
import hu.openig.model.Building;
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
	protected static final String TARGET = "Garthog 2";
	@Override
	public void onTime() {
		if (stage == M17.NONE && objective("Mission-16").state != ObjectiveState.ACTIVE) {
			stage = M17.WAIT;
			addMission("Mission-17", 7 * 24);
			addMission("Mission-6-Close", 6 * 24);
		}
		if (checkMission("Mission-6-Close")) {
			// check defenses
			boolean t1 = false;
			outer:
			for (Planet p : player.ownPlanets()) {
				for (Building b : p.surface.buildings.iterable()) {
					if (b.type.kind.equals("Defensive")) {
						setObjectiveState("Mission-6-Task-1", ObjectiveState.SUCCESS);
						t1 = true;
						break outer;
					}
				}
			}
			if (!t1) {
				setObjectiveState("Mission-6-Task-1", ObjectiveState.FAILURE);
			}
			addTimeout("Mission-6-Close-2", 4000);
		}
		if (checkTimeout("Mission-6-Close-2")) {
			// check production count
			if (player.statistics.productionCount.value > 0) {
				setObjectiveState("Mission-6-Task-2", ObjectiveState.SUCCESS);
			} else {
				setObjectiveState("Mission-6-Task-2", ObjectiveState.FAILURE);
			}
			addTimeout("Mission-6-Hide-3", 13000);
		}
		if (checkTimeout("Mission-6-Hide-3")) {
			objective("Mission-6-Task-1").visible = false;
			objective("Mission-6-Task-2").visible = false;
		}
		if (checkMission("Mission-17")) {
			stage = M17.INTRO;
			world.env.stopMusic();
			world.env.playVideo("interlude/retake_prototype", new Action0() {
				@Override
				public void invoke() {
					world.env.speed1();
					createGarthog();
					incomingMessage("Douglas-Prototype", "Mission-17");
					world.env.playMusic();
					stage = M17.RUN;
				}
			});
		}
		if (checkTimeout("Mission-17-Failed")) {
			stage = M17.DONE;
			gameover();
			loseGameMessageAndMovie("Douglas-Fire-Prototype-Lost", "lose/fired_level_2");
		}
		if (checkTimeout("Mission-17-Success")) {
			setObjectiveState("Mission-17", ObjectiveState.SUCCESS);
			addTimeout("Mission-17-Done", 13000);
		}
		if (checkTimeout("Mission-17-Done")) {
			objective("Mission-17").visible = false;
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
		
		equipFully(addInventory(f, "GarthogBattleship", 2));
		equipFully(addInventory(f, "GarthogDestroyer", 2));
		addInventory(f, "GarthogFighter", 3);
		addInventory(f, "Destroyer2", 1);

		InventoryItem ii = f.getInventoryItem(research("Destroyer2"));
		
		setSlot(ii, "laser1", "Laser1", 6);
		setSlot(ii, "laser2", "Laser1", 6);
		
		//-------------------------------------
		
		tagFleet(f, "Mission-17-Garthog");
		ii.tag = "Mission-17-Prototype";
		
		Planet g2 = planet(TARGET);
		f.moveTo(g2);
		f.task = FleetTask.SCRIPT;
		addScripted(f);
	}
	
	@Override
	public void onFleetAt(Fleet fleet, Planet planet) {
		if (planet.id.equals(TARGET) && hasTag(fleet, "Mission-17-Prototype")) {
			setObjectiveState("Mission-17", ObjectiveState.FAILURE);
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
	public void onSpacewarStart(SpacewarWorld war) {
		if (!isMissionSpacewar(war.battle(), "Mission-17")) {
			return;
		}
		war.battle().chat = "chat.mission-17.stolen.destroyer2.prototype";
	}
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		if (!isMissionSpacewar(battle, "Mission-17")) {
			return;
		}
		Player garthog = player("Garthog");
		Fleet proto = findTaggedFleet("Mission-17-Prototype", garthog);
		if (proto == null) {
			
			battle.rewardImage = "battlefinish/mission_21";
			battle.messageText = label("battlefinish.mission-17.21");
			battle.rewardText = label("battlefinish.mission-17.21_bonus");
			
			Fleet gf = findTaggedFleet("Mission-17-Garthog", garthog);
			if (gf != null) {
				gf.inventory.clear();
				removeScripted(gf);
				world.removeFleet(gf);
			}

			addTimeout("Mission-17-Success", 1000);
			
			cleanupScriptedFleets();
		}
	}
	
	@Override
	public boolean applicable() {
		return world.level == 2;
	}
	@Override
	public void save(XElement xmission) {
		super.save(xmission);
		xmission.set("stage", stage);
	}
	@Override
	public void load(XElement xmission) {
		super.load(xmission);
		stage = M17.valueOf(xmission.get("stage"));
	}
	@Override
	public void reset() {
		stage = M17.NONE;
	}
}
