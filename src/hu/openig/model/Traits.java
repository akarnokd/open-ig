/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.utils.Exceptions;
import hu.openig.utils.XElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Contains a set of traits.
 * @author akarnokd, 2012.08.18.
 */
public class Traits implements Iterable<Trait> {
    /** The list of traits. */
    protected final List<Trait> traits = new ArrayList<>();
    /** Traits by id. */
    protected final Map<String, Trait> traitsById = new HashMap<>();
    /** Traits by kind. */
    protected final Map<TraitKind, Trait> traitsByKind = new HashMap<>();
    /** The initial points to spend. */
    public int initialPoints;
    /**
     * Check the existence of a trait.
     * @param kind the kind
     * @return true if the trait exists
     */
    public boolean has(TraitKind kind) {
        return traitsByKind.containsKey(kind);
    }
    /**
     * Check the existence of a trait.
     * @param id the identifier
     * @return true if the trait exists
     */
    public boolean has(String id) {
        return traitsById.containsKey(id);
    }
    /**
     * Return a trait by its id.
     * @param <T> the concrete trait class
     * @param id the id
     * @return the trait, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T extends Trait> T trait(String id) {
        return (T)traitsById.get(id);
    }
    /**
     * Return a trait by its kind.
     * @param <T> the concrete trait class
     * @param kind the kind
     * @return the trait, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T extends Trait> T trait(TraitKind kind) {
        return (T)traitsByKind.get(kind);
    }
    /**
     * Parse a traits definition XML.
     * @param xml the XML tree
     */
    public void load(XElement xml) {
        clear();
        initialPoints = xml.getInt("initial-points");
        for (XElement xtr : xml.childrenWithName("trait")) {
            Trait t = createTrait(xtr);
            add(t);
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
        traitsByKind.clear();
        traitsById.clear();

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
            traitsById.put(t.id, t);
            traitsByKind.put(t.kind, t);
        }
    }
    /**
     * Add all traits from the other sequence.
     * @param ts the traits
     */
    public void addAll(Iterable<? extends Trait> ts) {
        if (ts != null) {
            for (Trait t : ts) {
                add(t);
            }
        }
    }
    /**
     * Replace the current traits.
     * @param ts the new traits
     */
    public void replace(Traits ts) {
        clear();
        addAll(ts);
        if (ts != null) {
            this.initialPoints = ts.initialPoints;
        }
    }
    @Override
    public Iterator<Trait> iterator() {
        return traits.iterator();
    }
    /**
     * Apply the given trait value to the given input value by multiplying it
     * together with the scale.
     * @param kind the kind
     * @param scale the scale
     * @param value the original value
     * @return the new value
     */
    public double apply(TraitKind kind, double scale, double value) {
        Trait t = trait(kind);
        if (t != null) {
            return value * t.value * scale;
        }
        return value;
    }
    /**
     * Apply the given trait value to the given input value by multiplying it
     * together with the scale.
     * @param id the trait identifier
     * @param scale the scale
     * @param value the original value
     * @return the new value
     */
    public double apply(String id, double scale, double value) {
        Trait t = trait(id);
        if (t != null) {
            return value * t.value * scale;
        }
        return value;
    }
    /**
     * @return true if there are no traits in this set
     */
    public boolean isEmpty() {
        return traits.isEmpty();
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + initialPoints;
        result = prime * result + ((traits == null) ? 0 : traits.hashCode());
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
        if (!(obj instanceof Traits)) {
            return false;
        }
        Traits other = (Traits) obj;
        if (initialPoints != other.initialPoints) {
            return false;
        }
        if (traits == null) {
            if (other.traits != null) {
                return false;
            }
        } else if (!traits.equals(other.traits)) {
            return false;
        }
        return true;
    }
    @Override
    public String toString() {
        return traits.toString();
    }
}
