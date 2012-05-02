/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Func1;
import hu.openig.core.Pred1;
import hu.openig.model.BattleGroundProjector;
import hu.openig.model.BattleGroundShield;
import hu.openig.model.BattleGroundTurret;
import hu.openig.model.BattleGroundVehicle;
import hu.openig.model.BattleInfo;
import hu.openig.model.BattleProjectile;
import hu.openig.model.BattleProjectile.Mode;
import hu.openig.model.AttackDefense;
import hu.openig.model.Building;
import hu.openig.model.Fleet;
import hu.openig.model.GroundwarUnit;
import hu.openig.model.HasInventory;
import hu.openig.model.InventoryItem;
import hu.openig.model.InventorySlot;
import hu.openig.model.Planet;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.SpaceStrengths;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.World;
import hu.openig.utils.U;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

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
	protected final World world;
	/** The battle configuration. */
	protected final BattleInfo battle;
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
	/** The vehicle filter. */
	final Func1<InventoryItem, Boolean> vehicles = new Pred1<InventoryItem>() {
		@Override
		public Boolean invoke(InventoryItem value) {
			return value.type.category == ResearchSubCategory.WEAPONS_TANKS
					|| value.type.category == ResearchSubCategory.WEAPONS_VEHICLES
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
		System.out.printf("Attacker: %s (%s)%n", battle.attacker.name, battle.attacker.owner.id);
		Planet p0 = battle.getPlanet();
		if (p0 != null) {
			System.out.printf("Planet: %s (%s)%n", p0.name, p0.owner.id);
		}
		Fleet f0 = battle.getFleet();
		if (f0 != null) {
			System.out.printf("Fleet: %s (%s)%n", f0.name, f0.owner.id);
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
	}
	/** Run the ground battle. */
	void runGroundBattle() {
		List<GroundwarUnit> attackerUnits = vehicles(battle.attacker.inventory);
		List<GroundwarUnit> defenderUnits = vehicles(battle.targetPlanet.inventory);
		
		AttackDefense attackerTVBattle = vehicleStrength(attackerUnits);
		AttackDefense defenderTVBattle = vehicleStrength(defenderUnits);
		
		battle.attacker.owner.ai.onAutoGroundwarStart(battle, attackerTVBattle, defenderTVBattle);
		battle.targetPlanet.owner.ai.onAutoGroundwarStart(battle, attackerTVBattle, defenderTVBattle);
		
		// tank+vehicle battle
		double attackerTime = defenderTVBattle.defense / attackerTVBattle.attack;
		double defenderTime = attackerTVBattle.defense / defenderTVBattle.attack;
		
		if (defenderTime <= attackerTime) {
			removeVehicles(battle.attacker);
			applyDamage(defenderUnits, defenderTime * attackerTVBattle.attack);
			
			applyPlanetDefended(battle.targetPlanet, PLANET_DEFENSE_LOSS);
		} else {
			removeVehicles(battle.targetPlanet);
			applyDamage(attackerUnits, attackerTime * defenderTVBattle.attack);

			// attack buildings one by one
			for (Building b : new ArrayList<Building>(battle.targetPlanet.surface.buildings)) {
				if (b.type.kind.equals("Defensive")) {
					double attackerRange = bestVehicleRange(attackerUnits);
					double defenderRange = bestBuildingRange(battle.targetPlanet, b);
					
					if (attackerRange > defenderRange) { 
						// attacker wins always, no further losses
						battle.targetPlanet.surface.removeBuilding(b);
					} else {
						
						System.out.printf("*Attacking building: %s%n", b.type.id);
						// take turns
						while (!attackerUnits.isEmpty()) {
							// Determines how many units will attack at once
							final int accessibility = world.random().nextInt(4) + 2;
							
							Collections.shuffle(attackerUnits, world.random());
							
							List<GroundwarUnit> attacking = subList(attackerUnits, 0, accessibility);
							attackerTVBattle = vehicleStrength(attacking);

							// remaining units attack buildings
							AttackDefense defenderBuildings = new AttackDefense();
							buildingStrength(battle.targetPlanet, b, defenderBuildings);

							System.out.printf("-NEXT TURN-%n");
							System.out.printf("Attacker count: %s%n", accessibility);
							System.out.printf("Attacker.Attack = %s%n", attackerTVBattle.attack);
							System.out.printf("Attacker.Defense = %s%n", attackerTVBattle.defense);
							System.out.printf("Building.Attack = %s%n", defenderBuildings.attack);
							System.out.printf("Building.Defense = %s%n", defenderBuildings.defense);
							
							attackerTime = defenderBuildings.defense / attackerTVBattle.attack;
							defenderTime = attackerTVBattle.defense / defenderBuildings.attack;
							
							System.out.printf("Attacker time (ms): %s%n", attackerTime);
							System.out.printf("Defender time (ms): %s%n", defenderTime);
							
							if (attackerTime < defenderTime) {
								battle.targetPlanet.surface.removeBuilding(b);
								System.out.printf("Attacker won, damage taken: %s%n", attackerTime * defenderBuildings.attack);
								applyDamage(attacking, attackerTime * defenderBuildings.attack);
								break; // next building
							} else {
								applyDamage(attacking, defenderTime * defenderBuildings.attack * 2);
								System.out.printf("Defender won, damage taken: %s%n", defenderTime * attackerTVBattle.attack);
								applyGroundDamage(battle.targetPlanet, b, defenderTime * attackerTVBattle.attack);
							}
						}
					}
				}
			}
			cleanupInventory(battle.targetPlanet);
			cleanupInventory(battle.attacker);
			if (attackerUnits.isEmpty()) {
				applyPlanetDefended(battle.targetPlanet, PLANET_DEFENSE_LOSS);
			} else {
				battle.targetPlanet.takeover(battle.attacker.owner);
				applyPlanetConquered(battle.targetPlanet, PLANET_CONQUER_LOSS);
			}
			battle.groundwarWinner = battle.targetPlanet.owner;
			battle.targetPlanet.rebuildRoads();
		}
	}
	/**
	 * Creates a sublist from the list or an empty list if start is beyond the size.
	 * @param <T> the element type
	 * @param list the list
	 * @param start the start index inclusive
	 * @param end the end index exclusive
	 * @return the sublist
	 */
	<T> List<T> subList(List<T> list, int start, int end) {
		if (start >= list.size()) {
			return new ArrayList<T>();
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
	void cleanupInventory(HasInventory inv) {
		Iterator<InventoryItem> it = inv.inventory().iterator();
		while (it.hasNext()) {
			InventoryItem ii = it.next();
			if (ii.count <= 0) {
				it.remove();
			}
		}
	}
	/**
	 * Deal certain amount of damage to units.
	 * @param units the list of units
	 * @param hitpoints the hitpoints
	 */
	void applyDamage(List<GroundwarUnit> units, double hitpoints) {
		List<GroundwarUnit> us = new ArrayList<GroundwarUnit>(units);
		Collections.shuffle(us, world.random());
		for (GroundwarUnit u : us) {
			if (hitpoints <= 0) {
				break;
			}
			if (u.hp <= hitpoints) {
				hitpoints -= u.hp;
				units.remove(u);
				u.item.count--;
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
		ArrayList<Building> bs = new ArrayList<Building>(p.surface.buildings);
		Collections.shuffle(bs, world.random());
		for (Building b : bs) {
			if (b.type.kind.equals("Defensive")) {
				p.surface.removeBuilding(b);
			}
		}
		p.rebuildRoads();
		
	}
	/**
	 * Remove all vehicles from the fleet.
	 * @param f the fleet
	 */
	void removeVehicles(HasInventory f) {
		List<InventoryItem> inv = f.inventory();
		List<InventoryItem> is = new ArrayList<InventoryItem>(inv);
		for (InventoryItem ii : is) {
			if (ii.type.category == ResearchSubCategory.WEAPONS_TANKS
					|| ii.type.category == ResearchSubCategory.WEAPONS_VEHICLES) {
				inv.remove(ii);
			}
		}
	}
	/**
	 * Compute the best vehicle range.
	 * @param invs the inventory
	 * @return the range
	 */
	double bestVehicleRange(Collection<GroundwarUnit> invs) {
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
		for (Building b : p.surface.buildings) {
			if (b.type.kind.equals("Defensive")) {
				r = Math.max(r, bestBuildingRange(p, b));
			}
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
	List<GroundwarUnit> vehicles(Collection<InventoryItem> items) {
		List<GroundwarUnit> result = U.newArrayList();
		for (InventoryItem ii : items) {
			BattleGroundVehicle bgv = world.battle.groundEntities.get(ii.type.id);
			if (bgv != null) {
				for (int i = 0; i < ii.count; i++) {
					GroundwarUnit u = new GroundwarUnit(bgv.normal);
					u.hp = bgv.hp;
					u.item = ii;
					u.model = bgv;
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
	AttackDefense vehicleStrength(Collection<GroundwarUnit> items) {
		double a = 0;
		double d = 0;
		
		for (GroundwarUnit ii : items) {
			a += ii.model.damage * 1.0 / ii.model.delay;
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
		
		for (Building b : p.surface.buildings) {
			if (b.type.kind.equals("Defensive")) {
				buildingStrength(p, b, result);
			}
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
				result.attack += bt.damage * 1.0 / bt.delay;
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
		
		SpaceStrengths str = getSpaceStrengths(battle);
		
		battle.attacker.owner.ai.onAutoSpacewarStart(battle, str);
		battle.enemy(battle.attacker.owner).ai.onAutoSpacewarStart(battle, str);
		
		AttackDefense attacker = str.attacker;
		AttackDefense defender = str.defender;
		
		System.out.printf("Attacker.Attack = %s%n", attacker.attack);
		System.out.printf("Attacker.Defense = %s%n", attacker.defense);

		System.out.printf("Defender.Attack = %s%n", defender.attack);
		System.out.printf("Defender.Defense = %s%n", defender.defense);

		double attackerTime = defender.defense / attacker.attack;
		double defenderTime = attacker.defense / defender.attack;
		
		System.out.printf("Attacker time (ms): %s%n", attackerTime);
		System.out.printf("Defender time (ms): %s%n", defenderTime);
		
		if (attackerTime < defenderTime) {
			battle.spacewarWinner = battle.attacker.owner;
			System.out.printf("Attacker wins. Inflicted damage upon: %s%n", attackerTime * defender.attack);
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
			System.out.printf("Defender wins. Inflicted damage upon: %s%n", defenderTime * attacker.attack);
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
	}
	/**
	 * Apply damage to the fleet.
	 * @param f the target fleet
	 * @param hitpoints the hitpoints
	 * @param filter the filter for items
	 */
	void applyDamage(HasInventory f, double hitpoints, Func1<InventoryItem, Boolean> filter) {
		List<InventoryItem> inv = f.inventory();
		ArrayList<InventoryItem> is = new ArrayList<InventoryItem>();
		for (InventoryItem ii : inv) {
			if (ii.parent == f) {
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
			if (ii.parent != f) {
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
				} else {
					SpacewarStructure str = new SpacewarStructure();
					str.hpMax = world.getHitpoints(ii.type);
					str.count = ii.count;
					str.hp = ii.hp;
					str.shield = ii.shield;
					str.damage((int)hitpoints);
					ii.count = str.count;
					ii.hp = str.hp;
					ii.shield = str.shield;
					if (ii.count <= 0) {
						inv.remove(ii);
					}
					hitpoints = 0;
				}
			}
		}
		if (f instanceof Fleet) {
			Fleet fleet = (Fleet) f;
			if (inv.size() == 0) {
				world.removeFleet(fleet);
				world.scripting.onLost(fleet);
			} else {
				fleet.adjustVehicleCounts();
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
		for (Building b : p.surface.buildings) {
			float eff = b.getEfficiency();
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
			p.surface.removeBuilding(b);
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
		
		ArrayList<InventoryItem> is = new ArrayList<InventoryItem>(p.inventory);
		Collections.shuffle(is, world.random());
		for (InventoryItem ii : is) {
			if (hitpoints <= 0) {
				break;
			}
			if (ii.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS
					|| ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
				double hp = world.getHitpoints(ii.type);
				hp += ii.shield;
				if (hitpoints >= hp * ii.count) {
					p.inventory.remove(ii);
					hitpoints -= hp * ii.count;
				} else {
					int dc = (int)(hitpoints / hp);
					ii.count -= dc;
					hitpoints = 0;
					if (ii.count <= 0) {
						p.inventory.remove(ii);
					}
				}
			}
		}
		
		
		double shieldValue = shieldValue(p);
		ArrayList<Building> bs = new ArrayList<Building>(p.surface.buildings);
		Collections.shuffle(bs, world.random());
		for (Building b : bs) {
			if (hitpoints <= 0) {
				break;
			}
			if (b.isOperational()) {
				if (b.type.kind.equals("Shield") || b.type.kind.equals("Gun")) {
					double hpMax = world.getHitpoints(b.type, p.owner, true);
					double hp = 1.0 * b.hitpoints * hpMax / b.type.hitpoints;
					
					double shieldedHP = hp + hp * shieldValue / 100;
					if (hitpoints >= shieldedHP) {
						p.surface.removeBuilding(b);
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
		for (Building b : new ArrayList<Building>(p.surface.buildings)) {
			if (b.isOperational()) {
				if (b.type.kind.equals("Gun") || b.type.kind.equals("Shield")) {
					p.surface.removeBuilding(b);
				}
			}
		}
		// remove ships and stations
		for (InventoryItem ii : new ArrayList<InventoryItem>(p.inventory)) {
			if (ii.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS
					|| ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
				p.inventory.remove(ii);
			}
		}
		p.rebuildRoads();
	}
	/**
	 * Compute the planet strength.
	 * @param p the planet
	 * @return the strength
	 */
	static AttackDefense planetStrength(Planet p) {
		double offense = 0;
		double defense = 0;
		for (InventoryItem ii : p.inventory) {
			if (ii.owner == p.owner) {
				if (ii.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS
						|| ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
					defense += p.owner.world.getHitpoints(ii.type) * ii.count;
					defense += ii.shield * ii.count;
					for (InventorySlot is : ii.slots) {
						if (is.type != null) {
							BattleProjectile bp = p.owner.world.battle.projectiles.get(is.type.id);
							if (bp != null) {
								if (bp.mode == Mode.BEAM) {
									offense += ii.count * is.count * bp.damage * 1.0 / bp.delay;
								} else {
									offense += ii.count * bp.damage * 1.0 / bp.delay;
								}
							}
						}
					}
				}
			}
		}
		double shieldValue = shieldValue(p);
		for (Building b : p.surface.buildings) {
			float eff = b.getEfficiency();
			if (Building.isOperational(eff)) {
				if (b.type.kind.equals("Shield")
						|| b.type.kind.equals("Gun")) {
					int hpMax = p.owner.world.getHitpoints(b.type, p.owner, true);
					int hp = (int)(1L * b.hitpoints * hpMax / b.type.hitpoints);
					defense += hp;
					defense += hp * shieldValue / 100;
					
					BattleGroundProjector bge = p.owner.world.battle.groundProjectors.get(b.type.id);
					if (bge != null && bge.projectile != null) {
						BattleProjectile pr = p.owner.world.battle.projectiles.get(bge.projectile);
						offense += bge.damage * 1.0 / pr.delay;
					}
					
				}
			}
		}
		
		AttackDefense d = new AttackDefense();
		d.attack = offense;
		d.defense = defense;
		return d;
	}
	/**
	 * Compute the fleet strength.
	 * @param f the fleet
	 * @return the strength
	 */
	static AttackDefense fleetStrength(Fleet f) {
		double offense = 0;
		double defense = 0;
		for (InventoryItem ii : f.inventory) {
			defense += ii.hp * ii.count;
			defense += ii.shield * ii.count;
			
			for (InventorySlot is : ii.slots) {
				if (is.type != null) {
					BattleProjectile bp = f.owner.world.battle.projectiles.get(is.type.id);
					if (bp != null) {
						if (bp.mode == Mode.BEAM) {
							offense += ii.count * is.count * bp.damage * 1.0 / bp.delay;
						} else {
							offense += ii.count * bp.damage * 1.0 / bp.delay;
						}
					}
				}
			}
		}
		AttackDefense d = new AttackDefense();
		d.attack = offense;
		d.defense = defense;
		return d;
	}
	/**
	 * Check if the inventory holds vehicles for a ground assault.
	 * @return true if ground battle available
	 */
	boolean attackerCanAttackGround() {
		if (battle.targetPlanet != null) {
			for (InventoryItem ii : battle.attacker.inventory) {
				if (ii.type.category == ResearchSubCategory.WEAPONS_TANKS
						|| ii.type.category == ResearchSubCategory.WEAPONS_VEHICLES) {
					return true;
				}
			}
		}
		return false;
	}
	/** Simulate the ground battle. */
	void autoGroundBattle() {
		System.err.println("Automatic ground battle not implemented!");
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
		for (Building b : planet.surface.buildings) {
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
		for (Building b : planet.surface.buildings) {
			if (b.type.kind.equals("Bunker")) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Apply losses and morale to the conquered planet.
	 * @param planet the target planet
	 * @param minLosses the minimum losses
	 */
	public static void applyPlanetConquered(Planet planet, int minLosses) {
		if (planet.morale >= 50) {
			planet.morale = 30;
		} else {
			planet.morale = Math.max(0, planet.morale - 10);
		}
		int populationLoss = minLosses; // Math.max(minLosses, Math.min(minLosses * 2, planet.population * 2 / 5));
		if (hasBunker(planet)) {
			populationLoss /= 2;
		}
		planet.population = Math.max(0, planet.population - populationLoss);
		if (planet.population <= 0) {
			planet.die();
		}
	}
	/**
	 * Apply losses and morale to the defended planet.
	 * @param planet the target planet
	 * @param minLosses the minimum losses
	 */
	public static void applyPlanetDefended(Planet planet, int minLosses) {
		if (planet.morale >= 50) { 
			planet.morale = 40;
		} else {
			planet.morale = Math.max(planet.morale - 10, 0);
		}
		int populationLoss = minLosses; // Math.max(minLosses, Math.min(minLosses * 2, planet.population * 3 / 10));
		if (hasBunker(planet)) {
			populationLoss /= 2;
		}
		planet.population = Math.max(0, planet.population - populationLoss);
		if (planet.population <= 0) {
			planet.die();
		}
	}
}
