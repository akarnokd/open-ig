/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.net.MessageArray;
import hu.openig.net.MessageObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The inventory item status record.
 * @author akarnokd, 2013.04.27.
 */
public class InventoryItemStatus implements MessageObjectIO, MessageArrayItemFactory<InventoryItemStatus> {
	/** The array name. */
	public static final String ARRAY_NAME = "INVENTORIES";
	/** The object name. */
	public static final String OBJECT_NAME = "INVENTORY";
	/** The inventory id. */
	public int id;
	/** The research type. */
	public String type;
	/** The number of items. */
	public int count;
	/** The item owner. */
	public String owner;
	/** The current HP. */
	public double hp;
	/** The current shield. */
	public double shield;
	/** The item's tag. */
	public String tag;
	/** Optional nickname of this ship. */
	public String nickname;
	/** The nickname index of this ship in case of redundancy. */
	public int nicknameIndex;
	/** Number of kills by this unit. */
	public int kills;
	/** The value of destroyed enemies. */
	public long killsCost;
	/** The inventory slot status. */
	public final List<InventorySlotStatus> slots = new ArrayList<>();
	@Override
	public void fromMessage(MessageObject mo) {
		id = mo.getInt("id");
		type = mo.getString("type");
		count = mo.getInt("count");
		owner = mo.getString("owner");
		hp = mo.getDouble("hp");
		shield = mo.getDouble("shield");
		tag = mo.getString("tag");
		nickname = mo.getString("nickname");
		nicknameIndex = mo.getInt("nicknameIndex");
		kills = mo.getInt("kills");
		killsCost = mo.getInt("killsCost");
		for (MessageObject ms : mo.getArray("slots").objects()) {
			InventorySlotStatus iss = new InventorySlotStatus();
			iss.fromMessage(ms);
			slots.add(iss);
		}
	}
	@Override
	public MessageObject toMessage() {
		MessageObject mo = new MessageObject(objectName());
		
		mo.set("id", id)
		.set("type", type)
		.set("count", count)
		.set("owner", owner)
		.set("hp", hp)
		.set("shield", shield)
		.set("tag", tag)
		.set("nickname", nickname)
		.set("nicknameIndex", nicknameIndex)
		.set("kills", kills)
		.set("killsCost", killsCost);
		MessageArray ma = new MessageArray(null);
		mo.set("slots", ma);
		for (InventorySlotStatus iss : slots) {
			ma.add(iss.toMessage());
		}
		
		return mo;
	}
	@Override
	public InventoryItemStatus invoke() {
		return new InventoryItemStatus();
	}
	@Override
	public String arrayName() {
		return ARRAY_NAME;
	}
	@Override
	public String objectName() {
		return OBJECT_NAME;
	}
	/**
	 * Clear the enemy info.
	 */
	public void clearEnemyInfo() {
		hp = 1;
		shield = 0;
		tag = null;
		kills = 0;
		killsCost = 0;
		nickname = null;
		nicknameIndex = 0;
		slots.clear();
	}
	/**
	 * Convert a list of inventory item statuses into a message array.
	 * @param list the inventory item status sequence
	 * @return the message array representation
	 */
	public static MessageArray toArray(Iterable<InventoryItemStatus> list) {
		MessageArray ma = new MessageArray(ARRAY_NAME);
		for (InventoryItemStatus iis : list) {
			ma.add(iis.toMessage());
		}
		return ma;
	}
	/**
	 * Parses a list of inventory item status from the message array.
	 * @param ma the message array
	 * @return the list of inventory item status records
	 */
	public static List<InventoryItemStatus> fromArray(MessageArray ma) {
		List<InventoryItemStatus> result = new ArrayList<>();
		for (MessageObject mo : ma.objects()) {
			InventoryItemStatus e = new InventoryItemStatus();
			e.fromMessage(mo);
			result.add(e);
		}
		return result;
	}
}
