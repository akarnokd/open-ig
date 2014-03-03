/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.ResourceType;
import hu.openig.utils.XElement;

import java.util.HashMap;
import java.util.Map;

/**
 * The label manager with option for game specific sublabels.
 * @author akarnokd, 2009.10.25.
 */
public class Labels {
	/** The label map. */
	protected final Map<String, String> map = new HashMap<>();
	/**
	 * Load the language file(s).
	 * @param rl the resource locator
	 * @param labelRefs the sequence of label resources
	 * @return this
	 */
	public Labels load(ResourceLocator rl, Iterable<String> labelRefs) {
		map.clear();
		for (String ref : labelRefs) {
			if (rl.get(ref, ResourceType.DATA) != null) {
				XElement xml = rl.getXML(ref);
				process(xml);
			}
		}
		return this;
	}
	/**
	 * Load the labels from an XML element.
	 * @param root the root node
	 * @return this
	 */
	public Labels load(XElement root) {
		map.clear();
		process(root);
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
//			Exceptions.add(ex);
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
		replaceWith(newLabels.map);
	}
	/** @return the backing map. */
	public Map<String, String> map() {
		return map;
	}
}
