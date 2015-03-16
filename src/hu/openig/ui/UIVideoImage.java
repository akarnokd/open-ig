/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ui;

import hu.openig.core.SwappableRenderer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An image which can render a stream of video frames.
 * @author akarnokd, 2011.03.18.
 */
public class UIVideoImage extends UIComponent implements SwappableRenderer {
	/** The lock to protect the swaps and rendering. */
	private Lock lock = new ReentrantLock();
	/** The front buffer. */
	private BufferedImage front;
	/** The back buffer. */
	private BufferedImage back;
	@Override
	public BufferedImage getBackbuffer() {
		return back;
	}

	@Override
	public void swap() {
		lock.lock();
		try {
			BufferedImage img = front;
			front = back;
			back = img;
		} finally {
			lock.unlock();
		}
		askRepaint();
	}

	@Override
	public void init(int width, int height) {
		lock.lock();
		try {
			if (front == null || front.getWidth() != width || front.getHeight() != height) {
				front = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				back = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			}
		} finally {
			lock.unlock();
		}
	}
	@Override
	public void draw(Graphics2D g2) {
		lock.lock();
		try {
			if (front != null) {
				g2.drawImage(front, 0, 0, null);
			}
		} finally {
			lock.unlock();
		}
	}
}
