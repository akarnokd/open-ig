/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
        if (trueDistance == null) {
            throw new IllegalStateException("trueDistance is null");
        }
        if (neighbors == null) {
            throw new IllegalStateException("neighbors is null");
        }
        if (estimation == null) {
            throw new IllegalStateException("estimation is null");
        }
        if (distance == null) {
            throw new IllegalStateException("distance is null");
        }

        Set<T> closedSet = new HashSet<>();
        Map<T, T> cameFrom = new HashMap<>();
        final Map<T, Integer> gScore = new HashMap<>();
        final Map<T, Integer> hScore = new HashMap<>();
        final Map<T, Integer> fScore = new HashMap<>();
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
                int c = Integer.compare(d1, d2);
                if (c == 0) {
                    d1 = trueDistance.invoke(initial, o1);
                    d2 = trueDistance.invoke(initial, o2);
                    c = Integer.compare(d1, d2);
                }
                return c;
            }
        };

        Set<T> openSet2 = new HashSet<>();
        PriorityQueue<T> openSet = new PriorityQueue<>(1024, smallestF);

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

            openSet.remove(current);
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
        LinkedList<T> path = new LinkedList<>();
        path.addLast(current);
        T parent = cameFrom.get(current);
        while (parent != null) {
            path.addFirst(parent);
            parent = cameFrom.get(parent);
        }

        return path;
    }
}
