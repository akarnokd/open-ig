/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.launcher;

import java.util.Date;

/**
 * The general release version properties.
 * @author akarnokd, 2014 nov. 12
 */
public class LReleaseVersion {
	/** The version number. */
	public String version;
	/** The release date. */
	public Date date;
	@Override
	public boolean equals(Object other) {
		if (other instanceof LReleaseVersion) {
			LReleaseVersion lReleaseVersion = (LReleaseVersion) other;
			return version.equals(lReleaseVersion.version);
		}
		return false;
	}
	@Override
	public int hashCode() {
		return version.hashCode();
	}
}
