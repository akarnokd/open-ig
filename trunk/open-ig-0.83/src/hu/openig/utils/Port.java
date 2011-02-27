/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.utils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple completion type port.
 * @author karnokd
 * @param <T> the object type to pass around
 */
public class Port<T> {
	/** The lock object. */
	private final Lock lock = new ReentrantLock();
	/** The wait condition. */
	private final Condition cond = lock.newCondition();
	/** The done indicator. */
	private boolean done;
	/** The value passed along the port. */
	private T value;
	/** 
	 * Signal the completion.
	 * @param value the value to pass along 
	 */
	public void signal(T value) {
		lock.lock();
		try {
			this.value = value;
			done = true;
			cond.signal();
		} finally {
			lock.unlock();
		}
	}
	/** 
	 * Await the completion.
	 * @return the object passed along
	 * @throws InterruptedException if the wait is interrupted 
	 */
	public T await() throws InterruptedException {
		lock.lock();
		try {
			while (!done) {
				cond.await();
			}
			cond.signal();
		} finally {
			lock.unlock();
		}
		return value;
	}
	/**
	 * Await the completion for the given period of time.
	 * @param time the time
	 * @param unit the unit
	 * @return the object passed along
	 * @throws InterruptedException if the wait is interrupted
	 * @throws TimeoutException if the wait timed out
	 */
	public T await(long time, TimeUnit unit) throws InterruptedException, TimeoutException {
		lock.lock();
		try {
			while (!done) {
				boolean res = cond.await(time, unit);
				if (!res && !done) {
					throw new TimeoutException("timeout");
				}
			}
			cond.signal();
		} finally {
			lock.unlock();
		}
		return value;
	}
	
	/**
	 * Reset the done flag to allow the next wait pair.
	 */
	public void reset() {
		lock.lock();
		try {
			done = false;
		} finally {
			lock.unlock();
		}
	}
}
