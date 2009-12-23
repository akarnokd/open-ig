/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * The base game window which handles paint and input events.
 * @author karnokd, 2009.12.23.
 * @version $Revision 1.0$
 */
public class GameWindow extends JFrame {
	/**	 */
	private static final long serialVersionUID = 4521036079508511968L;
	/** 
	 * The component that renders the primary and secondary screens into the current window.
	 * @author karnokd, 2009.12.23.
	 * @version $Revision 1.0$
	 */
	class ScreenRenderer extends JComponent {
		/** */
		private static final long serialVersionUID = -4538476567504582641L;
		/** Constructor. */
		public ScreenRenderer() {
			addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					if (primary != null) {
						primary.onResize();
					}
					if (secondary != null) {
						secondary.onResize();
					}
				}
			});
		}
		@Override
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D)g;
			if (primary != null) {
				primary.paintTo(g2);
			}
			if (secondary != null) {
				secondary.paintTo(g2);
			}
		}
	}
	/** The primary screen. */
	ScreenBase primary;
	/** The secondary screen drawn over the first. */
	ScreenBase secondary;
	/** The configuration object. */
	Configuration config;
	/** The common resource locator. */
	ResourceLocator rl;
	/** 
	 * Constructor. 
	 * @param config the configuration object.
	 */
	public GameWindow(Configuration config) {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		this.config = config;
		this.rl = config.newResourceLocator();
		
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		
		ScreenRenderer sr = new ScreenRenderer();
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addComponent(sr, 640, 640, Short.MAX_VALUE)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(sr, 480, 480, Short.MAX_VALUE)
		);
		pack();
		setMinimumSize(getSize());
		setLocationRelativeTo(null);
//		if (config.width > 0) {
//			setBounds(config.left, config.top, config.width, config.height);
//		}
		MouseActions ma = new MouseActions();
		sr.addMouseListener(ma);
		sr.addMouseMotionListener(ma);
		sr.addMouseWheelListener(ma);
		addKeyListener(new KeyEvents());
	}
	/**
	 * The common key manager.
	 * @author karnokd, 2009.12.24.
	 * @version $Revision 1.0$
	 */
	class KeyEvents extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			System.out.println("Press " + e.getKeyCode());
			if (secondary != null) {
				secondary.keyTyped(e.getKeyCode(), e.getModifiersEx());
			} else
			if (primary != null) {
				primary.keyTyped(e.getKeyCode(), e.getModifiersEx());
			}
		}
	}
	/**
	 * The common mouse action manager.
	 * @author karnokd, 2009.12.23.
	 * @version $Revision 1.0$
	 */
	class MouseActions extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			System.out.println("Press");
			if (secondary != null) {
				secondary.mousePressed(e.getButton(), e.getX(), e.getY(), e.getModifiersEx());
			} else
			if (primary != null) {
				primary.mousePressed(e.getButton(), e.getX(), e.getY(), e.getModifiersEx());
			}
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			if (secondary != null) {
				secondary.mousePressed(e.getButton(), e.getX(), e.getY(), e.getModifiersEx());
			} else
			if (primary != null) {
				primary.mousePressed(e.getButton(), e.getX(), e.getY(), e.getModifiersEx());
			}
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			if (secondary != null) {
				secondary.mouseMoved(e.getButton(), e.getX(), e.getY(), e.getModifiers());
			} else
			if (primary != null) {
				primary.mouseMoved(e.getButton(), e.getX(), e.getY(), e.getModifiers());
			}
		}
		@Override
		public void mouseMoved(MouseEvent e) {
			if (secondary != null) {
				secondary.mouseMoved(e.getButton(), e.getX(), e.getY(), e.getModifiers());
			} else
			if (primary != null) {
				primary.mouseMoved(e.getButton(), e.getX(), e.getY(), e.getModifiers());
			}
		}
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (secondary != null) {
				secondary.mouseScrolled(e.getUnitsToScroll(), e.getX(), e.getY(), e.getModifiers());
			} else
			if (primary != null) {
				primary.mouseScrolled(e.getUnitsToScroll(), e.getX(), e.getY(), e.getModifiers());
			}
		}
	}
}
