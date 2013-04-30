/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

/**
 * A simple action with one parameter and exception output.
 * @author akarnokd, 2013-04-23
 * @param <T> the first parameter type
 * @param <E> the exception type
 */
public interface Action1E<T, E extends Exception> {
	/**
	 * Invoke the action.
	 * @param param1 the first parameter
	 * @throws E on error
	 */
	void invoke(T param1) throws E;
}
