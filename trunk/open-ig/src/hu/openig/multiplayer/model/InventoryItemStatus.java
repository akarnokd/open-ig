/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The inventory item status record.
 * @author akarnokd, 2013.04.27.
 */
public class InventoryItemStatus {
	/** The inventory id. */
	public int id;
	/** The research type. */
	public String type;
	/** The number of items. */
	public int count;
	/** The item owner. */
	public String owner;
	/** Time to live. */
	public int ttl;
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
	public final List<InventorySlotStatus> slots = new ArrayList<InventorySlotStatus>();
}
