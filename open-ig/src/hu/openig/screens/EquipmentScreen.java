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

import java.awt.Graphics2D;
import java.awt.Rectangle;



/**
 * The equipment screen.
 * @author akarnokd, 2010.01.11.
 */
public class EquipmentScreen extends ScreenBase {
	/** The panel base rectangle. */
	Rectangle panelBaseRect;
	/** The equipment. */
	EquipmentGFX equipment;
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
		equipment = commons.equipment;
		panelBaseRect = new Rectangle(0, 0, 
				equipment.base.getWidth(), equipment.base.getHeight());
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
		RenderTools.centerScreen(panelBaseRect, width, height, true);
	}
	@Override
	public void draw(Graphics2D g2) {
		RenderTools.darkenAround(panelBaseRect, width, height, g2, 0.5f, true);
		g2.drawImage(equipment.base, panelBaseRect.x, panelBaseRect.y, null);
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
