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
	public boolean wontTalk;
	/**
	 * The set of common enemies.
	 */
	public final Set<Player> alliancesAgainst = U.newHashSet();
}
