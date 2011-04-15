/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig;

import hu.openig.core.Act;
import hu.openig.core.Configuration;
import hu.openig.core.Labels;
import hu.openig.core.ResourceLocator;
import hu.openig.model.Building;
import hu.openig.model.GameDefinition;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.Screens;
import hu.openig.model.SelectionMode;
import hu.openig.model.World;
import hu.openig.screen.CommonResources;
import hu.openig.screen.GameControls;
import hu.openig.screen.ScreenBase;
import hu.openig.screen.items.AchievementsScreen;
import hu.openig.screen.items.BarScreen;
import hu.openig.screen.items.BattlefinishScreen;
import hu.openig.screen.items.BridgeScreen;
import hu.openig.screen.items.DatabaseScreen;
import hu.openig.screen.items.DiplomacyScreen;
import hu.openig.screen.items.EquipmentScreen;
import hu.openig.screen.items.InfoScreen;
import hu.openig.screen.items.LoadSaveScreen;
import hu.openig.screen.items.LoadingScreen;
import hu.openig.screen.items.MainScreen;
import hu.openig.screen.items.MovieScreen;
import hu.openig.screen.items.PlanetScreen;
import hu.openig.screen.items.ResearchProductionScreen;
import hu.openig.screen.items.ShipwalkScreen;
import hu.openig.screen.items.SingleplayerScreen;
import hu.openig.screen.items.SpacewarScreen;
import hu.openig.screen.items.StarmapScreen;
import hu.openig.screen.items.StatusbarScreen;
import hu.openig.screen.items.VideoScreen;
import hu.openig.ui.UIMouse;
import hu.openig.utils.XElement;

import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

/**
 * The base game window which handles paint and input events.
 * @author akarnokd, 2009.12.23.
 */
