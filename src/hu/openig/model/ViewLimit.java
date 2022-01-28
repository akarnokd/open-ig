/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.Rectangle;

/** The view limit records. */
public class ViewLimit {
    /** The inner limit if non-null. */
    public Rectangle inner;
    /** The outer limit if non-null. */
    public Rectangle outer;
}
