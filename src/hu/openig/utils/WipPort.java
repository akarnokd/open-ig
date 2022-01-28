/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.utils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Port that has a work in progress counter.
 * The port is complete when the work in progress counter returns to zero.
 * @author akarnokd
 */
public class WipPort {
    /** The lock object. */
    private final Lock lock = new ReentrantLock();
    /** The wait condition. */
    private final Condition cond = lock.newCondition();
    /** The work in progress counter. */
    private final AtomicInteger wip = new AtomicInteger();
    /**
     * Creates a WipPort with zero value.
     */
    public WipPort() {

    }
    /**
     * Create a WipPort with the initial WIP count.
     * @param initial the initial WIP count.
     */
    public WipPort(int initial) {
        wip.set(initial);
    }
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
            lock.lock();
            try {
                cond.signalAll();
            } finally {
                lock.unlock();
            }
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
        lock.lock();
        try {
            while (wip.get() != 0) {
                cond.await();
            }
        } finally {
            lock.unlock();
        }
    }
}
