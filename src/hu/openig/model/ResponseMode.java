/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * What is the general direction of the response.
 * @author akarnokd, Apr 22, 2011
 */
public enum ResponseMode {
	/** Accept proposal. */
	YES,
	/** Neutral answer. */
	MAYBE,
	/** Decline proposal. */
	NO
}
