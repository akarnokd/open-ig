/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Action0;

import java.util.List;

/**
 * Base interface for the planners.
 * @author akarnokd, 2011.12.27.
 */
public interface AIPlanner {
	/**
	 * Execute the planner.
	 * @return the resulting actions
	 */
	List<Action0> run();
}
