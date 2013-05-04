/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The inventory slot status.
 * @author akarnokd, 2013.04.27.
 */
public class InventorySlotStatus {
	/** The inventory slot id. */
	public String id;
	/** The assigned technology, if not null. */
	public String type;
	/** Number of items. */
	public int count;
	/** Slot hitpoints. */
	public double hp;
}
