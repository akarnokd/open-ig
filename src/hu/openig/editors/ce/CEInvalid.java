/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import javax.swing.ImageIcon;

/**
 * Interface to indicate the component supports invalidation notification.
 * @author akarnokd, 2012.11.03.
 */
public interface CEInvalid {
    /**
     * Set the invalid icon and display text.
     * @param icon the icon
     * @param errorText the text
     */
    void setInvalid(ImageIcon icon, String errorText);
    /** Clear the invalid indicators. */
    void clearInvalid();
    /** @return get the invalid icon. */
    ImageIcon getInvalid();
}
