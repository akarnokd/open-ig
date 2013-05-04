/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

import java.util.ArrayList;
import java.util.List;
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
public class AsyncSubject<T, E> implements AsyncResult<T, E> {
	/** The value or error result. */
	protected Object value;
	/** Is this an error. */
	protected boolean error;
	/** The value has already been set. */
	protected boolean done;
	/** The list of clients. */
	protected final List<AsyncResult<? super T, ? super E>> clients = new ArrayList<AsyncResult<? super T, ? super E>>();
	/** The lock protecting the data structures. */
	protected final Lock lock = new ReentrantLock();
	/** The waiting condition. */
	protected final Condition cond = lock.newCondition();
	@Override
	public void onError(E ex) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onSuccess(T value) {
		// TODO Auto-generated method stub
		
	}
}
