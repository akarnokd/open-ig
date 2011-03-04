/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import hu.openig.gfx.EquipmentGFX;
import hu.openig.render.RenderTools;
import hu.openig.ui.UIImage;
import hu.openig.ui.UIImageButton;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;



/**
 * The equipment screen.
 * @author akarnokd, 2010.01.11.
 */
public class EquipmentScreen extends ScreenBase {
	/** The panel base rectangle. */
	final Rectangle base = new Rectangle();
	/** The equipment. */
	EquipmentGFX equipment;
	/** The left panel. */
	final Rectangle leftPanel = new Rectangle();
	/** The right panel. */
	final Rectangle rightPanel = new Rectangle();
	/** The info button. */
	UIImageButton infoButton;
	/** The production button. */
	UIImageButton productionButton;
	/** The research button. */
	UIImageButton researchButton;
	/** The bridge button. */
	UIImageButton bridgeButton;
	/** The large vertical starmap button right to the minimap. */
	UIImageButton starmapButton;
	/** The large vertical colony button right to the minimap. */
	UIImageButton colonyButton;
	/** The placeholder for the no-research-button case. */
	UIImage noResearch;
	/** The placeholder for the no-production-button case. */
	UIImage noProduction;
	/** Placeholder for the no-starmap-button case. */
	UIImage noStarmap;
	/** Placeholder for the no-colony-button case. */
	UIImage noColony;
	/** The equipment slot locations. */
	final List<Rectangle> slots = new ArrayList<Rectangle>();
	/** The current equipment mode to render and behave. */
	public enum EquipmentMode {
		/** Manage an existing or new fleet. */
		MANAGE_FLEET,
		/** Share or combine existing fleets. */
		SHARE_OR_COMBINE,
		/** Manage a planet. */
		MANAGE_PLANET
	}
	/** The equipment mode. */
	EquipmentMode mode;
	@Override
	public void onInitialize() {
		equipment = commons.equipment();
		base.setBounds(0, 0, 
				equipment.base.getWidth(), equipment.base.getHeight());
		
		infoButton = new UIImageButton(commons.common().infoButton);
		bridgeButton = new UIImageButton(commons.common().bridgeButton);
		researchButton = new UIImageButton(commons.research().research);
		productionButton = new UIImageButton(commons.research().production);
		
		starmapButton = new UIImageButton(commons.equipment().starmap);
		colonyButton = new UIImageButton(commons.equipment().planet);
		
		noResearch = new UIImage(commons.common().emptyButton);
		noResearch.visible = false;
		noProduction = new UIImage(commons.common().emptyButton);
		noProduction.visible = false;
		noStarmap = new UIImage(commons.equipment().buttonMapEmpty);
		noStarmap.visible = false;
		noColony = new UIImage(commons.equipment().buttonMapEmpty);
		noColony.visible = false;
		
		add(infoButton, bridgeButton, researchButton, 
			productionButton, starmapButton, colonyButton,
			noResearch, noProduction, noStarmap, noColony
		);
	}

	@Override
	public void onEnter() {
		onResize();
		if (mode == null) {
			mode = EquipmentMode.MANAGE_FLEET;
		}
	}

	@Override
	public void onLeave() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResize() {
		RenderTools.centerScreen(base, width, height, true);
		
		leftPanel.setBounds(base.x + 1, base.y + 1, 318, 198);
		rightPanel.setBounds(leftPanel.x + leftPanel.width + 2, leftPanel.y, 318, 198);
		infoButton.location(base.x + 535, base.y + 303 - 20);
		productionButton.location(infoButton.x, infoButton.y + infoButton.height);
		researchButton.location(productionButton.x, productionButton.y + productionButton.height);
		bridgeButton.location(researchButton.x, researchButton.y + researchButton.height);
		
		starmapButton.location(base.x + 479, base.y + 303 - 20);
		colonyButton.location(starmapButton.x + 26, starmapButton.y);
		
		noResearch.location(researchButton.location());
		noProduction.location(productionButton.location());
		noStarmap.location(starmapButton.location());
		noColony.location(colonyButton.location());
	}
	@Override
	public void draw(Graphics2D g2) {
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);
		g2.drawImage(equipment.base, base.x, base.y, null);
		
		super.draw(g2);
	}
	/**
	 * Set the equipment rendering mode. Use this before
	 * switching to the equipment screen.
	 * Based on this mode, the screen will use the current planet,
	 * the current fleet (with the nearest other fleet)
	 * @param mode the screen mode
	 */
	public void setEquipmentMode(EquipmentMode mode) {
		this.mode = mode;
	}
}
