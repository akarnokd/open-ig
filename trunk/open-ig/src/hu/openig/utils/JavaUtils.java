/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Utility class for missing java functionalities.
 * @author karnokd
 */
public final class JavaUtils {
	/** Constructor. */
	private JavaUtils() {
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
}
