/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Difficulty;
import hu.openig.core.Pair;
import hu.openig.utils.Exceptions;
import hu.openig.utils.U;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * The object describing the player's status and associated
 * objects.
 * @author akarnokd, 2009.10.25.
 */
public class Player {
	/** The world. */
	public final World world;
	/** The player id. */
	public final String id;
	/** The player's name. */
	public String name;
	/** The player's name. */
	public String shortName;
	/** The coloring used for this player. */
	public int color;
	/** The fleet icon. */
	public BufferedImage fleetIcon;
	/** The picture used in the database screen. */
	public BufferedImage picture;
	/** The race of the player. Determines the technology tree to be used. */
	public String race;
	/** The optional resource to play when contacting this race. */
	public String diplomacyHead;
	/** The in-progress production list. */
	public final Map<ResearchMainCategory, Map<ResearchType, Production>> production = new HashMap<ResearchMainCategory, Map<ResearchType, Production>>();
	/** The production history. */
	public final Map<ResearchMainCategory, List<ResearchType>> productionHistory = U.newHashMap();
	/** The in-progress research. */
	public final Map<ResearchType, Research> research = new HashMap<ResearchType, Research>();
	/** The completed research. */
	private final Map<ResearchType, List<ResearchType>> availableResearch = new LinkedHashMap<ResearchType, List<ResearchType>>();
	/** The fleets owned. */
	public final Map<Fleet, FleetKnowledge> fleets = new LinkedHashMap<Fleet, FleetKnowledge>();
	/** The planets owned. */
	public final Map<Planet, PlanetKnowledge> planets = new HashMap<Planet, PlanetKnowledge>();
	/** The inventory counts. */
	public final Map<ResearchType, Integer> inventory = new HashMap<ResearchType, Integer>();
	/** The current running research. */
	private ResearchType runningResearch;
	/** The actual planet. */
	public Planet currentPlanet;
	/** The actual fleet. */
	public Fleet currentFleet;
	/** The actual research. */
	private ResearchType currentResearch;
	/** The actual building. */
	public BuildingType currentBuilding;
	/** The type of the last selected thing: planet or fleet. */
	public SelectionMode selectionMode = SelectionMode.PLANET;
	/** The current money amount. */
	private long money;
	/** The player level statistics. */
	public final PlayerStatistics statistics = new PlayerStatistics();
	/** The global financial information yesterday. */
	public final PlayerFinances yesterday = new PlayerFinances();
	/** The global finalcial information today. */
	public final PlayerFinances today = new PlayerFinances();
	/** Initial stance for the newly discovered races. */
	public int initialStance = 50;
	/** The priority queue for the messages. */
	protected final PriorityQueue<Message> messageQueue = new PriorityQueue<Message>();
	/** The message history of the already displayes messages. */
	public final List<Message> messageHistory = new ArrayList<Message>();
	/** The AI behavior mode. */
	public AIMode aiMode;
	/** The defensive ratio for AI player. Ratios sum up to 1. */
	public double aiDefensiveRatio = 1.0 / 3;
	/** The offensive ratio for AI player. Ratios sum up to 1. */
	public double aiOffensiveRatio = 1.0 / 3;
	/** Do not list this player on the database screen. */
	public boolean noDatabase;
	/** Do not list this player in diplomacy tables. */
	public boolean noDiplomacy;
	/** The AI associated with this player. */
	public AIManager ai;
	/** If non-null, it represents the rectangle where the explorers should not go into. */
	public Rectangle explorationInnerLimit;
	/** If non-null, it represents the rectangle where the explorers should not go outside. */
	public Rectangle explorationOuterLimit;
	/** The colonization limit, -1 means unlimited. */
	public int colonizationLimit = -1;
	/** The limit where the AI considers attacking the other party. */
	public int warThreshold = 45;
	/** The negotiation offers from players. */
	public final Map<String, DiplomaticOffer> offers = U.newLinkedHashMap();
	/** The factor for police-to-morale conversion. */
	public double policeRatio;
	/** Pause the production without changing the production line settings. */
	public boolean pauseProduction;
	/** Pause the research without changing the current research settings. */
	public boolean pauseResearch;
	/** The selected traits for this player. */
	public final Traits traits = new Traits();
	/** The player AI's default difficulty. */
	public Difficulty difficulty;
	/** The group id, if bigger than zero. */
	public int group;
	/** The list of translated nicknames. */
	public final List<String> nicknames = U.newArrayList();
	/** The set of colonization targets. */
	public final Set<String> colonizationTargets = new LinkedHashSet<String>();
	/**
	 * Create a player for the world under the given id.
	 * @param world the world
	 * @param id the id
	 */
	public Player(World world, String id) {
		this.world = world;
		this.id = id;
		for (ResearchMainCategory cat : ResearchMainCategory.values()) {
			production.put(cat, new LinkedHashMap<ResearchType, Production>());
		}
	}
	/** @return the socual ratio for AI player. Ratios sum up to 1. */
	public double aiSocialRatio() {
		return Math.max(0, 1.0d - aiOffensiveRatio - aiDefensiveRatio);
	}
	/**
	 * @return returns the next planet by goind top-bottom relative to the current planet
	 */
	public Planet moveNextPlanet() {
		List<Planet> playerPlanets = ownPlanets();
		if (playerPlanets.size() > 0) {
			Collections.sort(playerPlanets, Planet.PLANET_ORDER);
			int idx = playerPlanets.indexOf(currentPlanet);
			Planet p = playerPlanets.get((idx + 1) % playerPlanets.size());
			currentPlanet = p;
			selectionMode = SelectionMode.PLANET;
			return p;
		}
		return null;
	}
	/**
	 * Determine the previous and next planet if there are more than 1 planets.
	 * @return the previous and next planets or null if only one planet is present
	 */
	public Pair<Planet, Planet> prevNextPlanet() {
		List<Planet> playerPlanets = ownPlanets();
		if (playerPlanets.size() > 1) {
			Collections.sort(playerPlanets, Planet.PLANET_ORDER);
			int idx = playerPlanets.indexOf(currentPlanet);
			Planet p1 = idx > 0 ? playerPlanets.get(idx - 1) : playerPlanets.get(playerPlanets.size() - 1);
			Planet p2 = playerPlanets.get((idx + 1) % playerPlanets.size());
			return Pair.of(p1, p2);
		}
		return null;
	}
	/**
	 * @return returns the previous planet by goind top-bottom relative to the current planet
	 */
	public Planet movePrevPlanet() {
		List<Planet> playerPlanets = ownPlanets();
		if (playerPlanets.size() > 0) {
			Collections.sort(playerPlanets, Planet.PLANET_ORDER);
			int idx = playerPlanets.indexOf(currentPlanet);
			if (idx == 0) {
				idx = playerPlanets.size(); 
			}
			Planet p = playerPlanets.get((idx - 1) % playerPlanets.size());
			currentPlanet = p;
			selectionMode = SelectionMode.PLANET;
			return p;
		}
		return null;
	}
	/**
	 * Test if the given research is available.
	 * @param rt the research
	 * @return true if available
	 */
	public boolean isAvailable(ResearchType rt) {
		return availableResearch.containsKey(rt);
	}
	/**
	 * Check if the given research ID is available.
	 * @param researchId the research ID
	 * @return true if available
	 */
	public boolean isAvailable(String researchId) {
		for (ResearchType rt : availableResearch.keySet()) {
			if (rt.id.equals(researchId)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * @return the number of built buildings per type
	 */
	public Map<BuildingType, Integer> countBuildings() {
		Map<BuildingType, Integer> result = new HashMap<BuildingType, Integer>();
		for (Planet p : planets.keySet()) {
			if (p.owner == this) {
				for (Building b : p.surface.buildings) {
					Integer cnt = result.get(b.type);
					result.put(b.type, cnt != null ? cnt + 1 : 1);
				}
			}
		}
		return result;
	}
	/** 
	 * @param map the optional map per planet.
	 * @return the global planet statistics. */
	public PlanetStatistics getPlanetStatistics(Map<Planet, PlanetStatistics> map) {
		PlanetStatistics ps = new PlanetStatistics();
		for (Planet p : planets.keySet()) {
			if (p.owner == this) {
				PlanetStatistics ps0 = p.getStatistics();
				if (map != null) {
					map.put(p, ps0);
				}
				ps.add(ps0);
			}
		}
		return ps;
	}
	/**
	 * Is there enough labs to research the technology? Does
	 * not consider the operational state of the labs.
	 * @param rt the technology
	 * @param ps the planet statistics
	 * @return true if there are at least the required lab
	 */
	public LabLevel hasEnoughLabs(ResearchType rt, PlanetStatistics ps) {
		if (ps.labs.civil < rt.civilLab) {
			return LabLevel.NOT_ENOUGH_TOTAL;
		}
		if (ps.labs.mech < rt.mechLab) {
			return LabLevel.NOT_ENOUGH_TOTAL;
		}
		if (ps.labs.comp < rt.compLab) {
			return LabLevel.NOT_ENOUGH_TOTAL;
		}
		if (ps.labs.ai < rt.aiLab) {
			return LabLevel.NOT_ENOUGH_TOTAL;
		}
		if (ps.labs.mil < rt.milLab) {
			return LabLevel.NOT_ENOUGH_TOTAL;
		}
		if (ps.activeLabs.civil < rt.civilLab) {
			return LabLevel.NOT_ENOUGH_ACTIVE;
		}
		if (ps.activeLabs.mech < rt.mechLab) {
			return LabLevel.NOT_ENOUGH_ACTIVE;
		}
		if (ps.activeLabs.comp < rt.compLab) {
			return LabLevel.NOT_ENOUGH_ACTIVE;
		}
		if (ps.activeLabs.ai < rt.aiLab) {
			return LabLevel.NOT_ENOUGH_ACTIVE;
		}
		if (ps.activeLabs.mil < rt.milLab) {
			return LabLevel.NOT_ENOUGH_ACTIVE;
		}
		
		return LabLevel.ENOUGH;
	}
	/**
	 * @param type the research type 
	 * @return the inventory count of the given research type. 
	 */
	public int inventoryCount(ResearchType type) {
		Integer c = inventory.get(type);
		return c != null ? c.intValue() : 0;
	}
	/**
	 * Add or remove a given amount of inventory item.
	 * @param type the type
	 * @param amount the delta
	 */
	public void changeInventoryCount(ResearchType type, int amount) {
		Integer c = inventory.get(type);
		if (c == null) {
			if (amount > 0) {
				inventory.put(type, amount);
			}
		} else {
			if (amount < 0 && c + amount <= 0) {
				inventory.remove(type);
			} else {
				inventory.put(type, c + amount);
			}
		}
	}
	/**
	 * Retrieve the this player's stance to the other player.
	 * @param p the other player
	 * @return the stance level 0..100
	 */
	public int getStance(Player p) {
		DiplomaticRelation dr = world.getRelation(this, p);
		return dr != null ? (int)dr.value : 0;
	}
	/**
	 * Set the stance with the given other player.
	 * @param p the other player
	 * @param value the value 0..100
	 */
	public void setStance(Player p, int value) {
		DiplomaticRelation dr = world.establishRelation(this, p);
		dr.value = value;
	}
	/** 
	 * Does this player know the other player?
	 * @param other the other player
	 * @return does this player know the other player? */
	public boolean knows(Player other) {
		DiplomaticRelation dr = world.getRelation(this, other);
		return dr != null && (dr.second.equals(other.id) || (dr.first.equals(other.id) && dr.full));
	}
	/**
	 * @return the set ow known players
	 */
	public Map<Player, DiplomaticRelation> knownPlayers() {
		Map<Player, DiplomaticRelation> result = U.newLinkedHashMap();
		for (DiplomaticRelation dr : world.relations) {
			if (dr.first.equals(id)) {
				result.put(world.players.get(dr.second), dr);
			}
			if (dr.second.equals(id) && dr.full) {
				result.put(world.players.get(dr.first), dr);
			}
		}
		return result;
	}
	/**
	 * @return The collection of all visible fleets. 
	 */
	public List<Fleet> visibleFleets() {
		return new ArrayList<Fleet>(fleets.keySet());
	}
	/**
	 * @return the list of player owned fleets sorted by name
	 */
	public List<Fleet> ownFleets() {
		List<Fleet> result = new ArrayList<Fleet>();
		for (Fleet f : fleets.keySet()) {
			if (f.owner == this) {
				result.add(f);
			}
		}
		Collections.sort(result, new Comparator<Fleet>() {
			@Override
			public int compare(Fleet o1, Fleet o2) {
				return o1.name.compareTo(o2.name);
			}
		});
		return result;
	}
	/** @return List the own planets. */
	public List<Planet> ownPlanets() {
		List<Planet> result = new ArrayList<Planet>();
		for (Planet p : planets.keySet()) {
			if (p.owner == this) {
				result.add(p);
			}
		}
		return result;
	}
	/** @return the current research. */
	public ResearchType currentResearch() {
		return currentResearch;
	}
	/**
	 * Change the current research type.
	 * @param type the research type
	 * @return this
	 */
	public Player currentResearch(ResearchType type) {
		this.currentResearch = type;
		return this;
	}
	/** 
	 * Add a research type without setting the equipment levels.
	 * @param rt the research to add
	 * @return true if this was a new research
	 */
	public boolean add(ResearchType rt) {
		if (!availableResearch.containsKey(rt)) {
			availableResearch.put(rt, new ArrayList<ResearchType>()) ;
			return true;
		}
		return false; 
	}
	/** 
	 * Set the availability of the given research.
	 * @param rt the research type
	 * @return this was a new research?
	 */
	public boolean setAvailable(ResearchType rt) {
		if (!availableResearch.containsKey(rt)) {
			setRelated(rt);
			return true;
		}
		return false;
	}
	/**
	 * Set the availability of slot technology based on current settings.
	 * @param rt the research type to set base technologies
	 */
	public void setRelated(ResearchType rt) {
		List<ResearchType> avail = new ArrayList<ResearchType>();
		
		for (EquipmentSlot slot : rt.slots.values()) {
			ResearchType et0 = null;
			for (ResearchType et : slot.items) {
				if (isAvailable(et)) {
					et0 = et;
				} else {
					break;
				}
			}
			if (et0 != null) {
				avail.add(et0);
			}
		}
		
		availableResearch.put(rt, avail);
	}
	/** @return map set of of the available research. */
	public Map<ResearchType, List<ResearchType>> available() {
		return availableResearch;
	}
	/**
	 * Returns a list of available researches used by the given research when it was completed.
	 * @param rt the base research
	 * @return the level of researches used by the base research
	 */
	public List<ResearchType> availableLevel(ResearchType rt) {
		if (availableResearch.containsKey(rt)) {
			return availableResearch.get(rt);
		}
		return Collections.emptyList();
	}
	/**
	 * Compare the current knowledge level of the given planet by the expected level.
	 * @param planet the target planet
	 * @param expected the expected level
	 * @return -1 if less known, 0 if exactly on the same level, +1 if more
	 */
	public int knowledge(Planet planet, PlanetKnowledge expected) {
		PlanetKnowledge k = planet.owner == this ? PlanetKnowledge.BUILDING : planets.get(planet);
		if (k == expected) {
			return 0;
		}
		if (k != null && expected == null) {
			return 1;
		}
		if (k == null && expected != null) {
			return -1;
		}
		return k == null || k.ordinal() < expected.ordinal() ? -1 : 1;
	}
	/**
	 * Compare the current knowledge level of the given fleet by the expected level.
	 * @param fleet the target planet
	 * @param expected the expected level
	 * @return -1 if less known, 0 if exactly on the same level, +1 if more
	 */
	public int knowledge(Fleet fleet, FleetKnowledge expected) {
		FleetKnowledge k = fleet.owner == this ? FleetKnowledge.FULL : fleets.get(fleet);
		if (k == expected) {
			return 0;
		}
		if (k != null && expected == null) {
			return 1;
		}
		if (k == null && expected != null) {
			return -1;
		}
		return k == null || k.ordinal() < expected.ordinal() ? -1 : 1;
	}
	/**
	 * Returns a fleet with the given ID.
	 * @param id the fleet id
	 * @return the fleet object
	 */
	public Fleet fleet(int id) {
		for (Fleet f : fleets.keySet()) {
			if (f.id == id) {
				return f;
			}
		}
		return null;
	}
	/** @return the race label. */
	public String getRaceLabel() {
		return "race." + race;
	}
	/** @return the active research or null if none. */
	public ResearchType runningResearch() {
		return runningResearch;
	}
	/**
	 * Sets the active research. Null to stop researching.
	 * @param rt the new technology to start researching
	 */
	public void runningResearch(ResearchType rt) {
		this.runningResearch = rt;
	}
	/**
	 * Check if the given coordinates fall into the allowed exploration regions (if exist).
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param ds the minimum distance from the border
	 * @return true if within limits
	 */
	public boolean withinLimits(double x, double y, int ds) {
		if (explorationInnerLimit != null && explorationInnerLimit.contains(x, y)) {
			Rectangle r = new Rectangle(explorationInnerLimit);
			r.grow(2 * ds, 2 * ds);
			if (r.contains(x, y)) {
				return false;
			}
		} else
		if (explorationOuterLimit != null && !explorationOuterLimit.contains(x, y)) {
			Rectangle r = new Rectangle(explorationOuterLimit);
			r.grow(-2 * ds, -2 * ds);
			if (!r.contains(x, y)) {
				return false;
			}
		}
		return true;
	}
	/**
	 * Enqueue a message.
	 * @param msg the message.
	 */
	public void addMessage(Message msg) {
		Message msg2 = messageQueue.peek();
		if (msg2 == null || msg2.priority < msg.priority) {
			messageQueue.add(msg);
		} else {
			addHistory(msg);
		}
		if (msg.sound != null) {
			world.env.playSound(SoundTarget.COMPUTER, msg.sound, null);
		}
	}
	/**
	 * Add an entry into the history listing.
	 * @param msg the message to add
	 */
	public void addHistory(Message msg) {
		messageHistory.add(msg);
		sortHistory();
	}
	/** Sort the history according to timestamp and priority. */
	public void sortHistory() {
		Collections.sort(messageHistory, new Comparator<Message>() {
			@Override
			public int compare(Message o1, Message o2) {
				int c = (o1.timestamp < o2.timestamp ? -1 : (o1.timestamp > o2.timestamp ? 1 : 0));
				if (c == 0) {
					c = o1.priority > o2.priority ? -1 : (o1.priority < o2.priority ? 1 : 0);;
				}
				return c;
			}
		});
	}
	/**
	 * @return peek the next message if any
	 */
	public Message peekMessage() {
		return messageQueue.peek();
	}
	/**
	 * Remove the specific message from the queue.
	 * @param msg the message to remove
	 */
	public void removeMessage(Message msg) {
		messageQueue.remove(msg);
	}
	/**
	 * Clear all entries from the messages.
	 */
	public void clearMessages() {
		messageQueue.clear();
	}
	@Override
	public String toString() {
		return id;
	}
	/**
	 * Add an entry to the production history.
	 * @param rt the technology
	 */
	public void addProductionHistory(ResearchType rt) {
		List<ResearchType> rts = productionHistory.get(rt.category.main);
		if (rts == null) {
			rts = U.newArrayList();
			productionHistory.put(rt.category.main, rts);
		}
		rts.remove(rt);
		rts.add(0, rt);
		for (int j = rts.size() - 1; j >= world.config.productionHistoryLimit; j--) {
			rts.remove(j);
		}
	}
	/**
	 * Fill-up available technologies.
	 */
	public void populateProductionHistory() {
		for (ResearchType rt : available().keySet()) {
			List<ResearchType> rts = productionHistory.get(rt.category.main);
			if (rts == null) {
				rts = U.newArrayList();
				productionHistory.put(rt.category.main, rts);
			}
			if (rts.size() < world.config.productionHistoryLimit && !rts.contains(rt)) {
				rts.add(rt);
			}
		}
	}
	/** @return The current money. */
	public long money() {
		return money;
	}
	/**
	 * Set a new money amount.
	 * @param newMoney the money amount
	 */
	public void money(long newMoney) {
		this.money = newMoney;
		if (this.money < 0) {
			Exceptions.add(new AssertionError("Negative money"));
		}
	}
	/**
	 * Change the money amount by the delta.
	 * @param delta the delta amount
	 */
	public void addMoney(long delta) {
		this.money += delta;
		if (this.money < 0) {
			Exceptions.add(new AssertionError("Negative money"));
		}
	}
	/**
	 * Returns an empire status copy of this player.
	 * @return the empire status
	 */
	public EmpireStatus toEmpireStatus() {
		EmpireStatus result = new EmpireStatus();
		
		result.id = id;
		result.money = money;

		for (Map.Entry<String, DiplomaticOffer> e : offers.entrySet()) {
			offers.put(e.getKey(), e.getValue().copy());
		}
		
		result.statistics.assign(statistics);
		result.yesterday.assign(yesterday);
		result.today.assign(today);
		
		result.pauseProduction = pauseProduction;
		result.pauseResearch = pauseResearch;
		
		result.colonizationTargets.addAll(colonizationTargets);
		
		return result;
	}
	/**
	 * Assign the values from the empire status record.
	 * @param es the empire status record
	 */
	public void fromEmpireStatus(EmpireStatus es) {
		this.money(es.money);
		this.offers.clear();
		for (Map.Entry<String, DiplomaticOffer> e : es.offers.entrySet()) {
			this.offers.put(e.getKey(), e.getValue().copy());
		}
		this.statistics.assign(es.statistics);
		this.yesterday.assign(es.yesterday);
		this.today.assign(es.today);
		this.pauseProduction = es.pauseProduction;
		this.pauseResearch = es.pauseResearch;
		this.colonizationTargets.clear();
		this.colonizationTargets.addAll(es.colonizationTargets);
	}
}
