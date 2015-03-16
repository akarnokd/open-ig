/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.tools.ani;

/**
 * Callback interface for notifying clients in
 * the progress of the transcoding.
 * @author karnokd
 */
public interface ProgressCallback {
	/**
	 * Signals the current and the maximum frame count.
	 * @param value the current frame
	 * @param max the maximum frame
	 */
	void progress(int value, int max);
	/** 
	 * Should the operation be terminated?
	 * @return true if the operation should terminate
	 */
	boolean cancel();
}
