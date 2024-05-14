/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Location;
import hu.openig.model.WarUnit;
import hu.openig.utils.Exceptions;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

public class PathPlanner {

    /** Plan only the given amount of paths per tick. */
    static final int PATHS_PER_TICK = 10;
    /** Common thread pool to use for pathing executions. */
    ScheduledExecutorService commonExecutorPool;
    /** A matrix with weighs on individual locations. */
    PathWeightMap pathWeightMap;
    /** An ordered map of units:paths planned. */
    final LinkedHashMap<WarUnit, PathPlanning> pathsToPlan = new LinkedHashMap<>();

    public PathPlanner(ScheduledExecutorService commonExecutorPool, PathWeightMap pathWeightMap) {
        this.commonExecutorPool = commonExecutorPool;
        this.pathWeightMap = pathWeightMap;

    }

    public void addPathPlanning(WarUnit unit, Location goal)  {
        pathsToPlan.put(unit, new PathPlanning(unit, goal, pathWeightMap));
    }
    /**
     * Execute path plannings asynchronously.
     */
    public void doPathPlannings() {
        if (pathsToPlan.size() > 0) {

            // map all units to locations
//            long t0 = System.nanoTime();

            List<Future<PathPlanning>> inProgress = new LinkedList<>();
            Iterator<Map.Entry<WarUnit, PathPlanning>> it = pathsToPlan.entrySet().iterator();
            int i = PATHS_PER_TICK;
            while (i-- > 0 && it.hasNext()) {
                Map.Entry<WarUnit, PathPlanning> ppi = it.next();
                it.remove();
                inProgress.add(commonExecutorPool.submit(ppi.getValue()));
            }
            for (Future<PathPlanning> f : inProgress) {
                try {
                    // If failed to find a path, try again, probably blocked by other units
                    if (f.get().pathFound) {
                        f.get().apply();
                    } else {
                        if (f.get().pathingAttempts > 0) {
                            pathsToPlan.put(f.get().unit , f.get());
                        }
                    }
                } catch (ExecutionException | InterruptedException ex) {
                    Exceptions.add(ex);
                }
            }

//            t0 = System.nanoTime() - t0;
//            System.out.printf("Planning %.6f%n", t0 / 1000000000d);
        }
    }
}
