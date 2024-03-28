/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Action0;
import hu.openig.core.Func1;
import hu.openig.model.BattleInfo;
import hu.openig.model.Building;
import hu.openig.model.Chats.Chat;
import hu.openig.model.Chats.Node;
import hu.openig.model.Fleet;
import hu.openig.model.FleetMode;
import hu.openig.model.GameScriptingEvents;
import hu.openig.model.GroundwarWorld;
import hu.openig.model.InventoryItem;
import hu.openig.model.InventorySlot;
import hu.openig.model.ModelUtils;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;
import hu.openig.model.SoundTarget;
import hu.openig.model.SoundType;
import hu.openig.model.SpacewarScriptResult;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarWorld;
import hu.openig.model.VideoMessage;
import hu.openig.model.ViewLimit;
import hu.openig.model.World;
import hu.openig.utils.Exceptions;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * The base class for missions.
 * @author akarnokd, 2012.01.14.
 */
public abstract class Mission implements GameScriptingEvents {
    /** The world object. */
    protected World world;
    /** The player object. */
    protected Player player;
    /** The scripting helper. */
    private MissionScriptingHelper helper;
    /** The pending objectives in case of save between incoming message and objective showup. */
    protected Deque<String[]> pendingObjectives = new LinkedList<>();
    /**
     * Initializes the mission object.
     * @param player the main player
     * @param helper the scripting helper
     * @param settings the initialization settings
     */
    public void init(Player player, MissionScriptingHelper helper, XElement settings) {
        this.player = player;
        this.helper = helper;
        this.world = player.world;
        initSettings(settings);
    }
    /**
     * Initialize the mission settings from the script configuration.
     * Can be overridden.
     * @param in the XML node of the mission
     */
    protected void initSettings(XElement in) {
        // default no-op
    }
    /**
     * Create a new fleet for the given owner.
     * @param name the fleet name
     * @param owner the owner
     * @param x the placement location
     * @param y the placement location
     * @return the fleet object
     */
    protected Fleet createFleet(String name, Player owner, double x, double y) {
        Fleet f = new Fleet(owner);
        f.name(name);
        f.x = x;
        f.y = y;
//        owner.fleets.put(f, FleetKnowledge.FULL);
        return f;
    }
    /**
     * Find visible fleets considering the current exploration limits as well.
     * @param toPlayer who should see the fleets
     * @param shouldSee should the player see the ship?
     * @param owner the owner of the fleets
     * @param ds the distance from limits
     * @return the list of fleets found
     */
    protected List<Fleet> findVisibleFleets(Player toPlayer, boolean shouldSee, Player owner, int ds) {
        List<Fleet> result = new ArrayList<>();

        for (Fleet f : owner.fleets.keySet()) {
            if (f.owner == owner

                    && (toPlayer.fleets.containsKey(f) || !shouldSee)
                    && toPlayer.withinLimits(f.x, f.y, ds)) {
                result.add(f);
            }
        }

        return result;
    }
    /**
     * Returns a planet.
     * @param id the planet id
     * @return the planet object
     */
    protected Planet planet(String id) {
        return world.planets.get(id);
    }
    /**
     * Returns a player.
     * @param id the player id
     * @return the player
     */
    protected Player player(String id) {
        return world.players.get(id);
    }
    /**
     * Find a fleet and inventory item having the given tag.
     * @param tag the tag
     * @param owner the owner of the fleet
     * @return the fleet and inventory item pair
     */
    protected Fleet findTaggedFleet(String tag, Player owner) {
        for (Fleet f : owner.fleets.keySet()) {
            if (f.owner == owner) {
                for (InventoryItem ii : f.inventory.iterable()) {
                    if (tag.equals(ii.tag)) {
                        return f;
                    }
                }
            }
        }
        return null;
    }
    /**
     * Retrieve a localized label.
     * @param id the label id
     * @return the localized text
     */
    protected String label(String id) {
        return world.env.labels().get(id);
    }
    /**
     * Check if the planet is under attack by a fleet.
     * @param p the planet
     * @return the fleet
     */
    protected boolean isUnderAttack(Planet p) {
        for (Fleet f : player.fleets.keySet()) {
            if (f.owner != player && f.targetPlanet() == p && f.mode == FleetMode.ATTACK) {
                return true;
            }
        }
        return false;
    }
    /**
     * Format a localized label.
     * @param id the label id
     * @param params the parameters
     * @return the localized text
     */
    protected String format(String id, Object... params) {
        return world.env.labels().format(id, params);
    }
    /**
     * Returns a fleet.
     * @param id the fleet id
     * @return the fleet object
     */
    protected Fleet fleet(int id) {
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
     * Lose the current game with the given forced message.
     * @param id the video message id
     */
    protected void loseGameMessage(String id) {
        world.env.stopMusic();
        world.env.pause();
        world.player.clearMessages();
        if (addIncomingMessage(id)) {
            world.env.forceMessage(id, new Action0() {
                @Override
                public void invoke() {
                    world.env.loseGame();
                }
            });
        }
    }
    /**
     * Lose the current game with the forced message and then full screen movie.
     * @param message the message
     * @param movie the movie
     */
    protected void loseGameMessageAndMovie(String message, final String movie) {
        world.env.stopMusic();
        world.env.pause();
        world.player.clearMessages();
        if (addIncomingMessage(message)) {
            world.env.forceMessage(message, new Action0() {
                @Override
                public void invoke() {
                    world.env.playVideo(movie, new Action0() {
                        @Override
                        public void invoke() {
                            world.env.loseGame();
                        }
                    });
                }
            });
        }
    }
    /**
     * Lose the current game with the given forced message.
     * @param id the video message id
     */
    protected void loseGameMovie(String id) {
        world.env.stopMusic();
        world.env.pause();
        world.player.clearMessages();
        world.env.playVideo(id, new Action0() {
            @Override
            public void invoke() {
                world.env.loseGame();
            }
        });
    }

    @Override
    public void onResearched(Player player, ResearchType rt) {
        // default implementation does not react to this event

    }

    @Override
    public void onProduced(Player player, ResearchType rt) {
        // default implementation does not react to this event

    }

    @Override
    public void onDestroyed(Fleet winner, Fleet loser) {
        // default implementation does not react to this event

    }

    @Override
    public void onColonized(Planet planet) {
        // default implementation does not react to this event

    }

    @Override
    public void onConquered(Planet planet, Player previousOwner) {
        // default implementation does not react to this event

    }

    @Override
    public void onPlayerBeaten(Player player) {
        // default implementation does not react to this event

    }

    @Override
    public void onDiscovered(Player player, Planet planet) {
        // default implementation does not react to this event

    }

    @Override
    public void onDiscovered(Player player, Player other) {
        // default implementation does not react to this event

    }

    @Override
    public void onDiscovered(Player player, Fleet fleet) {
        // default implementation does not react to this event

    }

    @Override
    public void onLostSight(Player player, Fleet fleet) {
        // default implementation does not react to this event

    }

    @Override
    public void onFleetAt(Fleet fleet, double x, double y) {
        // default implementation does not react to this event

    }

    @Override
    public void onFleetAt(Fleet fleet, Fleet other) {
        // default implementation does not react to this event

    }

    @Override
    public void onFleetAt(Fleet fleet, Planet planet) {
        // default implementation does not react to this event

    }

    @Override
    public void onStance(Player first, Player second) {
        // default implementation does not react to this event

    }

    @Override
    public void onAllyAgainst(Player first, Player second, Player commonEnemy) {
        // default implementation does not react to this event

    }

    @Override
    public void onBattleComplete(Player player, BattleInfo battle) {
        // default implementation does not react to this event

    }

    @Override
    public void onTime() {
        // default implementation does not react to this event

    }

    @Override
    public void onBuildingComplete(Planet planet, Building building) {
        // default implementation does not react to this event

    }

    @Override
    public void onRepairComplete(Planet planet, Building building) {
        // default implementation does not react to this event

    }

    @Override
    public void onUpgrading(Planet planet, Building building, int newLevel) {
        // default implementation does not react to this event

    }

    @Override
    public void onInventoryAdd(Planet planet, InventoryItem item) {
        // default implementation does not react to this event

    }

    @Override
    public void onInventoryRemove(Planet planet, InventoryItem item) {
        // default implementation does not react to this event

    }

    @Override
    public void onLost(Planet planet) {
        // default implementation does not react to this event

    }

    @Override
    public void onLost(Fleet fleet) {
        // default implementation does not react to this event

    }

    @Override
    public void onVideoComplete(String video) {
        // default implementation does not react to this event

    }

    @Override
    public void onSoundComplete(String audio) {
        // default implementation does not react to this event

    }

    @Override
    public void onPlanetInfected(Planet planet) {
        // default implementation does not react to this event

    }

    @Override
    public void onPlanetCured(Planet planet) {
        // default implementation does not react to this event

    }

    @Override
    public void onMessageSeen(String id) {
        // default implementation does not react to this event

    }

    @Override
    public void onNewGame() {
        // default implementation does not react to this event

    }

    @Override
    public void onLevelChanged() {
        // default implementation does not react to this event

    }

    @Override
    public void onSpacewarStart(SpacewarWorld war) {
        // default implementation does not react to this event

    }

    @Override
    public SpacewarScriptResult onSpacewarStep(SpacewarWorld war) {
        // default implementation does not react to this event
        return null;
    }

    @Override
    public void onSpacewarFinish(SpacewarWorld war) {
        // default implementation does not react to this event
    }

    @Override
    public void onGroundwarStart(GroundwarWorld war) {
        // default implementation does not react to this event

    }

    @Override
    public void onGroundwarStep(GroundwarWorld war) {
        // default implementation does not react to this event

    }

    @Override
    public void onGroundwarFinish(GroundwarWorld war) {
        // default implementation does not react to this event
    }
    /**
     * Check if the given spacewar is a mission-related spacewar.
     * <ul>
     * <li>The given mission is active (visible and state == ACTIVE)</li>
     * <li>The attacker is the player</li>
     * <li>The target is a fleet</li>
     * <li>The target appears in the scripted fleet set</li>
     * </ul>
     * @param battle the battle settings
     * @param mission the mission
     * @return true if it is the related spacewar
     */
    protected boolean isMissionSpacewar(BattleInfo battle, String mission) {
        Objective o = objective(mission);
        Set<Integer> scripted = helper.scriptedFleets();
        if (o.isActive()

                && battle.attacker.owner == player

                && battle.targetFleet != null) {
            return scripted.contains(battle.targetFleet.id);
        }
        return false;
    }
    @Override
    public void onAutobattleFinish(BattleInfo battle) {
        // default implementation does not react to this event

    }
    @Override
    public void onAutobattleStart(BattleInfo battle) {
        // default implementation does not react to this event

    }
    /**
     * Show a message and then an objective.
     * @param messageId the message identifier
     * @param objective the objectives to display
     */
    public void incomingMessage(String messageId, final String... objective) {
        if (objective.length == 0) {
            incomingMessage(messageId, (Action0)null);
        } else {
            pendingObjectives.add(objective);
            incomingMessage(messageId, new Action0() {
                @Override
                public void invoke() {
                    pendingObjectives.remove(objective);
                    for (String o : objective) {
                        showObjective(o);
                    }
                }
            });
        }
    }
    /**
     * Add an new incoming message to the received messages.
     * @param messageId the message id
     * @return true if successful
     */
    public boolean addIncomingMessage(String messageId) {
        VideoMessage msg = world.bridge.receiveMessages.get(messageId);
        if (msg == null) {
            Exceptions.add(new AssertionError("Missing video: " + messageId));
            return false;
        }
        msg = msg.copy();
        msg.seen = false;

        world.receivedMessages.add(0, msg);
        return true;
    }
    /**
     * Show a message and then an objective.
     * @param messageId the message identifier
     * @param action the action to invoke
     */
    public void incomingMessage(String messageId, final Action0 action) {
        if (addIncomingMessage(messageId)) {
            SoundType snd = ModelUtils.random(
                    SoundType.MESSAGE,

                    SoundType.NEW_MESSAGE_1,

                    SoundType.NEW_MESSAGE_2,

                    SoundType.NEW_MESSAGE_3);
            world.env.playSound(SoundTarget.COMPUTER, snd, action);
        }
    }
    /**
     * Remove any non-existent scripted fleets.
     */
    void cleanupScriptedFleets() {
        // cleanup scripted fleets
        for (int i : U.newArrayList(helper.scriptedFleets())) {
            if (fleet(i) == null) {
                helper.scriptedFleets().remove(i);
            }
        }
    }
    /**
     * Returns the research of the given id.
     * @param id the research id
     * @return the research type
     */
    ResearchType research(String id) {
        return world.researches.get(id);
    }
    @Override
    public void onTalkCompleted() {

    }
    /**
     * Set the slot contents.
     * @param ii the inventory item
     * @param slotId the slot
     * @param technology the technology
     * @param count the item count
     */
    protected void setSlot(InventoryItem ii, String slotId, String technology, int count) {
        for (InventorySlot is : ii.slots.values()) {
            if (is.slot.id.equals(slotId) && !is.slot.fixed) {
                is.type = research(technology);
                is.hp = is.hpMax(ii.owner);
                is.count = Math.min(is.slot.max, count);
            }
        }

    }
    /**
     * Remove the mission related variables between the given range.
     * @param idxs the array of mission indexes
     */
    protected void removeMissions(final int... idxs) {
        Func1<String, Boolean> filter = new Func1<String, Boolean>() {
            @Override
            public Boolean invoke(String value) {
                for (int idx : idxs) {
                    if (value.equals("Mission-" + idx)) {
                        return true;
                    }
                    if (value.startsWith("Mission-" + idx + "-")) {
                        return true;
                    }
                }
                return false;
            }
        };
        helper.clearMissionTimes(filter);
        helper.clearTimeouts(filter);
        helper.clearObjectives(filter);
    }
    /**
     * Remove all mission related variables between the given range.
     * @param start the start index inclusive
     * @param end the end index inclusive
     */
    protected void removeMissions(final int start, final int end) {
        Func1<String, Boolean> filter = new Func1<String, Boolean>() {
            @Override
            public Boolean invoke(String value) {
                for (int i = start; i <= end; i++) {
                    if (value.equals("Mission-" + i)) {
                        return true;
                    }
                    if (value.startsWith("Mission-" + i + "-")) {
                        return true;
                    }
                }
                return false;
            }
        };
        helper.clearMissionTimes(filter);
        helper.clearTimeouts(filter);
        helper.clearObjectives(filter);
    }
    /**
     * Checks if the given mission + "-Timeout" timer is due, clears it and returns true.
     * @param missionId the mission id
     * @return trie of timeout
     */
    boolean checkTimeout(String missionId) {
        if (helper.isTimeout(missionId)) {
            helper.clearTimeout(missionId);
            return true;
        }
        return false;
    }
    /**
     * Checks if the given mission is due, clears it and returns true.
     * @param missionId the mission id
     * @return trie of timeout
     */
    boolean checkMission(String missionId) {
        String id = missionId;
        if (helper.isMissionTime(id)) {
            helper.clearMissionTime(id);
            return true;
        }
        return false;
    }
    /**
     * Update the send and receive messages based on the planet statuses.
     * @param planets the list of planet ids
     */
    protected void setPlanetMessages(String... planets) {
        boolean anyAttack = false;
        for (String p : planets) {
            Planet planet = planet(p);
            if (planet.quarantineTTL > 0) {
                helper.send(p + "-Check").visible = false;
                helper.send(p + "-Come-Quickly").visible = false;
            } else {
                anyAttack |= isUnderAttack(planet);
            }
        }
        for (String p : planets) {
            Planet planet = planet(p);
            if (planet.quarantineTTL == 0) {
                boolean thisTarget = isUnderAttack(planet);
                if (thisTarget) {
                    helper.send(p + "-Check").visible = false;
                    helper.send(p + "-Come-Quickly").visible = true;
                    helper.send(p + "-Not-Under-Attack").visible = false;
                } else
                if (anyAttack) {
                    helper.send(p + "-Check").visible = false;
                    helper.send(p + "-Come-Quickly").visible = false;
                    helper.send(p + "-Not-Under-Attack").visible = true;
                } else {
                    helper.send(p + "-Check").visible = true;
                    helper.send(p + "-Come-Quickly").visible = false;
                    helper.send(p + "-Not-Under-Attack").visible = false;
                }
            }
        }

    }
    /**
     * Check if the fleet has a concrete tag.
     * @param f the fleet
     * @param tag the expected tag
     * @return true if fleet has tag
     */
    protected boolean hasTag(Fleet f, String tag) {
        for (InventoryItem ii : f.inventory.iterable()) {
            if (tag.equals(ii.tag)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Check if the participating fleets in the spacewar has a tagget ship or not.
     * @param bi the battle info
     * @param tag the tag to look for
     * @return true if any of the fleets has a a ship with the given tag
     */
    protected boolean hasTag(BattleInfo bi, String tag) {
        boolean r = false;
        if (bi.helperFleet != null) {
            r |= hasTag(bi.helperFleet, tag);
        }
        if (bi.attacker != null) {
            r |= hasTag(bi.attacker, tag);
        }
        if (bi.targetFleet != null) {
            r |= hasTag(bi.targetFleet, tag);
        }
        return r;
    }
    /**
     * Check if any of the fleets of {@code p} is following the given fleet.
     * @param target the target fleet
     * @param p the player
     * @return the first fleet that is following the target
     */
    Fleet getFollower(Fleet target, Player p) {
        for (Fleet f2 : p.fleets.keySet()) {
            if (f2.owner == p && f2.targetFleet == target) {
                return f2;
            }
        }
        return null;
    }
    /**
     * Load the state. Implementors should always call super.load().
     * @param xmission the mission XML
     */
    public void load(XElement xmission) {
        pendingObjectives.clear();
        for (XElement xobj : xmission.childrenWithName("pending-objective")) {
            pendingObjectives.add(U.split(xobj.get("list"), ","));
        }
    }
    /**
     * Save the state. Implementors should always call super.save().
     * @param xmission the mission XML
     */
    public void save(XElement xmission) {
        for (String[] obj : pendingObjectives) {
            XElement xobj = xmission.add("pending-objective");
            xobj.set("list", U.join(obj, ","));
        }
    }
    /** @return Check if this mission is still applicable (e.g., level check). */
    public abstract boolean applicable();
    /** Reset state to default. */
    public void reset() {

    }
    /**
     * Tag all items of the given fleet.
     * @param f the fleet
     * @param tag the new tag, null to untag
     */
    public void tagFleet(Fleet f, String tag) {
        for (InventoryItem ii : f.inventory.iterable()) {
            ii.tag = tag;
        }
    }
    /**
     * Start a joint space battle with the given ally and enemy.
     * @param war the war context
     * @param allyTag the ally fleet's tag
     * @param allyPlayer the ally fleets owner
     * @param enemyTag the enemy fleets tag
     * @param enemyPlayer the enemy fleets owner
     * @return true if the player attacked one of the given tagged fleets
     */
    boolean startJointSpaceBattle(SpacewarWorld war, String allyTag, Player allyPlayer, String enemyTag, Player enemyPlayer) {
        BattleInfo battle = war.battle();
        Fleet f1 = findTaggedFleet(allyTag, allyPlayer);
        Fleet f2 = findTaggedFleet(enemyTag, enemyPlayer);

        if (battle.targetFleet != null && (battle.targetFleet == f1 || battle.targetFleet == f2)) {

            if (battle.targetFleet == f1) {
                war.includeFleet(f2, f2.owner);
                battle.targetFleet = f2;
                battle.otherFleets.add(f1);
                f2.owner.ai.spaceBattle(war).spaceBattleInit();
            } else {
                war.addStructures(f1, EnumSet.of(
                        ResearchSubCategory.SPACESHIPS_BATTLESHIPS,
                        ResearchSubCategory.SPACESHIPS_CRUISERS,
                        ResearchSubCategory.SPACESHIPS_FIGHTERS));
            }
            Dimension d = war.space();
            List<SpacewarStructure> structures = war.structures();
            int maxH = 0;
            for (SpacewarStructure s : structures) {
                if (s.item != null && allyTag.equals(s.item.tag)) {
                    maxH += s.get().getHeight();
                }
            }
            int dy = (d.height - maxH) / 2;
            for (SpacewarStructure s : structures) {
                if (s.item != null && allyTag.equals(s.item.tag)) {
                    s.x = d.width / 2d;
                    s.y = dy;
                    war.alignToNearestCell(s);
                    war.addUnitLocation(s);
                    s.angle = 0.0;
                    s.owner = f1.owner;
                    s.guard = true;
                    dy += s.get().getHeight();
                }
            }
            battle.allowRetreat = false;
            battle.attackerAllies.add(allyPlayer);
            return true;
        }
        return false;
    }
    /**
     * Start a joint autobattle with an ally and enemy fleet.
     * @param battle the battle info
     * @param allyTag the ally fleet's tag
     * @param allyPlayer the ally fleets owner
     * @param enemyTag the enemy fleets tag
     * @param enemyPlayer the enemy fleets owner
     * @return true if the player attacked one of the given tagged fleets
     */
    boolean startJointAutoSpaceBattle(BattleInfo battle, String allyTag, Player allyPlayer, String enemyTag, Player enemyPlayer) {
        Fleet f1 = findTaggedFleet(allyTag, allyPlayer);
        Fleet f2 = findTaggedFleet(enemyTag, enemyPlayer);
        if (battle.targetFleet != null && (battle.targetFleet == f1 || battle.targetFleet == f2)) {
            if (battle.targetFleet == f1) {
                battle.targetFleet = f2;
                battle.otherFleets.add(f1);
            }
            battle.attacker.inventory.addAll(f1.inventory.iterable());
            return true;
        }
        return false;
    }
    /**
     * Remove the ally from the attacker's inventory.
     * @param battle the battle info
     * @param allyTag the ally tag
     * @return true if the given ally still exists
     */
    boolean finishJointAutoSpaceBattle(BattleInfo battle, String allyTag) {
        boolean result = false;
        for (InventoryItem ii : battle.attacker.inventory.list()) {
            if (allyTag.equals(ii.tag)) {
                battle.attacker.inventory.remove(ii);
                result = true;
            }
        }
        return result;
    }
    /**
     * Convenience method to add a timeout value.
     * @param name the timeout value
     * @param value the value in milliseconds
     */
    void addTimeout(String name, int value) {
        helper.setTimeout(name, value);
    }
    /**
     * Convenience method to add a mission time relative to now.
     * @param name the mission name
     * @param hoursRelative the hours relative to now
     */
    void addMission(String name, int hoursRelative) {
        helper.setMissionTime(name, helper.now() + hoursRelative);
    }
    /**
     * Add the given fleet as a scripted fleet.
     * @param f the fleet
     */
    void addScripted(Fleet f) {
        helper.scriptedFleets().add(f.id);
    }
    /**
     * Remove the given scripted fleet.
     * @param f the fleet
     */
    void removeScripted(Fleet f) {
        helper.scriptedFleets().remove(f.id);
    }
    @Override
    public void onDeploySatellite(Planet target, Player player,
            ResearchType satellite) {
        // default implementation does not react to this event
    }
    @Override
    public boolean fleetBlink(Fleet f) {
        return false;
    }
    @Override
    public void onFleetsMoved() {

    }
    /**
     * Returns a send-out message with the given id.
     * @param messageId the message id
     * @return the message
     */
    public VideoMessage send(String messageId) {
        VideoMessage msg = helper.send(messageId);
        if (msg == null) {
            Exceptions.add(new AssertionError("Missing send message: " + messageId));
        }
        return msg;
    }
    /**
     * Returns an objective.
     * @param id the objective id.
     * @return the objective
     */
    public Objective objective(String id) {
        Objective o = helper.objective(id);
        if (o == null) {
            Exceptions.add(new AssertionError("Missing objective: " + id));
        }
        return o;
    }
    /**
     * Sets the objective state.
     * @param id the identifier
     * @param state the new state
     * @return true if the state actually changed
     */
    public boolean setObjectiveState(String id, ObjectiveState state) {
        return helper.setObjectiveState(id, state);
    }
    /**
     * Sets the objective state.
     * @param o the objective
     * @param state the new state
     * @return true if the state actually changed
     */
    public boolean setObjectiveState(Objective o, ObjectiveState state) {
        return helper.setObjectiveState(o, state);
    }
    /**
     * Enter into game over state.
     */
    public void gameover() {
        helper.gameover();
    }
    /**
     * Display the given objective if not visible.
     * @param id the identifier
     * @return true if the objective was not visible
     */
    public boolean showObjective(String id) {
        return helper.showObjective(id);
    }
    /**
     * Display the given objective if not visible.
     * @param o the objective
     * @return true if the objective was not visible
     */
    public boolean showObjective(Objective o) {
        return helper.showObjective(o);
    }
    /**
     * Check if a timeout has been set.
     * @param id the timeout id

     * @return true if set
     */
    public boolean hasTimeout(String id) {
        return helper.hasTimeout(id);
    }
    /**
     * Check if a mission timer is already set.
     * @param id the timer id
     * @return true if set
     */
    public boolean hasMission(String id) {
        return helper.hasMissionTime(id);
    }
    /**
     * Remove a mission timer if exists.
     * @param id the identifier
     */
    public void clearMission(String id) {
        helper.clearMissionTime(id);
    }
    /**
     * Remove a real timeout if exists.
     * @param id the identifier
     */
    public void clearTimeout(String id) {
        helper.clearTimeout(id);
    }
    /**
     * @return the hours passed since the game base date
     */
    public int now() {
        return helper.now();
    }
    /**
     * Returns the view limit of the target player at the level.
     * @param player the player
     * @param level the level
     * @return the limits or null if not present
     */
    public ViewLimit getViewLimit(Player player, int level) {
        return helper.getViewLimit(player, level);
    }
    @Override
    public void onSpaceChat(SpacewarWorld world, Chat chat, Node node) {

    }
    @Override
    public void onRecordMessage() {

    }
    /**

     * Check if the receive message exists.
     * @param id the message to test
     * @return true if exists
     */
    public boolean hasReceive(String id) {
        return world.bridge.receiveMessages.containsKey(id);
    }
    /**
     * Max out the equipment counts on the given inventory items.
     * @param sequence the sequence
     */
    public void equipFully(Iterable<? extends InventoryItem> sequence) {
        for (InventoryItem ii : sequence) {
            for (InventorySlot is : ii.slots.values()) {
                if (!is.slot.fixed) {
                    is.count = is.slot.max;
                    if (is.type == null) {
                        is.type = is.slot.items.get(0);
                    }
                }
            }
        }
    }
    @Override
    public void onLoaded() {
        // show queued objectives
        while (!pendingObjectives.isEmpty()) {
            String[] objs = pendingObjectives.removeFirst();
            for (String s : objs) {
                showObjective(s);
            }
        }
    }
    /**
     * Adds the given number of technology to the inventory
     * of the fleet, bypassing standard deployment constraints.
     * @param f the target fleet
     * @param type the technology id
     * @param count the number of items
     * @return the list of created inventory items
     */
    public List<InventoryItem> addInventory(Fleet f, String type, int count) {
        ResearchType rt = research(type);
        if (f.canUndeploy(rt)) {
            InventoryItem ii = new InventoryItem(world.newId(), f.owner, rt);
            ii.init();
            ii.count = count;
            f.inventory().add(ii);
            return Collections.singletonList(ii);
        }
        List<InventoryItem> r = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            InventoryItem ii = new InventoryItem(world.newId(), f.owner, rt);
            ii.init();
            ii.count = 1;
            r.add(ii);
            f.inventory().add(ii);
        }
        return r;
    }
    /**
     * Check if the fleet still has ground attack capability.
     * @param f the target fleet
     * @return true if can attack ground
     */
    boolean canGroundAttack(Fleet f) {
        for (InventoryItem ii : f.inventory.iterable()) {
            if (ii.type.category == ResearchSubCategory.WEAPONS_TANKS

                    || ii.type.category == ResearchSubCategory.WEAPONS_VEHICLES) {
                return true;
            }
        }
        return false;
    }
}
