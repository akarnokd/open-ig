/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The research state.
 * @author akarnokd, 2011.04.06.
 */
public enum ResearchState {
	/** The research is stopped. */
	STOPPED,
	/** The research is in progress. */
	RUNNING,
	/** We run out of money. */
	MONEY,
	/** We run out of labs. */
	LAB,
	/** The research has completed. */
	COMPLETE
}
