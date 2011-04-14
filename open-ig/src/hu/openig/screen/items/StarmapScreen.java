/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;


import hu.openig.core.Act;
import hu.openig.model.Fleet;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.FleetMode;
import hu.openig.model.FleetStatistics;
import hu.openig.model.InventoryItem;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.PlanetProblems;
import hu.openig.model.PlanetStatistics;
import hu.openig.model.ResearchType;
import hu.openig.model.RotationDirection;
import hu.openig.model.Screens;
import hu.openig.model.SelectionMode;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.UIImage;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIImageTabButton;
import hu.openig.ui.UIImageToggleButton;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Button;
import hu.openig.ui.UIMouse.Modifier;
import hu.openig.ui.UIMouse.Type;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The starmap screen.
 * @author akarnokd, 2010.01.11.
 */
public class StarmapScreen extends ScreenBase {
	/** The horizontal/vertical scrollbar painter. */
	public class ScrollBarPainter {
		/** Horizontal scroll rectangle. */
		final Rectangle hscrollRect = new Rectangle();
		/** Horizontal scroll inner rectangle. */
		final Rectangle hscrollInnerRect = new Rectangle();
		/** Horizontal scrollknob rectangle. */
		final Rectangle hscrollKnobRect = new Rectangle();
		/** Vertical scroll rectangle. */
		final Rectangle vscrollRect = new Rectangle();
		/** Vertical scroll inner rectangle. */
		final Rectangle vscrollInnerRect = new Rectangle();
		/** Vertical scrollknob rectangle. */
		final Rectangle vscrollKnobRect = new Rectangle();
		/**
		 * Paint the scrollbars to the given graphics.
		 * @param g2 the graphics
		 */
		public void paint(Graphics2D g2) {
			paintHorizontally(g2, hscrollRect, commons.starmap().hScrollLeft, commons.starmap().hScrollRight, commons.starmap().hScrollFill);
			paintVertically(g2, vscrollRect, commons.starmap().vScrollTop, commons.starmap().vScrollBottom, commons.starmap().vScrollFill);
		}
		/**
		 * Reposition the scrollbar graphics.
		 * @param rectangle the window to enclose
		 * @param saveX how many pixels to save on the horizontal scrollbar's right side
		 * @param saveY how many pixels to save on the vertical scrollbar's bottom side
		 */
		public void setBounds(Rectangle rectangle, int saveX, int saveY) {
			hscrollRect.x = rectangle.x;
			hscrollRect.y = rectangle.y + rectangle.height + 1;
			hscrollRect.width = rectangle.width - saveX;
			hscrollRect.height = commons.starmap().hScrollFill.getHeight();
			
			vscrollRect.x = rectangle.x + rectangle.width + 1;
			vscrollRect.y = rectangle.y;
			vscrollRect.width = commons.starmap().vScrollFill.getWidth();
			vscrollRect.height = rectangle.height - saveY;
			
			hscrollInnerRect.setLocation(hscrollRect.x + 2, hscrollRect.y + 2);
			hscrollInnerRect.setSize(hscrollRect.width - 4, hscrollRect.height - 4);
			vscrollInnerRect.setLocation(vscrollRect.x + 2, vscrollRect.y + 2);
			vscrollInnerRect.setSize(vscrollRect.width - 4, vscrollRect.height - 4);
		}
	}
	/**
	 * Paint a horizontal resizable area from images.
	 * @param g2 the graphics
	 * @param origin the bounding rectangle
	 * @param left the left image
	 * @param right the right image
	 * @param fill the filler between the two images
	 */
	static void paintHorizontally(Graphics2D g2, Rectangle origin, BufferedImage left, BufferedImage right, BufferedImage fill) {
		g2.drawImage(left, origin.x, origin.y, null);
		g2.drawImage(right, origin.x + origin.width - right.getWidth(), origin.y, null);
		Paint pt = g2.getPaint();
	    g2.setPaint(new TexturePaint(fill, 
	    		new Rectangle(origin.x + left.getWidth(), origin.y
	    				, fill.getWidth(), fill.getHeight())));
	    g2.fillRect(origin.x + left.getWidth(), origin.y, 
	    		origin.width - left.getWidth() - right.getWidth(), fill.getHeight());
	    g2.setPaint(pt);
	}
	/**
	 * Paint a vertically resizable area from images.
	 * @param g2 the graphics
	 * @param origin the bounding rectangle
	 * @param top the left image
	 * @param bottom the right image
	 * @param fill the filler between the two images
	 */
	static void paintVertically(Graphics2D g2, Rectangle origin, BufferedImage top, BufferedImage bottom, BufferedImage fill) {
		g2.drawImage(top, origin.x, origin.y, null);
		g2.drawImage(bottom, origin.x, origin.y + origin.height - bottom.getHeight(), null);
		Paint pt = g2.getPaint();
	    
	    g2.setPaint(new TexturePaint(fill, 
	    		new Rectangle(origin.x, origin.y  + top.getHeight()
	    				, fill.getWidth(), fill.getHeight())));
	    
	    g2.fillRect(origin.x, origin.y + top.getHeight(), 
	    		origin.width, origin.height - top.getHeight() - bottom.getHeight());
	    
	    g2.setPaint(pt);
	}
	/** The minimum zoom level. */
	private int minimumZoom = 2;
	/** The maximum zoom level. */
	private int zoomLevelCount = 14;
	/** The zoom step. */
	/** The current zoom index. The actual zoom is (minimumZoom + zoomIndex) / 4.0 . */
	private int zoomIndex = 0;
	/** The current horizontal pixel offset for the starmap contents. */
	private int xOffset;
	/** The current vertical pixel offset for the starmap contents. */
	private int yOffset;
	/** Option to hide the right panel. */
	private boolean rightPanelVisible = true;
	/** Option to hide the left panel. */
	private boolean bottomPanelVisible = true;
	/** Option to hide the minimap. */
	private boolean minimapVisible = true;
	/** Are the scrollbars visible? */
	private boolean scrollbarsVisible = true;
	/** The main starmap window's coordinates. */
	final Rectangle starmapWindow = new Rectangle();
	/** The rendering rectangle of the starmap. */
	final Rectangle starmapRect = new Rectangle();
	/** The minimap rectangle. */
	final Rectangle minimapRect = new Rectangle();
	/** The minimap's inner rectangle. */
	final Rectangle minimapInnerRect = new Rectangle();
	/** The current minimap viewport rectangle. */
	final Rectangle minimapViewportRect = new Rectangle();
	/** The minimap small image. */
	private BufferedImage minimapBackground;
	/** The current radar dot. */
	private BufferedImage radarDot;
	/** The rotation animation timer. */
	Closeable rotationTimer;
	/** The starmap clipping rectangle. */
	Rectangle starmapClip;
	/** The scrollbar painter. */
	final ScrollBarPainter scrollbarPainter;
	/** The right panel rectangle. */
	final Rectangle rightPanel = new Rectangle();
	/** The bottom panel rectangle. */
	final Rectangle bottomPanel = new Rectangle();
	/** To blink the currently selected planet on the minimap. */
	boolean minimapPlanetBlink;
	/** The blink counter. */
	int blinkCounter;
	/** The divident ratio between the planet listing and the fleet listing. */
	private double planetFleetSplitter = 0.5;
	/** The planets listing entire subpanel. */
	final Rectangle planetsListPanel = new Rectangle();
	/** The fleets listing entire subpanel. */
	final Rectangle fleetsListPanel = new Rectangle();
	/** The zooming entire subpanel. */
	final Rectangle zoomingPanel = new Rectangle();
	/** The screen selection buttons subpanel. */
	final Rectangle buttonsPanel = new Rectangle();
	/** Planet fleet splitter. */
	final Rectangle planetFleetSplitterRect = new Rectangle();
	/** The planets/fleets splitter movement range. */
	final Rectangle planetFleetSplitterRange = new Rectangle();
	/** The planets listing. */
	final Rectangle planetsList = new Rectangle();
	/** The fleets listings. */
	final Rectangle fleetsList = new Rectangle();
	/** The planet scrolled index. */
	int planetsOffset;
	/** The fleets scroled index. */
	int fleetsOffset;
	/** Button. */
	UIImageButton prevPlanet;
	/** Button. */
	UIImageButton nextPlanet;
	/** Button. */
	UIImageButton colony;
	/** Button. */
	UIImageButton prevFleet;
	/** Button. */
	UIImageButton nextFleet;
	/** Button. */
	UIImageButton equipment;
	/** Button. */
	UIImageButton info;
	/** Button. */
	UIImageButton bridge;
	/** The zoom button. */
	UIImageButton zoom;
	/** The zoom direction. */
	boolean zoomDirection;
	/** In panning mode? */
	boolean panning;
	/** Mouse down. */
	boolean mouseDown;
	/** Moving the planets fleets splitter. */
	boolean pfSplitter;
	/** Last X. */
	int lastX;
	/** Last Y. */
	int lastY;
	/** The colony name. */
	UILabel colonyName;
	/** The colony owner. */
	UILabel colonyOwner;
	/** The colony race and surface type. */
	UILabel colonySurface;
	/** The colony population and tax. */
	UILabel colonyPopulationTax;
	/** The other properties. */
	UILabel colonyOther;
	/** Problem indicator icon. */
	UIImage problemsHouse;
	/** Problem indicator icon. */
	UIImage problemsEnergy;
	/** Problem indicator icon. */
	UIImage problemsFood;
	/** Problem indicator icon. */
	UIImage problemsHospital;
	/** Problem indicator icon. */
	UIImage problemsWorker;
	/** Problem indicator icon. */
	UIImage problemsVirus;
	/** Problem indicator icon. */
	UIImage problemsStadium;
	/** Problem indicator icon. */
	UIImage problemsRepair;
	/** Problem indicator icon. */
	UIImage problemsColonyHub;
	/** Deploy satellite button. */
	UIImageButton surveySatellite;
	/** Deploy spy satellite 1 button. */
	UIImageButton spySatellite1;
	/** Deploy spy satellite 2 button. */
	UIImageButton spySatellite2;
	/** Deploy hubble. */
	UIImageButton hubble2;
	/** Show radar. */
	UIImageToggleButton showRadarButton;
	/** Show fleet. */
	UIImageToggleButton showFleetButton;
	/** Show stars. */
	UIImageToggleButton showStarsButton;
	/** Show grid. */
	UIImageToggleButton showGridButton;
	/** Show name. */
	UIImageButton showNamesNone;
	/** Show name. */
	UIImageButton showNamesPlanet;
	/** Show name. */
	UIImageButton showNamesFleet;
	/** Show name. */
	UIImageButton showNamesBoth;
	/** The fleet name. */
	UILabel fleetName;
	/** The fleet owner. */
	UILabel fleetOwner;
	/** The fleet status. */
	UILabel fleetStatus;
	/** The fleet composition. */
	UILabel fleetComposition;
	/** The fleet nearby planet. */
	UILabel fleetPlanet;
	/** The fleet speed. */
	UILabel fleetSpeed;
	/** The fleet firepower. */
	UILabel fleetFirepower;
	/** Move fleet. */
	UIImageTabButton fleetMove;
	/** Attack with the fleet. */
	UIImageTabButton fleetAttack;
	/** Stop the fleet. */
	UIImageTabButton fleetStop;
	/** Colonize with the fleet. */
	UIImageButton fleetColonize;
	/** The fleet separator image. */
	UIImage fleetSeparator;
	/** The current fleet control mode. */
	FleetMode fleetMode;
	/** Show the planet names. */
	boolean showPlanetNames = true;
	/** Show the fleet names. */
	boolean showFleetNames = true;
	/** Construct the screen. */
	public StarmapScreen() {
		scrollbarPainter = new ScrollBarPainter();
	}
	/** Given the current panel visibility settings, set the map rendering coordinates. */
	void computeRectangles() {
		starmapWindow.x = 0;
		starmapWindow.y = 20;
		starmapWindow.width = width;
		starmapWindow.height = height - 37;
		if (scrollbarsVisible) {
			starmapWindow.width -= commons.starmap().vScrollFill.getWidth();
			starmapWindow.height -= commons.starmap().hScrollFill.getHeight();
		}
		if (rightPanelVisible) {
			starmapWindow.width -= commons.starmap().panelVerticalFill.getWidth();
			if (scrollbarsVisible) {
				starmapWindow.width -= 3;
			}
		} else {
			if (scrollbarsVisible) {
				starmapWindow.width -= 1;
			}
		}
		if (bottomPanelVisible) {
			starmapWindow.height -= commons.starmap().infoFill.getHeight();
			if (scrollbarsVisible) {
				starmapWindow.height -= 3;
			}
		} else {
			if (scrollbarsVisible) {
				starmapWindow.height -= 1;
			}
		}

		minimapRect.x = width - commons.starmap().minimap.getWidth();
		minimapRect.y = height - commons.starmap().minimap.getHeight() - 17;
		minimapRect.width = commons.starmap().minimap.getWidth();
		minimapRect.height = commons.starmap().minimap.getHeight();

		int saveX = 0;
		int saveY = 0;
		if (minimapVisible) {
			if (!rightPanelVisible) {
				saveX += minimapRect.width + 1;
				if (scrollbarsVisible) {
					saveX -= commons.starmap().vScrollFill.getWidth() + 1;
				}
			} else {
				if (!scrollbarsVisible) {
					saveX += commons.starmap().vScrollFill.getWidth() + 3;
				}
			}
			if (!bottomPanelVisible) {
				saveY += minimapRect.height + 1;
				if (scrollbarsVisible) {
					saveY -= commons.starmap().hScrollFill.getHeight() + 1;
				}
			} else {
				if (!scrollbarsVisible) {
					saveY += commons.starmap().hScrollFill.getHeight() + 3;
				}
			}
		}
		rightPanel.x = getInnerWidth() - commons.starmap().panelVerticalFill.getWidth();
		rightPanel.y = starmapWindow.y;
		rightPanel.width = commons.starmap().panelVerticalFill.getWidth();
		rightPanel.height = starmapWindow.height - saveY;

		bottomPanel.x = starmapWindow.x;
		bottomPanel.y = height - 18 - commons.starmap().infoFill.getHeight();
		bottomPanel.width = starmapWindow.width - saveX;
		bottomPanel.height = commons.starmap().infoFill.getHeight();

		scrollbarPainter.setBounds(starmapWindow, saveX, saveY);
		// ..............................................................
		// the right subpanels
		buttonsPanel.width = commons.starmap().panelVerticalFill.getWidth() - 4;
		buttonsPanel.height = commons.common().infoButton[0].getHeight() + commons.common().bridgeButton[0].getHeight() + 2;
		buttonsPanel.x = rightPanel.x + 2;
		buttonsPanel.y = rightPanel.y + rightPanel.height - buttonsPanel.height;
		
		zoomingPanel.width = buttonsPanel.width;
		zoomingPanel.height = commons.starmap().zoom[0].getHeight() + 2;
		zoomingPanel.x = buttonsPanel.x;
		zoomingPanel.y = buttonsPanel.y - zoomingPanel.height - 2;

		zoom.location(zoomingPanel.x + zoomingPanel.width - zoom.width - 1, zoomingPanel.y + 1);

		planetFleetSplitterRange.x = zoomingPanel.x;
		planetFleetSplitterRange.width = zoomingPanel.width;
		planetFleetSplitterRange.y = rightPanel.y + 16 + commons.starmap().backwards[0].getHeight()
			+ commons.starmap().colony[0].getHeight();
		planetFleetSplitterRange.height = zoomingPanel.y - planetFleetSplitterRange.y
			- 16 - commons.starmap().backwards[0].getHeight() - commons.starmap().equipment[0].getHeight();
		
		planetFleetSplitterRect.x = planetFleetSplitterRange.x;
		planetFleetSplitterRect.width = planetFleetSplitterRange.width;
		planetFleetSplitterRect.height = 4;
		planetFleetSplitterRect.y = (int)(planetFleetSplitterRange.y + (planetFleetSplitter * planetFleetSplitterRange.height));

		planetsListPanel.x = zoomingPanel.x;
		planetsListPanel.y = rightPanel.y + 2;
		planetsListPanel.width = zoomingPanel.width;
		planetsListPanel.height = planetFleetSplitterRect.y - planetsListPanel.y;
		
		fleetsListPanel.x = planetsListPanel.x;
		fleetsListPanel.y = planetsListPanel.y + planetsListPanel.height + 2;
		fleetsListPanel.width = planetsListPanel.width;
		fleetsListPanel.height = zoomingPanel.y - planetFleetSplitterRect.y - 4;
		
		prevPlanet.x = planetsListPanel.x + 2;
		prevPlanet.y = planetsListPanel.y + 1;
		nextPlanet.x = planetsListPanel.x + prevPlanet.width + 4;
		nextPlanet.y = planetsListPanel.y + 1;

		prevFleet.x = fleetsListPanel.x + 2;
		prevFleet.y = fleetsListPanel.y + 1;
		nextFleet.x = fleetsListPanel.x + prevFleet.width + 4;
		nextFleet.y = fleetsListPanel.y + 1;

		colony.x = planetsListPanel.x + 1;
		colony.y = planetsListPanel.y + planetsListPanel.height - colony.height - 1;

		equipment.x = fleetsListPanel.x + 1;
		equipment.y = fleetsListPanel.y + fleetsListPanel.height - equipment.height - 1;
		
		info.x = buttonsPanel.x + 1;
		info.y = buttonsPanel.y + 1;
		bridge.x = buttonsPanel.x + 1;
		bridge.y = info.y + info.height + 1;
		
		List<Planet> planets = planets();
		if (planets.size() > 0) {
			int idx = planets.indexOf(planet());
			prevPlanet.enabled(idx > 0);
			nextPlanet.enabled(idx + 1 < planets.size());
		} else {
			prevPlanet.enabled(false);
			nextPlanet.enabled(false);
		}
		
		List<Fleet> fleets = player().ownFleets();
		if (fleets.size() > 0) {
			int idx = fleets.indexOf(fleet());
			prevFleet.enabled(idx > 0);
			nextFleet.enabled(idx + 1 < fleets.size());
		} else {
			prevFleet.enabled(false);
			nextFleet.enabled(false);
		}

		// ..............................................................

		planetsList.x = planetsListPanel.x;
		planetsList.y = planetsListPanel.y + prevPlanet.height + 1;
		planetsList.width = planetsListPanel.width;
		planetsList.height = colony.y - planetsList.y;

		fleetsList.x = fleetsListPanel.x;
		fleetsList.y = fleetsListPanel.y + prevFleet.height + 1;
		fleetsList.width = fleetsListPanel.width;
		fleetsList.height = equipment.y - fleetsList.y;

		
		
		// TODO fleet and planet listings
		// ..............................................................
		minimapRect.x = width - commons.starmap().minimap.getWidth();
		minimapRect.y = height - commons.starmap().minimap.getHeight() - 17;
		minimapRect.width = commons.starmap().minimap.getWidth();
		minimapRect.height = commons.starmap().minimap.getHeight();
//		if ((!rightPanelVisible || ! bottomPanelVisible)) {
//			minimapRect.x = starmapWindow.x + starmapWindow.width - gfx.minimap.getWidth();
//			minimapRect.y = starmapWindow.y + starmapWindow.height - gfx.minimap.getHeight();
//		}
		
		minimapInnerRect.setBounds(minimapRect);
		minimapInnerRect.x += 2;
		minimapInnerRect.y += 2;
		minimapInnerRect.width -= 4;
		minimapInnerRect.height -= 3;
		// ..............................................................

		colonyName.location(bottomPanel.x + 10, bottomPanel.y + 6);
		colonyOwner.location(bottomPanel.x + 10, bottomPanel.y + 26);
		colonySurface.location(bottomPanel.x + 10, bottomPanel.y + 40);
		colonyPopulationTax.location(bottomPanel.x + 10, bottomPanel.y + 54);
		colonyOther.location(bottomPanel.x + 10, bottomPanel.y + 70);

		int probcnt = 8;
		problemsHouse.location(bottomPanel.x + bottomPanel.width - 215 - 11 * (probcnt--), bottomPanel.y + 70);
		problemsEnergy.location(bottomPanel.x + bottomPanel.width - 215 - 11 * (probcnt--), bottomPanel.y + 70);
		problemsWorker.location(bottomPanel.x + bottomPanel.width - 215 - 11 * (probcnt--), bottomPanel.y + 70);
		problemsFood.location(bottomPanel.x + bottomPanel.width - 215 - 11 * (probcnt--), bottomPanel.y + 70);
		problemsHospital.location(bottomPanel.x + bottomPanel.width - 215 - 11 * (probcnt--), bottomPanel.y + 70);
		problemsVirus.location(bottomPanel.x + bottomPanel.width - 215 - 11 * (probcnt--), bottomPanel.y + 70);
		problemsStadium.location(bottomPanel.x + bottomPanel.width - 215 - 11 * (probcnt--), bottomPanel.y + 70);
		problemsRepair.location(bottomPanel.x + bottomPanel.width - 215 - 11 * (probcnt--), bottomPanel.y + 70);
		problemsColonyHub.location(bottomPanel.x + bottomPanel.width - 215 - 11 * (probcnt--), bottomPanel.y + 70);

		surveySatellite.location(bottomPanel.x + bottomPanel.width - 199, bottomPanel.y + 4);
		spySatellite1.location(bottomPanel.x + bottomPanel.width - 199, bottomPanel.y + 24);
		spySatellite2.location(bottomPanel.x + bottomPanel.width - 199, bottomPanel.y + 44);
		hubble2.location(bottomPanel.x + bottomPanel.width - 199, bottomPanel.y + 64);

		showRadarButton.location(bottomPanel.x + bottomPanel.width - 110, bottomPanel.y + 25);
		showFleetButton.location(bottomPanel.x + bottomPanel.width - 55, bottomPanel.y + 25);
		showStarsButton.location(bottomPanel.x + bottomPanel.width - 110, bottomPanel.y + 45);
		showGridButton.location(bottomPanel.x + bottomPanel.width - 55, bottomPanel.y + 45);

		showNamesNone.location(bottomPanel.x + bottomPanel.width - 110, bottomPanel.y + 65);
		showNamesPlanet.location(bottomPanel.x + bottomPanel.width - 110, bottomPanel.y + 65);
		showNamesFleet.location(bottomPanel.x + bottomPanel.width - 110, bottomPanel.y + 65);
		showNamesBoth.location(bottomPanel.x + bottomPanel.width - 110, bottomPanel.y + 65);

		
		fleetName.location(colonyName.location());
		fleetOwner.location(colonyOwner.location());
		fleetStatus.location(colonySurface.location());
		fleetPlanet.location(colonyPopulationTax.location());
		
		fleetComposition.location(fleetPlanet.x, fleetPlanet.y + 16);
		int ff = Math.max(fleetSpeed.width, fleetFirepower.width);
		
		int fleetcmd = 214;
		
		fleetSpeed.location(bottomPanel.x + bottomPanel.width - fleetcmd - ff - 10, fleetOwner.y);
		fleetFirepower.location(bottomPanel.x + bottomPanel.width - fleetcmd - ff - 10, fleetSpeed.y + 10);
		
		fleetMove.location(bottomPanel.x + bottomPanel.width - fleetcmd, bottomPanel.y + 5);
		fleetAttack.location(bottomPanel.x + bottomPanel.width - fleetcmd, bottomPanel.y + 32);
		fleetStop.location(bottomPanel.x + bottomPanel.width - fleetcmd, bottomPanel.y + 59);
		
		fleetSeparator.location(bottomPanel.x + bottomPanel.width - fleetcmd - 4, bottomPanel.y + 2);

		fleetColonize.location(bottomPanel.x + bottomPanel.width - 110, bottomPanel.y + 4);

		
		computeViewport();
	}

