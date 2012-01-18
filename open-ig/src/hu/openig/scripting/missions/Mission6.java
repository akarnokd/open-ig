/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.scripting.missions;

/**
 * Mission 6: Defend Achilles.
 * @author akarnokd, 2012.01.18.
 */
public class Mission6 extends Mission {
	@Override
	public void onLevelChanged() {
		if (world.level == 2) {
			helper.send("Centronom-Check").visible = true;
			helper.send("New Caroline-Check").visible = true;
		}
	}
}
