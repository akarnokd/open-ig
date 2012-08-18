/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.util.Set;

/**
 * Base class for a concrete trait.
 * @author akarnokd, 2012.08.18.
 */
public class Trait {
	/** The trait's unique identifier. */
	public String id;
	/** The kind. */
	public TraitKind kind;
	/** The short name of the trait (label ref). */
	public String label;
	/** The description of the trait (label ref).*/
	public String description;
	/** The cost of the trait. */
	public int cost;
	/** Set of traits which should be unavailable when this trait is selected. */
	public final Set<String> excludeIds = U.newHashSet();
	/** Set of trait kinds which should be unavailable when this trait is selected. */
	public final Set<TraitKind> excludeKinds = U.newHashSet();
	/** Parameter. */
	public double value;
	/** 
	 * Load the trait contents from an XML.
	 * @param xml the xml
	 */
	public void load(XElement xml) {
		id = xml.get("id");
		kind = TraitKind.valueOf(xml.get("kind"));
		label = xml.get("label");
		description = xml.get("description");
		cost = xml.getInt("cost");
		value = xml.getDouble("value", 0);
		for (XElement xexc : xml.childrenWithName("exclude")) {
			if (xexc.has("id")) {
				excludeIds.add(xexc.get("id"));
			} else
			if (xexc.has("kind")) {
				excludeIds.add(xexc.get("kind"));
			}
		}
	}
	/**
	 * Save the trait contents into an XML.
	 * @param xml the xml
	 */
	public void save(XElement xml) {
		xml.set("id", id);
		xml.set("kind", kind);
		xml.set("label", label);
		xml.set("description", description);
		xml.set("cost", cost);
		if (value != 0d) {
			xml.set("value", value);
		}
		for (String s : excludeIds) {
			xml.add("exclude").set("id", s);
		}
		for (TraitKind s : excludeKinds) {
			xml.add("exclude").set("kind", s);
		}
	}
}
