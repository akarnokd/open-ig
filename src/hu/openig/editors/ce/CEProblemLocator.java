/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.utils.XElement;

/**
 * Allows the main problems panel to jump to the exact issue in a panel.
 * @author akarnokd, 2012.10.31.
 */
public interface CEProblemLocator {
    /**
     * Locate the problem based on the panel's own description.
     * @param description the data to help show a specific issue
     */
    void locateProblem(XElement description);
}
