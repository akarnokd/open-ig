/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.net;

import hu.openig.core.Action1;
import hu.openig.core.AsyncResult;
import hu.openig.core.Scheduler;
import hu.openig.multiplayer.model.ErrorResponse;
import hu.openig.utils.U;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Future;

/**
 * A game client sending and receiving messages in
 * blocking fashion.
 * @author akarnokd, 2013.04.22.
 */
public class MessageClient implements Closeable {
	/** The backing socket connection. */
	protected Socket socket;
	/** The incoming data reader. */
	protected Reader reader;
	/** The outgoing data writer. */
	protected Writer writer;
	/** The endpoint address. */
	protected final InetAddress address;
	/** The endpoint port. */
	protected final int port;
	/**
	 * Constructor.
	 * @param address the endpoint address
	 * @param port the port address
	 * @throws IOException on error
	 */
	public MessageClient(InetAddress address, int port) throws IOException {
		this.address = address;
		this.port = port;
	}
	/**
	 * Establish a connection.
	 * @throws IOException on connection error
	 */
	public void connect() throws IOException {
		socket = new Socket(address, port);
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
		writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
	}
	@Override
	public void close() throws IOException {
		U.close(writer, reader, socket);
		socket = null;
		reader = null;
		writer = null;
	}
	/**
	 * Send a query and parse the response, blocking in the process.
	 * @param request the request message
	 * @return the response object
	 * @throws IOException on communication error or message error
	 */
	public Object query(MessageSerializable request) throws IOException {
		if (writer == null) {
			throw new IOException("MessageClient not connected");
		}
		request.save(writer);
		writer.flush();
		return MessageObject.parse(reader);
	}
	/**
	 * Send a raw query and parse the response, blocking in the process.
	 * @param request the request message
	 * @return the response object or error
	 * @throws IOException on communication error or message error
	 */
	public Object query(CharSequence request) throws IOException {
		if (writer == null) {
			throw new IOException("MessageClient not connected");
		}
		writer.append(request);
		writer.flush();
		return MessageObject.parse(reader);
	}
	
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
	public Future<?> query(
			MessageSerializable request, 
			Scheduler waiter, 
			final AsyncResult<Object, ? super IOException> onResponse) {
		if (writer == null) {
			final IOException ex = new IOException("MessageClient not connected");
			return waiter.schedule(new AsyncException(ex, onResponse));
		}
		try {
			request.save(writer);
			writer.flush();
			
			return readAndDispatchAsync(waiter, onResponse);
		} catch (final IOException ex) { 
			return waiter.schedule(new AsyncException(ex, onResponse));
		}
	}
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
	public Future<?> query(
			CharSequence request, 
			Scheduler waiter, 
			final AsyncResult<Object, ? super IOException> onResponse) {
		if (writer == null) {
			final IOException ex = new IOException("MessageClient not connected");
			return waiter.schedule(new AsyncException(ex, onResponse));
		}
		try {
			writer.append(request);
			writer.flush();
			
			return readAndDispatchAsync(waiter, onResponse);
		} catch (final IOException ex) { 
			return waiter.schedule(new AsyncException(ex, onResponse));
		}
	}
	/**
	 * Reads the response of a query then
	 * dispatches the answer to the given response handler.
	 * @param waiter the scheduler for the execution of the notification
	 * @param onResponse the callback, if it implements Action1&lt;Object>,
	 * its invoke() method is called on the current thread.
	 * @return the future to the async execution
	 * @throws IOException on parsing or I/O error
	 */
	Future<?> readAndDispatchAsync(Scheduler waiter,
			final AsyncResult<Object, ? super IOException> onResponse)
			throws IOException {
		Object response = MessageObject.parse(reader);
		ErrorResponse er = ErrorResponse.asError(response);
		if (er != null) {
			return waiter.schedule(new AsyncException(er, onResponse));
		} else
		if (onResponse instanceof Action1<?>) {
			@SuppressWarnings("unchecked")
			Action1<Object> func1 = (Action1<Object>)onResponse;
			func1.invoke(response);
		}
		return waiter.schedule(new AsyncValue(response, onResponse));
	}
}
