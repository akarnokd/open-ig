/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Difficulty;
import hu.openig.core.Func1;
import hu.openig.core.Pred1;
import hu.openig.model.AttackDefense;
import hu.openig.model.BattleGroundProjector;
import hu.openig.model.BattleGroundShield;
import hu.openig.model.BattleGroundTurret;
import hu.openig.model.BattleGroundVehicle;
import hu.openig.model.BattleInfo;
import hu.openig.model.BattleProjectile;
import hu.openig.model.BattleProjectile.Mode;
import hu.openig.model.Building;
import hu.openig.model.Fleet;
import hu.openig.model.GroundwarUnit;
import hu.openig.model.HasInventory;
import hu.openig.model.InventoryItem;
import hu.openig.model.InventoryItems;
import hu.openig.model.InventorySlot;
import hu.openig.model.ModelUtils;
import hu.openig.model.Planet;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;
import hu.openig.model.SpaceStrengths;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.World;
import hu.openig.utils.U;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Simulation algorithms for automatic space and surface battles.
 * @author akarnokd, 2011.08.25.
 *
 */
public final class BattleSimulator {
	/** The base amount for planet defended. */
	public static final int PLANET_DEFENSE_LOSS = 100;
	/** The base amount for planet conquered. */
	public static final int PLANET_CONQUER_LOSS = 200;
	/** The world object. */
	private final World world;
	/** The battle configuration. */
	private final BattleInfo battle;
	/** The ship filter. */
	final Func1<InventoryItem, Boolean> ships = new Pred1<InventoryItem>() {
		@Override
		public Boolean invoke(InventoryItem value) {
			return value.type.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS
					||  value.type.category == ResearchSubCategory.SPACESHIPS_CRUISERS
					||  value.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS
					;
		}
	};
	/**
	 * Constructor.
	 * @param world the world object
	 * @param battle the battle configuration
	 */
	public BattleSimulator(World world, BattleInfo battle) {
		this.world = world;
		this.battle = battle;
	}
	/**
	 * Run the given battle automatically.
	 */
	public void autoBattle() {
		debug("Attacker: %s (%s)%n", battle.attacker.name(), battle.attacker.owner.id);
		Planet p0 = battle.getPlanet();
		if (p0 != null) {
			debug("Planet: %s (%s)%n", p0.name(), p0.owner.id);
		}
		Fleet f0 = battle.getFleet();
		if (f0 != null) {
			debug("Fleet: %s (%s)%n", f0.name(), f0.owner.id);
		}
		battle.findHelpers();
		
		// notify scripts and AI
		world.scripting.onAutobattleStart(battle);
		
		// execute battle
		if (battle.spaceBattleNeeded()) {
			runSpaceBattle();
		}
		if (attackerCanAttackGround()) {
			if (groundBattleNeeded(battle.targetPlanet)) {
				runGroundBattle();
			} else {
				battle.targetPlanet.takeover(battle.attacker.owner);
				applyPlanetConquered(battle.targetPlanet, PLANET_CONQUER_LOSS);
			}
		}
		world.scripting.onAutobattleFinish(battle);
		battle.battleFinished();
	}
	/**
	 * Prints debug information to the console.
	 * @param format  the message format
	 * @param params the parameters
	 */
	void debug(String format, Object... params) {
//		System.out.printf(format, params);
	}
	/** Run the ground battle. */
	void runGroundBattle() {
		
		battle.incrementGroundBattles();
		
		List<GroundwarUnit> attackerUnits = vehicles(battle.attacker.inventory.iterable());
		List<GroundwarUnit> defenderUnits = vehicles(battle.targetPlanet.inventory.iterable());
		
		AttackDefense attackerTVBattle = vehicleStrength(attackerUnits);
		AttackDefense defenderTVBattle = vehicleStrength(defenderUnits);
		
		battle.attacker.owner.ai.onAutoGroundwarStart(battle, attackerTVBattle, defenderTVBattle);
		battle.targetPlanet.owner.ai.onAutoGroundwarStart(battle, attackerTVBattle, defenderTVBattle);
		
		// tank+vehicle battle
		double attackerTime = defenderTVBattle.defense / attackerTVBattle.attack;
		double defenderTime = attackerTVBattle.defense / defenderTVBattle.attack;
		
		if (defenderTime <= attackerTime) {
			battle.groundwarWinner = battle.targetPlanet.owner;
			removeVehicles(battle.attacker);
			applyDamage(defenderUnits, defenderTime * attackerTVBattle.attack);
			
			applyPlanetDefended(battle.targetPlanet, PLANET_DEFENSE_LOSS);
		} else {
			battle.groundwarWinner = battle.attacker.owner;
			removeVehicles(battle.targetPlanet);
			applyDamage(attackerUnits, attackerTime * defenderTVBattle.attack);

			// attack buildings one by one
			for (Building b : battle.targetPlanet.surface.buildings.list()) {
				if (b.type.kind.equals("Defensive")) {
					double attackerRange = bestVehicleRange(attackerUnits);
					double defenderRange = bestBuildingRange(battle.targetPlanet, b);
					
					if (attackerRange > defenderRange) { 
						// attacker wins always, no further losses
						destroyBuilding(battle.targetPlanet, b);
					} else {
						
						debug("*Attacking building: %s%n", b.type.id);
						// take turns
						while (!attackerUnits.isEmpty()) {
							// Determines how many units will attack at once
							final int accessibility = ModelUtils.randomInt(4) + 2;
							
							ModelUtils.shuffle(attackerUnits);
							
							List<GroundwarUnit> attacking = subList(attackerUnits, 0, accessibility);
							attackerTVBattle = vehicleStrength(attacking);

							// remaining units attack buildings
							AttackDefense defenderBuildings = new AttackDefense();
							buildingStrength(battle.targetPlanet, b, defenderBuildings);

							debug("-NEXT TURN-%n");
							debug("Attacker count: %s%n", accessibility);
							debug("Attacker.Attack = %s%n", attackerTVBattle.attack);
							debug("Attacker.Defense = %s%n", attackerTVBattle.defense);
							debug("Building.Attack = %s%n", defenderBuildings.attack);
							debug("Building.Defense = %s%n", defenderBuildings.defense);
							
							attackerTime = defenderBuildings.defense / attackerTVBattle.attack;
							defenderTime = attackerTVBattle.defense / defenderBuildings.attack;
							
							debug("Attacker time (ms): %s%n", attackerTime);
							debug("Defender time (ms): %s%n", defenderTime);
							
							if (attackerTime < defenderTime) {
								destroyBuilding(battle.targetPlanet, b);
								debug("Attacker won, damage taken: %s%n", attackerTime * defenderBuildings.attack);
								applyDamage(attacking, attackerTime * defenderBuildings.attack);
								break; // next building
							}
							applyDamage(attacking, defenderTime * defenderBuildings.attack * 2);
							debug("Defender won, damage taken: %s%n", defenderTime * attackerTVBattle.attack);
							applyGroundDamage(battle.targetPlanet, b, defenderTime * attackerTVBattle.attack);
						}
					}
				}
			}
			cleanupInventory(battle.targetPlanet.inventory);
			cleanupInventory(battle.attacker.inventory);
			if (attackerUnits.isEmpty()) {
				applyPlanetDefended(battle.targetPlanet, PLANET_DEFENSE_LOSS);
			} else {
				battle.targetPlanet.takeover(battle.attacker.owner);
				applyPlanetConquered(battle.targetPlanet, PLANET_CONQUER_LOSS);
			}
			battle.groundwarWinner = battle.targetPlanet.owner;
			battle.targetPlanet.rebuildRoads();
		}
		battle.incrementGroundWin();
	}
	/**
	 * Creates a sublist from the list or an empty list if start is beyond the size.
	 * @param <T> the element type
	 * @param list the list
	 * @param start the start index inclusive
	 * @param end the end index exclusive
	 * @return the sublist
	 */
	static <T> List<T> subList(List<T> list, int start, int end) {
		if (start >= list.size()) {
			return new ArrayList<>();
		}
		if (end >= list.size()) {
			end = list.size();
		}
		return list.subList(start, end);
	}
	/**
	 * Remove empty inventory items.
	 * @param inv the inventory provied
	 */
	static void cleanupInventory(InventoryItems inv) {
		inv.removeIf(InventoryItem.CLEANUP);
	}
	/**
	 * Deal certain amount of damage to units.
	 * @param units the list of units
	 * @param hitpoints the hitpoints
	 */
	void applyDamage(List<GroundwarUnit> units, double hitpoints) {
		List<GroundwarUnit> us = new ArrayList<>(units);
		ModelUtils.shuffle(us);
		for (GroundwarUnit u : us) {
			if (hitpoints <= 0) {
				break;
			}
			if (u.hp <= hitpoints) {
				hitpoints -= u.hp;
				units.remove(u);
				u.item.count--;
				
				u.owner.statistics.vehiclesLost.value++;
				u.owner.statistics.vehiclesLostCost.value += world.researches.get(u.model.id).productionCost;
				
				battle.enemy(u.owner).statistics.vehiclesDestroyed.value++;
				battle.enemy(u.owner).statistics.vehiclesDestroyedCost.value += world.researches.get(u.model.id).productionCost;
			} else {
				u.hp -= hitpoints;
				hitpoints = 0;
			}
		}
	}
	/**
	 * Remove defender buildings.
	 * @param p the target planet
	 */
	void removeBuildings(Planet p) {
		List<Building> bs = p.surface.buildings.list();
		for (Building b : bs) {
			if (b.type.kind.equals("Defensive")) {
				destroyBuilding(p, b);
			}
		}
		p.rebuildRoads();
		
	}
	/**
	 * Remove all vehicles from the fleet.
	 * @param inv the inventory items
	 */
	void removeVehicles(HasInventory inv) {
		InventoryItems inventory = inv.inventory();
		List<InventoryItem> is = inventory.list();
		for (InventoryItem ii : is) {
			if (ii.type.category == ResearchSubCategory.WEAPONS_TANKS
					|| ii.type.category == ResearchSubCategory.WEAPONS_VEHICLES) {
				inventory.remove(ii);
				
				ii.owner.statistics.vehiclesLost.value++;
				ii.owner.statistics.vehiclesLostCost.value += ii.type.productionCost;
				
				battle.enemy(ii.owner).statistics.vehiclesDestroyed.value++;
				battle.enemy(ii.owner).statistics.vehiclesDestroyedCost.value += ii.type.productionCost;
			}
		}
	}
	/**
	 * Compute the best vehicle range.
	 * @param invs the inventory
	 * @return the range
	 */
	static double bestVehicleRange(Collection<GroundwarUnit> invs) {
		double r = 0;
		for (GroundwarUnit ii : invs) {
			r = Math.max(r, ii.model.maxRange);
		}
		return r;
	}
	/**
	 * Compute the best building range. 
	 * @param p the planet
	 * @return the range
	 */
	double bestBuildingRange(Planet p) {
		double r = 0;
		for (Building b : p.surface.buildings.findByKind("Defensive")) {
			r = Math.max(r, bestBuildingRange(p, b));
		}
		return r;
	}
	/**
	 * Compute the best building range. 
	 * @param p the planet
	 * @param b the building
	 * @return the range
	 */
	double bestBuildingRange(Planet p, Building b) {
		double r = 0;
		List<BattleGroundTurret> turrets = world.battle.getTurrets(b.type.id, p.race);
		for (BattleGroundTurret bt : turrets) {
			r = Math.max(r, bt.maxRange);
		}			
		return r;
	}
	/**
	 * Creates the ground war units from the inventory.
	 * @param items the items
	 * @return the list of units
	 */
	List<GroundwarUnit> vehicles(Iterable<InventoryItem> items) {
		List<GroundwarUnit> result = new ArrayList<>();
		for (InventoryItem ii : items) {
			BattleGroundVehicle bgv = world.battle.groundEntities.get(ii.type.id);
			if (bgv != null) {
				for (int i = 0; i < ii.count; i++) {
					GroundwarUnit u = new GroundwarUnit(bgv.normal);
					u.hp = bgv.hp;
					u.item = ii;
					u.model = bgv;
					u.owner = ii.owner;
					result.add(u);
				}
			}
		}
		return result;
	}
	/**
	 * Calculate vehicle strength.
	 * @param items the vehicle inventory
	 * @return the strength
	 */
	static AttackDefense vehicleStrength(Collection<GroundwarUnit> items) {
		double a = 0;
		double d = 0;
		
		for (GroundwarUnit ii : items) {
			a += ii.damage() * 1.0 / ii.model.delay;
			d += ii.hp;
		}
		
		AttackDefense result = new AttackDefense();
		result.attack = a;
		result.defense = d;
		return result;
	}
	/**
	 * Compute building strength.
	 * @param p the planet
	 * @return the strength
	 */
	AttackDefense buildingStrength(Planet p) {
		AttackDefense result = new AttackDefense();
		
		for (Building b : p.surface.buildings.findByKind("Defensive")) {
			buildingStrength(p, b, result);
		}
		
		return result;
	}
	/**
	 * The building strength calculator.
	 * @param p the planet
	 * @param b the building
	 * @param result the output
	 */
	void buildingStrength(Planet p, Building b, AttackDefense result) {
		List<BattleGroundTurret> turrets = world.battle.getTurrets(b.type.id, p.race);
		int turretCount = turrets.size();
		if (b.hitpoints * 2 < b.type.hitpoints) {
			turretCount /= 2;
		}
		int i = 0;
		for (BattleGroundTurret bt : turrets) {
			if (i < turretCount) {
				result.attack += bt.damage(p.owner) * 1.0 / bt.delay;
			}
		}
		int hpMax = world.getHitpoints(b.type, p.owner, false);
		result.defense += 1.0 * b.hitpoints * hpMax / b.type.hitpoints;
	}
	/**
	 * Compute the space strengths of the participating sides.
	 * @param battle the battle configuration
	 * @return the strength values
	 */
	public static SpaceStrengths getSpaceStrengths(BattleInfo battle) {
		
		SpaceStrengths str = new SpaceStrengths();
		
		str.attacker = fleetStrength(battle.attacker);
		
		str.defender = new AttackDefense();
		str.fleet = battle.getFleet();
		if (str.fleet != null) {
			str.defender.add(fleetStrength(str.fleet));
		}
		str.planet = battle.getPlanet();
		str.planetStrength = new AttackDefense();
		if (str.planet != null) {
			AttackDefense ps = planetStrength(str.planet);
			str.planetStrength.add(ps);
			if (str.planet.owner == battle.attacker.owner) {
				str.attacker.add(ps);
			} else {
				str.defender.add(ps);
			}
		}
		return str;
	}
	/**
	 * Run the space battle.
	 */
	void runSpaceBattle() {
		
		// update statistics
		battle.incrementSpaceBattles();
		
		SpaceStrengths str = getSpaceStrengths(battle);
		
		// mark participating fleets
		Set<Fleet> fleets = U.newSet(battle.otherFleets);
		fleets.add(battle.attacker);
		Fleet f2 = battle.getFleet();
		if (f2 != null) {
			fleets.add(f2);
		}
		
		
		battle.attacker.owner.ai.onAutoSpacewarStart(battle, str);
		battle.enemy(battle.attacker.owner).ai.onAutoSpacewarStart(battle, str);
		
		AttackDefense attacker = str.attacker;
		AttackDefense defender = str.defender;
		
		debug("Attacker.Attack = %s%n", attacker.attack);
		debug("Attacker.Defense = %s%n", attacker.defense);

		debug("Defender.Attack = %s%n", defender.attack);
		debug("Defender.Defense = %s%n", defender.defense);

//		double structureRatio = 1.0 * attacker.structures / defender.structures;
		
		double attackerAvgEcm = attacker.ecmCount > 0 ? 1.0 * attacker.ecmSum / attacker.ecmCount : 0.0;
		double attackerAvgAntiEcm = attacker.antiEcmCount > 0 ? 1.0 * attacker.antiEcmSum / attacker.antiEcmCount : 0.0;
		
		double defenderAvgEcm = defender.ecmCount > 0 ? 1.0 * defender.ecmSum / defender.ecmCount : 0.0;
		double defenderAvgAntiEcm = defender.antiEcmCount > 0 ? 1.0 * defender.antiEcmSum / defender.antiEcmCount : 0.0;

		double defenderHitProb = world.battle.getAntiECMProbabilityAvg(world.difficulty, defenderAvgAntiEcm, attackerAvgEcm);
		double attackerHitProb = world.battle.getAntiECMProbabilityAvg(world.difficulty, attackerAvgAntiEcm, defenderAvgEcm);
		
		attacker.defense = Math.max(1, attacker.defense - defender.onetimeAttack * defenderHitProb);
		defender.defense = Math.max(1, defender.defense - attacker.onetimeAttack * attackerHitProb);
		
		double attackerTime = defender.defense / attacker.attack;
		double defenderTime = attacker.defense / defender.attack;
		
		debug("Attacker time (ms): %s%n", attackerTime);
		debug("Defender time (ms): %s%n", defenderTime);
		
		if (attackerTime < defenderTime) {
			battle.spacewarWinner = battle.attacker.owner;
			debug("Attacker wins. Inflicted damage upon: %s%n", attackerTime * defender.attack);
			// attacker wins
			if (str.fleet != null) {
				world.removeFleet(str.fleet);
				world.scripting.onLost(str.fleet);
				str.fleet.inventory.clear();
			}
			if (str.planet != null) {
				if (str.planet.owner != battle.attacker.owner) {
					demolishDefenses(str.planet);
					applyDamage(battle.attacker, attackerTime * defender.attack, ships);

					applyPlanetDefended(str.planet, PLANET_DEFENSE_LOSS);
				} else {
					double planetPercent = str.planetStrength.defense / attacker.defense;
					
					applyDamage(battle.attacker, attackerTime * defender.attack * (1 - planetPercent), ships);
					double planetDamage = attackerTime * defender.attack * planetPercent;
					applyDamage(str.planet, planetDamage);
					if (planetDamage > 0) {
						applyPlanetDefended(str.planet, PLANET_DEFENSE_LOSS / 2);
					}
				}
			} else {
				applyDamage(battle.attacker, attackerTime * defender.attack, ships);
			}
		} else 
		if (attackerTime > defenderTime) {
			if (battle.targetPlanet != null) {
				battle.spacewarWinner = battle.targetPlanet.owner;
			} else
			if (battle.targetFleet != null) {
				battle.spacewarWinner = battle.targetFleet.owner;
			}
			debug("Defender wins. Inflicted damage upon: %s%n", defenderTime * attacker.attack);
			// defender wins
			world.removeFleet(battle.attacker);
			battle.attacker.inventory.clear();
			world.scripting.onLost(battle.attacker);
			
			if (str.planet != null) {
				if (str.planet.owner != battle.attacker.owner) {
					double planetPercent = str.planetStrength.defense / defender.defense;
					double planetDamage = defenderTime * attacker.attack * planetPercent;
					applyDamage(str.planet, planetDamage);

					if (str.fleet != null) {
						applyDamage(str.fleet, defenderTime * attacker.attack * (1 - planetPercent), ships);
					}
					if (planetDamage > 0) {
						applyPlanetDefended(str.planet, PLANET_DEFENSE_LOSS / 2);
					}
				} else {
					demolishDefenses(str.planet);
					applyPlanetDefended(str.planet, PLANET_DEFENSE_LOSS / 2);
					if (str.fleet != null) {
						applyDamage(str.fleet, defenderTime * attacker.attack, ships);
					}
				}
			} else {
				if (str.fleet != null) {
					applyDamage(str.fleet, defenderTime * attacker.attack, ships);
				}
			}
		} else {
			// draw
			battle.attacker.inventory.clear();
			world.removeFleet(battle.attacker);
			world.scripting.onLost(battle.attacker);

			if (str.fleet != null) {
				world.removeFleet(str.fleet);
				world.scripting.onLost(str.fleet);
			}
			if (str.planet != null) {
				demolishDefenses(str.planet);
				applyPlanetDefended(str.planet, PLANET_DEFENSE_LOSS);
			}
		}
		battle.incrementSpaceWin();
		
		for (Fleet f : fleets) {
			if (f.inventory.isEmpty()) {
				f.owner.statistics.fleetsLost.value++;
			}
		}
	}
	/**
	 * Apply damage to the fleet.
	 * @param f the target fleet
	 * @param hitpoints the hitpoints
	 * @param filter the filter for items
	 */
	void applyDamage(HasInventory f, double hitpoints, Func1<InventoryItem, Boolean> filter) {
		List<InventoryItem> inv = f.inventory().list();
		ArrayList<InventoryItem> is = new ArrayList<>();
		for (InventoryItem ii : inv) {
			if (f.inventory().contains(ii)) {
				is.add(ii);
			}
		}
		Collections.sort(is, new Comparator<InventoryItem>() {
			@Override
			public int compare(InventoryItem o1, InventoryItem o2) {
				return o1.type.productionCost - o2.type.productionCost;
			}
		});
		// put those elements at the back who are not part of the same parent
		for (InventoryItem ii : inv) {
			if (!f.inventory().contains(ii)) {
				is.add(ii);
			}
		}
		for (InventoryItem ii : is) {
			if (hitpoints <= 0) {
				break;
			}
			if (filter.invoke(ii)) {
				double hp0 = ii.hp;
				double hp = hp0 + ii.shield;
				if (hitpoints >= hp * ii.count) {
					inv.remove(ii);
					hitpoints -= hp * ii.count;
					
					ii.owner.statistics.shipsLost.value += ii.count;
					ii.owner.statistics.shipsLostCost.value += 2 * ii.sellValue();
					
					battle.enemy(ii.owner).statistics.shipsDestroyed.value += ii.count;
					battle.enemy(ii.owner).statistics.shipsDestroyedCost.value += 2 * ii.sellValue();
					
				} else {
					SpacewarStructure str = new SpacewarStructure(ii.type);
					str.hpMax = world.getHitpoints(ii.type, ii.owner);
					str.count = ii.count;
					str.hp = ii.hp;
					str.shield = ii.shield;
					str.damage(hitpoints);
					int diff = ii.count - str.count;
					ii.count = str.count;
					ii.hp = (int)str.hp;
					ii.shield = (int)str.shield;
					if (ii.count <= 0) {
						inv.remove(ii);
					}
					hitpoints = 0;

					long cost = ii.unitSellValue();
					
					ii.owner.statistics.shipsLost.value += diff;
					ii.owner.statistics.shipsLostCost.value += diff * cost;

					battle.enemy(ii.owner).statistics.shipsDestroyed.value += diff;
					battle.enemy(ii.owner).statistics.shipsDestroyedCost.value += diff * cost;
				}
			}
		}
		if (f instanceof Fleet) {
			Fleet fleet = (Fleet) f;
			if (inv.size() == 0) {
				world.removeFleet(fleet);
				world.scripting.onLost(fleet);
			} else {
				fleet.loseVehicles(battle.enemy(fleet.owner));
			}
		}
	}
	/**
	 * Compute the shield percentage (0..100).
	 * @param p the planet
	 * @return the shield percentage
	 */
	static double shieldValue(Planet p) {
		double shieldValue = 0;
		// add shields
		for (Building b : p.surface.buildings.iterable()) {
			double eff = b.getEfficiency();
			if (Building.isOperational(eff)) {
				if (b.type.kind.equals("Shield")) {
					BattleGroundShield bge = p.owner.world.battle.groundShields.get(b.type.id);
					shieldValue = Math.max(shieldValue, eff * bge.shields);
				}
			}
		}
		return shieldValue;
	}
	/**
	 * Apply ground to the planet.
	 * @param p the target planet
	 * @param b building
	 * @param hitpoints the hitpoints
	 */
	void applyGroundDamage(Planet p, Building b, double hitpoints) {
		double hpMax = world.getHitpoints(b.type, p.owner, false);
		double hp = 1.0 * b.hitpoints * hpMax / b.type.hitpoints;
		
		if (hitpoints >= hp) {
			destroyBuilding(p, b);
		} else {
			hp -= hitpoints;
			b.hitpoints = (int)(hp * b.type.hitpoints / hpMax);
		}
	}
	/**
	 * Apply damage to the planet.
	 * @param p the target planet
	 * @param hitpoints the hitpoints
	 */
	void applyDamage(Planet p, double hitpoints) {
		
		List<InventoryItem> is = p.inventory.list();
		ModelUtils.shuffle(is);
		for (InventoryItem ii : is) {
			if (hitpoints <= 0) {
				break;
			}
			if (ii.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS
					|| ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
				double hp = world.getHitpoints(ii.type, ii.owner);
				hp += ii.shield;
				if (hitpoints >= hp * ii.count) {
					p.inventory.remove(ii);
					hitpoints -= hp * ii.count;
					
					ii.owner.statistics.shipsLost.value += ii.count;
					ii.owner.statistics.shipsDestroyedCost.value += ii.sellValue() * 2;
					
					battle.enemy(ii.owner).statistics.shipsDestroyed.value += ii.count;
					battle.enemy(ii.owner).statistics.shipsDestroyedCost.value += ii.sellValue() * 2;

				} else {
					int dc = (int)(hitpoints / hp);
					int c0 = ii.count;
					long cs0 = ii.sellValue();
					ii.count = Math.max(0, ii.count - dc);
					hitpoints = 0;
					if (ii.count <= 0) {
						p.inventory.remove(ii);
					}
					int diff = c0 - ii.count;
					
					long sellDelta = cs0 - ii.sellValue();
					
					ii.owner.statistics.shipsLost.value += diff;
					ii.owner.statistics.shipsDestroyedCost.value += 2 * sellDelta;
					
					battle.enemy(ii.owner).statistics.shipsDestroyed.value += diff;
					battle.enemy(ii.owner).statistics.shipsDestroyedCost.value += 2 * sellDelta;

				}
			}
		}
		
		
		double shieldValue = shieldValue(p);
		List<Building> bs = p.surface.buildings.list();
		ModelUtils.shuffle(bs);
		for (Building b : bs) {
			if (hitpoints <= 0) {
				break;
			}
			if (b.isSpacewarOperational()) {
				if (b.type.kind.equals("Shield") || b.type.kind.equals("Gun")) {
					double hpMax = world.getHitpoints(b.type, p.owner, true);
					double hp = 1.0 * b.hitpoints * hpMax / b.type.hitpoints;
					
					double shieldedHP = hp + hp * shieldValue / 100;
					if (hitpoints >= shieldedHP) {
						destroyBuilding(p, b);
						hitpoints -= shieldedHP;
					} else {
						hp = shieldedHP - hitpoints;
						b.hitpoints = (int)(hp * b.type.hitpoints / hpMax);
						hitpoints = 0;
					}
				}
			}
		}
		p.rebuildRoads();
	}
	/**
	 * Demolish space defenses of the planet.
	 * @param p the planet
	 */
	void demolishDefenses(Planet p) {
		// demolish buildings
		for (Building b : p.surface.buildings.list()) {
			if (b.isSpacewarOperational()) {
				if (b.type.kind.equals("Gun") || b.type.kind.equals("Shield")) {
					destroyBuilding(p, b);
				}
			}
		}
		// remove ships and stations
		for (InventoryItem ii : p.inventory.list()) {
			if (ii.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS
					|| ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
				
				ii.owner.statistics.shipsLost.value += ii.count;
				ii.owner.statistics.shipsDestroyedCost.value += ii.sellValue() * 2;
				
				battle.enemy(ii.owner).statistics.shipsDestroyed.value += ii.count;
				battle.enemy(ii.owner).statistics.shipsDestroyedCost.value += ii.sellValue() * 2;

				p.inventory.remove(ii);
			}
		}
		p.rebuildRoads();
	}
	/**
	 * Destroy a building on the planet. 
	 * @param p the planet
	 * @param b the building
	 */
	void destroyBuilding(Planet p, Building b) {
		p.surface.removeBuilding(b);
		
		p.owner.statistics.buildingsDestroyed.value++;
		p.owner.statistics.buildingsDestroyedCost.value += b.type.cost * (1 + b.upgradeLevel);
		
		battle.attacker.owner.statistics.buildingsDestroyed.value++;
		battle.attacker.owner.statistics.buildingsDestroyedCost.value += b.type.cost * (1 + b.upgradeLevel);
	}
	/**
	 * Compute the planet strength.
	 * @param p the planet
	 * @return the strength
	 */
	static AttackDefense planetStrength(Planet p) {
		AttackDefense d = new AttackDefense();
		
		for (InventoryItem ii : p.inventory.iterable()) {
			if (ii.owner == p.owner) {
				if (ii.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS
						|| ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
					d.defense += ii.hp * ii.count;
					d.defense += ii.shield * ii.count;
					d.structures += ii.count;
					for (InventorySlot is : ii.slots.values()) {
						if (is.type != null) {
							BattleProjectile bp = p.owner.world.battle.projectiles.get(is.type.id);
							if (bp != null) {
								double dmg = bp.damage(p.owner);
								if (bp.mode == Mode.BEAM) {
									d.attack += 1.0 * ii.count * is.count * dmg / bp.delay;
								} else 
								if (bp.mode == Mode.ROCKET || bp.mode == Mode.MULTI_ROCKET) {
									d.onetimeAttack += 1.0 * ii.count * is.count * dmg /*  / bp.delay */;
									
								}
							}
							if (is.type.has("anti-ecm")) {
								d.antiEcmCount++;
								d.antiEcmSum += is.type.getInt("anti-ecm");
							}
							if (is.type.has(ResearchType.PARAMETER_ECM)) {
								d.ecmCount++;
								d.ecmSum += is.type.getInt(ResearchType.PARAMETER_ECM);
							}
						}
					}
				}
			}
		}
		double shieldValue = shieldValue(p);
		for (Building b : p.surface.buildings.iterable()) {
			if (b.type.kind.equals("Shield")
					|| b.type.kind.equals("Gun")) {
				if (b.isSpacewarOperational()) {
					int hpMax = p.owner.world.getHitpoints(b.type, p.owner, true);
					int hp = (int)(1L * b.hitpoints * hpMax / b.type.hitpoints);
					d.defense += hp;
					d.defense += hp * shieldValue / 100;
					
					BattleGroundProjector bge = p.owner.world.battle.groundProjectors.get(b.type.id);
					if (bge != null && bge.projectile != null) {
						BattleProjectile pr = p.owner.world.battle.projectiles.get(bge.projectile);
						d.attack += bge.damage(p.owner) * 1.0 / pr.delay;
					}
					d.structures++;
				}
			}
		}
		
		return d;
	}
	/**
	 * Compute the fleet strength.
	 * @param f the fleet
	 * @return the strength
	 */
	static AttackDefense fleetStrength(Fleet f) {
		AttackDefense d = new AttackDefense();
		for (InventoryItem ii : f.inventory.iterable()) {
			d.defense += ii.hp * ii.count;
			d.defense += ii.shield * ii.count;
			d.structures += ii.count;
			for (InventorySlot is : ii.slots.values()) {
				if (is.type != null) {
					BattleProjectile bp = f.owner.world.battle.projectiles.get(is.type.id);
					if (bp != null) {
						double dmg = bp.damage(ii.owner);
						if (bp.mode == Mode.BEAM) {
							d.attack += 1.0 * ii.count * is.count * dmg / bp.delay;
						} else 
						if (bp.mode == Mode.ROCKET || bp.mode == Mode.MULTI_ROCKET
						|| bp.mode == Mode.BOMB || bp.mode == Mode.VIRUS) {
							d.onetimeAttack += 1.0 * ii.count * is.count * dmg /* / bp.delay*/;
						}
					}
					if (is.type.has("anti-ecm")) {
						d.antiEcmCount++;
						d.antiEcmSum += is.type.getInt("anti-ecm");
					}
					if (is.type.has(ResearchType.PARAMETER_ECM)) {
						d.ecmCount++;
						d.ecmSum += is.type.getInt(ResearchType.PARAMETER_ECM);
					}
				}
			}
		}
		return d;
	}
	/**
	 * Check if the inventory holds vehicles for a ground assault.
	 * @return true if ground battle available
	 */
	boolean attackerCanAttackGround() {
		if (battle.targetPlanet != null) {
			for (InventoryItem ii : battle.attacker.inventory.iterable()) {
				if (ii.type.category == ResearchSubCategory.WEAPONS_TANKS
						|| ii.type.category == ResearchSubCategory.WEAPONS_VEHICLES) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * @param planet the target planet 
	 * @return true if there are troops or structures on the surface which need to be destroyed. 
	 */
	public static boolean groundBattleNeeded(Planet planet) {
		int vehicles = planet.inventoryCount(ResearchSubCategory.WEAPONS_TANKS, planet.owner)
				+ planet.inventoryCount(ResearchSubCategory.WEAPONS_VEHICLES, planet.owner);
		if (vehicles > 0) {
			return true;
		}
		for (Building b : planet.surface.buildings.iterable()) {
			if (b.isOperational() && b.type.kind.equals("Defensive")) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Check if the planet has bunker.
	 * @param planet the target planet
	 * @return true if has bunker
	 */
	public static boolean hasBunker(Planet planet) {
        Collection<Building> bs = planet.surface.buildings.findByKind("Bunker");
        if (!bs.isEmpty()) {
            return bs.iterator().next().isOperational();
        }
		return false;
	}
	/**
	 * Calculate new morale.
	 * @param planet the target planet
	 * @param defenderSuccess defenders succeeded?
	 */
	static void calcMoraleChange(Planet planet, boolean defenderSuccess) {
		Difficulty diff = planet.world.difficulty;
		if (planet.owner == planet.world.player) {
			if (defenderSuccess) {
				switch (diff) {
				case EASY:
					if (planet.morale > 25) {
						planet.morale *= 0.95;
					} else {
						planet.morale -= 2.5;
					}
					break;
				case HARD:
					if (planet.morale > 25) {
						planet.morale *= 0.85;
					} else {
						planet.morale -= 5;
					}
					break;
				default:
					if (planet.morale > 25) {
						planet.morale *= 0.9;
					} else {
						planet.morale -= 2.5;
					}
				}
			} else {
				switch (diff) {
				case EASY:
					if (planet.morale > 25) {
						planet.morale *= 0.9;
					} else {
						planet.morale -= 2.5;
					}
					break;
				case HARD:
					if (planet.morale > 25) {
						planet.morale *= 0.8;
					} else {
						planet.morale -= 5;
					}
					break;
				default:
					if (planet.morale > 25) {
						planet.morale *= 0.85;
					} else {
						planet.morale -= 2.5;
					}
				}
			}
		} else {
			if (defenderSuccess) {
				switch (diff) {
				case HARD:
					if (planet.morale > 25) {
						planet.morale *= 0.95;
					} else {
						planet.morale -= 2.5;
					}
					break;
				case EASY:
					if (planet.morale > 25) {
						planet.morale *= 0.85;
					} else {
						planet.morale -= 5;
					}
					break;
				default:
					if (planet.morale > 25) {
						planet.morale *= 0.9;
					} else {
						planet.morale -= 2.5;
					}
				}
			} else {
				switch (diff) {
				case EASY:
					if (planet.morale > 25) {
						planet.morale *= 0.9;
					} else {
						planet.morale -= 2.5;
					}
					break;
				case HARD:
					if (planet.morale > 25) {
						planet.morale *= 0.8;
					} else {
						planet.morale -= 5;
					}
					break;
				default:
					if (planet.morale > 25) {
						planet.morale *= 0.85;
					} else {
						planet.morale -= 2.5;
					}
				}
			}
		}
	}
	/**
	 * Apply losses and morale to the conquered planet.
	 * @param planet the target planet
	 * @param minLosses the minimum losses
	 */
	public static void applyPlanetConquered(Planet planet, int minLosses) {
		calcMoraleChange(planet, false);
		int populationLoss = minLosses; // Math.max(minLosses, Math.min(minLosses * 2, planet.population * 2 / 5));
		if (hasBunker(planet)) {
			populationLoss /= 2;
		}
		planet.population(Math.max(0, planet.population() - populationLoss));
		if (planet.population() <= 0) {
			planet.die();
		}
	}
	/**
	 * Apply losses and morale to the defended planet.
	 * @param planet the target planet
	 * @param minLosses the minimum losses
	 */
	public static void applyPlanetDefended(Planet planet, int minLosses) {
		calcMoraleChange(planet, true);
		int populationLoss = minLosses; // Math.max(minLosses, Math.min(minLosses * 2, planet.population * 3 / 10));
		if (hasBunker(planet)) {
			populationLoss /= 2;
		}
		planet.population(Math.max(0, planet.population() - populationLoss));
		if (planet.population() <= 0) {
			planet.die();
		}
	}
}
