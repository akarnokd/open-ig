/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer;

import hu.openig.core.Action2E;
import hu.openig.model.DeferredCall;
import hu.openig.net.ErrorResponse;
import hu.openig.net.ErrorType;
import hu.openig.net.MessageArray;
import hu.openig.net.MessageConnection;
import hu.openig.net.MessageObject;
import hu.openig.net.MessageSerializable;
import hu.openig.net.MissingAttributeException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

/**
 * Listens for and dispatches incoming message requests and produces
 * message responses.
 * @author akarnokd, 2013.04.23.
 */
public class RemoteGameListener implements Action2E<MessageConnection, Object, IOException> {
	/** The game API. */
	protected final RemoteGameAPI api;
	/** Use the EDT to execute the API methods? */
	protected final boolean useEDT;
	/**
	 * Constructor, takes a remote API entry point and uses the EDT to call its methods.
	 * @param api the API entry point
	 */
	public RemoteGameListener(RemoteGameAPI api) {
		this.api = api;
		this.useEDT = true;
	}
	/**
	 * Constructor, takes a remote game API and might use the EDT to call its methods.
	 * @param api the API entry point
	 * @param useEDT use EDT for the methods?
	 */
	public RemoteGameListener(RemoteGameAPI api, boolean useEDT) {
		this.api = api;
		this.useEDT = useEDT;
	}
	@Override
	public void invoke(MessageConnection conn, Object message)
			throws IOException {
		DeferredCall responseCall = null;
		try {
			responseCall = processMessageDeferred(message);
		} catch (MissingAttributeException ex) {
			conn.error(message, ErrorType.ERROR_FORMAT.ordinal(), ex.toString());
			return;
		}			
		if (useEDT) {
			try {
				SwingUtilities.invokeAndWait(responseCall);
			} catch (InterruptedException ex) {
				conn.error(message, ErrorType.ERROR_INTERRUPTED.ordinal(), ex.toString());
			} catch (InvocationTargetException ex) {
				conn.error(message, ErrorType.ERROR_SERVER_BUG.ordinal(), ex.toString());
			}
		} else {
			responseCall.run();
		}
			
		responseCall.done();
			
		if (responseCall.hasError()) {
			if (responseCall.error() instanceof ErrorResponse) {
				ErrorResponse er = (ErrorResponse)responseCall.error();
				conn.error(message, er.code.ordinal(), er.toString());
			} else {
				conn.error(message, ErrorType.ERROR_SERVER_IO.ordinal(), responseCall.error().toString());
			}
		} else {
			Object value = responseCall.value();
			if (value instanceof CharSequence) {
				conn.send(message, (CharSequence)value);
			} else
			if (value instanceof MessageSerializable) {
				conn.send(message, (MessageSerializable)value);
			} else {
				conn.error(message, ErrorType.ERROR_SERVER_BUG.ordinal(), value != null ? value.getClass().toString() : "null");
			}
		}
	}
	/**
	 * Composes a list of deferred calls which need to be executed
	 * in order on the EDT. The returned object is then used
	 * as the main result.
	 * @param message the original message
	 * @return the main deferred call
	 * @throws IOException on error
	 */
	protected DeferredCall processMessageDeferred(Object message) throws IOException {
		if (message instanceof MessageArray) {
			MessageArray ma = (MessageArray)message;
			if ("BATCH".equals(ma.name)) {
				final List<DeferredCall> calls = new ArrayList<DeferredCall>();
				for (Object o : ma) {
					if (o instanceof MessageArray) {
						calls.add(processMessageArrayDeferred((MessageArray)o));
					} else
					if (o instanceof MessageObject) {
						calls.add(processMessageObjectDeferred((MessageObject)o));
					} else {
						throw new ErrorResponse(ErrorType.ERROR_UNKNOWN_MESSAGE, message != null ? message.getClass().toString() : "null");
					}
				}
				return new DeferredCall() {
					@Override
					protected Object invoke() throws IOException {
						for (DeferredCall dc : calls) {
							dc.run();
							if (dc.hasError()) {
								break;
							}
						}
						return null;
					}
					@Override
					public void done() {
						MessageArray result = new MessageArray("BATCH_RESPONSE");
						for (DeferredCall dc : calls) {
							if (dc.hasError()) {
								break;
							}
							dc.done();
							result.add(dc.value());
						}
						this.value = result;
					}
				};
			} else {
				return processMessageArrayDeferred(ma);
			}
		}
		if (message instanceof MessageObject) {
			return processMessageObjectDeferred((MessageObject)message);
		}
		throw new ErrorResponse(ErrorType.ERROR_UNKNOWN_MESSAGE, message != null ? message.getClass().toString() : "null");
	}
	/**
	 * Process requests with message array as their outer elements.
	 * @param ma the message array
	 * @return the deferred call object
	 * @throws IOException on error
	 */
	protected DeferredCall processMessageArrayDeferred(MessageArray ma) throws IOException {
		throw new ErrorResponse(ErrorType.ERROR_UNKNOWN_MESSAGE, ma != null ? ma.name : "null");
		
	}
	/**
	 * Process request with message object as their outer elements.
	 * @param mo the message object
	 * @return the deferred call object
	 * @throws IOException on error
	 */
	protected DeferredCall processMessageObjectDeferred(final MessageObject mo) throws IOException {
		if ("PING".equals(mo.name)) {
			return new DeferredCall() {
				@Override
				protected Object invoke() throws IOException {
					return api.ping();
				}
			};
		} else
		if ("LOGIN".equals(mo.name)) {
			return new DeferredCall() {
				@Override
				protected Object invoke() throws IOException {
					return api.login(mo.getString("user"), mo.getString("passphrase"), mo.getString("version"));
				}
			};
			// FIXME
		}
		throw new ErrorResponse(ErrorType.ERROR_UNKNOWN_MESSAGE, mo != null ? mo.name : "null");
	}
}
