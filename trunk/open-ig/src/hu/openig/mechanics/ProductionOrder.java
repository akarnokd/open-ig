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
import hu.openig.model.Production;
import hu.openig.model.ResearchMainCategory;
import hu.openig.model.ResearchType;

import java.util.List;

/**
 * The production order management action.
 * @author akarnokd, 2011.12.27.
 */
public class ProductionOrder {
	/** The world. */
	final AIWorld world;
	/** The technology. */
	final ResearchType rt;
	/** The list of actions. */
	final List<Action0> actions;
	/** The controls. */
	final AIControls controls;
	/**
	 * Constructor. Initializes the fields.
	 * @param world the world
	 * @param rt the technology
	 * @param actions the action list
	 * @param controls the controls
	 */
	public ProductionOrder(AIWorld world, ResearchType rt,
			List<Action0> actions, AIControls controls) {
		this.world = world;
		this.rt = rt;
		this.actions = actions;
		this.controls = controls;
	}
	/** Place or replace a production order. */
	public void invoke() {
		Production prod = world.productions.get(rt);
		if (prod != null && prod.count > 0) {
			return;
		} else
		if (prod != null) {
			setProduction();
			return;
		}
		int prodCnt = 0;
		ResearchType cheapest = null;
		for (ResearchType rt : world.productions.keySet()) {
			prod = world.productions.get(rt);
			if (rt.category.main == rt.category.main) {
				prodCnt++;
				if (cheapest == null || prod.count == 0 || cheapest.productionCost > rt.productionCost) {
					cheapest = rt;
				}
			}
		}
		if (prodCnt < 5) {
			setProduction();
			return;
		}
		final ResearchType cp = cheapest;
		actions.add(new Action0() {
			@Override
			public void invoke() {
				controls.actionRemoveProduction(cp);
			}
		});
		return;
	}
	/**
	 * Issue a production action for the given technology.
	 */
	void setProduction() {
		int capacity = world.global.spaceshipActive;
		if (rt.category.main == ResearchMainCategory.EQUIPMENT) {
			capacity = world.global.equipmentActive;
		} else
		if (rt.category.main == ResearchMainCategory.WEAPONS) {
			capacity = world.global.weaponsActive;
		}
		final int count = Math.max(10, 
				capacity / rt.productionCost / world.player.world.params().productionUnit());
		actions.add(new Action0() {
			@Override
			public void invoke() {
				controls.actionStartProduction(rt, count, 50);
			}
		});
	}
}
