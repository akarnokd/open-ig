/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An asynchronous subject that stores
 * a result value or exception and allows
 * clients to wait for it to receive, or
 * allows other AsyncResult objects to be notified.
 * @author akarnokd, 2013.05.04.
 * @param <T> the value type
 * @param <E> the error type
 */
public class AsyncSubject<T, E extends Exception> implements AsyncResult<T, E> {
	/**
	 * An action that unregisters the observer.
	 * @author akarnokd, 2013.05.04.
	 */
	private final class UnregisterAction implements Action0 {
		/** The observer to unregister. */
		private final AsyncResult<? super T, ? super E> observer;
		/**
		 * Constructor, sets the observer.
		 * @param observer the observer
		 */
		private UnregisterAction(AsyncResult<? super T, ? super E> observer) {
			this.observer = observer;
		}

		@Override
		public void invoke() {
			lock.lock();
			try {
				clients.remove(observer);
			} finally {
				lock.unlock();
			}
		}
	}
	/** The value or error result. */
	protected Object value;
	/** Is this an error. */
	protected boolean error;
	/** The value has already been set. */
	protected boolean done;
	/** The list of clients. */
	protected final List<AsyncResult<? super T, ? super E>> clients = new ArrayList<>();
	/** The lock protecting the data structures. */
	protected final Lock lock = new ReentrantLock();
	/** The waiting condition. */
	protected final Condition cond = lock.newCondition();
	/** An empty action. */
	protected static final Action0 EMPTY_ACTION0 = new Action0() {
		@Override
		public void invoke() {
			
		}
	};
	@Override
	public void onError(E ex) {
		lock.lock();
		try {
			if (!done) {
				error = true;
				value = ex;
				done = true;
				cond.signalAll();
			} else {
				throw new IllegalStateException("Result already set");
			}
		} finally {
			lock.unlock();
		}
	}
	@Override
	public void onSuccess(T value) {
		lock.lock();
		try {
			if (!done) {
				error = true;
				this.value = value;
				done = true;
				cond.signalAll();
			} else {
				throw new IllegalStateException("Result already set");
			}
		} finally {
			lock.unlock();
		}
	}
	/**
	 * Register an async result observer with this subject.
	 * If the subject has already finished, the observer
	 * is immediately notified of the resulting value
	 * on the caller's thread.
	 * @param observer the observer to register
	 * @return the action to deregister this observer
	 */
	public Action0 register(final AsyncResult<? super T, ? super E> observer) {
		lock.lock();
		try {
			if (!done) {
				clients.add(observer);
				return new UnregisterAction(observer);
			}
			return EMPTY_ACTION0;
		} finally {
			lock.unlock();
		}
	}
	/**
	 * Synchronously wait for the result.
	 * @return the value
	 * @throws E the error, which might be InterruptedException
	 */
	@SuppressWarnings("unchecked")
	public T get() throws Exception {
		lock.lock();
		try {
			while (!done) {
				cond.await();
			}
			if (error) {
				throw (E)value;
			}
			return (T)value;
		} finally {
			lock.unlock();
		}
	}
	/**
	 * Synchronously wait for the result up to a certain amount of time.
	 * @param time the time value
	 * @param unit the time unit
	 * @return the value
	 * @throws Exception the error
	 */
	@SuppressWarnings("unchecked")
	public T get(long time, TimeUnit unit) throws Exception {
		lock.lock();
		try {
			while (!done) {
				if (!cond.await(time, unit)) {
					throw new TimeoutException();
				}
			}
			if (error) {
				throw (E)value;
			}
			return (T)value;
		} finally {
			lock.unlock();
		}
	}
	/**
	 * Is this subject done?
	 * @return true if done
	 */
	public boolean isDone() {
		lock.lock();
		try {
			return done;
		} finally {
			lock.unlock();
		}
	}
	/**
	 * Is this subject an error?
	 * <p>Note that the result is only valid if
	 * <code>isDone()</code> returns true.</p>
	 * @return true if this contains an error
	 */
	public boolean isError() {
		lock.lock();
		try {
			return error;
		} finally {
			lock.unlock();
		}
	}
	/**
	 * Returns the subject's result value.
	 * <p>Note that the result is only valid if
	 * <code>isDone()</code> returns true.</p>
	 * @return the value
	 */
	@SuppressWarnings("unchecked")
	public T value() {
		lock.lock();
		try {
			if (error) {
				throw new IllegalStateException("is error");
			}
			return (T)value;
		} finally {
			lock.unlock();
		}
	}
	/**
	 * Returns the subject's error value.
	 * <p>Note that the result is only valid if
	 * <code>isDone()</code> returns true.</p>
	 * @return the value
	 */
	@SuppressWarnings("unchecked")
	public E error() {
		lock.lock();
		try {
			if (!error) {
				throw new IllegalStateException("isn't error");
			}
			return (E)value;
		} finally {
			lock.unlock();
		}
	}
}
