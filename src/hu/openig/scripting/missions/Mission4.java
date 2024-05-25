/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.model.AIMode;
import hu.openig.model.BattleInfo;
import hu.openig.model.BattleProjectile.Mode;
import hu.openig.model.Chats.Chat;
import hu.openig.model.Chats.Node;
import hu.openig.model.DiplomaticRelation;
import hu.openig.model.Fleet;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.FleetMode;
import hu.openig.model.FleetTask;
import hu.openig.model.InventoryItem;
import hu.openig.model.ModelUtils;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarWorld;
import hu.openig.utils.XElement;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.EnumSet;
import java.util.List;

/**
 * Mission 4: Resolve pirate battle.
 * @author akarnokd, 2012.01.18.
 */
public class Mission4 extends Mission {
    /** The second pirates of two ships. */
    private static final String MISSION_4_PIRATES_2 = "Mission-4-Pirates-2";
    /** The mission stages. */
    enum M4 {
        /** Not started. */
        NONE,
        /** Wait for start. */
        WAIT,
        /** Running. */
        RUN,
        /** Finished. */
        DONE
    }
    /** The current stage. */
    M4 stage = M4.NONE;
    @Override
    public boolean applicable() {
        return world.level == 1;
    }
    @Override
    public void onTime() {
        Objective m2t2 = objective("Mission-2-Task-2");
        Objective m4 = objective("Mission-4");
        if (!m4.isCompleted() && m2t2.isCompleted() && stage == M4.NONE) {
            addMission("Mission-4", 8); // FIXME timing
            stage = M4.WAIT;
        }
        if (checkMission("Mission-4")) {
            world.env.speed1();
            send("Naxos-Not-Under-Attack").visible = false;
            incomingMessage("Naxos-Unknown-Ships", "Mission-4");
            addMission("Mission-4-Timeout", 24);
            createPirateTask();
            stage = M4.RUN;
        }

        if (checkMission("Mission-4-Timeout")) {

            send("Naxos-Not-Under-Attack").visible = true;
            setObjectiveState("Mission-4", ObjectiveState.FAILURE);
            addTimeout("Mission-4-Fire", 13000);

            removeFleets();
            stage = M4.DONE;
        }
        if (checkTimeout("Mission-4-Fire")) {
            gameover();
            loseGameMessageAndMovie("Douglas-Fire-No-Order", "lose/fired_level_1");
        }
        if (checkTimeout("Mission-4-Success")) {
            send("Naxos-Not-Under-Attack").visible = true;

            setObjectiveState("Mission-4", ObjectiveState.SUCCESS);
            addTimeout("Mission-4-Done", 13000);
            stage = M4.DONE;
        }
        if (checkTimeout("Mission-4-Done")) {
            objective("Mission-4").visible = false;
        }
    }
    /**
     * Remove the fleets involved in the situation.
     */
    void removeFleets() {
        Player pirates = player("Pirates");
        Fleet fi = findTaggedFleet("Mission-4-Pirates-1", pirates);
        if (fi != null) {
            world.removeFleet(fi);
            removeScripted(fi);
        }

        fi = findTaggedFleet(MISSION_4_PIRATES_2, pirates);
        if (fi != null) {
            world.removeFleet(fi);
            removeScripted(fi);
        }
        cleanupScriptedFleets();
    }
    /**
     * Create a carrier moving across the screen.
     */
    void createPirateTask() {
        Planet naxos = planet("Naxos");
        Player pirate = player("Pirates");
        Fleet f = createFleet(label("pirates.fleet_name"), pirate, naxos.x + 15, naxos.y - 5);
        addInventory(f, "PirateFighter2", 1);
        for (InventoryItem ii : f.inventory.iterable()) {
            ii.tag = "Mission-4-Pirates-1";
        }
        f.task = FleetTask.SCRIPT;
        addScripted(f);

        f = createFleet(label("pirates.fleet_name"), pirate, naxos.x + 17, naxos.y - 3);
        addInventory(f, "PirateFighter", 2);
        for (InventoryItem ii : f.inventory.iterable()) {
            ii.tag = MISSION_4_PIRATES_2;
        }
        f.task = FleetTask.SCRIPT;
        addScripted(f);

    }
    /**
     * Set the target for the carrier fleet.
     * @param f the fleet
     */
    void moveToDestination(Fleet f) {
        Planet sansterling = planet("San Sterling");
        f.waypoints.clear();
        f.mode = FleetMode.MOVE;
        f.task = FleetTask.SCRIPT;
        f.waypoints.add(new Point2D.Double(sansterling.x - 20, sansterling.y - 40));
    }
    /**
     * Issue the specific mission changes once task is completed.
     * @param pirateSurvived did the pirate survive?
     */
    void completeMission(boolean pirateSurvived) {
        addTimeout("Mission-4-Success", 3000);
        clearMission("Mission-4-Timeout");
        if (pirateSurvived) {
            // indicate that we helped the pirate
            addMission("Mission-4-Helped", 1);
            world.achievement("achievement.a_pirate_in_need");
        }
        removeFleets();
        // make sure the temporary Pirates2 player has no relations left.
        for (int i = world.relations.size() - 1; i >= 0; i--) {
            DiplomaticRelation r = world.relations.get(i);
            if (r.first.id.equals("Pirates2") || r.second.id.equals("Pirates2")) {
                world.relations.remove(i);
            }
        }
    }
    @Override
    public void onAutobattleFinish(BattleInfo battle) {
        if (isMissionSpacewar(battle, "Mission-4")) {
            boolean pirateSurvived = false;
            for (InventoryItem ii : battle.attacker.inventory.list()) {
                if ("Mission-4-Pirates-1".equals(ii.tag)) {
                    pirateSurvived = true;
                    battle.attacker.inventory.remove(ii);
                }
            }
            completeMission(pirateSurvived);
        }
    }
    @Override
    public void onAutobattleStart(BattleInfo battle) {
        if (isMissionSpacewar(battle, "Mission-4")) {
            Player pirates = player("Pirates");
            startJointAutoSpaceBattle(battle, "Mission-4-Pirates-1", pirates, MISSION_4_PIRATES_2, pirates);
        }
    }
    @Override
    public void onSpacewarStart(SpacewarWorld war) {
        if (isMissionSpacewar(war.battle(), "Mission-4")) {
            BattleInfo battle = war.battle();
            Player pirates = player("Pirates");
            Fleet f1 = findTaggedFleet("Mission-4-Pirates-1", pirates);
            Fleet f2 = findTaggedFleet(MISSION_4_PIRATES_2, pirates);

            if (battle.targetFleet != null && (battle.targetFleet == f1 || battle.targetFleet == f2)) {
                Player pirate2 = createSecondPirate();
                // pirate 1 attacked
                if (battle.targetFleet == f1) {
                    war.includeFleet(f2, f2.owner);
                    battle.targetFleet = f2;
                    battle.otherFleets.add(f1);
                } else {
                    // pirate 2 attacked
                    war.addStructures(f1, EnumSet.of(
                            ResearchSubCategory.SPACESHIPS_BATTLESHIPS,
                            ResearchSubCategory.SPACESHIPS_CRUISERS,
                            ResearchSubCategory.SPACESHIPS_FIGHTERS));
                }

                battle.attackerAllies.add(pirate2);
                // center pirate
                Dimension d = war.space();
                List<SpacewarStructure> structures = war.structures();
                SpacewarStructure a = null;
                for (SpacewarStructure s : structures) {
                    if (s.item != null && "Mission-4-Pirates-1".equals(s.item.tag)) {
                        s.x = d.width / 2d;
                        s.y = d.height / 2d;
                        war.alignToNearestCell(s);
                        war.addUnitLocation(s);
                        s.angle = 0.0;
                        s.owner = pirate2;
                        a = s;
                        s.guard = true;
                    }
                }
                for (SpacewarStructure s : war.structures(pirates)) {
                    s.attackUnit = a;
                }
                battle.allowRetreat = false;
                battle.chat = "chat.mission-4.piratesfight.at.Naxos";
            }
        }
    }
    /**
     * Create a temporary second pirate player to avoid
     * the same-owner issue in space battle.
     * @return f2 the patched fleet
     */
    Player createSecondPirate() {
        Player newOwner = new Player(world, "Pirates2");
        Player owner = player("Pirates");
        newOwner.name = owner.name;
        newOwner.color = owner.color;
        newOwner.shortName = owner.shortName;
        newOwner.fleetIcon = owner.fleetIcon;
        newOwner.race = owner.race;
        newOwner.aiMode = AIMode.PIRATES;
        newOwner.ai = world.env.getAI(newOwner);
        newOwner.ai.init(newOwner);
        newOwner.noDatabase = owner.noDatabase;
        newOwner.noDiplomacy = owner.noDiplomacy;

        return newOwner;
    }
    @Override
    public void onSpacewarFinish(SpacewarWorld war) {
        if (isMissionSpacewar(war.battle(), "Mission-4")) {
            // find the status of the trader ship
            boolean pirateSurvived = false;
            for (SpacewarStructure s : war.structures()) {
                if (s.item != null && "Mission-4-Pirates-1".equals(s.item.tag)) {
                    Player pirates = player("Pirates");
                    pirates.fleets.put(s.fleet, FleetKnowledge.FULL);
                    pirateSurvived = true;
                    break;
                }
            }
            completeMission(pirateSurvived);
            if (pirateSurvived) {
                war.battle().messageText = label("battlefinish.mission-4.10_bonus");
            } else {
                war.battle().messageText = label("battlefinish.mission-4.10");
            }
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
        stage = M4.valueOf(xmission.get("stage", M4.NONE.toString()));
    }
    @Override
    public void reset() {
        stage = M4.NONE;
    }
    @Override
    public void onSpaceChat(SpacewarWorld world, Chat chat, Node node) {
        if (stage == M4.RUN

                && "chat.mission-4.piratesfight.at.Naxos".equals(chat.id)
                && node != null && "2".equals(node.id)) {
            Player pirates = player("Pirates");
            for (SpacewarStructure s : world.structures(pirates)) {
                if (s.item != null && MISSION_4_PIRATES_2.equals(s.item.tag)) {
                    world.attack(s, ModelUtils.random(world.structures(player)), Mode.BEAM);
                }
            }
        }
    }
}
