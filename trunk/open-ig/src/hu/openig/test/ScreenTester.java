/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.test;

import hu.openig.Startup;
import hu.openig.core.Act;
import hu.openig.core.Configuration;
import hu.openig.core.Difficulty;
import hu.openig.core.ResourceLocator;
import hu.openig.model.WalkPosition;
import hu.openig.model.WalkShip;
import hu.openig.model.World;
import hu.openig.screens.AchievementsScreen;
import hu.openig.screens.AchievementsScreen.Mode;
import hu.openig.screens.BarScreen;
import hu.openig.screens.BattlefinishScreen;
import hu.openig.screens.BridgeScreen;
import hu.openig.screens.CommonResources;
import hu.openig.screens.DatabaseScreen;
import hu.openig.screens.DiplomacyScreen;
import hu.openig.screens.EquipmentScreen;
import hu.openig.screens.EquipmentScreen.EquipmentMode;
import hu.openig.screens.ResearchProductionScreen.RPMode;
import hu.openig.screens.GameControls;
import hu.openig.screens.InfoScreen;
import hu.openig.screens.LoadSaveScreen;
import hu.openig.screens.LoadingScreen;
import hu.openig.screens.MainScreen;
import hu.openig.screens.PlanetScreen;
import hu.openig.screens.ResearchProductionScreen;
import hu.openig.screens.ScreenBase;
import hu.openig.screens.Screens;
import hu.openig.screens.ShipwalkScreen;
import hu.openig.screens.SingleplayerScreen;
import hu.openig.screens.SpacewarScreen;
import hu.openig.screens.StarmapScreen;
import hu.openig.screens.StatusbarScreen;
import hu.openig.screens.VideoScreen;
import hu.openig.ui.UIMouse;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Utility program to improve the screen development and testing times.
 * @author karnokd
 */
