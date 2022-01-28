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
    /** The list of the group items. */
    public final List<InventoryItem> items = new ArrayList<>();
    /**

     * Add an inventory item.

     * @param pii the inventory item
     */
    public void add(InventoryItem pii) {
        items.add(pii);
    }
}
