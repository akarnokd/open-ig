/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

/**
 * A basic mutable long value.
 * @author akarnokd, 2013.05.05.
 */
public class LongField {
	/** The value. */
	public long value;
	@Override
	public String toString() {
		return String.valueOf(value);
	}
	/*
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LongField) {
			return value == ((LongField)obj).value;
		} else
		if (obj instanceof Number) {
			return value == ((Number)obj).longValue();
		}
		return false;
	}
	@Override
	public int hashCode() {
		return (int)((value >>> 32) ^ (value));
	}
	*/
}
