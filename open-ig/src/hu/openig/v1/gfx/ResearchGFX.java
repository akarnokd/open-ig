/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.gfx;

import hu.openig.v1.ResourceLocator;

import java.awt.image.BufferedImage;

/**
 * Images for the research and production screens.
 * @author karnok, 2009.11.09.
 * @version $Revision 1.0$
 */
public class ResearchGFX {
	/** The resource locator. */
	protected ResourceLocator rl;
	/** Empty button large. */
	@Img(name = "research/button_empty_large")
	public BufferedImage empty;
	/** Empty large. */
	@Img(name = "research/button_empty_elevated_large")
	public BufferedImage emptyElevated;
	/** Empty 3x large. */
	@Img(name = "research/button_empty_elevated_3x_large")
	public BufferedImage emptyElevated2;
	/** Empty button small. */
	@Img(name = "research/button_empty_small")
	public BufferedImage emptySmall;
	/** Fund button. */
	@Btn2(name = "research/button_fund")
	public BufferedImage[] fund;
	/** Info button. */
	@Btn2(name = "research/button_info")
	public BufferedImage[] info;
	/** Minus one button. */
	@Btn2(name = "research/button_minus_one")
	public BufferedImage[] minusOne;
	/** Plus one button. */
	@Btn2(name = "research/button_plus_one")
	public BufferedImage[] plusOne;
	/** Minus ten button. */
	@Btn2(name = "research/button_minus_ten")
	public BufferedImage[] minusTen;
	/** Plus ten button. */
	@Btn2(name = "research/button_plus_ten")
	public BufferedImage[] plusTen;
	/** Sell button. */
	@Btn2(name = "research/button_sell")
	public BufferedImage[] sell;
	/** Current research arrow. */
	@Img(name = "research/current_research_arrow")
	public BufferedImage current;
	/** Start button. */
	@Btn2(name = "research/button_start")
	public BufferedImage[] start;
	/** Stop button. */
	@Btn2(name = "research/button_stop")
	public BufferedImage[] stop;
	/** Current research panel. */
	@Img(name = "research/panel_current_research")
	public BufferedImage currentResearchPanel;
	/** Main class panel. */
	@Img(name = "research/panel_main_class")
	public BufferedImage mainClassPanel;
	/** Requirements panel. */
	@Img(name = "research/panel_requirements")
	public BufferedImage requirementsPanel;
	/** Research info panel. */
	@Img(name = "research/panel_research_info")
	public BufferedImage researchInfoPanel;
	/** Selected research panel. */
	@Img(name = "research/panel_selected_research")
	public BufferedImage selectedResearchPanel;
	/** Subtype panel. */
	@Img(name = "research/panel_subtype")
	public BufferedImage subtypePanel;
	/** Subtype wide panel. */
	@Img(name = "research/panel_subtype_wide")
	public BufferedImage subtypeWidePanel;
	/** Production base panel. */
	@Img(name = "research/production_base")
	public BufferedImage productionBasePanel;
	/** Production line. */
	@Img(name = "research/production_line")
	public BufferedImage productionLine;
	/** Missing research lab mark. */
	@Img(name = "research/research_missing_lab")
	public BufferedImage researchMissingLab;
	/** Missing prerequisite mark. */
	@Img(name = "research/research_missing_prerequisite")
	public BufferedImage researchMissingPrerequisite;
	/** The rolling CD icon. */
	@Anim(name = "research/research_rolling", width = 19)
	public BufferedImage[] rolling;
	/** The small rolling CD icon. */
	@Anim(name = "research/research_rolling_small", width = 19)
	public BufferedImage[] rollingSmall;
	/** Unavailable. */
	@Img(name = "research/unavailable")
	public BufferedImage unavailable;
	/**
	 * Constructor.
	 * @param rl the resource locator
	 */
	public ResearchGFX(ResourceLocator rl) {
		this.rl = rl;
	}
	/**
	 * Load resources.
	 * @param language the language
	 */
	public void load(String language) {
		GFXLoader.loadResources(this, rl, language);
	}
}
