/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.gfx;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Multiphase animation with equal X width.
 * @author karnok, 2009.11.09.
 * @version $Revision 1.0$
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
