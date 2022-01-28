/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The global strategy to use when attacking an enemy planet.
 * @author akarnokd, 2011.12.15.
 */
public enum AIAttackMode {
    /** Aim for capturing the planet. */
    CAPTURE,
    /**

     * Aim for cripple the planet's production/research capabilities.
     * <p>May be used by the AI if it can't raise a big enough fleet but would like to slow down the enemy.</p>

     */
    CRIPPLE
}
