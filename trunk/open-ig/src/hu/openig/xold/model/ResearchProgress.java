/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.xold.model;

/**
 * Contains a research progress information.
 * @author karnokd
 */
public class ResearchProgress {
	/** The technology to research. */
	public ResearchTech research;
	/** The money remaining from this research. */
	public int moneyRemaining;
	/** The allocated remaining money. */
	public int allocatedRemainingMoney;
}
