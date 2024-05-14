/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;


import hu.openig.core.Location;
import hu.openig.core.Pathfinding;

import java.util.LinkedList;
import java.util.List;

/**
 * Interface class for units using pathfinding.
 */
public interface WarUnit extends HasLocation, Owned {

    /** @return true if the unit has planned moves. */
    boolean hasPlannedMove();
    void setHasPlannedMove(boolean hasPlannedMove);
    /** @return true if the is destroyed. */
    boolean isDestroyed();
    void setAngle(double angle);
    double getAngle();
    void increaseAngle(double angle);
    int getAngleCount();
    int getRotationTime();
    int getMovementSpeed();
    /** @return the attack target of the WarUnit. */
    WarUnit getAttackTarget();
    /** @return attack move location of the WarUnit. */
    Location attackMoveLocation();
    /** @return next movement target location in the path of the WarUnit. */
    Location getNextMove();
    void setNextMove(Location nextMove);
    void clearNextMove();
    /** @return next rotation target location of the WarUnit. */
    Location getNextRotate();
    void setNextRotate(Location nextRotate);
    void clearNextRotate();
    /** @return true if the unit is moving. */
     boolean isMoving();
    /** @return true if the unit is in between cells. */
    boolean inMotion();
    LinkedList<Location> getPath();

    void setPathingMethod(Pathfinding pathing);
    Pathfinding getPathingMethod();
    /**
     * Merges the new path.
     * @param newPath the new path to follow
     */
    void setPath(List<Location> newPath);
}
