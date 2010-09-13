/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.test;

import hu.openig.core.Configuration;
import hu.openig.core.Labels;
import hu.openig.core.ResourceLocator;
import hu.openig.gfx.DatabaseGFX;
import hu.openig.model.Player;
import hu.openig.model.World;
import hu.openig.render.TextRenderer;

import java.awt.Container;
import java.util.Arrays;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Test bar talks.
 * @author karnok, 2009.10.09.
 * @version $Revision 1.0$
 */
public class DatabaseTest extends JFrame {
	/** */
	private static final long serialVersionUID = -227388662977233871L;
	/** The resource locator. */
	protected ResourceLocator rl;
	/** The current language. */
	protected String lang;
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

		DatabaseGFX dbGFX = new DatabaseGFX(rl);
		dbGFX.load(lang);
		
		TextRenderer txt = new TextRenderer(rl);
		
		DatabasePainter p = new DatabasePainter(dbGFX, txt);
		World w = new World();
		w.level = 5;
		w.player = new Player();
		w.player.discoveredAliens.addAll(Arrays.asList(
				"race.free_traders_alliance", 
				"race.free_nations_society",
				"race.pirates",
				"race.garthogs",
				"race.ychoms",
				"race.morgaths",
				"race.dribs",
				"race.ecaleps",
				"race.sulleps",
				"race.dargslans"
		));
		p.setWorld(w);
		
		Labels labels = new Labels();
		labels.load(rl, lang, null);
		
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
		final ResourceLocator rl = new ResourceLocator();
		rl.setContainers(config.containers);
		rl.scanResources();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new DatabaseTest(rl, "hu").setVisible(true);
			}
		});
	}
}
