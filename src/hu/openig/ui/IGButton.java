/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ui;

import hu.openig.render.GenericMediumButton;
import hu.openig.render.RenderTools;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
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
    /** The disabled pattern. */
    private BufferedImage disabledPattern;

    /** Constructor. Initializes the button graphics. */
    public IGButton() {
        super();
        setOpaque(false);
        largeButton = new GenericMediumButton("/hu/openig/gfx/button_medium.png");
        largeButtonPressed = new GenericMediumButton("/hu/openig/gfx/button_medium_pressed.png");
        int[] disabled = { 0xFF000000, 0xFF000000, 0, 0, 0xFF000000, 0, 0, 0, 0 };
        disabledPattern = new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB);
        disabledPattern.setRGB(0, 0, 3, 3, disabled, 0, 3);
    }
    /**
     * Constructor. Sets the text.
     * @param text the text
     */
    public IGButton(String text) {
        this();
        setText(text);
    }
    @Override
    public Dimension getPreferredSize() {
        return largeButton.getPreferredSize(getFontMetrics(getFont()), getText());
    }
    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g.setFont(getFont());
        g.setColor(getForeground());
        if (getModel().isPressed()) {
            largeButtonPressed.paintTo(g2, 0, 0, getWidth(), getHeight(), true, getText());
        } else {
            largeButton.paintTo(g2, 0, 0, getWidth(), getHeight(), false, getText());
        }
        Icon icon = getIcon();
        if (icon != null) {
            int w = (getWidth() - icon.getIconWidth()) / 2;
            int h = (getHeight() - icon.getIconHeight()) / 2;
            icon.paintIcon(this, g, w, h);
        }
        if (!isEnabled()) {
            RenderTools.fill(g2, 0, 0, getWidth(), getHeight(), disabledPattern);
        }
    }
}
