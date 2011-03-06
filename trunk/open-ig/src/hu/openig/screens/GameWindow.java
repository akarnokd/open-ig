/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import hu.openig.core.Act;
import hu.openig.core.Configuration;
import hu.openig.core.ResourceLocator;
import hu.openig.screens.AchievementsScreen.Mode;
import hu.openig.ui.UIMouse;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;

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
					if (primary != null) {
						primary.resize();
					}
					if (secondary != null) {
						secondary.resize();
					}
					if (movie != null) {
						movie.resize();
					}
				}
			});
		}
		@Override
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D)g;
			if (movieVisible) {
				movie.draw(g2);
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
	}
	/** The primary screen. */
	ScreenBase primary;
	/** The secondary screen drawn over the first. */
	ScreenBase secondary;
	/** The status bar to display over the primary and secondary screens. */
	ScreenBase statusbar;
	/** Is the status bar visible? */
	boolean statusbarVisible;
	/** The top overlay for playing 'full screen' movies. */
	MovieScreen movie;
	/** Is the movie visible. */
	boolean movieVisible;
	/** The configuration object. */
	Configuration config;
	/** The common resource locator. */
	ResourceLocator rl;
	/** The common resources. */
	CommonResources commons;
	/** The surface used to render the screens. */
	private ScreenRenderer surface;
	/** The list of screens. */
	protected List<ScreenBase> screens;
	/** 
	 * Constructor. 
	 * @param config the configuration object.
	 */
	public GameWindow(Configuration config) {
		super("Open Imperium Galactica " + Configuration.VERSION);
		URL icon = this.getClass().getResource("/hu/openig/res/open-ig-logo.png");
		if (icon != null) {
			try {
				setIconImage(ImageIO.read(icon));
			} catch (IOException e) {
				config.log("ERROR", e.getMessage(), e);
			}
		}
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		commons = new CommonResources(config, this);
		this.config = config;
		this.rl = commons.rl;
		
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		
		surface = new ScreenRenderer();
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
		MouseActions ma = new MouseActions();
		surface.addMouseListener(ma);
		surface.addMouseMotionListener(ma);
		surface.addMouseWheelListener(ma);
		addKeyListener(new KeyEvents());
		initScreens();
	}
	/* (non-Javadoc)
	 * @see hu.openig.v1.screens.GameControls#exit()
	 */
	@Override
	public void exit() {
		uninitScreens();
		dispose();
	}
	/* (non-Javadoc)
	 * @see hu.openig.v1.screens.GameControls#setWindowBounds(int, int, int, int)
	 */
	@Override
	public void setWindowBounds(int x, int y, int width, int height) {
		setBounds(x, y, width, height);
	}
	/* (non-Javadoc)
	 * @see hu.openig.v1.GameControls#switchLanguage(java.lang.String)
	 */
	@Override
	public void switchLanguage(String newLanguage) {
		commons.reinit(newLanguage);
		surface.repaint();
		for (ScreenBase sb : screens) {
			sb.initialize(commons, surface);
		}
	}
	/** Initialize the various screen renderers. */
	protected void initScreens() {
		screens = new ArrayList<ScreenBase>();
		
		try {
			for (Field f : commons.screens.getClass().getFields()) {
				if (ScreenBase.class.isAssignableFrom(f.getType())) {
					ScreenBase sb = ScreenBase.class.cast(f.getType().newInstance());
					f.set(commons.screens, sb);
					screens.add(sb);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		movie = commons.screens.movie;
		statusbar = commons.screens.statusbar;
		for (ScreenBase sb : screens) {
			sb.initialize(commons, surface);
		}
		
		displayPrimary(commons.screens.main);
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
	 * @return the instance
	 */
	ScreenBase getScreen(Screens screen) {
		switch (screen) {
		case ACHIEVEMENTS:
			return commons.screens.statisticsAchievements;
		case BAR:
			return commons.screens.bar;
		case BRIDGE:
			return commons.screens.bridge;
		case COLONY:
			return commons.screens.colony;
		case DIPLOMACY:
			return commons.screens.diplomacy;
		case EQUIPMENT:
			return commons.screens.equipment;
		case INFORMATION:
			return commons.screens.info;
		case PRODUCTION:
			return commons.screens.researchProduction;
		case RESEARCH:
			return commons.screens.researchProduction;
		case SPACEWAR:
			return commons.screens.spacewar;
		case STARMAP:
			return commons.screens.starmap;
		case SHIPWALK:
			return commons.screens.shipwalk;
		case DATABASE:
			return commons.screens.database;
		case LOADING:
			return commons.screens.loading;
		case LOAD_SAVE:
			return commons.screens.loadSave;
		case MAIN:
			return commons.screens.main;
		case MULTIPLAYER:
			return null; // TODO multiplayer screen
		case SINGLEPLAYER:
			return commons.screens.singleplayer;
		case VIDEOS:
			return commons.screens.videos;
		default:
		}
		return null;
	}
	@Override
	public void displayPrimary(Screens screen) {
		displayPrimary(getScreen(screen));
	}
	@Override
	public void displaySecondary(Screens screen) {
		displaySecondary(getScreen(screen));
	}
	/**
	 * Display the given screen as the primary object. The secondary object, if any, will be removed.
	 * @param screen the new screen to display
	 */
	public void displayPrimary(ScreenBase screen) {
		hideMovie();
		if (secondary != null) {
			secondary.onLeave();
			secondary = null;
		}
		if (primary != null && primary != screen) {
			primary.onLeave();
		}
		primary = screen;
		if (primary != null) {
			primary.onEnter();
			primary.resize();
			// send a mouse moved event so that if necessary, components can react to mouseOver immediately
			if (surface.isShowing()) {
				primary.mouse(UIMouse.createCurrent(surface));
			}
			surface.repaint();
		}
	}
	/**
	 * Display the given secondary screen.
	 * @param screen the screen to display as secondary
	 */
	public void displaySecondary(ScreenBase screen) {
		if (secondary != null && secondary != screen) {
			secondary.onLeave();
		}
		if (secondary != screen) {
			secondary = screen;
			secondary.onEnter();
			secondary.resize();
			if (surface.isShowing()) {
				secondary.mouse(UIMouse.createCurrent(surface));
			}
			surface.repaint();
		}
	}
	/**
	 * Display the movie window.
	 */
	public void displayMovie() {
		if (!movieVisible) {
			movieVisible = true;
			movie.onEnter();
			doMoveMouseAgain();
			surface.repaint();
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
			surface.repaint();
		}
	}
	/**
	 * Display the status bar.
	 */
	@Override 
	public void displayStatusbar() {
		if (!statusbarVisible) {
			statusbarVisible = true;
			statusbar.onEnter();
			doMoveMouseAgain();
			surface.repaint();
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
			surface.repaint();
		}
	}
	/**
	 * Hide the status bar.
	 */
	@Override 
	public void hideStatusbar() {
		if (statusbarVisible) {
			statusbarVisible = true;
			statusbar.onLeave();
			doMoveMouseAgain();
			surface.repaint();
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
		if (pri != null) {
			result |= pri.mouse(m);
		} else
		if (sec != null) {
			result |= sec.mouse(m);
		}
		if (result) {
			repaint();
		}
	}
	/**
	 * The common key manager.
	 * @author akarnokd, 2009.12.24.
	 */
	class KeyEvents extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			if (!handleScreenSwitch(e)) {
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
				if (rep) {
					repaint();
				}
			}
		}
		/**
		 * Handle the screen switch if the appropriate key is pressed.
		 * @param e the key event
		 * @return true if the key event was handled
		 */
		boolean handleScreenSwitch(KeyEvent e) {
			boolean result = false;
			if (!commons.worldLoading && commons.world != null && !movieVisible) {
				result = true;
				switch (e.getKeyCode()) {
				case KeyEvent.VK_F1:
					displayPrimary(commons.screens.bridge);
					break;
				case KeyEvent.VK_F2:
					displayPrimary(commons.screens.starmap);
					break;
				case KeyEvent.VK_F3:
					displayPrimary(commons.screens.colony);
					break;
				case KeyEvent.VK_F4:
					displaySecondary(commons.screens.equipment);
					break;
				case KeyEvent.VK_F5:
					displaySecondary(commons.screens.researchProduction);
					break;
				case KeyEvent.VK_F6:
					displaySecondary(commons.screens.researchProduction);
					break;
				case KeyEvent.VK_F7:
					displaySecondary(commons.screens.info);
					break;
				case KeyEvent.VK_F8:
					displayPrimary(commons.screens.diplomacy);
					break;
				case KeyEvent.VK_F9:
					displayPrimary(commons.screens.database);
					break;
				case KeyEvent.VK_F10:
					displayPrimary(commons.screens.bar);
					break;
				case KeyEvent.VK_F11:
					commons.screens.statisticsAchievements.mode = Mode.STATISTICS;
					displaySecondary(commons.screens.statisticsAchievements);
					surface.repaint();
					break;
				case KeyEvent.VK_F12:
					commons.screens.statisticsAchievements.mode = Mode.ACHIEVEMENTS;
					displaySecondary(commons.screens.statisticsAchievements);
					surface.repaint();
					break;
				case KeyEvent.VK_1:
					if (e.isControlDown()) {
						commons.world.level = 1;
						primary = null;
						displayPrimary(commons.screens.bridge);
					}
					break;
				case KeyEvent.VK_2:
					if (e.isControlDown()) {
						commons.world.level = 2;
						primary = null;
						displayPrimary(commons.screens.bridge);
					}
					break;
				case KeyEvent.VK_3:
					if (e.isControlDown()) {
						commons.world.level = 3;
						primary = null;
						displayPrimary(commons.screens.bridge);
					}
					break;
				case KeyEvent.VK_4:
					if (e.isControlDown()) {
						commons.world.level = 4;
						primary = null;
						displayPrimary(commons.screens.bridge);
					}
					break;
				case KeyEvent.VK_5:
					if (e.isControlDown()) {
						commons.world.level = 5;
						primary = null;
						displayPrimary(commons.screens.bridge);
					}
					break;
				default:
					result = false;
				}
			}
			if (result) {
				e.consume();
			}
			return result;
		}
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
			} else
			if (sec != null) {
				rep = sec.mouse(UIMouse.from(e));
			} else
			if (pri != null) {
				rep = pri.mouse(UIMouse.from(e));
			}
			if (rep) {
				repaint();
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
	public void center() {
		setLocationRelativeTo(null);
	}
	@Override
	public Rectangle getWindowBounds() {
		return getBounds();
	}
}
