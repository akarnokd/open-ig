/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.gfx;

import hu.openig.v1.core.ResourceLocator;

/**
 * Indicator interface, that the object loads some of its resources.
 * Might be used in conjunction with the annotated resources.
 * @author karnok, 2009.11.09.
 * @version $Revision 1.0$
 */
public interface ResourceSelfLoader {
	/**
	 * Load the resources.
	 * @param rl the resource locator to use
	 * @param language the language to use
	 */
	void load(ResourceLocator rl, String language);
}
