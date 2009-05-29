/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

/**
 * Record to store lab information of the player.
 * @author karnokd
 *
 */
public class LabInfo {
	/** The number of operational civil engineering labs. */
	public int currentCivil;
	/** The total number of civil engineering labs. */
	public int totalCivil;
	/** The number of operational mechanical engineering labs. */
	public int currentMechanic;
	/** The total number of mechanical engineering labs. */
	public int totalMechanic;
	/** The number of operational computer engineering labs. */
	public int currentComputer;
	/** The total number of computer engineering labs. */
	public int totalComputer;
	/** The number of operational AI engineering labs. */
	public int currentAi;
	/** The total number of AI engineering labs. */
	public int totalAi;
	/** The number of operational military engineering labs. */
	public int currentMilitary;
	/** The total number of military engineering labs. */
	public int totalMilitary;
}
