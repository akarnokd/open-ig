/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.launcher;

/**
 * The label manager callbacks.
 * @author akarnokd, 2012.09.20.
 */
public interface LauncherLabels {
	/** 
	 * Returns an exact label for the key.
	 * @param key the key
	 * @return the translated text
	 */
	String label(String key);
	/**
	 * Returns a parametrized label.
	 * @param key the key
	 * @param params the parameters
	 * @return the translated text
	 */
	String format(String key, Object... params);
}
