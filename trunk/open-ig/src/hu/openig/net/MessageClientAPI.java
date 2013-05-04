/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.net;

import hu.openig.core.AsyncResult;
import hu.openig.core.Scheduler;

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
	 * Send a raw query and parse the response, blocking in the process.
	 * @param request the request message
	 * @return the response object or error
	 * @throws IOException on communication error or message error
	 */
	Object query(CharSequence request) throws IOException;
	
	/**
	 * Send a request and await the answer asynchronously on
	 * the given thread pool and response processor.
	 * @param request the request object
	 * @param waiter the waiter thread pool, should be a single threaded
	 * or striped thread pool to avoid overlapping reads of subsequent
	 * async queries.
	 * @param onResponse the response message receiver, might receive an exception object
	 * @return the future of the async wait
	 */
	Future<?> query(
			MessageSerializable request, 
			Scheduler waiter, 
			final AsyncResult<Object, ? super IOException> onResponse);
	/**
	 * Send a request and await the answer asynchronously on
	 * the given thread pool and response processor.
	 * @param request the request object
	 * @param waiter the waiter thread pool, should be a single threaded
	 * or striped thread pool to avoid overlapping reads of subsequent
	 * async queries.
	 * @param onResponse the response message receiver, might receive an exception object
	 * @return the future of the async wait
	 */
	Future<?> query(
			CharSequence request, 
			Scheduler waiter, 
			final AsyncResult<Object, ? super IOException> onResponse);

}
