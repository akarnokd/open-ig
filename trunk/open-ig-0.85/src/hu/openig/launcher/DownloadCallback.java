/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.launcher;

/**
 * The download completion callback.
 * @author akarnokd, 2010.10.31.
 */
public interface DownloadCallback {
	/**
	 * Invoke the progress.
	 * @param progress the current progress
	 */
	void progress(DownloadProgress progress);
	/**
	 * The download succeded.
	 * @param progress the progress
	 * @param sha1 the SHA1 hash
	 */
	void success(DownloadProgress progress, byte[] sha1);
	/**
	 * The download failed.
	 * @param exception the exception
	 */
	void failed(Throwable exception);
	/**
	 * The download progress has been cancelled.
	 */
	void cancelled();
}
