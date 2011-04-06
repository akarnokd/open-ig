/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Act;
import hu.openig.screens.GameControls;

import javax.swing.Timer;

/**
 * Collection of algorithms which update the world
 * state as time progresses: progress on buildings, repair
 * research, production, fleet movement, etc.
 * @author akarnokd, Apr 5, 2011
 */
public class Simulator {
	/** The timer to periodically change things. */
	protected final Timer timer;
	/** The world object. */
	protected final World world;
	/** The general game world controls. */
	protected final GameControls controls;
	/**
	 * Construct the simulator.
	 * @param delay the real time delay between calculations
	 * @param world the world object
	 * @param controls the game world controls
	 */
	public Simulator(int delay, World world, GameControls controls) {
		this.controls = controls;
		this.world = world;
		timer = new Timer(delay, new Act() {
			@Override
			public void act() {
				if (compute()) {
					Simulator.this.controls.repaintInner();
				}
			}
		});
		timer.setInitialDelay(0);
	}
	/** 
	 * The main computation. 
	 * @return true if repaint will be needed 
	 */
	public boolean compute() {
		boolean result = false;
		for (Player player : world.players.values()) {
			result |= progressResearch(player) && player == world.player;
		}
		for (Planet p : world.planets) {
			if (p.owner != null) {
				result |= progressPlanet(p) && p == world.player.currentPlanet;
			}
		}
		return result;
	}
	/**
	 * Make progress on the buildings of the planet.
	 * @param planet the planet
	 * @return true if repaint will be needed 
	 */
	public boolean progressPlanet(Planet planet) {
		boolean result = false;
		float freeRepair = 0;
		for (Building b : planet.surface.buildings) {
			if (b.getEfficiency() >= 0.5 && b.hasResource("repair")) {
				freeRepair = Math.max(b.getResource("repair"), freeRepair);
			}
		}
		final int repairCost = 20;
		final int repairAmount = 50;
		for (Building b : planet.surface.buildings) {
			if (b.isConstructing()) {
				b.buildProgress += 200;
				b.buildProgress = Math.min(b.type.hitpoints, b.buildProgress);
				b.hitpoints += 200;
				b.hitpoints = Math.min(b.type.hitpoints, b.hitpoints);
				result = true;
			} else
			if (b.repairing) {
				if (b.hitpoints * 100 / b.type.hitpoints < freeRepair) {
					b.hitpoints += repairAmount;
					b.hitpoints = Math.min(b.type.hitpoints, b.hitpoints);
					result = true;
				} else {
					if (planet.owner.money >= 20) {
						planet.owner.money -= repairCost; // FIXME repair cost per unit?
						planet.owner.today.repairCost += repairCost;
						b.hitpoints += repairAmount;
						b.hitpoints = Math.min(b.type.hitpoints, b.hitpoints);
						result = true;
					}
				}
			} else
			if (b.isDamaged()) {
				if (b.hitpoints * 100 / b.type.hitpoints < freeRepair) {
					b.hitpoints += repairAmount;
					b.hitpoints = Math.min(b.type.hitpoints, b.hitpoints);
					result = true;
				}
			}
			if (b.repairing && b.hitpoints == b.type.hitpoints) {
				b.repairing = false;
				result = true;
			}
		}
		return result;
	}
	/**
	 * Make progress on the active research if any.
	 * @return true if repaint will be needed 
	 * @param player the player
	 */
	public boolean progressResearch(Player player) {
		if (player.runningResearch != null) {
			Research rs = player.research.get(player.runningResearch);
			int maxpc = rs.getResearchMaxPercent(player.getPlanetStatistics());
			// test for money
			// test for max percentage
			if (rs.remainingMoney > 0) {
				if (rs.getPercent() < maxpc) {
					float rel = 1.0f * rs.assignedMoney / rs.remainingMoney;
					int dmoney = (int)(rel * 40);
					if (dmoney < player.money) {
						rs.remainingMoney = Math.max(0, rs.remainingMoney - dmoney);
						rs.assignedMoney = Math.min((int)(rs.remainingMoney * rel) + 1, rs.remainingMoney);
						player.today.researchCost += dmoney;
						player.money -= dmoney;
						rs.state = ResearchState.RUNNING;
					} else {
						rs.state = ResearchState.MONEY;
					}
				} else {
					rs.state = ResearchState.LAB;
				}
			}
			// test for completedness
			if (rs.remainingMoney == 0) {
				player.runningResearch = null;
				player.research.remove(rs.type);
				player.availableResearch.add(rs.type);
			}
			return true;
		}
		return false;
	}
	/** Start the timer. */
	public void start() {
		timer.start();
	}
	/** Stop the timer. */
	public void stop() {
		timer.stop();
	}
	/**
	 * Set the time delay between calculations.
	 * @param delay the delay in milliseconds
	 */
	public void setDelay(int delay) {
		timer.setDelay(delay);
	}
	/** @return Is the timer running? */
	public boolean isRunning() {
		return timer.isRunning();
	}
}
