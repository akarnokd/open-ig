/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.net.MessageSerializable;

import java.io.IOException;

/**
 * The base interface for deferred execution.
 * @author akarnokd, 2013.05.08.
 */
public interface DeferredRunnable extends Runnable {
	/**
	 * Perform a post-processing step.
	 */
	void done();
	/**
	 * Returns the value or the IO exception.
	 * @return the value
	 * @throws IOException the exception
	 */
	MessageSerializable get() throws IOException;
	/**
	 * Returns true if this deferred runnable contains an error.
	 * @return true if the deferred runnable contains an error
	 */
	boolean isError();
}
