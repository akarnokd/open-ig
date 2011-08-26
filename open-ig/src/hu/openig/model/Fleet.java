/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A fleet.
 * @author akarnokd, 2010.01.07.
 */
public class Fleet implements Named, Owned {
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
	public Planet targetPlanet;
	/** The fleet movement mode. */
	public FleetMode mode;
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
			pii.hp = type.productionCost;
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
			if (fii.type.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS) {
				result.battleshipCount += fii.count;
				checkHyperdrive = true;
			} else
			if (fii.type.category == ResearchSubCategory.SPACESHIPS_CRUISERS) {
				result.cruiserCount += fii.count;
				checkHyperdrive = true;
			} else
			if (fii.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
				result.fighterCount += fii.count;
			} else
			if (fii.type.category == ResearchSubCategory.WEAPONS_TANKS
					|| fii.type.category == ResearchSubCategory.WEAPONS_VEHICLES
			) {
				result.vehicleCount += fii.count;
			}
			
			if (fii.type.has("radar")) {
				radar = Math.max(radar, fii.type.getInt("radar")); 
			}
			if (fii.type.has("vehicles")) {
				result.vehicleMax += fii.type.getInt("vehicles"); 
			}
			if (fii.type.has("damage")) {
				result.firepower += fii.type.getInt("damage");
			}
			int itemspeed = 6;
			for (InventorySlot slot : fii.slots) {
				if (slot.type != null) {
					if (slot.type.has("radar")) {
						radar = Math.max(radar, slot.type.getInt("radar")); 
					}
					if (slot.type.has("vehicles")) {
						result.vehicleMax += slot.type.getInt("vehicles"); 
					}
					if (checkHyperdrive && slot.type.has("speed")) {
						itemspeed = slot.type.getInt("speed");
					}
					if (slot.type.has("projectile")) {
						BattleProjectile bp = battle.projectiles.get(slot.type.get("projectile"));
						if (bp != null) {
							result.firepower += slot.count * bp.damage;
						}
					}
				}
			}
			result.speed = Math.min(itemspeed, result.speed);
		}
		
		if (result.speed == Integer.MAX_VALUE) {
			result.speed = 6;
		}
		
		float dmin = Integer.MAX_VALUE; 
		Planet pmin = null;
		for (Planet p : owner.planets.keySet()) {
			float d = (p.x - x) * (p.x - x) + (p.y - y) * (p.y - y);
			if (d < dmin) {
				dmin = d;
				pmin = p;
			}
		}
		if (dmin < 20 * 20) {
			result.planet = pmin;
		}
		if (!inventory.isEmpty() && radar == 0) {
			radar = 12;
		} else {
			radar *= 25;
		}
		
		return result;
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
				ii.hp = type.productionCost;
				
				for (EquipmentSlot es : type.slots.values()) {
					InventorySlot is = new InventorySlot();
					is.slot = es;
					if (es.fixed) {
						is.type = es.items.get(0);
						is.count = es.max;
						is.hp = is.type.productionCost;
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
								is.hp = rt1.productionCost;
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
}
