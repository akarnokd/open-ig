/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.net;


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
	 * Constructor with error code and a cause.
	 * @param code the error code
	 * @param cause the original exception
	 */
	public ErrorResponse(ErrorType code, Throwable cause) {
		super(cause);
		this.code = code;
	}
	/**
	 * Constructor with error code and message.
	 * @param code the error code
	 * @param message the error message
	 * @param cause the original exception
	 */
	public ErrorResponse(ErrorType code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}
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
		ErrorResponse er = asError(response);
		if (er != null) {
			throw er;
		}
	}
	/**
	 * Returns an ErrorResponse exception if the given
	 * object is an ERROR message, null otherwise.
	 * @param response the response
	 * @return ErrorResponse if the response is an error
	 */
	public static ErrorResponse asError(Object response) {
		if (response instanceof MessageObject) {
			MessageObject mo = (MessageObject)response;
			if (mo.name != null && mo.name.startsWith("ERROR")) {
				return new ErrorResponse(
						ErrorType.from(mo.get("code", null), ErrorType.UNKNOWN), 
						mo.getString("message", null));
			}
		}
		return null;
	}
	/**
	 * Returns the error code enum and the message.
	 * @return the text representation of this exception
	 */
	public String getText() {
		return code + ": " + getMessage();
	}
}
