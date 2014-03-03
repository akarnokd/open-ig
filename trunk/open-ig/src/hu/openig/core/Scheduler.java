/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

import java.util.concurrent.Future;

/**
 * General interface for scheduling tasks.
 * <p>Less verbose than an ExecutorService.</p>
 * @author akarnokd, 2013.04.30.
 */
public interface Scheduler {
	/**
	 * Schedule a simple runnable action.
	 * @param run the runnable to schedule
	 * @return the future object
	 */
	Future<?> schedule(Runnable run);
}
