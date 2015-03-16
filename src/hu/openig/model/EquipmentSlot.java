/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Record for an on-ship equipment slot.
 * @author akarnokd, 2011.04.01.
 */
public class EquipmentSlot {
	/** Is this slot fixed? */
	public final boolean fixed;
	/** The slot's identifier. */
	public final String id;
	/** The slot's display location. */
	public int x;
	/** The slot's display location. */
	public int y;
	/** The slot's display size. */
	public int width;
	/** The slot's display size. */
	public int height;
	/** The maximum amount allowed. */
	public int max;
	/** The list of allowed research types. */
	public final List<ResearchType> items = new ArrayList<>();
    /**
     * Constructor, initializes the slot identifier and fixed indicator.
     * @param id the slot identifier.
     * @param fixed the fixed-slot indicator
     */
    public EquipmentSlot(String id, boolean fixed) {
        this.id = Objects.requireNonNull(id);
        this.fixed = fixed;
    }
    @Override
    public String toString() {
        return id + (fixed ? " ! " : "") + " (max = " + max + ", items = " + items + ")";
    }
}
