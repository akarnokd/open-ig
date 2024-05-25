/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.mechanics.DefaultAIControls;
import hu.openig.model.BattleInfo;
import hu.openig.model.Building;
import hu.openig.model.Fleet;
import hu.openig.model.FleetMode;
import hu.openig.model.FleetTask;
import hu.openig.model.GroundwarWorld;
import hu.openig.model.InventoryItem;
import hu.openig.model.InventorySlot;
import hu.openig.model.ModelUtils;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.Player;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.SpacewarWorld;
import hu.openig.utils.XElement;

import java.util.List;

/**
 * Mission 11: Random planet attacked by garthog.
 * @author akarnokd, Jan 22, 2012
 *
 */
public class Mission11 extends Mission {
    /** The mission stages. */
    enum M11Stages {
        /** Not started. */
        NONE,
        /** Wait for start. */
        WAIT,
        /** Running. */
        RUN,
        /** Done. */
        DONE
    }
    /** The current stage. */
    M11Stages stage = M11Stages.NONE;
    @Override
    public boolean applicable() {
        return world.level == 2;
    }
    @Override
    public void onTime() {
        Objective m10 = objective("Mission-10");
        Objective m11 = objective("Mission-11");
        if (m10.isCompleted() && stage == M11Stages.NONE && !objective("Mission-11").isCompleted()) {
            addMission("Mission-11", 6 * 24);
            stage = M11Stages.WAIT;
        }
        if (stage == M11Stages.WAIT && checkMission("Mission-11")) {
            showObjective("Mission-11");
            createGarthog();
            stage = M11Stages.RUN;
        }
        if (checkTimeout("Mission-11-Done")) {
            if (m11.state == ObjectiveState.FAILURE) {
                gameover();
                loseGameMessageAndMovie("Douglas-Fire-Lost-Planet-2", "lose/fired_level_2");
            }
            m11.visible = false;
            garthogGoHome();
        }
        if (checkMission("Mission-11-Hide")) {
            Fleet garthog = findTaggedFleet("Mission-11-Garthog", player("Garthog"));
            if (garthog != null) {
                removeScripted(garthog);
                world.removeFleet(garthog);
            }
        }
    }
    /** Move the Garthog fleet home. */
    void garthogGoHome() {
        Fleet garthog = findTaggedFleet("Mission-11-Garthog", player("Garthog"));
        if (garthog != null) {
            garthog.moveTo(planet("Garthog 1"));
            garthog.task = FleetTask.SCRIPT;
            addMission("Mission-11-Hide", 6);
        }
    }
    /**
     * Create the attacking garthog fleet.
     */
    void createGarthog() {
        Planet from = planet("Garthog 1");
        Player garthog = player("Garthog");
        Fleet f = createFleet(format("fleet", garthog.shortName), garthog, from.x, from.y);
        // --------------------------------------------------
        // Adjust attacker strength here
        addInventory(f, "GarthogFighter", 20);
        equipFully(addInventory(f, "GarthogDestroyer", 10));
        equipFully(addInventory(f, "GarthogBattleship", 2));
        addInventory(f, "LightTank", 6);
        addInventory(f, "RadarCar", 1);
        addInventory(f, "GarthogRadarJammer", 1);
        // ---------------------------------------------------
        tagFleet(f, "Mission-11-Garthog");
        InventoryItem iib = f.getInventoryItem(research("GarthogBattleship"));
        InventorySlot is = iib.getSlot("hyperdrive");
        if (is != null) {
            is.type = null;
            is.count = 0;
        }

        Planet target = selectTarget();

        f.setTargetPlanet(target);

        garthog.changeInventoryCount(research("SpySatellite1"), 1);
        DefaultAIControls.actionDeploySatellite(garthog, target, research("SpySatellite1"));
        garthog.planets.put(target, PlanetKnowledge.BUILDING);

        f.mode = FleetMode.ATTACK;
        f.task = FleetTask.SCRIPT;

        incomingMessage(target.id + "-Is-Under-Attack");

    }
    /**
     * Select a planet to attack.
     * @return the selected planet
     */
    protected Planet selectTarget() {
        Planet target = null;
        double strength = Double.MAX_VALUE;
        // No Cheat: doesn't check garrison count, only things that can be seen by spysat2
        List<Planet> ps = player.ownPlanets();
        ModelUtils.shuffle(ps);
        for (Planet p : ps) {
            double sp = 0;
            // check station strength
            for (InventoryItem ii : p.inventory.iterable()) {
                if (ii.owner == player && ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
                    sp += world.getHitpoints(ii.type, ii.owner);
                }
            }
            // check gun and shield strength, check fortification strength
            for (Building b : p.surface.buildings.iterable()) {
                if (b.type.kind.equals("Gun")) {
                    sp += world.getHitpoints(b.type, player, true);
                }
                if (b.type.kind.equals("Shield")) {
                    sp += world.getHitpoints(b.type, player, true);
                }
                if (b.type.kind.equals("Defensive")) {
                    sp += world.getHitpoints(b.type, player, false);
                }
            }

            if (sp < strength) {
                strength = sp;
                target = p;
            }
        }
        return target;
    }
    @Override
    public void onSpacewarFinish(SpacewarWorld war) {
        if (isM11Active()) {
            Fleet garthog = findTaggedFleet("Mission-11-Garthog", player("Garthog"));
            if (garthog == null || !canGroundAttack(garthog)) {
                setObjectiveState("Mission-11", ObjectiveState.SUCCESS);
                war.battle().messageText = label("battlefinish.mission-5.garthog");
                addTimeout("Mission-11-Done", 13000);
                cleanupScriptedFleets();
                stage = M11Stages.DONE;
            }
        }

    }
    /**
     * @return check if mission 11 is active
     */
    private boolean isM11Active() {
        return objective("Mission-11").isActive();
    }
    @Override
    public void onGroundwarFinish(GroundwarWorld war) {
        onAutobattleFinish(war.battle());
    }
    @Override
    public void onAutobattleFinish(BattleInfo battle) {
        if (isM11Active()

                && battle.targetPlanet != null
                && battle.attacker.owner.id.equals("Garthog")) {
            if (battle.targetPlanet.owner != player) {
                setObjectiveState("Mission-11", ObjectiveState.FAILURE);
            } else {
                setObjectiveState("Mission-11", ObjectiveState.SUCCESS);
            }
            addTimeout("Mission-11-Done", 13000);
            Fleet garthog = findTaggedFleet("Mission-11-Garthog", player("Garthog"));
            if (garthog != null) {
                removeScripted(garthog);
            }
            cleanupScriptedFleets();

            stage = M11Stages.DONE;

        }
    }
    @Override
    public void save(XElement xmission) {
        super.save(xmission);
        xmission.set("stage", stage);
    }
    @Override
    public void load(XElement xmission) {
        super.load(xmission);
        stage = M11Stages.valueOf(xmission.get("stage", M11Stages.NONE.toString()));
    }
    @Override
    public void reset() {
        stage = M11Stages.NONE;
    }
}
