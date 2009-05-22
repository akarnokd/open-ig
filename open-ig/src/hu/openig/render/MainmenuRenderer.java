/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.render;

import hu.openig.core.BtnAction;
import hu.openig.core.MainmenuRects;
import hu.openig.res.GameResourceManager;
import hu.openig.res.gfx.MenuGFX;
import hu.openig.res.gfx.TextGFX;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JComponent;

/**
 * Main menu renderer.
 * @author karnokd, 2009.02.08.
 * @version $Revision 1.0$
 */
public class MainmenuRenderer extends JComponent implements MouseMotionListener, MouseListener {
	/** */
	private static final long serialVersionUID = -1930544023996086129L;
	/** The menu graphics. */
	private MenuGFX gfx;
	/** The text graphics. */
	private TextGFX text;
	/** The last width. */
	private int lastWidth;
	/** The last height. */
	private int lastHeight;
	/** The background picture selector. */
	private int picture;
	/** The main menu rectangles. */
	private MainmenuRects rects;
	/** Version string. */
	private String version;
	/** Which menu index to highlight. -1 means none */
	private Rectangle highlight;
	/** The action when the start new is clicked. */
	private BtnAction startNewAction;
	/** The action when the load is clicked. */
	private BtnAction loadAction;
	/** The action when the quit is clicked. */
	private BtnAction titleAnimAction;
	/** The action when the quit is clicked. */
	private BtnAction introAction;
	/** The action when the quit is clicked. */
	private BtnAction quitAction;
	/**
	 * Constructor. Initializes the graphics fields.
	 * @param grm the game resource manager
	 */
	public MainmenuRenderer(GameResourceManager grm) {
		this.gfx = grm.menuGFX;
		this.text = grm.commonGFX.text;
		rects = new MainmenuRects();
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		int w = getWidth();
		int h = getHeight();
		if (isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(0, 0, w, h);
		}
		
		Composite cp = null;
//		Composite cp = g2.getComposite();
//		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
//		g2.setColor(Color.BLACK);
//		g2.fillRect(0, 0, w, h);
//		g2.setComposite(cp);
		
		if (w != lastWidth || h != lastHeight) {
			lastWidth = w;
			lastHeight = h;
			// if the render window changes, re-zoom to update scrollbars
			updateRegions();
		}
		BufferedImage bimg = gfx.startImages[picture];
		g2.drawImage(bimg, rects.background.x, rects.background.y, null);
		
		if (highlight != null) {
			cp = g2.getComposite();
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			g2.setColor(Color.WHITE);
			g2.fill(highlight);
			g2.setComposite(cp);
		}
		if (version != null) {
			int th = 10;
			int tw = text.getTextWidth(th, version);
			text.paintTo(g2, rects.background.x + rects.background.width - tw - 5, 
					rects.background.y + rects.background.height - th - 5, th, TextGFX.RED, version);
		}
	}
	/**
	 * Update important rendering regions.
	 */
	private void updateRegions() {
		BufferedImage bimg = gfx.startImages[picture];
		
		int w = (getWidth() - bimg.getWidth()) / 2;
		int h = (getHeight() - bimg.getHeight()) / 2;
		
		rects.background.setBounds(w, h, bimg.getWidth(), bimg.getHeight());
		rects.rectStartNewGame.setBounds(w + 50, h + 142, 540, 38);
		rects.rectLoadGame.setBounds(w + 50, h + 198, 540, 40);
		rects.rectTitleAnimation.setBounds(w + 50, h + 260, 540, 38);
		rects.rectViewIntro.setBounds(w + 50, h + 316, 540, 38);
		rects.rectQuit.setBounds(w + 50, h + 385, 540, 38);
	}
	/**
	 * Set the background picture based on index.
	 * @param index the picture index
	 */
	public void setPicture(int index) {
		if (index >= 0 && index < gfx.startImages.length) {
			picture = index;
			repaint();
		}
	}
	/**
	 * Returns the current background image index.
	 * @return the current background image index
	 */
	public int getPicture() {
		return picture;
	}
	/**
	 * Set the version text to display.
	 * @param version the version text
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	/**
	 * Returns the version string.
	 * @return the version string
	 */
	public String getVersion() {
		return version;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		Point pt = e.getPoint();
		for (Rectangle r : rects.rectMenus) {
			if (r.contains(pt)) {
				if (highlight != null) {
					repaint(highlight);
				}
				highlight = r;
				repaint(r);
				return;
			}
		}
		if (highlight != null) {
			repaint(highlight);
			highlight = null;
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		Point pt = e.getPoint();
		for (Rectangle r : rects.rectMenus) {
			if (r.contains(pt)) {
				if (highlight != null) {
					repaint(highlight);
				}
				highlight = r;
				repaint(r);
				return;
			}
		}
		if (highlight != null) {
			repaint(highlight);
			highlight = null;
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		Point pt = e.getPoint();
		if (rects.rectStartNewGame.contains(pt) && startNewAction != null) {
			startNewAction.invoke();
		} else
		if (rects.rectLoadGame.contains(pt) && loadAction != null) {
			loadAction.invoke();
		} else
		if (rects.rectTitleAnimation.contains(pt) && titleAnimAction != null) {
			titleAnimAction.invoke();
		} else
		if (rects.rectViewIntro.contains(pt) && introAction != null) {
			introAction.invoke();
		} else
		if (rects.rectQuit.contains(pt) && quitAction != null) {
			quitAction.invoke();
		}
	}
	/**
	 * @param startNewAction the startNewAction to set
	 */
	public void setStartNewAction(BtnAction startNewAction) {
		this.startNewAction = startNewAction;
	}
	/**
	 * @return the startNewAction
	 */
	public BtnAction getStartNewAction() {
		return startNewAction;
	}
	/**
	 * @param loadAction the loadAction to set
	 */
	public void setLoadAction(BtnAction loadAction) {
		this.loadAction = loadAction;
	}
	/**
	 * @return the loadAction
	 */
	public BtnAction getLoadAction() {
		return loadAction;
	}
	/**
	 * @param titleAnimAction the titleAnimAction to set
	 */
	public void setTitleAnimAction(BtnAction titleAnimAction) {
		this.titleAnimAction = titleAnimAction;
	}
	/**
	 * @return the titleAnimAction
	 */
	public BtnAction getTitleAnimAction() {
		return titleAnimAction;
	}
	/**
	 * @param introAction the introAction to set
	 */
	public void setIntroAction(BtnAction introAction) {
		this.introAction = introAction;
	}
	/**
	 * @return the introAction
	 */
	public BtnAction getIntroAction() {
		return introAction;
	}
	/**
	 * @param quitAction the quitAction to set
	 */
	public void setQuitAction(BtnAction quitAction) {
		this.quitAction = quitAction;
	}
	/**
	 * @return the quitAction
	 */
	public BtnAction getQuitAction() {
		return quitAction;
	}
	/**
	 * Select a random start picture.
	 */
	public void setRandomPicture() {
		setPicture(new Random().nextInt(gfx.startImages.length));
	}
}
