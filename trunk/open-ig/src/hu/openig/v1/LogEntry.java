/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Log entry.
 * @author karnok, 2009.09.25.
 * @version $Revision 1.0$
 */
public class LogEntry {
	/** The timestamp. */
	public long timestamp;
	/** The severity. */
	public String severity;
	/** The message. */
	public String message;
	/** The associated stacktrace. */
	public String stackTrace;
	/**
	 * Create a log entry from an exception.
	 * @param t the throwable object.
	 * @return the log entry
	 */
	public static LogEntry from(Throwable t) {
		LogEntry result = new LogEntry();
		result.timestamp = System.currentTimeMillis();
		result.severity = "ERROR";
		result.message = t.getMessage();
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.flush();
		result.stackTrace = sw.toString();
		return result;
	}
}
