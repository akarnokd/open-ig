/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.utils.XElement;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Find non translated labels in a language file relative to the english labels.
 * @author akarnokd, 2014.04.24.
 */
public final class FindUntranslated {
	/** Utility class. */
	private FindUntranslated() { }
	/**
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		String targetLanguage = "de";
		
		XElement xBaseLabels = XElement.parseXML("data/en/labels.xml");
		Map<String, String> base = new LinkedHashMap<>();
		for (XElement child : xBaseLabels.childrenWithName("entry")) {
			base.put(child.get("key"), child.content);
		}
		
		XElement xTargetLabels = XElement.parseXML("data/" + targetLanguage + "/labels.xml");
		
		XElement xTodo = new XElement("labels");
		
		for (XElement xchild : xTargetLabels.childrenWithName("entry")) {
			if (xchild.content != null 
					&& Objects.equals(xchild.content, base.get(xchild.get("key")))) {
				xTodo.add("entry", xchild.content).set("key", xchild.get("key"));
			}
		}
		
		
		xTodo.save("labels-" + targetLanguage + ".xml");
	}

}
