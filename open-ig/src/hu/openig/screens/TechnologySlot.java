/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.screens;

import hu.openig.core.Action1;
import hu.openig.model.LabLevel;
import hu.openig.model.Research;
import hu.openig.model.ResearchMainCategory;
import hu.openig.model.ResearchType;
import hu.openig.render.RenderTools;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Represents the rendering for an equipment, production or research slot.
 * @author akarnokd, 2011.03.06.
 */
public class TechnologySlot extends UIComponent {
	/** The common resources. */
	final CommonResources commons;
	/** The research type shown. */
	public ResearchType type;
	/** The border color when a slot is present. */
	final Color availableColor = new Color(0xFF752424);
	/** The border color when a slot is selected. */
	final Color selectedColor = Color.RED;
	/** The normal text color. */
	final int textColor = 0xFF6DB269;
	/** The selected text color. */
	final int selectedTextColor = 0xFFFF0000;
	/** The current animation step for the rolling disk. */
	public int animationStep;
	/** The action to invoke when the user clicks on the slot. */
	public Action1<ResearchType> onPress;
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
		if (type == null) {
			return;
		}
		Rectangle target = new Rectangle(0, 0, width, height);
		g2.drawImage(type.image, target.x, target.y, null);
		if (commons.world().player.isAvailable(type)) {
			if (type.category.main != ResearchMainCategory.BUILDINS) {
				commons.text().paintTo(g2, target.x + 5, target.y + 56, 10, 
						selectedTextColor, Integer.toString(commons.world().player.count(type)));
			}
			if (type.category.main != ResearchMainCategory.BUILDINS) {
				commons.text().paintTo(g2, target.x + 5, target.y + 5, 10, 
						selectedTextColor, Integer.toString(type.productionCost));
			}
		} else
		if (commons.world().canResearch(type)) {
			g2.setColor(Color.BLACK);
			Research rp = commons.world().player.research.get(type);
			float percent = 0;
			if (rp != null) {
				percent = rp.getPercent() / 100f;
			}
			for (int i = 0; i < target.height - 7; i += 2) {
				float perc = 1.0f * i / (target.height - 7);
				if (perc >= percent) {
					g2.drawLine(target.x + 2, target.y + 4 + i, target.x + target.width - 4, target.y + 4 + i);
				}
			}
			
			if (commons.world().player.research.containsKey(type)) {
				BufferedImage[] rolling = commons.research().rolling;
				g2.drawImage(rolling[animationStep % rolling.length], target.x + 5, target.y + 49, null);
			}
			LabLevel lvl = commons.world().player.hasEnoughLabs(type);
			if (lvl == LabLevel.NOT_ENOUGH_TOTAL) {
				g2.drawImage(commons.research().researchMissingPrerequisite, target.x + 5 + 16, target.y + 49 + 5, null);
			} else
			if (lvl == LabLevel.NOT_ENOUGH_ACTIVE) {
				g2.drawImage(commons.research().researchMissingLab, target.x + 5 + 16, target.y + 49 + 5, null);
			}
			commons.text().paintTo(g2, target.x + 5, target.y + 5, 10, 
					selectedTextColor, Integer.toString(type.researchCost));
		} else {
			g2.setColor(Color.BLACK);
			for (int i = 0; i < target.height - 7; i += 2) {
				g2.drawLine(target.x + 2, target.y + 4 + i, target.x + target.width - 4, target.y + 4 + i);
			}
			RenderTools.drawCentered(g2, target, commons.research().unavailable);
		}
		boolean selected = commons.world().player.currentResearch == type;
		commons.text().paintTo(g2, target.x + 5, target.y + 71, 7, 
				selected ? selectedTextColor : textColor, type.name);
		if (selected) {
			g2.setColor(selectedColor);
		} else {
			g2.setColor(availableColor);
		}
		g2.drawRect(target.x, target.y, target.width - 1, target.height - 1);
		g2.drawRect(target.x + 1, target.y + 1, target.width - 3, target.height - 3);
	}
	@Override
	public boolean mouse(UIMouse e) {
		if (e.has(Type.DOWN) && type != null) {
			if (onPress != null) {
				onPress.invoke(type);
			}
			return true;
		}
		return false;
	}
}