	/**
	 * Compute the current viewport's coordinates.
	 */
	private void computeViewport() {
		double zoom = getZoom();

		starmapRect.width = (int)(commons.starmap().background.getWidth() * zoom);
		starmapRect.height = (int)(commons.starmap().background.getHeight() * zoom);
		
		if (starmapRect.width < starmapWindow.width) {
			xOffset = -(starmapWindow.width - starmapRect.width) / 2;
		}
		if (starmapRect.height < starmapWindow.height) {
			yOffset = -(starmapWindow.height - starmapRect.height) / 2;
		}
		starmapRect.x = starmapWindow.x - xOffset;
		starmapRect.y = starmapWindow.y - yOffset;
		
		// center if smaller
		limitOffsets();
		// ..............................................................
		int miniw = minimapInnerRect.width;
		int minih = minimapInnerRect.height;
		
		int x2 = 0;
		if (xOffset < 0) {
			minimapViewportRect.x = 0;
			x2 = miniw;
		}  else {
			minimapViewportRect.x = miniw * xOffset / starmapRect.width;
			x2 = miniw * (xOffset + starmapWindow.width) / starmapRect.width;
		}
		int y2 = 0;
		if (yOffset < 0) {
			minimapViewportRect.y = 0;
			y2 = minih;
		} else {
			minimapViewportRect.y = minih * yOffset / starmapRect.height;
			y2 = minih * (yOffset + starmapWindow.height) / starmapRect.height;
		}
		
		if (x2 > miniw) {
			x2 = miniw;
		}
		if (y2 > minih) {
			y2 = minih;
		}
		
		
		minimapViewportRect.width = x2 - minimapViewportRect.x;
		minimapViewportRect.height = y2 - minimapViewportRect.y;
		minimapViewportRect.x += minimapRect.x + 2;
		minimapViewportRect.y += minimapRect.y + 2;
		
		starmapClip = starmapWindow.intersection(starmapRect);
	}

