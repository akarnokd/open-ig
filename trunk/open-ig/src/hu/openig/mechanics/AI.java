/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.model.AIManager;
import hu.openig.model.AIObject;
import hu.openig.model.AITask;
import hu.openig.model.AITaskCandidate;
import hu.openig.model.AIWorld;
import hu.openig.model.BattleInfo;
import hu.openig.model.DiplomaticInteraction;
import hu.openig.model.Player;
import hu.openig.model.ResponseMode;
import hu.openig.model.World;
import hu.openig.utils.XElement;

/**
 * The general artificial intelligence to run generic starmap-planet-production-research operations.
 * @author akarnokd, 2011.12.08.
 */
public class AI implements AIManager {
	/**
	 * Create a task candidate object.
	 * @param task the task
	 * @param taskDoer the task doer
	 * @param taskTime the task time
	 * @return the candidate
	 */
	AITaskCandidate createCandidate(AITask task, AIObject taskDoer, double taskTime) {
		return new AITaskCandidate(task, taskDoer, score(task, taskDoer, taskTime));
	}
	/**
	 * Calculate the score for a task and task doer.
	 * @param task the task
	 * @param taskDoer the task doer
	 * @param taskTime the cached task time from AIObject.taskTime()
	 * @return the score
	 */
	double score(AITask task, AIObject taskDoer, double taskTime) {
		return (task.basePriority + task.dynamicPriority) / taskTime;
	}
	@Override
	public void manage(AIWorld world) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public ResponseMode diplomacy(World world, Player we, Player other,
			DiplomaticInteraction offer) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void spaceBattle(World world, Player we, BattleInfo battle) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void groundBattle(World world, Player we, BattleInfo battle) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void load(XElement in) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void save(XElement out) {
		// TODO Auto-generated method stub
		
	}
}
