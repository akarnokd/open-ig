/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * Represents an object which may be capable of performing some task.
 * @author akarnokd, 2011.12.08.
 */
public abstract class AIObject {
	/**
	 * Test if the given task can be performed by this object
	 * and return the task time required for it.
	 * @param task the task to perform
	 * @return the task time for this task or Double.INFINITE if never.
	 */
	public abstract double taskTime(AITask task);
	/**
	 * Assign the task candidate to this AI object.
	 * @param tc the task candidate
	 */
	public abstract void assign(AITaskCandidate tc);
}
