/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer.model;

import hu.openig.net.MessageObject;

import java.io.IOException;

/**
 * An error response.
 * @author akarnokd, 2013.04.22.
 */
public class ErrorResponse extends IOException {
	/** */
	private static final long serialVersionUID = 1135911041025210489L;
	/** The error code. */
	public final ErrorType code;
	/**
	 * Constructor with error code.
	 * @param code the error code
	 */
	public ErrorResponse(ErrorType code) {
		super();
		this.code = code;
	}
	/**
	 * Constructor with error code and message.
	 * @param code the error code
	 * @param message the error message
	 */
	public ErrorResponse(ErrorType code, String message) {
		super(message);
		this.code = code;
	}
	/**
	 * Throws an ErrorResponse exception if the given
	 * object is an ERROR message.
	 * @param response the response
	 * @throws ErrorResponse if the response is an error
	 */
	public static void throwIfError(Object response) throws ErrorResponse {
		if (response instanceof MessageObject) {
			MessageObject mo = (MessageObject)response;
			if (mo.name != null && mo.name.startsWith("ERROR")) {
				throw new ErrorResponse(
						ErrorType.from(mo.get("code", null), ErrorType.ERROR_UNKNOWN), 
						mo.getString("message", null));
			}
		}
	}
}
