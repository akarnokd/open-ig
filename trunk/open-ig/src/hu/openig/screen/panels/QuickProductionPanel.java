/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.panels;

import hu.openig.core.Action0;
import hu.openig.model.Building;
import hu.openig.model.Planet;
import hu.openig.model.PlanetStatistics;
import hu.openig.render.TextRenderer;
import hu.openig.screen.CommonResources;
import hu.openig.ui.HorizontalAlignment;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UILabel;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * The quick production panel.
 * @author akarnokd, 2012.08.13.
 */
public class QuickProductionPanel extends UIContainer {
	/** The common resources. */
	final CommonResources commons;
	/** Label. */
	UILabel shipTitle;
	/** Label. */
	UILabel shipAvailable;
	/** Label. */
	UILabel shipTotal;
	/** Label. */
	UILabel weaponTitle;
	/** Label. */
	UILabel weaponAvailable;
	/** Label. */
	UILabel weaponTotal;
	/** Label. */
	UILabel equipmentTitle;
	/** Label. */
	UILabel equipmentAvailable;
	/** Label. */
	UILabel equipmentTotal;
	/** Pause the production. */
	UIImageButton pause;
	/** Resume the production. */
	UIImageButton resume;
	/** The margin inside the panel. */
	static final int MARGIN = 6;
	/** The column separator. */
	static final int COLUMN_SEPARATOR = 15;
	/** The top divider y. */
	int topDivider;
	/** The middle divider. */
	int middleDivider;
	/** The middle divider. */
	int bottomDivider;
	/** Description of the currently hovered research. */
	UILabel hoverResearchDescription;
	/** Description of the currently hovered research. */
	UILabel hoverResearchTitle;
	/**
	 * Constructor. Initializes the inner controls.
	 * @param commons the commonr resources
	 */
	public QuickProductionPanel(final CommonResources commons) {
		this.commons = commons;
		
		shipTitle = new UILabel("", 10, commons.text());
		shipTitle.horizontally(HorizontalAlignment.CENTER);
		weaponTitle = new UILabel("", 10, commons.text());
		weaponTitle.horizontally(HorizontalAlignment.CENTER);
		equipmentTitle = new UILabel("", 10, commons.text());
		equipmentTitle.horizontally(HorizontalAlignment.CENTER);
		
		shipAvailable = new UILabel("", 10, commons.text());
		weaponAvailable = new UILabel("", 10, commons.text());
		equipmentAvailable = new UILabel("", 10, commons.text());

		shipTotal = new UILabel("", 10, commons.text());
		weaponTotal = new UILabel("", 10, commons.text());
		equipmentTotal = new UILabel("", 10, commons.text());

		resume = new UIImageButton(commons.common().moveRight);
		resume.onClick = new Action0() {
			@Override
			public void invoke() {
				commons.player().pauseProduction = false;
			}
		};
		
		pause = new UIImageButton(commons.common().pauseAll);
		pause.onClick = new Action0() {
			@Override
			public void invoke() {
				commons.player().pauseProduction = true;
			}
		};

		hoverResearchDescription = new UILabel("", 10, commons.text());
		hoverResearchDescription.wrap(true);
		
		hoverResearchTitle = new UILabel("", 10, commons.text());
		hoverResearchTitle.color(TextRenderer.RED);
		hoverResearchTitle.horizontally(HorizontalAlignment.CENTER);

		addThis();
	}
	@Override
	public void draw(Graphics2D g2) {
		g2.setColor(new Color(0, 0, 0, 192));
		g2.fillRect(0, 0, width, height);
		g2.setColor(new Color(192, 192, 192));
		g2.drawRect(0, 0, width - 1, height - 1);
		
		g2.drawLine(0, topDivider, width - 1, topDivider);
		g2.drawLine(0, middleDivider, width - 1, middleDivider);
		g2.drawLine(0, bottomDivider, width - 1, bottomDivider);
		super.draw(g2);
	}
	/**
	 * Update the display contents. 
	 */
	public void update() {
		PlanetStatistics ps = computeProductionInfo();
		
		shipTitle.text(commons.get("quickproduction.ship_title"), true);
		weaponTitle.text(commons.get("quickproduction.weapon_title"), true);
		equipmentTitle.text(commons.get("quickproduction.equipment_title"), true);
		
		shipAvailable.text(String.valueOf(ps.spaceshipActive), true);
		shipAvailable.color(ps.spaceshipActive < ps.spaceship ? TextRenderer.YELLOW : TextRenderer.GREEN);
		shipTotal.text(" / " + ps.spaceship, true);

		weaponAvailable.text(String.valueOf(ps.weaponsActive), true);
		weaponAvailable.color(ps.weaponsActive < ps.weapons ? TextRenderer.YELLOW : TextRenderer.GREEN);
		weaponTotal.text(" / " + ps.weapons, true);

		equipmentAvailable.text(String.valueOf(ps.equipmentActive), true);
		equipmentAvailable.color(ps.equipmentActive < ps.equipment ? TextRenderer.YELLOW : TextRenderer.GREEN);
		equipmentTotal.text(" / " + ps.equipment, true);

		int col1Width = Math.max(shipTitle.width, shipAvailable.width + shipTotal.width);
		int col2Width = Math.max(equipmentTitle.width, equipmentAvailable.width + equipmentTotal.width);
		int col3Width = Math.max(weaponTitle.width, weaponAvailable.width + weaponTotal.width);
		int titlesHeight = Math.max(shipTitle.height + shipAvailable.height, resume.height) + COLUMN_SEPARATOR;
		
		topDivider = MARGIN + titlesHeight - COLUMN_SEPARATOR / 2;

		// listing of production lines
		
		// divider
		
		// hover info

		// layout
		
		int colMax = Math.min(Math.max(col1Width, Math.max(col2Width, col3Width)), 400);
		col1Width = colMax;
		col2Width = colMax;
		col3Width = colMax;

		// TODO

		shipTitle.location(MARGIN, MARGIN);
		shipTitle.width = col1Width;
		shipAvailable.y = shipTitle.y + shipTitle.height + MARGIN;
		shipTotal.y = shipTitle.y + shipTitle.height + MARGIN;
		centerInto(shipTitle.x, shipTitle.width, 0, shipAvailable, shipTotal);
		

		equipmentTitle.location(MARGIN + col1Width + COLUMN_SEPARATOR, MARGIN);
		equipmentTitle.width = col2Width;
		equipmentAvailable.y = equipmentTitle.y + equipmentTitle.height + MARGIN;
		equipmentTotal.y = equipmentTitle.y + equipmentTitle.height + MARGIN;
		centerInto(equipmentTitle.x, equipmentTitle.width, 0, equipmentAvailable, equipmentTotal);

		weaponTitle.location(MARGIN + col1Width + 2 * COLUMN_SEPARATOR + col2Width, MARGIN);
		weaponTitle.width = col3Width;
		weaponAvailable.y = weaponTitle.y + weaponTitle.height + MARGIN;
		weaponTotal.y = weaponTitle.y + weaponTitle.height + MARGIN;
		centerInto(weaponTitle.x, weaponTitle.width, 0, weaponAvailable, weaponTotal);

		pause.location(weaponTitle.x + weaponTitle.width + COLUMN_SEPARATOR, MARGIN);
		pause.visible(!commons.player().pauseProduction);
		resume.location(pause.location());
		resume.visible(commons.player().pauseProduction);
		
		int innerWidth = col1Width + col2Width + col3Width + 3 * COLUMN_SEPARATOR + pause.width;
		
		width = innerWidth + 2 * MARGIN;

		height = titlesHeight + 2 * MARGIN;
	}
	/**
	 * Center the combination of components.
	 * @param x the left coordinate
	 * @param w the width
	 * @param gap the gap between components
	 * @param c1 the first component
	 * @param c2 the second component
	 */
	void centerInto(int x, int w, int gap, UIComponent c1, UIComponent c2) {
		int w1 = c1.width + c2.width + gap;
		c1.x = x + (w - w1) / 2;
		c2.x = c1.x + c1.width + gap;
	}
	/**
	 * @return Compute the production statistics of the planets. 
	 */
	PlanetStatistics computeProductionInfo() {
		PlanetStatistics result = new PlanetStatistics();
		for (Planet p : commons.player().ownPlanets()) {
			for (Building b : p.surface.buildings) {
				double eff = b.getEfficiency();
				if (Building.isOperational(eff)) {
					if (b.hasResource("spaceship")) {
						result.spaceshipActive += b.getResource("spaceship") * eff;
					}
					if (b.hasResource("equipment")) {
						result.equipmentActive += b.getResource("equipment") * eff;
					}
					if (b.hasResource("weapon")) {
						result.weaponsActive += b.getResource("weapon") * eff;
					}
				}
				if (b.hasResource("spaceship")) {
					result.spaceship += b.getResource("spaceship");
				}
				if (b.hasResource("equipment")) {
					result.equipment += b.getResource("equipment");
				}
				if (b.hasResource("weapon")) {
					result.weapons += b.getResource("weapon");
				}
			}
			
			result.planetCount++;
		}

		return result;
	}
	/**
	 * Clear memorized references and values.
	 */
	public void clear() {
		// TODO
	}
}
