package hu.openig.gfx;

import hu.openig.core.Btn;
import hu.openig.core.BtnAction;
import hu.openig.core.InfoBarRegions;
import hu.openig.core.InfoScreen;
import hu.openig.sound.UISounds;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

/**
 * Planet surface renderer class.
 * @author karnokd, 2009.01.16.
 * @version $Revision 1.0$
 */
public class InformationRenderer extends JComponent implements MouseListener, MouseMotionListener, 
MouseWheelListener, ActionListener {
	/** */
	private static final long serialVersionUID = 1638048442106816873L;
	/** The planet graphics. */
	private final InformationGFX gfx;
	/** The common graphics. */
	private final CommonGFX cgfx;
	/** The last width. */
	private int lastWidth;
	/** The last height. */
	private int lastHeight;
	/** The text renderer. */
	private TextGFX text;
	/** The user interface sounds. */
	private UISounds uiSound;
	/** Buttons which change state on click.*/
	private final List<Btn> toggleButtons = new ArrayList<Btn>();
	/** The various buttons. */
	private final List<Btn> buttons = new ArrayList<Btn>();
	/** Buttons which fire on the press mouse action. */
	private final List<Btn> pressButtons = new ArrayList<Btn>();
	/** The fixed size of this renderer. */
	private final Dimension controlSize = new Dimension();
	/** The main information area. */
	private Rectangle mainArea = new Rectangle();
	/** The top right area for title text. */
	private Rectangle titleArea = new Rectangle();
	/** Secondary information area. */
	private Rectangle secondaryArea = new Rectangle();
	/** Starmap, building picture, alien picture area depending on the current information page. */
	private Rectangle pictureArea = new Rectangle();
	/** The currently showing screen. */
	private InfoScreen currentScreen;
	private Btn btnColonyInfo;
	private Btn btnMilitaryInfo;
	private Btn btnFinancialInfo;
	private Btn btnBuildings;
	private Btn btnPlanets;
	private Btn btnFleets;
	private Btn btnInventions;
	private Btn btnAliens;
	/** The first large button beneath the picture area. Its function depends on the current screen. */ 
	private Btn btnLarge1;
	private Rectangle btnLarge1Rect = new Rectangle();
	private BufferedImage btnLarge1Normal;
	private BufferedImage btnLarge1Down;
	/** The second large button beneath the picture area. Its function depends on the current screen. */ 
	private Btn btnLarge2;
	private Rectangle btnLarge2Rect = new Rectangle();
	private BufferedImage btnLarge2Normal;
	private BufferedImage btnLarge2Down;
	
	private Btn btnColony;
	private Btn btnStarmap;
	private Btn btnEquipment;
	private Btn btnResearch;
	private Btn btnProduction;
	private Btn btnDiplomacy;
	/** The screen rectangle. */
	private Rectangle screen = new Rectangle();
	
	private boolean showMinimap = true;
	private BtnAction onColonyClicked;
	private BtnAction onStarmapClicked;
	/** Regions of the info bars. */
	public InfoBarRegions infoBarRects = new InfoBarRegions();
	/**
	 * Constructor, expecting the planet graphics and the common graphics objects.
	 * @param gfx
	 * @param cgfx
	 * @throws IOException
	 */
	public InformationRenderer(InformationGFX gfx, CommonGFX cgfx, UISounds uiSound) {
		this.gfx = gfx;
		this.cgfx = cgfx;
		this.text = cgfx.text;
		this.uiSound = uiSound;
		
		controlSize.width = gfx.infoScreen.getWidth();
		controlSize.height = gfx.infoScreen.getHeight();

		initButtons();
		
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addMouseListener(this);
//		setOpaque(true);
	}
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		int w = getWidth();
		int h = getHeight();
		
		Composite cp = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, w, h);
		g2.setComposite(cp);
		
		if (w != lastWidth || h != lastHeight) {
			lastWidth = w;
			lastHeight = h;
			// if the render window changes, re-zoom to update scrollbars
			updateRegions();
		}
		// RENDER INFOBARS
		cgfx.renderInfoBars(this, g2);
		
		g2.drawImage(gfx.infoScreen, screen.x, screen.y, null);
		
		renderButton(g2, btnColonyInfo, InfoScreen.COLONY_INFORMATION, gfx.btnColonyInfo, gfx.btnColonyInfoLight, gfx.btnColonyInfoLightDown);
		renderButton(g2, btnMilitaryInfo, InfoScreen.MILITARY_INFORMATION, gfx.btnMilitaryInfo, gfx.btnMilitaryInfoLight, gfx.btnMilitaryInfoLightDown);
		renderButton(g2, btnFinancialInfo, InfoScreen.FINANCIAL_INFORMATION, gfx.btnFinancialInfo, gfx.btnFinancialInfoLight, gfx.btnFinancialInfoLightDown);
		renderButton(g2, btnBuildings, InfoScreen.BUILDINGS, gfx.btnBuildings, gfx.btnBuildingsLight, gfx.btnBuildingsLightDown);
		
		renderButton(g2, btnPlanets, InfoScreen.PLANETS, gfx.btnPlanets, gfx.btnPlanetsLight, gfx.btnPlanetsLightDown);
		renderButton(g2, btnFleets, InfoScreen.FLEETS, gfx.btnFleets, gfx.btnFleetsLight, gfx.btnFleetsLightDown);
		renderButton(g2, btnInventions, InfoScreen.INVENTIONS, gfx.btnInventions, gfx.btnInventionsLight, gfx.btnInventionsLightDown);
		renderButton(g2, btnAliens, InfoScreen.ALIENS, gfx.btnAliens, gfx.btnAliensLight, gfx.btnAliensLightDown);
		
		if (btnLarge1 != null) {
			if (btnLarge1.down) {
				g2.drawImage(btnLarge1Down, btnLarge1Rect.x, btnLarge1Rect.y, null);
			} else {
				g2.drawImage(btnLarge1Normal, btnLarge1Rect.x, btnLarge1Rect.y, null);
			}
		} else {
			g2.drawImage(gfx.btnEmptyLarge, btnLarge1Rect.x, btnLarge1Rect.y, null);
		}
		if (btnLarge2 != null) {
			if (btnLarge2.down) {
				g2.drawImage(btnLarge2Down, btnLarge2Rect.x, btnLarge2Rect.y, null);
			} else {
				g2.drawImage(btnLarge2Normal, btnLarge2Rect.x, btnLarge2Rect.y, null);
			}
		} else {
			g2.drawImage(gfx.btnEmptyLarge, btnLarge2Rect.x, btnLarge2Rect.y, null);
		}
		
		int y = 0;
		Shape cs = g2.getClip();
		g2.setClip(mainArea);
		int size = 5;
		text.paintTo(g2, mainArea.x, mainArea.y + (y++ * 16), size, TextGFX.YELLOW, "Sample text");
		text.paintTo(g2, mainArea.x, mainArea.y + (y++ * 16), size, TextGFX.GREEN, "Sample text");
		text.paintTo(g2, mainArea.x, mainArea.y + (y++ * 16), size, TextGFX.GRAY, "Sample text");
		text.paintTo(g2, mainArea.x, mainArea.y + (y++ * 16), size, TextGFX.RED, "Sample text");
		
		text.paintTo(g2, mainArea.x, mainArea.y + (y++ * 16), size, TextGFX.DARK_GREEN, "Sample text");
		text.paintTo(g2, mainArea.x, mainArea.y + (y++ * 16), size, TextGFX.ORANGE, "Sample text");
		text.paintTo(g2, mainArea.x, mainArea.y + (y++ * 16), size, TextGFX.WHITE, "Sample text");
		text.paintTo(g2, mainArea.x, mainArea.y + (y++ * 16), size, TextGFX.CYAN, "Sample text");
		text.paintTo(g2, mainArea.x, mainArea.y + (y++ * 16), size, TextGFX.PURPLE, "Sample text");
		text.paintTo(g2, mainArea.x, mainArea.y + (y++ * 16), size, TextGFX.LIGHT_GREEN, "Sample text");
		text.paintTo(g2, mainArea.x, mainArea.y + (y++ * 16), size, TextGFX.BLUE, "Sample text");
		text.paintTo(g2, mainArea.x, mainArea.y + (y++ * 16), size, TextGFX.LIGHT_BLUE, "Sample text");
		g2.setClip(cs);
		
		if (showMinimap) {
			int viewerRank = 4;
			g2.drawImage(cgfx.minimapInfo[viewerRank], pictureArea.x, pictureArea.y, null);
			// DRAW GRID
			Shape sp = g2.getClip();
			g2.setClip(pictureArea);
			g2.setColor(cgfx.GRID_COLOR);
			Stroke st = g2.getStroke();
			g2.setStroke(cgfx.GRID_STROKE);
			int y0 = 34;
			int x0 = 40;
			for (int i = 1; i < 5; i++) {
				g2.drawLine(pictureArea.x + x0, pictureArea.y, pictureArea.x + x0, pictureArea.y + pictureArea.height);
				g2.drawLine(pictureArea.x, pictureArea.y + y0, pictureArea.x + pictureArea.width, pictureArea.y + y0);
				x0 += 41;
				y0 += 34;
			}
			int i = 0;
			y0 = 28;
			x0 = 2;
			for (char c = 'A'; c < 'Z'; c++) {
				text.paintTo(g2, pictureArea.x + x0, pictureArea.y + y0, 5, TextGFX.GRAY, String.valueOf(c));
				x0 += 41;
				i++;
				if (i % 5 == 0) {
					x0 = 2;
					y0 += 34;
				}
			}
			
			g2.setStroke(st);
			g2.setClip(sp);
		}
	}
	/**
	 * Renders a button based on its state.
	 * @param g2
	 * @param button
	 * @param screen
	 * @param light
	 * @param down
	 */
	private void renderButton(Graphics2D g2, Btn button, InfoScreen screen, 
			BufferedImage normal, BufferedImage light, BufferedImage down) {
		if (button.visible) {
			if (button.down) {
				g2.drawImage(down, button.rect.x, button.rect.y, null);
			} else
			if (currentScreen == screen) {
				g2.drawImage(light, button.rect.x, button.rect.y, null);
			} else {
				g2.drawImage(normal, button.rect.x, button.rect.y, null);
			}
		} else {
			g2.drawImage(gfx.btnEmpty, button.rect.x, button.rect.y, null);
		}
	}
	@Override
	public Dimension getPreferredSize() {
		return controlSize.getSize();
	}
