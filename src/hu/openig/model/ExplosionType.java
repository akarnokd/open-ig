/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The explosion animation types.
 * @author akarnokd, 2011.09.07.
 */
public enum ExplosionType {
    /** Explosion. */
    GROUND_BLUE_MINI("groundwar/explosion_blue_mini", 3),
    /** Explosion. */
    GROUND_GRAY("groundwar/explosion_gray", 29),
    /** Explosion. */
    GROUND_GREEN("groundwar/explosion_green", 38),
    /** Explosion. */
    GROUND_RED("groundwar/explosion_red", 19),
    /** Explosion. */
    GROUND_YELLOW("groundwar/explosion_yellow", 16),
    /** Explosion. */
    GROUND_YELLOW_MINI("groundwar/explosion_yellow_mini", 5),
    /** Explosion. */
    GROUND_ROCKET("groundwar/rocket_explosions", 9),
    /** Explosion. */
    GROUND_ROCKET_2("groundwar/rocket_explosions_2", 9),

    /** Small explosion animation. */
    EXPLOSION_SMALL("spacewar/explosion_1", 11),
    /** Medium explosion animation. */
    EXPLOSION_MEDIUM("spacewar/explosion_2", 11),
    /** Large explosion animation. */
    EXPLOSION_LARGE("spacewar/explosion_3", 18),
    /** Tiny explosion animation. */
    EXPLOSION_TINY("spacewar/explosion_hit", 5),
    /** Green explosion. */
    EXPLOSION_GREEN("spacewar/explosion_4", 30),
    /** Multi explosion animation.The actual animation is a combinations of multiple explosion animations. */
    EXPLOSION_MULTI("spacewar/explosion_3", 18),
    ;
    /** The explosion image resource. */
    public final String image;
    /** The animation phases. */
    public final int frames;
    /**

     * Constructor.
     * @param image the explosion image
     * @param frames the number of frames
     */
    ExplosionType(String image, int frames) {
        this.image = image;
        this.frames = frames;
    }
}
