/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.net;

/**
 * Indicate a missing message attribute.
 * @author akarnokd, 2013.04.22.
 */
public class MissingAttributeException extends RuntimeException {
	/** */
	private static final long serialVersionUID = -5881303110144464248L;

	/**
	 * Constructor with message.
	 * @param message the message
	 */
	public MissingAttributeException(String message) {
		super(message);
	}
}
