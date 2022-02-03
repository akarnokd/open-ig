/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

/**
 * A predicate with two parameters.
 * @author akarnokd, 2011.12.28.
 * @param <T> the first parameter type
 * @param <U> the second parameter type
 */
public interface Pred2<T, U> extends Func2<T, U, Boolean> {

}
