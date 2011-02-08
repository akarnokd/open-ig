/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.xold.model;

/**
 * Slot type where the user cannot edit the content equipment.
 * Used, for example, by fighters and space stations which have fixed
 * weapons.
 * @author karnokd
 *
 */
public class FixedSlot {
	/** The type of the slot. */
	public String type;
	/** The name of the technology in this slot. */
	String eqId;
	/** The actual technology in this slot. */
	public ResearchTech id;
	/** The number for this slot. */
	public int value;
}
