/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.model.AIManager;
import hu.openig.model.Building;
import hu.openig.model.DiplomaticInteraction;
import hu.openig.model.Fleet;
import hu.openig.model.GroundwarWorld;
import hu.openig.model.InventoryItem;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchState;
import hu.openig.model.ResearchType;
import hu.openig.model.ResponseMode;
import hu.openig.model.SpacewarAction;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarWorld;
import hu.openig.utils.XElement;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * An AI which combines two AI managers.
 * @author akarnokd, 2011.12.26.
 */
public class AIMixed implements AIManager {
	/** The first manager. */
	protected final AIManager first;
	/** The second manager. */
	protected final AIManager second;
	/**
	 * Constructor.
	 * @param first the first manager
	 * @param second the second manager
	 */
	public AIMixed(AIManager first, AIManager second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public void init(Player p) {
		first.init(p);
		second.init(p);
	}

	@Override
	public void prepare() {
		first.prepare();
		second.prepare();
	}

	@Override
	public void manage() {
		first.manage();
		second.manage();
	}

	@Override
	public void apply() {
		first.apply();
		second.apply();
	}

	@Override
	public ResponseMode diplomacy(Player other,
			DiplomaticInteraction offer) {
		ResponseMode mode1 = first.diplomacy(other, offer);
		ResponseMode mode2 = second.diplomacy(other, offer);
		return Collections.min(Arrays.asList(mode1, mode2));
	}

	@Override
	public void spaceBattleInit(SpacewarWorld world) {
		first.spaceBattleInit(world);
		second.spaceBattleInit(world);
	}

	@Override
	public SpacewarAction spaceBattle(SpacewarWorld world,
			List<SpacewarStructure> idles) {
		SpacewarAction action1 = first.spaceBattle(world, idles);
		SpacewarAction action2 = second.spaceBattle(world, idles);
		return Collections.min(Arrays.asList(action1, action2));
	}

	@Override
	public void spaceBattleDone(SpacewarWorld world) {
		first.spaceBattleDone(world);
		second.spaceBattleDone(world);
	}

	@Override
	public void groundBattleInit(GroundwarWorld battle) {
		first.groundBattleInit(battle);
		second.groundBattleInit(battle);
	}

	@Override
	public void groundBattle(GroundwarWorld battle) {
		first.groundBattle(battle);
		second.groundBattle(battle);
	}

	@Override
	public void groundBattleDone(GroundwarWorld battle) {
		first.groundBattleDone(battle);
		second.groundBattleDone(battle);
	}

	@Override
	public void save(XElement out) {
		first.save(out.add("first"));
		second.save(out.add("second"));
	}

	@Override
	public void load(XElement in) {
		XElement x = in.childElement("first");
		if (x != null) {
			first.load(x);
		}
		x = in.childElement("second");
		if (x != null) {
			second.load(x);
		}
	}

	@Override
	public void onResearchStateChange(ResearchType rt, ResearchState state) {
		first.onResearchStateChange(rt, state);
		second.onResearchStateChange(rt, state);
	}

	@Override
	public void onProductionComplete(ResearchType rt) {
		first.onProductionComplete(rt);
		second.onProductionComplete(rt);
	}

	@Override
	public void onDiscoverPlanet(Planet planet) {
		first.onDiscoverPlanet(planet);
		second.onDiscoverPlanet(planet);
	}

	@Override
	public void onDiscoverFleet(Fleet fleet) {
		first.onDiscoverFleet(fleet);
		second.onDiscoverFleet(fleet);
	}

	@Override
	public void onDiscoverPlayer(Player player) {
		first.onDiscoverPlayer(player);
		second.onDiscoverPlayer(player);
	}

	@Override
	public void onFleetArrivedAtPoint(Fleet fleet, double x, double y) {
		first.onFleetArrivedAtPoint(fleet, x, y);
		second.onFleetArrivedAtPoint(fleet, x, y);
	}

	@Override
	public void onFleetArrivedAtPlanet(Fleet fleet, Planet planet) {
		first.onFleetArrivedAtPlanet(fleet, planet);
		second.onFleetArrivedAtPlanet(fleet, planet);
	}

	@Override
	public void onFleetArrivedAtFleet(Fleet fleet, Fleet other) {
		first.onFleetArrivedAtFleet(fleet, other);
		second.onFleetArrivedAtFleet(fleet, other);
	}

	@Override
	public void onBuildingComplete(Planet planet, Building building) {
		first.onBuildingComplete(planet, building);
		second.onBuildingComplete(planet, building);
	}

	@Override
	public void onLostSight(Fleet fleet) {
		first.onLostSight(fleet);
		second.onLostSight(fleet);
	}

	@Override
	public void onLostTarget(Fleet fleet, Fleet target) {
		first.onLostTarget(fleet, target);
		second.onLostTarget(fleet, target);
	}

	@Override
	public void onNewDay() {
		first.onNewDay();
		second.onNewDay();
	}

	@Override
	public void onSatelliteDestroyed(Planet planet, InventoryItem ii) {
		first.onSatelliteDestroyed(planet, ii);
		second.onSatelliteDestroyed(planet, ii);
	}

	@Override
	public void onPlanetDied(Planet planet) {
		first.onPlanetDied(planet);
		second.onPlanetDied(planet);
	}

	@Override
	public void onPlanetRevolt(Planet planet) {
		first.onPlanetRevolt(planet);
		second.onPlanetRevolt(planet);
	}

	@Override
	public void onPlanetConquered(Planet planet, Player lastOwner) {
		first.onPlanetConquered(planet, lastOwner);
		second.onPlanetConquered(planet, lastOwner);
	}

	@Override
	public void onPlanetLost(Planet planet) {
		first.onPlanetLost(planet);
		second.onPlanetLost(planet);
	}

	@Override
	public void onRadar() {
		first.onRadar();
		second.onRadar();
	}

}
