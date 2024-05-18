/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Class implementing the pathfinding algorithms.
 * @author akarnokd, 2011.09.06.
 */
public class Pathfinding extends AStarSearch<Location> {
    /** Test for passability. */
    public Func1<Location, Boolean> isPassable;
    /** Test for permanent obstacles. */
    public Func1<Location, Boolean> isBlocked;
    /** Setup the A*. */
    public Pathfinding() {
        neighbors = new Func1<Location, List<Location>>() {
            @Override
            public List<Location> invoke(Location value) {
                return neighbors(value);
            }
        };

        this.estimation = defaultEstimator;
        this.distance = defaultDistance;
        this.trueDistance = defaultTrueDistance;
    }

    /**
     * The default estimator for distance away from the target.
     */
    final Func2<Location, Location, Integer> defaultEstimator = new Func2<Location, Location, Integer>() {
        @Override
        public Integer invoke(Location t, Location u) {
            return (Math.abs(t.x - u.x) + Math.abs(t.y - u.y)) * 1000;
        }
    };

    /** Routine that tells the distance between two neighboring locations. */
    final Func2<Location, Location, Integer> defaultDistance = new Func2<Location, Location, Integer>() {
        @Override
        public Integer invoke(Location t, Location u) {
            if (t.x == u.x || u.y == t.y) {
                return 1000;
            }
            return 1414;
        }
    };

    /**
     * Computes the distance between any cells.
     */
    final Func2<Location, Location, Integer> defaultTrueDistance = new Func2<Location, Location, Integer>() {
        @Override
        public Integer invoke(Location t, Location u) {
            return (int)(1000 * Math.hypot(t.x - u.x, t.y - u.y));
        }
    };

    /**
     * Search for a path leading closest to the given destination.
     * @param initial the initial location
     * @param destination the destination
     * @return the path, empty if the target is completely unreachable
     */
    public Pair<Boolean, List<Location>> searchApproximate(final Location initial, final Location destination) {
        return  search(initial, destination);
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
        Set<Location> result = new HashSet<>();
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
        LinkedList<Location> result = new LinkedList<>();
        Location left = current.delta(-1, 0);
        Location right = current.delta(1, 0);
        Location bottom = current.delta(0, 1);
        Location top = current.delta(0, -1);

        if (!isBlocked.invoke(left) && isPassable.invoke(left)) {
            result.add(left);
        }
        if (!isBlocked.invoke(right) && isPassable.invoke(right)) {
            result.add(right);
        }
        if (!isBlocked.invoke(bottom) && isPassable.invoke(bottom)) {
            result.add(bottom);
        }
        if (!isBlocked.invoke(top) && isPassable.invoke(top)) {
            result.add(top);
        }
        if (!isBlocked.invoke(left) && !isBlocked.invoke(top)) {
            Location c = current.delta(-1, -1);
            if (!isBlocked.invoke(c) && isPassable.invoke(c)) {
                result.add(c);
            }
        }
        if (!isBlocked.invoke(left) && !isBlocked.invoke(bottom)) {
            Location c = current.delta(-1, 1);
            if (!isBlocked.invoke(c) && isPassable.invoke(c)) {
                result.add(c);
            }
        }
        if (!isBlocked.invoke(right) && !isBlocked.invoke(top)) {
            Location c = current.delta(1, -1);
            if (!isBlocked.invoke(c) && isPassable.invoke(c)) {
                result.add(c);
            }
        }
        if (!isBlocked.invoke(right) && !isBlocked.invoke(bottom)) {
            Location c = current.delta(1, 1);
            if (!isBlocked.invoke(c) && isPassable.invoke(c)) {
                result.add(c);
            }
        }
        return result;
    }
}
