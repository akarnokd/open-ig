/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.utils;

import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

/**
 * Utility class for parallel operations.
 * @author karnokd
 *
 */
public final class Parallels {
	/** Private constructor. */
	private Parallels() {
		// utility class
	}
	/**
	 * Schedule to run a task after the given amount of time delay
	 * in the event dispatch thread.
	 * @param delay the delay in milliseconds
	 * @param task the task to run.
	 */
	public static void runDelayedInEDT(final long delay, final Runnable task) {
		if (task != null) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						TimeUnit.MILLISECONDS.sleep(delay);
						SwingUtilities.invokeLater(task);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}, "Delayed EDT run with" + delay).start();
		}
	}
}
