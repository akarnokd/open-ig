/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

/**
 * A pair of objects.
 * @author akarnokd, 2011.12.29.
 * @param <T> the first object type
 * @param <U> the second object type
 */
public final class Pair<T, U> {
	/** The first object. */
	public final T first;
	/** The second object. */
	public final U second;
	/**
	 * Constructor.
	 * @param first the first object
	 * @param second the second object
	 */
	private Pair(T first, U second) {
		this.first = first;
		this.second = second;
	}
	/**
	 * Construct a new pair.
	 * @param <A> the first object type
	 * @param <B> the second object type
	 * @param a the first object
	 * @param b the second object
	 * @return the pair
	 */
	public static <A, B> Pair<A, B> of(A a, B b) {
		return new Pair<>(a, b);
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Pair<?, ?>) {
			Pair<?, ?> pair = (Pair<?, ?>) obj;
			return 
				(pair.first == first || (pair.first != null && pair.first.equals(first)))
				&& (pair.second == second || (pair.second != null && pair.second.equals(second)))
				;
		}
		return false;
	}
	@Override
	public int hashCode() {
		int h = first != null ? first.hashCode() : 0;
		h = h * 31 + (second != null ? second.hashCode() : 0);
		return h;
	}
	@Override
	public String toString() {
		return "Pair{" + first + ", " + second + "}";
	}
}
