/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.screens;

import hu.openig.render.RenderTools;
import hu.openig.ui.UIComponent;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Represents the rendering for an equipment, production or research slot.
 * @author akarnokd, 2011.03.06.
 */
public class TechnologySlot extends UIComponent {
	/** The technology name. */
	String name;
	/** The inventory count. */
	int inventory;
	/** Is the slot selected? */
	boolean selected;
	/** Is the technology researched and available. */
	boolean available;
	/** How much of the technology is researched? 0..1 */
	float percent;
	/** Is under research. */
	boolean researching;
	/** There is not enough powered laboratory to complete the research. */
	boolean missingLab;
	/** There is not enough total laboratory to complete the research. */
	boolean missingPrerequisite;
	/** Indicator that the technology has prerequisites to be researched first. */
	boolean notResearchable;
	/** The technology image. */
	BufferedImage image;
	/** The border color when a slot is present. */
	final Color availableColor = new Color(0xFF752424);
	/** The border color when a slot is selected. */
	final Color selectedColor = Color.RED;
	/** The invisible slot color. */
	final Color invisibleColor = new Color(0xFF65496D);
	/** The normal text color. */
	final int textColor = 0xFF6DB269;
	/** The selected text color. */
	final int selectedTextColor = 0xFFFF0000;
	/** The common resources. */
	CommonResources commons;
	/** The current animation step for the rolling disk. */
	public int animationStep;
	/**
	 * Constructor.
	 * @param commons the common resources
	 */
	public TechnologySlot(CommonResources commons) {
		this.commons = commons;
	}
	/**
	 * Render the technology based on its state.
	 * @param g2 the target graphics context
	 */
	@Override
	public void draw(Graphics2D g2) {
		Rectangle target = new Rectangle(0, 0, width, height);
		if (visible) {
			g2.drawImage(image, target.x, target.y, null);
			if (notResearchable) {
				RenderTools.drawCentered(g2, target, commons.research().unavailable);
			} else
			if (researching) {
				g2.setColor(Color.BLACK);
				for (int i = 0; i < target.height - 7; i += 2) {
					float perc = 1.0f * i / (target.height - 7);
					if (perc >= percent) {
						g2.drawLine(target.x + 2, target.y + 4 + i, target.x + target.width - 4, target.y + 4 + i);
					}
				}
				BufferedImage[] rolling = commons.research().rolling;
				g2.drawImage(rolling[animationStep % rolling.length], target.x + 5, target.y + 49, null);
				if (missingLab) {
					g2.drawImage(commons.research().researchMissingLab, target.x + 5 + 16, target.y + 49 + 5, null);
				} else
				if (missingPrerequisite) {
					g2.drawImage(commons.research().researchMissingPrerequisite, target.x + 5 + 16, target.y + 49 + 5, null);
				}
			} else
			if (available) {
				commons.text().paintTo(g2, target.x + 5, target.y + 56, 10, 
						selectedTextColor, Integer.toString(inventory));
			}
			commons.text().paintTo(g2, target.x + 5, target.y + 71, 7, 
					selected ? selectedTextColor : textColor, name);
		}
		if (visible) {
			g2.setColor(invisibleColor);
		} else
		if (selected) {
			g2.setColor(selectedColor);
		} else {
			g2.setColor(availableColor);
		}
		g2.drawRect(target.x, target.y, target.width - 1, target.height - 1);
		g2.drawRect(target.x + 1, target.y + 1, target.width - 3, target.height - 3);
	}
}
