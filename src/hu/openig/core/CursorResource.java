/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

import hu.openig.model.Cursors;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author p-smith, 2016.01.22.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CursorResource {
    int x();
    int y();
    Cursors key();
}
