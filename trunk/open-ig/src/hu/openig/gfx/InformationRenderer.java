package hu.openig.gfx;

import hu.openig.core.Btn;
import hu.openig.core.BtnAction;
import hu.openig.sound.UISounds;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
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
	public static enum InfoScreen {
		COLONY_INFORMATION,
		MILITARY_INFORMATION,
		FINANCIAL_INFORMATION,
		BUILDINGS,
		PLANETS,
		FLEETS,
		INVENTIONS,
		ALIENS
	}
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
	private InfoScreen currentScreen = InfoScreen.COLONY_INFORMATION;
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
	/**
	 * Constructor, expecting the planet graphics and the common graphics objects.
	 * @param gfx
	 * @param cgfx
	 * @throws IOException
	 */
	public InformationRenderer(InformationGFX gfx, CommonGFX cgfx, UISounds uiSound) throws IOException {
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
		setOpaque(true);
	}
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		int w = getWidth();
		int h = getHeight();
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, w, h);

		if (w != lastWidth || h != lastHeight) {
			lastWidth = w;
			lastHeight = h;
			// if the render window changes, re-zoom to update scrollbars
			updateRegions();
		}
		g2.drawImage(gfx.infoScreen, 0, 0, null);
		
		renderButton(g2, btnColonyInfo, InfoScreen.COLONY_INFORMATION, gfx.btnColonyInfoLight, gfx.btnColonyInfoLightDown);
		renderButton(g2, btnMilitaryInfo, InfoScreen.MILITARY_INFORMATION, gfx.btnMilitaryInfoLight, gfx.btnMilitaryInfoLightDown);
		renderButton(g2, btnFinancialInfo, InfoScreen.FINANCIAL_INFORMATION, gfx.btnFinancialInfoLight, gfx.btnFinancialInfoLightDown);
		renderButton(g2, btnBuildings, InfoScreen.BUILDINGS, gfx.btnBuildingsLight, gfx.btnBuildingsLightDown);
		
		renderButton(g2, btnPlanets, InfoScreen.PLANETS, gfx.btnPlanetsLight, gfx.btnPlanetsLightDown);
		renderButton(g2, btnFleets, InfoScreen.FLEETS, gfx.btnFleetsLight, gfx.btnFleetsLightDown);
		renderButton(g2, btnInventions, InfoScreen.INVENTIONS, gfx.btnInventionsLight, gfx.btnInventionsLightDown);
		renderButton(g2, btnAliens, InfoScreen.ALIENS, gfx.btnAliensLight, gfx.btnAliensLightDown);
		
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
			BufferedImage light, BufferedImage down) {
		if (button.visible) {
			if (button.down) {
				g2.drawImage(down, button.rect.x, button.rect.y, null);
			} else
			if (currentScreen == screen) {
				g2.drawImage(light, button.rect.x, button.rect.y, null);
			}
		} else {
			g2.drawImage(gfx.btnEmpty, button.rect.x, button.rect.y, null);
		}
	}
	@Override
	public Dimension getPreferredSize() {
		return controlSize.getSize();
	}
	@Override
	public Dimension getMinimumSize() {
		return controlSize.getSize();
	}
	@Override
	public Dimension getMaximumSize() {
		return controlSize.getSize();
	}
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
	}
	protected void doColonyClick() {
		uiSound.playSound("Colony");
	}
	protected void doAliensClick() {
		if (currentScreen != InfoScreen.ALIENS) {
			currentScreen = InfoScreen.ALIENS;
			uiSound.playSound("AlienRaces");
			repaint();
		}
	}
	protected void doInventionsClick() {
		if (currentScreen != InfoScreen.INVENTIONS) {
			currentScreen = InfoScreen.INVENTIONS;
			uiSound.playSound("Inventions");
			repaint();
		}
	}
	protected void doFleetsClick() {
		if (currentScreen != InfoScreen.FLEETS) {
			currentScreen = InfoScreen.FLEETS;
			uiSound.playSound("Fleets");
			repaint();
		}
	}
	protected void doPlanetsClick() {
		if (currentScreen != InfoScreen.PLANETS) {
			currentScreen = InfoScreen.PLANETS;
			uiSound.playSound("Planets");
			repaint();
		}
	}
	protected void doBuildingsClick() {
		if (currentScreen != InfoScreen.BUILDINGS) {
			currentScreen = InfoScreen.BUILDINGS;
			uiSound.playSound("Buildings");
			repaint();
		}
	}
	protected void doFinancialInfoClick() {
		if (currentScreen != InfoScreen.FINANCIAL_INFORMATION) {
			currentScreen = InfoScreen.FINANCIAL_INFORMATION;
			uiSound.playSound("FinancialInformation");
			repaint();
		}
	}
	protected void doMilitaryInfoClick() {
		if (currentScreen != InfoScreen.MILITARY_INFORMATION) {
			currentScreen = InfoScreen.MILITARY_INFORMATION;
			uiSound.playSound("MilitaryInformation");
			repaint();
		}
	}
	protected void doColonyInfoClick() {
		if (currentScreen != InfoScreen.COLONY_INFORMATION) {
			currentScreen = InfoScreen.COLONY_INFORMATION;
			uiSound.playSound("ColonyInformation");
			repaint();
		}
	}
	/**
	 * Update location of various interresting rectangles of objects.
	 */
	private void updateRegions() {
		mainArea.setBounds(2, 2, 411, 362);
		
		titleArea.setBounds(415, 2, 203, 26);
		
		secondaryArea.setBounds(415, 30, 203, 179);
		
		pictureArea.setBounds(415, 211, 203, 170);
		
		btnColonyInfo.rect.setBounds(1, 364, 102, 28);
		btnMilitaryInfo.rect.setBounds(104, 364, 102, 28);
		btnFinancialInfo.rect.setBounds(207, 364, 102, 28);
		btnBuildings.rect.setBounds(310, 364, 102, 28);
		
		btnPlanets.rect.setBounds(1, 392, 102, 28);
		btnFleets.rect.setBounds(104, 392, 102, 28);
		btnInventions.rect.setBounds(207, 392, 102, 28);
		btnAliens.rect.setBounds(310, 392, 102, 28);
		
		btnLarge1Rect.setBounds(413, 381, 102, 39);
		btnLarge2Rect.setBounds(516, 381, 102, 39);
		
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
	 */
	protected void selectButtonFor(int index, Btn button) {
		if (index == 1) {
			if (btnLarge1 != null) {
				btnLarge1.visible = false;
			}
			if (button != null) {
				button.rect.setBounds(btnLarge1Rect);
				btnLarge1 = button;
				btnLarge1.visible = true;
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
			} else {
				btnLarge2 = null;
			}
		}
		repaint(button.rect);
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
}