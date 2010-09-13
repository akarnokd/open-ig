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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * The main menu rendering and actions.
 * @author karnokd, 2009.12.25.
 * @version $Revision 1.0$
 */
public class MainMenu extends ScreenBase {
	/**
	 * The click label.
	 * @author karnokd, 2009.12.26.
	 * @version $Revision 1.0$
	 */
	class ClickLabel {
		/** The origin. */
		public int x;
		/** The origin. */
		public int y;
		/** The label's maximum width to use when centering. */
		public int width;
		/** The action to invoke. */
		public Act action;
		/** The text size. */
		public int size;
		/** The text label. */
		public String label;
		/** The selected state. */
		public boolean selected;
		/** The pressed state. */
		public boolean pressed;
		/** The button is disabled and does not react to mouse events. */
		public boolean disabled;
		/**
		 * Constructor.
		 * @param x the X coordinate
		 * @param y the Y coordinate
		 * @param width the available width for centering
		 * @param size the text size
		 * @param label the label
		 */
		public ClickLabel(int x, int y, int width, int size, String label) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.size = size;
			this.label = label;
		}
		/**
		 * Paint the label.
		 * @param g2 the graphics context
		 * @param x0 the origin
		 * @param y0 the origin
		 */
		public void paintTo(Graphics2D g2, int x0, int y0) {
			int color = 0xFFFFCC00;
			if (disabled) {
				color = 0xFFC0C0C0;
			} else
			if (pressed) {
				color = 0xFFFF0000;
			} else
			if (selected) {
				color = 0xFFFFFFFF;
			}
			int textWidth = commons.text.getTextWidth(size, commons.labels.get(label));
			commons.text.paintTo(g2, x0 + x + (width - textWidth) / 2, y0 + y, size, color, commons.labels.get(label));
		}
		/**
		 * Test if the mouse is within the label.
		 * @param mx the mouse X coordinate
		 * @param my the mouse Y coordinate
		 * @param x0 the screen rendering origin
		 * @param y0 the screen rendering origin
		 * @return true if mouse in the label
		 */
		public boolean test(int mx, int my, int x0, int y0) {
			int w = width;
			return !disabled && (x0 + x) <= mx && (x0 + x + w) > mx
			&& (y0 + y) <= my && (y0 + y + size) > my;
		}
		/** Invoke the associated action. */
		public void invoke() {
			if (action != null) {
				action.act();
			}
		}
	}
	/** The screen X origin. */
	private int xOrigin;
	/** The screen Y origin. */
	private int yOrigin;
	/** The background image. */
	private BufferedImage background;
	/** The list of clickable labels. */
	private List<ClickLabel> clicklabels;
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#finish()
	 */
	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#initialize()
	 */
	@Override
	public void initialize() {
		clicklabels = new LinkedList<ClickLabel>();
		
		ClickLabel single = new ClickLabel(120, 120, 400, 20, "mainmenu.singleplayer");
		single.action = new Act() {
			@Override
			public void act() {
				commons.control.displayPrimary(commons.screens.singleplayer);
			}
		};
		clicklabels.add(single);

		clicklabels.add(new ClickLabel(120, 155, 400, 14, "mainmenu.continue"));
		ClickLabel load = new ClickLabel(120, 180, 400, 14, "mainmenu.load");
		load.action = new Act() {
			@Override
			public void act() {
				commons.control.displayPrimary(commons.screens.loadSave);
			}
		};
		clicklabels.add(load);
		
		ClickLabel multiplayer = new ClickLabel(120, 215, 400, 20 , "mainmenu.multiplayer");
		multiplayer.disabled = true;
		clicklabels.add(multiplayer);
		ClickLabel settings = new ClickLabel(120, 250, 400, 20, "mainmenu.settings");
		settings.action = new Act() {
			@Override
			public void act() {
				doSettings();
			}
		};
		clicklabels.add(settings);
		ClickLabel videosLabel = new ClickLabel(120, 285, 400, 20, "mainmenu.videos");
		videosLabel.action = new Act() {
			@Override
			public void act() {
				commons.control.displaySecondary(commons.screens.videos);
			}
		};
		clicklabels.add(videosLabel);
		
		ClickLabel introLabel = new ClickLabel(120, 320, 400, 14, "mainmenu.videos.intro");
		introLabel.action = new Act() {
			@Override
			public void act() {
				doPlayIntro();
			}
		};
		clicklabels.add(introLabel);
		ClickLabel titleLabel = new ClickLabel(120, 345, 400, 14, "mainmenu.videos.title");
		titleLabel.action = new Act() {
			@Override
			public void act() {
				doPlayTitle();
			}
		};
		clicklabels.add(titleLabel);
		
		ClickLabel exit = new ClickLabel(120, 380, 400, 20, "mainmenu.exit");
		exit.action = new Act() { public void act() { doExit(); } };
		clicklabels.add(exit);
		
		// Language switcher on the main menu, for convenience
		final ClickLabel toEng = new ClickLabel(550, 400, 20, 14, "EN");
		toEng.disabled = commons.config.language.equals("en");
		final ClickLabel toHu = new ClickLabel(550, 380, 20, 14, "HU");
		toHu.disabled = commons.config.language.equals("hu");
		
		toEng.action = new Act() {
			@Override
			public void act() {
				toEng.disabled = true;
				toHu.disabled = false;
				commons.control.switchLanguage("en");
				selectRandomBackground();
				repaint();
			}
		};
		toHu.action = new Act() {
			@Override
			public void act() {
				toEng.disabled = false;
				toHu.disabled = true;
				commons.control.switchLanguage("hu");
				selectRandomBackground();
				repaint();
			}
		};
		
		clicklabels.add(toEng);
		clicklabels.add(toHu);
	}
	/** Perform the exit. */
	void doExit() {
		commons.control.exit();
	}
	/**
	 * Play the intro videos.
	 */
	protected void doPlayIntro() {
		commons.control.playVideos("intro/intro_1", "intro/intro_2", "intro/intro_3");
	}
	/**
	 * Play the title video.
	 */
	protected void doPlayTitle() {
		commons.control.playVideos("intro/gt_interactive_intro");
	}
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#keyTyped(int, int)
	 */
	@Override
	public void keyTyped(int key, int modifiers) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mouseMoved(int, int, int, int)
	 */
	@Override
	public void mouseMoved(int button, int x, int y, int modifiers) {
		boolean needRepaint = false;
		for (ClickLabel cl : clicklabels) {
			if (cl.test(x, y, xOrigin, yOrigin)) {
				needRepaint |= !cl.selected;
				cl.selected = true;
			} else {
				needRepaint |= cl.selected;
				cl.selected = false;
			}
		}
		if (needRepaint) {
			repaint();
		}
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mousePressed(int, int, int, int)
	 */
	@Override
	public void mousePressed(int button, int x, int y, int modifiers) {
		boolean needRepaint = false;
		for (ClickLabel cl : clicklabels) {
			if (cl.test(x, y, xOrigin, yOrigin)) {
				needRepaint |= !cl.pressed;
				cl.pressed = true;
			} else {
				needRepaint |= cl.pressed;
				cl.pressed = false;
			}
		}
		if (needRepaint) {
			repaint();
		}
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mouseReleased(int, int, int, int)
	 */
	@Override
	public void mouseReleased(int button, int x, int y, int modifiers) {
		boolean needRepaint = false;
		for (ClickLabel cl : clicklabels) {
			if (cl.test(x, y, xOrigin, yOrigin) && cl.pressed) {
				cl.invoke();
			}
			needRepaint |= cl.pressed;
			cl.pressed = false;
		}
		if (needRepaint) {
			repaint();
		}
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mouseScrolled(int, int, int, int)
	 */
	@Override
	public void mouseScrolled(int direction, int x, int y, int modifiers) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#onEnter()
	 */
	@Override
	public void onEnter() {
		selectRandomBackground();
		onResize();
	}
	/** The random used for background selection. */
	Random rnd = new Random();
	/**
	 * Set the background randomly.
	 */
	protected void selectRandomBackground() {
		background = commons.background.start[rnd.nextInt(commons.background.start.length)];
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#onLeave()
	 */
	@Override
	public void onLeave() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#onResize()
	 */
	@Override
	public void doResize() {
		// relocate objects if necessary
		xOrigin = (parent.getWidth() - background.getWidth()) / 2;
		yOrigin = (parent.getHeight() - background.getHeight()) / 2;
	}
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#paintTo(java.awt.Graphics2D)
	 */
	@Override
	public void paintTo(Graphics2D g2) {
		onResize(); // repaint might come before an onResize
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, parent.getWidth(), parent.getHeight());
		g2.drawImage(background, xOrigin, yOrigin, null);
	
		commons.text.paintTo(g2, xOrigin + 121, yOrigin + 21, 14, 0xFF000000, "Open");
		commons.text.paintTo(g2, xOrigin + 120, yOrigin + 20, 14, 0xFFFFFF00, "Open");
		commons.text.paintTo(g2, xOrigin + 501, yOrigin + 65, 14, 0xFF000000, Configuration.VERSION);
		commons.text.paintTo(g2, xOrigin + 500, yOrigin + 64, 14, 0xFFFF0000, Configuration.VERSION);
		
		Composite c0 = g2.getComposite();
		g2.setComposite(AlphaComposite.SrcOver.derive(0.8f));
		
		g2.fillRoundRect(xOrigin + 60, yOrigin + 100, 640 - 120, 442 - 100 - 20, 40, 40);
		g2.setComposite(c0);
	
		for (ClickLabel cl : clicklabels) {
			cl.paintTo(g2, xOrigin, yOrigin);
		}
	}
	/** Display the settings video. */
	void doSettings() {
		// do a small reflection trick to avoid circular dependency
		try {
			Class<?> clazz = Class.forName("hu.openig.v1.Setup");
			Constructor<?> c = clazz.getConstructor(Configuration.class, GameControls.class);
			Object instance = c.newInstance(commons.config, commons.control);
			clazz.getMethod("setVisible", Boolean.TYPE).invoke(instance, true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	@Override
	public void mouseDoubleClicked(int button, int x, int y, int modifiers) {
		// TODO Auto-generated method stub
		
	}
}
