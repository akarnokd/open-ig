/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.net;

import hu.openig.core.Action2E;
import hu.openig.utils.Exceptions;
import hu.openig.utils.U;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

/**
 * The listener's client connection manager object: parsing requests and
 * serializing responses.
 * @author akarnokd, 2013.04.22.
 *
 */
public class MessageConnection implements Runnable, Closeable {
	/** The backing socket connection. */
	protected final Socket socket;
	/** The incoming data reader. */
	protected final Reader reader;
	/** The outgoing data writer. */
	protected final Writer writer;
	/** The incoming message handler. */
	protected Action2E<MessageConnection, Object, IOException> onMessage;
	/** The current message that needs to be answered. */
	protected final List<Object> messages = new Vector<>();
	/** The client registry. */
	protected final List<MessageConnection> clients;
	/**
	 * Constructor.
	 * @param socket the client connection
	 * @param clients the client registry
	 * @throws IOException on error
	 */
	public MessageConnection(Socket socket, List<MessageConnection> clients) throws IOException {
		this.socket = socket;
		this.clients = clients;
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
		writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
	}
	@Override
	public void close() throws IOException {
		U.close(writer, reader, socket);
		clients.remove(this);
	}
	@Override
	public void run() {
		try {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Object msg = MessageObject.parse(reader);
					messages.add(msg);
					if (onMessage != null) {
						try {
							onMessage.invoke(this, msg);
						} catch (ErrorResponse ex) {
							error(msg, ex.code.ordinal(), ex.getMessage());
						}
					} else {
						error(msg, 4, "Message handler missing on server.");
					}
				} catch (MessageSyntaxError ex) {
					error(null, 1, ex.toString());
				} catch (EOFException ex) {
					break;
				}
				
			}
		} catch (IOException ex) {
			if (!socket.isClosed()) {
				Exceptions.add(ex);
			}
		} finally {
			clients.remove(this);
		}
	}
	/**
	 * Send a message back.
	 * @param requestMessage the original request message that needs to match up with the current known message
	 * @param responseMessage the response message object
	 * @throws IOException on error
	 */
	public void send(Object requestMessage, MessageSerializable responseMessage) throws IOException {
		checkRequest(requestMessage);
		responseMessage.save(writer);
		writer.flush();
		messages.remove(0);
	}
	/**
	 * Respond with an error code and message.
	 * @param requestMessage the request message to match
	 * @param code the error code
	 * @param message the error message
	 * @throws IOException on error
	 */
	public void error(Object requestMessage, int code, String message) throws IOException {
		MessageObject mo = new MessageObject("ERROR");
		mo.set("code", code);
		mo.set("message", message);
		send(requestMessage, mo);
	}
	/**
	 * Checks if the given request message is the next to answer.
	 * @param requestMessage the request message, if null, the message sequence is not checked
	 * @throws IOException if the message is not the next one
	 */
	public void checkRequest(Object requestMessage) throws IOException {
		if (requestMessage == null) {
			return;
		}
		if (messages.isEmpty()) {
			throw new IOException("No request object");
		}
		Object sequenceMessage = messages.get(0);
		if (requestMessage != sequenceMessage) {
			throw new IOException("Not answering the next request!");
		}
	}
	/**
	 * Sends a reply to the given request message in a raw format.
	 * @param requestMessage the original request message that needs to match up with the current known message
	 * @param responseMessage the response message object
	 * @throws IOException on error
	 */
	public void send(Object requestMessage, CharSequence responseMessage) throws IOException {
		checkRequest(requestMessage);
		writer.append(responseMessage);
		writer.flush();
		messages.remove(0);
	}
	/**
	 * Sets the message handler.
	 * @param newOnMessage the message handler.
	 */
	public void setOnMessage(Action2E<MessageConnection, Object, IOException> newOnMessage) {
		this.onMessage = newOnMessage;
	}
	/**
	 * Returns the current message handler.
	 * @return the message handler, may be null
	 */
	public Action2E<MessageConnection, Object, IOException> getOnMessage() {
		return onMessage;
	}
}