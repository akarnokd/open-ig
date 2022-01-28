/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.core;

/**
 * The road sides constants.
 * @author akarnokd, 2009.05.23.
 */
public final class Sides {
    /** Constructor. */
    private Sides() {
        // constant class
    }
    /** Road has a left exit. */
    public static final int LEFT = 8;
    /** Road has a top exit. */
    public static final int TOP = 4;
    /** Road has a bottom exit. */
    public static final int BOTTOM = 2;
    /** Road has a right exit. */
    public static final int RIGHT = 1;
}
