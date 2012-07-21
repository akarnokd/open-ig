/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.utils;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Utility class for missing java functionalities.
 * @author akarnokd
 */
public final class U {
	/** Constructor. */
	private U() {
		// utility class
	}
	/**
	 * Finds an array in another array of bytes.
	 * @param source the source array
	 * @param what the array of bytes to find in source
	 * @param start the start index of search
	 * @return the index of the what or -1 if not found
	 */
	public static int arrayIndexOf(byte[] source, byte[] what, int start) {
		loop:
		for (int i = start; i < source.length - what.length; i++) {
			for (int j = 0; j < what.length; j++) {
				if (what[j] != source[i + j]) {
					continue loop;
				}
			}
			return i;
		}
		return -1;
	}
	/**
	 * Compare two strings with natural ordering.
	 * Natural order means compare parts of numeric and non-numeric
	 * regions.
	 * @param s1 the first string
	 * @param s2 the second string
	 * @param cases be case sensitive?
	 * @return the comparison
	 */
	public static int naturalCompare(String s1, String s2, boolean cases) {
		String[] a1 = s1.split("\\s+");
		String[] a2 = s2.split("\\s+");
		int result = 0;
		for (int i = 0; i < Math.min(a1.length, a2.length); i++) {
			boolean num1 = isNumeric(a1[i]);
			boolean num2 = isNumeric(a2[i]);
			if (num1 && num2) {
				result = Integer.parseInt(a1[i]) - Integer.parseInt(a2[i]);
				if (result != 0) {
					return result;
				}
			} else 
			if (num1 || num2) {
				if (num1) {
					return -1;
				}
				return 1;
			} else {
				if (cases) {
					result = a1[i].compareTo(a2[i]);
				} else {
					result = a1[i].compareToIgnoreCase(a2[i]);
				}
				if (result != 0) {
					return result;
				}
			}
		}
		return a1.length - a2.length;
	}
	/**
	 * Returns true if the string only consists of digits.
	 * @param s the string to test, non null
	 * @return true if the string only contains digits
	 */
	private static boolean isNumeric(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (!Character.isDigit(s.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	/**
	 * A natural case sensitive comparator.
	 */
	public static final Comparator<String> NATURAL_COMPARATOR = new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
			return naturalCompare(o1, o2, true);
		}
	};
	/**
	 * A natural case insensitive comparator.
	 */
	public static final Comparator<String> NATURAL_COMPARATOR_NOCASE = new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
			return naturalCompare(o1, o2, false);
		}
	};
	/**
	 * Nullsafe equality test.
	 * @param o1 the first object
	 * @param o2 the second object
	 * @return true if they are the same or o1.equals(o2). This also includes the null == null case
	 */
	public static boolean equal(Object o1, Object o2) {
		return o1 == o2 || (o1 != null && o1.equals(o2));
	}
	/**
	 * Creates a new array list with default capacity.
	 * @param <T> the type of the elements in the list
	 * @return the new array list
	 */
	public static <T> ArrayList<T> newArrayList() {
		return new ArrayList<T>();
	}
	/**
	 * Creates a new linked list with default capacity.
	 * @param <T> the type of the elements in the list
	 * @return the new linked list
	 */
	public static <T> LinkedList<T> newLinkedList() {
		return new LinkedList<T>();
	}
	/**
	 * Creates a new hash map with default capacity.
	 * @param <K> the type of the key
	 * @param <V> the type of the value
	 * @return the new hash map
	 */
	public static <K, V> HashMap<K, V> newHashMap() {
		return new HashMap<K, V>();
	}
	/**
	 * Creates a new linked hash map with default capacity.
	 * @param <K> the type of the key
	 * @param <V> the type of the value
	 * @return the new linked hash map
	 */
	public static <K, V> LinkedHashMap<K, V> newLinkedHashMap() {
		return new LinkedHashMap<K, V>();
	}
	/**
	 * Creates a new hash set with default capacity.
	 * @param <T> the value type
	 * @return the new hash set
	 */
	public static <T> HashSet<T> newHashSet() {
		return new HashSet<T>();
	}
	/**
	 * Wraps the given runnable into another one which captures and prints
	 * the stack trace for any exception occurred in the original run() method.
	 * @param run the runnable to wrap
	 * @return the new runnable
	 */
	public static Runnable checked(final Runnable run) {
		return new Runnable() {
			@Override
			public void run() {
				try {
					run.run();
				} catch (RuntimeException t) {
					t.printStackTrace();
					throw t;
				}
			}
		};
	}
	/**
	 * Concatenate the sequence of two iterables.
	 * @param <T> the element type
	 * @param first the first iterable
	 * @param second the second iterable
	 * @return the combining iterable
	 */
	public static <T> Iterable<T> concat(final Iterable<? extends T> first, final Iterable<? extends T> second) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>() {
					/** The current iterable. */
					Iterable<? extends T> currentIt = first;
					/** The current iterable. */
					Iterator<? extends T> current = first.iterator();
					@Override
					public boolean hasNext() {
						if (current.hasNext()) {
							return true;
						}
						if (currentIt != second) {
							currentIt = second;
							current = currentIt.iterator();
						}
						return current.hasNext();
					}
					@Override
					public T next() {
						if (hasNext()) {
							return current.next();
						}
						throw new NoSuchElementException();
					}
					@Override
					public void remove() {
						current.remove();
					}
				};
			}
		};
	}
	/**
	 * Concatenate two lists by creating a new one.
	 * @param <T> the element type
	 * @param first the first list
	 * @param second the second list
	 * @return the concatenated list
	 */
	public static <T> List<T> concat(List<? extends T> first, List<? extends T> second) {
		List<T> result = newArrayList();
		result.addAll(first);
		result.addAll(second);
		return result;
	}
	/**
	 * Concatenate three lists by creating a new one.
	 * @param <T> the element type
	 * @param first the first list
	 * @param second the second list
	 * @param third the third list
	 * @return the concatenated list
	 */
	public static <T> List<T> concat(List<? extends T> first, List<? extends T> second, List<? extends T> third) {
		List<T> result = newArrayList();
		result.addAll(first);
		result.addAll(second);
		result.addAll(third);
		return result;
	}
	/**
	 * Concatenate two collections and place them into the given class of collection.
	 * @param <T> the element type of the collection
	 * @param <C> the output collection type
	 * @param <W> the first collection type
	 * @param <V> the second collection type
	 * @param first the first collection
	 * @param second the second collection
	 * @param clazz the container class
	 * @return the new collection
	 */
	public static <T,
	C extends Collection<T>, 
	W extends Collection<? extends T>,
	V extends Collection<? extends T>> C concat(W first, V second, Class<C> clazz) {
		try {
			C result = clazz.cast(clazz.newInstance());
			result.addAll(first);
			result.addAll(second);
			return result;
		} catch (IllegalAccessException ex) {
			throw new IllegalArgumentException("clazz", ex);
		} catch (InstantiationException ex) {
			throw new IllegalArgumentException("clazz", ex);
		}
	}
	/**
	 * Sort the given sequence into a new list according to its natural order.
	 * @param <T> the element type
	 * @param src the source sequence 
	 * @return the list sorted.
	 */
	public static <T extends Comparable<? super T>> List<T> sort(Iterable<? extends T> src) {
		List<T> result = new ArrayList<T>();
		for (T t : src) {
			result.add(t);
		}
		Collections.sort(result);
		return result;
	}
	/**
	 * Sort the given sequence into a new list according to the comparator.
	 * @param <T> the element type
	 * @param src the source sequence
	 * @param comp the element comparator 
	 * @return the list sorted.
	 */
	public static <T> List<T> sort(final Iterable<? extends T> src, final Comparator<? super T> comp) {
		List<T> result = new ArrayList<T>();
		for (T t : src) {
			result.add(t);
		}
		Collections.sort(result, comp);
		return result;
	}
	/**
	 * Sort the given list in place and return it.
	 * @param <T> the element type
	 * @param src the source list
	 * @param comp the comparator
	 * @return the source list
	 */
	public static <T> List<T> sort2(List<T> src, Comparator<? super T> comp) {
		Collections.sort(src, comp);
		return src;
	}
	/**
	 * Add the sequence of Ts to the destination collection and return this collection.
	 * @param <T> the element type
	 * @param <V> the collection type
	 * @param dest the destination collection
	 * @param src the source sequence
	 * @return the {@code dest} parameter
	 */
	public static <T, V extends Collection<T>> 
	V addAll(V dest, Iterable<? extends T> src) {
		for (T t : src) {
			dest.add(t);
		}
		return dest;
	}
	
	/**
	 * Create a new {@code ArrayList} from the supplied sequence.
	 * @param <T> the element type
	 * @param src the source sequence
	 * @return the created and filled-in ArrayList
	 */
	public static <T> ArrayList<T> newArrayList(Iterable<? extends T> src) {
		if (src instanceof Collection) {
			return new ArrayList<T>((Collection<? extends T>)src);
		}
		ArrayList<T> result = newArrayList();
		for (T t : src) {
			result.add(t);
		}
		return result;
	}
	/**
	 * Create a new {@code HashSet} from the supplied sequence.
	 * @param <T> the element type
	 * @param src the source sequence
	 * @return the created and filled-in HashSet
	 */
	public static <T> HashSet<T> newHashSet(Iterable<? extends T> src) {
		if (src instanceof Collection) {
			return new HashSet<T>((Collection<? extends T>)src);
		}
		HashSet<T> result = newHashSet();
		for (T t : src) {
			result.add(t);
		}
		return result;
	}
	/**
	 * Create a new {@code HashSet} from the supplied array.
	 * @param <T> the element type
	 * @param src the source array
	 * @return the created and filled-in HashSet
	 */
	public static <T> HashSet<T> newHashSet(T... src) {
		HashSet<T> result = newHashSet();
		for (T t : src) {
			result.add(t);
		}
		return result;
	}
	/**
	 * Create a new {@code HashMap} from the supplied other map.
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param src the source map
	 * @return the created and filled-in HashMap
	 */
	public static <K, V> HashMap<K, V> newHashMap(Map<? extends K, ? extends V> src) {
		return new HashMap<K, V>(src);
	}
	/**
	 * Create a new {@code HashMap} from the supplied sequence of map entries.
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param src the source map
	 * @return the created and filled-in HashMap
	 */
	public static <K, V> HashMap<K, V> newHashMap(Iterable<? extends Map.Entry<? extends K, ? extends V>> src) {
		HashMap<K, V> result = newHashMap();
		for (Map.Entry<? extends K, ? extends V> e : src) {
			result.put(e.getKey(), e.getValue());
		}
		return result;
	}
	/**
	 * Create a new {@code HashMap} from the supplied other map.
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param src the source map
	 * @return the created and filled-in HashMap
	 */
	public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(Map<? extends K, ? extends V> src) {
		return new LinkedHashMap<K, V>(src);
	}
	/**
	 * Create a new {@code HashMap} from the supplied sequence of map entries.
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param src the source map
	 * @return the created and filled-in HashMap
	 */
	public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(
			Iterable<? extends Map.Entry<? extends K, ? extends V>> src) {
		LinkedHashMap<K, V> result = newLinkedHashMap();
		for (Map.Entry<? extends K, ? extends V> e : src) {
			result.put(e.getKey(), e.getValue());
		}
		return result;
	}
	/**
	 * Wraps the iterator into an iterable to be used with for-each.
	 * @param <T> the element type
	 * @param it the iterator
	 * @return the iterable
	 */
	public static <T> Iterable<T> iterable(final Iterator<T> it) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return it;
			}
		};
	}
	/**
	 * Compare two doubles.
	 * @param v1 the first value
	 * @param v2 the second value
	 * @return -1 if v1 &lt; v2, 1 if v1 > v2, 0 otherwise
	 */
	public static int compare(double v1, double v2) {
		return v1 < v2 ? -1 : (v1 > v2 ? 1 : 0);
	}
	/**
	 * Compare two integers.
	 * @param v1 the first value
	 * @param v2 the second value
	 * @return -1 if v1 &lt; v2, 1 if v1 > v2, 0 otherwise
	 */
	public static int compare(int v1, int v2) {
		return v1 < v2 ? -1 : (v1 > v2 ? 1 : 0);
	}
	/**
	 * Compare two longs.
	 * @param v1 the first value
	 * @param v2 the second value
	 * @return -1 if v1 &lt; v2, 1 if v1 > v2, 0 otherwise
	 */
	public static int compare(long v1, long v2) {
		return v1 < v2 ? -1 : (v1 > v2 ? 1 : 0);
	}
	/**
	 * Close the parameter silently.
	 * @param c the closeable
	 */
	public static void close(Closeable c) {
		if (c != null) {
			try {
				c.close();
			} catch (IOException ex) {
				// ignored
			}
		}
	}
	/**
	 * Returns an integer parameter or the default value.
	 * @param parameters the parameter map
	 * @param name the parameter name
	 * @param def the default value
	 * @return the int value
	 */
	public static int getInt(Map<String, String> parameters, String name, int def) {
		String s = parameters.get(name);
		if (s == null) {
			s = parameters.get(decamelcase(name));
		}
		return s != null ? Integer.parseInt(s) : def;
	}
	/**
	 * Returns an double parameter or the default value.
	 * @param parameters the parameter map
	 * @param name the parameter name
	 * @param def the default value
	 * @return the double value
	 */
	public static double getDouble(Map<String, String> parameters, String name, double def) {
		String s = parameters.get(name);
		if (s == null) {
			s = parameters.get(decamelcase(name));
		}
		return s != null ? Double.parseDouble(s) : def;
	}
	/**
	 * Convert a camel-cased text into a dashed lower case.
	 * @param s the string to convert
	 * @return the dashed conversion.
	 */
	public static String decamelcase(String s) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (Character.isUpperCase(c)) {
				b.append('-');
				b.append(Character.toLowerCase(c));
			} else {
				b.append(c);
			}
		}
		return b.toString();
	}
}
