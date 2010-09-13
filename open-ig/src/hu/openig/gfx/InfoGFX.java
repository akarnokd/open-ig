/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.gfx;

import hu.openig.core.Btn2;
import hu.openig.core.Btn3;
import hu.openig.core.GFXLoader;
import hu.openig.core.Img;
import hu.openig.core.ResourceLocator;

import java.awt.image.BufferedImage;

/**
 * @author karnok, 2009.11.09.
 * @version $Revision 1.0$
 */
public class InfoGFX {
	/** The resource locator. */
	protected ResourceLocator rl;
	/** The base image. */
	@Img(name = "info/info_base")
	public BufferedImage base;
	/** The empty button. */
	@Img(name = "info/button_empty")
	public BufferedImage emptyButton;
	/** Aliens. */
	@Btn3(name = "info/button_aliens")
	public BufferedImage[] aliens;
	/** Buildings. */
	@Btn3(name = "info/button_buildings")
	public BufferedImage[] buildings;
	/** Colony info. */
	@Btn3(name = "info/button_colony_info")
	public BufferedImage[] colonyInfo;
	/** Financial info. */
	@Btn3(name = "info/button_financial_info")
	public BufferedImage[] financialInfo;
	/** Colony. */
	@Btn2(name = "info/button_colony")
	public BufferedImage[] colony;
	/** Diplomacy. */
	@Btn2(name = "info/button_diplomacy")
	public BufferedImage[] diplomacy;
	/** Equipment. */
	@Btn2(name = "info/button_equipment")
	public BufferedImage[] equipment;
	/** Fleets. */
	@Btn3(name = "info/button_fleets")
	public BufferedImage[] fleets;
	/** Inventions. */
	@Btn3(name = "info/button_inventions")
	public BufferedImage[] inventions;
	/** Military info. */
	@Btn3(name = "info/button_military_info")
	public BufferedImage[] militaryInfo;
	/** Planets. */
	@Btn3(name = "info/button_planets")
	public BufferedImage[] planets;
	/** Production. */
	@Btn2(name = "info/button_production")
	public BufferedImage[] production;
	/** Research. */
	@Btn2(name = "info/button_research")
	public BufferedImage[] research;
	/** Starmap. */
	@Btn2(name = "info/button_starmap")
	public BufferedImage[] starmap;
	/** Less tax. */
	@Btn2(name = "info/button_tax_less")
	public BufferedImage[] taxLess;
	/** More tax. */
	@Btn2(name = "info/button_tax_more")
	public BufferedImage[] taxMore;
	/**
	 * Constructor.
	 * @param rl the resource locator
	 */
	public InfoGFX(ResourceLocator rl) {
		this.rl = rl;
	}
	/**
	 * Load settings to the given language.
	 * @param language the language
	 */
	public void load(String language) {
		GFXLoader.loadResources(this, rl, language);
	}
}
