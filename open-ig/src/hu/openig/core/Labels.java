/*
 * Copyright 2008-2012, David Karnok 
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
	 * @param gameLabel the optional game namings, ignored if null or empty
	 * @return this
	 */
	public Labels load(ResourceLocator rl, String gameLabel) {
		map.clear();
		process(rl.getXML("labels"));
		if (gameLabel != null && !gameLabel.isEmpty()) {
			if (rl.get(gameLabel, ResourceType.DATA) != null) {
				process(rl.getXML(gameLabel));
			}
		}
		return this;
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
	public synchronized String get(String key) { // FIXME temporary
		String value = map.get(key);
		if (value == null) {
			System.err.println("\t<entry key='" + key + "'></entry>");
//			AssertionError ex = new AssertionError();
//			ex.printStackTrace();
			map.put(key, key);
			return key;
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
