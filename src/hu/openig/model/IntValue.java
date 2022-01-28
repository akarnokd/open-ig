/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * A mutable integer value.
 * @author akarnokd, 2012.12.23.
 */
public class IntValue {
    /** The value. */
    public int value;
    /**
     * Constructor.
     */
    public IntValue() {
    }
    /**
     * Constructor with initial value.
     * @param initialValue the initial value
     */
    public IntValue(int initialValue) {
        this.value = initialValue;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntValue) {
            IntValue intValue = (IntValue) obj;
            return value == intValue.value;
        }
        return false;
    }
    @Override
    public int hashCode() {
        return value;
    }
    /**
     * @return creates a copy of this value
     */
    public IntValue copy() {
        return new IntValue(value);
    }
}
