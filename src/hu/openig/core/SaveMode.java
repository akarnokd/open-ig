/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

/**
 * Represents the save mode.
 * @author akarnokd, 2011.12.29.
 */
public enum SaveMode {
    /** The user did a manual save. */
    MANUAL,
    /** Automatic save once a day. */
    AUTO,
    /** User quick save. */
    QUICK,
    /** Similar to manual save but fixed name. */
    LEVEL
}
