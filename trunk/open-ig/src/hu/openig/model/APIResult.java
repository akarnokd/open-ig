/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.EDTResult;

import java.io.IOException;

/**
 * A simple abstract alias for an EDT result with IOException type.
 * @author akarnokd, 2013.05.31.
 * @param <T> the success value type
 */
public abstract class APIResult<T> extends EDTResult<T, IOException> {

}
