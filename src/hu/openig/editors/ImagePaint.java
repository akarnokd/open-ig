/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.editors;

import hu.openig.model.Tile;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;

/**
 * Paint a buffered image.
 * @author akarnokd
 */
public class ImagePaint extends JComponent {
	/** */
	private static final long serialVersionUID = 3168477795343188089L;
	/** The image. */
	transient Tile tile;
	/** The brighteness factor. */
	float alpha = 1.0f;
	@Override
	public Dimension getPreferredSize() {
		if (tile != null) {
			return new Dimension(tile.imageWidth + 4, tile.imageHeight + 4);
		}
		return new Dimension(440, 300);
	}
	@Override
	public void paint(Graphics g) {
		if (tile != null) {
			int x = (getWidth() - tile.imageWidth) / 2;
			int y = (getHeight() - tile.imageHeight) / 2;
			tile.alpha = alpha;
			g.setColor(Color.RED);
			g.drawImage(tile.getFullImage(), x, y, null);
			g.drawRect(x - 1, y - 1, tile.imageWidth + 1, tile.imageHeight + 1);
			g.drawRect(x - 2, y - 2, tile.imageWidth + 3, tile.imageHeight + 3);
		}
	}
	/**
	 * Set the image from the tile.
	 * @param tile the tile
	 */
	public void setImage(Tile tile) {
		this.tile = tile;
		revalidate();
		repaint();
	}
	/**
	 * Set the brightness value.
	 * @param alpha between 0.0 and 1.0
	 */
	public void setAlpha(float alpha) {
		this.alpha = alpha;
		repaint();
	}
}
