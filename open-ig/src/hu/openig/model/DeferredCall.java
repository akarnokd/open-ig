/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.net.ErrorType;
import hu.openig.net.MessageObject;
import hu.openig.net.MessageSerializable;

import java.io.IOException;


/**
 * An abstract class which collects the result or exception from the execution
 * of its <code>invoke()</code> method executed on a different thread.
 * @author akarnokd, 2013.04.26.
 */
public abstract class DeferredCall extends DeferredTransform<MessageObjectIO> {
	@Override
	protected final MessageSerializable transform(MessageObjectIO intermediate) throws IOException {
		MessageObject message = intermediate.toMessage();
		if (message == null) {
			ErrorType.TO_MESSAGE_NOT_IMPLEMENTED.raise();
		}
		return message;
	}
}