/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.net;

import hu.openig.core.AsyncResult;
import hu.openig.core.Scheduler;
import hu.openig.core.Schedulers;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A game client sending and receiving messages in
 * blocking fashion.
 * @author akarnokd, 2013.04.22.
 */
public class MessageClient implements Closeable, MessageClientAPI {
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
	/** The message sending pool. */
	protected ExecutorService sendPool;
	/** The scheduler where the received message is sent to the async listener. */
	protected final Scheduler receivePool;
	/**
	 * Constructor, uses the EDT to process the receive result.
	 * @param address the endpoint address
	 * @param port the port address
	 */
	public MessageClient(InetAddress address, int port) {
		this.address = address;
		this.port = port;
		receivePool = Schedulers.edt();
	}
	/**
	 * Constructor.
	 * @param address the endpoint address
	 * @param port the port address
	 * @param receivePool where the asyncresult is notified.
	 */
	public MessageClient(InetAddress address, int port, Scheduler receivePool) {
		this.address = address;
		this.port = port;
		this.receivePool = receivePool;
	}
	/**
	 * Establish a connection.
	 * @throws IOException on connection error
	 */
	public void connect() throws IOException {
		ThreadPoolExecutor tp = new ThreadPoolExecutor(1, 1, 3, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		tp.allowCoreThreadTimeOut(true);
		sendPool = tp;
		
		socket = new Socket(address, port);
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
		writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
	}
	@Override
	public void close() throws IOException {
		ExecutorService sp = sendPool;
		if (sp != null) {
			sp.shutdown();
			sendPool = null;
		}
		U.close(writer, reader, socket);
		socket = null;
		reader = null;
		writer = null;
	}
	@Override
	public Object query(final MessageSerializable request) throws IOException {
		Future<Object> f = sendPool.submit(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				if (writer == null) {
					throw new IOException("MessageClient not connected");
				}
				request.save(writer);
				writer.flush();
				return MessageObject.parse(reader);
			}
		});
		try {
			return f.get();
		} catch (InterruptedException | ExecutionException e) {
			if (e.getCause() instanceof IOException) {
				throw (IOException)e.getCause();
			}
			throw new IOException(e);
		}
	}
	
	@Override
	public Future<?> query(
			final MessageSerializable request, 
			final AsyncResult<Object, ? super IOException> onResponse) {
		return sendPool.submit(new Runnable() {
			@Override
			public void run() {
				try {
					if (writer == null) {
						throw new IOException("MessageClient not connected");
					}				
					request.save(writer);
					writer.flush();
					final Object o = MessageObject.parse(reader);
					
					MessageUtils.applyTransform(onResponse, o);
					
					receivePool.schedule(new Runnable() {
						@Override
						public void run() {
							onResponse.onSuccess(o);
						}
					});
				} catch (final IOException ex) {
					receivePool.schedule(new Runnable() {
						@Override
						public void run() {
							onResponse.onError(ex);
						}
					});
				}
			}
		});
	}
}
