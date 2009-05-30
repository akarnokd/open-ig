/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.render;

import hu.openig.ani.Player;
import hu.openig.core.SwappableRenderer;
import hu.openig.model.GameBuildingPrototype;
import hu.openig.model.GameWorld;
import hu.openig.model.ResearchTech;
import hu.openig.res.GameResourceManager;
import hu.openig.res.gfx.CommonGFX;
import hu.openig.res.gfx.ResearchGFX;
import hu.openig.res.gfx.TextGFX;
import hu.openig.sound.SoundFXPlayer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JComponent;

/**
 * Research screen renderer.
 * @author karnokd
 */
public class ResearchRenderer extends JComponent implements SwappableRenderer {
	/** The serial version id. */
	private static final long serialVersionUID = 1724420039211512267L;
	/** The common graphics. */
	private final CommonGFX cgfx;
	/** The research graphics. */
	private final ResearchGFX gfx;
	/** The text graphics. */
	final TextGFX text;
	/** The last rendering position. */
	private final AchievementRenderer achievementRenderer;
	/** The user interface sounds. */
	SoundFXPlayer uiSound;
	/** The information bar renderer. */
	private InfobarRenderer infobarRenderer;
	/** The game world. */
	private GameWorld gameWorld;
	/** The last width. */
	private int lastWidth;
	/** The last height. */
	private int lastHeight;
	/** The main screen rectangle. */
	private final Rectangle screenRect = new Rectangle();
	/** The current selected class. */
	private int clazzIndex;
	/** The current selected type. */
	private int typeIndex;
	/** The areas for the main options labels. */
	private final Rectangle[] rectMainOptions = new Rectangle[] {
		new Rectangle(), new Rectangle(), new Rectangle(), new Rectangle()
	};
	/** Areas for the current research selector arrow. */
	private final Rectangle[] rectMainArrow = new Rectangle[] {
		new Rectangle(), new Rectangle(), new Rectangle(), new Rectangle()
	};
	/** The areas for the sub options labels. */
	private final Rectangle[] rectSubOptions = {
		new Rectangle(), new Rectangle(), new Rectangle(), new Rectangle(), new Rectangle()	
	};
	/** Areas for the current research selector arrow in sub options. */
	private final Rectangle[] rectSubArrow = {
		new Rectangle(), new Rectangle(), new Rectangle(), new Rectangle(), new Rectangle()	
	};
	/** The actual research rectangles. */
	private final Rectangle[] rectResearch = new Rectangle[] {
		new Rectangle(), new Rectangle(),
		new Rectangle(), new Rectangle(),
		new Rectangle(), new Rectangle()
	};
	/** Animation rectangle. */
	private final Rectangle rectAnimation = new Rectangle();
	/** The animation player. */
	private Player anim = new Player(this);
	/** The animation research tech. */
	private ResearchTech selectedTech;
	/** The lock that protects the swapping and drawing of the frontbuffer. */
	private Lock swapLock = new ReentrantLock();
	/** The backbuffer image. */
	private BufferedImage backbuffer;
	/** The frontbuffer image.*/
	private BufferedImage frontbuffer;
	/** To skip the blackness on start. */
	private volatile boolean justInited;
	/**
	 * Returns the back buffer which is safe to draw to at any time.
	 * The get should be initiated by the party who is supplying the images.
	 * @return the backbuffer image.
	 */
	public BufferedImage getBackbuffer() {
		if (backbuffer == null) {
			throw new IllegalStateException("init() not called");
		}
		if (justInited) {
			justInited = false;
			return new BufferedImage(backbuffer.getWidth(), backbuffer.getHeight(), BufferedImage.TYPE_INT_ARGB);
		}
		return backbuffer;
	}
	/**
	 * Swap the front and backbuffers.
	 * The swap must be initiated by the party who is supplying the images.
	 */
	public void swap() {
		swapLock.lock();
		try {
			BufferedImage tmp = frontbuffer;
			frontbuffer = backbuffer;
			backbuffer = tmp;
		} finally {
			swapLock.unlock();
		}
		repaint(rectAnimation);
	}
	/**
	 * Initialize the drawing buffers with the defined size.
	 * Use setScalingMode() to enable resizing of the actual image.
	 * @param width the image width
	 * @param height the image height.
	 */
	public void init(int width, int height) {
		swapLock.lock();
		try {
			// create new buffer only if the animation size changes
			if (backbuffer == null || backbuffer.getWidth() != width || backbuffer.getHeight() != height) {
				backbuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				frontbuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				// disable acceleration on these images?! they change so frequently it
				// is just overhead to move them back and forth between the memory and VRAM on
				// invalidate
				backbuffer.setAccelerationPriority(0);
				frontbuffer.setAccelerationPriority(0);
			}
			justInited = true;
		} finally {
			swapLock.unlock();
		}
	}

