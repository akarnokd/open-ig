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
import hu.openig.model.SpacewarWorld;

/**
 * Mission 6: Defend Achilles.
 * @author akarnokd, 2012.01.18.
 */
public class Mission6 extends Mission {
	@Override
	public boolean applicable() {
		return world.level == 2;
	}
	@Override
	public void onLevelChanged() {
		removeMissions(1, 25);

		if (world.level == 2) {
			// ensure the initial fleet conditions are met
			player.setAvailable(research("Fighter2"));
			createMainShip();
			
			helper.setMissionTime("Mission-6", helper.now());
			String a = "achievement.captain";
			if (!world.env.profile().hasAchievement(a)) {
				world.env.achievementQueue().add(a);
				world.env.profile().grantAchievement(a);
			}
		}
	}
	/**
	 * Creates the main ship for level 2.
	 */
	void createMainShip() {
		Planet ach = planet("Achilles");
		Pair<Fleet, InventoryItem> own = findTaggedFleet("CampaignMainShip2", player);
		Fleet f = null;
		if (own == null) {
			own = findTaggedFleet("CampaignMainShip1", player);
			if (own != null) {
				f = own.first;
			} else {
				f = createFleet(label("Empire.main_fleet"), player, ach.x + 5, ach.y + 5);
			}
			f.addInventory(research("Fighter2"), 2);
			f.addInventory(research("Cruiser1"), 1);
			for (InventoryItem ii : f.inventory) {
				if (ii.type.id.equals("Cruiser1")) {
					ii.tag = "CampaignMainShip2";
					setSlot(ii, "laser", "Laser1", 6);
					setSlot(ii, "shield", "Shield1", 1);
					setSlot(ii, "cannon", "IonCannon", 1);
					setSlot(ii, "hyperdrive", "HyperDrive1", 1);
				}
			}
			// move further away
		} else {
			f = own.first;
		}
		f.x = ach.x - 50;
		f.y = ach.y + 50;
	}
	@Override
	public void onTime() {
		checkPlanetStateMessages();
		checkMainShip();
		if (helper.canStart("Mission-6")) {
			helper.showObjective("Mission-6");
//			incomingMessage("Achilles-Is-Under-Attack");
			createAttackers();
		}
		Objective m6 = helper.objective("Mission-6");
		if (checkTimeout("Mission-6-Done")) {
			helper.receive("Achilles-Is-Under-Attack").visible = false;
			if (m6.state == ObjectiveState.FAILURE) {
				helper.gameover();
				loseGameMessageAndMovie("Douglas-Fire-Lost-Planet", "loose/fired_level_2");
			}
			m6.visible = false;
			Pair<Fleet, InventoryItem> garthog = findTaggedFleet("Mission-6-Garthog", player("Garthog"));
			if (garthog != null) {
				removeScripted(garthog.first);
				world.removeFleet(garthog.first);
			}
		}
	}
	/**
	 * Display planet send messages based on current situation.
	 */
	void checkPlanetStateMessages() {
		String[] planets = { "Achilles", "Naxos", "San Sterling", "New Caroline", "Centronom" };
		
		setPlanetMessages(planets);
		if (helper.isActive("Mission-6")) {
			helper.send("Achilles-Check").visible = false;
			helper.send("Achilles-Come-Quickly").visible = true;
			helper.send("Achilles-Not-Under-Attack").visible = false;
		}
	}
	/**
	 * Check distance to Achilles.
	 */
	void checkDistance() {
		Pair<Fleet, InventoryItem> garthog = findTaggedFleet("Mission-6-Garthog", player("Garthog"));
		if (garthog != null) {
			if (garthog.first.mode != FleetMode.ATTACK) {
				Planet ach = planet("Achilles");
				double d = Math.hypot(ach.x - garthog.first.x, ach.y - garthog.first.y);
				if (d <= 1) {
					garthog.first.targetPlanet(ach);
					garthog.first.mode = FleetMode.ATTACK;
					garthog.first.task = FleetTask.SCRIPT;
				}
			}
		}
	}
	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		if (helper.isActive("Mission-6")) {
			Pair<Fleet, InventoryItem> garthog = findTaggedFleet("Mission-6-Garthog", player("Garthog"));
			if (garthog == null) {
				helper.setObjectiveState("Mission-6", ObjectiveState.SUCCESS);
				war.battle().rewardText = label("battlefinish.mission-6.14_bonus");
				war.battle().messageText = label("battlefinish.mission-6.14");
			} else {
				helper.scriptedFleets().remove(garthog.first.id);
				cleanupScriptedFleets();
			}
		}		
	}
	@Override
	public void onGroundwarFinish(GroundwarWorld war) {
		onAutobattleFinish(war.battle());
	}
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		if (helper.isActive("Mission-6")) {
			Planet ach = planet("Achilles");
			if (ach.owner != player) {
				helper.setObjectiveState("Mission-6", ObjectiveState.FAILURE);
			} else {
				helper.setObjectiveState("Mission-6", ObjectiveState.SUCCESS);
			}
			helper.setTimeout("Mission-6-Done", 13000);
			Pair<Fleet, InventoryItem> garthog = findTaggedFleet("Mission-6-Garthog", player("Garthog"));
			if (garthog != null) {
				helper.scriptedFleets().remove(garthog.first.id);
			}
			cleanupScriptedFleets();
		}
	}
	/**
	 * Create the attacking garthog fleet.
	 */
	void createAttackers() {
		Planet from = planet("Achilles");
		Player garthog = player("Garthog");
		Fleet f = createFleet(label("Garthog.fleet"), garthog, from.x + 20, from.y + 10);
		// --------------------------------------------------
		f.addInventory(research("GarthogFighter"), 10);
		f.addInventory(research("GarthogDestroyer"), 3);
		f.addInventory(research("GarthogBattleship"), 2);
		f.addInventory(research("LightTank"), 6);
		f.addInventory(research("RadarCar"), 1);
		f.addInventory(research("GarthogRadarJammer"), 1);
		// ---------------------------------------------------
		
		for (InventoryItem ii : f.inventory) {
			ii.tag = "Mission-6-Garthog";
		}
		Planet ach = planet("Achilles");
		f.targetPlanet(ach);
		f.mode = FleetMode.ATTACK;
		f.task = FleetTask.SCRIPT;
		garthog.changeInventoryCount(research("SpySatellite1"), 1);
		DefaultAIControls.actionDeploySatellite(garthog, ach, research("SpySatellite1"));
		garthog.planets.put(ach, PlanetKnowledge.BUILDING);
	}
//	@Override
//	public void onDiscovered(Player player, Fleet fleet) {
//		if (helper.isActive("Mission-6")) {
//			if (player == this.player && hasTag(fleet, "Mission-6-Garthog")) {
//				world.env.speed1();
//				world.env.computerSound(SoundType.ENEMY_FLEET_DETECTED);
//			}
//		}
//	}
	/**
	 * Check if the main ship still exists.
	 */
	void checkMainShip() {
		Pair<Fleet, InventoryItem> ft = findTaggedFleet("CampaignMainShip2", player);
		if (ft == null) {
			if (!helper.hasTimeout("MainShip-Lost")) {
				helper.setTimeout("MainShip-Lost", 3000);
			}
			if (helper.isTimeout("MainShip-Lost")) {
				helper.gameover();
				loseGameMovie("loose/destroyed_level_2");
			}
		}
	}
}
