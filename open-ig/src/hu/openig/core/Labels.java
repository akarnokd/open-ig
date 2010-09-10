/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

import hu.openig.utils.XML;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

/**
 * The label manager with option for game specific sublabels.
 * @author karnok, 2009.10.25.
 * @version $Revision 1.0$
 */
public class Labels {
	/** The label map. */
	protected final Map<String, String> map = new HashMap<String, String>();
	/**
	 * Load the language file(s).
	 * @param rl the resource locator
	 * @param language the language
	 * @param gameLabel the optional game namings, ignored if null or empty
	 */
	public void load(ResourceLocator rl, String language, String gameLabel) {
		map.clear();
		process(rl.getXML(language, "labels"));
		if (gameLabel != null && !gameLabel.isEmpty()) {
			if (rl.get(language, gameLabel, ResourceType.DATA) != null) {
				process(rl.getXML(language, gameLabel));
			}
		}
	}
	/**
	 * Process the document.
	 * @param root the root element
	 */
	protected void process(Element root) {
		for (Element e : XML.childrenWithName(root, "entry")) {
			map.put(e.getAttribute("key"), e.getTextContent());
		}
	}
	/**
	 * Returns a given key.
	 * @param key the key
	 * @return the associated value
	 */
	public String get(String key) {
		String value = map.get(key);
		if (value == null) {
			throw new AssertionError("Missing value for key " + key);
		}
		return value;
	}
	/**
	 * Use the given entry as a formatter and generate the string using the given parameters.
	 * @param key the key
	 * @param values the list of values
	 * @return the string
	 */
	public String format(String key, Object... values) {
		return String.format(get(key), values);
	}
}
