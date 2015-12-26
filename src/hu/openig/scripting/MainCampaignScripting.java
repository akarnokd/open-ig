/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting;

import hu.openig.core.Func1;
import hu.openig.core.SaveMode;
import hu.openig.model.BattleInfo;
import hu.openig.model.Building;
import hu.openig.model.Chats.Chat;
import hu.openig.model.Chats.Node;
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
import hu.openig.model.SoundTarget;
import hu.openig.model.SoundType;
import hu.openig.model.SpacewarScriptResult;
import hu.openig.model.SpacewarWorld;
import hu.openig.model.TraitKind;
import hu.openig.model.VideoMessage;
import hu.openig.model.ViewLimit;
import hu.openig.model.World;
import hu.openig.scripting.missions.Mission;
import hu.openig.scripting.missions.MissionScriptingHelper;
import hu.openig.utils.Exceptions;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

/**
 * The scripting for the original game's campaign.
 * @author akarnokd, 2012.01.12.
 */
public class MainCampaignScripting extends Mission implements GameScripting, MissionScriptingHelper {
	/** The view limits. */
	final Map<String, ViewLimit> viewLimits = new HashMap<>();
	/** The map of root objectives. */
	final Map<String, Objective> rootObjectives = new LinkedHashMap<>();
	/** The map of all main and sub objectives. */
	final Map<String, Objective> allObjectives = new LinkedHashMap<>();
	/** The last level. */
	int lastLevel;
	/** The countdowns of various actions. */
	final Map<String, Integer> countdowns = new HashMap<>();
	/** The mission timer. Contains mission specific start after N hours based on the current game since the start of the entire game. */
	final Map<String, Integer> missiontimer = new HashMap<>();
	/** The mission timer deallocation log. */
	final Map<String, String> missiontimerlog = new HashMap<>();
	/** Indicates a game over condition. */
	boolean gameOver;
	/** Set of fleet ids currently under control of scripting. */
	final Set<Integer> scriptedFleets = new HashSet<>();
	/** The list of missions. */
	final List<Mission> missions = new ArrayList<>();
	/** The debugging frame. */
	private JFrame debugFrame;
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
		for (Map.Entry<Planet, PlanetKnowledge> e : new HashMap<>(empire.planets).entrySet()) {
			if (e.getKey().owner == empire || e.getKey().owner == traders) {
				empire.planets.remove(e.getKey());
			}
		}
		for (int i = 1; i <= world.level; i++) {
			String[] ps = planets[i - 1];
			for (String pi : ps) {
				setOwner(pi, empire);
			}
		}
		if (empire.currentPlanet == null || empire.currentPlanet.owner == traders) {
			empire.currentPlanet = planet("Achilles");
		}
	}
	/**
	 * Apply view limits according to the current level.
	 */
	void applyViewLimits() {
		for (Player p : world.players.values()) {
			ViewLimit vl = getViewLimit(p, world.level);
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
		o.visible = false;
		return o;
	}
	@Override
	public List<Objective> currentObjectives() {
		List<Objective> result = new ArrayList<>();
		
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
		missiontimerlog.clear();
		scriptedFleets.clear();
		if (debugFrame != null) {
			debugFrame.dispose();
			debugFrame = null;
		}
	}

	@Override
	public List<VideoMessage> getSendMessages() {
		List<VideoMessage> result = new ArrayList<>();
		
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
		for (VideoMessage msg : world.receivedMessages) {
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
						Exceptions.add(new AssertionError(String.format("Mission class %s is incompatible", clazz)));
					}
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
					Exceptions.add(ex);
				}
			}
		}
		
		setupObjectiveFunctions();
	}

	@Override
	public void load(XElement in) {
		super.load(in);
		gameOver = in.getBoolean("gameover", false);
		lastLevel = in.getInt("lastLevel", world.level);
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
		countdowns.clear();
		for (XElement xttls : in.childrenWithName("countdowns")) {
			for (XElement xttl : xttls.childrenWithName("countdown")) {
				countdowns.put(xttl.get("id"), xttl.getInt("value"));
			}
		}
		missiontimer.clear();
		missiontimerlog.clear();
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
				} catch (IllegalAccessException | NoSuchFieldException ex) {
					Exceptions.add(ex);
				}
			}
		}
		
		scriptedFleets.clear();
		for (XElement xsfs : in.childrenWithName("scripted-fleets")) {
			for (XElement xsf : xsfs.childrenWithName("fleet")) {
				scriptedFleets.add(xsf.getInt("id"));
			}
		}
		
		Map<String, Mission> mss = new HashMap<>();
		for (Mission m : missions) {
			mss.put(m.getClass().getName(), m);
		}
		for (XElement xmss : in.childrenWithName("mission-states")) {
			for (XElement xms : xmss.childrenWithName("mission")) {
				String clazz = xms.get("class");
				Mission m = mss.get(clazz);
				if (m == null) {
					System.err.printf("Warning: Mission %s instance not found.%n", clazz);
				} else {
					m.load(xms);
				}
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
		List<Objective> result = new ArrayList<>();
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
			if (!m.applicable()) {
				continue;
			}
			m.onAllyAgainst(first, second, commonEnemy);
		}
	}

	@Override
	public void onBattleComplete(Player player, BattleInfo battle) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onBattleComplete(player, battle);
		}		
	}

	@Override
	public void onBuildingComplete(Planet planet, Building building) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onBuildingComplete(planet, building);
		}
	}

	@Override
	public void onColonized(Planet planet) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onColonized(planet);
		}
	}

	@Override
	public void onConquered(Planet planet, Player previousOwner) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onConquered(planet, previousOwner);
		}
	}

	@Override
	public void onDestroyed(Fleet winner, Fleet loser) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onDestroyed(winner, loser);
		}
	}

	@Override
	public void onDiscovered(Player player, Fleet fleet) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onDiscovered(player, fleet);
		}
	}
	@Override
	public void onDiscovered(Player player, Planet planet) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onDiscovered(player, planet);
		}
	}
	@Override
	public void onDiscovered(Player player, Player other) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onDiscovered(player, other);
		}
	}

	@Override
	public void onFleetAt(Fleet fleet, double x, double y) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onFleetAt(fleet, x, y);
		}
	}
	@Override
	public void onFleetAt(Fleet fleet, Fleet other) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onFleetAt(fleet, other);
		}
	}

	@Override
	public void onFleetAt(Fleet fleet, Planet planet) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onFleetAt(fleet, planet);
		}
	}

	@Override
	public void onInventoryAdd(Planet planet, InventoryItem item) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onInventoryAdd(planet, item);
		}
	}

	@Override
	public void onInventoryRemove(Planet planet, InventoryItem item) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onInventoryRemove(planet, item);
		}
	}

	@Override
	public void onLevelChanged() {
		applyViewLimits();
		applyPlanetOwners();
		for (Mission m : missions) {
			m.onLevelChanged();
		}
		lastLevel = world.level;

		world.env.save(SaveMode.LEVEL);
	}
	@Override
	public void clearMissionTimes(Func1<String, Boolean> filter) {
		for (String s : U.newArrayList(missiontimer.keySet())) {
			if (filter.invoke(s)) {
				missiontimer.remove(s);
				missiontimerlog.remove(s);
			}
		}
	}
	@Override
	public void clearTimeouts(Func1<String, Boolean> filter) {
		for (String s : U.newArrayList(countdowns.keySet())) {
			if (filter.invoke(s)) {
				countdowns.remove(s);
			}
		}
	}
	@Override
	public void clearObjectives(Func1<String, Boolean> filter) {
		for (Map.Entry<String, Objective> e : allObjectives.entrySet()) {
			if (filter.invoke(e.getKey())) {
				e.getValue().visible = false;
				e.getValue().state = ObjectiveState.ACTIVE;
			}
		}
	}

	@Override
	public void clearMessages(Func1<String, Boolean> filter) {
		for (Map.Entry<String, VideoMessage> e : world.bridge.sendMessages.entrySet()) {
			if (filter.invoke(e.getKey())) {
				e.getValue().visible = false;
				e.getValue().seen = false;
			}
		}
		for (VideoMessage e : world.receivedMessages) {
			if (filter.invoke(e.id)) {
				e.seen = false;
			}
		}
	}


	@Override
	public void onLost(Fleet fleet) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onLost(fleet);
		}
	}

	@Override
	public void onLost(Planet planet) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onLost(planet);
		}

	}
	@Override
	public void onLostSight(Player player, Fleet fleet) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onLostSight(player, fleet);
		}		
	}
	@Override
	public void onMessageSeen(String id) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onMessageSeen(id);
		}
	}

	@Override
	public void onNewGame() {
		lastLevel = 0;
		onLevelChanged();

		for (Mission m : missions) {
			m.onNewGame();
		}
	}

	@Override
	public void onPlanetCured(Planet planet) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onPlanetCured(planet);
		}
	}

	@Override
	public void onPlanetInfected(Planet planet) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onPlanetInfected(planet);
		}
	}

	@Override
	public void onPlayerBeaten(Player player) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onPlayerBeaten(player);
		}
	}

	@Override
	public void onProduced(Player player, ResearchType rt) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onProduced(player, rt);
		}		
	}
	@Override
	public void onRepairComplete(Planet planet, Building building) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onRepairComplete(planet, building);
		}		
	}
	@Override
	public void onResearched(Player player, ResearchType rt) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onResearched(player, rt);
		}		
	}
	@Override
	public void onSoundComplete(String audio) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onSoundComplete(audio);
		}		
	}
	@Override
	public void onStance(Player first, Player second) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onStance(first, second);
		}
	}
	@Override
	public void onTime() {
		// Run timed scripts only if the current player is the initial player. Helps in debugging other races in the campaign.
		if (player == world.player) {
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
			updateCounters();
			for (Mission m : missions) {
				if (lastLevel != world.level) {
					onLevelChanged();
				}
				if (!m.applicable()) {
					continue;
				}
				m.onTime();
			}
		}
	}
	/** Update timeout counters. */
	void updateCounters() {
		for (Map.Entry<String, Integer> e : countdowns.entrySet()) {
			Integer i = e.getValue();
			e.setValue(Math.max(0, i - world.env.simulationSpeed() * world.params().simulationRatio()));
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
	public void save(XElement out) {
		super.save(out);
		out.set("lastLevel", lastLevel);
		out.set("gameover", gameOver);
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
					Exceptions.add(ex);
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
				xsf.set("name", f.name());
			}
		}
		XElement xmss = out.add("mission-states");
		for (Mission m : missions) {
			XElement xms = xmss.add("mission");
			xms.set("class", m.getClass().getName());
			m.save(xms);
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
			if (world.config.autoDisplayObjectives) {
				world.env.showObjectives(true);
				if (newState == ObjectiveState.SUCCESS) {
					world.env.playSound(SoundTarget.EFFECT, SoundType.SUCCESS, null);
				} else 
				if (newState == ObjectiveState.FAILURE) {
					world.env.playSound(SoundTarget.EFFECT, SoundType.FAIL, null);
				}
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
        return i != null && i <= 0;
    }
	@Override
	public void setTimeout(String id, int time) {
		countdowns.put(id, time);
	}
	@Override
	public void clearTimeout(String id) {
		if (countdowns.remove(id) == null) {
			Exceptions.add(new AssertionError("ClearTimeout, missing id: " + id));
		}
	}
	@Override
	public void onSpacewarFinish(SpacewarWorld war) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onSpacewarFinish(war);
		}
	}
	@Override
	public void onSpacewarStart(SpacewarWorld war) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onSpacewarStart(war);
		}
	}
	@Override
	public SpacewarScriptResult onSpacewarStep(SpacewarWorld war) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			SpacewarScriptResult r = m.onSpacewarStep(war);
			if (r != null) {
				return r;
			}
		}	
		return null;
	}
	@Override
	public void onGroundwarFinish(GroundwarWorld war) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onGroundwarFinish(war);
		}
	}
	@Override
	public void onGroundwarStart(GroundwarWorld war) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onGroundwarStart(war);
		}		
	}
	@Override
	public void onGroundwarStep(GroundwarWorld war) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
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
			if (world.config.autoDisplayObjectives) {
				world.env.showObjectives(true);
				world.env.playSound(SoundTarget.EFFECT, SoundType.NEW_TASK, null);
			}
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
		missiontimerlog.remove(id);
	}
	@Override
	public void clearMissionTime(String id) {
		if (missiontimer.remove(id) == null) {
			Exceptions.add(new AssertionError(String.format("MissionTime %s not found.", id)));
			System.err.printf("MissionTime %s last deallocation: %s%n", id, missiontimerlog.get(id));
		} else {
			missiontimerlog.put(id, stackTrace(new AssertionError("MissionTime " + id)));
		}
	}
	@Override
	public boolean isMissionTime(String id) {
		Integer i = missiontimer.get(id);
		if (i != null) {
			return i <= now();
		}
		return false;
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
			if (!m.applicable()) {
				continue;
			}
			m.onAutobattleFinish(battle);
		}
	}
	@Override
	public void onAutobattleStart(BattleInfo battle) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onAutobattleStart(battle);
		}
	}
	@Override
	public void onTalkCompleted() {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onTalkCompleted();
		}		
	}
	@Override
	public void debug() {
		if (debugFrame != null) {
			debugFrame.dispose();
			debugFrame = null;
		}
		debugFrame = new JFrame("Debug mission triggers");
		debugFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JPanel panel = new JPanel();
		JScrollPane sp = new JScrollPane(panel);
		
		Container c = debugFrame.getContentPane();
		c.setLayout(new BorderLayout());
		
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
		c.add(split, BorderLayout.CENTER);
		split.setTopComponent(sp);
		
		panel.setLayout(new GridBagLayout());
		
		GridBagConstraints gc = new GridBagConstraints();
		for (int i = 1; i < 26; i++) {
			final int j = i;
			JButton btnStart = new JButton("Start M" + i + " now");
			btnStart.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Objective o = objective("Mission-" + j);
					if (o != null) {
						o.state = ObjectiveState.ACTIVE;
					}
					setMissionTime("Mission-" + j, 0);
				}
			});
			
			JButton btnSucceed = new JButton("Complete M" + i);
			btnSucceed.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Objective o = objective("Mission-" + j);
					if (o != null) {
						removeMission(j, false);
						o.state = ObjectiveState.SUCCESS;
						o.visible = false;
					}
				}
			});
			JButton btnReset = new JButton("Reset M" + i);
			btnReset.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					removeMission(j, true);
				}
			});
			if (i == 8) {
				btnReset.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						world.testNeeded = false;
						world.testCompleted = false;
					}
				});
			}
			if (i == 15) {
				btnReset.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						world.currentTalk = null;
						world.allowRecordMessage = false;
					}
				});
			}
			gc.gridx = 0;
			gc.gridy = i;
			panel.add(btnStart, gc);
			gc.gridx = 1;
			panel.add(btnReset, gc);
			gc.gridx = 2;
			panel.add(btnSucceed, gc);
		}
		
		JPanel dump = new JPanel();
		split.setBottomComponent(dump);
		
		dump.setLayout(new BorderLayout());
		
		final JTextArea out = new JTextArea(30, 10);
		out.setEditable(false);
		JButton dumpNow = new JButton("Dump timers");
		dumpNow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				out.setText("");
				out.append("-------------------\r\n");
				out.append("Realtime countdowns\r\n");
				out.append("-------------------\r\n");
				for (Map.Entry<String, Integer> es : countdowns.entrySet()) {
					out.append(es.getKey() + " : " + es.getValue() + "\r\n");
				}
				out.append("--------------\r\n");
				out.append("Mission timers\r\n");
				out.append("--------------\r\n");
				for (Map.Entry<String, Integer> es : missiontimer.entrySet()) {
					long t = world.initialDate.getTime() + 60L * 60L * 1000 * es.getValue();
					out.append(es.getKey() + " : " + es.getValue() + " (" + XElement.formatDateTime(new Date(t)) + ")\r\n");
					Objective o = objective(es.getKey());
					if (o != null) {
						out.append(" ~ " + o.title + "\r\n");
						if (o.description != null && !o.description.isEmpty()) {
							out.append(" ~ " + o.description + "\r\n");
						}
					}
				}
				out.append("----------\r\n");
				out.append("Objectives\r\n");
				out.append("----------\r\n");
				for (Map.Entry<String, Objective> es : allObjectives.entrySet()) {
					Objective o = es.getValue();
					out.append(es.getKey() + " : " + o.state + (o.visible ? " [visible]" : "") + "\r\n");
					out.append(" ~ " + o.title + "\r\n");
					if (o.description != null && !o.description.isEmpty()) {
						out.append(" ~ " + o.description + "\r\n");
					}
				}
				out.setCaretPosition(0);
			}
		});
		JScrollPane sp2 = new JScrollPane(out);
		dump.add(dumpNow, BorderLayout.NORTH);
		dump.add(sp2, BorderLayout.CENTER);
		
		debugFrame.setSize(800, 600);
		debugFrame.setLocationRelativeTo(null);
		debugFrame.setVisible(true);
	}
	/**
	 * Resets the mission.
	 * @param j the mission index
	 * @param reset reset the internal state?
	 */
	void removeMission(final int j, boolean reset) {
		Func1<String, Boolean> func = new Func1<String, Boolean>() {
			@Override
			public Boolean invoke(String value) {
				if (value.equals("Mission-" + j)) {
					return true;
				}
                return value.startsWith("Mission-" + j + "-");
            }
		};
		clearMessages(func);
		clearMissionTimes(func);
		clearTimeouts(func);
		clearObjectives(func);
		if (reset) {
			for (Mission m : missions) {
				if (m.getClass().getName().endsWith("n" + j)) {
					m.reset();
				}
			}
		}
	}
	@Override
	public boolean applicable() {
		return true;
	}
	@Override
	public boolean mayPlayerAttack(Player player) {
		// allow garthogs to attack
		return (world.level == 3 && player.id.equals("Garthog"))
				|| world.level > 3;
	}
	@Override
	public void onDeploySatellite(Planet target, Player player,
			ResearchType satellite) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			m.onDeploySatellite(target, player, satellite);
		}
	}
	@Override
	public boolean fleetBlink(Fleet f) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;
			}
			Boolean b = m.fleetBlink(f);
			if (b) {
				return b;
			}
		}
		return false;
	}
	@Override
	public void onFleetsMoved() {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;

			}
			m.onFleetsMoved();
		}
	}
	@Override
	public ViewLimit getViewLimit(Player player, int level) {
		return viewLimits.get(player.id + ".Level." + level);
	}
	@Override
	public void onSpaceChat(SpacewarWorld world, Chat chat, Node node) {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;

			}
			m.onSpaceChat(world, chat, node);
		}
	}
	@Override
	public void onRecordMessage() {
		for (Mission m : missions) {
			if (!m.applicable()) {
				continue;

			}
			m.onRecordMessage();
		}
	}

    /**
	 * Extract the stacktrace text from the exception.
	 * @param t the exception
	 * @return the stacktrace string
	 */
	public String stackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.flush();
		return sw.toString();
	}
	@Override
	public void onLoaded() {
		super.onLoaded();
		for (Mission m : missions) {
			m.onLoaded();
		}
	}
	@Override
	public boolean mayPlayerImproveDefenses(Player player) {
		return world.level >= 3 || !"Garthog".equals(player.id);
	}
	@Override
	public double playerPopulationGrowthOverride(Planet planet, double simulatorValue) {
		if ("Garthog".equals(planet.owner.id) && world.level < 3) {
			return Math.min(planet.population() + 50, simulatorValue);
		}
		return simulatorValue;
	}
	@Override
	public double playerTaxIncomeOverride(Planet planet, double simulatorValue) {
		if ("Garthog".equals(planet.owner.id) && world.level < 3) {
			return Math.min(10000, simulatorValue);
		}
		return simulatorValue;
	}
	@Override
	public int fleetSpeedOverride(Fleet fleet, int speed) {
		if ("Traders".equals(fleet.owner.id)  
				&& player.traits.has(TraitKind.PRE_WARP)
				&& !player.isAvailable("HyperDrive1")) {
			return speed / 2;
		}
		if ((world.level == 2 
				&& "Garthog".equals(fleet.owner.id) 
				&& (hasTag(fleet, "Mission-14-Garthog")) || hasTag(fleet, "Mission-17-Garthog"))) {
			speed = 5;
		}
		return speed;
	}
}
