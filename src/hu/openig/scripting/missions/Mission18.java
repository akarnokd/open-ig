/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Action0;
import hu.openig.model.Fleet;
import hu.openig.model.FleetTask;
import hu.openig.model.InventoryItem;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchType;
import hu.openig.model.TraitKind;

/**
 * Mission 18: Conquer the Garthogs.
 * <p>Also: initialize level 3.</p>
 * @author akarnokd, Feb 13, 2012
 */
public class Mission18 extends Mission {

    @Override
    public boolean applicable() {
        return world.level == 3;
    }
    @Override
    public void onLevelChanged() {
        if (world.level != 3) {
            return;
        }
        removeMissions(1, 25);

        player.setAvailable(research("Battleship1"));
        player.setAvailable(research("Cruiser1"));
        player.setAvailable(research("SpaceStation1"));

        createMainShip();

        // achievement
        world.achievement("achievement.commander");

        addMission("Mission-18", 1);

        send("Douglas-Reinforcements-Denied").visible = false;

        player.populateProductionHistory();
    }
    /**
     * Creates the main ship for level 3.
     */
    void createMainShip() {
        Fleet own = findTaggedFleet("CampaignMainShip3", player);
        if (own != null) {
            return;
        }
        own = findTaggedFleet("CampaignMainShip2", player);
        if (own == null) {
            own = findTaggedFleet("CampaignMainShip1", player);
        }
        Fleet f;
        if (own != null) {
            f = own;
        } else {
            Planet ach = planet("Achilles");
            f = createFleet(label("Empire.main_fleet"), player, ach.x + 5, ach.y + 5);
        }

        ResearchType rt = research("Battleship1");
        addInventory(f, rt.id, 1);
        addInventory(f, "LightTank", 4);

        InventoryItem ii = f.getInventoryItem(rt);
        ii.tag = "CampaignMainShip3";

        // loadout
        setSlot(ii, "laser", "Laser1", 14);
        setSlot(ii, "bomb", "Bomb1", 6);
        setSlot(ii, "rocket", "Rocket1", 4);
        setSlot(ii, "radar", "Radar1", 1);
        setSlot(ii, "cannon", "IonCannon", 6);
        setSlot(ii, "shield", "Shield1", 1);
        if (!player.traits.has(TraitKind.PRE_WARP)) {
            setSlot(ii, "hyperdrive", "HyperDrive1", 1);
        }

    }
    @Override
    public void onTime() {
        checkMainShip();
        checkSuccess();
        if (checkTimeout("Mission-18-Failed")) {
            gameover();
            loseGameMessageAndMovie("Douglas-Fire-Lost-Planet-2", "lose/fired_level_3");
        }
        if (checkTimeout("Mission-18-Hide")) {
            objective("Mission-18").visible = false;
        }
        if (checkMission("Mission-18")) {
            for (int i = 1; i < 6; i++) {
                objective("Mission-18-Task-" + i).visible = true;
            }
            showObjective("Mission-18");
        }
        // planet messages
        String[] planets = { "Achilles", "Naxos", "San Sterling", "New Caroline", "Centronom", "Zeuson" };
        setPlanetMessages(planets);

        if (checkMission("Mission-18-Promote")) {
            world.env.stopMusic();
            world.env.playVideo("interlude/level_4_intro", new Action0() {
                @Override
                public void invoke() {
                    promote();
                }
            });
        }
    }
    @Override
    public void onPlanetInfected(Planet planet) {
        if (planet.owner == player) {
            String msgId = planet.id + "-Virus";
            if (hasReceive(msgId)) {
                incomingMessage(msgId, (Action0)null);
            }
        }
    }
    /** Check if the main ship is still operational. */
    void checkMainShip() {
        Fleet ft = findTaggedFleet("CampaignMainShip3", player);
        if (ft == null) {
            if (!hasTimeout("MainShip-Lost")) {
                addTimeout("MainShip-Lost", 3000);
            }
            if (checkTimeout("MainShip-Lost")) {
                gameover();
                loseGameMovie("lose/destroyed_level_3");
            }
        }
    }
    /**
     * Promotion action.
     */
    void promote() {
        world.level = 4;
        world.env.playMusic();
    }
    /** Check if we own all the necessary planets. */
    void checkSuccess() {
        Player g = player("Garthog");
        if (g.statistics.planetsOwned.value == 0 && !hasMission("Mission-18-Promote")) {
            setObjectiveState("Mission-18", ObjectiveState.SUCCESS);
            addTimeout("Mission-18-Hide", 13000);
            addMission("Mission-18-Promote", 3);
        }
    }
    @Override
    public void onConquered(Planet planet, Player previousOwner) {
        if (planet.owner == player || previousOwner == player) {
            boolean win = previousOwner.id.equals("Garthog");
            switch (planet.id) {
                case "Garthog 1":
                    setObjectiveState("Mission-18-Task-1", win ? ObjectiveState.SUCCESS : ObjectiveState.ACTIVE);
                    break;
                case "Garthog 2":
                    setObjectiveState("Mission-18-Task-2", win ? ObjectiveState.SUCCESS : ObjectiveState.ACTIVE);
                    break;
                case "Garthog 3":
                    setObjectiveState("Mission-18-Task-3", win ? ObjectiveState.SUCCESS : ObjectiveState.ACTIVE);
                    break;
                case "Garthog 4":
                    setObjectiveState("Mission-18-Task-4", win ? ObjectiveState.SUCCESS : ObjectiveState.ACTIVE);
                    break;
                case "Garthog 5":
                    setObjectiveState("Mission-18-Task-5", win ? ObjectiveState.SUCCESS : ObjectiveState.ACTIVE);
                    break;
                default:
                    if (player.ownPlanets().isEmpty() && player.ownFleets().isEmpty()) {
                        setObjectiveState("Mission-18", ObjectiveState.FAILURE);
                        addTimeout("Mission-18-Failed", 13000);
                    }
                    break;
            }
        }
    }
    @Override
    public void onLost(Planet planet) {
        if (planet.owner == player || planet.owner == null) {
            if (planet.id.startsWith("Garthog")) {
                Planet ach = planet("Achilles");
                // create a fleet to colonize the planet, for convenience
                Fleet f = createFleet(format("colonizer_fleet", player.shortName), player, ach.x, player.explorationOuterLimit.y - 5);
                addInventory(f, "ColonyShip", 1);
                f.moveTo(planet);
                f.task = FleetTask.SCRIPT;
                tagFleet(f, "Mission-18-Colonizer");
                addScripted(f);
            } else {
                if (player.ownPlanets().isEmpty() && player.ownFleets().isEmpty()) {
                    setObjectiveState("Mission-18", ObjectiveState.FAILURE);
                    addTimeout("Mission-18-Failed", 13000);
                }
            }
        }

    }
    @Override
    public void onFleetAt(Fleet fleet, Planet planet) {
        if (fleet.owner == player && planet.owner == null

                && hasTag(fleet, "Mission-18-Colonizer")) {
            removeScripted(fleet);
            fleet.colonize(planet);
        }
    }
}
