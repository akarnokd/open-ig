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
import hu.openig.model.ModelUtils;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.SoundTarget;
import hu.openig.model.SoundType;
import hu.openig.model.SpacewarWorld;
import hu.openig.utils.XElement;

import java.util.List;

/**
 * Mission 12: New Caroline virus infection.
 * @author akarnokd, Feb 4, 2012
 */
public class Mission12 extends Mission {
    /** The stages. */
    public enum M12Stages {
        /** Not started yet. */
        NONE,
        /** Waiting for the first action. */
        INITIAL_DELAY,
        /** Incoming message. */
        FIRST_MESSAGE,
        /** First rundown. */
        FIRST_RUNDOWN,
        /** Waiting for subsequent infections. */
        SUBSEQUENT_DELAY,
        /** Infection rundown. */
        SUBSEQUENT_MESSAGE,
        /** Subsequent rundown. */
        SUBSEQUENT_RUNDOWN,
        /** Mission done. */
        DONE
    }
    /** The mission stages. */
    protected M12Stages stage = M12Stages.NONE;
    /** If traders were killed. */
    protected boolean tradersLost;
    /** Was there multiple planets infected? */
    protected boolean multipleInfections;
    /** Reinforcements called once. */
    protected boolean reinforcements;
    /** The infection turns. */
    protected int turns = 3;
    /** Execute the completion routine on load? */
    protected boolean runCompleter;
    @Override
    public void onTime() {
        Objective m11t1 = objective("Mission-11");
        if (m11t1.isCompleted() && stage == M12Stages.NONE && !objective("Mission-12").isCompleted()) {
            addMission("Mission-12", 12);
            stage = M12Stages.INITIAL_DELAY;
        }
        if (checkMission("Mission-12")) {
            world.env.speed1();
            incomingMessage("New Caroline-Garthog-Virus", "Mission-12", "Mission-12-Task-1");
            stage = M12Stages.FIRST_MESSAGE;
            planet("New Caroline").quarantineTTL = Planet.DEFAULT_QUARANTINE_TTL;

            // allow reinforcements
            send("Douglas-Reinforcements-Approved").visible = true;
            send("Douglas-Reinforcements-Denied").visible = false;
            send("Douglas-Reinforcements-Denied-2").visible = false;
            send("Douglas-Reinforcements-Approved").visible = true;
        }
        Objective m12 = objective("Mission-12");
        if (checkMission("Mission-12-Subsequent")

                && m12.state == ObjectiveState.ACTIVE) {
            Objective m14 = objective("Mission-14");
            if (!hasMission("Mission-14")
                    && (!m14.visible && m14.state == ObjectiveState.ACTIVE)) {
                world.env.speed1();

                String m12tx = "";
                int infectionCount = 0;
                for (int i = 2; i <= turns; i++) {
                    Objective o = objective("Mission-12-Task-" + i);
                    if (!o.visible && o.state == ObjectiveState.ACTIVE) {
                        m12tx = "Mission-12-Task-" + i;
                        infectionCount = i;
                        break;
                    }
                }

                incomingMessage("New Caroline-Garthog-Virus-Again", "Mission-12", m12tx);

                stage = M12Stages.SUBSEQUENT_MESSAGE;
                Planet nc = planet("New Caroline");
                nc.quarantineTTL = Planet.DEFAULT_QUARANTINE_TTL;

                if (infectionCount > 2) {
                    List<Planet> ps = player.ownPlanets();
                    ps.remove(nc);
                    ModelUtils.random(ps).quarantineTTL = Planet.DEFAULT_QUARANTINE_TTL;
                }
            }
        }
        if (checkMission("Mission-12-Hide")) {
            objective("Mission-12").visible = false;
        }
        if (checkMission("Mission-12-TaskSuccess")) {
            completeActiveTask();
        }
        if (checkTimeout("Mission-12-Hide")) {
            objective("Mission-12").visible = false;
        }
    }
    @Override
    public void onMessageSeen(String id) {
        if (stage == M12Stages.FIRST_MESSAGE) {
            if ("New Caroline-Garthog-Virus".equals(id)) {
                stage = M12Stages.FIRST_RUNDOWN;
            }
        }
        if (stage == M12Stages.SUBSEQUENT_MESSAGE) {
            if ("New Caroline-Garthog-Virus-Again".equals(id)) {
                stage = M12Stages.SUBSEQUENT_RUNDOWN;
            }
        }
//        if ("New Caroline-Garthog-Virus-Resolved".equals(id)
//                || "New Caroline-Garthog-Virus-Again-Deaths".equals(id)) {
//            completeActiveTask();

//        }
        if ("Douglas-Report-Viruses".equals(id)) {
            addMission("Mission-14", 8);
            objective("Mission-12-Task-6").visible = true;
            setObjectiveState("Mission-12-Task-6", ObjectiveState.SUCCESS);
        }
        if ("Douglas-Reinforcements-Approved".equals(id)) {
            if (!reinforcements

                    && (stage == M12Stages.FIRST_RUNDOWN

                    || stage == M12Stages.SUBSEQUENT_MESSAGE
                    || stage == M12Stages.SUBSEQUENT_RUNDOWN
                    || stage == M12Stages.SUBSEQUENT_MESSAGE)) {
                send("Douglas-Reinforcements-Denied-2").visible = true;
                send("Douglas-Reinforcements-Denied-2").seen = false;
                send("Douglas-Reinforcements-Approved").visible = false;

                reinforcements = true;

                addReinforcements();
            }
        }
    }
    /**
     * Add reinforcements to the main fleet or a new fleet.
     */
    private void addReinforcements() {
        Fleet f = findTaggedFleet("CampaignMainShip2", player);

        int f1 = 4;
        int f2 = 2;

        if (f.inventoryCount(research("Fighter1")) > world.params().fighterLimit() - f1
                || f.inventoryCount(research("Fighter2")) > world.params().fighterLimit() - f2) {

            f = createFleet(label("Empire.main_fleet"), player, f.x + 5, f.y + 5);
        }

        addInventory(f, "Fighter1", f1);
        addInventory(f, "Fighter2", f2);

        world.env.playSound(SoundTarget.COMPUTER, SoundType.REINFORCEMENT_ARRIVED_2, null);
    }
    /**
     * Complete the active task.
     */
    void completeActiveTask() {
        boolean noCarriers = objective("Mission-14").state == ObjectiveState.SUCCESS;
        for (int i = turns; i >= 1; i--) {
            Objective o = objective("Mission-12-Task-" + i);
            if (o.visible && o.state == ObjectiveState.ACTIVE) {
                if (setObjectiveState("Mission-12-Task-" + i, ObjectiveState.SUCCESS)) {
                    if (hasMission("Mission-12-TaskSuccess")) {
                        clearMission("Mission-12-TaskSuccess");
                    }
                    if (i >= 2) {
                        send("Douglas-Report-Viruses").visible = true;
                    }
                    if (i >= turns && !noCarriers) {
                        showObjective("Mission-12-Task-6");
                    }
                }
                break;
            }
        }
        if (noCarriers) {
            if (setObjectiveState("Mission-12", ObjectiveState.SUCCESS)) {
                addTimeout("Mission-12-Hide", 13000);
            }
        }
    }
    @Override
    public void onLost(Planet planet) {
        for (int i = 1; i <= turns; i++) {
            Objective o = objective("Mission-12-Task-" + i);
            if (o.isActive()) {
                setObjectiveState(o, ObjectiveState.FAILURE);
            }
            setObjectiveState("Mission-12", ObjectiveState.FAILURE);
        }
        gameover();
        loseGameMessageAndMovie("Douglas-Fire-Lost-Planet-2", "lose/fired_level_2");
    }
    @Override
    public void onPlanetCured(Planet planet) {
        // check how many planets have infection
        int cnt = 0;
        for (Planet p : player.ownPlanets()) {
            if (p.quarantineTTL > 0) {
                cnt++;
            }
        }
        if (cnt == 0) {
            Action0 completer = createCompleter();
            if (stage == M12Stages.FIRST_RUNDOWN || stage == M12Stages.FIRST_MESSAGE) {
                if (multipleInfections) {
                    runCompleter = true;
                    incomingMessage("New Caroline-Garthog-Virus-Breached", completer);
                } else {
                    runCompleter = true;
                    incomingMessage("New Caroline-Garthog-Virus-Resolved", completer);
                }
                addMission("Mission-12-Subsequent", 24);
                addMission("Mission-12-TaskSuccess", 2);
                stage = M12Stages.SUBSEQUENT_DELAY;
                addMission("Mission-12-Hide", 5);
            }
            if (stage == M12Stages.SUBSEQUENT_RUNDOWN || stage == M12Stages.SUBSEQUENT_MESSAGE) {
                if (tradersLost) {
                    runCompleter = true;
                    incomingMessage("New Caroline-Garthog-Virus-Again-Deaths", completer);
                } else

                if (multipleInfections) {
                    runCompleter = true;
                    incomingMessage("New Caroline-Garthog-Virus-Breached", completer);
                } else {
                    runCompleter = true;
                    incomingMessage("New Caroline-Garthog-Virus-Resolved", completer);
                }
                boolean noCarriers = objective("Mission-14").state == ObjectiveState.SUCCESS;
                for (int i = turns; i >= 2; i--) {
                    Objective o = objective("Mission-12-Task-" + i);
                    if (o.visible && o.state == ObjectiveState.ACTIVE) {
                        if (i < turns && !noCarriers) {
                            addMission("Mission-12-Subsequent", 24);
                            stage = M12Stages.SUBSEQUENT_DELAY;
                        } else {
                            stage = M12Stages.DONE;
                        }
                        addMission("Mission-12-TaskSuccess", 2);
                        break;
                    }
                }

            }
        }
    }
    /**
     * Create the completer action.
     * @return the action
     */
    private Action0 createCompleter() {
        return new Action0() {
            @Override
            public void invoke() {
                runCompleter = false;
                completeActiveTask();

            }
        };
    }
    @Override
    public void onPlanetInfected(Planet planet) {
        if ((stage == M12Stages.FIRST_RUNDOWN

                || stage == M12Stages.FIRST_MESSAGE
                || stage == M12Stages.SUBSEQUENT_MESSAGE
                || stage == M12Stages.SUBSEQUENT_RUNDOWN
                ) && planet.owner == player) {
            int cnt = 0;
            for (Planet p : player.ownPlanets()) {
                if (p.quarantineTTL > 0) {
                    cnt++;
                }
            }
            if (!planet.id.equals("New Caroline")) {
                multipleInfections = true;
            }
            if (cnt > 2) {
                gameover();
                loseGameMessageAndMovie("Douglas-Fire-Lost-Planet-2", "lose/fired_level_2");
            } else {
                if (!planet.id.equals("New Caroline")) {
                    incomingMessage(planet.id + "-Virus");
                }
            }
        }
    }
    @Override
    public void onAutobattleFinish(BattleInfo battle) {
        if (stage == M12Stages.FIRST_RUNDOWN

                || stage == M12Stages.SUBSEQUENT_RUNDOWN) {
            if (battle.targetFleet != null

                    && battle.targetFleet.owner == player("Traders")
                    && battle.targetFleet.inventory.isEmpty()) {
                tradersLost = true;
            }
        }
    }
    @Override
    public void onSpacewarFinish(SpacewarWorld war) {
        onAutobattleFinish(war.battle());
    }
    @Override
    public boolean applicable() {
        return world.level == 2;
    }
    @Override
    public void load(XElement xmission) {
        super.load(xmission);
        stage = M12Stages.valueOf(xmission.get("stage"));
        tradersLost = xmission.getBoolean("traders-lost");
        multipleInfections = xmission.getBoolean("multiple-infections", false);
        reinforcements = xmission.getBoolean("reinforcements", false);
        runCompleter = xmission.getBoolean("run-completer", false);
    }
    @Override
    public void save(XElement xmission) {
        super.save(xmission);
        xmission.set("stage", stage);
        xmission.set("traders-lost", tradersLost);
        xmission.set("multiple-infections", multipleInfections);
        xmission.set("reinforcements", reinforcements);
        xmission.set("run-completer", runCompleter);
    }
    @Override
    public void onLoaded() {
        super.onLoaded();
        if (runCompleter) {
            createCompleter().invoke();
        }
    }
    @Override
    public void reset() {
        tradersLost = false;
        multipleInfections = false;
        reinforcements = false;
        stage = M12Stages.NONE;
        super.reset();
    }

    @Override
    public boolean fleetBlink(Fleet f) {
        Planet p = f.targetPlanet();
        return f.owner == player("Traders")

                && (stage != M12Stages.NONE && stage != M12Stages.DONE && stage != M12Stages.SUBSEQUENT_DELAY)

                && p != null && p.quarantineTTL > 0;
    }
}
