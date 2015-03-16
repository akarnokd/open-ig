/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

/**
 * A simple action with two parameters.
 * @author akarnokd, Mar 29, 2011
 * @param <T> the first parameter type
 * @param <U> the second parameter type
 */
public interface Action2<T, U> {
	/**
	 * Invoke the action.
	 * @param param1 the first parameter
	 * @param param2 the second parameter
	 */
	void invoke(T param1, U param2);
}
