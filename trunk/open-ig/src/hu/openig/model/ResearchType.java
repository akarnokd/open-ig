/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
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
	public final Set<String> race = new HashSet<String>();
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
	/** The optional prerequisites. */
	public final List<ResearchType> prerequisites = new ArrayList<ResearchType>();
	/** The optional properties. */
	public final Map<String, String> properties = new HashMap<String, String>();
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
	public final Map<String, EquipmentSlot> slots = new LinkedHashMap<String, EquipmentSlot>();
	// -------------------------------------------------
	// Resources for the Space battle screen.
//	/** 
//	 * The 286x197 image to display during the space battle in the information panel. 
//	 * Uses the <code>_huge</code> postfix.
//	 * Projectiles may not use this. 
//	 */
//	public BufferedImage spaceBattleImage;
//	/** The image sequence to rotate a ship. The first image is at angle zero relative to the screen-x coordinate. */
//	public BufferedImage[] rotation;
//	// -------------------------------------------------
//	// Resources for the ground battle screen
//	/** 
//	 * The image matrix where the first dimension indicates a firing phase and the second indicates
//	 * a rotation phase. The initial rotation faces the negative y axis of the surface.
//	 * Uses the <code>_matrix</code> suffix.
//	 */
//	public BufferedImage[][] fireAndRotation;
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
	/** @return the research time in 10s of ingame minutes. */
	public int researchTime() {
		return researchCost * 3 / 80;
	}
}
