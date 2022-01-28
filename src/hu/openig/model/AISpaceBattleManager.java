/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.List;

/**
 * Space battle callback methods.
 * @author akarnokd, 2014.04.01.
 */
public interface AISpaceBattleManager {
    /**
     * Initialize the space battle.
     * <p>Called before the first battle simulation step.</p>
     */
    void spaceBattleInit();
    /**
     * Handle some aspects of a space battle.
     * @param idles the list of objects which have completed their current attack objectives and awaiting new commands
     * @return the global action
     */
    SpacewarAction spaceBattle(List<SpacewarStructure> idles);
    /**
     * Called after the space battle has been concluded and losses applied.
     */
    void spaceBattleDone();

}
