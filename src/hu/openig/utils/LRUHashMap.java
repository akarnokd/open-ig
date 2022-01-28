/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.utils;

import java.util.LinkedHashMap;

/**
 * Least recently used map.
 * @author akarnokd
 * @param <K> the key type
 * @param <V> the value type
 */
public class LRUHashMap<K, V> extends LinkedHashMap<K, V> {
    /**  */
    private static final long serialVersionUID = -8032627958631838087L;
    /** The capacity. */
    private int capacity;
    @Override
    protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
        return size() == capacity;
    }
    /**
     * Constructor. Sets the capacity.
     * @param size the capacity
     */
    public LRUHashMap(int size) {
        super(size);
        this.capacity = size;
    }
    /**
     * Creates a new LRUHashMap instance.
     * @param <K> the key type
     * @param <V> the value type
     * @param size the capacity
     * @return the new LRUHashMap
     */
    public static <K, V> LRUHashMap<K, V> create(int size) {
        return new LRUHashMap<>(size);
    }
}
