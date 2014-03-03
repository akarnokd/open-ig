/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.net.MessageObject;

/**
 * Interface representing the capability to save into
 * and load from a MessageObject.
 * @author akarnokd, 2013.04.23.
 */
public interface MessageObjectIO {
	/**
	 * Serialize the contents into a message object.
	 * @return the message object, not null
	 */
	MessageObject toMessage();
	/**
	 * Deserialize the contents from the message object.
	 * @param mo the message object
	 */
	void fromMessage(MessageObject mo);
	/**
	 * Returns the name of the message object.
	 * @return the name used to create and expect a message object
	 */
	String objectName();
}
