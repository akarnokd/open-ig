/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;


import java.io.IOException;

/**
 * A deferred function call that forwards the result
 * or exception to an async result.
 * @author akarnokd, 2013.05.02.
 *
 * @param <T> the return value type
 * @param <E> the exception type
 * @see DeferredVoid 
 */
public abstract class DeferredAction<T, E extends IOException> implements Runnable {
	/** The async result to call. */
	private final AsyncResult<? super T, ? super IOException> action;
	/**
	 * Constructor.
	 * @param action the wrapped action
	 */
	public DeferredAction(AsyncResult<? super T, ? super IOException> action) {
		this.action = action;
	}
	@Override
	public final void run() {
		try {
			action.onSuccess(invoke());
		} catch (IOException ex) {
			action.onError(ex);
		}
	}
	/**
	 * The user customizable action that returns a value or exception.
	 * @return the result value
	 * @throws E the exception
	 */
	public abstract T invoke() throws E;
}