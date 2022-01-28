/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.launcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a release entry item.
 * @author akarnokd, 2014 nov. 12
 */
public class LReleaseItem {
    /** The item category if not null. */
    public String category;
    /** The associated issues if any. */
    public final List<Integer> issues = new ArrayList<>();
    /** The textual description of the item. */
    public String text;
}
