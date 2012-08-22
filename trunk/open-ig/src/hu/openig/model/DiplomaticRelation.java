/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.utils.U;

import java.util.Date;
import java.util.Set;

/**
 * Contains the diplomatic relation status between two players. 
 * The status is defined pairwise between players (unordered).
 * @author akarnokd, 2012.03.19.
 */
public class DiplomaticRelation {
	/** The first player who made the contact. */
	public Player first;
	/** The second player who made the contact. */
	public Player second;
	/** Indicate that the secondary side has also discovered the first. */
	public boolean full;
	/** The relation value, 0..100. */
	public double value;
	/** When was the last contact. */
	public Date lastContact;
	/** Indicator that the parties won't talk for longer periods of time. */
	private boolean wontTalk;
	/** The parties have trade agreement. */
	public boolean tradeAgreement;
	/** The alliance can't be broken and parties can't attack each other. */
	public boolean strongAlliance;
	/**
	 * The set of common enemies.
	 */
	public final Set<Player> alliancesAgainst = U.newHashSet();
	/** @return a copy of this record. */
	public DiplomaticRelation copy() {
		DiplomaticRelation dr = new DiplomaticRelation();
		dr.first = first;
		dr.second = second;
		dr.full = full;
		dr.value = value;
		dr.lastContact = lastContact;
		dr.wontTalk = wontTalk;
		dr.alliancesAgainst.addAll(alliancesAgainst);
		return dr;
	}
	/**
	 * Set the new won't talk value.
	 * @param value the value
	 */
	public void wontTalk(boolean value) {
		this.wontTalk = value;
	}
	/**
	 * @return the current won't talk status
	 */
	public boolean wontTalk() {
		return wontTalk;
	}
}
