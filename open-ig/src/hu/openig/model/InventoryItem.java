/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a planet's or fleet's inventory. A planet may 'own' multiple things from multiple players, e.g.,
 * spy satellites. In planet listings, only the current player's items are considered.
 * @author akarnokd, 2011.04.05.
 */
public class InventoryItem {
	/** The parent fleet or planet. */
	public final HasInventory parent;
	/** The owner. */
	public Player owner;
	/** The item's type. */
	public ResearchType type;
	/** The item's count. */
	public int count;
	/** The current hit points. */
	public int hp;
	/** The current shield points. */
	public int shield;
	/** The optional tag used by the AI or scripting to remember a concrete inventory item. */
	public String tag;
	/** The fleet's inventory slots. */
	public final List<InventorySlot> slots = new ArrayList<InventorySlot>();
	/**
	 * Constructor. Initializes the parent.
	 * @param parent the parent
	 */
	public InventoryItem(HasInventory parent) {
		this.parent = parent;
	}
	/**
	 * @return the maximum shield amount or -1 for no shielding
	 */
	public int shieldMax() {
		int result = -1;
		if (type.has("shield")) {
			result = type.getInt("shield");
		}
		for (InventorySlot sl : slots) {
			if (sl.type != null && sl.type.properties.containsKey("shield")) {
				result = Math.max(result, sl.type.getInt("shield"));
			}
		}
		return result * owner.world.getHitpoints(type) / 100;
	}
	/**
	 * Return the inventory slot with the given identifier.
	 * @param id the slot id
	 * @return the the slot or null if no such slot
	 */
	public InventorySlot getSlot(String id) {
		for (InventorySlot is : slots) {
			if (is.slot.id.equals(id)) {
				return is;
			}
		}
		return null;
	}
	/**
	 * Create slots from the base definition.
	 */
	public void createSlots() {
		for (EquipmentSlot es : type.slots.values()) {
			InventorySlot is = new InventorySlot();
			is.slot = es;
			if (es.fixed) {
				is.type = es.items.get(0);
				is.count = es.max;
				is.hp = owner.world.getHitpoints(is.type);
			}
			slots.add(is);
		}
	}
	/**
	 * Returns the sell value of this inventory item.
	 * @return the sell value
	 */
	public long sellValue() {
		long result = 1L * count * type.productionCost / 2;
		for (InventorySlot is : slots) {
			if (is.type != null && !is.slot.fixed) {
				result += is.count * is.type.productionCost / 2;
			}
		}
		return result;
	}
	/**
	 * Sell the inventory item.
	 */
	public void sell() {
		long money = sellValue();
		owner.money += money;
		owner.statistics.moneySellIncome += money;
		owner.statistics.moneyIncome += money;
		
		owner.world.statistics.moneyIncome += money;
		owner.world.statistics.moneySellIncome += money;

		count = 0;
		slots.clear();
	}
	/**
	 * Strip the assigned equipment and put it back into the owner's inventory.
	 */
	public void strip() {
		for (InventorySlot is : slots) {
			if (is.type != null && !is.slot.fixed) {
				owner.changeInventoryCount(is.type, is.count);
				is.type = null;
				is.count = 0;
				is.hp = 0;
			}
		}
	}
	@Override
	public String toString() {
		return String.format("Type = %s, Owner = %s, Count = %s, HP = %s, Shield = %s, Tag = %s", type.id, owner.id, count, hp, shield, tag);
	}
}
