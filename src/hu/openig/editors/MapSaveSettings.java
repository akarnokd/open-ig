/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.editors;

import java.io.File;

/**
 * The save settings for the map editor.
 * @author akarnokd
 */
public class MapSaveSettings {
	/** The filename. */
	public File fileName;
	/** Save the surface data? */
	boolean surface;
	/** Save the building data? */
	boolean buildings;
}
