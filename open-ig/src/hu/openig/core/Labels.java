/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

import hu.openig.utils.XElement;

import java.util.HashMap;
import java.util.Map;

/**
 * The label manager with option for game specific sublabels.
 * @author akarnokd, 2009.10.25.
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
	protected void process(XElement root) {
		for (XElement e : root.childrenWithName("entry")) {
			map.put(e.get("key"), e.content);
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
			throw new AssertionError("Missing value for key: " + key);
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
	/**
	 * Replace all labels with the supplied new labels.
	 * This may be used to set an asynchronously loaded label source.
	 * @param newLabels the new labels
	 */
	public void replaceWith(Map<String, String> newLabels) {
		map.clear();
		map.putAll(newLabels);
	}
	/**
	 * Replace all labels with the supplied new labels.
	 * This may be used to set an asynchronously loaded label source.
	 * @param newLabels the new labels
	 */
	public void replaceWith(Labels newLabels) {
		map.clear();
		map.putAll(newLabels.map);
	}
}
