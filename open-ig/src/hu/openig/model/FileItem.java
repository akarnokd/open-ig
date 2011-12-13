/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Difficulty;

import java.util.Date;

/** The file item. */
public class FileItem implements Comparable<FileItem> {
	/** The save name. */
	public String name;
	/** The level. */
	public int level;
	/** The difficulty. */
	public Difficulty difficulty;
	/** The ingame date. */
	public Date gameDate;
	/** The save date. */
	public Date saveDate;
	/** The player money. */
	public long money;
	@Override
	public int compareTo(FileItem o) {
		return o.saveDate.compareTo(saveDate);
	}
}
