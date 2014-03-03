/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

import javax.swing.SwingUtilities;

/**
 * Implementation of the async result which redispatches the result
 * on the event dispatch thread if not already on the EDT.
 * @author akarnokd, 2013.05.31.
 * @param <T> the value type
 * @param <E> the exception type
 */
public abstract class EDTResult<T, E> implements AsyncResult<T, E> {

	@Override
	public final void onSuccess(final T value) {
		if (SwingUtilities.isEventDispatchThread()) {
			success(value);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					success(value);
				}
			});
		}
	}
	/**
	 * The success callback.
	 * @param value the value
	 */
	public abstract void success(T value);

	@Override
	public final void onError(final E ex) {
		if (SwingUtilities.isEventDispatchThread()) {
			error(ex);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					error(ex);
				}
			});
		}
	}
	/**
	 * The error callback.
	 * @param ex the exception
	 */
	public abstract void error(E ex);

}
