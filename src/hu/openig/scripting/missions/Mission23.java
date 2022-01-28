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
import hu.openig.utils.XElement;

/**
 * Mission 23: Final vision.
 * @author akarnokd, 2012.03.02.
 */
public class Mission23 extends Mission {
    /** The mission stages. */
    enum M23 {
        /** Not started. */
        NONE,
        /** Wait for vision. */
        WAIT,
        /** Playing vision. */
        VISION,
        /** Talk is available. */
        TALK,
        /** Talk about the android. */
        ANDROID,
        /** Mission done. */
        DONE
    }
    /** The current stage. */
    M23 stage = M23.NONE;

    @Override
    public boolean applicable() {
        return world.level >= 4;
    }
    @Override
    public void onTime() {
        if (stage == M23.NONE) {
            stage = M23.WAIT;
            addMission("Mission-23", 7 * 12);
        }
        if (checkMission("Mission-23")) {
            stage = M23.VISION;
            world.env.stopMusic();
            world.env.playVideo("interlude/dream_4", new Action0() {
                @Override
                public void invoke() {
                    stage = M23.TALK;
                    world.currentTalk = "doctor";
                    showObjective("Mission-23");
                    addMission("Mission-23-Timeout", 2 * 24);
                    world.env.playMusic();

                }
            });
        }
        if (checkMission("Mission-23-Timeout")) {
            if (stage == M23.TALK) {
                stage = M23.DONE;
                setObjectiveState("Mission-23", ObjectiveState.FAILURE);
                addTimeout("Mission-23-Hide", 13000);
                world.currentTalk = null;
            }
        }
        if (checkTimeout("Mission-23-Hide")) {
            objective("Mission-23").visible = false;
        }
        if (checkMission("Mission-23-EndTalk")) {
            world.currentTalk = null;
            send("Android").visible = true;
            showObjective("Mission-23-Task-1");
            addMission("Mission-23-Android-Timeout", 2 * 24);
        }
        if (checkMission("Mission-23-Android-Timeout")) {
            if (stage == M23.ANDROID) {
                send("Android").visible = false;
                setObjectiveState("Mission-23-Task-1", ObjectiveState.FAILURE);
                addTimeout("Mission-23-Task-1-Hide", 13000);
            }
        }
        if (checkTimeout("Mission-23-Task-1-Hide")) {
            objective("Mission-23-Task-1").visible = false;
        }
        if (checkTimeout("Mission-23-Achievement")) {
            world.achievement("achievement.i_robot");
        }
    }
    @Override
    public void onMessageSeen(String id) {
        if ("Android".equals(id) && stage == M23.ANDROID) {
            stage = M23.DONE;
            setObjectiveState("Mission-23-Task-1", ObjectiveState.SUCCESS);
            clearMission("Mission-23-Android-Timeout");
            addTimeout("Mission-23-Task-1-Hide", 13000);
            addTimeout("Mission-23-Achievement", 5000);
        }
    }
    @Override
    public void onTalkCompleted() {
        if ("doctor".equals(world.currentTalk) && stage == M23.TALK) {
            setObjectiveState("Mission-23", ObjectiveState.SUCCESS);
            clearMission("Mission-23-Timeout");
            stage = M23.ANDROID;
            addTimeout("Mission-23-Hide", 13000);
            addMission("Mission-23-EndTalk", 1);
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
        stage = M23.valueOf(xmission.get("stage", M23.NONE.toString()));
    }
    @Override
    public void reset() {
        stage = M23.NONE;
        world.currentTalk = null;
    }

}
