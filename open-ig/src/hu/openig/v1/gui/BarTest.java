/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.gui;

import hu.openig.v1.Act;
import hu.openig.v1.Configuration;
import hu.openig.v1.ResourceLocator;
import hu.openig.v1.core.TalkState;
import hu.openig.v1.core.Talks;

import java.awt.Container;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

/**
 * Test bar talks.
 * @author karnok, 2009.10.09.
 * @version $Revision 1.0$
 */
public class BarTest extends JFrame {
	/** */
	private static final long serialVersionUID = -227388662977233871L;
	/** The walk settings. */
	protected Talks talks;
	/** The walk painter. */
	protected BarPainter barpainter;
	/** The resource locator. */
	protected ResourceLocator rl;
	/** The current language. */
	protected String lang;
	/**
	 * Constructor.
	 * @param talks the talks
	 * @param rl the resource locator
	 * @param lang the language
	 */
	public BarTest(final Talks talks, ResourceLocator rl, String lang) {
		super("Bar test");
		this.talks = talks;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);

		barpainter = new BarPainter(rl, lang);
		
		JMenuBar menu = new JMenuBar();
		JMenu mnuOptions = new JMenu("Talks");
		menu.add(mnuOptions);
		
		for (String s : talks.persons.keySet()) {
			final String fs = s;
			JMenuItem mnuItem = new JMenuItem(s);
			mnuItem.addActionListener(new Act() {
				@Override
				public void act() {
					barpainter.person = talks.persons.get(fs);
					barpainter.state = barpainter.person.states.get(TalkState.START);
					repaint();
				}
			});
			mnuOptions.add(mnuItem);
		}
		setJMenuBar(menu);
		barpainter.person = talks.persons.get("brian");
		barpainter.state = barpainter.person.states.get(TalkState.START);
		
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addComponent(barpainter, 640, 640, Short.MAX_VALUE)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(barpainter, 480, 480, Short.MAX_VALUE)
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
		final Talks w = new Talks();
		final ResourceLocator rl = new ResourceLocator();
		rl.setContainers(config.containers);
		rl.scanResources();
		w.load(rl, "hu", "campaign/main");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new BarTest(w, rl, "hu").setVisible(true);
			}
		});
	}
}
