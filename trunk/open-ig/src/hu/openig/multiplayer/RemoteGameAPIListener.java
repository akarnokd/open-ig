/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer;

import hu.openig.core.Action2E;
import hu.openig.multiplayer.model.DeferredCall;
import hu.openig.multiplayer.model.ErrorResponse;
import hu.openig.multiplayer.model.LoginRequest;
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
public class RemoteGameAPIListener implements Action2E<MessageConnection, Object, IOException> {
	/** The game API. */
	protected final RemoteGameAPI api;
	/** Use the EDT to execute the API methods? */
	protected final boolean useEDT;
	/**
	 * Constructor, takes a remote API entry point and uses the EDT to call its methods.
	 * @param api the API entry point
	 */
	public RemoteGameAPIListener(RemoteGameAPI api) {
		this.api = api;
		this.useEDT = true;
	}
	/**
	 * Constructor, takes a remote game API and might use the EDT to call its methods.
	 * @param api the API entry point
	 * @param useEDT use EDT for the methods?
	 */
	public RemoteGameAPIListener(RemoteGameAPI api, boolean useEDT) {
		this.api = api;
		this.useEDT = useEDT;
	}
	@Override
	public void invoke(MessageConnection conn, Object message)
			throws IOException {
		try {
			final DeferredCall responseCall = processMessageDeferred(message);
			
			if (useEDT) {
				SwingUtilities.invokeAndWait(responseCall);
			} else {
				responseCall.run();
			}
			
			responseCall.done();
			
			Object response = responseCall.result();
			
			if (response instanceof CharSequence) {
				conn.send(message, (CharSequence)response);
			} else
			if (response instanceof MessageSerializable) {
				conn.send(message, (MessageSerializable)response);
			} else {
				conn.error(message, ErrorResponse.ERROR_SERVER_BUG, response != null ? response.getClass().toString() : "null");
			}
		} catch (MissingAttributeException ex) {
			conn.error(message, ErrorResponse.ERROR_FORMAT, ex.toString());
		} catch (ErrorResponse ex) {
			conn.error(message, ex.code, ex.toString());
		} catch (IOException ex) {
			conn.error(message, ErrorResponse.ERROR_SERVER_IO, ex.toString());
		} catch (InterruptedException ex) {
			conn.error(message, ErrorResponse.ERROR_INTERRUPTED, ex.toString());
		} catch (InvocationTargetException ex) {
			conn.error(message, ErrorResponse.ERROR_SERVER_BUG, ex.toString());
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
						throw new ErrorResponse(ErrorResponse.ERROR_UNKNOWN_MESSAGE, message != null ? message.getClass().toString() : "null");
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
					public void done() throws IOException {
						MessageArray result = new MessageArray("BATCH_RESPONSE");
						for (DeferredCall dc : calls) {
							dc.done();
							result.add(dc.result());
						}
						this.result = result;
					}
				};
			} else {
				return processMessageArrayDeferred(ma);
			}
		}
		if (message instanceof MessageObject) {
			return processMessageObjectDeferred((MessageObject)message);
		}
		throw new ErrorResponse(ErrorResponse.ERROR_UNKNOWN_MESSAGE, message != null ? message.getClass().toString() : "null");
	}
	/**
	 * Process requests with message array as their outer elements.
	 * @param ma the message array
	 * @return the deferred call object
	 * @throws IOException on error
	 */
	protected DeferredCall processMessageArrayDeferred(MessageArray ma) throws IOException {
		throw new ErrorResponse(ErrorResponse.ERROR_UNKNOWN_MESSAGE, ma != null ? ma.name : "null");
		
	}
	/**
	 * Process request with message object as their outer elements.
	 * @param mo the message object
	 * @return the deferred call object
	 * @throws IOException on error
	 */
	protected DeferredCall processMessageObjectDeferred(MessageObject mo) throws IOException {
		if ("PING".equals(mo.name)) {
			return new DeferredCall() {
				@Override
				protected Object invoke() throws IOException {
					api.ping();
					return "PONG { }";
				}
			};
		} else
		if ("LOGIN".equals(mo.name)) {
			final LoginRequest req = new LoginRequest();
			req.fromMessage(mo);
			
			return new DeferredCall() {
				@Override
				protected Object invoke() throws IOException {
					return api.login(req);
				}
			};
		}
		throw new ErrorResponse(ErrorResponse.ERROR_UNKNOWN_MESSAGE, mo != null ? mo.name : "null");
	}
}
