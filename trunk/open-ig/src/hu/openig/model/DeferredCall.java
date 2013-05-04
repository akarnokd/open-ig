/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Result;
import hu.openig.net.ErrorResponse;
import hu.openig.net.ErrorType;
import hu.openig.net.MissingAttributeException;

import java.io.IOException;

/**
 * An abstract class which collects the result or exception from the execution
 * of its <code>invoke()</code> method executed on a different thread.
 * @author akarnokd, 2013.04.26.
 */
public abstract class DeferredCall implements Runnable {
	/** The result object. */
	protected Object value;
	/** The error object. */
	protected IOException error;
	@Override
	public final void run() {
		try {
			value = invoke();
		} catch (MissingAttributeException ex) {
			error = new ErrorResponse(ErrorType.ERROR_FORMAT, ex.toString());
		} catch (IOException ex) {
			error = ex;
		}
	}
	/**
	 * The concrete function call that produces a value or exception.
	 * @return the result
	 * @throws IOException on various error cases
	 */
	protected abstract Object invoke() throws IOException;
	/**
	 * Called on the original thread.
	 * This can be overridden to run cleanup on
	 * the produced result. By default, it just throws the exception
	 * or converts a MessageIO object into MessageObject
	 */
	public void done() {
		if (hasValue()) {
			if (value instanceof MessageObjectIO) {
				value = ((MessageObjectIO)value).toMessage();
			}
		}
	}
	/**
	 * Returns the result object.
	 * @return the result object
	 */
	public Result<Object, IOException> asResult() {
		if (hasValue()) {
			return Result.newValue(value);
		}
		return Result.newError(error);
	}
	/**
	 * Does this deferred call contain a value result?
	 * @return true if value is produced
	 */
	public boolean hasValue() {
		return error == null;
	}
	/**
	 * Does this deferred call contain an error?
	 * @return true if error is produced
	 */
	public boolean hasError() {
		return error != null;
	}
	/**
	 * Returns the value object.
	 * @return the value
	 */
	public Object value() {
		if (hasError()) {
			throw new IllegalStateException("is error");
		}
		return value;
	}
	/**
	 * Returns the exception object.
	 * @return the exception
	 */
	public IOException error() {
		if (hasValue()) {
			throw new IllegalStateException("is value");
		}
		return error;
	}
}