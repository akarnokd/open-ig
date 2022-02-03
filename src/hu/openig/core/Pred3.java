/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

/**
 * A predicate with three parameters.
 * @author akarnokd, 2022.02.03.
 * @param <T> the first parameter type
 * @param <U> the second parameter type
 * @param <V> the third parameter type
 */
public interface Pred3<T, U, V> {
    /**
     * Invokes the predicate.
     * @param t the first parameter
     * @param u the second parameter
     * @param v the third parameter
     * @return the result
     */
    boolean invoke(T t, U u, V v);
}
