/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * Record to track achievement numerical progress.
 * @author akarnokd, 2022. febr. 3.
 */
public final class AchievementProgress {
    /** The name of the achievement. */
    public final String name;
    /** The current progress. */
    public double progress;
    /** The amount to reach for the achievement to be granted. */
    public double max;
    /** If true, the achievement numerical progress is displayed on the dialog as well. */
    public boolean displayProgress;
    /** Set to true if the achievement was awarded when there was no progress tracking. */
    public boolean legacy;
    /**
     * Constructs an empty achievement progress for the given achievement name.
     * @param name the name
     */
    public AchievementProgress(String name) {
        this.name = name;
    }
    /**
     * Checks if this achievement has reached its progress goal.
     * @return true if the progress reached the expected max
     */
    public boolean isComplete() {
        return legacy || (progress >= max && max > 0);
    }
}
