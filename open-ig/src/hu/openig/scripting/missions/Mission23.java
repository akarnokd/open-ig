/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

/**
 * Mission 23: Final vision.
 * @author akarnokd, 2012.03.02.
 */
public class Mission23 extends Mission {

	@Override
	public boolean applicable() {
		return world.level >= 4;
	}

}
