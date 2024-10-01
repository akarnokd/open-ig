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
import hu.openig.model.InventoryItem;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.SoundTarget;
import hu.openig.model.SoundType;
import hu.openig.model.SpacewarWorld;
import hu.openig.utils.XElement;

/**
 * Mission 3: Escort carrier.
 * @author akarnokd, 2012.01.14.
 */
public class Mission3 extends Mission {
    /** The stages. */
    enum M3 {
        /** Not yet started. */
        NONE,
        /** Wait for startup. */
        WAIT,
        /** The mission is running. */
        RUNNING,
        /** Attacking. */
        ATTACK,
        /** Done. */
        DONE
    }
    /** The current mission stage. */
    protected M3 stage = M3.NONE;
    /** Execute the completion routine on load? */
    protected boolean runCompleter;
    @Override
    public boolean applicable() {
        return world.level == 1;
    }
    @Override
    public void onTime() {
        Objective m2t1 = objective("Mission-2-Task-1");
        Objective m3 = objective("Mission-3");
        if (stage == M3.NONE && !m3.isCompleted() && m2t1.isCompleted()) {
            addMission("Mission-3", 8); // FIXME time required
            stage = M3.WAIT;
        }
        if (checkMission("Mission-3")) {
            world.env.speed1();

            incomingMessage("Douglas-Carrier");
            addTimeout("Mission-3-Message", 20000);
        }
        if (checkTimeout("Mission-3-Message")) {
            if (!objective("Mission-3").isCompleted()) {
                runMission();
            }
        }
        if (m3.isActive()) {
            checkCarrierLocation();
        }
        if (checkMission("Mission-3-Timeout")) {
            world.env.stopMusic();
            world.env.playVideo("interlude/merchant_destroyed", new Action0() {
                @Override
                public void invoke() {
                    setObjectiveState("Mission-3", ObjectiveState.FAILURE);
                    addTimeout("Mission-3-Timeout", 13000);

                    removeFleets();
                }
            });
        }
        if (checkTimeout("Mission-3-Timeout")) {
            gameover();
            loseGameMessageAndMovie("Douglas-Fire-Escort-Failed", "lose/fired_level_1");
        }
        if (checkTimeout("Mission-3-Failed")) {
            world.env.playVideo("interlude/merchant_destroyed", new Action0() {
                @Override
                public void invoke() {
                    runCompleter = true;
                    incomingMessage("Douglas-Carrier-Lost", createCompleter());
                }
            });
        }
        if (checkMission("Mission-3-Success")) {
            setObjectiveState("Mission-3", ObjectiveState.SUCCESS);
            addTimeout("Mission-3-Done", 13000);
            removeFleets();
        }
        if (checkTimeout("Mission-3-Done")) {
            objective("Mission-3").visible = false;
        }
    }
    /**
     * @return creates an action that runs if the carrier is lost.
     */
    private Action0 createCompleter() {
        return new Action0() {
            @Override
            public void invoke() {
                runCompleter = false;
                setObjectiveState("Mission-3", ObjectiveState.FAILURE);
                addTimeout("Mission-3-Done", 13000);
            }
        };
    }

