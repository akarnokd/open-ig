/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.net.MessageObject;

/**
 * The inventory slot status.
 * @author akarnokd, 2013.04.27.
 */
public class InventorySlotStatus implements MessageObjectIO {
	/** The inventory slot id. */
	public String id;
	/** The assigned technology, if not null. */
	public String type;
	/** Number of items. */
	public int count;
	/** Slot hitpoints. */
	public double hp;
	@Override
	public void fromMessage(MessageObject mo) {
		id = mo.getString("id");
		type = mo.getString("type");
		count = mo.getInt("count");
		hp = mo.getDouble("hp");
	}
	@Override
	public MessageObject toMessage() {
		MessageObject mo = new MessageObject(objectName());
		
		mo.set("id", id);
		mo.set("type", type);
		mo.set("count", count);
		mo.set("hp", hp);
		
		return mo;
	}
	@Override
	public String objectName() {
		return "SLOT";
	}
}
