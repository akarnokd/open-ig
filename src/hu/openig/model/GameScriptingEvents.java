/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.model.Chats.Chat;
import hu.openig.model.Chats.Node;

/**
 * Interface for the common game scripting events.
 * @author akarnokd, 2012.01.14.
 */
public interface GameScriptingEvents {
    /**
     * Notification about research complete.
     * @param player the player
     * @param rt the research
     */
    void onResearched(Player player, ResearchType rt);
    /**
     * Notification about production complete.
     * @param player the player
     * @param rt the research type
     */
    void onProduced(Player player, ResearchType rt);
    /**
     * Notification about the destruction of a fleet.
     * @param winner the winner fleet
     * @param loser the loser fleet
     */
    void onDestroyed(Fleet winner, Fleet loser);
    /**
     * Notification about a planet colonized.
     * @param planet the planet
     */
    void onColonized(Planet planet);
    /**
     * Notification about a planet conquered.
     * @param planet the planet
     * @param previousOwner the previous owner of the planet
     */
    void onConquered(Planet planet, Player previousOwner);
    /**
     * Notification about a player beaten.
     * @param player the player
     */
    void onPlayerBeaten(Player player);
    /**
     * Notification about a player discovering a planet.
     * @param player the player
     * @param planet the planet
     */
    void onDiscovered(Player player, Planet planet);
    /**
     * Notification about a player discovering another player.
     * @param player the player
     * @param other the discovered player
     */
    void onDiscovered(Player player, Player other);
    /**
     * Notification about a player discovering another player's fleet.
     * @param player the player
     * @param fleet the fleet
     */
    void onDiscovered(Player player, Fleet fleet);
    /**
     * Notification about a player losing sight of a fleet.
     * @param player the player
     * @param fleet another player's fleet
     */
    void onLostSight(Player player, Fleet fleet);
    /**
     * Notification about a fleet arriving at a specific location.
     * @param fleet the fleet
     * @param x the X coordinate
     * @param y the Y coordinate
     */
    void onFleetAt(Fleet fleet, double x, double y);
    /**
     * Notification about a fleet catching up to another fleet.
     * @param fleet the fleet
     * @param other the other fleet
     */
    void onFleetAt(Fleet fleet, Fleet other);
    /**
     * Notification about a fleet reaching a planet.
     * @param fleet the fleet
     * @param planet the planet
     */
    void onFleetAt(Fleet fleet, Planet planet);
    /**
     * Notification about a stance change.

     * @param first the player experiencing the stance change
     * @param second the other player
     */
    void onStance(Player first, Player second);
    /**
     * Notification about an alliance between two players against another.
     * @param first the first player
     * @param second the second playeyr
     * @param commonEnemy the common enemy
     */
    void onAllyAgainst(Player first, Player second, Player commonEnemy);
    /**
     * Notification about a battle completion.
     * @param player the player
     * @param battle the battle containing an outcome
     */
    void onBattleComplete(Player player, BattleInfo battle);
    /**
     * Notification about a time progress.
     */
    void onTime();
    /**
     * Notification about a building being completed.
     * @param planet the planet
     * @param building the building
     */
    void onBuildingComplete(Planet planet, Building building);
    /**
     * Notification about a building fully repaired.
     * @param planet the planet
     * @param building the building
     */
    void onRepairComplete(Planet planet, Building building);
    /**
     * Notification about upgrading a building.
     * <p>Called before the building upgrade is actually started simulation, e.g., the building
     * parameter represents the pre-upgrade state of the building.</p>
     * @param planet the planet
     * @param building the building
     * @param newLevel the new level
     */
    void onUpgrading(Planet planet, Building building, int newLevel);
    /**
     * Notification about an inventory item added to a planet.
     * @param planet the planet
     * @param item the inventory item
     */
    void onInventoryAdd(Planet planet, InventoryItem item);
    /**
     * Notification about an inventory item added to a planet.
     * @param planet the planet
     * @param item the item
     */
    void onInventoryRemove(Planet planet, InventoryItem item);
    /**
     * Notification about a planet lost.
     * <p>Called just before the planet dies.</p>
     * @param planet the planet
     */
    void onLost(Planet planet);
    /**
     * Notification about a fleet lost.
     * @param fleet the fleet
     */
    void onLost(Fleet fleet);
    /**

     * Notification about a full screen video playback completed.
     * @param video the video that has completed

     */
    void onVideoComplete(String video);
    /**
     * Notification about a sound playback completed.
     * @param audio the audio
     */
    void onSoundComplete(String audio);
    /**
     * Notification about a planet infection.
     * @param planet the planet
     */
    void onPlanetInfected(Planet planet);
    /**
     * Notification about a planet cured.
     * @param planet the planet
     */
    void onPlanetCured(Planet planet);
    /**
     * Notification about the user watched a message.
     * @param id the message id
     */
    void onMessageSeen(String id);
    /**
     * Notification about a new game start.
     */
    void onNewGame();
    /** Notify the scripting that a level jump occurred. */
    void onLevelChanged();
    /**
     * Called once the structures are laid out.
     * @param war the battle context
     */
    void onSpacewarStart(SpacewarWorld war);
    /**
     * Called after each step in the spacewar.
     * @param war the battle context
     * @return the indication what to do
     */
    SpacewarScriptResult onSpacewarStep(SpacewarWorld war);
    /**
     * Called once the battle is concluded but before going to the next phase.
     * @param war the battle context
     */
    void onSpacewarFinish(SpacewarWorld war);
    /**
     * Called once the player placed its own units and the AI did its own placements.
     * @param war the battle context
     */
    void onGroundwarStart(GroundwarWorld war);
    /**
     * Called after each battle step.
     * @param war the battle context
     */
    void onGroundwarStep(GroundwarWorld war);
    /**
     * Called once the battle is concluded but before going to the next phase.
     * @param war the battle context
     */
    void onGroundwarFinish(GroundwarWorld war);
    /**
     * Notification about the auto-battle to be executed.
     * @param battle the battle settings
     */
    void onAutobattleStart(BattleInfo battle);
    /**
     * Notification about the auto-battle completion.
     * @param battle the battle settings
     */
    void onAutobattleFinish(BattleInfo battle);
    /**
     * Notification about a talk completed.
     */
    void onTalkCompleted();
    /**
     * Notification about a satellite being deployed.
     * @param target the target planet
     * @param player the player who is deploying
     * @param satellite the satellite type
     */
    void onDeploySatellite(Planet target, Player player, ResearchType satellite);
    /**
     * Returns true if the given fleet should be drawn as blinking on the starmap screen.
     * @param f the fleet
     * @return true if blinking required
     */
    boolean fleetBlink(Fleet f);
    /** Called when fleets moved. */
    void onFleetsMoved();
    /**
     * Called when the user selects a particular chat node.
     * @param world the spacewar world
     * @param chat the current chat
     * @param node the selected node
     */
    void onSpaceChat(SpacewarWorld world, Chat chat, Node node);
    /** Called when the user clicks on the record message button. */
    void onRecordMessage();
    /** Notification when the game was just loaded. Should execute any timed and pending operations. */
    void onLoaded();
}
