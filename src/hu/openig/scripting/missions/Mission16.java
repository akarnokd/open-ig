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
import hu.openig.model.FleetKnowledge;
import hu.openig.model.FleetTask;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.SoundTarget;
import hu.openig.model.SoundType;
import hu.openig.model.SpacewarWorld;
import hu.openig.utils.XElement;

/**
 * Mission 16: Money carrier.
 * @author akarnokd, 2012.02.10.
 */
public class Mission16 extends Mission {
    /** The mission state enum. */
    public enum M16 {
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
    M16 stage = M16.NONE;
    /** Reinforcements called. */
    boolean reinforcements;
    @Override
    public boolean applicable() {
        return world.level == 2;
    }
    @Override
    public void reset() {
        stage = M16.NONE;
    }
    @Override
    public void load(XElement xmission) {
        super.load(xmission);
        stage = M16.valueOf(xmission.get("stage"));
        reinforcements = xmission.getBoolean("reinforcements");
    }
    @Override
    public void save(XElement xmission) {
        super.save(xmission);
        xmission.set("stage", stage);
        xmission.set("reinforcements", reinforcements);
    }
    /** The ally tag. */
    protected static final String ALLY = "Mission-16-Carrier";
    /** The enemy tag. */
    protected static final String ENEMY = "Mission-16-Garthog";
    @Override
    public void onTime() {
        Objective m15 = objective("Mission-15");
        final Objective m16 = objective("Mission-16");
        if (m15.state != ObjectiveState.ACTIVE
                && stage == M16.NONE) {
            stage = M16.WAIT;
            addMission("Mission-16", 4 * 24);
        }
        if (checkMission("Mission-16")) {
            stage = M16.INTRO;
            world.env.stopMusic();
            world.env.playVideo("interlude/colony_ship_arrival_2", new Action0() {
                @Override
                public void invoke() {
                    world.env.playMusic();
                    incomingMessage("Douglas-Money", "Mission-16");
                    createCarrier();
                    stage = M16.RUN;
                    send("Douglas-Reinforcements-Approved").visible = true;
                    send("Douglas-Reinforcements-Denied").visible = false;
                    send("Douglas-Reinforcements-Denied-2").visible = false;
                }
            });
        }
        if (checkMission("Mission-16-Timeout")) {
            removeFleets();
            setObjectiveState(m16, ObjectiveState.FAILURE);

            addTimeout("Mission-16-Fire", 13000);
            addTimeout("Mission-16-Hide", 13000);
        }
        if (checkTimeout("Mission-16-Hide")) {
            stage = M16.DONE;
            m16.visible = false;

            send("Douglas-Reinforcements-Approved").visible = false;
            send("Douglas-Reinforcements-Denied").visible = true;
            send("Douglas-Reinforcements-Denied-2").visible = false;
        }
        if (checkTimeout("Mission-16-Fire")) {
            gameover();
            loseGameMessageAndMovie("Douglas-Fire-Escort-Failed", "lose/fired_level_2");
        }
    }
    /** @return the garthog player. */
    Player garthog() {
        return player("Garthog");
    }
    /**
     * Remove the mission fleets.
     */
    void removeFleets() {
        Fleet benson = findTaggedFleet(ALLY, player);
        Player g = garthog();
        Fleet garthog = findTaggedFleet(ENEMY, g);
        if (benson != null) {
            world.removeFleet(benson);
            removeScripted(benson);
        }
        if (garthog != null) {
            world.removeFleet(garthog);
            removeScripted(garthog);
        }
    }
    @Override
    public void onMessageSeen(String id) {
        if ("Douglas-Reinforcements-Approved".equals(id)

                && !reinforcements && stage == M16.RUN) {
            reinforcements = true;
            addReinforcements();
        }
    }
    /** Create the carrier. */
    void createCarrier() {
        Fleet f = createFleet(label("mission-16.fleet"), player, player.explorationOuterLimit.x, player.explorationOuterLimit.y);

        addInventory(f, "TradersFreight1", 1);

        tagFleet(f, ALLY);

        Planet sst = planet("San Sterling");
        f.moveTo(sst);
        f.task = FleetTask.SCRIPT;

        addScripted(f);
    }
    /**
     * Add reinforcements to your fleet.
     */
    void addReinforcements() {
        Fleet f = findTaggedFleet("CampaignMainShip2", player);
        if (f != null) {
            // --------------------------------------------

            int f1 = 8;
            int f2 = 4;

            if (f.inventoryCount(research("Fighter1")) > world.params().fighterLimit() - f1
                    || f.inventoryCount(research("Fighter2")) > world.params().fighterLimit() - f2) {

                f = createFleet(label("Empire.main_fleet"), player, f.x + 5, f.y + 5);
            }

            addInventory(f, "Fighter1", f1);
            addInventory(f, "Fighter2", f2);

            // --------------------------------------------

            world.env.playSound(SoundTarget.COMPUTER, SoundType.REINFORCEMENT_ARRIVED_2, null);
        }
    }
    @Override
    public void onFleetAt(Fleet fleet, Planet planet) {
        if (hasTag(fleet, ALLY) && planet.id.equals("San Sterling")) {
            garthogAttack();
        }
    }

