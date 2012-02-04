/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Pair;
import hu.openig.mechanics.DefaultAIControls;
import hu.openig.model.BattleInfo;
import hu.openig.model.Building;
import hu.openig.model.Fleet;
import hu.openig.model.FleetMode;
import hu.openig.model.FleetTask;
import hu.openig.model.GroundwarWorld;
import hu.openig.model.InventoryItem;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.Player;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.SoundType;
import hu.openig.model.SpacewarWorld;

import java.util.Collections;
import java.util.List;

/**
 * @author akarnokd, Jan 22, 2012
 *
 */
public class Mission11 extends Mission {
	@Override
	public boolean applicable() {
		return world.level == 2;
	}
	@Override
	public void onTime() {
		Objective m10 = helper.objective("Mission-10");
		Objective m11 = helper.objective("Mission-11");
		if (m10.state != ObjectiveState.ACTIVE
				&& !m11.visible && m11.state == ObjectiveState.ACTIVE
				&& !helper.hasMissionTime("Mission-11")) {
			helper.setMissionTime("Mission-11", helper.now() + 7 * 24);
		}
		if (checkMission("Mission-11")) {
			helper.showObjective("Mission-11");
			createGarthog();
		}
		if (checkTimeout("Mission-11-Done")) {
			if (m11.state == ObjectiveState.FAILURE) {
				helper.gameover();
				loseGameMessageAndMovie("Douglas-Fire-Lost-Planet-2", "loose/fired_level_2");
			}
			m11.visible = false;
		}
		if (checkTimeout("Mission-11-Planet")) {
			Pair<Fleet, InventoryItem> garthog = findTaggedFleet("Mission-11-Garthog", player("Garthog"));
			if (garthog != null) {
				incomingMessage(garthog.first.targetPlanet().id + "-Is-Under-Attack");
			}
		}
	}
	/**
	 * Create the attacking garthog fleet.
	 */
	void createGarthog() {
		Planet from = planet("Garthog 1");
		Player garthog = player("Garthog");
		Fleet f = createFleet(label("Garthog.fleet"), garthog, from.x, from.y);
		// --------------------------------------------------
		// Adjust attacker strength here
		f.addInventory(research("GarthogFighter"), 20);
		f.addInventory(research("GarthogDestroyer"), 10);
		f.addInventory(research("GarthogBattleship"), 2);
		f.addInventory(research("LightTank"), 6);
		f.addInventory(research("RadarCar"), 1);
		f.addInventory(research("GarthogRadarJammer"), 1);
		// ---------------------------------------------------
		
		for (InventoryItem ii : f.inventory) {
			ii.tag = "Mission-11-Garthog";
		}
		Planet target = null;
		double strength = Double.MAX_VALUE;
		// No Cheat: doesn't check garrison count, only things that can be seen by spysat2
		List<Planet> ps = player.ownPlanets();
		Collections.shuffle(ps, world.random());
		for (Planet p : ps) {
			double sp = 0;
			// check station strength
			for (InventoryItem ii : p.inventory) {
				if (ii.owner == player && ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
					sp += world.getHitpoints(ii.type);
				}
			}
			// check gun and shield strength, check fortification strength
			for (Building b : p.surface.buildings) {
				if (b.type.kind.equals("Gun")) {
					sp += world.getHitpoints(b.type, player, true);
				}
				if (b.type.kind.equals("Shield")) {
					sp += world.getHitpoints(b.type, player, true);
				}
				if (b.type.kind.equals("Defensive")) {
					sp += world.getHitpoints(b.type, player, false);
				}
			}
			
			if (sp < strength) {
				strength = sp;
				target = p;
			}
		}
		
		f.targetPlanet(target);

		garthog.changeInventoryCount(research("SpySatellite1"), 1);
		DefaultAIControls.actionDeploySatellite(garthog, target, research("SpySatellite1"));
		garthog.planets.put(target, PlanetKnowledge.BUILDING);
		
		f.mode = FleetMode.ATTACK;
		f.task = FleetTask.SCRIPT;
	}
	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		if (helper.isActive("Mission-11")) {
			Pair<Fleet, InventoryItem> garthog = findTaggedFleet("Mission-11-Garthog", player("Garthog"));
			if (garthog == null) {
				helper.setObjectiveState("Mission-11", ObjectiveState.SUCCESS);
				war.battle().messageText = label("battlefinish.mission-5.garthog");
				helper.setTimeout("Mission-11-Done", 13000);
				clearMessages();
				cleanupScriptedFleets();
			}
		}		
	}
	@Override
	public void onDiscovered(Player player, Fleet fleet) {
		if (helper.isActive("Mission-11")) {
			if (player == this.player && hasTag(fleet, "Mission-11-Garthog")) {
				world.env.speed1();
				world.env.computerSound(SoundType.ENEMY_FLEET_DETECTED);
				helper.setTimeout("Mission-11-Planet", 2000);
			}
		}
	}
	@Override
	public void onGroundwarFinish(GroundwarWorld war) {
		onAutobattleFinish(war.battle());
	}
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		if (helper.isActive("Mission-11") 
				&& battle.targetPlanet != null
				&& battle.attacker.owner.id.equals("Garthog")) {
			if (battle.targetPlanet.owner != player) {
				helper.setObjectiveState("Mission-11", ObjectiveState.FAILURE);
			} else {
				helper.setObjectiveState("Mission-11", ObjectiveState.SUCCESS);
			}
			helper.setTimeout("Mission-11-Done", 13000);
			Pair<Fleet, InventoryItem> garthog = findTaggedFleet("Mission-11-Garthog", player("Garthog"));
			if (garthog != null) {
				helper.scriptedFleets().remove(garthog.first.id);
			}
			cleanupScriptedFleets();
			clearMessages();
		}
	}
	/** Hide receive attack messages. */
	void clearMessages() {
		String[] planets = { "Achilles", "Naxos", "San Sterling", "Centronom", "New Caroline" };
		for (String s : planets) {
			helper.receive(s + "-Is-Under-Attack").visible = false;
		}
	}
}
