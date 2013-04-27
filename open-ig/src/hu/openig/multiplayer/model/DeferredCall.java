/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer.model;

import hu.openig.core.Result;
import hu.openig.net.MissingAttributeException;

import java.io.IOException;

/**
 * An abstract class which collects the result or exception from the execution
 * of its <code>invoke()</code> method executed on a different thread.
 * @author akarnokd, 2013.04.26.
 */
public abstract class DeferredCall implements Runnable {
	/** The result object. */
	protected Result<? extends Object, IOException> result;
	@Override
	public final void run() {
		try {
			result = invoke();
		} catch (MissingAttributeException ex) {
			result = Result.newError(new ErrorResponse(ErrorType.ERROR_FORMAT, ex.toString()));
		}
	}
	/**
	 * The concrete function call that produces a value or exception.
	 * @return the result
	 */
	protected abstract Result<? extends Object, IOException> invoke();
	/**
	 * Called on the original thread.
	 * This can be overridden to run cleanup on
	 * the produced result. By default, it just throws the exception
	 * or converts a MessageIO object into MessageObject
	 */
	public void done() {
		if (!result.isError()) {
			if (result.value() instanceof MessageObjectIO) {
				result = Result.newValue((Object)((MessageObjectIO)result.value()).toMessage());
			}
		}
	}
	/**
	 * Returns the result object.
	 * @return the result object
	 */
	public Result<? extends Object, IOException> result() {
		return result;
	}
}