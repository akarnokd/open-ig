/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.gui;

import hu.openig.core.Act;
import hu.openig.core.Configuration;
import hu.openig.core.ResourceLocator;

import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.GroupLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Test the new starmap talks.
 * @author karnok, 2009.10.09.
 * @version $Revision 1.0$
 */
public class StarmapTest extends JFrame {
	/** */
	private static final long serialVersionUID = -227388662977233871L;
	/** The walk painter. */
	protected StarmapPainter starmapPainter;
	/** The resource locator. */
	protected ResourceLocator rl;
	/**
	 * Switch language on the components.
	 * @param lang the language
	 */
	void switchLanguage(String lang) {
		starmapPainter.changeLanguage(lang);
		repaint();
	}
	/**
	 * Constructor.
	 * @param rl the resource locator
	 * @param lang the language
	 */
	public StarmapTest(ResourceLocator rl, String lang) {
		super("Starmap test");
		this.rl = rl;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		Container c = getContentPane();
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				starmapPainter.stop();
			}
		});
		
		JPanel panel = new JPanel();
		panel.setDoubleBuffered(true);
		c.add(panel);
		GroupLayout gl = new GroupLayout(panel);
		panel.setLayout(gl);

		starmapPainter = new StarmapPainter(rl, lang);
		
		JMenuBar menu = new JMenuBar();
		
		JMenu mnuLanguage = new JMenu("Language");
		JMenuItem mi1 = new JMenuItem("English");
		mi1.addActionListener(new Act() { public void act() { switchLanguage("en"); } });
		JMenuItem mi2 = new JMenuItem("Hungarian");
		mi2.addActionListener(new Act() { public void act() { switchLanguage("hu"); } });
		mnuLanguage.add(mi1);
		mnuLanguage.add(mi2);
		menu.add(mnuLanguage);
		
		JMenu mnuView = new JMenu("View");
		final JCheckBoxMenuItem c1 = new JCheckBoxMenuItem("Show right panel", starmapPainter.isRightPanelVisible());
		c1.addActionListener(new Act() {
			@Override
			public void act() {
				starmapPainter.setRightPanelVisible(c1.isSelected());
				starmapPainter.repaint();
			}
		});
		final JCheckBoxMenuItem c2 = new JCheckBoxMenuItem("Show bottom panel", starmapPainter.isBottomPanelVisible());
		c2.addActionListener(new Act() {
			@Override
			public void act() {
				starmapPainter.setBottomPanelVisible(c2.isSelected());
				starmapPainter.repaint();
			}
		});
		final JCheckBoxMenuItem c3 = new JCheckBoxMenuItem("Show minimap", starmapPainter.isMinimapVisible());
		c3.addActionListener(new Act() {
			@Override
			public void act() {
				starmapPainter.setMinimapVisible(c3.isSelected());
				starmapPainter.repaint();
			}
		});
		final JCheckBoxMenuItem c4 = new JCheckBoxMenuItem("Show scrollbars", starmapPainter.isScrollbarsVisible());
		c4.addActionListener(new Act() {
			@Override
			public void act() {
				starmapPainter.setScrollbarsVisible(c4.isSelected());
				starmapPainter.repaint();
			}
		});
		
		mnuView.add(c1);
		mnuView.add(c2);
		mnuView.add(c3);
		mnuView.add(c4);
		
		menu.add(mnuView);
		
		setJMenuBar(menu);
		
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addComponent(starmapPainter, 640, 640, Short.MAX_VALUE)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(starmapPainter, 480, 480, Short.MAX_VALUE)
		);
		pack();
		setMinimumSize(getSize());
		setLocationRelativeTo(null);
	}
	/**
	 * Main program.
	 * @param args no arguments
	 */
	public static void main(String[] args) {
		final Configuration config = new Configuration("open-ig-config.xml");
		config.load();
		final ResourceLocator rl = config.newResourceLocator();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new StarmapTest(rl, config.language).setVisible(true);
			}
		});
	}
}
