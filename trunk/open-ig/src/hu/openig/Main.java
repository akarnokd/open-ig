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
import hu.openig.gfx.InformationRenderer;
import hu.openig.gfx.MainmenuRenderer;
import hu.openig.gfx.MenuGFX;
import hu.openig.gfx.OptionsGFX;
import hu.openig.gfx.OptionsRenderer;
import hu.openig.gfx.PlanetGFX;
import hu.openig.gfx.PlanetRenderer;
import hu.openig.gfx.StarmapGFX;
import hu.openig.gfx.StarmapRenderer;
import hu.openig.model.GMPlanet;
import hu.openig.music.Music;
import hu.openig.sound.UISounds;
import hu.openig.utils.ResourceMapper;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
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
	public static final String VERSION = "0.5 Alpha";
	/** The user interface sounds. */
	UISounds uis;
	/** The common graphics objects. */
	CommonGFX cgfx;
	/** The starmap renderer. */
	StarmapRenderer smr;
	/** The planet surface renderer. */
	PlanetRenderer pr;
	/** The information screen renderer. */
	InformationRenderer ir;
	/** The main menu renderer. */
	MainmenuRenderer mmr;
	/** The full screen movie surface. */
	MovieSurface mov;
	/** Timer for fade in-out animations. */
	Timer fadeTimer;
	/** The fade timer firing interval in milliseconds. */
	static final int FADE_TIME = 50;
	/** The panel used for screen switching. */
	JLayeredPane layers;
	/** The array of screens. */
	JComponent[] screens;
	/** The root directory. */
	String root;
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
	OptionsRenderer or;
	/** The program is currently in Game mode. */
	boolean inGame;
	/** The resource file mapper. */
	private ResourceMapper resMap;
	/**
	 * Initialize resources from the given root directory.
	 * @param root the root directory
	 */
	protected void initialize(final String root) {
		this.root = root;
		//setUndecorated(true);
		setTitle("Open Imperium Galactica");
		setBackground(Color.BLACK);
		fadeTimer = new Timer(FADE_TIME, null);
		
		exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		resMap = new ResourceMapper(root);
		music = new Music(".");

		List<Future<?>> futures = new LinkedList<Future<?>>();
		futures.add(exec.submit(new Runnable() { public void run() { uis = new UISounds(resMap); } }));
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
		
		// initialize renderers
		smr = new StarmapRenderer(starmapGFX, cgfx, uis);
		pr = new PlanetRenderer(planetGFX, cgfx, uis);
		ir = new InformationRenderer(infoGFX, cgfx, uis);
		mmr = new MainmenuRenderer(menuGFX, cgfx.text);
		mmr.setOpaque(true);
		mmr.setBackground(Color.BLACK);
		mov = new MovieSurface();
		mov.setOpaque(true);
		mov.setBackground(Color.BLACK);
		or = new OptionsRenderer(optionsGFX, cgfx.text, uis);
		or.setVisible(false);
		player = new Player(mov);
		player.setMasterGain(0);
		uis.setMasterGain(0);
		or.setAudioVolume(1);
		or.setMusicVolume(1);
		screens = new JComponent[] {
			smr, pr, ir, mmr, mov, or
		};
		// setup renderers
		mmr.setVisible(true);
		mmr.setVersion(VERSION);
		mmr.setRandomPicture();
		smr.setVisible(false);
		ir.setVisible(false);
		pr.setVisible(false);
		mov.setVisible(false);
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
		layers.add(smr, Integer.valueOf(lvl++));
		layers.add(pr, Integer.valueOf(lvl++));
		layers.add(ir, Integer.valueOf(lvl++));
		layers.add(mmr, Integer.valueOf(lvl++));

		layers.add(mov, Integer.valueOf(lvl++));
		layers.add(or, Integer.valueOf(lvl++));
		
		GroupLayout gl = new GroupLayout(layers);
		layers.setLayout(gl);
		gl.setHorizontalGroup(gl.createParallelGroup()
			.addComponent(mmr, 640, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(smr, 640, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(pr, 640, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(ir, 640, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(mov, 640, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(or, 640, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		);
		gl.setVerticalGroup(
			gl.createParallelGroup()
			.addComponent(mmr, 480, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(smr, 480, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(pr, 480, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(ir, 480, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(mov, 480, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(or, 480, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
		initModel();
		smr.startAnimations();
		//GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(this);
		setVisible(true);
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
		
	}
	/**
	 * Sets action listeners on the various screens.
	 */
	private void setListeners() {
		smr.setOnColonyClicked(new BtnAction() { public void invoke() { onStarmapColony(); } });
		smr.setOnInformationClicked(new BtnAction() { public void invoke() { onStarmapInfo(); } });
		pr.setOnStarmapClicked(new BtnAction() { public void invoke() { onColonyStarmap(); } });
		pr.setOnInformationClicked(new BtnAction() { public void invoke() { onColonyInfo(); } });
		ir.setOnStarmapClicked(new BtnAction() { public void invoke() { onInfoStarmap(); } });
		ir.setOnColonyClicked(new BtnAction() { public void invoke() { onInfoColony(); } });
		pr.setOnPlanetsClicked(new BtnAction() { public void invoke() { onColonyPlanets(); } });
		
		mmr.setStartNewAction(new BtnAction() { public void invoke() { onStarmap(); } });
		mmr.setLoadAction(new BtnAction() { public void invoke() { onLoad(); } });
		mmr.setTitleAnimAction(new BtnAction() { public void invoke() { onTitle(); } });
		mmr.setIntroAction(new BtnAction() { public void invoke() { onIntro(); } });
		mmr.setQuitAction(new BtnAction() { public void invoke() { onQuit(); } });
		
		or.setOnAdjustMusic(new BtnAction() { public void invoke() { onAdjustMusic(); } });
		or.setOnAdjustSound(new BtnAction() { public void invoke() { onAdjustSound(); } });
		or.setOnExit(new BtnAction() { public void invoke() { doExit(); } });
	}
	/** Go to starmap from main menu. */
	private void onStarmap() {
		inGame = true;
		uis.playSound("WelcomeToIG");
		showScreen(smr);
		startStopAnimations(true);
		
		music.playFile("res:/hu/openig/res/music2.ogg", "res:/hu/openig/res/music1.ogg", "res:/hu/openig/res/music3.ogg");
	}
	/** Quit pressed on starmap. */
	private void onQuit() {
		dispose();
	}
	/**
	 * Action for starmap colony button pressed.
	 */
	private void onStarmapColony() {
		showScreen(pr);
	}
	/** Action for starmap info button pressed. */
	private void onStarmapInfo() {
		ir.setScreenButtonsFor(InfoScreen.PLANETS);
		ir.setVisible(true);
		layers.validate();
	}
	/** Action for colony starmap button pressed. */
	private void onColonyStarmap() {
		showScreen(smr);
	}
	/** Action for colony planets button pressed. */
	private void onColonyPlanets() {
		ir.setScreenButtonsFor(InfoScreen.PLANETS);
		ir.setVisible(true);
		layers.validate();
	}
	/** Action for colony info button pressed. */
	private void onColonyInfo() {
		ir.setScreenButtonsFor(InfoScreen.COLONY_INFORMATION);
		ir.setVisible(true);
		layers.validate();
	}
	/** Action for info screen starmap button pressed. */
	private void onInfoStarmap() {
		showScreen(smr);
	}
	/** Action for info screen colony button pressed. */
	private void onInfoColony() {
		showScreen(pr);
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
		File file = new File(root + "/IMPERIUM.EXE");
		if (!file.exists()) {
			JOptionPane.showMessageDialog(null, "Please place this program into the Imperium Galactica directory or specify the location via the first command line parameter.");
			return;
		}
		final String fRoot = root;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Main m = new Main();
				m.initialize(fRoot);
			}
		});
	}
	/** Action for F2 keypress. */
	private void onF2Action() {
		if (!player.isPlayback()) {
			if (!smr.isVisible()) {
				uis.playSound("Starmap");
				showScreen(smr);
			} else
			if (ir.isVisible()) {
				uis.playSound("Starmap");
				showScreen(smr);
			}
		}
	}
	/** Action for F3 keypress. */
	private void onF3Action() {
		if (!player.isPlayback()) {
			if (!pr.isVisible()) {
				uis.playSound("Colony");
				showScreen(pr);
			} else
			if (ir.isVisible()) {
				uis.playSound("Colony");
				showScreen(pr);
			}
		}
	}
	/** Action for F7 keypress. */
	private void onF7Action() {
		if (!player.isPlayback()) {
			if (!ir.isVisible()) {
				if (smr.isVisible()) {
					uis.playSound("Planets");
					ir.setScreenButtonsFor(InfoScreen.PLANETS);
				} else
				if (pr.isVisible()) {
					uis.playSound("ColonyInformation");
					ir.setScreenButtonsFor(InfoScreen.COLONY_INFORMATION);
				}
				ir.setVisible(true);
				layers.validate();
			}
		}
	}
	/** Initialize model to test model dependand rendering. */
	private void initModel() {
//		for (SurfaceType st : SurfaceType.values()) {
//			GMPlanet p = new GMPlanet();
//			p.name = "Planet " + st.surfaceIndex;
//			p.radarRadius = 50;
//			p.showName = true;
//			p.showRadar = true;
//			p.surfaceType = st;
//			p.surfaceVariant = 1;
//			p.visible = true;
//			p.x = 100 + st.surfaceIndex * 50;
//			p.y = 100 + st.surfaceIndex * 50;
//			p.nameColor = TextGFX.GALACTIC_EMPIRE_ST;
//			p.rotationDirection = st.surfaceIndex % 2 == 0;
//			smr.planets.add(p);
//		}
		smr.planets.addAll(GMPlanet.parse("/hu/openig/res/planets.xml"));
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
		showScreen(mov);
		player.setFilename(root + "/INTRO/GT_TITLE.ANI");
		player.setOnCompleted(new BtnAction() { public void invoke() { onPlaybackCompleted(); } });
		player.startPlayback();
	}
	/** Play the sequence of intro videos. */ 
	private void onIntro() {
		showScreen(mov);
		player.setFilename(root + "/INTRO/BLOCK1.ANI");
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
		player.setFilename(root + "/INTRO/BLOCK23.ANI");
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
		player.setFilename(root + "/INTRO/BLOCK4.ANI");
		player.setOnCompleted(new BtnAction() { public void invoke() { onPlaybackCompleted(); } });
		player.startPlayback();
	}
	/** If the main menu playback completes, restore the main menu. */
	private void onPlaybackCompleted() {
		showScreen(mmr);
	}
	/** The escape key pressed. */
	private void onESCAction() {
		if (player.isPlayback()) {
			playbackCancelled = true;
			player.stopAndWait();
		} else
		if (!mmr.isVisible()) {
			if (!or.isVisible()) {
				or.setRandomPicture();
			}
			or.setVisible(!or.isVisible());
			startStopAnimations(!or.isVisible());
			layers.validate();
		} else
		if (mmr.isVisible() && or.isVisible()) {
			or.setVisible(!or.isVisible());
			startStopAnimations(!or.isVisible());
			layers.validate();
		}
	}
	/**
	 * Start or stop animations when the options screen is displayed.
	 * @param state start or stop animations
	 */
	private void startStopAnimations(boolean state) {
		if (state) {
			smr.startAnimations();
		} else {
			smr.stopAnimations();
		}
	}
	/** Show the options screen when called from the main menu. */
	private void onLoad() {
		or.setRandomPicture();
		or.setVisible(!or.isVisible());
		startStopAnimations(!or.isVisible());
		layers.validate();
	}
	/** If adjusting music volume. */
	private void onAdjustSound() {
		if (or.getAudioVolume() < 0.0001) {
			uis.setMute(true);
		} else {
			uis.setMute(false);
			uis.setMasterGain((float)(20 * Math.log10(or.getAudioVolume())));
		}
	}
	/** If adjusting sound volume. */
	private void onAdjustMusic() {
		if (or.getMusicVolume() < 0.0001) {
			music.setMute(true);
		} else {
			music.setMute(false);
			music.setMasterGain((float)(20 * Math.log10(or.getMusicVolume())));
		}
	}
	/** Perform actions to quit from the application. */
	private void doQuit() {
		uis.close();
		smr.stopAnimations();
		player.setOnCompleted(null);
		player.stopAndWait();
		music.stop();
		music.close();
		exec.shutdown();
	}
	/** Perform the exit operation. */
	private void doExit() {
		smr.setVisible(false);
		or.setVisible(false);
		mmr.setVisible(true);
		startStopAnimations(false);
		layers.validate();
		music.stop();
		music.close();
		inGame = false;
	}
}
