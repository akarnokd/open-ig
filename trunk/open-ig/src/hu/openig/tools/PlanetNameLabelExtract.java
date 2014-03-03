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
 * Takes the planets.xml and adds a label reference to all entries, then
 * adds the original names to the label files.
 * @author akarnokd, 2012.11.09.
 */
public final class PlanetNameLabelExtract {
	/** Utility class. */
	private PlanetNameLabelExtract() { }
	/**
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		String fileName = "data/generic/campaign/main/planets.xml";
//		String fileName = "data/generic/skirmish/human/planets.xml";
		
		XElement xplanets = XElement.parseXML(fileName);
		for (XElement xplanet : xplanets.childrenWithName("planet")) {
			String key = "planets." + xplanet.get("id");
			String label = xplanet.get("label", null);
			String name = xplanet.get("name");
			if (label == null) {
				xplanet.set("label", key);
			}
			System.out.printf("\t<entry key='%s'>%s</entry>%n", key, name);
		}
		xplanets.save(fileName);
	}

}
