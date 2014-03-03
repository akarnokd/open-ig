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
 * A deferred void function call that forwards the
 * exception to an async result.
 * @author akarnokd, 2013.05.02.
 *
 * @param <E> the exception type
 */
public abstract class DeferredVoid<E extends IOException> implements Runnable {
	/** The async result to call. */
	private final AsyncResult<? super Void, ? super IOException> action;
	/**
	 * Constructor.
	 * @param action the wrapped action
	 */
	public DeferredVoid(AsyncResult<? super Void, ? super IOException> action) {
		this.action = action;
	}
	@Override
	public final void run() {
		try {
			invoke();
		} catch (IOException ex) {
			action.onError(ex);
		}
	}
	/**
	 * The user customizable action that returns a value or exception.
	 * @throws E the exception
	 */
	public abstract void invoke() throws E;
}