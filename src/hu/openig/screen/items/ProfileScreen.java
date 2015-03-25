/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.model.Profile;
import hu.openig.model.Screens;
import hu.openig.model.SoundType;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIGenericButton;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;
import hu.openig.ui.UIPanel;
import hu.openig.ui.UIScrollBox;
import hu.openig.utils.U;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The profile screen accessible through the main menu.
 * @author akarnokd, 2013.09.02.
 */
public class ProfileScreen extends ScreenBase {
	/** The screen origin. */
	final Rectangle base = new Rectangle(0, 0, 550, 400);
	/** Create new label. */
	UILabel createNew;
	/** The text field with the new name. */
	UILabel newProfileName;
	/** The new name text. */
	String newProfileStr = "";
	/** The existing profiles list label. */
	UILabel existingProfiles;
	/** The scroll box for the profiles. */
	UIScrollBox profileScroll;
	/** The select button. */
	UIGenericButton selectButton;
	/** The back button. */
	UIGenericButton backButton;
	/** The current blink state. */
	boolean blinkState;
	/** The close-token of the blink timer. */
	Closeable blink;
	/** The profiles listing panel. */
	UIPanel profilesListPanel;
	@Override
	public void onInitialize() {
		createNew = new UILabel(get("profiles.create_new"), 14, commons.text());
		newProfileName = new UILabel("", 14, commons.text());
		newProfileName.color(TextRenderer.YELLOW);
		
		
		selectButton = new UIGenericButton(get("profiles.select_button"), fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		selectButton.disabledPattern(commons.common().disabledPattern);
		selectButton.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
				doSelectCreate();
			}
		};
		
		existingProfiles = new UILabel(get("profiles.existing_profiles"), 14, commons.text());
		
		profilesListPanel = new UIPanel();
		profileScroll = new UIScrollBox(profilesListPanel, 30,
				new UIImageButton(commons.database().arrowUp),
				new UIImageButton(commons.database().arrowDown));
		
