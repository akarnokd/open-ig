/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A location and static image of a position within the ship
 * walks.
 * @author akarnokd, 2009.10.09.
 */
public class WalkPosition {
    /** The back reference to the owner ship. */
    public final WalkShip ship;
    /** The walk position id. */
    public final String id;
    /** The static image of the position. */
    public String pictureName;
    /** The list of possible transitions. */
    public final List<WalkTransition> transitions = new ArrayList<>();
    /**
     * Constructor, initializes the id and ship fields.
     * @param id the identifier
     * @param ship the ship reference
     */
    public WalkPosition(String id, WalkShip ship) {
        this.id = Objects.requireNonNull(id);
        this.ship = Objects.requireNonNull(ship);
    }
}
