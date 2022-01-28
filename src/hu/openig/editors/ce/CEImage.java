/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * An image rendering component.
 * @author akarnokd, 2012.11.02.
 */
public class CEImage extends JComponent {
    /** */
    private static final long serialVersionUID = -7956863168562025891L;
    /** the icon to draw. */
    protected BufferedImage icon;
    /** Show a modal dialog? */
    protected boolean modal;
    /** Constructor. */
    public CEImage() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (icon != null) {
                    display();
                }
            }
        });
    }
    /**
     * Display a window with the full image.
     */
    protected void display() {
        if (modal) {
            JDialog f = new JDialog();
            f.setModal(true);

            f.getContentPane().add(new JLabel(String.format("%d x %d", icon.getWidth(), icon.getHeight())), BorderLayout.NORTH);
            f.getContentPane().add(new JLabel(new ImageIcon(icon)), BorderLayout.CENTER);
            f.pack();
            f.setLocationRelativeTo(this);
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setVisible(true);
        } else {
            JFrame f = new JFrame();

            f.getContentPane().add(new JLabel(String.format("%d x %d", icon.getWidth(), icon.getHeight())), BorderLayout.NORTH);
            f.getContentPane().add(new JLabel(new ImageIcon(icon)), BorderLayout.CENTER);
            f.pack();
            f.setLocationRelativeTo(this);
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setVisible(true);
        }
    }
    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(Color.RED);
        g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        if (icon != null) {
            int iw = icon.getWidth();
            int ih = icon.getHeight();

            double scalex = 1d * getWidth() / iw;
            double scaley = 1d * getHeight() / ih;

            double scale = Math.min(scalex, scaley);

            AffineTransform at = g2.getTransform();

            int dx = (int)((getWidth() - iw * scale) / 2);
            int dy = (int)((getHeight() - ih * scale) / 2);
            g2.translate(dx, dy);
            g2.scale(scale, scale);

            g2.drawImage(icon, 0, 0, null);

            g2.setTransform(at);
        }
    }
    /**
     * @return the icon
     */
    public BufferedImage getIcon() {
        return icon;
    }
    /**
     * Sets the icon.
     * @param icon the icon or null for no display
     */
    public void setIcon(BufferedImage icon) {
        this.icon = icon;
        if (icon != null) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
        repaint();
    }
    /**
     * The dialog displayed by clicking should be modal?
     * @param modal true if use modal
     */
    public void setModal(boolean modal) {
        this.modal = modal;
    }
}
