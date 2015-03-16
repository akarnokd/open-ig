/*
 * Copyright 2008-2014, David Karnok 
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
			result += owner.world.getHitpoints(rt, owner);
			cnt++;
		}
		return result / cnt;
	}
	/** 
	 * Check if the current slot supports the given technology.
	 * @param rt the technology to check
	 * @return true if the technology is supported
	 */
	public boolean supports(ResearchType rt) {
		return slot.items.contains(rt);
	}
	/**
	 * Refills the slot based on available inventory of the given player.
	 * @param owner the owner of this slot
	 */
	public void refill(Player owner) {
		if (type != null) {
			int demand = slot.max - count;
			int inv = owner.inventoryCount(type);
			int add = Math.min(inv, demand);
			count += add;
			owner.changeInventoryCount(type, -add);
		} else {
			for (int i = slot.items.size() - 1; i >= 0; i--) {
				ResearchType rt = slot.items.get(i);
				if (owner.isAvailable(rt)) {
					int demand = slot.max;
					int inv = owner.inventoryCount(rt);
					int add = Math.min(inv, demand);
					if (add > 0) {
						type = rt;
						count = add;
						owner.changeInventoryCount(rt, -add);
						break;
					}
				}
			}
		}
	}
	@Override
	public String toString() {
	    if (type != null) {
	        return type.id + " (slot = " + slot.id + ", count = " + count + ", max = " + slot.max + ", hp = " + hp + ")";
	    }
	    return "Empty " + " (slot = " + slot.id + ", max = " + slot.max + ")";
	}
}
