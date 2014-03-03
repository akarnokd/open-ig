/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.net;

import hu.openig.core.Func1;
import hu.openig.utils.Exceptions;
import hu.openig.utils.U;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * The game listener object, managing connections,
 * login and message exchanges.
 * 
 * @author akarnokd, 2013.04.22.
 */
public class MessageListener implements Closeable {
	/** The server socket. */
	protected volatile ServerSocket server;
	/** The bind address. */
	protected final InetAddress local;
	/** The bind port. */
	protected final int port;
	/** The executor service to handle connections and message exchanges. */
	protected final ExecutorService exec;
	/** The accepter future object. */
	protected Future<?> accepter;
	/** The list of active client connections. */
	protected final List<MessageConnection> clients = new Vector<>();
	/** The event handler called after accepting a connection. */
	protected Func1<MessageConnection, Boolean> onConnection;
	/** Is the listener closed manually? */
	protected volatile boolean closed;
	/**
	 * Constructor, sets the address and port, but
	 * does not bind the server.
	 * @param local the local address
	 * @param port the port
	 * @param exec the executor service for blocking waits and message exchanges
	 */
	public MessageListener(InetAddress local, int port, ExecutorService exec) {
		this.local = local;
		this.port = port;
		this.exec = exec;
	}
	/**
	 * Establish the connection.
	 */
	public void connect() {
		closed = false;
		try {
			server = new ServerSocket();
			server.bind(new InetSocketAddress(local, port));
			accepter = exec.submit(new Runnable() {
				@Override
				public void run() {
					acceptConnections();
				}
			});
		} catch (IOException ex) {
			Exceptions.add(ex);
		}
	}
	@Override
	public void close() throws IOException {
		ServerSocket s = server;
		server = null;
		if (s != null) {
			closed = true;
			s.close();
		}
        if (accepter != null) {
            accepter.cancel(true);
            accepter = null;
        }
		U.close(clients);
		clients.clear();
	}
	/**
	 * Blocking call that accepts incoming connections.
	 */
	protected void acceptConnections() {
		try {
			while (!Thread.currentThread().isInterrupted()) {
                ServerSocket s = server;
                if (s != null) {
                    Socket socket = s.accept();
                    MessageConnection conn = new MessageConnection(socket, clients);
                    if (onConnection == null || onConnection.invoke(conn)) {
                        clients.add(conn);
                        exec.execute(conn);
                    }
                }
			}
		} catch (IOException ex) {
			if (!closed) {
				Exceptions.add(ex);
			}
		}
	}
	/**
	 * Sets the onConnection event handler. Set
	 * it to <code>null</code> to disable conenction filtering.
	 * @param onConnection the connection handler
	 */
	public void setOnConnection(Func1<MessageConnection, Boolean> onConnection) {
		this.onConnection = onConnection;
	}
	/**
	 * Returns the current onConnection event handler.
	 * @return the event handler or null if not set
	 */
	public Func1<MessageConnection, Boolean> getOnConnection() {
		return onConnection;
	}
}
