/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Action0;
import hu.openig.model.BattleInfo;
import hu.openig.model.Fleet;
import hu.openig.model.FleetTask;
import hu.openig.model.InventoryItem;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchType;
import hu.openig.model.SoundTarget;
import hu.openig.model.SoundType;
import hu.openig.model.SpacewarWorld;
import hu.openig.utils.XElement;

/**
 * Mission 13: Escort Admiral Benson.
 * @author akarnokd, 2012.02.10.
 */
public class Mission13 extends Mission {
    /** The mission state enum. */
    public enum M13 {
        /** Not started yet. */
        NONE,
        /** Wait to start. */
        WAIT,
        /** Intro phase. */
        INTRO,
        /** Running. */
        RUN,
        /** Done. */
        DONE
    }
    /** The mission stage. */
    M13 stage = M13.NONE;
    /** Indicate that Benson was reached. */
    boolean bensonReached;
    @Override
    public boolean applicable() {
        return world.level == 2;
    }

    @Override
    public void onTime() {
        Objective m14 = objective("Mission-14");
        if (m14.state == ObjectiveState.SUCCESS
                && stage == M13.NONE) {
            stage = M13.WAIT;
            addMission("Mission-13", 4 * 24 + 12);
            addMission("Mission-8-Visions-2", 2 * 24);
        }
        if (checkMission("Mission-13")) {
            stage = M13.INTRO;
            world.env.stopMusic();
            world.env.playVideo("interlude/flagship_arrival", new Action0() {
                @Override
                public void invoke() {
                    stage = M13.RUN;
                    incomingMessage("Douglas-Admiral-Benson", "Mission-13");

                    createBenson();
                    addMission("Mission-13-Attack", 2);

                    world.env.playMusic();
                }
            });
        }
        if (checkMission("Mission-13-Attack")) {
            createGarthog();
        }
        if (checkMission("Mission-13-Timeout")) {
            removeFleets();
            setObjectiveState("Mission-13", ObjectiveState.FAILURE);

            addTimeout("Mission-13-Fire", 13000);
            addTimeout("Mission-13-Hide", 13000);
        }
        if (checkTimeout("Mission-13-Hide")) {
            stage = M13.DONE;
            objective("Mission-13").visible = false;
        }
        if (checkTimeout("Mission-13-Fire")) {
            gameover();
            loseGameMessageAndMovie("Douglas-Admiral-Benson-Failed", "lose/fired_level_2");
        }
    }
    @Override
    public void onFleetsMoved() {
        if (stage == M13.RUN) {
            checkFollowBenson();
        }
    }
    /**
     * Remove the mission fleets.
     */
    void removeFleets() {
        Fleet benson = findTaggedFleet("Mission-13-Benson", player);
        Player g = garthog();
        Fleet garthog = findTaggedFleet("Mission-13-Garthog", g);
        if (benson != null) {
            world.removeFleet(benson);
            removeScripted(benson);
        }
        if (garthog != null) {
            world.removeFleet(garthog);
            removeScripted(garthog);
        }
    }
    /**
     * Create benson's fleet.
     */
    void createBenson() {
        Planet a = planet("Achilles");
        Fleet f = createFleet(label("mission-13.benson_fleet"), player, a.x, a.y);

        //---------------------------------
        ResearchType bs1 = research("Battleship1");
        addInventory(f, bs1.id, 1);

        InventoryItem ii = f.getInventoryItem(bs1);

        setSlot(ii, "laser", "Laser1", 14);
        setSlot(ii, "rocket", "Rocket1", 3);
        setSlot(ii, "cannon", "IonCannon", 3);
        setSlot(ii, "shield", "Shield1", 1);

        //---------------------------------
        tagFleet(f, "Mission-13-Benson");

        Planet nc = planet("New Caroline");
        f.moveTo(nc);
        f.task = FleetTask.SCRIPT;
        addScripted(f);
    }
    /**
     * Create the garthog fleet.
     */
    void createGarthog() {
        Planet g1 = planet("Garthog 1");
        Player g = garthog();
        Fleet f = createFleet(format("fleet", g.shortName), g, g1.x, g1.y);

        //---------------------------------

        addInventory(f, "GarthogFighter", 3);
        equipFully(addInventory(f, "GarthogDestroyer", 2));
        equipFully(addInventory(f, "GarthogBattleship", 1));

        //---------------------------------

        tagFleet(f, "Mission-13-Garthog");

        f.task = FleetTask.SCRIPT;
        addScripted(f);
    }
    /**
     * Reach benson.
     */
    void checkFollowBenson() {
        Player g = garthog();
        Fleet benson = findTaggedFleet("Mission-13-Benson", player);
        Fleet garthog = findTaggedFleet("Mission-13-Garthog", g);

        if (benson != null && garthog != null && !bensonReached) {
            double d = Math.hypot(benson.x - garthog.x,

                    benson.y - garthog.y);
            if (d <= 10) {
                benson.stop();
                benson.task = FleetTask.SCRIPT;
            }
            if (d <= 5) {
                addMission("Mission-13-Timeout", 12);

                benson.stop();
                benson.task = FleetTask.SCRIPT;
                garthog.stop();
                garthog.task = FleetTask.SCRIPT;

                garthog.x = benson.x + 4;
                garthog.y = benson.y + 1;

                Fleet ff = getFollower(benson, player);
                if (ff != null) {
                    ff.attack(garthog);
                }
                bensonReached = true;
            } else {
                garthog.moveTo(benson.x, benson.y);
                garthog.task = FleetTask.SCRIPT;
            }
        }
    }
    /**
     * Conclude the space battle.
     * @param battle the battle parameters
     * @return success
     */
    boolean concludeBattle(BattleInfo battle) {
        boolean result = false;
        // intercept before stop
        if (hasMission("Mission-13-Timeout")) {
            clearMission("Mission-13-Timeout");
        }
        Player g = garthog();
        Fleet benson = findTaggedFleet("Mission-13-Benson", player);
        Fleet garthog = findTaggedFleet("Mission-13-Garthog", g);

        if (benson != null) {
            Planet nc = planet("New Caroline");
            benson.moveTo(nc);
            benson.task = FleetTask.SCRIPT;
            result = true;
        } else {
            setObjectiveState("Mission-13", ObjectiveState.FAILURE);
            addTimeout("Mission-13-Fire", 13000);
        }
        // remove garthog anyway
        if (garthog != null) {
            world.removeFleet(garthog);
        }
        cleanupScriptedFleets();

        return result;
    }
    @Override
    public void onDiscovered(Player player, Fleet fleet) {
        if (stage == M13.RUN && player == this.player) {
            if (hasTag(fleet, "Mission-13-Garthog")) {
                if (world.env.config().slowOnEnemyAttack) {
                    world.env.speed1();
                }
                world.env.playSound(SoundTarget.COMPUTER, SoundType.ENEMY_FLEET_DETECTED, null);
            }
        }
    }
    @Override
    public void onFleetAt(Fleet fleet, Planet planet) {
        if (planet.id.equals("New Caroline") && hasTag(fleet, "Mission-13-Benson")) {
            setObjectiveState("Mission-13", ObjectiveState.SUCCESS);
            removeFleets();
            addTimeout("Mission-13-Hide", 13000);
        }
    }
    @Override
    public void onSpacewarFinish(SpacewarWorld war) {
        BattleInfo battle = war.battle();
        if (isMissionSpacewar(battle, "Mission-13")) {
            if (concludeBattle(battle)) {
                battle.rewardImage = "battlefinish/mission_16";
                battle.messageText = label("battlefinish.mission-13.16");
                battle.rewardText = label("battlefinish.mission-13.16_bonus");
                createReward();
            }
        }
    }
    @Override
    public void onAutobattleFinish(BattleInfo battle) {
        if (isMissionSpacewar(battle, "Mission-13")) {
            finishJointAutoSpaceBattle(battle, "Mission-13-Benson");
            if (concludeBattle(battle)) {
                createReward();
            }
        }
    }
    /** Create mission reward. */
    void createReward() {
        player.changeInventoryCount(research("Laser1"), 16);
    }
    @Override
    public void onSpacewarStart(SpacewarWorld war) {
        if (isMissionSpacewar(war.battle(), "Mission-13")) {
            Player g = garthog();
            startJointSpaceBattle(war, "Mission-13-Benson", player, "Mission-13-Garthog", g);
            war.battle().chat = "chat.mission-13.escort.Benson";
        }
    }
    @Override
    public void onAutobattleStart(BattleInfo battle) {
        if (isMissionSpacewar(battle, "Mission-13")) {
            Player g = garthog();
            startJointAutoSpaceBattle(battle, "Mission-13-Benson", player, "Mission-13-Garthog", g);
        }
    }
    /** @return the garthog player. */
    Player garthog() {
        return player("Garthog");
    }
    @Override
    public void reset() {
        stage = M13.NONE;
        bensonReached = false;
    }
    @Override
    public void load(XElement xmission) {
        super.load(xmission);
        stage = M13.valueOf(xmission.get("stage"));
        bensonReached = xmission.getBoolean("benson-reached", false);
    }
    @Override
    public void save(XElement xmission) {
        super.save(xmission);
        xmission.set("stage", stage);
        xmission.set("benson-reached", bensonReached);
    }
}
