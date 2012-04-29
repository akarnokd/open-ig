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
import hu.openig.model.FleetMode;
import hu.openig.model.FleetTask;
import hu.openig.model.InventoryItem;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.SoundType;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarWorld;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Mission 5: Defend the Thorin.
 * @author akarnokd, 2012.01.18.
 */
public class Mission5 extends Mission {
	/** Reinforcements once. */
	protected boolean reinforcements;
	@Override
	public boolean applicable() {
		return world.level == 1;
	}
	@Override
	public void onTime() {
		Objective m2t1 = helper.objective("Mission-2-Task-3");
		Objective m1 = helper.objective("Mission-1");
		Objective m5 = helper.objective("Mission-5");
		Objective m5t1 = helper.objective("Mission-5-Task-1");
		if (!m5.visible 
				&& m5.state == ObjectiveState.ACTIVE
				&& m2t1.state != ObjectiveState.ACTIVE
				&& m1.state != ObjectiveState.ACTIVE
				&& !helper.hasMissionTime("Mission-5")) {
			helper.setMissionTime("Mission-5", helper.now() + 24);
		}
		if (helper.canStart("Mission-5")) {
			world.env.speed1();
			helper.setTimeout("Mission-5-Message", 3000);
			helper.clearMissionTime("Mission-5");
			incomingMessage("Douglas-Thorin");
		}
		if (helper.isTimeout("Mission-5-Message")) {
			helper.clearTimeout("Mission-5-Message");
			helper.showObjective("Mission-5");
			createTullen();
			helper.setMissionTime("Mission-5-Timeout-1", helper.now() + 24);
			helper.setTimeout("Mission-5-Task-1", 2000);
		}
		if (helper.isTimeout("Mission-5-Task-1")) {
			helper.showObjective("Mission-5-Task-1");
			helper.clearTimeout("Mission-5-Task-1");
		}
		if (helper.isMissionTime("Mission-5-Timeout-1")) {
			helper.setObjectiveState("Mission-5", ObjectiveState.FAILURE);
			helper.setObjectiveState("Mission-5-Task-1", ObjectiveState.FAILURE);
			helper.clearMissionTime("Mission-5-Timeout-1");
			helper.receive("Douglas-Thorin").visible = false;
			helper.gameover();
			loseGameMessageAndMovie("Douglas-Fire-Mistakes", "loose/fired_level_1");
		}
		if (m5t1.visible && m5t1.state == ObjectiveState.ACTIVE) {
			if (checkFleetInRange()) {
				helper.clearMissionTime("Mission-5-Timeout-1");
				world.env.stopMusic();
				world.env.playVideo("interlude/thorin_escort", new Action0() {
					@Override
					public void invoke() {
						helper.setObjectiveState("Mission-5-Task-1", ObjectiveState.SUCCESS);
						helper.setMissionTime("Mission-5-Task-2", helper.now() + 2);
						moveTullen();
						helper.send("Douglas-Thorin-Reinforcements").visible = true;
						helper.send("Douglas-Reinforcements-Denied").visible = false;
						world.env.playMusic();
					}
				});
			}
		}
		if (helper.isMissionTime("Mission-5-Task-2")) {
			helper.clearMissionTime("Mission-5-Task-2");
			helper.showObjective("Mission-5-Task-2");
			createGarthog();
		}
		if (helper.isMissionTime("Mission-5-Task-2-Timeout")) {
			helper.clearMissionTime("Mission-5-Task-2-Timeout");
			helper.setObjectiveState("Mission-5-Task-2", ObjectiveState.FAILURE);
			helper.setObjectiveState("Mission-5", ObjectiveState.FAILURE);
			removeFleets();
			helper.setTimeout("Mission-5-Failed", 13000);
		}
		if (checkTimeout("Mission-5-Failed")) {
			helper.receive("Douglas-Thorin").visible = false;
			helper.send("Douglas-Thorin-Reinforcements").visible = false;
			helper.gameover();
			loseGameMessageAndMovie("Douglas-Fire-Escort-Failed", "loose/fired_level_1");
		}
//			if (checkTimeout("Mission-5-Destoyed")) {
//				helper.receive("Douglas-Thorin").visible = false;
//				helper.send("Douglas-Thorin-Reinforcements").visible = false;
//				helper.gameover();
//				loseGameMovie("loose/destroyed_level_1");
//			}
		if (helper.isTimeout("Mission-5-Success")) {
			helper.clearTimeout("Mission-5-Success");
			
			helper.receive("Douglas-Thorin").visible = false;
			helper.send("Douglas-Thorin-Reinforcements").visible = false;
			
			helper.setObjectiveState("Mission-5-Task-2", ObjectiveState.SUCCESS);
			helper.setObjectiveState("Mission-5", ObjectiveState.SUCCESS);
			removeFleets();
			
			helper.setTimeout("Mission-5-Promote", 13000);
		}
		if (helper.isTimeout("Mission-5-Promote")) {
			helper.objective("Mission-5").visible = false;
			helper.clearTimeout("Mission-5-Promote");
			incomingMessage("Douglas-Promotion-2");
			
			helper.setTimeout("Mission-5-Promote-2", 16000);
			helper.setTimeout("Level-1-Success", 4000);
		}
		if (helper.isTimeout("Level-1-Success")) {
			helper.clearTimeout("Level-1-Success");
			helper.setObjectiveState("Mission-1-Task-3", ObjectiveState.SUCCESS);
			helper.setObjectiveState("Mission-1-Task-4", ObjectiveState.SUCCESS);
		}
		if (checkTimeout("Mission-5-Promote-2")) {
			helper.objective("Mission-1-Task-3").visible = false;
			helper.objective("Mission-1-Task-4").visible = false;
			helper.receive("Douglas-Promotion-2").visible = false;

			world.env.stopMusic();
			world.env.pause();

			world.env.forceMessage("Douglas-Promotion-2", null /*new Action0() {
				@Override
				public void invoke() {
					world.env.playVideo("interlude/level_2_intro", new Action0() {
						@Override
						public void invoke() {
							promote();
							world.env.speed1();
						}
					});
				}
			}*/);
		}
	}
	@Override
	public void onMessageSeen(String id) {
		if ("Douglas-Promotion-2".equals(id)) {
			helper.receive("Douglas-Promotion-2").visible = false;
			if (helper.hasTimeout("Mission-5-Promote-2")) {
				helper.clearTimeout("Mission-5-Promote-2");
			}
			
			helper.objective("Mission-1-Task-3").visible = false;
			helper.objective("Mission-1-Task-4").visible = false;
			
			world.env.stopMusic();
			world.env.playVideo("interlude/level_2_intro", new Action0() {
				@Override
				public void invoke() {
					promote();
				}
			});
		} else
		if ("Douglas-Thorin-Reinforcements".equals(id)) {
			if (!reinforcements) {
				reinforcements = true;
				Pair<Fleet, InventoryItem> own = findTaggedFleet("CampaignMainShip1", player);
				if (own != null) {
					own.first.addInventory(research("Fighter1"), 3);
				}
				world.env.computerSound(SoundType.REINFORCEMENT_ARRIVED_1);
			}
		}
	}
	/**
	 * Perform the promotion action.
	 */
	void promote() {
		world.level = 2;
		world.env.playMusic();
	}
	/**
	 * Check if Tullen successfully left the area.
	 * @return true if left
	 */
	boolean checkTullenLeft() {
		Pair<Fleet, InventoryItem> tullen = findTaggedFleet("Mission-5", player);
		if (tullen != null) {
			return tullen.first.waypoints.isEmpty() && !player.withinLimits(tullen.first.x, tullen.first.y, 0);
		}
		return false;
	}
	/**
	 * Remove both fleets.
	 */
	void removeFleets() {
		Pair<Fleet, InventoryItem> tullen = findTaggedFleet("Mission-5", player);
		if (tullen != null) {
			world.removeFleet(tullen.first);
		}
		Pair<Fleet, InventoryItem> garthog = findTaggedFleet("Mission-5-Garthog", player("Garthog"));
		if (garthog != null) {
			world.removeFleet(garthog.first);
		}
		cleanupScriptedFleets();
	}
	/**
	 * Stop the fleets.
	 */
	void stopFleets() {
		Pair<Fleet, InventoryItem> tullen = findTaggedFleet("Mission-5", player);
		tullen.first.stop();
		tullen.first.task = FleetTask.SCRIPT;
		Pair<Fleet, InventoryItem> garthog = findTaggedFleet("Mission-5-Garthog", player("Garthog"));
		garthog.first.stop();
		garthog.first.task = FleetTask.SCRIPT;
		
	}
	@Override
	public void onFleetsMoved() {
		Objective m5t2 = helper.objective("Mission-5-Task-2");
		if (m5t2.visible && m5t2.state == ObjectiveState.ACTIVE
				&& !helper.hasMissionTime("Mission-5-Task-2-Timeout")) {
			checkTullenReached();
		}
	};
	/**
	 * Check if the Garthog fleet reached Tullen.
	 */
	void checkTullenReached() {
		Pair<Fleet, InventoryItem> tullen = findTaggedFleet("Mission-5", player);
		Pair<Fleet, InventoryItem> garthog = findTaggedFleet("Mission-5-Garthog", player("Garthog"));
		if (garthog != null) {
			garthog.first.waypoints.clear();
			garthog.first.waypoints.add(new Point2D.Double(tullen.first.x, tullen.first.y));
			double d = Math.hypot(tullen.first.x - garthog.first.x, tullen.first.y - garthog.first.y);
			if (d <= 5) {
				stopFleets();
				helper.setMissionTime("Mission-5-Task-2-Timeout", helper.now() + 24);
				
				// follower automatically attacks
				Fleet ff = getFollower(tullen.first, player);
				if (ff != null) {
					ff.attack(garthog.first);
				}
			}
		}
	}
	@Override
	public void onDiscovered(Player player, Fleet fleet) {
		if (player == this.player && hasTag(fleet, "Mission-5-Garthog")) {
			if (world.env.config().slowOnEnemyAttack) {
				world.env.speed1();
			}
			world.env.computerSound(SoundType.ENEMY_FLEET_DETECTED);
		}
	}
	/**
	 * Set the target for the carrier fleet.
	 * @param f the fleet
	 */
	void moveToDestination(Fleet f) {
		Planet target = planet("San Sterling");
		f.waypoints.clear();
		f.mode = FleetMode.MOVE;
		f.task = FleetTask.SCRIPT;
		f.waypoints.add(new Point2D.Double(target.x - 60, target.y - 40));
	}
	/**
	 * Issue move order to tullen.
	 */
	void moveTullen() {
		Pair<Fleet, InventoryItem> fi = findTaggedFleet("Mission-5", player);
		moveToDestination(fi.first);
	}
	/**
	 * Check if one of the player's fleet is in range of Thorin.
	 * @return true if in range
	 */
	boolean checkFleetInRange() {
		Pair<Fleet, InventoryItem> fi = findTaggedFleet("Mission-5", player);
		for (Fleet f : player.fleets.keySet()) {
			if (f.owner == player && f != fi.first) {
				double d = Math.hypot(f.x - fi.first.x, f.y - fi.first.y);
				if (d < 5) {
					return true;
				}
			}
		}
		return false;
	}
	@Override
	public void onSpacewarStart(SpacewarWorld war) {
		if (isMissionSpacewar(war.battle(), "Mission-5")) {
			BattleInfo battle = war.battle();
			Player garthog = player("Garthog");
			Pair<Fleet, InventoryItem> f1 = findTaggedFleet("Mission-5", player);
			Pair<Fleet, InventoryItem> f2 = findTaggedFleet("Mission-5-Garthog", garthog);

			if (battle.targetFleet == f1.first) {
				// thorin attacked?
				war.includeFleet(f2.first, f2.first.owner);
				battle.targetFleet = f2.first;
			} else {
				// garthog attacked
				war.addStructures(f1.first.inventory, EnumSet.of(
						ResearchSubCategory.SPACESHIPS_BATTLESHIPS,
						ResearchSubCategory.SPACESHIPS_CRUISERS,
						ResearchSubCategory.SPACESHIPS_FIGHTERS));
			}
			int helpers = 0; 
			if (helper.isMissionTime("Mission-4-Helped")) {
				// add 3 helper pirates
				List<InventoryItem> iis = createHelperShips(f1.first);
				helpers = iis.size();
				war.addStructures(iis, EnumSet.of(ResearchSubCategory.SPACESHIPS_FIGHTERS));
				battle.attackerAllies.add(player("Pirates"));
			}
			
			// center pirate
			Dimension d = war.space();
			List<SpacewarStructure> structures = war.structures();
			int y = (d.height - helpers * 40) / 2; 
			for (SpacewarStructure s : structures) {
				if (s.item != null && "Mission-5".equals(s.item.tag)) {
					s.x = d.width / 2;
					s.y = d.height / 2;
					s.angle = 0.0;
					s.owner = f1.first.owner;
					s.guard = true;
				}
				if (s.item != null && "Mission-5-Help".equals(s.item.tag)) {
					s.x = d.width / 3;
					s.y = y;
					s.angle = 0.0;
					s.guard = true;
					y += 40;
				}
			}
			battle.allowRetreat = false;
		}

	}
	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		if (isMissionSpacewar(war.battle(), "Mission-5")) {
			// find the status of the trader ship
			boolean thorinSurvived = false;
			for (SpacewarStructure s : war.structures()) {
				if (s.item != null && "Mission-5".equals(s.item.tag)) {
					thorinSurvived = true;
					break;
				}
			}
			completeMission(thorinSurvived);
			if (thorinSurvived) {
				war.battle().messageText = label("battlefinish.mission-5.garthog");
				war.battle().rewardText = label("battlefinish.mission-5.garthog_bonus");
			}
		}
	}
	/**
	 * Issue the specific mission changes once task is completed.
	 * @param survive did the ship to be protected survive?
	 */
	void completeMission(boolean survive) {
		helper.clearMissionTime("Mission-5-Task-2-Timeout");
		Pair<Fleet, InventoryItem> garthog = findTaggedFleet("Mission-5-Garthog", player("Garthog"));
		if (garthog != null) {
			world.removeFleet(garthog.first);
		}
		Pair<Fleet, InventoryItem> own = findTaggedFleet("CampaignMainShip1", player);
		if (survive && own != null) {
			helper.setTimeout("Mission-5-Success", 3000);
			moveTullen();
		} else {
			if (own != null) {
				helper.setTimeout("Mission-5-Failed", 3000);
			} else {
				helper.setTimeout("Mission-5-Destroyed", 3000);
			}
		}
	}
	@Override
	public void onAutobattleStart(BattleInfo battle) {
		if (isMissionSpacewar(battle, "Mission-5")) {
			Player g = player("Garthog");
			Pair<Fleet, InventoryItem> f1 = findTaggedFleet("Mission-5", player);
			Pair<Fleet, InventoryItem> f2 = findTaggedFleet("Mission-5-Garthog", g);
			if (battle.targetFleet == f1.first) {
				battle.targetFleet = f2.first;
			}
			battle.attacker.inventory.addAll(f1.first.inventory);
			if (helper.isMissionTime("Mission-4-Helped")) {
				battle.attacker.inventory.addAll(createHelperShips(f1.first));
			}
		}
	}
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		if (isMissionSpacewar(battle, "Mission-5")) {
			boolean tullenSurvived = false;
			for (InventoryItem ii : new ArrayList<InventoryItem>(battle.attacker.inventory)) {
				if ("Mission-5".equals(ii.tag)) {
					tullenSurvived = true;
					battle.attacker.inventory.remove(ii);
				}
				if ("Mission-5-Help".equals(ii.tag)) {
					battle.attacker.inventory.remove(ii);
				}
			}
			completeMission(tullenSurvived);
		}
	}
	/**
	 * Create Tullen's fleet.
	 * FIXME setup proper strength
	 */
	void createTullen() {
		Planet p = planet("Naxos");
		Fleet f = createFleet(label("mission-5.tullens_fleet"), player, p.x + 40, p.y - 10);
		f.addInventory(research("Flagship"), 1);
		for (InventoryItem ii : f.inventory) {
			ii.tag = "Mission-5";
			if (ii.type.id.equals("Flagship")) {
				// -------------------------------------------------------
				// Set flagship strength and equipment here
				ii.hp = 800;
				setSlot(ii, "laser", "Laser1", 6);
				setSlot(ii, "rocket", "Rocket1", 4);
				// -------------------------------------------------------
			}
		}
		f.task = FleetTask.SCRIPT;
		f.waypoints.add(new Point2D.Double(p.x + 10, p.y - 20));
		helper.scriptedFleets().add(f.id);
	}
	/** 
	 * Create garthog fleet to attack tullen.
	 * FIXME strength adjustments 
	 */
	void createGarthog() {
		Planet p = planet("Naxos");
		Fleet f = createFleet(label("mission-5.garthog_fleet"), player("Garthog"), p.x + 40, p.y - 20);
		// -------------------------------------------------------
		// Set strengths here
		f.addInventory(research("GarthogFighter"), 12);
		f.addInventory(research("GarthogDestroyer"), 1);
		// -------------------------------------------------------
		f.task = FleetTask.SCRIPT;
		for (InventoryItem ii : f.inventory) {
			ii.tag = "Mission-5-Garthog";
		}
		helper.scriptedFleets().add(f.id);
	}
	/**
	 * Create helper ship inventory.
	 * @param parent the parent fleet
	 * @return the list of added items
	 */
	List<InventoryItem> createHelperShips(Fleet parent) {
		List<InventoryItem> result = U.newArrayList();
		// -------------------------------------------------------
		// Set help strength here
		int helpingPirates = 3;
		String pirateTech = "PirateFighter2";
		// -------------------------------------------------------
		for (int i = 0; i < helpingPirates; i++) {
			InventoryItem pii = new InventoryItem(parent);
			pii.type = research(pirateTech);
			pii.owner = player("Pirates");
			pii.count = 1;
			pii.hp = world.getHitpoints(pii.type);
			pii.tag = "Mission-5-Help";
			pii.createSlots();
			pii.shield = Math.max(0, pii.shieldMax());
			result.add(pii);
		}
		
		return result;
	}
	@Override
	public void load(XElement xmission) {
		reinforcements = xmission.getBoolean("reinforcements", false);
	}
	@Override
	public void save(XElement xmission) {
		xmission.set("reinforcements", reinforcements);
	}
}
