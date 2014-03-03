/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The global exception manager class.
 * @author akarnokd, 2012.08.03.
 */
public final class Exceptions {
	/** Utility class. */
	private Exceptions() { }
	/**
	 * The set to remember exception stacktraces.
	 */
	public static final ConcurrentMap<String, String> HISTORY = new ConcurrentHashMap<>();
	/**
	 * Add a throwable to the history and show it if this is the first time.
	 * @param t the throwable
	 */
	public static void add(Throwable t) {
		add(t, null);
	}
	/**
	 * Add a throwable and message to the history and show it if this is the first time.
	 * @param t the exception
	 * @param message the additional message
	 */
	public static void add(Throwable t, String message) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		if (message != null) {
			pw.println(message);
		}
		if (t != null) {
			t.printStackTrace(pw);
		}
		pw.flush();
		final String s = sw.toString();
		
		if (HISTORY.putIfAbsent(s, s) == null) {
			System.err.println(s);
		}
	}
	/**
	 * Clear the history.
	 */
	public static void clear() {
		HISTORY.clear();
	}
}
