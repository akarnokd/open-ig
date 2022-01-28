/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

/** The available subCategories. */
public enum ResearchSubCategory {
    /** Spaceships / Fighters (1). */
    SPACESHIPS_FIGHTERS(ResearchMainCategory.SPACESHIPS),
    /** Spaceships / Cruisers (2). */
    SPACESHIPS_CRUISERS(ResearchMainCategory.SPACESHIPS),
    /** Spaceships / Battleships (3). */
    SPACESHIPS_BATTLESHIPS(ResearchMainCategory.SPACESHIPS),
    /** Spaceships / Satellites (4). */
    SPACESHIPS_SATELLITES(ResearchMainCategory.SPACESHIPS),
    /** Spaceships / Stations (5). */
    SPACESHIPS_STATIONS(ResearchMainCategory.SPACESHIPS),

    /** Equipment / Hyperdrives (1). */
    EQUIPMENT_HYPERDRIVES(ResearchMainCategory.EQUIPMENT),
    /** Equipment / Modules (2). */
    EQUIPMENT_MODULES(ResearchMainCategory.EQUIPMENT),
    /** Equipment / Radars (3). */
    EQUIPMENT_RADARS(ResearchMainCategory.EQUIPMENT),
    /** Equipment / Shields (4). */
    EQUIPMENT_SHIELDS(ResearchMainCategory.EQUIPMENT),

    /** Weapons / Lasers (1). */
    WEAPONS_LASERS(ResearchMainCategory.WEAPONS),
    /** Weapons / Cannons, e.g., meson cannon (2). */
    WEAPONS_CANNONS(ResearchMainCategory.WEAPONS),
    /** Weapons / Projectiles, e.g., rockets (3). */
    WEAPONS_PROJECTILES(ResearchMainCategory.WEAPONS),
    /** Weapons / Tanks (4). */
    WEAPONS_TANKS(ResearchMainCategory.WEAPONS),
    /** Weapons / Vehicles, e.g., radar car (5). */
    WEAPONS_VEHICLES(ResearchMainCategory.WEAPONS),

    /** Buildings / Civil (1). */
    BUILDINGS_CIVIL(ResearchMainCategory.BUILDINGS),
    /** Buildings / Military (2). */
    BUILDINGS_MILITARY(ResearchMainCategory.BUILDINGS),
    /** Buildings / Radars (3). */
    BUILDINGS_RADARS(ResearchMainCategory.BUILDINGS),
    /** Buildings / Guns, e.g., planetary defense cannons (4). */
    BUILDINGS_GUNS(ResearchMainCategory.BUILDINGS),
    ;
    /** The main category reference. */
    public final ResearchMainCategory main;
    /**
     * Constructor to set the main category.
     * @param cat the main category.
     */
    ResearchSubCategory(ResearchMainCategory cat) {
        this.main = cat;
    }
}
