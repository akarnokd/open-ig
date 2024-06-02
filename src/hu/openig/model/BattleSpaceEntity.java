/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * The definition of a battle entity having a normal and alien
 * rotation images and a huge-image.
 * @author akarnokd, Jul 31, 2011
 */
public class BattleSpaceEntity {
    /** The image used for displaying details of the selected entity. */
    public String infoImageName;
    /** The normal rotation image. */
    public BufferedImage[] normal;
    /** The alternative rotation image. */
    public BufferedImage[] alternative;
    /** The symmetrically trimmed width of the object image at 0 rotation angle. */
    public int trimmedWidth;
    /** The symmetrically trimmed height of the object image at 0 rotation angle. */
    public int trimmedHeight;
    /** The sound effect for destruction explosion. */
    public SoundType destructionSound;
    /** The animation of the destruction explosion. */
    public ExplosionType destructionExplosion;
    /** The rotation speed: full rotation time in milliseconds. */
    public int rotationTime;
    /** The movement speed: milliseconds per pixel. */
    public int movementSpeed;
    /** The default hitpoints. */
    public int hp;
    /** The battle efficiency settings of this projector. */
    public final List<BattleEfficiencyModel> efficiencies = new ArrayList<>();
}
