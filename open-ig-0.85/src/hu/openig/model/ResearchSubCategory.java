/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

/** The available subCategories. */
public enum ResearchSubCategory {
	/** Spaceships / Fighters. */
	SPACESHIPS_FIGHTERS(ResearchMainCategory.SPACESHIPS),
	/** Spaceships / Cruisers. */
	SPACESHIPS_CRUISERS(ResearchMainCategory.SPACESHIPS),
	/** Spaceships / Battleships. */
	SPACESHIPS_BATTLESHIPS(ResearchMainCategory.SPACESHIPS),
	/** Spaceships / Satellites. */
	SPACESHIPS_SATELLITES(ResearchMainCategory.SPACESHIPS),
	/** Spaceships / Stations. */
	SPACESHIPS_STATIONS(ResearchMainCategory.SPACESHIPS),
	
	/** Equipment / Hyperdrives. */
	EQUIPMENT_HYPERDRIVES(ResearchMainCategory.EQUIPMENT),
	/** Equipment / Modules. */
	EQUIPMENT_MODULES(ResearchMainCategory.EQUIPMENT),
	/** Equipment / Radars. */
	EQUIPMENT_RADARS(ResearchMainCategory.EQUIPMENT),
	/** Equipment / Shields. */
	EQUIPMENT_SHIELDS(ResearchMainCategory.EQUIPMENT),
	
	/** Weapons / Lasers. */
	WEAPONS_LASERS(ResearchMainCategory.WEAPONS),
	/** Weapons / Cannons, e.g., meson cannon. */
	WEAPONS_CANNONS(ResearchMainCategory.WEAPONS),
	/** Weapons / Projectiles, e.g., rockets. */
	WEAPONS_PROJECTILES(ResearchMainCategory.WEAPONS),
	/** Weapons / Tanks. */
	WEAPONS_TANKS(ResearchMainCategory.WEAPONS),
	/** Weapons / Vehicles, e.g., radar car. */
	WEAPONS_VEHICLES(ResearchMainCategory.WEAPONS),
	
	/** Buildings / Civil. */
	BUILDINGS_CIVIL(ResearchMainCategory.BUILDINS),
	/** Buildings / Military. */
	BUILDINGS_MILITARY(ResearchMainCategory.BUILDINS),
	/** Buildings / Radars. */
	BUILDINGS_RADARS(ResearchMainCategory.BUILDINS),
	/** Buildings / Guns, e.g., planetary defense cannons. */
	BUILDINGS_GUNS(ResearchMainCategory.BUILDINS),
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
