/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools.ani;

/**
 * A simple interface which allows the caller to decode a palette into RGBA
 * color.
 * @author karnokd, 2009.01.11.
 * @version $Revision 1.0$
 */
public interface PaletteDecoder {
    /**
     * Retrieves the RGBA color associated to the given color index
     * in respect to the given palette. The actual palette might be
     * stored in the implementing class or elsewhere
     * @param index the color index
     * @return the RGBA color of the given color index
     */
    int getColor(int index);
}
