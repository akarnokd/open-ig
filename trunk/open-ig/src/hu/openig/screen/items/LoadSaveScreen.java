/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.core.Difficulty;
import hu.openig.core.Func1;
import hu.openig.core.SaveMode;
import hu.openig.model.FileItem;
import hu.openig.model.Screens;
import hu.openig.model.SoundType;
import hu.openig.model.World;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.UICheckBox;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIGenericButton;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;
import hu.openig.ui.UISpinner;
import hu.openig.utils.XElement;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.xml.stream.XMLStreamException;



/**
 * A load and save game screen.
 * @author akarnokd, 2010.01.11.
 */
public class LoadSaveScreen extends ScreenBase {
	/** Which settings page to display. */
	public enum SettingsPage {
		/** Load/save. */
		LOAD_SAVE,
		/** Audio options. */
		AUDIO,
		/** The control options. */
		CONTROL,
		/** The visual options. */
		VISUAL,
		/** Gameplay options. */
		GAMEPLAY
	}
	/** The fields for the settings screen. */
	@Retention(RetentionPolicy.RUNTIME)
	@interface Settings { 
		/** On which page to display. */
		SettingsPage page();
	}
	/** The panel base rectangle. */
	final Rectangle base = new Rectangle(0, 0, 640, 442);
	/** The random background. */
	final Random rnd = new Random();
	/** The displayed background. */
	BufferedImage background;
	/** The screen to restore when exiting. */
	Screens restore;
	/** Show the settings page? */
	public SettingsPage settingsMode;
	/** Allow saving? */
	private boolean maySave;
	/** Resume the simulation after exiting? */
	boolean resume;
	/** The save button. */
	UIGenericButton loadSavePage;
	/** The save button. */
	UIGenericButton audioPage;
	/** The save button. */
	UIGenericButton controlPage;
	/** The save button. */
	UIGenericButton visualPage;
	/** The save button. */
	UIGenericButton gameplayPage;
	/** The save button. */
	UIGenericButton back;
	/** The save button. */
	@Settings(page = SettingsPage.LOAD_SAVE)
	UIGenericButton save;
	/** The load button. */
	@Settings(page = SettingsPage.LOAD_SAVE)
	UIGenericButton load;
	/** Delete. */
	@Settings(page = SettingsPage.LOAD_SAVE)
	UIGenericButton delete;
	/** Return to main menu. */
	UIGenericButton mainmenu;
	/** The file listing worker. */
	SwingWorker<Void, Void> listWorker;
	/** The file list. */
	@Settings(page = SettingsPage.LOAD_SAVE)
	FileList list;
	/** The sound volume. */
	@Settings(page = SettingsPage.AUDIO)
	UISpinner soundVolume;
	/** The sound label. */
	@Settings(page = SettingsPage.AUDIO)
	UILabel soundLabel;
	/** The music volume. */
	@Settings(page = SettingsPage.AUDIO)
	UISpinner musicVolume;
	/** The music label. */
	@Settings(page = SettingsPage.AUDIO)
	UILabel musicLabel;
	/** The video volume. */
	@Settings(page = SettingsPage.AUDIO)
	UISpinner videoVolume;
	/** The video volume. */
	@Settings(page = SettingsPage.AUDIO)
	UILabel videoLabel;
	/** Subtitles? */
	@Settings(page = SettingsPage.AUDIO)
	UICheckBox subtitles;
	/** Animate technology? */
	@Settings(page = SettingsPage.VISUAL)
	UICheckBox animateTech;
	/** Re-equip tanks? */
	@Settings(page = SettingsPage.GAMEPLAY)
	UICheckBox reequipTanks;
	/** Re-equip bombs? */
	@Settings(page = SettingsPage.GAMEPLAY)
	UICheckBox reequipBombs;
	/** Enable computer voice for screen switches. */
	@Settings(page = SettingsPage.AUDIO)
	UICheckBox computerVoiceScreen;
	/** Enable computer voice for notifications. */
	@Settings(page = SettingsPage.AUDIO)
	UICheckBox computerVoiceNotify;
	/** Auto-build credit limit. */
	@Settings(page = SettingsPage.GAMEPLAY)
	UISpinner autoBuildLimit;
	/** Auto build label. */
	@Settings(page = SettingsPage.GAMEPLAY)
	UILabel autoBuildLabel;
	/** Enable automatic repair. */
	@Settings(page = SettingsPage.GAMEPLAY)
	UICheckBox autoRepair;
	/** Allow button sounds. */
	@Settings(page = SettingsPage.AUDIO)
	UICheckBox buttonSounds;
	/** Play satellite deploy animation? */
	@Settings(page = SettingsPage.VISUAL)
	UICheckBox satelliteDeploy;
	/** Auto-build credit limit. */
	@Settings(page = SettingsPage.GAMEPLAY)
	UISpinner researchMoneyPercent;
	/** Auto build label. */
	@Settings(page = SettingsPage.GAMEPLAY)
	UILabel researchMoneyLabel;
	/** Play satellite deploy animation? */
	@Settings(page = SettingsPage.GAMEPLAY)
	UICheckBox automaticBattle;
	/** Auto build label. */
	@Settings(page = SettingsPage.GAMEPLAY)
	UILabel autoRepairLabel;
	/** Auto-build credit limit. */
	@Settings(page = SettingsPage.GAMEPLAY)
	UISpinner autoRepairLimit;
	/** Display the radar union? */
	@Settings(page = SettingsPage.GAMEPLAY)
	UICheckBox radarUnion;
	/** The save name. */
	@Settings(page = SettingsPage.LOAD_SAVE)
	UILabel saveName;
	/** The save name. */
	@Settings(page = SettingsPage.LOAD_SAVE)
	UILabel saveNameText;
	/** Classic RTS controls? */
	@Settings(page = SettingsPage.CONTROL)
	UICheckBox classicControls;
	/** Swap left-right mouse? */
	@Settings(page = SettingsPage.CONTROL)
	UICheckBox swapLeftRight;
	/** The timer for the blinking cursor. */
	Timer blink;
	/** The currently editing save text. */
	String saveText = "";
	/** The blink phase. */
	boolean blinking;
	/** The other settings popup. */
	UIGenericButton otherSettings;
	/** Slow down time on detected attack. */
	@Settings(page = SettingsPage.GAMEPLAY)
	UICheckBox slowOnAttack;
	/** The time step for the simulation. */
	@Settings(page = SettingsPage.GAMEPLAY)
	UILabel timestepLabel;
	/** The time step for the simulation. */
	@Settings(page = SettingsPage.GAMEPLAY)
	UISpinner timestepValue;
	/** The time step for the simulation. */
	@Settings(page = SettingsPage.VISUAL)
	UILabel uiScaleLabel;
	/** The time step for the simulation. */
	@Settings(page = SettingsPage.VISUAL)
	UISpinner uiScaleValue;
	/** Scale cutscenes. */
	@Settings(page = SettingsPage.VISUAL)
	UICheckBox movieScale;
	/** Allow skipping the movies via mouse click. */
	@Settings(page = SettingsPage.CONTROL)
	UICheckBox movieSkipClick;
	/** Toggle fullscreen. */
	@Settings(page = SettingsPage.VISUAL)
	UICheckBox fullScreen;
	/** Show building names. */
	@Settings(page = SettingsPage.VISUAL)
	UICheckBox buildingName;
	@Override
	public void onInitialize() {
		blink = new Timer(500, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				blinking = !blinking;
				displaySaveText();
				askRepaint();
			}
		});
		
		loadSavePage = new UIGenericButton(get("settings.load_save"), fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		loadSavePage.disabledPattern(commons.common().disabledPattern);
		loadSavePage.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
				setRandomBackground();
				displayPage(SettingsPage.LOAD_SAVE);
			}
		};
		audioPage = new UIGenericButton("   ", fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		audioPage.disabledPattern(commons.common().disabledPattern);
		audioPage.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
				displayPage(SettingsPage.AUDIO);
			}
		};
		audioPage.icon(rl.getImage("settings_audio"));
		
		controlPage = new UIGenericButton("   ", fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		controlPage.disabledPattern(commons.common().disabledPattern);
		controlPage.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
				displayPage(SettingsPage.CONTROL);
			}
		};
		controlPage.icon(rl.getImage("settings_control"));
		visualPage = new UIGenericButton("   ", fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		visualPage.disabledPattern(commons.common().disabledPattern);
		visualPage.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
				displayPage(SettingsPage.VISUAL);
			}
		};
		visualPage.icon(rl.getImage("settings_visual"));

		gameplayPage = new UIGenericButton(get("settings.gameplay"), fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		gameplayPage.disabledPattern(commons.common().disabledPattern);
		gameplayPage.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
				displayPage(SettingsPage.GAMEPLAY);
			}
		};

		back = new UIGenericButton(get("settings.back"), fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		back.disabledPattern(commons.common().disabledPattern);
		back.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
				doBack();
			}
		};

		// ===================================================
		
		save = new UIGenericButton(get("save"), fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		save.disabledPattern(commons.common().disabledPattern);
		save.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
				doSave();
			}
		};
		
		load = new UIGenericButton(get("load"), fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		load.disabledPattern(commons.common().disabledPattern);
		load.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
				doLoad();
			}
		};
		
		mainmenu = new UIGenericButton(get("mainmenu"), fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		mainmenu.disabledPattern(commons.common().disabledPattern);
		mainmenu.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
				doMainMenu();
			}
		};

		otherSettings = new UIGenericButton(get("othersettings"), fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		otherSettings.disabledPattern(commons.common().disabledPattern);
		otherSettings.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
				doOtherSettings();
			}
		};

		
		delete = new UIGenericButton(get("delete"), fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		delete.disabledPattern(commons.common().disabledPattern);
		delete.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
				doDelete();
			}
		};
		
		list = new FileList();

		// -----------------------------------------------------------------------------------------------
		
		final UIImageButton sprev = new UIImageButton(commons.common().moveLeft);
		sprev.setDisabledPattern(commons.common().disabledPattern);
		sprev.setHoldDelay(250);
		final UIImageButton snext = new UIImageButton(commons.common().moveRight);
		snext.setDisabledPattern(commons.common().disabledPattern);
		snext.setHoldDelay(250);

		sprev.onClick = new Action0() {
			@Override
			public void invoke() {
				config.effectVolume = Math.max(0, config.effectVolume - 1);
				effectSound(SoundType.BAR);
				askRepaint(base);
			}
		};
		snext.onClick = new Action0() {
			@Override
			public void invoke() {
				config.effectVolume = Math.min(100, config.effectVolume + 1);
				effectSound(SoundType.BAR);
				askRepaint(base);
			}
		};
		
		soundVolume = new UISpinner(14, sprev, snext, commons.text()) {
			@Override
			public boolean mouse(UIMouse e) {
				if ((e.has(Type.DOWN) || e.has(Type.DRAG))  && e.x > prev.x + prev.width && e.x < next.x) {
					int dw = next.x - prev.x - prev.width - 2;
					int x0 = e.x - prev.x - prev.width;
					config.effectVolume = Math.max(0, Math.min(100, x0 * 100 / dw));
					return true;
				} else
				if ((e.has(Type.UP) && (e.x > prev.x + prev.width && e.x < next.x || config.effectVolume == 100))) {
					effectSound(SoundType.BAR);
					return true;
				}
				return super.mouse(e);
			}
			@Override
			public void draw(Graphics2D g2) {
				super.draw(g2);
				int dw = width - next.width - prev.width - 2;
				int ox = prev.x + prev.width + 1;
				int dx = config.effectVolume * dw / 100;
				g2.setColor(Color.WHITE);
				g2.fillRect(ox + dx, 1, 2, height - 2);
			}
		};
		soundVolume.getValue = new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return config.effectVolume + "%";
			}
		};
		soundLabel = new UILabel(get("settings.sound_volume"), 14, commons.text());
		
		final UIImageButton mprev = new UIImageButton(commons.common().moveLeft);
		mprev.setDisabledPattern(commons.common().disabledPattern);
		mprev.setHoldDelay(250);
		final UIImageButton mnext = new UIImageButton(commons.common().moveRight);
		mnext.setDisabledPattern(commons.common().disabledPattern);
		mnext.setHoldDelay(250);

		mprev.onClick = new Action0() {
			@Override
			public void invoke() {
				config.musicVolume = Math.max(0, config.musicVolume - 1);
				commons.music.setVolume(config.musicVolume);
				if (!commons.music.isRunning()) {
					int e = config.effectVolume;
					config.effectVolume = config.musicVolume;
					effectSound(SoundType.BAR);
					config.effectVolume = e;
				}
				askRepaint(base);
			}
		};
		mnext.onClick = new Action0() {
			@Override
			public void invoke() {
				config.musicVolume = Math.min(100, config.musicVolume + 1);
				commons.music.setVolume(config.musicVolume);
				if (!commons.music.isRunning()) {
					int e = config.effectVolume;
					config.effectVolume = config.musicVolume;
					effectSound(SoundType.BAR);
					config.effectVolume = e;
				}
				askRepaint(base);
			}
		};

		musicVolume = new UISpinner(14, mprev, mnext, commons.text()) {
			@Override
			public boolean mouse(UIMouse e) {
				if ((e.has(Type.DOWN) || e.has(Type.DRAG))  && e.x > prev.x + prev.width && e.x < next.x) {
					int dw = next.x - prev.x - prev.width - 2;
					int x0 = e.x - prev.x - prev.width;
					config.musicVolume = Math.max(0, Math.min(100, x0 * 100 / dw));
					return true;
				} else
				if ((e.has(Type.UP) && (e.x > prev.x + prev.width && e.x < next.x || config.musicVolume == 100))) {
					if (!commons.music.isRunning()) {
						int ev = config.effectVolume;
						config.effectVolume = config.musicVolume;
						effectSound(SoundType.BAR);
						config.effectVolume = ev;
					}
					commons.music.setVolume(config.musicVolume);
					return true;
				}
				return super.mouse(e);
			}
			@Override
			public void draw(Graphics2D g2) {
				super.draw(g2);
				int dw = width - next.width - prev.width - 2;
				int ox = prev.x + prev.width + 1;
				int dx = config.musicVolume * dw / 100;
				g2.setColor(Color.WHITE);
				g2.fillRect(ox + dx, 1, 2, height - 2);
			}
		};
		musicVolume.getValue = new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return config.musicVolume + "%";
			}
		};
		musicLabel = new UILabel(get("settings.music_volume"), 14, commons.text());
		
		final UIImageButton vprev = new UIImageButton(commons.common().moveLeft);
		vprev.setDisabledPattern(commons.common().disabledPattern);
		vprev.setHoldDelay(250);
		vprev.onClick = new Action0() {
			@Override
			public void invoke() {
				config.videoVolume = Math.max(0, config.videoVolume - 1);

				int e = config.effectVolume;
				config.effectVolume = config.videoVolume;
				effectSound(SoundType.BAR);
				config.effectVolume = e;
				
				askRepaint(base);
			}
		};
		final UIImageButton vnext = new UIImageButton(commons.common().moveRight);
		vnext.setDisabledPattern(commons.common().disabledPattern);
		vnext.setHoldDelay(250);
		vnext.onClick = new Action0() {
			@Override
			public void invoke() {
				config.videoVolume = Math.min(100, config.videoVolume + 1);

				int e = config.effectVolume;
				config.effectVolume = config.videoVolume;
				effectSound(SoundType.BAR);
				config.effectVolume = e;

				askRepaint(base);
			}
		};

		videoVolume = new UISpinner(14, vprev, vnext, commons.text()) {
			@Override
			public boolean mouse(UIMouse e) {
				if ((e.has(Type.DOWN) || e.has(Type.DRAG))  && e.x > prev.x + prev.width && e.x < next.x) {
					int dw = next.x - prev.x - prev.width - 2;
					int x0 = e.x - prev.x - prev.width;
					config.videoVolume = Math.max(0, Math.min(100, x0 * 100 / dw));
					return true;
				} else
				if (e.has(Type.UP) && ((e.x > prev.x + prev.width && e.x < next.x) 
							|| config.videoVolume == 100)) {
					int ev = config.effectVolume;
					config.effectVolume = config.videoVolume;
					effectSound(SoundType.BAR);
					config.effectVolume = ev;
					return true;
				}
				return super.mouse(e);
			}
			@Override
			public void draw(Graphics2D g2) {
				super.draw(g2);
				int dw = width - next.width - prev.width - 2;
				int ox = prev.x + prev.width + 1;
				int dx = config.videoVolume * dw / 100;
				g2.setColor(Color.WHITE);
				g2.fillRect(ox + dx, 1, 2, height - 2);
			}
		};
		videoVolume.getValue = new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return config.videoVolume + "%";
			}
		};
		videoLabel = new UILabel(get("settings.video_volume"), 14, commons.text());
		
		subtitles = new UICheckBox(get("settings.subtitles"), 14, commons.common().checkmark, commons.text());
		subtitles.onChange = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				config.subtitles = subtitles.selected();
			}
		};
		animateTech = new UICheckBox(get("settings.animatetech"), 14, commons.common().checkmark, commons.text());
		animateTech.onChange = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				config.animateInventory = animateTech.selected();
			}
		};
		
		reequipTanks = new UICheckBox(get("settings.reequip_tanks"), 14, commons.common().checkmark, commons.text());
		reequipTanks.onChange = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				config.reequipTanks = reequipTanks.selected();
			}
		};
		reequipBombs = new UICheckBox(get("settings.reequip_bombs"), 14, commons.common().checkmark, commons.text());
		reequipBombs.onChange = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				config.reequipBombs = reequipBombs.selected();
			}
		};
		computerVoiceScreen = new UICheckBox(get("settings.computer_voice_screen"), 14, commons.common().checkmark, commons.text());
		computerVoiceScreen.onChange = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				config.computerVoiceScreen = computerVoiceScreen.selected();
			}
		};
		computerVoiceNotify = new UICheckBox(get("settings.computer_voice_notify"), 14, commons.common().checkmark, commons.text());
		computerVoiceNotify.onChange = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				config.computerVoiceNotify = computerVoiceNotify.selected();
			}
		};
	
		final UIImageButton aprev = new UIImageButton(commons.common().moveLeft);
		aprev.setDisabledPattern(commons.common().disabledPattern);
		aprev.setHoldDelay(250);
		final UIImageButton anext = new UIImageButton(commons.common().moveRight);
		anext.setDisabledPattern(commons.common().disabledPattern);
		anext.setHoldDelay(250);
		
		aprev.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_LOW_1);
				config.autoBuildLimit = Math.max(0, config.autoBuildLimit - 5000);
				askRepaint(base);
			}
		};
		anext.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_LOW_1);
				config.autoBuildLimit = Math.min(Integer.MAX_VALUE, config.autoBuildLimit + 5000);
				askRepaint(base);
			}
		};
		
		autoBuildLimit = new UISpinner(14, aprev, anext, commons.text());
		autoBuildLimit.getValue = new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return config.autoBuildLimit + " cr";
			}
		};
		autoBuildLabel = new UILabel(get("settings.autobuild_limit"), 14, commons.text());

		autoRepair = new UICheckBox(get("settings.auto_repair"), 14, commons.common().checkmark, commons.text());
		autoRepair.onChange = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				config.autoRepair = autoRepair.selected();
			}
		};

		buttonSounds = new UICheckBox(get("settings.button_sounds"), 14, commons.common().checkmark, commons.text());
		buttonSounds.onChange = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				config.buttonSounds = buttonSounds.selected();
			}
		};

		satelliteDeploy = new UICheckBox(get("settings.satellite_deplay"), 14, commons.common().checkmark, commons.text());
		satelliteDeploy.onChange = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				config.satelliteDeploy = satelliteDeploy.selected();
			}
		};

		
		final UIImageButton rmprev = new UIImageButton(commons.common().moveLeft);
		rmprev.setDisabledPattern(commons.common().disabledPattern);
		rmprev.setHoldDelay(250);
		final UIImageButton rmnext = new UIImageButton(commons.common().moveRight);
		anext.setDisabledPattern(commons.common().disabledPattern);
		anext.setHoldDelay(250);
		
		rmprev.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				config.researchMoneyPercent = Math.max(125, config.researchMoneyPercent - 125);
				askRepaint(base);
			}
		};
		rmnext.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				config.researchMoneyPercent = Math.min(2000, config.researchMoneyPercent + 125);
				askRepaint(base);
			}
		};
		
		researchMoneyPercent = new UISpinner(14, rmprev, rmnext, commons.text());
		researchMoneyPercent.getValue = new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				StringBuilder b = new StringBuilder();
				b.append(config.researchMoneyPercent / 10);
				b.append(".").append(config.researchMoneyPercent % 10);
				b.append(" %");
				return b.toString();
			}
		};
		researchMoneyLabel = new UILabel(get("settings.research_money_percent"), 14, commons.text());

		automaticBattle = new UICheckBox(get("settings.autobattle"), 14, commons.common().checkmark, commons.text());
		automaticBattle.onChange = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				config.automaticBattle = automaticBattle.selected();
			}
		};
		
		radarUnion = new UICheckBox(get("settings.radarunion"), 14, commons.common().checkmark, commons.text());
		radarUnion.onChange = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				config.radarUnion = radarUnion.selected();
			}
		};

		// ------------------------------------------------------------------
		
		final UIImageButton arprev = new UIImageButton(commons.common().moveLeft);
		arprev.setDisabledPattern(commons.common().disabledPattern);
		arprev.setHoldDelay(250);
		final UIImageButton arnext = new UIImageButton(commons.common().moveRight);
		arnext.setDisabledPattern(commons.common().disabledPattern);
		arnext.setHoldDelay(250);
		
		arprev.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_LOW_1);
				config.autoRepairLimit = Math.max(0, config.autoRepairLimit - 5000);
				askRepaint(base);
			}
		};
		arnext.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_LOW_1);
				config.autoRepairLimit = Math.min(Integer.MAX_VALUE, config.autoRepairLimit + 5000);
				askRepaint(base);
			}
		};
		
		autoRepairLimit = new UISpinner(14, arprev, arnext, commons.text());
		autoRepairLimit.getValue = new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return config.autoRepairLimit + " cr";
			}
		};
		autoRepairLabel = new UILabel(get("settings.auto_repair_limit"), 14, commons.text());

		saveName = new UILabel(get("settings.save_name"), 14, commons.text());
		
		saveNameText = new UILabel("", 14, commons.text());
		saveNameText.color(TextRenderer.YELLOW);
