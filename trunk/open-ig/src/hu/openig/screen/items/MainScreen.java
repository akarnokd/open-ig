/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.core.Configuration;
import hu.openig.core.Pair;
import hu.openig.model.Screens;
import hu.openig.model.SoundType;
import hu.openig.render.RenderTools;
import hu.openig.screen.ScreenBase;
import hu.openig.screen.items.LoadSaveScreen.SettingsPage;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIMouse;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.SwingUtilities;

/**
 * The main menu rendering and actions.
 * @author akarnokd, 2009.12.25.
 */
public class MainScreen extends ScreenBase {
	/**
	 * The click label.
	 * @author akarnokd, 2009.12.26.
	 */
	class ClickLabel {
		/** The origin. */
		public int x;
		/** The origin. */
		public int y;
		/** The label's maximum width to use when centering. */
		public int width;
		/** The action to invoke. */
		public Action0 action;
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
			if (width < 0) {
				this.width = commons.text().getTextWidth(size, get(label)) + 20;
			} else {
				this.width = width;
			}
			this.size = size;
			this.label = label;
		}
		/** Invoke the associated action. */
		public void invoke() {
			if (action != null) {
				action.invoke();
			}
		}
		/**
		 * Paint the label.
		 * @param g2 the graphics context
		 */
		public void paintTo(Graphics2D g2) {
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
			int textWidth = commons.text().getTextWidth(size, get(label));

			int c0 = x + (width - textWidth) / 2;
			int c1 = y;

			Composite save0 = g2.getComposite();
			g2.setComposite(AlphaComposite.SrcOver.derive(0.5f));
			if (selected) {
				g2.setColor(new Color(224, 0, 0));
				g2.fillRoundRect(c0 - 10, c1 - 5, textWidth + 20, size + 10, 10, 10);
			} else {
				g2.setColor(Color.BLACK);
				g2.fillRoundRect(c0 - 10, c1 - 5, textWidth + 20, size + 10, 10, 10);
			}
			g2.setComposite(save0);

			commons.text().paintTo(g2, 2 + c0, 2 + c1, size, 0xFF000000, get(label));
			commons.text().paintTo(g2, 1 + c0, 1 + c1, size, 0xFF000000, get(label));
			commons.text().paintTo(g2, c0, c1, size, color, get(label));
		}
		/**
		 * Test if the mouse is within the label.
		 * @param mx the mouse X coordinate
		 * @param my the mouse Y coordinate
		 * @return true if mouse in the label
		 */
		public boolean test(int mx, int my) {
			int w = width;
			return !disabled && (x) <= mx && (x + w) > mx
			&& (y - 5) <= my && (y + size + 5) > my;
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
	/** The random used for background selection. */
	Random rnd = new Random();
	/** The continue labe. */
	private ClickLabel continueLabel;
	/** The achievements screen. */
	UIImageButton achievements;
	/** The single player click label. */
	private ClickLabel single;
	/** Label button. */
	private ClickLabel load;
	/** Label button. */
	private ClickLabel multiplayer;
	/** Label button. */
	private ClickLabel settings;
	/** Label button. */
	private ClickLabel videosLabel;
	/** Label button. */
	private ClickLabel introLabel;
	/** Label button. */
	private ClickLabel titleLabel;
	/** Label button. */
	private ClickLabel exit;
	/** Label button. */
	private ClickLabel creditsLabel;
	/** Resume the last gameplay. */
	void doContinue() {
		continueLabel.disabled = !isSaveAvailable();
		if (!continueLabel.disabled) {
			load(null);
		}
	}
	/** Perform the exit. */
	void doExit() {
		effectSound(SoundType.GOOD_BYE);
		try {
			Thread.sleep(1500);
		} catch (InterruptedException ex) {
			// ignored
		}
		exit();
	}
	/**
	 * Play the intro videos.
	 */
	protected void doPlayIntro() {
		commons.control().playVideos("intro/intro_1", "intro/intro_2", "intro/intro_3");
	}
	/**
	 * Play the title video.
	 */
	protected void doPlayTitle() {
		commons.control().playVideos("intro/gt_interactive_intro");
	}
	/** Display the settings video. */
	void doSettings() {
		// reload global configuration
		config.load();
		commons.control().displayOptions();
		LoadSaveScreen scr = commons.control().getScreen(Screens.LOAD_SAVE);
		scr.maySave(false);
		scr.displayPage(SettingsPage.AUDIO);
	}

	@Override
	public void draw(Graphics2D g2) {
		onResize(); // repaint might come before an onResize


		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, getInnerWidth(), getInnerHeight());

		AffineTransform save0 = g2.getTransform();
		
		if (config.scaleAllScreens) {
			int w = background.getWidth();
			int h = background.getHeight();
			Pair<Point, Double> pd = RenderTools.fitWindow(getInnerWidth(), getInnerHeight() - RenderTools.STATUS_BAR_TOP - RenderTools.STATUS_BAR_BOTTOM, w, h);
			xOrigin = pd.first.x;
			yOrigin = pd.first.y + RenderTools.STATUS_BAR_TOP;
			g2.translate(xOrigin, yOrigin);
			g2.scale(pd.second, pd.second);
		} else {
			g2.translate(xOrigin, yOrigin);
		}

		g2.drawImage(background, 0, 0, null);
	
		int w0 = commons.text().getTextWidth(14, "Open");
		g2.setColor(new Color(0, 0, 0, 128));
		g2.fillRect(118, 18, w0 + 4, 20);
		commons.text().paintTo(g2, 121, 21, 14, 0xFF000000, "Open");
		commons.text().paintTo(g2, 120, 20, 14, 0xFFFFFF00, "Open");
		
		
		int w1 = commons.text().getTextWidth(14, Configuration.VERSION);
		g2.setColor(new Color(0, 0, 0, 128));
		g2.fillRect(498, 61, w1 + 4, 20);
		commons.text().paintTo(g2, 501, 65, 14, 0xFF000000, Configuration.VERSION);
		commons.text().paintTo(g2, 500, 64, 14, 0xFFFF0000, Configuration.VERSION);
		
		for (ClickLabel cl : clicklabels) {
			cl.paintTo(g2);
		}
		
		super.draw(g2);
		
		g2.setTransform(save0);
	}
	@Override
	public boolean mouse(UIMouse e) {
		// scale mouse activity
		int w = background.getWidth();
		int h = background.getHeight();
		if (config.scaleAllScreens) {
			Pair<Point, Double> pd = RenderTools.fitWindow(getInnerWidth(), getInnerHeight() - RenderTools.STATUS_BAR_TOP - RenderTools.STATUS_BAR_BOTTOM, w, h);
			e.x = (int)((e.x - pd.first.x) / pd.second);
			e.y = (int)((e.y - pd.first.y - RenderTools.STATUS_BAR_TOP) / pd.second);
		} else {
			e.x -= xOrigin;
			e.y -= yOrigin;
		}
		// test mouse activity
		boolean needRepaint = false;
		switch (e.type) {
		case MOVE:
		case DRAG:
		case ENTER:
		case LEAVE:
			for (ClickLabel cl : clicklabels) {
				if (cl.test(e.x, e.y)) {
					needRepaint |= !cl.selected;
					cl.selected = true;
				} else {
					needRepaint |= cl.selected;
					cl.selected = false;
				}
			}
			break;
		case DOWN:
			for (ClickLabel cl : clicklabels) {
				if (cl.test(e.x, e.y)) {
					needRepaint |= !cl.pressed;
					cl.pressed = true;
				} else {
					needRepaint |= cl.pressed;
					cl.pressed = false;
				}
			}
			break;
		case UP:
			for (ClickLabel cl : clicklabels) {
				if (cl.test(e.x, e.y) && cl.pressed) {
					cl.invoke();
				}
				needRepaint |= cl.pressed;
				cl.pressed = false;
			}
			break;
		default:
		}
		if (!needRepaint) {
			return super.mouse(e);
		}
		return needRepaint;
	}
	@Override
	public void onEndGame() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onEnter(Screens mode) {
		selectRandomBackground();
		onResize();
		
		checkExistingSave();
	}
	/**
	 * Search for previous saves to continue.
	 */
	public void checkExistingSave() {
		continueLabel.disabled = true;
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				final boolean found = isSaveAvailable();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						continueLabel.disabled = !found;
						askRepaint();
					}
				});
			}
		}, "Save-Lookup");
		t.start();
	}
	/**
	 * @return Check if save file is available.
	 */
	boolean isSaveAvailable() {
		File dir = new File("save/default");
		if (dir.exists()) {
			File[] files = dir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.startsWith("save-") && name.endsWith(".xml");
				}
			});
			return files != null && files.length > 0;
		}
		return false;
	}
	@Override
	public void onFinish() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onInitialize() {
		clicklabels = new LinkedList<ClickLabel>();
		
		single = new ClickLabel(120, 120, -400, 20, "mainmenu.singleplayer");
		single.action = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
				displayPrimary(Screens.SINGLEPLAYER);
			}
		};
		clicklabels.add(single);

		continueLabel = new ClickLabel(120, 155, -400, 14, "mainmenu.continue");
		continueLabel.action = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
				doContinue();
			}
		};
		continueLabel.disabled = true;
		clicklabels.add(continueLabel);
		load = new ClickLabel(120, 180, -400, 14, "mainmenu.load");
		load.action = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
				commons.control().displayOptions();
				LoadSaveScreen scr = (LoadSaveScreen)commons.control().getScreen(Screens.LOAD_SAVE);
				scr.displayPage(SettingsPage.LOAD_SAVE);
				scr.maySave(false);
			}
		};
		clicklabels.add(load);
		
		multiplayer = new ClickLabel(120, 215, -400, 20 , "mainmenu.multiplayer");
		multiplayer.disabled = true;
		clicklabels.add(multiplayer);
		settings = new ClickLabel(120, 250, -400, 20, "mainmenu.settings");
		settings.action = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
				doSettings();
			}
		};
		clicklabels.add(settings);
		videosLabel = new ClickLabel(120, 285, -400, 20, "mainmenu.videos");
		videosLabel.action = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
				displaySecondary(Screens.VIDEOS);
			}
		};
		clicklabels.add(videosLabel);
		
		introLabel = new ClickLabel(120, 320, -180, 14, "mainmenu.videos.intro");
		introLabel.action = new Action0() {
			@Override
			public void invoke() {
				doPlayIntro();
			}
		};
		clicklabels.add(introLabel);
		titleLabel = new ClickLabel(340, 320, -180, 14, "mainmenu.videos.title");
		titleLabel.action = new Action0() {
			@Override
			public void invoke() {
				doPlayTitle();
			}
		};
		clicklabels.add(titleLabel);
		
		creditsLabel = new ClickLabel(120, 345, -400, 14, "credits");
		creditsLabel.action = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
				doPlayCredits();
			}
		};
		clicklabels.add(creditsLabel);
		
		exit = new ClickLabel(120, 380, -400, 20, "mainmenu.exit");
		exit.action = new Action0() { @Override public void invoke() { 
			doExit(); 
		} };
		clicklabels.add(exit);
		
		// Language switcher on the main menu, for convenience
		final ClickLabel toEng = new ClickLabel(550, 400, 20, 14, "EN");
		toEng.disabled = commons.config.language.equals("en");
		final ClickLabel toHu = new ClickLabel(550, 380, 20, 14, "HU");
		toHu.disabled = commons.config.language.equals("hu");
		final ClickLabel toDe = new ClickLabel(550, 360, 20, 14, "DE");
		toDe.disabled = commons.config.language.equals("de");
		
		toDe.action = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
				toEng.disabled = false;
				toDe.disabled = true;
				toHu.disabled = false;
				commons.control().switchLanguage("de");
				selectRandomBackground();
				checkExistingSave();

				askRepaint();
			}
		};
		toEng.action = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
				toEng.disabled = true;
				toHu.disabled = false;
				toDe.disabled = false;
				commons.control().switchLanguage("en");
				selectRandomBackground();
				checkExistingSave();

				askRepaint();
			}
		};
		toHu.action = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
				toEng.disabled = false;
				toHu.disabled = true;
				toDe.disabled = false;
				commons.control().switchLanguage("hu");
				selectRandomBackground();
				checkExistingSave();
				askRepaint();
			}
		};
		
		clicklabels.add(toEng);
		clicklabels.add(toHu);
		clicklabels.add(toDe);
		
		achievements = new UIImageButton(commons.starmap().achievements);
		achievements.onClick = new Action0() {
			@Override
			public void invoke() {
				displaySecondary(Screens.ACHIEVEMENTS);
			}
		};
		
		addThis();
	}

	@Override
	public void onLeave() {
		// TODO Auto-generated method stub

	}
	@Override
	public void onResize() {
		if (background == null) {
			selectRandomBackground();
		}
		
		// relocate objects if necessary
		int w = background.getWidth();
		int h = background.getHeight();
		if (config.scaleAllScreens) {
			Pair<Point, Double> pd = RenderTools.fitWindow(getInnerWidth(), getInnerHeight(), w, h);
			xOrigin = pd.first.x;
			yOrigin = pd.first.y;
		} else {
			xOrigin = (getInnerWidth() - w) / 2;
			yOrigin = (getInnerHeight() - h) / 2;
		}
		
		achievements.location(w - 20 - achievements.width, single.y);

		for (ClickLabel cl : Arrays.asList(single, continueLabel, load, 
				multiplayer, settings, videosLabel, exit, creditsLabel)) {
			cl.x = w / 2 - cl.width / 2;
		}
		introLabel.x = w / 3 - introLabel.width / 2;
		titleLabel.x = w * 2 / 3 - titleLabel.width / 2;
		
	}
	@Override
	public Screens screen() {
		return Screens.MAIN;
	}
	/**
	 * Set the background randomly.
	 */
	protected void selectRandomBackground() {
		background = commons.background().start[rnd.nextInt(commons.background().start.length)];
	}
	/**
	 * Use the given background for the main menu.
	 * @param newBackground the new background
	 */
	public void useBackground(BufferedImage newBackground) {
		if (newBackground == null) {
			throw new IllegalArgumentException("newBackground is null");
		}
		background = newBackground;
		askRepaint();
	}
	@Override
	public void load(String name) {
		commons.control().load(name);
	}
	/** Play the credits. */
	void doPlayCredits() {
		displayPrimary(Screens.CREDITS);
	}
}
