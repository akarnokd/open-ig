/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.swing.SwingUtilities;

/**
 * Helper class to construct schedulers.
 * @author akarnokd, 2013.04.30.
 */
public final class Schedulers {
	/**
	 * A scheduled async result wrapper.
	 * @author akarnokd, 2013.04.30.
	 *
	 * @param <V> the value type
	 * @param <E> the error type
	 */
	public static final class ScheduledAsyncResult<V, E> implements
			AsyncResult<V, E> {
		/** The wrapped async result. */
		private final AsyncResult<? super V, ? super E> onResult;
		/** The scheduler. */
		private final Scheduler scheduler;

		/**
		 * Constructor, sets the fields.
		 * @param onResult the async result to wrap.
		 * @param scheduler the scheduler
		 */
		private ScheduledAsyncResult(
				AsyncResult<? super V, ? super E> onResult, Scheduler scheduler) {
			this.onResult = onResult;
			this.scheduler = scheduler;
		}

		@Override
		public void onSuccess(final V value) {
			scheduler.schedule(new Runnable() {
				@Override
				public void run() {
					onResult.onSuccess(value);
				}
			});
		}

		@Override
		public void onError(final E ex) {
			scheduler.schedule(new Runnable() {
				@Override
				public void run() {
					onResult.onError(ex);
				}
			});
		}
	}
	/** Helper class. */
	private Schedulers() { }
	/** The EDT scheduler. */
	private static final Scheduler EDT = new Scheduler() {
		@Override
		public Future<?> schedule(Runnable run) {
			final FutureTask<Void> ft = new FutureTask<>(run, null);
			SwingUtilities.invokeLater(ft);
			return ft;
		}
	};
	/** The EDT scheduler. */
	private static final Scheduler CURRENT = new Scheduler() {
		@Override
		public Future<?> schedule(Runnable run) {
			final FutureTask<Void> ft = new FutureTask<>(run, null);
			ft.run();
			return ft;
		}
	};
	/**
	 * @return the EDT scheduler
	 */
	public static Scheduler edt() {
		return EDT;
	}
	/**
	 * @return the current thread scheduler
	 */
	public static Scheduler current() {
		return CURRENT;
	}
	/**
	 * Wraps an executor service into a scheduler.
	 * @param exec the executor service
	 * @return the scheduler
	 */
	public static Scheduler executor(final ExecutorService exec) {
		return new Scheduler() {
			@Override
			public Future<?> schedule(Runnable run) {
				return exec.submit(run);
			}
		};
	}
	/**
	 * Wraps an executor service into a scheduler.
	 * @param exec the executor service
	 * @return the scheduler
	 */
	public static Scheduler executor(final Executor exec) {
		return new Scheduler() {
			@Override
			public Future<?> schedule(Runnable run) {
				FutureTask<Void> ft = new FutureTask<>(run, null);
				exec.execute(run);
				return ft;
			}
		};
	}
	/**
	 * Wraps the given {@code onResult} to re-execute
	 * the completion messages on the given scheduler.
	 * @param <V> the value type
	 * @param <E> the error type
	 * @param onResult the async result
	 * @param scheduler the scheduler
	 * @return the new async result
	 */
	public static <V, E> AsyncResult<V, E> scheduleOn(
			final AsyncResult<? super V, ? super E> onResult, 
			final Scheduler scheduler) {
		return new ScheduledAsyncResult<>(onResult, scheduler);
	}
}
