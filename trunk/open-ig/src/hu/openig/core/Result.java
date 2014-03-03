/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

/**
 * A synchronous result with value or error.
 * Use the {@code newError} or {@code newValue} to create instances.
 * The class is immutable.
 * <p>The dual of AsyncResult.</p>
 * @author akarnokd, 2013.04.27.
 * @param <T> the result type
 * @param <E> the error type
 * @see AsyncResult
 */
public final class Result<T, E> {
	/** The error indicator. */
	private final boolean isError;
	/** The result or error. */
	private final Object value;
	/**
	 * Constructor.
	 * @param isError is this an error
	 * @param value the value or error object
	 */
	private Result(boolean isError, Object value) {
		this.isError = isError;
		this.value = value;
	}
	/**
	 * Is this result an error?
	 * @return true if this is an error
	 */
	public boolean isError() {
		return isError;
	}
	/**
	 * Return the value.
	 * @return the value, might be null
	 */
	@SuppressWarnings("unchecked")
	public T value() {
		if (isError) {
			throw new IllegalStateException("Is error!");
		}
		return (T)value;
	}
	/**
	 * Return the error.
	 * @return the error, not null
	 */
	@SuppressWarnings("unchecked")
	public E error() {
		if (!isError) {
			throw new IllegalStateException("Is not error!");
		}
		return (E)value;
	}
	/**
	 * Constructs a result object with the given value.
	 * @param value the value, might be null
	 * @return a non-error result instance
	 * @param <T> the value type
	 * @param <E> the error type
	 */
	public static <T, E> Result<T, E> newValue(T value) {
		return new Result<>(false, value);
	}
	/**
	 * Constructs a result object with the given error.
	 * @param error the error object
	 * @return a error result instance
	 * @param <T> the value type
	 * @param <E> the error type
	 * @param <F> the inference fix type
	 */
	public static <T, E, F extends E> Result<T, E> newError(F error) {
		return new Result<>(true, error);
	}
	/**
	 * Sends the value or error to the given async result.
	 * @param out the output async result
	 */
	@SuppressWarnings("unchecked")
	public void send(AsyncResult<? super T, ? super E> out) {
		if (isError) {
			out.onError((E)value);
		} else {
			out.onSuccess((T)value);
		}
	}
	/** The constant void result. */
	private static final Result<Void, Object> VOID = newValue(null);
	/**
	 * Returns a properly typed void value result.
	 * @param <E> the unused error type
	 * @return the void result
	 */
	@SuppressWarnings("unchecked")
	public static <E> Result<Void, E> newVoid() {
		return (Result<Void, E>)VOID;
	}
	/**
	 * Cast this class into the given value-type
	 * in case this is in fact an error.
	 * @param <U> the expected value type, irrelevant
	 * @return the cast this
	 */
	@SuppressWarnings("unchecked")
	public <U> Result<U, E> castError() {
		if (isError) {
			return (Result<U, E>)this;
		}
		throw new IllegalStateException("Must be just an error");
	}
}
