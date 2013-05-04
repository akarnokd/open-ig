/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.net.ErrorResponse;
import hu.openig.net.ErrorType;
import hu.openig.net.MessageArray;
import hu.openig.net.MessageObject;
import hu.openig.utils.U;

import java.io.IOException;

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
				if (U.equal(n, mo.name)) {
					return mo;
				}
			}
			if (name.length == 0) {
				return mo;
			}
		}
		throw new ErrorResponse(ErrorType.ERROR_RESPONSE, response != null ? response.toString() : "null");
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
				if (U.equal(n, ma)) {
					return ma;
				}
			}
			if (name.length == 0) {
				return ma;
			}
		}
		throw new ErrorResponse(ErrorType.ERROR_RESPONSE, response != null ? response.toString() : "null");
	}

}
