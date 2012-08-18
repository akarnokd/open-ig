/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.utils.Exceptions;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.util.Iterator;
import java.util.List;

/**
 * Contains a set of traits.
 * @author akarnokd, 2012.08.18.
 */
public class Traits implements Iterable<Trait> {
	/** The list of traits. */
	public final List<Trait> traits = U.newArrayList();
	/** The initial points to spend. */
	public int initialPoints;
	/**
	 * Check the existence of a trait.
	 * @param kind the kind
	 * @return true if the trait exists
	 */
	public boolean has(TraitKind kind) {
		for (Trait t : traits) {
			if (t.kind == kind) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Check the existence of a trait.
	 * @param id the identifier
	 * @return true if the trait exists
	 */
	public boolean has(String id) {
		for (Trait t : traits) {
			if (t.id.equals(id)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Return a trait by its id.
	 * @param <T> the concrete trait class
	 * @param id the id
	 * @return the trait, or null if not found
	 */
	public <T extends Trait> T trait(String id) {
		for (Trait t : traits) {
			if (t.id.equals(id)) {
				@SuppressWarnings("unchecked") T tt = (T)t;
				return tt;
			}
		}
		return null;
	}
	/**
	 * Return a trait by its kind.
	 * @param <T> the concrete trait class
	 * @param kind the kind
	 * @return the trait, or null if not found
	 */
	public <T extends Trait> T trait(TraitKind kind) {
		for (Trait t : traits) {
			if (t.kind == kind) {
				@SuppressWarnings("unchecked") T tt = (T)t;
				return tt;
			}
		}
		return null;
	}
	/**
	 * Parse a traits definition XML.
	 * @param xml the XML tree
	 */
	public void load(XElement xml) {
		traits.clear();
		initialPoints = xml.getInt("initial-points");
		for (XElement xtr : xml.childrenWithName("trait")) {
			Trait t = createTrait(xtr);
			traits.add(t);
		}
	}
	/**
	 * Create a trait instance.
	 * @param xtr the trait XML definition
	 * @return the trait
	 */
	protected Trait createTrait(XElement xtr) {
		TraitKind tk = TraitKind.valueOf(xtr.get("kind"));
		// special cases go here
		Trait result = new Trait();
		result.load(xtr);
		result.kind = tk;
		
		return result;
	}
	/**
	 * Clear the traits list.
	 */
	public void clear() {
		traits.clear();
		initialPoints = 0;
	}
	/**
	 * Add a trait.
	 * @param t the trait
	 */
	public void add(Trait t) {
		if (has(t.id)) {
			Exceptions.add(new AssertionError("Duplicate trait IDs: " + t.id));
		} else {
			traits.add(t);
		}
	}
	@Override
	public Iterator<Trait> iterator() {
		return traits.iterator();
	}
}
