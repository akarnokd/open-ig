/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting;

import hu.openig.core.Action0;
import hu.openig.model.BattleInfo;
import hu.openig.model.Building;
import hu.openig.model.GameScripting;
import hu.openig.model.Fleet;
import hu.openig.model.FleetMode;
import hu.openig.model.GroundwarWorld;
import hu.openig.model.InventoryItem;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.Player;
import hu.openig.model.ResearchType;
import hu.openig.model.SpacewarWorld;
import hu.openig.model.VideoMessage;
import hu.openig.model.World;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The scripting for the original game's campaign.
 * @author akarnokd, 2012.01.12.
 */
public class MainCampaignScripting implements GameScripting {
	/** The view limit records. */
	static class ViewLimit {
		/** The inner limit if non-null. */
		Rectangle inner;
		/** The outer limit if non-null. */
		Rectangle outer;
	}
	/** The current world. */
	protected World world;
	/** The current player. */
	protected Player player;
	/** The view limits. */
	final Map<String, ViewLimit> viewLimits = U.newHashMap();
	/** The map of root objectives. */
	final Map<String, Objective> rootObjectives = U.newLinkedHashMap();
	/** The map of all main and sub objectives. */
	final Map<String, Objective> allObjectives = U.newLinkedHashMap();
	/** The last level. */
	int lastLevel;
	/** The countdowns of various actions. */
	final Map<String, Integer> countdowns = U.newHashMap();
	/**
	 * Add/remove planets to the player based on level.
	 */
	void applyPlanetOwners() {
		Player empire = player("Empire");
		String[][] planets = {
				{ "Achilles", "Naxos", "San Sterling" }, 
				{ "Centronom", "New Caroline" },
				{ "Zeuson" },
				{ "Edgepolis", "Myridan", "Earth", "Giantropolis" },
				{ }
		};
		// use traders as the owner to disable planet management
		Player traders = player("Traders");
		for (String[] p : planets) {
			for (String pi : p) {
				setOwner(pi, traders);
			}
		}
		empire.planets.clear();
		for (int i = 1; i <= world.level; i++) {
			String[] ps = planets[i - 1];
			for (String pi : ps) {
				setOwner(pi, empire);
			}
		}
		empire.currentPlanet = planet("Achilles");
	}
	/**
	 * Apply view limits according to the current level.
	 */
	void applyViewLimits() {
		for (Player p : world.players.values()) {
			ViewLimit vl = viewLimits.get(p.id + ".Level." + world.level);
			if (vl != null) {
				p.explorationInnerLimit = vl.inner;
				p.explorationOuterLimit = vl.outer;
			} else {
				p.explorationInnerLimit = null;
				p.explorationOuterLimit = null;
			}
		}
	}
	/**
	 * Check the level 1 objectives.
	 */
	void checkLevel1Objectives() {
		checkMission1Task2();
		checkMission1Complete();
		
		Objective o0 = objective("Mission-1");
		if (o0.state == ObjectiveState.SUCCESS && isTimeout("Mission-1-Success")) {
			o0.visible = false;
			objective("Mission-1-Task-1").visible = false;
			objective("Mission-1-Task-2").visible = false;
			objective("Mission-1-Task-4").visible = true;
			world.env.showObjectives(true);
			clearTimeout("Mission-1-Success");
		} else
		if (o0.state == ObjectiveState.FAILURE && isTimeout("Mission-1-Failure")) {
			world.env.forceMessage("Douglas-Fire-Lost-Planet", new Action0() {
				@Override
				public void invoke() {
					world.env.loseGame();
				}
			});
			clearTimeout("Mission-1-Failure");
		}
	}
	/**
	 * Check if mission 1 was completed.
	 */
	void checkMission1Complete() {
		final Objective o0 = objective("Mission-1");
		final Objective o1 = objective("Mission-1-Task-1");
		final Objective o2 = objective("Mission-1-Task-2");
		
		if (o0.visible && o1.state == ObjectiveState.SUCCESS && o2.state == ObjectiveState.SUCCESS) {
			if (o0.state == ObjectiveState.ACTIVE) {
				setObjectiveState(o0, ObjectiveState.SUCCESS);
				setTimeout("Mission-1-Success", 13000);
			}
			
		}
	}
	/**
	 * Check if Achilles was lost.
	 * @param planet the planet
	 */
	void checkMission1Failure(Planet planet) {
		if (planet.id.equals("Achilles")) {
			Objective o = objective("Mission-1");
			if (o.visible) {
				if (setObjectiveState(o, ObjectiveState.FAILURE)) {
					setTimeout("Mission-1-Failure", 3000);
				}
			}
		}
	}
	/**
	 * Check if the colony hub was built on Achilles.
	 * @param planet the event planet
	 * @param building the event building
	 */
	void checkMission1Task1(Planet planet, Building building) {
		// Mission 1, Task 1: Build a Colony Hub
		if (planet.id.equals("Achilles") && building.type.kind.equals("MainBuilding")) {
			Objective o = allObjectives.get("Mission-1-Task-1");
			if (o.visible) {
				setObjectiveState(o, ObjectiveState.SUCCESS);
			}
		}
	}
	/**
	 * Check if Achilles contains the required types of undamaged, operational buildings for Mission 1 Task 2.
	 */
	void checkMission1Task2() {
		Objective m1t2 = objective("Mission-1-Task-2");
		if (!m1t2.visible) {
			return;
		}
		Planet p = planet("Achilles");
		String[][] buildingSets = {
				{ "PrefabHousing", "ApartmentBlock", "Arcology" },
				{ "NuclearPlant", "FusionPlant", "SolarPlant" },
				{ "CivilDevCenter", "MechanicalDevCenter", "ComputerDevCenter", "AIDevCenter", "MilitaryDevCenter" },
				{ "Police" },
				{ "FireBrigade" }, 
				{ "MilitarySpaceport" },
				{ "Radar1", "Radar2", "Radar3" }, 
		};
		boolean okay = true;
		Set<String> buildingTypes = U.newHashSet();
		for (Building b : p.surface.buildings) {
			if (!b.isOperational() || b.isDamaged()) {
				okay = false;
				break;
			} else {
				buildingTypes.add(b.type.id);
			}
		}
		if (okay) {
			for (String[] bts : buildingSets) {
				boolean found = true;
				for (String bt : bts) {
					if (buildingTypes.contains(bt)) {
						found = true;
						break;
					}
				}
				if (!found) {
					okay = false;
					break;
				}
			}
			if (okay) {
				setObjectiveState(m1t2, ObjectiveState.SUCCESS);
			}
		}
	}

