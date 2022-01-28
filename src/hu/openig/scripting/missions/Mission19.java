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
import hu.openig.model.BattleProjectile.Mode;
import hu.openig.model.Chats.Chat;
import hu.openig.model.Chats.Node;
import hu.openig.model.Fleet;
import hu.openig.model.FleetTask;
import hu.openig.model.InventoryItem;
import hu.openig.model.ModelUtils;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;
import hu.openig.model.SoundTarget;
import hu.openig.model.SoundType;
import hu.openig.model.SpacewarScriptResult;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarStructure.StructureType;
import hu.openig.model.SpacewarWorld;
import hu.openig.utils.XElement;

import java.awt.Dimension;

/**
 * Mission 19: blockade of Zeuson.
 * @author akarnokd, 2012.02.23.
 */
public class Mission19 extends Mission {
    /** The governor fleet tag. */
    private static final String MISSION_19_GOVERNOR = "Mission-19-Governor";

    /** Mission stages. */
    enum M19 {
        /** Not started yet. */
        NONE,
        /** Waiting for message. */
        INIT_WAIT,
        /** Fleet appear wait. */
        APPEAR_WAIT,
        /** Chase. */
        RUN,
        /** Done. */
        DONE
    }
    /** The governor's ship type. */
    protected static final String GOVERNOR_SHIP_TYPE = "TradersFreight2";
    /** The mission stages. */
    protected M19 stage = M19.NONE;
    /** Initial hp of the governor fleet. */
    protected int initialHP;
    /** The original relation with the free traders. */
    protected double originalRelation;
    @Override
    public boolean applicable() {
        return world.level == 3;
    }
    @Override
    public void save(XElement xmission) {
        super.save(xmission);
        xmission.set("stage", stage);
        xmission.set("original-relation", originalRelation);
    }
    @Override
    public void load(XElement xmission) {
        super.load(xmission);
        stage = M19.valueOf(xmission.get("stage"));
        originalRelation = xmission.getDouble("original-relation", 50);
    }
    @Override
    public void reset() {
        stage = M19.NONE;
    }
    @Override
    public void onTime() {
        if (stage == M19.NONE) {
            stage = M19.INIT_WAIT;
            addMission("Mission-19", 7 * 24);
        }

        if (checkMission("Mission-19")) {
            world.env.stopMusic();
            world.env.playVideo("interlude/take_prisoner", new Action0() {
                @Override
                public void invoke() {
                    world.env.playMusic();

                    incomingMessage("Douglas-Rebel-Governor", "Mission-19");

                    stage = M19.APPEAR_WAIT;
                    addMission("Mission-19-Appear", 3 * 24);
                }
            });
        }
        if (checkMission("Mission-19-Appear")) {
            stage = M19.RUN;
            createGovernor();
        }
        if (stage == M19.RUN) {
            checkGovernorPosition();
        }
        if (checkTimeout("Mission-19-Hide")) {
            objective("Mission-19").visible = false;
        }
        if (checkTimeout("Mission-19-Failure")) {
            stage = M19.DONE;
            gameover();
            loseGameMessageAndMovie("Douglas-Fire-Battle", "lose/fired_level_3");
        }
    }
    /**
     * Check if the governor left the sector.
     */
    void checkGovernorPosition() {
        Fleet f = findTaggedFleet(MISSION_19_GOVERNOR, freeTraders());
        if (f != null) {
            if (!player.explorationOuterLimit.contains(f.x, f.y)) {
                failMission(f);
            }
        }

    }
    /**
     * Create the governor's fleet.
     */
    void createGovernor() {
        Player ft = freeTraders();
        Planet z = planet("Zeuson");
        Fleet f = createFleet(label("mission-19.unknown"), ft, z.x, z.y);
        // -----------------

        addInventory(f, GOVERNOR_SHIP_TYPE, 1);
        addInventory(f, "Fighter1", 2);

        // -----------------
        tagFleet(f, MISSION_19_GOVERNOR);

        f.moveTo(z.x + 300, z.y);
        f.task = FleetTask.SCRIPT;
        addScripted(f);

        world.env.speed1();
        world.env.playSound(SoundTarget.COMPUTER, SoundType.UNKNOWN_SHIP, null);

        originalRelation = world.establishRelation(player, ft).value;
    }
    /** @return the free trader player. */
    private Player freeTraders() {
        return player("FreeTraders");
    }
    @Override
    public void onFleetAt(Fleet fleet, Planet planet) {
        if (planet.id.equals("Zeuson") && fleet.owner.id.equals("FreeTraders")

                && hasTag(fleet, MISSION_19_GOVERNOR)) {
            world.removeFleet(fleet);
            removeScripted(fleet);
        }
    }
    @Override
    public void onSpacewarStart(SpacewarWorld war) {
        BattleInfo battle = war.battle();
        if (stage == M19.RUN && isMissionSpacewar(battle, "Mission-19")) {
            initialHP = 0;
            for (SpacewarStructure s : war.structures(freeTraders())) {
                initialHP += s.hp;
            }
            battle.chat = "chat.mission-19.rebel.Zeuson.governor";

            // leaving planet check

            if (battle.helperPlanet != null && battle.helperPlanet.owner == battle.attacker.owner) {
                if (battle.helperPlanet.id.equals("Zeuson")) {

                    Dimension d = war.space();
                    for (SpacewarStructure s : war.structures()) {
                        if (s.type == StructureType.SHIP) {
                            s.x = d.width - s.x - 100;
                            s.angle -= Math.PI;
                        }
                    }

                    battle.invert = true;
                }
            }

        }
    }
    @Override
    public SpacewarScriptResult onSpacewarStep(SpacewarWorld war) {
        if (stage == M19.RUN && isMissionSpacewar(war.battle(), "Mission-19")) {
            int currentHP = 0;
            for (SpacewarStructure s : war.structures(freeTraders())) {
                currentHP += s.hp;
            }
            if (currentHP * 2 < initialHP) {
                for (SpacewarStructure s : war.structures(freeTraders())) {
                    war.flee(s);
                }

                war.battle().enemyFlee = true;
            } else {
                for (SpacewarStructure s : war.structures(freeTraders())) {
                    if (s.attack == null) {
                        war.move(s, Math.cos(s.angle) * 1000, s.y);
                    }
                }
            }
            return SpacewarScriptResult.CONTINUE;
        }
        return null;
    }