	/**
	 * Display the colony problem icons for the own planets.
	 * @param p the planet
	 */
	void displayColonyProblems(Planet p) {
		problemsHouse.visible(false);
		problemsEnergy.visible(false);
		problemsWorker.visible(false);
		problemsFood.visible(false);
		problemsHospital.visible(false);
		problemsVirus.visible(false);
		problemsStadium.visible(false);
		problemsRepair.visible(false);
		problemsColonyHub.visible(false);
		if (p.owner == player()) {
			PlanetStatistics ps = p.getStatistics();
			if (ps.hasProblem(PlanetProblems.HOUSING)) {
				problemsHouse.image(commons.common().houseIcon).visible(true);
			} else
			if (ps.hasWarning(PlanetProblems.HOUSING)) {
				problemsHouse.image(commons.common().houseIconDark).visible(true);
			}
			
			if (ps.hasProblem(PlanetProblems.ENERGY)) {
				problemsEnergy.image(commons.common().energyIcon).visible(true);
			} else
			if (ps.hasWarning(PlanetProblems.ENERGY)) {
				problemsEnergy.image(commons.common().energyIconDark).visible(true);
			}
			
			if (ps.hasProblem(PlanetProblems.WORKFORCE)) {
				problemsWorker.image(commons.common().workerIcon).visible(true);
			} else
			if (ps.hasWarning(PlanetProblems.WORKFORCE)) {
				problemsWorker.image(commons.common().workerIconDark).visible(true);
			}
			
			if (ps.hasProblem(PlanetProblems.FOOD)) {
				problemsFood.image(commons.common().foodIcon).visible(true);
			} else
			if (ps.hasWarning(PlanetProblems.FOOD)) {
				problemsFood.image(commons.common().foodIconDark).visible(true);
			}
			
			if (ps.hasProblem(PlanetProblems.HOSPITAL)) {
				problemsHospital.image(commons.common().hospitalIcon).visible(true);
			} else
			if (ps.hasWarning(PlanetProblems.HOSPITAL)) {
				problemsHospital.image(commons.common().hospitalIconDark).visible(true);
			}

			if (ps.hasProblem(PlanetProblems.VIRUS)) {
				problemsVirus.image(commons.common().virusIcon).visible(true);
			} else
			if (ps.hasWarning(PlanetProblems.VIRUS)) {
				problemsVirus.image(commons.common().virusIconDark).visible(true);
			}

			if (ps.hasProblem(PlanetProblems.STADIUM)) {
				problemsStadium.image(commons.common().stadiumIcon).visible(true);
			} else
			if (ps.hasWarning(PlanetProblems.STADIUM)) {
				problemsStadium.image(commons.common().stadiumIconDark).visible(true);
			}

			if (ps.hasProblem(PlanetProblems.REPAIR)) {
				problemsRepair.image(commons.common().repairIcon).visible(true);
			} else
			if (ps.hasWarning(PlanetProblems.REPAIR)) {
				problemsRepair.image(commons.common().repairIconDark).visible(true);
			}

			if (ps.hasProblem(PlanetProblems.COLONY_HUB)) {
				problemsColonyHub.image(commons.common().colonyHubIcon).visible(true);
			} else
			if (ps.hasWarning(PlanetProblems.COLONY_HUB)) {
				problemsColonyHub.image(commons.common().colonyHubIconDark).visible(true);
			}
		}
	}
	/** Display the current fleet info. */
	void displayFleetInfo() {
		
		Fleet f = fleet();
		boolean fleetMode = f != null && player().selectionMode == SelectionMode.FLEET;

		fleetName.visible(fleetMode);
		fleetOwner.visible(fleetMode);
		fleetStatus.visible(fleetMode);
		fleetPlanet.visible(fleetMode);
		fleetComposition.visible(fleetMode && knowledge(f, FleetKnowledge.COMPOSITION) >= 0);
		fleetAttack.visible(fleetMode && f.owner == player());
		fleetStop.visible(fleetMode && f.owner == player());
		fleetMove.visible(fleetMode && f.owner == player());
		fleetSeparator.visible(fleetMode && f.owner == player());

		fleetColonize.visible(false);
		
		fleetFirepower.visible(fleetMode && knowledge(f, FleetKnowledge.FULL) >= 0);
		fleetSpeed.visible(fleetMode);
		
		if (!fleetMode) {
			return;
		}
		fleetName.color(f.owner.color);
		
		if (this.fleetMode == null) {
			fleetAttack.down = f.mode == FleetMode.ATTACK;
			fleetMove.down = f.mode == FleetMode.MOVE;
			fleetStop.down = f.mode == null;
		}
		
		FleetStatistics fs = f.getStatistics();
		
		fleetColonize.visible(
				fleetMode && f.owner == player()
				&& fs.planet != null && fs.planet.owner == null
				&& f.inventoryCount(world().researches.get("ColonyShip")) > 0
		);

		
		
		fleetName.text(f.name, true);
		fleetOwner.text(f.owner.name, true);
		
		if (knowledge(f, FleetKnowledge.FULL) >= 0) {
			fleetFirepower.text(format("fleetstatus.firepower", fs.firepower), true).visible(true);
			fleetComposition.text(format("fleetstatus.composition",
					zeroDash(fs.battleshipCount),
					zeroDash(fs.cruiserCount),
					zeroDash(fs.fighterCount),
					zeroDash(fs.vehicleCount)
			), true).visible(true);
		} else	
		if (knowledge(f, FleetKnowledge.COMPOSITION) >= 0) {
			fleetFirepower.visible(false);
			fleetComposition.text(format("fleetstatus.composition",
					((fs.battleshipCount / 10) * 10) + ".." + ((fs.battleshipCount  / 10 + 1) * 10),
					((fs.cruiserCount / 10) * 10) + ".." + ((fs.cruiserCount  / 10 + 1) * 10),
					((fs.fighterCount / 10) * 10) + ".." + ((fs.fighterCount  / 10 + 1) * 10),
					((fs.vehicleCount / 10) * 10) + ".." + ((fs.vehicleCount  / 10 + 1) * 10)
			), true).visible(true);
		} else {
			fleetFirepower.visible(false);
			fleetComposition.visible(false);
		}

		fleetComposition.text(format("fleetstatus.composition", fs.battleshipCount, fs.cruiserCount, fs.fighterCount, fs.vehicleCount), true);
		fleetSpeed.text(format("fleetstatus.speed", fs.speed), true);
		if (fs.planet != null) {
			fleetPlanet.text(format("fleetstatus.nearby", fs.planet.name), true);
		} else {
			fleetPlanet.text(format("fleetstatus.nearby", "----"), true);
		}
		if (f.targetFleet == null && f.targetPlanet == null) {
			if (f.waypoints.size() > 0) {
				fleetStatus.text(format("fleetstatus.moving"), true);
			} else {
				fleetStatus.text(format("fleetstatus.stopped"), true);
			}
		} else {
			if (f.mode == FleetMode.ATTACK) {
				if (f.targetFleet != null) {
					fleetStatus.text(format("fleetstatus.attack", f.targetFleet.name), true);
				} else {
					fleetStatus.text(format("fleetstatus.attack", f.targetPlanet.name), true);
				}
			} else {
				if (f.targetFleet != null) {
					fleetStatus.text(format("fleetstatus.moving.after", f.targetFleet.name), true);
				} else {
					fleetStatus.text(format("fleetstatus.moving.to", f.targetPlanet.name), true);
				}
			}
		}
	}
	/**
	 * Replace the value with a dash if the value is zero.
	 * @param i the value
	 * @return a dash or the value as string
	 */
	String zeroDash(int i) {
		if (i == 0) {
			return "-";
		}
		return Integer.toString(i);
	}
	/** Display the right panel's planet info on the current selected planet. */
	void displayPlanetInfo() {
		if (player().selectionMode != SelectionMode.PLANET) {
			colonyName.visible(false);
			colonyOwner.visible(false);
			colonySurface.visible(false);
			colonyPopulationTax.visible(false);
			colonyOther.visible(false);

			problemsHouse.visible(false);
			problemsEnergy.visible(false);
			problemsWorker.visible(false);
			problemsFood.visible(false);
			problemsHospital.visible(false);
			problemsVirus.visible(false);
			problemsStadium.visible(false);
			problemsRepair.visible(false);
			problemsColonyHub.visible(false);
// FIXME
			surveySatellite.visible(false);
			spySatellite1.visible(false);
			spySatellite2.visible(false);
			hubble2.visible(false);
			
			return;
		} 
		
		Planet p = planet();

		surveySatellite.visible(
				p.owner != player()
				&& (knowledge(p, PlanetKnowledge.NAME) <= 0 || !p.isPopulated())
				&& player().inventoryCount(world().researches.get("Satellite")) > 0
				&& !p.hasInventory(world().researches.get("Satellite"), player())
		);
		spySatellite1.visible(
				p.owner != player() 
				&& p.owner != null
				&& knowledge(p, PlanetKnowledge.OWNER) >= 0
				&& player().inventoryCount(world().researches.get("SpySatellite1")) > 0
				&& !p.hasInventory(world().researches.get("SpySatellite1"), player())
		);
		spySatellite2.visible(
				p.owner != player()
				&& p.owner != null
				&& knowledge(p, PlanetKnowledge.OWNER) >= 0
				&& player().inventoryCount(world().researches.get("SpySatellite2")) > 0
				&& !p.hasInventory(world().researches.get("SpySatellite2"), player())
		);
		hubble2.visible(
				p.owner == player()
				&& player().inventoryCount(world().researches.get("Hubble2")) > 0
				&& !p.hasInventory(world().researches.get("Hubble2"), player())
		);

		
		if (p.owner == player() || knowledge(p, PlanetKnowledge.NAME) >= 0) {
			colonyName.text(p.name, true);
			int c = TextRenderer.GRAY;
			if (p.owner == player() || knowledge(p, PlanetKnowledge.OWNER) >= 0 && p.isPopulated()) {
				c = p.owner.color;
			}
			colonyName.color(c);
			colonyName.visible(true);
		} else {
			colonyName.visible(false);
		}
		
		if (p.owner == player() || (knowledge(p, PlanetKnowledge.OWNER) >= 0)) {
			colonyOwner.text(p.owner != null ? p.owner.name : "", true);
			colonyOwner.visible(true);
		} else {
			colonyOwner.visible(false);
		}
		if (p.owner == player() || (knowledge(p, PlanetKnowledge.OWNER) >= 0) && p.isPopulated()) {
			colonySurface.text(
					get(p.getRaceLabel()) + ", "
					+ format("buildinginfo.planet.surface", get(p.type.label))
			, true).visible(true);
		} else {
			colonySurface.text(
					format("buildinginfo.planet.surface", get(p.type.label))
			, true).visible(true);
		}
		
		if (p.owner == player()) {
			colonyName.text(p.name, true);
			
			colonyPopulationTax.text(
					format("colonyinfo.population.own", 
							p.population, get(p.getRaceLabel()), get(p.getMoraleLabel())
					) + "  "
					+ format("colonyinfo.tax_short", get(p.getTaxLabel()))
			, true).visible(true);
		} else {
			if (knowledge(p, PlanetKnowledge.BUILDING) >= 0) {
				if (p.isPopulated()) {
					colonyPopulationTax.text(format("colonyinfo.population.short.alien", 
							p.population
					), true).visible(true);
				} else {
					colonyPopulationTax.visible(false);
				}
			} else {
				colonyPopulationTax.visible(false);
			}
		}
		colonyOther.text(world().getOtherItems(), true).visible(true);
		
		displayColonyProblems(p);
	}
	/**
	 * Zoom in and keep try to keep the given pixel under the mouse.
	 * @param x the X coordinate within the starmapWindow
	 * @param y the Y coordinate within the starmapWindow
	 */
	public void doZoomIn(int x, int y) {
		if (zoomIndex < zoomLevelCount) {
			readjustZoom(x, y, zoomIndex + 1);
		}
	}

