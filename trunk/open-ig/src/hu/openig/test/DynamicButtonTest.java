/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.test;

import hu.openig.utils.ImageUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
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
	/** Button image's zones. */
	static class GenericButton {
		/** The top-left corner image. */
		public BufferedImage topLeft;
		/** The top-center filler image. */
		public BufferedImage topCenter;
		/** The top-right corner image. */
		public BufferedImage topRight;
		/** The left-middle filler image. */
		public BufferedImage leftMiddle;
		/** The right-middle filler image. */
		public BufferedImage rightMiddle;
		/** The bottom-left corner image. */
		public BufferedImage bottomLeft;
		/** The bottom-center filler image. */
		public BufferedImage bottomCenter;
		/** The bottom-right corner image. */
		public BufferedImage bottomRight;
		/**
		 * Load and split the given base button image.
		 * The origina is expected to be 103x28 image.
		 * @param name the resource name
		 */
		public void loadAndSplit(String name) {
			try {
				InputStream in = getClass().getResource(name).openStream();
				try {
					BufferedImage img = ImageIO.read(in);
					
					topLeft = ImageUtils.newSubimage(img, 0, 0, 40, 6);
					bottomLeft = ImageUtils.newSubimage(img, 0, 22, 40, 6);
					topRight = ImageUtils.newSubimage(img, 63, 0, 40, 6);
					bottomRight = ImageUtils.newSubimage(img, 63, 22, 40, 6);
					topCenter = ImageUtils.newSubimage(img, 39, 0, 1, 6);
					bottomCenter = ImageUtils.newSubimage(img, 40, 22, 1, 6);
					leftMiddle = ImageUtils.newSubimage(img, 0, 7, 4, 1);
					rightMiddle = ImageUtils.newSubimage(img, 99, 7, 4, 1);
					
				} finally {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		/**
		 * Draw the generic button graphics to the target canvas.
		 * @param g2 the target graphics2d canvas
		 * @param x the leftmost coordinate
		 * @param y the rightmost coordinate
		 * @param width the full width of the button, must be at least 103
		 * @param height the full height of the button, must be at least 28
		 * @param down should compensate for the pressed-down state?
		 * @param text the display text of the button
		 */
		public void paintTo(Graphics2D g2, int x, int y, 
				int width, int height, boolean down, String text) {
//			g2.scale(3, 3);
			g2.drawImage(topLeft, x, y, null);
			g2.drawImage(topRight, x + width - topRight.getWidth(), y, null);
			g2.drawImage(bottomLeft, x, y + height - bottomLeft.getHeight(), null);
			g2.drawImage(bottomRight, x + width - bottomRight.getWidth(), y + height - bottomRight.getHeight(), null);
			
			Paint save = g2.getPaint();
			
			g2.setPaint(new TexturePaint(topCenter, new Rectangle(x, y, topCenter.getWidth(), topCenter.getHeight())));
			g2.fillRect(x + topLeft.getWidth(), y, width - topLeft.getWidth() - topRight.getWidth(), topCenter.getHeight());
			
			g2.setPaint(new TexturePaint(bottomCenter, new Rectangle(x, y + height - bottomLeft.getHeight(), bottomCenter.getWidth(), bottomCenter.getHeight())));
			g2.fillRect(x + bottomLeft.getWidth(), y + height - bottomLeft.getHeight(), 
					width - bottomLeft.getWidth() - bottomRight.getWidth(), bottomCenter.getHeight());
			
			g2.setPaint(new TexturePaint(leftMiddle, new Rectangle(x, y + topLeft.getHeight(), leftMiddle.getWidth(), leftMiddle.getHeight())));
			g2.fillRect(x, y + topLeft.getHeight(), leftMiddle.getWidth(), height - topLeft.getHeight() - bottomLeft.getHeight());

			g2.setPaint(new TexturePaint(rightMiddle, new Rectangle(x + width - rightMiddle.getWidth(), y + topLeft.getHeight(), rightMiddle.getWidth(), rightMiddle.getHeight())));
			g2.fillRect(x + width - rightMiddle.getWidth(), y + topRight.getHeight(), rightMiddle.getWidth(), height - topRight.getHeight() - bottomRight.getHeight());
			
			int dx = down ? 0 : -1;
			
			GradientPaint grad = new GradientPaint(
					new Point(x + leftMiddle.getWidth(), y + topLeft.getHeight() + dx),
					new Color(0xFF4C7098),
					new Point(x + leftMiddle.getWidth(), y + height - bottomLeft.getHeight()),
					new Color(0xFF805B71)
			);
			g2.setPaint(grad);
			g2.fillRect(x + leftMiddle.getWidth() + dx, y + topLeft.getHeight() + dx, width - leftMiddle.getWidth() - rightMiddle.getWidth() - 2 * dx, height - topCenter.getHeight() - bottomCenter.getHeight() - dx);
			g2.setPaint(save);

			FontMetrics fm = g2.getFontMetrics();
			int tw = fm.stringWidth(text);
			int th = fm.getHeight();
			
			int tx = x + (width - tw) / 2 + dx;
			int ty = y + (height - th) / 2 + dx;
			
			g2.setColor(new Color(0x509090));
			g2.drawString(text, tx + 1, ty + 1 + fm.getAscent());
			g2.setColor(Color.BLACK);
			g2.drawString(text, tx, ty + fm.getAscent());
			
			if (down) {
				g2.setColor(new Color(0, 0, 0, 92));
				g2.fillRect(x + 3, y + 3, width - 5, 4);
				g2.fillRect(x + 3, y + 7, 4, height - 9);
			}

		}
	}
	/** Images for the normal button. */
	GenericButton normal;
	/** Images for the pressed button. */
	GenericButton pressed;
	/** The canvas component to paint to. */
	class Canvas extends JComponent {
		/** */
		private static final long serialVersionUID = 1479267741896678559L;
		/** The indicator for th button's pressed state. */
		boolean isDown;
		/** The button's text. */
		String text = "";
		@Override
		public void paint(Graphics g) {
			g.setFont(g.getFont().deriveFont(Font.BOLD));
			FontMetrics fm = g.getFontMetrics();
			int tw = fm.stringWidth(text);
			int th = fm.getHeight();
			int width = Math.max(103, tw + 12);
			int height = Math.max(28, th + 12);
			if (isDown) {
				pressed.paintTo((Graphics2D)g, 10, 10, width, height, isDown, text);
			} else {
				normal.paintTo((Graphics2D)g, 10, 10, width, height, isDown, text);
			}
		}
	}
	/** Constructor. Builds the GUI. */
	public DynamicButtonTest() {
		super("Dynamic button");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		normal = new GenericButton();
		pressed = new GenericButton();
		
		normal.loadAndSplit("/hu/openig/gfx/button_base.png");
		pressed.loadAndSplit("/hu/openig/gfx/button_base_pressed.png");
	
		
		Container c = getContentPane();
		final Canvas cv = new Canvas();
		c.add(cv, BorderLayout.CENTER);

		final JTextField buttonText = new JTextField();
		c.add(buttonText, BorderLayout.NORTH);
		buttonText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cv.text = buttonText.getText();
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