	/**
	 * Check if either Naxos or San Sterling was lost.
	 * @param planet the event planet
	 */
	void checkMission1Task3Failure(Planet planet) {
		if (planet.id.equals("Naxos") || planet.id.equals("San Sterling")) {
			Objective o = objective("Mission-1-Task-3");
			if (o.visible) {
				if (setObjectiveState(o, ObjectiveState.FAILURE)) {
					setTimeout("Mission-1-Failure", 3000);
				}
			}
		}
	}
	/**
	 * Check if either Naxos or San Sterling was lost.
	 * @param planet the event planet
	 */
	void checkMission1Task4Failure(Planet planet) {
		if (planet.id.equals("Achilles")) {
			Objective o = objective("Mission-1-Task-4");
			if (o.visible) {
				if (setObjectiveState(o, ObjectiveState.FAILURE)) {
					setTimeout("Mission-1-Failure", 3000);
				}
			}
		}
	}
	/** Check the objectives. */
	void checkObjectives() {
		if (world.level == 1) {
			checkLevel1Objectives();
			send("Naxos-Check").visible = true;
			send("San Sterling-Check").visible = true;
			
		}
		
	}
	/**
	 * Create an objective from the XML.
	 * @param xo the objective XML
	 * @return the objective
	 */
	Objective createObjective(XElement xo) {
		Objective o = new Objective();
		o.id = xo.get("id");
		o.title = label(xo.get("title"));
		o.description = label(xo.get("description"));
		o.visible = xo.getBoolean("visible");
		return o;
	}
	@Override
	public List<Objective> currentObjectives() {
		// TODO Auto-generated method stub
		List<Objective> result = U.newArrayList();
		
		for (Objective o : rootObjectives.values()) {
			if (o.visible) {
				result.add(o);
			}
		}
		
		return result;
	}