	/**
	 * Zoom out and keep try to keep the given pixel under the mouse.
	 * @param x the X coordinate within the starmapWindow
	 * @param y the Y coordinate within the starmapWindow
	 */
	public void doZoomOut(int x, int y) {
		if (zoomIndex > 0) {
			readjustZoom(x, y, zoomIndex - 1);
		}
	}

	@Override
	public void draw(Graphics2D g2) {

		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, width, height);
		
		displayPlanetInfo();
		displayFleetInfo();

		computeRectangles();
		
		
		Shape save0 = g2.getClip();
		
		g2.clipRect(starmapClip.x, starmapClip.y, starmapClip.width, starmapClip.height);
		g2.drawImage(commons.starmap().background, starmapRect.x, starmapRect.y, starmapRect.width, starmapRect.height, null);
		
		double zoom = getZoom();
		
		if (showStarsButton.selected) {
			RenderTools.paintStars(g2, starmapRect, starmapClip, starmapClip, zoomIndex, zoomLevelCount);
		}
		
		if (showGridButton.selected) {
			RenderTools.paintGrid(g2, starmapRect, commons.starmap().gridColor, commons.text());
		}
		
		Collection<Fleet> fleets = player().visibleFleets();
		
		// render radar circles
		if (showRadarButton.selected) {
			for (Planet p : planets()) {
				p.getStatistics();
				if (p.radar > 0) {
					paintRadar(g2, p.x, p.y, p.radar, zoom);
				}
			}
			if (showFleetButton.selected) {
				for (Fleet f : fleets) {
					if (f.owner == player()) {
						f.getStatistics();
						paintRadar(g2, (int)f.x, (int)f.y, f.radar * 35, zoom);
					}
				}
			}
		}

