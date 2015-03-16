/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

/**
 * An action without parameters.
 * @author akarnokd, 2011.12.08.
 * @param <E> the exception
 */
public interface Action0E<E extends Exception> {
	/**
	 * Invoke the action.
	 * @throws E on error
	 */
	void invoke() throws E;
}
