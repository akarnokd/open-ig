/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Difficulty;
import hu.openig.core.Pair;
import hu.openig.utils.Exceptions;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
    protected final Map<ResearchMainCategory, Map<ResearchType, Production>> production = new HashMap<>();
    /** The production history. */
    public final Map<ResearchMainCategory, List<ResearchType>> productionHistory = new HashMap<>();
    /** The in-progress research. */
    protected final Map<ResearchType, Research> researches = new LinkedHashMap<>();
    /** The completed research. */
    protected final AvailableResearches availableResearch = new AvailableResearches();
    /** The fleets owned. */
    public final Map<Fleet, FleetKnowledge> fleets = new LinkedHashMap<>();
    /** The planets owned. */
    public final Map<Planet, PlanetKnowledge> planets = new LinkedHashMap<>();
    /** The inventory counts. */
    public final Map<ResearchType, Integer> inventory = new HashMap<>();
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
    private double money;
    /** The player level statistics. */
    public final PlayerStatistics statistics = new PlayerStatistics();
    /** The global financial information yesterday. */
    public final PlayerFinances yesterday = new PlayerFinances();
    /** The global financial information today. */
    public final PlayerFinances today = new PlayerFinances();
    /** Initial stance for the newly discovered races. */
    public int initialStance = 50;
    /** The priority queue for the messages. */
    protected final PriorityQueue<Message> messageQueue = new PriorityQueue<>();
    /** The message history of the already displayed messages. */
    public final List<Message> messageHistory = new ArrayList<>();
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
    public final Map<String, DiplomaticOffer> offers = new LinkedHashMap<>();
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
    public final List<String> nicknames = new ArrayList<>();
    /** The set of colonization targets. */
    public final Set<String> colonizationTargets = new LinkedHashSet<>();
    /** The collection of various spies. */
    public final List<Spy> spies = new ArrayList<>();
    /** The scaling of the incoming tax. */
    public double taxScale = 1.0;
    /** The daily base of the incoming tax. */
    public double taxBase = 0.0;
    /** The list of available black market items. */
    public final List<InventoryItem> blackMarket = new ArrayList<>();
    /** The in-game date when the black market should restock. */
    public Date blackMarketRestock;
    /** True if this player was originally designated in the campaign/skirmish to be the user's player. */
    public boolean isMainPlayer;
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
        if (!playerPlanets.isEmpty()) {
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
        if (!playerPlanets.isEmpty()) {
            Collections.sort(playerPlanets, Planet.PLANET_ORDER);
            int idx = playerPlanets.indexOf(currentPlanet);
            if (idx <= 0) {
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
        return availableResearch.containsKey(researchId);
    }
    /**
     * @return the number of built buildings per type
     */
    public Map<BuildingType, Integer> countBuildings() {
        Map<BuildingType, Integer> result = new HashMap<>();
        for (Planet p : planets.keySet()) {
            if (p.owner == this) {
                for (Building b : p.surface.buildings.iterable()) {
                    Integer cnt = result.get(b.type);
                    result.put(b.type, cnt != null ? cnt + 1 : 1);
                }
            }
        }
        return result;
    }
    /**
     * Calculate and cache the planet statistics.
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
        return c != null ? c : 0;
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
        Map<Player, DiplomaticRelation> result = new LinkedHashMap<>();
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
        return new ArrayList<>(fleets.keySet());
    }
    /**
     * @return the list of player owned fleets sorted by name
     */
    public List<Fleet> ownFleets() {
        List<Fleet> result = new ArrayList<>();
        for (Fleet f : fleets.keySet()) {
            if (f.owner == this) {
                result.add(f);
            }
        }
        Collections.sort(result, new Comparator<Fleet>() {
            @Override
            public int compare(Fleet o1, Fleet o2) {
                return o1.name().compareTo(o2.name());
            }
        });
        return result;
    }
    /** @return List the own planets. */
    public List<Planet> ownPlanets() {
        List<Planet> result = new ArrayList<>();
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
        if (!availableResearch.containsKey(rt.id)) {
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
        List<ResearchType> avail = new ArrayList<>();

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
    public Iterable<ResearchType> available() {
        return availableResearch.map().keySet();
    }
    /** @return map set of of the available research. */
    public Set<ResearchType> availableSet() {
        return availableResearch.map().keySet();
    }
    /**
     * Returns the number of available researches.
     * @return the available research count
     */
    public int availableCount() {
        return availableResearch.map().size();
    }
    /**
     * Removes the given available research.
     * @param rt the research to remove.
     */
    public void removeAvailable(ResearchType rt) {
        availableResearch.remove(rt);
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
                    c = o1.priority > o2.priority ? -1 : (o1.priority < o2.priority ? 1 : 0);
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
            rts = new ArrayList<>();
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
        for (ResearchType rt : available()) {
            List<ResearchType> rts = productionHistory.get(rt.category.main);
            if (rts == null) {
                rts = new ArrayList<>();
                productionHistory.put(rt.category.main, rts);
            }
            if (rts.size() < world.config.productionHistoryLimit && !rts.contains(rt)) {
                rts.add(rt);
            }
        }
    }
    /** @return The current money. */
    public long money() {
        return (long)money;
    }
    /**
     * Set a new money amount.
     * @param newMoney the money amount
     */
    public void money(double newMoney) {
        this.money = newMoney;
        if (this.money < 0) {
            Exceptions.add(new AssertionError("Negative money"));
        }
    }
    /**
     * Change the money amount by the delta.
     * @param delta the delta amount
     */
    public void addMoney(double delta) {
        this.money += delta;
        if (this.money < 0) {
            Exceptions.add(new AssertionError("Negative money"));
        }
    }
    /**
     * Does the player have a strong alliance with the other
     * player?
     * @param other the other player
     * @return true if it has a strong alliance
     */
    public boolean isStrongAlliance(Player other) {
        DiplomaticRelation dr = world.getRelation(this, other);
        return dr != null && dr.strongAlliance;
    }
    /**
     * Adds a production line for the given research type.
     * @param rt the technology
     * @return non-null if a new line was added or the production is already present
     */
    public Production addProduction(ResearchType rt) {
        if (rt.nobuild) {
            throw new IllegalArgumentException("The technology can't be produced.");
        }
        if (rt.category.main == ResearchMainCategory.BUILDINGS) {
            throw new IllegalArgumentException("Buildings can't be produced.");
        }
        if (isAvailable(rt)) {
            Map<ResearchType, Production> prodLine = production.get(rt.category.main);
            Production prod = prodLine.get(rt);
            if (prod == null) {
                if (prodLine.size() < 5) {
                    prod = new Production();
                    prod.type = rt;
                    prod.priority = 50;
                    prodLine.put(rt, prod);
                    addProductionHistory(rt);
                    return prod;

                }
            } else {
                addProductionHistory(rt);
                return prod;
            }
        }
        return null;
    }
    /**
     * Remove the production line for the given technology.
     * Unfinished progress is refunded 50%.
     * @param rt the research type
     */
    public void removeProduction(ResearchType rt) {
        Map<ResearchType, Production> prodLine = production.get(rt.category.main);
        Production prod = prodLine.remove(rt);
        if (prod != null) {
            int m = prod.progress / 2;
            addMoney(m);
            statistics.moneyProduction.value -= m;
            statistics.moneySpent.value -= m;
            world.statistics.moneyProduction.value -= m;
            world.statistics.moneySpent.value -= m;

            addProductionHistory(rt);
        }

    }
    /**
     * Returns the production line for the given technology, if exists.
     * @param rt the technology
     * @return the production entry or null if not in production
     */
    public Production getProduction(ResearchType rt) {
        Map<ResearchType, Production> prodLine = production.get(rt.category.main);
        return prodLine.get(rt);
    }
    /**
     * Sell the given number of items from the main inventory.
     * @param rt the technology.
     * @param count the count to sell
     */
    public void sellInventory(ResearchType rt, int count) {
        int current = inventoryCount(rt);
        int sell = Math.min(current, count);

        if (sell > 0) {
            changeInventoryCount(rt, -sell);

            long m = 1L * sell * rt.productionCost / 2;

            addMoney(m);

            statistics.moneySellIncome.value += m;
            statistics.moneyIncome.value += m;
            statistics.sellCount.value += sell;

            world.statistics.moneySellIncome.value += m;
            world.statistics.moneyIncome.value += m;
            world.statistics.sellCount.value += sell;
        }
    }
    /**
     * Returns true if all prerequisites of the given research type have been met.
     * If a research is available, it will result as false
     * @param rt the research type
     * @return true
     */
    public boolean canResearch(ResearchType rt) {
        if (!isAvailable(rt)) {
            if (rt.level <= world.level) {
                return checkPrerequisites(rt);
            }
        }
        return false;
    }
    /**
     * Check if the prerequisites of the research are available.
     * @param rt the technology to check
     * @return true if all prerequisites are met
     */
    private boolean checkPrerequisites(ResearchType rt) {
        for (ResearchType rt0 : rt.prerequisites) {
            if (!isAvailable(rt0)) {
                return false;
            }
        }
        return true;
    }
    /**
     * Starts the given research.
     * @param rt the technology to start
     * @return the research progress object
     */
    public Research startResearch(ResearchType rt) {
        if (rt.level > world.level) {
            throw new IllegalArgumentException("Research not available on this level");
        }
        if (isAvailable(rt)) {
            throw new IllegalStateException("Research available");
        }
        if (!checkPrerequisites(rt)) {
            throw new IllegalStateException("Prerequisites not met.");
        }
        ResearchType rrt = runningResearch();
        if (rrt != null) {
            Research r = researches.get(rrt);
            if (r != null && r.state != ResearchState.COMPLETE) {
                r.state = ResearchState.STOPPED;
            }
        }

        double moneyFactor = world.config.researchMoneyPercent / 1000d;

        runningResearch(rt);
        Research r = researches.get(rt);
        if (r == null) {
            r = new Research();
            r.type = rt;
            r.remainingMoney = r.type.researchCost(traits);
            researches.put(rt, r);
        }
        r.setMoneyFactor(moneyFactor);
        r.state = ResearchState.RUNNING;
        return r;
    }
    /**
     * Stop the given research.
     * @param rt the research to stop
     */
    public void stopResearch(ResearchType rt) {
        Research r = researches.get(rt);
        if (r != null) {
            r.state = ResearchState.STOPPED;
            if (r.type == runningResearch()) {
                runningResearch(null);
            }
        }
    }
    /**
     * Complete the the given running research and
     * remove it from the researches map.
     * @param rt the technology to complete
     */
    public void completeResearch(ResearchType rt) {
        Research r = researches.remove(rt);
        if (r != null) {
            r.state = ResearchState.COMPLETE;
            r.remainingMoney = 0;
            r.assignedMoney = 0;
            setAvailable(rt);

            statistics.researchCount.value++;
            world.statistics.researchCount.value++;
        }
    }
    /**
     * Returns a collection of all activated research progress.
     * @return the collection of research progress
     */
    public Collection<Research> researches() {
        return researches.values();
    }
    /**
     * Returns the research progress for the given technology if it
     * is among the started but unfinished researches.
     * @param rt the technology
     * @return the research progress or null if not present
     */
    public Research getResearch(ResearchType rt) {
        return researches.get(rt);
    }
    /**
     * Returns the progress for the current running research if any.
     * @return the current research progress or null if nothing is running
     */
    public Research runningResearchProgress() {
        ResearchType rrt = runningResearch();
        if (rrt != null) {
            return researches.get(rrt);
        }
        return null;
    }
    /**
     * Returns a collection for the active production lines under the given
     * main category.
     * @param mcat the main category
     * @return the collection of production lines
     */
    public Collection<Production> productionLines(ResearchMainCategory mcat) {
        return production.get(mcat).values();
    }
    /**
     * Returns the active production technologies under the given main category.
     * @param mcat the main category
     * @return the set of running production technologies
     */
    public Set<ResearchType> productionLineTypes(ResearchMainCategory mcat) {
        return production.get(mcat).keySet();
    }
    /**
     * Determine if this player has been defeated.
     * @return true if the player has been defeated
     */
    public boolean isDefeated() {
        return ownPlanets().isEmpty();
    }
    /**
     * Buy one unit of the given black market inventory item and optionally
     * deliver it to the given planet.
     * @param itemId the inventory identifier inside the black market list
     * @param deliverTo where to deploy the item, certain items are added to the player
     * inventory.
     */
    public void buy(int itemId, Planet deliverTo) {
        InventoryItem ii = null;
        for (InventoryItem ii0 : blackMarket) {
            if (ii0.id == itemId) {
                ii = ii0;
                break;
            }
        }
        if (ii == null) {
            throw new AssertionError("Item to buy not found: " + itemId);
        }
        boolean requiresPlanet = blackMarketRequiresPlanet(ii.type);
        if (requiresPlanet && deliverTo == null) {
            throw new AssertionError("Item requires a target planet: " + ii.type);
        }
        if (requiresPlanet && !deliverTo.hasMilitarySpaceport()) {
            throw new AssertionError("Military spaceport required: " + deliverTo + " for " + ii.type);
        }
        if (requiresPlanet && deliverTo.owner != this) {
            throw new AssertionError("Not the player's planet: " + deliverTo + " for " + ii.type);
        }
        long cost = blackMarketCost(ii);
        if (cost > money()) {
            throw new AssertionError("Not enough money. Required: " + cost + ", available: " + money);
        }

        addMoney(-cost);

        statistics.moneySpent.value += cost;
        world.statistics.moneySpent.value += cost;

        if (--ii.count == 0) {
            blackMarket.remove(ii);
        }

        if (!requiresPlanet) {
            changeInventoryCount(ii.type, 1);
        } else {
            if (ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
                if (deliverTo.getAddLimit(ii.type, this) == 0) {
                    throw new AssertionError("Planet is full: " + deliverTo + " can't deploy " + ii.type);
                }
                InventoryItem pii = new InventoryItem(world.newId(), this, ii.type);
                pii.assign(ii);
                pii.count = 1;
                deliverTo.inventory.add(pii);
            } else {
                // find a nearby fleet to deploy into
                Fleet found = null;
                for (Fleet f : fleets.keySet()) {
                    if (f.owner == this && f.nearbyPlanet() == deliverTo) {
                        if (f.getAddLimit(ii.type) > 0) {
                            found = f;
                            break;
                        }
                    }
                }

                if (found == null) {
                    // create a new fleet
                    found = deliverTo.newFleet();
                }
                InventoryItem pii = new InventoryItem(world.newId(), this, ii.type);
                pii.assign(ii);
                pii.count = 1;
                found.inventory.add(pii);
            }

        }
    }
    /**

     * Returns the unit cost of the given black market item.
     * @param ii the inventory item
     * @return the cost
     */
    public long blackMarketCost(InventoryItem ii) {
        return ii.unitSellValue() * 4;
    }
    /**
     * Returns true if a technology needs to be deployed to a planet.
     * @param rt the technology
     * @return true if planet is required
     */
    public boolean blackMarketRequiresPlanet(ResearchType rt) {
        return
                rt.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS
                || rt.category == ResearchSubCategory.SPACESHIPS_CRUISERS
                || rt.category == ResearchSubCategory.SPACESHIPS_STATIONS
        ;
    }
}
