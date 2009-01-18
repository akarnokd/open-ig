/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.ani;
/**
 * The game's main clock.
 * @author karnokd
 *
 */
public class Clock implements Runnable {
	/** Pause clock flag. */
	private volatile boolean pauseClock;
	@Override
	public void run() {
		if (!pauseClock) {
			
		}
	}
	/** Pause the clock. */
	public void pause() {
		this.pauseClock = true;
	}
	/** Resume the clock. */
	public void resume() {
		this.pauseClock = false;
	}
}