    /** Remove the scripted fleets. */
    void removeFleets() {
        Fleet fi = findTaggedFleet("Mission-3-Pirates", player("Pirates"));
        if (fi != null) {
            world.removeFleet(fi);
            removeScripted(fi);
        }
        fi = findTaggedFleet("Mission-3-Carrier", player);
        if (fi != null) {
            world.removeFleet(fi);
            removeScripted(fi);
        }
        cleanupScriptedFleets();
    }
    /**
     * Create a carrier moving across the screen.
     */
    void createCarrierTask() {
        Planet naxos = planet("Naxos");
        Fleet f = createFleet(label("mission-3.escort_carrier.name"), player, naxos.x + 20, naxos.y + 40);
        addInventory(f, "TradersFreight3", 1);
        for (InventoryItem ii : f.inventory.iterable()) {
            ii.tag = "Mission-3-Carrier";
        }
        f.task = FleetTask.SCRIPT;
        moveToDestination();
        addScripted(f);
    }
    /**
     * Set the target for the carrier fleet.
     */
    void moveToDestination() {
        Fleet f = findTaggedFleet("Mission-3-Carrier", player);
        f.moveTo(planet("Centronom"));
        f.task = FleetTask.SCRIPT;
    }
    /**
     * Check the carrier's location and attack it with pirate.
     */
    void checkCarrierLocation() {
        if (stage == M3.RUNNING) {
            final Fleet fi = findTaggedFleet("Mission-3-Carrier", player);
            Planet sansterling = planet("San Sterling");
            double d = Math.hypot(fi.x - sansterling.x, fi.y - sansterling.y);

            if (d < 15) {
                world.env.speed1();
                world.env.stopMusic();
                world.env.playVideo("interlude/merchant_attacked", new Action0() {
                    @Override
                    public void invoke() {

                        addMission("Mission-3-Timeout", 24);

                        Fleet pf = createFleet(label("pirates.fleet_name"),

                                player("Pirates"), fi.x + 1, fi.y + 1);

                        addInventory(pf, "PirateFighter", 3);
                        for (InventoryItem ii : pf.inventory.iterable()) {
                            ii.tag = "Mission-3-Pirates";
                        }

                        addScripted(pf);
                        fi.stop();

                        Fleet ff = getFollower(fi, player);
                        if (ff != null) {
                            ff.owner.fleets.put(pf, FleetKnowledge.VISIBLE);
                            ff.attack(pf);
                        } else {
                            world.env.playSound(SoundTarget.COMPUTER, SoundType.CARRIER_UNDER_ATTACK, null);
                        }
                        stage = M3.ATTACK;
                        world.env.playMusic();
                    }
                });

            }
        } else
        if (stage == M3.DONE) {
            final Fleet fi = findTaggedFleet("Mission-3-Carrier", player);
            if (fi != null) {
                if (!player.explorationOuterLimit.contains(fi.x, fi.y)) {
                    addMission("Mission-3-Success", 0);
                } else {
                    Planet centronom = planet("Centronom");
                    double d = Math.hypot(fi.x - centronom.x, fi.y - centronom.y);
                    if (d < 5) {
                        addMission("Mission-3-Success", 0);
                    }
                }
            }
        }
    }
    /**
     * Issue the specific mission changes once task is completed.
     * @param traderSurvived did the trader survive?
     */
    void completeMission(boolean traderSurvived) {
        if (!traderSurvived) {
            addTimeout("Mission-3-Failed", 3000);
        } else {
            moveToDestination();
        }
        clearMission("Mission-3-Timeout");

        Fleet pf = findTaggedFleet("Mission-3-Pirates", player("Pirates"));
        if (pf != null) {
            world.removeFleet(pf);
        }

        cleanupScriptedFleets();

        stage = M3.DONE;
    }
    @Override
    public void onAutobattleFinish(BattleInfo battle) {
        if (stage == M3.ATTACK && isMissionSpacewar(battle, "Mission-3")) {
            boolean traderSurvived = finishJointAutoSpaceBattle(battle, "Mission-3-Carrier");
            completeMission(traderSurvived);
            if (traderSurvived) {
                player.changeInventoryCount(world.researches.get("Shield1"), 1);
            }
        }
    }
    @Override
    public void onAutobattleStart(BattleInfo battle) {
        if (stage == M3.ATTACK && isMissionSpacewar(battle, "Mission-3")) {
            startJointAutoSpaceBattle(battle, "Mission-3-Carrier", player, "Mission-3-Pirates", player("Pirates"));
        }
    }
    @Override
    public void onSpacewarStart(SpacewarWorld war) {
        if (stage == M3.ATTACK && isMissionSpacewar(war.battle(), "Mission-3")) {
            if (startJointSpaceBattle(war, "Mission-3-Carrier", player, "Mission-3-Pirates", player("Pirates"))) {
                war.battle().chat = "chat.mission-3.escort.cargo";
            }
        }
    }
    @Override
    public void onSpacewarFinish(SpacewarWorld war) {
        if (stage == M3.ATTACK && isMissionSpacewar(war.battle(), "Mission-3")) {
            // find the status of the trader ship
            boolean traderSurvived = findTaggedFleet("Mission-3-Carrier", player) != null;
            completeMission(traderSurvived);
            if (traderSurvived) {
                war.battle().rewardText = label("mission-3.reward");
                war.battle().messageText = label("battlefinish.mission-3.11");
                player.changeInventoryCount(world.researches.get("Shield1"), 1);
            }
        }
    }
    @Override
    public void reset() {
        stage = M3.NONE;
    }
    @Override
    public void save(XElement xmission) {
        super.save(xmission);
        xmission.set("stage", stage);
        xmission.set("run-completer", runCompleter);
    }
    @Override
    public void load(XElement xmission) {
        super.load(xmission);
        stage = M3.valueOf(xmission.get("stage", M3.NONE.toString()));
        runCompleter = xmission.getBoolean("run-completer", false);
    }
    @Override
    public boolean fleetBlink(Fleet f) {
        return stage == M3.ATTACK && f.owner == player && hasTag(f, "Mission-3-Carrier");
    }
    @Override
    public void onMessageSeen(String id) {
        if ("Douglas-Carrier".equals(id)) {
            runMission();
        }
    }
    /** Play the interlude video, show the objective and create the trader. */
    void runMission() {
        Objective m3 = objective("Mission-3");
        if (!m3.isCompleted() && !m3.visible) {
            world.env.stopMusic();
            world.env.playVideo("interlude/merchant_in", new Action0() {
                @Override
                public void invoke() {
                    world.env.playMusic();
                    showObjective("Mission-3");
                    stage = M3.RUNNING;
                    createCarrierTask();
                }
            });
        }
    }
    @Override
    public void onLoaded() {
        super.onLoaded();
        if (runCompleter) {
            createCompleter().invoke();
        }
    }
}
