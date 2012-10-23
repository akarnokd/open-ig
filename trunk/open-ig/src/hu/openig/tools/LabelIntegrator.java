/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.utils.IOUtils;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.io.File;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Combine a label with fragments.
 * @author akarnokd, 2012.10.23.
 */
public final class LabelIntegrator {
	/** Utility class. */
	private LabelIntegrator() { }

	/**
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		Map<String, String> labels = U.newLinkedHashMap();
		File dest = new File("data/ru/labels.xml");
		XElement xlabels = XElement.parseXML(dest);
		for (XElement xentry : xlabels.childrenWithName("entry")) {
			String key = xentry.get("key");
			if (!key.isEmpty()) {
				labels.put(key, xentry.content);
			}
		}
		// ----------------------
		String prefix = "c:/Downloads/";
		List<String> files = Arrays.asList("message v01.xml", "doctor-manualsave v01.xml", "MainFleet-autosave v01.xml", "battlefinish v01.xml");
		
		for (String f : files) {
			String data = "<labels>" + new String(IOUtils.load(prefix + f), "UTF-8") + "</labels>";
			
			XElement xentries = XElement.parseXML(new StringReader(data));
			
			for (XElement xentry : xentries.childrenWithName("entry")) {
				String key = xentry.get("key");
				String value = xentry.content;
				if (labels.containsKey(key)) {
//					System.out.printf("Update  %s = %s%n", key, value);
//					System.out.flush();
					labels.put(key, value);
				} else {
					System.err.printf("Missing %s = %s%n", key, value);
					System.err.flush();
				}
			}
		}
		
		
		// ----------------------
		xlabels.clear();
		for (Map.Entry<String, String> e : labels.entrySet()) {
			XElement xentry = xlabels.add("entry");
			xentry.set("key", e.getKey());
			xentry.content = e.getValue();
		}
		xlabels.save(dest);
	}

}
