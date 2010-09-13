/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.xold.render;

import hu.openig.xold.ani.SpidyAniPlayer;
import hu.openig.xold.core.Btn;
import hu.openig.xold.core.BtnAction;
import hu.openig.xold.core.FactoryInfo;
import hu.openig.xold.core.LabInfo;
import hu.openig.core.SwappableRenderer;
import hu.openig.xold.model.GameWorld;
import hu.openig.xold.model.ProductionProgress;
import hu.openig.xold.model.ResearchProgress;
import hu.openig.xold.model.ResearchTech;
import hu.openig.xold.res.GameResourceManager;
import hu.openig.xold.res.gfx.CommonGFX;
import hu.openig.xold.res.gfx.ProductionGFX;
import hu.openig.xold.res.gfx.ResearchGFX;
import hu.openig.xold.res.gfx.TextGFX;
import hu.openig.sound.SoundFXPlayer;
import hu.openig.utils.JavaUtils;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Collections;
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
	private SpidyAniPlayer anim = new SpidyAniPlayer(this);
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
	/** The total capacity rectangle. */
	private final Rectangle rectTotalCapacity = new Rectangle();
	/** The production lines coordinates. */
	private final List<ProductionLine> lines = JavaUtils.newArrayList();
	/** Number of production lines. */
	private static final int PRODUCTION_LINES = 5;
	/** Decrease production count by 10. */
	private Btn btnMinusTen;
	/** Decrease production count by 1. */
	private Btn btnMinusOne;
	/** Increase production count by 1. */
	private Btn btnPlusOne;
	/** Increase production count by 10. */
	private Btn btnPlusTen;
	/** Sell one item from inventory. */
	private Btn btnSell;
	/** Add/remove button rectangle. */
	private Btn btnAddRemove;
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
		
		for (int i = 0; i < PRODUCTION_LINES; i++) {
			lines.add(new ProductionLine());
		}
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
		
		btnMinusTen = new Btn(new BtnAction() { public void invoke() { doAddCount(-10); } });
		releaseButtons.add(btnMinusTen);
		btnMinusOne = new Btn(new BtnAction() { public void invoke() { doAddCount(-1); } });
		releaseButtons.add(btnMinusOne);
		btnPlusOne = new Btn(new BtnAction() { public void invoke() { doAddCount(1); } });
		releaseButtons.add(btnPlusOne);
		btnPlusTen = new Btn(new BtnAction() { public void invoke() { doAddCount(10); } });
		releaseButtons.add(btnPlusTen);
		btnSell = new Btn(new BtnAction() { public void invoke() { doSell(1); } });
		releaseButtons.add(btnSell);
		
		btnAddRemove = new Btn(new BtnAction() { public void invoke() { doAddRemoveClick(); } });
		releaseButtons.add(btnAddRemove);
	}
	/**
	 * Sell the given amount of items from the currently selected one.
	 * @param i the amount to sell
	 */
	protected void doSell(int i) {
		int j = gameWorld.getInventoryCount(gameWorld.player.selectedTech);
		gameWorld.player.inventory.put(gameWorld.player.selectedTech, Math.max(0, j - i));
		if (j > i) {
			j = i;
		}
		gameWorld.player.money += j * gameWorld.player.selectedTech.buildCost / 2;
		repaint();
	}
	/**
	 * Adds the number of items to the currently selected production.
	 * @param i the count to increment or decrement
	 */
	private void doAddCount(int i) {
		List<ProductionProgress> ppl = gameWorld.player.production.get(clazzIndex);
		if (ppl != null) {
			for (ProductionProgress p : ppl) {
				if (p.tech == gameWorld.player.selectedTech) {
					p.count = Math.max(0, p.count + i);
					gameWorld.player.rebalanceProduction(ppl, p.tech.clazzIndex);
					repaint();
					break;
				}
			}
		}
		
	}
	/**
	 * Adds or removes a production for the current selection.
	 */
	protected void doAddRemoveClick() {
		gameWorld.player.addRemove(gameWorld.player.selectedTech);
		repaint();
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
		
		renderProductions(g2);
		List<ProductionProgress> ppl = gameWorld.player.production.get(clazzIndex);
		btnAddRemove.disabled = !(gameWorld.isAvailable(rt) && (rt != null && !"building".equals(rt.factory)));
		if (!btnAddRemove.disabled) {
			if (gameWorld.isInProduction(rt)) {
				if (btnAddRemove.down) {
					g2.drawImage(gfx.btnRemoveDown, btnAddRemove.rect.x, btnAddRemove.rect.y, null);
				} else {
					g2.drawImage(gfx.btnRemove, btnAddRemove.rect.x, btnAddRemove.rect.y, null);
				}
			} else {
				if (ppl == null || ppl.size() < PRODUCTION_LINES) {
					if (btnAddRemove.down) {
						g2.drawImage(gfx.btnAddDown, btnAddRemove.rect.x, btnAddRemove.rect.y, null);
					} else {
						g2.drawImage(gfx.btnAdd, btnAddRemove.rect.x, btnAddRemove.rect.y, null);
					}
				}
			}
		}
		btnMinusTen.disabled = true;
		if (ppl != null) {
			for (ProductionProgress p : ppl) {
				if (p.tech == gameWorld.player.selectedTech) {
					btnMinusTen.disabled = false;
					break;
				}
			}
		}
		btnMinusOne.disabled = btnMinusTen.disabled;
		btnPlusOne.disabled = btnMinusTen.disabled;
		btnPlusTen.disabled = btnMinusTen.disabled;
		
		if (btnMinusTen.disabled) {
			g2.drawImage(gfx.btnMinusTenDisabled, btnMinusTen.rect.x, btnMinusTen.rect.y, null);
		} else {
			if (btnMinusTen.down) {
				g2.drawImage(gfx.btnMinusTenDown, btnMinusTen.rect.x, btnMinusTen.rect.y, null);
			}
		}
		if (btnMinusOne.disabled) {
			g2.drawImage(gfx.btnMinusOneDisabled, btnMinusOne.rect.x, btnMinusOne.rect.y, null);
		} else {
			if (btnMinusOne.down) {
				g2.drawImage(gfx.btnMinusOneDown, btnMinusOne.rect.x, btnMinusOne.rect.y, null);
			}
		}
		if (btnPlusOne.disabled) {
			g2.drawImage(gfx.btnPlusOneDisabled, btnPlusOne.rect.x, btnPlusOne.rect.y, null);
		} else {
			if (btnPlusOne.down) {
				g2.drawImage(gfx.btnPlusOneDown, btnPlusOne.rect.x, btnPlusOne.rect.y, null);
			}
		}
		if (btnPlusTen.disabled) {
			g2.drawImage(gfx.btnPlusTenDisabled, btnPlusTen.rect.x, btnPlusTen.rect.y, null);
		} else {
			if (btnPlusTen.down) {
				g2.drawImage(gfx.btnPlusTenDown, btnPlusTen.rect.x, btnPlusTen.rect.y, null);
			}
		}
		btnSell.disabled = gameWorld.getInventoryCount(rt) == 0;
		if (btnSell.disabled) {
			g2.drawImage(gfx.btnSellDisabled, btnSell.rect.x, btnSell.rect.y, null);
		} else {
			if (btnSell.down) {
				g2.drawImage(gfx.btnSellDown, btnSell.rect.x, btnSell.rect.y, null);
			}
		}
		
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
	 * Renders the production lines.
	 * @param g2 the graphics object
	 */
	private void renderProductions(Graphics2D g2) {
		FactoryInfo fi = gameWorld.player.getFactoryInfo();
		Shape sp = g2.getClip();
		// render capacity value
		int value = -1;
		int total = -1;
		switch (clazzIndex) {
		case 1:
			value = fi.currentShip;
			total = fi.totalShip;
			break;
		case 2:
			value = fi.currentEquipment;
			total = fi.totalEquipment;
			break;
		case 3:
			value = fi.currentWeapons;
			total = fi.totalWeapons;
			break;
		default:
		}
		int color = TextGFX.GREEN;
		if (value >= total / 2 && value < total) {
			color = TextGFX.YELLOW;
		} else
		if (value < total / 2) {
			color = TextGFX.RED;
		}
		if (value < 0) {
			text.paintTo(g2, rectTotalCapacity.x, rectTotalCapacity.y, 14, color, "----");
		} else {
			String s = Integer.toString(value);
			g2.setClip(rectTotalCapacity);
			text.paintTo(g2, rectTotalCapacity.x, rectTotalCapacity.y, 14, color, s);
			String s1 = Integer.toString(total);
			int len = text.getTextWidth(7, s1);
			text.paintTo(g2, rectTotalCapacity.x + rectTotalCapacity.width - len, rectTotalCapacity.y + 4, 7
					, TextGFX.GREEN, s1);
		}
		g2.setClip(sp);
		// render production lines
		List<ProductionProgress> ppl = gameWorld.player.production.get(clazzIndex);
		if (ppl == null) {
			ppl = Collections.emptyList();
		}
		for (int i = 0; i < Math.min(lines.size(), ppl.size()); i++) {
			ProductionProgress p = ppl.get(i);
			ProductionLine pl = lines.get(i);
			color = p.tech == gameWorld.player.selectedTech ? TextGFX.RED : TextGFX.GREEN;
			text.paintTo(g2, pl.productName.x + 2, pl.productName.y + 2, 10, color, p.tech.name);
			String s = Integer.toString(p.priority); 
			int len = text.getTextWidth(10, s);
			text.paintTo(g2, pl.priority.x + (pl.priority.width - len) / 2, pl.priority.y + 1, 10, color, s);
			
			s = Integer.toString(p.capacity); 
			len = text.getTextWidth(10, s);
			text.paintTo(g2, pl.capacity.x + (pl.capacity.width - len) / 2, pl.capacity.y + 1, 10, color, s);
			
			s = Integer.toString(p.capacityPercent) + "%"; 
			len = text.getTextWidth(10, s);
			text.paintTo(g2, pl.capacityPercent.x + (pl.capacityPercent.width - len) / 2, pl.capacityPercent.y + 1, 10, color, s);
			
			s = Integer.toString(p.count); 
			len = text.getTextWidth(10, s);
			text.paintTo(g2, pl.count.x + (pl.count.width - len) / 2, pl.count.y + 1, 10, color, s);
			
			if (p.count > 0) {
				s = Integer.toString(p.progress) + "%"; 
				len = text.getTextWidth(10, s);
				text.paintTo(g2, pl.completed.x + (pl.completed.width - len) / 2, pl.completed.y + 1, 10, color, s);
				
				s = Integer.toString(p.getCost()); 
				len = text.getTextWidth(10, s);
				text.paintTo(g2, pl.cost.x + (pl.cost.width - len) / 2, pl.cost.y + 1, 10, color, s);
			}
		}
		
		g2.setClip(sp);
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
		
		btnAddRemove.setBounds(screenRect.x + 534, screenRect.y + 283, 102, 39);
		btnEquipment.setBounds(screenRect.x + 534, screenRect.y + 322, 102, 39);
		btnResearch.setBounds(screenRect.x + 534, screenRect.y + 361, 102, 39);
		btnBridge.setBounds(screenRect.x + 534, screenRect.y + 400, 102, 39);
		
		rectTotalCapacity.setBounds(screenRect.x + 420, screenRect.y + 420, 101, 15);
		btnMinusTen.setBounds(screenRect.x + 5, screenRect.y + 417, 52, 21);
		btnMinusOne.setBounds(screenRect.x + 59, screenRect.y + 417, 52, 21);
		btnPlusOne.setBounds(screenRect.x + 113, screenRect.y + 417, 52, 21);
		btnPlusTen.setBounds(screenRect.x + 167, screenRect.y + 417, 52, 21);
		btnSell.setBounds(screenRect.x + 222, screenRect.y + 417, 52, 21);
		
		int i = 0;
		for (ProductionLine line : lines) {
			line.productName.setBounds(screenRect.x + 9, screenRect.y + 306 + i * 23, 166, 14);
			line.btnLessPriority.setBounds(screenRect.x + 195, screenRect.y + 307 + i * 23, 7, 12);
			line.priority.setBounds(screenRect.x + 202, screenRect.y + 306 + i * 23, 26, 14);
			line.btnMorePriority.setBounds(screenRect.x + 228, screenRect.y + 307 + i * 23, 8, 12);
			line.capacity.setBounds(screenRect.x + 240, screenRect.y + 306 + i * 23, 54, 14);
			line.capacityPercent.setBounds(screenRect.x + 307, screenRect.y + 306 + i * 23, 44, 14);
			line.btnLessCount.setBounds(screenRect.x + 355, screenRect.y + 307 + i * 23, 7, 12);
			line.count.setBounds(screenRect.x + 362, screenRect.y + 306 + i * 23, 26, 14);
			line.btnMoreCount.setBounds(screenRect.x + 388, screenRect.y + 306 + i * 23, 8, 12);
			line.completed.setBounds(screenRect.x + 400, screenRect.y + 306 + i * 23, 50, 14);
			line.cost.setBounds(screenRect.x + 454, screenRect.y + 306 + i * 23, 69, 14);
			line.line.setBounds(screenRect.x + 9, screenRect.y + 306 + i * 23, 514, 14);
			i++;
		}
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
				List<ProductionProgress> ppl = gameWorld.player.production.get(clazzIndex);
				for (int i = 0; i < lines.size(); i++) {
					ProductionLine pl = lines.get(i);
					if (ppl != null && ppl.size() > i) {
						ProductionProgress p = ppl.get(i);
						if (pl.line.contains(pt)) {
							gameWorld.player.selectedTech = p.tech;
							clazzIndex = p.tech.clazzIndex;
							typeIndex = p.tech.typeIndex;
							repaint();
						}
						if (pl.btnLessPriority.contains(pt)) {
							p.priority = Math.max(0, p.priority - 5);
							gameWorld.player.rebalanceProduction(ppl, p.tech.clazzIndex);
							repaint();
						} else
						if (pl.btnMorePriority.contains(pt)) {
							p.priority = Math.min(100, p.priority + 5);
							gameWorld.player.rebalanceProduction(ppl, p.tech.clazzIndex);
							repaint();
						} else
						if (pl.btnLessCount.contains(pt)) {
							p.count = Math.max(0, p.count - 1);
							gameWorld.player.rebalanceProduction(ppl, p.tech.clazzIndex);
							repaint();
						} else
						if (pl.btnMoreCount.contains(pt)) {
							p.count++;
							gameWorld.player.rebalanceProduction(ppl, p.tech.clazzIndex);
							repaint();
						}
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
	/**
	 * Record to store production line rectangle locations.
	 * @author karnokd, 2009.06.10.
	 * @version $Revision 1.0$
	 */
	static class ProductionLine {
		/** The production name rectangle. */
		final Rectangle productName = new Rectangle();
		/** Less priority button rectangle. */
		final Rectangle btnLessPriority = new Rectangle();
		/** The production priority number rectangle. */
		final Rectangle priority = new Rectangle();
		/** More priority button rectangle. */
		final Rectangle btnMorePriority = new Rectangle();
		/** Capacity value rectangle. */
		final Rectangle capacity = new Rectangle();
		/** Capacity percentage rectangle. */
		final Rectangle capacityPercent = new Rectangle();
		/** Less count button rectangle. */
		final Rectangle btnLessCount = new Rectangle();
		/** The number of items to produce. */
		final Rectangle count = new Rectangle();
		/** More count button rectangle. */
		final Rectangle btnMoreCount = new Rectangle();
		/** The completed percentage. */
		final Rectangle completed = new Rectangle();
		/** The cost of the produced items. */
		final Rectangle cost = new Rectangle();
		/** The line rectangle. */
		final Rectangle line = new Rectangle();
	}
}
