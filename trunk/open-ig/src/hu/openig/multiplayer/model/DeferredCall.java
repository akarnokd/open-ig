/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer.model;

import java.io.IOException;

/**
 * An abstract class which collects the result or exception from the execution
 * of its <code>invoke()</code> method executed on a different thread.
 * @author akarnokd, 2013.04.26.
 */
public abstract class DeferredCall implements Runnable {
	/** The result object. */
	protected Object result;
	/** The error object. Null if no error happened. */
	protected IOException error;
	@Override
	public final void run() {
		try {
			result = invoke();
		} catch (IOException ex) {
			error = ex;
		}
	}
	/**
	 * Returns the error exception or {@code null} if no error happened.
	 * @return the exception
	 */
	public IOException error() {
		return error;
	}
	/**
	 * Returns the computation result, this might be {@code null} as
	 * a valid response.
	 * @return the result
	 */
	public Object result() {
		return result;
	}
	/**
	 * The concrete function call that produces a value or exception.
	 * @return the result
	 * @throws IOException the error
	 */
	protected abstract Object invoke() throws IOException;
	/**
	 * Checks if the call resulted in an error.
	 * @return true if error happened
	 */
	public boolean hasError() {
		return error != null;
	}
	/**
	 * Called on the original thread.
	 * This can be overridden to run cleanup on
	 * the produced result. By default, it just throws the exception
	 * or converts a MessageIO object into MessageObject
	 * @throws IOException on error
	 */
	public void done() throws IOException {
		if (error != null) {
			throw error;
		}
		if (result instanceof MessageObjectIO) {
			result = ((MessageObjectIO)result).toMessage();
		}
	}
}