/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig;

import hu.openig.ani.MovieSurface;
import hu.openig.ani.Player;
import hu.openig.core.BtnAction;
import hu.openig.core.GameSpeed;
import hu.openig.core.ImageInterpolation;
import hu.openig.core.InfoScreen;
import hu.openig.core.PlayerType;
import hu.openig.core.ScreenLayerer;
import hu.openig.core.StarmapSelection;
import hu.openig.model.GameBuilding;
import hu.openig.model.GameBuildingPrototype;
import hu.openig.model.GameFleet;
import hu.openig.model.GamePlanet;
import hu.openig.model.GamePlayer;
import hu.openig.model.GameRace;
import hu.openig.model.GameWorld;
import hu.openig.model.ResearchTech;
import hu.openig.music.Music;
import hu.openig.render.AchievementRenderer;
import hu.openig.render.InfobarRenderer;
import hu.openig.render.InformationRenderer;
import hu.openig.render.MainmenuRenderer;
import hu.openig.render.OptionsRenderer;
import hu.openig.render.PlanetRenderer;
import hu.openig.render.StarmapRenderer;
import hu.openig.res.GameResourceManager;
import hu.openig.sound.SoundFXPlayer;
import hu.openig.utils.IOUtils;
import hu.openig.utils.JavaUtils;
import hu.openig.utils.ResourceMapper;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * The main entry point for now.
 * @author karnokd
 */
