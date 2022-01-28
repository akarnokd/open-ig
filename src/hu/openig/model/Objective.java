/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Func0;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a main objective.
 * @author akarnokd, Jan 12, 2012
 */
public class Objective {
    /** The objective id. */
    public String id;
    /** Is the objective visible. */
    public boolean visible = true;
    /** The state. */
    public ObjectiveState state = ObjectiveState.ACTIVE;
    /** The objective's title. */
    public String title;
    /** The objective's short description. */
    public String description;
    /** Optional function to indicate a gauge of progress. */
    public Func0<Double> progress;
    /** Optional function to indicate a progress textually. */
    public Func0<String> progressValue;
    /** The sub objectives. */
    public final List<Objective> subObjectives = new ArrayList<>();
    /** @return is the objective active (visible and ACTIVE state). */
    public boolean isActive() {
        return visible && state == ObjectiveState.ACTIVE;
    }
    /** @return is the objective completed in any way? */
    public boolean isCompleted() {
        return state != ObjectiveState.ACTIVE;
    }
}
