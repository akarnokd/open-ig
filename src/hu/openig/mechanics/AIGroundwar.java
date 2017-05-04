/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.core.Location;
import hu.openig.model.Building;
import hu.openig.model.GroundwarGun;
import hu.openig.model.GroundwarUnit;
import hu.openig.model.GroundwarWorld;
import hu.openig.model.HasLocation;
import hu.openig.model.ModelUtils;
import hu.openig.model.Player;
import hu.openig.model.SurfaceEntity;
import hu.openig.model.SurfaceEntityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author akarnokd, Feb 4, 2012
 *
 */
public class AIGroundwar {
	/** The AI player. */
	protected final Player p;
	/** The war context. */
	protected final GroundwarWorld war;
	/** The list of actions issued by the AI. */
	protected final List<Action0> actions = new ArrayList<>();
	/** Vehicle category. */
	protected final List<GroundwarUnit> tanks = new ArrayList<>();
	/** Vehicle category. */
	protected final List<GroundwarUnit> ranged = new ArrayList<>();
	/** Vehicle category. */
	protected final List<GroundwarUnit> minelayers = new ArrayList<>();
	/** Vehicle category. */
	protected final List<GroundwarUnit> paralizers = new ArrayList<>();
	/** Targets. */
	protected final List<HasLocation> targets = new ArrayList<>();
	/**
	 * Initialize this class.
	 * @param p the player
	 * @param war the war context
	 */
	public AIGroundwar(Player p, GroundwarWorld war) {
		this.p = p;
		this.war = war;
	}
	/**
	 * Make decisions about the actions.
	 */
	public void run() {
		categorizeObjects();
		
		if (!targets.isEmpty()) {
			attackRegular();
		}
		handleMinelayers();
		
	}
	/**
	 * Handle minelayers.
	 */
	void handleMinelayers() {
		// minelayers start placing mines onto the roads
		Set<Location> um = unmined();
		for (GroundwarUnit u : minelayers) {
			um.remove(u.target());
		}
		for (final GroundwarUnit u : minelayers) {
			if (!u.isMoving() && u.phase == 0) {
				Location l = u.location();
				SurfaceEntity e = war.planet().surface.buildingmap.get(l);
				if (e != null && e.type == SurfaceEntityType.ROAD
						&& !war.hasMine(l.x, l.y)) {
					
					actions.add(new Action0() {
						@Override
						public void invoke() {
							war.special(u);
						}
					});
					
					um.remove(u.location());
				} else {
					final Location loc2 = nearest(u, um);
					if (loc2 != null) {
						actions.add(new Action0() {
							@Override
							public void invoke() {
								war.move(u, loc2.x, loc2.y);
							}
						});
					}
				}
			}
		}
	}
	/**
	 * List all units and structures who are currently attacking this unit.
	 * @param u the unit
	 * @return the list of attackers
	 */
	private List<HasLocation> attackersOf(GroundwarUnit u) {
		List<HasLocation> result = new ArrayList<>();
		for (GroundwarUnit gu : war.units()) {
			if (gu.attackUnit == u && (u.inRange(gu) || gu.inRange(u))) {
				result.add(gu);
			}
		}
		for (GroundwarGun g : war.guns()) {
			if (g.attack == u && (u.inRange(g.building) || g.inRange(u))) {
				result.add(g.building);
			}
		}
		return result;
	}
	/**
	 * Handle regular attack units.
	 */
	private void attackRegular() {
		// regular units, attack nearest value
		for (final GroundwarUnit u : tanks) {
			HasLocation n = nearest(u, attackersOf(u));
			if (n == null) {
				if (p.world.env.config().aiGroundAttackMixed && !u.hasValidTarget()) {
					orderByDistance(u, targets);
					if (targets.size() == 1) {
						n = targets.get(0);
					} else
					if (targets.size() == 2) {
						n = ModelUtils.random(targets.subList(0, 2));
					} else
					if (targets.size() > 2) {
						n = ModelUtils.random(targets.subList(0, 3));
					}
				} else {
					n = nearest(u, targets);
				}
			}
			final HasLocation o = n;
			if (!u.hasValidTarget() || targetOf(u) != o) {
				if (o instanceof Building) {
					actions.add(new Action0() {
						@Override
						public void invoke() {
							war.attack(u, (Building)o);
						}
					});
				} else 
				if (o != null) {
					actions.add(new Action0() {
						@Override
						public void invoke() {
							war.attack(u, (GroundwarUnit)o);
						}
					});
				}
			}
		}
		// paralizers attack only if the rest attacks a unit
		for (final GroundwarUnit u : paralizers) {
			final HasLocation o = nearest(u, targets);
			if (!u.hasValidTarget() || targetOf(u) != u.attackUnit) {
				if (o instanceof GroundwarUnit) {
					actions.add(new Action0() {
						@Override
						public void invoke() {
							war.attack(u, (GroundwarUnit)o);
						}
					});
				}
			}
		}
	}
	/**
	 * Returns the current target of the unit.
	 * @param u the unit
	 * @return the target or null if idle
	 */
	private static HasLocation targetOf(GroundwarUnit u) {
		if (u.attackBuilding != null) {
			return u.attackBuilding;
		} else
		if (u.attackUnit != null) {
			return u.attackUnit;
		}
		return null;
	}
	/**
	 * Categorize tanks, buildings and enemy.
	 */
	private void categorizeObjects() {
		if (war.planet().owner != p) {
			for (Building b : war.planet().surface.buildings.iterable()) {
				boolean add = p.world.env.config().aiGroundAttackEverything;
				
				add |= b.type.kind.equals("Defensive");
				add |= b.type.kind.equals("Power") && !b.isSeverlyDamaged();
				add |= b.type.hasResource("repair") && !b.isSeverlyDamaged();
				
				if (add) {
					targets.add(b);
				}
			}
		}
		
		// the various attack categories
		
		for (GroundwarUnit u : war.units()) {
			if (u.owner != p) {
				targets.add(u);
			} else {
				switch (u.model.type) {
				case TANK:
				case KAMIKAZE:
				case SELF_REPAIR_TANK:
				case ROCKET_JAMMER:
					tanks.add(u);
					break;
				case ROCKET_SLED:
				case ARTILLERY:
					tanks.add(u);
					ranged.add(u);
					break;
				case MINELAYER:
					minelayers.add(u);
					break;
				case PARALIZER:
					paralizers.add(u);
					break;
				default:
				}
			}
		}
	}
	/**
	 * Find the nearest target.
	 * @param u the unit
	 * @param targets the possible targets
	 * @return the nearest target or null if no targets at all
	 */
	private static HasLocation nearest(final GroundwarUnit u, List<HasLocation> targets) {
		if (targets.isEmpty()) {
			return null;
		}
		return Collections.min(targets, new Comparator<HasLocation>() { 
			@Override
			public int compare(HasLocation o1, HasLocation o2) {
				double d1 = u.distance(o1);
				double d2 = u.distance(o2);
				return Double.compare(d1, d2);
			}
		});
	}
	/**
	 * Sort the targets by distance from the given unit.
	 * @param u the unit
	 * @param targets the list of targets
	 */
	private static void orderByDistance(final GroundwarUnit u, List<HasLocation> targets) {
		Collections.sort(targets, new Comparator<HasLocation>() { 
			@Override
			public int compare(HasLocation o1, HasLocation o2) {
				double d1 = u.distance(o1);
				double d2 = u.distance(o2);
				return Double.compare(d1, d2);
			}
		});
	}
	/**
	 * Return the nearest location.
	 * @param u the unit
	 * @param locs the locations
	 * @return the location or null if locs is empty
	 */
	private static Location nearest(final GroundwarUnit u, Set<Location> locs) {
		if (locs.isEmpty()) {
			return null;
		}
		return Collections.min(locs, new Comparator<Location>() {
			@Override
			public int compare(Location o1, Location o2) {
				double d1 = u.distance(o1);
				double d2 = u.distance(o2);
				return Double.compare(d1, d2);
			}
		});
	}
	/**
	 * Generates the set of roads where no mine has been laid.
	 * @return the set of unminded roads
	 */
	private Set<Location> unmined() {
		Set<Location> result = new HashSet<>();
		for (Map.Entry<Location, SurfaceEntity> e : war.planet().surface.buildingmap.entrySet()) {
			if (e.getValue().type == SurfaceEntityType.ROAD) {
				Location key = e.getKey();
				if (!war.hasMine(key.x, key.y) && war.isPassable(key.x, key.y)) {
					result.add(key);
				}
			}
		}
		return result;
	}
	/**
	 * Apply the actions.
	 */
	public void apply() {
		if (!actions.isEmpty()) {
			actions.get(0).invoke();
		}
	}
}
