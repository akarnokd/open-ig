/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The scaffolding tile definition.
 * @author akarnokd
 */
public class Scaffolding {
    /** The normal elements. */
    public final List<Tile> normal = new ArrayList<>();
    /** The damaged elements. */
    public final List<Tile> damaged = new ArrayList<>();
}
