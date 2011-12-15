/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen;

import hu.openig.core.Configuration;
import hu.openig.core.ResourceLocator;
import hu.openig.model.BuildingType;
import hu.openig.model.Fleet;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.Player;
import hu.openig.model.ResearchType;
import hu.openig.model.Screens;
import hu.openig.model.SoundType;
import hu.openig.model.World;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIMouse;

import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Closeable;
import java.io.IOException;

/**
 * A screen base class.
 * @author akarnokd, 2009.12.23.
 */
public abstract class ScreenBase extends UIContainer {
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
	 * Ask for the repaint of the given partial region.
	 * @param x left coordinate
	 * @param y top coordinate
	 * @param width the width
	 * @param height the height
	 */
	public void askRepaint(int x, int y, int width, int height) {
		commons.control().repaintInner(x, y, width, height);
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
		return player().currentResearch();
	}
	/**
	 * Change the current research type.
	 * @param type new the research type
	 */
	public void research(ResearchType type) {
		world().selectResearch(type);
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
		return commons.labels().get(label);
	}
	/** 
	 * Returns a formatted translation of the given label.
	 * @param label the label
	 * @param args the arguments to the formatter
	 * @return the translation
	 */
	public String format(String label, Object... args) {
		return commons.labels().format(label, args);
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
		return player().knowledge(planet, expected);
	}
	/**
	 * Compare the current knowledge level of the given fleet by the expected level.
	 * @param fleet the target planet
	 * @param expected the expected level
	 * @return -1 if less known, 0 if exactly on the same level, +1 if more
	 */
	public int knowledge(Fleet fleet, FleetKnowledge expected) {
		return player().knowledge(fleet, expected);
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
	/**
	 * Jump to the given primary screen.
	 * @param screen the new screen
	 * @return the screen object
	 */
	public ScreenBase displayPrimary(Screens screen) {
		return commons.control().displayPrimary(screen);
	}
	/**
	 * Jump to the given secondary screen.
	 * @param screen the new screen
	 * @return the screen object
	 */
	public ScreenBase displaySecondary(Screens screen) {
		return commons.control().displaySecondary(screen);
	}
	/** Hide the secondary screen. */
	public void hideSecondary() {
		commons.control().hideSecondary();
	}
	/** Display the status bars. */
	public void displayStatusbar() {
		commons.control().displayStatusbar();
	}
	/** Hide the status bars. */
	public void hideStatusbar() {
		commons.control().hideStatusbar();
	}
	/** Exit the game. */
	public void exit() {
		commons.control().exit();
	}
	/** @return the rendering component's width */ 
	public int getInnerWidth() {
		return commons.control().getInnerWidth();
	}
	/** @return the rendering component's height. */
	public int getInnerHeight() {
		return commons.control().getInnerHeight();
	}
	/**
	 * Returns a font metrics for the default bold font with the given size.
	 * @param size the size
	 * @return the font metrics
	 */
	public FontMetrics fontMetrics(int size) {
		return commons.control().fontMetrics(size);
	}
	/** Ask for repaint. */
	@Override
	public void askRepaint() {
		commons.control().repaintInner();
	}
	/** @return the world. */
	public World world() {
		return commons.world();
	}
	/**
	 * Called when a game is ending. Implement this so the UI does not hold any reference
	 * to the game world anymore.
	 */
	public abstract void onEndGame();
	/** Save the game. */
	public void save() {
		commons.control().save();
	}
	/** 
	 * Load a specific save.
	 * @param name the save name
	 */
	public void load(String name) {
		commons.control().load(name);
	}
	/**
	 * Close the given closeable and consume the IOException.
	 * @param c the closeable
	 */
	public void close0(Closeable c) {
		try {
			if (c != null) {
				c.close();
			}
		} catch (IOException ex) {
			
		}
	}
	/**
	 * Play a given sound for buttons if the effect is enabled in options.
	 * @param type the sound type
	 */
	public void sound(SoundType type) {
		if (config.buttonSounds) {
			commons.sounds.play(type);
		}
	}
}
