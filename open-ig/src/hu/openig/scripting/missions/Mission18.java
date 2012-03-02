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
import hu.openig.mechanics.DefaultAIControls;
import hu.openig.model.Fleet;
import hu.openig.model.FleetTask;
import hu.openig.model.InventoryItem;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchType;

/**
 * Mission 18: Conquer the Garthogs.
 * <p>Also: initialize level 3.</p>
 * @author akarnokd, Feb 13, 2012
 */
public class Mission18 extends Mission {
	
	@Override
	public boolean applicable() {
		return world.level == 3;
	}
	@Override
	public void onLevelChanged() {
		if (world.level != 3) {
			return;
		}
		removeMissions(1, 25);
		
		player.setAvailable(research("Battleship1"));
		player.setAvailable(research("Cruiser1"));
		createMainShip();

		// achievement
		achievement("achievement.commander");
		
		addMission("Mission-18", 1);
	}
	/**
	 * Creates the main ship for level 3.
	 */
	void createMainShip() {
		Pair<Fleet, InventoryItem> own = findTaggedFleet("CampaignMainShip3", player);
		if (own != null) {
			return;
		}
		own = findTaggedFleet("CampaignMainShip2", player);
		if (own == null) {
			own = findTaggedFleet("CampaignMainShip1", player);
		}
		Fleet f = null;
		if (own != null) {
			f = own.first;
		} else {
			Planet ach = planet("Achilles");
			f = createFleet(label("Empire.main_fleet"), player, ach.x + 5, ach.y + 5);
		}			
		ResearchType rt = research("Battleship1");
		f.addInventory(rt, 1);
		f.addInventory(research("LightTank"), 4);
		
		InventoryItem ii = f.getInventoryItem(rt);
		ii.tag = "CampaignMainShip3";

		// loadout
		setSlot(ii, "laser", "Laser1", 14);
		setSlot(ii, "bomb", "Bomb1", 6);
		setSlot(ii, "rocket", "Rocket1", 4);
		setSlot(ii, "radar", "Radar1", 1);
		setSlot(ii, "cannon", "IonCannon", 6);
		setSlot(ii, "shield", "Shield1", 1);
		setSlot(ii, "hyperdrive", "HyperDrive1", 1);

	}
	@Override
	public void onTime() {
		checkMainShip();
		checkSuccess();
		if (checkTimeout("Mission-18-Failed")) {
			helper.gameover();
			loseGameMessageAndMovie("Douglas-Fire-Lost-Planet-2", "loose/fired_level_3");
		}
		if (checkTimeout("Mission-18-Hide")) {
			helper.objective("Mission-18").visible = false;
		}
		if (checkMission("Mission-18")) {
			helper.objective("Mission-18").visible = true;
			for (int i = 1; i < 6; i++) {
				helper.objective("Mission-18-Task-" + i).visible = true;
			}
		}
		// planet messages
		String[] planets = { "Achilles", "Naxos", "San Sterling", "New Caroline", "Centronom", "Zeuson" };
		setPlanetMessages(planets);
		
		if (checkMission("Mission-18-Promote")) {
			world.env.stopMusic();
			world.env.playVideo("interlude/level_4_intro", new Action0() {
				@Override
				public void invoke() {
					promote();
				}
			});
		}
		
		
		// -------------------------------------------------------
		// help the garthog economy
		// FIXME 
		Player garthog = player("Garthog");
		int mi = 50;
		switch (world.difficulty) {
		case NORMAL:
			mi = 100;
			break;
		case HARD:
			mi = 200;
			break;
		default:
		}
		garthog.money += mi;
		garthog.statistics.moneyIncome += mi;
		// -------------------------------------------------------
	}
	/** Check if the main ship is still operational. */
	void checkMainShip() {
		Pair<Fleet, InventoryItem> ft = findTaggedFleet("CampaignMainShip3", player);
		if (ft == null) {
			if (!helper.hasTimeout("MainShip-Lost")) {
				helper.setTimeout("MainShip-Lost", 3000);
			}
			if (helper.isTimeout("MainShip-Lost")) {
				helper.gameover();
				loseGameMovie("loose/destroyed_level_3");
			}
		}
	}
	/**
	 * Promotion action.
	 */
	void promote() {
		world.level = 4;
		world.env.playMusic();
	}
	/** Check if we own all the necessary planets. */
	void checkSuccess() {
		Player g = player("Garthog");
		if (g.statistics.planetsOwned == 0 && !helper.hasMissionTime("Mission-18-Promote")) {
			helper.setObjectiveState("Mission-18", ObjectiveState.SUCCESS);
			addTimeout("Mission-18-Hide", 13000);
			// TODO next level
			addMission("Mission-18-Promote", 3);
		}
	}
	@Override
	public void onConquered(Planet planet, Player previousOwner) {
		if (planet.owner == player || previousOwner == player) {
			boolean win = previousOwner.id.equals("Garthog");
			if (planet.id.equals("Garthog 1")) {
				helper.setObjectiveState("Mission-18-Task-1", win ? ObjectiveState.SUCCESS : ObjectiveState.ACTIVE);
			} else
			if (planet.id.equals("Garthog 2")) {
				helper.setObjectiveState("Mission-18-Task-2", win ? ObjectiveState.SUCCESS : ObjectiveState.ACTIVE);
			} else
			if (planet.id.equals("Garthog 3")) {
				helper.setObjectiveState("Mission-18-Task-3", win ? ObjectiveState.SUCCESS : ObjectiveState.ACTIVE);
			} else
			if (planet.id.equals("Garthog 4")) {
				helper.setObjectiveState("Mission-18-Task-4", win ? ObjectiveState.SUCCESS : ObjectiveState.ACTIVE);
			} else
			if (planet.id.equals("Garthog 5")) {
				helper.setObjectiveState("Mission-18-Task-5", win ? ObjectiveState.SUCCESS : ObjectiveState.ACTIVE);
			} else {
				helper.setObjectiveState("Mission-18", ObjectiveState.FAILURE);
				gameover();
			}
		}
	}
	@Override
	public void onLost(Planet planet) {
		if (planet.owner == player) {
			if (planet.id.startsWith("Garthog")) {
				Planet ach = planet("Achilles");
				// create a fleet to colonize the planet, for convenience
				Fleet f = createFleet(label("Empire.colonizer_fleet"), player, ach.x, player.explorationOuterLimit.y - 5);
				f.addInventory(research("ColonyShip"), 1);
				f.moveTo(planet);
				f.task = FleetTask.SCRIPT;
				tagFleet(f, "Mission-18-Colonizer");
				addScripted(f);
			} else {
				helper.setObjectiveState("Mission-18", ObjectiveState.FAILURE);
				gameover();
			}
		}		
	}
	@Override
	public void onFleetAt(Fleet fleet, Planet planet) {
		if (fleet.owner == player && planet.owner == null 
				&& hasTag(fleet, "Mission-18-Colonizer")) {
			removeScripted(fleet);
			DefaultAIControls.colonizeWithFleet(fleet, planet);
		}
	}
	/** Issue game over. */
	void gameover() {
		addTimeout("Mission-18-Failed", 13000);
	}
}
