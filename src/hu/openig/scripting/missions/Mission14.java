/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Action0;
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
import hu.openig.model.SpacewarScriptResult;
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
			objective("Mission-14").visible = false;
			objective("Mission-12").visible = false;
			objective("Mission-12-Task-6").visible = false;
			send("Douglas-Virus-Carriers-Destroyed").visible = false;
		}
		if (checkTimeout("Mission-14-Report-Hide")) {
			send("Douglas-Virus-Carriers-Destroyed").visible = false;
		}
		if (checkTimeout("Mission-14-Failed")) {
			gameover();
			loseGameMessageAndMovie("Douglas-Fire-Mistakes", "lose/fired_level_2");
		}
		if (checkTimeout("Mission-14-Success")) {
			setObjectiveState("Mission-14", ObjectiveState.SUCCESS);
			setObjectiveState("Mission-12", ObjectiveState.SUCCESS);
			addTimeout("Mission-14-Hide", 13000);
		}
	}
	@Override
	public SpacewarScriptResult onSpacewarStep(SpacewarWorld war) {
		BattleInfo bi = war.battle();
		if (isMissionSpacewar(bi, "Mission-14")
				&& (bi.targetFleet != null && hasTag(bi.targetFleet, "Mission-14-Garthog"))) {
			Player garthog = player("Garthog");
			for (SpacewarStructure s : war.structures(garthog)) {
				war.move(s, Math.cos(s.angle) * 1000, s.y);
			}
			return SpacewarScriptResult.CONTINUE;
		}
		return null;
	}
	/**
	 * Deploy satellites, create garthog carriers.
	 */
	void doDeploy() {
		
		ResearchType h1 = research("Hubble1");
		player.changeInventoryCount(h1, 2);
		DefaultAIControls.actionDeploySatellite(player, planet("San Sterling"), h1);
		DefaultAIControls.actionDeploySatellite(player, planet("Naxos"), h1);

		Player garthog = player("Garthog");
		Planet g2 = planet("Garthog 4");
		
		Fleet f = createFleet(label("Garthog.virus-carrier"), garthog, g2.x, g2.y);
		
		addInventory(f, "GarthogVirusBomb", 3);
		// ---------------------------------------------------
		
		for (InventoryItem ii : f.inventory.iterable()) {
			ii.tag = "Mission-14-Garthog";
		}
		Planet nc = planet("New Caroline");
		f.targetPlanet(nc);
		f.mode = FleetMode.MOVE;
		f.task = FleetTask.SCRIPT;
		garthog.changeInventoryCount(research("SpySatellite1"), 1);
		DefaultAIControls.actionDeploySatellite(garthog, nc, research("SpySatellite1"));
		garthog.planets.put(nc, PlanetKnowledge.BUILDING);
		
		addScripted(f);
		
		stage = M14.DESTROY;
		
		send("Douglas-Report-Viruses").visible = false;
		
		showObjective("Mission-14");
	}
	@Override
	public void onSpacewarStart(SpacewarWorld war) {
		BattleInfo battle = war.battle();
		if (isMissionSpacewar(battle, "Mission-14") && hasTag(battle, "Mission-14-Garthog")) {
			battle.tag = "Mission-14-Garthog";
			battle.chat = "chat.mission-14.destroy.viruscarrier";
		}
	}
	
	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		onAutobattleFinish(war.battle());
	}
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		if ("Mission-14-Garthog".equals(battle.tag)) {
			Player garthog = player("Garthog");
			Fleet tf = findTaggedFleet("Mission-14-Garthog", garthog);
			if (tf != null) {
				Planet nc = planet("New Caroline");
				tf.targetPlanet(nc);
				tf.mode = FleetMode.MOVE;
				tf.task = FleetTask.SCRIPT;
			} else {
				addTimeout("Mission-14-Success", 1000);
				stage = M14.DONE;
				send("Douglas-Virus-Carriers-Destroyed").visible = true;
				battle.rewardImage = "battlefinish/mission_23";
				battle.messageText = label("battlefinish.mission-14.23");
				
				world.achievement("achievement.influenza");
			}
		}
	}
	@Override
	public void onMessageSeen(String id) {
		if ("Douglas-Virus-Carriers-Destroyed".equals(id)) {
			if (send("Douglas-Virus-Carriers-Destroyed").visible) {
				addTimeout("Mission-14-Report-Hide", 10000);
			}
		}
	}
	
	@Override
	public void onFleetAt(Fleet fleet, Planet planet) {
		if (hasTag(fleet, "Mission-14-Garthog") && planet.id.equals("New Caroline")) {
			setObjectiveState("Mission-14", ObjectiveState.FAILURE);
			addTimeout("Mission-14-Failed", 13000);
			world.removeFleet(fleet);
			planet.quarantineTTL = Planet.DEFAULT_QUARANTINE_TTL;
			world.scripting.onPlanetInfected(planet);
			removeScripted(fleet);
			stage = M14.DONE;
		}
	}
	@Override
	public boolean applicable() {
		return world.level == 2;
	}
	@Override
	public void load(XElement xmission) {
		super.load(xmission);
		stage = M14.valueOf(xmission.get("stage"));
	}
	@Override
	public void save(XElement xmission) {
		super.save(xmission);
		xmission.set("stage", stage);
	}
	@Override
	public void reset() {
		stage = M14.NONE;
		super.reset();
	}

}
