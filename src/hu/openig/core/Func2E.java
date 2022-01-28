/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

/**
 * A two parameter function.
 * @author akarnokd, 2011.09.06.
 * @param <T> the first parameter type
 * @param <U> the second parameter type
 * @param <R> the result type
 * @param <E> the exception type
 */
public interface Func2E<T, U, R, E extends Exception> {
    /**
     * Invokes the function.
     * @param t the first parameter
     * @param u the second parameter
     * @return the result
     * @throws E on error
     */
    R invoke(T t, U u) throws E;
}
