/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Action0;
import hu.openig.core.Difficulty;
import hu.openig.model.BattleInfo;
import hu.openig.model.Fleet;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.FleetMode;
import hu.openig.model.FleetTask;
import hu.openig.model.InventoryItem;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.SoundTarget;
import hu.openig.model.SoundType;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarWorld;
import hu.openig.utils.XElement;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.EnumSet;
import java.util.List;

/**
 * Mission 5: Defend the Thorin.
 * @author akarnokd, 2012.01.18.
 */
public class Mission5 extends Mission {
    /** The stages. */
    enum M5 {
        /** Not started. */
        NONE,
        /** Wait for objective. */
        WAIT,
        /** Meet with Thorin. */
        MEET,
        /** Running. */
        RUN,
        /** Completed. */
        DONE
    }
    /** Reinforcements once. */
    protected boolean reinforcements;
    /** The current stage. */
    M5 stage = M5.NONE;
    /** Execute the task 3 success action? */
    boolean runTask3Success;
    @Override
    public boolean applicable() {
        return world.level == 1;
    }
    @Override
    public void onTime() {
        Objective m2t1 = objective("Mission-2-Task-3");
        Objective m1 = objective("Mission-1");
        Objective m5 = objective("Mission-5");
        Objective m5t1 = objective("Mission-5-Task-1");
        if (!m5.isCompleted() && m2t1.isCompleted() && m1.isCompleted() && stage == M5.NONE) {
            addMission("Mission-5", 24);
            stage = M5.WAIT;
        }
        if (checkMission("Mission-5")) {
            world.env.speed1();
            incomingMessage("Douglas-Thorin", "Mission-5");
            createTullen();
            addMission("Mission-5-Timeout-1", 24);
            addTimeout("Mission-5-Task-1", 2000);
            stage = M5.MEET;
        }
        if (checkTimeout("Mission-5-Task-1")) {
            showObjective("Mission-5-Task-1");
        }
        if (checkMission("Mission-5-Timeout-1")) {
            setObjectiveState("Mission-5", ObjectiveState.FAILURE);
            setObjectiveState("Mission-5-Task-1", ObjectiveState.FAILURE);
            gameover();
            loseGameMessageAndMovie("Douglas-Fire-Mistakes", "lose/fired_level_1");
        }
        if (m5t1.visible && m5t1.state == ObjectiveState.ACTIVE) {
            if (checkFleetInRange()) {
                clearMission("Mission-5-Timeout-1");
                world.env.stopMusic();
                world.env.playVideo("interlude/thorin_escort", new Action0() {
                    @Override
                    public void invoke() {
                        setObjectiveState("Mission-5-Task-1", ObjectiveState.SUCCESS);
                        showObjective("Mission-5-Task-3");

                        addMission("Mission-5-Task-2", 1); // FIXME garthog delay
                        addMission("Mission-5-Task-2-Timeout", 28);
                        moveTullen();
                        send("Douglas-Thorin-Reinforcements").visible = true;
                        send("Douglas-Reinforcements-Denied").visible = false;
                        world.env.playMusic();
                        world.env.pause();
                        stage = M5.RUN;

                    }
                });
            }
        }
        if (checkMission("Mission-5-Task-2")) {
            showObjective("Mission-5-Task-2");
            createGarthog();
        }
        if (checkMission("Mission-5-Task-2-Timeout")) {
            setObjectiveState("Mission-5-Task-2", ObjectiveState.FAILURE);
            setObjectiveState("Mission-5", ObjectiveState.FAILURE);
            removeFleets();
            addTimeout("Mission-5-Failed", 13000);
        }
        if (checkTimeout("Mission-5-Failed")) {
            send("Douglas-Thorin-Reinforcements").visible = false;
            gameover();
            loseGameMessageAndMovie("Douglas-Fire-Escort-Failed", "lose/fired_level_1");
        }
        if (checkTimeout("Mission-5-Success")) {
            stage = M5.DONE;
            send("Douglas-Thorin-Reinforcements").visible = false;

            setObjectiveState("Mission-5-Task-2", ObjectiveState.SUCCESS);
            setObjectiveState("Mission-5", ObjectiveState.SUCCESS);
            removeFleets();

            addTimeout("Mission-5-Promote", 13000);
            addTimeout("Level-1-Success", 3000);
        }
        if (checkTimeout("Mission-5-Promote")) {
            objective("Mission-5").visible = false;
            incomingMessage("Douglas-Promotion-2");
            objective("Mission-1-Task-3").visible = false;
            objective("Mission-1-Task-4").visible = false;

            world.env.stopMusic();
            world.env.pause();

            world.env.forceMessage("Douglas-Promotion-2", new Action0() {
                @Override
                public void invoke() {
                    world.env.playVideo("interlude/level_2_intro", new Action0() {
                        @Override
                        public void invoke() {
                            promote();
                        }
                    });
                }
            });

        }
        if (checkTimeout("Level-1-Success")) {
            setObjectiveState("Mission-1-Task-3", ObjectiveState.SUCCESS);
            setObjectiveState("Mission-1-Task-4", ObjectiveState.SUCCESS);
        }
    }
    @Override
    public void onMessageSeen(String id) {
        if ("Douglas-Thorin-Reinforcements".equals(id)) {
            if (!reinforcements) {
                reinforcements = true;
                Fleet own = findTaggedFleet("CampaignMainShip1", player);
                if (own != null) {
                    addInventory(own, "Fighter1", 3);
                }
                runTask3Success = true;
                world.env.playSound(SoundTarget.COMPUTER, SoundType.REINFORCEMENT_ARRIVED_1, createTask3Success());
            }
        }
    }
    /**
     * @return Creates a task success action.
     */
    private Action0 createTask3Success() {
        return new Action0() {
            @Override
            public void invoke() {
                runTask3Success = false;
                setObjectiveState("Mission-5-Task-3", ObjectiveState.SUCCESS);
            }
        };
    }
    /**
     * Perform the promotion action.
     */
    void promote() {
        world.level = 2;
        world.env.playMusic();
    }
    /**
     * Check if Tullen successfully left the area.
     * @return true if left
     */
    boolean checkTullenLeft() {
        Fleet tullen = findTaggedFleet("Mission-5", player);
        if (tullen != null) {
            return tullen.waypoints.isEmpty() && !player.withinLimits(tullen.x, tullen.y, 0);
        }
        return false;
    }
    /**
     * Remove both fleets.
     */
    void removeFleets() {
        Fleet tullen = findTaggedFleet("Mission-5", player);
        if (tullen != null) {
            world.removeFleet(tullen);
        }
        Fleet garthog = findTaggedFleet("Mission-5-Garthog", player("Garthog"));
        if (garthog != null) {
            world.removeFleet(garthog);
        }
        cleanupScriptedFleets();
    }
    /**
     * Stop the fleets.
     */
    void stopFleets() {
        Fleet tullen = findTaggedFleet("Mission-5", player);
        tullen.stop();
        tullen.task = FleetTask.SCRIPT;
        Fleet garthog = findTaggedFleet("Mission-5-Garthog", player("Garthog"));
        garthog.stop();
        garthog.task = FleetTask.SCRIPT;

    }
    @Override
    public void onFleetsMoved() {
        Objective m5t2 = objective("Mission-5-Task-2");
        if (m5t2.isActive()) {
            checkTullenReached();
        }
    }

