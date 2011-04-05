/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Act;

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
	/**
	 * Construct the simulator.
	 * @param delay the real time delay between calculations
	 * @param world the world object
	 */
	public Simulator(int delay, World world) {
		this.world = world;
		timer = new Timer(delay, new Act() {
			@Override
			public void act() {
				compute();
			}
		});
		timer.setInitialDelay(0);
	}
	/** The main computation. */
	public void compute() {
		// TODO computations
	}
	/** Start the timer. */
	public void start() {
		timer.start();
	}
	/** Stop the timer. */
	public void stop() {
		timer.stop();
	}
}
