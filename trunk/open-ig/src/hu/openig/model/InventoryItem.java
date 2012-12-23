/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Pair;
import hu.openig.utils.U;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
	public double hp;
	/** The current shield points. */
	public int shield;
	/** The optional tag used by the AI or scripting to remember a concrete inventory item. */
	public String tag;
	/** The fleet's inventory slots. */
	public final List<InventorySlot> slots = new ArrayList<InventorySlot>();
	/** Optional nickname of this ship. */
	public String nickname;
	/** The nickname index of this ship in case of redundancy. */
	public int nicknameIndex;
	/** Number of kills by this unit. */
	public int kills;
	/** The value of destroyed enemies. */
	public long killsCost;
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
		if (type.has(ResearchType.PARAMETER_SHIELD)) {
			result = type.getInt(ResearchType.PARAMETER_SHIELD);
		}
		for (InventorySlot sl : slots) {
			if (sl.type != null && sl.type.has(ResearchType.PARAMETER_SHIELD)) {
				result = Math.max(result, sl.type.getInt(ResearchType.PARAMETER_SHIELD));
			}
		}
		if (result >= 0) {
			return result * owner.world.getHitpoints(type, owner) / 100;
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
		return unitSellValue() * count;
	}
	/**
	 * @return the unit sell value of this inventory item.
	 */
	public long unitSellValue() {
		long result = 1L * type.productionCost / 2;
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
		owner.addMoney(money);
		
		owner.statistics.sellCount++;
		owner.statistics.moneySellIncome += money;
		owner.statistics.moneyIncome += money;
		
		owner.world.statistics.sellCount++;
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
		return owner.world.getHitpoints(type, owner);
	}
	/**
	 * Compute the damage/dps of regular weapons.
	 * @return the pair of damage and dps
	 */
	public Pair<Double, Double> maxDamageDPS() {
		double damage = 0;
		double dps = 0;
		for (InventorySlot is : slots) {
			if (is.type != null && (is.type.category == ResearchSubCategory.WEAPONS_CANNONS
					|| is.type.category == ResearchSubCategory.WEAPONS_LASERS)) {
				BattleProjectile bp = owner.world.battle.projectiles.get(is.type.get(ResearchType.PARAMETER_PROJECTILE));
				double dmg = bp.damage(owner);
				damage += dmg * is.count * count;
				dps += dmg * is.count * 1000d / bp.delay * count;
			}
		}
		if (type.category == ResearchSubCategory.WEAPONS_TANKS 
				|| type.category == ResearchSubCategory.WEAPONS_VEHICLES) {
			BattleGroundVehicle bgw = owner.world.battle.groundEntities.get(type.id);

			double dmg = bgw.damage(owner);
			damage += dmg * count;
			dps += dmg * 1000d * count / bgw.delay;
		}
		return Pair.of(damage, dps);
	}
	/**
	 * Remove excess fighters from the supplied inventory set.
	 * @param items the inventory items
	 */
	public static void removeExcessFighters(List<InventoryItem> items) {
		// remove above-limit fighters back into the inventory
		for (InventoryItem ii : items) {
			if (ii.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
				int diff = ii.count - ii.owner.world.params().fighterLimit();
				if (diff > 0) {
					ii.owner.changeInventoryCount(ii.type, diff);
					ii.count -= diff;
				}
			}
		}
	}
	/**
	 * Remove excess tanks from the inventory.
	 * @param items the inventory items
	 * @param owner the owner
	 * @param count the current count
	 * @param max the maximum allowed
	 */
	public static void removeExcessTanks(List<InventoryItem> items, Player owner, int count, int max) {
		// remove excess vehicles
		while (count > max) {
			List<InventoryItem> iis = new ArrayList<InventoryItem>(items);
			Collections.shuffle(iis);
			for (InventoryItem ii2 : iis) {
				if (ii2.owner == owner 
						&& (ii2.type.category == ResearchSubCategory.WEAPONS_TANKS
						|| ii2.type.category == ResearchSubCategory.WEAPONS_VEHICLES)) {
					owner.changeInventoryCount(ii2.type, 1);
					ii2.count--;
					if (ii2.count <= 0) {
						items.remove(ii2);
					}
					count--;
					break;
				}
			}
			
		}
	}
	/**
	 * Generate a nickname and index for this inventory item.
	 */
	public void generateNickname() {
		if (owner.nicknames.isEmpty() 
				|| (type.category != ResearchSubCategory.SPACESHIPS_CRUISERS 
				&& type.category != ResearchSubCategory.SPACESHIPS_BATTLESHIPS)) {
			return;
		}
		nickname = null;
		nicknameIndex = 0;
		
		Map<String, IntValue> counts = U.newHashMap();
		for (String nn : owner.nicknames) {
			counts.put(nn, new IntValue());
		}
		// find the largest counts for each nicknames.
		for (Fleet f : owner.ownFleets()) {
			for (InventoryItem ii : f.inventory) {
				if (ii.nickname != null) {
					IntValue v = counts.get(ii.nickname);
					if (v == null) {
						v = new IntValue();
						counts.put(ii.nickname, v);
					}
					v.value = Math.max(v.value, ii.nicknameIndex + 1);
				}
			}
		}
		// order usage counts
		List<Pair<String, IntValue>> countsOrdered = U.newArrayList();
		for (Map.Entry<String, IntValue> e : counts.entrySet()) {
			countsOrdered.add(Pair.of(e.getKey(), e.getValue()));
		}
		Collections.shuffle(countsOrdered, owner.world.random());
		Collections.sort(countsOrdered, new Comparator<Pair<String, IntValue>>() {
			@Override
			public int compare(Pair<String, IntValue> o1,
					Pair<String, IntValue> o2) {
				return U.compare(o1.second.value, o2.second.value);
			}
		});
		nickname = countsOrdered.get(0).first;
		nicknameIndex = countsOrdered.get(0).second.value;
	}
}
