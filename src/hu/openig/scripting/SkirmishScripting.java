/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting;

import hu.openig.model.AIMode;
import hu.openig.model.BattleInfo;
import hu.openig.model.Building;
import hu.openig.model.Chats.Chat;
import hu.openig.model.Chats.Node;
import hu.openig.model.CustomGameDefinition;
import hu.openig.model.Fleet;
import hu.openig.model.GameScripting;
import hu.openig.model.GroundwarWorld;
import hu.openig.model.InventoryItem;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchType;
import hu.openig.model.SoundTarget;
import hu.openig.model.SoundType;
import hu.openig.model.SpacewarScriptResult;
import hu.openig.model.SpacewarWorld;
import hu.openig.model.VideoMessage;
import hu.openig.model.World;
import hu.openig.utils.XElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The scripting supervising a skirmish game.
 * @author akarnokd, 2012.08.22.
 */
public class SkirmishScripting implements GameScripting {
	/** The world object. */
	protected World world;
	/** The player object. */
	protected Player player;
	/** The skirmish definition. */
	protected CustomGameDefinition def;
	/** The map of all main and sub objectives. */
	final Map<String, Objective> allObjectives = new LinkedHashMap<>();
	/** Show the objectives once. */
	protected boolean objectivesOnce = true;
	/** The remaining hold time for occupation victory, in minutes. */
	protected int holdTime;
	/** The group holding the territories. */
	protected int holdGroup;
	@Override
	public void onTime() {
		if (objectivesOnce) {
			objectivesOnce = false;
			world.env.showObjectives(true);
		}
		if (def.victoryConquest) {
			checkConquestVictory();
		}
		if (def.victoryEconomic) {
			checkEconomicVictory();
		}
		if (def.victoryOccupation) {
			checkOccupationVictory();
		}
		if (def.victoryTechnology) {
			checkTechnologyVictory();
		}
		if (def.victorySocial) {
			checkSocialVictory();
		}
	}
	/**
	 * Check social victory condition.
	 */
	void checkSocialVictory() {
		Map<Integer, Integer> groupPlanets = new HashMap<>();
		for (Planet p : world.planets.values()) {
			if (p.owner != null) {
				if (p.morale >= def.victorySocialMorale) {
					Integer v = groupPlanets.get(p.owner.group);
					v = v != null ? v + 1 : 1;
					groupPlanets.put(p.owner.group, v);
					
					if (v >= def.victorySocialPlanets) {
						completeGame("social", p.owner.group == player.group);
						return;
					}
				}
			}
		}
	}
	/**
	 * Check if a group had researched all of its technologies.
	 */
	void checkTechnologyVictory() {
		for (Player p : world.players.values()) {
			if (p.aiMode == AIMode.PIRATES || p.aiMode == AIMode.TRADERS) {
				continue;
			}
			int avail = p.availableCount();
			
			int req = 0;
			for (ResearchType rt : world.researches.values()) {
				if (rt.race.contains(p.race) && rt.level < 6) {
					req++;
				}
			}
			if (avail >= req) {
				completeGame("technology", p.group == player.group);
				return;
			}
			
		}
	}
	/** Check for the occupation victory. */
	void checkOccupationVictory() {
		Map<Integer, Integer> groupPlanets = new HashMap<>();
		for (Planet p : world.planets.values()) {
			if (p.owner != null) {
				Integer v = groupPlanets.get(p.owner.group);
				groupPlanets.put(p.owner.group, v != null ? v + 1 : 1);
			}
		}
		int bestGroup = 0;
		int bestSize = 0;
		for (Map.Entry<Integer, Integer> ge : groupPlanets.entrySet()) {
			if (ge.getValue() >= def.victoryOccupationPercent * world.planets.planets.size() / 100) {
				if (bestSize < ge.getValue()) {
					bestSize = ge.getValue();
					bestGroup = ge.getKey();
				}
			}
		}
		if (bestGroup > 0) {
			if (bestGroup != holdGroup) {
				holdGroup = bestGroup;
				holdTime = def.victoryOccupationTime * 24 * 60;
			} else {
				holdTime -= world.params().speed();
			}
			if (holdTime <= 0 && holdGroup > 0) {
				completeGame("occupation", holdGroup == player.group);
			}
		} else {
			holdGroup = bestGroup;
			holdTime = 0;
		}
	}
	/**
	 * Check for economic victory.
	 */
	void checkEconomicVictory() {
		Map<Integer, Long> groups = new HashMap<>();
		for (Player p : world.players.values()) {
			Long v = groups.get(p.group);
			v = (v != null ? v + p.money() : p.money());
			if (v >= def.victoryEconomicMoney) {
				completeGame("economic", p.group == player.group);
				return;
			}
            groups.put(p.group, v);
		}
	}
	/**
	 * Check if all but one group has planets.
	 */
	void checkConquestVictory() {
		Map<Integer, Integer> groupPlanets = new HashMap<>();
		for (Planet p : world.planets.values()) {
			if (groupPlanets.size() > 1) {
				return;
			}
			if (p.owner != null) {
				Integer v = groupPlanets.get(p.owner.group);
				groupPlanets.put(p.owner.group, v != null ? v + 1 : 1);
			}
		}
		if (groupPlanets.size() > 1) {
			return;
		}
		completeGame("conquest", groupPlanets.containsKey(player.group));
	}
	/**
	 * Complete the game by setting objective state and doing an endgame.
	 * @param objective the objective
	 * @param win the win state
	 */
	void completeGame(String objective, boolean win) {
		if (win) {
			if (setObjectiveState(objective, ObjectiveState.SUCCESS)) {
				world.env.pause();
				world.env.winGame();
			}
		} else {
			if (setObjectiveState(objective, ObjectiveState.FAILURE)) {
				world.env.pause();
				world.env.loseGame();
			}
		}
	}
	
