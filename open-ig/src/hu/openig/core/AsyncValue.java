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
 * value to an AsyncResult object.
 * @author akarnokd, 2013.04.30.
 */
public final class AsyncValue implements Runnable {
	/** The exception. */
	private final Object value;
	/** The async result object. */
	private final AsyncResult<Object, ? super IOException> onResponse;

	/**
	 * Constructor, sets the fields.
	 * @param value the value
	 * @param onResponse the async callback
	 */
	public AsyncValue(Object value,
			AsyncResult<Object, ? super IOException> onResponse) {
		this.value = value;
		this.onResponse = onResponse;
	}

	@Override
	public void run() {
		onResponse.onSuccess(value);
	}
}