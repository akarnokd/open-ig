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
 * A runnable implementation which submits a constant
 * IOException to an AsyncResult object.
 * @author akarnokd, 2013.04.30.
 */
public final class AsyncException implements Runnable {
	/** The exception. */
	private final IOException ex;
	/** The async result object. */
	private final AsyncResult<?, ? super IOException> onResponse;

	/**
	 * Constructor, sets the fields.
	 * @param ex the execption
	 * @param onResponse the async callback
	 */
	public AsyncException(IOException ex,
			AsyncResult<?, ? super IOException> onResponse) {
		this.ex = ex;
		this.onResponse = onResponse;
	}

	@Override
	public void run() {
		onResponse.onError(ex);
	}
}