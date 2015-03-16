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
 * Extract labels for garthogs.
 * @author akarnokd, 2012.07.13.
 */
public final class DiplomacyTranslateGarthog {
	/** Utility class. */
	private DiplomacyTranslateGarthog() { }
	/**
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		XElement x = XElement.parseXML("data/generic/campaign/main/diplomacy.xml");
		for (XElement p : x.childrenWithName("player")) {
			String id = p.get("id");
			if ("Garthog".equals(id)) {
				int i = 1;
				for (XElement e1 : p.children()) {
					if (e1.name.equals("terminate")) {
						String label = "diplomacy." + id + ".terminate." + i; 
						System.out.printf("<entry key='%s'>%s</entry>%n", label, e1.content);
						i++;
						e1.content = label;
					} else {
						for (XElement e2 : e1.children()) {
							String label = "diplomacy." + id + "." + e1.get("type") + "." + e2.get("type") + "." + i; 
							System.out.printf("<entry key='%s'>%s</entry>%n", label, e2.content);
							e2.content = label;
							i++;
						}
					}
				}
			}
		}
		x.save("data/generic/campaign/main/diplomacy.xml");
	}

}
