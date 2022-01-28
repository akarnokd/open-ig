/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

/**
 * A test composite AI which combines the default AI and the User AI.
 * <p>This allows automatic gameplay and UI message responses.</p>
 * @author akarnokd, 2011.12.26.
 */
public class AITest extends AIMixed {
    /**
     * Constructor.
     */
    public AITest() {
        super(new AI(), new AIUser());
    }
}
