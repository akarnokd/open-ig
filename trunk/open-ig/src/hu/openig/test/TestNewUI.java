/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Test the event response of the new UI mini framework.
 * @author karnok, 2011.02.25.
 */
public class TestNewUI extends JFrame {
	/** */
	private static final long serialVersionUID = 7179590260742963280L;
	/** The main container. */
	private ColorChangerContainer cc1;
	/**
	 * Build the GUI.
	 */
	public TestNewUI() {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		cc1 = new ColorChangerContainer();
		cc1.width = 300;
		cc1.height = 300;
		
		ColorChangerContainer cc2 = new ColorChangerContainer();
		cc2.location(25, 25);
		cc2.size(250, 250);
		
		ColorChanger c1 = new ColorChanger();
		c1.location(50, 50);
		c1.size(50, 50);
		
		ColorChanger c2 = new ColorChanger();
		c2.location(150, 50);
		c2.size(50, 50);

		ColorChanger c3 = new ColorChanger();
		c3.location(50, 150);
		c3.size(50, 50);

		cc2.add(c1, c2, c3);
		cc1.add(cc2);
		MouseAdapter ma = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (cc1.mouse(UIMouse.from(e))) {
					repaint();
				}
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				if (cc1.mouse(UIMouse.from(e))) {
					repaint();
				}
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				if (cc1.mouse(UIMouse.from(e))) {
					repaint();
				}
			}
			@Override
			public void mouseExited(MouseEvent e) {
				if (cc1.mouse(UIMouse.from(e))) {
					repaint();
				}
			}
			@Override
			public void mouseMoved(MouseEvent e) {
				if (cc1.mouse(UIMouse.from(e))) {
					repaint();
				}
			}
			@Override
			public void mousePressed(MouseEvent e) {
				if (cc1.mouse(UIMouse.from(e))) {
					repaint();
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if (cc1.mouse(UIMouse.from(e))) {
					repaint();
				}
			}
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (cc1.mouse(UIMouse.from(e))) {
					repaint();
				}
			}
		};
		JComponent cp = new JComponent() {
			/** */
			private static final long serialVersionUID = 1573861064090356038L;
			{
				setOpaque(true);
			}
			@Override
			public void paint(Graphics g) {
				cc1.draw((Graphics2D)g);
			}
		};
		cp.addMouseListener(ma);
		cp.addMouseMotionListener(ma);
		cp.addMouseWheelListener(ma);
		getContentPane().add(cp);
	}
	/**
	 * Component that changes color when the mouse
	 * enters or exits.
	 * @author karnok, 2011.02.25.
	 */
	class ColorChanger extends UIComponent {
		@Override
		public void draw(Graphics2D g2) {
			if (over) {
				g2.setColor(Color.GREEN);
			} else {
				g2.setColor(Color.RED);
			}
			g2.fillRect(0, 0, width, height);
		}
		@Override
		public boolean mouse(UIMouse e) {
			return e.type == UIMouse.Type.ENTER || e.type == UIMouse.Type.LEAVE;
		}
	}
	/**
	 * Component that changes color when the mouse
	 * enters or exits.
	 * @author karnok, 2011.02.25.
	 */
	class ColorChangerContainer extends UIContainer {
		@Override
		public void draw(Graphics2D g2) {
			if (over) {
				g2.setColor(Color.BLUE);
			} else {
				g2.setColor(Color.YELLOW);
			}
			g2.fillRect(0, 0, width, height);
			super.draw(g2);
		}
		@Override
		public boolean mouse(UIMouse e) {
			boolean r = super.mouse(e);
			return r || e.type == UIMouse.Type.ENTER || e.type == UIMouse.Type.LEAVE;
		}
	}
	/**
	 * @param args no arguments
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				TestNewUI n = new TestNewUI();
				n.setSize(800, 600);
				n.setLocationRelativeTo(null);
				n.setVisible(true);
			}
		});
	}

}
