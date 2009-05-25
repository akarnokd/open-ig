/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Location;
import hu.openig.core.PlanetInfo;
import hu.openig.core.Tile;
import hu.openig.core.TileProvider;
import hu.openig.core.TileStatus;
import hu.openig.core.Tuple2;

import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.Map;

/**
 * Actual building instance.
 * @author karnokd
 */
public class GameBuilding implements TileProvider {
	/** The prototype building. */
	public GameBuildingPrototype prototype;
	/** Shortcut for building images for the actual tech id. */
	public GameBuildingPrototype.BuildingImages images;
	/** The planetary information provider. */
	public PlanetInfo planetInfo;
	/** The tile X coordinate of the left edge. */
	public int x;
	/** The tile Y coordinate of the top edge. */
	public int y;
	/** The current damage level. */
	public int health; // FIXME damage percent or hitpoints?
	/** The current build progress percent: 0 to 100. */
	public int progress;
	/** The lazily initialized rectangle. */
	private Rectangle rect;
	/** Is this building enabled? Disabled buildings don't consume/produce energy or workers. */
	public boolean enabled = true;
	/** The current energy received. */
	public int energy;
	/** The current worker amount. */
	public int workers;
	/** Indicator that this building is being repaired. */
	public boolean repairing;
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tile getTile(Location location) {
		int dx = location.x - x;
		int dy = y - location.y;
		if (progress == 100) {
			if (dx == 0 || dy == images.regularTile.width - 1) {
				if (health < 50) { // FIXME health level to switch to damaged tile
					return images.damagedTile;
				}
				return images.regularTile;
			}
			return null; // no tile to draw
		}
		// FIXME: maybe the returned tile should depend on the internal location, to have non-uniformly built structure visual effect
		return images.buildPhases.get(images.buildPhases.size() * progress / 100); 
	}
	/**
	 * Returns the rectangle containint this building inclusive the roads around.
	 * Note, that height in this case points to +y whereas rendering is done into the -y direction.
	 * @return the rectangle
	 */
	public Rectangle getRectWithRoad() {
		if (rect == null) {
			rect = new Rectangle(x - 1, y + 1, images.regularTile.height + 2, images.regularTile.width + 2);
		}
		return rect;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public TileStatus getStatus() {
		if (health == 0) {
			return TileStatus.DESTROYED;
		} else
		if (health < 100) {
			return TileStatus.DAMAGED;
		} else
		if (getOperationPercent() < 0.5f) {
			return TileStatus.NO_ENERGY;
		}
		return TileStatus.NORMAL;
	}
	/**
	 * Returns the primary production value weighted with the operation percentage.
	 * @return the production type and value tuple
	 */
	public Tuple2<String, Object> getPrimaryProduction() {
		LinkedList<Map.Entry<String, ?>> plist = new LinkedList<Map.Entry<String, ?>>();
		plist.addAll(prototype.values.entrySet());
		plist.addAll(prototype.properties.entrySet());
		for (Map.Entry<String, ?> e : plist) {
			String key = e.getKey();
			if (GameBuildingPrototype.PRODUCT_TYPES.contains(key)) {
				Object value = e.getValue();
				if (value instanceof Integer && GameBuildingPrototype.PRODUCTION_PERCENTABLES.contains(key)) {
					value = (int)(((Integer)value) * getOperationPercent()); 
				}
				return new Tuple2<String, Object>(key, value);
			}
		}
		return null;
	}
	/**
	 * Returns the current received energy
	 * relative to the current energy demand.
	 * @return the [0..1] value. Zero if there is no demand for energy
	 */
	public float getEnergyPercent() {
		int e = getEnergyDemand();
		if (e > 0) {
			return energy * 1.0f / e;
		}
		return 0;
	}
	/**
	 * Returns the current worker amount relative
	 * to the current worker demand.
	 * @return the [0..1] value. Zero if there is no demand for workers
	 */
	public float getWorkerPercent() {
		int w = getWorkerDemand();
		if (w > 0) {
			return workers * 1.0f / w;
		}
		return 0;
	}
	/**
	 * Returns the energy demand of the building.
	 * @return the demand
	 */
	public int getEnergyDemand() {
		return enabled && progress == 100 && health >= 50 ? prototype.energy : 0;
	}
	/** 
	 * Returns the worker demand of this building.
	 * If the building is off line or incomplete, the demand is zero
	 * @return the worker amount 
	 */
	public int getWorkerDemand() {
		return enabled && progress == 100 && health >= 50 ? prototype.workers : 0;
	}
	/**
	 * Returns the operation percentage based
	 * on the current energy and worker supply.
	 * @return the operation percentage
	 */
	public float getOperationPercent() {
		int e = getEnergyDemand();
		int w = getWorkerDemand();
		float result = 0;
		// if no energy required, use the worker demand for the operation value
		if (e == 0 && w > 0) {
			result = workers * 1.0f / w;
		} else
		if (e > 0 && w == 0) {
			result = energy * 1.0f / e;
		} else
		if (e != 0 && w != 0) {
			result = Math.min(energy * 1.0f / e, workers * 1.0f / w);
		}
		if (health >= 50) {
			return result * 100 / health;
		}
		return 0;
	}
}
