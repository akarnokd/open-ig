/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.test;


import hu.openig.render.GenericButtonRenderer;
import hu.openig.render.GenericLargeButton;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Destbed for the new dynamic buttons used for creating buttons labeled arbitrarily.
 * @author karnokd
 *
 */
public class DynamicButtonTest extends JFrame {
	/** */
	private static final long serialVersionUID = 5107635280873032348L;
	/** Images for the normal button. */
	GenericButtonRenderer normal;
	/** Images for the pressed button. */
	GenericButtonRenderer pressed;
	/** The canvas component to paint to. */
	class Canvas extends JComponent {
		/** */
		private static final long serialVersionUID = 1479267741896678559L;
		/** The indicator for th button's pressed state. */
		boolean isDown;
		/** The button's text. */
		String text = "";
		/** Initialize. */
		public Canvas() {
			setOpaque(true);
		}
		@Override
		public void paint(Graphics g) {
			g.setColor(Color.YELLOW);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setFont(g.getFont().deriveFont(Font.BOLD));
			FontMetrics fm = g.getFontMetrics();
			Dimension d = normal.getPreferredSize(fm, text);
			if (isDown) {
				pressed.paintTo((Graphics2D)g, 0, 0, d.width, d.height, isDown, text);
			} else {
				normal.paintTo((Graphics2D)g, 0, 0, d.width, d.height, isDown, text);
			}
		}
		@Override
		public Dimension getPreferredSize() {
			return normal.getPreferredSize(getFontMetrics(getFont().deriveFont(Font.BOLD)), text);
		}
		@Override
		public Dimension getMaximumSize() {
			return getPreferredSize();
		}
		@Override
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}
		/**
		 * Set the text content.
		 * @param text the text
		 */
		public void setText(String text) {
			this.text = text;
			invalidate();
		}
	}
	/** Constructor. Builds the GUI. */
	public DynamicButtonTest() {
		super("Dynamic button");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		normal = new GenericLargeButton("/hu/openig/gfx/button_large.png");
		pressed = new GenericLargeButton("/hu/openig/gfx/button_large_pressed.png");
		
		
		Container c = getContentPane();
		final Canvas cv = new Canvas();
		final JTextField buttonText = new JTextField();

		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addComponent(buttonText)
			.addComponent(cv)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(buttonText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(cv)
		);
		
		buttonText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cv.setText(buttonText.getText());
				repaint();
			}
		});
		
		cv.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					cv.isDown = true;
					repaint();
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					cv.isDown = false;
					repaint();
				}
			}
		});
		
		setSize(600, 400);
	}
	/**
	 * @param args no arguments
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				DynamicButtonTest frame = new DynamicButtonTest();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}

}
