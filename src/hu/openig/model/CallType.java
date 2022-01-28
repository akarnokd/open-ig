/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The alien's call type.
 * @author akarnokd, Apr 22, 2011
 */
public enum CallType {
    /** Aliens surrender. */
    SURRENDER,
    /** Aliens propose alliance. */
    ALLIANCE,
    /** Aliens propose peace. */
    PEACE,
    /** Aliens ask for money. */
    MONEY,
    /** Aliens declare war. */
    WAR,
    /** Aliens demand resign. */
    RESIGN
}
