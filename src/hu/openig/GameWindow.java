/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig;

import hu.openig.core.Action0;
import hu.openig.core.Func1;
import hu.openig.core.SaveMode;
import hu.openig.mechanics.AI;
import hu.openig.mechanics.AIPirate;
import hu.openig.mechanics.AITest;
import hu.openig.mechanics.AITrader;
import hu.openig.mechanics.AIUser;
import hu.openig.mechanics.BattleSimulator;
import hu.openig.model.AIManager;
import hu.openig.model.BattleInfo;
import hu.openig.model.Building;
import hu.openig.model.Configuration;
import hu.openig.model.Cursors;
import hu.openig.model.CustomGameDefinition;
import hu.openig.model.Fleet;
import hu.openig.model.GameDefinition;
import hu.openig.model.InventoryItem;
import hu.openig.model.InventorySlot;
import hu.openig.model.Labels;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.Player;
import hu.openig.model.Profile;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;
import hu.openig.model.Screens;
import hu.openig.model.SkirmishDefinition;
import hu.openig.model.SoundTarget;
import hu.openig.model.SoundType;
import hu.openig.model.World;
import hu.openig.screen.CommonResources;
import hu.openig.screen.GameControls;
import hu.openig.screen.GameKeyManager;
import hu.openig.screen.ScreenBase;
import hu.openig.screen.api.SettingsPage;
import hu.openig.screen.items.GameOverScreen;
import hu.openig.screen.items.LoadSaveScreen;
import hu.openig.screen.items.MovieScreen;
import hu.openig.screen.items.PlanetScreen;
import hu.openig.screen.items.SpacewarScreen;
import hu.openig.screen.items.StarmapScreen;
import hu.openig.screen.items.StarmapScreen.ShowNamesMode;
import hu.openig.screen.items.StatusbarScreen;
import hu.openig.scripting.SkirmishScripting;
import hu.openig.ui.UIColorLabel;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Button;
import hu.openig.utils.Exceptions;
import hu.openig.utils.Parallels;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.xml.stream.XMLStreamException;

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
		/** The last width. */
		int lastW = -1;
		/** The last height. */
		int lastH = -1;
		/** The last scaling. */
		int lastScale = -1;
		/** Save transform before draw. */
		AffineTransform predraw;
		/** When did the last paint happen. */
		long lastPaint = System.nanoTime();
		/** The FPS value to draw to the overlay. */
		//		Double fps;
		/**
		 * Set opacity. 
		 */
		public ScreenRenderer() {
			setOpaque(true);
			setFocusTraversalKeysEnabled(false);
		}
		/**
		 * Push state.
		 * @param g2 graphics context
		 */
		void push(Graphics2D g2) {
			if (predraw == null) {
				predraw = g2.getTransform();
			} else {
				Exceptions.add(new IllegalStateException("Predraw already set."));
			}
		}
		/**
		 * Pop state.
		 * @param g2 graphics context
		 */
		void pop(Graphics2D g2) {
			if (predraw != null) {
				g2.setTransform(predraw);
				predraw = null;
			} else {
				Exceptions.add(new IllegalStateException("Predraw already null."));
			}
		}
		@Override
		public void paint(Graphics g) {
			long prevPaint = lastPaint;
			lastPaint = System.nanoTime();
			boolean r0 = repaintRequest;
			boolean r1 = repaintRequestPartial;
			repaintRequest = false;
			repaintRequestPartial = false;
			int uis = config.uiScale;

			if (getWidth() != lastW || getHeight() != lastH || uis != lastScale) {
				r0 = true;
				lastW = getWidth();
				lastH = getHeight();
				lastScale = config.uiScale;
				try {
					if (primary != null) {
						primary.resize();
					}
					if (secondary != null) {
						secondary.resize();
					}
					if (options != null) {
						options.resize();
					}
					if (movie != null) {
						movie.resize();
					}
					if (statusbarVisible) {
						statusbar.resize();
					}
				} catch (Throwable t) {
					Exceptions.add(t);
				}
			}

			Graphics2D g2 = (Graphics2D)g;
			AffineTransform at0 = g2.getTransform();
			try {
				if (uis != 100) {
					g2.scale(uis / 100d, uis / 100d);
				}
				if (movieVisible() && movie.opaque()) {
					movie.draw(g2);
				} else {
					if (r1 && !r0 && !optionsVisible) {
						if (secondary != null) {
							push(g2);
							try {
								secondary.draw(g2);
							} finally {
								pop(g2);
							}
						}
						if (statusbarVisible) {
							push(g2);
							try {
								statusbar.draw(g2);
							} finally {
								pop(g2);
							}
						}
					} else {
						if (primary != null) {
							push(g2);
							try {
								primary.draw(g2);
							} finally {
								pop(g2);
							}
						}
						if (secondary != null) {
							push(g2);
							try {
								secondary.draw(g2);
							} finally {
								pop(g2);
							}
						}
						if (optionsVisible) {
							push(g2);
							try {
								options.draw(g2);
							} finally {
								pop(g2);
							}
						}
						if (statusbarVisible) {
							push(g2);
							try {
								statusbar.draw(g2);
							} finally {
								pop(g2);
							}
						}
						if (movieVisible()) {
							push(g2);
							try {
								movie.draw(g2);
							} finally {
								pop(g2);
							}
						}				
					}
				}
				renderTooltip(g2);
			} catch (Throwable t) {
				Exceptions.add(t);
			} finally {
				g2.setTransform(at0);
			}
			if (config.showFPS) {
				long t2 = System.nanoTime();
				String str = String.format("BF: %5.3f ms, IF: %5.3f ms%n", (lastPaint - prevPaint) / 1_000_000d, (t2 - lastPaint) / 1_000_000d);
				g2.setColor(Color.WHITE);
				g2.drawString(str, 12, getHeight() - 5);
			}
		}
	}
	/** The record of screens. */
	public final GameScreens allScreens = new GameScreens();
	/** A pending repaint request. */
	boolean repaintRequest;
	/** A partial repaint request. */
	boolean repaintRequestPartial;
	/** Enable fixed framerate mode. */
	boolean fixedFramerate;
	/** The fixed framerate timer. */
	Timer fixedFramerateTimer;
	/** The frame rate. */
	static final int FRAMERATE = 30;
	/** The primary screen. */
	ScreenBase primary;
	/** The secondary screen drawn over the first. */
	ScreenBase secondary;
	/** The status bar to display over the primary and secondary screens. */
	StatusbarScreen statusbar;
	/** The top overlay for playing 'full screen' movies. */
	MovieScreen movie;
	/** The options screen. */
	LoadSaveScreen options;
	/** Is the status bar visible? */
	boolean statusbarVisible;
	/** Is the movie visible. */
	private boolean bMovieVisible;
	/** Options visible. */
	boolean optionsVisible;
	/** The configuration object. */
	Configuration config;
	/** The common resources. */
	CommonResources commons;
	/** The surface used to render the screens. */
	ScreenRenderer surface;
	/** The current overlay component. */
	JComponent overlayComponent;
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
	/** The tooltip helper rectangle. */
	public Rectangle tooltipHelper;
	/** The simple tooltip text. */
	public String tooltipText;
	/** The tooltip show timer. */
	public Timer tooltipShowTimer;
	/** The component providing the tooltip. */
	public UIComponent tooltipComponent;
	/** Is the tooltip visible? */
	boolean tooltipVisible;
	/** Show or hide the helper rectangle. */
	boolean helperRectangleVisible;
	/** The main game key manager. */
	private GameKeyManager gameKeyManager;
	/** 
	 * Constructor. 
	 * @param config the configuration object.
	 * @param commons the common resources
	 */
	public GameWindow(Configuration config, CommonResources commons) {
		super("Open Imperium Galactica " + Configuration.VERSION + " [pid: " + ManagementFactory.getRuntimeMXBean().getName() + "][Java: " + System.getProperty("java.version") + "]");
		setFocusTraversalKeysEnabled(false);
		URL icon = GameWindow.class.getResource("/hu/openig/gfx/open-ig-logo.png");
		if (icon != null) {
			try {
				setIconImage(ImageIO.read(icon));
			} catch (IOException e) {
				Exceptions.add(e);
			}
		}

		this.commons = commons;
		commons.control(this);
		this.config = config;
		this.surface = new ScreenRenderer();

		if (config.fullScreen) {
			this.setUndecorated(true);
		}
		commons.setCursor(Cursors.POINTER);

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

		// Event handling

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowGainedFocus(WindowEvent e) {
				GameWindow.this.requestFocusInWindow();
			}
		});

		MouseActions ma = new MouseActions();
		surface.addMouseListener(ma);
		surface.addMouseMotionListener(ma);
		surface.addMouseWheelListener(ma);

		gameKeyManager = new GameKeyManager(commons);

		addKeyListener(gameKeyManager);

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
							// Mac workaround for not having the keyboard focus in fullscreen.
							GameWindow.this.setVisible(false);
							GameWindow.this.setVisible(true);
							setSize(gs.getDefaultConfiguration().getBounds().getSize());
							break;
						}
					}
				}
			});
		}
		init();
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
		setFocusTraversalKeysEnabled(false);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowGainedFocus(WindowEvent e) {
				GameWindow.this.requestFocusInWindow();
			}
		});

		this.surface = new ScreenRenderer();

		this.commons = that.commons;
		this.commons.control(this);
		this.config = that.config;
		this.screens = that.screens;
		setIconImage(that.getIconImage());
		this.primary = that.primary;
		this.secondary = that.secondary;
		this.options = that.options;
		this.movie = that.movie;
		this.movieVisible(that.movieVisible());
		this.statusbar = that.statusbar;
		this.statusbarVisible = that.statusbarVisible;
		this.optionsVisible = that.optionsVisible;

		assign(this.allScreens, that.allScreens);

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

		MouseActions ma = new MouseActions();
		surface.addMouseListener(ma);
		surface.addMouseMotionListener(ma);
		surface.addMouseWheelListener(ma);

		that.removeKeyListener(that.gameKeyManager);
		
		gameKeyManager = new GameKeyManager(commons);
		addKeyListener(gameKeyManager);

		// fix movie
		if (movie.playbackFinished instanceof MovieFinishAction) {
			((MovieFinishAction)movie.playbackFinished).gw = this;
		}
		init();
	}
	/** Initialize the frame-common components. */
	void init() {
		fixedFramerateTimer = new Timer(1000 / FRAMERATE, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (fixedFramerate
						&& (repaintRequest || repaintRequestPartial)) {
					repaintRequest = true;
					surface.repaint();
				}
			}
		});
		fixedFramerateTimer.start();
		tooltipShowTimer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tooltipVisible = true;
				repaintInner();
				tooltipShowTimer.stop();
			}
		});
	}
	/** Finish the frame-common objects. */
	void done() {
		fixedFramerateTimer.stop();
		tooltipShowTimer.stop();
	}
	/**
	 * Assign public fields of the same object.
	 * @param <T> the object type
	 * @param o1 the first object
	 * @param o2 the second object
	 */
	<T> void assign(T o1, T o2) {
		try {
			for (Field f : o1.getClass().getFields()) {
				Field f2 = o2.getClass().getField(f.getName());
				f.set(o1, f2.get(o2));
			}
		} catch (IllegalAccessException | NoSuchFieldException ex) {
			Exceptions.add(ex);
		}
	}
	@Override
	public void exit() {
		uninitScreens();
		commons.stop();
		commons.sounds.close();
		if (commons.world() != null) {
			commons.world().scripting.done();
		}
		commons.done();

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

		done();
		dispose();
		config.crashLog = null;
		U.close(config.watcherWindow);
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
		screens = new ArrayList<>();
		try {
			for (Field f : allScreens.getClass().getFields()) {
				if (ScreenBase.class.isAssignableFrom(f.getType())) {
					ScreenBase sb = ScreenBase.class.cast(f.getType().getConstructor().newInstance());
					f.set(allScreens, sb);
					screens.add(sb);
				}
			}
			for (ScreenBase sb : screens) {
				sb.initialize(commons);
			}
			movie = allScreens.movie;
			statusbar = allScreens.statusbar;
			options = allScreens.loadSave;

			commons.profile.load();

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					displayPrimary(Screens.MAIN);
				}
			});
		} catch (Throwable ex) {
			Exceptions.add(ex);
			damagedInstall();
		}
	}
	/** Unitialize the screens. */
	protected void uninitScreens() {
		for (ScreenBase sb : screens) {
			if (primary == sb || secondary == sb || options == sb) {
				sb.onLeave();
			}
			sb.onFinish();
		}
		primary = null;
		secondary = null;
		options = null;
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
		SoundType sound = null;
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
			sound = (SoundType.BAR);
			break;
		case BRIDGE:
			sb = allScreens.bridge;
			sound = (SoundType.BRIDGE);
			break;
		case COLONY:
			sb = allScreens.colony;
			sound = (SoundType.COLONY);
			break;
		case DIPLOMACY:
			sb = allScreens.diplomacy;
			sound = (SoundType.DIPLOMACY);
			break;
		case EQUIPMENT:
			sb = allScreens.equipment;
			sound = (SoundType.EQUIPMENT);
			break;
		case INFORMATION_COLONY:
			sb = allScreens.info;
			mode = screen;
			sound = (SoundType.INFORMATION_COLONY);
			break;
		case INFORMATION_ALIENS:
			sb = allScreens.info;
			mode = screen;
			sound = (SoundType.INFORMATION_ALIENS);
			break;
		case INFORMATION_BUILDINGS:
			sb = allScreens.info;
			mode = screen;
			sound = (SoundType.INFORMATION_BUILDINGS);
			break;
		case INFORMATION_FINANCIAL:
			sb = allScreens.info;
			mode = screen;
			sound = (SoundType.INFORMATION_FINANCIAL);
			break;
		case INFORMATION_FLEETS:
			sb = allScreens.info;
			mode = screen;
			sound = (SoundType.INFORMATION_FLEETS);
			break;
		case INFORMATION_INVENTIONS:
			sb = allScreens.info;
			mode = screen;
			sound = (SoundType.INFORMATION_INVENTIONS);
			break;
		case INFORMATION_MILITARY:
			sb = allScreens.info;
			mode = screen;
			sound = (SoundType.INFORMATION_MILITARY);
			break;
		case INFORMATION_PLANETS:
			sb = allScreens.info;
			mode = screen;
			sound = (SoundType.INFORMATION_PLANETS);
			break;
		case PRODUCTION:
			sb = allScreens.researchProduction;
			mode = Screens.PRODUCTION;
			sound = (SoundType.PRODUCTION);
			break;
		case RESEARCH:
			sb = allScreens.researchProduction;
			mode = Screens.RESEARCH;
			sound = (SoundType.RESEARCH);
			break;
		case SPACEWAR:
			sb = allScreens.spacewar;
			break;
		case STARMAP:
			sb = allScreens.starmap;
			sound = (SoundType.STARMAP);
			break;
		case SHIPWALK:
			sb = allScreens.shipwalk;
			break;
		case DATABASE:
			sb = allScreens.database;
			sound = (SoundType.DATABASE);
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
		case TEST:
			sb = allScreens.test;
			break;
		case GAME_OVER:
			sb = allScreens.gameOver;
			break;
		case CREDITS:
			sb = allScreens.credits;
			break;
		case BATTLE_FINISH:
			sb = allScreens.battleFinish;
			break;
		case SKIRMISH:
			sb = allScreens.skirmish;
			break;
		case TRAITS:
			sb = allScreens.traits;
			break;
		case PROFILE:
			sb = allScreens.profile;
			break;
		case SPYING:
			sb = allScreens.spying;
			break;
		case TRADE:
			sb = allScreens.trade;
			break;
		default:
		}
		if (asPrimary) {
			//			hideMovie();
			boolean playSec = false;
			boolean changes = false;
			if (secondary != null) {
				secondary.onLeave();
				secondary = null;
				playSec = true;
				changes = true;
			}
			if (primary == null || primary.screen() != screen) {
				if (primary != null) {
					primary.onLeave();
				}
				if (sound != null && !commons.battleMode) {
					commons.playSound(SoundTarget.SCREEN, sound, null);
				}
				primary = sb;
				if (primary != null) {
					primary.resize();
					primary.onEnter(mode);
				}
				changes = true;
			} else
				if (playSec) {
					if (sound != null) {
						commons.playSound(SoundTarget.SCREEN, sound, null);
					}
				}
			if (changes) {
				moveMouse();
				repaintInner();
			}
		} else {
			if (primary != null) {
				mouseLeave(primary);
			}
			if (secondary == null || secondary.screen() != screen) {
				if (secondary != null) {
					secondary.onLeave();
				}
				if (sound != null) {
					commons.playSound(SoundTarget.SCREEN, sound, null);
				}
				secondary = sb;
				if (secondary != null) {
					secondary.resize();
					secondary.onEnter(mode);
					repaintInner();
					moveMouse();
				}
			}
		}

		commons.setCursor(Cursors.POINTER);
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
		if (!movieVisible()) {
			commons.setCursor(Cursors.POINTER);
			movieVisible(true);
			movie.onEnter(null);
			moveMouse();
			repaintInner();
		}
	}
	/**
	 * Hide the movie windows.
	 */
	public void hideMovie() {
		if (movieVisible()) {
			movieVisible(false);
			movie.onLeave();
			moveMouse();
			repaintInner();
		}
	}
	/**
	 * Display the movie window.
	 */
	@Override
	public void displayOptions() {
		if (!movieVisible()) {
			optionsVisible = true;
			options.resize();
			options.onEnter(null);
			repaintInner();
			moveMouse();
		}
	}
	/**
	 * Hide the movie windows.
	 */
	@Override
	public void hideOptions() {
		if (optionsVisible) {
			optionsVisible = false;
			options.onLeave();
			moveMouse();
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
			moveMouse();
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
			moveMouse();
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
			moveMouse();
			repaintInner();
		}
	}
	/** 
	 * Scale the mouse event coordinates according to the UI scale.
	 * @param m the mouse event 
	 */
	void scaleMouse(UIMouse m) {
		if (config.uiScale != 100) {
			m.x = (int)(m.x * 100d / config.uiScale);
			m.y = (int)(m.y * 100d / config.uiScale);
		}
	}
	/**
	 * Force a leave message.
	 * @param c the component
	 */
	public void mouseLeave(UIComponent c) {
		UIMouse m = UIMouse.createCurrent(surface);
		scaleMouse(m);
		m.type = UIMouse.Type.LEAVE;
		c.mouse(m);
	}
	@Override
	public void moveMouse() {
		boolean result = false;
		ScreenBase sb = statusbar;
		UIMouse m = UIMouse.createCurrent(surface);
		scaleMouse(m);

		handleTooltip(m);

		if (statusbarVisible) {
			result |= sb.mouse(m);
		}
		ScreenBase pri = primary;
		ScreenBase sec = secondary;
		ScreenBase opt = options;
		if (optionsVisible && opt != null) {
			result |= opt.mouse(m);
		} else
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
	 * Test scenario:
	 * Kill all player planets except Achilles.
	 * Achilles without buildings.
	 * Make the colony ship available.
	 */
	void fullConquestTest() {
		World w = world();
		Player p = w.player;
		// die all of the player's planets
		for (Planet planet : w.planets.values()) {
			if (planet.owner == p) {
				planet.die();
				if (planet.id.equals("Achilles")) {
					planet.owner = p;
					planet.race = p.race;
					planet.population(5000);
					p.planets.put(planet, PlanetKnowledge.BUILDING);
				} else {
					p.planets.remove(planet);
				}
			}
		}
		// remove all non-default technologies
		for (ResearchType rt : U.newArrayList(p.available())) {
			if (rt.level > 0 && rt.race.contains(p.race)) {
				p.removeAvailable(rt);
			}
		}
		// enable colony ship
		for (ResearchType rt : w.researches.values()) {
			if (rt.id.equals("ColonyShip")) {
				p.setAvailable(rt);
			}
			if (rt.id.equals("OrbitalFactory")) {
				p.setAvailable(rt);
			}
		}
		p.money(100000);
		saveWorld(null, SaveMode.QUICK);
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
					done();
					dispose();
					gs.setFullScreenWindow(gw);
					// Mac workaround for not having the keyboard focus in fullscreen.
					gw.setVisible(false);
					gw.setVisible(true);
					setSize(gs.getDefaultConfiguration().getBounds().getSize());
				} else {
					done();
					dispose();
					gs.setFullScreenWindow(null);
					gw.setBounds(saveX, saveY, saveWidth, saveHeight);
					gw.setVisible(true);
				}
				gw.requestFocus();
				gw.moveMouse();
				break;
			}
		}
	}
	/** Create a second window with the same content. */
	void newWindow() {
		GameWindow gw = new GameWindow(GameWindow.this, false);
		gw.setVisible(true);
		gw.moveMouse();
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
			UIMouse me = UIMouse.from(e);

			invertIf(me);
			scaleMouse(me);

			handleTooltip(me);

			if (movieVisible()) {
				rep = movie.mouse(me);
				if (rep) {
					repaintInner();
				}
				return;
			} else
				if (optionsVisible) {
					rep = options.mouse(me);
					if (rep) {
						repaintInner();
					}
					return;
				}
			if (statusbarVisible) {
				if (statusbar.mouse(me)) {
					repaintInner();
					return;
				}
				if (statusbar.overPanel(me)) {
					return;
				}
			}
			if (sec != null) {
				rep = sec.mouse(me);
			} else
				if (pri != null) {
					rep = pri.mouse(me);
				}
			if (rep) {
				repaintInner();
			}
		}
		/**
		 * Invert buttons if the settings tells so.
		 * @param m the mouse event
		 */
		void invertIf(UIMouse m) {
			if (config.swapMouseButtons) {
				boolean left = m.has(Button.LEFT);
				boolean right = m.has(Button.RIGHT);
				if (left != right) {
					m.buttons.remove(Button.LEFT);
					m.buttons.remove(Button.RIGHT);
					if (left) {
						m.buttons.add(Button.RIGHT);
					} else {
						m.buttons.add(Button.LEFT);
					}
				}
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
	/**
	 * The movie completion handler.
	 * @author akarnokd, 2012.06.13.
	 */
	static class MovieFinishAction implements Action0 {
		/** Which is the current main window? */
		public GameWindow gw;
		/** The extra action to invoke on compeltion. */
		public final Action0 onComplete;
		/**
		 * Initializes the action.
		 * @param gw the current game window.
		 * @param onComplete the extra completion handler
		 */
		public MovieFinishAction(GameWindow gw, Action0 onComplete) {
			this.gw = gw;
			this.onComplete = onComplete;
		}
		@Override
		public void invoke() {
			gw.hideMovie();
			if (onComplete != null) {
				onComplete.invoke();
			}
		}
	}
	@Override
	public void playVideos(final Action0 onComplete, String... videos) {
		Profile p = commons.profile;
		for (String s : videos) {
			p.unlockVideo(s);
		}
		p.save();
		
		Collections.addAll(movie.mediaQueue, videos);
		movie.playbackFinished = new MovieFinishAction(this, onComplete);
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
		int h = surface.getHeight();
		if (config.uiScale != 100) {
			h = (int)(h * 100d / config.uiScale);
		}
		return Math.max(h, 480);
	}
	@Override
	public int getInnerWidth() {
		int w = surface.getWidth();
		if (config.uiScale != 100) {
			w = (int)(w * 100d / config.uiScale);
		}
		return Math.max(w, 640);
	}
	@Override
	public void repaintInner() {
		// issue a single repaint, e.g., coalesce the repaints
		if (!repaintRequest) {
			repaintRequest = true;
			if (!fixedFramerate) {
				surface.repaint();
			}
		}
	}
	@Override
	public void repaintInner(int x, int y, int w, int h) {
		repaintRequestPartial = true;
		if (!fixedFramerate) {
			if (config.uiScale != 100) {
				x = (int)(x * config.uiScale * 1d / 100);
				y = (int)(y * config.uiScale * 1d / 100);
				w = (int)(w * config.uiScale * 1d / 100);
				h = (int)(h * config.uiScale * 1d / 100);
			}
			surface.repaint(x, y, w, h);
		}
	}
	@Override
	public FontMetrics fontMetrics(int size) {
		return getFontMetrics(getFont().deriveFont((float)size).deriveFont(Font.BOLD));
	}
	/**
	 * Save the world.
	 * @param name the user entered name, if mode == MANUAL
	 * @param mode the mode
	 * @return the filename of the saved state
	 */
	public synchronized Future<File> saveWorld(final String name, final SaveMode mode) {
		final String pn = commons.profile.name;
		final XElement xworld = commons.world().saveState();
		saveSettings(xworld);
		commons.saving.inc();
		Callable<File> sw = new Callable<File>() {
			@Override
			public File call() throws Exception {
				return save(name, mode, pn, xworld);
			}
		};
		ExecutorService exec = Executors.newSingleThreadExecutor();
		Future<File> f = exec.submit(sw);
		exec.shutdown();
		return f;
	}
	/**
	 * Performs the save operation.
	 * @param name the save name
	 * @param mode the save mode
	 * @param pn the profil name
	 * @param xworld the world
	 * @return the saved file
	 */
	synchronized File save(String name, SaveMode mode, final String pn,
			final XElement xworld) {
		try {
			File dir = new File("save/" + pn);
			if (dir.exists() || dir.mkdirs()) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
				String sdate = sdf.format(new Date());
				File fout = new File(dir, "save-" + sdate + ".xml.gz");
				File foutx = new File(dir, "info-" + sdate + ".xml");
				try {
					if (name == null && mode == SaveMode.LEVEL) {
						name = commons.labels().format("save.level", xworld.get("level"));
					}

					xworld.set("save-name", name);
					xworld.set("save-mode", mode);

					XElement info = World.deriveShortWorldState(xworld);

					try (GZIPOutputStream gout = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(fout), 64 * 1024))) {
						xworld.save(gout);
					}


					info.save(foutx);

					limitSaves(dir, mode);
				} catch (IOException ex) {
					Exceptions.add(ex);
				}
				return fout;
			}
			System.err.println("Could not create save/default.");
		} catch (Throwable t) {
			Exceptions.add(t);
		} finally {
			commons.saving.dec();
		}
		return null;
	}
	/*/** Filter for XML files starting with info- or save-. */
	protected static final FilenameFilter SAVE_FILES = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return (name.startsWith("info-") || name.startsWith("save-")) 
					&& name.endsWith(".xml");
		}
	};
	/**
	 * Limit the number of various saves.
	 * @param dir the saves directory
	 * @param mode the mode
	 */
	void limitSaves(File dir, SaveMode mode) {
		if (mode == SaveMode.MANUAL || mode == SaveMode.LEVEL) {
			return;
		}
		// locate saves
		Set<String> saves = new HashSet<>();
		File[] files = dir.listFiles(SAVE_FILES);
		if (files == null) {
			return;
		}
		// candidate saves
		for (File f : files) {
			String n = f.getName();
			n = n.substring(5, n.length() - 4);
			saves.add(n);
		}

		List<String> savesSorted = new ArrayList<>(saves);
		Collections.sort(savesSorted);
		// latest first
		Collections.reverse(savesSorted);

		int remaining = 5; // the save limit

		for (String s : savesSorted) {
			File info = new File(dir, "info-" + s + ".xml");
			File save = new File(dir, "save-" + s + ".xml.gz");
			if (info.canRead()) {
				// if no associated save, delete the info
				if (!save.canRead()) {
					if (!info.delete()) {
						info.deleteOnExit();
					}
					continue;
				}
				// load world info
				try {
					XElement xml = XElement.parseXML(info.getAbsolutePath());
					String saveMode = xml.get("save-mode", SaveMode.AUTO.toString());

					if (saveMode.equals(mode.toString())) {
						remaining--;
						if (remaining < 0) {
							if (!info.delete()) {
								System.err.println("Warning: Could not delete file " + info);
							}
							if (!save.delete()) {
								System.err.println("Warning: Could not delete file " + save);
							}
						}
					}
				} catch (XMLStreamException ex) {
					Exceptions.add(ex);
				}
			} else 
				if (save.canRead()) {
					try {
						XElement xml = XElement.parseXMLGZ(save);
						String saveMode = xml.get("save-mode", SaveMode.AUTO.toString());
						if (saveMode.equals(mode.toString())) {
							remaining--;
							if (remaining < 0) {
								if (!save.delete()) {
									System.err.println("Warning: Could not delete file " + save);
								}
							}
						}
					} catch (XMLStreamException ex) {
						Exceptions.add(ex);
					}
				}
		}
	}
	@Override
	public void save(String name, SaveMode mode) {
		saveWorld(name, mode);
	}
	@Override
	public void load(String name) {
		loadWorld(name);
	}
	/** 
	 * Cancel loading and return to the main menu.
	 */
	void cancelLoad() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				commons.worldLoading = false;
				displayPrimary(Screens.MAIN);
			}
		});
	}
	@Override
	public void restart() {
		// allow the popup of related exceptions on a newly loaded game.
		Exceptions.clear();

		for (ScreenBase sb : screens) {
			sb.onEndGame();
		}
		commons.battleMode = false;

		displayPrimary(Screens.LOADING);
		hideStatusbar();
		commons.worldLoading = true;
		
		commons.stop();
		
		commons.pool.execute(new Runnable() {
			@Override
			public void run() {
				commons.world().loadSkirmish(commons.rl, new SkirmishScripting());
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						commons.worldLoading = false;
						commons.nongame = false;
						displayPrimary(Screens.BRIDGE);
						displayStatusbar();
						
						commons.start(true);
						world().scripting.onLoaded();
					}
				});
			}
		});
	}
	/** 
	 * Load a the specified world save. 
	 * @param name the full save name
	 */
	public void loadWorld(final String name) {
		// allow the popup of related exceptions on a newly loaded game.
		Exceptions.clear();

		final Screens pri = primary != null ? primary.screen() : null;
		final Screens sec = secondary != null ? secondary.screen() : null;
		final boolean status = statusbarVisible;

		commons.stopMusic();
		for (ScreenBase sb : screens) {
			sb.onEndGame();
		}
		commons.battleMode = false;
		displayPrimary(Screens.LOADING);
		hideStatusbar();


		final String currentGame = commons.world() != null ? commons.world().name : null; 
		final CustomGameDefinition currentSkirmish = commons.world() != null ? commons.world().skirmishDefinition : null;

		commons.worldLoading = true;
		boolean running = false;
		if (commons.world() != null && commons.simulation != null) {
			running = !commons.simulation.paused();
			commons.stop();
		}
		final boolean frunning = running;

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String lname = name;
					if (lname == null) {
						File dir = new File("save/" + commons.profile.name);
						if (dir.exists()) {
							File[] files = dir.listFiles(new FilenameFilter() {
								@Override
								public boolean accept(File dir, String name) {
									return name.startsWith("save-") && name.endsWith(".xml.gz");
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
								System.out.println("Warning! No saves!");
								cancelLoad();
								return;
							}
						} else {
							System.err.println("save/default not found");
							cancelLoad();
							return;
						}
					}


					final XElement xworld = XElement.parseXMLGZ(lname);

					final World fworld = loadWorldData(currentGame, currentSkirmish, xworld);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							reenterWorld(pri, sec, status, frunning, xworld,
									fworld);
						}
					});

				} catch (Throwable t) {
					Exceptions.add(t);
					cancelLoad();
				}
			}
		}, "Load");
		t.start();
	}
	/**
	 * Re-enter a world.
	 * @param pri the primary screen
	 * @param sec the secondary screen
	 * @param status the status
	 * @param frunning was running
	 * @param xworld the world settings
	 * @param fworld the world object
	 */
	void reenterWorld(final Screens pri, final Screens sec,
			final boolean status, final boolean frunning,
			final XElement xworld, final World fworld) {
		commons.labels().replaceWith(fworld.labels);
		commons.world(fworld);
		commons.worldLoading = false;
		commons.nongame = false;

		restoreSettings(xworld);
		commons.start(true);
		if (!frunning) {
			commons.simulation.pause();
		}

		if (pri == Screens.MAIN 
				|| pri == Screens.LOAD_SAVE 
				|| pri == Screens.SINGLEPLAYER
				) {
			displayPrimary(Screens.BRIDGE);
			displayStatusbar();
		} else
			if (pri == Screens.SPACEWAR) {
				displayPrimary(Screens.STARMAP);
				displayStatusbar();
			} else
				if (pri != null) {
					displayPrimary(pri);
				}
		if (sec != null) {
			if ((sec != Screens.PRODUCTION || world().level >= 2)
					&& (sec != Screens.RESEARCH || world().level >= 3)
					&& (sec != Screens.DIPLOMACY || world().level >= 4)
					&& (sec != Screens.BAR || world().level >= 2)
					&& (sec != Screens.TEST || world().testNeeded)) {
				displaySecondary(sec);
			}
		}
		if (status) {
			displayStatusbar();
		}
		fworld.scripting.onLoaded();
	}
	/**
	 * Save the game related settings such as position and configuration values.
	 * @param xworld the world object
	 */
	void saveSettings(XElement xworld) {
		xworld.set("starmap-x", allScreens.starmap.getXOffset());
		xworld.set("starmap-y", allScreens.starmap.getYOffset());
		xworld.set("starmap-z", allScreens.starmap.getZoomIndex());

		xworld.set("spacewar-command", allScreens.spacewar.viewCommandSelected());
		xworld.set("spacewar-damage", allScreens.spacewar.viewDamageSelected());
		xworld.set("spacewar-range", allScreens.spacewar.viewRangeSelected());
		xworld.set("spacewar-grid", allScreens.spacewar.viewGridSelected());

		xworld.set("starmap-divider", allScreens.starmap.planetFleetSplitter);
		xworld.set("starmap-radars", allScreens.starmap.showRadarButton.selected);
		xworld.set("starmap-stars", allScreens.starmap.showStarsButton.selected);
		xworld.set("starmap-grid", allScreens.starmap.showGridButton.selected);
		xworld.set("starmap-fleets", allScreens.starmap.showFleetButton.selected);
		xworld.set("starmap-names", allScreens.starmap.showNames());

		xworld.set("bridge-send", allScreens.bridge.sendSelected);
		xworld.set("bridge-receive", allScreens.bridge.receiveSelected);

		config.saveProperties(xworld);
	}
	/**
	 * Restore the game related settings such as position and configuration values.
	 * @param xworld the world XElement
	 */
	void restoreSettings(XElement xworld) {
		// restore starmap location and zoom
		StarmapScreen sm = allScreens.starmap;
		if (xworld.has("starmap-z")) {
			int z = xworld.getInt("starmap-z");
			sm.setZoomIndex(z);
			sm.newGameStarted = false;
		}
		if (xworld.has("starmap-x")) {
			int x = xworld.getInt("starmap-x");
			sm.setXOffset(x);
			sm.newGameStarted = false;
		}
		if (xworld.has("starmap-y")) {
			int y = xworld.getInt("starmap-y");
			sm.setYOffset(y);
			sm.newGameStarted = false;
		}
		allScreens.spacewar.viewCommandSelected(xworld.getBoolean("spacewar-command", allScreens.spacewar.viewCommandSelected()));
		allScreens.spacewar.viewDamageSelected(xworld.getBoolean("spacewar-damage", allScreens.spacewar.viewDamageSelected()));
		allScreens.spacewar.viewRangeSelected(xworld.getBoolean("spacewar-range", allScreens.spacewar.viewRangeSelected()));
		allScreens.spacewar.viewGridSelected(xworld.getBoolean("spacewar-grid", allScreens.spacewar.viewGridSelected()));

		sm.planetFleetSplitter = xworld.getDouble("starmap-divider", sm.planetFleetSplitter);
		sm.showRadarButton.selected = xworld.getBoolean("starmap-radars", sm.showRadarButton.selected);
		sm.showStarsButton.selected = xworld.getBoolean("starmap-stars", sm.showStarsButton.selected);
		sm.showGridButton.selected = xworld.getBoolean("starmap-grid", sm.showGridButton.selected);
		sm.showFleetButton.selected = xworld.getBoolean("starmap-fleets", sm.showFleetButton.selected);
		sm.showNames(xworld.getEnum("starmap-names", ShowNamesMode.class, sm.showNames()));


		config.loadProperties(xworld);

		commons.music.setVolume(config.musicVolume);
		commons.music.setMute(config.muteMusic);

		allScreens.bridge.sendSelected = xworld.getBoolean("bridge-send", false);
		allScreens.bridge.receiveSelected = xworld.getBoolean("bridge-receive", true);
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
	public Closeable register(int delay, Action0 action) {
		return commons.register(delay, action);
	}
	@Override
	public void endGame() {
		hideStatusbar();

		tooltipComponent = null;
		tooltipText = null;
		tooltipShowTimer.stop();
		tooltipHelper = null;
		tooltipVisible = false;

		commons.stop();
		commons.world(null);
		for (ScreenBase sb : screens) {
			sb.onEndGame();
		}
		commons.startTimer();
	}
	@Override
	public void startBattle() {
		while (true) {
			final BattleInfo bi = world().pendingBattles.poll();
			// exit when no more battle is pending
			if (bi == null) {
				break;
			}
			bi.attacker.stop();
			// check if the source fleet does still exist
			if (!bi.attacker.owner.fleets.containsKey(bi.attacker)) {
				continue;
			}
			// check if the target fleet does still exist
			if (bi.targetFleet != null 
					&& !bi.targetFleet.owner.fleets.containsKey(bi.targetFleet)) {
				continue;
			}
			// check if the target planet already belongs to the attacker
			if (bi.targetPlanet != null 
					&& (bi.targetPlanet.owner == bi.attacker.owner 
					|| bi.targetPlanet.owner == null
					|| bi.attacker.owner.knowledge(bi.targetPlanet, PlanetKnowledge.OWNER) < 0)) {
				continue;
			}
			if (bi.targetPlanet != null) {
				bi.originalTargetPlanetOwner = bi.targetPlanet.owner;
			}
			Player targetPlayer = bi.targetPlanet != null ? bi.targetPlanet.owner : (bi.targetFleet != null ? bi.targetFleet.owner : null);
			if (config.automaticBattle 
					|| (bi.attacker.owner != world().player && targetPlayer != world().player)) {
				new BattleSimulator(world(), bi).autoBattle();
				continue;
			}
			bi.originalSpeed = commons.simulation.speed();
			// do a space battle
			if (bi.targetFleet != null) {
				commons.battleMode = true;
				SpacewarScreen sws = (SpacewarScreen)displayPrimary(Screens.SPACEWAR);
				sws.initiateBattle(bi);
			} else {
				// check orbital defenses and nearby fleet

				boolean spaceBattle = false;
				boolean groundBattle = false;
				// check inventory for defensive items
				for (InventoryItem ii : bi.targetPlanet.inventory.iterable()) {
					if (ii.owner == bi.targetPlanet.owner) {
						if (ii.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS
								|| ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
							spaceBattle = true;
						}
						if (ii.type.category == ResearchSubCategory.WEAPONS_TANKS 
								|| ii.type.category == ResearchSubCategory.WEAPONS_VEHICLES) {
							groundBattle = true;
						}
					}
				}
				// check surface for defensive structures
				for (Building b : bi.targetPlanet.surface.buildings.iterable()) {
					if (b.isOperational() && "Defensive".equals(b.type.kind)) {
						groundBattle = true;
					}
					if (b.isOperational() && "Gun".equals(b.type.kind)) {
						spaceBattle = true;
					}
				}
				for (Fleet f : bi.targetPlanet.owner.fleets.keySet()) {
					if (f.owner == bi.targetPlanet.owner) {
						if (World.dist(f.x, f.y, bi.targetPlanet.x, bi.targetPlanet.y) < 20) {
							spaceBattle = true;
							break;
						}
					}
				}

				if (spaceBattle) {
					commons.battleMode = true;
					commons.stopMusic();
					commons.simulation.pause();
					SpacewarScreen sws = (SpacewarScreen)displayPrimary(Screens.SPACEWAR);
					sws.initiateBattle(bi);
				} else {
					// check if the attacker has ground vehicles at all
					boolean ableToGroundBattle = false;
					for (InventoryItem ii : bi.attacker.inventory.iterable()) {
						if (ii.type.category == ResearchSubCategory.WEAPONS_TANKS
								|| ii.type.category == ResearchSubCategory.WEAPONS_VEHICLES) {
							ableToGroundBattle = true;
							break;
						}
					}
					if (!ableToGroundBattle) {
						if (bi.attacker.owner == world().player) {
							displayError(commons.labels().format("message.no_vehicles_for_assault", bi.targetPlanet.name()));
							commons.playSound(SoundTarget.BUTTON, SoundType.NOT_AVAILABLE, null);
						}
						continue;
					}
					if (groundBattle) {
						commons.battleMode = true;
						commons.stopMusic();
						commons.simulation.pause();
						playVideos(new Action0() {
							@Override
							public void invoke() {
								PlanetScreen ps = (PlanetScreen)displayPrimary(Screens.COLONY);
								ps.initiateBattle(bi);
								commons.playBattleMusic();
							}
						}, "groundwar/felall");

					} else {
						// just take ownership
						bi.targetPlanet.takeover(bi.attacker.owner);
						BattleSimulator.applyPlanetConquered(bi.targetPlanet, BattleSimulator.PLANET_CONQUER_LOSS);
						world().scripting.onAutobattleFinish(bi);
						continue;
					}
				}
			}
			break;
		}
		repaintInner();
	}
	@Override
	public void displayError(String text) {
		allScreens.statusbar.errorText = text;
		int ttl = text.length() * StatusbarScreen.DEFAULT_ERROR_TTL / 10;
		allScreens.statusbar.errorTTL = Math.max(ttl, StatusbarScreen.DEFAULT_ERROR_TTL);
	}

	/**
	 * Adds a new inventory item with the specified type to the target fleet.
	 * @param target the target fleet
	 * @param rt the type
	 * @param count the count
	 */
	void addToFleet(Fleet target, ResearchType rt, int count) {
		InventoryItem ii = new InventoryItem(world().newId(), target.owner, rt);
		ii.count = count;
		ii.init();
		// fill in best equipment
		for (InventorySlot is : ii.slots.values()) {
			if (!is.slot.fixed) {
				is.type = is.slot.items.get(is.slot.items.size() - 1);
				is.count = is.slot.max;
				is.hp = is.hpMax(target.owner);
			}
		}
		ii.shield = Math.max(0, ii.shieldMax());
		target.inventory.add(ii);
	}
	@Override
	public Func1<Player, AIManager> aiFactory() {
		return defaultAIFactory(commons);
	}
	/**
	 * Returns a default AI factory.
	 * @param commons the common resources
	 * @return the factory function
	 */
	public static Func1<Player, AIManager> defaultAIFactory(final CommonResources commons) {
		return new Func1<Player, AIManager>() {
			@Override
			public AIManager invoke(Player value) {
				switch (value.aiMode) {
				case TRADERS:
					return new AITrader();
				case PIRATES:
					return new AIPirate();
				case NONE:
					return new AIUser();
				case TEST:
					return new AITest();
				default:
					return new AI();
				}
			}
		};
	}
	@Override
	@SuppressWarnings("unchecked")
	public <T extends ScreenBase> T getScreen(Screens screen) {
		switch (screen) {
		case ACHIEVEMENTS:
			return (T)allScreens.statisticsAchievements;
		case BAR:
			return (T)allScreens.bar;
		case BATTLE_FINISH:
			return (T)allScreens.battleFinish;
		case BRIDGE:
			return (T)allScreens.bridge;
		case COLONY:
			return (T)allScreens.colony;
		case CREDITS:
			return (T)allScreens.credits;
		case DATABASE:
			return (T)allScreens.database;
		case DIPLOMACY:
			return (T)allScreens.diplomacy;
		case EQUIPMENT:
			return (T)allScreens.equipment;
		case INFORMATION_ALIENS:
			return (T)allScreens.info;
		case INFORMATION_BUILDINGS:
			return (T)allScreens.info;
		case INFORMATION_COLONY:
			return (T)allScreens.info;
		case INFORMATION_FINANCIAL:
			return (T)allScreens.info;
		case INFORMATION_FLEETS:
			return (T)allScreens.info;
		case INFORMATION_INVENTIONS:
			return (T)allScreens.info;
		case INFORMATION_MILITARY:
			return (T)allScreens.info;
		case INFORMATION_PLANETS:
			return (T)allScreens.info;
		case LOADING:
			return (T)allScreens.loading;
		case LOAD_SAVE:
			return (T)allScreens.loadSave;
		case MAIN:
			return (T)allScreens.main;
		case MOVIE:
			return (T)allScreens.movie;
		case MULTIPLAYER:
			return null; // FIXME multiplayer screen
		case PRODUCTION:
			return (T)allScreens.researchProduction;
		case RESEARCH:
			return (T)allScreens.researchProduction;
		case SHIPWALK:
			return (T)allScreens.shipwalk;
		case SINGLEPLAYER:
			return (T)allScreens.singleplayer;
		case SPACEWAR:
			return (T)allScreens.spacewar;
		case STARMAP:
			return (T)allScreens.starmap;
		case STATISTICS:
			return (T)allScreens.statisticsAchievements;
		case STATUSBAR:
			return (T)allScreens.statusbar;
		case TEST:
			return (T)allScreens.test;
		case GAME_OVER:
			return (T)allScreens.gameOver;
		case VIDEOS:
			return (T)allScreens.videos;
		case TRAITS:
			return (T)allScreens.traits;
		case SKIRMISH:
			return (T)allScreens.skirmish;
		case PROFILE:
			return (T)allScreens.profile;
		case SPYING:
			return (T)allScreens.spying;
		case TRADE:
			return (T)allScreens.trade;
		default:
			throw new AssertionError(String.valueOf(screen));
		}
	}
	@Override
	public JComponent renderingComponent() {
		return surface;
	}
	@Override
	public void forceMessage(String messageId, Action0 onSeen) {
		if (world() != null && !commons.worldLoading) {
			displayPrimary(Screens.BRIDGE);
			commons.playSound(SoundTarget.COMPUTER, SoundType.MESSAGE, null);
			allScreens.bridge.forceMessage(messageId, onSeen);
		}
	}
	@Override
	public void loseGame() {
		displaySecondary(Screens.GAME_OVER);
	}
	@Override
	public void winGame() {
		GameOverScreen gos = allScreens.gameOver;
		gos.win = true;
		displaySecondary(Screens.GAME_OVER);		
	}
	@Override
	public void showObjectives(boolean state) {
		boolean wasVisible = allScreens.statusbar.objectives.visible();
		allScreens.statusbar.objectives.visible(state);
		repaintInner();
		if (state && !wasVisible) {
			Parallels.runDelayedInEDT(10000, new Runnable() {
				@Override
				public void run() {
					allScreens.statusbar.objectives.visible(false);
				}
			});
		}
	}
	@Override
	public boolean isFullscreen() {
		return isUndecorated();
	}
	@Override
	public void setFullscreen(boolean value) {
		if (isFullscreen() != value) {
			switchFullscreen();
		}
	}
	@Override
	public void runResize() {
		surface.lastW = -1;
		surface.lastH = -1;
	}
	@Override
	public void windowToScale() {
		if (!isFullscreen() && getExtendedState() == JFrame.NORMAL) {
			int dx = getWidth() - surface.getWidth();
			int dy = getHeight() - surface.getHeight();

			int sw = Math.max(640, (int)(config.uiScale * 6.40 + 1));
			int sh = Math.max(480, (int)(config.uiScale * 4.80 + 1));

			Rectangle mxs = getGraphicsConfiguration().getBounds();

			setSize(Math.min(mxs.width, Math.max(sw + dx, getWidth())), Math.min(mxs.height, Math.max(sh + dy, getHeight())));
			
			commons.text().setFontScaling(config.uiScale / 100d);
		}
	}
	@Override
	public boolean movieVisible() {
		return bMovieVisible;
	}
	/**
	 * Set the movie visibility status.
	 * @param value the new status
	 */
	public void movieVisible(boolean value) {
		this.bMovieVisible = value;
	}
	/**
	 * Handle the tooltip for the mouse.
	 * @param m the mouse
	 */
	void handleTooltip(UIMouse m) {
		if (movieVisible() || m.has(UIMouse.Type.DOWN) || m.has(UIMouse.Type.DRAG)) {
			tooltipHelper = null;
			tooltipText = null;
			tooltipVisible = false;
			tooltipShowTimer.stop();
			return;
		}
		ScreenBase top = null;
		UIComponent c = null;
		List<ScreenBase> bases = new ArrayList<>();
		if (optionsVisible) {
			bases.add(options);
		} else {
			if (statusbarVisible) {
				bases.add(statusbar);
			}
			if (secondary != null) {
				bases.add(secondary);
			} else
				if (primary != null) {
					bases.add(primary);
				}
		}
		for (ScreenBase b : bases) {
			c = b.componentAt(m.x, m.y);
			if (c != b) {
				top = b;
				break;
			}
			c = null;
		}
		if (!optionsVisible) {
			// if no inner component, use the screen itself
			if (c == null && secondary != null) {
				c = secondary.componentAt(m.x, m.y);
				if (c != null) {
					top = secondary;
				}
			} else
				if (c == null && primary != null) {
					c = primary.componentAt(m.x, m.y);
					if (c != null) {
						top = primary;
					}
				}
		}

		Rectangle r = tooltipHelper;
		UIComponent c0 = tooltipComponent;
		if (c != null && top != null) {
			tooltipComponent = c;
			Rectangle tth = top.componentRectangle(c);

			double s = tth.width * 1d / c.width; 

			int cx = m.x - tth.x;
			int cy = m.y - tth.y;

			int rx = (int)(cx / s);
			int ry = (int)(cy / s);

			tooltipText = c.tooltip(rx, ry);
			if (tooltipText != null) {
				Rectangle ttr = c.tooltipLocation(rx, ry);

				int rx2 = (int)(tth.x + ttr.x / s);
				int ry2 = (int)(tth.y + ttr.y / s);
				int rw2 = (int)((ttr.width * s));
				int rh2 = (int)((ttr.height * s));

				tooltipHelper = new Rectangle(rx2, ry2, rw2, rh2);
			} else {
				tooltipHelper = tth;
				tooltipShowTimer.stop();
				tooltipVisible = false;
			}
		} else {
			tooltipComponent = null;
			tooltipHelper = null;
			tooltipVisible = false;
			tooltipShowTimer.stop();
		}
		if (!Objects.equals(r, tooltipHelper) || !Objects.equals(tooltipComponent, c0)) {
			tooltipVisible = false;
			tooltipShowTimer.stop();
			tooltipShowTimer.start();
			repaintInner();
		}
	}
	/**
	 * Render the current tooltip.
	 * @param g2 the graphics context.
	 */
	protected void renderTooltip(Graphics2D g2) {
		if (tooltipHelper != null && config.showTooltips) {
			if (helperRectangleVisible) {
				g2.setColor(Color.ORANGE);
				g2.drawRect(tooltipHelper.x, tooltipHelper.y, tooltipHelper.width - 1, tooltipHelper.height - 1);
			}
			if (tooltipText != null && tooltipVisible) {
				g2.setColor(Color.WHITE);

				int th = 10;

				UIColorLabel lbl = new UIColorLabel(th * config.uiScale / 100, commons.text());
				lbl.width = config.tooltipWidth;
				lbl.text(tooltipText);
				lbl.width = lbl.maxWidth();

				g2.setColor(new Color(40, 40, 40, 240));
				int x0 = tooltipHelper.x + (tooltipHelper.width - lbl.width - 6) / 2;
				int y0 = tooltipHelper.y + tooltipHelper.height;
				if (x0 < 0) {
					x0 = 0;
				} else
					if (x0 + lbl.width + 6 > getInnerWidth()) {
						x0 = getInnerWidth() - lbl.width - 6;
					}
				if (y0 + 20 > getInnerHeight()) {
					y0 = tooltipHelper.y - lbl.height - 6;
				}
				g2.fillRect(x0, y0, lbl.width + 6, lbl.height + 6);
				g2.setColor(Color.LIGHT_GRAY);
				g2.drawRect(x0, y0, lbl.width + 5, lbl.height + 5);
				g2.translate(x0 + 3, y0 + 3);
				lbl.draw(g2);
				g2.translate(-x0 - 3, -y0 - 3);
			}
		}
	}
	@Override
	public void tooltipChanged(UIComponent c) {
		if (c == tooltipComponent) {
			UIMouse m = UIMouse.createCurrent(surface);
			scaleMouse(m);
			boolean vis = tooltipVisible;
			handleTooltip(m);
			if (vis) {
				tooltipVisible = true;
			}
		}
	}
	/**
	 * Run the launcher.
	 */
	void damagedInstall() {
		try {
			Startup.runLauncher(config);
		} finally {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					dispose();
				}
			});
		}
	}
	@Override
	public boolean optionsVisible() {
		return optionsVisible;
	}
	@Override
	public boolean isUIComponentDebug() {
		return helperRectangleVisible;
	}
	@Override
	public void setUIComponentDebug(boolean value) {
		this.helperRectangleVisible = value;
	}
	@Override
	public void displayOptions(boolean save, SettingsPage page) {
		displayOptions();
		options.maySave(save);
		options.displayPage(page);
	}
	@Override
	public boolean isObjectivesVisible() {
		return allScreens.statusbar.objectives.visible();
	}
	@Override
	public void setObjectivesVisible(boolean value) {
		allScreens.statusbar.objectives.visible(value);
	}
	@Override
	public void toggleQuickResearch() {
		allScreens.statusbar.toggleQuickResearch();		
	}
	@Override
	public void toggleQuickProduction() {
		allScreens.statusbar.toggleQuickProduction();		
	}
	@Override
	public boolean isFixedFrameRate() {
		return fixedFramerate;
	}
	@Override
	public void setFixedFrameRate(boolean value) {
		fixedFramerate = value;
	}
	/**
	 * Continue the last save.
	 */
	public void continueLastGame() {
		if (primary == allScreens.main && secondary == null) {
			allScreens.main.checkExistingSave();
		}
	}
	/**
	 * Load the world data, considering if it is a campaign or skirmish.
	 * @param currentGame the current game
	 * @param skirmish the current skirmish definition
	 * @param xworld the world data
	 * @return the loaded world
	 */
	protected World loadWorldData(final String currentGame,
			final CustomGameDefinition skirmish,
			final XElement xworld) {
		String game = xworld.get("game");
		XElement sk = xworld.childElement("skirmish-definition");
		World world = commons.world(); 

		SkirmishDefinition sk1 = null;
		if (sk != null) {
			sk1 = new SkirmishDefinition();
			sk1.load(sk, commons.traits);
		}
		if (!game.equals(currentGame) || !Objects.equals(skirmish, sk1)) {
			commons.world(null);
			// load world model
			world = new World(commons);

			if (sk != null) {
				world.skirmishDefinition = sk1;
				world.definition = sk1.createDefinition(commons.rl);
				world.loadSkirmish(commons.rl, new SkirmishScripting());
			} else {
				world.definition = GameDefinition.parse(commons.rl, game);
				world.labels = new Labels();
				List<String> labels = new ArrayList<>();
				labels.add("labels");
				labels.addAll(world.definition.labels);
				world.labels.load(commons.rl, labels);
				world.loadCampaign(commons.rl);
			}
		}

		world.loadState(xworld);

		return world;
	}
}
