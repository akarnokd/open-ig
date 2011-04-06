/*
 * Copyright 2008-2011, David Karnok 
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
	/** @return the research progress as 100s percent. */
	public float getPercent() {
		return (type.researchCost - remainingMoney) * 100f / type.researchCost;
	}
	/** @return Get the research time amount. */
	public int getTime() {
		if (remainingMoney == 0) {
			return 0;
		}
		int normalFullTime = (type.researchCost / 20);
		float remainingPercent = (100 - getPercent()) / 100;
		float fundingPercent = 0.5f * remainingMoney / assignedMoney;
		return (int)(normalFullTime * fundingPercent * remainingPercent);
	}
	/**
	 * The maximum percent of the research with the current active lab capacity.
	 * @param ps the planet statistics
	 * @return the max percent in 100s
	 */
	public int getResearchMaxPercent(PlanetStatistics ps) {
		return 100 * (
				Math.min(ps.civilLabActive, type.civilLab)
				+ Math.min(ps.mechLabActive, type.mechLab)
				+ Math.min(ps.compLabActive, type.compLab)
				+ Math.min(ps.aiLab, type.aiLab)
				+ Math.min(ps.milLab, type.milLab)
		) 
		/ (type.civilLab + type.mechLab + type.compLab + type.aiLab + type.milLab);
	}
}
