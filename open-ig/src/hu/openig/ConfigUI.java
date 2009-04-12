/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/**
 * Extra configuration window for the application.
 * Here are those new settings which cannot be put into the custom graphics
 * of the original screens.
 * @author karnokd
 *
 */
public class ConfigUI extends JFrame {
	/** */
	private static final long serialVersionUID = -5027787006572177827L;
	/** The configuration tabs. */
	private JTabbedPane tabs;
	/** Constructor. */
	public ConfigUI() {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		tabs = new JTabbedPane();
		
		tabs.add("Graphics", new JPanel());
		tabs.add("Sound", new JPanel());
		tabs.add("Game", new JPanel());
		tabs.add("Network", new JPanel());
		tabs.add("About", new JPanel());
		
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		c.add(tabs, BorderLayout.CENTER);
		
		setLocationRelativeTo(null);
	}
	/**
	 * Main program to independently test the ui.
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ConfigUI cui = new ConfigUI();
				cui.setVisible(true);
			}
		});
//		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//		for (GraphicsDevice sd : ge.getScreenDevices()) {
//			System.out.printf("%s%n", sd.getIDstring());
//			for (GraphicsConfiguration gc : sd.getConfigurations()) {
//				Rectangle rect = gc.getBounds();
//				System.out.printf("  %dx%d%n", rect.width, rect.height);
//			}
//			for (DisplayMode dm : sd.getDisplayModes()) {
//				System.out.printf("! %dx%d, %dbit, %dHz%n", dm.getWidth(), dm.getHeight(), dm.getBitDepth(), dm.getRefreshRate());
//			}
//		}
//		GraphicsDevice gd = ge.getDefaultScreenDevice();
//		ConfigUI w = new ConfigUI();
//		w.setUndecorated(true);
//		gd.setFullScreenWindow(w);
//		//gd.setDisplayMode(new DisplayMode(1024, 768, 32, 60));
//		try {
//			TimeUnit.SECONDS.sleep(5);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		gd.setFullScreenWindow(null);
	}
}
