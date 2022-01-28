/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The assignment enumeration for spying.
 * @author akarnokd, 2013.10.02.
 */
public enum SpyAssignment {
    /**

     * Train spy.<br>
     * Parameter fields: {@code trainingLevel} = 1 .. 5<br>
     * Costs: 1000, 2000, 5000, 10000, 20000<br>
     * Durations: 50, 75, 100, 150, 200<br>
     */
    TRAINING,
    /**
     * Get empire information.<br>
     * Parameter fields: {@code targetEmpire}
     * Cost: 500<br>
     * Duration: 50<br>
     */
    EMPIRE_INFO,
    /**
     * Get planet information.<br>
     * Parameter fields: {@code targetEmpire}, {@code targetPlanet}<br>
     * Cost: 500<br>
     * Duration: 50<br>
     */
    PLANET_INFO,
    /**
     * Get research information.<br>
     * Parameter fields: {@code targetEmpire}<br>
     * Cost: 1000<br>
     * Duration: 50<br>
     */
    RESEARCH_INFO,
    /**
     * Steal money.<br>
     * Parameter fields: {@code targetEmpire}<br>
     * Cost: 5000<br>
     * Duration: 100<br>
     */
    STEAL_MONEY,
    /**
     * Steal research.<br>
     * Parameter fields: {@code targetEmpire}, {@code research}<br>
     * Cost: 10000<br>
     * Duration: 150<br>
     */
    STEAL_RESEARCH,
    /**
     * Sabotage building.<br>
     * Parameter fields: {@code targetEmpire}, {@code targetPlanet}, {@code buildingKind}<br>
     * Cost: 1500<br>
     * Duration: 100<br>
     */
    SABOTAGE_BUILDING,
    /**
     * Sabotage ship in a fleet.<br>
     * Parameter fields: {@code targetEmpire}, {@code research}<br>
     * Cost: 2000<br>
     * Duration: 100<br>
     */
    SABOTAGE_FLEET,
    /**
     * Counter-spy.
     * Parameter fields: none
     * Cost: 500<br>
     * Duration: continuous<br>
     */
    COUNTER_SPY
}
