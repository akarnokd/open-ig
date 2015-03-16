/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A technologycal research base.
 * @author akarnokd, 2010.01.07.
 */
public class ResearchType {
	/** The research identifier. */
	public String id;
	/** The display name of the research. */
	public String name;
	/** The display long name of the research. */
	public String longName;
	/** The display description label of the research. */
	public String description;
	/** The research category. */
	public ResearchSubCategory category;
	/** The 104x78 image to display on the research/production screen. */
	public BufferedImage image;
	/** The 204x170 wireframe image to display on the information panel. Uses the <code>_wired_large</code> postfix.*/
	public BufferedImage infoImageWired;
	/** The 204x170 image to display on the information panel. Uses the <code>_large</code> postfix.*/
	public BufferedImage infoImage;
	/** The required factory. */
	public String factory;
	/** The cost of research. */
	public int researchCost;
	/** The cost of production. */
	public int productionCost;
	/** The target races. */
	public final Set<String> race = new HashSet<>();
	/** The level when this item becomes available for research. Zero means always available. */
	public int level;
	/** The civil lab requirements. */
	public int civilLab;
	/** The mechanical lab requirements. */
	public int mechLab;
	/** The computer lab requirements. */
	public int compLab;
	/** The AI lab requirements. */
	public int aiLab;
	/** The military lab requirements. */
	public int milLab;
	/** The index on the screen listing. */
	public int index;
	/** The video resource name. */
	public String video;
	/** Do not allow producing this technology. */
	public boolean nobuild;
	/** The optional prerequisites. */
	public final List<ResearchType> prerequisites = new ArrayList<>();
	/** The optional properties. */
	public final Map<String, String> properties = new HashMap<>();
	// -------------------------------------------------
	// Resources for the Equipment screen.
	/** The equipment image to display as a fleet listing (left panel). Uses the <code>_tiny</code> postfix. */
	public BufferedImage equipmentImage;
	/** 
	 * The equipment image to display when the ship is customized (right panel). 
	 * Uses the <code>_small</code> postfix.
	 * If not present, it is the same as the image with the <code>_huge</code> postfix.
	 */
	public BufferedImage equipmentCustomizeImage;
	/** The available equipment slots. */
	public final Map<String, EquipmentSlot> slots = new LinkedHashMap<>();
	/** The vehicle capacity parameter name. */
	public static final String PARAMETER_VEHICLES = "vehicles";
	/** The shield percentage parameter name. */
	public static final String PARAMETER_SHIELD = "shield";
	/** The radar level parameter name. */
	public static final String PARAMETER_RADAR = "radar";
	/** The speed parameter name. */
	public static final String PARAMETER_SPEED = "speed";
	/** The projectile parameter name. */
	public static final String PARAMETER_PROJECTILE = "projectile";
	/** The projectile parameter name. */
	public static final String PARAMETER_DETECTOR = "detector";
	/** AI hint for one per fleet parameter name. */
	public static final String PARAMETER_ONE_PER_FLEET = "one-per-fleet";
	/** Needs orbital factory parameter name. */
	public static final String PARAMETER_NEEDS_ORBITAL_FACTORY = "needsOrbitalFactory";
	/** The electronic countermeasure parameter name. */
	public static final String PARAMETER_ECM = "ecm";
	/**
	 * Retrieve a property value.
	 * @param property the property name
	 * @return the property value or null if not present.
	 */
	public String get(String property) {
		return properties.get(property);
	}
	/**
	 * Does the property exist.
	 * @param property the property name
	 * @return true if exists
	 */
	public boolean has(String property) {
		return properties.containsKey(property);
	}
	/**
	 * Get a property as an integer value.
	 * @param property the property name
	 * @return the value
	 */
	public int getInt(String property) {
		return Integer.parseInt(get(property));
	}
	/**
	 * Get a property as an integer value or return the default value.
	 * @param property the property name
	 * @param def the default value if the property does not exist.
	 * @return the value
	 */
	public int getInt(String property, int def) {
		String s = get(property);
		if (s == null) {
			return def;
		}
		return Integer.parseInt(s);
	}
	/** 
	 * @param traits the traits to consider
	 * @return the research time in 10s of ingame minutes. 
	 */
	public int researchTime(Traits traits) {
		return researchCost(traits) * 3 / 80;
	}
	/**
	 * The research cost for your race.
	 * @param traits the traits to consider
	 * @return the cost
	 */
	public int researchCost(Traits traits) {
		Trait t = traits.trait(TraitKind.SCIENCE);
		int v = researchCost;
		if (t != null) {
			v = (int)(v * (1 + t.value / 100));
		}
		return v;
	}
	/**
	 * Check if the global planet statistics provides enough active labs to support the given research.
	 * @param ps the global planet statistics
	 * @return true if enough active labs
	 */
	public boolean hasEnoughLabs(PlanetStatistics ps) {
		return ps.activeLabs.civil >= civilLab
				&& ps.activeLabs.mech >= mechLab 
				&& ps.activeLabs.comp >= compLab
				&& ps.activeLabs.ai >= aiLab
				&& ps.activeLabs.mil >= milLab;
	}
	/**
	 * Check if the global planet statistics provides enough built labs to support the given research.
	 * @param ps the global planet statistics
	 * @return true if enough active labs
	 */
	public boolean hasEnoughLabsBuilt(PlanetStatistics ps) {
		return ps.labs.civil >= civilLab
				&& ps.labs.mech >= mechLab 
				&& ps.labs.comp >= compLab
				&& ps.labs.ai >= aiLab
				&& ps.labs.mil >= milLab;
	}
	/**
	 * @return The sum of all kinds of lab counts. 
	 */
	public int labCount() {
		return civilLab + mechLab + compLab + aiLab + milLab;
	}
	/**
	 * Check if any of the slot supports this technology.
	 * @param rt the technology
	 * @return the slot or null if not supported
	 */
	public EquipmentSlot supports(ResearchType rt) {
		for (EquipmentSlot es : slots.values()) {
			if (es.items.contains(rt)) {
				return es;
			}
		}
		return null;
	}
	@Override
	public String toString() {
		return id;
	}
	/**
	 * Orders the technology as expensives first.
	 */
	public static final Comparator<ResearchType> EXPENSIVE_FIRST = new Comparator<ResearchType>() {
		@Override
		public int compare(ResearchType o1, ResearchType o2) {
			return o2.productionCost - o1.productionCost;
		}
	};
	/**
	 * Orders the technology as cheapest first.
	 */
	public static final Comparator<ResearchType> CHEAPEST_FIRST = new Comparator<ResearchType>() {
		@Override
		public int compare(ResearchType o1, ResearchType o2) {
			return o1.productionCost - o2.productionCost;
		}
	};
	/**
	 * Comparator to sort by main category, subcategory and index.
	 */
	public static final Comparator<ResearchType> LISTING_SORT = new Comparator<ResearchType>() {
		@Override
		public int compare(ResearchType o1, ResearchType o2) {
			int c = o1.category.main.ordinal() - o2.category.main.ordinal();
			if (c == 0) {
				c = o1.category.ordinal() - o2.category.ordinal();
				if (c == 0) {
					c = o1.index - o2.index;
				}
			}
			return c;
		}
	};
}
