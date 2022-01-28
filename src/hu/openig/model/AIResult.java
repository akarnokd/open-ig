/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The result of a planning step.
 * @author akarnokd, 2011.12.30.
 */
public enum AIResult {
    /** Action was successful. */
    SUCCESS,
    /** No technology available.*/
    NO_AVAIL,
    /** No room for deployment/construction. */
    NO_ROOM,
    /** Not enough money. */
    NO_MONEY,
    /** No action was applicable, continue. */
    CONTINUE,
}
