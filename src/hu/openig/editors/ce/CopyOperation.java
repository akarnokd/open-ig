/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

/** The copy operation. */
public enum CopyOperation {
    /** Reference the data file. */
    REFERENCE,
    /** Create a full copy of the datafile. */
    COPY,
    /** Have an empty datafile. */
    BLANK
}