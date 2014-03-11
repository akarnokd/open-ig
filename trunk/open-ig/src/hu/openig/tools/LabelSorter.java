/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.utils.XElement;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sort a label file based on keys.
 * @author akarnokd, 2014.03.11.
 *
 */
public final class LabelSorter {
	/** Utility class. */
	private LabelSorter() { }
	/**
	 * Main code.
	 * @param args no arguments.
	 * @throws Exception on error
	 */
	public static void main(String[] args) throws Exception {
		File f = new File("data/fr/labels.xml");
		XElement xml = XElement.parseXML(f);
		
		Map<String, String> keys = new HashMap<>();
		for (XElement e : xml.children()) {
			if (e.has("key")) {
				String k = e.get("key");
				if (keys.containsKey(k)) {
					System.out.printf("Duplicate key: %s%n%s", k, keys.get(k));
				}
				keys.put(k, e.content);
			}
		}
		List<String> keyOrdered = new ArrayList<>(keys.keySet());
		Collections.sort(keyOrdered);
		
		XElement xml2 = new XElement("labels");
		for (String k : keyOrdered) {
			XElement xe = xml2.add("entry");
			xe.set("key", k);
			xe.content = keys.get(k);
		}
		xml2.save(f);
	}
}
