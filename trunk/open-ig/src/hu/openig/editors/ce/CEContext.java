/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.utils.XElement;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.undo.UndoManager;

/**
 * The editor's context that lets the panels communicate with each other
 * if necessary.
 * @author akarnokd, 2012.10.31.
 */
public interface CEContext {
	/**
	 * Returns an XML resource.
	 * @param resource the resource path
	 * @return the XML file
	 */
	XElement getXML(String resource);
	/**
	 * Returns a plain text resource.
	 * @param resource the resource path
	 * @return the lines
	 */
	List<String> getText(String resource);
	/**
	 * Returns the raw data of a resource.
	 * @param resource the resource path
	 * @return the bytes
	 */
	byte[] getData(String resource);
	/**
	 * Returns an image resource.
	 * @param resource the resource path
	 * @return the image
	 */
	BufferedImage getImage(String resource);
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
	/** @return the default language to display the multi-language labels of the current project (not the editor's own language). */
	String projectLanguage();
	/**
	 * Returns the label for the specified language and key.
	 * @param language the language
	 * @param key the key
	 * @return the translation or null if not present
	 */
	String label(String language, String key);
	/**
	 * Returns the label for the project language and key.
	 * @param key the key
	 * @return the translation or null if not present
	 */
	String label(String key);
	/**
	 * Update the label for the project language and key.
	 * @param key the key
	 * @param value the value
	 */
	void setLabel(String key, String value);
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
	 * Save the specified resource.
	 * @param resource the resource path
	 * @param xml the xml to save
	 */
	void saveXML(String resource, XElement xml);
	/**
	 * Save the given lines of text.
	 * @param resource the resource path
	 * @param lines the lines
	 */
	void saveText(String resource, Iterable<String> lines);
	/**
	 * Save the given text.
	 * @param resource the resource path
	 * @param text the text
	 */
	void saveText(String resource, CharSequence text);
	/**
	 * Save the given raw data.
	 * @param resource the resource
	 * @param data the data bytes
	 */
	void saveData(String resource, byte[] data);
	/**
	 * Save the given image.
	 * @param resource the resource
	 * @param image the image
	 */
	void saveImage(String resource, BufferedImage image);
	/**
	 * Deletes the specified resource.
	 * @param resource the resource path
	 */
	void delete(String resource);
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
	/**
	 * Check if the given resource exists.
	 * @param resource the resource
	 * @return true if exists
	 */
	boolean exists(String resource);
	/** @return the main player's race. */
	String mainPlayerRace();
	/**
	 * Check if a label exists.
	 * @param key the label key
	 * @return true if exists
	 */
	boolean hasLabel(String key);
	/** @return the supported languages. */
	List<String> languages();
	/**
	 * Get a resource for the specified language (or generic).
	 * @param language the language
	 * @param resource the resource name with extension
	 * @return the data bytes or null if not found
	 */
	byte[] getData(String language, String resource);
	/** @return the global undo manager. */
	UndoManager undoManager();
	/** Notify the window that the undo manager has changed. */
	void undoManagerChanged();
}
