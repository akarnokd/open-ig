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
	/** Message syntax error. */
	public static final int ERROR_SYNTAX = 1;
	/** Unknown message. */
	public static final int ERROR_UNKNOWN_MESSAGE = 2;
	/** Unknown error. */
	public static final int ERROR_UNKNOWN = 3;
	/** Server not ready. */
	public static final int ERROR_NOT_READY = 4;
	/** The user login failed due credential errors. */
	public static final int ERROR_USER = 5;
	/** The server version differs from the client version. */
	public static final int ERROR_VERSION = 6;
	/** The message can't be used in the current state. */
	public static final int ERROR_INVALID_MESSAGE = 7;
	/** Unable to relogin. */
	public static final int ERROR_RELOGIN = 8;
	/** Unexpected response. */
	public static final int ERROR_RESPONSE = 10;
	/** Message format error, i.e., missing or invalid attributes. */
	public static final int ERROR_FORMAT = 11;
	/** The error code. */
	public final int code;
	/**
	 * Constructor with error code.
	 * @param code the error code
	 */
	public ErrorResponse(int code) {
		super();
		this.code = code;
	}
	/**
	 * Constructor with error code and message.
	 * @param code the error code
	 * @param message the error message
	 */
	public ErrorResponse(int code, String message) {
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
						mo.getInt("code", ERROR_UNKNOWN), 
						mo.getString("message", null));
			}
		}
	}
}
