package hu.openig.utils;

import java.util.LinkedHashMap;

/**
 * Least recently used map.
 * @author karnokd
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
		return new LRUHashMap<K, V>(size);
	}
}
