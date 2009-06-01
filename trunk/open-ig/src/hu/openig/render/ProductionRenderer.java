/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.render;

import hu.openig.ani.Player;
import hu.openig.core.Btn;
import hu.openig.core.BtnAction;
import hu.openig.core.LabInfo;
import hu.openig.core.SwappableRenderer;
import hu.openig.model.GameWorld;
import hu.openig.model.ResearchProgress;
import hu.openig.model.ResearchTech;
import hu.openig.res.GameResourceManager;
import hu.openig.res.gfx.CommonGFX;
import hu.openig.res.gfx.ProductionGFX;
import hu.openig.res.gfx.ResearchGFX;
import hu.openig.res.gfx.TextGFX;
import hu.openig.sound.SoundFXPlayer;
import hu.openig.utils.JavaUtils;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * The production screen renderer.
 * @author karnokd
 */
public class ProductionRenderer extends JComponent implements SwappableRenderer {
	/** */
	private static final long serialVersionUID = 4399379607215998650L;
	/** The common graphics. */
	private final CommonGFX cgfx;
	/** The research graphics. */
	private final ResearchGFX rgfx;
	/** The production graphics object. */
	private final ProductionGFX gfx;
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
	/** The areas for the sub options labels. */
	private final Rectangle[] rectSubOptions = {
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
	/** List of buttons which fire on mouse release. */
	private final List<Btn> releaseButtons = JavaUtils.newArrayList();
	/** Is the currently animating technology available. */
	private boolean selectedTechAvail;
	/** The research timer. */
	private Timer researchTimer;
	/** The research timer firing interval. */
	private static final int RESEARCH_TIMER = 100;
	/** The research timer step counter. */
	private int researchStep;
	/** Action on cancel information screen. */
	private BtnAction onCancelScreen;
	/** Action for equipment button clicked. */
	private BtnAction onEquipmentClick;
	/** Action for research button clicked. */
	private BtnAction onResearchClick;
	/** Equipment rectangle. */
	private Btn btnEquipment;
	/** Production rectangle. */
	private Btn btnResearch;
	/** Bridge rectangle. */
	private Btn btnBridge;
	/**
	 * {@inheritDoc}
	 */
	@Override
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
	 * {@inheritDoc}
	 */
	@Override
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
	 * {@inheritDoc}
	 */
	@Override
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
	 * Constructor. Sets the graphics objects.
	 * @param grm the resource manager
	 * @param uiSound the sounds
	 * @param infobarRenderer information bar renderer
	 * @param achievementRenderer achievement renderer
	 */
	public ProductionRenderer(GameResourceManager grm, 
			SoundFXPlayer uiSound, InfobarRenderer infobarRenderer,
			AchievementRenderer achievementRenderer) {		
		this.rgfx = grm.researchGFX;
		this.gfx = grm.productionGFX;
		this.cgfx = grm.commonGFX;
		this.text = cgfx.text;
		this.uiSound = uiSound;
		this.infobarRenderer = infobarRenderer;
		this.achievementRenderer = achievementRenderer;
		MouseActions ma = new MouseActions();
		this.addMouseListener(ma);
		this.addMouseMotionListener(ma);
		this.addMouseWheelListener(ma);
		researchTimer = new Timer(RESEARCH_TIMER, new ActionListener() { public void actionPerformed(ActionEvent e) { doResearchTimer(); } });
		initButtons();
	}
	/** Advance the research image. */
	protected void doResearchTimer() {
		researchStep = (researchStep + 1) % 16;
		if (gameWorld.player.activeResearch != null) {
			repaint();
		}
	}
	/** Initialize buttons. */
	private void initButtons() {
		btnEquipment = new Btn(new BtnAction() { public void invoke() { doEquipmentClick(); } });
		btnResearch = new Btn(new BtnAction() { public void invoke() { doResearchClick(); } });
		btnBridge = new Btn();
		releaseButtons.add(btnEquipment);
		releaseButtons.add(btnResearch);
		releaseButtons.add(btnBridge);
	}
	/** On production clicked. */
	protected void doResearchClick() {
		if (onResearchClick != null) {
			onResearchClick.invoke();
		}
	}
	/** On equipment clicked. */
	protected void doEquipmentClick() {
		if (onEquipmentClick != null) {
			onEquipmentClick.invoke();
		}
	}
	/** Display the currently researched technology. */
	protected void doViewResearch() {
		if (gameWorld.player.activeResearch != null) {
			gameWorld.player.selectedTech = gameWorld.player.activeResearch.research;
			clazzIndex = gameWorld.player.selectedTech.clazzIndex;
			typeIndex = gameWorld.player.selectedTech.typeIndex;
			gameWorld.selectBuildingFor(gameWorld.player.selectedTech);
		}
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

		if (true) {
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
		g2.drawImage(gfx.screen, screenRect.x, screenRect.y, null);
		ResearchProgress arp = gameWorld.player.activeResearch;
		// draw main options
		for (int i = 0; i < rectMainOptions.length; i++) {
			if (i + 1 == clazzIndex) {
				g2.drawImage(rgfx.mainOptionsLight[i], rectMainOptions[i].x, rectMainOptions[i].y, null);
			} else {
				g2.drawImage(rgfx.mainOptions[i], rectMainOptions[i].x, rectMainOptions[i].y, null);
			}
			if (arp != null && arp.research.clazzIndex == i + 1) {
				g2.drawImage(rgfx.arrow, rectMainOptions[i].x - 16, rectMainOptions[i].y + 6, null);
			}
		}
		// put the empty background to position 5
		g2.drawImage(gfx.emptySubOption, rectSubOptions[4].x, rectSubOptions[4].y, null);
		if (clazzIndex > 0) {
			BufferedImage[] images = rgfx.subOptions[clazzIndex - 1];
			BufferedImage[] imagesLight = rgfx.subOptionsLight[clazzIndex - 1];
			for (int i = 0; i < images.length; i++) {
				if (typeIndex == i + 1) {
					g2.drawImage(imagesLight[i], rectSubOptions[i].x, rectSubOptions[i].y, null);
				} else {
					g2.drawImage(images[i], rectSubOptions[i].x, rectSubOptions[i].y, null);
				}
				if (arp != null && arp.research.clazzIndex == clazzIndex && arp.research.typeIndex == i + 1) {
					g2.drawImage(rgfx.arrow, rectSubOptions[i].x - 14, rectSubOptions[i].y, null);
				}
			}
		}
		if (typeIndex > 0) {
			List<ResearchTech> rtl = gameWorld.player.getResearchFor(clazzIndex, typeIndex);
			for (int i = 0; i < rtl.size(); i++) {
				ResearchTech rt = rtl.get(i);
				g2.drawImage(rt.smallImage, rectResearch[i].x, rectResearch[i].y, null);
				if (gameWorld.isAvailable(rt)) {
					text.paintTo(g2, rectResearch[i].x + 2, rectResearch[i].y + 2, 7, TextGFX.GREEN, Integer.toString(rt.buildCost));
					if (!"Buildings".equals(rt.clazz)) {
						text.paintTo(g2, rectResearch[i].x + 3, rectResearch[i].y + rectResearch[i].height - 18, 7, 
								TextGFX.RED, Integer.toString(gameWorld.getInventoryCount(rt)));
					}
				} else {
					g2.setColor(Color.BLACK);
					for (int j = 1; j < rectResearch[i].height; j += 2) {
						g2.drawLine(rectResearch[i].x, rectResearch[i].y + j, rectResearch[i].x + rectResearch[i].width - 1, rectResearch[i].y + j);
					}
					if (!gameWorld.isResearchable(rt)) {
						g2.drawImage(cgfx.researchDisallowed, rectResearch[i].x + (rectResearch[i].width - cgfx.researchDisallowed.getWidth()) / 2, 
								rectResearch[i].y + (rectResearch[i].height - cgfx.researchDisallowed.getHeight()) / 2, null);
					} else {
						LabInfo li = gameWorld.player.getLabInfo();
						int cd = gameWorld.isEnoughLabFor(li, rt) ? (gameWorld.isEnoughWorkingLabFor(li, rt) ? 0 : 2) : 1;
						if (arp != null && arp.research == rt) {
							g2.drawImage(cgfx.researchCDs[cd][researchStep], rectResearch[i].x + 2, rectResearch[i].y + rectResearch[i].height - 30, null);
						} else {
							g2.drawImage(cgfx.researchCDs[cd][0], rectResearch[i].x + 2, rectResearch[i].y + rectResearch[i].height - 30, null);
						}
					}
				}
				if (rt == gameWorld.player.selectedTech) {
					g2.setColor(Color.RED);
					g2.drawRect(rectResearch[i].x, rectResearch[i].y, rectResearch[i].width - 1, rectResearch[i].height - 1);
					g2.drawRect(rectResearch[i].x - 1, rectResearch[i].y - 1, rectResearch[i].width + 1, rectResearch[i].height + 1);
				}
				text.paintTo(g2, rectResearch[i].x + 3, rectResearch[i].y + rectResearch[i].height - 9, 7, 
						rt == gameWorld.player.selectedTech ? TextGFX.RED : TextGFX.GREEN, rt.name);
			}
		}
		ResearchTech rt = gameWorld.player.selectedTech;
		if (rt != this.selectedTech || (this.selectedTech != null && gameWorld.isAvailable(this.selectedTech) != selectedTechAvail)
				|| (this.selectedTech == null && !anim.isPlayback())) {
			this.selectedTech = rt;
			this.selectedTechAvail = gameWorld.isAvailable(this.selectedTech);
			anim.stopAndWait();
			if (rt != null && gameWorld.isAvailable(rt) && rt.animation != null) {
				anim.setFilename(rt.animation.getAbsolutePath());
			} else
			if (rt != null && gameWorld.isResearchable(rt) && rt.animationWired != null) {
				anim.setFilename(rt.animationWired.getAbsolutePath());
			} else {
				anim.setFilename(rgfx.emptyAnimation.getAbsolutePath());
			}
			anim.setLoop(true);
			anim.setNoAudio(true);
			anim.setMemoryPlayback(true);
			anim.startPlayback();
		}
		renderAnimation(g2);
		
		if (btnEquipment.down) {
			g2.drawImage(gfx.btnEquipmentDown, btnEquipment.rect.x, btnEquipment.rect.y, null);
		}
		if (btnResearch.down) {
			g2.drawImage(gfx.btnResearchDown, btnResearch.rect.x, btnResearch.rect.y, null);
		}
		if (btnBridge.down) {
			g2.drawImage(gfx.btnBridgeDown, btnBridge.rect.x, btnBridge.rect.y, null);
		}
		
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
		screenRect.setBounds((w - gfx.screen.getWidth()) / 2, (h - gfx.screen.getHeight()) / 2, 
				gfx.screen.getWidth(), gfx.screen.getHeight());
		
		for (int i = 0; i < rectMainOptions.length; i++) {
			rectMainOptions[i].setBounds(screenRect.x + 341, screenRect.y + 10 + i * 22, 168, 20);
		}
		for (int i = 0; i < rectSubOptions.length; i++) {
			rectSubOptions[i].setBounds(screenRect.x + 359, screenRect.y + 112 + i * 16, 168, 14);
		}
		for (int i = 0; i < rectResearch.length; i++) {
			rectResearch[i].setBounds(screenRect.x + 3 + 106 * i, screenRect.y + 201, 104, 78);
		};
		rectAnimation.setBounds(screenRect.x + 2, screenRect.y + 2, 316, 196);
		
		btnEquipment.setBounds(screenRect.x + 534, screenRect.y + 322, 102, 39);
		btnResearch.setBounds(screenRect.x + 534, screenRect.y + 361, 102, 39);
		btnBridge.setBounds(screenRect.x + 534, screenRect.y + 400, 102, 39);
	}
	/**
	 * @param onCancelScreen the onCancelScreen to set
	 */
	public void setOnCancelScreen(BtnAction onCancelScreen) {
		this.onCancelScreen = onCancelScreen;
	}

	/**
	 * @return the onCancelScreen
	 */
	public BtnAction getOnCancelScreen() {
		return onCancelScreen;
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
					if (typeIndex > rgfx.subOptions[i].length) {
						typeIndex = rgfx.subOptions[i].length;
					}
					repaint(screenRect);
					return;
				}
			}
			if (clazzIndex > 0) {
				BufferedImage[] opts = rgfx.subOptions[clazzIndex - 1];
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
						gameWorld.selectBuildingFor(gameWorld.player.selectedTech);
						repaint(screenRect);
						return;
					}
				}
			}
			if (e.getButton() == MouseEvent.BUTTON1) {
				for (Btn b : releaseButtons) {
					if (b.test(pt)) {
						b.down = true;
						repaint(b.rect);
					}
				}
				if (!screenRect.contains(pt)) {
					if (onCancelScreen != null) {
						onCancelScreen.invoke();
					}
				}
			}
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			Point pt = e.getPoint();
			if (e.getButton() == MouseEvent.BUTTON1) {
				for (Btn b : releaseButtons) {
					if (b.test(pt) && b.down) {
						b.click();
					}
					b.down = false;
					repaint(b.rect);
				}
			}
		}
	}
	/** Start animations. */
	public void startAnimations() {
		researchTimer.start();
		if (anim.getFilename() != null) {
			anim.startPlayback();
		}
	}
	/** Stop animations. */
	public void stopAnimations() {
		researchTimer.stop();
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
			clazzIndex = 1;
			typeIndex = 1;
		}
	}

	/**
	 * @param onEquipmentClick the onEquipmentClick to set
	 */
	public void setOnEquipmentClick(BtnAction onEquipmentClick) {
		this.onEquipmentClick = onEquipmentClick;
	}

	/**
	 * @return the onEquipmentClick
	 */
	public BtnAction getOnEquipmentClick() {
		return onEquipmentClick;
	}

	/**
	 * @param onResearchClick the onResearchClick to set
	 */
	public void setOnResearchClick(BtnAction onResearchClick) {
		this.onResearchClick = onResearchClick;
	}

	/**
	 * @return the onResearchClick
	 */
	public BtnAction getOnResearchClick() {
		return onResearchClick;
	}
}
