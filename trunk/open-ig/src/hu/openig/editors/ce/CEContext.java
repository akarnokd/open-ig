/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.utils.XElement;

import java.awt.Component;
import java.io.File;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.undo.UndoManager;

/**
 * The editor's context that lets the panels communicate with each other
 * if necessary.
 * @author akarnokd, 2012.10.31.
 */
public interface CEContext {
	/**
	 * Returns the icon for the given indicator.
	 * @param indicator the indicator enum
	 * @return the image icon
	 */
	ImageIcon getIcon(CESeverityIndicator indicator);
	/**
	 * Returns a fixed translation for the given campaign-editor related label.
	 * @param key the key
	 * @return the translated text
	 */
	String get(String key);
	/**
	 * Returns a formatted string for the given campaign-related label.
	 * @param key the key
	 * @param params the optional parameters
	 * @return the translated text
	 */
	String format(String key, Object... params);
	/**
	 * Update the enclosin tab's icon.
	 * @param c the component who's tab should be changed
	 * @param title the new title, if not null
	 * @param icon the new icon, if null, the icon is removed
	 */
	void updateTab(Component c, String title, ImageIcon icon);
	/**
	 * Store a new undoable state.
	 * @param c the component that can perform the redo
	 * @param name the textual description of the change
	 * @param oldState the old state before the change
	 * @param newState the new state after the change
	 */
	void addUndo(CEUndoRedoSupport c, String name, XElement oldState, XElement newState);
	/**
	 * Adds a problem to the global issue list.
	 * @param severity the severity
	 * @param message the message
	 * @param panel the panel's name
	 * @param c the problem locator component
	 * @param description the description
	 */
	void addProblem(CESeverityIndicator severity, 
			String message,
			String panel,
			CEProblemLocator c, 
			XElement description);
	/**
	 * Remove all problems of the specified panel name.
	 * @param panel the panel name, as given to the addProblem() method
	 */
	void clearProblems(String panel);
	/** @return the working directory. */
	File getWorkDir();
	/** @return the global undo manager. */
	UndoManager undoManager();
	/** Notify the window that the undo manager has changed. */
	void undoManagerChanged();
	/**
	 * Set a new master data record.
	 * @param newData the data
	 */
	void campaignData(CampaignData newData);
	/** @return the current master data record. */
	CampaignData campaignData();
	/** @return the data manager. */
	CEDataManager dataManager();
	/** Load up the controls with the current campaign data. */
	void load();
	/** @return the shared set of recent campaigns. */
	Set<String> getRecent();
	/** 
	 * Add a new recent entry. 
	 * @param s the path
	 */
	void addRecent(String s);
}
