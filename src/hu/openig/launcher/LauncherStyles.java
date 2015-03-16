/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.launcher;

import java.awt.Color;
import java.awt.Font;

/**
 * The font and color style of the launcher.
 * @author akarnokd, 2012.09.20.
 */
public interface LauncherStyles {
	/** @return the background color. */
	Color backgroundColor();
	/** @return the default text color. */
	Color foreground();
	/** @return a medium sized font. */
	Font fontMedium();
}
