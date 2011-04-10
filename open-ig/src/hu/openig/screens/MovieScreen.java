/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import hu.openig.core.Act;
import hu.openig.core.SwappableRenderer;
import hu.openig.model.Screens;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screens.MediaPlayer.LabelEvent;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The movie screen used for full screen video playback with sound and subtitles.
 * @author akarnokd, 2010.01.08.
 */
public class MovieScreen extends ScreenBase implements SwappableRenderer {
	/** The center region for the movie frames. */
	Rectangle movieRect = new Rectangle();
	/** The current label to display. */
	String label;
	/** The image swap lock. */
	Lock swapLock = new ReentrantLock();
	/** The front buffer. */
	BufferedImage frontBuffer;
	/** The back buffer. */
	BufferedImage backBuffer;
	/** The media queue to play videos after each other. */
	public final Queue<String> mediaQueue = new LinkedList<String>();
	/** The action to invoke when the playback has finished. */
	public Act playbackFinished;
	/**
	 * The media player.
	 */
	private MediaPlayer player;
	/**
	 * Upscale the 8 bit signed values to 16 bit signed values.
	 * @param data the data to upscale
	 * @return the upscaled data
	 */
	public static short[] upscale8To16AndSignify(byte[] data) {
		short[] result = new short[data.length];
		for (int i = 0; i < data.length; i++) {
			result[i] = (short)(((data[i] & 0xFF) - 128) * 256);
		}
		return result;
	}
	/**
	 * Start playback.
	 * @param media the media
	 */
	protected void startPlayback(final String media) {
		player = new MediaPlayer(commons, media, this);
		player.onComplete = new Act() {
			@Override
			public void act() {
				frontBuffer = null;
				playNext();
			}
		};
		player.onLabel = new LabelEvent() {
			@Override
			public void label(String text) {
				label = text;
			}
		};
		player.start();
	}
	/**
	 * 
	 */
	protected void playNext() {
		String nextMedia = mediaQueue.poll();
		if (nextMedia != null) {
			startPlayback(nextMedia);
		} else 
		if (playbackFinished != null) {
			playbackFinished.act();
		}
	}
	@Override
	public void onResize() {
		movieRect.setBounds((getInnerWidth() - 640) / 2, (getInnerHeight() - 480) / 2, 640, 480);
	}

	@Override
	public void onFinish() {
		mediaQueue.clear();
		stopPlayback();
	}

	@Override
	public void onInitialize() {
		
	}

	@Override
	public boolean keyboard(KeyEvent e) {
		// TODO Auto-generated method stub
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			stopPlayback();
		}
		return false;
	}

	/** Stop the current playback. */
	protected void stopPlayback() {
		if (player != null) {
			player.stop();
		}
		label = null;
		askRepaint();
	}
	@Override
	public boolean mouse(UIMouse e) {
		if (e.has(Type.CLICK)) {
			stopPlayback();
		}
		return false;
	}

	@Override
	public void onEnter(Screens mode) {
		playNext();
	}

	@Override
	public void onLeave() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draw(Graphics2D g2) {
		onResize();
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, getInnerWidth(), getInnerHeight());
		if (frontBuffer != null) {
			swapLock.lock();
			try {
				RenderTools.setInterpolation(g2, true);
				g2.drawImage(frontBuffer, movieRect.x, movieRect.y, 
						movieRect.width, movieRect.height, null);
				if (label != null) {
					paintLabel(g2, movieRect.x, movieRect.y, movieRect.width, movieRect.height);
				}
				RenderTools.setInterpolation(g2, false);
			} finally {
				swapLock.unlock();
			}
		}	
	}

	/**
	 * Paint a word-wrapped label.
	 * @param g2 the graphics context.
	 * @param x0 the X coordinate
	 * @param y0 the Y coordinate
	 * @param width the draw width
	 * @param height the draw height
	 */
	public void paintLabel(Graphics2D g2, int x0, int y0, int width, int height) {
		List<String> lines = new ArrayList<String>();
		int maxWidth = commons.text().wrapText(label, width, 14, lines);
		int y = height - lines.size() * 21 - 7;
		Composite cp = g2.getComposite();
		g2.setComposite(AlphaComposite.SrcOver.derive(0.8f));
		g2.fillRect(x0 + (width - maxWidth) / 2 - 3, y0 + y - 3, maxWidth + 6, lines.size() * 21 + 6);
		g2.setComposite(cp);
		for (String s : lines) {
			int tw = commons.text().getTextWidth(14, s);
			int x = (width - tw) / 2;
			commons.text().paintTo(g2, x0 + x, y0 + y, 14, TextRenderer.WHITE, s);
			y += 21;
		}
	}
	@Override
	public BufferedImage getBackbuffer() {
		return backBuffer;
	}

	@Override
	public void init(int width, int height) {
		swapLock.lock();
		try {
			backBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			frontBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		} finally {
			swapLock.unlock();
		}
	}

	@Override
	public void swap() {
		swapLock.lock();
		try {
			BufferedImage temp = backBuffer;
			backBuffer = frontBuffer;
			frontBuffer = temp;
		} finally {
			swapLock.unlock();
		}
		askRepaint();
	}
	@Override
	public Screens screen() {
		return Screens.MOVIE;
	}
	@Override
	public void onEndGame() {
		// TODO Auto-generated method stub
		
	}
}
