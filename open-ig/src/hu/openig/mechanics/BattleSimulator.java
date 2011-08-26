/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.model.BattleGroundProjector;
import hu.openig.model.BattleGroundShield;
import hu.openig.model.BattleInfo;
import hu.openig.model.BattleProjectile;
import hu.openig.model.BattleProjectile.Mode;
import hu.openig.model.Building;
import hu.openig.model.Fleet;
import hu.openig.model.FleetStatistics;
import hu.openig.model.InventoryItem;
import hu.openig.model.InventorySlot;
import hu.openig.model.Message;
import hu.openig.model.Planet;
import hu.openig.model.PlanetStatistics;
import hu.openig.model.Player;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.World;
import hu.openig.utils.JavaUtils;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Map;

/**
 * Simulation algorithms for the space and surface battles.
 * @author akarnokd, 2011.08.25.
 *
 */
public final class BattleSimulator {
	/** The world object. */
	protected final World world;
	/** The battle configuration. */
	protected final BattleInfo battle;
	/**
	 * Constructor.
	 * @param world the world object
	 * @param battle the battle configuration
	 */
	public BattleSimulator(World world, BattleInfo battle) {
		this.world = world;
		this.battle = battle;
	}
	/** The space battle statistics record. */
	static class SpaceBattleStatistics {
		/** Total firepower. */
		public int firepower;
		/** Total hitpoints. */
		public int hp;
		/** The ecm level. */
		public int ecm;
		/** Virus infection possible. */
		boolean virus;
		/** Firepower per anti-ecm levels. */
		public final Map<Integer, Integer> antiEcmFirepower = JavaUtils.newHashMap();
		/**
		 * Calculate the effective firepower of the antiEcmFirepowers in respect to the
		 * other party's ecm level.
		 * @param otherPartyEcm the other party's ecm level
		 * @return the effective firepower
		 */
		public int calculateEcmFirepower(int otherPartyEcm) {
			int result = 0;
			for (Map.Entry<Integer, Integer> e : antiEcmFirepower.entrySet()) {
				if (e.getKey() < otherPartyEcm) {
					result += e.getValue() * 20 / 100;
				} else
				if (e.getKey() == otherPartyEcm) {
					result += e.getValue() * 50 / 100;
				} else {
					result += e.getValue();
				}
			}
			return result;
		}
	}
	/**
	 * Run the given battle automatically.
	 */
	public void autoBattle() {
		// comparison variables
		SpaceBattleStatistics attacker = new SpaceBattleStatistics();
		SpaceBattleStatistics defender = new SpaceBattleStatistics();
		
		Planet nearbyPlanet = battle.targetPlanet;
		if (battle.targetFleet != null) {
			Planet np = findNearbyPlanet(world, battle);
			if (np != null) {
				nearbyPlanet = np;
			}
		}
		Fleet nearbyFleet = battle.targetFleet;
		if (battle.targetPlanet != null) {
			Fleet nf = findNearbyFleet(battle.targetPlanet);
			if (nf != null) {
				nearbyFleet = nf;
			}
		}
		
		setBattleStatistics(battle.attacker.owner, battle.attacker.inventory, attacker);
		
		// the target is a planet
		if (battle.targetPlanet != null) {
			// if there is a support fleet
			if (nearbyFleet != null) {
				setBattleStatistics(nearbyFleet.owner, nearbyFleet.inventory, defender);
			}
			setBattleStatistics(battle.targetPlanet.owner, battle.targetPlanet.inventory, defender);
			setBattleStatistics(battle.targetPlanet, defender);
		} else {
			// else target is a fleet
			
			// if there is a support planet
			if (nearbyPlanet != null) {
				// it supports the attacker
				if (nearbyPlanet.owner == battle.attacker.owner) {
					setBattleStatistics(nearbyPlanet.owner, nearbyPlanet.inventory, attacker);
					setBattleStatistics(nearbyPlanet, attacker);
				} else {
					// it supports the defender
					setBattleStatistics(nearbyPlanet.owner, nearbyPlanet.inventory, defender);
					setBattleStatistics(nearbyPlanet, defender);
				}
			}
		}
		
		// play out space battle
		int afp = attacker.calculateEcmFirepower(defender.ecm);
		int dfp = defender.calculateEcmFirepower(attacker.ecm);
		double attack = (attacker.firepower + afp);
		double defend =  (defender.firepower + dfp); 
		double attackerRatio = attack / defender.hp;
		double defenderRatio = defend / attacker.hp;
		
		boolean doGroundBattle = false;
		
		if (attackerRatio > defenderRatio) {
			// defender looses

			damageFleet(battle.attacker, (int)(100 * defenderRatio / attackerRatio));
			
			// the helper fleet is destroyed
			if (nearbyFleet != null) {
				nearbyFleet.owner.fleets.remove(nearbyFleet);
				// TODO statistics
			}
			// the target fleet is destroyed
			if (battle.targetFleet != null) {
				battle.targetFleet.owner.fleets.remove(battle.targetFleet);
			}
			// the helper planet is damaged
			if (nearbyPlanet != null) {
				if (nearbyPlanet.owner != battle.attacker.owner) {
					damageDefenses(world, nearbyPlanet, 100, 20);
					if (attacker.virus) {
						nearbyPlanet.quarantine = true;
						nearbyPlanet.quarantineTTL = Planet.DEFAULT_QUARANTINE_TTL;
					}
				} else {
					if (defend > 0) {
						if (defender.virus) {
							nearbyPlanet.quarantine = true;
							nearbyPlanet.quarantineTTL = Planet.DEFAULT_QUARANTINE_TTL;
						}
						damageDefenses(world, nearbyPlanet, 
								(int)(100 * defenderRatio * dfp / attackerRatio / defend), 
								(int)(20 * defenderRatio * dfp / attackerRatio / defend));
					} else {
						damageDefenses(world, nearbyPlanet, 
								(int)(100 * defenderRatio / attackerRatio), 
								0);
					}
				}
			}
			if (battle.targetPlanet != null) {
				damageDefenses(world, battle.targetPlanet, 100, 20);
				if (attacker.virus) {
					battle.targetPlanet.quarantine = true;
					battle.targetPlanet.quarantineTTL = Planet.DEFAULT_QUARANTINE_TTL;
				}
				FleetStatistics fs = battle.attacker.getStatistics(world.battle);
				if (fs.vehicleCount > 0) {
					// continue with ground assault
					doGroundBattle = true;
				}
			}
		} else
		if (attackerRatio < defenderRatio) {
			// attacker looses
			
			// destroy attacker's fleet
			battle.attacker.owner.fleets.remove(battle.attacker);
			
			if (battle.targetFleet != null) {
				damageFleet(battle.targetFleet, (int)(100 * attackerRatio / defenderRatio));
				
			}
			if (nearbyFleet != null) {
				damageFleet(nearbyFleet, (int)(100 * attackerRatio / defenderRatio));
			}
			// TODO statistics place
			if (nearbyPlanet != null) {
				if (nearbyPlanet.owner == battle.attacker.owner) {
					if (attack > 0) {
						if (defender.virus) {
							nearbyPlanet.quarantine = true;
							nearbyPlanet.quarantineTTL = Planet.DEFAULT_QUARANTINE_TTL;
						}
						damageDefenses(world, nearbyPlanet, 
								(int)(100 * attackerRatio * afp / defenderRatio / attack), 
								(int)(20 * attackerRatio * afp / defenderRatio / attack));
					} else {
						damageDefenses(world, nearbyPlanet, 
								(int)(100 * attackerRatio / defenderRatio), 
								0);
					}
				} else {
					if (defend > 0) {
						if (attacker.virus) {
							nearbyPlanet.quarantine = true;
							nearbyPlanet.quarantineTTL = Planet.DEFAULT_QUARANTINE_TTL;
						}
						damageDefenses(world, nearbyPlanet, 
								(int)(100 * defenderRatio * dfp / attackerRatio / defend), 
								(int)(20 * defenderRatio * dfp / attackerRatio / defend));
					} else {
						damageDefenses(world, nearbyPlanet, 
								(int)(100 * defenderRatio / attackerRatio), 
								0);
					}
				}
			}
		} else {
			// destroy attacker's fleet
			battle.attacker.owner.fleets.remove(battle.attacker);
			
			// the helper fleet is destroyed
			if (nearbyFleet != null) {
				nearbyFleet.owner.fleets.remove(nearbyFleet);
				// TODO statistics
			}
			// the target fleet is destroyed
			if (battle.targetFleet != null) {
				battle.targetFleet.owner.fleets.remove(battle.targetFleet);
			}
			// destroy planet's defenses
			if (battle.targetPlanet != null) {
				damageDefenses(world, battle.targetPlanet, 100, 20);
				if (attacker.virus) {
					battle.targetPlanet.quarantine = true;
					battle.targetPlanet.quarantineTTL = Planet.DEFAULT_QUARANTINE_TTL;
				}
			}

			// destroy planet's defenses
			if (nearbyPlanet != null) {
				damageDefenses(world, nearbyPlanet, 100, 20);
				if ((nearbyPlanet.owner == battle.attacker.owner && defender.virus) || (nearbyPlanet.owner != battle.attacker.owner && attacker.virus)) {
					nearbyPlanet.quarantine = true;
					nearbyPlanet.quarantineTTL = Planet.DEFAULT_QUARANTINE_TTL;
				}
			}
		}
		
		// -------------------------------------------------------------------------------
		if (doGroundBattle) {
			autoGroundBattle();
		}
	}
	/**
	 * Damage the given fleet by the given percent.
	 * @param fleet the target fleet
	 * @param percent the target percent
	 */
	void damageFleet(Fleet fleet, int percent) {
		int hpBefore = 0;
		int hpAfter = 0;
		for (InventoryItem ii : new ArrayList<InventoryItem>(fleet.inventory)) {
			int d = ii.type.productionCost * percent / 100;
			int hp0 = ii.hp;
			if (ii.shield > 0) {
				if (ii.shield >= d / 2) {
					ii.shield -= d / 2;
					ii.hp = Math.max(0, ii.hp - d / 2);
				} else {
					d -= ii.shield * 2;
					ii.hp = Math.max(0, ii.hp - d);
					ii.shield = 0;
				}
			} else {
				ii.hp = Math.max(0, ii.hp - d);
			}
			if (ii.hp <= 0) {
				fleet.inventory.remove(ii);
			} else {
				hpBefore += hp0;
				hpAfter += ii.hp;
				
				// use up rockets and bombs
				for (InventorySlot is : ii.slots) {
					if (is.type != null && is.type.category == ResearchSubCategory.WEAPONS_PROJECTILES) {
						is.count = is.count * (100 - percent) / 100;
					}
				}

			}
		}
		FleetStatistics fs = fleet.getStatistics(world.battle);
		// remove vehicles proportional to the damage taken
		int max = fs.vehicleMax * hpAfter / hpBefore;
		if (fs.vehicleCount > max) {
			int remaining = fs.vehicleCount - max;
			for (InventoryItem ii : new ArrayList<InventoryItem>(fleet.inventory)) {
				if (ii.type.category == ResearchSubCategory.WEAPONS_TANKS || ii.type.category == ResearchSubCategory.WEAPONS_VEHICLES) {
					if (ii.count >= remaining) {
						ii.count -= remaining;
						break;
					} else {
						remaining -= ii.count;
						fleet.inventory.remove(ii);
					}
				}
			}
		}
	}
	/**
	 * Damage buildings on the planet.
	 * The life loss is computed from the assigned workers to these structures and
	 * if housing is damaged, their population-part as well. If bunker is on the planet
	 * it reduces the life loss by a certain level.
	 * @param world the world
	 * @param planet the target planet
	 * @param defensivePercent the damage percent to deal against defensive structures
	 * @param surroundingPercent the damage percent to deal against nearby structures
	 */
	static void damageDefenses(World world, Planet planet, 
			int defensivePercent, int surroundingPercent) {
		
		// damage stations and fighters
		for (InventoryItem ii : new ArrayList<InventoryItem>(planet.inventory)) {
			if (ii.owner == planet.owner) {
				if (ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
					ii.hp = Math.max(0, ii.hp - ii.type.productionCost * defensivePercent / 100);
					if (ii.hp <= 0) {
						planet.inventory.remove(ii);
					} else {
						// use up rockets and bombs
						for (InventorySlot is : ii.slots) {
							if (is.type != null && is.type.category == ResearchSubCategory.WEAPONS_PROJECTILES) {
								is.count = is.count * (100 - defensivePercent) / 100;
							}
						}
					}
				}
				if (ii.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
					ii.count = Math.max(0, ii.count - ii.count * defensivePercent / 100);
					if (ii.count <= 0) {
						planet.inventory.remove(ii);
					}
				}
			}
		}
		
		boolean replaceRoads = false;
		long lifeLoss = 0;
		final int range = 6;
		PlanetStatistics ps = planet.getStatistics();
		
		for (Building b : new ArrayList<Building>(planet.surface.buildings)) {
			if ((b.type.kind.equals("Gun") || b.type.kind.equals("Shield")) && b.hitpoints > 0) {
				int hp0 = b.hitpoints;
				b.hitpoints = Math.max(0, b.hitpoints - b.type.hitpoints * defensivePercent / 100);
				if (b.hitpoints <= 0) {
					planet.surface.removeBuilding(b);
				}
				lifeLoss += b.assignedWorker * (hp0 - b.hitpoints) / hp0;
				
				// find nearby buildings
				for (Building c : new ArrayList<Building>(planet.surface.buildings)) {
					if (c != b && buildingInRange(b, c, range) && c.hitpoints > 0) {
						
						hp0 = c.hitpoints;
						c.hitpoints = Math.max(0, c.hitpoints - c.type.hitpoints * surroundingPercent / 100);
						if (c.hitpoints <= 0) {
							planet.surface.removeBuilding(c);
						}
						lifeLoss += c.assignedWorker * (hp0 - c.hitpoints) / hp0;
						
						if (c.hasResource("house") && ps.houseAvailable > 0) {
							float houseLifes = c.getResource("house") * planet.population / ps.houseAvailable;
							lifeLoss += houseLifes * surroundingPercent * (hp0 - c.hitpoints) / 100 / hp0;
						}
					}
				}
			}
		}
		for (Building b : planet.surface.buildings) {
			if (b.hasResource("survival")) {
				lifeLoss = (long)(lifeLoss * b.getResource("survival") / 100);
			}
		}
		planet.population = Math.max(0, planet.population - (int)lifeLoss);
		
		if (planet.population <= 0) {
			// population erradicated, planet died
			Message msg = world.newMessage("message.planet_died");
			msg.priority = 80;
			msg.targetPlanet = planet;
			planet.owner.messageQueue.add(msg);
			
			planet.die();
		} else {
			planet.morale /= 2;
		}
		
		if (replaceRoads) {
			planet.surface.placeRoads(planet.race, world.buildingModel);
		}
	}
	/**
	 * Test if the given other building has a cell within the given distance to
	 * the center building.
	 * @param center the center building
	 * @param other the other building to test
	 * @param distance the max distance
	 * @return true if within
	 */
	static boolean buildingInRange(Building center, Building other, int distance) {
		Rectangle cr = new Rectangle(
			center.location.x - distance, 
			center.location.y - center.tileset.normal.height + distance,
			center.tileset.normal.width + 2 * distance,
			center.tileset.normal.height + 2 * distance
		);
		Rectangle or = new Rectangle(
				other.location.x, 
				other.location.y - other.tileset.normal.height,
				other.tileset.normal.width,
				other.tileset.normal.height
		);
		return cr.intersects(or);
	}
	/**
	 * Set the surface defense-related statistics.
	 * @param planet the planet
	 * @param stats the statistics output
	 */
	void setBattleStatistics(Planet planet, SpaceBattleStatistics stats) {
		double shieldValue = 0;
		// shields first
		for (Building b : planet.surface.buildings) {
			float eff = b.getEfficiency();
			if (Building.isOperational(eff) && b.type.kind.equals("Shield")) {
				BattleGroundShield bge = world.battle.groundShields.get(b.type.id);
				shieldValue = Math.max(shieldValue, eff * bge.shields);
			}
		}
		// guns last
		for (Building b : planet.surface.buildings) {
			float eff = b.getEfficiency();
			if (Building.isOperational(eff) 
					&&  (b.type.kind.equals("Gun") || b.type.kind.equals("Shield"))) {
				BattleGroundProjector bge = world.battle.groundProjectors.get(b.type.id);

				stats.hp += b.hitpoints + (b.hitpoints * shieldValue / 100);
				
				BattleProjectile pr = world.battle.projectiles.get(bge.projectile);
				if (pr != null) {
					stats.firepower += pr.damage;
				}
			}
		}
	}
	/**
	 * Calculate inventory battle statistics.
	 * @param owner the owner of items
	 * @param inventory the sequence of inventory
	 * @param stats the statistics output
	 */
	void setBattleStatistics(Player owner, Iterable<? extends InventoryItem> inventory, 
			SpaceBattleStatistics stats) {
		// collect attacker statistics
		for (InventoryItem ii : inventory) {
			if (ii.owner != owner) {
				continue;
			}
			stats.hp += (ii.hp + ii.shield) * ii.count;
			stats.ecm = Math.max(stats.ecm, ii.type.getInt("ecm", 0));
			for (InventorySlot is : ii.slots) {
				if (is.type != null) {
					stats.ecm = Math.max(stats.ecm, is.type.getInt("ecm", 0));
					if (is.type.has("projectile")) {
						BattleProjectile bp = world.battle.projectiles.get(is.type.get("projectile"));
						int firepower = is.count  * bp.damage;
						if (is.type.has("anti-ecm")) {
							int antiEcm = is.type.getInt("anti-ecm");
							Integer fp = stats.antiEcmFirepower.get(antiEcm);
							stats.antiEcmFirepower.put(antiEcm, fp != null ? fp.intValue() + firepower : firepower);
						} else {
							stats.firepower += firepower;
						}
						if (bp.mode == Mode.VIRUS) {
							stats.virus = true;
						}
					}
				}
			}
		}
	}
	/**
	 * Find the nearest fleet to the planet with the same owner.
	 * @param planet the target planet
	 * @return the nearest fleet or null if none
	 */
	public static Fleet findNearbyFleet(Planet planet) {
		Fleet nf = null;
		final int checkRange = 20;
		double minDistance = Double.MAX_VALUE;
		for (Fleet f : planet.owner.fleets.keySet()) {
			if (f.owner == planet.owner) {
				double d1 = World.dist(planet.x, planet.y, f.x, f.y);
				if (d1 < minDistance) {
					minDistance = d1;
					if (minDistance < checkRange) {
						nf = f;
					}
				}
			}
		}
			
		return nf;
	}
	/**
	 * Find a nearby planet in relation to the attacker of target fleets.
	 * @param world the world object
	 * @param battle the battle settings
	 * @return the nearby planet or null if none
	 */
	public static Planet findNearbyPlanet(World world, BattleInfo battle) {
		final int checkRange = 20;
		Planet nearbyPlanet = null;
		double minDistance = Double.MAX_VALUE;
		for (Planet p : world.planets.values()) {
			if (p.owner == battle.attacker.owner || p.owner == battle.targetFleet.owner) {
				double d1 = World.dist(p.x, p.y, battle.attacker.x, battle.attacker.y);
				if (d1 < minDistance) {
					minDistance = d1;
					if (minDistance < checkRange) {
						nearbyPlanet = p;
					}
				}
				double d2 = World.dist(p.x, p.y, battle.targetFleet.x, battle.targetFleet.y);
				if (d2 < minDistance) {
					minDistance = d1;
					if (minDistance < checkRange) {
						nearbyPlanet = p;
					}
				}
			}
		}
		return nearbyPlanet;
	}
	/** Simulate the ground battle. */
	void autoGroundBattle() {
		
	}
}