	@Override
	public void done() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Returns a fleet.
	 * @param id the fleet id
	 * @return the fleet object
	 */
	Fleet fleet(int id) {
		for (Player p : world.players.values()) {
			for (Fleet f : p.fleets.keySet()) {
				if (f.id == id) {
					return f;
				}
			}
		}
		return null;
	}
	/**
	 * Format a localized label.
	 * @param id the label id
	 * @param params the parameters
	 * @return the localized text
	 */
	String format(String id, Object... params) {
		return world.env.labels().format(id, params);
	}

	@Override
	public List<VideoMessage> getReceiveMessages() {
		List<VideoMessage> result = U.newArrayList();

		for (VideoMessage msg : world.bridge.receiveMessages.values()) {
			if (msg.visible) {
				result.add(msg);
			}
		}
		
		Collections.sort(result, new Comparator<VideoMessage>() {
			@Override
			public int compare(VideoMessage o1, VideoMessage o2) {
				return o1.id.compareTo(o2.id);
			}
		});
		return result;
	}

	@Override
	public List<VideoMessage> getSendMessages() {
		List<VideoMessage> result = U.newArrayList();
		
		for (VideoMessage msg : world.bridge.sendMessages.values()) {
			if (msg.visible) {
				result.add(msg);
			}
		}
		Collections.sort(result, new Comparator<VideoMessage>() {
			@Override
			public int compare(VideoMessage o1, VideoMessage o2) {
				return o1.id.compareTo(o2.id);
			}
		});
		return result;
	}

	/**
	 * Hide messages that start with the given id.
	 * @param id the message id
	 */
	void hideMessages(String id) {
		for (VideoMessage msg : world.bridge.sendMessages.values()) {
			if (msg.id.startsWith(id)) {
				msg.visible = false;
			}
		}
		for (VideoMessage msg : world.bridge.receiveMessages.values()) {
			if (msg.id.startsWith(id)) {
				msg.visible = false;
			}
		}
	}

	@Override
	public void init(World world, XElement in) {
		this.world = world;
		player = world.player;
		init(in);
	}

	/**
	 * Initialize the default config.
	 * @param in the input XML
	 */
	protected void init(XElement in) {
		// view limits
		for (XElement xvls : in.childrenWithName("limit-views")) {
			for (XElement xvl : xvls.childrenWithName("limit-view")) {
				String id = xvl.get("id");
				ViewLimit vl = new ViewLimit();
				vl.inner = World.rectangleOf(xvl.get("inner"));
				vl.outer = World.rectangleOf(xvl.get("outer"));
				viewLimits.put(id, vl);
			}
		}
		// objectives
		for (XElement xos : in.childrenWithName("objectives")) {
			loadObjectives(xos, true);
		}
		setupObjectiveFunctions();
	}

