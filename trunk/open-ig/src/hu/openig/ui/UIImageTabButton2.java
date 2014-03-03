/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ui;

import hu.openig.core.Action0;
import hu.openig.render.RenderTools;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * A three state image button with normal, select+pressed and selected state.
 * The difference from <code>UIImageButton</code>
 * that when clicked, the button remains in the selected state.
 * You must manually remove the selected state.
 * 
 * @author akarnokd, 2011.02.26.
 */
public class UIImageTabButton2 extends UIComponent {
	/** The normal state image. */
	protected BufferedImage normalImage;
	/** The pressed state image. */
	protected BufferedImage selectedPressedImage;
	/** The hovered state image. */
	protected BufferedImage selectedImage;
	/** The disabled pattern to use for the button. */
	protected BufferedImage disabledPattern;
	/** The action to invoke when the button is clicked. */
	public Action0 onClick;
	/** 
	 * The action to invoke when the button is pressed down.
	 * Can be used to use this button as a tab.
	 */
	public Action0 onPress;
	/** Is the mouse pressed down on this component. */
	public boolean down;
	/** Indicates the button is in selected state. */
	public boolean selected;
	/**
	 * Constructor with the default images.
	 * @param normalImage the normal state image
	 * @param selectedPressedImage the selected+pressed image
	 * @param selectedImage the selected image
	 */
	public UIImageTabButton2(BufferedImage normalImage, 
			BufferedImage selectedPressedImage, BufferedImage selectedImage) {
		this.normalImage = normalImage;
		this.selectedPressedImage = selectedPressedImage;
		this.selectedImage = selectedImage;
		this.width = normalImage.getWidth();
		this.height = normalImage.getHeight();
	}
	/**
	 * Creates an image button by using the elements of the supplied array.
	 * The first element is the normal image, the second should be the pressed image
	 * and an optional third image should be the hovered image. If
	 * no hovered image is specified, the normal image is used instead.
	 * You may use this constructor with the resource BufferedImage arrays of buttons
	 * @param images the array of images.
	 */
	public UIImageTabButton2(BufferedImage[] images) {
		this.normalImage = images[0];
		this.selectedPressedImage = images[1];
		this.selectedImage = images.length > 2 ? images[2] : selectedPressedImage;
		this.width = normalImage.getWidth();
		this.height = normalImage.getHeight();
	}
	/**
	 * Call the click action if set.
	 */
	protected void doClick() {
		if (onClick != null) {
			onClick.invoke();
		}
	}
	/** Call the press action if set. */
	protected void doPress() {
		if (onPress != null) {
			onPress.invoke();
		}
	}
	@Override
	public void draw(Graphics2D g2) {
		if (!enabled && disabledPattern != null) {
			g2.drawImage(normalImage, 0, 0, null);
			RenderTools.fill(g2, 0, 0, width, height, disabledPattern);
		} else
		if (down) {
			g2.drawImage(selectedPressedImage, 0, 0, null);
		} else
		if (selected) {
			g2.drawImage(selectedImage, 0, 0, null);
		} else {
			g2.drawImage(normalImage, 0, 0, null);
		}
	}
	@Override
	public boolean mouse(UIMouse e) {
		switch (e.type) {
		case DOWN:
			down = true;
			selected = true;
			doPress();
			return true;
		case CLICK:
			doClick();
			return true;
		case DOUBLE_CLICK:
			for (int i = 0; i < e.z - 1; i++) {
				doClick();
			}
			return true;
		case UP:
		case LEAVE:
			down = false;
			return true;
		default:
			return false;
		}
	}
	/**
	 * Set the disabled pattern for this button.
	 * @param pattern the pattern to fill with the area of the button when it is disabled
	 * @return this
	 */
	public UIImageTabButton2 disabledPattern(BufferedImage pattern) {
		this.disabledPattern = pattern;
		return this;
	}
}
