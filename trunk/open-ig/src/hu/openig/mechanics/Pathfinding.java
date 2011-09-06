/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Func1;
import hu.openig.core.Func2;
import hu.openig.core.Location;
import hu.openig.utils.JavaUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class implementing the pathfinding algorithms for ground battles.
 * @author akarnokd, 2011.09.06.
 */
public class Pathfinding {
	/** Test for passability. */
	public Func1<Location, Boolean> isPassable;
	/** The estimation function. */
	public Func2<Location, Location, Integer> estimation;
	/** The distance function between neighbors. */
	public Func2<Location, Location, Integer> distance;
	/** 
	 * Search for a path.
	 * @param initial the initial location
	 * @param destination the destination location
	 * @return the sortest path or an empty list if no such path exists. 
	 */
	public List<Location> search(Location initial, Location destination) {
		Set<Location> closedSet = JavaUtils.newHashSet();
		Map<Location, Location> cameFrom = JavaUtils.newHashMap();
		final Map<Location, Integer> gScore = JavaUtils.newHashMap();
		final Map<Location, Integer> hScore = JavaUtils.newHashMap();
		final Map<Location, Integer> fScore = JavaUtils.newHashMap();
		final Comparator<Location> smallestF = new Comparator<Location>() {
			@Override
			public int compare(Location o1, Location o2) {
				int g1 = fScore.get(o1);
				int g2 = fScore.get(o2);
				return g1 < g2 ? -1 : (g1 > g2 ? 1 : 0);
			}
		};
		Set<Location> openSet2 = JavaUtils.newHashSet();
		List<Location> openSet = JavaUtils.newArrayList();
		
		gScore.put(initial, 0);
		hScore.put(initial, estimation.invoke(initial, destination));
		fScore.put(initial, gScore.get(initial) + hScore.get(initial));
		
		openSet.add(initial);
		openSet2.add(initial);
		
		while (!openSet.isEmpty()) {
			
			Location current = openSet.get(0);
			
			if (current.equals(destination)) {
				return reconstructPath(cameFrom, destination);
			}
			
			openSet.remove(0);
			openSet2.remove(current);
			closedSet.add(current);
			
			for (Location loc : neighbors(current)) {
				if (!closedSet.contains(loc)) {
					int tentativeScore = gScore.get(current) + distance.invoke(current, loc);
					if (!openSet2.contains(loc)) {
						
						cameFrom.put(loc, current);
						gScore.put(loc, tentativeScore);
						hScore.put(loc, estimation.invoke(loc, destination));
						fScore.put(loc, gScore.get(loc) + hScore.get(loc));
						
						openSet.add(loc);
						Collections.sort(openSet, smallestF);
						openSet2.add(loc);
						
					} else
					if (tentativeScore < gScore.get(loc)) {
						cameFrom.put(loc, current);
						gScore.put(loc, tentativeScore);
						hScore.put(loc, estimation.invoke(loc, destination));
						fScore.put(loc, gScore.get(loc) + hScore.get(loc));
						
						Collections.sort(openSet, smallestF);
					}
				}
			}
		}
		return Collections.emptyList();
	}
	/**
	 * Returns a list of neighboring, passable cells, but avoids corner cutting cases.
	 * @param current the current location
	 * @return the list of neighbors
	 */
	List<Location> neighbors(Location current) {
		LinkedList<Location> result = JavaUtils.newLinkedList();
		Location left = current.delta(-1, 0);
		Location right = current.delta(1, 0);
		Location bottom = current.delta(0, 1);
		Location top = current.delta(0, -1);
		
		boolean pleft = isPassable.invoke(left);
		boolean pright = isPassable.invoke(right);
		boolean pbottom = isPassable.invoke(bottom);
		boolean ptop = isPassable.invoke(top);
		
		if (pleft) {
			result.add(left);
		}
		if (pright) {
			result.add(right);
		}
		if (pbottom) {
			result.add(bottom);
		}
		if (ptop) {
			result.add(top);
		}
		if (pleft && ptop) {
			Location c = current.delta(-1, -1);
			if (isPassable.invoke(c)) {
				result.add(c);
			}
		}
		if (pleft && pbottom) {
			Location c = current.delta(-1, 1);
			if (isPassable.invoke(c)) {
				result.add(c);
			}
		}
		if (pright && ptop) {
			Location c = current.delta(1, -1);
			if (isPassable.invoke(c)) {
				result.add(c);
			}
		}
		if (pright && pbottom) {
			Location c = current.delta(1, 1);
			if (isPassable.invoke(c)) {
				result.add(c);
			}
		}
		return result;
	}
	/**
	 * Reconstructs the path from the traceback map.
	 * @param cameFrom the map of who came from where
	 * @param current the current location
	 * @return the list of the path elements
	 */
	List<Location> reconstructPath(Map<Location, Location> cameFrom, Location current) {
		LinkedList<Location> path = JavaUtils.newLinkedList();
		path.addLast(current);
		Location parent = cameFrom.get(current);
		while (parent != null) {
			path.addFirst(parent);
			parent = cameFrom.get(parent);
		}
		
		return path;
	}
}
