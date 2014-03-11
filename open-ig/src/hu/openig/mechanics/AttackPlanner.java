/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.core.Action1;
import hu.openig.core.Difficulty;
import hu.openig.model.AIAttackMode;
import hu.openig.model.AIBuilding;
import hu.openig.model.AIControls;
import hu.openig.model.AIFleet;
import hu.openig.model.AIInventoryItem;
import hu.openig.model.AIPlanet;
import hu.openig.model.AIWorld;
import hu.openig.model.ApproachType;
import hu.openig.model.CallType;
import hu.openig.model.DiplomaticOffer;
import hu.openig.model.DiplomaticRelation;
import hu.openig.model.ExplorationMap;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.FleetTask;
import hu.openig.model.InventoryItem;
import hu.openig.model.ModelUtils;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.PlanetStatistics;
import hu.openig.model.Player;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.SoundTarget;
import hu.openig.model.SoundType;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The starmap exploration and satellite planner.
 * @author akarnokd, 2011.12.27.
 */
public class AttackPlanner extends Planner {
	/**
	 * @author akarnokd, 2012.05.02.
	 *
	 */
	private final class PlanetTargetValueComparator implements
			Comparator<AIPlanet> {
		/**
		 * The center of our empire.
		 */
		private final Double center;

		/**
		 * Constructor.
		 * @param center the center of our empire.
		 */
		private PlanetTargetValueComparator(Double center) {
			this.center = center;
		}

		@Override
		public int compare(AIPlanet o1, AIPlanet o2) {
			double value1 = planetValue(o1);
			double value2 = planetValue(o2);
			
			double distance1 = Math.hypot(o1.planet.x - center.x, o1.planet.y - center.y);
			double distance2 = Math.hypot(o2.planet.x - center.x, o2.planet.y - center.y);
			
			double relation1 = getDiplomaticMultiplier(o1.owner);
			double relation2 = getDiplomaticMultiplier(o2.owner);

			double n1 = relation1 * distance1 * value1;
			double n2 = relation2 * distance2 * value2;
			
			return java.lang.Double.compare(n1, n2);
		}
	}
	/** The war limit. */
	protected static final int WAR_LIMIT = 75;
	/** Sets the new attack date. */
	final Action1<Date> setNewAttack;
	/**
	 * Constructor. Initializes the fields.
	 * @param world the world object
	 * @param controls the controls to affect the world in actions
	 * @param exploration the exploration map
	 * @param setNewAttack set the new attack date
	 */
	public AttackPlanner(AIWorld world, AIControls controls, 
			ExplorationMap exploration, Action1<Date> setNewAttack) {
		super(world, controls);
		this.setNewAttack = setNewAttack;
	}
	@Override
	public void plan() {
		if (world.ownPlanets.isEmpty()) {
			return;
		}
		// don't bother when exploring
//		for (AIFleet f : world.ownFleets) {
//			if (f.task.ordinal() <= FleetTask.EXPLORE.ordinal()) {
//				return;
//			}
//		}
		
		if (world.nextAttack != null && world.nextAttack.compareTo(world.now) < 0) {
			world.nextAttack = null;
			
			List<AIFleet> fleets = findFleetsFor(FleetTask.ATTACK, null);
			filterCandidates(fleets);
			if (!fleets.isEmpty()) {
				Collections.sort(fleets, new Comparator<AIFleet>() {
					@Override
					public int compare(AIFleet o1, AIFleet o2) {
						return java.lang.Double.compare(o1.statistics.firepower, o2.statistics.firepower);
					}
				});
				
				int fcmax = fleets.size() / 2 + 1;
				if (world.difficulty == Difficulty.EASY) {
					fcmax = 1;
				}
				int fc = 0;
				Iterator<AIFleet> fi = fleets.iterator();
				while (fc < fcmax && fi.hasNext()) {
					final AIFleet ownFleet = fi.next();
					
					AIFleet targetFleet = null;
					AIPlanet targetPlanet = null;
					if (world.mayConquer && ownFleet.statistics.groundFirepower > 0) {
						if (ModelUtils.randomBool()) {
							targetFleet = selectTargetFleet();
						}
						if (targetFleet == null) {
							targetPlanet = selectTargetPlanet();
						}
					} else {
						targetFleet = selectTargetFleet();
					}
					if (targetFleet != null) {
						final AIFleet ftargetFleet = targetFleet;
						ownFleet.task = FleetTask.ATTACK;
						add(new Action0() {
							@Override
							public void invoke() {
								if (ownFleet.task != FleetTask.SCRIPT) {
									controls.actionAttackFleet(ownFleet.fleet, ftargetFleet.fleet, false);
								}
							}
						});
						fc++;
					} else 
					if (targetPlanet != null) {
						final AIPlanet ftargetPlanet = targetPlanet;
						ownFleet.task = FleetTask.ATTACK;
						add(new Action0() {
							@Override
							public void invoke() {
								if (ownFleet.task != FleetTask.SCRIPT) {
									controls.actionAttackPlanet(ownFleet.fleet, ftargetPlanet.planet, AIAttackMode.CAPTURE);
								}
							}
						});
						fc++;
					}
				}
			}
			if (world.hasDiplomacyRoom) {
				manageDiplomacy();
			}
		}
		computeNextAttack();
	}
	/**
	 * Consider fleets who are at least on half strength of the full potential.
	 * @param fleets the list of fleets
	 */
	void filterCandidates(List<AIFleet> fleets) {
		for (int i = fleets.size() - 1; i >= 0; i--) {
			AIFleet fl = fleets.get(i);
			if (fl.statistics.fighterCount * 2 >= world.fighterLimit
					&& fl.statistics.cruiserCount * 3 >= world.cruiserLimit
					&& fl.statistics.battleshipCount * 3 >= world.battleshipLimit
					&& fl.statistics.vehicleCount * 2 >= fl.statistics.vehicleMax) {
				continue;
			}
			fleets.remove(i);
		}
	}
	/**
	 * Compute the time when the next attack will happen.
	 */
	void computeNextAttack() {
		if (world.nextAttack == null) {
			if (world.mayConquer) {
				int base = 3;
				if (world.difficulty == Difficulty.NORMAL) {
					base = 5;
				} else
				if (world.difficulty == Difficulty.EASY) {
					base = 7;
				}
				long next = 1L * (ModelUtils.randomInt(3) + base) * (8 + 2 * ModelUtils.randomInt(6)) * 60 * 60 * 1000;
				world.nextAttack = new Date(world.now.getTime() + next);
				setNewAttack.invoke(world.nextAttack);
			}
		}
	}
	/**
	 * Select the weakest fleet based on our knowledge.
	 * @return the weakest fleet or null if no fleet available
	 */
	AIFleet selectTargetFleet() {
		List<AIFleet> candidates = new ArrayList<>();
		for (AIFleet f : world.enemyFleets) {
			if (!(f.fleet.owner.ai instanceof AITrader) && !(f.fleet.owner.ai instanceof AIPirate)) {
				
				DiplomaticRelation dr = world.relations.get(f.fleet.owner);
				
				if (dr != null && dr.full && !dr.strongAlliance) {
					if (dr.value < WAR_LIMIT && hasActiveAlliance(dr.alliancesAgainst)) {
						candidates.add(f);
					}
				}
			}
		}
		if (!candidates.isEmpty()) {
			final Point2D.Double center = world.center();
			return Collections.min(candidates, new Comparator<AIFleet>() {
				@Override
				public int compare(AIFleet o1, AIFleet o2) {
					double v1 = fleetValue(o1);
					double v2 = fleetValue(o2);
					
					double d1 = Math.hypot(o1.x - center.x, o1.y - center.y);
					double d2 = Math.hypot(o2.x - center.x, o2.y - center.y);
					if (v1 < v2) {
						return -1;
					} else
					if (v1 > v2) {
						return 1;
					} else
					if (d1 < d2) {
						return -1;
					} else
					if (d1 > d2) {
						return 1;
					}
					return 0;
				}
			});
		}
		return null;
	}
	/**
	 * Select an enemy planet.
	 * @return the planet or null if none found
	 */
	AIPlanet selectTargetPlanet() {
		List<AIPlanet> candidates = new ArrayList<>();
		for (AIPlanet p : world.enemyPlanets) {
			if (p.owner != null) {
				if (world.explorationInnerLimit != null && world.explorationInnerLimit.contains(p.planet.x, p.planet.y)) {
					continue;
				}
				if (world.explorationOuterLimit != null && !world.explorationOuterLimit.contains(p.planet.x, p.planet.y)) {
					continue;
				}
				DiplomaticRelation dr = world.relations.get(p.owner);
				if (dr != null && dr.full && !dr.strongAlliance) {
					if (dr.value < this.p.warThreshold 
							&& !hasActiveAlliance(dr.alliancesAgainst)) {
						if (planetValue(p) > 0) {
							candidates.add(p);
						}
					}
				}
			}
		}
		if (!candidates.isEmpty()) {
			final Point2D.Double center = world.center();
			
			Collections.sort(candidates, new PlanetTargetValueComparator(center));
			
			double prob = ModelUtils.random();
			if (prob < 0.6 || candidates.size() == 1) {
				return candidates.get(0);
			} else
			if ((prob >= 0.6 && prob < 0.75) || candidates.size() == 2) {
				return candidates.get(1);
			} else {
				int n = 2 + ModelUtils.randomInt(candidates.size() - 2);
				return candidates.get(n);
			}
		}
		return null;
	}
	/**
	 * Returns the diplomatic value multiplier, e.g., 100 - relation,
	 * which is considered when selecting valuable targets.
	 * @param owner the other player
	 * @return the value
	 */
	double getDiplomaticMultiplier(Player owner) {
		DiplomaticRelation dr = world.relations.get(owner);
		if (dr != null) {
			return dr.value;
		}
		return 50;
	}
	/**
	 * Check if the sequence of common enemies has still active elements.
	 * @param against the sequence of other players
	 * @return true if any of those players still has planets
	 */
	boolean hasActiveAlliance(Iterable<? extends String> against) {
		for (String p0 : against) {
			if (!world.planetsOf(p0).isEmpty()) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Estimate fleet value.
	 * @param f the target fleet
	 * @return the value
	 */
	public static double fleetValue(AIFleet f) {
		if (f.knowledge == FleetKnowledge.VISIBLE) {
			return 0;
		}
		double v = 0;
		if (f.knowledge == FleetKnowledge.COMPOSITION) {
			for (AIInventoryItem ii : f.inventory) {
				if (ii.type.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS
						|| ii.type.category == ResearchSubCategory.SPACESHIPS_CRUISERS
						|| ii.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
					v += ii.type.productionCost * ((ii.count + 9 / 10) * 10);
				}
			}
		} else {
			for (AIInventoryItem ii : f.inventory) {
				v += ii.type.productionCost * ii.count;
			}
		}
		return v;
	}
	/**
	 * Computes the planet value.
	 * @param p the planet
	 * @return the value
	 */
	public static double planetValue(AIPlanet p) {
		if (p.knowledge == PlanetKnowledge.OWNER) {
			return 1;
		}
		double v = 0;
		if (p.knowledge.ordinal() >= PlanetKnowledge.STATIONS.ordinal()) {
			for (AIInventoryItem ii : p.inventory) {
				if (ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
					v += ii.type.productionCost * ii.count;
				}
			}
		}
		if (p.knowledge.ordinal() >= PlanetKnowledge.BUILDING.ordinal()) {
			for (AIBuilding b : p.buildings) {
				if (b.type.kind.equals("Defensive") 
						|| b.type.kind.equals("Gun")
						|| b.type.kind.equals("Shield")) {
					v += b.type.cost * 10;
				} else {
					v += b.type.cost;
				}
			}
			v += p.statistics.vehicleMax * 50;
		}
		return v;
	}
	/** 
	 * Audio signal for new message.
	 * @param p the target player 
	 */
	void signalMessage(Player p) {
		if (p == p.world.player) {
			if (ModelUtils.randomBool()) {
				p.world.env.playSound(SoundTarget.COMPUTER, SoundType.NEW_MESSAGE_1, null);
			} else {
				p.world.env.playSound(SoundTarget.COMPUTER, SoundType.NEW_MESSAGE_2, null);
			}
		}
	}
	/**
	 * Make contact with other races and send offers.
	 */
	void manageDiplomacy() {
		for (Map.Entry<Player, DiplomaticRelation> dr0 : world.relations.entrySet()) {
			final Player other = dr0.getKey();
			double rnd = ModelUtils.random();
			DiplomaticRelation dr = dr0.getValue();
			boolean mayContact = dr.lastContact == null 
					|| (world.now.getTime() - dr.lastContact.getTime() >= (dr.wontTalk() ? 7L : 1L) * 24 * 60 * 60 * 1000);
			
			if (dr.full && !world.activeOffer.contains(other) && mayContact) {
				
				final ApproachType at = ModelUtils.random(Arrays.asList(ApproachType.values()));
				
				if (rnd < 0.1 && world.ownPlanets.size() < 2 && dr.value < p.warThreshold) {
					add(new Action0() {
						@Override
						public void invoke() {
							other.offers.put(p.id, new DiplomaticOffer(CallType.SURRENDER, at));
							signalMessage(p);
						}
					});
					break;
				} else
				if (rnd < 0.25 && dr.value >= p.warThreshold + 25) {
					add(new Action0() {
						@Override
						public void invoke() {
							other.offers.put(p.id, new DiplomaticOffer(CallType.ALLIANCE, at));
							signalMessage(p);
						}
					});
					break;
				} else
				if (rnd < 0.5 && dr.value < p.warThreshold && countPlanets(world.enemyPlanets, other) < 3) {
					add(new Action0() {
						@Override
						public void invoke() {
							other.offers.put(p.id, new DiplomaticOffer(CallType.RESIGN, at));
							signalMessage(p);
						}
					});
					break;
				} else
				if (rnd < 0.25 && world.money >= 75000 && dr.value >= p.warThreshold + 5) {
					add(new Action0() {
						@Override
						public void invoke() {
							other.offers.put(p.id, new DiplomaticOffer(CallType.MONEY, at).value(2500L * (ModelUtils.randomInt(20) + 1)));
							signalMessage(p);
						}
					});
					break;
				} else
				if (rnd < 0.5 && dr.value < p.warThreshold - 10) {
					add(new Action0() {
						@Override
						public void invoke() {
							other.offers.put(p.id, new DiplomaticOffer(CallType.WAR, at));
							signalMessage(p);
						}
					});
					break;
				} else
				if (rnd < 0.1 && dr.value >= p.warThreshold - 10 && dr.value < p.warThreshold) {
					add(new Action0() {
						@Override
						public void invoke() {
							other.offers.put(p.id, new DiplomaticOffer(CallType.PEACE, at));
							signalMessage(p);
						}
					});
					break;
				}
			}
		}
	}
	/**
	 * Count the number of planets of the owner.
	 * @param planets the known planet list
	 * @param owner the owner
	 * @return the count
	 */
	int countPlanets(List<AIPlanet> planets, Player owner) {
		int result = 0;
		for (AIPlanet aip : planets) {
			if (aip.owner == owner) {
				result++;
			}
		}
		return result;
	}
	/**
	 * Handle the loss of a planet due to conquest.
	 * @param planet the target planet
	 * @param p the the AI player
	 */
	public static void onPlanetLost(Planet planet, AI p) {
		switch (p.w.difficulty) {
		case NORMAL:
			p.nextAttack = new Date(p.w.time.getTimeInMillis() + 2L * 60 * 60 * 1000);
			break;
		case HARD:
			p.nextAttack = new Date(p.w.time.getTimeInMillis() + 30 * 60 * 1000);
			break;
		default:
		}
	}
	/**
	 * Handle the conquest of a planet.
	 * @param planet the target planet
	 * @param p the the AI player
	 */
	public static void onPlanetConquered(Planet planet, AI p) {
		PlanetStatistics ps = planet.getStatistics();
		int max = ps.vehicleMax - 1;
		List<Planet> ownPlanets = p.p.ownPlanets();
		ownPlanets.remove(planet);
		if (!ownPlanets.isEmpty()) {
			Collections.sort(ownPlanets, Planet.VALUE);
			
			for (Planet p0 : ownPlanets) {
				if (max <= 0) {
					break;
				}
				for (InventoryItem ii : p0.inventory.iterable()) {
					if (ii.owner == p.p && ii.type.category == ResearchSubCategory.WEAPONS_TANKS) {
						p0.undeployItem(ii.id, 1);
						planet.deployItem(ii.type, p.p, 1);
						max--;
						break;
					}
				}
			}
		}
	}
}
