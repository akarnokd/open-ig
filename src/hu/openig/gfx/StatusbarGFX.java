/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.gfx;

import hu.openig.core.Img;
import hu.openig.model.ResourceLocator;

import java.awt.image.BufferedImage;

/**
 * Statusbar graphics components.
 * @author akarnokd, 2009.11.09.
 */
public class StatusbarGFX {
    /** Ingame top left. */
    @Img(name = "statusbar_ingame_top_left")
    public BufferedImage ingameTopLeft;
    /** Ingame top filler. */
    @Img(name = "statusbar_ingame_top_fill")
    public BufferedImage ingameTopFill;
    /** Ingame top right. */
    @Img(name = "statusbar_ingame_top_right")
    public BufferedImage ingameTopRight;
    /** Ingame bottom left. */
    @Img(name = "statusbar_ingame_bottom_left")
    public BufferedImage ingameBottomLeft;
    /** Ingage bottom fill. */
    @Img(name = "statusbar_ingame_bottom_fill")
    public BufferedImage ingameBottomFill;
    /** Ingame bottom right. */
    @Img(name = "statusbar_ingame_bottom_right")
    public BufferedImage ingameBottomRight;
    /** Nongame top left. */
    @Img(name = "statusbar_nongame_top_left")
    public BufferedImage nongameTopLeft;
    /** Nongame top fill. */
    @Img(name = "statusbar_nongame_top_fill")
    public BufferedImage nongameTopFill;
    /** Nongame top right. */
    @Img(name = "statusbar_nongame_top_right")
    public BufferedImage nongameTopRight;
    /** Nongame bottom left. */
    @Img(name = "statusbar_nongame_bottom_left")
    public BufferedImage nongameBottomLeft;
    /** Nongame bottom fill. */
    @Img(name = "statusbar_nongame_bottom_fill")
    public BufferedImage nongameBottomFill;
    /** Nongame bottom right. */
    @Img(name = "statusbar_nongame_bottom_right")
    public BufferedImage nongameBottomRight;
    /** Research notification icon. */
    @Img(name = "research_notify")
    public BufferedImage researchNotify;
    /** Production notification icon. */
    @Img(name = "production_notify")
    public BufferedImage productionNotify;
    /** Money notification icon. */
    @Img(name = "money_notify")
    public BufferedImage moneyNotification;
    /** Normal gear icon. */
    @Img(name = "gear_normal")
    public BufferedImage gearNormal;
    /** Light gear icon. */
    @Img(name = "gear_light")
    public BufferedImage gearLight;
    /** The statusbar icon background. */
    @Img(name = "statusbar_icon_back")
    public BufferedImage iconBack;
    /** Normal research icon. */
    @Img(name = "research_normal")
    public BufferedImage researchNormal;
    /** Light research icon. */
    @Img(name = "research_light")
    public BufferedImage researchLight;
    /** The objectives icon. */
    @Img(name = "objectives")
    public BufferedImage objectives;
    /** The menu image. */
    @Img(name = "menu")
    public BufferedImage menu;
    /**
     * Load the resources.
     * @param rl the resource locator
     * @return this;
     */
    public StatusbarGFX load(ResourceLocator rl) {
        GFXLoader.loadResources(this, rl);
        return this;
    }
}
