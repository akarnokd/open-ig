/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.TimeZone;

/**
 * Model utilities.
 * @author akarnokd, 2013.05.05.
 */
public final class ModelUtils {
	/** Utility class. */
	private ModelUtils() { }
	/** The date formatter. */
	public static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			sdf.setCalendar(new GregorianCalendar(TimeZone.getTimeZone("GMT")));
			return sdf;
		}
	};
	/**
	 * The random number generator for simulation/AI activities.
	 */
	public static final ThreadLocal<Random> RANDOM = new ThreadLocal<Random>() {
		@Override
		public Random get() {
			return new Random();
		}
	};
	/**
	 * Format a date object.
	 * @param date the date
	 * @return the string representation
	 */
	public static String format(Date date) {
		return DATE_FORMAT.get().format(date);
	}
	/**
	 * Parses a date from string.
	 * @param s the string representation
	 * @return the date
	 * @throws ParseException on format error
	 */
	public static Date parse(String s) throws ParseException {
		return DATE_FORMAT.get().parse(s);
	}
	/**
	 * Returns a random integer from 0 to max.
	 * @param max the maximum value, exclusive
	 * @return the random integer
	 */
	public static int randomInt(int max) {
		return RANDOM.get().nextInt(max);
	}
	/**
	 * Returns a random value between 0 and 1.
	 * @return the random value
	 */
	public static double random() {
		return RANDOM.get().nextDouble();
	}
	/**
	 * Returns a true or false randomly.
	 * @return the random boolean value
	 */
	public static boolean randomBool() {
		return RANDOM.get().nextBoolean();
	}
	/**
	 * Returns a random long.
	 * @return the random long
	 */
	public static long randomLong() {
		return RANDOM.get().nextLong();
	}
	/**
	 * Shuffle the given list in-place.
	 * @param <T> the list type
	 * @param list the list to shuffle
	 * @return the list
	 */
	public static <T> List<T> shuffle(List<T> list) {
		Collections.shuffle(list, RANDOM.get());
		return list;
	}
	/**
	 * Returns a random element from the list.
	 * @param <T> the element type
	 * @param ts the list of elements
	 * @return the selected element
	 */
	public static <T> T random(Collection<T> ts) {
		int idx = ModelUtils.randomInt(ts.size());
		if (ts instanceof List<?>) {
			return ((List<T>)ts).get(idx);
		}
		Iterator<T> it = ts.iterator();
		int i = 0;
		while (it.hasNext()) {
			T t = it.next();
			if (i == idx) {
				return t;
			}
			i++;
		}
		throw new NoSuchElementException();
	}
	/**
	 * Returns a random element from the given array.
	 * @param <T> the element type
	 * @param ts the array of Ts
	 * @return the random element
	 */
	@SafeVarargs
	public static <T> T random(T... ts) {
		int idx = ModelUtils.randomInt(ts.length);
		return ts[idx];
	}
}
