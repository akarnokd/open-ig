/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.launcher;

/**
 * @author karnokd, 2010.10.31.
 * @version $Revision 1.0$
 */
public class DownloadProgress {
	/** The total amount of bytes to download. */
	public long bytesTotal;
	/** The bytes already received. */
	public long bytesReceived;
	/** The start timestamp of the download. */
	public long startTimestamp;
	/**
	 * @return computes the remaining time
	 */
	public long getRemainingTime() {
		double speed = getSpeed();
		if (speed > 0.0 && bytesTotal >= 0.0) {
			return (long)((bytesTotal - bytesReceived) / speed);
		}
		return Long.MAX_VALUE;
	}
	/**
	 * @return compute the speed in bytes / millisecond
	 */
	public double getSpeed() {
		long td = System.currentTimeMillis() - startTimestamp;
		double speed = 0.0; // bytes / milliseconds
		if (td > 0) {
			speed = 1.0 * bytesReceived / td;
		}
		return speed;
	}
}
