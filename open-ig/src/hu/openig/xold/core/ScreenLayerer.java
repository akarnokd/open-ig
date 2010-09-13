/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.xold.core;

import javax.swing.JComponent;

/**
 * Callback interface to ask if a component is on the top layer at the moment.
 * @author karnokd
 *
 */
public interface ScreenLayerer {
	/**
	 * Returns true if the given component is visible
	 * and is in the foreground.
	 * @param component the component
	 * @return true if this is a top component
	 */
	boolean isTopScreen(JComponent component);
}
