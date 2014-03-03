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
 * Exception to report message syntax errors.
 * @author akarnokd, 2013.04.21.
 */
public class MessageSyntaxError extends IOException {
	/** */
	private static final long serialVersionUID = 1874268741478375500L;

	/**
	 * Default empty constructor.
	 */
	public MessageSyntaxError() {
		super();
	}

	/**
	 * Constructor setting the error text and a cause.
	 * @param message the message
	 * @param cause the cause
	 */
	public MessageSyntaxError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor. Sets the error text.
	 * @param message the message
	 */
	public MessageSyntaxError(String message) {
		super(message);
	}

	/**
	 * Constructor. Sets the cause exception.
	 * @param cause the cause
	 */
	public MessageSyntaxError(Throwable cause) {
		super(cause);
	}
}
