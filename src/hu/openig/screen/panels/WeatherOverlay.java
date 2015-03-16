/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.panels;

import hu.openig.model.WeatherType;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The weather effects overlay.
 * @author Daniel Bajor, 2012.11.17.
 */
public class WeatherOverlay {
	/** Number of drops. */
	public static final int MAX_DROPS = 2000;
	/** The horizontal margin. */
	public static final int MARGIN_X = 0;
	/** The vertical margin. */
	public static final int MARGIN_Y = 15;
	/** Rain mode velocity. */
	public static final double RAIN_VELOCITY = 30;
	/** Snow mode velocity. */
	public static final double SNOW_VELOCITY = 4;
	/** The individual drops. */
	private final List<WeatherDrop> drops = new ArrayList<>(MAX_DROPS);
	/** Current horizontal wind speed. */
	private double wind;
	/** The current weather type. */
	public WeatherType type = WeatherType.RAIN;
	/** The common random. */
	private final Random random = new Random(System.nanoTime());
	/** The base snow color. */
	public static final Color SNOW_COLOR = new Color(255, 255, 255);
	/** The base rain color. */
	public static final Color RAIN_COLOR = new Color(200, 200, 255);
	/** The rendering bounds. */
	public Dimension bounds;
	/** The light level. */
	public double alpha = 1d;
	/**
	 * Initialize the overlay.
	 * @param bounds the the rendering bounds.
	 */
	public WeatherOverlay(Dimension bounds) {
		this.bounds = bounds;
		for (int i = 0; i < MAX_DROPS; i++) {
			drops.add(new WeatherDrop(random.nextInt(bounds.width), random.nextInt(bounds.height)));
		}
	}
	/**
	 * Update the bounds of the overlay.
	 * @param newWidth the new width
	 * @param newHeight the new height
	 */
	public void updateBounds(int newWidth, int newHeight) {
		if (bounds.width != newWidth || bounds.height != newHeight) {
			bounds.width = newWidth;
			bounds.height = newHeight;
			drops.clear();
			for (int i = 0; i < MAX_DROPS; i++) {
				drops.add(new WeatherDrop(random.nextInt(bounds.width), random.nextInt(bounds.height)));
			}
		}
	}
	/**
	 * An individual weather drop.
	 * @author Daniel Bajor, 2012.11.17.
	 */
	public class WeatherDrop {
		/** The drop's position. */
		private Color color;
		/** The drop's shape. */
		private final Rectangle shape;
//		/** The RNG for effects. */
//		private final Random random = new Random(System.nanoTime());
		/**
		 * Initialize the drop.
		 * @param x the location
		 * @param y the location
		 */
		public WeatherDrop(double x, double y) {
			shape = new Rectangle((int)x, (int)y, 2, 2);
			updateColor();
		}
		/**
		 * Update the drop location in respect to the wind.
		 * @param wind the wind
		 */
		public void update(double wind) {
			shape.x += type == WeatherType.SNOW 
					? wind + (random.nextDouble() - 0.5) * 2
					: wind + (random.nextDouble() - 0.5) * 0.1;
			double velocity = type == WeatherType.RAIN ? RAIN_VELOCITY : SNOW_VELOCITY;
			shape.y += velocity + random.nextDouble();
			if (type == WeatherType.SNOW) {
				shape.height = 2;
			} else {
				shape.height = 10;
			}
			updateColor();
			if (!shape.intersects(-MARGIN_X, -MARGIN_Y, bounds.width + 2 * MARGIN_X, bounds.height + 2 * MARGIN_Y)) {
				reposition();
			}
		}
		/**
		 * Update the drop color.
		 */
		private void updateColor() {
			double a = Math.max(0, Math.min(1, alpha + random.nextDouble() * 0.2 - 0.1));  
			color = type == WeatherType.RAIN
//					? RAIN_COLOR
//					: SNOW_COLOR;
//					? new Color(RAIN_COLOR.getRed(), RAIN_COLOR.getGreen(), RAIN_COLOR.getBlue(), random.nextInt(156) + 50) 
//					: new Color(SNOW_COLOR.getRed(), SNOW_COLOR.getGreen(), SNOW_COLOR.getBlue(), random.nextInt(226) + 30);
					? new Color((int)(RAIN_COLOR.getRed() * a), (int)(RAIN_COLOR.getGreen() * a), (int)(RAIN_COLOR.getBlue() * a)) 
					: new Color((int)(SNOW_COLOR.getRed() * a), (int)(SNOW_COLOR.getGreen() * a), (int)(SNOW_COLOR.getBlue() * a));
		}
		/**
		 * Reposition outbound drop.
		 */
		private void reposition() {
			shape.x = random.nextInt(bounds.width + 2 * MARGIN_X) - MARGIN_X + 1;
			if (shape.x < 0 || shape.x > bounds.width) {
				shape.y = random.nextInt(bounds.height + 2 * MARGIN_Y) - MARGIN_Y + 1;
			} else {
				shape.y = random.nextInt(1 + 2 * MARGIN_Y) - MARGIN_Y + 1;
			}
		}
		/**
		 * Render the drop.
		 * @param g2 the graphics context
		 */
		public void paint(Graphics2D g2) {
			g2.setColor(color);
			g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g2.fillRect(shape.x, shape.y, shape.width, shape.height);
		}
	}
	/**
	 * Render all maintained drops.
	 * @param g2 the graphics context
	 */
	public void draw(Graphics2D g2) {
		for (WeatherDrop wd : drops) {
			wd.paint(g2);
		}
	}
	/**
	 * Update and move the drops.
	 */
	public void update() {
		double delta = (random.nextDouble() - 0.5) * 0.5;
		wind = Math.abs(wind + delta) < 2 ? wind + delta : wind;
		for (WeatherDrop wd : drops) {
			wd.update(wind);
		}
	}
}
