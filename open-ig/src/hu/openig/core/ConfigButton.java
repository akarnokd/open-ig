/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.core;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.ButtonModel;
import javax.swing.JButton;

/**
 * Special rounded rectangle toggle button.
 * @author akarnokd, 2009.09.23.
 */
public class ConfigButton extends JButton {
	/** */
	private static final long serialVersionUID = -2759017088425629378L;
	/**
	 * Constructor. Sets the text.
	 * @param text the title
	 */
	public ConfigButton(String text) {
		super(text);
		setOpaque(false);
	}
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setFont(getFont());
		FontMetrics fm = g2.getFontMetrics();
		
		ButtonModel mdl = getModel();
		String s = getText();
		
		g2.setComposite(AlphaComposite.SrcOver.derive(0.85f));
		if (!mdl.isEnabled()) {
			g2.setColor(new Color(0x808080));
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
			g2.setColor(new Color(0x000000));
		} else
		if (mdl.isPressed() || mdl.isSelected()) {
			if (mdl.isRollover()) {
				g2.setColor(new Color(0xE0E0E0));
			} else {
				g2.setColor(new Color(0xFFFFFF));
			}
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
			g2.setColor(new Color(0x000000));
		} else {
			if (mdl.isRollover()) {
				g2.setColor(new Color(0x000000));
			} else {
				g2.setColor(new Color(0x202020));
			}
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
			g2.setColor(new Color(0xFFFFFFFF));
		}
		int x = (getWidth() - fm.stringWidth(s)) / 2;
		int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent() + fm.getLeading();
		g2.drawString(s, x, y);
	}
}
