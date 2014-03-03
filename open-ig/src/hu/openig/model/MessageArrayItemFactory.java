/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Func0;

/**
 * Base interface to create message objects
 * from message array items, plus delivering
 * the expected outer array name.
 * @author akarnokd, 2013.05.04.
 * @param <T> the message object I/O type
 */
public interface MessageArrayItemFactory<T extends MessageObjectIO> extends Func0<T> {
	/** 
	 * Returns the expected name of the array.
	 * @return the expected name of the array
	 */
	String arrayName();
}
