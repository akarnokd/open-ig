/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting;

import hu.openig.model.BattleInfo;
import hu.openig.model.Building;
import hu.openig.model.Fleet;
import hu.openig.model.GameScripting;
import hu.openig.model.GroundwarWorld;
import hu.openig.model.InventoryItem;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.Player;
import hu.openig.model.ResearchType;
import hu.openig.model.SoundType;
import hu.openig.model.SpacewarWorld;
import hu.openig.model.VideoMessage;
import hu.openig.model.World;
import hu.openig.scripting.missions.Mission;
import hu.openig.scripting.missions.MissionScriptingHelper;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.Rectangle;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The scripting for the original game's campaign.
 * @author akarnokd, 2012.01.12.
 */
public class MainCampaignScripting extends Mission implements GameScripting, MissionScriptingHelper {
	/** The view limit records. */
	static class ViewLimit {
		/** The inner limit if non-null. */
		Rectangle inner;
		/** The outer limit if non-null. */
		Rectangle outer;
	}
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
	/** The mission timer. Contains mission specific start after N hours based on the current game since the start of the entire game. */
	final Map<String, Integer> missiontimer = U.newHashMap();
	/** Indicates a game over condition. */
	boolean gameOver;
	/** Set of fleet ids currently under control of scripting. */
	final Set<Integer> scriptedFleets = U.newHashSet();
	/** The list of missions. */
	final List<Mission> missions = U.newArrayList();
	/**
	 * Annotate fields of simple types to save their values into the game's state.
	 * @author akarnokd, 2012.01.14.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@interface Variable { }
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
	 * Create an objective from the XML.
	 * @param xo the objective XML
	 * @return the objective
	 */
	Objective createObjective(XElement xo) {
		Objective o = new Objective();
		o.id = xo.get("id");
		o.title = label(xo.get("title"));
		String d = xo.get("description", null);
		if (d != null) {
			o.description = label(d);
		}
		o.visible = xo.getBoolean("visible");
		return o;
	}
	@Override
	public List<Objective> currentObjectives() {
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
		world = null;
		player = null;
		allObjectives.clear();
		rootObjectives.clear();
		missions.clear();
		countdowns.clear();
		missiontimer.clear();
		scriptedFleets.clear();
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
	public void init(Player player, XElement in) {
		super.init(player, null, null);
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
		for (XElement xmss : in.childrenWithName("missions")) {
			for (XElement xms : xmss.childrenWithName("mission")) {
				String clazz = xms.get("class");
				try {
					Class<?> c = Class.forName(clazz);
					if (Mission.class.isAssignableFrom(c)) {
						Mission m = Mission.class.cast(c.newInstance());
						m.init(player, this, xms);
						missions.add(m);
					} else {
						new AssertionError(String.format("Mission class %s is incompatible", clazz));
					}
				} catch (InstantiationException ex) {
					ex.printStackTrace();
				} catch (IllegalAccessException ex) {
					ex.printStackTrace();
				} catch (ClassNotFoundException ex) {
					ex.printStackTrace();
				}
			}
		}
		
		setupObjectiveFunctions();
	}

	@Override
	public void load(XElement in) {
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
		missiontimer.clear();
		for (XElement xttls : in.childrenWithName("missiontimers")) {
			for (XElement xttl : xttls.childrenWithName("missiontime")) {
				missiontimer.put(xttl.get("id"), xttl.getInt("value"));
			}
		}
		
		for (XElement xvars : in.childrenWithName("variables")) {
			for (XElement xvar : xvars.childrenWithName("var")) {
				String id = xvar.get("id");
				String value = xvar.get("value");
				try {
					Field f = getClass().getDeclaredField(id);
					if (f.isAnnotationPresent(Variable.class)) {
						if (f.getType() == Boolean.TYPE) {
							f.set(this, "true".equals(value));
						} else
						if (f.getType() == Integer.TYPE) {
							f.set(this, Integer.parseInt(value));
						} else
						if (f.getType() == Long.TYPE) {
							f.set(this, Long.parseLong(value));
						} else
						if (f.getType() == Float.TYPE) {
							f.set(this, Float.parseFloat(value));
						} else
						if (f.getType() == Double.TYPE) {
							f.set(this, Double.parseDouble(value));
						} else
						if (f.getType() == String.class) {
							f.set(this, value);
						} else {
							System.err.printf("Field %s of type %s received the value %s%n", f.getName(), f.getType(), value);
						}
					}
				} catch (IllegalAccessException ex) {
					ex.printStackTrace();
				} catch (NoSuchFieldException ex) {
					ex.printStackTrace();
				}
			}
		}
		
		scriptedFleets.clear();
		for (XElement xsfs : in.childrenWithName("scripted-fleets")) {
			for (XElement xsf : xsfs.childrenWithName("fleet")) {
				scriptedFleets.add(xsf.getInt("id"));
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

	@Override
	public Objective objective(String id) {
		return allObjectives.get(id);
	}

	@Override
	public void onAllyAgainst(Player first, Player second, Player commonEnemy) {
		for (Mission m : missions) {
			m.onAllyAgainst(first, second, commonEnemy);
		}
	}

	@Override
	public void onBattleComplete(Player player, BattleInfo battle) {
		for (Mission m : missions) {
			m.onBattleComplete(player, battle);
		}		
	}

	@Override
	public void onBuildingComplete(Planet planet, Building building) {
		for (Mission m : missions) {
			m.onBuildingComplete(planet, building);
		}
	}

	@Override
	public void onColonized(Planet planet) {
		for (Mission m : missions) {
			m.onColonized(planet);
		}
	}

	@Override
	public void onConquered(Planet planet, Player previousOwner) {
		for (Mission m : missions) {
			m.onConquered(planet, previousOwner);
		}
	}

	@Override
	public void onDestroyed(Fleet winner, Fleet loser) {
		for (Mission m : missions) {
			m.onDestroyed(winner, loser);
		}
	}

	@Override
	public void onDiscovered(Player player, Fleet fleet) {
		for (Mission m : missions) {
			m.onDiscovered(player, fleet);
		}
	}
	@Override
	public void onDiscovered(Player player, Planet planet) {
		for (Mission m : missions) {
			m.onDiscovered(player, planet);
		}
	}
	@Override
	public void onDiscovered(Player player, Player other) {
		for (Mission m : missions) {
			m.onDiscovered(player, other);
		}
	}

	@Override
	public void onFleetAt(Fleet fleet, double x, double y) {
		for (Mission m : missions) {
			m.onFleetAt(fleet, x, y);
		}
	}
	@Override
	public void onFleetAt(Fleet fleet, Fleet other) {
		for (Mission m : missions) {
			m.onFleetAt(fleet, other);
		}
	}

	@Override
	public void onFleetAt(Fleet fleet, Planet planet) {
		for (Mission m : missions) {
			m.onFleetAt(fleet, planet);
		}
	}

	@Override
	public void onInventoryAdd(Planet planet, InventoryItem item) {
		for (Mission m : missions) {
			m.onInventoryAdd(planet, item);
		}
	}

	@Override
	public void onInventoryRemove(Planet planet, InventoryItem item) {
		for (Mission m : missions) {
			m.onInventoryRemove(planet, item);
		}
	}

	@Override
	public void onLevelChanged() {
		applyViewLimits();
		applyPlanetOwners();
		lastLevel = world.level;
		for (Mission m : missions) {
			m.onLevelChanged();
		}
	}

	@Override
	public void onLost(Fleet fleet) {
		for (Mission m : missions) {
			m.onLost(fleet);
		}
	}

	@Override
	public void onLost(Planet planet) {
		for (Mission m : missions) {
			m.onLost(planet);
		}

	}
	@Override
	public void onLostSight(Player player, Fleet fleet) {
		for (Mission m : missions) {
			m.onLostSight(player, fleet);
		}		
	}
	@Override
	public void onMessageSeen(String id) {
		for (Mission m : missions) {
			m.onMessageSeen(id);
		}
	}

	@Override
	public void onNewGame() {
		lastLevel = world.level;
		onLevelChanged();

		for (Mission m : missions) {
			m.onNewGame();
		}
	}

	@Override
	public void onPlanetCured(Planet planet) {
		for (Mission m : missions) {
			m.onPlanetCured(planet);
		}
	}

	@Override
	public void onPlanetInfected(Planet planet) {
		for (Mission m : missions) {
			m.onPlanetInfected(planet);
		}
	}

	@Override
	public void onPlayerBeaten(Player player) {
		for (Mission m : missions) {
			m.onPlayerBeaten(player);
		}
	}

	@Override
	public void onProduced(Player player, ResearchType rt) {
		for (Mission m : missions) {
			m.onProduced(player, rt);
		}		
	}
	@Override
	public void onRepairComplete(Planet planet, Building building) {
		for (Mission m : missions) {
			m.onRepairComplete(planet, building);
		}		
	}
	@Override
	public void onResearched(Player player, ResearchType rt) {
		for (Mission m : missions) {
			m.onResearched(player, rt);
		}		
	}
	@Override
	public void onSoundComplete(String audio) {
		for (Mission m : missions) {
			m.onSoundComplete(audio);
		}		
	}
	@Override
	public void onStance(Player first, Player second) {
		for (Mission m : missions) {
			m.onStance(first, second);
		}
	}
	@Override
	public void onTime() {
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
		for (Mission m : missions) {
			m.onTime();
		}
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
		for (Mission m : missions) {
			m.onUpgrading(planet, building, newLevel);
		}
	}
	@Override
	public void onVideoComplete(String video) {
		for (Mission m : missions) {
			m.onVideoComplete(video);
		}		
	}
	@Override
	public VideoMessage receive(String id) {
		return world.bridge.receiveMessages.get(id);
	}
	@Override
	public void save(XElement out) {
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

		xttls = out.add("missiontimers");
		for (Map.Entry<String, Integer> e : missiontimer.entrySet()) {
			XElement xttl = xttls.add("missiontime");
			xttl.set("id", e.getKey());
			int h = e.getValue();
			
			Date dt = new Date(world.initialDate.getTime() + h * 60L * 60 * 1000);
			xttl.set("value", h);
			xttl.set("date", XElement.formatDateTime(dt));
		}

		XElement xvars = out.add("variables");
		for (Field f : getClass().getDeclaredFields()) {
			if (f.isAnnotationPresent(Variable.class)) {
				try {
					XElement xvar = xvars.add("var");
					xvar.set("id", f.getName());
					xvar.set("value", f.get(this));
				} catch (IllegalAccessException ex) {
					ex.printStackTrace();
				}
			}
		}
		XElement xsfs = out.add("scripted-fleets");
		for (int i : scriptedFleets) {
			Fleet f = fleet(i);
			if (f != null) {
				XElement xsf = xsfs.add("fleet");
				xsf.set("id", i);
				xsf.set("owner", f.owner.id);
				xsf.set("name", f.name);
			}
		}
	}
	@Override
	public VideoMessage send(String id) {
		return world.bridge.sendMessages.get(id);
	}
	@Override
	public boolean setObjectiveState(String oId, ObjectiveState newState) {
		return setObjectiveState(objective(oId), newState);
	}
	@Override
	public boolean setObjectiveState(Objective o, ObjectiveState newState) {
		if (o.state != newState) {
			o.state = newState;
			world.env.showObjectives(true);
			if (newState == ObjectiveState.SUCCESS) {
				world.env.effectSound(SoundType.SUCCESS);
			} else 
			if (newState == ObjectiveState.FAILURE) {
				world.env.effectSound(SoundType.FAIL);
			}
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
	@Override
	public boolean isTimeout(String id) {
		Integer i = countdowns.get(id);
		if (i != null && i.intValue() <= 0) {
			return true;
		}
		return false;
	}
	@Override
	public void setTimeout(String id, int time) {
		countdowns.put(id, time);
	}
	@Override
	public void clearTimeout(String id) {
		if (countdowns.remove(id) == null) {
			new AssertionError("ClearTimeout, missing id: " + id).printStackTrace();
		}
	}
	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		for (Mission m : missions) {
			m.onSpacewarFinish(war);
		}
	}
	@Override
	public void onSpacewarStart(SpacewarWorld war) {
		for (Mission m : missions) {
			m.onSpacewarStart(war);
		}
	}
	@Override
	public void onSpacewarStep(SpacewarWorld war) {
		for (Mission m : missions) {
			m.onSpacewarStep(war);
		}		
	}
	@Override
	public void onGroundwarFinish(GroundwarWorld war) {
		for (Mission m : missions) {
			m.onGroundwarFinish(war);
		}
	}
	@Override
	public void onGroundwarStart(GroundwarWorld war) {
		for (Mission m : missions) {
			m.onGroundwarStart(war);
		}		
	}
	@Override
	public void onGroundwarStep(GroundwarWorld war) {
		for (Mission m : missions) {
			m.onGroundwarStep(war);
		}
	}
	@Override
	public boolean mayControlFleet(Fleet f) {
		return !scriptedFleets.contains(f.id);
	}
	@Override
	public boolean showObjective(String id) {
		return showObjective(objective(id));
	}
	@Override
	public boolean showObjective(Objective o) {
		if (!o.visible) {
			o.visible = true;
			world.env.showObjectives(true);
			world.env.effectSound(SoundType.NEW_TASK);
			return true;
		}
		return false;
	}
	@Override
	public boolean mayAutoSave() {
		return !gameOver;
	}
	@Override
	public int now() {
		long init = world.initialDate.getTime();
		long now = world.time.getTimeInMillis();
		
		return (int)((now - init) / 3600000);
	}
	@Override
	public void setMissionTime(String id, int hours) {
		missiontimer.put(id, hours);
	}
	@Override
	public void clearMissionTime(String id) {
		if (missiontimer.remove(id) == null) {
			new AssertionError(String.format("MissionTime %s not found.", id)).printStackTrace();
		}
	}
	@Override
	public boolean isMissionTime(String id) {
		Integer i = missiontimer.get(id);
		if (i != null) {
			return i.intValue() <= now();
		}
		return false;
	}
	@Override
	public boolean isActive(String oId) {
		return isActive(objective(oId));
	}
	@Override
	public boolean isActive(Objective o) {
		return o.visible && o.state == ObjectiveState.ACTIVE;
	}
	@Override
	public boolean canStart(String oId) {
		Objective o0 = objective(oId);
		return !o0.visible && o0.state == ObjectiveState.ACTIVE && isMissionTime(oId); 
	}
	@Override
	public boolean hasMissionTime(String id) {
		return missiontimer.containsKey(id);
	}
	@Override
	public boolean hasTimeout(String id) {
		return countdowns.containsKey(id);
	}
	@Override
	public void gameover() {
		gameOver = true;
	}
	@Override
	public Set<Integer> scriptedFleets() {
		return scriptedFleets;
	}
	@Override
	public void onAutobattleFinish(BattleInfo battle) {
		for (Mission m : missions) {
			m.onAutobattleFinish(battle);
		}
	}
	@Override
	public void onAutobattleStart(BattleInfo battle) {
		for (Mission m : missions) {
			m.onAutobattleStart(battle);
		}
	}
}
