/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.utils.XElement;

/**
 * The callback interface to help undo or redo a certain change.
 * @author akarnokd, 2012.10.31.
 */
public interface CEUndoRedoSupport {
    /**
     * Restore the state (undo or redo).
     * @param state the state to restore
     */
    void restoreState(XElement state);
}
