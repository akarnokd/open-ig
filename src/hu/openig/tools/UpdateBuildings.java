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
 * Update the old buildings.xml to the new one.
 * @author akarnokd, 2012.09.17.
 */
public final class UpdateBuildings {
	/** Utility class. */
	private UpdateBuildings() { }

	/**
	 * @param args ignored
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		String f = "dlc/gump891202-racemod-0.1/generic/skirmish/racemod-0.1/buildings.xml";
		XElement xml = XElement.parseXML(f);
		for (XElement xb : xml.childrenWithName("building")) {
			for (XElement xg : xb.childrenWithName("graphics")) {
				String base = xg.get("base", null);
				xg.set("base", null);
				if (base != null) {
					for (XElement xt : xg.childrenWithName("tech")) {
						xt.set("image", String.format(base, xt.get("id")));
					}
				}
			}
		}
		xml.save(f);
	}

}
