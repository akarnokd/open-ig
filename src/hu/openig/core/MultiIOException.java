/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * IO exception that can have multiple suppressed
 * exceptions.
 * @author akarnokd, 2013.05.05.
 */
public class MultiIOException extends IOException {
	/** */
	private static final long serialVersionUID = -4142647125004280002L;
	/** The list of suppressed exceptions. */
	protected final List<IOException> exceptions = new ArrayList<>();
	/**
	 * Adds the exception to the multi-IO exception. Creates
	 * a new multi-IO exception if ex is null.
	 * @param ex the current multi-IO exception, might be null
	 * @param toAdd the exception to add, not null
	 * @return the ex or a new multi-IO exception
	 */
	public static MultiIOException add(MultiIOException ex, IOException toAdd) {
		if (ex == null) {
			ex = new MultiIOException();
		}
		ex.exceptions.add(toAdd);
		return ex;
	}
	/**
	 * The unmodifiable list of the exceptions.
	 * @return the exception list
	 */
	public List<IOException> exceptions() {
		return Collections.unmodifiableList(exceptions);
	}
	@Override
	public void printStackTrace() {
		super.printStackTrace();
		for (IOException ex : exceptions) {
			ex.printStackTrace();
		}
	}
	@Override
	public void printStackTrace(PrintStream s) {
		super.printStackTrace(s);
		for (IOException ex : exceptions) {
			ex.printStackTrace();
		}
	}
	@Override
	public void printStackTrace(PrintWriter s) {
		super.printStackTrace(s);
		for (IOException ex : exceptions) {
			ex.printStackTrace();
		}
	}
}
