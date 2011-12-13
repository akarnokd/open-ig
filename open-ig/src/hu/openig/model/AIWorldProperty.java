/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * Represents a world property value.
 * @author akarnokd, 2011.12.13.
 */
public class AIWorldProperty {
	/** The property key. */
	public AIWorldPropertyKey key;
	/** The subject. */
	public Object subject;
	/** The value. */
	public Object value;
}
