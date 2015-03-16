/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.utils.XElement;

/**
 * Interface to load and save panel preferences when the application
 * starts or exits.
 * @author akarnokd, 2012.10.31.
 */
public interface CEPanelPreferences {
	/**
	 * Load the preferences from the supplied XML element.
	 * @param preferences the saved preferences in XML format.
	 */
	void loadPreferences(XElement preferences);
	/**
	 * Save the preferences into the supplied XML element.
	 * @param preferences the parent element where to save the preferences
	 */
	void savePreferences(XElement preferences);
	/** @return the preferences identifier to use in the XML. */
	String preferencesId();
}
