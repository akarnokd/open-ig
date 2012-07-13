/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a planet's or fleet's inventory. A planet may 'own' multiple things from multiple players, e.g.,
 * spy satellites. In planet listings, only the current player's items are considered.
 * @author akarnokd, 2011.04.05.
 */
public class InventoryItem {
	/** The parent fleet or planet. */
	public HasInventory parent;
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
		if (result >= 0) {
			return result * owner.world.getHitpoints(type) / 100;
		}
		return -1;
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
			} else {
				List<ResearchType> availList = owner.availableLevel(type);
				
				for (ResearchType rt1 : es.items) {
					if (availList.contains(rt1)) {
						is.type = rt1;
						// always assign a hyperdrive
						if (rt1.category == ResearchSubCategory.EQUIPMENT_HYPERDRIVES) {
							is.count = 1;
						} else {
							is.count = es.max / 2;
						}
					}
				}
				if (is.count == 0) {
					is.type = null;
				}
			}
			is.hp = is.hpMax(owner);
			
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
			}
		}
	}
	@Override
	public String toString() {
		return String.format("InventoryItem { Type = %s, Owner = %s, Count = %s, HP = %s, Shield = %s, Tag = %s }", type.id, owner.id, count, hp, shield, tag);
	}
	/**
	 * Upgrade the slots of this inventory item.
	 */
	public void upgradeSlots() {
		for (InventorySlot is : slots) {
			if (!is.slot.fixed) {
				for (int i = is.slot.items.size() - 1; i >= 0; i--) {
					ResearchType rt = is.slot.items.get(i);
					int cnt = owner.inventoryCount(rt);
					if (cnt > 0) {
						int toAdd = Math.min(cnt, is.slot.max);
						is.type = rt;
						is.count = toAdd;
						owner.changeInventoryCount(rt, -toAdd);
						break;
					}
				}
			}
		}
	}
	/**
	 * Check if the equipment of the given inventory item can be upgraded.
	 * @return true if equipment upgrade can be performed
	 */
	public boolean checkSlots() {
		for (InventorySlot is : slots) {
			if (!is.slot.fixed) {
				// check if next better type is available
				int index = is.slot.items.indexOf(is.type) + 1;
				for (int i = index; i < is.slot.items.size(); i++) {
					if (owner.inventoryCount(is.slot.items.get(i)) > 0) {
						return true;
					}
				}
				// check if current type can be more filled in
				index = Math.max(0, index - 1);
				ResearchType t0 = is.slot.items.get(index);
				int diff = is.slot.max - is.count;
				if (diff > 0 && owner.inventoryCount(t0) > 0) {
					return true;
				}
			}
		}
		return false;
	}
	/** @return the Max hitpoits. */
	public int hpMax() {
		return owner.world.getHitpoints(type);
	}
	/**
	 * Compute the damage/dps of regular weapons.
	 * @return the pair of damage and dps
	 */
	public Pair<Integer, Double> maxDamageDPS() {
		int damage = 0;
		double dps = 0;
		for (InventorySlot is : slots) {
			if (is.type != null && (is.type.category == ResearchSubCategory.WEAPONS_CANNONS
					|| is.type.category == ResearchSubCategory.WEAPONS_LASERS)) {
				BattleProjectile bp = owner.world.battle.projectiles.get(is.type.get("projectile"));
				damage += bp.damage * is.count * count;
				dps += bp.damage * is.count * 1000d / bp.delay * count;
			}
		}
		if (type.category == ResearchSubCategory.WEAPONS_TANKS 
				|| type.category == ResearchSubCategory.WEAPONS_VEHICLES) {
			BattleGroundVehicle bgw = owner.world.battle.groundEntities.get(type.id);
			damage += bgw.damage * count;
			dps += bgw.damage * 1000d * count / bgw.delay;
		}
		return Pair.of(damage, dps);
	}
}
