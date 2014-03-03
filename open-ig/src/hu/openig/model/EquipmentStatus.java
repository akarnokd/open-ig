/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * A space battle equipment status record.
 * @author akarnokd, 2013.05.02.
 */
public class EquipmentStatus {
	/** The equipment slot id. */
	public String id;
	/** The equipment item type. */
	public String type;
	/** Number of items. */
	public int count;
	/** The slots hitpoints. */
	public double hp;
	/** The slots maximum hitpoints. */
	public double hpMax;
	/** The slot's cooldown. */
	public int cooldown;
}
