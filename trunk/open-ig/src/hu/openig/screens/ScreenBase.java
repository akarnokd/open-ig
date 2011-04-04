/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import hu.openig.core.Act;
import hu.openig.core.Configuration;
import hu.openig.core.ResourceLocator;
import hu.openig.model.BuildingType;
import hu.openig.model.Fleet;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.Player;
import hu.openig.model.ResearchType;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIMouse;

import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * A screen base class.
 * @author akarnokd, 2009.12.23.
 */
public abstract class ScreenBase extends UIContainer implements GameControls {
	/** The global configuration object. */
	protected Configuration config;
	/** The global resource locator. */
	protected ResourceLocator rl;
	/** The common resources. */
	protected CommonResources commons;
	/** 
	 * Initialize any resources that are required by the screen. 
	 * Called by the rendering system to initialize a screen at startup.
	 * @param commons the configuration object
	 */
	public final void initialize(CommonResources commons) {
		this.components.clear();
		this.commons = commons;
		this.config = commons.config;
		this.rl = commons.rl;
		onInitialize();
	}
	/** The custom initialization routine. Override this to perform additional initialization, i.e., create sub-components. */
	public abstract void onInitialize();
	/** 
	 * Perform actions when the player displays the screen (e.g start animation timers). 
	 * @param mode the screen mode to display 
	 */
	public abstract void onEnter(Screens mode);
	/** Perform actions when the player leaves the screen (e.g. stop animation timers). */
	public abstract void onLeave();
	/** Release resources of the screen, and e.g. cancel any animation timers. */
	public abstract void onFinish();
	/** Called by the rendering system when the parent swing component changed its size. */
	public final void resize() {
		width = getInnerWidth();
		height = getInnerHeight();
		onResize();
	}
	/** Called if the component size changed since the last call. */
	public abstract void onResize();
	/**
	 * Ask for the repaint of the given  component area only.
	 * @param c the target component
	 */
	public void askRepaint(UIComponent c) {
		Point p = c.absLocation();
		commons.control().repaintInner(p.x, p.y, c.width, c.height);
	}
	/**
	 * Ask for the repaint of the given partial region.
	 * @param rect the region to repaint
	 */
	public void askRepaint(Rectangle rect) {
		commons.control().repaintInner(rect.x, rect.y, rect.width, rect.height);
	}
	/**
	 * @return The rectangle that represents the non-transparent region of this screen.
	 * It may be used to optimize rendering of any underlying screen.
	 */
	public Rectangle nontransparent() {
		return new Rectangle(0, 0, width, height);
	}
	@Override
	public boolean mouse(UIMouse e) {
		if (super.mouse(e)) {
			askRepaint();
			return true;
		}
		return false;
	}
	/** @return The current player. */
	public Player player() {
		return commons.world().player;
	}
	/** @return the current planet. */
	public Planet planet() {
		Planet p = player().currentPlanet;
		if (p == null) {
			p = player().moveNextPlanet();
		}
		return p;
	}
	/** @return the current building type. */
	public BuildingType building() {
		return player().currentBuilding;
	}
	/** @return the current research type. */
	public ResearchType research() {
		return player().currentResearch;
	}
	/** @return the current fleet. */
	public Fleet fleet() {
		return player().currentFleet;
	}
	/** @return the race of the buildings on the current planet or the player's race. */
	public String race() {
		return planet().isPopulated() ? planet().race : player().race;
	}
	/**
	 * Returns a translation for the given label.
	 * @param label the label
	 * @return the translation
	 */
	public String get(String label) {
		return commons.labels0().get(label);
	}
	/** 
	 * Returns a formatted translation of the given label.
	 * @param label the label
	 * @param args the arguments to the formatter
	 * @return the translation
	 */
	public String format(String label, Object... args) {
		return commons.labels0().format(label, args);
	}
	/**
	 * Returns the planet knowledge about the given planet by the current player.
	 * @param p the target planet
	 * @return the knowledge
	 */
	public PlanetKnowledge knowledge(Planet p) {
		return player().planets.get(p);
	}
	/**
	 * Compare the current knowledge level of the given planet by the expected level.
	 * @param planet the target planet
	 * @param expected the expected level
	 * @return -1 if less known, 0 if exactly on the same level, +1 if more
	 */
	public int knowledge(Planet planet, PlanetKnowledge expected) {
		PlanetKnowledge k = player().planets.get(planet);
		if (k == expected) {
			return 0;
		}
		if (k != null && expected == null) {
			return 1;
		}
		if (k == null && expected != null) {
			return -1;
		}
		return k.ordinal() < expected.ordinal() ? -1 : 1;
	}
	/**
	 * Compare the current knowledge level of the given fleet by the expected level.
	 * @param planet the target planet
	 * @param expected the expected level
	 * @return -1 if less known, 0 if exactly on the same level, +1 if more
	 */
	public int knowledge(Fleet planet, FleetKnowledge expected) {
		FleetKnowledge k = player().fleets.get(planet);
		if (k == expected) {
			return 0;
		}
		if (k != null && expected == null) {
			return 1;
		}
		if (k == null && expected != null) {
			return -1;
		}
		return k.ordinal() < expected.ordinal() ? -1 : 1;
	}
	/**
	 * Returns the fleet knowledge about the given fleet by the current player.
	 * @param p the target planet
	 * @return the knowledge
	 */
	public FleetKnowledge knowledge(Fleet p) {
		return player().fleets.get(p);
	}
	/** @return the screen's type. */
	public abstract Screens screen();
	@Override
	public void switchLanguage(String newLanguage) {
		commons.control().switchLanguage(newLanguage);
	}
	@Override
	public ScreenBase displayPrimary(Screens screen) {
		return commons.control().displayPrimary(screen);
	}
	@Override
	public ScreenBase displaySecondary(Screens screen) {
		return commons.control().displaySecondary(screen);
	}
	@Override
	public void hideSecondary() {
		commons.control().hideSecondary();
	}
	@Override
	public void playVideos(String... videos) {
		commons.control().playVideos(videos);
	}
	@Override
	public void playVideos(Act onComplete, String... videos) {
		commons.control().playVideos(onComplete, videos);
	}
	@Override
	public void displayStatusbar() {
		commons.control().displayStatusbar();
	}
	@Override
	public void hideStatusbar() {
		commons.control().hideStatusbar();
	}
	@Override
	public void exit() {
		commons.control().exit();
	}
	@Override
	public void repaintInner() {
		commons.control().repaintInner();
	}
	@Override
	public int getInnerWidth() {
		return commons.control().getInnerWidth();
	}
	@Override
	public int getInnerHeight() {
		return commons.control().getInnerHeight();
	}
	@Override
	public void repaintInner(int x, int y, int w, int h) {
		commons.control().repaintInner(x, y, w, h);
	}
	@Override
	public FontMetrics fontMetrics(int size) {
		return commons.control().fontMetrics(size);
	}
	/** Ask for repaint. */
	@Override
	public void askRepaint() {
		repaintInner();
	}
}
