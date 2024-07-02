/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * Defines the trait kinds used to select the appropriate effect routines.
 * @author akarnokd, 2012.08.18.
 */
public enum TraitKind {
    /** Override hyperdrive availabilities. */
    PRE_WARP,
    /** Change ship production ratio. */
    SHIP_PRODUCTION,
    /** Change equipment production ratio. */
    EQUIPMENT_PRODUCTION,
    /** Change weapon production ratio. */
    WEAPON_PRODUCTION,
    /** Debug option. */
    TRAIT_DEBUG,
    /** Modify fertility values. */
    FERTILE,
    /** Modify tax collection values. */
    TAX,
    /** Hitpoint change of guns, shields and space stations. */
    SPACE_DEFENSE,
    /** Weapon damage. */
    WEAPONS,
    /** Change research costs. */
    SCIENCE,
    /** Reveal nearby planets. */
    ASTRONOMER,
    /** Reveal all planets. */
    ASTRONOMER_PLUS,
    /** Change diplomatic success rates. */
    DIPLOMACY,
    /** Regenerate shields faster, repair ships without military spaceport. */
    ENGINEERS,
    /** Regenerate shields during combat. */
    COMBAT_ENGINEERS,
    /** Groundwar fortifications surrender rate. */
    SURRENDER
}