	@Override
	public void init(Player player, XElement in) {
		this.player = player;
		this.world = player.world;
		this.def = world.skirmishDefinition;
		
		// prepare objectives according to the win conditions
		Objective o1 = new Objective();
		o1.id = "conquest";
		o1.title = world.labels.get("skirmish.objectives.conquest");
		o1.description = world.labels.get("skirmish.objectives.conquest.desc");
		o1.visible = def.victoryConquest;
		
		Objective o2 = new Objective();
		o2.id = "economic";
		o2.title = world.labels.get("skirmish.objectives.economic");
		o2.description = world.labels.format("skirmish.objectives.economic.desc", def.victoryEconomicMoney);
		o2.visible = def.victoryEconomic;

		Objective o3 = new Objective();
		o3.id = "occupation";
		o3.title = world.labels.get("skirmish.objectives.occupation");
		o3.description = world.labels.format("skirmish.objectives.occupation.desc", def.victoryOccupationPercent, def.victoryOccupationTime);
		o3.visible = def.victoryOccupation;

		Objective o4 = new Objective();
		o4.id = "technology";
		o4.title = world.labels.get("skirmish.objectives.technology");
		o4.description = world.labels.format("skirmish.objectives.technology.desc");
		o4.visible = def.victoryTechnology;

		Objective o5 = new Objective();
		o5.id = "social";
		o5.title = world.labels.get("skirmish.objectives.social");
		o5.description = world.labels.format("skirmish.objectives.social.desc", def.victorySocialMorale, def.victorySocialPlanets);
		o5.visible = def.victorySocial;

		
		allObjectives.put(o1.id, o1);
		allObjectives.put(o2.id, o2);
		allObjectives.put(o3.id, o3);
		allObjectives.put(o4.id, o4);
		allObjectives.put(o5.id, o5);
	}
	@Override
	public List<Objective> currentObjectives() {
		List<Objective> result = new ArrayList<>();
		
		for (Objective o : allObjectives.values()) {
			if (o.visible) {
				result.add(o);
			}
		}
		
		return result;
	}
	/**
	 * Sets the objective state.
	 * @param oId the identifier
	 * @param newState the new state
	 * @return true if the state actually changed
	 */
	public boolean setObjectiveState(String oId, ObjectiveState newState) {
		return setObjectiveState(objective(oId), newState);
	}
	/**
	 * Sets the objective state.
	 * @param o the objective
	 * @param newState the new state
	 * @return true if the state actually changed
	 */
	public boolean setObjectiveState(Objective o, ObjectiveState newState) {
		if (o.state != newState) {
			o.state = newState;
			world.env.showObjectives(true);
			if (newState == ObjectiveState.SUCCESS) {
				world.env.playSound(SoundTarget.EFFECT, SoundType.SUCCESS, null);
			} else 
			if (newState == ObjectiveState.FAILURE) {
				world.env.playSound(SoundTarget.EFFECT, SoundType.FAIL, null);
			}
			return true;
		}
		return false;
	}
	/**
	 * Get a send-message.
	 * @param id the message id
	 * @return the video message or null if not available
	 */
	public VideoMessage send(String id) {
		return world.bridge.sendMessages.get(id);
	}
	@Override
	public void load(XElement in) {
		objectivesOnce = in.getBoolean("objectives-once", false);
		holdTime = in.getInt("hold-time", 0);
		holdGroup = in.getInt("hold-group", 0);
		for (XElement xmsgs : in.childrenWithName("sends")) {
			for (XElement xmsg : xmsgs.childrenWithName("send")) {
				String id = xmsg.get("id");
				VideoMessage vm = send(id);
				if (vm != null) {
					vm.visible = xmsg.getBoolean("visible");
					vm.seen = xmsg.getBoolean("seen", false);
				}
			}
		}
		// reset objectives
		for (Objective o : allObjectives.values()) {
			o.visible = false;
			o.state = ObjectiveState.ACTIVE;
		}
		for (XElement xos : in.childrenWithName("objectives")) {
			for (XElement xo : xos.childrenWithName("objective")) {
				String id = xo.get("id");
				Objective o = allObjectives.get(id);
				o.visible = xo.getBoolean("visible");
				o.state = ObjectiveState.valueOf(xo.get("state"));
			}
		}
	}
	@Override
	public void save(XElement out) {
		out.set("objectives-once", objectivesOnce);
		out.set("hold-time", holdTime);
		out.set("hold-group", holdGroup);
		XElement xmsgs = out.add("sends");
		for (VideoMessage vm : world.bridge.sendMessages.values()) {
			XElement xmsg = xmsgs.add("send");
			xmsg.set("id", vm.id);
			xmsg.set("visible", vm.visible);
			xmsg.set("seen", vm.seen);
		}
		XElement xos = out.add("objectives");
		for (Objective o : allObjectives.values()) {
			XElement xo = xos.add("objective");
			xo.set("id", o.id);
			xo.set("visible", o.visible);
			xo.set("state", o.state);
		}
	}
	/**
	 * Returns an objective.
	 * @param id the objective id.
	 * @return the objective
	 */
	public Objective objective(String id) {
		return allObjectives.get(id);
	}
	@Override
	public void onResearched(Player player, ResearchType rt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProduced(Player player, ResearchType rt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDestroyed(Fleet winner, Fleet loser) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onColonized(Planet planet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConquered(Planet planet, Player previousOwner) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerBeaten(Player player) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDiscovered(Player player, Planet planet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDiscovered(Player player, Player other) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDiscovered(Player player, Fleet fleet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLostSight(Player player, Fleet fleet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFleetAt(Fleet fleet, double x, double y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFleetAt(Fleet fleet, Fleet other) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFleetAt(Fleet fleet, Planet planet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStance(Player first, Player second) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAllyAgainst(Player first, Player second, Player commonEnemy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBattleComplete(Player player, BattleInfo battle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBuildingComplete(Planet planet, Building building) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRepairComplete(Planet planet, Building building) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpgrading(Planet planet, Building building, int newLevel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onInventoryAdd(Planet planet, InventoryItem item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onInventoryRemove(Planet planet, InventoryItem item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLost(Planet planet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLost(Fleet fleet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVideoComplete(String video) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSoundComplete(String audio) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlanetInfected(Planet planet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlanetCured(Planet planet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMessageSeen(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNewGame() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLevelChanged() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSpacewarStart(SpacewarWorld war) {
		// TODO Auto-generated method stub

	}

	@Override
	public SpacewarScriptResult onSpacewarStep(SpacewarWorld war) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGroundwarStart(GroundwarWorld war) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGroundwarStep(GroundwarWorld war) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGroundwarFinish(GroundwarWorld war) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAutobattleStart(BattleInfo battle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTalkCompleted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDeploySatellite(Planet target, Player player,
			ResearchType satellite) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean fleetBlink(Fleet f) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onFleetsMoved() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSpaceChat(SpacewarWorld world, Chat chat, Node node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRecordMessage() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<VideoMessage> getSendMessages() {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}

	@Override
	public void done() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean mayControlFleet(Fleet f) {
		return true;
	}

	@Override
	public boolean mayAutoSave() {
		return true;
	}

	@Override
	public void debug() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean mayPlayerAttack(Player player) {
		return true;
	}
	@Override
	public void onLoaded() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean mayPlayerImproveDefenses(Player player) {
		return true;
	}
	@Override
	public double playerPopulationGrowthOverride(Planet planet, double simulatorValue) {
		return simulatorValue;
	}
	@Override
	public double playerTaxIncomeOverride(Planet planet, double simulatorValue) {
		return simulatorValue;
	}
	@Override
	public int fleetSpeedOverride(Fleet fleet, int speed) {
		return speed;
	}
}
