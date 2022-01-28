/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The spacewar script results.
 * @author akarnokd, 2012.02.10.
 */
public enum SpacewarScriptResult {
    /** Continue as normal. */
    CONTINUE,
    /** Instant win. */
    PLAYER_WIN,
    /** Instant lose. */
    PLAYER_LOSE,
}
