/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.image.BufferedImage;

/**
 * The space battle's ground shield settings.
 * @author akarnokd, 2011.08.16.
 */
public class BattleGroundShield {
    /** The normal image. */
    public BufferedImage normal;
    /** The alien/alternative image. */
    public BufferedImage alternative;
    /** The symmetrically trimmed width of the object image at 0 rotation angle. */
    public int trimmedWidth;
    /** The symmetrically trimmed height of the object image at 0 rotation angle. */
    public int trimmedHeight;
    /** The destruction sound. */
    public SoundType destructionSound;
    /** The animation of the destruction explosion. */
    public ExplosionType destructionExplosion;
    /** The shield percentage. */
    public int shields;
    /** The information image. */
    public String infoImageName;
}
