/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.utils;

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
	 * @return the comparison
	 */
	public static int naturalCompare(String s1, String s2) {
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
				result = a1[i].compareTo(a2[i]);
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
}
