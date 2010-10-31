/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.launcher;

/**
 * The list of files to download from a specific location.
 * @author karnokd, 2010.10.31.
 * @version $Revision 1.0$
 */
public class LFile extends LBaseItem {
	/** The URL to the file. */
	public String url;
	/** The MD5 hash of the target file. */
	public String md5;
	/** The SHA1 hash of the target file. */
	public String sha1;
}
