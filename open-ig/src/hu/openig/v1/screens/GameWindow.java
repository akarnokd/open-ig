/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.screens;

import hu.openig.v1.core.Act;
import hu.openig.v1.core.Configuration;
import hu.openig.v1.core.ResourceLocator;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
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
		for (ScreenBase sb : screens) {
			sb.initialize(commons, surface);
		}
		
		displayPrimary(commons.screens.mainmenu);
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
	 * @param screen the new screen to display
	 */
	@Override
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
	 * @param screen the screen to display as secondary
	 */
	@Override
	public void displaySecondary(ScreenBase screen) {
		if (secondary != null && secondary != screen) {
			secondary.onLeave();
		}
		if (secondary != screen) {
			secondary = screen;
			secondary.onEnter();
			if (surface.isShowing()) {
				Point p = getCurrentMousePosition();
				secondary.mouseMoved(0, p.x, p.y, 0);
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
			statusbar.handleRepaint();
		}
		if (secondary != null) {
			secondary.mouseMoved(0, pt.x, pt.y, 0);
			secondary.handleRepaint();
		} else
		if (primary != null) {
			primary.mouseMoved(0, pt.x, pt.y, 0);
			primary.handleRepaint();
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
			ScreenBase pri = primary;
			ScreenBase sec = secondary;
			if (movieVisible) {
				movie.keyTyped(e.getKeyCode(), e.getModifiersEx());
				movie.handleRepaint();
			} else
			if (sec != null) {
				sec.keyTyped(e.getKeyCode(), e.getModifiersEx());
				sec.handleRepaint();
			} else
			if (pri != null) {
				pri.keyTyped(e.getKeyCode(), e.getModifiersEx());
				pri.handleRepaint();
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
			ScreenBase pri = primary;
			ScreenBase sec = secondary;
			if (movieVisible) {
				movie.mousePressed(e.getButton(), e.getX(), e.getY(), e.getModifiersEx());
				movie.handleRepaint();
			} else
			if (sec != null) {
				sec.mousePressed(e.getButton(), e.getX(), e.getY(), e.getModifiersEx());
				sec.handleRepaint();
			} else
			if (pri != null) {
				pri.mousePressed(e.getButton(), e.getX(), e.getY(), e.getModifiersEx());
				pri.handleRepaint();
			}
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			ScreenBase pri = primary;
			ScreenBase sec = secondary;
			if (movieVisible) {
				movie.mouseReleased(e.getButton(), e.getX(), e.getY(), e.getModifiersEx());
				movie.handleRepaint();
			} else
			if (sec != null) {
				sec.mouseReleased(e.getButton(), e.getX(), e.getY(), e.getModifiersEx());
				sec.handleRepaint();
			} else
			if (pri != null) {
				pri.mouseReleased(e.getButton(), e.getX(), e.getY(), e.getModifiersEx());
				pri.handleRepaint();
			}
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			ScreenBase pri = primary;
			ScreenBase sec = secondary;
			if (movieVisible) {
				movie.mouseMoved(e.getButton(), e.getX(), e.getY(), e.getModifiers());
				movie.handleRepaint();
			} else
			if (sec != null) {
				sec.mouseMoved(e.getButton(), e.getX(), e.getY(), e.getModifiers());
				sec.handleRepaint();
			} else
			if (pri != null) {
				pri.mouseMoved(e.getButton(), e.getX(), e.getY(), e.getModifiers());
				pri.handleRepaint();
			}
		}
		@Override
		public void mouseMoved(MouseEvent e) {
			ScreenBase pri = primary;
			ScreenBase sec = secondary;
			if (movieVisible) {
				movie.mouseMoved(e.getButton(), e.getX(), e.getY(), e.getModifiers());
				movie.handleRepaint();
			} else
			if (sec != null) {
				sec.mouseMoved(e.getButton(), e.getX(), e.getY(), e.getModifiers());
				sec.handleRepaint();
			} else
			if (pri != null) {
				pri.mouseMoved(e.getButton(), e.getX(), e.getY(), e.getModifiers());
				pri.handleRepaint();
			}
		}
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			ScreenBase pri = primary;
			ScreenBase sec = secondary;
			if (movieVisible) {
				movie.mouseScrolled(e.getUnitsToScroll(), e.getX(), e.getY(), e.getModifiers());
				movie.handleRepaint();
			} else
			if (sec != null) {
				sec.mouseScrolled(e.getUnitsToScroll(), e.getX(), e.getY(), e.getModifiers());
				sec.handleRepaint();
			} else
			if (pri != null) {
				pri.mouseScrolled(e.getUnitsToScroll(), e.getX(), e.getY(), e.getModifiers());
				pri.handleRepaint();
			}
		}
	}
	/* (non-Javadoc)
	 * @see hu.openig.v1.GameControls#playVideos(java.lang.String[])
	 */
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
