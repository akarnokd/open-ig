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
import hu.openig.net.MessageSerializable;
import hu.openig.utils.Exceptions;

import java.io.IOException;

/**
 * A deferred computation with a final transformation
 * step to MessageSerializable in the done call.
 * @author akarnokd, 2013.05.08.
 * @param <U> the intermediate result
 */
public abstract class DeferredTransform<U> implements DeferredRunnable {
	/** The result value. */
	private MessageSerializable value;
	/** The intermediate result. */
	private U temp;
	/** The result error. */
	private IOException error;
	/**
	 * Method that produces the result of type T or
	 * an IOException.
	 * @return the intermediate result value
	 * @throws IOException the IOException
	 */
	protected abstract U invoke() throws IOException;
	/**
	 * Transforms the given intermediate value into the final
	 * serialzable form.
	 * @param intermediate the intermediate value
	 * @return the final value
	 * @throws IOException on error
	 */
	protected abstract MessageSerializable transform(U intermediate) throws IOException;
	@Override
	public final void run() {
		try {
			temp = invoke();
		} catch (ErrorResponse ex) {
			error = ex;
		} catch (IOException ex) {
			error = new ErrorResponse(ErrorType.SERVER_IO, ex);
		} catch (Throwable ex) {
			error = new ErrorResponse(ErrorType.SERVER_IO, ex);
			Exceptions.add(ex);
		}
	}
	@Override
	public final void done() {
		if (error == null) {
			try {
				value = transform(temp);
			} catch (ErrorResponse ex) {
				error = ex;
			} catch (IOException ex) {
				error = new ErrorResponse(ErrorType.SERVER_IO, ex);
			} catch (Throwable ex) {
				error = new ErrorResponse(ErrorType.SERVER_BUG, ex);
				Exceptions.add(ex);
			}
		}
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
