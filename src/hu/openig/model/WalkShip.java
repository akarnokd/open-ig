/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Record describing a particular ship walk standing positions.
 * @author akarnokd, 2009.10.09.
 */
public class WalkShip {
    /** The ship walk level. */
    public String level;
    /** The map of positions in the ship. */
    public final Map<String, WalkPosition> positions = new HashMap<>();
}
