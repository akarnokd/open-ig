/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.test;

import hu.openig.core.Act;
import hu.openig.core.Configuration;
import hu.openig.core.Labels;
import hu.openig.core.ResourceLocator;
import hu.openig.gfx.DatabaseGFX;
import hu.openig.model.Player;
import hu.openig.model.World;
import hu.openig.render.TextRenderer;
import hu.openig.screens.GameControls;
import hu.openig.screens.ScreenBase;
import hu.openig.screens.Screens;

import java.awt.Container;
import java.awt.FontMetrics;
import java.util.concurrent.Executors;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Test bar talks.
 * @author akarnokd, 2009.10.09.
 */
public class DatabaseTest extends JFrame {
	/** */
	private static final long serialVersionUID = -227388662977233871L;
	/** The resource locator. */
	protected ResourceLocator rl;
	/** @return The game controls. */
	public GameControls controls() {
		return new GameControls() {

			@Override
			public void switchLanguage(String newLanguage) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public ScreenBase displayPrimary(Screens screen) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ScreenBase displaySecondary(Screens screen) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void hideSecondary() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void playVideos(String... videos) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void playVideos(Act onComplete, String... videos) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void displayStatusbar() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void hideStatusbar() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void exit() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void repaintInner() {
				repaint();
				
			}

			@Override
			public int getInnerWidth() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getInnerHeight() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public void repaintInner(int x, int y, int w, int h) {
				repaint();
			}

			@Override
			public FontMetrics fontMetrics(int size) {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
	}
	/**
	 * Constructor.
	 * @param rl the resource locator
	 * @param lang the language
	 */
	public DatabaseTest(ResourceLocator rl, String lang) {
		super("Database test");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);

		DatabaseGFX dbGFX = new DatabaseGFX().load(rl);
		
		TextRenderer txt = new TextRenderer(rl);
		
		DatabasePainter p = new DatabasePainter(dbGFX, txt);
		World w = new World(Executors.newCachedThreadPool(), controls());
		w.level = 5;
		w.player = new Player();
//		w.player.discoveredAliens.addAll(Arrays.asList(
//				"race.free_traders_alliance", 
//				"race.free_nations_society",
//				"race.pirates",
//				"race.garthogs",
//				"race.ychoms",
//				"race.morgaths",
//				"race.dribs",
//				"race.ecaleps",
//				"race.sulleps",
//				"race.dargslans"
//		));
		p.setWorld(w);
		
		Labels labels = new Labels().load(rl, null);
		
		p.setLabels(labels);
		
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addComponent(p, 640, 640, Short.MAX_VALUE)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(p, 480, 480, Short.MAX_VALUE)
		);
		pack();
		setLocationRelativeTo(null);
		setMinimumSize(getSize());
	}
	/**
	 * Main program.
	 * @param args no arguments
	 */
	public static void main(String[] args) {
		Configuration config = new Configuration("open-ig-config.xml");
		config.load();
		final ResourceLocator rl = config.newResourceLocator();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new DatabaseTest(rl, "hu").setVisible(true);
			}
		});
	}
}
