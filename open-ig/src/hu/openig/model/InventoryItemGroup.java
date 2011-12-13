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

/** An item group. */
public class InventoryItemGroup {
	/** The group type. */
	public final ResearchType type;
	/** The selected index, -1 if none. */
	public int index = 0;
	/**
	 * Constructor.
	 * @param type the item type
	 */
	public InventoryItemGroup(ResearchType type) {
		this.type = type;
	}
//	/** @return The total hit points. */
//	public long hp() {
//		long result = 0;
//		for (InventoryItem pii : items) {
//			result += pii.hp;
//		}
//		return result;
//	}
//	/** @return The total shield points. */
//	public long shield() {
//		long result = 0;
//		int shielded = 0;
//		for (InventoryItem pii : items) {
//			if (pii.shieldMax() >= 0) {
//				result += pii.shield;
//				shielded++;
//			}
//		}
//		return shielded > 0 ? result : -1;
//	}
//	/** @return the total theoretical shield level. */
//	public long shieldMax() {
//		long result = 0;
//		int shielded = 0;
//		int count = 0;
//		for (InventoryItem pii : items) {
//			long sm = pii.shieldMax();
//			if (sm >= 0) {
//				result += pii.shieldMax();
//				shielded++;
//			}
//			count += pii.count;
//		}
//		return shielded > 0 ? result : -1;
//	}
	/** The list of the group items. */
	public final List<InventoryItem> items = new ArrayList<InventoryItem>();
	/** 
	 * Add an inventory item. 
	 * @param pii the inventory item
	 */
	public void add(InventoryItem pii) {
		items.add(pii);
	}
}
