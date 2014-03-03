/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.utils.XElement;

import java.util.HashSet;
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
	public final Set<String> excludeIds = new HashSet<>();
	/** Set of trait kinds which should be unavailable when this trait is selected. */
	public final Set<TraitKind> excludeKinds = new HashSet<>();
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
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cost;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result
				+ ((excludeIds == null) ? 0 : excludeIds.hashCode());
		result = prime * result
				+ ((excludeKinds == null) ? 0 : excludeKinds.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		long temp;
		temp = Double.doubleToLongBits(value);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Trait)) {
			return false;
		}
		Trait other = (Trait) obj;
		if (cost != other.cost) {
			return false;
		}
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (excludeIds == null) {
			if (other.excludeIds != null) {
				return false;
			}
		} else if (!excludeIds.equals(other.excludeIds)) {
			return false;
		}
		if (excludeKinds == null) {
			if (other.excludeKinds != null) {
				return false;
			}
		} else if (!excludeKinds.equals(other.excludeKinds)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (kind != other.kind) {
			return false;
		}
		if (label == null) {
			if (other.label != null) {
				return false;
			}
		} else if (!label.equals(other.label)) {
			return false;
		}
        return Double.doubleToLongBits(value) == Double
                .doubleToLongBits(other.value);
    }
	@Override
	public String toString() {
		return String.valueOf(kind);
	}
}
