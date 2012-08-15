/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.core.Func0;
import hu.openig.utils.ConsoleWatcher;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.xml.stream.XMLStreamException;

/**
 * The campaign editor.
 * @author akarnokd, 2012.08.15.
 */
public class CampaignEditor extends JFrame {
	/** */
	private static final long serialVersionUID = -4044298769130516091L;
	/** The main version. */
	public static final String VERSION = "0.01";
	/** The configuration file. */
	public static final String CONFIG_FILE = "open-ig-ce-config.xml";
	/** The console watcher. */
	public static Closeable consoleWatcher;
	/** The UI language. */
	public static String language = "en";
	/**
	 * Initialize the GUI.
	 */
	public CampaignEditor() {
		super("Open-IG Campaign Editor v" + VERSION);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				doExit();
			}
		});
		initComponents();
		pack();
		if (getWidth() < 640) {
			setBounds(getX(), getY(), 640, getHeight());
		}
		if (getHeight() < 480) {
			setBounds(getX(), getY(), getWidth(), 480);
		}
		setLocationRelativeTo(null);
	}
	/**
	 * Load and restore the window state based on the configuration.
	 */
	public void loadConfig() {
		File cf = new File(CONFIG_FILE);
		if (cf.canRead()) {
			try {
				loadConfigXML(XElement.parseXML(cf));
			} catch (XMLStreamException ex) {
				ex.printStackTrace();
			}
		}
	}
	/**
	 * Load the configuration from the XML.
	 * @param xml the XML
	 */
	void loadConfigXML(XElement xml) {
		loadWindowState(this, xml.childElement("main-window"));
	}
	/**
	 * Load the window state from the specified XML element.
	 * @param w the target frame
	 * @param xml the xml element
	 */
	public static void loadWindowState(JFrame w, XElement xml) {
		if (xml == null) {
			return;
		}
		int state = xml.getInt("window-state", w.getExtendedState());
		if (state != JFrame.MAXIMIZED_BOTH) {
			int x = xml.getInt("window-x", w.getX());
			int y = xml.getInt("window-y", w.getY());
			int width = xml.getInt("window-width", w.getWidth());
			int height = xml.getInt("window-height", w.getHeight());
			w.setExtendedState(state);
			w.setBounds(x, y, width, height);
		} else {
			w.setExtendedState(state);
		}
	}
	/**
	 * Save the window state into the given XML element.
	 * @param w the window
	 * @param xml the output
	 */
	public static void saveWindowState(JFrame w, XElement xml) {
		int state = w.getExtendedState();
		xml.set("window-state", state);
		if (state != JFrame.MAXIMIZED_BOTH) {
			xml.set("window-x", w.getX());
			xml.set("window-y", w.getY());
			xml.set("window-width", w.getWidth());
			xml.set("window-height", w.getHeight());
		}
	}
	/**
	 * Save the current configuration.
	 */
	public void saveConfig() {
		File cf = new File(CONFIG_FILE);
		try {
			saveConfigXML().save(cf);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * @return create an XML representation of the configuration
	 */
	XElement saveConfigXML() {
		XElement result = new XElement("open-ig-campaign-editor-config");
		
		saveWindowState(this, result.add("main-window"));
		
		return result;
	}
	/**
	 * Exit the editor.
	 */
	void doExit() {
		try {
			saveConfig();
		} finally {
			U.close(consoleWatcher);
			consoleWatcher = null;
			dispose();
		}
	}
	/**
	 * Program entry.
	 * @param args no arguments
	 */
	public static void main(final String[] args) {
		Set<String> argSet = U.newHashSet(args);

		if (argSet.contains("-en")) {
			language = "en";
		} else
		if (argSet.contains("-hu")) {
			language = "hu";
		} else {
			language = "en";
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				consoleWatcher = new ConsoleWatcher(args, VERSION, new Func0<String>() {
					@Override
					public String invoke() {
						return language;
					}
				}, null);
				CampaignEditor ce = new CampaignEditor();
				ce.loadConfig();
				ce.setVisible(true);
			}
		});
	}
	/**
	 * Initialize the internal components.
	 */
	void initComponents() {
		
	}
}
