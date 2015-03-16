/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A three phased button with normal, _selected_pressed and _selected phases.
 * @author akarnokd, 2009.11.09.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Btn3 {
	/** The path to the resource. */
	String name();
}
