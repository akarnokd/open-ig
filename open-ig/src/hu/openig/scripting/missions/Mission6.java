/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Pair;
import hu.openig.model.BattleInfo;
import hu.openig.model.Fleet;
import hu.openig.model.FleetMode;
import hu.openig.model.FleetTask;
import hu.openig.model.GroundwarWorld;
import hu.openig.model.InventoryItem;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.SpacewarWorld;

import java.awt.geom.Point2D;

/**
 * Mission 6: Defend Achilles.
 * @author akarnokd, 2012.01.18.
 */
public class Mission6 extends Mission {
	@Override
	public void onLevelChanged() {
		if (world.level == 2) {
			removeMissions(6, 17);
			helper.send("Centronom-Check").visible = true;
			helper.send("New Caroline-Check").visible = true;

			// ensure the initial fleet conditions are met
			player.setAvailable(research("Fighter2"));
			createMainShip();
			
			helper.setMissionTime("Mission-6", helper.now() + 12);
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
		Pair<Fleet, InventoryItem> own = findTaggedFleet("CampaignMainShip2", player);
		if (own == null) {
			own = findTaggedFleet("CampaignMainShip1", player);
			Fleet f = null;
			if (own != null) {
				f = own.first;
			} else {
				Planet ach = planet("Achilles");
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
		}
	}
	@Override
	public void onTime() {
		if (world.level != 2) {
			return;
		}
		checkMainShip();
		if (helper.canStart("Mission-6")) {
			helper.showObjective("Mission-6");
			helper.setTimeout("Mission-6", 13000);
		}
		if (helper.isTimeout("Mission-6")) {
			helper.clearTimeout("Mission-6");
			createAttackers();
		}
		Objective m6 = helper.objective("Mission-6");
		if (m6.visible && m6.state == ObjectiveState.ACTIVE) {
			checkDistance();
		}
		if (helper.isTimeout("Mission-6-Done")) {
			helper.clearTimeout("Mission-6-Done");
			if (m6.state == ObjectiveState.FAILURE) {
				helper.gameover();
				loseGameMessageAndMovie("Douglas-Fire-Lost-Planet", "loose/fired_level_2");
			}
			m6.visible = false;
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
			}
		}		
	}
	@Override
	public void onGroundwarFinish(GroundwarWorld war) {
		if (helper.isActive("Mission-6")) {
			Planet ach = planet("Achilles");
			if (ach.owner != player) {
				helper.setObjectiveState("Mission-6", ObjectiveState.FAILURE);
			} else {
				helper.setObjectiveState("Mission-6", ObjectiveState.SUCCESS);
			}
			helper.setTimeout("Mission-6-Done", 13000);
		}
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
		}
	}
	/**
	 * Create the attacking garthog fleet.
	 */
	void createAttackers() {
		Planet from = planet("Garthog 1");
		Fleet f = createFleet("Garthog.fleet", player("Garthog"), from.x, from.y);
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
		f.waypoints.add(new Point2D.Double(ach.x, ach.y));
		f.mode = FleetMode.MOVE;
		f.task = FleetTask.SCRIPT;
	}
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
