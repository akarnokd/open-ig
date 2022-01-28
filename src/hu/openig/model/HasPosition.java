/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * Indicates the position of a space object.
 * @author akarnokd, 2014.02.13.
 */
public interface HasPosition {
    /** @return the X coordinate. */
    double x();
    /** @return the Y coordinate. */
    double y();
}
