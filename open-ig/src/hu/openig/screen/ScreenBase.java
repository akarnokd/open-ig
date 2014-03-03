/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen;

import hu.openig.core.Pair;
import hu.openig.model.BuildingType;
import hu.openig.model.Configuration;
import hu.openig.model.Fleet;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.GameAPI;
import hu.openig.model.GameAsyncAPI;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.Player;
import hu.openig.model.ResearchType;
import hu.openig.model.ResourceLocator;
import hu.openig.model.Screens;
import hu.openig.model.SoundTarget;
import hu.openig.model.SoundType;
import hu.openig.model.World;
import hu.openig.render.RenderTools;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIMouse;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;

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
//	/**
//	 * Ask for the repaint of the given  component area only.
//	 * @param c the target component
//	 */
//	public void askRepaint(UIComponent c) {
//		Point p = c.absLocation();
//		commons.control().repaintInner(p.x, p.y, c.width, c.height);
//	}
//	/**
//	 * Ask for the repaint of the given partial region.
//	 * @param rect the region to repaint
//	 */
//	public void askRepaint(Rectangle rect) {
//		commons.control().repaintInner(rect.x, rect.y, rect.width, rect.height);
//	}
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
	/**
	 * Returns the control API for the current player.
	 * @return the game API
	 */
	public GameAPI api() {
		return player().api;
	}
	/**
	 * Returns the asynchronous game API for the current player.
	 * @return the asynchronou game API
	 */
	public GameAsyncAPI asyncAPI() {
		return player().asyncAPI;
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
	public void buttonSound(SoundType type) {
		commons.playSound(SoundTarget.BUTTON, type, null);
	}
	/**
	 * Play a given sound if the computer notification voice is enabled.
	 * @param type the sound type
	 */
	public void computerSound(SoundType type) {
		commons.playSound(SoundTarget.COMPUTER, type, null);
	}
	/**
	 * Play sound effects (space and ground wars).
	 * @param type the osund type
	 */
	public void effectSound(SoundType type) {
		commons.playSound(SoundTarget.EFFECT, type, null);
	}
	/**
	 * Play a screen-switching related sound.
	 * @param type the sound type
	 */
	public void screenSound(SoundType type) {
		commons.playSound(SoundTarget.SCREEN, type, null);
	}
	/**
	 * Scale the mouse coordinates according to the current screen scaling policy or do nothing.
	 * @param e the mouse event
	 * @param base the screen's base rectangle
	 * @param margin the optional top/bottom margin size
	 */
	public void scaleMouse(UIMouse e, Rectangle base, int margin) {
		if (config.scaleAllScreens) {
			Pair<Point, Double> pd = RenderTools.fitWindow(
					getInnerWidth(), getInnerHeight()
					- RenderTools.STATUS_BAR_TOP - RenderTools.STATUS_BAR_BOTTOM - 2 * margin, 
					base.width, base.height);
			double dx = base.x * (1 - pd.second);
			double dy = base.y * (1 - pd.second);
			e.x = (int)(((e.x - dx) / pd.second));
			e.y = (int)(((e.y - dy) / pd.second));
		}
	}
	/**
	 * Returns the scaling factor of this screen.
	 * @param base the base rectangle
	 * @param margin the margin
	 * @return the scaling factor
	 */
	public Pair<Point, Double> scale(Rectangle base, int margin) {
		if (config.scaleAllScreens) {
			Pair<Point, Double> pd = RenderTools.fitWindow(
					getInnerWidth(), getInnerHeight()
					- RenderTools.STATUS_BAR_TOP - RenderTools.STATUS_BAR_BOTTOM - 2 * margin, 
					base.width, base.height);
			return pd;
		}
		return Pair.of(new Point(0, 0), 1d);
	}
	/**
	 * Scale the drawing to the current window if applicable.
	 * @param g2 the graphics context
	 * @param base the screen's base rectangle
	 * @return the saved transform before the scaling
	 * @param margin the optional top/bottom margin size
	 */
	public AffineTransform scaleDraw(Graphics2D g2, Rectangle base, int margin) {
		AffineTransform save0 = g2.getTransform();
		if (config.scaleAllScreens) {
			Pair<Point, Double> pd = RenderTools.fitWindow(
					getInnerWidth(), 
					getInnerHeight() - RenderTools.STATUS_BAR_TOP - RenderTools.STATUS_BAR_BOTTOM - 2 * margin, 
					base.width, base.height);
			g2.translate(base.x, base.y);
			g2.scale(pd.second, pd.second);
			g2.translate(-base.x, -base.y);
		}
		return save0;
	}
	/**
	 * Scale the base screen coordinates based on the current policy or just center.
	 * @param base the base rectangle
	 * @param margin the optional top/bottom margin size
	 */
	public void scaleResize(Rectangle base, int margin) {
		if (config.scaleAllScreens) {
			Pair<Point, Double> pd = RenderTools.fitWindow(
					getInnerWidth(), 
					getInnerHeight() - RenderTools.STATUS_BAR_TOP - RenderTools.STATUS_BAR_BOTTOM - 2 * margin, 
					base.width, base.height);
			base.x = pd.first.x;
			base.y = pd.first.y + RenderTools.STATUS_BAR_TOP + margin;
		} else {
			RenderTools.centerScreen(base, getInnerWidth(), getInnerHeight(), true);
		}
	}
	/**
	 * Ask for the repaint of the given  component area only.
	 * @param base the base of scaling
	 * @param c the target component
	 * @param margin the optional margin
	 */
	public void scaleRepaint(Rectangle base, UIComponent c, int margin) {
		Point p = c.absLocation();
		scaleRepaint(base, new Rectangle(p.x, p.y, c.width, c.height), margin);
	}
	/**
	 * Ask for the repaint of the given partial region.
	 * @param base the region to repaint
	 * @param object the object to repaint
	 * @param margin the optional margin
	 */
	public void scaleRepaint(Rectangle base, Rectangle object, int margin) {
		if (config.scaleAllScreens) {
			Pair<Point, Double> pd = RenderTools.fitWindow(
					getInnerWidth(), 
					getInnerHeight() - RenderTools.STATUS_BAR_TOP - RenderTools.STATUS_BAR_BOTTOM
					- 2 * margin, 
					base.width, base.height);
			int sw = (int)(object.width * pd.second);
			int sh = (int)(object.height * pd.second);
			commons.control().repaintInner(object.x, object.y, sw, sh);
		} else {
			commons.control().repaintInner(object.x, object.y, object.width, object.height);
		}
	}
	/**
	 * Scale the mouse coordinates to the current screen.
	 * <p>Override this method to provide per-screen based scaling override.</p>
	 * @param mx the mouse X
	 * @param my the mouse Y
	 * @return the scaled coordinates
	 */
	protected Point scaleBase(int mx, int my) {
		return new Point(mx, my);
	}
	/**
	 * <p>Override this to provide an uniform margin for many features.</p>
	 * @return the margin in pixels
	 */
	protected int margin() {
		return 0;
	}
	/**
	 * <p>Override this method to provide a per-screen based scaling.</p>
	 * @return the screen's scaling factor.
	 */
	protected Pair<Point, Double> scale() {
		return Pair.of(new Point(0, 0), 1d);
	}
	@Override
	public UIComponent componentAt(int x, int y) {
		Point p = scaleBase(x, y);
		return super.componentAt(p.x, p.y);
	}
	/**
	 * Returns the rectangle of the given component in the
	 * current coordinate system.
	 * @param c the component
	 * @return the enclosing rectangle
	 */
	public Rectangle componentRectangle(UIComponent c) {
		Point p = c.absLocation();
		Pair<Point, Double> s = scale();
		int dx = (int)(s.first.x - s.second * s.first.x);
		int dy = (int)(s.first.y - s.second * s.first.y);
		
		return new Rectangle(
				(int)(p.x * s.second + dx), 
				(int)(p.y * s.second + dy), 
				(int)(c.width * s.second), (int)(c.height * s.second));
	}
	/**
	 * Set the tooltip from a potentially formatted label.
	 * @param c the component
	 * @param label the label as the tooltip
	 * @param params the optional parameters
	 */
	public void setTooltip(UIComponent c, String label, Object... params) {
		if (label != null) {
			String t2 = format(label, params);
			setTooltipText(c, t2);
		} else {
			setTooltipText(c, null);
		}
	}
	/**
	 * Set the tooltip to the given text.
	 * @param c the component
	 * @param text the text
	 */
	public void setTooltipText(UIComponent c, String text) {
		String t1 = c.tooltip();
		c.tooltip(text);
		if (!Objects.equals(t1, text)) {
			commons.control().tooltipChanged(c);
		}
	}
}
