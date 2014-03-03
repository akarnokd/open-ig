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
 * Base interface for serializing message object hierarchy.
 * @author akarnokd, 2013.04.21.
 */
public interface MessageSerializable {
	/**
	 * Store the contents into the given appendable object.
	 * @param append the output appendable
	 * @throws IOException on error
	 */
	void save(Appendable append) throws IOException;
}
