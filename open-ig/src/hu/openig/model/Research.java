/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * A concrete in-progress research.
 * @author akarnokd, 2010.01.07.
 */
public class Research {
	/** The research should progress. */
	public ResearchState state;
	/** The thing to research. */
	public ResearchType type;
	/** The assigned money amount. */
	public int assignedMoney;
	/** The remaining money amount. */
	public int remainingMoney;
	/**
	 * @param traits the traits to consider 
	 * @return the research progress as 100s percent. 
	 */
	public float getPercent(Traits traits) {
		int c = type.researchCost(traits);
		float result = (c - remainingMoney) * 100f / c; 
		return result;
	}
	/** 
	 * @param traits the traits to consider 
	 * @return Get the research time amount. 
	 */
	public int getTime(Traits traits) {
		if (remainingMoney == 0) {
			return 0;
		}
		int normalFullTime = type.researchTime(traits);
		float remainingPercent = (100 - getPercent(traits)) / 100;
		float fundingPercent = 0.5f * remainingMoney / assignedMoney;
		return (int)(normalFullTime * fundingPercent * remainingPercent);
	}
	/**
	 * The maximum percent of the research with the current active lab capacity.
	 * @param ps the planet statistics
	 * @return the max percent in 100s
	 */
	public int getResearchMaxPercent(PlanetStatistics ps) {
		int totalLabs = type.civilLab + type.mechLab + type.compLab + type.aiLab + type.milLab;
		if (totalLabs > 0) {
			return 100 * (
					Math.min(ps.activeLabs.civil, type.civilLab)
					+ Math.min(ps.activeLabs.mech, type.mechLab)
					+ Math.min(ps.activeLabs.comp, type.compLab)
					+ Math.min(ps.activeLabs.ai, type.aiLab)
					+ Math.min(ps.activeLabs.mil, type.milLab)
			) 
			/ (totalLabs);
		}
		return 0;
	}
	/**
	 * Create a copy of this research.
	 * @return the copy
	 */
	public Research copy() {
		Research result = new Research();
		result.assignedMoney = assignedMoney;
		result.remainingMoney = remainingMoney;
		result.state = state;
		result.type = type;
		
		return result;
	}
	/**
	 * Set the assigned money by the given factor of the remaining money.
	 * @param moneyFactor the money factor
	 */
	public void setMoneyFactor(double moneyFactor) {
		assignedMoney = (int)(remainingMoney * moneyFactor / 2);
	}
	/**
	 * Sets the assigned money.
	 * @param money the new assigned money
	 */
	public void setAssignedMoney(int money) {
		assignedMoney = Math.max(0, Math.min(money, remainingMoney));
	}
}
