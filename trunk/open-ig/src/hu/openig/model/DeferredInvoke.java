/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.net.ErrorResponse;
import hu.openig.net.ErrorType;
import hu.openig.net.MessageObject;
import hu.openig.net.MessageSerializable;
import hu.openig.utils.Exceptions;

import java.io.IOException;

/**
 * A deferred call with default result.
 * @author akarnokd, 2013.05.08.
 */
public abstract class DeferredInvoke implements DeferredRunnable {
	/** The response value. */
	protected final MessageSerializable value;
	/** The response error. */
	protected IOException error;
	/**
	 * Constructor, sets the default response value
	 * to OK{}.
	 */
	public DeferredInvoke() {
		this("OK");
	}
	/**
	 * Constructor, with the name of the response message
	 * object.
	 * @param responseName the response message object name
	 */
	public DeferredInvoke(String responseName) {
		value = new MessageObject(responseName);
	}
	/**
	 * The method that gets executed.
	 * @throws IOException the IOException
	 */
	protected abstract void invoke() throws IOException;
	@Override
	public final void run() {
		try {
			invoke();
		} catch (ErrorResponse ex) {
			error = ex;
		} catch (IOException ex) {
			error = new ErrorResponse(ErrorType.SERVER_IO, ex);
		} catch (Throwable ex) {
			error = new ErrorResponse(ErrorType.SERVER_BUG, ex);
			Exceptions.add(ex);
		}
	}

	@Override
	public final void done() {
		// no operation
	}
	@Override
	public final MessageSerializable get() throws IOException {
		if (error != null) {
			throw error;
		}
		return value;
	}
	@Override
	public final boolean isError() {
		return error != null;
	}
}
