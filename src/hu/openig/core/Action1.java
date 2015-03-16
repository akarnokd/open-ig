/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

/**
 * A simple action with one parameter.
 * @author akarnokd, Mar 29, 2011
 * @param <T> the parameter type
 */
public interface Action1<T> {
	/**
	 * Invoke the action.
	 * @param value the parameter.
	 */
	void invoke(T value);
}
