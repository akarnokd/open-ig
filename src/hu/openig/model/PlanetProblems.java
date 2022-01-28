/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The enumeration for displaying planetary problems on the starmap and other screens.
 * @author akarnokd, 2010.08.22.
 */
public enum PlanetProblems {
    /** Not enough living space. */
    HOUSING,
    /** Not enough food. */
    FOOD,
    /** Not enough power. */
    ENERGY,
    /** Not enough hospital. */
    HOSPITAL,
    /** Not enough workforce. */
    WORKFORCE,
    /** Virus problems. */
    VIRUS,
    /** Population demands a stadium. */
    STADIUM,
    /** Buildings need repair. */
    REPAIR,
    /** Colony hub missing. */
    COLONY_HUB,
    /** Not enough police. */
    POLICE,
    /** Fire brigade needed. */
    FIRE_BRIGADE
}