public class Main extends JFrame {
	/** */
	private static final long serialVersionUID = 6922932910697940684L;
	/** Version string. */
	public static final String VERSION = "0.69 Alpha"; // TODO reach 1.0!
	/** The game resource manager. */
	GameResourceManager grm;
	/** The user interface sounds. */
	SoundFXPlayer uiSounds;
	/** The starmap renderer. */
	StarmapRenderer starmapRenderer;
	/** The planet surface renderer. */
	PlanetRenderer planetRenderer;
	/** The information screen renderer. */
	InformationRenderer informationRenderer;
	/** The main menu renderer. */
	MainmenuRenderer mainmenuRenderer;
	/** The full screen movie surface. */
	MovieSurface moviePlayer;
	/** Timer for fade in-out animations. */
	Timer fadeTimer;
	/** The fade timer firing interval in milliseconds. */
	static final int FADE_TIME = 50;
	/** The panel used for screen switching. */
	JLayeredPane layers;
	/** The array of screens. */
	JComponent[] screens;
	/** The animation player. */
	Player player;
	/** The music player. */
	Music music;
	/** Set to true if the ESC is pressed while a full screen playback is in progress. */
	private boolean playbackCancelled;
	/** The executor service for parallel operations. */
	public ExecutorService exec;
	/** The options screen renderer. */
	OptionsRenderer optionsRenderer;
	/** The program is currently in Game mode. */
	boolean inGame;
	/** The resource file mapper. */
	private ResourceMapper resMap;
	/** The current game world. */
	private GameWorld gameWorld;
	/** The information bar renderer. */
	private InfobarRenderer infobarRenderer;
	/** The language of the used resources. */
	private String language;
	/** The achievement renderer. */
	private AchievementRenderer achievementRenderer;
	/** The periodic screen refresh timer to fix some anomalies. 
	 * This is the only timed operation which is allowed to fire repaint
	 * events. The other timers are simply changing the state and leaving this timer to
	 * repaint.
	 */
	private Timer screenRefreshTimer;
	/** The main screen refresh time. */
	private static final int SCREEN_REFRESH_TIME = 2500;
	/**
	 * Initialize resources from the given root directory.
	 * @param resMap the resource mapper
	 * @param language the resource language code
	 */
	protected void initialize(final ResourceMapper resMap, final String language) {
		//setUndecorated(true);
		this.language = language;
		setTitle("Open Imperium Galactica (" + VERSION + ")");
		setBackground(Color.BLACK);
		fadeTimer = new Timer(FADE_TIME, null);
		
		exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		this.resMap = resMap;
		music = new Music(resMap);

		grm = new GameResourceManager(resMap);
		uiSounds = new SoundFXPlayer(grm);
		
		gameWorld = new GameWorld();
		// disable controls
		gameWorld.showTime = false;
		gameWorld.showSpeedControls = false;
		
		
		// initialize renderers
		achievementRenderer = new AchievementRenderer(grm, uiSounds);
		infobarRenderer = new InfobarRenderer(grm);
		
		starmapRenderer = new StarmapRenderer(grm, uiSounds, infobarRenderer, achievementRenderer);
		achievementRenderer.add(starmapRenderer);
		planetRenderer = new PlanetRenderer(grm, uiSounds, infobarRenderer, achievementRenderer);
		achievementRenderer.add(planetRenderer);
		informationRenderer = new InformationRenderer(grm, uiSounds, infobarRenderer, achievementRenderer);
		achievementRenderer.add(informationRenderer);
		optionsRenderer = new OptionsRenderer(grm, uiSounds, infobarRenderer, achievementRenderer);
		achievementRenderer.add(optionsRenderer);

		mainmenuRenderer = new MainmenuRenderer(grm);
		mainmenuRenderer.setOpaque(true);
		mainmenuRenderer.setBackground(Color.BLACK);
		moviePlayer = new MovieSurface();
		moviePlayer.setOpaque(true);
		moviePlayer.setBackground(Color.BLACK);
		optionsRenderer.setVisible(false);
		player = new Player(moviePlayer);
		
		player.setMasterGain(0);
		uiSounds.setMasterGain(0);
		optionsRenderer.setAudioVolume(0); // TODO fix during testing
		optionsRenderer.setMusicVolume(0); // TODO fix during testing
		music.setMute(true);
		uiSounds.setMute(true);
		
		screens = new JComponent[] {
			starmapRenderer, planetRenderer, informationRenderer, mainmenuRenderer, moviePlayer, optionsRenderer
		};
		achievementRenderer.setScreenLayerer(new ScreenLayerer() { 
			@Override
			public boolean isTopScreen(JComponent component) {
				for (int i = screens.length - 1; i >= 0; i--) {
					if (screens[i].isVisible()) {
						return screens[i] == component;
					}
				}
				return false;
			} 
		});
		// setup renderers
		mainmenuRenderer.setVisible(true);
		mainmenuRenderer.setVersion(VERSION);
		mainmenuRenderer.setRandomPicture();
		starmapRenderer.setVisible(false);
		informationRenderer.setVisible(false);
		planetRenderer.setVisible(false);
		moviePlayer.setVisible(false);
		
		infobarRenderer.setGameWorld(gameWorld);
		starmapRenderer.setGameWorld(gameWorld);
		informationRenderer.setGameWorld(gameWorld);
		planetRenderer.setGameWorld(gameWorld);
		optionsRenderer.setGameWorld(gameWorld);
		
		setListeners();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				doQuit();
			}
		});

		setKeyboard();
		
		layers = new JLayeredPane();
		int lvl = 0;
		layers.add(starmapRenderer, Integer.valueOf(lvl++));
		layers.add(planetRenderer, Integer.valueOf(lvl++));
		layers.add(informationRenderer, Integer.valueOf(lvl++));
		layers.add(mainmenuRenderer, Integer.valueOf(lvl++));

		layers.add(moviePlayer, Integer.valueOf(lvl++));
		layers.add(optionsRenderer, Integer.valueOf(lvl++));
		
		GroupLayout gl = new GroupLayout(layers);
		layers.setLayout(gl);
		gl.setHorizontalGroup(gl.createParallelGroup()
			.addComponent(mainmenuRenderer, 640, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(starmapRenderer, 640, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(planetRenderer, 640, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(informationRenderer, 640, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(moviePlayer, 640, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(optionsRenderer, 640, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		);
		gl.setVerticalGroup(
			gl.createParallelGroup()
			.addComponent(mainmenuRenderer, 480, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(starmapRenderer, 480, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(planetRenderer, 480, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(informationRenderer, 480, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(moviePlayer, 480, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(optionsRenderer, 480, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		);
		
		
		// Determine minimum client width and height
		Container c = getContentPane();
		gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setHorizontalGroup(gl.createParallelGroup()
			.addComponent(layers, 640, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		);
		gl.setVerticalGroup(
			gl.createParallelGroup()
			.addComponent(layers, 480, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		);
		pack();
		setLocationRelativeTo(null);
		final int inW = getWidth();
		final int inH = getHeight();
		setMinimumSize(new Dimension(inW, inH));
		
		setFocusTraversalKeysEnabled(false);
//		starmapRenderer.startAnimations();
		//GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(this);
		setVisible(true);
		// switch to a particular screen.
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				onStarmap();
			}
		});
		screenRefreshTimer = new Timer(SCREEN_REFRESH_TIME, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
		});
		screenRefreshTimer.start();
	}
	/**
	 * Set the keyboard shortcuts.
	 */
	private void setKeyboard() {
		JRootPane rp = getRootPane();
		
		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0, false);
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "F2");
		rp.getActionMap().put("F2", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { onF2Action(); } });
		
		ks = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0, false);
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "F3");
		rp.getActionMap().put("F3", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { onF3Action(); } });

		ks = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0, false);
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "F7");
		rp.getActionMap().put("F7", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { onF7Action(); } });

		ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "ESC");
		rp.getActionMap().put("ESC", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { onESCAction(); } });
		
		// Diagnostic/Cheat keystrokes
		ks = KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK, false);
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "CTRL+K");
		rp.getActionMap().put("CTRL+K", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { doKnowAllPlanets(); } });
		
		ks = KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK, false);
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "CTRL+K");
		rp.getActionMap().put("CTRL+K", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { doKnowAllPlanets(); } });

		ks = KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK, false);
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "CTRL+N");
		rp.getActionMap().put("CTRL+N", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { doKnowAllPlanetsByName(); } });

		ks = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK, false);
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "CTRL+F");
		rp.getActionMap().put("CTRL+F", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { doKnowAllFleets(); } });
		
		ks = KeyStroke.getKeyStroke('+');
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "NUM+");
		rp.getActionMap().put("NUM+", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { doSelectNexPlanetOrFleet(); } });
		
		ks = KeyStroke.getKeyStroke('-');
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "NUM-");
		rp.getActionMap().put("NUM-", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { doSelectPrevPlanetOrFleet(); } });
		
		ks = KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK, false);
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "CTRL+O");
		rp.getActionMap().put("CTRL+O", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { doOwnPlanets(false); } });
		
		ks = KeyStroke.getKeyStroke('.', InputEvent.CTRL_DOWN_MASK, false);
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "CTRL+.");
		rp.getActionMap().put("CTRL+.", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { doDoToggleInterpolations(); } });
		
		ks = KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK, false);
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "CTRL+C");
		rp.getActionMap().put("CTRL+C", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { doClearPlanetBuildings(); } });
		
		ks = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK, false);
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "CTRL+S");
		rp.getActionMap().put("CTRL+S", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { doSavePlanetBuildings(); } });
		
		ks = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "DEL");
		rp.getActionMap().put("DEL", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { doDeleteBuilding(); } });
		
		ks = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK, false);
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "CTRL+D");
		rp.getActionMap().put("CTRL+D", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { doDamageBuilding(); } });
		
		ks = KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK, false);
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "CTRL+M");
		rp.getActionMap().put("CTRL+M", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { doTakeoverPlanet(); } });
		
		ks = KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK, false);
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "CTRL+P");
		rp.getActionMap().put("CTRL+P", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { doExtractResource(); } });
		
		ks = KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, false);
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "CTRL+SHIFT+O");
		rp.getActionMap().put("CTRL+SHIFT+O", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { doOwnPlanets(true); } });
		
		ks = KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK, false);
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "CTRL+I");
		rp.getActionMap().put("CTRL+I", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { doOwnEnemyPlanets(); } });
	}
	/**
	 * Extracts the current planet configuration for the resource allocation optimizer.
	 */
	protected void doExtractResource() {
		if (gameWorld.player.selectedPlanet != null) {
			try {
				PrintWriter out = new PrintWriter(new FileWriter(gameWorld.player.selectedPlanet.name + ".dat"));
				int i = 0;
				out.printf("%d%n", gameWorld.player.selectedPlanet.buildings.size());
				for (GameBuilding b : gameWorld.player.selectedPlanet.buildings) {
					if (i++ > 0) {
						out.printf(",%n");
					}
					Integer ep = b.prototype.values.get("energy-prod");
					out.printf("<\"%s\", %d, %d, %s, %d>", b.prototype.name, b.prototype.energy, b.prototype.workers, 0.5, ep != null ? ep : 0);
				}
				out.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}		
	}
	/**
	 * Takes over the currently selected planet.
	 */
	protected void doTakeoverPlanet() {
		if (gameWorld.player.selectedPlanet != null) {
			gameWorld.player.selectedPlanet.owner = gameWorld.player;
			repaint();
		}
	}
	/**
	 * Toggle the selected building's health between 100 and 40 percent.
	 */
	protected void doDamageBuilding() {
		if (gameWorld.player.selectedPlanet != null) {
			if (gameWorld.player.selectedPlanet.selectedBuilding != null) {
				if (gameWorld.player.selectedPlanet.selectedBuilding.health < 100) {
					gameWorld.player.selectedPlanet.selectedBuilding.health = 100;
				} else {
					gameWorld.player.selectedPlanet.selectedBuilding.health = 40;
				}
				planetRenderer.clearRadarCache();
				repaint();
			}
		}
	}
	/**
	 * Deletes the currently selected building from the current planet,
	 * if the planet renderer is active.
	 */
	protected void doDeleteBuilding() {
		if (planetRenderer.isVisible() && !informationRenderer.isVisible()) {
			planetRenderer.doDemolish();
		}
	}
	/**
	 * Clear planet buildings from the currently selected planet.
	 */
	protected void doClearPlanetBuildings() {
		GamePlanet planet = gameWorld.player.selectedPlanet;
		if (planet != null) {
			planet.buildings.clear();
			planet.buildingKinds.clear();
			planet.buildingTypes.clear();
			planet.map.clear();
			planet.selectedBuilding = null;
			planetRenderer.clearRadarCache();
			planetRenderer.repaint();
		}
	}
	/**
	 * FIXME for development only
	 * Save planet buildings from the currently selected planet.
	 */
	protected void doSavePlanetBuildings() {
		GamePlanet planet = gameWorld.player.selectedPlanet;
		if (planet != null) {
			try {
				PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(planet.name + ".xml"), "UTF-8"));
				try {
					out.printf("<?xml version='1.0' encoding='UTF-8'?>%n");
					out.printf("<buildings>%n");
					for (GameBuilding gb : planet.buildings) {
						out.printf("\t<building>%n");
						out.printf("\t\t<id>%s</id>%n", gb.prototype.id);
						out.printf("\t\t<health>%s</health>%n", gb.health);
						out.printf("\t\t<progress>%s</progress>%n", gb.progress);
						out.printf("\t\t<x>%s</x>%n", gb.x);
						out.printf("\t\t<y>%s</y>%n", gb.y);
						out.printf("\t\t<enabled>%s</enabled>%n", gb.enabled);
						out.printf("\t</building>%n");
					}
					out.printf("</buildings>%n");
				} finally {
					out.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		planetRenderer.repaint();
	}
	/** Toggle bicubic interpolation on the starmap background. */
	protected void doDoToggleInterpolations() {
		starmapRenderer.setInterpolation(
				ImageInterpolation.values()[(starmapRenderer.getInterpolation().ordinal() + 1) % ImageInterpolation.values().length]);
		planetRenderer.clearRadarCache();
		planetRenderer.setInterpolation(
				ImageInterpolation.values()[(starmapRenderer.getInterpolation().ordinal() + 1) % ImageInterpolation.values().length]);

	}
	/**
	 * Sets action listeners on the various screens.
	 */
	private void setListeners() {
		starmapRenderer.setOnColonyClicked(new BtnAction() { public void invoke() { onStarmapColony(); } });
		starmapRenderer.setOnInformationClicked(new BtnAction() { public void invoke() { onStarmapInfo(); } });
		planetRenderer.setOnStarmapClicked(new BtnAction() { public void invoke() { onColonyStarmap(); } });
		planetRenderer.setOnInformationClicked(new BtnAction() { public void invoke() { onColonyInfo(); } });
		planetRenderer.setOnListClicked(new BtnAction() { public void invoke() { onBuildings(); } });
		
		informationRenderer.setOnStarmapClicked(new BtnAction() { public void invoke() { onInfoStarmap(); } });
		informationRenderer.setOnColonyClicked(new BtnAction() { public void invoke() { onInfoColony(); } });
		informationRenderer.setOnCancelInfoscreen(new BtnAction() { public void invoke() { onCancelInfoScreen(); } });
		informationRenderer.setOnDblClickBuilding(new BtnAction() { public void invoke() { onDblClickBuilding(); } });
		informationRenderer.setOnDblClickPlanet(new BtnAction() { public void invoke() { onDblClickPlanet(); } });
		
		planetRenderer.setOnPlanetsClicked(new BtnAction() { public void invoke() { onColonyPlanets(); } });
		
		mainmenuRenderer.setStartNewAction(new BtnAction() { public void invoke() { onStarmap(); } });
		mainmenuRenderer.setLoadAction(new BtnAction() { public void invoke() { onLoad(); } });
		mainmenuRenderer.setTitleAnimAction(new BtnAction() { public void invoke() { onTitle(); } });
		mainmenuRenderer.setIntroAction(new BtnAction() { public void invoke() { onIntro(); } });
		mainmenuRenderer.setQuitAction(new BtnAction() { public void invoke() { onQuit(); } });
		
		optionsRenderer.setOnAdjustMusic(new BtnAction() { public void invoke() { onAdjustMusic(); } });
		optionsRenderer.setOnAdjustSound(new BtnAction() { public void invoke() { onAdjustSound(); } });
		optionsRenderer.setOnExit(new BtnAction() { public void invoke() { doExit(); } });
	}
	/**
	 * Display planet when double clicking on the planet name.
	 */
	protected void onDblClickPlanet() {
		showScreen(planetRenderer);
	}
	/**
	 * Enter build mode on planet when double clicking on the building name.
	 */
	protected void onDblClickBuilding() {
		if (planetRenderer.isVisible()) {
			showScreen(planetRenderer);
			planetRenderer.doBuild();
		}
	}
	/**
	 * Cancel information screen and return to the previous screen.
	 */
	protected void onCancelInfoScreen() {
		if (planetRenderer.isVisible()) {
			showScreen(planetRenderer);
		} else
		if (starmapRenderer.isVisible()) {
			showScreen(starmapRenderer);
		}
	}
	/**
	 * Switch to buildings on the information screen.
	 */
	protected void onBuildings() {
		informationRenderer.setScreenButtonsFor(InfoScreen.BUILDINGS);
		informationRenderer.setVisible(true);
		layers.validate();
	}
	/** Go to starmap from main menu. */
	private void onStarmap() {
		inGame = true;
		
		// start new game with the following settings
		startNewGame();
		
		uiSounds.playSound("WelcomeToIG");
		showScreen(starmapRenderer);
		startStopAnimations(true);
		
		List<String> userMusic = findUserMusic();
		userMusic.add("res:/hu/openig/res/Music2.ogg");
		userMusic.add("res:/hu/openig/res/Music1.ogg");
		userMusic.add("res:/hu/openig/res/Music3.ogg");
		music.playFile(userMusic.toArray(new String[userMusic.size()]));
	}
	/** Quit pressed on starmap. */
	private void onQuit() {
		dispose();
	}
	/**
	 * Retrieves all filenames from the MUSIC/ subdirectory with
	 * .WAV or .OGG extension. The list is then ordered by
	 * a natural short order
	 * @return the ordered list of user music, never null
	 */
	private List<String> findUserMusic() {
		List<String> result = new ArrayList<String>();
		for (String s : resMap.keySet()) {
			if (s.startsWith("MUSIC/") && (s.endsWith(".OGG") || s.endsWith(".WAV"))) {
				result.add(s);
			}
		}
		Collections.sort(result, JavaUtils.NATURAL_COMPARATOR);
		return result;
	}
	/**
	 * Action for starmap colony button pressed.
	 */
	private void onStarmapColony() {
		showScreen(planetRenderer);
	}
	/** Action for starmap info button pressed. */
	private void onStarmapInfo() {
		if (gameWorld.player.selectionType == StarmapSelection.PLANET) {
			informationRenderer.setScreenButtonsFor(InfoScreen.COLONY_INFORMATION);
		} else 
		if (gameWorld.player.selectionType == StarmapSelection.FLEET) {
			informationRenderer.setScreenButtonsFor(InfoScreen.FLEETS);
		} else {
			informationRenderer.setScreenButtonsFor(InfoScreen.COLONY_INFORMATION);
		}
		informationRenderer.setVisible(true);
		layers.validate();
	}
	/** Action for colony starmap button pressed. */
	private void onColonyStarmap() {
		showScreen(starmapRenderer);
	}
	/** Action for colony planets button pressed. */
	private void onColonyPlanets() {
		informationRenderer.setScreenButtonsFor(InfoScreen.PLANETS);
		informationRenderer.setVisible(true);
		layers.validate();
	}
	/** Action for colony info button pressed. */
	private void onColonyInfo() {
		informationRenderer.setScreenButtonsFor(InfoScreen.COLONY_INFORMATION);
		informationRenderer.setVisible(true);
		layers.validate();
	}
	/** Action for info screen starmap button pressed. */
	private void onInfoStarmap() {
		showScreen(starmapRenderer);
	}
	/** Action for info screen colony button pressed. */
	private void onInfoColony() {
		showScreen(planetRenderer);
	}
	/**
	 * Open Imperium Galactica main program.
	 * @param args arguments one optional argument for specifying IG's root directory
	 * @throws Exception ignores any exception
	 */
	public static void main(String[] args)  throws Exception {
		// FIXME D3D pipeline is slow for an unknown reason
		System.setProperty("sun.java2d.d3d", "false");
		String root = ".";
		if (args.length > 0) {
			root = args[0];
		}
		final ResourceMapper resMap = new ResourceMapper(root);
		File file = resMap.get("IMPERIUM.EXE");
		if (file == null || !file.exists()) {
			JOptionPane.showMessageDialog(null, "Please place this program into the Imperium Galactica directory or specify the location via the first command line parameter.");
			return;
		}
		final String language = determineIGLanguage(resMap.get("MAIN.EXE"));
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Main m = new Main();
				m.initialize(resMap, language);
			}
		});
	}
	/**
	 * Returns the language of the target Imperium Galactica.
	 * @param mainEXE the MAIN.EXE file
	 * @return the language code, defaults to 'en'
	 */
	private static String determineIGLanguage(File mainEXE) {
		if (mainEXE != null) {
			byte[] data = IOUtils.load(mainEXE);
			// Real time in hungarian
			try {
				byte[] hungarian = { 0x56, 0x61, 0x6C, (byte)0xA2, 0x73, 0x20, 0x69, 0x64, (byte)0x93 };
				if (JavaUtils.arrayIndexOf(data, hungarian, 0) >= 0) {
					return "hu";
				}
				byte[] english = "Realtime".getBytes("ISO-8859-1");
				if (JavaUtils.arrayIndexOf(data, english, 0) >= 0) {
					return "en";
				}
			} catch (UnsupportedEncodingException ex) {
				// should not occur
			}
		}
		return "en";
	}
	/** Action for F2 keypress. */
	private void onF2Action() {
		if (!player.isPlayback()) {
			if (!starmapRenderer.isVisible()) {
				uiSounds.playSound("Starmap");
				showScreen(starmapRenderer);
			} else
			if (informationRenderer.isVisible()) {
				uiSounds.playSound("Starmap");
				showScreen(starmapRenderer);
			}
		}
	}
	/** Action for F3 keypress. */
	private void onF3Action() {
		if (!player.isPlayback()) {
			if (!planetRenderer.isVisible()) {
				uiSounds.playSound("Colony");
				showScreen(planetRenderer);
			} else
			if (informationRenderer.isVisible()) {
				uiSounds.playSound("Colony");
				showScreen(planetRenderer);
			}
		}
	}
	/** Action for F7 keypress. */
	private void onF7Action() {
		if (!player.isPlayback()) {
			if (!informationRenderer.isVisible()) {
				if (starmapRenderer.isVisible()) {
					uiSounds.playSound("Planets");
					informationRenderer.setScreenButtonsFor(InfoScreen.PLANETS);
				} else
				if (planetRenderer.isVisible()) {
					uiSounds.playSound("ColonyInformation");
					informationRenderer.setScreenButtonsFor(InfoScreen.COLONY_INFORMATION);
				}
				informationRenderer.setVisible(true);
				layers.validate();
			}
		}
	}
	/**
	 * Show the given screen and hide all other screens.
	 * @param comp the component to show
	 */
	private void showScreen(JComponent comp) {
		for (JComponent c : screens) {
			c.setVisible(c == comp);
		}
		layers.validate();
	}
	/**
	 * Play the title intro.
	 */
	private void onTitle() {
		showScreen(moviePlayer);
		player.setFilename(resMap.get("INTRO/GT_TITLE.ANI").getAbsolutePath());
		player.setOnCompleted(new BtnAction() { public void invoke() { onPlaybackCompleted(); } });
		player.startPlayback();
	}
	/** Play the sequence of intro videos. */ 
	private void onIntro() {
		showScreen(moviePlayer);
		player.setFilename(resMap.get("INTRO/BLOCK1.ANI").getAbsolutePath());
		player.setOnCompleted(new BtnAction() { public void invoke() { onIntro1(); } });
		player.startPlayback();
	}
	/**
	 * Play intro video 2.
	 */
	private void onIntro1() {
		if (playbackCancelled) {
			playbackCancelled = false;
//			onPlaybackCompleted();
//			return;
		}
		player.setFilename(resMap.get("INTRO/BLOCK23.ANI").getAbsolutePath());
		player.setOnCompleted(new BtnAction() { public void invoke() { onIntro2(); } });
		player.startPlayback();
	}
	/**
	 * Play intro video 3.
	 */
	private void onIntro2() {
		if (playbackCancelled) {
			playbackCancelled = false;
//			onPlaybackCompleted();
//			return;
		}
		player.setFilename(resMap.get("INTRO/BLOCK4.ANI").getAbsolutePath());
		player.setOnCompleted(new BtnAction() { public void invoke() { onPlaybackCompleted(); } });
		player.startPlayback();
	}
	/** If the main menu playback completes, restore the main menu. */
	private void onPlaybackCompleted() {
		showScreen(mainmenuRenderer);
	}
	/** The escape key pressed. */
	private void onESCAction() {
		if (player.isPlayback()) {
			playbackCancelled = true;
			player.stopAndWait();
		} else
		if (!mainmenuRenderer.isVisible()) {
			if (!optionsRenderer.isVisible()) {
				optionsRenderer.setRandomPicture();
			}
			optionsRenderer.setVisible(!optionsRenderer.isVisible());
			startStopAnimations(!optionsRenderer.isVisible());
			// enable/disable speed controls
			gameWorld.showSpeedControls = !optionsRenderer.isVisible();
			layers.validate();
		} else
		if (mainmenuRenderer.isVisible() && optionsRenderer.isVisible()) {
			optionsRenderer.setVisible(!optionsRenderer.isVisible());
			// enable/disable speed controls
			gameWorld.showSpeedControls = !optionsRenderer.isVisible();
			startStopAnimations(!optionsRenderer.isVisible());
			layers.validate();
		}
	}
	/**
	 * Start or stop animations when the options screen is displayed.
	 * @param state start or stop animations
	 */
	private void startStopAnimations(boolean state) {
		if (state) {
			achievementRenderer.startAnimations();
			starmapRenderer.startAnimations();
			informationRenderer.startAnimations();
			planetRenderer.startTimers();
		} else {
			achievementRenderer.stopAnimations();
			starmapRenderer.stopAnimations();
			informationRenderer.stopAnimations();
			planetRenderer.stopTimers();
		}
	}
	/** Show the options screen when called from the main menu. */
	private void onLoad() {
		optionsRenderer.setRandomPicture();
		optionsRenderer.setVisible(!optionsRenderer.isVisible());
		startStopAnimations(!optionsRenderer.isVisible());
		layers.validate();
	}
	/** If adjusting music volume. */
	private void onAdjustSound() {
		if (optionsRenderer.getAudioVolume() < 0.0001) {
			uiSounds.setMute(true);
		} else {
			uiSounds.setMute(false);
			uiSounds.setMasterGain((float)(20 * Math.log10(optionsRenderer.getAudioVolume())));
		}
	}
	/** If adjusting sound volume. */
	private void onAdjustMusic() {
		if (optionsRenderer.getMusicVolume() < 0.0001) {
			music.setMute(true);
		} else {
			music.setMute(false);
			music.setMasterGain((float)(20 * Math.log10(optionsRenderer.getMusicVolume())));
		}
	}
	/** Perform actions to quit from the application. */
	private void doQuit() {
		screenRefreshTimer.stop();
		uiSounds.close();
		startStopAnimations(false);
		player.setOnCompleted(null);
		player.stopAndWait();
		music.stop();
		music.close();
		exec.shutdown();
	}
	/** Perform the exit operation. */
	private void doExit() {
		starmapRenderer.setVisible(false);
		optionsRenderer.setVisible(false);
		mainmenuRenderer.setVisible(true);
		startStopAnimations(false);
		layers.validate();
		music.stop();
		music.close();
		inGame = false;
	}
	/**
	 * Prepare the game world for a new game.
	 */
	private void startNewGame() {
		gameWorld.showSpeedControls = true;
		gameWorld.showTime = true;
		gameWorld.showUltrafast = false;
		gameWorld.gameSpeed = GameSpeed.NORMAL;
		gameWorld.calendar.setTimeInMillis(45997848000000L); //August 13th, 3427, 12 PM

		gameWorld.language = language;
		gameWorld.labels = grm.labels;
		
		gameWorld.research.clear();
		gameWorld.research.putAll(ResearchTech.parse("/hu/openig/res/tech.xml", grm));
		
		gameWorld.races.clear();
		gameWorld.races.addAll(GameRace.parse("/hu/openig/res/races.xml"));
		
		gameWorld.buildingPrototypesMap.clear();
		gameWorld.buildingPrototypesMap.putAll(GameBuildingPrototype.parse("/hu/openig/res/buildings.xml", grm));
		gameWorld.buildingPrototypes.clear();
		gameWorld.buildingPrototypes.addAll(gameWorld.buildingPrototypesMap.values());
		
		gameWorld.assignBuildingToResearch();
		
		gameWorld.players.clear();
		for (int i = 0; i < gameWorld.races.size(); i++) {
			// create the single player
			GamePlayer player = new GamePlayer();
			player.playerType = i == 0 ? PlayerType.LOCAL_HUMAN : PlayerType.LOCAL_AI;
			player.race = gameWorld.getRace(i + 1);
			player.money = 32000;
			player.name = gameWorld.getLabel("EmpireNames." + player.race.id);
			player.fleetIcon = i;
			gameWorld.players.add(player);
			if (i == 0) {
				gameWorld.player = player;
			}
		}
		gameWorld.level = 6;
		gameWorld.assignTechnologyToPlayers();
		
		gameWorld.planets.clear();
		gameWorld.planets.addAll(GamePlanet.parse("/hu/openig/res/planets.xml", gameWorld));
		// initialize local player
		
		// create fleets for all owned planets
		int i = 1;
		for (GamePlanet p : gameWorld.planets) {
			if (p.owner != null) {
				GameFleet f = new GameFleet();
				f.name = p.owner.race.id + " " + i + " fleet over " + p.name;
				f.owner = p.owner;
				f.x = p.x;
				f.y = p.y;
				gameWorld.fleets.add(f);
			}
			i++;
		}
		
		gameWorld.setPlanetOwnerships();
		gameWorld.setFleetOwnerships();
		gameWorld.allocateResources();
		starmapRenderer.scrollToLogical(gameWorld.player.ownPlanets.iterator().next().getPoint());
		achievementRenderer.enqueueAchievement("Welcome to Open Imperium Galactica");
		achievementRenderer.enqueueAchievement("Good luck");
	}
	/**
	 * Diagnostic method to set the player know all planets in the game world.
	 * To know all planets by name use knowAllPlanetsByName().
	 */
	public void doKnowAllPlanets() {
		for (GamePlanet p : gameWorld.planets) {
			gameWorld.player.knowPlanet(p);
		}
		repaint();
	}
	/**
	 * Diagnostic method to set the player know all planets in the game world.
	 * To know all planets by name use knowAllPlanetsByName().
	 */
	public void doKnowAllPlanetsByName() {
		for (GamePlanet p : gameWorld.planets) {
			gameWorld.player.knowPlanetByName(p);
		}
		repaint();
	}
	/**
	 * Diagnostic method to set the player know all planets in the game world.
	 * To know all planets by name use knowAllPlanetsByName().
	 */
	public void doKnowAllFleets() {
		for (GameFleet p : gameWorld.fleets) {
			gameWorld.player.knowFleet(p);
		}
		repaint();
	}
	/** Select next planet. */
	private void doSelectNexPlanetOrFleet() {
		if (gameWorld.player.selectionType == null || gameWorld.player.selectionType == StarmapSelection.PLANET) {
			List<GamePlanet> list = gameWorld.getOwnPlanetsInOrder();
			if (list.size() > 0) {
				int idx = list.indexOf(gameWorld.player.selectedPlanet);
				gameWorld.player.selectedPlanet = list.get((idx + 1) % list.size());
				gameWorld.player.selectionType = StarmapSelection.PLANET;
				repaint();
			}
		} else {
			List<GameFleet> list = gameWorld.getOwnFleetsByCoords();
			if (list.size() > 0) {
				int idx = list.indexOf(gameWorld.player.selectedFleet);
				gameWorld.player.selectedFleet = list.get((idx + 1) % list.size());
				gameWorld.player.selectionType = StarmapSelection.FLEET;
				repaint();
			}
		}
	}
	/** Select previous planet. */
	private void doSelectPrevPlanetOrFleet() {
		if (gameWorld.player.selectionType == null || gameWorld.player.selectionType == StarmapSelection.PLANET) {
			List<GamePlanet> list = gameWorld.getOwnPlanetsInOrder();
			if (list.size() > 0) {
				int idx = list.indexOf(gameWorld.player.selectedPlanet) - 1;
				if (idx < 0) {
					idx = list.size() - 1;
				}
				gameWorld.player.selectedPlanet = list.get(idx);
				gameWorld.player.selectionType = StarmapSelection.PLANET;
				repaint();
			}
		} else {
		List<GameFleet> list = gameWorld.getOwnFleetsByCoords();
			if (list.size() > 0) {
				int idx = list.indexOf(gameWorld.player.selectedFleet) - 1;
				if (idx < 0) {
					idx = list.size() - 1;
				}
				gameWorld.player.selectedFleet = list.get(idx);
				gameWorld.player.selectionType = StarmapSelection.FLEET;
				repaint();
			}
		}
	}
	/**
	 * Set owner of all empty planets to the player.
	 * @param all own all planets or just the empty ones 
	 */
	private void doOwnPlanets(boolean all) {
		for (GamePlanet p : gameWorld.planets) {
			if (all || p.owner == null) {
				if (p.owner != null) {
					p.owner.loosePlanet(p);
				}
				p.owner = gameWorld.player;
				if (p.populationRace == null) {
					p.populationRace = gameWorld.player.race;
				}
				gameWorld.player.possessPlanet(p);
			}
		}
		repaint();
	}
	/**
	 * Set owner of all enemy planets to the player.
	 */
	private void doOwnEnemyPlanets() {
		for (GamePlanet p : gameWorld.planets) {
			if (p.owner != null) {
				p.owner.loosePlanet(p);
				p.owner = gameWorld.player;
				gameWorld.player.possessPlanet(p);
			}
		}
		repaint();
	}
}
