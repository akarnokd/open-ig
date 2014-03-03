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
 * Multiphase animation with equal X width.
 * @author akarnokd, 2009.11.09.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Anim {
	/** The resource name. */
	String name();
	/** The phase width. */
	int width() default -1;
	/** The number of phases. */
	int step() default -1;
}
