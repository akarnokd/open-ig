/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.utils;

import hu.openig.core.Func1;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
					Exceptions.add(t);
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
	 * Close all the closeables and return all exceptions at once.
	 * @param cs the array of closeables
	 * @throws IOException the exception
	 */
	public static void close(Closeable... cs) throws IOException {
		IOException ex = null;
		for (Closeable c : cs) {
			try {
				if (c != null) {
					c.close();
				}
			} catch (IOException exc) {
				ex = exc; // FIXME aggregate exceptions
			}
		}
		if (ex != null) {
			throw ex;
		}
	}
	/**
	 * Close all the closeables and return all exceptions at once.
	 * @param cs the array of closeables
	 * @throws IOException the exception
	 */
	public static void close(Iterable<? extends Closeable> cs) throws IOException {
		IOException ex = null;
		List<Closeable> cs1 = new ArrayList<Closeable>();
		for (Closeable c : cs) {
			cs1.add(c);
		}
		for (Closeable c : cs1) {
			try {
				if (c != null) {
					c.close();
				}
			} catch (IOException exc) {
				ex = exc; // FIXME aggregate exceptions
			}
		}
		if (ex != null) {
			throw ex;
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
	/**
	 * Returns the maximum.
	 * @param is the array ints
	 * @return the maximum
	 */
	public static int max(int... is) {
		int r = is[0];
		for (int i = 1; i < is.length; i++) {
			r = Math.max(r, is[i]);
		}
		return r;
	}
	/**
	 * Pad the string from the left with the given characters up to the length.
	 * @param s the string
	 * @param length the length
	 * @param c the padding character
	 * @return the new string
	 */
	public static String padLeft(String s, int length, char c) {
		if (s.length() >= length) {
			return s;
		}
		StringBuilder b = new StringBuilder();
		length -= s.length();
		while (length-- > 0) {
			b.append(c);
		}
		b.append(s);
		return b.toString();
	}
	/**
	 * Join the specified array of elements by the given separator.
	 * @param objs the object array
	 * @param separator the separator
	 * @return the string
	 */
	public static String join(Object[] objs, String separator) {
		StringBuilder b = new StringBuilder();
		
		if (objs != null) {
			int i = 0;
			for (Object o : objs) {
				if (i > 0) {
					b.append(separator);
				}
				b.append(o);
				i++;
			}
		}
		return b.toString();
	}
	/**
	 * Join the specified sequence of elements by the given separator.
	 * @param objs the object array
	 * @param separator the separator
	 * @return the string
	 */
	public static String join(Iterable<?> objs, String separator) {
		StringBuilder b = new StringBuilder();
		
		if (objs != null) {
			int i = 0;
			for (Object o : objs) {
				if (i > 0) {
					b.append(separator);
				}
				b.append(o);
				i++;
			}
		}
		return b.toString();
	}
	/**
	 * Splits the value string by the given separator.
	 * If the value is empty, an empty array is returned.
	 * @param value the value
	 * @param separator the separator
	 * @return the array of components
	 */
	public static String[] split(String value, String separator) {
		if (value.isEmpty()) {
			return new String[0];
		}
		List<String> result = newArrayList();
		int idx = 0;
		while (true) {
			int idx2 = value.indexOf(separator, idx);
			if (idx2 < 0) {
				result.add(value.substring(idx));
				break;
			} else {
				result.add(value.substring(idx, idx2));
				idx = idx2 + separator.length();
			}
		}
		return result.toArray(new String[0]);
	}
	/**
	 * Returns the stacktrace string.
	 * @param t the exception
	 * @return the string of the stacktrace
	 */
	public static String stacktrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.flush();
		return sw.toString();
	}
	/**
	 * Returns the bigger of the two comparable objects.
	 * @param <T> the self-comparable type
	 * @param t1 the first object
	 * @param t2 the second object
	 * @return the bigger
	 */
	public static <T extends Comparable<? super T>> T max(T t1, T t2) {
		return t1.compareTo(t2) < 0 ? t2 : t1;
	}
	/**
	 * Returns the bigger of the two comparable objects.
	 * @param <T> the self-comparable type
	 * @param t1 the first object
	 * @param t2 the second object
	 * @return the bigger
	 */
	public static <T extends Comparable<? super T>> T min(T t1, T t2) {
		return t1.compareTo(t2) > 0 ? t2 : t1;
	}
	/**
	 * Returns the values of fields which are assignable to the type.
	 * @param <T> the value type
	 * @param o the parent object, non null
	 * @param type the expected minimum type
	 * @return the list of field values
	 */
	public static <T> List<T> fieldsOf(Object o, Class<T> type) {
		List<T> result = newArrayList();
		for (Field f : o.getClass().getDeclaredFields()) {
			if (type.isAssignableFrom(f.getType())) {
				try {
					result.add(type.cast(f.get(o)));
				} catch (IllegalArgumentException e) {
					// ignored
				} catch (IllegalAccessException e) {
					// ignored
				}
			}
		}
		return result;
	}
	/**
	 * Returns a list of the array items or an empty list if the array is null.
	 * @param <T> the element type
	 * @param array the array
	 * @return the non-null list
	 */
	public static <T> List<T> nonNull(T[] array) {
		if (array == null) {
			return U.newArrayList();
		}
		return Arrays.asList(array);
	}
	/**
	 * List the files under a directory.
	 * @param parentDir the parent directory
	 * @return the list of files.
	 */
	public static List<File> listFiles(File parentDir) {
		return nonNull(parentDir.listFiles());
	}
	/**
	 * List files under a directory filtered.
	 * @param parentDir the parent directory
	 * @param filter the filter function
	 * @return the list of files found
	 */
	public static List<File> listFiles(File parentDir, final Func1<File, Boolean> filter) {
		return nonNull(parentDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return filter.invoke(pathname);
			}
		}));
	}
	/**
	 * Wrap the given enumeration into an iterable sequence.
	 * The enumeration is shared between returned iterables.
	 * @param <T> the element type
	 * @param en the enumeration
	 * @return the iterable
	 */
	public static <T> Iterable<T> enumerate(final Enumeration<? extends T> en) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>() {
					@Override
					public boolean hasNext() {
						return en.hasMoreElements();
					}
					@Override
					public T next() {
						return en.nextElement();
					}
					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}
	/**
	 * List entry paths under a zip file.
	 * @param zipFile the zip file
	 * @return the list of elements or empty list if an error occurs or non-zip file
	 */
	public static List<String> zipEntries(File zipFile) {
		List<String> result = U.newArrayList();
		try {
			ZipFile zf = new ZipFile(zipFile);
			try {
				for (ZipEntry ze : enumerate(zf.entries())) {
					result.add(ze.getName());
				}
			} finally {
				zf.close();
			}
		} catch (IOException ex) {
			// ignored
		}
		return result;
	}
	/**
	 * Returns the entry names of the zip file allowed by the filter.
	 * @param zipFile the zip file
	 * @param filter the entry filtering
	 * @return the list of entries
	 */
	public static List<String> zipEntries(File zipFile, Func1<ZipEntry, Boolean> filter) {
		List<String> result = U.newArrayList();
		try {
			ZipFile zf = new ZipFile(zipFile);
			try {
				for (ZipEntry ze : enumerate(zf.entries())) {
					if (filter.invoke(ze)) {
						result.add(ze.getName());
					}
				}
			} finally {
				zf.close();
			}
		} catch (IOException ex) {
			// ignored
		}
		return result;
	}
	/**
	 * List the file-like entries of the zip file under the supplied path.
	 * Returns an empty list if the file is invalid.
	 * @param zipFile the zip file
	 * @param parentPath the parent path
	 * @return the list of file entries
	 */
	public static List<String> zipFileEntries(File zipFile, String parentPath) {
		parentPath = parentPath.replace('\\', '/').replaceAll("/{2,}", "/");
		List<String> result = U.newArrayList();
		try {
			ZipFile zf = new ZipFile(zipFile);
			try {
				for (ZipEntry ze : enumerate(zf.entries())) {
					String zname = ze.getName().replace('\\', '/');
					if (!ze.isDirectory() && zname.startsWith(parentPath)) {
						result.add(ze.getName());
					}
				}
			} finally {
				zf.close();
			}
		} catch (IOException ex) {
			// ignored
		}
		return result;
	}
	/**
	 * List the dir-like entries of the zip file under the supplied path.
	 * Returns an empty list if the file is invalid.
	 * @param zipFile the zip file
	 * @param parentPath the parent path
	 * @return the list of file entries
	 */
	public static List<String> zipDirEntries(File zipFile, String parentPath) {
		parentPath = parentPath.replace('\\', '/').replaceAll("/{2,}", "/");
		List<String> result = U.newArrayList();
		Set<String> once = U.newHashSet();
		try {
			ZipFile zf = new ZipFile(zipFile);
			try {
				for (ZipEntry ze : enumerate(zf.entries())) {
					String zname = ze.getName().replace('\\', '/');
					if (ze.isDirectory() && zname.startsWith(parentPath)) {
						if (once.add(ze.getName())) {
							result.add(ze.getName());
						}
					} else
					if (!ze.isDirectory() && zname.startsWith(parentPath)) {
						String subPath = zname.substring(parentPath.length());
						if (subPath.startsWith("/")) {
							subPath = subPath.substring(1);
						}
						int idx = subPath.indexOf('/');
						if (idx > 0) {
							String path2 = parentPath + "/" + subPath.substring(0, idx);
							if (once.add(path2)) {
								result.add(path2);
							}
						}
					}
				}
			} finally {
				zf.close();
			}
		} catch (IOException ex) {
			// ignored
		}
		return result;
	}
	/**
	 * Returns the data bytes of the given zip entry.
	 * @param zipFile the zip file
	 * @param path the path
	 * @return the bytes, null if not found or not a zip file
	 */
	public static byte[] zipData(File zipFile, String path) {
		byte[] result = null;
		try {
			ZipFile zf = new ZipFile(zipFile);
			try {
				ZipEntry ze = zf.getEntry(path);
				if (ze != null) {
					InputStream in = zf.getInputStream(ze);
					try {
						result = new byte[in.available()];
						in.read(result);
					} finally {
						in.close();
					}
				}
			} finally {
				zf.close();
			}
		} catch (IOException ex) {
			// ignored
		}
		return result;
	}
	/**
	 * Append the given values to the beginning of the sequence in a new List.
	 * @param <T> element type
	 * @param src the source sequence
	 * @param ts the prefix elements
	 * @return the new list
	 */
	public static <T> List<T> startWith(Iterable<? extends T> src, T... ts) {
		List<T> result = newArrayList();
		result.addAll(Arrays.asList(ts));
		for (T t : src) {
			result.add(t);
		}
		return result;
	}
	/**
	 * Trim the contents of the string array inplace.
	 * @param strings the strings
	 * @return the same string array
	 */
	public static String[] trim(String... strings) {
		for (int i = 0; i < strings.length; i++) {
			strings[i] = strings[i].trim();
		}
		return strings;
	}
	/**
	 * The roman number codes.
	 */
    private static final String[] RCODE = {"M", "CM", "D", "CD", "C", "XC", "L",
                                           "XL", "X", "IX", "V", "IV", "I"};
    /** The integer limits per roman number. */
    private static final int[]    BVAL  = {1000, 900, 500, 400,  100,   90,  50,
                                           40,   10,    9,   5,   4,    1};
    /**
     * Convert an integer into a roman number string.
     * @param value the binary
     * @return  the string
     */
    public static String intToRoman(int value) {
        if (value <= 0 || value >= 4000) {
            throw new NumberFormatException("Value outside roman numeral range.");
        }
        StringBuilder roman = new StringBuilder();
        
        // Loop from biggest value to smallest, successively subtracting,
        // from the binary value while adding to the roman representation.
        for (int i = 0; i < RCODE.length; i++) {
            while (value >= BVAL[i]) {
                value -= BVAL[i];
                roman.append(RCODE[i]);
            }
        }
        return roman.toString();
    }
    /**
     * Check if the given string is null or empty.
     * @param s the string to test
     * @return true if null or empty
     */
    public static boolean nullOrEmpty(String s) {
    	return s == null || s.isEmpty();
    }
    /**
     * Check if the given string is null or full of whitespace.
     * @param s the string to test
     * @return true if null of whitespace.
     */
    public static boolean nullOrWhitespace(String s) {
    	return s == null || s.trim().isEmpty();
    }
}
