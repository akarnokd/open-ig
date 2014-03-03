/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

/**
 * Interface to handle a result or exception of
 * an asynchronous call.
 * @author akarnokd, 2013.04.27.
 * @param <T> the result type
 * @param <E> the error type
 * @see Result
 */
public interface AsyncResult<T, E> {
	/**
	 * The success callback.
	 * @param value the return value
	 */
	void onSuccess(T value);
	/**
	 * The error callback.
	 * @param ex the exception
	 */
	void onError(E ex);
}