    /**
     * Check if the Garthog fleet reached Tullen.
     */
    void checkTullenReached() {
        Fleet tullen = findTaggedFleet("Mission-5", player);
        Fleet garthog = findTaggedFleet("Mission-5-Garthog", player("Garthog"));
        if (garthog != null) {
            garthog.waypoints.clear();
            garthog.waypoints.add(new Point2D.Double(tullen.x, tullen.y));
            double d = Math.hypot(tullen.x - garthog.x, tullen.y - garthog.y);
            if (d <= 10) {
                tullen.stop();
                tullen.task = FleetTask.SCRIPT;
            }
            if (d <= 5) {
                stopFleets();

                // follower automatically attacks
                Fleet ff = getFollower(tullen, player);
                if (ff != null) {
                    ff.attack(garthog);
                    ff.owner.fleets.put(garthog, FleetKnowledge.VISIBLE);
                }
            }
        }
    }
    @Override
    public void onDiscovered(Player player, Fleet fleet) {
        if (player == this.player && hasTag(fleet, "Mission-5-Garthog")) {
            if (world.env.config().slowOnEnemyAttack) {
                world.env.speed1();
            }
            world.env.playSound(SoundTarget.COMPUTER, SoundType.ENEMY_FLEET_DETECTED, null);
        }
    }
    /**
     * Set the target for the carrier fleet.
     * @param f the fleet
     */
    void moveToDestination(Fleet f) {
        Planet target = planet("San Sterling");
        f.waypoints.clear();
        f.mode = FleetMode.MOVE;
        f.moveTo(target.x - 60, target.y - 60);
        f.task = FleetTask.SCRIPT;
    }
    /**
     * Issue move order to tullen.
     */
    void moveTullen() {
        Fleet fi = findTaggedFleet("Mission-5", player);
        moveToDestination(fi);
    }
    /**
     * Check if one of the player's fleet is in range of Thorin.
     * @return true if in range
     */
    boolean checkFleetInRange() {
        Fleet fi = findTaggedFleet("Mission-5", player);
        for (Fleet f : player.fleets.keySet()) {
            if (f.owner == player && f != fi) {
                double d = Math.hypot(f.x - fi.x, f.y - fi.y);
                if (d < 5) {
                    // check also if close to Naxos
                    Planet p = planet("Naxos");
                    d = Math.hypot(p.x + 10 - f.x, p.y - 20 - f.y);
                    if (d < 5) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    @Override
    public void onSpacewarStart(SpacewarWorld war) {
        if (isMissionSpacewar(war.battle(), "Mission-5")) {
            BattleInfo battle = war.battle();
            Player garthog = player("Garthog");
            Fleet f1 = findTaggedFleet("Mission-5", player);
            Fleet f2 = findTaggedFleet("Mission-5-Garthog", garthog);

            if (!reinforcements) {
                setObjectiveState("Mission-5-Task-3", ObjectiveState.FAILURE);
                send("Douglas-Thorin-Reinforcements").visible = false;
            }

            if (battle.targetFleet == f1) {
                // thorin attacked?
                war.includeFleet(f2, f2.owner);
                battle.targetFleet = f2;
                f2.owner.ai.spaceBattle(war).spaceBattleInit();
            } else {
                // garthog attacked
                war.addStructures(f1, EnumSet.of(
                        ResearchSubCategory.SPACESHIPS_BATTLESHIPS,
                        ResearchSubCategory.SPACESHIPS_CRUISERS,
                        ResearchSubCategory.SPACESHIPS_FIGHTERS));
            }
            int helpers = 0;

            if (checkMission("Mission-4-Helped")) {
                // add 3 helper pirates
                Fleet hf = createHelperShips(f1);
                helpers = hf.inventory.size();
                war.addStructures(hf, EnumSet.of(
                        ResearchSubCategory.SPACESHIPS_FIGHTERS));
                battle.attackerAllies.add(player("Pirates"));

                battle.chat = "chat.mission-5.garthog.with.allied.pirates";
            } else {
                battle.chat = "chat.mission-5.garthog.without.allied.pirates";
            }

            // center pirate
            Dimension d = war.space();
            List<SpacewarStructure> structures = war.structures();
            int y = (d.height - helpers * 40) / 2;

            for (SpacewarStructure s : structures) {
                if (s.item != null && "Mission-5".equals(s.item.tag)) {
                    s.x = d.width / 2d;
                    s.y = d.height / 2d;
                    war.alignToNearestCell(s);
                    war.addUnitLocation(s);
                    s.angle = 0.0;
                    s.owner = f1.owner;
                    s.guard = true;
                    s.hpMax = (int)s.hp;
                }
                if (s.item != null && "Mission-5-Help".equals(s.item.tag)) {
                    s.x = d.width / 3d;
                    s.y = y;
                    war.alignToNearestCell(s);
                    war.addUnitLocation(s);
                    s.angle = 0.0;
                    s.guard = true;
                    y += 50;
                }
            }
            battle.allowRetreat = false;
        }

    }
    @Override
    public void onSpacewarFinish(SpacewarWorld war) {
        if (isMissionSpacewar(war.battle(), "Mission-5")) {
            // find the status of the trader ship
            boolean thorinSurvived = false;
            for (SpacewarStructure s : war.structures()) {
                if (s.item != null && "Mission-5".equals(s.item.tag)) {
                    thorinSurvived = true;
                    break;
                }
            }
            completeMission(thorinSurvived);
            if (thorinSurvived) {
                war.battle().messageText = label("battlefinish.mission-5.garthog");
                war.battle().rewardText = label("battlefinish.mission-5.garthog_bonus");
            }
        }
    }
    /**
     * Issue the specific mission changes once task is completed.
     * @param survive did the ship to be protected survive?
     */
    void completeMission(boolean survive) {
        clearMission("Mission-5-Task-2-Timeout");
        Fleet garthog = findTaggedFleet("Mission-5-Garthog", player("Garthog"));
        if (garthog != null) {
            world.removeFleet(garthog);
        }
        Fleet hf = findTaggedFleet("Mission-5-Help", player("Pirates"));
        if (hf != null) {
            world.removeFleet(hf);
        }
        cleanupScriptedFleets();
        Fleet own = findTaggedFleet("CampaignMainShip1", player);
        if (survive && own != null) {
            addTimeout("Mission-5-Success", 3000);
            moveTullen();
        } else {
            setObjectiveState("Mission-5-Task-3", ObjectiveState.FAILURE);
            setObjectiveState("Mission-5", ObjectiveState.FAILURE);
            if (own != null) {
                addTimeout("Mission-5-Failed", 3000);
            }
        }
    }
    @Override
    public void onAutobattleStart(BattleInfo battle) {
        if (isMissionSpacewar(battle, "Mission-5")) {
            Player g = player("Garthog");
            Fleet f1 = findTaggedFleet("Mission-5", player);
            Fleet f2 = findTaggedFleet("Mission-5-Garthog", g);
            if (battle.targetFleet == f1) {
                battle.targetFleet = f2;
            }
            battle.attacker.inventory.addAll(f1.inventory.iterable());
            if (checkMission("Mission-4-Helped")) {
                battle.attacker.inventory.addAll(createHelperShips(f1).inventory.list());
            }

            if (!reinforcements) {
                setObjectiveState("Mission-5-Task-3", ObjectiveState.FAILURE);
                send("Douglas-Thorin-Reinforcements").visible = false;
            }
        }
    }
    @Override
    public void onAutobattleFinish(BattleInfo battle) {
        if (isMissionSpacewar(battle, "Mission-5")) {
            boolean tullenSurvived = false;
            for (InventoryItem ii : battle.attacker.inventory.list()) {
                if ("Mission-5".equals(ii.tag)) {
                    tullenSurvived = true;
                    battle.attacker.inventory.remove(ii);
                }
                if ("Mission-5-Help".equals(ii.tag)) {
                    battle.attacker.inventory.remove(ii);
                }
            }
            completeMission(tullenSurvived);
        }
    }
    /**
     * Create Tullen's fleet.
     * FIXME setup proper strength
     */
    void createTullen() {
        Planet p = planet("Naxos");
        Fleet f = createFleet(label("mission-5.tullens_fleet"), player, p.x + 40, p.y - 10);
        addInventory(f, "Flagship", 1);
        for (InventoryItem ii : f.inventory.iterable()) {
            ii.tag = "Mission-5";
            if (ii.type.id.equals("Flagship")) {
                // -------------------------------------------------------
                // Set flagship strength and equipment here
                ii.hp = 800;
                setSlot(ii, "laser", "Laser1", 6);
                setSlot(ii, "rocket", "Rocket1", 4);
                // -------------------------------------------------------
            }
        }
        f.moveTo(p.x + 10, p.y - 20);
        f.task = FleetTask.SCRIPT;
        addScripted(f);
    }
    /**

     * Create garthog fleet to attack tullen.
     * FIXME strength adjustments

     */
    void createGarthog() {
        Planet p = planet("Naxos");

//        Fleet t = findTaggedFleet("Mission-5", player);
//        t.stop();
//        t.task = FleetTask.SCRIPT;

        Fleet f = createFleet(label("mission-5.garthog_fleet"),

                player("Garthog"), p.x + 30, p.y - 20);
        // -------------------------------------------------------
        // Set strengths here
        int fighterCounts = 6;
        if (world.difficulty == Difficulty.NORMAL) {
            fighterCounts = 9;
        } else
        if (world.difficulty == Difficulty.HARD) {
            fighterCounts = 12;
        }
        addInventory(f, "GarthogFighter", fighterCounts);
        equipFully(addInventory(f, "GarthogDestroyer", 1));
        // -------------------------------------------------------
        f.task = FleetTask.SCRIPT;
        for (InventoryItem ii : f.inventory.iterable()) {
            ii.tag = "Mission-5-Garthog";
        }
        addScripted(f);
    }
    /**
     * Create helper ship inventory.
     * @param parent the parent fleet
     * @return the list of added items
     */
    Fleet createHelperShips(Fleet parent) {
        Fleet result = new Fleet(world.newId(), player("Pirates"));
        result.x = parent.x;
        result.y = parent.y;
        result.name("");
        // -------------------------------------------------------
        // Set help strength here
        int helpingPirates = 2;
        if (world.difficulty == Difficulty.HARD) {
            helpingPirates = 3;
        }
        String pirateTech = "PirateFighter2";
        // -------------------------------------------------------
        for (int i = 0; i < helpingPirates; i++) {
            InventoryItem pii = new InventoryItem(world.newId(), player("Pirates"), research(pirateTech));
            pii.count = 1;
            pii.init();
            pii.tag = "Mission-5-Help";
            result.inventory.add(pii);
        }

        return result;
    }
    @Override
    public void load(XElement xmission) {
        super.load(xmission);
        reinforcements = xmission.getBoolean("reinforcements", false);
        stage = M5.valueOf(xmission.get("stage", M5.NONE.toString()));
        runTask3Success = xmission.getBoolean("run-task-3-success", false);
    }
    @Override
    public void save(XElement xmission) {
        super.save(xmission);
        xmission.set("reinforcements", reinforcements);
        xmission.set("stage", stage);
        xmission.set("run-task-3-success", runTask3Success);
    }
    @Override
    public void reset() {
        reinforcements = false;
        stage = M5.NONE;
    }
    @Override
    public void onLoaded() {
        super.onLoaded();
        if (runTask3Success) {
            createTask3Success().invoke();
        }
    }
}
