/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.test;

import hu.openig.core.Act;
import hu.openig.core.Labels;
import hu.openig.gfx.DatabaseGFX;
import hu.openig.model.World;
import hu.openig.render.TextRenderer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * Paints the database screen.
 * @author karnok, 2009.10.25.
 * @version $Revision 1.0$
 */
public class DatabasePainter extends JComponent {
	/** */
	private static final long serialVersionUID = 8248585668821087238L;
	/** The database graphics. */
	protected DatabaseGFX gfx;
	/** The text renderer. */
	protected TextRenderer text;
	/** The current world. */
	protected World world;
	/** Is the picture edge visible? */
	protected boolean pictureEdgeVisible;
	/** Picture frame visible. */
	protected boolean pictureFrameVisible;
	/** The text panel visibility. */
	protected boolean textpanelVisible;
	/** The ship map. */
	protected boolean mapVisible;
	/** The map phase total count. */
	protected int mapPhaseCount = 10;
	/** The text phase count. */
	protected int textPhaseCount = 10;
	/** Picture phase count. */
	protected int picturePhaseCount = 10;
	/** Picture phase count. */
	protected int alienPhaseCount = 10;
	/** The map phase total index. */
	protected int mapPhaseIndex;
	/** The map phase total index. */
	protected int alienPhaseIndex;
	/** The timer for collapsing and expanding. */
	protected Timer expandCollapse;
	/** The text phase index. */
	protected int textPhaseIndex;
	/** The text out phase index. */
	protected int textOutIndex;
	/** Buttons to highlight. */
	protected Set<DatabaseButton> highlight;
	/** Buttons to unlight. */
	protected Set<DatabaseButton> unlight;
	/** The button highlighter/unhighlighter. */
	protected Timer highlightTimer;
	/** Picture out index. */
	protected int pictureOutIndex;
	/** Picture in index. */
	protected int pictureInIndex;
	/** Help contents visible. */
	protected boolean helpVisible;
	/** Aliens contents visible. */
	protected boolean aliensVisible;
	/** Selected help. */
	protected int selectedHelp = -1;
	/** Selected aliens. */
	protected int selectedAliens = -1;
	/** Highlighted help. */
	protected int highlightHelp = -1;
	/** Highlighted aliens. */
	protected int highlightAliens = -1;
	/** The labels. */
	protected Labels labels;
	/** The scroll text offset. */
	protected int textOffset;
	/** The text rows. */
	protected final List<String> rows = new ArrayList<String>();
	/** Move up. */
	protected DatabaseButton moveUp;
	/** Move down. */
	protected DatabaseButton moveDown;
	/** The title of the current starship map. */
	protected String mapTitle;
	/** The race description. */
	protected String raceDesc;
	/** Alien details displayed. */
	protected boolean alienDetails;
	/**
	 * The database button.
	 * @author karnok, 2009.10.25.
	 * @version $Revision 1.0$
	 */
	class DatabaseButton {
		/** X. */
		int x;
		/** Y. */
		int y;
		/** Width. */
		int width;
		/** Height. */
		int height;
		/** Phase. */
		int phase;
		/** The action to perform when clicked. */
		ActionListener action;
		/** The graphics phases. */
		BufferedImage[] phases;
		/** The selection indicator. */
		boolean selected;
		/**
		 * Constructor.
		 * @param x the X coordinate
		 * @param y the Y coordinate
		 * @param phases graphics phases
		 * @param action the action to perform on click
		 */
		public DatabaseButton(int x, int y, BufferedImage[] phases, ActionListener action) {
			this.x = x;
			this.y = y;
			this.phases = phases;
			this.action = action;
			width = phases[0].getWidth();
			height = phases[0].getHeight();
		}
		/**
		 * Paint to the given graphics object.
		 * @param g2 the graphics
		 * @param x0 the parent screen X origin
		 * @param y0 the parent screen Y origin
		 */
		void paintTo(Graphics2D g2, int x0, int y0) {
			g2.drawImage(phases[selected ? phases.length - 1 : phase], x0 + x, y0 + y, null);
		}
		/**
		 * Test a point is within the button's area.
		 * @param pt the point
		 * @param x0 the parent origin
		 * @param y0 the parent origin
		 * @return true if within
		 */
		boolean test(Point pt, int x0, int y0) {
			return pt.x >= x0 + x && pt.x <= x0 + x + width - 1
			&& pt.y >= y0 + y && pt.y <= y0 + y + height - 1;
		}
		/**
		 * Perform action.
		 */
		void doAction() {
			if (action != null) {
				action.actionPerformed(null);
			}
		}
	}
	/** Record message. */
	protected DatabaseButton recordMessage;
	/** Aliens. */
	protected DatabaseButton aliens;
	/** Map. */
	protected DatabaseButton map;
	/** Help. */
	protected DatabaseButton help;
	/** Exit. */
	protected DatabaseButton exit;
	/** Bridge. */
	protected DatabaseButton bridge;
	/** Info. */
	protected DatabaseButton info;
	/** Starmap. */
	protected DatabaseButton starmap;
	/** Diplomacy. */
	protected DatabaseButton diplomacy;
	/** The list of database buttons. */
	protected final Set<DatabaseButton> buttons;
	/** Help text labels. */
	protected String[] helpTexts = {
		"database.repair_colony_buildings",
		"database.battleship_production",
		"database.deploy_starbase",
		"database.moral_boosting",
		"database.effective_spacewar",
		"database.effective_groudwar",
		"database.spaceship_equipping",
		"database.research_labs",
		"database.tax_income",
		"database.discovering_planets"
	};
	/**
	 * Constructor.
	 * @param gfx the graphics
	 * @param text th text
	 */
	public DatabasePainter(DatabaseGFX gfx, TextRenderer text) {
		this.gfx = gfx;
		this.text = text;
		this.buttons = new HashSet<DatabaseButton>();
		expandCollapse = new Timer(0, null);
		expandCollapse.setDelay(50);
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				doMouseClicked(e, getWidth(), getHeight());
			}
		});
		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				doMouseMoved(e, getWidth(), getHeight());
			}
		});
		addMouseWheelListener(new MouseAdapter() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				doMouseWheelMoved(e);
			}
		});
		
		highlight = new HashSet<DatabaseButton>();
		unlight = new HashSet<DatabaseButton>();
		highlightTimer = new Timer(50, new Act() {
			@Override
			public void act() {
				doHighlight();
			}
		});
		
		prepare();
		
	}
	/**
	 * Do highlight phasing.
	 */
	protected void doHighlight() {
		Iterator<DatabaseButton> dbi = highlight.iterator();
		while (dbi.hasNext()) {
			DatabaseButton dbb = dbi.next();
			dbb.phase = Math.min(dbb.phase + 1, dbb.phases.length - 1);
			if (dbb.phase == dbb.phases.length - 1) {
				dbi.remove();
			}
		}
		dbi = unlight.iterator();
		while (dbi.hasNext()) {
			DatabaseButton dbb = dbi.next();
			dbb.phase = Math.max(dbb.phase - 1, 0);
			if (dbb.phase == 0) {
				dbi.remove();
			}
		}
		if (highlight.size() == 0 && unlight.size() == 0) {
			highlightTimer.stop();
		}
		repaint();
	}
	/**
	 * Do actions on mouse wheel movement.
	 * @param e the event
	 */
	protected void doMouseWheelMoved(MouseWheelEvent e) {
		if (e.getUnitsToScroll() < 0) {
			doMoveUp();
		} else {
			doMoveDown();
		}
	}
	/**
	 * Do actions on mouse moved.
	 * @param e the mouse event
	 * @param width the width
	 * @param height the height
	 */
	protected void doMouseMoved(MouseEvent e, int width, int height) {
		int x0 = (width - gfx.background.getWidth()) / 2;
		int y0 = (height - gfx.background.getHeight()) / 2;
		boolean startTimer = false;
		for (DatabaseButton btn : buttons) {
			if (btn.test(e.getPoint(), x0, y0)) {
				unlight.remove(btn);
				if (highlight.add(btn)) {
					startTimer = true;
				}
			} else {
				if (btn.phase > 0) {
					unlight.add(btn);
					highlight.remove(btn);
					startTimer = true;
				}
			}
		}
		if (startTimer) {
			highlightTimer.start();
		}
//		if (helpVisible) {
			int lastHighlight = highlightHelp;
			highlightHelp = -1;
			for (int i = 0; i < helpTexts.length; i++) {
				int x = x0 + 20;
				int y = y0 + 25 + 8 + i * 20;
				int x1 = x + text.getTextWidth(10, labels.get(helpTexts[i]));
				int y1 = y + 10;
				if (e.getX() >= x && e.getX() <= x1 && e.getY() >= y && e.getY() <= y1) {
					highlightHelp = i;
				}
			}
			if (lastHighlight != highlightHelp) {
				repaint();
			}
//		}
//		if (aliensVisible) {
			lastHighlight = highlightAliens;
			highlightAliens = -1;
			for (int i = 0; i < world.player.discoveredAliens.size(); i++) {
				int x = x0 + 20;
				int y = y0 + 25 + 8 + i * 20;
				int x1 = x + text.getTextWidth(14, labels.get(world.player.discoveredAliens.get(i)));
				int y1 = y + 14;
				if (e.getX() >= x && e.getX() <= x1 && e.getY() >= y && e.getY() <= y1) {
					highlightAliens = i;
				}
			}
			if (lastHighlight != highlightAliens) {
				repaint();
			}
//		}
	}
	/**
	 * Do actions on mouse click.
	 * @param e the mouse event
	 * @param width the width
	 * @param height the height
	 */
	protected void doMouseClicked(MouseEvent e, int width, int height) {
		if (expandCollapse.isRunning()) {
			return;
		}
		int x0 = (width - gfx.background.getWidth()) / 2;
		int y0 = (height - gfx.background.getHeight()) / 2;
		for (DatabaseButton btn : buttons) {
			if (btn.test(e.getPoint(), x0, y0)) {
				btn.doAction();
				break;
			}
		}
		if (helpVisible) {
			for (int i = 0; i < helpTexts.length; i++) {
				int x = x0 + 20;
				int y = y0 + 25 + 8 + i * 20;
				int x1 = x + text.getTextWidth(10, labels.get(helpTexts[i]));
				int y1 = y + 10;
				if (e.getX() >= x && e.getX() <= x1 && e.getY() >= y && e.getY() <= y1) {
					if (selectedHelp != i) {
						doShowText(labels.get(helpTexts[i] + ".details"));
					}
					selectedHelp = i;
				}
			}
			repaint();
		}
		if (aliensVisible) {
			for (int i = 0; i < world.player.discoveredAliens.size(); i++) {
				int x = x0 + 20;
				int y = y0 + 25 + 8 + i * 20;
				int x1 = x + text.getTextWidth(14, labels.get(world.player.discoveredAliens.get(i)));
				int y1 = y + 14;
				if (e.getX() >= x && e.getX() <= x1 && e.getY() >= y && e.getY() <= y1) {
					if (selectedAliens != i) {
						splitRows(labels.get(world.player.discoveredAliens.get(i) + ".details"));
						alienDetails = true;
						doShowAlienText();
					}
					selectedAliens = i;
				}
			}
			repaint();
		}
	}
	/**
	 * Prepare graphics objects.
	 */
	public void prepare() {
		int r = max(gfx.recordMessage[0].getWidth(), gfx.aliens[0].getWidth(),
				gfx.map[0].getWidth(), gfx.help[0].getWidth(), gfx.exit[0].getWidth());
		
		int y = 53;
		int x = gfx.background.getWidth() - r - 40;
		
		buttons.clear();
		
		recordMessage = new DatabaseButton(x, y + 0 * 28 - gfx.recordMessage[0].getHeight(), gfx.recordMessage, null);
		aliens = new DatabaseButton(x, y + 1 * 28 - gfx.aliens[0].getHeight(), gfx.aliens, new Act() {
			@Override
			public void act() {
				doAliensClicked();
			}
		});
		map = new DatabaseButton(x, y + 2 * 28 - gfx.map[0].getHeight(), gfx.map, new Act() {
			@Override
			public void act() {
				doMapClicked();
			}
		});
		help = new DatabaseButton(x, y + 3 * 28 - gfx.help[0].getHeight(), gfx.help, new Act() {
			@Override
			public void act() {
				doHelpClicked();
			}
		});
		exit = new DatabaseButton(x, y + 4 * 28 + 10 - gfx.exit[0].getHeight(), gfx.exit, null);
		
		r = max(gfx.bridge[0].getWidth(), gfx.info[0].getWidth(), gfx.starmap[0].getWidth(),
				gfx.diplomacy[0].getWidth());
		
		y = 320;
		x = gfx.background.getWidth() - r - 10;
		
		bridge = new DatabaseButton(x, y + 0 * 34 - gfx.bridge[0].getHeight(), gfx.bridge, null);
		info = new DatabaseButton(x, y + 1 * 34 - gfx.info[0].getHeight(), gfx.info, null);
		starmap = new DatabaseButton(x, y + 2 * 34 - gfx.starmap[0].getHeight(), gfx.starmap, null);
		diplomacy = new DatabaseButton(x, y + 3 * 34 - gfx.diplomacy[0].getHeight(), gfx.diplomacy, null);
		
		moveUp = new DatabaseButton(465, 300, gfx.arrowUp, new Act() {
			@Override
			public void act() {
				doMoveUp();
			}
		});
		moveDown = new DatabaseButton(465, 380, gfx.arrowDown, new Act() {
			@Override
			public void act() {
				doMoveDown();
			}
		});
		
		buttons.add(recordMessage);
		buttons.add(aliens);
		buttons.add(map);
		buttons.add(help);
		buttons.add(exit);
		buttons.add(bridge);
		buttons.add(info);
		buttons.add(starmap);
		buttons.add(diplomacy);
		buttons.add(moveUp);
		buttons.add(moveDown);
	}
	/**
	 * Paint the contents into the graphics object.
	 * @param g2 the graphics
	 * @param width the total width
	 * @param height the total height
	 */
	public void paintTo(Graphics2D g2, int width, int height) {
		int x0 = (width - gfx.background.getWidth()) / 2;
		int y0 = (height - gfx.background.getHeight()) / 2;
		g2.drawImage(gfx.background, x0, y0, null);
		
		recordMessage.paintTo(g2, x0, y0);
		aliens.paintTo(g2, x0, y0);
		map.paintTo(g2, x0, y0);
		help.paintTo(g2, x0, y0);
		exit.paintTo(g2, x0, y0);
		
		bridge.paintTo(g2, x0, y0);
		info.paintTo(g2, x0, y0);
		starmap.paintTo(g2, x0, y0);
		if (world.level >= 4) {
			diplomacy.paintTo(g2, x0, y0);
		}
		
		if (pictureEdgeVisible) {
			g2.drawImage(gfx.pictureEdge[0], x0 + 4, y0 + 2, null);
			g2.drawImage(gfx.pictureEdge[1], x0 + 4 + 18 + 300, y0 + 2, null);
			g2.drawImage(gfx.pictureEdge[2], x0 + 4, y0 + 2 + 18 + 220, null);
			g2.drawImage(gfx.pictureEdge[3], x0 + 4 + 18 + 300, y0 + 2 + 18 + 220, null);
		}
		if (pictureFrameVisible) {
			int x1 = x0 + 4 + 8;
			int y1 = y0 + 2 + 8;
			int w1 = 320;
			int h1 = 240;
			
			int x2 = x1 + w1 * (picturePhaseCount - pictureInIndex) / picturePhaseCount / 2; 
			int y2 = y1 + h1 * (picturePhaseCount - pictureInIndex) / picturePhaseCount / 2; 
			
			int w2 = w1 * pictureInIndex / picturePhaseCount;
			int h2 = h1 * pictureInIndex / picturePhaseCount;

			
			Composite cs = g2.getComposite();
			g2.setComposite(AlphaComposite.SrcOver.derive(0.75f));
			g2.setColor(Color.BLACK);
			g2.fillRect(x2, y2, w2, h2);
			g2.setComposite(cs);

			if (mapVisible) {
				x1 = x0 + 4 + 8;
				y1 = y0 + 2 + 8;
				BufferedImage m = gfx.shipMap[world.level - 1];
				w1 = m.getWidth();
				h1 = m.getHeight();
				
				x2 = x1 + w1 * (mapPhaseCount - mapPhaseIndex) / mapPhaseCount / 2; 
				y2 = y1 + h1 * (mapPhaseCount - mapPhaseIndex) / mapPhaseCount / 2; 
				
				w2 = w1 * mapPhaseIndex / mapPhaseCount;
				h2 = h1 * mapPhaseIndex / mapPhaseCount;
				
				g2.drawImage(m, x2, y2, w2, h2, null);
			} else
			if (helpVisible) {
				int x = x0 + 20;
				int y = y0 + 25 + 8;
				for (int i = 0; i < helpTexts.length; i++) {
					int c = selectedHelp == i ? (highlightHelp == i ? 0xFFF9090 : 0xFFFF0000) 
							: (highlightHelp == i ? 0xFFFFFFFF : 0xFFFFFF00);  
					text.paintTo(g2, x, y + i * 20, 10, c, labels.get(helpTexts[i]));
				}
			} else
			if (aliensVisible) {
				if (alienDetails) {
					x1 = x0 + 4 + 8;
					y1 = y0 + 2 + 8;
					BufferedImage m = gfx.shipMap[world.level - 1];
					w1 = m.getWidth();
					h1 = m.getHeight();
					
					x2 = x1 + w1 * (alienPhaseCount - alienPhaseIndex) / alienPhaseCount / 2; 
					y2 = y1 + h1 * (alienPhaseCount - alienPhaseIndex) / alienPhaseCount / 2; 
					
					w2 = w1 * alienPhaseIndex / alienPhaseCount;
					h2 = h1 * alienPhaseIndex / alienPhaseCount;
					
					g2.drawImage(m, x2, y2, w2, h2, null);
					
					if (selectedAliens >= 0) {
						int x = x0 + 20;
						int y = y0 + 2 + 8 + 202;
						text.paintTo(g2, x, y, 14, 0xFFFFFF00, labels.get(world.player.discoveredAliens.get(selectedAliens)));
						text.paintTo(g2, x, y + 20, 14, 0xFFFFFF00, labels.get(world.player.discoveredAliens.get(selectedAliens)));
					}
				} else {
					int x = x0 + 20;
					int y = y0 + 25 + 8;
					for (int i = 0; i < world.player.discoveredAliens.size(); i++) {
						int c = selectedAliens == i ? (highlightAliens == i ? 0xFFF9090 : 0xFFFF0000) 
								: (highlightAliens == i ? 0xFFFFFFFF : 0xFFFFFF00);  
						text.paintTo(g2, x, y + i * 20, 14, c, labels.get(world.player.discoveredAliens.get(i)));
					}
				}
			}

			x1 = x0 + 4 + 8;
			y1 = y0 + 2 + 8;
			w1 = 320;
			h1 = 240;
			
			x2 = x1 + w1 * (picturePhaseCount - pictureOutIndex) / picturePhaseCount / 2; 
			y2 = y1 + h1 * (picturePhaseCount - pictureOutIndex) / picturePhaseCount / 2; 
			
			w2 = w1 * pictureOutIndex / picturePhaseCount;
			h2 = h1 * pictureOutIndex / picturePhaseCount;
			
			Shape sp = g2.getClip();
			g2.setClip(new Rectangle(x2, y2, w2, h2));
			g2.drawImage(gfx.background, x0, y0, null);

			g2.setClip(sp);
		}
		if (textpanelVisible) {
			int x1 = x0 + 9;
			int y1 = y0 + 290;
			int w1 = gfx.textPanel.getWidth();
			int h1 = gfx.textPanel.getHeight();
			
			int x2 = x1 + w1 * (textPhaseCount - textPhaseIndex) / textPhaseCount / 2; 
			int y2 = y1 + h1 * (textPhaseCount - textPhaseIndex) / textPhaseCount / 2; 
			
			int w2 = w1 * textPhaseIndex / textPhaseCount;
			int h2 = h1 * textPhaseIndex / textPhaseCount;

			Composite cs = g2.getComposite();
			g2.setComposite(AlphaComposite.SrcOver.derive(0.5f));
			g2.setColor(Color.BLACK);
			g2.fillRect(x2, y2, w2, h2);
			g2.setComposite(cs);
			if (textPhaseIndex == textPhaseCount) {
				g2.drawImage(gfx.textPanel, x1, y1, null);
				if (mapTitle != null) {
					text.paintTo(g2, x0 + 18, y0 + 299, 14, 0xFFFFFF00, mapTitle);
				}
				if (rows.size() > 0) {
					if (textOffset > 0) {
						moveUp.paintTo(g2, x0, y0);
					}
					if (textOffset + 10 < rows.size()) {
						moveDown.paintTo(g2, x0, y0);
					}
					Shape sp = g2.getClip();
					g2.setClip(new Rectangle(x0 + 18, y0 + 299, 430, 120));
					int y = y0 + 299;
					for (int i = textOffset; i < rows.size(); i++) {
						text.paintTo(g2, x0 + 9 + 9, y, 10, 0xFFFFFF00, rows.get(i));
						y += 12;
					}
					g2.setClip(sp);
				}
			}
			
			// restore percent of the background
			w1 = gfx.textPanel.getWidth();
			h1 = gfx.textPanel.getHeight();
			
			x2 = x1 + w1 * (textPhaseCount - textOutIndex) / textPhaseCount / 2; 
			y2 = y1 + h1 * (textPhaseCount - textOutIndex) / textPhaseCount / 2; 
			
			w2 = w1 * textOutIndex / textPhaseCount;
			h2 = h1 * textOutIndex / textPhaseCount;
			
			Shape sp = g2.getClip();
			g2.setClip(new Rectangle(x2, y2, w2, h2));
			g2.drawImage(gfx.background, x0, y0, null);
			g2.setClip(sp);
		}
	}
	@Override
	public void paint(Graphics g) {
		paintTo((Graphics2D)g, getWidth(), getHeight());
	}
	/**
	 * Set the world object.
	 * @param w the world
	 */
	public void setWorld(World w) {
		this.world = w;
	}
	/**
	 * Find maximum of the given list of values.
	 * @param values the values array
	 * @return the return
	 */
	protected static int max(int... values) {
		int result = values[0];
		for (int i = 1; i < values.length; i++) {
			result = Math.max(result, values[i]);
		}
		return result;
	}
	/**
	 * Clear listeners from the given timer object.
	 * @param timer the timer object
	 */
	protected void clearListeners(Timer timer) {
		ActionListener[] lsnr = timer.getActionListeners();
		for (ActionListener l : lsnr) {
			timer.removeActionListener(l);
		}
	}
	/**
	 * Perform action on map button clicked.
	 */
	protected void doMapClicked() {
		map.selected = true;
		help.selected = false;
		aliens.selected = false;
		doHide(new Act() {
			@Override
			public void act() {
				doMapShow();
			}
		});
	}
	/**
	 * Show map animations.
	 */
	protected void doMapShow() {
		if (!pictureFrameVisible) {
			pictureFrameVisible = true;
			pictureInIndex = 0;
			clearListeners(expandCollapse);
			expandCollapse.addActionListener(new Act() {
				@Override
				public void act() {
					if (pictureInIndex < picturePhaseCount) {
						pictureInIndex++;
						repaint();
					} else {
						pictureEdgeVisible = true;
						helpVisible = true;
						expandCollapse.stop();
						mapPhaseIndex = 0;
						mapVisible = true;
						pictureFrameVisible = true;
						clearListeners(expandCollapse);
						expandCollapse.addActionListener(new Act() {
							@Override
							public void act() {
								if (mapPhaseIndex < mapPhaseCount) {
									mapPhaseIndex++;
									repaint();
								} else {
									expandCollapse.stop();
									pictureEdgeVisible = true;
									textPhaseIndex = 0;
									textpanelVisible = true;
									clearListeners(expandCollapse);
									expandCollapse.addActionListener(new Act() {
										@Override
										public void act() {
											if (textPhaseIndex < textPhaseCount) {
												textPhaseIndex++;
												repaint();
											} else {
												expandCollapse.stop();
												splitRows(labels.get("database.map." + world.level));
												mapTitle = labels.get("database.map." + world.level + ".title");
												repaint();
											}
										}
									});
									expandCollapse.start();
								}
							}
						});
						expandCollapse.start();
					}
				}
			});
			expandCollapse.start();
		}
	}
	/**
	 * If help clicked.
	 */
	protected void doHelpClicked() {
		map.selected = false;
		help.selected = true;
		aliens.selected = false;
		doHide(new Act() {
			@Override
			public void act() {
				doHelpShow();
			}
		});
	}
	/** 
	 * Hide current text or picture panels. 
	 * @param endAction the action to perform at the last step.
	 */
	protected void doHide(final ActionListener endAction) {
		if (textpanelVisible) {
			textOutIndex = 0;
			clearListeners(expandCollapse);
			expandCollapse.addActionListener(new Act() {
				@Override
				public void act() {
					if (textOutIndex < textPhaseCount) {
						textOutIndex++;
						repaint();
					} else {
						textpanelVisible = false;
						textOutIndex = 0;
						expandCollapse.stop();
						mapTitle = null;
						rows.clear();
						doHide(endAction);
					}
				}
			});
			expandCollapse.start();
			return;
		}
		if (pictureFrameVisible) {
			pictureOutIndex = 0;
			clearListeners(expandCollapse);
			expandCollapse.addActionListener(new Act() {
				@Override
				public void act() {
					if (pictureOutIndex < picturePhaseCount) {
						pictureOutIndex++;
						repaint();
					} else {
						pictureFrameVisible = false;
						alienDetails = false;
						pictureOutIndex = 0;
						expandCollapse.stop();
						doHide(endAction);
					}
				}
			});
			expandCollapse.start();
			return;
		}
		mapVisible = false;
		helpVisible = false;
		aliensVisible = false;
		if (endAction != null) {
			endAction.actionPerformed(null);
		}
	}
	/** Do help show animations. */
	private void doHelpShow() {
		if (!pictureFrameVisible) {
			pictureFrameVisible = true;
			pictureInIndex = 0;
			selectedHelp = -1;
			highlightHelp = -1;
			clearListeners(expandCollapse);
			expandCollapse.addActionListener(new Act() {
				@Override
				public void act() {
					if (pictureInIndex < picturePhaseCount) {
						pictureInIndex++;
						repaint();
					} else {
						pictureEdgeVisible = true;
						helpVisible = true;
						expandCollapse.stop();
						repaint();
					}
				}
			});
			expandCollapse.start();
		}
	}
	/** Aliens clicked. */
	protected void doAliensClicked() {
		map.selected = false;
		help.selected = false;
		aliens.selected = true;
		doHide(new Act() { public void act() { doAliensShow(); } });
	}
	/** Aliens show. */
	protected void doAliensShow() {
		if (!pictureFrameVisible) {
			pictureFrameVisible = true;
			pictureInIndex = 0;
			selectedAliens = -1;
			highlightAliens = -1;
			clearListeners(expandCollapse);
			expandCollapse.addActionListener(new Act() {
				@Override
				public void act() {
					if (pictureInIndex < picturePhaseCount) {
						pictureInIndex++;
						repaint();
					} else {
						pictureEdgeVisible = true;
						aliensVisible = true;
						expandCollapse.stop();
						repaint();
					}
				}
			});
			expandCollapse.start();
		}
	}
	/** 
	 * Show the text panel.
	 * @param newText the new text to show 
	 */
	protected void doShowText(final String newText) {
		if (textpanelVisible) {
			textOutIndex = 0;
			clearListeners(expandCollapse);
			expandCollapse.addActionListener(new Act() {
				@Override
				public void act() {
					if (textOutIndex < textPhaseCount) {
						textOutIndex++;
						repaint();
					} else {
						textpanelVisible = false;
						textOutIndex = 0;
						expandCollapse.stop();
						rows.clear();
						doShowText(newText);
					}
				}
			});
			expandCollapse.start();
			return;
		}
		textPhaseIndex = 0;
		textpanelVisible = true;
		clearListeners(expandCollapse);
		expandCollapse.addActionListener(new Act() {
			@Override
			public void act() {
				if (textPhaseIndex < textPhaseCount) {
					textPhaseIndex++;
					repaint();
				} else {
					expandCollapse.stop();
					splitRows(newText);
					repaint();
				}
			}
		});
		expandCollapse.start();
	}
	/** Show the text panel. */
	protected void doShowAlienText() {
		if (textpanelVisible) {
			textOutIndex = 0;
			clearListeners(expandCollapse);
			expandCollapse.addActionListener(new Act() {
				@Override
				public void act() {
					if (textOutIndex < textPhaseCount) {
						textOutIndex++;
						repaint();
					} else {
						textpanelVisible = false;
						textOutIndex = 0;
						expandCollapse.stop();
						doShowAlienText();
					}
				}
			});
			expandCollapse.start();
			return;
		}
		textPhaseIndex = 0;
		textpanelVisible = true;
		clearListeners(expandCollapse);
		expandCollapse.addActionListener(new Act() {
			@Override
			public void act() {
				if (textPhaseIndex < textPhaseCount) {
					textPhaseIndex++;
					repaint();
				} else {
					expandCollapse.stop();
					repaint();
				}
			}
		});
		expandCollapse.start();
	}
	/**
	 * Set the labels.
	 * @param labels the label
	 */
	public void setLabels(Labels labels) {
		this.labels = labels;
	}
	/**
	 * Split the rows.
	 * @param desc the original text
	 */
	protected void splitRows(String desc) {
		textOffset = 0;
		rows.clear();
		text.wrapText(desc, 430, 10, rows);
	}
	/** Move text up. */
	protected void doMoveUp() {
		if (aliensVisible || helpVisible) {
			if (textOffset > 0) {
				textOffset--;
			}
			repaint();
		}
	}
	/** Move text down. */
	protected void doMoveDown() {
		if (aliensVisible || helpVisible) {
			if (textOffset + 10 < rows.size()) {
				textOffset++;
			}
			repaint();
		}
	}
}
