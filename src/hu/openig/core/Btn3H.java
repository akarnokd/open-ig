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
 * A three state button with "", "_hovered" and "_pressed" postfixed images.
 * @author akarnokd, Mar 20, 2011
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Btn3H {
	/** The resource name. */
	String name();
}
