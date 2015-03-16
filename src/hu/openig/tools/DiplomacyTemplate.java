/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.model.Labels;
import hu.openig.utils.XElement;

/**
 * Replace diplomatic labels with actual text.
 * @author akarnokd, 2012.06.19.
 */
public final class DiplomacyTemplate {
	/** Utility class. */
	private DiplomacyTemplate() { }
	/**
	 * Program.
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		XElement xdipl = XElement.parseXML("data/generic/campaign/main/diplomacy.xml");
		Labels labels = new Labels();
		labels.load(XElement.parseXML("data/hu/labels.xml"));
		
		for (XElement x1 : xdipl.childrenWithName("player")) {
			for (XElement x2 : x1.children()) {
				for (XElement x3 : x2.children()) {
					x3.content = labels.get(x3.content);
				}
			}
		}
		xdipl.save("diplomacy_hu.xml");
	}
}
