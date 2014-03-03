/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/** The attack/defense record. */
public class AttackDefense {
	/** Attack value. */
	public double attack;
	/** Defense value. */
	public double defense;
	/**
	 * Add another record.
	 * @param d the other defense
	 */
	public void add(AttackDefense d) {
		this.attack += d.attack;
		this.defense += d.defense;
	}
}
