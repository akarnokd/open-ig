/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.editors;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Annotation to indicate wich UI elements to rename. */
@Retention(RetentionPolicy.RUNTIME)
@interface Rename {
	/** The target label id. */
	String to();
	/** The optional tooltip. */
	String tip() default "";
}
