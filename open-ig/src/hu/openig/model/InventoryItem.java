/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a planet's inventory. A planet may 'own' multiple things from multiple players, e.g.,
 * spy satellites. In planet listings, only the current player's items are considered.
 * @author akarnokd, 2011.04.05.
 */
public class InventoryItem {
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
	/** The fleet's inventory slots. */
	public final List<InventorySlot> slots = new ArrayList<InventorySlot>();
	/**
	 * @return the maximum shield amount or -1 for no shielding
	 */
	public int shieldMax() {
		int result = -1;
		for (InventorySlot sl : slots) {
			if (sl.type != null && sl.type.properties.containsKey("shield")) {
				result = Math.max(result, Integer.parseInt(sl.type.get("shield")));
			}
		}
		return result * type.productionCost / 100;
	}
}
