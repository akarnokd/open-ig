/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Action0;
import hu.openig.model.ObjectiveState;

/**
 * Mission 15: see spy, talk to Colonel, talk to kelly.
 * @author akarnokd, 2012.01.18.
 */
public class Mission15 extends Mission {
    @Override
    public boolean applicable() {
        return world.level == 2;
    }
    @Override
    public void onTime() {
        if (checkMission("Mission-15")) {
            world.env.stopMusic();
            world.env.playVideo("interlude/spy_on_johnson", new Action0() {
                @Override
                public void invoke() {
                    world.currentTalk = "kelly";
                    showObjective("Mission-15");
                    showObjective("Mission-15-Task-1");
                    addMission("Mission-15-Task-1-Timeout", 7 * 24);
                    world.env.playMusic();
                }
            });
        }
        if (checkMission("Mission-15-Morning")) {
            world.messageRecording = false;
            world.env.stopMusic();
            world.env.playVideo("interlude/watch_recording", new Action0() {
                @Override
                public void invoke() {
                    world.allowRecordMessage = false;
                    // record watched in time

                    send("Douglas-Report-Spy").visible = true;

                    showObjective("Mission-15-Task-3");
                    addMission("Mission-15-Task-3-Timeout", 7 * 24);
                    world.env.playMusic();
                }
            });
        }
        // TIMEOUTS ---------------------------------------------------------------
        if (checkMission("Mission-15-Task-1-Timeout")) {
            // record not taken in time
            setObjectiveState("Mission-15-Task-1", ObjectiveState.FAILURE);
            setObjectiveState("Mission-15", ObjectiveState.FAILURE);
            world.currentTalk = null;
        }
        if (checkMission("Mission-15-Task-2-Timeout")) {
            // record not taken in time
            setObjectiveState("Mission-15-Task-2", ObjectiveState.FAILURE);
            setObjectiveState("Mission-15", ObjectiveState.FAILURE);
        }
        if (checkMission("Mission-15-Task-3-Timeout")) {
            // not reported in time
            setObjectiveState("Mission-15-Task-3", ObjectiveState.FAILURE);
            setObjectiveState("Mission-15", ObjectiveState.FAILURE);
            addTimeout("Mission-15-Hide", 13000);

        }
        // hide tasks ---------------------------------------------------------------
        if (checkTimeout("Mission-15-Hide")) {
            objective("Mission-15").visible = false;
            send("Douglas-Report-Spy").visible = false;
            world.currentTalk = null;
        }
    }

    @Override
    public void onMessageSeen(String id) {
        if ("Douglas-Report-Spy".equals(id)) {
            if (setObjectiveState("Mission-15-Task-3", ObjectiveState.SUCCESS)) {
                setObjectiveState("Mission-15", ObjectiveState.SUCCESS);
                clearMission("Mission-15-Task-3-Timeout");
                addTimeout("Mission-15-Hide", 13000);
            }
        }
    }
    @Override
    public void onTalkCompleted() {
        if ("kelly".equals(world.currentTalk)) {
            if (setObjectiveState("Mission-15-Task-1", ObjectiveState.SUCCESS)) {
                world.allowRecordMessage = true;
                showObjective("Mission-15-Task-2");
                clearMission("Mission-15-Task-1-Timeout");
                // record message timeout
                addMission("Mission-15-Task-2-Timeout", 7 * 24);
            }
        }
    }
    @Override
    public void onRecordMessage() {
        if (objective("Mission-15-Task-2").state == ObjectiveState.ACTIVE
                && !hasMission("Mission-15-Morning")) {
            world.messageRecording = true;
            int t = ((now() / 24) * 24) + 23 - now();
            addMission("Mission-15-Morning", t);
            setObjectiveState("Mission-15-Task-2", ObjectiveState.SUCCESS);
            clearMission("Mission-15-Task-2-Timeout");
        }
    }
    @Override
    public void reset() {
        world.allowRecordMessage = false;
        world.messageRecording = false;
    }
}
