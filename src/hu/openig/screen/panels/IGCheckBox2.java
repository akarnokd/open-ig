/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.panels;

import hu.openig.model.Trait;
import hu.openig.ui.IGCheckBox;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JToolTip;

/**
 * A checkbox with custom tooltip.
 * @author akarnokd, 2013.04.25.
 */
public class IGCheckBox2 extends IGCheckBox {
    /** */
    private static final long serialVersionUID = -4517019104964592795L;
    /** The associatd trait. */
    public Trait trait;
    /**
     * Constructor.
     * @param text the text
     * @param font the font
     * @param trait the associated trait
     */
    public IGCheckBox2(String text, Font font, Trait trait) {
        super(text, font);
        this.trait = trait;
    }
    @Override
    public JToolTip createToolTip() {
        JToolTip tip = new JToolTip();
        tip.setForeground(Color.BLACK);
        tip.setBackground(Color.YELLOW);
        tip.setFont(getFont());
        return tip;
    }
}