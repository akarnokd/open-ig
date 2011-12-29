/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.core.Func1;
import hu.openig.model.AIBuilding;
import hu.openig.model.AIControls;
import hu.openig.model.AIPlanet;
import hu.openig.model.AIWorld;
import hu.openig.model.Building;
import hu.openig.model.BuildingType;
import hu.openig.model.Player;
import hu.openig.model.Production;
import hu.openig.model.ResearchMainCategory;
import hu.openig.model.ResearchType;
import hu.openig.model.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The base class for planners.
 * @author akarnokd, 2011.12.28.
 */
public abstract class Planner {
	/**
	 * Interface for selecting a building or building type.
	 * @author akarnokd, 2011.12.28.
	 */
	public interface BuildingSelector {
		/**
		 * Accept the given building for upgrading.
		 * @param planet the planet in question
		 * @param building the building
		 * @return true if accept
		 */
		boolean accept(AIPlanet planet, AIBuilding building);
		/**
		 * Accept the given building type for construction.
		 * @param planet the planet in question
		 * @param buildingType the building type
		 * @return true if accept
		 */
		boolean accept(AIPlanet planet, BuildingType buildingType);
	}
	/**
	 * Interface for comparing values of buildings. 
	 * @author akarnokd, 2011.12.28.
	 */
	public interface BuildingOrder {
		/**
		 * Compare the values of two concrete buildings.
		 * @param b1 the first building
		 * @param b2 the second building
		 * @return -1, 0 or 1
		 */
		int compare(AIBuilding b1, AIBuilding b2);
		/**
		 * Compare the values of two building types.
		 * @param b1 the first building type
		 * @param b2 the second building type
		 * @return -1, 0 or 1
		 */
		int compare(BuildingType b1, BuildingType b2);
	}
	/** Default incremental cost-order. */
	final BuildingOrder costOrder = new BuildingOrder() {
		@Override
		public int compare(AIBuilding o1, AIBuilding o2) {
			return o1.type.cost < o2.type.cost ? -1 : (o1.type.cost > o2.type.cost ? 1 : 0);
		}
		@Override
		public int compare(BuildingType o1, BuildingType o2) {
			return o1.cost < o2.cost ? -1 : (o1.cost > o2.cost ? 1 : 0);
		}
	};
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
	/**
	 * Plan a specific category of building actions.
	 * @param condition the condition to check for the planet
	 * @param planetOrder the order in which the planets should be visited.
	 * @param selector the selector of candidate buildings
	 * @param order the order between alternative buildings
	 * @param upgradeFirst try upgrading first?
	 * @return true if action taken
	 */
	public final boolean planCategory(
			Func1<AIPlanet, Boolean> condition,
			Comparator<AIPlanet> planetOrder,
			BuildingSelector selector, 
			BuildingOrder order, boolean upgradeFirst) {
		// try to upgrade or build a new power plant
		List<AIPlanet> planets = new ArrayList<AIPlanet>(world.ownPlanets);
		Collections.sort(planets, planetOrder);
		for (final AIPlanet planet : planets) {
			if (planet.statistics.constructing) {
				continue;
			}
			if (condition.invoke(planet)) {
				if (manageBuildings(planet, selector, order, upgradeFirst)) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Shuffle the given collection randomly.
	 * @param <T> the element type 
	 * @param collection the collection
	 * @return the shuffled collection
	 */
	<T> List<T> shuffle(Collection<? extends T> collection) {
		List<T> result = new ArrayList<T>(collection);
		Collections.shuffle(result, w.random.get());
		return result;
	}
	/**
	 * Count the number of elements in the collection which are
	 * allowed by the filter and number associated by the counter.
	 * @param <T> the element type
	 * @param src the source sequence
	 * @param where the filter
	 * @param counter the counter
	 * @return the count
	 */
	<T> int count(Iterable<T> src, Func1<? super T, Boolean> where, Func1<? super T, Integer> counter) {
		int result = 0;
		for (T ii : src) {
			if (where.invoke(ii)) {
				result += counter.invoke(ii);
			}
		}
		return result;
	}
	/**
	 * Build or upgrade a power plant on the planet.
	 * @param planet the target planet
	 * @param selector to select the building type
	 * @param order the comparator for order
	 * @param upgradeFirst try upgrading first?
	 * @return true if action taken 
	 */
	public final boolean manageBuildings(final AIPlanet planet, 
			final BuildingSelector selector,
			final BuildingOrder order, boolean upgradeFirst) {
		if (upgradeFirst) {
			if (manageUpgrade(planet, selector, order)) {
				return true;
			}
			if (manageConstruction(planet, selector, order)) {
				return true;
			}
		} else {
			if (manageConstruction(planet, selector, order)) {
				return true;
			}
			if (manageUpgrade(planet, selector, order)) {
				return true;
			}
		}
		if (manageRepair(planet, selector, order)) {
			return true;
		}
		return false;
	}
	/**
	 * Try to choose an upgrade option.
	 * @param planet the target planet
	 * @param selector the building selector
	 * @param order the building order
	 * @return true if action taken
	 */
	public final boolean manageUpgrade(final AIPlanet planet, 
			final BuildingSelector selector,
			final BuildingOrder order) {
		for (final AIBuilding b : planet.buildings) {
			if (!b.enabled && selector.accept(planet, b)) {
				add(new Action0() {
					@Override
					public void invoke() {
						controls.actionEnableBuilding(planet.planet, b.building, true);
					}
				});
				return true;
			}
		}
		// scan for the most affordable upgrade
		AIBuilding upgrade = null;
		for (final AIBuilding b : planet.buildings) {
			if (selector.accept(planet, b) && b.canUpgrade() && !b.isDamaged() && b.type.cost <= world.money) {
				if (upgrade == null || order.compare(upgrade, b) < 0) {
					upgrade = b;
				}
			}
		}
		if (upgrade != null) {
			final Building fupgrade = upgrade.building;
			add(new Action0() {
				@Override
				public void invoke() {
					controls.actionUpgradeBuilding(planet.planet, fupgrade, fupgrade.upgradeLevel + 1);
					
				}
			});
			return true;
		}
		return false;
	}
	/**
	 * Try to choose a construction option.
	 * @param planet the target planet
	 * @param selector the building selector
	 * @param order the building order
	 * @return true if action taken
	 */
	public final boolean manageConstruction(final AIPlanet planet, 
			final BuildingSelector selector,
			final BuildingOrder order) {
		// try building a new one
		BuildingType create = null;
		for (final BuildingType bt : w.buildingModel.buildings.values()) {
			if (selector.accept(planet, bt) && planet.planet.canBuild(bt) && bt.cost <= world.money) {
				if (planet.findLocation(bt) != null) {
					if (create == null || order.compare(create, bt) < 0) {
						create = bt;
					}
				}
			}
		}
		if (create != null) {
			final BuildingType fcreate = create;
			add(new Action0() {
				@Override
				public void invoke() {
					controls.actionPlaceBuilding(planet.planet, fcreate);
				}
			});
			return true;
		}
		return false;
	}
	/**
	 * Try to choose a repair option.
	 * @param planet the target planet
	 * @param selector the building selector
	 * @param order the building order
	 * @return true if action taken
	 */
	boolean manageRepair(final AIPlanet planet, 
			final BuildingSelector selector,
			final BuildingOrder order) {
		// try repairing existing
		for (final AIBuilding b : planet.buildings) {
			if (!b.repairing && b.isDamaged() && selector.accept(planet, b)) {
				add(new Action0() {
					@Override
					public void invoke() {
						controls.actionRepairBuilding(planet.planet, b.building, true);
					}
				});
				return true;
			}
		}
		return false;
	}
	/**
	 * Place a production order of the given count,
	 * if not enough slots available, the cheapest and/or finished production will be replaced.
	 * @param rt the research type
	 * @param count the minimum production amount when starting a new production
	 */
	public void placeProductionOrder(final ResearchType rt, final int count) {
		Production prod = world.productions.get(rt);
		if (prod != null && prod.count > 0) {
			return;
		} else
		if (prod != null) {
			issueProductionOrder(rt, count);
			return;
		}
		int prodCnt = 0;
		ResearchType cheapest = null;
		for (ResearchType rt0 : world.productions.keySet()) {
			prod = world.productions.get(rt0);
			if (rt.category.main == rt0.category.main) {
				prodCnt++;
				if (cheapest == null || prod.count == 0 || cheapest.productionCost > rt0.productionCost) {
					cheapest = rt0;
				}
			}
		}
		if (prodCnt < 5) {
			issueProductionOrder(rt, count);
			return;
		}
		final ResearchType cp = cheapest;
		add(new Action0() {
			@Override
			public void invoke() {
				controls.actionRemoveProduction(cp);
			}
		});
		return;
	}
	/**
	 * Issue a production action for the given technology.
	 * @param rt the technology to produce
	 * @param count the minimum amount to produce
	 */
	void issueProductionOrder(final ResearchType rt, final int count) {
		int capacity = world.global.spaceshipActive;
		if (rt.category.main == ResearchMainCategory.EQUIPMENT) {
			capacity = world.global.equipmentActive;
		} else
		if (rt.category.main == ResearchMainCategory.WEAPONS) {
			capacity = world.global.weaponsActive;
		}
		final int count0 = Math.max(count, 
				capacity / rt.productionCost / world.player.world.params().productionUnit());
		add(new Action0() {
			@Override
			public void invoke() {
				controls.actionStartProduction(rt, count0, 50);
			}
		});
	}
	/**
	 * Counts the buildings on the planet and returns false if already at max.
	 * @param planet the target planet
	 * @param bt the building type
	 * @param max the maximum value
	 * @return true if limit reached
	 */
	public boolean limit(AIPlanet planet, BuildingType bt, int max) {
		 int count = 0;
		 for (AIBuilding b : planet.buildings) {
			 if (b.type == bt) {
				 count++;
			 }
		 }
		 return count < max;
	}
	/**
	 * Returns the given label.
	 * @param value the label
	 * @return the translation
	 */
	public String label(String value) {
		return w.env.labels().get(value);
	}
}