//	@Override
//	public Dimension getMinimumSize() {
//		return controlSize.getSize();
//	}
//	@Override
//	public Dimension getMaximumSize() {
//		return controlSize.getSize();
//	}
//	@Override
//	public Dimension getSize() {
//		return controlSize.getSize();
//	}
	/** Initialize buttons. */
	private void initButtons() {
		pressButtons.add(btnColonyInfo = new Btn(new BtnAction() { public void invoke() { doColonyInfoClick(); } }));
		pressButtons.add(btnMilitaryInfo = new Btn(new BtnAction() { public void invoke() { doMilitaryInfoClick(); } }));
		pressButtons.add(btnFinancialInfo = new Btn(new BtnAction() { public void invoke() { doFinancialInfoClick(); } }));
		pressButtons.add(btnBuildings = new Btn(new BtnAction() { public void invoke() { doBuildingsClick(); } }));
		pressButtons.add(btnPlanets = new Btn(new BtnAction() { public void invoke() { doPlanetsClick(); } }));
		pressButtons.add(btnFleets = new Btn(new BtnAction() { public void invoke() { doFleetsClick(); } }));
		pressButtons.add(btnInventions = new Btn(new BtnAction() { public void invoke() { doInventionsClick(); } }));
		pressButtons.add(btnAliens = new Btn(new BtnAction() { public void invoke() { doAliensClick(); } }));
		
		buttons.add(btnColony = new Btn(new BtnAction() { public void invoke() { doColonyClick(); } }));
		buttons.add(btnStarmap = new Btn(new BtnAction() { public void invoke() { doStarmapClick(); } }));
		buttons.add(btnEquipment = new Btn(new BtnAction() { public void invoke() { doEquipmentClick(); } }));
		buttons.add(btnProduction = new Btn(new BtnAction() { public void invoke() { doProductionClick(); } }));
		buttons.add(btnResearch = new Btn(new BtnAction() { public void invoke() { doResearchClick(); } }));
		buttons.add(btnDiplomacy = new Btn(new BtnAction() { public void invoke() { doDiplomacyClick(); } }));
		
	}
	
	protected void doDiplomacyClick() {
		uiSound.playSound("Diplomacy");
	}
	protected void doResearchClick() {
		uiSound.playSound("Research");
	}
	protected void doProductionClick() {
		uiSound.playSound("Production");
	}
	protected void doEquipmentClick() {
		uiSound.playSound("Equipment");
	}
	protected void doStarmapClick() {
		uiSound.playSound("Starmap");
		if (onStarmapClicked != null) {
			onStarmapClicked.invoke();
		}
	}
	protected void doColonyClick() {
		uiSound.playSound("Colony");
		if (onColonyClicked != null) {
			onColonyClicked.invoke();
		}
	}
	protected void doAliensClick() {
		if (currentScreen != InfoScreen.ALIENS) {
			uiSound.playSound("AlienRaces");
			setScreenButtonsFor(InfoScreen.ALIENS);
			repaint();
		}
	}
	protected void doInventionsClick() {
		if (currentScreen != InfoScreen.INVENTIONS) {
			uiSound.playSound("Inventions");
			setScreenButtonsFor(InfoScreen.INVENTIONS);
			repaint();
		}
	}
	protected void doFleetsClick() {
		if (currentScreen != InfoScreen.FLEETS) {
			uiSound.playSound("Fleets");
			setScreenButtonsFor(InfoScreen.FLEETS);
			repaint();
		}
	}
	protected void doPlanetsClick() {
		if (currentScreen != InfoScreen.PLANETS) {
			uiSound.playSound("Planets");
			setScreenButtonsFor(InfoScreen.PLANETS);
			repaint();
		}
	}
	protected void doBuildingsClick() {
		if (currentScreen != InfoScreen.BUILDINGS) {
			uiSound.playSound("Buildings");
			setScreenButtonsFor(InfoScreen.BUILDINGS);
			repaint();
		}
	}
	protected void doFinancialInfoClick() {
		if (currentScreen != InfoScreen.FINANCIAL_INFORMATION) {
			uiSound.playSound("FinancialInformation");
			setScreenButtonsFor(InfoScreen.FINANCIAL_INFORMATION);
			repaint();
		}
	}
	protected void doMilitaryInfoClick() {
		if (currentScreen != InfoScreen.MILITARY_INFORMATION) {
			uiSound.playSound("MilitaryInformation");
			setScreenButtonsFor(InfoScreen.MILITARY_INFORMATION);
			repaint();
		}
	}
	protected void doColonyInfoClick() {
		if (currentScreen != InfoScreen.COLONY_INFORMATION) {
			uiSound.playSound("ColonyInformation");
			setScreenButtonsFor(InfoScreen.COLONY_INFORMATION);
			repaint();
		}
	}
	/**
	 * Update location of various interresting rectangles of objects.
	 */
	private void updateRegions() {
		
		cgfx.updateRegions(this, infoBarRects);
		
		screen.x = (getWidth() - gfx.infoScreen.getWidth()) / 2;
		screen.y = (getHeight() - gfx.infoScreen.getHeight()) / 2;
		
		mainArea.setBounds(screen.x + 2, screen.y + 2, 411, 362);
		
		titleArea.setBounds(screen.x + 415, screen.y + 2, 203, 26);
		
		secondaryArea.setBounds(screen.x + 415, screen.y + 30, 203, 179);
		
		pictureArea.setBounds(screen.x + 415, screen.y + 211, 203, 170);
		
		btnPlanets.rect.setBounds(screen.x + 1, screen.y + 364, 102, 28);
		btnColonyInfo.rect.setBounds(screen.x + 104, screen.y + 364, 102, 28);
		btnMilitaryInfo.rect.setBounds(screen.x + 207, screen.y + 364, 102, 28);
		btnFinancialInfo.rect.setBounds(screen.x + 310, screen.y + 364, 102, 28);
		
		btnFleets.rect.setBounds(screen.x + 1, screen.y + 392, 102, 28);
		btnBuildings.rect.setBounds(screen.x + 104, screen.y + 392, 102, 28);
		btnInventions.rect.setBounds(screen.x + 207, screen.y + 392, 102, 28);
		btnAliens.rect.setBounds(screen.x + 310, screen.y + 392, 102, 28);
		
		btnLarge1Rect.setBounds(screen.x + 413, screen.y + 381, 102, 39);
		btnLarge2Rect.setBounds(screen.x + 516, screen.y + 381, 102, 39);
		
		if (btnLarge1 != null) {
			btnLarge1.rect.setBounds(btnLarge1Rect);
		}
		if (btnLarge2 != null) {
			btnLarge2.rect.setBounds(btnLarge2Rect);
		}
	}
	@Override
	public void mouseDragged(MouseEvent e) {
	}
	@Override
	public void mouseMoved(MouseEvent e) {
	}
	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (e.getClickCount() == 1) {
				Point pt = e.getPoint();
				for (Btn b : pressButtons) {
					if (b.test(pt)) {
						b.down = true;
						b.click();
						repaint(b.rect);
					}
				}
				for (Btn b : toggleButtons) {
					if (b.test(pt)) {
						b.down = !b.down;
						b.click();
						repaint(b.rect);
					}
				}
				for (Btn b : buttons) {
					if (b.test(pt)) {
						b.down = true;
						repaint(b.rect);
					}
				}
			}
		}
	}
	/**
	 * Assign a large button to the first or second place and repaint the screen
	 * @param index the index to assign, either 1 or 2
	 * @param button the button to assign, or null to remove assignment
	 * @param normal the normal image
	 * @param down the down image
	 */
	protected void selectButtonFor(int index, Btn button, BufferedImage normal, BufferedImage down) {
		if (index == 1) {
			if (btnLarge1 != null) {
				btnLarge1.visible = false;
			}
			if (button != null) {
				button.rect.setBounds(btnLarge1Rect);
				btnLarge1 = button;
				btnLarge1.visible = true;
				btnLarge1Normal = normal;
				btnLarge1Down = down;
				repaint(button.rect);
			} else {
				btnLarge1 = null;
			}
		} else {
			if (btnLarge2 != null) {
				btnLarge2.visible = false;
			}
			if (button != null) {
				button.rect.setBounds(btnLarge2Rect);
				btnLarge2 = button;
				btnLarge2.visible = true;
				btnLarge2Normal = normal;
				btnLarge2Down = down;
				repaint(button.rect);
			} else {
				btnLarge2 = null;
			}
		}
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		boolean needRepaint = false;
		for (Btn b : buttons) {
			needRepaint |= b.down;
			b.down = false;
		}
		for (Btn b : pressButtons) {
			needRepaint |= b.down;
			b.down = false;
		}
		if (needRepaint) {
			repaint();
		}
	}
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (e.getClickCount() == 1) {
				Point pt = e.getPoint();
				for (Btn b : buttons) {
					if (b.test(pt)) {
						b.click();
					}
				}
			}
		}
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		
	}
	@Override
	public void mouseExited(MouseEvent e) {
		
	}
	@Override
	public void actionPerformed(ActionEvent e) {
	}
	/**
	 * @param onColonyClicked the onColonyClicked to set
	 */
	public void setOnColonyClicked(BtnAction onColonyClicked) {
		this.onColonyClicked = onColonyClicked;
	}
	/**
	 * @return the onColonyClicked
	 */
	public BtnAction getOnColonyClicked() {
		return onColonyClicked;
	}
	/**
	 * @param onStarmapClicked the onStarmapClicked to set
	 */
	public void setOnStarmapClicked(BtnAction onStarmapClicked) {
		this.onStarmapClicked = onStarmapClicked;
	}
	/**
	 * @return the onStarmapClicked
	 */
	public BtnAction getOnStarmapClicked() {
		return onStarmapClicked;
	}
	public void setScreenButtonsFor(InfoScreen screen) {
		if (screen != null) {
			switch (screen) {
			case COLONY_INFORMATION:
				selectButtonFor(1, btnColony, gfx.btnColonyLarge, gfx.btnColonyLargeDown);
				selectButtonFor(2, btnStarmap, gfx.btnStarmapLarge, gfx.btnStarmapLargeDown);
				break;
			case MILITARY_INFORMATION:
				selectButtonFor(1, btnColony, gfx.btnColonyLarge, gfx.btnColonyLargeDown);
				selectButtonFor(2, btnStarmap, gfx.btnStarmapLarge, gfx.btnStarmapLargeDown);
				break;
			case FINANCIAL_INFORMATION:
				selectButtonFor(1, btnColony, gfx.btnColonyLarge, gfx.btnColonyLargeDown);
				selectButtonFor(2, btnStarmap, gfx.btnStarmapLarge, gfx.btnStarmapLargeDown);
				break;
			case BUILDINGS:
				selectButtonFor(1, btnColony, gfx.btnColonyLarge, gfx.btnColonyLargeDown);
				selectButtonFor(2, btnStarmap, gfx.btnStarmapLarge, gfx.btnStarmapLargeDown);
				break;
			case PLANETS:
				selectButtonFor(1, btnColony, gfx.btnColonyLarge, gfx.btnColonyLargeDown);
				selectButtonFor(2, btnStarmap, gfx.btnStarmapLarge, gfx.btnStarmapLargeDown);
				break;
			case FLEETS:
				selectButtonFor(1, btnEquipment, gfx.btnEquipmentLarge, gfx.btnEquipmentLargeDown);
				selectButtonFor(2, btnStarmap, gfx.btnStarmapLarge, gfx.btnStarmapLargeDown);
				break;
			case INVENTIONS:
				selectButtonFor(1, btnProduction, gfx.btnProductionLarge, gfx.btnProductionLargeDown);
				selectButtonFor(2, btnResearch, gfx.btnResearchLarge, gfx.btnResearchLargeDown);
				break;
			case ALIENS:
				selectButtonFor(1, btnDiplomacy, gfx.btnDiplomacyLarge, gfx.btnDiplomacyLargeDown);
				selectButtonFor(2, null, null, null);
				break;
			}
		} else {
			selectButtonFor(1, null, null, null);
			selectButtonFor(2, null, null, null);
		}
		currentScreen = screen;
	}
}