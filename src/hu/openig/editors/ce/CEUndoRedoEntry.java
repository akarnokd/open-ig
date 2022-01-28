/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.utils.XElement;

import javax.swing.undo.UndoableEdit;

/**
 * The undo-redo state object.
 * @author akarnokd, 2012.10.31.
 */
public class CEUndoRedoEntry implements UndoableEdit {
    /** The panel to apply the changes. */
    public final CEUndoRedoSupport panel;
    /** The old state. */
    public final XElement oldState;
    /** The new state. */
    public final XElement newState;
    /** The activity description. */
    public final String name;
    /**
     * Constructor. Initializes the fields.
     * @param panel the panel
     * @param name the activity description
     * @param oldState the old state
     * @param newState the new state
     */
    public CEUndoRedoEntry(CEUndoRedoSupport panel, String name, XElement oldState, XElement newState) {
        this.panel = panel;
        this.oldState = oldState;
        this.newState = newState;
        this.name = name;
    }
    @Override
    public void undo() {
        panel.restoreState(oldState);
    }
    @Override
    public boolean canUndo() {
        return true;
    }
    @Override
    public void redo() {
        panel.restoreState(newState);
    }
    @Override
    public boolean canRedo() {
        return true;
    }
    @Override
    public void die() {

    }
    @Override
    public boolean addEdit(UndoableEdit anEdit) {
        return false;
    }
    @Override
    public boolean replaceEdit(UndoableEdit anEdit) {
        return false;
    }
    @Override
    public boolean isSignificant() {
        return true;
    }
    @Override
    public String getPresentationName() {
        return name;
    }
    @Override
    public String getUndoPresentationName() {
        return name;
    }
    @Override
    public String getRedoPresentationName() {
        return name;
    }

}
