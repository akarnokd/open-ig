/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.gfx;

import hu.openig.core.Btn2;
import hu.openig.core.Btn3;
import hu.openig.core.Img;
import hu.openig.model.ResourceLocator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * The information screen graphics entities.
 * @author akarnokd, 2009.11.09.
 */
public class InfoGFX {
    /** The base image. */
    @Img(name = "info/info_base_top")
    public BufferedImage baseTop;
    /** The base image. */
    @Img(name = "info/info_base_left")
    public BufferedImage baseLeft;
    /** The base image. */
    @Img(name = "info/info_base_middle")
    public BufferedImage baseMiddle;
    /** The base image. */
    @Img(name = "info/info_base_right")
    public BufferedImage baseRight;
    /** The base image. */
    @Img(name = "info/info_base_divider_1")
    public BufferedImage baseDivider1;
    /** The base image. */
    @Img(name = "info/info_base_divider_2")
    public BufferedImage baseDivider2;
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
     * Load settings to the given language.
     * @param rl the resource locator
     * @return this
     */
    public InfoGFX load(ResourceLocator rl) {
        GFXLoader.loadResources(this, rl);
        return this;
    }
    /**
     * Paint the info panel to the graphics surface.
     * @param g2 the graphics context
     * @param x the top-left coordinate
     * @param y the top-left coordinate
     */
    public void drawInfoPanel(Graphics2D g2, int x, int y) {
        g2.setColor(Color.BLACK);
        g2.fillRect(x, y, 620, 420);
        g2.drawImage(baseTop, x, y, null);
        g2.drawImage(baseLeft, x, y, null);
        g2.drawImage(baseMiddle, x + 413, y, null);
        g2.drawImage(baseRight, x + 618, y, null);
        g2.drawImage(baseDivider1, x + 413, y + 28, null);
        g2.drawImage(baseDivider2, x + 413, y + 209, null);
    }
}
