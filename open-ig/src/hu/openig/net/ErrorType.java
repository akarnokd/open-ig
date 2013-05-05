/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.net;


/**
 * The error type enumerations.
 * @author akarnokd, 2013.04.27.
 */
public enum ErrorType {
	/** There was no error. */
	SUCCESS,
	/** Message syntax error. */
	ERROR_SYNTAX,
	/** Unknown message. */
	ERROR_UNKNOWN_MESSAGE,
	/** Unknown error. */
	ERROR_UNKNOWN,
	/** Server not ready. */
	ERROR_NOT_READY,
	/** The user login failed due credential errors. */
	ERROR_USER,
	/** The server version differs from the client version. */
	ERROR_VERSION,
	/** The message can't be used in the current state. */
	ERROR_INVALID_MESSAGE,
	/** Unable to relogin. */
	ERROR_RELOGIN,
	/** Unexpected response. */
	ERROR_RESPONSE,
	/** Message format error, i.e., missing or invalid attributes. */
	ERROR_FORMAT,
	/** Server run into an assertion bug. */
	ERROR_SERVER_BUG,
	/** Server run into an assertion bug. */
	ERROR_SERVER_IO,
	/** The session has been changed by a new login. */
	ERROR_SESSION_INVALID,
	/** The user is not logged in. */
	ERROR_NOT_LOGGED_IN,
	/** Server activity interrupted/cancelled. */
	ERROR_INTERRUPTED,
	/** Fleet missing or not visible to the player. */
	ERROR_UNKNOWN_FLEET,
	/** Planet missing or not visible to the player. */
	ERROR_UNKNOWN_PLANET,
	/** Research missing. */
	ERROR_UNKNOWN_RESEARCH,
	/** Player missing or not known to the player. */
	ERROR_UNKNOWN_PLAYER
	;
	/**
	 * Interprets the message attribute as error code or error name string.
	 * @param o the object
	 * @param defaultValue the default value
	 * @return the error type enum
	 */
	public static ErrorType from(Object o, ErrorType defaultValue) {
		if (o instanceof Number) {
			int idx = ((Number)o).intValue();
			if (idx >= 0 && idx < values().length) {
				return values()[idx];
			}
		} else
		if (o instanceof CharSequence) {
			for (ErrorType e : values()) {
				if (e.name().equals(o)) {
					return e;
				}
			}
		}
		return defaultValue;
	}
}