	/**
	 * Constructor. Sets the graphics objects.
	 * @param grm the resource manager
	 * @param uiSound the sounds
	 * @param infobarRenderer information bar renderer
	 * @param achievementRenderer achievement renderer
	 */
	public ResearchRenderer(GameResourceManager grm, 
			SoundFXPlayer uiSound, InfobarRenderer infobarRenderer,
			AchievementRenderer achievementRenderer) {		
		this.gfx = grm.researchGFX;
		this.cgfx = grm.commonGFX;
		this.text = cgfx.text;
		this.uiSound = uiSound;
		this.infobarRenderer = infobarRenderer;
		this.achievementRenderer = achievementRenderer;
		MouseActions ma = new MouseActions();
		this.addMouseListener(ma);
		this.addMouseMotionListener(ma);
		this.addMouseWheelListener(ma);
	}
	/**
	 * @param gameWorld the gameWorld to set
	 */
	public void setGameWorld(GameWorld gameWorld) {
		this.gameWorld = gameWorld;
	}
	/**
	 * @return the gameWorld
	 */
	public GameWorld getGameWorld() {
		return gameWorld;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		int w = getWidth();
		int h = getHeight();

		if (false) {
			alphablendBackground(g2);
		}
		if (w != lastWidth || h != lastHeight) {
			lastWidth = w;
			lastHeight = h;
			// if the render window changes, re-zoom to update scrollbars
			updateRegions();
		}
		// RENDER INFOBARS
		infobarRenderer.renderInfoBars(this, g2);
		
		g2.drawImage(gfx.researchScreen, screenRect.x, screenRect.y, null);
		// draw main options
		for (int i = 0; i < rectMainOptions.length; i++) {
			if (i + 1 == clazzIndex) {
				g2.drawImage(gfx.mainOptionsLight[i], rectMainOptions[i].x, rectMainOptions[i].y, null);
			} else {
				g2.drawImage(gfx.mainOptions[i], rectMainOptions[i].x, rectMainOptions[i].y, null);
			}
		}
		if (clazzIndex > 0) {
			BufferedImage[] images = gfx.subOptions[clazzIndex - 1];
			BufferedImage[] imagesLight = gfx.subOptionsLight[clazzIndex - 1];
			for (int i = 0; i < images.length; i++) {
				if (typeIndex == i + 1) {
					g2.drawImage(imagesLight[i], rectSubOptions[i].x, rectSubOptions[i].y, null);
				} else {
					g2.drawImage(images[i], rectSubOptions[i].x, rectSubOptions[i].y, null);
				}
			}
		}
		if (typeIndex > 0) {
			List<ResearchTech> rtl = gameWorld.player.getResearchFor(clazzIndex, typeIndex);
			for (int i = 0; i < rtl.size(); i++) {
				ResearchTech rt = rtl.get(i);
				g2.drawImage(rt.smallImage, rectResearch[i].x, rectResearch[i].y, null);
				if (rt == gameWorld.player.selectedTech) {
					g2.setColor(Color.RED);
					g2.drawRect(rectResearch[i].x, rectResearch[i].y, rectResearch[i].width, rectResearch[i].height);
					g2.drawRect(rectResearch[i].x - 1, rectResearch[i].y - 1, rectResearch[i].width + 2, rectResearch[i].height + 2);
				}
			}
		}
		if (gameWorld.player.selectedTech != this.selectedTech
				|| (this.selectedTech == null && !anim.isPlayback())) {
			this.selectedTech = gameWorld.player.selectedTech;
			if (selectedTech == null || selectedTech.animation == null) {
				anim.stopAndWait();
				anim.setFilename(gfx.emptyAnimation.getAbsolutePath());
			} else {
				anim.setFilename(selectedTech.animation.getAbsolutePath());
			}
			anim.setLoop(true);
			anim.setNoAudio(true);
			anim.setMemoryPlayback(true);
			anim.startPlayback();
		}
		renderAnimation(g2);
		achievementRenderer.renderAchievements(g2, this);
	}
	/**
	 * Alpha blends the background beneath this component.
	 * @param g2 the graphics object
	 */
	private void alphablendBackground(Graphics2D g2) {
		Composite cp = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.setComposite(cp);
	}
	/**
	 * Renders the animation frame.
	 * @param g2 the graphics object
	 */
	private void renderAnimation(Graphics2D g2) {
		swapLock.lock();
		try {
			g2.drawImage(frontbuffer, rectAnimation.x, rectAnimation.y, null);
		} finally {
			swapLock.unlock();
		}
	}
	/**
	 * Update location of various interresting rectangles of objects.
	 */
	private void updateRegions() {
		infobarRenderer.updateRegions(this);
		int w = getWidth();
		int h = getHeight();
		screenRect.setBounds((w - gfx.researchScreen.getWidth()) / 2, (h - gfx.researchScreen.getHeight()) / 2, 
				gfx.researchScreen.getWidth(), gfx.researchScreen.getHeight());
		
		for (int i = 0; i < rectMainOptions.length; i++) {
			rectMainOptions[i].setBounds(screenRect.x + 341, screenRect.y + 10 + i * 22, 168, 20);
			rectMainArrow[i].setBounds(screenRect.x + 325, screenRect.y + 13 + i * 22, 15, 11);
		}
		for (int i = 0; i < rectSubOptions.length; i++) {
			rectSubOptions[i].setBounds(screenRect.x + 342, screenRect.y + 112 + i * 16, 168, 14);
			rectSubArrow[i].setBounds(screenRect.x + 325, screenRect.y + 112 + i * 16, 15, 11);
		}
		for (int i = 0; i < rectResearch.length; i++) {
			rectResearch[i].setBounds(screenRect.x + 3 + 106 * i, screenRect.y + 201, 104, 78);
		};
		rectAnimation.setBounds(screenRect.x + 2, screenRect.y + 2, 316, 196);
	}
	/**
	 * Class to handle mouse actions in the research screen. 
	 * @author karnokd
	 */
	private class MouseActions extends MouseAdapter {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			Point pt = e.getPoint();
			for (int i = 0; i < rectMainOptions.length; i++) {
				if (rectMainOptions[i].contains(pt)) {
					clazzIndex = i + 1;
					// fix sorter suboptions
					if (typeIndex > gfx.subOptions[i].length) {
						typeIndex = gfx.subOptions[i].length;
					}
					repaint(screenRect);
					return;
				}
			}
			if (clazzIndex > 0) {
				BufferedImage[] opts = gfx.subOptions[clazzIndex - 1];
				for (int i = 0; i < rectSubOptions.length; i++) {
					if (rectSubOptions[i].contains(pt) && i < opts.length) {
						typeIndex = i + 1;
						repaint(screenRect);
						return;
					}
				}
			}
			if (typeIndex > 0 && clazzIndex > 0) {
				List<ResearchTech> rtl = gameWorld.player.getResearchFor(clazzIndex, typeIndex);
				for (int i = 0; i < rtl.size(); i++) {
					if (rectResearch[i].contains(pt)) {
						gameWorld.player.selectedTech = rtl.get(i);
						// if this is a building, select its prototype
						if ("Buildings".equals(gameWorld.player.selectedTech.clazz)) {
							for (GameBuildingPrototype bp : gameWorld.buildingPrototypes) {
								if (bp.researchTech == gameWorld.player.selectedTech) {
									gameWorld.player.selectedBuildingPrototype = bp;
								}
							}
						}
						repaint(screenRect);
						return;
					}
				}
			}
		}
	}
	/**
	 * Starts the animations.
	 */
	public void startAnimation() {
		if (anim.getFilename() != null) {
			anim.startPlayback();
		}
	}
	/** Stops the animations. */
	public void stopAnimation() {
		anim.stopAndWait();
	}
	/**
	 * Sets the list indexes to display the currently selected technology if any.
	 */
	public void selectCurrentTech() {
		if (gameWorld.player.selectedTech != null) {
			clazzIndex = gameWorld.player.selectedTech.clazzIndex;
			typeIndex = gameWorld.player.selectedTech.typeIndex;
		} else {
			clazzIndex = 0;
			typeIndex = 0;
		}
	}
}
