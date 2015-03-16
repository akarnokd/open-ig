/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.launcher;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * The list of files to download from a specific location.
 * @author akarnokd, 2010.10.31.
 */
public class LFile extends LBaseItem {
	/** The URL to the file. */
	public String url;
	/** The SHA1 hash of the target file. */
	public String sha1;
	/** The file size in bytes. */
	public long size;
	/** Indicator of the resource language. */
	public String language;
	/**
	 * Convert the hex string into a byte array.
	 * @param hex the hex string
	 * @return the byte array
	 */
	public static byte[] toByteArray(String hex) {
		byte[] result = new byte[hex.length() / 2];
		for (int i = 0; i < result.length; i++) {
			result[i] = (byte)Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
		}
		return result;
	}
	/**
	 * Returns the filename part from the URL.
	 * @return the filename part
	 * @throws MalformedURLException on malformed data
	 */
	public String name() throws MalformedURLException {
		URL u = new URL(url);
		String fn = u.getFile();
		int idx = fn.lastIndexOf("/");
		if (idx >= 0) {
			fn = fn.substring(idx + 1);
		}
		return fn;
	}
}
