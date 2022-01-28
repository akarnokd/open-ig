/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Func1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Collection of inventory items with lookup
 * support by id, type, owner, etc.
 * @author akarnokd, 2013.05.06.
 */
public class InventoryItems {
    /** The map by id. */
    private final Map<Integer, InventoryItem> byId = new LinkedHashMap<>();
    /** The map by type. */
    private final Map<String, Set<InventoryItem>> byType = new HashMap<>();
    /** The map by owner. */
    private final Map<String, Set<InventoryItem>> byOwner = new HashMap<>();
    /**
     * Returns a list copy of the inventory items in this collection.
     * @return the inventory item list
     */
    public List<InventoryItem> list() {
        List<InventoryItem> result = new ArrayList<>(1 + size());
        result.addAll(byId.values());
        return result;
    }
    /**
     * Returns an iterable and read-only view of the inventory items.
     * @return the iterable view
     */
    public Iterable<InventoryItem> iterable() {
        return Collections.unmodifiableCollection(byId.values());

    }
    /**
     * Returns the number of entries in this collection.
     * @return the number of inventory items
     */
    public int size() {
        return byId.size();
    }
    /**
     * Is this collection empty?
     * @return true if empty
     */
    public boolean isEmpty() {
        return byId.isEmpty();
    }
    /**
     * Remove all items from this collection.
     */
    public void clear() {
        byId.clear();
        byType.clear();
        byOwner.clear();
    }
    /**
     * Add an inventory item to this collection
     * if not already present.
     * @param ii the inventory item
     */
    public void add(InventoryItem ii) {
        if (!byId.containsKey(ii.id)) {
            byId.put(ii.id, ii);
            Set<InventoryItem> is = byType.get(ii.type.id);
            if (is == null) {
                is = new LinkedHashSet<>();
                byType.put(ii.type.id, is);
            }
            is.add(ii);

            is = byOwner.get(ii.owner.id);
            if (is == null) {
                is = new LinkedHashSet<>();
                byOwner.put(ii.owner.id, is);
            }
            is.add(ii);
        } else {
            System.err.println("Duplicate id: " + ii);
        }
    }
    /**
     * Remove an inventory item from this collection.
     * @param ii the inventory item to remove
     * @return true if the item was removed
     */
    public boolean remove(InventoryItem ii) {
        if (byId.remove(ii.id) != null) {
            if (!byType.get(ii.type.id).remove(ii)) {
                System.err.println(ii);
            }
            if (!byOwner.get(ii.owner.id).remove(ii)) {
                System.err.println(ii);
            }
            return true;
        }
        return false;
    }
    /**
     * Remove an inventory item from this collection.
     * @param id the inventory item id
     * @return the inventory item removed or null if it was not
     * in this collection
     */
    public InventoryItem remove(int id) {
        InventoryItem ii = byId.get(id);
        remove(ii);
        return ii;
    }
    /**
     * Remove all inventory items which are in the given
     * sequence.
     * @param iis the inventory item sequence
     * @return true if the collection was modified
     */
    public boolean removeAll(Iterable<InventoryItem> iis) {
        boolean removed = false;
        for (InventoryItem ii : iis) {
            removed |= remove(ii);
        }
        return removed;
    }
    /**
     * Remove inventory items by their IDs.
     * @param iis the inventory id sequence
     * @return true if the collection was modified
     */
    public boolean removeById(Iterable<Integer> iis) {
        boolean removed = false;
        for (Integer ii : iis) {
            removed |= remove(ii) != null;
        }
        return removed;
    }
    /**
     * Returns a non-null collection view of the
     * inventory items having the given type.
     * @param type the type
     * @return the unmodifiable collection
     */
    public Collection<InventoryItem> findByType(String type) {
        Set<InventoryItem> set = byType.get(type);
        if (set != null) {
            return Collections.unmodifiableCollection(set);
        }
        return Collections.emptySet();
    }
    /**
     * Returns a non-null collection view of the
     * inventory items having the given type.
     * @param owner the owner
     * @return the unmodifiable collection
     */
    public Collection<InventoryItem> findByOwner(String owner) {
        Set<InventoryItem> set = byOwner.get(owner);
        if (set != null) {
            return Collections.unmodifiableCollection(set);
        }
        return Collections.emptySet();
    }
    /**
     * Find an inventory item by its id.
     * @param id the id
     * @return the inventory item or null if not in this collection
     */
    public InventoryItem findById(int id) {
        return byId.get(id);
    }
    /**
     * Remove inventory items by owner.
     * @param owner the owner
     * @return true if the collection was modified
     */
    public boolean removeByOwner(String owner) {
        Set<InventoryItem> set = byOwner.remove(owner);
        if (set != null) {
            for (InventoryItem ii : set) {
                byId.remove(ii.id);
                byType.get(ii.type.id).remove(ii);
            }
            return !set.isEmpty();
        }

        return false;
    }
    /**
     * Remove inventory items by type.
     * @param type the type
     * @return true if the collection was modified
     */
    public boolean removeByType(String type) {
        Set<InventoryItem> set = byType.remove(type);
        if (set != null) {
            for (InventoryItem ii : set) {
                byId.remove(ii.id);
                byOwner.get(ii.owner.id).remove(ii);
            }
            return !set.isEmpty();
        }

        return false;
    }
    /**
     * Retain all elements which appear in the given id sequence.
     * @param ids the id sequence
     * @return true if the collection was modified
     */
    public boolean retainAllById(Iterable<Integer> ids) {
        Set<Integer> idSet;
        if (ids instanceof Set<?>) {
            idSet = (Set<Integer>)ids;
        } else {
            idSet = new HashSet<>();
            for (Integer i : ids) {
                idSet.add(i);
            }
        }
        boolean modified = false;
        Iterator<InventoryItem> e = byId.values().iterator();
        while (e.hasNext()) {
            InventoryItem ii = e.next();
            if (!idSet.contains(ii.id)) {
                e.remove();
                byOwner.get(ii.owner.id).remove(ii);
                byType.get(ii.type.id).remove(ii);

                modified = true;
            }
        }
        return modified;
    }
    /**
     * Returns the set of item ids in this collection.
     * @return the unmodifiable set of item ids
     */
    public Set<Integer> inventoryIds() {
        return Collections.unmodifiableSet(byId.keySet());
    }
    /**
     * Remove items where the predicate returns true.
     * @param predicate the predicate to test against each item
     * @return true if the collection was modified
     */
    public boolean removeIf(Func1<? super InventoryItem, Boolean> predicate) {
        boolean modified = false;
        Iterator<InventoryItem> it = byId.values().iterator();
        while (it.hasNext()) {
            InventoryItem ii = it.next();
            if (predicate.invoke(ii)) {
                it.remove();
                byOwner.get(ii.owner.id).remove(ii);
                byType.get(ii.type.id).remove(ii);

                modified = true;
            }
        }
        return modified;
    }
    /**
     * Add all inventory items from the sequence.
     * @param items the inventory
     */
    public void addAll(Iterable<InventoryItem> items) {
        for (InventoryItem ii : items) {
            add(ii);
        }
    }
    /**
     * Checks if this collection contains the given inventory item.
     * @param ii the inventory item
     * @return true if contained
     */
    public boolean contains(InventoryItem ii) {
        return contains(ii.id);
    }
    /**
     * Checks if this collection contains the given inventory item id.
     * @param id the inventory item id
     * @return true if contained
     */
    public boolean contains(int id) {
        return byId.containsKey(id);
    }
    /**
     * Computes the number of items to produce to fully upgrade the ships in the given
     * inventory. It considers the available owner inventory and any tech stripped
     * from the ships that can be moved to other ships.
     * @param owner the owner
     * @return the map of each technology and counts to build (positive only).
     */
    public Map<ResearchType, Integer> buildDemand(Player owner) {
        // the total number of new equipment
        Map<ResearchType, Integer> counts = new HashMap<>();
        // the equipment becoming available due replacement
        Map<ResearchType, Integer> released = new HashMap<>();
        for (InventoryItem ii : iterable()) {
            if (ii.owner != owner) {
                continue;
            }
            for (InventorySlot is : ii.slots.values()) {
                if (!is.slot.fixed) {
                    ResearchType best = null;
                    for (ResearchType rt0 : is.slot.items) {
                        if (owner.isAvailable(rt0)) {
                            best = rt0;
                        }
                    }
                    if (best != null) {
                        Integer c = counts.get(best);
                        if (best == is.type) {
                            counts.put(best, c != null ? c + (is.slot.max - is.count) : (is.slot.max - is.count));
                        } else {
                            counts.put(best, c != null ? c + is.slot.max : is.slot.max);
                            if (is.type != null && is.count > 0) {
                                Integer c1 = released.get(is.type);
                                released.put(is.type, c1 != null ? c1 + is.count : is.count);
                            }
                        }
                    }
                }
            }
        }
        for (Map.Entry<ResearchType, Integer> e : new ArrayList<>(counts.entrySet())) {
            ResearchType key = e.getKey();
            int invc = owner.inventoryCount(key);
            if (released.containsKey(key)) {
                invc += released.get(key);
            }
            int d = e.getValue();
            if (d <= invc) {
                counts.remove(key);
            } else {
                counts.put(key, d - invc);
            }
        }
        return counts;
    }
}
