/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marker annotation to load a field with the same name from the XML.
 * @author akarnokd, 2012.07.21.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface LoadField { 
	
}
