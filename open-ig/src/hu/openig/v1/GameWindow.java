/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * The base game window which handles paint and input events.
 * @author karnokd, 2009.12.23.
 * @version $Revision 1.0$
 */
public class GameWindow extends JFrame implements GameControls {
	/**	 */
	private static final long serialVersionUID = 4521036079508511968L;
	/** 
	 * The component that renders the primary and secondary screens into the current window.
	 * @author karnokd, 2009.12.23.
	 * @version $Revision 1.0$
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
						primary.onResize();
					}
					if (secondary != null) {
						secondary.onResize();
					}
					if (movie != null) {
						movie.onResize();
					}
				}
			});
		}
		@Override
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D)g;
			if (movieVisible) {
				movie.paintTo(g2);
			} else {
				if (primary != null) {
					primary.paintTo(g2);
				}
				if (secondary != null) {
					secondary.paintTo(g2);
				}
				if (statusbarVisible) {
					statusbar.paintTo(g2);
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
	/** The main menu. */
	MainMenu mainMenu;
	/** The surface used to render the screens. */
	private ScreenRenderer surface;
	/** The spacewar rendering screen. */
	private SpacewarScreen spacewar;
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
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				uninitScreens();
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
		
		mainMenu = new MainMenu();
		screens.add(mainMenu);
		
		spacewar = new SpacewarScreen();
		screens.add(spacewar);

		movie = new MovieScreen();
		screens.add(movie);
		
		for (ScreenBase sb : screens) {
			sb.initialize(commons, surface);
		}
		
		displayPrimary(mainMenu);
	}
	/** Unitialize the screens. */
	protected void uninitScreens() {
		for (ScreenBase sb : screens) {
			if (primary == sb || secondary == sb) {
				sb.onLeave();
			}
			sb.finish();
		}
		primary = null;
		secondary = null;
	}
	/**
	 * Display the given screen as the primary object. The secondary object, if any, will be removed.
	 * @param sb the new screen to display
	 */
	@Override
	public void displayPrimary(ScreenBase sb) {
		hideMovie();
		if (secondary != null) {
			secondary.onLeave();
			secondary = null;
		}
		if (primary != null && primary != sb) {
			primary.onLeave();
		}
		primary = sb;
		if (primary != null) {
			primary.onEnter(); 
			// send a mouse moved event so that if necessary, components can react to mouseOver immediately
			if (surface.isShowing()) {
				Point p = getCurrentMousePosition();
				primary.mouseMoved(0, p.x, p.y, 0);
			}
		}
		surface.repaint();
	}
	/**
	 * Retrieves the current mouse position relative to the rendering surface.
	 * @return the current relative mouse position.
	 */
	public Point getCurrentMousePosition() {
		Point p = MouseInfo.getPointerInfo().getLocation();
		Point c = surface.getLocationOnScreen();
		return new Point(c.x - p.x, c.y - p.y);
	}
	/**
	 * Display the given secondary screen.
	 * @param sb the screen to display as secondary
	 */
	@Override
	public void displaySecondary(ScreenBase sb) {
		if (secondary != null && secondary != sb) {
			secondary.onLeave();
		}
		secondary = sb;
		surface.repaint();
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
	public void displayStatusbar() {
		if (!statusbarVisible) {
			statusbarVisible = true;
			statusbar.onEnter();
			doMoveMouseAgain();
			surface.repaint();
		}
	}
	/**
	 * Hide the status bar.
	 */
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
		Point pt = getCurrentMousePosition();
		if (statusbarVisible) {
			statusbar.mouseMoved(0, pt.x, pt.y, 0);
		}
		if (secondary != null) {
			secondary.mouseMoved(0, pt.x, pt.y, 0);
		} else
		if (primary != null) {
			primary.mouseMoved(0, pt.x, pt.y, 0);
		}
	}
	/**
	 * The common key manager.
	 * @author karnokd, 2009.12.24.
	 * @version $Revision 1.0$
	 */
	class KeyEvents extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
//			System.out.println("Press " + e.getKeyCode());
			if (movieVisible) {
				movie.keyTyped(e.getKeyCode(), e.getModifiersEx());
			} else
			if (secondary != null) {
				secondary.keyTyped(e.getKeyCode(), e.getModifiersEx());
			} else
			if (primary != null) {
				primary.keyTyped(e.getKeyCode(), e.getModifiersEx());
			}
		}
	}
	/**
	 * The common mouse action manager.
	 * @author karnokd, 2009.12.23.
	 * @version $Revision 1.0$
	 */
	class MouseActions extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
//			System.out.println("Press");
			if (movieVisible) {
				movie.mousePressed(e.getButton(), e.getX(), e.getY(), e.getModifiersEx());
			} else
			if (secondary != null) {
				secondary.mousePressed(e.getButton(), e.getX(), e.getY(), e.getModifiersEx());
			} else
			if (primary != null) {
				primary.mousePressed(e.getButton(), e.getX(), e.getY(), e.getModifiersEx());
			}
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			if (movieVisible) {
				movie.mouseReleased(e.getButton(), e.getX(), e.getY(), e.getModifiersEx());
			} else
			if (secondary != null) {
				secondary.mouseReleased(e.getButton(), e.getX(), e.getY(), e.getModifiersEx());
			} else
			if (primary != null) {
				primary.mouseReleased(e.getButton(), e.getX(), e.getY(), e.getModifiersEx());
			}
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			if (movieVisible) {
				movie.mouseMoved(e.getButton(), e.getX(), e.getY(), e.getModifiers());
			} else
			if (secondary != null) {
				secondary.mouseMoved(e.getButton(), e.getX(), e.getY(), e.getModifiers());
			} else
			if (primary != null) {
				primary.mouseMoved(e.getButton(), e.getX(), e.getY(), e.getModifiers());
			}
		}
		@Override
		public void mouseMoved(MouseEvent e) {
			if (movieVisible) {
				movie.mouseMoved(e.getButton(), e.getX(), e.getY(), e.getModifiers());
			} else
			if (secondary != null) {
				secondary.mouseMoved(e.getButton(), e.getX(), e.getY(), e.getModifiers());
			} else
			if (primary != null) {
				primary.mouseMoved(e.getButton(), e.getX(), e.getY(), e.getModifiers());
			}
		}
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (movieVisible) {
				movie.mouseScrolled(e.getUnitsToScroll(), e.getX(), e.getY(), e.getModifiers());
			} else
			if (secondary != null) {
				secondary.mouseScrolled(e.getUnitsToScroll(), e.getX(), e.getY(), e.getModifiers());
			} else
			if (primary != null) {
				primary.mouseScrolled(e.getUnitsToScroll(), e.getX(), e.getY(), e.getModifiers());
			}
		}
	}
	/* (non-Javadoc)
	 * @see hu.openig.v1.GameControls#playVideos(java.lang.String[])
	 */
	@Override
	public void playVideos(String... videos) {
		for (String s : videos) {
			movie.mediaQueue.add(s);
		}
		movie.playbackFinished = new Act() {
			@Override
			public void act() {
				hideMovie();
			}
		};
		displayMovie();
		
	}
}
