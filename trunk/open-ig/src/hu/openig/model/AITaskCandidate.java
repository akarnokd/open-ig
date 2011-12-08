/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * Represents a task-action candidate.
 * @author akarnokd, 2011.12.08.
 */
public class AITaskCandidate {
	/** The task to perform. */
	public final AITask task;
	/** The task score. */
	public final double score;
	/** The task doer who would perform the task. */
	public final AIObject taskDoer;
	/**
	 * Creates a new task candidate with the triplet of the task, task doer and score.
	 * @param task the task to perform
	 * @param taskDoer who will perform the task
	 * @param score the score
	 */
	public AITaskCandidate(AITask task, AIObject taskDoer, double score) {
		this.task = task;
		this.taskDoer = taskDoer;
		this.score = score;
	}
}
