/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.core.Func1;

import java.util.HashMap;
import java.util.Map;

/**
 * GOAP computation with Iterative Deepening A* search.
 * <p>Plan cost: number of actions, total action duration, resource consumption.</p>
 * @author akarnokd, 2011.12.22.
 */
public final class IDAStarGOAP {
	/** Utility class. */
	private IDAStarGOAP() {
		// TODO Auto-generated constructor stub
	}
	/** The world model. */
	abstract static class WorldModel {
		/** @return create independent copy. */
		abstract WorldModel copy();
		/** @return the next available action. */
		abstract Action nextAction();
		/** 
		 * Apply the action.
		 * @param action the action to apply 
		 */
		abstract void applyAction(Action action);
		/** @return The world state hash. */
		abstract int hash();
	}
	/**
	 * The goal.
	 * @author akarnokd, 2011.12.22.
	 */
	abstract static class Goal {
		/**
		 * Returns true if the current world state fills this goal.
		 * @param model the world model
		 * @return true if world state met the goal
		 */
		abstract boolean isFulfilled(WorldModel model);
	}
	/** TranspositionTable entry. */
	static class Entry {
		/** The world model.*/
		WorldModel model;
		/** The depth this model was seen. */
		int depth;
	}
	/** Caches the visited world states to avoid going down them again. */
	static class TranspositionTable {
		/** The state map. */
		final Map<Integer, Entry> map = new HashMap<>();
		/**
		 * Did we encounter this world state before?
		 * @param model the model
		 * @return true if already encountered
		 */
		boolean has(WorldModel model) {
			Entry e = map.get(model.hash());
			return e != null && e.model.equals(model);
		}
		/**
		 * Add or replace a world model in the map if new or has lesser depth.
		 * @param model the model
		 * @param depth the depth
		 */
		void add(WorldModel model, int depth) {
			int hash = model.hash();
			Entry e = map.get(hash);
			if (e != null && e.model.equals(model)) {
				if (depth < e.depth) {
					e.depth = depth;
				}
			} else {
				if (e == null) {
					e = new Entry();
					e.model = model.copy();
					e.depth = depth;
					map.put(hash, e);
				} else {
					if (depth < e.depth) {
						e.model = model.copy();
						e.depth = depth;
					}
				}
			}
		}
	}
	/** The action to perform. */
	abstract static class Action {
		/** @return the cost to execute the action. */
		abstract double getCost();
	}
	/** Pair of cutoff and action values. */
	static class CutoffAction {
		/** The search cutoff cost value. */
		double cutoff;
		/** The action to take. */
		Action action;
		/**
		 * Constructor. Initializes the fields.
		 * @param cutoff the cutoff cost value
		 * @param action the action to take
		 */
		public CutoffAction(double cutoff, Action action) {
			this.cutoff = cutoff;
			this.action = action;
		}
		
	}
	/**
	 * Execute the planning for the given goal.
	 * @param worldModel the starting world model
	 * @param goal the goal
	 * @param heuristic the cost heuristic
	 * @param maxDepth the maximum search depth
	 * @return the action to take or null if no action available
	 */
	public static Action planAction(WorldModel worldModel, Goal goal, Func1<WorldModel, Double> heuristic, int maxDepth) {
		double cutoff = heuristic.invoke(worldModel);
		TranspositionTable transpositionTable = new TranspositionTable();
		
		while (cutoff >= 0) {
			CutoffAction ca = depthFirst(worldModel, goal, transpositionTable, heuristic, maxDepth, cutoff);
			if (ca.action != null) {
				return ca.action;
			}
			cutoff = ca.cutoff;
		}
		return null;
	}
	/**
	 * Perform a depth first search.
	 * @param worldModel the current world model.
	 * @param goal the goal
	 * @param transpositionTable the visited world state store
	 * @param heuristic the heuristic to compute the world state cost
	 * @param maxDepth the maximum search depth
	 * @param cutoff the initial cost cutoff value
	 * @return a new cutoff value and a possible action
	 */
	private static CutoffAction depthFirst(WorldModel worldModel, Goal goal,
			TranspositionTable transpositionTable,
			Func1<WorldModel, Double> heuristic, int maxDepth, double cutoff) {
		WorldModel[] models = new WorldModel[maxDepth + 1];
		Action[] actions = new Action[maxDepth];
		double[] costs = new double[maxDepth];
		
		models[0] = worldModel.copy();
		int currentDepth = 0;
		double smallestCutoff = Double.POSITIVE_INFINITY;
		
		while (currentDepth >= 0) {
			if (goal.isFulfilled(models[currentDepth])) {
				return new CutoffAction(cutoff, actions[0]);
			}
			if (currentDepth >= maxDepth) {
				currentDepth--;
				continue;
			}
			double cost = heuristic.invoke(models[currentDepth]) + costs[currentDepth];
			if (cost > cutoff) {
				if  (cutoff < smallestCutoff) {
					smallestCutoff = cutoff;
				}
				currentDepth--;
				continue;
			}
			Action nextAction = models[currentDepth].nextAction();
			if (nextAction != null) {
				actions[currentDepth] = nextAction;

				models[currentDepth + 1] = models[currentDepth].copy();
				models[currentDepth + 1].applyAction(nextAction);
				costs[currentDepth + 1] = costs[currentDepth] + nextAction.getCost();
				
				if (!transpositionTable.has(models[currentDepth + 1])) {
					currentDepth++;
				}
				transpositionTable.add(models[currentDepth + 1], currentDepth);
			} else {
				currentDepth--;
			}
		}
		
		return new CutoffAction(smallestCutoff, null);
	}
}