		backButton = new UIGenericButton(get("profiles.back_button"), fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		backButton.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
				hide();
			}
		};
		
		addThis();
	}

	void hide() {
	    MainScreen ms = (MainScreen)commons.control().getScreen(Screens.MAIN);
	    ms.checkExistingSave();
	    hideSecondary();
	}
	
	@Override
	public void onResize() {
		scaleResize(base, margin());

		createNew.location(base.x + 10, base.y + 10);
		newProfileName.location(base.x + 10, createNew.y + createNew.height + 10);
		newProfileName.width = base.width - 20;
		
		existingProfiles.location(base.x + 10, newProfileName.bottom() + 11);
		
		selectButton.location(base.x + 10 + (base.width / 2 - 10 - selectButton.width) / 2, base.y + base.height - selectButton.height - 10);
		backButton.location(base.x + 10 + base.width / 2 + (base.width / 2 - 10 - selectButton.width) / 2, base.y + base.height - backButton.height - 10);

		profileScroll.location(base.x + 10, existingProfiles.bottom() + 11);
		profileScroll.width = base.width - 20;
		profileScroll.height = selectButton.y - 10 - profileScroll.y;
	}

	@Override
	public void onEnter(Screens mode) {
		onResize();

		blink = commons.register(500, new Action0() {
			@Override
			public void invoke() {
				blinkState = !blinkState;
				updateNewProfile();
			}
		});

		newProfileStr = commons.config.currentProfile;
		updateNewProfile();
		
		updateProfilesList(getProfiles());
		selectProfile(newProfileStr, true);
	}

	@Override
	public void onLeave() {
		U.close(blink);
		blink = null;
	}

	@Override
	public void onFinish() {

	}
	
	@Override
	public void draw(Graphics2D g2) {
		AffineTransform savea = scaleDraw(g2, base, margin());
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);
		
		g2.setColor(new Color(0, 0, 0, 224));
		g2.fill(base);
		
		selectButton.enabled(!newProfileStr.isEmpty());
		
		super.draw(g2);
		
		g2.setColor(Color.YELLOW);
		int m1 = 3;
		g2.drawRect(newProfileName.x - m1, newProfileName.y - m1, newProfileName.width + m1 * 2 - 1, newProfileName.height + m1 * 2 - 1);

		g2.drawRect(profileScroll.x - m1, profileScroll.y - m1, profileScroll.width + m1 * 2 - 1, profileScroll.height + m1 * 2 - 1);

		g2.setTransform(savea);
	}

	@Override
	public Screens screen() {
		return Screens.PROFILE;
	}

	@Override
	public void onEndGame() {
		// TODO Auto-generated method stub

	}
	@Override
	public boolean mouse(UIMouse e) {
		scaleMouse(e, base, margin());
		if (!base.contains(e.x, e.y) && e.has(Type.DOWN)) {
			hide();
			return true;
		}
		return super.mouse(e);
	}
	@Override
	public boolean keyboard(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			hide();
			e.consume();
		} else
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			doSelectCreate();
			e.consume();
		}
		if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
			if (newProfileStr.length() > 0) {
				newProfileStr = newProfileStr.substring(0, newProfileStr.length() - 1);
				updateNewProfile();
				if (newProfileStr.length() > 0) {
					selectProfile(newProfileStr, true);
				} else {
					selectProfile(newProfileStr, false);
				}
			}
			e.consume();
		} else
		if (commons.text().isSupported(e.getKeyChar())) {
			if (newProfileStr.length() < 40) {
				char c = e.getKeyChar();
				if (c == '.' || Character.isJavaIdentifierPart(c)) {
					newProfileStr += c;
					blinkState = true;
					updateNewProfile();
					selectProfile(newProfileStr, true);
				}
			} else {
				buttonSound(SoundType.NOT_AVAILABLE);
			}
			e.consume();
		}
		return super.keyboard(e);
	}
	/**
	 * Update the new profile text.
	 */
	public void updateNewProfile() {
		String s = newProfileStr;
		if (blinkState) {
			newProfileName.text(s + "-");
		} else {
			newProfileName.text(s);
		}
		askRepaint();
	}
	/**
	 * Creates or selects a new profile.
	 */
	void doSelectCreate() {
		if (newProfileStr.isEmpty()) {
			return;
		}
		commons.config.currentProfile = newProfileStr;
		commons.profile = new Profile();
		commons.profile.name = newProfileStr;
		commons.profile.prepare();
		hide();
	}
	/**
	 * Returns a list of the existing profile names.
	 * @return the list of existing profiles.
	 */
	public List<String> getProfiles() {
		List<String> r = new ArrayList<>();
		
		File[] files = new File("save").listFiles();
		
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory() && new File(f, "profile.xml").canRead()) {
					r.add(f.getName());
				}
			}
		}
		
		return r;
	}
	/**
	 * Profile label.
	 * @author akarnokd, 2013.09.02.
	 */
	class ProfileLabel extends UILabel {
		/** The profile name. */
		public final String name;
		/**
		 * Constructor, initializes the profile name.
		 * @param name the profile name
		 */
		public ProfileLabel(String name) {
			super(name, 14, commons.text());
			this.name = name;
		}
		@Override
		public boolean mouse(UIMouse e) {
			if (e.has(Type.DOUBLE_CLICK)) {
				doSelectCreate();
				return true;
			}
			return super.mouse(e);
		}
	}
	/**
	 * Update the list of profiles with the supplied values.
	 * @param profiles the list of profile names
	 */
	public void updateProfilesList(List<String> profiles) {
		profilesListPanel.clear();
		
		
		int y = 0;
		for (final String p : profiles) {
			final ProfileLabel pl = new ProfileLabel(p);
			pl.hoverColor(TextRenderer.YELLOW);
			pl.location(0, y);
			pl.width = profileScroll.width - commons.database().arrowUp[0].getWidth() - 10;
			pl.height = 20;
			
			profilesListPanel.add(pl);
			
			pl.onPress = new Action0() {
				@Override
				public void invoke() {
					selectProfile(p, false);
					newProfileStr = p;
					updateNewProfile();
					profileScroll.scrollToVisible(pl);
					profileScroll.adjustButtons();
				}
			};
			
			y += pl.height;
		}
		profilesListPanel.pack();
		profileScroll.adjustButtons();
		askRepaint();
	}
	/**
	 * Select and deselect profile names.
	 * @param p the profile name
	 * @param first the first that starts with the name?
	 */
	void selectProfile(String p, boolean first) {
		boolean once = true;
		for (UIComponent c : profilesListPanel.components()) {
			if (c instanceof ProfileLabel) {
				ProfileLabel l1 = (ProfileLabel) c;
				boolean highlight;
				if (first) {
					highlight = l1.name.startsWith(p) & once;
					if (highlight) {
						profileScroll.scrollToVisible(l1);
						profileScroll.adjustButtons();
						once = false;
					}
				} else {
					highlight = p.equals(l1.name);
				}
				if (highlight) {
					l1.backgroundColor(TextRenderer.BLUE);
					l1.color(TextRenderer.WHITE);
				} else {
					l1.backgroundColor(0);
					l1.color(TextRenderer.GREEN);
				}
			}
		}
	}
}
