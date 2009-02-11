/*
 * Copyright 2008, David Karnok 
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
import hu.openig.core.SurfaceType;
import hu.openig.gfx.CommonGFX;
import hu.openig.gfx.InformationGFX;
import hu.openig.gfx.InformationRenderer;
import hu.openig.gfx.MainmenuRenderer;
import hu.openig.gfx.MenuGFX;
import hu.openig.gfx.PlanetGFX;
import hu.openig.gfx.PlanetRenderer;
import hu.openig.gfx.StarmapGFX;
import hu.openig.gfx.StarmapRenderer;
import hu.openig.gfx.TextGFX;
import hu.openig.model.GMPlanet;
import hu.openig.sound.UISounds;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

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
	public static final String version = "0.5 Alpha";
	UISounds uis;
	CommonGFX cgfx;
	StarmapRenderer smr;
	PlanetRenderer pr;
	InformationRenderer ir;
	MainmenuRenderer mmr;
	/** The full screen movie surface. */
	MovieSurface mov;
	Timer fadeTimer;
	int FADE_TIME = 50;
	JLayeredPane layers;
	/** The array of screens. */
	JComponent[] screens;
	/** The root directory. */
	String root;
	/** The animation player. */
	Player player;
	/** Set to true if the ESC is pressed while a full screen playback is in progress. */
	private boolean playbackCancelled;
	protected void initialize(String root) {
		this.root = root;
		setTitle("Open Imperium Galactica");
		setBackground(Color.BLACK);
		fadeTimer = new Timer(FADE_TIME, null);
		uis = new UISounds(root);
		cgfx = new CommonGFX(root);
		
		// initialize renderers
		smr = new StarmapRenderer(new StarmapGFX(root), cgfx, uis);
		pr = new PlanetRenderer(new PlanetGFX(root), cgfx, uis);
		ir = new InformationRenderer(new InformationGFX(root), cgfx, uis);
		mmr = new MainmenuRenderer(new MenuGFX(root), cgfx.text);
		mov = new MovieSurface();
		player = new Player(mov);
		
		screens = new JComponent[] {
			smr, pr, ir, mmr, mov
		};
		
		// setup renderers
		mmr.setVisible(true);
		mmr.setVersion(version);
		mmr.setRandomPicture();
		smr.setVisible(false);
		ir.setVisible(false);
		pr.setVisible(false);
		setListeners();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				uis.close();
				smr.stopAnimations();
				player.setOnCompleted(null);
				player.stopAndWait();
			}
		});

		setKeyboard();
		
		layers = new JLayeredPane();
		layers.add(smr, Integer.valueOf(0));
		layers.add(pr, Integer.valueOf(1));
		layers.add(ir, Integer.valueOf(2));
		layers.add(mmr, Integer.valueOf(3));

		layers.add(mov, Integer.valueOf(4));
		
		GroupLayout gl = new GroupLayout(layers);
		layers.setLayout(gl);
		gl.setHorizontalGroup(gl.createParallelGroup()
			.addComponent(mmr, 640, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(smr, 640, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(pr, 640, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(ir, 640, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(mov, 640, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		);
		gl.setVerticalGroup(
			gl.createParallelGroup()
			.addComponent(mmr, 480, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(smr, 480, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(pr, 480, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(ir, 480, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(mov, 480, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
		
		initModel();
		smr.startAnimations();
		setVisible(true);
	}
	private void setKeyboard() {
		JRootPane rp = getRootPane();
		
		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0, false);
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "F2");
		rp.getActionMap().put("F2", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { onF2Action(); }});
		
		ks = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0, false);
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "F3");
		rp.getActionMap().put("F3", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { onF3Action(); }});

		ks = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0, false);
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "F7");
		rp.getActionMap().put("F7", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { onF7Action(); }});

		ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "ESC");
		rp.getActionMap().put("ESC", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { onESCAction(); }});
		
	}
	/**
	 * Sets action listeners on the various screens.
	 */
	private void setListeners() {
		smr.setOnColonyClicked(new BtnAction() { public void invoke() { onStarmapColony(); }});
		smr.setOnInformationClicked(new BtnAction() { public void invoke() { onStarmapInfo(); }});
		pr.setOnStarmapClicked(new BtnAction() { public void invoke() { onColonyStarmap(); }});
		pr.setOnInformationClicked(new BtnAction() { public void invoke() { onColonyInfo(); }});
		ir.setOnStarmapClicked(new BtnAction() { public void invoke() { onInfoStarmap(); }});
		ir.setOnColonyClicked(new BtnAction() { public void invoke() { onInfoColony(); }});
		pr.setOnPlanetsClicked(new BtnAction() { public void invoke() { onColonyPlanets(); }});
		
		mmr.setStartNewAction(new BtnAction() { public void invoke() { onStarmap(); }});
		mmr.setTitleAnimAction(new BtnAction() { public void invoke() { onTitle(); }});
		mmr.setIntroAction(new BtnAction() { public void invoke() { onIntro(); }});
		mmr.setQuitAction(new BtnAction() { public void invoke() { onQuit(); }});
	}
	/** Go to starmap from main menu. */
	private void onStarmap() {
		showScreen(smr);
	}
	/** Quit pressed on starmap. */
	private void onQuit() {
		dispose();
	}
	private void onStarmapColony() {
		showScreen(pr);
	}
	private void onStarmapInfo() {
		ir.setScreenButtonsFor(InfoScreen.PLANETS);
		ir.setVisible(true);
		layers.validate();
	}
	private void onColonyStarmap() {
		showScreen(smr);
	}
	private void onColonyPlanets() {
		ir.setScreenButtonsFor(InfoScreen.PLANETS);
		ir.setVisible(true);
		layers.validate();
	}
	private void onColonyInfo() {
		ir.setScreenButtonsFor(InfoScreen.COLONY_INFORMATION);
		ir.setVisible(true);
		layers.validate();
	}
	private void onInfoStarmap() {
		showScreen(smr);
	}
	private void onInfoColony() {
		showScreen(pr);
	}
	/**
	 * @param args
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
	private void onF2Action() {
		if (!player.isPlayback()) {
			if (!smr.isVisible()) {
				uis.playSound("Starmap");
				showScreen(smr);
			}
		}
	}
	private void onF3Action() {
		if (!player.isPlayback()) {
			if (!pr.isVisible()) {
				uis.playSound("Colony");
				showScreen(pr);
			}
		}
	}
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
		for (SurfaceType st : SurfaceType.values()) {
			GMPlanet p = new GMPlanet();
			p.name = "Planet " + st.surfaceIndex;
			p.radarRadius = 50;
			p.showName = true;
			p.showRadar = true;
			p.surfaceType = st;
			p.surfaceVariant = 1;
			p.visible = true;
			p.x = 100 + st.surfaceIndex * 50;
			p.y = 100 + st.surfaceIndex * 50;
			p.nameColor = TextGFX.GALACTIC_EMPIRE_ST;
			p.rotationDirection = st.surfaceIndex % 2 == 0;
			smr.planets.add(p);
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
		}
	}
}
