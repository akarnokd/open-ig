/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the inventory item for the AI computation.
 * @author akarnokd, 2011.12.14.
 */
public class AIInventoryItem {
    /** The original inventory item. */
    public InventoryItem parent;
    /** The type. */
    public ResearchType type;
    /** The owner. */
    public Player owner;
    /** The count. */
    public int count;
    /** The copy of the inventory slots. */
    public final List<InventorySlot> slots = new ArrayList<>();
    /**
     * Constructs an empty object.
     */
    public AIInventoryItem() {
    }
    /**
     * Constructs the object from the inventory item.
     * @param ii the source
     */
    public AIInventoryItem(InventoryItem ii) {
        this.parent = ii;
        this.type = ii.type;
        this.count = ii.count;
        this.owner = ii.owner;
        for (InventorySlot is : ii.slots.values()) {
            slots.add(is.copy());
        }
    }
}
