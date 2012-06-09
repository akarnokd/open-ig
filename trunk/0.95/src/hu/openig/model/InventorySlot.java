/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * A concrete slot settings for an inventory item, e.g., cruisers and battleships.
 * @author akarnokd, 2011.04.05.
 */
public class InventorySlot {
	/**  The slots definition. */
	public EquipmentSlot slot;
	/** The current entity in the slot, may be null! */
	public ResearchType type;
	/** The item count. */
	public int count;
	/** The item's hitpoints. */
	public double hp;
	/** @return is the slot filled to max? */
	public boolean isFilled() {
		return count >= slot.max;
	}
	/**
	 * Create a copy of this slot.
	 * @return the new inventory slot object
	 */
	public InventorySlot copy() {
		InventorySlot is = new InventorySlot();
		is.slot = slot;
		is.type = type;
		is.count = count;
		is.hp = hp;
		return is;
	}
	/**
	 * @return returns the equipment category of this inventory slot
	 */
	public ResearchSubCategory getCategory() {
		if (type != null) {
			return type.category;
		}
		for (ResearchType es : slot.items) {
			return es.category;
		}
		return null;
	}
	/**
	 * Compute the max hitpoints of the slot.
	 * @param owner the owner
	 * @return the max hp
	 */
	public int hpMax(Player owner) {
		int result = 0;
		int cnt = 0;
		for (ResearchType rt : slot.items) {
			result += owner.world.getHitpoints(rt);
			cnt++;
		}
		return result / cnt;
	}
}
