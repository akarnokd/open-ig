/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.gui;

import hu.openig.v1.core.Act;
import hu.openig.v1.core.Configuration;
import hu.openig.v1.core.Labels;
import hu.openig.v1.core.ResourceLocator;
import hu.openig.v1.core.TalkPerson;
import hu.openig.v1.core.TalkSpeech;
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
	/**
	 * Switch language on the components.
	 * @param lang the language
	 */
	void switchLanguage(String lang) {
		barpainter.labels.load(rl, lang, "campaign/main");
		barpainter.lang = lang;
		barpainter.setState(barpainter.state);
		repaint();
	}
	/**
	 * Reset the visit state of the speeches.
	 * @param person the person
	 */
	void resetVisits(TalkPerson person) {
		for (TalkState ts : person.states.values()) {
			for (TalkSpeech tsp : ts.speeches) {
				tsp.spoken = false;
			}
		}
	}
	/**
	 * Constructor.
	 * @param talks the talks
	 * @param rl the resource locator
	 * @param lang the language
	 */
	public BarTest(final Talks talks, ResourceLocator rl, String lang) {
		super("Bar test");
		this.talks = talks;
		this.rl = rl;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);

		Labels lbl = new Labels();
		lbl.load(rl, lang, "campaign/main");
		
		barpainter = new BarPainter(rl, lang, lbl);
		
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
					barpainter.setState(barpainter.person.states.get(TalkState.START));
					resetVisits(barpainter.person);
					repaint();
				}
			});
			mnuOptions.add(mnuItem);
		}
		JMenu mnuLanguage = new JMenu("Language");
		JMenuItem mi1 = new JMenuItem("English");
		mi1.addActionListener(new Act() { public void act() { switchLanguage("en"); } });
		JMenuItem mi2 = new JMenuItem("Hungarian");
		mi2.addActionListener(new Act() { public void act() { switchLanguage("hu"); } });
		mnuLanguage.add(mi1);
		mnuLanguage.add(mi2);
		menu.add(mnuLanguage);
		
		setJMenuBar(menu);
		barpainter.person = talks.persons.get("brian");
		barpainter.setState(barpainter.person.states.get(TalkState.START));
		
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
		final Configuration config = new Configuration("open-ig-config.xml");
		config.load();
		final Talks w = new Talks();
		final ResourceLocator rl = new ResourceLocator();
		rl.setContainers(config.containers);
		rl.scanResources();
		w.load(rl, config.language, "campaign/main");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new BarTest(w, rl, config.language).setVisible(true);
			}
		});
	}
}
