/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

import hu.openig.core.Func1;
import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Player;
import hu.openig.model.VideoMessage;
import hu.openig.model.ViewLimit;

import java.util.Set;

/**
 * Interface for helper functions and methods for the missions.
 * @author akarnokd, 2012.01.14.
 */
public interface MissionScriptingHelper {
    /**
     * Retrieve an objective.
     * @param id the id
     * @return the objective
     */
    Objective objective(String id);
    /**
     * Check if a timeout has been reached.
     * @param id the reference id
     * @return true if timeout reached
     */
    boolean isTimeout(String id);
    /**
     * Register a new timeout counter.
     * @param id the reference id
     * @param time the time
     */
    void setTimeout(String id, int time);
    /**
     * Check if the specified timeout has been set.
     * @param id the timeout id
     * @return true if set
     */
    boolean hasTimeout(String id);
    /**
     * Remove timeout.
     * @param id the reference id
     */
    void clearTimeout(String id);
    /** @return The hours passed since the start of the game. */
    int now();
    /**
     * Set the mission time to the given hours since the game start.
     * @param id the mission id
     * @param hours the hours
     */
    void setMissionTime(String id, int hours);
    /**
     * Checks if the mission time has been already set.
     * @param id the identifier
     * @return true if already set
     */
    boolean hasMissionTime(String id);
    /**
     * Clear the mission time.
     * @param id the mission id
     */
    void clearMissionTime(String id);
    /**
     * Notify the game over state.
     */
    void gameover();
    /**
     * Check if the mission time has arrived.
     * @param id the mission id
     * @return true if the current hours since start is greater than the predefined time
     */
    boolean isMissionTime(String id);
    /**
     * Check if the objective/mission can be started.
     * <ul>
     * <li>The given objective is invisible and is in state ACTIVE</li>
     * <li>The isMissionTime() returns true for the id</li>
     * </ul>
     * @param oId the objective id
     * @return true if the mission can be started
     */
    boolean canStart(String oId);
    /**
     * Change the objective state and show the objectives.
     * @param oId the objective ID
     * @param newState the new state
     * @return true if the state actually changed
     */
    boolean setObjectiveState(String oId, ObjectiveState newState);
    /**
     * Change the objective state and show the objectives.
     * @param o the objective
     * @param newState the new state
     * @return true if the state actually changed
     */
    boolean setObjectiveState(Objective o, ObjectiveState newState);
    /**
     * Get a send-message.
     * @param id the message id
     * @return the video message or null if not available
     */
    VideoMessage send(String id);
    /**
     * Show an objective in the objectives tab.
     * @param id the objective id
     * @return false if the objective is already visible
     */
    boolean showObjective(String id);
    /**
     * Show an objective in the objectives tab.
     * @param o the objective
     * @return false if the objective is already visible
     */
    boolean showObjective(Objective o);
    /**
     * Returns the set of current scripted fleets.
     * @return the set of current scripted fleets
     */
    Set<Integer> scriptedFleets();
    /**
     * Clear the timeouts specified by the filter function.
     * @param filter the filter function
     */
    void clearTimeouts(Func1<String, Boolean> filter);
    /**
     * Clear the mission times specified by the filter function.
     * @param filter the filter function
     */
    void clearMissionTimes(Func1<String, Boolean> filter);
    /**
     * Hide and reset any objectives specified by the filter function.
     * @param filter the filter
     */
    void clearObjectives(Func1<String, Boolean> filter);
    /**
     * Hide and reset any messages specified by the filter.
     * @param filter the filter
     */
    void clearMessages(Func1<String, Boolean> filter);
    /**
     * Returns the view limits for the given player at the level.
     * @param player the player
     * @param level the level
     * @return the limit or null if no limit
     */
    ViewLimit getViewLimit(Player player, int level);
}
