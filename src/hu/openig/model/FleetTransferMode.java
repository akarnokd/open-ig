/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The transfer mode options when moving ships and vehicles
 * between fleets.
 * @author akarnokd, 2013.04.27.
 */
public enum FleetTransferMode {
    /** Transfer a single object. */
    ONE,
    /** Transfer half of the objects. */
    HALF,
    /** Transfer all of the objects. */
    ALL
    ;
    /**
     * Interprets the message attribute as fleet transfer mode ordinal or name string.
     * @param o the object
     * @param defaultValue the default value
     * @return the enum value
     */
    public static FleetTransferMode from(Object o, FleetTransferMode defaultValue) {
        if (o instanceof Number) {
            int idx = ((Number)o).intValue();
            if (idx >= 0 && idx < values().length) {
                return values()[idx];
            }
        } else
        if (o instanceof CharSequence) {
            for (FleetTransferMode e : values()) {
                if (e.name().equals(o)) {
                    return e;
                }
            }
        }
        return defaultValue;
    }
}
