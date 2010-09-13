/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.xold.ai;

import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A* search algorithm.
 * @author karnokd
 */
public final class AStarSearch {
	/** Private constructor. */
	private AStarSearch() {
		// utility class
	}
	/**
	 * An A* search node.
	 * The class is incompatible in terms of the equals() contract and the comparable contract.
	 * @author karnokd
	 */
	public abstract static class AStarNode implements Comparable<AStarNode> {
		/** The parent node for backtracking. */
		AStarNode parent;
		/** The current cost from the start node. */
		float costFromStart;
		/** The estimated cost to the goal node. */
		float estimatedCostToGoal;
		/**
		 * Returns the cost of the node. 
		 * @return the cost of the node
		 */
		public float getCost() {
			return costFromStart + estimatedCostToGoal;
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compareTo(AStarNode other) {
			return Float.compare(getCost(), other.getCost());
		}
		/**
		 * Returns the cost between this node and the other node.
		 * @param node the other node
		 * @return the cost
		 */
		public abstract float getCost(AStarNode node);
		/**
		 * Gets the estimated cost between this node and the
		 * specified node. The estimated cost should never exceed
		 * the true cost. The better the estimate, the more
		 * efficient the search.
		 * @param node the other node
		 * @return the cost
		 */
		public abstract float getEstimatedCost(AStarNode node);
		/**
		 * Returns an iterable of the current node's neighboring nodes.
		 * @return the neighboring nodes iterable
		 */
		public abstract Iterable<AStarNode> getNeighbors();
	}
	/**
	 * Simple insertion sort based priority list.
	 * @author karnokd
	 *
	 * @param <T> a comparable element type
	 */
	public static class PriorityList<T extends Comparable<T>> extends LinkedList<T> {
		/** */
		private static final long serialVersionUID = -4565085428648480489L;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean add(T element) {
			for (int i = 0; i < size(); i++) {
				if (get(i).compareTo(element) >= 0) {
					add(i, element);
					return true;
				}
			}
			return super.add(element);
		};
	}
	/**
	 * Constructs a path starting from the given node.
	 * @param node the node to start building from
	 * @return the list of nodes as the path
	 */
	protected static List<AStarNode> constructPath(AStarNode node) {
		List<AStarNode> result = new LinkedList<AStarNode>();
		AStarNode start = node;
		while (start.parent != null) {
			result.add(0, start);
			start = start.parent;
		}
		return result;
	}
	/**
	 * Tries to find the optimal path between the start and goal nodes
	 * using the A* search algorithm.
	 * @param start the start node
	 * @param goal the goal node
	 * @return the list of nodes as the resulting path, or null if there is no such path
	 */
	public static List<AStarNode> findPath(AStarNode start, AStarNode goal) {
		PriorityList<AStarNode> openList = new PriorityList<AStarNode>();
		// the closed set
		Map<AStarNode, Object> closedSet = new IdentityHashMap<AStarNode, Object>();
		// the open set for fast access
		Map<AStarNode, Object> openSet = new IdentityHashMap<AStarNode, Object>();
		Object nothing = new Object();
		
		start.costFromStart = 0;
		start.estimatedCostToGoal = start.getEstimatedCost(goal);
		start.parent = null;
		openList.add(start);
		openSet.put(start, nothing);
		while (!openList.isEmpty()) {
			AStarNode node = openList.removeFirst();
			if (node == goal) {
				return constructPath(node);
			}
			for (AStarNode neighbor : node.getNeighbors()) {
				boolean isOpen = openSet.containsKey(node);
				boolean isClosed = closedSet.containsKey(node);
				
				float costFromStart = node.costFromStart + node.getCost(neighbor);
				// check if the neighbor node has not been
                // traversed or if a shorter path to this
                // neighbor node is found.
				if ((!isOpen && !isClosed) || costFromStart < neighbor.costFromStart) {
					neighbor.parent = node;
					neighbor.costFromStart = costFromStart;
					neighbor.estimatedCostToGoal = neighbor.getEstimatedCost(goal);
					
					if (isClosed) {
						closedSet.remove(neighbor);
					}
					if (!isOpen) {
						openList.add(neighbor);
						openSet.put(neighbor, nothing);
					}
				}
			}
			closedSet.put(node, nothing);
		}
		return null;
	}
}
