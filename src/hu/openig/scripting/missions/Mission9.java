/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.mechanics.AITrader;
import hu.openig.model.BattleInfo;
import hu.openig.model.Fleet;
import hu.openig.model.FleetMode;
import hu.openig.model.FleetTask;
import hu.openig.model.InventoryItem;
import hu.openig.model.ModelUtils;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchMainCategory;
import hu.openig.model.ResearchType;
import hu.openig.model.SoundTarget;
import hu.openig.model.SoundType;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarWorld;
import hu.openig.utils.XElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Mission 9: deal with the with the San Sterling smuggler.
 * @author akarnokd, Jan 22, 2012
 */
public class Mission9 extends Mission {
    /** The stages. */
    enum M9Stages {
        /** None. */
        NONE,
        /** Wait for intro. */
        WAIT,
        /** Running. */
        RUN,
        /** Done. */
        DONE
    }
    /** The current stage. */
    M9Stages stage = M9Stages.NONE;
    /** Flag to indicate the battle started by attacking the smuggler. */
    boolean smugglerAttacked;
    @Override
    public boolean applicable() {
        return world.level == 2;
    }
    @Override
    public void onTime() {
        Objective m7t1 = objective("Mission-7-Task-1");
        if (m7t1.isCompleted() && stage == M9Stages.NONE) {
            addMission("Mission-9", 3 * 24);
            stage = M9Stages.WAIT;
        }
        if (checkMission("Mission-9")) {
            incomingMessage("San Sterling-Smuggler", "Mission-9");
            stage = M9Stages.RUN;

            addMission("Mission-9-Trader-1", 3);
        }

        if (checkMission("Mission-9-Trader-1")) {
            createTrader("Centronom");
            addMission("Mission-9-Trader-2", 6);
        }
        if (checkMission("Mission-9-Trader-2")) {
            createTrader("Achilles");
            addMission("Mission-9-Trader-3", 12);
        }
        if (checkMission("Mission-9-Trader-3")) {
            createTrader("Naxos");
            createSmuggler();
        }

        if (checkTimeout("Mission-9-Slipped")) {
            setObjectiveState("Mission-9", ObjectiveState.FAILURE);
            incomingMessage("San Sterling-Smuggler-Escaped");
            addTimeout("Mission-9-Hide", 30000);
        }
        if (checkTimeout("Mission-9-Innocent")) {
            setObjectiveState("Mission-9", ObjectiveState.FAILURE);
            gameover();
            loseGameMessageAndMovie("San Sterling-Smuggler-Killed-Innocent", "lose/fired_level_2");
        }
        if (checkTimeout("Mission-9-Killed")) {
            setObjectiveState("Mission-9", ObjectiveState.SUCCESS);
            incomingMessage("San Sterling-Smuggler-Killed");
            addTimeout("Mission-9-Hide", 30000);
        }
        if (checkTimeout("Mission-9-Success")) {
            setObjectiveState("Mission-9", ObjectiveState.SUCCESS);
            addTimeout("Mission-9-Hide", 13000);
            addMission("Mission-9-Hide-Fleet", 12);
            incomingMessage("Douglas-Success");
        }
        if (checkTimeout("Mission-9-Hide")) {
            objective("Mission-9").visible = false;
        }
        if (checkMission("Mission-9-Hide-Fleet")) {
            Fleet smg = findTaggedFleet("Mission-9-Smuggler", player("Traders"));
            if (smg != null) {
                world.removeFleet(smg);
                removeScripted(smg);
            }
        }
    }
    /**
     * Create a trader at the given planet.
     * @param planet the planet
     */
    void createTrader(String planet) {
        Player tr = player("Traders");
        Planet pl = planet(planet);

        Fleet f0 = createFleet(label("traders.fleetname"),

                tr, pl.x, pl.y);

        List<ResearchType> shipTypes = new ArrayList<>();
        for (ResearchType rt : world.researches.values()) {
            if (rt.race.contains("traders") && rt.category.main == ResearchMainCategory.SPACESHIPS) {
                shipTypes.add(rt);
            }
        }

        ResearchType rt0 = ModelUtils.random(shipTypes);
        addInventory(f0, rt0.id, 1);

        f0.moveTo(planet("San Sterling"));

        ((AITrader)tr.ai).setLastVisited(f0, pl);
    }
    /** Create the smuggler's ship. */
    void createSmuggler() {
        Player tr = player("Traders");
        Planet sst = planet("San Sterling");
        // create simple pirate fleet
        Fleet pf = createFleet(label("mission-9.trader_name"),

                tr, sst.x + 80, sst.y + 80);
        pf.task = FleetTask.SCRIPT;
        int n = ModelUtils.randomInt(2) + 1;
        // ----------------------------------------------------------------
        addInventory(pf, "TradersFreight" + n, 1);
        // ----------------------------------------------------------------
        for (InventoryItem ii : pf.inventory.iterable()) {
            ii.tag = "Mission-9-Smuggler";
        }
        pf.mode = FleetMode.MOVE;
        pf.targetPlanet(sst);

        addScripted(pf);

        ((AITrader)tr.ai).setLastVisited(pf, planet("Exterior 21"));
    }
    @Override
    public void onSpacewarStart(SpacewarWorld war) {
        Fleet attacker = war.battle().attacker;
        Fleet targetFleet = war.battle().targetFleet;
        if (stage == M9Stages.RUN
                && attacker.owner == player

                && targetFleet != null
                && targetFleet.owner.id.equals("Traders")) {
            if (hasTag(targetFleet, "Mission-9-Smuggler")) {
                war.battle().chat = "chat.mission-9.smuggler";
                smugglerAttacked = true;
            } else
            if (targetFleet.targetPlanet() == planet("San Sterling")
            || targetFleet.arrivedAt == planet("San Sterling")) {
                String filter = "chat.blockade.incoming";

                AITrader ai = (AITrader)player("Traders").ai;

                List<String> chats = ai.filterChats(filter);
                int idx = ai.fleetIndex(targetFleet);
                int comm = idx % chats.size();

                war.battle().chat = chats.get(comm);
            }
        }
    }
    @Override
    public void onDiscovered(Player player, Fleet fleet) {
        if (stage == M9Stages.RUN && player == this.player && hasTag(fleet, "Mission-9-Smuggler")) {
            world.env.playSound(SoundTarget.COMPUTER, SoundType.UNKNOWN_SHIP, null);
            world.env.speed1();
        }
    }
    @Override
    public void onAutobattleStart(BattleInfo battle) {
        if (stage == M9Stages.RUN
                && battle.attacker.owner == player

                && battle.targetFleet != null
                && hasTag(battle.targetFleet, "Mission-9-Smuggler")) {
            smugglerAttacked = true;
        }
    }
    @Override
    public void onSpacewarFinish(SpacewarWorld war) {
        if (stage == M9Stages.RUN

                && war.battle().attacker.owner == player

                && war.battle().targetFleet != null) {

            Player traders = player("Traders");
            // check for losses
            boolean smugglerKilled = false;
            boolean innocentKilled = false;
            boolean diverted = false;
            boolean slipped = false;

            for (SpacewarStructure s : war.battle().spaceLosses) {
                if (s.item != null && "Mission-9-Smuggler".equals(s.item.tag)) {
                    smugglerKilled = smugglerAttacked;
                } else
                if (s.owner == traders) {
                    innocentKilled = true;
                }
            }

            Fleet smg = findTaggedFleet("Mission-9-Smuggler", traders);
            if (smg != null) {
                if (war.battle().enemyFlee) {
                    diverted = true;
                } else {
                    slipped = true;
                }
            }

            if (smugglerAttacked) {
                Planet sst = planet("San Sterling");
                if (smugglerKilled) {
                    addTimeout("Mission-9-Killed", 3000);
                    stage = M9Stages.DONE;
                } else
                if (innocentKilled) {
                    addTimeout("Mission-9-Innocent", 3000);
                    stage = M9Stages.DONE;
                } else
                if (diverted) {
                    addTimeout("Mission-9-Success", 3000);
                    war.battle().messageText = label("battlefinish.mission-9.17");

                    smg.moveTo(sst.x + 80, sst.y + 80);
                    smg.task = FleetTask.SCRIPT;
                    stage = M9Stages.DONE;
                } else
                if (slipped) {
                    // resume flight
                    smg.targetPlanet(sst);
                    smg.task = FleetTask.SCRIPT;
                }
            }
        }
    }
    @Override
    public void onFleetAt(Fleet fleet, Planet planet) {
        if (stage == M9Stages.RUN) {
            if (planet.id.equals("San Sterling")

                    && hasTag(fleet, "Mission-9-Smuggler")) {
                world.removeFleet(fleet);
                removeScripted(fleet);
                setObjectiveState("Mission-9", ObjectiveState.FAILURE);
                addTimeout("Mission-9-Slipped", 13000);
                stage = M9Stages.DONE;
            }
        }
    }
    @Override
    public void onAutobattleFinish(BattleInfo battle) {
        if (stage == M9Stages.RUN && isMissionSpacewar(battle, "Mission-9")) {
            Player traders = player("Traders");
            if (battle.targetFleet != null

                    && battle.targetFleet.owner == traders) {
                Fleet smg = findTaggedFleet("Mission-9-Smuggler",

                        traders);
                if (smg == null && smugglerAttacked) {
                    addTimeout("Mission-9-Killed", 3000);
                    stage = M9Stages.DONE;
                } else

                if (battle.targetFleet.inventory.isEmpty()) {
                    addTimeout("Mission-9-Innocent", 3000);
                    stage = M9Stages.DONE;
                }
            }
        }
    }
    @Override
    public boolean fleetBlink(Fleet f) {
        return objective("Mission-9").isActive()
                && f.owner == player("Traders")
                && (hasTag(f, "Mission-9-Smuggler")
                || f.targetPlanet() == planet("San Sterling"));
    }
    @Override
    public void save(XElement xmission) {
        super.save(xmission);
        xmission.set("stage", stage);
    }
    @Override
    public void load(XElement xmission) {
        super.load(xmission);
        stage = M9Stages.valueOf(xmission.get("stage", M9Stages.NONE.toString()));
    }
}
