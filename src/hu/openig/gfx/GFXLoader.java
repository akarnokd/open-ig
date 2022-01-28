/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.gfx;

import hu.openig.core.Anim;
import hu.openig.core.Btn2;
import hu.openig.core.Btn3;
import hu.openig.core.Btn3H;
import hu.openig.core.Cat;
import hu.openig.core.Img;
import hu.openig.model.ResourceLocator;
import hu.openig.utils.Exceptions;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;

/**
 * Utility class to load graphics into annotated fields.
 * @author akarnokd, 2009.11.09.
 */
public final class GFXLoader {
    /** Constructor. */
    private GFXLoader() {
        // utility class
    }
    /**
     * Load resources of the given annotated target object.
     * @param target the target object
     * @param rl the resource locator
     */
    public static void loadResources(Object target, ResourceLocator rl) {
        try {
            for (Field f : target.getClass().getFields()) {
                Img ia = f.getAnnotation(Img.class);
                if (ia != null) {
                    f.set(target, rl.getImage(ia.name()));
                } else {
                    Btn2 ib2 = f.getAnnotation(Btn2.class);
                    if (ib2 != null) {
                        f.set(target, getButton2(rl, ib2.name()));
                    } else {
                        Btn3 ib3 = f.getAnnotation(Btn3.class);
                        if (ib3 != null) {
                            f.set(target, getButton3(rl, ib3.name()));
                        } else {
                            Cat ic = f.getAnnotation(Cat.class);
                            if (ic != null) {
                                f.set(target, getCategory(rl, ic.name()));
                            } else {
                                Anim ian = f.getAnnotation(Anim.class);
                                if (ian != null) {
                                    f.set(target, rl.getAnimation(ian.name(), ian.width(), ian.step()));
                                } else {
                                    Btn3H ib3h = f.getAnnotation(Btn3H.class);
                                    if (ib3h !=  null) {
                                        f.set(target, getButton3H(rl, ib3h.name()));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IllegalAccessException ex) {
            Exceptions.add(ex);
        }
    }
    /**
     * Get a two phase button image (with and without _pressed).
     * @param rl the resource locator
     * @param name the button name
     * @return the array cointaining the normal and the pressed state
     */
    private static BufferedImage[] getButton2(ResourceLocator rl, String name) {
        return new BufferedImage[] {
            rl.getImage(name),
            rl.getImage(name + "_pressed")
        };
    }
    /**
     * Get a two phase button image (with and without _selected).
     * @param rl the resource locator
     * @param name the button name
     * @return the array cointaining the normal and the pressed state
     */
    private static BufferedImage[] getCategory(ResourceLocator rl, String name) {
        return new BufferedImage[] {
            rl.getImage(name),
            rl.getImage(name + "_selected")
        };
    }
    /**
     * Get a three phase button image (normal, _selected_pressed, _selected).
     * @param rl the resource locator
     * @param name the resource name
     * @return the array cointaining the normal, selected and pressed state
     */
    private static BufferedImage[] getButton3(ResourceLocator rl, String name) {
        return new BufferedImage[] {
            rl.getImage(name),
            rl.getImage(name + "_selected_pressed"),
            rl.getImage(name + "_selected")
        };
    }
    /**
     * Get a three phase button image (normal, _pressed, _hovered).
     * @param rl the resource locator
     * @param name the button name
     * @return the array cointaining the normal, pressed and hovered state
     */
    private static BufferedImage[] getButton3H(ResourceLocator rl, String name) {
        return new BufferedImage[] {
            rl.getImage(name),
            rl.getImage(name + "_pressed"),
            rl.getImage(name + "_hovered")
        };
    }
}
