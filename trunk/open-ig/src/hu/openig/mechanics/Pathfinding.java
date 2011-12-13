/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Func1;
import hu.openig.core.Location;
import hu.openig.utils.JavaUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Class implementing the pathfinding algorithms for ground battles.
 * @author akarnokd, 2011.09.06.
 */
public class Pathfinding extends AStarSearch<Location> {
	/** Test for passability. */
	public Func1<Location, Boolean> isPassable;
	/** Setup the A*. */
	public Pathfinding() {
		neighbors = new Func1<Location, List<Location>>() {
			@Override
			public List<Location> invoke(Location value) {
				return neighbors(value);
			}
		};
	}
	/**
	 * Search for a path leading closest to the given destination.
	 * @param initial the initial location
	 * @param destination the destination
	 * @return the path, empty if the target is completely unreachable
	 */
	public List<Location> searchApproximate(final Location initial, final Location destination) {
		List<Location> result = search(initial, destination);
		if (result.size() == 0) {
			// try locating a passable target nearby
			int maxradius = Math.max(Math.abs(destination.x - initial.x), Math.abs(destination.y - initial.y));
			int r = 1;
			while (r < maxradius) {
				List<Location> locations = new ArrayList<Location>(squareAround(destination, r));
				Collections.sort(locations, new Comparator<Location>() {
					@Override
					public int compare(Location o1, Location o2) {
						int d1 = distance2(initial, destination, o1);
						int d2 = distance2(initial, destination, o2);
						return d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
					}
				});
				for (Location found : locations) {
					List<Location> result2 = search(initial, found);
					if (result2.size() > 0) {
						return result2;
					}
				}
				r++;
			}
			return Collections.emptyList();
		}
		return result;
	}
	/**
	 * Computes the sum of the distance squares between (target and source1) and (target and source2).
	 * @param source1 the first point
	 * @param source2 the second point
	 * @param target the target point
	 * @return the distance
	 */
	int distance2(Location source1, Location source2, Location target) {
		return (target.x - source1.x) * (target.x - source1.x)
				+ (target.y - source1.y) * (target.y - source1.y)
				+ (target.x - source2.x) * (target.x - source2.x)
				+ (target.y - source2.y) * (target.y - source2.y);
	}
	/**
	 * Returns a list of passable locations around the given center by the given radius.
	 * @param center the center location
	 * @param radius the radius
	 * @return the locations around the center
	 */
	public Set<Location> squareAround(Location center, int radius) {
		Set<Location> result = JavaUtils.newHashSet();
		int x0 = center.x - radius;
		int x1 = center.x + radius;
		int y0 = center.y - radius;
		int y1 = center.y + radius;
		for (int x = x0; x <= x1; x++) {
			Location loc = Location.of(x, y0);
			if (isPassable.invoke(loc)) {
				result.add(loc);
			}
			loc = Location.of(x, y1);
			if (isPassable.invoke(loc)) {
				result.add(loc);
			}
		}
		for (int y = y0; y <= y1; y++) {
			Location loc = Location.of(x0, y);
			if (isPassable.invoke(loc)) {
				result.add(loc);
			}
			loc = Location.of(x1, y);
			if (isPassable.invoke(loc)) {
				result.add(loc);
			}
		}
		return result;
	}
	/**
	 * Returns a list of neighboring, passable cells, but avoids corner cutting cases.
	 * @param current the current location
	 * @return the list of neighbors
	 */
	private List<Location> neighbors(Location current) {
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
}
