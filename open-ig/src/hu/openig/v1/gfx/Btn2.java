/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.gfx;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A two phase button image.
 * @author karnok, 2009.11.09.
 * @version $Revision 1.0$
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Btn2 {
	/** The pase resource name. */
	String name();
}