    @Override
    public void onSpacewarFinish(SpacewarWorld war) {
        BattleInfo battle = war.battle();
        if (stage == M19.RUN && isMissionSpacewar(battle, "Mission-19")) {
            battleFinish(battle);
        }
    }
    /**
     * Battle finished.
     * @param battle the battle
     */
    void battleFinish(BattleInfo battle) {
        Fleet f = findTaggedFleet(MISSION_19_GOVERNOR, freeTraders());
        if (f != null) {
            ResearchType gr = research(GOVERNOR_SHIP_TYPE);
            InventoryItem ii = f.getInventoryItem(gr);
            if (ii != null && battle.enemyFlee) {
                // governor survived
                f.moveTo(planet("Zeuson"));
                f.task = FleetTask.SCRIPT;

                setObjectiveState("Mission-19", ObjectiveState.SUCCESS);
                addTimeout("Mission-19-Hide", 13000);

                battle.messageText = label("battlefinish.mission-19.31");
                battle.rewardImage = "battlefinish/mission_31";

                world.establishRelation(player, ii.owner).value = originalRelation;

                stage = M19.DONE;

                return;
            } else
            if (ii == null) {
                failMission(f);
            }
        } else {
            failMission(null);
        }
        cleanupScriptedFleets();
    }
    @Override
    public void onAutobattleFinish(BattleInfo battle) {
        if (stage == M19.RUN && isMissionSpacewar(battle, "Mission-19")) {
            battleFinish(battle);
        }
    }

    /**
     * Fail the mission.
     * @param f the governor's fleet
     */
    void failMission(Fleet f) {
        stage = M19.DONE;
        if (f != null) {
            world.removeFleet(f);
            removeScripted(f);
        }
        setObjectiveState("Mission-19", ObjectiveState.FAILURE);
        addTimeout("Mission-19-Failure", 13000);
    }
    @Override
    public void onSpaceChat(SpacewarWorld world, Chat chat, Node node) {
        if (stage == M19.RUN

                && "chat.mission-19.rebel.Zeuson.governor".equals(chat.id)

                && node != null

                && "7".equals(node.id)) {
            Player pirates = freeTraders();
            for (SpacewarStructure s : world.structures(pirates)) {
                if (s.item != null && MISSION_19_GOVERNOR.equals(s.item.tag)
                        && s.item.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
                    world.attack(s, ModelUtils.random(world.structures(player)), Mode.BEAM);
                }
            }

        }
    }
}
