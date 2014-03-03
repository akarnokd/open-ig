/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.net.ErrorResponse;
import hu.openig.net.ErrorType;
import hu.openig.net.MessageObject;
import hu.openig.net.MissingAttributeException;

/**
 * The answer of a successful login request.
 * @author akarnokd, 2013.04.22.
 */
public class WelcomeResponse implements MessageObjectIO {
	/** The assigned session id. */
	public String sessionId;
	@Override
	public void fromMessage(MessageObject mo) {
		sessionId = mo.getString("session");
	}
	@Override
	public MessageObject toMessage() {
		MessageObject mo = new MessageObject("WELCOME");
		
		mo.set("session", sessionId);
		
		return mo;
	}
	/**
	 * Creates a new instance from the message object.
	 * @param mo the message object
	 * @return the welcome message
	 * @throws ErrorResponse on message format error
	 */
	public static WelcomeResponse from(MessageObject mo) throws ErrorResponse {
		try {
			WelcomeResponse result = new WelcomeResponse();
			result.fromMessage(mo);
			return result;
		} catch (MissingAttributeException ex) {
			throw new ErrorResponse(ErrorType.FORMAT, ex.toString());
		}
	}
	@Override
	public String objectName() {
		return "WELCOME";
	}
}
