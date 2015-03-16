/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Action0;
import hu.openig.model.Fleet;
import hu.openig.model.FleetTask;
import hu.openig.model.InventoryItem;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchType;
import hu.openig.model.ViewLimit;
import hu.openig.utils.XElement;

/**
 * Mission 22: enter level 4. Colonize or capture 7 more planets.
 * @author akarnokd, 2012.02.23.
 */
public class Mission22 extends Mission {
	/** Mission stages. */
	enum M22 {
		/** Not started. */
		NONE,
		/** Wait for objective. */
		WAIT,
		/** Running. */
		RUN,
		/** Done. */
		DONE
	}
	/** The current stage. */
	M22 stage = M22.NONE;
	/** The number of planets owned at the beginning of the mission. */
	long planetsOwned;
	@Override
	public boolean applicable() {
		return world.level == 4;
	}
	@Override
	public void onLevelChanged() {
		if (world.level != 4) {
			return;
		}
		removeMissions(1, 25);
		
		createMainShip();
		// achievement
		String a = "achievement.admiral";
		world.achievement(a);
		
		Player dsl = player("Dargslan");
		
		ViewLimit vl = getViewLimit(dsl, 3);
		if (vl != null) {
			dsl.explorationInnerLimit = vl.inner;
			dsl.explorationOuterLimit = vl.outer;
		} else {
			dsl.explorationInnerLimit = null;
			dsl.explorationOuterLimit = null;
		}
		
		player.populateProductionHistory();
	}
	
	/**
	 * Creates the main ship for level 4.
	 */
	void createMainShip() {
		Fleet own = findTaggedFleet("CampaignMainShip4", player);
		if (own != null) {
			return;
		}
		own = findTaggedFleet("CampaignMainShip3", player);
		if (own == null) {
			own = findTaggedFleet("CampaignMainShip2", player);
		}
		if (own == null) {
			own = findTaggedFleet("CampaignMainShip1", player);
		}
		Fleet f;
		if (own != null 
				&& own.getStatistics().battleshipCount < 3 
				&& own.getStatistics().cruiserCount < 25
				&& own.inventoryCount(research("Fighter2")) < 30 - 6) {
			f = own;
		} else {
			Planet ach = planet("Achilles");
			f = createFleet(label("Empire.main_fleet"), player, ach.x + 5, ach.y + 5);
		}			
		ResearchType rt = research("Flagship");
		addInventory(f, rt.id, 1);
		addInventory(f, "LightTank", 6);
		
		addInventory(f, "Cruiser1", 1);
		addInventory(f, "Fighter2", 6);
		
		InventoryItem ii = f.getInventoryItem(rt);
		ii.tag = "CampaignMainShip4";

		// loadout
		setSlot(ii, "laser", "Laser2", 24);
		setSlot(ii, "bomb", "Bomb1", 8);
		setSlot(ii, "rocket", "Rocket1", 16);
		setSlot(ii, "radar", "Radar1", 1);
		setSlot(ii, "cannon1", "IonCannon", 12);
		setSlot(ii, "cannon2", "IonCannon", 12);
		setSlot(ii, "shield", "Shield1", 14);
		setSlot(ii, "hyperdrive", "HyperDrive1", 1);
		
		for (Fleet fa : player.ownFleets()) {
			if (fa.task == FleetTask.SCRIPT) {
				fa.task = FleetTask.IDLE;
				removeScripted(fa);
			}
		}
		
		player.setAvailable(research("Flagship"));
	}
	@Override
	public void onTime() {
		if (stage == M22.NONE) {
			stage = M22.WAIT;
			addTimeout("Mission-22-Objective", 4000);
			addMission("Mission-22-Delay", 28 * 24);
			addMission("Mission-22-Delay-2", 7 * 24);
		}
		// delay any dargslan activity
		if (checkMission("Mission-22-Delay")) {
			Player dsl = player("Dargslan");
			ViewLimit vl = getViewLimit(dsl, 4);
			if (vl != null) {
				dsl.explorationInnerLimit = vl.inner;
				dsl.explorationOuterLimit = vl.outer;
			} else {
				dsl.explorationInnerLimit = null;
				dsl.explorationOuterLimit = null;
			}
		}
		// unbound colonization
		if (checkMission("Mission-22-Delay-2")) {
			for (Player p : world.players.values()) {
				p.colonizationLimit = -1;
			}
		}
		if (checkTimeout("Mission-22-Objective")) {
			showObjective("Mission-22");
			planetsOwned = player.statistics.planetsOwned.value;
			stage = M22.RUN;
		}
		if (stage == M22.RUN) {
			if (player.statistics.planetsOwned.value >= planetsOwned + 7) {
				setObjectiveState("Mission-22", ObjectiveState.SUCCESS);
				stage = M22.DONE;
				addTimeout("Mission-22-Hide", 13000);
				addMission("Mission-24", 1);
			}
		}
		if (checkTimeout("Mission-22-Hide")) {
			objective("Mission-22").visible = false;
		}
		String[] planets = { "Achilles", "Naxos", "San Sterling", "New Caroline", "Centronom", "Zeuson" };
		setPlanetMessages(planets);
		checkMainShip();
	}
	/** Check if the main ship is still operational. */
	void checkMainShip() {
		Fleet ft = findTaggedFleet("CampaignMainShip4", player);
		if (ft == null) {
			if (!hasTimeout("MainShip-Lost")) {
				addTimeout("MainShip-Lost", 3000);
			}
			if (checkTimeout("MainShip-Lost")) {
				gameover();
				loseGameMovie("lose/destroyed_level_3");
			}
		}
	}
	@Override
	public void onPlanetInfected(Planet planet) {
		if (planet.owner == player) {
			String msgId = planet.id + "-Virus";
			if (hasReceive(msgId)) {
				incomingMessage(msgId, (Action0)null);
			}
		}
	}

	@Override
	public void save(XElement xmission) {
		super.save(xmission);
		xmission.set("stage", stage);
		xmission.set("planets-owned", planetsOwned);
	}
	@Override
	public void load(XElement xmission) {
		super.load(xmission);
		stage = M22.valueOf(xmission.get("stage", M22.NONE.toString()));
		planetsOwned = xmission.getLong("planets-owned");
	}
	@Override
	public void reset() {
		stage = M22.NONE;
	}
}
