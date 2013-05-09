/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Difficulty;
import hu.openig.utils.U;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The number of types of vehicles for a particular vehicle limit.
 * @author akarnokd, 2012.01.10.
 */
public class VehiclePlan {
	/** Units of one per kind per composition. */
	public final List<ResearchType> onePerKind = U.newArrayList();
	/** Tank technologies. */
	public final List<ResearchType> tanks = U.newArrayList();
	/** Sled technologies. */
	public final List<ResearchType> sleds = U.newArrayList();
	/** Demands per type. */
	public final Map<ResearchType, Integer> demand = U.newHashMap();
	/** The list of special units. */
	protected final List<ResearchType> special = U.newArrayList();
	/**
	 * Calculate the composition.
	 * @param available the available technology
	 * @param battle the battle model
	 * @param max the maximum amount of units
	 * @param diff the difficulty settings to determine the mixture
	 */
	public void calculate(Iterable<ResearchType> available, 
			BattleModel battle, int max, Difficulty diff) {

		// fill in fighters
		for (ResearchType rt : available) {
			if (rt.category == ResearchSubCategory.WEAPONS_TANKS) {
				tanks.add(rt);
			} else
			if (rt.category == ResearchSubCategory.WEAPONS_VEHICLES) {
				if (rt.has(ResearchType.PARAMETER_ONE_PER_FLEET)) {
					onePerKind.add(rt);
				} else {
					BattleGroundVehicle veh = battle.groundEntities.get(rt.id);
					if (veh != null && veh.type == GroundwarUnitType.ROCKET_SLED) {
						sleds.add(rt);
					} else {
						special.add(rt);
					}
				}
			}
		}
		Collections.sort(tanks, ResearchType.EXPENSIVE_FIRST);
		Collections.sort(sleds, ResearchType.EXPENSIVE_FIRST);
		
		if (!sleds.isEmpty()) {
			ResearchType bestSled = sleds.get(0);
			sleds.clear();
			sleds.add(bestSled);
		}
		sleds.addAll(special);
		
		// expected composition
		int tankCount = special.isEmpty() ? max * 2 / 3 : max / 2;
		int vehicleCount = max - tankCount;
		int sledCount = vehicleCount - onePerKind.size();
		if (sleds.size() == 0) {
			tankCount += sledCount;
			sledCount = 0;
		}
		if (tanks.size() > 0 && tankCount > 0) {
			// depending on the difficulty, use less sophisticated tech as well
			if (diff == Difficulty.HARD || tanks.size() == 1) {
				demand.put(tanks.get(0), tankCount);
			} else
			if (diff == Difficulty.NORMAL) {
				int tc1 = tankCount / 2;
				int tc2 = tankCount - tc1;
				demand.put(tanks.get(0), tc1);
				demand.put(tanks.get(1), tc2);
			} else {
				int tc1 = tankCount / 3;
				int tc2 = tankCount - tc1;
				demand.put(tanks.get(0), tc1);
				demand.put(tanks.get(1), tc2);
			}
			max -= tankCount;
		}
		if (!sleds.isEmpty()) {
			int si = 0;
			while (sledCount > 0 && max > 0) {
				Integer dc = demand.get(sleds.get(si));
				demand.put(sleds.get(si), dc != null ? dc + 1 : 1);
				max--;
				sledCount--;
				si = (si + 1) % sleds.size();
			}
		}
		for (ResearchType rt : onePerKind) {
			if (max <= 0) {
				break;
			}
			demand.put(rt, 1);
			max--;
		}
	}
}
