/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.net;

import hu.openig.core.Action1E;
import hu.openig.core.AsyncResult;

import java.io.IOException;
import java.util.Objects;

/**
 * Message utilities class.
 * @author akarnokd, 2013.04.23.
 */
public final class MessageUtils {
	/** */
	private MessageUtils() { }
	/**
	 * Checks if the response object is a MessageObject and
	 * has the given name.
	 * @param response the response object
	 * @param name the array of expected names; if empty, any array name is accepted
	 * @return the MessageObject
	 * @throws IOException if the message is incorrect.
	 */
	public static MessageObject expectObject(Object response, String... name) throws IOException {
		ErrorResponse.throwIfError(response);
		if (response instanceof MessageObject) {
			MessageObject mo = (MessageObject)response;
			for (String n : name) {
				if (Objects.equals(n, mo.name)) {
					return mo;
				}
			}
			if (name.length == 0) {
				return mo;
			}
		}
		throw new ErrorResponse(ErrorType.RESPONSE, response != null ? response.toString() : "null");
	}
	/**
	 * Checks if the response object is a MessageArray and
	 * has the given name.
	 * @param response the response object
	 * @param name the array of expected names; if empty, any array name is accepted
	 * @return the MessageArray
	 * @throws IOException if the message is incorrect.
	 */
	public static MessageArray expectArray(Object response, String... name) throws IOException {
		ErrorResponse.throwIfError(response);
		if (response instanceof MessageArray) {
			MessageArray ma = (MessageArray)response;
			for (String n : name) {
				if (Objects.equals(n, ma)) {
					return ma;
				}
			}
			if (name.length == 0) {
				return ma;
			}
		}
		throw new ErrorResponse(ErrorType.RESPONSE, response != null ? response.toString() : "null");
	}
	/**
	 * Execute an Action1E if the given result object implements it.
	 * This can be used to execute an internal transform on the result
	 * which replaces the onSuccess value.
	 * @param result the async result object
	 * @param o the value to transform
	 * @throws IOException on error
	 */
	public static void applyTransform(AsyncResult<Object, ? super IOException> result, Object o) throws IOException {
		if (result instanceof Action1E<?, ?>) {
			@SuppressWarnings("unchecked")
			Action1E<Object, IOException> at = (Action1E<Object, IOException>)result;
			at.invoke(o);
		}
	}
}
