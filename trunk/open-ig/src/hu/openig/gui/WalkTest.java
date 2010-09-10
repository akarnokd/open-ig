/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.gui;

import hu.openig.core.Configuration;
import hu.openig.core.ResourceLocator;
import hu.openig.model.Walks;

import java.awt.Container;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Test ship walking.
 * @author karnok, 2009.10.09.
 * @version $Revision 1.0$
 */
public class WalkTest extends JFrame {
	/** */
	private static final long serialVersionUID = -227388662977233871L;
	/** The walk settings. */
	protected Walks walks;
	/** The walk painter. */
	protected WalkPainter walkpainter;
	/** The resource locator. */
	protected ResourceLocator rl;
	/** The current language. */
	protected String lang;
	/**
	 * Constructor.
	 * @param walks the walks
	 * @param rl the resource locator
	 * @param lang the language
	 */
	public WalkTest(Walks walks, ResourceLocator rl, String lang) {
		this.walks = walks;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		Container c = getRootPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);

		walkpainter = new WalkPainter(rl, lang);
		
		walkpainter.ship = walks.ships.get("level3");
		walkpainter.position = walkpainter.ship.positions.get("bridge");
		
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addComponent(walkpainter, 640, 640, Short.MAX_VALUE)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(walkpainter, 480, 480, Short.MAX_VALUE)
		);
		pack();
		setLocationRelativeTo(null);
	}
	/**
	 * Main program.
	 * @param args no arguments
	 */
	public static void main(String[] args) {
		Configuration config = new Configuration("open-ig-config.xml");
		config.load();
		final Walks w = new Walks();
		final ResourceLocator rl = new ResourceLocator();
		rl.setContainers(config.containers);
		rl.scanResources();
		w.load(rl, "hu", "campaign/main/walks");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new WalkTest(w, rl, "hu").setVisible(true);
			}
		});
	}
}
