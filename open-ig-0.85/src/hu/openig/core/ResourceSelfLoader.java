/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;


/**
 * Indicator interface, that the object loads some of its resources.
 * Might be used in conjunction with the annotated resources.
 * @author akarnokd, 2009.11.09.
 */
public interface ResourceSelfLoader {
	/**
	 * Load the resources.
	 * @param rl the resource locator to use
	 * @param language the language to use
	 */
	void load(ResourceLocator rl, String language);
}
