/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;


import hu.openig.core.Location;

import java.util.List;

/**
 * Interface class for units using pathfinding.
 */
public interface WarUnit extends HasLocation, Owned {

    /** @return the attack target of the WarUnit. */
    WarUnit getAttackTarget();
    /** @return attack move location of the WarUnit. */
    Location attackMoveLocation();
    /** @return next movement target location in the path of the WarUnit. */
    Location nextMove();
    /** @return next rotation target location of the WarUnit. */
    Location nextRotate();
    /**
     * Merges the new path.
     * @param newPath the new path to follow
     */
    void mergePath(List<Location> newPath);
}
