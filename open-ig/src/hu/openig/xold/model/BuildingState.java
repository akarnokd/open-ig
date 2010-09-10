/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.xold.model;

import hu.openig.xold.core.StateExchanger;

/**
 * Contains the state of a building. Used in
 * asynchronous threads to do changes to the state of a buildings safely.
 * (Building status changes should be done in the event thread.)
 * @author karnokd
 */
public class BuildingState implements StateExchanger {
	/** The original building which to this state belongs to. */
	public final GameBuilding building;
	/** The planet on which this building is on. */
	public final GamePlanet planet;
	/** The current health. */
	public int health;
	/** The current progress. */
	public int progress;
	/** The current enabled state. */
	public boolean enabled;
	/** The current repairing state. */
	public boolean repairing;
	/** The current energy amount. */
	public int energy;
	/** The current worker amount. */
	public int workers;
	/** Indicator to remove this building from the planet. */
	public boolean remove;
	/**
	 * Constructor.
	 * @param building the building
	 * @param planet the planet
	 */
	public BuildingState(GameBuilding building, GamePlanet planet) {
		this.building = building;
		this.planet = planet;
		load();
	}
	/**
	 * Load values from the actual building object.
	 */
	private void load() {
		health = building.health;
		progress = building.progress;
		enabled = building.enabled;
		repairing = building.repairing;
		energy = building.energy;
		workers = building.workers;
		remove = false;
	}
	/**
	 * Save changes to the actual building object.
	 */
	public void save() {
		if (remove) {
			planet.removeBuilding(building);
		} else {
			building.health = health;
			building.progress = progress;
			building.enabled = enabled;
			building.repairing = repairing;
			building.energy = energy;
			building.workers = workers;
		}
	}
}
