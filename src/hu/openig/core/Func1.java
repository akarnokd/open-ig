/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

/**
 * A function interface with a single parameter.
 * @author akarnokd, Mar 29, 2011
 * @param <T> the parameter type
 * @param <R> the result type
 */
public interface Func1<T, R> {
    /**
     * Invoke the function.
     * @param value the parameter value
     * @return the return value
     */
    R invoke(T value);
}
