/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.model.BattleProjectile.Mode;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A fleet.
 * @author akarnokd, 2010.01.07.
 */
public class Fleet implements Named, Owned, Iterable<InventoryItem> {
	/** The unique fleet identifier. */
	public int id;
	/** The owner of the fleet. */
	public Player owner;
	/** The X coordinate. */
	public float x;
	/** The Y coordinate. */
	public float y;
//	/** The associated ship icon. */
//	public BufferedImage shipIcon;
	/** The radar radius. */
	public int radar;
	/** The fleet name. */
	public String name;
	/** The fleet inventory: ships and tanks. */
	public final List<InventoryItem> inventory = new ArrayList<InventoryItem>();
	/** The current list of movement waypoints. */
	public final List<Point2D.Float> waypoints = new ArrayList<Point2D.Float>();
	/** If the fleet should follow the other fleet. */
	public Fleet targetFleet;
	/** If the fleet should move to the planet. */
	private Planet targetPlanet;
	/** If the fleet was moved to a planet. */
	public Planet arrivedAt;
	/** The fleet movement mode. */
	public FleetMode mode;
	/**
	 * Set the new target planet and save the current target into {@code arrivedAt}.
	 * @param p the new target planet
	 */
	public void targetPlanet(Planet p) {
		if (p == null) {
			arrivedAt = targetPlanet;
		} else {
			arrivedAt = null;
		}
		targetPlanet = p;
	}
	/**
	 * Returns the current target planet.
	 * @return the current target planet
	 */
	public Planet targetPlanet() {
		return targetPlanet;
	}
	@Override
	public String name() {
		return name;
	}
	@Override
	public Player owner() {
		// TODO Auto-generated method stub
		return owner;
	}
	/**
	 * Returns the number of items of the give research type.
	 * @param rt the research type to count
	 * @return the count
	 */
	public int inventoryCount(ResearchType rt) {
		int count = 0;
		for (InventoryItem pii : inventory) {
			if (pii.type == rt) {
				count += pii.count;
			}
		}
		return count;
	}
	/** 
	 * Change the inventory amount of a given technology. 
	 * @param type the item type
	 * @param amount the amount delta
	 */
	public void changeInventory(ResearchType type, int amount) {
		int idx = 0;
		boolean found = false;
		for (InventoryItem pii : inventory) {
			if (pii.type == type) {
				pii.count += amount;
				if (pii.count <= 0) {
					inventory.remove(idx);
				}
				found = true;
				break;
			}
			idx++;
		}
		if (!found && amount > 0) {
			InventoryItem pii = new InventoryItem();
			pii.type = type;
			pii.owner = owner;
			pii.count = amount;
			pii.hp = owner.world.getHitpoints(type);
			pii.createSlots();
			pii.shield = Math.max(0, pii.shieldMax());
			
			inventory.add(pii);
		}
	}
	/**
	 * Returns the number of items of the give category of the given owner.
	 * @param cat the research sub-category
	 * @return the count
	 */
	public int inventoryCount(ResearchSubCategory cat) {
		int count = 0;
		for (InventoryItem pii : inventory) {
			if (pii.type.category == cat) {
				count += pii.count;
			}
		}
		return count;
	}
	/**
	 * @param battle the battle configuration 
	 * @return calculate the fleet statistics. 
	 */
	public FleetStatistics getStatistics(Battle battle) {
		FleetStatistics result = new FleetStatistics();

		result.speed = Integer.MAX_VALUE;
		radar = 0;
		for (InventoryItem fii : inventory) {
			boolean checkHyperdrive = false;
			boolean checkFirepower = false;
			boolean checkRadar = false;
			if (fii.type.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS) {
				result.battleshipCount += fii.count;
				checkHyperdrive = true;
				checkFirepower = true;
				checkRadar = true;
			} else
			if (fii.type.category == ResearchSubCategory.SPACESHIPS_CRUISERS) {
				result.cruiserCount += fii.count;
				checkHyperdrive = true;
				checkFirepower = true;
				checkRadar = true;
			} else
			if (fii.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
				result.fighterCount += fii.count;
				checkFirepower = true;
			} else
			if (fii.type.category == ResearchSubCategory.WEAPONS_TANKS
					|| fii.type.category == ResearchSubCategory.WEAPONS_VEHICLES
			) {
				result.vehicleCount += fii.count;
			}
			
			if (fii.type.has("vehicles")) {
				result.vehicleMax += fii.type.getInt("vehicles"); 
			}
			if (fii.type.has("damage")) {
				result.firepower += fii.type.getInt("damage");
			}
			for (InventorySlot slot : fii.slots) {
				if (slot.type != null) {
					if (checkRadar && slot.type.has("radar")) {
						radar = Math.max(radar, slot.type.getInt("radar")); 
					}
					if (slot.type.has("vehicles")) {
						result.vehicleMax += slot.type.getInt("vehicles"); 
					}
					if (checkHyperdrive && slot.type.has("speed")) {
						result.speed = Math.min(slot.type.getInt("speed"), result.speed);
					}
					if (checkFirepower && slot.type.has("projectile")) {
						BattleProjectile bp = battle.projectiles.get(slot.type.get("projectile"));
						if (bp != null && bp.mode == Mode.BEAM) {
							result.firepower += slot.count * bp.damage * fii.count;
						}
					}
				}
			}
			;
		}
		
		if (result.speed == Integer.MAX_VALUE) {
			result.speed = 6;
		}
		
		result.planet = nearbyPlanet();
		
		if (!inventory.isEmpty() && radar == 0) {
			radar = 12;
		} else {
			radar *= 25;
		}
		
		return result;
	}
	/**
	 * Calculate the speed value of the fleet.
	 * @return the speed
	 */
	public int getSpeed() {
		int speed = Integer.MAX_VALUE;
		for (InventoryItem fii : inventory) {
			boolean checkHyperdrive = fii.type.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS 
					|| fii.type.category == ResearchSubCategory.SPACESHIPS_CRUISERS;
			for (InventorySlot slot : fii.slots) {
				if (slot.type != null) {
					if (checkHyperdrive && slot.type.has("speed")) {
						speed = Math.min(slot.type.getInt("speed"), speed);
					}
				}
			}
		}		
		if (speed == Integer.MAX_VALUE) {
			speed = 6;
		}
		return speed;
	}
	/**
	 * @return Returns the nearest planet or null if out of range. 
	 */
	public Planet nearbyPlanet() {
		float dmin = Integer.MAX_VALUE; 
		Planet pmin = null;
		for (Planet p : owner.planets.keySet()) {
			float d = (p.x - x) * (p.x - x) + (p.y - y) * (p.y - y);
			if (d < dmin && d < 20 * 20) {
				dmin = d;
				pmin = p;
			}
		}
		if (pmin != null) {
			return pmin;
		}
		return null;
	}
	/** 
	 * Add a given number of inventory item to this fleet.
	 * @param type the technology to add
	 * @param amount the amount to add
	 * @return result the items added
	 */
	public List<InventoryItem> addInventory(ResearchType type, int amount) {
		List<InventoryItem> result = new ArrayList<InventoryItem>();
		if (type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS
			|| type.category == ResearchSubCategory.WEAPONS_TANKS
			|| type.category == ResearchSubCategory.WEAPONS_VEHICLES
		) {
			changeInventory(type, amount);
		} else {
			for (int i = 0; i < amount; i++) {
				InventoryItem ii = new InventoryItem();
				ii.count = 1;
				ii.type = type;
				ii.owner = owner;
				ii.hp = owner.world.getHitpoints(type);
				
				for (EquipmentSlot es : type.slots.values()) {
					InventorySlot is = new InventorySlot();
					is.slot = es;
					if (es.fixed) {
						is.type = es.items.get(0);
						is.count = es.max;
						is.hp = owner.world.getHitpoints(is.type);
					} else {
						List<ResearchType> availList = owner.availableLevel(type);
						
						for (ResearchType rt1 : es.items) {
							if (availList.contains(rt1)) {
								is.type = rt1;
								// always assign a hyperdrive
								if (rt1.category == ResearchSubCategory.EQUIPMENT_HYPERDRIVES) {
									is.count = 1;
								} else {
									is.count = es.max / 2;
								}
								is.hp = owner.world.getHitpoints(rt1);
							}
						}
						if (is.count == 0) {
							is.type = null;
						}
					}
					ii.slots.add(is);
				}
				
				inventory.add(ii);
				result.add(ii);
			}
		}
		return result;
	}
	/** @return the non-fighter and non-vehicular inventory items. */
	public List<InventoryItem> getSingleItems() {
		List<InventoryItem> result = new ArrayList<InventoryItem>();
		for (InventoryItem ii : inventory) {
			if (ii.type.category != ResearchSubCategory.SPACESHIPS_FIGHTERS
					&& ii.type.category != ResearchSubCategory.WEAPONS_TANKS
					&& ii.type.category != ResearchSubCategory.WEAPONS_VEHICLES
				) {
				result.add(ii);
			}
		}
		return result;
	}
	/**
	 * Retrieve the first inventory item with the given type.
	 * @param rt the type
	 * @return the inventory item or null if not present
	 */
	public InventoryItem getInventoryItem(ResearchType rt) {
		for (InventoryItem ii : inventory) {
			if (ii.type == rt) {
				return ii;
			}
		}
		return null;
	}
	/**
	 * Compute how many of the supplied items can be added without violating the limit constraints. 
	 * @param rt the item type
	 * @param battle the battle configuration
	 * @return the currently alloved
	 */
	public int getAddLimit(ResearchType rt, Battle battle) {
		FleetStatistics fs = getStatistics(battle);
		switch (rt.category) {
		case SPACESHIPS_BATTLESHIPS:
			return 3 - fs.battleshipCount;
		case SPACESHIPS_CRUISERS:
			return 25 - fs.cruiserCount;
		case SPACESHIPS_FIGHTERS:
			return 30 - inventoryCount(rt);
		case WEAPONS_TANKS:
		case WEAPONS_VEHICLES:
			return fs.vehicleMax - fs.vehicleCount;
		default:
			return 0;
		}
	}
	/** 
	 * Returns a list of same-owned fleets within the given radius.
	 * @param limit the radius
	 * @return the list of nearby fleets
	 */
	public List<Fleet> fleetsInRange(float limit) {
		List<Fleet> result = new ArrayList<Fleet>();
		for (Fleet f : owner.fleets.keySet()) {
			if (f.owner == owner && f != this) {
				float dist = (x - f.x) * (x - f.x) + (y - f.y) * (y - f.y);
				if (dist <= limit * limit) {
					result.add(f);
				}
			}
		}
		return result;
	}
	@Override
	public Iterator<InventoryItem> iterator() {
		return inventory.iterator();
	}
	/**
	 * If the current vehicle count is greater than the supported vehicle count,
	 * remove excess vehicles.
	 * @return the number of vehicles removed
	 */
	public int adjustVehicleCounts() {
		int vehicleCount = 0;
		int vehicleMax = 0;
		int result = 0;
		for (InventoryItem fii : inventory) {
			if (fii.type.category == ResearchSubCategory.WEAPONS_TANKS
					|| fii.type.category == ResearchSubCategory.WEAPONS_VEHICLES) {
				vehicleCount += fii.count;
			}
			if (fii.type.has("vehicles")) {
				vehicleMax += fii.type.getInt("vehicles"); 
			}
			for (InventorySlot slot : fii.slots) {
				if (slot.type != null) {
					if (slot.type.has("vehicles")) {
						vehicleMax += slot.type.getInt("vehicles"); 
					}
				}
			}
		}
		while (vehicleCount > vehicleMax) {
			for (InventoryItem fii : inventory) {
				if (fii.type.category == ResearchSubCategory.WEAPONS_TANKS
						|| fii.type.category == ResearchSubCategory.WEAPONS_VEHICLES) {
					fii.count--;
					vehicleCount--;
					result++;
					if (fii.count == 0) {
						inventory.remove(fii);
					}
					break;
				}
			}			
		}
		return result;
	}
}
