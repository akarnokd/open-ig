/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.model.AIControls;
import hu.openig.model.AIWorld;
import hu.openig.model.BuildingType;
import hu.openig.model.Player;
import hu.openig.model.World;

import java.util.ArrayList;
import java.util.List;

/**
 * The base class for planners.
 * @author akarnokd, 2011.12.28.
 */
public abstract class Planner {
	/** The world copy. */
	final AIWorld world;
	/** The original world object. */
	final World w;
	/** The player. */
	final Player p;
	/** The actions to perform. */
	final List<Action0> applyActions;
	/** The controls to affect the world in actions. */
	final AIControls controls;
	/**
	 * Constructor. Initializes the fields.
	 * @param world the world object
	 * @param controls the controls to affect the world in actions
	 */
	public Planner(AIWorld world, AIControls controls) {
		this.world = world;
		this.controls = controls;
		this.p = world.player;
		this.w = p.world;
		this.applyActions = new ArrayList<Action0>();
	}
	/**
	 * Execute the planning and return the action list.
	 * @return the action list
	 */
	public final List<Action0> run() {
		plan();
		return applyActions;
	}
	/** Perform the planning. */
	protected abstract void plan();
	/**
	 * Add the given action to the output.
	 * @param action the action to add
	 */
	final void add(Action0 action) {
		applyActions.add(action);
	}
	/**
	 * Add an empty action.
	 */
	final void addEmpty() {
		applyActions.add(new Action0() {
			@Override
			public void invoke() {
				
			}
		});
	}
	/**
	 * Display the action log.
	 * @param message the message
	 * @param values the message parameters
	 */
	final void log(String message, Object... values) {
		System.out.printf("AI:%s:", p.id);
		System.out.printf(message, values);
		System.out.println();
	}
	/**
	 * Find a type the building kind.
	 * @param kind the kind
	 * @return the building type
	 */
	protected BuildingType findBuildingKind(String kind) {
		for (BuildingType bt : w.buildingModel.buildings.values()) {
			if (bt.kind.equals(kind)) {
				return bt;
			}
		}
		return null;
	}
	/**
	 * Find a type the building id.
	 * @param id the building type id
	 * @return the building type
	 */
	protected BuildingType findBuilding(String id) {
		for (BuildingType bt : w.buildingModel.buildings.values()) {
			if (bt.id.equals(id)) {
				return bt;
			}
		}
		return null;
	}
}
