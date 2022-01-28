/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.Objects;

/**
 * A answer for the Phsychologist's test.
 * @author akarnokd, 2011.04.20.
 */
public class TestAnswer {
    /** The answer id. */
    public final String id;
    /** The label to display. */
    public String label;
    /** Is this option selected? */
    public boolean selected;
    /** The points awarded for this answer. */
    public int points;
    /**
     * Constructor, sets the answer's identifier.
     * @param id the identifier
     */
    public TestAnswer(String id) {
        this.id = Objects.requireNonNull(id);
    }
}
