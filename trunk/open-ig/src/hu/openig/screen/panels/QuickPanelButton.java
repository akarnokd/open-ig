/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.panels;

import hu.openig.core.Action0;
import hu.openig.render.TextRenderer;
import hu.openig.screen.CommonResources;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Button;
import hu.openig.ui.UIMouse.Type;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * A quick panel's button with a numerical value displayed. 
 * @author akarnokd, 2012.06.25.
 */
public class QuickPanelButton extends UIComponent {
	/** The common resources. */
	final CommonResources commons;
	/** The menu icon width. */
	final int menuIconWidth;
	/** The left click action. */
	public Action0 onLeftClick;
	/** The right click action. */
	public Action0 onRightClick;
	/** The text to display. */
	public String text;
	/** The image to display. */
	public BufferedImage icon;
	/** The max text width. */
	int textMaxWidth;
	/** Is the text visible? */
	public boolean textVisible;
	/** The text color. */
	public int textColor = TextRenderer.YELLOW;
	/**
	 * Initialize the button.
	 * @param commons the common resources
	 * @param pattern the pattern
	 * @param menuIconWidth the icon width
	 */
	public QuickPanelButton(CommonResources commons, 
			String pattern, int menuIconWidth) {
		this.commons = commons;
		this.menuIconWidth = menuIconWidth;
		textMaxWidth = commons.text().getTextWidth(10, pattern);
		width = textMaxWidth + menuIconWidth;
		height = 20;
	}
	@Override
	public void draw(Graphics2D g2) {
		if (textVisible) {
			int w = commons.text().getTextWidth(10, text);
			int x0 = menuIconWidth + (textMaxWidth - w) / 2;
			commons.text().paintTo(g2, x0, 0 + 4, 10, textColor, text);
		}
		
		g2.drawImage(commons.statusbar().iconBack, 0, 0, null);
		g2.drawImage(icon, 0 + 7, 0 + 3, null);
	}
	@Override
	public boolean mouse(UIMouse e) {
		if (e.has(Type.DOWN)) {
			if (e.has(Button.LEFT) && onLeftClick != null) {
				onLeftClick.invoke();
				return true;
			}
			if (e.has(Button.RIGHT) && onRightClick != null) {
				onRightClick.invoke();
				return true;
			}
		}
		return super.mouse(e);
	}
}