		for (Planet p : commons.world().planets.values()) {
			if (knowledge(p, PlanetKnowledge.VISIBLE) < 0) {
				continue;
			}
			BufferedImage phase = p.type.body[p.rotationPhase];
			double d = p.diameter * zoom / 4;
			int di = (int)d;
			int x0 = (int)(starmapRect.x + p.x * zoom - d / 2);
			int y0 = (int)(starmapRect.y + p.y * zoom - d / 2);
			g2.drawImage(phase, x0, y0, (int)d, (int)d, null);
			
			int tw = commons.text().getTextWidth(5, p.name);
			int xt = (int)(starmapRect.x + p.x * zoom - tw / 2);
			int yt = (int)(starmapRect.y + p.y * zoom + d / 2) + 4;
			int labelColor = TextRenderer.GRAY;
			if (p.owner != null && knowledge(p, PlanetKnowledge.OWNER) >= 0) {
				labelColor = p.owner.color;
			}
			if (showPlanetNames && knowledge(p, PlanetKnowledge.NAME) >= 0) {
				commons.text().paintTo(g2, xt, yt, 5, labelColor, p.name);
			}
			if (p == planet()) {
				if (player().selectionMode == SelectionMode.PLANET) {
					g2.setColor(Color.WHITE);
				} else {
					g2.setColor(Color.GRAY);
				}
				g2.drawLine(x0 - 1, y0 - 1, x0 + 2, y0 - 1);
				g2.drawLine(x0 - 1, y0 + di + 1, x0 + 2, y0 + di + 1);
				g2.drawLine(x0 + di - 2, y0 - 1, x0 + di + 1, y0 - 1);
				g2.drawLine(x0 + di - 2, y0 + di + 1, x0 + di + 1, y0 + di + 1);
				
				g2.drawLine(x0 - 1, y0 - 1, x0 - 1, y0 + 2);
				g2.drawLine(x0 + di + 1, y0 - 1, x0 + di + 1, y0 + 2);
				g2.drawLine(x0 - 1, y0 + di - 2, x0 - 1, y0 + di + 1);
				g2.drawLine(x0 + di + 1, y0 + di - 2, x0 + di + 1, y0 + di + 1);
			}
			if (p.quarantine && minimapPlanetBlink) {
				g2.setColor(Color.RED);
				g2.drawRect(x0 - 1, y0 - 1, 2 + (int)d, 2 + (int)d);
			}
			if (p.owner == player()) {
				PlanetStatistics ps = p.getStatistics();
				
				Set<PlanetProblems> combined = new HashSet<PlanetProblems>();
				combined.addAll(ps.problems.keySet());
				combined.addAll(ps.warnings.keySet());
				
				if (combined.size() > 0) {
					int w = combined.size() * 11 - 1;
					int i = 0;
					for (PlanetProblems pp : combined) {
						BufferedImage icon = null;
						BufferedImage iconDark = null;
						switch (pp) {
						case HOUSING:
							icon = commons.common().houseIcon;
							iconDark = commons.common().houseIconDark;
							break;
						case FOOD:
							icon = commons.common().foodIcon;
							iconDark = commons.common().foodIconDark;
							break;
						case HOSPITAL:
							icon = commons.common().hospitalIcon;
							iconDark = commons.common().hospitalIconDark;
							break;
						case ENERGY:
							icon = commons.common().energyIcon;
							iconDark = commons.common().energyIconDark;
							break;
						case WORKFORCE:
							icon = commons.common().workerIcon;
							iconDark = commons.common().workerIconDark;
							break;
						case STADIUM:
							icon = commons.common().stadiumIcon;
							iconDark = commons.common().stadiumIconDark;
							break;
						case VIRUS:
							icon = commons.common().virusIcon;
							iconDark = commons.common().virusIconDark;
							break;
						case REPAIR:
							icon = commons.common().repairIcon;
							iconDark = commons.common().repairIconDark;
							break;
						case COLONY_HUB:
							icon = commons.common().colonyHubIcon;
							iconDark = commons.common().colonyHubIconDark;
							break;
						default:
						}
						if (ps.hasProblem(pp)) {
							g2.drawImage(icon, (int)(starmapRect.x + p.x * zoom - w / 2 + i * 11), y0 - 13, null);
						} else
						if (ps.hasWarning(pp)) {
							g2.drawImage(iconDark, (int)(starmapRect.x + p.x * zoom - w / 2 + i * 11), y0 - 13, null);						
						}
						i++;
					}
				}
			}
		}
		if (showFleetButton.selected) {
			for (Fleet f : fleets) {
				if (f.owner == player()) {
					if (f.mode == FleetMode.ATTACK) {
						if (f.targetFleet != null) {
							g2.setColor(new Color(255, 0, 0, 128));
							g2.drawLine(
									(int)(starmapRect.x + f.x * zoom), 
									(int)(starmapRect.y + f.y * zoom), 
									(int)(starmapRect.x + f.targetFleet.x * zoom), 
									(int)(starmapRect.y + f.targetFleet.y * zoom));
						} else
						if (f.targetPlanet != null) {
							g2.setColor(new Color(255, 0, 0, 128));
							g2.drawLine(
									(int)(starmapRect.x + f.x * zoom), 
									(int)(starmapRect.y + f.y * zoom), 
									(int)(starmapRect.x + f.targetPlanet.x * zoom), 
									(int)(starmapRect.y + f.targetPlanet.y * zoom));
						}
					} else {
						float lastx = f.x;
						float lasty = f.y;
						if (f.targetFleet != null) {
							g2.setColor(new Color(255, 255, 255, 128));
							g2.drawLine(
									(int)(starmapRect.x + f.x * zoom), 
									(int)(starmapRect.y + f.y * zoom), 
									(int)(starmapRect.x + f.targetFleet.x * zoom), 
									(int)(starmapRect.y + f.targetFleet.y * zoom));
						} else
						if (f.targetPlanet != null) {
							g2.setColor(new Color(255, 255, 255, 128));
							g2.drawLine(
									(int)(starmapRect.x + f.x * zoom), 
									(int)(starmapRect.y + f.y * zoom), 
									(int)(starmapRect.x + f.targetPlanet.x * zoom), 
									(int)(starmapRect.y + f.targetPlanet.y * zoom));
						} else 
						if (f.waypoints.size() > 0) {
							g2.setColor(new Color(255, 255, 255, 128));
							for (Point2D.Float pt : f.waypoints) {
								g2.drawLine(
									(int)(starmapRect.x + lastx * zoom), 
									(int)(starmapRect.y + lasty * zoom), 
									(int)(starmapRect.x + pt.x * zoom), 
									(int)(starmapRect.y + pt.y * zoom));
								lastx = pt.x;
								lasty = pt.y;
							}
						}
					}
				}
				int x0 = (int)(starmapRect.x + f.x * zoom - f.owner.fleetIcon.getWidth() / 2);
				int y0 = (int)(starmapRect.y + f.y * zoom - f.owner.fleetIcon.getHeight() / 2);
				g2.drawImage(f.owner.fleetIcon, x0, y0, null);
				int tw = commons.text().getTextWidth(5, f.name);
				int xt = (int)(starmapRect.x + f.x * zoom - tw / 2);
				int yt = (int)(starmapRect.y + f.y * zoom + f.owner.fleetIcon.getHeight() / 2) + 3;
				if (showFleetNames) {
					commons.text().paintTo(g2, xt, yt, 5, f.owner.color, f.name);
				}
				if (f == fleet()) {
					if (player().selectionMode == SelectionMode.FLEET) {
						g2.setColor(Color.WHITE);
					} else {
						g2.setColor(Color.GRAY);
					}
					g2.drawRect(x0 - 1, y0 - 1, f.owner.fleetIcon.getWidth() + 2, f.owner.fleetIcon.getHeight() + 2);
				}
			}
		}
		
		g2.setClip(save0);

		// TODO panel rendering
		
		if (rightPanelVisible) {
			paintVertically(g2, rightPanel, commons.starmap().panelVerticalTop, commons.starmap().panelVerticalFill, commons.starmap().panelVerticalFill);
			
//			g2.setColor(Color.GRAY);
//			g2.fill(planetsListPanel);
//			g2.setColor(Color.LIGHT_GRAY);
//			g2.fill(fleetsListPanel);
////			g2.setColor(Color.YELLOW);
////			g2.fill(planetFleetSplitterRange);
			
			g2.drawImage(commons.starmap().panelVerticalSeparator, planetFleetSplitterRect.x, planetFleetSplitterRect.y, null);
			g2.drawImage(commons.starmap().panelVerticalSeparator, zoomingPanel.x, zoomingPanel.y - 2, null);
			g2.drawImage(commons.starmap().panelVerticalSeparator, buttonsPanel.x, buttonsPanel.y - 2, null);
			
			g2.setClip(save0);
			g2.clipRect(planetsList.x, planetsList.y, planetsList.width, planetsList.height);
			List<Planet> planets = planets();
			for (int i = planetsOffset; i < planets.size(); i++) {
				Planet p = planets.get(i);
				int color = TextRenderer.GREEN;
				if (p == planet()) {
					color = TextRenderer.RED;
				}
				commons.text().paintTo(g2, planetsList.x + 3, planetsList.y + (i - planetsOffset) * 10 + 2, 7, color, p.name);
			}
			g2.setClip(save0);
			g2.clipRect(fleetsList.x, fleetsList.y, fleetsList.width, fleetsList.height);
			List<Fleet> playersFleet = player().ownFleets();
			for (int i = fleetsOffset; i < playersFleet.size(); i++) {
				Fleet f = playersFleet.get(i);
				int color = TextRenderer.GREEN;
				if (f == fleet()) {
					color = TextRenderer.RED;
				}
				commons.text().paintTo(g2, fleetsList.x + 3, fleetsList.y + (i - fleetsOffset) * 10 + 2, 7, color, f.name);
			}
			
			g2.setClip(save0);
		}
		if (bottomPanelVisible) {
			paintHorizontally(g2, bottomPanel, commons.starmap().infoLeft, commons.starmap().infoRight, commons.starmap().infoFill);
		}
		
		if (scrollbarsVisible) {
			scrollbarPainter.paint(g2);
		}
		
