/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import java.util.List;

/**
 * Simple class to evaluate the planning algorithms from the books and articles.
 * @author akarnokd, 2011.12.21.
 */
public final class BasicPlanning {
	/** Utility class. */
	private BasicPlanning() {
		// utility class
	}
	/** The goal. */
	static class Goal {
		/** The display name. */
		String name;
		/** The goal value. */
		double value;
		/**
		 * Compute the discontentment value of this goal (e.g., how bad is it).
		 * @param newValue the new goal value
		 * @return the new discontentment
		 */
		double getDiscontentment(double newValue) {
			return newValue * newValue;
		}
		/**
		 * The change in value over the given time period.
		 * @param time the change duration
		 * @return the value delta over time
		 */
		double getChange(int time) {
			return 0;
		}
	}
	/** The action. */
	abstract static class Action {
		/**
		 * Returns the goal change value for the given goal.
		 * @param goal the goal
		 * @return the change in the goal's value
		 */
		abstract double getGoalChange(Goal goal);
		/**
		 * The duration of the action.
		 * @return the duration in time units
		 */
		abstract int getDuration();
	}
	/**
	 * Chose the best action for.
	 * @param actions the action list
	 * @param goals the goals
	 * @return the action choosen
	 */
	static Action chooseAction(List<Action> actions, List<Goal> goals) {
		Action best = actions.get(0);
		double bestValue = calculateDiscontentment(actions.get(0), goals);
		for (int i = 1; i < actions.size(); i++) {
			Action action = actions.get(i);
			double thisValue = calculateDiscontentment(action, goals);
			if (thisValue < bestValue) {
				best = action;
				bestValue = thisValue;
			}
		}
		return best;
	}
	/**
	 * Calculates the total discontentment of the goals given the action.
	 * @param action the action
	 * @param goals the goals
	 * @return the total discontentment
	 */
	static double calculateDiscontentment(Action action, List<Goal> goals) {
		double discontentment = 0;
		for (Goal goal : goals) {
			double newValue = goal.value + action.getGoalChange(goal);
			newValue += goal.getChange(action.getDuration());
			discontentment += goal.getDiscontentment(newValue);
		}
		return discontentment;
	}

}
