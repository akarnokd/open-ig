/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

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
import hu.openig.model.TraitKind;

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
		if (world.level == 2) {
			removeMissions(1, 25);
			// ensure the initial fleet conditions are met
			player.setAvailable(research("Fighter2"));
			createMainShip();
			
			addMission("Mission-6", 1);
			world.achievement("achievement.captain");
			
			send("Douglas-Reinforcements-Denied").visible = true;
			
			player.populateProductionHistory();
		}
	}
	/**
	 * Creates the main ship for level 2.
	 */
	void createMainShip() {
		Planet ach = planet("Achilles");
		Fleet own = findTaggedFleet("CampaignMainShip2", player);
		Fleet f;
		if (own == null) {
			own = findTaggedFleet("CampaignMainShip1", player);
			if (own != null) {
				f = own;
			} else {
				f = createFleet(label("Empire.main_fleet"), player, ach.x + 5, ach.y + 5);
			}
			addInventory(f, "Fighter2", 2);
			addInventory(f, "Cruiser1", 1);
			for (InventoryItem ii : f.inventory.iterable()) {
				if (ii.type.id.equals("Cruiser1")) {
					ii.tag = "CampaignMainShip2";
					setSlot(ii, "laser", "Laser1", 6);
					setSlot(ii, "shield", "Shield1", 1);
					setSlot(ii, "cannon", "IonCannon", 1);
					if (!player.traits.has(TraitKind.PRE_WARP)) {
						setSlot(ii, "hyperdrive", "HyperDrive1", 1);
					}
				}
			}
			// move further away
		} else {
			f = own;
		}
		f.x = ach.x - 50;
		f.y = ach.y + 50;
	}
	@Override
	public void onTime() {
		checkPlanetStateMessages();
		checkMainShip();
		if (checkMission("Mission-6")) {
			showObjective("Mission-6");
			createAttackers();
		}
		Objective m6 = objective("Mission-6");
		if (checkTimeout("Mission-6-Failure")) {
			gameover();
			loseGameMessageAndMovie("Douglas-Fire-Lost-Planet", "lose/fired_level_2");
		}
		if (checkTimeout("Mission-6-Done")) {
			m6.visible = false;
			Fleet garthog = findTaggedFleet("Mission-6-Garthog", player("Garthog"));
			if (garthog != null) {
				removeScripted(garthog);
				world.removeFleet(garthog);
			}
			addTimeout("Mission-6-Task-1", 4000);
			addTimeout("Mission-6-Task-2", 8000);
		}
		if (checkTimeout("Mission-6-Task-1")) {
			showObjective("Mission-6-Task-1");
		}
		if (checkTimeout("Mission-6-Task-2")) {
			showObjective("Mission-6-Task-2");
		}
	}
	/**
	 * Display planet send messages based on current situation.
	 */
	void checkPlanetStateMessages() {
		String[] planets = { "Achilles", "Naxos", "San Sterling", "New Caroline", "Centronom" };
		
		setPlanetMessages(planets);
		if (objective("Mission-6").isActive()) {
			send("Achilles-Check").visible = false;
			send("Achilles-Come-Quickly").visible = true;
			send("Achilles-Not-Under-Attack").visible = false;
		}
	}
	/**
	 * Check distance to Achilles.
	 */
	void checkDistance() {
		Fleet garthog = findTaggedFleet("Mission-6-Garthog", player("Garthog"));
		if (garthog != null) {
			if (garthog.mode != FleetMode.ATTACK) {
				Planet ach = planet("Achilles");
				double d = Math.hypot(ach.x - garthog.x, ach.y - garthog.y);
				if (d <= 1) {
					garthog.targetPlanet(ach);
					garthog.mode = FleetMode.ATTACK;
					garthog.task = FleetTask.SCRIPT;
				}
			}
		}
	}
	@Override
	public void onSpacewarStart(SpacewarWorld war) {
		if (objective("Mission-6").isActive()) {
			Fleet garthog = findTaggedFleet("Mission-6-Garthog", player("Garthog"));
			if (garthog != null 
					&& (war.battle().attacker == garthog
					|| war.battle().targetFleet == garthog)) {
				war.battle().chat = "chat.mission-6.defend.Achilles";
			}
		}
	}
	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		if (objective("Mission-6").isActive()) {
			Fleet garthog = findTaggedFleet("Mission-6-Garthog", player("Garthog"));
			if (garthog == null || !canGroundAttack(garthog)) {
				setObjectiveState("Mission-6", ObjectiveState.SUCCESS);
				war.battle().rewardText = label("battlefinish.mission-6.14_bonus");
				war.battle().messageText = label("battlefinish.mission-6.14");
				
				world.achievement("achievement.defender");
				addTimeout("Mission-6-Done", 13000);
			}
			if (garthog != null) {
				removeScripted(garthog);
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
		if (objective("Mission-6").isActive()) {
			Planet ach = planet("Achilles");
			if (ach.owner != player) {
				setObjectiveState("Mission-6", ObjectiveState.FAILURE);
				addTimeout("Mission-6-Failure", 1000);
			} else {
				setObjectiveState("Mission-6", ObjectiveState.SUCCESS);
				
				world.achievement("achievement.defender");
			}
			addTimeout("Mission-6-Done", 13000);
			Fleet garthog = findTaggedFleet("Mission-6-Garthog", player("Garthog"));
			if (garthog != null) {
				removeScripted(garthog);
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
		Fleet f = createFleet(format("fleet", garthog.shortName), garthog, from.x + 20, from.y + 10);
		// --------------------------------------------------
		addInventory(f, "GarthogFighter", 10);
		equipFully(addInventory(f, "GarthogDestroyer", 3));
		equipFully(addInventory(f, "GarthogBattleship", 2));
		addInventory(f, "LightTank", 6);
		addInventory(f, "RadarCar", 1);
		addInventory(f, "GarthogRadarJammer", 1);
		// ---------------------------------------------------
		
		for (InventoryItem ii : f.inventory.iterable()) {
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
	/**
	 * Check if the main ship still exists.
	 */
	void checkMainShip() {
		Fleet ft = findTaggedFleet("CampaignMainShip2", player);
		if (ft == null) {
			if (!hasTimeout("MainShip-Lost")) {
				addTimeout("MainShip-Lost", 3000);
			}
			if (checkTimeout("MainShip-Lost")) {
				gameover();
				loseGameMovie("lose/destroyed_level_2");
			}
		}
	}
}
