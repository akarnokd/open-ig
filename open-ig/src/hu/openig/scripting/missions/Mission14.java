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
import hu.openig.model.BattleInfo;
import hu.openig.model.Fleet;
import hu.openig.model.FleetMode;
import hu.openig.model.FleetTask;
import hu.openig.model.InventoryItem;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.Player;
import hu.openig.model.ResearchType;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarWorld;
import hu.openig.utils.XElement;

/**
 * Mission 14: stop the virus carriers.
 * @author akarnokd, Feb 4, 2012
 */
public class Mission14 extends Mission {
	/** The mission stages. */
	public enum M14 {
		/** Not active. */
		NONE,
		/** Intro. */
		INIT,
		/** Wait to destroy carriers. */
		DESTROY,
		/** Done. */
		DONE
	}
	/** The current stage. */
	protected M14 stage = M14.NONE;
	@Override
	public void onTime() {
		if (checkMission("Mission-14") && stage == M14.NONE) {
			stage = M14.INIT;
			world.env.stopMusic();
			world.env.playVideo("interlude/deploy_hubble_2", new Action0() {
				@Override
				public void invoke() {
					world.env.speed1();
					doDeploy();
					world.env.playMusic();
				}
			});
		}
		if (checkTimeout("Mission-14-Hide")) {
			helper.objective("Mission-14").visible = false;
			helper.objective("Mission-12").visible = false;
		}
		if (checkTimeout("Mission-14-Report-Hide")) {
			helper.send("Douglas-Virus-Carriers-Destroyed").visible = false;
		}
		if (checkTimeout("Mission-14-Failed")) {
			helper.gameover();
			loseGameMessageAndMovie("Douglas-Fire-Mistakes", "loose/fired_level_2");
		}
		if (checkTimeout("Mission-14-Success")) {
			helper.setObjectiveState("Mission-14", ObjectiveState.SUCCESS);
			helper.setTimeout("Mission-14-Hide", 13000);
			helper.objective("Mission-12").visible = true;
			helper.setObjectiveState("Mission-12", ObjectiveState.SUCCESS);
		}
	}
	@Override
	public void onSpacewarStep(SpacewarWorld war) {
		if (isMissionSpacewar(war.battle(), "Mission-14")) {
			Player garthog = player("Garthog");
			for (SpacewarStructure s : war.structures(garthog)) {
				war.move(s, Math.cos(s.angle) * 1000, s.y);
			}
		}
	}
	/**
	 * Deploy satellites, create garthog carriers.
	 */
	void doDeploy() {
		
		ResearchType h1 = research("Hubble1");
		player.changeInventoryCount(h1, 2);
		DefaultAIControls.actionDeploySatellite(player, planet("Achilles"), h1);
		DefaultAIControls.actionDeploySatellite(player, planet("Naxos"), h1);

		Player garthog = player("Garthog");
		Planet g2 = planet("Garthog 4");
		
		Fleet f = createFleet(label("Garthog.virus-carrier"), garthog, g2.x, g2.y);
		
		f.addInventory(research("GarthogVirusBomb"), 3);
		// ---------------------------------------------------
		
		for (InventoryItem ii : f.inventory) {
			ii.tag = "Mission-14-Garthog";
		}
		Planet nc = planet("New Caroline");
		f.targetPlanet(nc);
		f.mode = FleetMode.MOVE;
		f.task = FleetTask.SCRIPT;
		garthog.changeInventoryCount(research("SpySatellite1"), 1);
		DefaultAIControls.actionDeploySatellite(garthog, nc, research("SpySatellite1"));
		garthog.planets.put(nc, PlanetKnowledge.BUILDING);
		
		helper.scriptedFleets().add(f.id);
		
		stage = M14.DESTROY;
		
		helper.send("Douglas-Report-Viruses").visible = false;
		
		helper.showObjective("Mission-14");
		
		if (helper.hasMissionTime("Mission-12-Subsequent")) {
			helper.clearMissionTime("Mission-12-Subsequent");
		}
	}
	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		onAutobattleFinish(war.battle());
	}
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		if (isMissionSpacewar(battle, "Mission-14")) {
			Player garthog = player("Garthog");
			Pair<Fleet, InventoryItem> tf = findTaggedFleet("Mission-14-Garthog", garthog);
			if (tf != null) {
				Fleet f = tf.first;
				Planet nc = planet("New Caroline");
				f.targetPlanet(nc);
				f.mode = FleetMode.MOVE;
				f.task = FleetTask.SCRIPT;
			} else {
				helper.setTimeout("Mission-14-Success", 1000);
				stage = M14.DONE;
				helper.send("Douglas-Virus-Carriers-Destroyed").visible = true;
				battle.rewardImage = "battlefinish/mission_23";
				battle.messageText = label("battlefinish.mission-14.23");
			}
		}
	}
	@Override
	public void onMessageSeen(String id) {
		if ("Douglas-Virus-Carriers-Destroyed".equals(id)) {
			helper.setTimeout("Mission-14-Report-Hide", 10000);
		}
	}
	
	@Override
	public void onFleetAt(Fleet fleet, Planet planet) {
		if (hasTag(fleet, "Mission-14-Garthog") && planet.id.equals("New Caroline")) {
			helper.setObjectiveState("Mission-14", ObjectiveState.FAILURE);
			helper.setTimeout("Mission-14-Failed", 13000);
			world.removeFleet(fleet);
			planet.quarantineTTL = Planet.DEFAULT_QUARANTINE_TTL;
			world.scripting.onPlanetInfected(planet);
			helper.scriptedFleets().remove(fleet.id);
			stage = M14.DONE;
		}
	}
	@Override
	public boolean applicable() {
		return world.level == 2;
	}
	@Override
	public void load(XElement xmission) {
		stage = M14.valueOf(xmission.get("stage"));
	}
	@Override
	public void save(XElement xmission) {
		xmission.set("stage", stage);
	}
	@Override
	public void reset() {
		stage = M14.NONE;
		super.reset();
	}

}
