/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * Task categories to determine the budget and priority for a task.
 * @author akarnokd, 2011.12.08.
 */
public enum AITaskCategory {
	/** Offensive. */
	OFFENSIVE,
	/** Defensive. */
	DEFENSIVE,
	/** Social. */
	SOCIAL,
	/** General, e.g., uses the same priority boosting as the largest ratio of the offensive/defensive/social settings. */
	GENERAL
}
