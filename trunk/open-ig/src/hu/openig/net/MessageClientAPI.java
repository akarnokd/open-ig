/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.net;

import hu.openig.core.AsyncResult;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * Base interface for sending and receiving message objects
 * and arrays.
 * @author akarnokd, 2013.05.04.
 */
public interface MessageClientAPI {
	/**
	 * Send a query and parse the response, blocking in the process.
	 * @param request the request message
	 * @return the response object
	 * @throws IOException on communication error or message error
	 */
	Object query(MessageSerializable request) throws IOException;
	/**
	 * Sends a message on an asynchronous thread, awaits
	 * the result and executes the response handler on a different thread.
	 * @param request the request object
	 * @param onResponse the async response handler
	 * @return the future for the entire query
	 */
	Future<?> query(
			MessageSerializable request, 
			final AsyncResult<Object, ? super IOException> onResponse);

}
