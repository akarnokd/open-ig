/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.v1.editors;

import hu.openig.v1.core.Tile;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;

/**
 * Paint a buffered image.
 * @author karnokd
 */
public class ImagePaint extends JComponent {
	/** */
	private static final long serialVersionUID = 3168477795343188089L;
	/** The image. */
	Tile tile;
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
			g.drawImage(tile.alphaBlendImage(), x, y, null);
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
