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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

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
		tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		c.add(tabs, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(null);
	}
	/**
	 * Save frame properties.
	 * @param frame the frame to save
	 * @throws IOException if the config file has problems
	 */
	void saveFrame(JFrame frame) throws IOException {
	    Properties props = new Properties();
	    props.setProperty("State", String.valueOf(frame.getExtendedState()));
	    props.setProperty("X", String.valueOf(frame.getX()));
	    props.setProperty("Y", String.valueOf(frame.getY()));
	    props.setProperty("W", String.valueOf(frame.getWidth()));
	    props.setProperty("H", String.valueOf(frame.getHeight()));
	    OutputStream out = new FileOutputStream("config.xml");
	    try {
	    	props.storeToXML(out, null);
	    } finally {
	    	out.close();
	    }
	}
	/**
	 * Load a frame properties.
	 * @param frame the target frame to instantiate
	 * @throws IOException if the config file has problems
	 */
	void loadFrame(JFrame frame) throws IOException {
	    Properties props = new Properties();
	    InputStream in = new FileInputStream("config.xml");
	    try {
		    props.loadFromXML(in);
		    int extendedState = Integer.parseInt(props.getProperty("State", String.valueOf(frame.getExtendedState())));
		    if (extendedState != JFrame.MAXIMIZED_BOTH) {
		        frame.setBounds(
	        		Integer.parseInt(props.getProperty("X", String.valueOf(frame.getX()))),
					Integer.parseInt(props.getProperty("Y", String.valueOf(frame.getY()))),
					Integer.parseInt(props.getProperty("W", String.valueOf(frame.getWidth()))),
					Integer.parseInt(props.getProperty("H", String.valueOf(frame.getHeight())))
		        );
		    } else {
		        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		    }
	    } finally {
	    	in.close();
	    }
	}	/**
	 * Main program to independently test the ui.
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		for (Map.Entry<?, ?>  e : System.getProperties().entrySet()) {
			System.out.printf("%s = %s%n", e.getKey(), e.getValue());
		}
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
