/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Location;

import java.awt.geom.Point2D;

/**
 * Interface for fractional and integral location.
 * @author akarnokd, Feb 4, 2012
 */
public interface HasLocation {
    /** @return the exact fractional location. */
    Point2D.Double exactLocation();
    /** @return the integral location. */
    Location location();
    /** Set new location.
     *  @param x the x coordinate
     *  @param y the x coordinate
     */
    void setLocation(double x, double y);
}