		if (minimapVisible) {
			g2.drawImage(commons.starmap().minimap, minimapRect.x, minimapRect.y, null);
			g2.drawImage(minimapBackground, minimapInnerRect.x, minimapInnerRect.y, null);
			g2.setColor(Color.WHITE);
			g2.drawImage(world().galaxyModel.map, minimapInnerRect.x, minimapInnerRect.y, minimapInnerRect.width, minimapInnerRect.height, null);
			g2.drawRect(minimapViewportRect.x, minimapViewportRect.y, minimapViewportRect.width - 1, minimapViewportRect.height - 1);
			g2.setClip(save0);
			g2.clipRect(minimapInnerRect.x, minimapInnerRect.y, minimapInnerRect.width, minimapInnerRect.height);
			// render planets
			for (Planet p : commons.world().planets.values()) {
				if (knowledge(p, PlanetKnowledge.VISIBLE) < 0) {
					continue;
				}
				if (p != planet() || minimapPlanetBlink) {
					int x0 = minimapInnerRect.x + (p.x * minimapInnerRect.width / commons.starmap().background.getWidth());
					int y0 = minimapInnerRect.y + (p.y * minimapInnerRect.height / commons.starmap().background.getHeight());
					int labelColor = TextRenderer.GRAY;
					if (p.owner != null && knowledge(p, PlanetKnowledge.OWNER) >= 0) {
						labelColor = p.owner.color;
					}
					g2.setColor(new Color(labelColor));
					g2.fillRect(x0 - 1, y0 - 1, 3, 3);
				}
			}
		}
		g2.setClip(save0);
		
		
		super.draw(g2);
	}
	/** 
	 * Get a planet at the given absolute location. 
	 * @param x the absolute x
	 * @param y the absolute y
	 * @return a planet or null if not found
	 */
	public Fleet getFleetAt(int x, int y) {
		double zoom = getZoom();
		for (Fleet f : player().visibleFleets()) {
			int w = f.owner.fleetIcon.getWidth();
			int h = f.owner.fleetIcon.getHeight();
			int x0 = (int)(starmapRect.x + f.x * zoom - w * 0.5);
			int y0 = (int)(starmapRect.y + f.y * zoom - h * 0.5);
			if (x0 <= x && x <= x0 + w && y0 <= y && y <= y0 + h) {
				return f;
			}
		}
		return null;
	}
	/** 
	 * Get a planet at the given absolute location. 
	 * @param x the absolute x
	 * @param y the absolute y
	 * @return a planet or null if not found
	 */
	public Planet getPlanetAt(int x, int y) {
		double zoom = getZoom();
		for (Planet p : commons.world().planets.values()) {
			double d = p.diameter * zoom / 4;
			int di = (int)d;
			int x0 = (int)(starmapRect.x + p.x * zoom - d / 2);
			int y0 = (int)(starmapRect.y + p.y * zoom - d / 2);
			if (x0 <= x && x <= x0 + di && y0 <= y && y <= y0 + di) {
				return p;
			}
		}
		return null;
	}
	/**
	 * Convert the screen coordinates to map coordinates.
	 * @param x the screen X coordinate
	 * @param y the screen Y coordinate
	 * @return the map point
	 */
	public Point2D.Float toMapCoordinates(int x, int y) {
		float zoom = (float)getZoom();
		float nx = (x - starmapRect.x) / zoom;
		float ny = (y - starmapRect.y) / zoom;
		return new Point2D.Float(nx, ny);
	}
	/**
	 * @return computes the current zoom factor.
	 */
	public double getZoom() {
		return (minimumZoom + zoomIndex) / 4.0;
	}
	/**
	 * @return the bottomPanelVisible
	 */
	public boolean isBottomPanelVisible() {
		return bottomPanelVisible;
	}
	/**
	 * @return the minimapVisible
	 */
	public boolean isMinimapVisible() {
		return minimapVisible;
	}
	/**
	 * @return the rightPanelVisible
	 */
	public boolean isRightPanelVisible() {
		return rightPanelVisible;
	}
	/**
	 * @return the scrollbarsVisible
	 */
	public boolean isScrollbarsVisible() {
		return scrollbarsVisible;
	}
	@Override
	public boolean keyboard(KeyEvent e) {
		boolean rep = false;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			pan(0, -30);
			rep = true;
			break;
		case KeyEvent.VK_DOWN:
			pan(0, 30);
			rep = true;
			break;
		case KeyEvent.VK_LEFT:
			pan(-30, 0);
			rep = true;
			break;
		case KeyEvent.VK_RIGHT:
			pan(30, 0);
			rep = true;
			break;
		default:
		}
		return rep;
	}
	/** Limit the offsets how far they can go. */
	public void limitOffsets() {
		int maxX = starmapRect.width - starmapWindow.width;
		int maxY = starmapRect.height - starmapWindow.height;
		if (xOffset > maxX) {
			xOffset = maxX;
		}
		if (xOffset < 0) {
			xOffset = 0;
		}
		if (yOffset > maxY) {
			yOffset = maxY;
		}
		if (yOffset < 0) {
			yOffset = 0;
		}
	}
	/**
	 * Limit the scroll box offset value based on the size of the box and rowheight.
	 * @param offset the current offset
	 * @param count the total item count
	 * @param height the box height
	 * @param rowHeight the rowheight
	 * @return the corrected offset
	 */
	int limitScrollBox(int offset, int count, int height, int rowHeight) {
		int visibleRows = height / rowHeight;
		if (count <= visibleRows || offset <= 0) {
			return 0;
		}
		if (offset > count - visibleRows) {
			return count - visibleRows;
		}
		return offset; 
	}
	@Override
	public boolean mouse(UIMouse e) {
		boolean rep = false;
		switch (e.type) {
		case MOVE:
		case DRAG:
			if (panning || (e.has(Button.RIGHT) && e.has(Type.DRAG))) {
				if (starmapWindow.contains(e.x, e.y)) {
					if (!panning) {
						lastX = e.x;
						lastY = e.y;
						panning = true;
					}
					int dx = e.x - lastX;
					int dy = e.y - lastY;
					
					pan(dx, dy);
					lastX = e.x;
					lastY = e.y;
					rep = true;
				}
			}
			if (mouseDown) {
				if (e.has(Button.LEFT) && minimapVisible && minimapInnerRect.contains(e.x, e.y)) {
					scrollMinimapTo(e.x - minimapInnerRect.x, e.y - minimapInnerRect.y);
					rep = true;
				}
				if (e.has(Button.LEFT) && pfSplitter && planetFleetSplitterRange.contains(e.x, e.y)) {
					planetFleetSplitter = 1.0 * (e.y - planetFleetSplitterRange.y) / (planetFleetSplitterRange.height);
					fleetsOffset = limitScrollBox(fleetsOffset, player().ownFleets().size(), fleetsList.height, 10);
					planetsOffset = limitScrollBox(planetsOffset, planets().size(), planetsList.height, 10);
					rep = true;
				}
			}
			break;
		case DOWN:
			if (e.has(Button.RIGHT)) {
				panning = true;
				lastX = e.x;
				lastY = e.y;
			}
			mouseDown = true;
			if (e.has(Button.LEFT) && minimapVisible && minimapInnerRect.contains(e.x, e.y)) {
				scrollMinimapTo(e.x - minimapInnerRect.x, e.y - minimapInnerRect.y);
				rep = true;
			}
			if (starmapWindow.contains(e.x, e.y) && e.has(Button.RIGHT) && player().selectionMode == SelectionMode.FLEET && fleet().owner == player()) {
				if (e.has(Modifier.CTRL)) {
					// attack move
					Planet p = getPlanetAt(e.x, e.y);
					if (p != null && p.owner != player() && knowledge(p, PlanetKnowledge.OWNER) >= 0) {
						fleetMode = null;
						fleet().targetPlanet = p;
						fleet().targetFleet = null;
						fleet().mode = FleetMode.ATTACK;
					} else {
						Fleet f = getFleetAt(e.x, e.y);
						if (f != null && f.owner != player() && f != fleet()) {
							fleetMode = null;
							fleet().targetPlanet = null;
							fleet().targetFleet = f;
							fleet().mode = FleetMode.ATTACK;
						} else {
							fleet().targetPlanet = null;
							fleet().targetFleet = null;
							fleet().mode = FleetMode.MOVE;
							fleet().waypoints.clear();
							fleet().waypoints.add(toMapCoordinates(e.x, e.y));
						}
					}
				} else
				if (e.has(Modifier.SHIFT)) {
					Planet p = getPlanetAt(e.x, e.y);
					if (p != null) {
						fleet().targetPlanet = p;
						fleet().targetFleet = null;
						fleet().mode = FleetMode.MOVE;
					} else {
						Fleet f = getFleetAt(e.x, e.y);
						if (f != null && f != fleet()) {
							fleet().targetPlanet = null;
							fleet().targetFleet = f;
							fleet().mode = FleetMode.MOVE;
						} else {
							fleet().targetPlanet = null;
							fleet().targetFleet = null;
							fleet().mode = FleetMode.MOVE;
							fleet().waypoints.clear();
							fleet().waypoints.add(toMapCoordinates(e.x, e.y));
						}
					}
					fleetMode = null;
				}
			} else
			if (e.has(Button.LEFT)) {
				if (starmapWindow.contains(e.x, e.y)) {
					if (fleetMode == FleetMode.ATTACK) {
						Planet p = getPlanetAt(e.x, e.y);
						if (p != null && p.owner != player() && knowledge(p, PlanetKnowledge.OWNER) >= 0) {
							fleetMode = null;
							fleet().targetPlanet = p;
							fleet().targetFleet = null;
							fleet().mode = FleetMode.ATTACK;
						} else {
							Fleet f = getFleetAt(e.x, e.y);
							if (f != null && f.owner != player()) {
								fleetMode = null;
								fleet().targetPlanet = null;
								fleet().targetFleet = f;
								fleet().mode = FleetMode.ATTACK;
							}
						}
					} else
					if (fleetMode == FleetMode.MOVE) {
						if (e.has(Modifier.SHIFT)) {
							fleet().targetPlanet = null;
							fleet().targetFleet = null;
							fleet().mode = FleetMode.MOVE;
							fleet().waypoints.add(toMapCoordinates(e.x, e.y));
						} else {
							Planet p = getPlanetAt(e.x, e.y);
							if (p != null) {
								fleet().targetPlanet = p;
								fleet().targetFleet = null;
								fleet().mode = FleetMode.MOVE;
							} else {
								Fleet f = getFleetAt(e.x, e.y);
								if (f != null && f != fleet()) {
									fleet().targetPlanet = null;
									fleet().targetFleet = f;
									fleet().mode = FleetMode.MOVE;
								} else {
									fleet().targetPlanet = null;
									fleet().targetFleet = null;
									fleet().mode = FleetMode.MOVE;
									fleet().waypoints.clear();
									fleet().waypoints.add(toMapCoordinates(e.x, e.y));
								}
							}
							fleetMode = null;
						}
					} else
					if (!e.has(Modifier.CTRL) && !e.has(Modifier.SHIFT)) {
						Planet p = getPlanetAt(e.x, e.y);
						Fleet f = getFleetAt(e.x, e.y);
	
						if (p != null) {
							player().currentPlanet = p;
							player().selectionMode = SelectionMode.PLANET;
						}
						if (f != null) {
							player().currentFleet = f;
							player().selectionMode = SelectionMode.FLEET;
						}
					}
					rep = true;
				} else
				if (planetFleetSplitterRect.contains(e.x, e.y) && planetFleetSplitterRange.height > 0) {
					pfSplitter = true;
				}
				if (rightPanelVisible) {
					if (planetsList.contains(e.x, e.y)) {
						int idx = planetsOffset + (e.y - planetsList.y) / 10;
						List<Planet> planets = planets();
						if (idx < planets.size()) {
							player().currentPlanet = planets.get(idx);
							player().selectionMode = SelectionMode.PLANET;
							rep = true;
						}
					}
					if (fleetsList.contains(e.x, e.y)) {
						int idx = fleetsOffset + (e.y - fleetsList.y) / 10;
						List<Fleet> fleets = player().ownFleets();
						if (idx < fleets.size()) {
							player().currentFleet = fleets.get(idx);
							player().selectionMode = SelectionMode.FLEET;
							rep = true;
						}
					}
				}
			}
			break;
		case DOUBLE_CLICK:
			if (starmapWindow.contains(e.x, e.y)) {
				Planet p = getPlanetAt(e.x, e.y);
				if (p != null) {
					player().currentPlanet = p;
					player().selectionMode = SelectionMode.PLANET;
					displayPrimary(Screens.COLONY);
					rep = true;
				} else {
					Fleet f = getFleetAt(e.x, e.y);
					if (f != null) {
						player().currentFleet = f;
						player().selectionMode = SelectionMode.FLEET;
						displaySecondary(Screens.EQUIPMENT);
					}
				}
			} else 
			if (planetsList.contains(e.x, e.y)) {
				int idx = planetsOffset + (e.y - planetsList.y) / 10;
				List<Planet> planets = planets();
				if (idx < planets.size()) {
					player().currentPlanet = planets.get(idx);
					displayPrimary(Screens.COLONY);
				}
			} else
			if (fleetsList.contains(e.x, e.y)) {
				int idx = fleetsOffset + (e.y - fleetsList.y) / 10;
				List<Fleet> fleets = player().ownFleets();
				if (idx < fleets.size()) {
					player().currentFleet = fleets.get(idx);
					displaySecondary(Screens.EQUIPMENT);
				}
			}
			break;
		case UP:
			panning = false;
			mouseDown = false;
			pfSplitter = false;
			break;
		case LEAVE:
			panning = false;
			mouseDown = false;
			pfSplitter = false;
			break;
		case WHEEL:
			if (starmapWindow.contains(e.x, e.y)) {
				if (e.has(Modifier.CTRL)) {
					if (e.z < 0) {
						doZoomIn(e.x, e.y);
						rep = true;
					} else {
						doZoomOut(e.x, e.y);
						rep = true;
					}
				} else
				if (e.has(Modifier.SHIFT)) {
					if (e.z < 0) {
						pan(+30, 0);
					} else {
						pan(-30, 0);
					}
				} else {
					if (e.z < 0) {
						pan(0, +30);
					} else {
						pan(0, -30);
					}
				}
			} else
			if (fleetsList.contains(e.x, e.y)) {
				if (e.z < 0) {
					fleetsOffset--;
				} else {
					fleetsOffset++;
				}
				List<Fleet> fleets = player().ownFleets();
				fleetsOffset = limitScrollBox(fleetsOffset, fleets.size(), fleetsList.height, 10);
			} else
			if (planetsList.contains(e.x, e.y)) {
				if (e.z < 0) {
					planetsOffset--;
				} else {
					planetsOffset++;
				}
				planetsOffset = limitScrollBox(planetsOffset, planets().size(), planetsList.height, 10);
			}
			break;
		default:
		}
		if (!rep) {
			rep = super.mouse(e);
		}
		return rep;
	}
	@Override
	public void onEndGame() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onEnter(Screens mode) {
		rotationTimer = commons.register(75, new Act() {
			@Override
			public void act() {
				rotatePlanets();
				askRepaint();
			}
		});
	}
	@Override
	public void onFinish() {
	}
	@Override
	public void onInitialize() {
		prevPlanet = new UIImageButton(commons.starmap().backwards);
		prevPlanet.setDisabledPattern(commons.common().disabledPattern);
		nextPlanet = new UIImageButton(commons.starmap().forwards);
		nextPlanet.setDisabledPattern(commons.common().disabledPattern);
		prevFleet = new UIImageButton(commons.starmap().backwards);
		prevFleet.setDisabledPattern(commons.common().disabledPattern);
		nextFleet = new UIImageButton(commons.starmap().forwards);
		nextFleet.setDisabledPattern(commons.common().disabledPattern);
		colony = new UIImageButton(commons.starmap().colony);
		equipment = new UIImageButton(commons.starmap().equipment);
		info = new UIImageButton(commons.common().infoButton);
		bridge = new UIImageButton(commons.common().bridgeButton);
		
		prevPlanet.onClick = new Act() {
			@Override 
			public void act() {
				List<Planet> planets = planets();
				int idx = planets.indexOf(planet());
				if (idx > 0 && planets.size() > 0) {
					player().currentPlanet = planets.get(idx - 1);
					planetsOffset = limitScrollBox(idx - 1, planets.size(), planetsList.height, 10);
				}
			}
		};
		nextPlanet.onClick = new Act() {
			@Override
			public void act() {
				List<Planet> planets = planets();
				int idx = planets.indexOf(planet());
				if (idx + 1 < planets.size()) {
					player().currentPlanet = planets.get(idx + 1);
					planetsOffset = limitScrollBox(idx + 1, planets.size(), planetsList.height, 10);
				}
			}
		};
		prevFleet.onClick = new Act() {
			@Override 
			public void act() {
				List<Fleet> fleets = player().ownFleets();
				int idx = fleets.indexOf(fleet());
				if (idx > 0 && fleets.size() > 0) {
					player().currentFleet = fleets.get(idx - 1);
					fleetsOffset = limitScrollBox(idx - 1, fleets.size(), fleetsList.height, 10);
				}
			}
		};
		nextFleet.onClick = new Act() {
			@Override
			public void act() {
				List<Fleet> fleets = player().ownFleets();
				int idx = fleets.indexOf(fleet());
				if (idx + 1 < fleets.size()) {
					player().currentFleet = fleets.get(idx + 1);
					fleetsOffset = limitScrollBox(idx + 1, fleets.size(), fleetsList.height, 10);
				}
			}
		};

		colony.onClick = new Act() {
			@Override
			public void act() {
				displayPrimary(Screens.COLONY);
			}	
		};
		equipment.onClick = new Act() {
			@Override
			public void act() {
				displaySecondary(Screens.EQUIPMENT);
			}
		};
		info.onClick = new Act() {
			@Override
			public void act() {
				displaySecondary(Screens.INFORMATION_COLONY);
			}
		};
		bridge.onClick = new Act() {
			@Override
			public void act() {
				displayPrimary(Screens.BRIDGE);
			}
		};
		
		zoom = new UIImageButton(commons.starmap().zoom) {
			@Override
			public boolean mouse(UIMouse e) {
				zoomDirection = (e.has(Button.LEFT));
				return super.mouse(e);
			};
		};
		zoom.setHoldDelay(100);
		zoom.onClick = new Act() {
			@Override
			public void act() {
				if (zoomDirection) {
					doZoomIn(starmapWindow.width / 2, starmapWindow.height / 2);
				} else {
					doZoomOut(starmapWindow.width / 2, starmapWindow.height / 2);
				}
			}
		};
		
		colonyName = new UILabel("", 14, commons.text());
		colonyOwner = new UILabel("", 10, commons.text());
		colonySurface = new UILabel("", 10, commons.text());
		colonyPopulationTax = new UILabel("", 10, commons.text());
		colonyOther = new UILabel("", 7, commons.text());

		problemsHouse = new UIImage(commons.common().houseIcon);
		problemsEnergy = new UIImage(commons.common().energyIcon);
		problemsWorker = new UIImage(commons.common().workerIcon);
		problemsFood = new UIImage(commons.common().foodIcon);
		problemsHospital = new UIImage(commons.common().hospitalIcon);
		problemsVirus = new UIImage(commons.common().virusIcon);
		problemsStadium = new UIImage(commons.common().stadiumIcon);
		problemsRepair = new UIImage(commons.common().repairIcon);
		problemsColonyHub = new UIImage(commons.common().colonyHubIcon);

		surveySatellite = new UIImageButton(commons.starmap().deploySatellite);
		surveySatellite.onClick = new Act() {
			@Override
			public void act() {
				surveySatellite.visible(false);
				deploySatellite("Satellite", "interlude/deploy_satellite");
			}
		};
		spySatellite1 = new UIImageButton(commons.starmap().deploySpySat1);
		spySatellite1.onClick = new Act() {
			@Override
			public void act() {
				surveySatellite.visible(false);
				deploySatellite("SpySatellite1", "interlude/deploy_spy_satellite_1");
			}
		};
		spySatellite2 = new UIImageButton(commons.starmap().deploySpySat2);
		spySatellite2.onClick = new Act() {
			@Override
			public void act() {
				surveySatellite.visible(false);
				deploySatellite("SpySatellite2", "interlude/deploy_spy_satellite_2");
			}
		};
		hubble2 = new UIImageButton(commons.starmap().deployHubble);
		hubble2.onClick = new Act() {
			@Override
			public void act() {
				surveySatellite.visible(false);
				deploySatellite("Hubble2", "interlude/deploy_hubble_2");
			}
		};
		
		showRadarButton = new UIImageToggleButton(commons.starmap().viewRadar);
		showRadarButton.selected = true;
		showFleetButton = new UIImageToggleButton(commons.starmap().viewFleet);
		showFleetButton.selected = true;
		showStarsButton = new UIImageToggleButton(commons.starmap().viewStar);
		showStarsButton.selected = true;
		showGridButton = new UIImageToggleButton(commons.starmap().viewSector);
		showGridButton.selected = true;

		
		
		showNamesNone = new UIImageButton(commons.starmap().namesNone);
		showNamesNone.visible(!showFleetNames && !showPlanetNames);
		showNamesNone.onClick = new Act() {
			@Override
			public void act() {
				showNamesNone.visible(false);
				showNamesPlanet.visible(true);
				showFleetNames = false;
				showPlanetNames = true;
			}
		};
		
		showNamesPlanet = new UIImageButton(commons.starmap().namesPlanets);
		showNamesPlanet.visible(!showFleetNames && showPlanetNames);
		showNamesPlanet.onClick = new Act() {
			@Override
			public void act() {
				showNamesPlanet.visible(false);
				showNamesFleet.visible(true);
				showFleetNames = true;
				showPlanetNames = false;
			}
		};

		showNamesFleet = new UIImageButton(commons.starmap().namesFleets);
		showNamesFleet.visible(showFleetNames && !showPlanetNames);
		showNamesFleet.onClick = new Act() {
			@Override
			public void act() {
				showNamesFleet.visible(false);
				showNamesBoth.visible(true);
				showFleetNames = true;
				showPlanetNames = true;
			}
		};

		showNamesBoth = new UIImageButton(commons.starmap().namesBoth);
		showNamesBoth.visible(showFleetNames && showPlanetNames);
		showNamesBoth.onClick = new Act() {
			@Override
			public void act() {
				showNamesBoth.visible(false);
				showNamesNone.visible(true);
				showFleetNames = false;
				showPlanetNames = false;
			}
		};
		
		fleetName = new UILabel("", 14, commons.text());
		fleetOwner = new UILabel("", 10, commons.text());
		fleetStatus = new UILabel("", 10, commons.text());
		fleetPlanet = new UILabel("", 10, commons.text());
		fleetComposition = new UILabel("", 7, commons.text());
		fleetSpeed = new UILabel("", 7, commons.text());
		fleetFirepower = new UILabel("", 7, commons.text());
		fleetMove = new UIImageTabButton(commons.starmap().move);
		fleetAttack = new UIImageTabButton(commons.starmap().attack);
		fleetStop = new UIImageTabButton(commons.starmap().stop);
		fleetColonize = new UIImageButton(commons.starmap().colonize);
		fleetSeparator = new UIImage(commons.starmap().commandSeparator);

		fleetMove.onPress = new Act() {
			@Override
			public void act() {
				doFleetMove();
			}
		};
		fleetAttack.onPress = new Act() {
			@Override
			public void act() {
				doFleetAttack();
			}
		};
		fleetStop.onPress = new Act() {
			@Override
			public void act() {
				doFleetStop();
			}
		};

		addThis();
	}
	/**
	 * Deploy a satellite with an animation.
	 * @param typeId the satellite id
	 * @param media the media to play
	 */
	void deploySatellite(final String typeId, String media) {
		final Planet p = planet();
		final boolean isPaused = commons.paused();
		if (!isPaused) {
			commons.pause();
		}
		commons.control().playVideos(new Act() {
			@Override
			public void act() {
				ResearchType rt = world().researches.get(typeId);
				
				InventoryItem pii = new InventoryItem();
				pii.count = 1;
				pii.owner = player();
				pii.type = rt;
				
				p.inventory.add(pii);
				
				if (!isPaused) {
					commons.resume();
				}
			}
		}, media);
	}
	@Override
	public void onLeave() {
		close0(rotationTimer);
		rotationTimer = null;
	}
	@Override
	public void onResize() {
		computeRectangles();
	}
	/**
	 * Paint the radar circle.
	 * @param g2 the graphics object.
	 * @param x the center coordinate
	 * @param y the center coordinate
	 * @param radius the radius
	 * @param zoom the zoom factor
	 */
	void paintRadar(Graphics2D g2, int x, int y, float radius, double zoom) {
		if (radius <= 0) {
			radius = 0.3f * 35;
		}
		double angle = 0;
		int n = (int)(2 * radius * Math.PI * zoom / 10);
		double dangle = Math.PI * 2 / n;
		while (angle < 2 * Math.PI) {
			
			double rx = (x + Math.cos(angle) * radius) * zoom + starmapRect.x;
			double ry = (y + Math.sin(angle) * radius) * zoom + starmapRect.y;
			
			g2.drawImage(radarDot, (int)rx, (int)ry, null);
			
			angle += dangle;
		}
	}
	/**
	 * Pan the starmap with the given pixels.
	 * @param dx the horizontal difference
	 * @param dy the vertical difference
	 */
	public void pan(int dx, int dy) {
		xOffset -= dx;
		yOffset -= dy;
		// limit the offset
		limitOffsets();
	}
	/** @return the player's own planets. */
	public List<Planet> planets() {
		List<Planet> pls = player().getPlayerPlanets();
		Collections.sort(pls, Planet.NAME_ORDER);
		return pls;
	}
	/**
	 * @param x the mouse coordinate
	 * @param y the mouse coordinate
	 * @param newIndex the new zoom index
	 */
	private void readjustZoom(int x, int y, int newIndex) {
		double pre = getZoom();
		
		double vx = (x - starmapRect.x) / pre;
		double vy = (y - starmapRect.y) / pre;
		
		zoomIndex = newIndex;
		
		double post = getZoom();
		
		selectRadarDot();
		limitOffsets();
		computeViewport();
		
		double vx2 = (x - starmapRect.x) / post;
		double vy2 = (y - starmapRect.y) / post;
		
		xOffset -= (vx2 - vx) * post;
		yOffset -= (vy2 - vy) * post;
		
		limitOffsets();
		computeViewport();

	}
	/**
	 * Rotate the planets on screen.
	 */
	protected void rotatePlanets() {
		for (Planet p : commons.world().planets.values()) {
			if (p.rotationDirection == RotationDirection.LR) {
				p.rotationPhase = (p.rotationPhase + 1) % p.type.body.length;
			} else {
				if (p.rotationPhase > 0) {
					p.rotationPhase = (p.rotationPhase - 1) % p.type.body.length;
				} else {
					p.rotationPhase = p.type.body.length - 1;
				}
			}
		}
		minimapPlanetBlink = blinkCounter < 6;
		blinkCounter = (blinkCounter + 1) % 12;
	}
	@Override
	public Screens screen() {
		return Screens.STARMAP;
	}
	/**
	 * Scroll to the given minimap relative coordinate.
	 * @param x the click on the minimap
	 * @param y the click on the minimap
	 */
	public void scrollMinimapTo(int x, int y) {
		xOffset = x * starmapRect.width / minimapInnerRect.width - starmapWindow.width / 2;
		yOffset = y * starmapRect.height / minimapInnerRect.height - starmapWindow.height / 2;
		limitOffsets();
		computeViewport();
	}
	/**
	 * Select the radar dot image for the current zoom level.
	 */
	void selectRadarDot() {
		radarDot = commons.starmap().radarDots[commons.starmap().radarDots.length * zoomIndex / (zoomLevelCount + 1)];
	}
	/**
	 * @param bottomPanelVisible the bottomPanelVisible to set
	 */
	public void setBottomPanelVisible(boolean bottomPanelVisible) {
		this.bottomPanelVisible = bottomPanelVisible;
	}
	/**
	 * @param minimapVisible the minimapVisible to set
	 */
	public void setMinimapVisible(boolean minimapVisible) {
		this.minimapVisible = minimapVisible;
	}
	/**
	 * @param rightPanelVisible the rightPanelVisible to set
	 */
	public void setRightPanelVisible(boolean rightPanelVisible) {
		this.rightPanelVisible = rightPanelVisible;
	}
	/**
	 * @param scrollbarsVisible the scrollbarsVisible to set
	 */
	public void setScrollbarsVisible(boolean scrollbarsVisible) {
		this.scrollbarsVisible = scrollbarsVisible;
	}
	/** Move the fleet (turns on movement selection mode. */
	void doFleetMove() {
		fleetMove.down = true;
		fleetAttack.down = false;
		fleetStop.down = false;
		fleetMode = FleetMode.MOVE;
	}
	/** Attack with the fleet (turns on attach selection mode. */
	void doFleetAttack() {
		fleetMove.down = false;
		fleetAttack.down = true;
		fleetStop.down = false;
		fleetMode = FleetMode.ATTACK;
	}
	/** Stop the current fleet. */
	void doFleetStop() {
		fleetMove.down = false;
		fleetAttack.down = false;
		fleetStop.down = true;
		fleet().waypoints.clear();
		fleet().targetFleet = null;
		fleet().targetPlanet = null;
		fleet().mode = null;
		fleetMode = null;
	}
}