public class ScreenTester extends JFrame implements GameControls {
	/** */
	private static final long serialVersionUID = -3535790397080644321L;
	/** The version number for packaging. */
	public static final String VERSION = "0.20";
	/** The parent color to use. */
	volatile Color parentColor = Color.LIGHT_GRAY;
	/** The normal screen. */
	final String txtScreen = "No screen selected. Please select a screen from the Screens menu.";
	/** The normal screen. */
	final String txtLoad = "Loading resources. This may take some seconds...";
	/** The normal screen. */
	final String txtError = "An error occurred. Please check the console output.";
	/** The parent text. */
	volatile String parentText = txtLoad;
	/** The UI language. */
	volatile String language = "hu";
	/** The render panel. */
	final JComponent surface = new JComponent() {
		/** */
		private static final long serialVersionUID = 9211819397474717113L;
		{
			setBackground(Color.LIGHT_GRAY);
			setOpaque(true);
			addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					if (screen != null) {
						screen.resize();
					}
					repaint();
				}
			});
			RepaintManager.currentManager(this).setDoubleBufferingEnabled(true);
		}
		@Override
		public void paint(Graphics g) {
			g.setColor(parentColor);
			g.fillRect(0, 0, surface.getWidth(), surface.getHeight());
			if (screen != null) {
				screen.draw((Graphics2D)g);
			} else {
				g.setColor(Color.BLACK);
				
				int w = g.getFontMetrics().stringWidth(parentText);
				int h = g.getFontMetrics().getHeight();
				
				g.drawString(parentText, (getWidth() - w) / 2, (getHeight() - h) / 2 + g.getFontMetrics().getAscent());
			}
		};
	};
	/** The global configuration object. */
	protected volatile Configuration config;
	/** The global resource locator. */
	protected volatile ResourceLocator rl;
	/** The common resources. */
	protected volatile CommonResources commons;
	/** The current screen being rendered. */
	ScreenBase screen;
	/** The action menu. */
	private JMenu menuAction;
	/** The screen menu. */
	private JMenu menuScreen;
	/** The view menu. */
	private JMenu menuView;
	/** Construct the GUI. */
	public ScreenTester() {
		super("Screen Tester");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addComponent(surface, 640, 640, Short.MAX_VALUE)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(surface, 480, 480, Short.MAX_VALUE)
		);
		
		prepareMenu();
		
		pack();
		setMinimumSize(getSize());
		MouseAdapter ma = getMouseAdapter();
		surface.addMouseListener(ma);
		surface.addMouseMotionListener(ma);
		surface.addMouseWheelListener(ma);
		surface.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				ScreenBase sb = screen;
				if (sb != null) {
					if (sb.keyboard(e)) {
						repaint();
					}
				}
			}
		});
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (screen != null) {
					try {
						screen.onLeave();
						screen.onFinish();
						screen = null;
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			};
		});
		doReload();
	}
	/**
	 * Create a wrapper for mouse events.
	 * @return the mouse adapter
	 */
	MouseAdapter getMouseAdapter() {
		MouseAdapter ma = new MouseAdapter() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				ScreenBase sb = screen;
				if (sb != null) {
					if (sb.mouse(UIMouse.from(e))) {
						repaint();
					}
				}
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				ScreenBase sb = screen;
				if (sb != null) {
					if (sb.mouse(UIMouse.from(e))) {
						repaint();
					}
				}
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				ScreenBase sb = screen;
				if (sb != null) {
					if (sb.mouse(UIMouse.from(e))) {
						repaint();
					}
				}
			}
			@Override
			public void mouseMoved(MouseEvent e) {
				ScreenBase sb = screen;
				if (sb != null) {
					if (sb.mouse(UIMouse.from(e))) {
						repaint();
					}
				}
			}
			@Override
			public void mousePressed(MouseEvent e) {
				ScreenBase sb = screen;
				if (sb != null) {
					if (sb.mouse(UIMouse.from(e))) {
						repaint();
					}
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				ScreenBase sb = screen;
				if (sb != null) {
					if (sb.mouse(UIMouse.from(e))) {
						repaint();
					}
				}
			}
		};
		return ma;
	}
	/**
	 * Enable/disable menus.
	 * @param status the status
	 */
	void enableDisableMenu(boolean status) {
		menuAction.setEnabled(status);
		menuScreen.setEnabled(status);
		menuView.setEnabled(status);
	}
	/** Build the menu bar. */
	void prepareMenu() {
		JMenuBar menubar = new JMenuBar();
		
		menuAction = new JMenu("Action");
		
		
		JMenuItem miRunGame = new JMenuItem("Run Game");
		miRunGame.addActionListener(new Act() {
			@Override
			public void act() {
				Startup.main(new String[] { });
			}
		});
		
		menuAction.add(miRunGame);
		menuAction.addSeparator();
		
		JMenuItem miRepaint = new JMenuItem("Repaint");
		miRepaint.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
		});
		JMenuItem miExit = new JMenuItem("Exit");
		miExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doExit();
			}
		});
		JMenuItem miReloadHu = new JMenuItem("Reload resources: Hungarian");
		miReloadHu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				language = "hu";
				doReload();
			}
		});
		JMenuItem miReloadEn = new JMenuItem("Reload resources: English");
		miReloadEn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				language = "en";
				doReload();
			}
		});
		
		menuAction.add(miRepaint);
		menuAction.addSeparator();
		menuAction.add(miReloadHu);
		menuAction.add(miReloadEn);
		menuAction.addSeparator();
		menuAction.add(miExit);
		
		
		menuScreen = new JMenu("Screens");
		
		menuScreen.add(addScreenItem("Bridge", BridgeScreen.class, null));
		menuScreen.add(addScreenItem("Starmap", StarmapScreen.class, null));
		menuScreen.add(addScreenItem("Planet", PlanetScreen.class, null));
		menuScreen.add(addScreenItem("Equipment", EquipmentScreen.class, null));
		menuScreen.add(addScreenItem("Production", ResearchProductionScreen.class, RPMode.PRODUCTION));
		menuScreen.add(addScreenItem("Research", ResearchProductionScreen.class, RPMode.RESEARCH));
		menuScreen.add(addScreenItem("Information", InfoScreen.class, null)); // TODO information subscreens!
		menuScreen.add(addScreenItem("Diplomacy", DiplomacyScreen.class, null));
		menuScreen.add(addScreenItem("Database", DatabaseScreen.class, null));
		menuScreen.add(addScreenItem("Bar", BarScreen.class, null));
		menuScreen.add(addScreenItem("Achievements", AchievementsScreen.class, Mode.ACHIEVEMENTS));
		menuScreen.add(addScreenItem("Statistics", AchievementsScreen.class, Mode.STATISTICS));
		menuScreen.addSeparator();
		menuScreen.add(addScreenItem("Main menu", MainScreen.class, null));
		menuScreen.add(addScreenItem("Single player menu", SingleplayerScreen.class, null));
		menuScreen.add(addScreenItem("Loading", LoadingScreen.class, null));
		menuScreen.add(addScreenItem("Status bar", StatusbarScreen.class, null));
		menuScreen.add(addScreenItem("Load & Save", LoadSaveScreen.class, null));
		menuScreen.add(addScreenItem("Shipwalk", ShipwalkScreen.class, null));
		menuScreen.add(addScreenItem("Spacewar", SpacewarScreen.class, null));
		menuScreen.add(addScreenItem("Battle finish", BattlefinishScreen.class, null));
		menuScreen.add(addScreenItem("Videos", VideoScreen.class, null));
		
		menuView = new JMenu("View");
		menuView.setVisible(false);

		menubar.add(menuAction);
		menubar.add(menuScreen);
		menubar.add(menuView);
		
		setJMenuBar(menubar);
	}
	/**
	 * Add a screen to the menu.
	 * @param name the menu item name
	 * @param clazz the menu item class
	 * @param mode the mode
	 * @return the menu item
	 */
	JMenuItem addScreenItem(String name, 
			final Class<? extends ScreenBase> clazz, final Object mode) {
		JMenuItem item = new JMenuItem(name);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setTitle(String.format("Screen Tester: %d / %d MB", Runtime.getRuntime().freeMemory() / 1024 / 1024, Runtime.getRuntime().totalMemory() / 1024 / 1024));
				if (screen != null) {
					screen.onLeave();
					screen.onFinish();
					screen = null;
				}
				try {
					screen = clazz.getConstructor().newInstance();
					screen.initialize(commons);
					screen.onEnter(mode);
					screen.resize();
					repaint();
					onScreen(clazz.getSimpleName(), screen);
				} catch (Exception ex) {
					ex.printStackTrace();
				} finally {
					setTitle(String.format("Screen Tester: %d / %d MB", Runtime.getRuntime().freeMemory() / 1024 / 1024, Runtime.getRuntime().totalMemory() / 1024 / 1024));
				}
			}
		});
		return item;
	}
	/** Exit the application. */
	void doExit() {
		try {
			if (screen != null) {
				screen.onLeave();
				screen.onFinish();
				screen = null;
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			dispose();
		}
	}
	/** Reload game resources. */
	void doReload() {
		menuView.setVisible(false);
		menuView.removeAll();
		String screenClass = null;
		if (screen != null) {
			screenClass = screen.getClass().getName();
			screen.onLeave();
			screen.onFinish();
			screen = null;
		}
		final String clazz = screenClass;
		config = null;
		commons = null;
		rl = null;
		parentColor = Color.LIGHT_GRAY;
		parentText = txtLoad;
		enableDisableMenu(false);
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				try {
					config = new Configuration("open-ig-config.xml");
					config.load();
					config.language = language;
					long t = System.nanoTime();
					commons = new CommonResources(config, ScreenTester.this);
					System.out.printf("Common resources: %.3f ms%n", (System.nanoTime() - t) / 1000000.0);
					rl = commons.rl;

					t = System.nanoTime();
					commons.world = new World();
					commons.world.definition = SingleplayerScreen.parseDefinition(commons, "campaign/main");
					commons.world.difficulty = Difficulty.values()[0];
					commons.labels().load(commons.rl, commons.language(), commons.world.definition.name);
					commons.world.load(commons.rl, commons.language(), commons.world.definition.name);
					System.out.printf("Rest: %.3f ms%n", (System.nanoTime() - t) / 1000000.0);
				} catch (Throwable t) {
					t.printStackTrace();
					parentColor = new Color(0xFFFF8080);
					parentText = txtError;
				}
				return null;
			}
			@Override
			protected void done() {
				try {
					parentColor = new Color(0xFF80FF80);
					parentText = txtScreen;
					enableDisableMenu(true);
					// restore the current screen
					if (clazz != null) {
						try {
							screen = ScreenBase.class.cast(Class.forName(clazz).newInstance());
							screen.initialize(commons);
							screen.onEnter(null);
							screen.resize();
							repaint();
							onScreen(screen.getClass().getSimpleName(), screen);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					repaint();
				} finally {
					setTitle(String.format("Screen Tester: %d / %d MB", Runtime.getRuntime().freeMemory() / 1024 / 1024, Runtime.getRuntime().totalMemory() / 1024 / 1024));
				}
			}
		};
		worker.execute();
		repaint();
	}
	
	/**
	 * @param args no arguments
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ScreenTester tester = new ScreenTester();
				tester.setLocationRelativeTo(null);
				tester.setVisible(true);
			}
		});
	}
	@Override
	public void switchLanguage(String newLanguage) {
		
	}
	@Override
	public void displayPrimary(Screens newScreen) {
		if (screen != null) {
			screen.onLeave();
			screen.onFinish();
			screen = null;
		}
		String clazz = null;
		Object mode = null;
		switch (newScreen) {
		case ACHIEVEMENTS:
			clazz = AchievementsScreen.class.getName();
			break;
		case BAR:
			clazz = BarScreen.class.getName();
			break;
		case BRIDGE:
			clazz = BridgeScreen.class.getName();
			break;
		case COLONY:
			clazz = PlanetScreen.class.getName();
			break;
		case EQUIPMENT:
			clazz = EquipmentScreen.class.getName();
			break;
		case DIPLOMACY:
			clazz = DiplomacyScreen.class.getName();
			break;
		case INFORMATION:
			clazz = InfoScreen.class.getName();
			break;
		case PRODUCTION:
			clazz = ResearchProductionScreen.class.getName();
			mode = RPMode.PRODUCTION;
			break;
		case RESEARCH:
			clazz = ResearchProductionScreen.class.getName();
			mode = RPMode.RESEARCH;
			break;
		case SPACEWAR:
			clazz = SpacewarScreen.class.getName();
			break;
		case STARMAP:
			clazz = StarmapScreen.class.getName();
			break;
		case SHIPWALK:
			clazz = ShipwalkScreen.class.getName();
			break;
		case DATABASE:
			clazz = DatabaseScreen.class.getName();
			break;
		case LOADING:
			clazz = LoadingScreen.class.getName();
			break;
		case LOAD_SAVE:
			clazz = LoadSaveScreen.class.getName();
			break;
		case MAIN:
			clazz = MainScreen.class.getName();
			break;
		case MULTIPLAYER:
			clazz = null; // TODO multiplayer screen
			break;
		case SINGLEPLAYER:
			clazz = SingleplayerScreen.class.getName();
			break;
		case VIDEOS:
			clazz = VideoScreen.class.getName();
			break;
		default:
		}
		if (clazz != null) {
			try {
				screen = ScreenBase.class.cast(Class.forName(clazz).newInstance());
				screen.initialize(commons);
				screen.onEnter(mode);
				screen.resize();
				repaint();
				onScreen(screen.getClass().getSimpleName(), screen);
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			} catch (IllegalAccessException ex) {
				ex.printStackTrace();
			} catch (InstantiationException ex) {
				ex.printStackTrace();
			}
		}
	}
	@Override
	public void displaySecondary(Screens newScreen) {
		displayPrimary(newScreen);
	}
	@Override
	public void hideSecondary() {
		
	}
	@Override
	public void displayStatusbar() {
		
	}
	@Override
	public void hideStatusbar() {
		
	}
	@Override
	public void setWindowBounds(int x, int y, int width, int height) {
		
	}
	@Override
	public Rectangle getWindowBounds() {
		return null;
	}
	@Override
	public void center() {
		
	}
	@Override
	public void exit() {
		
	}
	@Override
	public void playVideos(Act onComplete, String... videos) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void playVideos(String... videos) {
		// TODO Auto-generated method stub
	}
	/**
	 * Invoke some custom action after displaying a screen.
	 * @param className the screen's class name.
	 * @param screen the screen object
	 */
	void onScreen(String className, final ScreenBase screen) {
		menuView.removeAll();
		menuView.setVisible(false);
		if ("ShipwalkScreen".equals(className)) {
			prepareShipWalkMenu(screen);
			menuView.setVisible(true);
		} else
		if ("MainMenu".equals(className)) {
			prepareMainMenuMenu(screen);
			menuView.setVisible(true);
		} else
		if ("EquipmentScreen".equals(className)) {
			prepareEquipmentMenu((EquipmentScreen)screen);
			menuView.setVisible(true);
		}
	}
	/**
	 * Prepare the menu for the equipment screen.
	 * @param screen the screen
	 */
	void prepareEquipmentMenu(final EquipmentScreen screen) {
		JMenu mode = new JMenu("Mode");
		menuView.add(mode);
		
		for (final EquipmentMode md : EquipmentMode.values()) {
			JMenuItem mi = new JMenuItem(md.toString());
			mi.addActionListener(new Act() {
				@Override
				public void act() {
					screen.setEquipmentMode(md);
					repaint();
				}
			});
			mode.add(mi);
		}
	}
	/**
	 * Prepare the menu for the main menu screen.
	 * @param screen the screen
	 */
	void prepareMainMenuMenu(ScreenBase screen) {
		final MainScreen mm = (MainScreen)screen;
		int i = 1;
		for (final BufferedImage img : commons.background().start) {
			JMenuItem mi = new JMenuItem("Background #" + i);
			mi.addActionListener(new Act() {
				@Override
				public void act() {
					mm.useBackground(img);
				}
			});
			menuView.add(mi);
			i++;
		}
	}
	/**
	 * Prepare the menu for the ship walk screen.
	 * @param screen the ship walk screen.
	 */
	private void prepareShipWalkMenu(final ScreenBase screen) {
		final ShipwalkScreen sw = (ShipwalkScreen)screen;
		WalkShip last = null;
		List<WalkShip> list = new ArrayList<WalkShip>(commons.world.walks.ships.values());
		Collections.sort(list, new Comparator<WalkShip>() {
			@Override
			public int compare(WalkShip o1, WalkShip o2) {
				return o1.level.compareTo(o2.level);
			}
		});
		int j = 0;
		for (final WalkShip s : list) {
			if (j++ > 0) {
				menuView.addSeparator();
			}
			JMenuItem mi = new JMenuItem("Level " + s.level);
			last = s;
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					sw.position = s.positions.get("*bridge");
					repaint();
				}
			});
			menuView.add(mi);
			JMenu sm = new JMenu("Locations");
			List<Map.Entry<String, WalkPosition>> el = new ArrayList<Map.Entry<String, WalkPosition>>(s.positions.entrySet());
			Collections.sort(el, new Comparator<Map.Entry<String, WalkPosition>>() {
				@Override
				public int compare(Entry<String, WalkPosition> o1,
						Entry<String, WalkPosition> o2) {
					String s1 = o1.getKey().startsWith("*") ? o1.getKey().substring(1) : o1.getKey();
					String s2 = o2.getKey().startsWith("*") ? o2.getKey().substring(1) : o2.getKey();
					return s1.compareTo(s2);
				}
			});
			for (final Map.Entry<String, WalkPosition> e : el) {
				JMenuItem pos = new JMenuItem(e.getKey());
				
				pos.addActionListener(new Act() {
					@Override
					public void act() {
						sw.position = e.getValue();
						repaint();
					}
				});
				
				sm.add(pos);
			}
			
			menuView.add(sm);
			
		}
		if (last != null) {
			sw.position = last.positions.get("*bridge");
			repaint();
		}
	}
	@Override
	public int getInnerHeight() {
		return surface.getHeight();
	}
	@Override
	public int getInnerWidth() {
		// TODO Auto-generated method stub
		return surface.getWidth();
	}
	@Override
	public void repaintInner() {
		surface.repaint();
	}
	@Override
	public void repaintInner(int x, int y, int w, int h) {
		surface.repaint(x, y, w, h);
	}
	@Override
	public FontMetrics fontMetrics(int size) {
		return getFontMetrics(getFont().deriveFont((float)size).deriveFont(Font.BOLD));
	}
}
