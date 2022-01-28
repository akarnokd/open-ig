/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

/**
 * A parameterless function.
 * @author akarnokd, 2011.09.06.
 * @param <R> the result type
 */
public interface Func0<R> {
    /** @return the result */
    R invoke();
}
