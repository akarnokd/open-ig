/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;


/**
 * Testing goal oriented action planning.
 * @author akarnokd, 2011.12.22.
 */
public final class SimpleGOAP {
	/** Utility class. */
	private SimpleGOAP() {
		
	}
	/**
	 * The world model.
	 * @author akarnokd, 2011.12.22.
	 */
	static class WorldModel {
		/** @return calculate the current discontentment value. */
		double calculateDiscontentment() {
			return 0;
		}
		/** @return Return the next action for the current world state or null. */
		Action nextAction() {
			return null;
		}
		/**
		 * @return create a copy of the current world state
		 */
		WorldModel copy() {
			WorldModel result = new WorldModel();
			
			return result;
		}
		/**+
		 * Apply the action to the world.
		 * @param action the action
		 */
		void applyAction(Action action) {
			action.update(this);
		}
	}
	/**
	 * The action to perform.
	 * @author akarnokd, 2011.12.22.
	 */
	public static class Action {
		/**
		 * Update the world based on the action.
		 * @param model the world model
		 */
		void update(WorldModel model) {
			
		}
	}
	/**
	 * Plan for the best action to take in the given initial world state.
	 * @param model the initial world state
	 * @param maxDepth the depth of the search
	 * @return the action to take or null if no action available
	 */
	public static Action planAction(WorldModel model, int maxDepth) {
		WorldModel[] models = new WorldModel[maxDepth + 1];
		Action[] actions = new Action[maxDepth];
		models[0] = model.copy();
		int currentDepth = 0;
		Action bestAction = null;
		double bestValue = Double.POSITIVE_INFINITY;
		while (currentDepth >= 0) {
			double currentValue = models[currentDepth].calculateDiscontentment();
			if (currentDepth >= maxDepth) {
				if (currentValue < bestValue) {
					bestValue = currentValue;
					bestAction = actions[0];

					models[currentDepth] = null;
					currentDepth--;
					continue;
				}
			}
			Action nextAction = models[currentDepth].nextAction();
			if (nextAction != null) {
				models[currentDepth + 1] = models[currentDepth].copy();
				actions[currentDepth] = nextAction;
				models[currentDepth + 1].applyAction(nextAction);
				currentDepth++;
			} else {
				models[currentDepth] = null;
				currentDepth --;
			}
		}
		return bestAction;
	}
}
