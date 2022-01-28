/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Difficulty;

import java.io.File;
import java.util.Date;

/** The file item. */
public class FileItem {
    /** The save name. */
    public String saveName;
    /** The file to load. */
    public final File file;
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
    /** Skirmish? */
    public boolean skirmish;
    /**
     * Constructor.
     * @param file the backing file
     */
    public FileItem(File file) {
        this.file = file;
    }
}
