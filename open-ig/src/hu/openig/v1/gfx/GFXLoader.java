/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.gfx;

import hu.openig.utils.ImageUtils;
import hu.openig.v1.ResourceLocator;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;

/**
 * @author karnok, 2009.11.09.
 * @version $Revision 1.0$
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
	 * @param language the language
	 */
	public static void loadResources(Object target, ResourceLocator rl, String language) {
		try {
			for (Field f : target.getClass().getFields()) {
				Img ia = f.getAnnotation(Img.class);
				if (ia != null) {
					f.set(target, rl.getImage(language, ia.name()));
				} else {
					Btn2 ib2 = f.getAnnotation(Btn2.class);
					if (ib2 != null) {
						f.set(target, getButton2(rl, language, ib2.name()));
					} else {
						Btn3 ib3 = f.getAnnotation(Btn3.class);
						if (ib3 != null) {
							f.set(target, getButton3(rl, language, ib3.name()));
						} else {
							Cat ic = f.getAnnotation(Cat.class);
							if (ic != null) {
								f.set(target, getCategory(rl, language, ic.name()));
							} else {
								Anim ian = f.getAnnotation(Anim.class);
								if (ian != null) {
									f.set(target, getAnim(rl, language, ian.name(), ian.width()));
								}
							}
						}
					}
				}
			}
			if (target instanceof ResourceSelfLoader) {
				((ResourceSelfLoader)target).load(rl, language);
			}
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Get a multi-phase animation by splitting the target image.
	 * @param rl the resource locator
	 * @param language the target language
	 * @param name the button name
	 * @param width the phase width
	 * @return the array.
	 */
	private static BufferedImage[] getAnim(ResourceLocator rl, String language, String name, int width) {
		BufferedImage img = rl.getImage(language, name);
		BufferedImage[] result = new BufferedImage[img.getWidth() / width];
		for (int i = 0; i < result.length; i++) {
			result[i] = ImageUtils.newSubimage(img, i * width, 0, width, img.getHeight());
		}
		return result;
	}
	/**
	 * Get a two phase button image (with and without _pressed).
	 * @param rl the resource locator
	 * @param language the target language
	 * @param name the button name
	 * @return the array cointaining the normal and the pressed state
	 */
	private static BufferedImage[] getButton2(ResourceLocator rl, String language, String name) {
		return new BufferedImage[] {
			rl.getImage(language, name),
			rl.getImage(language, name + "_pressed")
		};
	}
	/**
	 * Get a two phase button image (with and without _selected).
	 * @param rl the resource locator
	 * @param language the target language
	 * @param name the button name
	 * @return the array cointaining the normal and the pressed state
	 */
	private static BufferedImage[] getCategory(ResourceLocator rl, String language, String name) {
		return new BufferedImage[] {
			rl.getImage(language, name),
			rl.getImage(language, name + "_selected")
		};
	}
	/**
	 * Get a three phase button image (normal, _selected_pressed, _selected).
	 * @param rl the resource locator
	 * @param language the target language
	 * @param name the button name
	 * @return the array cointaining the normal and the pressed state
	 */
	private static BufferedImage[] getButton3(ResourceLocator rl, String language, String name) {
		return new BufferedImage[] {
			rl.getImage(language, name),
			rl.getImage(language, name + "_selected"),
			rl.getImage(language, name + "_selected_pressed")
		};
	}
}
