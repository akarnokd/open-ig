/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A building collection which supports iteration
 * and keeps track of buildings based on id, type,
 * kind etc.
 * @author akarnokd, 2013.05.06.
 */
public class Buildings {
    /** Map by unique id. */
    protected Map<Integer, Building> byId = new LinkedHashMap<>();
    /** Map by building type. */
    protected Map<String, Set<Building>> byType = new HashMap<>();
    /** Map by kind. */
    protected Map<String, Set<Building>> byKind = new HashMap<>();
    /**
     * Remove all buildings.
     */
    public void clear() {
        byId.clear();
        byType.clear();
        byKind.clear();
    }
    /**
     * Adds a building to the collection.
     * if not already in the collection
     * @param b the building
     */
    public void add(Building b) {
        if (!byId.containsKey(b.id)) {
            byId.put(b.id, b);

            Set<Building> bs = byType.get(b.type.id);
            if (bs == null) {
                bs = new HashSet<>();
                byType.put(b.type.id, bs);
            }
            bs.add(b);

            bs = byKind.get(b.type.kind);
            if (bs == null) {
                bs = new HashSet<>();
                byKind.put(b.type.kind, bs);
            }
            bs.add(b);
        }
    }
    /**
     * Removes a building from the collection.
     * @param b the building to remove
     * @return true if the building was removed from the collection
     */
    public boolean remove(Building b) {
        if (byId.remove(b.id) != null) {
            byType.get(b.type.id).remove(b);
            byKind.get(b.type.kind).remove(b);
            return true;
        }
        return false;
    }
    /**
     * Returns the given building by the identifier
     * if in this collection.
     * @param id the building id
     * @return the building or null if not found
     */
    public Building findById(int id) {
        return byId.get(id);
    }
    /**
     * Returns the collection of buildings with the

     * given type.
     * @param type the building type
     * @return the collection of buildings, unmodifiable
     */
    public Collection<Building> findByType(String type) {
        Set<Building> bs = byType.get(type);
        if (bs != null) {
            return Collections.unmodifiableSet(bs);
        }
        return Collections.emptySet();
    }
    /**
     * Returns the collection of buildings with the

     * given kind.
     * @param kind the building kind
     * @return the collection of buildings, unmodifiable
     */
    public Collection<Building> findByKind(String kind) {
        Set<Building> bs = byKind.get(kind);
        if (bs != null) {
            return Collections.unmodifiableSet(bs);
        }
        return Collections.emptySet();
    }
    /**
     * @return the number of buildings in this collection
     */
    public int size() {
        return byId.size();
    }
    /**
     * @return true if the collection is empty
     */
    public boolean isEmpty() {
        return byId.isEmpty();
    }
    /**
     * Returns a copy of the buildings contained in this collection.
     * @return the building list
     */
    public List<Building> list() {
        List<Building> result = new ArrayList<>(1 + size());
        result.addAll(byId.values());
        return result;
    }
    /**
     * Remove an inventory item from this collection.
     * @param id the inventory item id
     * @return the inventory item removed or null if it was not
     * in this collection
     */
    public Building remove(int id) {
        Building b = byId.get(id);
        remove(b);
        return b;
    }
    /**
     * Remove all inventory items which are in the given
     * sequence.
     * @param bs the inventory item sequence
     * @return true if the collection was modified
     */
    public boolean removeAll(Iterable<Building> bs) {
        boolean removed = false;
        for (Building ii : bs) {
            removed |= remove(ii);
        }
        return removed;
    }
    /**
     * Remove buildings by their IDs.
     * @param iis the building id sequence
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
     * Returns an iterable and read-only view of this collection.
     * @return the iterable object
     */
    public Iterable<Building> iterable() {
        return Collections.unmodifiableCollection(byId.values());
    }
    /**
     * Remove inventory items by type.
     * @param type the type
     * @return true if the collection was modified
     */
    public boolean removeByType(String type) {
        Set<Building> set = byType.remove(type);
        if (set != null) {
            for (Building ii : set) {
                byId.remove(ii.id);
                byKind.get(ii.type.kind).remove(ii);
            }
            return !set.isEmpty();
        }

        return false;
    }
    /**
     * Returns the set of building ids in this collection.
     * @return the unmodifiable set of building ids
     */
    public Set<Integer> inventoryIds() {
        return Collections.unmodifiableSet(byId.keySet());
    }
    /**
     * Adds buildings from the source sequence.
     * @param bs the building sequence
     */
    public void addAll(Iterable<? extends Building> bs) {
        for (Building b : bs) {
            add(b);
        }
    }
    /**
     * Checks if the given building is in this collection.
     * @param b the building to test
     * @return true if contained within
     */
    public boolean contains(Building b) {
        return contains(b.id);
    }
    /**
     * Checks if the given building id is in this collection.
     * @param id the building id to test
     * @return true if contained within
     */
    public boolean contains(int id) {
        return byId.containsKey(id);
    }
}
