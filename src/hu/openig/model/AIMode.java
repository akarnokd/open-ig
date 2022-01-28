/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * Enumeration for AI behavior of players.
 * Resource allocation is balanced between defensive stuff, offensive stuff, social stuff.
 * @author akarnokd, Jul 31, 2011
 */
public enum AIMode {
    /** Player controlled. */
    NONE,
    /** Special AI attacking trader ships only. */
    PIRATES,
    /** Special AI creating fleets of trader ships and moving between planets with trader's spaceport. */
    TRADERS,
    /** The default AI stance balancing between expenses defined by the ratio parameters. */
    DEFAULT,
    /** Special AI combining the default AI and User AI. */
    TEST
}
