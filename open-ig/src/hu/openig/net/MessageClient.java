/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.net;

import hu.openig.core.Action1;
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
import java.util.concurrent.ExecutorService;
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
	 * @throws IOException on error
	 */
	public Object query(MessageSerializable request) throws IOException {
		request.save(writer);
		writer.flush();
		return MessageObject.parse(reader);
	}
	/**
	 * Send a raw query and parse the response, blocking in the process.
	 * @param request the request message
	 * @return the response object
	 * @throws IOException on error
	 */
	public Object query(CharSequence request) throws IOException {
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
	 * @throws IOException on error
	 */
	public Future<?> query(MessageSerializable request, 
			ExecutorService waiter, 
			final Action1<Object> onResponse) throws IOException {
		request.save(writer);
		writer.flush();
		return waiter.submit(new Runnable() {
			@Override
			public void run() {
				try {
					onResponse.invoke(MessageObject.parse(reader));
				} catch (IOException ex) {
					onResponse.invoke(ex);
				}
			}
		});
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
	 * @throws IOException on error
	 */
	public Future<?> query(CharSequence request, 
			ExecutorService waiter, 
			final Action1<Object> onResponse) throws IOException {
		writer.append(request);
		writer.flush();
		return waiter.submit(new Runnable() {
			@Override
			public void run() {
				try {
					onResponse.invoke(MessageObject.parse(reader));
				} catch (IOException ex) {
					onResponse.invoke(ex);
				}
			}
		});
	}
}