//		saveNameText.backgroundColor(0xC0000000);

		
		classicControls = new UICheckBox(get("settings.classic_controls"), 14, commons.common().checkmark, commons.text());
		classicControls.onChange = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				config.classicControls = classicControls.selected();
			}
		};

		swapLeftRight = new UICheckBox(get("settings.swap_mouse_buttons"), 14, commons.common().checkmark, commons.text());
		swapLeftRight.onChange = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				config.swapMouseButtons = swapLeftRight.selected();
			}
		};
		
		slowOnAttack = new UICheckBox(get("settings.slow_on_attack"), 14, commons.common().checkmark, commons.text());
		slowOnAttack.onChange = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				config.slowOnEnemyAttack = slowOnAttack.selected();
			}
		};
		
		// ------------------------------------------
		
		final UIImageButton tsprev = new UIImageButton(commons.common().moveLeft);
		tsprev.setDisabledPattern(commons.common().disabledPattern);
		tsprev.setHoldDelay(250);
		final UIImageButton tsnext = new UIImageButton(commons.common().moveRight);
		tsnext.setDisabledPattern(commons.common().disabledPattern);
		tsnext.setHoldDelay(250);
		
		tsprev.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_LOW_1);
				config.timestep = Math.max(1, config.timestep - 1);
				askRepaint(base);
			}
		};
		tsnext.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_LOW_1);
				config.timestep = Math.min(60, config.timestep + 1);
				askRepaint(base);
			}
		};
		
		timestepValue = new UISpinner(14, tsprev, tsnext, commons.text()) {
			@Override
			public boolean mouse(UIMouse e) {
				if ((e.has(Type.DOWN) || e.has(Type.DRAG))  && e.x > prev.x + prev.width && e.x < next.x) {
					int dw = next.x - prev.x - prev.width - 2;
					int x0 = e.x - prev.x - prev.width;
					config.timestep = Math.max(1, Math.min(100, 1 + x0 * 59 / dw));
					return true;
				} else
				if ((e.has(Type.UP) && (e.x > prev.x + prev.width && e.x < next.x || config.timestep == 60))) {
					return true;
				}
				return super.mouse(e);
			}
			@Override
			public void draw(Graphics2D g2) {
				super.draw(g2);
				int dw = width - next.width - prev.width - 2;
				int ox = prev.x + prev.width + 1;
				int dx = (config.timestep - 1) * dw / 59;
				g2.setColor(Color.WHITE);
				g2.fillRect(ox + dx, 1, 2, height - 2);
			}
		};
		timestepValue.getValue = new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return format("settings.base_speed_value", config.timestep);
			}
		};
		timestepLabel = new UILabel(get("settings.base_speed"), 14, commons.text());

		// ------------------------------------------
		
		prepareUIScale();

		movieScale = new UICheckBox(get("settings.movie_scale"), 14, commons.common().checkmark, commons.text());
		movieScale.onChange = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				config.movieScale = movieScale.selected();
			}
		};

		// ------------------------------------------
		
		movieSkipClick = new UICheckBox(get("settings.movie_click_skip"), 14, commons.common().checkmark, commons.text());
		movieSkipClick.onChange = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				config.movieClickSkip = movieSkipClick.selected();
			}
		};
		
		// ------------------------------------------
		fullScreen = new UICheckBox(get("settings.fullscreen"), 14, commons.common().checkmark, commons.text());
		fullScreen.onChange = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				commons.control().setFullscreen(fullScreen.selected());
			}
		};
		
		// ------------------------------------------
		buildingName = new UICheckBox(get("settings.building_name"), 14, commons.common().checkmark, commons.text());
		buildingName.onChange = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				config.showBuildingName = buildingName.selected();
			}
		};
		
		addThis();
	}
	/**
	 * Prepare the UI scale controls.
	 */
	void prepareUIScale() {
		final UIImageButton scprev = new UIImageButton(commons.common().moveLeft);
		scprev.setDisabledPattern(commons.common().disabledPattern);
		scprev.setHoldDelay(250);
		final UIImageButton scnext = new UIImageButton(commons.common().moveRight);
		scnext.setDisabledPattern(commons.common().disabledPattern);
		scnext.setHoldDelay(250);
		
		scprev.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_LOW_1);
				config.uiScale = Math.max(100, config.uiScale - 25);
				askRepaint();
			}
		};
		scnext.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_LOW_1);
				config.uiScale = Math.min(400, config.uiScale + 25);
				askRepaint();
			}
		};
		
		uiScaleValue = new UISpinner(14, scprev, scnext, commons.text());
		uiScaleValue.getValue = new Func1<Void, String>() {
			@Override
			public String invoke(Void value) {
				return config.uiScale + "%";
			}
		};
		uiScaleLabel = new UILabel(get("settings.ui_scale"), 14, commons.text());

	}
	/**
	 * Show the controls of the given page and hide the rest.
	 * @param page the settings page controls to display
	 */
	public void displayPage(SettingsPage page) {
		for (Field f : getClass().getDeclaredFields()) {
			if (f.isAnnotationPresent(Settings.class) && UIComponent.class.isAssignableFrom(f.getType())) {
				Settings s = f.getAnnotation(Settings.class);
				try {
					UIComponent.class.cast(f.get(this)).visible(s.page() == page);
				} catch (IllegalAccessException ex) {
					ex.printStackTrace();
				}
			}
		}
		settingsMode = page;
	}
	@Override
	public void onEnter(Screens mode) {
		restore = mode;
		setRandomBackground();
		settingsMode = SettingsPage.LOAD_SAVE;
		if (commons.simulation != null) {
			resume = commons.world() != null && !commons.simulation.paused();
			commons.simulation.pause();
		} else {
			resume = false;
		}
		commons.nongame = true;
		commons.worldLoading = true;
		list.items.clear();
		list.selected = null;
		
		startWorker();
		
		reequipTanks.selected(config.reequipTanks);
		reequipBombs.selected(config.reequipBombs);
		computerVoiceScreen.selected(config.computerVoiceScreen);
		computerVoiceNotify.selected(config.computerVoiceNotify);
		autoRepair.selected(config.autoRepair);
		buttonSounds.selected(config.buttonSounds);
		satelliteDeploy.selected(config.satelliteDeploy);
		automaticBattle.selected(config.automaticBattle);
		radarUnion.selected(config.radarUnion);
		classicControls.selected(config.classicControls);
		swapLeftRight.selected(config.swapMouseButtons);
		subtitles.selected(config.subtitles);
		animateTech.selected(config.animateInventory);
		slowOnAttack.selected(config.slowOnEnemyAttack);
		movieScale.selected(config.movieScale);
		movieSkipClick.selected(config.movieClickSkip);
		fullScreen.selected(commons.control().isFullscreen());
		buildingName.selected(config.showBuildingName);
	}
	/**
	 * Choose a random background for the options.
	 */
	void setRandomBackground() {
		background = commons.background().options[rnd.nextInt(commons.background().options.length)];
	}

	@Override
	public void onLeave() {
		blink.stop();
		commons.nongame = false;
		commons.worldLoading = false;
		if (listWorker != null) {
			listWorker.cancel(true);
			listWorker = null;
		}
		// save only if no game is active
		if (commons.world() == null) {
			config.save();
			MainScreen ms = commons.control().getScreen(Screens.MAIN);
			ms.checkExistingSave();
		}
				
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResize() {
		RenderTools.centerScreen(base, getInnerWidth(), getInnerHeight(), true);

		// tabs
		
		loadSavePage.location(base.x + 10, base.y + 10);
		audioPage.location(loadSavePage.x + 10 + loadSavePage.width, base.y + 10);
		controlPage.location(audioPage.x + 10 + audioPage.width, base.y + 10);
		visualPage.location(controlPage.x + 10 + controlPage.width, base.y + 10);
		gameplayPage.location(visualPage.x + 10 + visualPage.width , base.y + 10);
		if (world() != null) {
			back.location(gameplayPage.x + 10 + gameplayPage.width, base.y + 10);
			mainmenu.location(base.x + base.width - 10 - mainmenu.width, base.y + 10);
		} else {
			otherSettings.location(gameplayPage.x + 10 + gameplayPage.width, base.y + 10);
			back.location(base.x + base.width - 10 - back.width, base.y + 10);
		}
		
		// ------------------------------------------------------------
		// load/save
		
		load.location(base.x + 30, loadSavePage.y + loadSavePage.height + 10);
		save.location(base.x + 40 + load.width, load.y);
		delete.location(base.x + base.width - delete.width - 10, load.y);
		
		list.location(base.x + 10, load.y + load.height + 30);
		list.size(base.width - 20, base.height - list.y + base.y - 6);
		
		saveName.location(base.x + 10, load.y + load.height + 8);
		saveNameText.location(saveName.x + saveName.width + 10, saveName.y);
		saveNameText.size(base.width - saveName.width - 30, 14);
		// audio
		
		soundLabel.location(base.x + 30, base.y + 70 + 8);
		musicLabel.location(base.x + 30, base.y + 100 + 8);
		videoLabel.location(base.x + 30, base.y + 130 + 8);
		
		int vol = Math.max(soundLabel.width, Math.max(musicLabel.width, videoLabel.width));
		
		soundVolume.location(base.x + 50 + vol, base.y + 70);
		soundVolume.width = 160;
		musicVolume.location(base.x + 50 + vol, base.y + 100);
		musicVolume.width = 160;
		videoVolume.location(base.x + 50 + vol, base.y + 130);
		videoVolume.width = 160;

		computerVoiceScreen.location(base.x + 30, base.y + 160 + 8);
		computerVoiceNotify.location(base.x + 30, base.y + 190 + 8);

		buttonSounds.location(base.x + 30, base.y + 220 + 8);
		subtitles.location(base.x + 30, base.y + 250 + 8);

		// ----------------------------------------------------------
		// visual
		int dy = 70;
		uiScaleLabel.location(base.x + 30, base.y + dy + 8);
		uiScaleValue.location(base.x + 30 + uiScaleLabel.width + 30, base.y + dy);
		uiScaleValue.width = 160;
		dy += 30;
		movieScale.location(base.x + 30, base.y + dy + 8);
		dy += 30;
		satelliteDeploy.location(base.x + 30, base.y + dy + 8);
		dy += 30;
		animateTech.location(base.x + 30, base.y + dy + 8);
		dy += 30;
		fullScreen.location(base.x + 30, base.y + dy + 8);
		dy += 30;
		buildingName.location(base.x + 30, base.y + dy + 8);
		
		// -----------------------------
		// controls
		dy = 70;
		classicControls.location(base.x + 30, base.y + dy + 8);
		dy += 30;
		swapLeftRight.location(base.x + 30, base.y + dy + 8);
		dy += 30;
		movieSkipClick.location(base.x + 30, base.y + dy + 8);

		// --------------------------------------------------------------------------------------
		// gameplay

		dy = 60;
		
		reequipTanks.location(base.x + 30, base.y + dy + 8);
		dy += 30;
		reequipBombs.location(base.x + 30, base.y + dy + 8);
		dy += 30;
		
		autoRepair.location(base.x + 30, base.y + dy + 8);
		dy += 30;

		autoRepairLabel.location(base.x + 30, base.y + dy + 8);
		autoRepairLimit.width = 200;
		autoRepairLimit.location(base.x + base.width - autoRepairLimit.width - 30, base.y + dy);
		dy += 30;
		
		autoBuildLabel.location(base.x + 30, base.y + dy + 8);
		autoBuildLimit.width = 200;
		autoBuildLimit.location(base.x + base.width - autoBuildLimit.width - 30, base.y + dy);
		dy += 30;

		researchMoneyLabel.location(base.x + 30, base.y + dy + 8);
		researchMoneyPercent.width = 200;
		researchMoneyPercent.location(base.x + base.width - researchMoneyPercent.width - 30, base.y + dy);
		dy += 30;

		automaticBattle.location(base.x + 30, base.y + dy + 8);
		dy += 30;
		radarUnion.location(base.x + 30, base.y + dy + 8);
		dy += 30;
		slowOnAttack.location(base.x + 30, base.y + dy + 8);
		dy += 30;
		
		timestepLabel.location(base.x + 30, base.y + dy + 8);
		timestepValue.width = 250;
		timestepValue.location(base.x + base.width - timestepValue.width - 30, base.y + dy);
		dy += 30;
	}
	@Override
	public Screens screen() {
		return Screens.LOAD_SAVE;
	}
	@Override
	public void onEndGame() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void draw(Graphics2D g2) {
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);

		if (settingsMode != SettingsPage.LOAD_SAVE) {
			g2.drawImage(commons.background().setup, base.x, base.y, null);

//			g2.setColor(new Color(0, 0, 0, 192));
//			g2.fillRect(base.x + 10, base.y + 50, base.width - 20, base.height - 60);

			soundVolume.prev.enabled(config.effectVolume > 0);
			soundVolume.next.enabled(config.effectVolume < 100);

			musicVolume.prev.enabled(config.musicVolume > 0);
			musicVolume.next.enabled(config.musicVolume < 100);

			videoVolume.prev.enabled(config.videoVolume > 0);
			videoVolume.next.enabled(config.videoVolume < 100);

			autoBuildLimit.prev.enabled(config.autoBuildLimit > 0);
			autoBuildLimit.next.enabled(config.autoBuildLimit < Integer.MAX_VALUE);
			
			researchMoneyPercent.prev.enabled(config.researchMoneyPercent > 125);
			researchMoneyPercent.next.enabled(config.researchMoneyPercent < 2000);

			autoRepairLimit.prev.enabled(config.autoRepairLimit > 0);
			
			timestepValue.prev.enabled(config.timestep > 1);
			timestepValue.next.enabled(config.timestep < 60);
			
		} else {
			g2.drawImage(background, base.x, base.y, null);

			g2.setColor(new Color(0, 0, 0, 192));
			g2.fillRect(base.x + 10, base.y + 115, base.width - 20, base.height - 115 - 10);

			save.enabled(maySave);
			load.enabled(list.selected != null);
			delete.enabled(list.selected != null);
		}
		if (settingsMode == SettingsPage.LOAD_SAVE) {
			loadSavePage.color(0xFFFFFFFF);
		} else {
			loadSavePage.color(0xFF000000);
		}
		if (settingsMode == SettingsPage.AUDIO) {
			audioPage.color(0xFFFFFFFF);
		} else {
			audioPage.color(0xFF000000);
		}
		if (settingsMode == SettingsPage.GAMEPLAY) {
			gameplayPage.color(0xFFFFFFFF);
		} else {
			gameplayPage.color(0xFF000000);
		}
		mainmenu.visible(world() != null);
		otherSettings.visible(world() == null);
		
		super.draw(g2);
	}
	@Override
	public void drawComponent(Graphics2D g2, UIComponent c) {
		if (c instanceof UILabel) {
			g2.setColor(new Color(0, 0, 0, 192));
			int px = c.x;
			int py = c.y;
			g2.translate(px, py);
			g2.fillRect(-5, -5, c.width + 10, c.height + 10);
			g2.translate(-px, -py);
		}
		super.drawComponent(g2, c);
	}
	@Override
	public boolean mouse(UIMouse e) {
		if (e.has(Type.DOWN) && !e.within(base.x, base.y, base.width, base.height)) {
			doBack();
		}
		return super.mouse(e);
	}
	@Override
	public boolean keyboard(KeyEvent e) {
		int code = e.getKeyCode();
		char chr = e.getKeyChar();
		if (code == KeyEvent.VK_ESCAPE) {
			doBack();
			e.consume();
		} else {
			if ((chr == 'x' || chr == 'X') && settingsMode == SettingsPage.VISUAL) {
				config.uiScale = 100;
				e.consume();
				askRepaint();
			} else
			if (maySave && settingsMode == SettingsPage.LOAD_SAVE) {
				if (code == KeyEvent.VK_ENTER) {
					doSave();
					e.consume();
					return true;
				} else
				if (code == KeyEvent.VK_BACK_SPACE) {
					if (saveText.length() > 0) {
						saveText = saveText.substring(0, saveText.length() - 1);
					}
					displaySaveText();
					e.consume();
					return true;
				} else
				if (commons.text().isSupported(chr)) {
					saveText += chr;
					displaySaveText();
					e.consume();
					return true;
				}
			} else {
				if (chr == 'o' || chr == 'O') {
					doBack();
					e.consume();
				} else
				if (chr == ' ' 
						|| chr == '1'
						|| chr == '2'
						|| chr == '3'
					) {
						e.consume();
					}
			}
		}
		return super.keyboard(e);
	}
	/** Start the list population worker. */
	void startWorker() {
		listWorker = new SwingWorker<Void, Void>() {
			final List<FileItem> flist = new ArrayList<FileItem>();
			@Override
			protected Void doInBackground() throws Exception {
				try {
					findSaves(flist);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return null;
			}
			@Override
			protected void done() {
				listWorker = null;
				list.items.addAll(flist);
				askRepaint();
			};
		};
		listWorker.execute();
	}
	/** The file list. */
	class FileList extends UIContainer {
		/** The scroll top. */
		int top;
		/** The selected row. */
		FileItem selected;
		/** The file items. */
		final List<FileItem> items = new ArrayList<FileItem>();
		/** The row height. */
		int rowHeight = 18;
		/** The text height. */
		int textHeight = 10;
		@Override
		public void draw(Graphics2D g2) {
//			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			dateFormat2.setCalendar(new GregorianCalendar(TimeZone.getTimeZone("GMT")));
			
			if (listWorker != null && !listWorker.isDone()) {
				commons.text().paintTo(g2, 0, 0, 14, TextRenderer.GRAY, get("loading") + "...");
			} else {
//				Composite save0 = g2.getComposite();

//				g2.setComposite(AlphaComposite.SrcOver.derive(0.75f));
//				g2.setColor(Color.BLACK);
//				g2.fillRect(0, 0, width, height);
//				g2.setComposite(save0);

				int rows = height / rowHeight;
				if (top < 0 || items.size() == 0) {
					top = 0;
				}
				if (items.size() == 0) {
					commons.text().paintTo(g2, 0, 0, 14, TextRenderer.GRAY, get("no_saves"));
				} else {
					int y = 0;
					for (int i = top; i < top + rows && i < items.size(); i++) {
						FileItem fi = items.get(i);
						
						int c = TextRenderer.GREEN;
						if (fi == selected) {
							c = TextRenderer.YELLOW;
						}
						
						int dh = (rowHeight - textHeight) / 2;
						
						
						paintText(g2, 5, y + dh, c, fi.saveName);
						paintText(g2, 5 + 250, y + dh, c, get(fi.difficulty.label) + "-" + fi.level);
//						commons.text().paintTo(g2, 5 + 300, y + dh, textHeight, c, "" + fi.level);
						paintText(g2, 5 + 330, y + dh, c, dateFormat2.format(fi.gameDate));
						String m = fi.money + " cr";
						paintText(g2, width - 5 - commons.text().getTextWidth(10, m), y + dh, c, m);
						y += rowHeight;
					}
				}
			}
		}
		/**
		 * Paint the text with the given color.
		 * @param g2 the graphics context
		 * @param x the X coordinate
		 * @param y the Y coordinate
		 * @param c the color
		 * @param s the text
		 */
		void paintText(Graphics2D g2, int x, int y, int c, String s) {
//			int tw = commons.text().getTextWidth(textHeight, s);

//			Composite save0 = g2.getComposite();
//			g2.setComposite(AlphaComposite.SrcOver.derive(0.75f));
//			g2.setColor(Color.BLACK);
//			g2.fillRect(x - 2, y - 2, tw + 4, textHeight + 4);
//			g2.setComposite(save0);

			commons.text().paintTo(g2, x, y, textHeight, c, s);
		}
		
		@Override
		public boolean mouse(UIMouse e) {
			if (e.has(Type.DOWN)) {
				int idx = top + e.y / rowHeight;
				if (idx >= 0 && idx < items.size()) {
					selected = items.get(idx);
					buttonSound(SoundType.CLICK_MEDIUM_2);
					return true;
				}
			} else
			if (e.has(Type.DOUBLE_CLICK)) {
				int idx = top + e.y / rowHeight;
				if (idx >= 0 && idx < items.size()) {
					selected = items.get(idx);
					buttonSound(SoundType.CLICK_MEDIUM_2);
					doLoad();
					return true;
				}
			} else
			if (e.has(Type.WHEEL)) {
				if (e.z < 0) {
					doScrollUp();
					doScrollUp();
					doScrollUp();
				} else {
					doScrollDown();
					doScrollDown();
					doScrollDown();
				}
				return true;
			}
			return super.mouse(e);
		}
		/** Scroll up. */
		void doScrollUp() {
			top = Math.max(0, top - 1);
		}
		/** Scroll down. */
		void doScrollDown() {
			int rows = height / rowHeight;
			top = Math.max(0, Math.min(top + 1, items.size() - rows));
		}
	}
	/** Go back to main menu. */
	void doMainMenu() {
		commons.control().hideOptions();
		displayPrimary(Screens.MAIN);
		if (commons.world() != null) {
			commons.control().endGame();
		}
	}
	/** Delete the current selected item. */
	void doDelete() {
		int idx = list.items.indexOf(list.selected);
		if (idx >= 0) {
			
			File f = list.selected.file;
			if (f.exists() && !f.delete()) {
				System.err.println("Could not delete " + f);
			}
			String fsname = list.selected.file.getName().substring(5);
			f = new File("save/" + commons.profile.name + "/savex-" + fsname);
			if (f.exists() && !f.delete()) {
				System.err.println("Could not delete " + f);
			}
			f = new File("save/" + commons.profile.name + "/info-" + fsname);
			if (f.exists() && !f.delete()) {
				System.err.println("Could not delete " + f);
			}
			list.items.remove(idx);
			
			idx = Math.min(idx, list.items.size() - 1);
			if (idx >= 0) {
				list.selected = list.items.get(idx);
			} else {
				list.selected = null;
			}
		}
	}
	/** Load the selected item. */
	void doLoad() {
		doBack();
		load(list.selected.file.getAbsolutePath());
	}
	/** Create a save. */
	void doSave() {
		commons.control().save(saveText, SaveMode.MANUAL);
		doBack();
	}
	/** Return to the previous screen. */
	void doBack() {
		commons.control().hideOptions();
		if (resume) {
			commons.simulation.resume();
		}
	}
	/**
	 * Change the saveability settings.
	 * @param value the value
	 */
	public void maySave(boolean value) {
		this.maySave = value;
		saveNameText.text(saveText, false);
		if (maySave) {
			blink.start();
			saveNameText.color(TextRenderer.YELLOW);
		} else {
			blink.stop();
			saveNameText.color(0xFFC0C0C0);
		}
	}
	/** Display the save text and any cursor. */
	void displaySaveText() {
		if (blinking) {
			saveNameText.text(saveText + "-", false);
		} else {
			saveNameText.text(saveText, false);
		}
	}
	/**
	 * Find saves in the profile directory.
	 * @param saves the list of ordered saves
	 */
	void findSaves(List<FileItem> saves) {
		try {
			commons.saving.await();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		// check if save dir exists
		File dir = new File("save/" + commons.profile.name);
		if (!dir.exists()) {
			return;
		}
		
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("save-") || name.startsWith("savex-") || name.startsWith("info-");
			}
		});
		if (files == null) {
			return;
		}
		Set<String> saveSet = new HashSet<String>();
		for (File f : files) {
			String n = f.getName();
			if (n.startsWith("savex-")) {
				if (!f.delete()) {
					System.err.println("Warning: Could not delete file " + f);
				}
				continue;
			}
			
			saveSet.add(n.substring(5, n.length() - 4));
			
		}
		
		Deque<String> queue = new LinkedList<String>(saveSet);

		SimpleDateFormat sdf0 = new SimpleDateFormat("MM-dd HH:mm:ss");

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		sdf.setCalendar(new GregorianCalendar(TimeZone.getTimeZone("GMT")));
		
		while (!queue.isEmpty()) {
			String s = queue.removeFirst();
			File info = new File(dir, "info-" + s + ".xml");
			File save = new File(dir, "save-" + s + ".xml");
			if (info.canRead()) {
				// if no associated save, delete the info
				if (!save.canRead()) {
					if (!info.delete()) {
						System.err.println("Warning: Could not delete file " + info);
					}
					continue;
				}
				// load world info
				try {
					XElement xml = XElement.parseXML(info.getAbsolutePath());
					
					FileItem fi = new FileItem(save);
					fi.saveDate = new Date(save.lastModified());

					fi.saveName = xml.get("save-name", null);
					if (fi.saveName == null) {
						SaveMode sm = SaveMode.valueOf(xml.get("save-mode", SaveMode.AUTO.toString()));
						if (sm == SaveMode.QUICK) {
							fi.saveName = get("quicksave");
						} else {
							fi.saveName = get("autosave");
						}
						fi.saveName += " (" + sdf0.format(fi.saveDate) + ")";
					}
					try {
						fi.gameDate = sdf.parse(xml.get("time"));
					} catch (ParseException ex) {
						ex.printStackTrace();
						fi.gameDate = new Date();
					}
					fi.level = xml.getInt("level", 5);
					fi.money = xml.getLong("money", 0);
					fi.difficulty = Difficulty.valueOf(xml.get("difficulty", Difficulty.NORMAL.toString()));
					
					saves.add(fi);
				} catch (XMLStreamException ex) {
					ex.printStackTrace();
				}
			} else 
			if (save.canRead()) {
				try {
					XElement xml = XElement.parseXML(save.getAbsolutePath());
					// create a info and retry
					World.deriveShortWorldState(xml).save(info.getAbsolutePath());
					// retry
					queue.addFirst(s);
				} catch (IOException ex) {
					ex.printStackTrace();
				} catch (XMLStreamException ex) {
					ex.printStackTrace();
				}
			}
		}
		
		Collections.sort(saves, new Comparator<FileItem>() {
			@Override
			public int compare(FileItem o1, FileItem o2) {
				return o2.saveDate.compareTo(o1.saveDate);
			}
		});
	}
	/** Display the other settings dialog. */
	void doOtherSettings() {
		JComponent c = commons.control().renderingComponent();
		Container cont = c.getParent();
		while (cont != null && !(cont instanceof JFrame)) {
			cont = cont.getParent();
		}
		OtherSettingsDialog f = new OtherSettingsDialog(
				(JFrame)cont, commons.labels(), commons.config, commons.background().setup);
		f.setLocationRelativeTo(c);
		f.setVisible(true);
	}
}
