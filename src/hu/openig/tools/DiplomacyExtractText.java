/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.core.Action1;
import hu.openig.utils.XElement;

/**
 * Extract text nodes from a diplomacy XML.
 * @author akarnokd, 2012.06.22.
 *
 */
public final class DiplomacyExtractText {
	/** Utility class. */
	private DiplomacyExtractText() { }
	/**
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		XElement d = XElement.parseXML("diplomacy_garthog.xml");
		d.visit(true, new Action1<XElement>() {
			@Override
			public void invoke(XElement value) {
				if (value.content != null) {
					System.out.println(value.content);
				}
			}
		});
	}

}
