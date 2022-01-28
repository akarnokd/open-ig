/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

/**
 * Difficulty level.
 * @author akarnokd, 2010.01.16.
 */
public enum Difficulty {
    /** Easy mode. */
    EASY("difficulty.easy"),
    /** Normal mode. */
    NORMAL("difficulty.normal"),
    /** Hard mode. */
    HARD("difficulty.hard");
    /** The label associated with the difficulty. */
    public final String label;
    /**
     * Constructor.
     * @param label the label to set
     */
    Difficulty(String label) {
        this.label = label;
    }
}
