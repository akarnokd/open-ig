/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Difficulty;
import hu.openig.model.BattleInfo;
import hu.openig.model.DiplomaticRelation;
import hu.openig.model.Fleet;
import hu.openig.model.FleetTask;
import hu.openig.model.InventoryItem;
import hu.openig.model.ModelUtils;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarWorld;
import hu.openig.utils.XElement;

import java.util.List;

/**
 * Mission 7: defend 2 traders from Garthog pirates.
 * @author akarnokd, 2012.01.19.
 *
 */
public class Mission7 extends Mission {
    /** Stages. */
    enum M7 {
        /** Not started. */
        NONE,
        /** Wait. */
        WAIT_1,
        /** Running. */
        RUN_1,
        /** Done. */
        DONE_1,
        /** Wait. */
        WAIT_2,
        /** Running. */
        RUN_2,
        /** Done. */
        DONE_2
    }
    /** The current stage. */
    M7 stage = M7.NONE;
    @Override
    public boolean applicable() {
        return world.level == 2;
    }
    @Override
    public void onTime() {
        // a week after the initial garthog attack
        Objective m6 = objective("Mission-6");
        Objective m7 = objective("Mission-7");
        Objective m7t1 = objective("Mission-7-Task-1");
        Objective m7t2 = objective("Mission-7-Task-2");
        if (m6.isCompleted() && !m7.isCompleted() && !m7t1.isCompleted() && stage == M7.NONE) {
            addMission("Mission-7", 7 * 24);
            stage = M7.WAIT_1;
        }
        // a week after the first virus attack
        Objective m12t1 = objective("Mission-12-Task-1");
        if (m12t1.isCompleted() && !m7t2.isCompleted() && stage == M7.DONE_1) {
            stage = M7.WAIT_2;
            addMission("Mission-7-Task-2", 6 * 24 + 12);
        }

        if (checkMission("Mission-7")) {
            showObjective("Mission-7");
            addMission("Mission-7-Task-1", 24);
        }
        if (checkMission("Mission-7-Task-1")) {
            stage = M7.RUN_1;
            checkStartTask(1);
        }
        if (checkMission("Mission-7-Task-2")) {
            stage = M7.RUN_2;
            checkStartTask(2);
        }
        for (int i = 1; i <= 2; i++) {
            if (checkTimeout("Mission-7-Task-" + i + "-Success")) {
                setObjectiveState("Mission-7-Task-" + i, ObjectiveState.SUCCESS);
                if (i == 2) {
                    setObjectiveState("Mission-7", ObjectiveState.SUCCESS);
                    DiplomaticRelation dr = world.establishRelation(player, player("FreeTraders"));
                    dr.value = 75;
                } else {
                    int reward = 5000;
                    player.addMoney(reward);
                    player.statistics.moneyIncome.value += reward;
                }
                addTimeout("Mission-7-Hide", 13000);
                stage = i == 1 ? M7.DONE_1 : M7.DONE_2;
            }
            if (checkTimeout("Mission-7-Task-" + i + "-Failed")) {
                setObjectiveState("Mission-7-Task-" + i, ObjectiveState.FAILURE);
                setObjectiveState("Mission-7", ObjectiveState.FAILURE);
                addTimeout("Mission-7-Hide", 13000);
                addTimeout("Mission-7-Fire", 16000);
                removeFleets();
                stage = i == 1 ? M7.DONE_1 : M7.DONE_2;
            }
            if (checkMission("Mission-7-Task-" + i + "-Timeout")) {
                setObjectiveState("Mission-7-Task-" + i, ObjectiveState.FAILURE);
                setObjectiveState("Mission-7", ObjectiveState.FAILURE);
                addTimeout("Mission-7-Hide", 13000);
                addTimeout("Mission-7-Fire", 16000);
                removeFleets();
                stage = i == 1 ? M7.DONE_1 : M7.DONE_2;
            }
        }
        if (checkTimeout("Mission-7-Hide")) {
            objective("Mission-7").visible = false;
        }
        if (checkTimeout("Mission-7-Fire")) {
            gameover();
            loseGameMessageAndMovie("Douglas-Fire-Lost-Merchants", "lose/fired_level_2");
        }
    }
    /** Remove the attacker fleets. */
    void removeFleets() {
        Fleet merchant = findTaggedFleet("Mission-7-Trader", player("Traders"));
        if (merchant != null) {
            removeScripted(merchant);
            world.removeFleet(merchant);
        }
        Fleet g = findTaggedFleet("Mission-7-Garthog", player("Garthog"));
        if (g != null) {
            removeScripted(g);
            world.removeFleet(g);
        }
    }
    /**
     * Check if the traders are in position to start the task.

     * @param task the task index
     * @return true if can start
     */
    boolean checkStartTask(int task) {
        List<Fleet> fs = findVisibleFleets(player, false, player("Traders"), 4);
        fs = filterByRange(fs, world.params().groundRadarUnitSize() - 2,

                "Naxos", "San Sterling", "Achilles", "Centronom", "New Caroline");
        String m7ti = "Mission-7-Task-" + task;
        String m7tio = "Mission-7-Task-" + task + "-Timeout";
        if (!fs.isEmpty()) {
            incomingMessage("Merchant-Under-Attack-Garthog", m7ti);
            objective("Mission-7").visible = true;

            world.env.speed1();
            Fleet f = ModelUtils.random(fs);
            f.stop();
            f.task = FleetTask.SCRIPT;
            for (InventoryItem ii : f.inventory.iterable()) {
                ii.tag = "Mission-7-Trader";
            }

            // create simple pirate fleet
            Fleet pf = createFleet(label("Garthog.pirates"), player("Garthog"), f.x + 1, f.y + 1);
            pf.task = FleetTask.SCRIPT;

            // ----------------------------------------------------------------
            // adjust fleet strength here
            if (task == 1) {
                if (world.difficulty == Difficulty.HARD) {
                    addInventory(pf, "GarthogFighter", 8);
                    equipFully(addInventory(pf, "GarthogDestroyer", 2));
                } else
                if (world.difficulty == Difficulty.NORMAL) {
                    addInventory(pf, "GarthogFighter", 10);
                    equipFully(addInventory(pf, "GarthogDestroyer", 1));
                } else {
                    addInventory(pf, "GarthogFighter", 5);
                    equipFully(addInventory(pf, "GarthogDestroyer", 1));
                }
            } else {
                if (world.difficulty == Difficulty.HARD) {
                    addInventory(pf, "GarthogFighter", 15);
                    equipFully(addInventory(pf, "GarthogDestroyer", 3));
                } else
                if (world.difficulty == Difficulty.NORMAL) {
                    addInventory(pf, "GarthogFighter", 12);
                    equipFully(addInventory(pf, "GarthogDestroyer", 2));

                } else {
                    addInventory(pf, "GarthogFighter", 8);
                    equipFully(addInventory(pf, "GarthogDestroyer", 2));
                }
            }
            for (InventoryItem ii : pf.inventory.iterable()) {
                ii.tag = "Mission-7-Garthog";
            }

            // ----------------------------------------------------------------

            addScripted(f);
            addScripted(pf);

            // set failure timeout
            addMission(m7tio, 24);
            return true;
        }
        // schedule again
        addMission(m7ti, 0);
        return false;
    }
    /**
     * Ensure that the given list of fleets contains only elements
     * which are within the given radar range of the listed planet ids.
     * @param fs the fleet list
     * @param r the radar range in pixels
     * @param planets the planets to check
     * @return fs parameter
     */
    List<Fleet> filterByRange(List<Fleet> fs, int r,
            String... planets) {
        outer:
        for (int i = fs.size() - 1; i >= 0; i--) {
            Fleet f = fs.get(i);
            for (String p : planets) {
                Planet op = planet(p);
                double d = Math.hypot(f.x - op.x, f.y - op.y);
                if (d <= r) {
                    continue outer;
                }
            }
            fs.remove(i);
        }
        return fs;
    }
    /**
     * Handle the spacewar between a trader and a pirate.

     * @param war the war context
     * @param task the task index
     */
    void spacewarStartTraderVsPirate(SpacewarWorld war, int task) {
        String missionTag = "Mission-7-Task-" + task;
        if (isMissionSpacewar(war.battle(), missionTag)) {
            BattleInfo battle = war.battle();
            Player traders = player("Traders");
            Player pirates = player("Garthog");

            if (startJointSpaceBattle(war, "Mission-7-Trader", traders, "Mission-7-Garthog", pirates)) {
                battle.chat = "chat.mission-7.defend.merchant" + task;
                battle.tag = missionTag;
            }
        }
    }
    /**
     * Finish the war for the trader vs pirate missions.
     * @param war the war context
     * @param task the task index
     */
    void spacewarFinishTraderVsPirate(SpacewarWorld war, int task) {
        String missionTag = "Mission-7-Task-" + task;
        if (missionTag.equals(war.battle().tag)) {
            // find the status of the trader ship
            Player traders = player("Traders");
            List<SpacewarStructure> sts = war.structures(traders);
            boolean traderSurvived = !sts.isEmpty();
            completeTaskN(traderSurvived, task);
            if (traderSurvived) {
                if (task == 1) {
                    war.battle().messageText = label("battlefinish.mission-7.merchant7");
                    war.battle().rewardText = format("mission-7.save_trader.reward", 5000);
                    war.battle().rewardImage = "battlefinish/mission_1_8d";
                } else {
                    war.battle().messageText = label("battlefinish.mission-7_task2.merchant8");
                    war.battle().rewardText = label("battlefinish.mission-7_task2.merchant8_bonus");
                }
            }
        }
    }
    /**
     * Issue the specific mission changes once task is completed.
     * @param traderSurvived did the trader survive?
     * @param task the current task id
     */
    void completeTaskN(boolean traderSurvived, int task) {
        Player traders = player("Traders");
        Player pirates = player("Garthog");
        if (!traderSurvived) {
            addTimeout("Mission-7-Task-" + task + "-Failed", 3000);
            if (hasMission("Mission-7-Task-" + task + "-Timeout")) {
                clearMission("Mission-7-Task-" + task + "-Timeout");
            }
        } else {
            addTimeout("Mission-7-Task-" + task + "-Success", 3000);
            if (hasMission("Mission-7-Task-" + task + "-Timeout")) {
                clearMission("Mission-7-Task-" + task + "-Timeout");
            }
        }
        cleanupScriptedFleets();

        Fleet merchant = findTaggedFleet("Mission-7-Trader", traders);
        if (merchant != null) {
            removeScripted(merchant);

            // fix owner
            for (InventoryItem ii : merchant.inventory.iterable()) {
                ii.tag = null;
            }
            merchant.moveTo(ModelUtils.random(
                    planet("Achilles"), planet("Naxos"), planet("San Sterling"),
                    planet("Centronom"), planet("New Caroline")));
            merchant.task = FleetTask.MOVE;
        }
        Fleet g = findTaggedFleet("Mission-7-Garthog", pirates);
        if (g != null) {
            removeScripted(g);
            world.removeFleet(g);
        }
    }
    @Override
    public void onSpacewarFinish(SpacewarWorld war) {
        for (int i = 1; i <= 2; i++) {
            spacewarFinishTraderVsPirate(war, i);
        }
    }
    @Override
    public void onSpacewarStart(SpacewarWorld war) {
        for (int i = 1; i <= 2; i++) {
            spacewarStartTraderVsPirate(war, i);
        }
    }
    @Override
    public void onAutobattleFinish(BattleInfo battle) {
        for (int task = 1; task <= 2; task++) {
            if (("Mission-7-Task-" + task).equals(battle.tag)) {
                Player trader = player("Traders");
                boolean traderSurvived = false;
                for (InventoryItem ii : battle.attacker.inventory.list()) {
                    if (ii.owner == trader) {
                        traderSurvived = true;
                        battle.attacker.inventory.remove(ii);
                    }
                }
                completeTaskN(traderSurvived, task);
            }
        }
    }
    @Override
    public void onAutobattleStart(BattleInfo battle) {
        for (int task = 1; task <= 2; task++) {
            String missionTag = "Mission-7-Task-" + task;
            if (isMissionSpacewar(battle, missionTag)) {
                battle.tag = missionTag;
                Player traders = player("Traders");
                Player pirates = player("Garthog");
                startJointAutoSpaceBattle(battle, "Mission-7-Trader", traders, "Mission-7-Garthog", pirates);
            }
        }
    }
    @Override
    public boolean fleetBlink(Fleet f) {
        return f.task == FleetTask.SCRIPT && f.owner == player("Traders");
    }
    @Override
    public void load(XElement xmission) {
        super.load(xmission);
        stage = M7.valueOf(xmission.get("stage", M7.NONE.toString()));
    }
    @Override
    public void save(XElement xmission) {
        super.save(xmission);
        xmission.set("stage", stage);
    }
    @Override
    public void reset() {
        stage = M7.NONE;
    }
}