	/**
	 * Check if the planet is under attack by a fleet.
	 * @param p the planet
	 * @return the fleet
	 */
	public boolean isUnderAttack(Planet p) {
		for (Fleet f : player.fleets.keySet()) {
			if (f.owner != player && f.targetPlanet() == p && f.mode == FleetMode.ATTACK) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Retrieve a localized label.
	 * @param id the label id
	 * @return the localized text
	 */
	String label(String id) {
		return world.env.labels().get(id);
	}

	@Override
	public void load(XElement in) {
		// TODO Auto-generated method stub
		lastLevel = in.getInt("lastLevel", world.level);
		for (XElement xmsgs : in.childrenWithName("receives")) {
			for (XElement xmsg : xmsgs.childrenWithName("receive")) {
				String id = xmsg.get("id");
				VideoMessage vm = receive(id);
				if (vm != null) {
					vm.visible = xmsg.getBoolean("visible");
				}
			}
		}
		for (XElement xmsgs : in.childrenWithName("sends")) {
			for (XElement xmsg : xmsgs.childrenWithName("send")) {
				String id = xmsg.get("id");
				VideoMessage vm = send(id);
				if (vm != null) {
					vm.visible = xmsg.getBoolean("visible");
				}
			}
		}
		for (XElement xos : in.childrenWithName("objectives")) {
			for (XElement xo : xos.childrenWithName("objective")) {
				String id = xo.get("id");
				Objective o = allObjectives.get(id);
				o.visible = xo.getBoolean("visible");
				o.state = ObjectiveState.valueOf(xo.get("state"));
			}
		}
		countdowns.clear();
		for (XElement xttls : in.childrenWithName("countdowns")) {
			for (XElement xttl : xttls.childrenWithName("countdown")) {
				countdowns.put(xttl.get("id"), xttl.getInt("value"));
			}
		}
	}

	/**
	 * Load the child-objectives.
	 * @param xos the parent XElement
	 * @param root is this a root list?
	 * @return the list of child objectives
	 */
	List<Objective> loadObjectives(XElement xos, boolean root) {
		List<Objective> result = U.newArrayList();
		for (XElement xo : xos.childrenWithName("objective")) {
			Objective o = createObjective(xo);
			if (root) {
				rootObjectives.put(o.id, o);
			}
			allObjectives.put(o.id, o);
			result.add(o);
			o.subObjectives.addAll(loadObjectives(xo, false));
		}
		return result;
	}

	/**
	 * Retrieve an objective.
	 * @param id the id
	 * @return the objective
	 */
	Objective objective(String id) {
		return allObjectives.get(id);
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
		if (world.level == 1) {
			checkMission1Task1(planet, building);
		}
	}

	@Override
	public void onColonized(Planet planet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConquered(Planet planet, Player previousOwner) {
		if (world.level == 1 && previousOwner == player) {
			checkMission1Failure(planet);
			checkMission1Task3Failure(planet);
			checkMission1Task4Failure(planet);
		}
	}

	@Override
	public void onDestroyed(Fleet winner, Fleet loser) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDiscovered(Player player, Fleet fleet) {
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
	public void onInventoryAdd(Planet planet, InventoryItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onInventoryRemove(Planet planet, InventoryItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLevelChanged() {
		applyViewLimits();
		applyPlanetOwners();
		lastLevel = world.level;
	}

	@Override
	public void onLost(Fleet fleet) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onLost(Planet planet) {
		if (world.level == 1) {
			checkMission1Failure(planet);
			checkMission1Task3Failure(planet);
		}
	}
	@Override
	public void onLostSight(Player player, Fleet fleet) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onMessageSeen(String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNewGame() {
		// TODO Auto-generated method stub
		lastLevel = world.level;
		onLevelChanged();
		
		objective("Mission-1").visible = true;
		objective("Mission-1-Task-1").visible = true;
		objective("Mission-1-Task-2").visible = true;
		objective("Mission-1-Task-3").visible = true;
		world.env.showObjectives(true);
	}

	@Override
	public void onPlanetCured(Planet planet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPlanetInfected(Planet planet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPlayerBeaten(Player player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProduced(Player player, ResearchType rt) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onRepairComplete(Planet planet, Building building) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onResearched(Player player, ResearchType rt) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onSoundComplete(String audio) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onStance(Player first, Player second) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onTime() {
		// TODO Auto-generated method stub
		for (Planet p : player.planets.keySet()) {
			if (p.owner != player) {
				hideMessages(p.id);
			} else {
				if (isUnderAttack(p)) {
					VideoMessage msg = send(p.id + "-Is-Under-Attack");
					if (msg != null) {
						msg.visible = true;
					}
				} else {
					VideoMessage msg = send(p.id + "-Not-Under-Attack");
					if (msg != null) {
						msg.visible = false;
					}
				}
			}
		}
		// update view limits if level changes
		if (lastLevel != world.level) {
			onLevelChanged();
		}
		updateCounters();
		checkObjectives();
	}
	/** Update timeout counters. */
	void updateCounters() {
		for (Map.Entry<String, Integer> e : countdowns.entrySet()) {
			Integer i = e.getValue();
			e.setValue(Math.max(0, i - world.env.simulationSpeed()));
		}
	}
	@Override
	public void onUpgrading(Planet planet, Building building, int newLevel) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onVideoComplete(String video) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * Returns a planet.
	 * @param id the planet id
	 * @return the planet object
	 */
	Planet planet(String id) {
		return world.planets.get(id);
	}
	/**
	 * Returns a player.
	 * @param id the player id
	 * @return the player
	 */
	Player player(String id) {
		return world.players.get(id);
	}
	/**
	 * Get a receive-message.
	 * @param id the message id
	 * @return the video message or null if not available
	 */
	VideoMessage receive(String id) {
		return world.bridge.receiveMessages.get(id);
	}
	@Override
	public void save(XElement out) {
		// TODO Auto-generated method stub
		out.set("lastLevel", lastLevel);
		
		XElement xmsgs = out.add("receives");
		for (VideoMessage vm : world.bridge.receiveMessages.values()) {
			XElement xmsg = xmsgs.add("receive");
			xmsg.set("id", vm.id);
			xmsg.set("visible", vm.visible);
		}
		xmsgs = out.add("sends");
		for (VideoMessage vm : world.bridge.sendMessages.values()) {
			XElement xmsg = xmsgs.add("send");
			xmsg.set("id", vm.id);
			xmsg.set("visible", vm.visible);
		}
		XElement xos = out.add("objectives");
		for (Objective o : allObjectives.values()) {
			XElement xo = xos.add("objective");
			xo.set("id", o.id);
			xo.set("visible", o.visible);
			xo.set("state", o.state);
		}
		
		XElement xttls = out.add("countdowns");
		for (Map.Entry<String, Integer> e : countdowns.entrySet()) {
			XElement xttl = xttls.add("countdown");
			xttl.set("id", e.getKey());
			xttl.set("value", e.getValue());
		}
	}
	/**
	 * Get a send-message.
	 * @param id the message id
	 * @return the video message or null if not available
	 */
	VideoMessage send(String id) {
		return world.bridge.sendMessages.get(id);
	}
	/**
	 * Change the objective state and show the objectives.
	 * @param o the objective
	 * @param newState the new state
	 * @return true if the state actually changed
	 */
	boolean setObjectiveState(Objective o, ObjectiveState newState) {
		if (o.state != newState) {
			o.state = newState;
			world.env.showObjectives(true);
			return true;
		}
		return false;
	}
	/**
	 * Change the owner of the given planet without invoking event handlers.
	 * @param planetId the planet id
	 * @param p the player owner
	 */
	void setOwner(String planetId, Player p) {
		Planet pl = planet(planetId);
		if (pl.owner != null) {
			pl.owner.planets.remove(pl);
		}
		pl.owner = p;
		p.planets.put(pl, PlanetKnowledge.BUILDING);
	}
	/**
	 * Set the progress lambdas on certain objectives.
	 */
	void setupObjectiveFunctions() {
		// TODO implement
	}
	/**
	 * Check if a timeout has been reached.
	 * @param id the reference id
	 * @return true if timeout reached
	 */
	boolean isTimeout(String id) {
		Integer i = countdowns.get(id);
		if (i != null && i.intValue() <= 0) {
			return true;
		}
		return false;
	}
	/**
	 * Register a new timeout counter.
	 * @param id the reference id
	 * @param time the time
	 */
	void setTimeout(String id, int time) {
		countdowns.put(id, time);
	}
	/**
	 * Remove timeout.
	 * @param id the reference id
	 */
	void clearTimeout(String id) {
		if (countdowns.remove(id) == null) {
			new AssertionError("ClearTimeout, missing id: " + id).printStackTrace();
		}
	}
	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onSpacewarStart(SpacewarWorld war) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onSpacewarStep(SpacewarWorld war) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onGroundwarFinish(GroundwarWorld war) {
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
	public boolean mayControlFleet(Fleet f) {
		// TODO Auto-generated method stub
		return true;
	}
}
