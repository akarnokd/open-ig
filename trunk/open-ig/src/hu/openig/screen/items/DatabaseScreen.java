/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.core.Pair;
import hu.openig.gfx.DatabaseGFX;
import hu.openig.model.Player;
import hu.openig.model.Screens;
import hu.openig.model.SoundType;
import hu.openig.render.RenderTools;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.Timer;



/**
 * The database screen.
 * @author akarnokd, 2010.01.11.
 */
public class DatabaseScreen extends ScreenBase {
	/** The panel base rectangle. */
	final Rectangle base = new Rectangle();
	/** Is the picture edge visible? */
	protected boolean pictureEdgeVisible;
	/** Picture frame visible. */
	protected boolean pictureFrameVisible;
	/** The text panel visibility. */
	protected boolean textpanelVisible;
	/** The ship map. */
	protected boolean mapVisible;
	/** Help contents visible. */
	protected boolean helpVisible;
	/** Aliens contents visible. */
	protected boolean aliensVisible;
	/** Alien details displayed. */
	protected boolean alienDetails;
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
	/** The text phase index. */
	protected int textPhaseIndex;
	/** The text out phase index. */
	protected int textOutIndex;
	/** The map phase total index. */
	protected int alienPhaseIndex;
	/** Picture out index. */
	protected int pictureOutIndex;
	/** Picture in index. */
	protected int pictureInIndex;
	/** Selected help. */
	protected int selectedHelp = -1;
	/** Selected aliens. */
	protected int selectedAliens = -1;
	/** Highlighted help. */
	protected int highlightHelp = -1;
	/** Highlighted aliens. */
	protected int highlightAliens = -1;
	/** The timer for collapsing and expanding. */
	protected Timer expandCollapse;
	/** Buttons to highlight. */
	protected Set<DatabaseButton> highlight;
	/** Buttons to unlight. */
	protected Set<DatabaseButton> unlight;
	/** The button highlighter/unhighlighter. */
	protected Timer highlightTimer;
	/** The scroll text offset. */
	protected int textOffset;
	/** The text rows. */
	protected final List<String> rows = new ArrayList<>();
	/** Move up. */
	private DatabaseButton moveUp;
	/** Move down. */
	private DatabaseButton moveDown;
	/** The title of the current starship map. */
	protected String mapTitle;
	/** The blink timer. */
	protected Closeable blinkTimer;
	/** The counter used to toggle blink phases. */
	protected long blinkCounter;
	/**
	 * The database button.
	 * @author akarnokd, 2009.10.25.
	 */
	static class DatabaseButton {
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
		Action0 action;
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
		public DatabaseButton(int x, int y, BufferedImage[] phases, Action0 action) {
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
		 * @param mx the mouse x
		 * @param my the mouse y
		 * @param x0 the parent origin
		 * @param y0 the parent origin
		 * @return true if within
		 */
		boolean test(int mx, int my, int x0, int y0) {
			return mx >= x0 + x && mx <= x0 + x + width - 1
			&& my >= y0 + y && my <= y0 + y + height - 1;
		}
		/**
		 * Perform action.
		 */
		void doAction() {
			if (action != null) {
				action.invoke();
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
	protected final Set<DatabaseButton> buttons = new HashSet<>();
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

	@Override
	public void onInitialize() {
		base.setBounds(0, 0, 
				commons.database().background.getWidth(), commons.database().background.getHeight());
		
		expandCollapse = new Timer(0, null);
		expandCollapse.setDelay(50);
		
		highlight = new HashSet<>();
		unlight = new HashSet<>();
		highlightTimer = new Timer(50, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
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
		doRepaint();
	}
	/**
	 * Do actions on mouse wheel movement.
	 * @param e the event
	 */
	protected void doMouseWheelMoved(UIMouse e) {
		if (e.z < 0) {
			doMoveUp();
		} else {
			doMoveDown();
		}
	}
	/**
	 * Do actions on mouse moved.
	 * @param e the mouse event
	 */
	protected void doMouseMoved(UIMouse e) {
		boolean startTimer = false;
		for (DatabaseButton btn : buttons) {
			if (btn.test(e.x, e.y, base.x, base.y)) {
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
		int lastHighlight;
		if (helpVisible) {
			lastHighlight = highlightHelp;
			highlightHelp = -1;
			for (int i = 0; i < helpTexts.length; i++) {
				int x = base.x + 20;
				int y = base.y + 25 + 8 + i * 20;
				int x1 = x + commons.text().getTextWidth(10, get(helpTexts[i]));
				int y1 = y + 10;
				if (e.x >= x && e.x <= x1 && e.y >= y && e.y <= y1) {
					highlightHelp = i;
				}
			}
			if (lastHighlight != highlightHelp) {
				doRepaint();
			}
		}
		if (aliensVisible && !alienDetails) {
			lastHighlight = highlightAliens;
			highlightAliens = -1;
			int i = 0;
			for (Player p : getKnownOtherPlayers()) {
				int x = base.x + 20;
				int y = base.y + 25 + 8 + i * 20;
				int x1 = x + commons.text().getTextWidth(14, get("database.race." + p.id.toLowerCase(Locale.ENGLISH)));
				int y1 = y + 14;
				if (e.x >= x && e.x <= x1 && e.y >= y && e.y <= y1) {
					highlightAliens = i;
				}
				i++;
			}
			if (lastHighlight != highlightAliens) {
				doRepaint();
			}
		}
	}
	/**
	 * @return the set of other players who may appear in the database screen.
	 */
	private Iterable<Player> getKnownOtherPlayers() {
		List<Player> p = new LinkedList<>();
		for (Player q : player().knownPlayers().keySet()) {
			if (!q.noDatabase) {
				p.add(q);
			}
		}
		return p;
	}
	/**
	 * Do actions on mouse click.
	 * @param e the mouse event
	 */
	protected void doMouseClicked(UIMouse e) {
		if (expandCollapse.isRunning()) {
			return;
		}
		for (DatabaseButton btn : buttons) {
			if (btn.test(e.x, e.y, base.x, base.y)) {
				btn.doAction();
				break;
			}
		}
		if (helpVisible) {
			for (int i = 0; i < helpTexts.length; i++) {
				int x = base.x + 20;
				int y = base.y + 25 + 8 + i * 20;
				int x1 = x + commons.text().getTextWidth(10, get(helpTexts[i]));
				int y1 = y + 10;
				if (e.x >= x && e.x <= x1 && e.y >= y && e.y <= y1) {
					if (selectedHelp != i) {
						doShowText(get(helpTexts[i] + ".details"));
					}
					selectedHelp = i;
				}
			}
			doRepaint();
		}
		if (aliensVisible && !alienDetails) {
			int i = 0;
			for (Player p : getKnownOtherPlayers()) {
				int x = base.x + 20;
				int y = base.y + 25 + 8 + i * 20;
				int x1 = x + commons.text().getTextWidth(14, get("database.race." + p.id.toLowerCase()));
				int y1 = y + 14;
				if (e.x >= x && e.x <= x1 && e.y >= y && e.y <= y1) {
					selectedAliens = i;
					splitRows(get("database.race." + p.id.toLowerCase() + ".details"));
					alienDetails = true;
					doShowAlienText();
				}
				i++;
			}
			doRepaint();
		}
	}
	/**
	 * Prepare graphics objects.
	 */
	public void prepare() {
		DatabaseGFX gfx = commons.database();
		int r = max(gfx.recordMessage[0].getWidth(), gfx.aliens[0].getWidth(),
				gfx.map[0].getWidth(), gfx.help[0].getWidth(), gfx.exit[0].getWidth());
		
		int y = 53;
		int x = gfx.background.getWidth() - r - 40;
		
		buttons.clear();
		
		recordMessage = new DatabaseButton(x, y + 0 * 28 - gfx.recordMessage[0].getHeight(), gfx.recordMessage, null);
		aliens = new DatabaseButton(x, y + 1 * 28 - gfx.aliens[0].getHeight(), gfx.aliens, new Action0() {
			@Override
			public void invoke() {
				doAliensClicked();
			}
		});
		map = new DatabaseButton(x, y + 2 * 28 - gfx.map[0].getHeight(), gfx.map, new Action0() {
			@Override
			public void invoke() {
				doMapClicked();
			}
		});
		help = new DatabaseButton(x, y + 3 * 28 - gfx.help[0].getHeight(), gfx.help, new Action0() {
			@Override
			public void invoke() {
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
		
		recordMessage.action = new Action0() {
			@Override
			public void invoke() {
				if (!world().allowRecordMessage) {
					effectSound(SoundType.RECORD_MESSAGE_NO);
				} else {
					commons.playAudio(SoundType.RECORD_MESSAGE_YES.resource, new Action0() {
						@Override
						public void invoke() {
							world().scripting.onRecordMessage();
						}
					});
				}
			}
		};
		
		bridge.action = new Action0() {
			@Override
			public void invoke() {
				displayPrimary(Screens.BRIDGE);
			}
		};
		info.action = new Action0() {
			@Override
			public void invoke() {
				displaySecondary(Screens.INFORMATION_PLANETS);
			}
		};
		starmap.action = new Action0() {
			@Override
			public void invoke() {
				displayPrimary(Screens.STARMAP);
			}
		};
		diplomacy.action = new Action0() {
			@Override
			public void invoke() {
				displaySecondary(Screens.DIPLOMACY);
			}
		};
		
		exit.action = new Action0() {
			@Override
			public void invoke() {
				hideSecondary();
			}
		};
		
		moveUp = new DatabaseButton(465, 300, gfx.arrowUp, new Action0() {
			@Override
			public void invoke() {
				doMoveUp();
			}
		});
		moveDown = new DatabaseButton(465, 380, gfx.arrowDown, new Action0() {
			@Override
			public void invoke() {
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
		doHide(new Action0() {
			@Override
			public void invoke() {
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
			expandCollapse.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (pictureInIndex < picturePhaseCount) {
						pictureInIndex++;
						doRepaint();
					} else {
						pictureEdgeVisible = true;
						helpVisible = true;
						expandCollapse.stop();
						mapPhaseIndex = 0;
						mapVisible = true;
						pictureFrameVisible = true;
						clearListeners(expandCollapse);
						expandCollapse.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								if (mapPhaseIndex < mapPhaseCount) {
									mapPhaseIndex++;
									doRepaint();
								} else {
									expandCollapse.stop();
									pictureEdgeVisible = true;
									textPhaseIndex = 0;
									textpanelVisible = true;
									clearListeners(expandCollapse);
									expandCollapse.addActionListener(new ActionListener() {
										@Override
										public void actionPerformed(ActionEvent e) {
											if (textPhaseIndex < textPhaseCount) {
												textPhaseIndex++;
												doRepaint();
											} else {
												expandCollapse.stop();
												splitRows(get("database.map." + world().level));
												mapTitle = get("database.map." + world().level + ".title");
												doRepaint();
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
		doHide(new Action0() {
			@Override
			public void invoke() {
				doHelpShow();
			}
		});
	}
	/** 
	 * Hide current text or picture panels. 
	 * @param endAction the action to perform at the last step.
	 */
	protected void doHide(final Action0 endAction) {
		if (textpanelVisible) {
			textOutIndex = 0;
			clearListeners(expandCollapse);
			expandCollapse.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (textOutIndex < textPhaseCount) {
						textOutIndex++;
						doRepaint();
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
			expandCollapse.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (pictureOutIndex < picturePhaseCount) {
						pictureOutIndex++;
						doRepaint();
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
			endAction.invoke();
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
			expandCollapse.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (pictureInIndex < picturePhaseCount) {
						pictureInIndex++;
						doRepaint();
					} else {
						pictureEdgeVisible = true;
						helpVisible = true;
						expandCollapse.stop();
						doRepaint();
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
		doHide(new Action0() { @Override public void invoke() { doAliensShow(); } });
	}
	/** Aliens show. */
	protected void doAliensShow() {
		if (!pictureFrameVisible) {
			pictureFrameVisible = true;
			pictureInIndex = 0;
			selectedAliens = -1;
			highlightAliens = -1;
			clearListeners(expandCollapse);
			expandCollapse.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (pictureInIndex < picturePhaseCount) {
						pictureInIndex++;
						doRepaint();
					} else {
						pictureEdgeVisible = true;
						aliensVisible = true;
						expandCollapse.stop();
						doRepaint();
					}
				}
			});
			expandCollapse.start();
		}
	}
	/** Perform a partial repain. */
	void doRepaint() {
		scaleRepaint(base, base, margin());
	}
	/** 
	 * Show the text panel.
	 * @param newText the new text to show 
	 */
	protected void doShowText(final String newText) {
		if (textpanelVisible) {
			textOutIndex = 0;
			clearListeners(expandCollapse);
			expandCollapse.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (textOutIndex < textPhaseCount) {
						textOutIndex++;
						doRepaint();
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
		expandCollapse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (textPhaseIndex < textPhaseCount) {
					textPhaseIndex++;
					doRepaint();
				} else {
					expandCollapse.stop();
					splitRows(newText);
					doRepaint();
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
			expandCollapse.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (textOutIndex < textPhaseCount) {
						textOutIndex++;
						doRepaint();
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
		alienPhaseIndex = 0;
		textpanelVisible = true;
		clearListeners(expandCollapse);
		expandCollapse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (textPhaseIndex < textPhaseCount) {
					textPhaseIndex++;
				}
				if (alienPhaseIndex < alienPhaseCount) {
					alienPhaseIndex++;
				}
				if (textPhaseIndex >= textPhaseCount
						&& alienPhaseIndex >= alienPhaseCount) {
					expandCollapse.stop();
				}
				doRepaint();
			}
		});
		expandCollapse.start();
	}
	/**
	 * Split the rows.
	 * @param desc the original text
	 */
	protected void splitRows(String desc) {
		textOffset = 0;
		rows.clear();
		commons.text().wrapText(desc, 430, 10, rows);
	}
	/** Move text up. */
	protected void doMoveUp() {
		if (aliensVisible || helpVisible) {
			if (textOffset > 0) {
				textOffset--;
			}
			doRepaint();
		}
	}
	/** Move text down. */
	protected void doMoveDown() {
		if (aliensVisible || helpVisible) {
			if (textOffset + 10 < rows.size()) {
				textOffset++;
			}
			doRepaint();
		}
	}

	@Override
	public void onEnter(Screens mode) {
		mapPhaseIndex = 0;
		textPhaseIndex = 0;
		textOutIndex = 0;
		alienPhaseIndex = 0;
		pictureOutIndex = 0;
		pictureInIndex = 0;
		selectedHelp = -1;
		selectedAliens = -1;
		highlightHelp = -1;
		highlightAliens = -1;
		
		pictureEdgeVisible = false;
		pictureFrameVisible = false;
		textpanelVisible = false;
		mapVisible = false;
		helpVisible = false;
		aliensVisible = false;
		alienDetails = false;

		highlight.clear();
		unlight.clear();
		
		for (DatabaseButton btn : buttons) {
			btn.phase = 0;
			btn.selected = false;
		}
		
		blinkTimer = commons.register(500, new Action0() {
			@Override
			public void invoke() {
				blinkCounter++;
			}
		});
	}

	@Override
	public void onLeave() {
		highlightTimer.stop();
		expandCollapse.stop();
		close0(blinkTimer);
		blinkTimer = null;
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResize() {
		scaleResize(base, margin());
	}
	@Override
	public boolean mouse(UIMouse e) {
		scaleMouse(e, base, margin());
		if (!base.contains(e.x, e.y) && e.has(Type.DOWN)) {
			hideSecondary();
			return true;
		}
		if (e.has(Type.CLICK)) {
			doMouseClicked(e);
		} else
		if (e.has(Type.MOVE) 
				|| e.has(Type.ENTER) 
				|| e.has(Type.LEAVE)
				|| e.has(Type.DRAG)) {
			doMouseMoved(e);
		} else
		if (e.has(Type.WHEEL)) {
			doMouseWheelMoved(e);
		}
		return super.mouse(e);
	}
	@Override
	public void draw(Graphics2D g2) {
		AffineTransform savea = scaleDraw(g2, base, margin());
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);
		g2.drawImage(commons.database().background, base.x, base.y, null);

		int x0 = base.x;
		int y0 = base.y;
		
		if (blinkCounter % 2 == 0 && world().allowRecordMessage && world().messageRecording) {
			g2.setColor(Color.RED);
            int d = recordMessage.height;
			g2.fillOval(x0 + recordMessage.x + recordMessage.width + 15, y0 + recordMessage.y, d, d);
		}
		
		recordMessage.paintTo(g2, x0, y0);
		aliens.paintTo(g2, x0, y0);
		map.paintTo(g2, x0, y0);
		help.paintTo(g2, x0, y0);
		exit.paintTo(g2, x0, y0);
		
		bridge.paintTo(g2, x0, y0);
		info.paintTo(g2, x0, y0);
		starmap.paintTo(g2, x0, y0);
		if (commons.world().getShip().positions.containsKey("*diplomacy")) {
			diplomacy.paintTo(g2, x0, y0);
		}
		
		if (pictureEdgeVisible) {
			g2.drawImage(commons.database().pictureEdge[0], x0 + 4, y0 + 2, null);
			g2.drawImage(commons.database().pictureEdge[1], x0 + 4 + 18 + 300, y0 + 2, null);
			g2.drawImage(commons.database().pictureEdge[2], x0 + 4, y0 + 2 + 18 + 220, null);
			g2.drawImage(commons.database().pictureEdge[3], x0 + 4 + 18 + 300, y0 + 2 + 18 + 220, null);
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
				BufferedImage m = commons.database().shipMap[world().level - 1];
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
					commons.text().paintTo(g2, x, y + i * 20, 10, c, get(helpTexts[i]));
				}
			} else
			if (aliensVisible) {
				if (alienDetails) {
					x1 = x0 + 4 + 8;
					y1 = y0 + 2 + 8;
					BufferedImage m = null;
					Player selectedAlien = null;
					int i = 0;
					for (Player p : getKnownOtherPlayers()) {
						if (i++ == selectedAliens) {
							m = p.picture;
							selectedAlien = p;
							break;
						}
					}
					if (m != null && selectedAlien != null) {
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
							commons.text().paintTo(g2, x, y, 14, selectedAlien.color, get("database.race." + selectedAlien.id.toLowerCase()));
							commons.text().paintTo(g2, x, y + 20, 14, selectedAlien.color, selectedAlien.name);
						}
					}
				} else {
					int x = x0 + 20;
					int y = y0 + 25 + 8;
					int i = 0;
					for (Player p  : getKnownOtherPlayers()) {
						int c = selectedAliens == i ? (highlightAliens == i ? 0xFFF9090 : 0xFFFF0000) 
								: (highlightAliens == i ? 0xFFFFFFFF : 0xFFFFFF00);  
						commons.text().paintTo(g2, x, y + i * 20, 14, c, get("database.race." + p.id.toLowerCase()));
						i++;
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
			g2.drawImage(commons.database().background, x0, y0, null);

			g2.setClip(sp);
		}
		if (textpanelVisible) {
			int x1 = x0 + 9;
			int y1 = y0 + 290;
			int w1 = commons.database().textPanel.getWidth();
			int h1 = commons.database().textPanel.getHeight();
			
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
				g2.drawImage(commons.database().textPanel, x1, y1, null);
				if (mapTitle != null) {
					commons.text().paintTo(g2, x0 + 18, y0 + 299, 14, 0xFFFFFF00, mapTitle);
				}
				if (rows.size() > 0) {
					if (textOffset > 0) {
						moveUp.paintTo(g2, x0, y0);
					}
					if (textOffset + 10 < rows.size()) {
						moveDown.paintTo(g2, x0, y0);
					}
					Shape sp = g2.getClip();
					g2.setClip(new Rectangle(x0 + 18, y0 + 302, 430, 120));
					int y = y0 + 302;
					for (int i = textOffset; i < rows.size(); i++) {
						commons.text().paintTo(g2, x0 + 9 + 9, y, 10, 0xFFFFFF00, rows.get(i));
						y += 12;
					}
					g2.setClip(sp);
				}
			}
			
			// restore percent of the background
			w1 = commons.database().textPanel.getWidth();
			h1 = commons.database().textPanel.getHeight();
			
			x2 = x1 + w1 * (textPhaseCount - textOutIndex) / textPhaseCount / 2; 
			y2 = y1 + h1 * (textPhaseCount - textOutIndex) / textPhaseCount / 2; 
			
			w2 = w1 * textOutIndex / textPhaseCount;
			h2 = h1 * textOutIndex / textPhaseCount;
			
			Shape sp = g2.getClip();
			g2.setClip(new Rectangle(x2, y2, w2, h2));
			g2.drawImage(commons.database().background, x0, y0, null);
			g2.setClip(sp);
		}

		
		super.draw(g2);
		
		g2.setTransform(savea);
	}
	@Override
	public Screens screen() {
		return Screens.DATABASE;
	}
	@Override
	public void onEndGame() {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected Point scaleBase(int mx, int my) {
		UIMouse m = new UIMouse();
		m.x = mx;
		m.y = my;
		scaleMouse(m, base, margin()); 
		return new Point(m.x, m.y);
	}
	@Override
	protected Pair<Point, Double> scale() {
		Pair<Point, Double> s = scale(base, margin());
		return Pair.of(new Point(base.x, base.y), s.second);
	}
}
