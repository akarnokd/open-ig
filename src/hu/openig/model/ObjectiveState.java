/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The objective's state.
 * @author akarnokd, Jan 12, 2012
 */
public enum ObjectiveState {
	/** Objective is active (e.g., simply visible). */
	ACTIVE,
	/** Objective completed successfully. */
	SUCCESS,
	/** Objective failed. */
	FAILURE
}
