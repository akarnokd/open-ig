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
 * The background images for various screens,
 * including menu, options and battle end screens.
 * @author akarnokd, 2009.11.09.
 */
public class BackgroundGFX {
    /** The start backgrounds. */
    public BufferedImage[] start;
    /** The options backgrounds. */
    public BufferedImage[] options;
    /** The difficulty backgrounds. */
    public BufferedImage[] difficulty;
    /** The test screen background. */
    @Img(name = "phsychologist_test")
    public BufferedImage test;
    /** The setup screen background. */
    @Img(name = "setup")
    public BufferedImage setup;
    /** The gameover image. */
    @Img(name = "gameover")
    public BufferedImage gameover;
    /** The Open Imperium Galactica text logo. */
    @Img(name = "open-ig-textlogo")
    public BufferedImage openigTextLogo;
    /**
     * Load resources.
     * @param rl the resource locator
     * @return this
     */
    public BackgroundGFX load(ResourceLocator rl) {
        GFXLoader.loadResources(this, rl);
        start = new BufferedImage[] {
            rl.getImage("start_1"),
            rl.getImage("start_2"),
            rl.getImage("start_3")
        };
        options = new BufferedImage[] {
            rl.getImage("options_1"),
            rl.getImage("options_2"),
        };
        difficulty = new BufferedImage[] {
            rl.getImage("difficulty_1"),
            rl.getImage("difficulty_2")
        };
        return this;
    }
}
