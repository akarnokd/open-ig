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
import hu.openig.model.FleetMode;
import hu.openig.model.FleetTask;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.utils.XElement;

/**
 * Mission 10: Escort centronom governor.
 * @author akarnokd, Jan 22, 2012
 */
public class Mission10 extends Mission {
    /** The stages. */
    enum M10Stages {
        /** Not started yet. */
        NONE,
        /** Wait for intro. */
        WAIT,
        /** Main operation running. */
        RUN,
        /** Mission done. */
        DONE
    }
    /** The current mission stage. */
    M10Stages stage = M10Stages.NONE;
    @Override
    public void onTime() {
        Objective m9 = objective("Mission-9");
        if (stage == M10Stages.NONE && m9.isCompleted() && !objective("Mission-10").isCompleted()) {
            addMission("Mission-10", 3 * 24);
            stage = M10Stages.WAIT;
        }
        if (checkMission("Mission-10")) {
            world.env.stopMusic();
            world.env.playVideo("interlude/colony_ship_arrival", new Action0() {
                @Override
                public void invoke() {
                    world.env.playMusic();
                    incomingMessage("Douglas-Centronom-Governor", "Mission-10");
                    world.env.speed1();
                    createGovernor();
                    stage = M10Stages.RUN;
                }
            });

        }
        if (checkTimeout("Mission-10-Objective")) {
            showObjective("Mission-10");
        }
        if (checkTimeout("Mission-10-Hide")) {
            objective("Mission-10").visible = false;
        }
    }
    /**
     * Create the governor's fleet.
     */
    void createGovernor() {
        Planet sst = planet("San Sterling");
        Planet nax = planet("Naxos");
        // create simple pirate fleet
        Fleet pf = createFleet(label("mission-10.governor_name"),

                player, sst.x, sst.y);
        pf.task = FleetTask.SCRIPT;
        // ----------------------------------------------------------------
        addInventory(pf, "ColonyShip", 1);
        addInventory(pf, "TradersFreight1", 1);
        // ----------------------------------------------------------------
        tagFleet(pf, "Mission-10-Governor");
        pf.mode = FleetMode.MOVE;
        pf.setTargetPlanet(nax);

        addScripted(pf);

    }
    @Override
    public void onFleetAt(Fleet fleet, Planet planet) {
        Objective m10 = objective("Mission-10");
        if (m10.isActive()) {
            if (planet.id.equals("Naxos")

                    && hasTag(fleet, "Mission-10-Governor")) {
                world.removeFleet(fleet);
                removeScripted(fleet);
                if (setObjectiveState("Mission-10", ObjectiveState.SUCCESS)) {
                    addTimeout("Mission-10-Hide", 13000);
                    stage = M10Stages.DONE;
                }
            }
        }
    }
    @Override
    public boolean applicable() {
        return world.level == 2;
    }
    @Override
    public void save(XElement xmission) {
        super.save(xmission);
        xmission.set("stage", stage);
    }
    @Override
    public void load(XElement xmission) {
        super.load(xmission);
        stage = M10Stages.valueOf(xmission.get("stage", M10Stages.NONE.toString()));
    }
    @Override
    public void reset() {
        stage = M10Stages.NONE;
    }
}
