/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

/**
 * Indicator interface to let the panel save or load the values
 * of the current project.
 * @author akarnokd, 2012.10.31.
 */
public interface CEProjectLoadSave {
    /**
     * Load the necessary resources.
     */
    void load();
    /**
     * Save the resources.
     */
    void save();
}
