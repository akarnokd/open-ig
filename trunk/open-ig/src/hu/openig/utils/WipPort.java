/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.utils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Port that has a work in progress counter.
 * The port is complete when the work in progress counter returns to zero.
 * @author karnokd
 */
public class WipPort {
	/** The completion port. */
	private final Port<Void> complete = new Port<Void>();
	/** The work in progress counter. */
	private final AtomicInteger wip = new AtomicInteger();
	/**
	 * Increment a work in progress counter.
	 */
	public void inc() {
		wip.incrementAndGet();
	}
	/** 
	 * Decrement the work in progress counter and signal if it reached zero.
	 * Throws IllegalStateException if the wip counter goes below zero.
	 */
	public void dec() {
		int value = wip.decrementAndGet();
		if (value == 0) {
			complete.signal(null);
		} else
		if (value < 0) {
			throw new IllegalStateException("Broken WIP port: " + value);
		}
	}
	/**
	 * Await the completion signal.
	 * @throws InterruptedException if the wait was interrupted
	 */
	public void await() throws InterruptedException {
		complete.await();
	}
	/**
	 * Await the completion signal for the specified amount of time.
	 * @param time the time
	 * @param unit the unit
	 * @throws InterruptedException if the wait was interrupted
	 * @throws TimeoutException if the wait timed out
	 */
	public void await(long time, TimeUnit unit) throws InterruptedException, TimeoutException {
		complete.await(time, unit);
	}
	/**
	 * Reset the completion port to reuse.
	 */
	public void reset() {
		complete.reset();
	}

}
