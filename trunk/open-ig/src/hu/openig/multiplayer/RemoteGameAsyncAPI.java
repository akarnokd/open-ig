/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer;

import hu.openig.core.AsyncResult;

import java.io.IOException;

/**
 * The asynchronous version of the RemoteGameAPI.
 * @author akarnokd, 2013.04.27.
 */
public interface RemoteGameAsyncAPI {
	/**
	 * Start a batch request.
	 */
	void begin();
	/**
	 * Finish a batch request and send out the composite requests.
	 */
	void end();
	/**
	 * Finish a batch request and send out the composite requests.
	 * @param out the async completion handler when all responses have
	 * been received and processed.
	 */
	void end(AsyncResult<? super Void, ? super IOException> out);
	/**
	 * A simple ping-pong request pair with the time elapsed in
	 * milliseconds.
	 * @param out the async result
	 */
	void ping(AsyncResult<? super Long, ? super IOException> out);
}
