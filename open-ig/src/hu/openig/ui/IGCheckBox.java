/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ui;

import hu.openig.utils.Exceptions;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JCheckBox;

/**
 * The Imperium Galactica style checkbox.
 * @author akarnokd, 2011.12.30.
 */
public class IGCheckBox extends JCheckBox {
	/** */
	private static final long serialVersionUID = -5984826744059866766L;
	/** The check icon. */
	protected BufferedImage check;
	/** The check icon grayscale. */
	protected BufferedImage checkGs;
	/** 
	 * Constructor.
	 * @param text the text
	 * @param font the font
	 */
	public IGCheckBox(String text, Font font) {
		super();
		setOpaque(false);
		setText(text);
		setFont(font);
		try {
			check = ImageIO.read(getClass().getResource("/hu/openig/gfx/checkmark.png"));
			checkGs = ImageIO.read(getClass().getResource("/hu/openig/gfx/checkmark_grayscale.png"));
		} catch (IOException ex) {
			Exceptions.add(ex);
		}
	}
	@Override
	public Dimension getPreferredSize() {
		FontMetrics fm = getFontMetrics(getFont());
		int h = Math.max(check.getHeight(), fm.getHeight());
		int w = fm.getHeight() + 10 + fm.stringWidth(getText());
		return new Dimension(w, h);
	};
	@Override
	public void paint(Graphics g) {
		
		Graphics2D g2 = (Graphics2D)g;
		
		Object rh = g2.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		g2.setFont(getFont());
		
		int size = 16;
		if (isEnabled()) {
			g2.setColor(getForeground());
		} else {
			g2.setColor(Color.GRAY);
		}
		g2.drawRect(0, check.getHeight() - size, size - 1, size - 1);
		g2.drawRect(1, check.getHeight() - size + 1, size - 3, size - 3);
		
		if (isSelected()) {
			if (isEnabled()) {
				g2.drawImage(check, 0, 0, null);
			} else {
				g2.drawImage(checkGs, 0, 0, null);
			}
		}
		
		int dy = check.getHeight();
		
		g2.drawString(getText(), size + 6, dy);

		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, rh);
	}
}
