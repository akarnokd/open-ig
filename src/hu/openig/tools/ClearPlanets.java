/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.utils.XElement;

/**
 * Remove planets, inventory and owner info from a planets.xml file.
 * @author akarnokd, 2012.01.04.
 */
public final class ClearPlanets {
	/** Utility class. */
	private ClearPlanets() {
		// utility class
	}
	/**
	 * Main program.
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		String fn = "data/generic/skirmish/human/planets.xml";
		XElement xml = XElement.parseXML(fn);
		
		for (XElement xe : xml.childrenWithName("planet")) {
			xe.set("owner", null);
			xe.set("population", 0);
			xe.set("race", null);
			XElement xi = xe.childElement("inventory");
			if (xi != null) {
				xi.clear();
			}
			XElement xb = xe.childElement("buildings");
			if (xb != null) {
				xb.clear();
			}
		}
		xml.save(fn);
	}

}
