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
import hu.openig.core.InfoScreen;
import hu.openig.gfx.CommonGFX;
import hu.openig.gfx.InformationGFX;
import hu.openig.gfx.MenuGFX;
import hu.openig.gfx.OptionsGFX;
import hu.openig.gfx.PlanetGFX;
import hu.openig.gfx.StarmapGFX;
import hu.openig.model.GameFleet;
import hu.openig.model.GamePlanet;
import hu.openig.model.GamePlayer;
import hu.openig.model.GameRace;
import hu.openig.model.GameSpeed;
import hu.openig.model.GameWorld;
import hu.openig.model.PlayerType;
import hu.openig.music.Music;
import hu.openig.render.InfobarRenderer;
import hu.openig.render.InformationRenderer;
import hu.openig.render.MainmenuRenderer;
import hu.openig.render.OptionsRenderer;
import hu.openig.render.PlanetRenderer;
import hu.openig.render.StarmapRenderer;
import hu.openig.res.Labels;
import hu.openig.sound.UISounds;
import hu.openig.utils.ResourceMapper;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
 *
 */
public class Main extends JFrame {
	/** */
	private static final long serialVersionUID = 6922932910697940684L;
	/** Version string. */
	public static final String VERSION = "0.65 Alpha"; // TODO reach 1.0!
	/** The user interface sounds. */
	UISounds uiSounds;
	/** The common graphics objects. */
	CommonGFX cgfx;
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
	/** The starmap graphics objects. */
	StarmapGFX starmapGFX;
	/** The planet graphics objects. */
	PlanetGFX planetGFX;
	/** The information screen renderer. */
	InformationGFX infoGFX;
	/** The main menu renderer. */
	MenuGFX menuGFX;
	/** The options screen renderer. */
	OptionsGFX optionsGFX;
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
	/**
	 * Initialize resources from the given root directory.
	 * @param resMap the resource mapper
	 */
	protected void initialize(final ResourceMapper resMap) {
		//setUndecorated(true);
		setTitle("Open Imperium Galactica (" + VERSION + ")");
		setBackground(Color.BLACK);
		fadeTimer = new Timer(FADE_TIME, null);
		
		exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		this.resMap = resMap;
		music = new Music(".");

		List<Future<?>> futures = new LinkedList<Future<?>>();
		futures.add(exec.submit(new Runnable() { public void run() { uiSounds = new UISounds(resMap); } }));
		futures.add(exec.submit(new Runnable() { public void run() { cgfx = new CommonGFX(resMap); } }));
		futures.add(exec.submit(new Runnable() { public void run() { starmapGFX = new StarmapGFX(resMap); } }));
		futures.add(exec.submit(new Runnable() { public void run() { planetGFX = new PlanetGFX(resMap); } }));
		futures.add(exec.submit(new Runnable() { public void run() { infoGFX = new InformationGFX(resMap); } }));
		futures.add(exec.submit(new Runnable() { public void run() { menuGFX = new MenuGFX(resMap); } }));
		futures.add(exec.submit(new Runnable() { public void run() { optionsGFX = new OptionsGFX(resMap); } }));
		
		for (Future<?> f : futures) {
			try {
				f.get();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			} catch (ExecutionException e1) {
				e1.printStackTrace();
			}
		}
		
		gameWorld = new GameWorld();
		// disable controls
		gameWorld.showTime = false;
		gameWorld.showSpeedControls = false;
		
		// initialize renderers
		infobarRenderer = new InfobarRenderer(cgfx);
		starmapRenderer = new StarmapRenderer(starmapGFX, cgfx, uiSounds, infobarRenderer);
		planetRenderer = new PlanetRenderer(planetGFX, cgfx, uiSounds, infobarRenderer);
		informationRenderer = new InformationRenderer(infoGFX, cgfx, uiSounds, infobarRenderer);
		mainmenuRenderer = new MainmenuRenderer(menuGFX, cgfx.text);
		mainmenuRenderer.setOpaque(true);
		mainmenuRenderer.setBackground(Color.BLACK);
		moviePlayer = new MovieSurface();
		moviePlayer.setOpaque(true);
		moviePlayer.setBackground(Color.BLACK);
		optionsRenderer = new OptionsRenderer(optionsGFX, cgfx, uiSounds, infobarRenderer);
		optionsRenderer.setVisible(false);
		player = new Player(moviePlayer);
		player.setMasterGain(0);
		uiSounds.setMasterGain(0);
		optionsRenderer.setAudioVolume(1);
		optionsRenderer.setMusicVolume(0); // TODO fix during testing
		music.setMute(true);
		screens = new JComponent[] {
			starmapRenderer, planetRenderer, informationRenderer, mainmenuRenderer, moviePlayer, optionsRenderer
		};
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
		
	}
	/**
	 * Sets action listeners on the various screens.
	 */
	private void setListeners() {
		starmapRenderer.setOnColonyClicked(new BtnAction() { public void invoke() { onStarmapColony(); } });
		starmapRenderer.setOnInformationClicked(new BtnAction() { public void invoke() { onStarmapInfo(); } });
		planetRenderer.setOnStarmapClicked(new BtnAction() { public void invoke() { onColonyStarmap(); } });
		planetRenderer.setOnInformationClicked(new BtnAction() { public void invoke() { onColonyInfo(); } });
		informationRenderer.setOnStarmapClicked(new BtnAction() { public void invoke() { onInfoStarmap(); } });
		informationRenderer.setOnColonyClicked(new BtnAction() { public void invoke() { onInfoColony(); } });
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
	/** Go to starmap from main menu. */
	private void onStarmap() {
		inGame = true;
		
		// start new game with the following settings
		startNewGame();
		
		uiSounds.playSound("WelcomeToIG");
		showScreen(starmapRenderer);
		startStopAnimations(true);
		
		music.playFile("res:/hu/openig/res/Music2.ogg", "res:/hu/openig/res/Music1.ogg", "res:/hu/openig/res/Music3.ogg");
	}
	/** Quit pressed on starmap. */
	private void onQuit() {
		dispose();
	}
	/**
	 * Action for starmap colony button pressed.
	 */
	private void onStarmapColony() {
		showScreen(planetRenderer);
	}
	/** Action for starmap info button pressed. */
	private void onStarmapInfo() {
		informationRenderer.setScreenButtonsFor(InfoScreen.PLANETS);
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
		// D3D pipeline is slow for an unknown reason
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
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Main m = new Main();
				m.initialize(resMap);
			}
		});
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
			starmapRenderer.startAnimations();
		} else {
			starmapRenderer.stopAnimations();
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
		uiSounds.close();
		starmapRenderer.stopAnimations();
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

		gameWorld.language = "en";
		gameWorld.labels = Labels.parse("/hu/openig/res/labels.xml");

		gameWorld.races.clear();
		gameWorld.races.addAll(GameRace.parse("/hu/openig/res/races.xml"));
		
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

		gameWorld.planets.clear();
		gameWorld.planets.addAll(GamePlanet.parse("/hu/openig/res/planets.xml", gameWorld));
		// initialize local player
		
		// create fleets for all owned planets
		for (GamePlanet p : gameWorld.planets) {
			if (p.owner != null) {
				GameFleet f = new GameFleet();
				f.name = p.owner.race.id + " fleet over " + p.name;
				f.owner = p.owner;
				f.x = p.x;
				f.y = p.y;
				gameWorld.fleets.add(f);
			}
		}
		
		gameWorld.setPlanetOwnerships();
		gameWorld.setFleetOwnerships();
		starmapRenderer.scrollToLogical(gameWorld.player.ownPlanets.iterator().next().getPoint());
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
}
