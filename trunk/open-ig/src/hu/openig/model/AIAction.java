/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.utils.JavaUtils;

import java.util.Map;

/**
 * Represents a GOAP action with set of world preconditions and postconditions.
 * @author akarnokd, 2011.12.13.
 */
public abstract class AIAction {
	/** The name of the action. */
	public String name;
	/** The current state of this action. */
	public AIActionState state;
	/**
	 * The list of preconditions as world properties.
	 */
	public final Map<AIWorldPropertyKey, AIWorldProperty> preconditions = JavaUtils.newHashMap();
	/**
	 * The list of effects after the action is successfully executed.
	 */
	public final Map<AIWorldPropertyKey, AIWorldProperty> effects = JavaUtils.newHashMap();
	/**
	 * Returns true if the action is valid in the given world context and goal.
	 * <p>This function is called every time the GOAP planner tries to add this action to the 
	 * plan. You may implement cacheing or precomputed values to make it faster.</p>
	 * @param world the world raw state
	 * @param goal the current goal to be reached
	 * @return true if valid
	 */
	public abstract boolean isValidContext(AIWorld world, AIGoal goal);
	/**
	 * Check if all precondition of this action is met by the world state.
	 * @param state the state map from world key to world value
	 * @return true if the action has filled in all of its preconditions
	 */
	public abstract boolean isValidAction(Map<AIWorldPropertyKey, AIWorldProperty> state);
}
