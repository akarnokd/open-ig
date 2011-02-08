/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.xold.core;

import hu.openig.utils.JavaUtils;

/**
 * Tuple containing two different types.
 * Immutable
 * @author karnokd, 2009.05.25.
 * @version $Revision 1.0$
 * @param <T1> the type of the first element
 * @param <T2> the type of the second element
 */
public final class Tuple2<T1, T2> {
	/** The first element. */
	public final T1 first;
	/** The second element. */
	public final T2 second;
	/**
	 * Private constructor.
	 * @param t1 the first value
	 * @param t2 the second value
	 */
	public Tuple2(T1 t1, T2 t2) {
		this.first = t1;
		this.second = t2;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Tuple2<?, ?>) {
			Tuple2<?, ?> o = (Tuple2<?, ?>)obj;
			return JavaUtils.equal(first, o.first) 
			&& JavaUtils.equal(second, o.second);
		}
		return false;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return 17 + 31 * (first != null ? first.hashCode() : 0)
		+ (second != null ? second.hashCode() : 0);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "(" + first + ", " + second + ")";
	}
}
