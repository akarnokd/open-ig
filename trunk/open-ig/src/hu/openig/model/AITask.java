/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * Represents a task the AI player should perform.
 * @author akarnokd, 2011.12.08.
 */
public class AITask {
	/** The task type. */
	public AITaskType type;
	/** The base priority value. */
	public double basePriority;
	/** The dynamic priority value. */
	public double dynamicPriority;
	/** The object representing the objective of this task. */
	public Object objective;
	/** The assigned task doer. */
	public AIObject taskDoer;
	/**
	 * Execute the specific action.
	 */
	public void execute() {
		
	}
}
