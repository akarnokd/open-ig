/*
 * Copyright 2008-2014, David Karnok 
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
	SYNTAX,
	/** Unknown message. */
	UNKNOWN_MESSAGE,
	/** Unknown error. */
	UNKNOWN,
	/** Server not ready. */
	NOT_READY,
	/** The user login failed due credential errors. */
	USER,
	/** The server version differs from the client version. */
	VERSION,
	/** The message can't be used in the current state. */
	INVALID_MESSAGE,
	/** Unable to relogin. */
	RELOGIN,
	/** Unexpected response. */
	RESPONSE,
	/** Message format error, i.e., missing or invalid attributes. */
	FORMAT,
	/** Server run into an assertion bug. */
	SERVER_BUG,
	/** Server run into an internal or reflected IO error. */
	SERVER_IO,
	/** The session has been changed by a new login. */
	SESSION_INVALID,
	/** The user is not logged in. */
	NOT_LOGGED_IN,
	/** Server activity interrupted/cancelled. */
	INTERRUPTED,
	/** Fleet missing or not visible to the player. */
	UNKNOWN_FLEET,
	/** Planet missing or not visible to the player. */
	UNKNOWN_PLANET,
	/** Research missing. */
	UNKNOWN_RESEARCH,
	/** Player missing or not known to the player. */
	UNKNOWN_PLAYER,
	/** Is not your fleet. */
	NOT_YOUR_FLEET,
	/** Trying to attack a friendly fleet. */
	FRIENDLY_FLEET,
	/** Trying to attack a friendly planet. */
	FRIENDLY_PLANET,
	/** Planet occupied. */
	PLANET_OCCUPIED,
	/** No colonizable planet near by. */
	NO_PLANET_NEARBY,
	/** Not your planet. */
	NOT_YOUR_PLANET,
	/** No military spaceport. */
	NO_SPACEPORT,
	/** Can't create fleet. */
	CANT_CREATE_FLEET,
	/** The fleet isn't empty. */
	FLEET_ISNT_EMPTY,
	/** Unknown fleet/planet item. */
	UNKNOWN_INVENTORY_ITEM,
	/** Unknown fleet equipment. */
	UNKNOWN_EQUIPMENT,
	/** Unable to deploy inventory. */
	CANT_DEPLOY_INVENTORY,
	/** Unable to undeploy inventory. */
	CANT_UNDEPLOY_INVENTORY,
	/** Unknown building type. */
	UNKNOWN_BUILDING,
	/** Unknown building race. */
	UNKNOWN_BUILDING_RACE,
	/** The building is not available yet or here. */
	CANT_BUILD,
	/** Can't place building at the specified coordinates. */
	CANT_PLACE_BUILDING,
	/** Not enough money. */
	NOT_ENOUGH_MONEY,
	/** Not enough room for building. */
	NOT_ENOUGH_ROOM,
	/** Can't upgrade the building to the target level. */
	CANT_UPGRADE_BUILDING,
	/** Production lines are full. */
	PRODUCTION_LINES_FULL,
	/** Technology not under production. */
	NOT_PRODUCING,
	/** Technology not under research. */
	NOT_RESEARCHING,
	/** Research prerequisites not met. */
	PREREQUISITES_NOT_MET,
	/** No game running. */
	NO_GAME_RUNNING,
	/** Cheats are not allowed. */
	CHEATS_DISABLED,
	/** No game is available to join. */
	NO_GAME_AVAILABLE,
	/** The required user settings were not accepted. */
	JOIN_REJECTED,
	/** The toMessage of a MessageObjectIO returned null. */
	TO_MESSAGE_NOT_IMPLEMENTED
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
	/**
	 * Raise an exception with this error code.
	 * @throws ErrorResponse the error response exception
	 */
	public void raise() throws ErrorResponse {
		throw new ErrorResponse(this);
	}
	/**
	 * Raise an exception with this error code and the message.
	 * @param message the error message
	 * @throws ErrorResponse the error response exception
	 */
	public void raise(String message) throws ErrorResponse {
		throw new ErrorResponse(this, message);
	}
	/**
	 * Raise an exception with this error code.
	 * @param ex the exception to wrap
	 * @throws ErrorResponse the error response exception
	 */
	public void raise(Throwable ex) throws ErrorResponse {
		throw new ErrorResponse(this, ex);
	}
	/**
	 * Raise an exception with this error code.
	 * @param message the message
	 * @param ex the exception to wrap
	 * @throws ErrorResponse the error response exception
	 */
	public void raise(String message, Throwable ex) throws ErrorResponse {
		throw new ErrorResponse(this, message, ex);
	}
}
