/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * Represents the state of the GOAP action.
 * @author akarnokd, 2011.12.13.
 */
public enum AIActionState {
	/** The action can be invoked for planning. */
	READY,
	/** An evaluation is running to calculate the cost of the action. */
	EVALUATING,
	/** The action is executing. */
	RUNNING,
	/** The action failed to execute. */
	FAILED,
	/** The action completed successfully. */
	SUCCESSFUL
}
