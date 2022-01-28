/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

/**
 * A function interface without parameters.
 * @author akarnokd, Mar 29, 2011
 * @param <R> the result type
 * @param <E> the exception
 */
public interface Func0E<R, E extends Exception> {
    /**
     * Invoke the function.
     * @return the return value
     * @throws E exception
     */
    R invoke() throws E;
}