    @Override
    public void onSpacewarFinish(SpacewarWorld war) {
        BattleInfo battle = war.battle();
        if (isMissionSpacewar(battle, "Mission-16")) {
            if (concludeBattle(battle)) {
                battle.rewardImage = "battlefinish/mission_20";
                battle.messageText = label("battlefinish.mission-16.20");
            }
        }
    }
    @Override
    public void onAutobattleFinish(BattleInfo battle) {
        if (isMissionSpacewar(battle, "Mission-16")) {
            finishJointAutoSpaceBattle(battle, ALLY);
            concludeBattle(battle);
        }
    }
    /**
     * Conclude the space battle.
     * @param battle the battle parameters
     * @return success
     */
    boolean concludeBattle(BattleInfo battle) {
        boolean result = false;
        clearMission("Mission-16-Timeout");
        Fleet carrier = findTaggedFleet(ALLY, player);

        if (carrier != null) {
            setObjectiveState("Mission-16", ObjectiveState.SUCCESS);
            addTimeout("Mission-16-Hide", 13000);
            result = true;
        } else {
            setObjectiveState("Mission-16", ObjectiveState.FAILURE);
            addTimeout("Mission-16-Fire", 13000);
        }
        removeFleets();
        cleanupScriptedFleets();
        return result;
    }
    @Override
    public void onSpacewarStart(SpacewarWorld war) {
        if (isMissionSpacewar(war.battle(), "Mission-16")) {
            Player g = garthog();
            startJointSpaceBattle(war, ALLY, player, ENEMY, g);
            war.battle().chat = "chat.mission-16.escort.money.carrier";
        }
    }
    @Override
    public void onAutobattleStart(BattleInfo battle) {
        if (isMissionSpacewar(battle, "Mission-16")) {
            Player g = garthog();
            startJointAutoSpaceBattle(battle, ALLY, player, ENEMY, g);
        }
    }
    /**
     * Create the garthog fleet.
     */
    void createGarthog() {
        Planet g1 = planet("San Sterling");
        Player g = garthog();
        Fleet f = createFleet(format("fleet", g.shortName), g, g1.x - 3, g1.y - 3);

        //---------------------------------

        addInventory(f, "GarthogFighter", 4);
        equipFully(addInventory(f, "GarthogDestroyer", 2));

        //---------------------------------

        tagFleet(f, ENEMY);

        f.task = FleetTask.SCRIPT;
        addScripted(f);
    }
    /** Create the garthog attack. */
    void garthogAttack() {
        createGarthog();

        world.env.speed1();

        addMission("Mission-16-Timeout", 12);

        Fleet carrier = findTaggedFleet(ALLY, player);
        Fleet ff = getFollower(carrier, player);
        if (ff != null) {
            Fleet garthog = findTaggedFleet(ENEMY, garthog());
            ff.attack(garthog);
            ff.owner.fleets.put(garthog, FleetKnowledge.VISIBLE);
        } else {
            world.env.playSound(SoundTarget.COMPUTER, SoundType.CARRIER_UNDER_ATTACK, null);
        }

    }
}
