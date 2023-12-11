/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

/**
 * Tracks the player's lost battles during ranks 1 and 2 and triggers game over if too many were lost.
 * @author akarnokd, Dec 11, 2023
 */
public class Mission27 extends Mission {

    @Override
    public boolean applicable() {
        return world.level <= 2;
    }

    @Override
    public void onTime() {
        if (player.statistics.spaceLoses.value + player.statistics.groundLoses.value >= 4) {
            if (!hasTimeout("Mission-27-Fire")) {
                addTimeout("Mission-27-Fire", 13000);
            }
        }
        if (checkTimeout("Mission-27-Fire")) {
            gameover();
            loseGameMessageAndMovie("Douglas-Fire-Battle",
                    world.level == 1 ? "lose/fired_level_1" : "lose/fired_level_2");
        }
    }
}
