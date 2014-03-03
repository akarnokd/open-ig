/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;


/**
 * Class that wraps an async result with different
 * value type and implements Action1 to translate
 * between an incoming type {@code T} and outgoing
 * type {@code U}. Implementations shoud set the
 * {@code value} field.
 * @author akarnokd, 2013.05.01.
 *
 * @param <T> the incoming type of this AsyncResult
 * @param <V> the outgoing type of the wrapped AsyncResult
 * @param <E> the error type
 */
public abstract class AsyncTransform<T, V, E extends Exception> 
implements AsyncResult<T, E>, Action1E<T, E> {
	/** The wrapped async result. */
	protected final AsyncResult<? super V, ? super E> out;
	/** The externally transformed value. */
	private V value;
	/** Status check. */
	private boolean hasValue;
	/**
	 * Constructor.
	 * @param out the output async result
	 */
	public AsyncTransform(AsyncResult<? super V, ? super E> out) {
		this.out = out;
	}
	@Override
	public void onSuccess(T value) {
		if (hasValue) {
			out.onSuccess(this.value);
		} else {
			throw new IllegalStateException("value not set");
		}
	}
	@Override
	public void onError(E ex) {
		out.onError(ex);
	}
	/**
	 * Sets the given value, so the notification
	 * simply ignores the {@code onSuccess} value.
	 * @param value the value to use instead
	 */
	public void setValue(V value) {
		this.value = value;
		this.hasValue = true;
	}
}