/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ui;

import hu.openig.render.GenericMediumButton;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JButton;

/**
 * A large Imperium Galactica button.
 * @author akarnokd, 2011.12.30.
 */
public class IGButton extends JButton {
	/** */
	private static final long serialVersionUID = -942400292186201786L;
	/** The large button. */
	private GenericMediumButton largeButton;
	/** The large button pressed. */
	private GenericMediumButton largeButtonPressed;

	/** Constructor. Initializes the button graphics. */
	public IGButton() {
		super();
		setOpaque(true);
		largeButton = new GenericMediumButton("/hu/openig/gfx/button_medium.png");
		largeButtonPressed = new GenericMediumButton("/hu/openig/gfx/button_medium_pressed.png");
	}
	@Override
	public Dimension getPreferredSize() {
		return largeButton.getPreferredSize(getFontMetrics(getFont()), getText());
	}
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g.setColor(getForeground());
		if (isSelected()) {
			largeButtonPressed.paintTo(g2, 0, 0, getWidth(), getHeight(), true, getText());
		} else {
			largeButtonPressed.paintTo(g2, 0, 0, getWidth(), getHeight(), true, getText());
		}
	}
}