public class GameWindow extends JFrame implements GameControls {
	/**	 */
	private static final long serialVersionUID = 4521036079508511968L;
	/** 
	 * The component that renders the primary and secondary screens into the current window.
	 * @author akarnokd, 2009.12.23.
	 */
	class ScreenRenderer extends JComponent {
		/** */
		private static final long serialVersionUID = -4538476567504582641L;
		/** Constructor. */
		public ScreenRenderer() {
			addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					try {
						if (primary != null) {
							primary.resize();
						}
						if (secondary != null) {
							secondary.resize();
						}
						if (movie != null) {
							movie.resize();
						}
						if (statusbarVisible) {
							statusbar.resize();
						}
					} catch (Throwable t) {
						t.printStackTrace();
					}
					repaintRequest = true;
					repaintRequestPartial = false;
				}
			});
		}
		@Override
		public void paint(Graphics g) {
			boolean r0 = repaintRequest;
			boolean r1 = repaintRequestPartial;
			repaintRequest = false;
			repaintRequestPartial = false;
			
			Graphics2D g2 = (Graphics2D)g;
			try {
				if (movieVisible) {
					movie.draw(g2);
				} else {
					if (r1 && !r0) {
						if (secondary != null) {
							secondary.draw(g2);
						}
					} else {
						if (primary != null) {
							primary.draw(g2);
						}
						if (secondary != null) {
							secondary.draw(g2);
						}
						if (statusbarVisible) {
							statusbar.draw(g2);
						}
					}
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
	/** The record of screens. */
	public final AllScreens allScreens = new AllScreens();
	/** The record of screens. */
	public final class AllScreens {
		/** Private constructor. */
		private AllScreens() {

		}
		/** Main menu. */
		public MainScreen main;
		/** Videos. */
		public VideoScreen videos;
		/** Bridge. */
		public BridgeScreen bridge;
		/** Starmap. */
		public StarmapScreen starmap;
		/** Colony. */
		public PlanetScreen colony;
		/** Equipment. */
		public EquipmentScreen equipment;
		/** Research and production. */
		public ResearchProductionScreen researchProduction;
		/** Information. */
		public InfoScreen info;
		/** Diplomacy. */
		public DiplomacyScreen diplomacy;
		/** Database. */
		public DatabaseScreen database;
		/** Bar. */
		public BarScreen bar;
		/** Statistics and achievements. */
		public AchievementsScreen statisticsAchievements;
		/** Spacewar. */
		public SpacewarScreen spacewar;
		/** Single player. */
		public SingleplayerScreen singleplayer;
		/** Load and save. */
		public LoadSaveScreen loadSave;
		/** Battle finish screen. */
		public BattlefinishScreen battleFinish;
		/** The movie screens. */
		public MovieScreen movie;
		/** The loading in progress screen. */
		public LoadingScreen loading;
		/** The ship walk screen. */
		public ShipwalkScreen shipwalk;
		/** The status bar screen. */
		public StatusbarScreen statusbar;
	}
	/** A pending repaint request. */
	boolean repaintRequest;
	/** A partial repaint request. */
	boolean repaintRequestPartial;
	/** The primary screen. */
	ScreenBase primary;
	/** The secondary screen drawn over the first. */
	ScreenBase secondary;
	/** The status bar to display over the primary and secondary screens. */
	ScreenBase statusbar;
	/** The top overlay for playing 'full screen' movies. */
	MovieScreen movie;
	/** Is the status bar visible? */
	boolean statusbarVisible;
	/** Is the movie visible. */
	boolean movieVisible;
	/** The configuration object. */
	Configuration config;
	/** The common resource locator. */
	ResourceLocator rl;
	/** The common resources. */
	CommonResources commons;
	/** The surface used to render the screens. */
	ScreenRenderer surface;
	/** The list of screens. */
	List<ScreenBase> screens;
	/** The location save to switch back from full-screen. */
	public int saveX;
	/** The location save to switch back from full-screen. */
	public int saveY;
	/** The location save to switch back from full-screen. */
	public int saveWidth;
	/** The location save to switch back from full-screen. */
	public int saveHeight;
	/** 
	 * Constructor. 
	 * @param config the configuration object.
	 */
	public GameWindow(Configuration config) {
		super("Open Imperium Galactica " + Configuration.VERSION);
		URL icon = this.getClass().getResource("/hu/openig/gfx/open-ig-logo.png");
		if (icon != null) {
			try {
				setIconImage(ImageIO.read(icon));
			} catch (IOException e) {
				e.printStackTrace();
//				config.log("ERROR", e.getMessage(), e);
			}
		}
		
		this.commons = new CommonResources(config, this);
		this.config = config;
		this.rl = commons.rl;
		this.surface = new ScreenRenderer();
		
		if (config.fullScreen) {
			this.setUndecorated(true);
		}
		
		RepaintManager.currentManager(this).setDoubleBufferingEnabled(true);
		
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
		pack();
		setMinimumSize(getSize());
		setLocationRelativeTo(null);
//		if (config.width > 0) {
//			setBounds(config.left, config.top, config.width, config.height);
//		}
		
		// Event handling
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		MouseActions ma = new MouseActions();
		surface.addMouseListener(ma);
		surface.addMouseMotionListener(ma);
		surface.addMouseWheelListener(ma);
		addKeyListener(new KeyEvents());
		
		// load resources
		initScreens();
		if (config.left != null && config.top != null) {
			this.setLocation(config.left, config.top);
			saveX = config.left;
			saveY = config.top;
		}
		if (config.width != null && config.height != null) {
			this.setSize(config.width, config.height);
			saveWidth = config.width;
			saveHeight = config.height;
		}
		if (config.maximized) {
			this.setExtendedState(MAXIMIZED_BOTH);
		}

		if (config.fullScreen) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
					for (final GraphicsDevice gs : ge.getScreenDevices()) {
						if (gs.getDefaultConfiguration().getBounds().intersects(getBounds())) {
							gs.setFullScreenWindow(GameWindow.this);
							setSize(gs.getDefaultConfiguration().getBounds().getSize());
							break;
						}
					}
				}
			});
		}
	}
	/** 
	 * A copy constructor to save the state of a previous window without reloading
	 * the resources. 
	 * @param that the previous window
	 * @param undecorated should it be undecorated?
	 */
	public GameWindow(GameWindow that, boolean undecorated) {
		setUndecorated(undecorated);
		this.setTitle(that.getTitle());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});

		this.surface = new ScreenRenderer();
		
		this.commons = that.commons;
		this.commons.control(this);
		this.rl = that.rl;
		this.config = that.config;
		this.screens = that.screens;
		setIconImage(that.getIconImage());
		this.primary = that.primary;
		this.secondary = that.secondary;
		this.movie = that.movie;
		this.movieVisible = that.movieVisible;
		this.statusbar = that.statusbar;
		this.statusbarVisible = that.statusbarVisible;
		
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
		pack();
		setMinimumSize(getSize());
		setLocationRelativeTo(null);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		MouseActions ma = new MouseActions();
		surface.addMouseListener(ma);
		surface.addMouseMotionListener(ma);
		surface.addMouseWheelListener(ma);
		addKeyListener(new KeyEvents());
	}
	@Override
	public void exit() {
		commons.stop();
		uninitScreens();
		
		config.fullScreen = isUndecorated();
		if (!config.fullScreen) {
			config.maximized = (getExtendedState() & MAXIMIZED_BOTH) != 0;
			if (!config.maximized) {
				config.left = getX();
				config.top = getY();
				config.width = getWidth();
				config.height = getHeight();
			}
		} else {
			config.left = saveX;
			config.top = saveY;
			config.width = saveWidth;
			config.height = saveHeight;
		}
		config.save();
		
		dispose();
		try {
			config.watcherWindow.close();
		} catch (IOException e) {
		}
	}
	@Override
	public void switchLanguage(String newLanguage) {
		commons.reinit(newLanguage);
		for (ScreenBase sb : screens) {
			sb.initialize(commons);
		}
		repaintInner();
	}
	/** Initialize the various screen renderers. */
	protected void initScreens() {
		screens = new ArrayList<ScreenBase>();
		
		try {
			for (Field f : allScreens.getClass().getFields()) {
				if (ScreenBase.class.isAssignableFrom(f.getType())) {
					ScreenBase sb = ScreenBase.class.cast(f.getType().newInstance());
					f.set(allScreens, sb);
					screens.add(sb);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		for (ScreenBase sb : screens) {
			sb.initialize(commons);
		}
		movie = allScreens.movie;
		statusbar = allScreens.statusbar;
		
		displayPrimary(Screens.MAIN);
	}
	/** Unitialize the screens. */
	protected void uninitScreens() {
		for (ScreenBase sb : screens) {
			if (primary == sb || secondary == sb) {
				sb.onLeave();
			}
			sb.onFinish();
		}
		primary = null;
		secondary = null;
	}
	/**
	 * Returns a screen instance for the given screen enum.
	 * @param screen the screen.
	 * @param asPrimary as primary screen?
	 * @param mode the mode to pass into the screen, might be overridden
	 * @return the reference to the new screen.
	 */
	ScreenBase display(Screens screen, boolean asPrimary, Screens mode) {
		ScreenBase sb = null;
		switch (screen) {
		case ACHIEVEMENTS:
			sb = allScreens.statisticsAchievements;
			mode = Screens.ACHIEVEMENTS;
			break;
		case STATISTICS:
			sb = allScreens.statisticsAchievements;
			mode = Screens.STATISTICS;
			break;
		case BAR:
			sb = allScreens.bar;
			break;
		case BRIDGE:
			sb = allScreens.bridge;
			break;
		case COLONY:
			sb = allScreens.colony;
			break;
		case DIPLOMACY:
			sb = allScreens.diplomacy;
			break;
		case EQUIPMENT:
			sb = allScreens.equipment;
			break;
		case INFORMATION_COLONY:
		case INFORMATION_ALIENS:
		case INFORMATION_BUILDINGS:
		case INFORMATION_FINANCIAL:
		case INFORMATION_FLEETS:
		case INFORMATION_INVENTIONS:
		case INFORMATION_MILITARY:
		case INFORMATION_PLANETS:
			sb = allScreens.info;
			mode = screen;
			break;
		case PRODUCTION:
			sb = allScreens.researchProduction;
			mode = Screens.PRODUCTION;
			break;
		case RESEARCH:
			sb = allScreens.researchProduction;
			mode = Screens.RESEARCH;
			break;
		case SPACEWAR:
			sb = allScreens.spacewar;
			break;
		case STARMAP:
			sb = allScreens.starmap;
			break;
		case SHIPWALK:
			sb = allScreens.shipwalk;
			break;
		case DATABASE:
			sb = allScreens.database;
			break;
		case LOADING:
			sb = allScreens.loading;
			break;
		case LOAD_SAVE:
			sb = allScreens.loadSave;
			break;
		case MAIN:
			sb = allScreens.main;
			break;
		case MULTIPLAYER:
			sb = null; // TODO multiplayer screen
			break;
		case SINGLEPLAYER:
			sb = allScreens.singleplayer;
			break;
		case VIDEOS:
			sb = allScreens.videos;
			break;
		default:
		}
		if (asPrimary) {
			hideMovie();
			if (secondary != null) {
				secondary.onLeave();
				secondary = null;
				repaintInner();
			}
			if (primary == null || primary.screen() != screen) {
				if (primary != null) {
					primary.onLeave();
				}
				primary = sb;
				if (primary != null) {
					primary.resize();
					primary.onEnter(mode);
					repaintInner();
					doMoveMouseAgain();
				}
			}
		} else {
			if (secondary == null || secondary.screen() != screen) {
				if (secondary != null) {
					secondary.onLeave();
				}
				secondary = sb;
				if (secondary != null) {
					secondary.resize();
					secondary.onEnter(mode);
					repaintInner();
					doMoveMouseAgain();
				}
			}
		}
		return sb;
	}
	@Override
	public ScreenBase displayPrimary(Screens screen) {
		return display(screen, true, null);
	}
	@Override
	public ScreenBase displaySecondary(Screens screen) {
		return display(screen, false, null);
	}
	/**
	 * Display the movie window.
	 */
	public void displayMovie() {
		if (!movieVisible) {
			movieVisible = true;
			movie.onEnter(null);
			doMoveMouseAgain();
			repaintInner();
		}
	}
	/**
	 * Hide the movie windows.
	 */
	public void hideMovie() {
		if (movieVisible) {
			movieVisible = false;
			movie.onLeave();
			doMoveMouseAgain();
			repaintInner();
		}
	}
	/**
	 * Display the status bar.
	 */
	@Override 
	public void displayStatusbar() {
		if (!statusbarVisible) {
			statusbarVisible = true;
			statusbar.resize();
			statusbar.onEnter(null);
			doMoveMouseAgain();
			repaintInner();
		}
	}
	/* (non-Javadoc)
	 * @see hu.openig.v1.GameControls#hideSecondary()
	 */
	@Override
	public void hideSecondary() {
		if (secondary != null) {
			secondary.onLeave();
			secondary = null;
			doMoveMouseAgain();
			repaintInner();
		}
	}
	/**
	 * Hide the status bar.
	 */
	@Override 
	public void hideStatusbar() {
		if (statusbarVisible) {
			statusbarVisible = false;
			statusbar.onLeave();
			doMoveMouseAgain();
			repaintInner();
		}
	}
	/**
	 * On screen transitions, issue a fake mouse moved events to support the highlighting.
	 */
	public void doMoveMouseAgain() {
		boolean result = false;
		ScreenBase sb = statusbar;
		UIMouse m = UIMouse.createCurrent(surface);
		if (statusbarVisible) {
			result |= sb.mouse(m);
		}
		ScreenBase pri = primary;
		ScreenBase sec = secondary;
		if (sec != null) {
			result |= sec.mouse(m);
		} else
		if (pri != null) {
			result |= pri.mouse(m);
		}
		if (result) {
			repaintInner();
		}
	}
	/**
	 * The common key manager.
	 * @author akarnokd, 2009.12.24.
	 */
	class KeyEvents extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			boolean rep = false;
			ScreenBase pri = primary;
			ScreenBase sec = secondary;
			if (movieVisible) {
				rep |= movie.keyboard(e);
			} else
			if (sec != null) {
				rep |= sec.keyboard(e);
			} else
			if (pri != null) {
				rep |= pri.keyboard(e);
			}
			if (!e.isConsumed()) {
				handleScreenSwitch(e);
			}
			if (rep) {
				repaintInner();
			}
		}
		/**
		 * Handle the screen switch if the appropriate key is pressed.
		 * @param e the key event
		 */
		void handleScreenSwitch(KeyEvent e) {
			if (e.isAltDown()) {
				if (e.getKeyCode() == KeyEvent.VK_F4) {
					exit();
				}
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					switchFullscreen();
				}
				e.consume();
			}
			if (!commons.worldLoading && commons.world() != null && !movieVisible) {
				if (e.getKeyChar() == '+') {
					commons.world().player.moveNextPlanet();
					repaintInner();
					e.consume();
				} else
				if (e.getKeyChar() == '-') {
					commons.world().player.movePrevPlanet();
					repaintInner();
					e.consume();
				}
				switch (e.getKeyCode()) {
				case KeyEvent.VK_F1:
					displayPrimary(Screens.BRIDGE);
					e.consume();
					break;
				case KeyEvent.VK_F2:
					displayPrimary(Screens.STARMAP);
					e.consume();
					break;
				case KeyEvent.VK_F3:
					displayPrimary(Screens.COLONY);
					e.consume();
					break;
				case KeyEvent.VK_F4:
					if (secondary != null) {
						if (secondary.screen() == Screens.EQUIPMENT) {
							hideSecondary();
						} else {
							displaySecondary(Screens.EQUIPMENT);
						}
					} else {
						switch (primary.screen()) {
						case COLONY:
							commons.world().player.selectionMode = SelectionMode.PLANET;
							displaySecondary(Screens.EQUIPMENT);
							break;
						default:
							displaySecondary(Screens.EQUIPMENT);
						}
					}
					e.consume();
					break;
				case KeyEvent.VK_F5:
					if (secondary != null) {
						if (secondary.screen() == Screens.PRODUCTION) {
							hideSecondary();
						} else {
							displaySecondary(Screens.PRODUCTION);
						}
					} else {
						displaySecondary(Screens.PRODUCTION);
					}
					e.consume();
					break;
				case KeyEvent.VK_F6:
					if (secondary != null) {
						if (secondary.screen() == Screens.RESEARCH) {
							hideSecondary();
						} else {
							displaySecondary(Screens.RESEARCH);
						}
					} else {
						displaySecondary(Screens.RESEARCH);
					}
					e.consume();
					break;
				case KeyEvent.VK_F7:
					if (secondary != null) {
						switch (secondary.screen()) {
						case EQUIPMENT:
							displaySecondary(Screens.INFORMATION_INVENTIONS);
							break;
						case DIPLOMACY:
							displaySecondary(Screens.INFORMATION_ALIENS);
							break;
						case INFORMATION_ALIENS:
						case INFORMATION_BUILDINGS:
						case INFORMATION_COLONY:
						case INFORMATION_FINANCIAL:
						case INFORMATION_FLEETS:
						case INFORMATION_INVENTIONS:
						case INFORMATION_MILITARY:
						case INFORMATION_PLANETS:
							hideSecondary();
							break;
						case RESEARCH:
						case PRODUCTION:
							displaySecondary(Screens.INFORMATION_INVENTIONS);
							break;
						default:
							displaySecondary(Screens.INFORMATION_PLANETS);
						}
					} else {
						switch (primary.screen()) {
						case STARMAP:
							displaySecondary(Screens.INFORMATION_PLANETS);
							break;
						case COLONY:
							displaySecondary(Screens.INFORMATION_COLONY);
							break;
						default:
							displaySecondary(Screens.INFORMATION_PLANETS);
						} 
					}
					e.consume();
					break;
				case KeyEvent.VK_F8:
					if (secondary != null) {
						if (secondary.screen() == Screens.DATABASE) {
							hideSecondary();
						} else {
							displaySecondary(Screens.DATABASE);
						}
					} else {
						displaySecondary(Screens.DATABASE);
					}
					e.consume();
					break;
				case KeyEvent.VK_F9:
					if (commons.world().level > 1) {
						if (secondary != null) {
							if (secondary.screen() == Screens.BAR) {
								hideSecondary();
							} else {
								displaySecondary(Screens.BAR);
							}
						} else {
							displaySecondary(Screens.BAR);
						}
					}
					e.consume();
					break;
				case KeyEvent.VK_F10:
					if (commons.world().level > 3) {
						if (secondary != null) {
							if (secondary.screen() == Screens.DIPLOMACY) {
								hideSecondary();
							} else {
								displaySecondary(Screens.DIPLOMACY);
							}
						} else {
							displaySecondary(Screens.DIPLOMACY);
						}
					}
					e.consume();
					break;
				case KeyEvent.VK_F11:
					if (secondary != null) {
						if (secondary.screen() == Screens.STATISTICS) {
							hideSecondary();
						} else {
							displaySecondary(Screens.STATISTICS);
						}
					} else {
						displaySecondary(Screens.STATISTICS);
					}
					e.consume();
					break;
				case KeyEvent.VK_F12:
					if (secondary != null) {
						if (secondary.screen() == Screens.ACHIEVEMENTS) {
							hideSecondary();
						} else {
							displaySecondary(Screens.ACHIEVEMENTS);
						}
					} else {
						displaySecondary(Screens.ACHIEVEMENTS);
					}
					e.consume();
					break;
				case KeyEvent.VK_1:
					if (e.isControlDown()) {
						commons.world().level = 1;
						if (primary != null) {
							primary.onLeave();
						}
						primary = null;
						displayPrimary(Screens.BRIDGE);
					} else {
						commons.speed(1000);
						repaintInner();
					}
					e.consume();
					break;
				case KeyEvent.VK_2:
					if (e.isControlDown()) {
						commons.world().level = 2;
						if (primary != null) {
							primary.onLeave();
						}
						primary = null;
						displayPrimary(Screens.BRIDGE);
					} else {
						commons.speed(500);
						repaintInner();
					}
					e.consume();
					break;
				case KeyEvent.VK_3:
					if (e.isControlDown()) {
						commons.world().level = 3;
						if (primary != null) {
							primary.onLeave();
						}
						primary = null;
						displayPrimary(Screens.BRIDGE);
					} else {
						commons.speed(250);
						repaintInner();
					}
					e.consume();
					break;
				case KeyEvent.VK_SPACE:
					if (commons.paused()) {
						commons.resume();
					} else {
						commons.pause();
					}
					repaintInner();
					e.consume();
					break;
				case KeyEvent.VK_4:
					if (e.isControlDown()) {
						commons.world().level = 4;
						if (primary != null) {
							primary.onLeave();
						}
						primary = null;
						displayPrimary(Screens.BRIDGE);
						e.consume();
					}
					break;
				case KeyEvent.VK_5:
					if (e.isControlDown()) {
						commons.world().level = 5;
						if (primary != null) {
							primary.onLeave();
						}
						primary = null;
						displayPrimary(Screens.BRIDGE);
						e.consume();
					}
					break;
				case KeyEvent.VK_O:
					if (e.isControlDown()) {
						Planet p = commons.world().player.currentPlanet; 
						if (p != null) {
							if (p.owner == null) {
								p.population = 5000; // initial colony
							}
							p.owner = commons.world().player;
							if (p.race == null || p.race.isEmpty()) {
								p.race = p.owner.race;
								p.owner.statistics.planetsColonized++;
							} else {
								p.owner.statistics.planetsConquered++;
							}
							for (Building b : p.surface.buildings) {
								if (b.type.research != null) {
									commons.world().player.setAvailable(b.type.research);
								}
							}
							p.owner.planets.put(p, PlanetKnowledge.BUILDING);
							repaintInner();
						}
						e.consume();
					} else {
						LoadSaveScreen scr = (LoadSaveScreen)display(Screens.LOAD_SAVE, false, secondary != null ? secondary.screen() : null);
						scr.maySave = true;
					}
					break;
				case KeyEvent.VK_I:
					if (e.isControlDown()) {
						if (commons.world().player.currentResearch() != null) {
							boolean researched = commons.world().player.setAvailable(commons.world().player.currentResearch());
							Integer cnt = commons.world().player.inventory.get(commons.world().player.currentResearch());
							cnt = cnt != null ? cnt + 1 : 1;
							commons.world().player.inventory.put(commons.world().player.currentResearch(), cnt);
							if (secondary.screen() == Screens.RESEARCH
									|| secondary.screen() == Screens.PRODUCTION) {
								if (researched) {
									((ResearchProductionScreen)secondary).playAnim(commons.world().player.currentResearch());
								}
							}
							repaintInner();
						}
						e.consume();
					}
					break;
				case KeyEvent.VK_S:
					if (e.isControlDown()) {
						saveWorld();
						e.consume();
					}
					break;
				case KeyEvent.VK_L:
					if (e.isControlDown()) {
						loadWorld(null);
						e.consume();
					}
					break;
				case KeyEvent.VK_ESCAPE:
					LoadSaveScreen scr = (LoadSaveScreen)display(Screens.LOAD_SAVE, false, secondary != null ? secondary.screen() : null);
					scr.maySave = true;
					e.consume();
					break;
				default:
				}
			}
		}
	}
	/**
	 * Toggle between full screen mode.
	 */
	void switchFullscreen() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		for (final GraphicsDevice gs : ge.getScreenDevices()) {
			if (gs.getDefaultConfiguration().getBounds().intersects(getBounds())) {
				GameWindow gw = new GameWindow(GameWindow.this, gs.getFullScreenWindow() == null);
				if (gs.getFullScreenWindow() == null) {
					gw.saveX = getX();
					gw.saveY = getY();
					gw.saveWidth = getWidth();
					gw.saveHeight = getHeight();
		    		dispose();
					gs.setFullScreenWindow(gw);
					setSize(gs.getDefaultConfiguration().getBounds().getSize());
				} else {
		    		dispose();
					gs.setFullScreenWindow(null);
					gw.setBounds(saveX, saveY, saveWidth, saveHeight);
					gw.setVisible(true);
				}
				gw.doMoveMouseAgain();
				break;
			}
		}
	}
	/** Create a second window with the same content. */
	void newWindow() {
		GameWindow gw = new GameWindow(GameWindow.this, false);
		gw.setVisible(true);
		gw.doMoveMouseAgain();
	}
	/**
	 * The common mouse action manager.
	 * @author akarnokd, 2009.12.23.
	 */
	class MouseActions extends MouseAdapter {
		/** 
		 * Transform and invoke the mouse action on the current top screen. 
		 * @param e the mouse event
		 */
		void invoke(MouseEvent e) {
			ScreenBase pri = primary;
			ScreenBase sec = secondary;
			boolean rep = false;
			if (movieVisible) {
				rep = movie.mouse(UIMouse.from(e));
				repaintInner();
				return;
			} else
			if (statusbarVisible) {
				if (statusbar.mouse(UIMouse.from(e))) {
					repaintInner();
					return;
				}
			}
			if (sec != null) {
				rep = sec.mouse(UIMouse.from(e));
			} else
			if (pri != null) {
				rep = pri.mouse(UIMouse.from(e));
			}
			if (rep) {
				repaintInner();
			}
		}
		@Override
		public void mousePressed(MouseEvent e) {
			invoke(e);
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			invoke(e);
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			invoke(e);
		}
		@Override
		public void mouseMoved(MouseEvent e) {
			invoke(e);
		}
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			invoke(e);
		}
		@Override
		public void mouseClicked(MouseEvent e) {
			invoke(e);
		}
		@Override
		public void mouseEntered(MouseEvent e) {
			invoke(e);
		}
		@Override
		public void mouseExited(MouseEvent e) {
			invoke(e);
		}
	}
	@Override
	public void playVideos(final Act onComplete, String... videos) {
		for (String s : videos) {
			movie.mediaQueue.add(s);
		}
		movie.playbackFinished = new Act() {
			@Override
			public void act() {
				hideMovie();
				if (onComplete != null) {
					onComplete.act();
				}
			}
		};
		displayMovie();
	}
	/* (non-Javadoc)
	 * @see hu.openig.v1.screens.GameControls#playVideos(java.lang.String[])
	 */
	@Override
	public void playVideos(String... videos) {
		playVideos(null, videos);
	}
	@Override
	public int getInnerHeight() {
		return surface.getHeight();
	}
	@Override
	public int getInnerWidth() {
		return surface.getWidth();
	}
	@Override
	public void repaintInner() {
		// issue a single repaint, e.g., coalesce the repaints
		if (!repaintRequest) {
			repaintRequest = true;
			surface.repaint();
		}
	}
	@Override
	public void repaintInner(int x, int y, int w, int h) {
		repaintRequestPartial = true;
		surface.repaint(x, y, w, h);
	}
	@Override
	public FontMetrics fontMetrics(int size) {
		return getFontMetrics(getFont().deriveFont((float)size).deriveFont(Font.BOLD));
	}
	/** Save the world. */
	public void saveWorld() {
		final XElement world = commons.world().saveState();
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					File dir = new File("save/default");
					if (dir.exists() || dir.mkdirs()) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
						
						File fout = new File("save/default/save-" + sdf.format(new Date()) + ".xml");
						try {
							PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fout), "UTF-8"));
							try {
								out.println("<?xml version='1.0' encoding='UTF-8'?>");
								out.print(world);
							} finally {
								out.close();
							}
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					} else {
						System.err.println("Could not create save/default.");
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}, "Save");
		t.start();
	}
	@Override
	public void save() {
		saveWorld();
	}
	@Override
	public void load(String name) {
		loadWorld(name);
	}
	/** 
	 * Load a the specified world save. 
	 * @param name the full save name
	 */
	public void loadWorld(final String name) {
		final Screens pri = primary != null ? primary.screen() : null;
		final Screens sec = secondary != null ? secondary.screen() : null;
		final boolean status = statusbarVisible;
		
		displayPrimary(Screens.LOADING);
		hideStatusbar();
		
		for (ScreenBase sb : screens) {
			sb.onEndGame();
		}
		
		final String currentGame = commons.world() != null ? commons.world().name : null; 
		commons.worldLoading = true;
		boolean running = false;
		if (commons.world() != null) {
			running = !commons.paused();
			commons.stop();
		}
		final boolean frunning = running;
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String lname = name;
					if (lname == null) {
						File dir = new File("save/default");
						if (dir.exists()) {
							File[] files = dir.listFiles(new FilenameFilter() {
								@Override
								public boolean accept(File dir, String name) {
									return name.startsWith("save-") && name.endsWith(".xml");
								}
							});
							if (files != null && files.length > 0) {
								Arrays.sort(files, new Comparator<File>() {
									@Override
									public int compare(File o1, File o2) {
										return o2.getName().compareTo(o1.getName());
									}
								});
								lname = files[0].getAbsolutePath();
							} else {
								System.err.println("No saves!");
							}
						} else {
							System.err.println("save/default not found");
						}
					}

					
					XElement xworld = XElement.parseXML(lname);
					
					String game = xworld.get("game");
					World world = commons.world(); 
					if (!game.equals(currentGame)) {
						commons.world(null);
						// load world model
						world = new World();
						world.definition = GameDefinition.parse(commons.rl, game);
						world.labels = new Labels();
						world.labels.load(commons.rl, world.definition.labels);
						world.load(commons.rl, world.definition.name);
					}
					
					world.loadState(xworld);
					
					final World fworld = world;
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							commons.labels0().replaceWith(fworld.labels);
							commons.world(fworld);
							commons.worldLoading = false;
							
							if (pri == Screens.MAIN || pri == Screens.LOAD_SAVE || pri == Screens.SINGLEPLAYER) {
								displayPrimary(Screens.BRIDGE);
								displayStatusbar();
							} else
							if (pri != null) {
								displayPrimary(pri);
							}
							if (sec != null) {
								displaySecondary(sec);
							}
							if (status) {
								displayStatusbar();
							}
							commons.start();
							if (!frunning) {
								commons.pause();
							}
						}
					});

				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}, "Load");
		t.start();
	}
	@Override
	public Screens primary() {
		return primary != null ? primary.screen() : null;
	}
	@Override
	public Screens secondary() {
		return secondary != null ? secondary.screen() : null;
	}
	@Override
	public World world() {
		return commons.world();
	}
	@Override
	public Closeable register(int delay, Act action) {
		return commons.register(delay, action);
	}
	@Override
	public void endGame() {
		hideStatusbar();
		commons.stop();
		commons.world(null);
		for (ScreenBase sb : screens) {
			sb.onEndGame();
		}
	}
}
