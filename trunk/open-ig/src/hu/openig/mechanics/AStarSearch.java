/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Func1;
import hu.openig.core.Func2;
import hu.openig.core.Pair;
import hu.openig.utils.U;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * The generic A* search algorithm on node provider of type T.
 * <p>The distance estimation is computed via integer values. You may need to scale some real-valued
 * cases.</p>
 * @author akarnokd, 2011.12.13.
 * @param <T> the node object type
 */
public class AStarSearch<T> {
	/** The estimation function. */
	public Func2<T, T, Integer> estimation;
	/** The distance function between neighbors. */
	public Func2<T, T, Integer> distance;
	/** The neighbor function. */
	public Func1<T, List<T>> neighbors;
	/** The distance function between arbitrary locations. */
	public Func2<T, T, Integer> trueDistance;
	/** 
	 * Search for a path.
	 * @param initial the initial location
	 * @param destination the destination location
	 * @return pair of true and list of locations if exact route found,
	 * false and list with a path that brings close to the target. 
	 */
	public Pair<Boolean, List<T>> search(final T initial, final T destination) {
		Set<T> closedSet = U.newHashSet();
		Map<T, T> cameFrom = U.newHashMap();
		final Map<T, Integer> gScore = U.newHashMap();
		final Map<T, Integer> hScore = U.newHashMap();
		final Map<T, Integer> fScore = U.newHashMap();
		final Comparator<T> smallestF = new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				int g1 = fScore.get(o1);
				int g2 = fScore.get(o2);
				return g1 < g2 ? -1 : (g1 > g2 ? 1 : 0);
			}
		};
		
		Comparator<T> nearestComparator = new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				int d1 = trueDistance.invoke(destination, o1);
				int d2 = trueDistance.invoke(destination, o2);
				int c = U.compare(d1, d2);
				if (c == 0) {
					d1 = trueDistance.invoke(initial, o1);
					d2 = trueDistance.invoke(initial, o2);
					c = U.compare(d1, d2);
				}
				return c;
			}
		};
		
		Set<T> openSet2 = U.newHashSet();
		PriorityQueue<T> openSet = new PriorityQueue<T>(1024, smallestF);
		
		gScore.put(initial, 0);
		hScore.put(initial, estimation.invoke(initial, destination));
		fScore.put(initial, gScore.get(initial) + hScore.get(initial));
		
		openSet.add(initial);
		openSet2.add(initial);
		
		T nearest = null;
		
		while (!openSet.isEmpty()) {
			
			T current = openSet.remove();
			
			if (current.equals(destination)) {
				return Pair.of(true, reconstructPath(cameFrom, destination));
			}
			
			openSet.remove(0);
			openSet2.remove(current);
			closedSet.add(current);
			
			if (nearest == null || nearestComparator.compare(nearest, current) > 0) {
				nearest = current;
			}
			
			
			for (T loc : neighbors.invoke(current)) {
				if (!closedSet.contains(loc)) {
					int tentativeScore = gScore.get(current) + distance.invoke(current, loc);
					if (!openSet2.contains(loc)) {
						cameFrom.put(loc, current);
						
						openSet.remove(loc);
						
						gScore.put(loc, tentativeScore);
						hScore.put(loc, estimation.invoke(loc, destination));
						fScore.put(loc, gScore.get(loc) + hScore.get(loc));
						
						openSet.add(loc);
						
						openSet2.add(loc);
						
					} else
					if (tentativeScore < gScore.get(loc)) {
						cameFrom.put(loc, current);

						openSet.remove(loc);
						
						gScore.put(loc, tentativeScore);
						hScore.put(loc, estimation.invoke(loc, destination));
						fScore.put(loc, gScore.get(loc) + hScore.get(loc));
						
						openSet.add(loc);
					}
				}
			}
		}
		
		// if we get here, there was no direct path available
		// find a target location which minimizes initial-L-destination
		if (closedSet.isEmpty()) {
			return Pair.of(false, Collections.<T>emptyList());
		}
		return Pair.of(true, reconstructPath(cameFrom, nearest));
	}
	/**
	 * Reconstructs the path from the traceback map.
	 * @param cameFrom the map of who came from where
	 * @param current the current location
	 * @return the list of the path elements
	 */
	public List<T> reconstructPath(Map<T, T> cameFrom, T current) {
		LinkedList<T> path = U.newLinkedList();
		path.addLast(current);
		T parent = cameFrom.get(current);
		while (parent != null) {
			path.addFirst(parent);
			parent = cameFrom.get(parent);
		}
		
		return path;
	}
}
