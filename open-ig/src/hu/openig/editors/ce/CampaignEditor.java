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
import hu.openig.utils.Exceptions;
import hu.openig.utils.IOUtils;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.undo.UndoManager;
import javax.xml.stream.XMLStreamException;

/**
 * The campaign editor.
 * @author akarnokd, 2012.08.15.
 */
public class CampaignEditor extends JFrame implements CEContext {
	/**
	 * A simple directory filter.
	 * @author akarnokd, 2012.11.01.
	 */
	private final class DirFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}
	}

	/** */
	private static final long serialVersionUID = -4044298769130516091L;
	/** The main version. */
	public static final String VERSION = "0.02";
	/** The configuration file. */
	public static final String CONFIG_FILE = "open-ig-ce-config.xml";
	/** The console watcher. */
	public static Closeable consoleWatcher;
	/** The UI language. */
	public static String language = "en";
	/** The undo manager. */
	UndoManager undoManager;
	/** The main menu. */
	JMenuBar mainMenu;
	/** The main split that divides the tabs and the error/warning panel. */
	JSplitPane mainSplit;
	/** The tabs. */
	JTabbedPane tabs;
	/** The error panel. */
	JPanel problemPanel;
	/** The warning icon. */
	ImageIcon warning;
	/** The error icon. */
	ImageIcon error;
	/** The toolbar. */
	JToolBar toolbar;
	/** The label map. */
	Map<String, String> labels;
	/** The language flags. */
	Map<String, ImageIcon> flags;
	/** Panel. */
	CETechnologyPanel technologiesPanel;
	/** Labels. */
	CELabelsPanel labelsPanel;
	/** The main IG labels. */
	Map<String, Map<String, String>> mainLabels = U.newHashMap();
	/** The project's language. */
	String projectLanguage;
	/** The default directories. */
	String[] defaultDirectories = { "data", "audio", "images", "video" };
	/** The working directory. */
	File workDir = new File(".");
	/** The languages. */
	List<String> languages = U.newArrayList();
	/** The startup dialog. */
	CEStartupDialog startupDialog;
	/** Undo menu item. */
	JMenuItem mnuEditUndo;
	/** Redo menu item. */
	JMenuItem mnuEditRedo;
	/** Menu item. */
	JMenuItem mnuFileNew;
	/** Menu item. */
	JMenuItem mnuFileOpen;
	/** Menu item. */
	JMenuItem mnuFileRecent;
	/** Menu item. */
	JMenuItem mnuFileSave;
	/** Menu item. */
	JMenuItem mnuFileImport;
	/** Menu item. */
	JMenuItem mnuFileExport;
	/** Menu item. */
	JMenuItem mnuFileExit;
	/** Menu item. */
	JMenuItem mnuHelpOnline;
	/** Menu item. */
	JMenuItem mnuHelpAbout;
	/** Menu item. */
	JMenuItem mnuEditCut;
	/** Menu item. */
	JMenuItem mnuEditCopy;
	/** Menu item. */
	JMenuItem mnuEditPaste;
	/** Menu item. */
	JMenuItem mnuEditDelete;
	/** Menu item. */
	JMenuItem mnuFileSaveAs;
	/** Toolbar item. */
	AbstractButton toolbarCut;
	/** Toolbar item. */
	AbstractButton toolbarCopy;
	/** Toolbar item. */
	AbstractButton toolbarPaste;
	/** Toolbar item. */
	AbstractButton toolbarRemove;
	/** Toolbar item. */
	AbstractButton toolbarUndo;
	/** Toolbar item. */
	AbstractButton toolbarRedo;
	/** Toolbar item. */
	AbstractButton toolbarNew;
	/** Toolbar item. */
	AbstractButton toolbarOpen;
	/** Toolbar item. */
	AbstractButton toolbarSave;
	/** Toolbar item. */
	AbstractButton toolbarImport;
	/** Toolbar item. */
	AbstractButton toolbarExport;
	/** Toolbar item. */
	AbstractButton toolbarSaveAs;
	/** Toolbar item. */
	AbstractButton toolbarHelp;
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
		
		for (XElement xpref : xml.childrenWithName("panel-preference")) {
			String id = xpref.get("id");
			for (Component c : tabs.getComponents()) {
				if (c instanceof CEPanelPreferences) {
					CEPanelPreferences pp = (CEPanelPreferences) c;
					if (pp.preferencesId().equals(id)) {
						pp.loadPreferences(xpref);
					}
				}
			}
		}
		
		for (XElement xpref : xml.childrenWithName("dialog-preference")) {
			String id = xpref.get("id");
			if (id.equals(startupDialog.preferencesId())) {
				loadWindowState(startupDialog, xpref);
				startupDialog.loadPreferences(xpref);
			}
		}
	}
	/**
	 * Load the window state from the specified XML element.
	 * @param w the target frame
	 * @param xml the xml element
	 */
	public static void loadWindowState(Frame w, XElement xml) {
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
	 * Load the window state from the specified XML element.
	 * @param w the target frame
	 * @param xml the xml element
	 */
	public static void loadWindowState(JDialog w, XElement xml) {
		if (xml == null) {
			return;
		}
		int x = xml.getInt("window-x", w.getX());
		int y = xml.getInt("window-y", w.getY());
		int width = xml.getInt("window-width", w.getWidth());
		int height = xml.getInt("window-height", w.getHeight());
		w.setBounds(x, y, width, height);
	}
	/**
	 * Save the window state into the given XML element.
	 * @param w the window
	 * @param xml the output
	 */
	public static void saveWindowState(Frame w, XElement xml) {
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
	 * Save the window state into the given XML element.
	 * @param w the window
	 * @param xml the output
	 */
	public static void saveWindowState(Dialog w, XElement xml) {
		xml.set("window-x", w.getX());
		xml.set("window-y", w.getY());
		xml.set("window-width", w.getWidth());
		xml.set("window-height", w.getHeight());
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
		
		for (Component c : tabs.getComponents()) {
			if (c instanceof CEPanelPreferences) {
				CEPanelPreferences pp = (CEPanelPreferences) c;
				XElement xpref = result.add("panel-preference");
				xpref.set("id", pp.preferencesId());
				pp.savePreferences(xpref);
			}
		}

		XElement xpref0 = result.add("dialog-preference");
		xpref0.set("id", startupDialog.preferencesId());
		saveWindowState(startupDialog, xpref0);
		startupDialog.savePreferences(xpref0);
		
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
// FIXME language = "hu";
			language = "en";
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
				ce.startupDialog.setVisible(true);
			}
		});
	}
	/**
	 * Initialize the internal components.
	 */
	void initComponents() {
		undoManager = new UndoManager();
		
		warning = new ImageIcon(getClass().getResource("/hu/openig/gfx/warning.png"));
		error = new ImageIcon(getClass().getResource("/hu/openig/gfx/error.png"));
		
		labels = U.newHashMap();
		flags = U.newHashMap();
		
		// fetch labels
		try {
			XElement xlabels = XElement.parseXML(getClass().getResource("ce_labels.xml"));
			for (XElement xlang : xlabels.childrenWithName("language")) {
				String id = xlang.get("id");
				if (id.equals(language)) {
					for (XElement xentry : xlang.childrenWithName("entry")) {
						String key = xentry.get("key");
						if (key != null && !key.isEmpty() && xentry.content != null && !xentry.content.isEmpty()) {
							labels.put(key, xentry.content);
						}
					}
				}
				flags.put(id, new ImageIcon(getClass().getResource(xlang.get("flag"))));
			}
		} catch (XMLStreamException ex) {
			Exceptions.add(ex);
		} catch (IOException ex) {
			Exceptions.add(ex);
		}
		
		ToolTipManager.sharedInstance().setDismissDelay(120000);
		
		projectLanguage = language;
		
		initMainLabels();

		mainMenu = new JMenuBar();
		
		initMenu();

		toolbar = new JToolBar();
		
		initToolbar();
		
		
		mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		tabs = new JTabbedPane();
		
		initTabs();
		
		problemPanel = new JPanel();
		
		mainSplit.setTopComponent(tabs);
		mainSplit.setBottomComponent(problemPanel);
		mainSplit.setOneTouchExpandable(true);
		mainSplit.setResizeWeight(1d);
		
		setJMenuBar(mainMenu);
		
		Container c = getContentPane();
		
		c.add(toolbar, BorderLayout.PAGE_START);
		c.add(mainSplit, BorderLayout.CENTER);
		// TODO other stuff
		
		startupDialog = new CEStartupDialog(this);
	}
	/**
	 * Initialize the toolbar.
	 */
	void initToolbar() {
		// TODO create toolbar entries.

		toolbarCut = createFor("res/Cut24.gif", "Cut", mnuEditCut, false);
		toolbarCopy = createFor("res/Copy24.gif", "Copy", mnuEditCopy, false);
		toolbarPaste = createFor("res/Paste24.gif", "Paste", mnuEditPaste, false);
		toolbarRemove = createFor("res/Remove24.gif", "Remove", mnuEditDelete, false);
		toolbarUndo = createFor("res/Undo24.gif", "Undo", mnuEditUndo, false);
		toolbarRedo = createFor("res/Redo24.gif", "Redo", mnuEditRedo, false);
		toolbarNew = createFor("res/New24.gif", "New", mnuFileNew, false);
		toolbarOpen = createFor("res/Open24.gif", "Open", mnuFileOpen, false);
		toolbarSave = createFor("res/Save24.gif", "Save", mnuFileSave, false);
		toolbarImport = createFor("res/Import24.gif", "Import", mnuFileImport, false);
		toolbarExport = createFor("res/Export24.gif", "Export", mnuFileImport, false);
		toolbarSaveAs = createFor("res/SaveAs24.gif", "Save as", mnuFileSaveAs, false);
		toolbarHelp = createFor("res/Help24.gif", "Help", mnuHelpOnline, false);

		toolbar.add(toolbarNew);
		toolbar.add(toolbarOpen);
		toolbar.add(toolbarSave);
		toolbar.addSeparator();
		toolbar.add(toolbarImport);
		toolbar.add(toolbarExport);
		toolbar.add(toolbarSaveAs);
		toolbar.addSeparator();
		toolbar.add(toolbarCut);
		toolbar.add(toolbarCopy);
		toolbar.add(toolbarPaste);
		
		toolbar.add(toolbarRemove);
		toolbar.addSeparator();
		toolbar.add(toolbarUndo);
		toolbar.add(toolbarRedo);

		toolbar.addSeparator();
		toolbar.add(toolbarHelp);

	}
	/**
	 * Create a imaged button for the given menu item.
	 * @param graphicsResource the graphics resource location.
	 * @param tooltip the tooltip text
	 * @param inMenu the menu item to relay the click to.
	 * @param toggle create a toggle button?
	 * @return the button
	 */
	AbstractButton createFor(String graphicsResource, String tooltip, final JMenuItem inMenu, boolean toggle) {
		AbstractButton result = toggle ? new JToggleButton() : new JButton();
		URL res = getClass().getResource(graphicsResource);
		if (res != null) {
			result.setIcon(new ImageIcon(res));
		}
		result.setToolTipText(tooltip);
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				inMenu.doClick();
			}
		});
		return result;
	}
	/**
	 * Initialize the tabs.
	 */
	void initTabs() {
		labelsPanel = new CELabelsPanel(this);
		technologiesPanel = new CETechnologyPanel(this);
		
		// TODO initialize tabs
		tabs.addTab(get("Project"), null, new JPanel());
		tabs.addTab(get("Definition"), null, new JPanel());
		tabs.addTab(get("Labels"), null, labelsPanel);
		tabs.addTab(get("Galaxy"), null, new JPanel());
		tabs.addTab(get("Players"), null, new JPanel());
		tabs.addTab(get("Planets"), null, new JPanel());
		tabs.addTab(get("Technology"), null, technologiesPanel);
		tabs.addTab(get("Buildings"), null, new JPanel());
		tabs.addTab(get("Battle"), null, new JPanel());
		tabs.addTab(get("Diplomacy"), null, new JPanel());
		tabs.addTab(get("Bridge"), null, new JPanel());
		tabs.addTab(get("Talks"), null, new JPanel());
		tabs.addTab(get("Shipwalk"), null, new JPanel());
		tabs.addTab(get("Chat"), null, new JPanel());
		tabs.addTab(get("Test"), null, new JPanel());
	}
	/**
	 * Initialize the menu.
	 */
	void initMenu() {
		// TODO create menu items
		JMenu mnuFile = new JMenu(get("menu.file"));
		JMenu mnuEdit = new JMenu(get("menu.edit"));
		JMenu mnuView = new JMenu(get("menu.view"));
		JMenu mnuTools = new JMenu(get("menu.tools"));
		JMenu mnuHelp = new JMenu(get("menu.help"));

		// -----------------------

		mnuFileNew = new JMenuItem(get("menu.file.new"));
		
		mnuFileOpen = new JMenuItem(get("menu.file.open"));
		mnuFileRecent = new JMenuItem(get("menu.file.recent"));
		mnuFileSave = new JMenuItem(get("menu.file.save"));
		mnuFileSaveAs = new JMenuItem(get("menu.file.saveas"));
		mnuFileImport = new JMenuItem(get("menu.file.import"));
		mnuFileExport = new JMenuItem(get("menu.file.export"));
		
		mnuFileExit = new JMenuItem(get("menu.file.exit"));
		
		mnuFile.add(mnuFileNew);
		mnuFile.addSeparator();
		mnuFile.add(mnuFileOpen);
		mnuFile.add(mnuFileRecent);
		mnuFile.add(mnuFileSave);
		mnuFile.add(mnuFileSaveAs);
		mnuFile.addSeparator();
		mnuFile.add(mnuFileImport);
		mnuFile.add(mnuFileExport);
		mnuFile.addSeparator();
		mnuFile.add(mnuFileExit);
		
		// -----------------------
		mnuEditCut = new JMenuItem(get("menu.edit.cut"));
		mnuEditCopy = new JMenuItem(get("menu.edit.copy"));
		mnuEditPaste = new JMenuItem(get("menu.edit.paste"));
		mnuEditDelete = new JMenuItem(get("menu.edit.delete"));
		
		mnuEditUndo = new JMenuItem(get("menu.edit.undo"));
		mnuEditRedo = new JMenuItem(get("menu.edit.redo"));

		// -----------------------
		
		mnuHelpOnline = new JMenuItem(get("menu.help.online"));
		mnuHelpAbout = new JMenuItem(get("menu.help.about"));

		// -----------------------
		
		mainMenu.add(mnuFile);
		mainMenu.add(mnuEdit);
		mainMenu.add(mnuView);
		mainMenu.add(mnuTools);
		mainMenu.add(mnuHelp);
		
		// -----------------------
		updateUndoRedo();
	}
	@Override
	public XElement getXML(String resource) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<String> getText(String resource) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public byte[] getData(String resource) {
		return getData(projectLanguage, resource);
	}
	/** @return scan for all languages. */
	public List<String> getLanguages() {
		Set<String> result = U.newHashSet();
		
		File dlc = new File(workDir, "dlc");
		
		List<File> dirs = U.newArrayList();
		List<File> zips = U.newArrayList();
		// check unpacked dlcs
		File[] dlcDirs = dlc.listFiles(new DirFilter());
		if (dlcDirs != null) {
			dirs.addAll(Arrays.asList(dlcDirs));
		}
		for (String s : defaultDirectories) {
			dirs.add(new File(workDir, s));
		}

		File[] dlcZips = dlc.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".zip");
			}
		});
		if (dlcZips != null) {
			zips.addAll(Arrays.asList(dlcZips));
		}

		File[] normalZips = dlc.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String n = pathname.getName().toLowerCase();
				return pathname.isFile() && n.startsWith("open-ig-") && n.endsWith(".zip");
			}
		});
		if (normalZips != null) {
			zips.addAll(Arrays.asList(normalZips));
		}
		
		for (File f : dirs) {
			File[] fs = f.listFiles(new DirFilter());
			if (fs != null) {
				for (File fss : fs) {
					if (!fss.getName().equals("generic")) {
						result.add(fss.getName());
					}
				}
			}
		}
		
		for (File f : zips) {
			try {
				ZipFile zf = new ZipFile(f);
				try {
					Enumeration<? extends ZipEntry> zes = zf.entries();
					while (zes.hasMoreElements()) {
						ZipEntry ze = zes.nextElement();
						
						String name = ze.getName();
						int idx = name.indexOf('/');
						if (idx >= 0) {
							name = name.substring(0, idx);
						}
						result.add(name);
					}
				} finally {
					zf.close();
				}
				
			} catch (IOException ex) {
				// ignored
			}
		}
		
		return U.newArrayList(result);
	}
	/**
	 * Check if the given resource exists under the language or generic.
	 * @param language the language
	 * @param resource the resource with extension
	 * @return true if exists
	 */
	public boolean exists(String language, String resource) {
		File dlc = new File(workDir, "dlc");
		
		// check unpacked dlcs
		File[] dlcDirs = dlc.listFiles(new DirFilter());
		if (dlcDirs != null) {
			boolean result = scanDirsExist(language, resource, dlcDirs);
			if (result) {
				return true;
			}
		}
		// check dlc zips
		File[] dlcZips = dlc.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".zip");
			}
		});
		if (dlcZips != null) {
			boolean result = scanZipsExist(language, resource, dlcZips);
			if (result) {
				return result;
			}
		}
		// check master directories
		File[] normalDirs = new File[defaultDirectories.length];
		for (int i = 0; i < normalDirs.length; i++) {
			normalDirs[i] = new File(workDir, defaultDirectories[i]);
		}
		
		boolean result = scanDirsExist(language, resource, normalDirs);
		if (result) {
			return result;
		}
		
		// check dlc zips
		File[] upgradeZips = dlc.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String n = pathname.getName().toLowerCase();
				return pathname.isFile() && n.startsWith("open-ig-upgrade") && n.endsWith(".zip");
			}
		});
		if (upgradeZips != null) {
			Arrays.sort(upgradeZips, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					return o2.getName().toLowerCase().compareTo(o1.getName().toLowerCase());
				}
			});
			result = scanZipsExist(language, resource, upgradeZips);
			if (result) {
				return result;
			}
		}

		// check dlc zips
		File[] normalZips = dlc.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String n = pathname.getName().toLowerCase();
				return pathname.isFile() && n.startsWith("open-ig-") && !n.startsWith("open-ig-upgrade") && n.endsWith(".zip");
			}
		});
		if (normalZips != null) {
			result = scanZipsExist(language, resource, normalZips);
			if (result) {
				return result;
			}
		}

		return false;
	}
	/**
	 * Get a resource for the specified language (or generic).
	 * @param language the language
	 * @param resource the resource name with extension
	 * @return the data bytes or null if not found
	 */
	@Override
	public byte[] getData(String language, String resource) {
		File dlc = new File(workDir, "dlc");
		
		// check unpacked dlcs
		File[] dlcDirs = dlc.listFiles(new DirFilter());
		if (dlcDirs != null) {
			byte[] result = scanDirs(language, resource, dlcDirs);
			if (result != null) {
				return result;
			}
		}
		// check dlc zips
		File[] dlcZips = dlc.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".zip");
			}
		});
		if (dlcZips != null) {
			byte[] result = scanZips(language, resource, dlcZips);
			if (result != null) {
				return result;
			}
		}
		// check master directories
		File[] normalDirs = new File[defaultDirectories.length];
		for (int i = 0; i < normalDirs.length; i++) {
			normalDirs[i] = new File(workDir, defaultDirectories[i]);
		}
		
		byte[] result = scanDirs(language, resource, normalDirs);
		if (result != null) {
			return result;
		}
		
		// check dlc zips
		File[] upgradeZips = dlc.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String n = pathname.getName().toLowerCase();
				return pathname.isFile() && n.startsWith("open-ig-upgrade") && n.endsWith(".zip");
			}
		});
		if (upgradeZips != null) {
			Arrays.sort(upgradeZips, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					return o2.getName().toLowerCase().compareTo(o1.getName().toLowerCase());
				}
			});
			result = scanZips(language, resource, upgradeZips);
			if (result != null) {
				return result;
			}
		}

		// check dlc zips
		File[] normalZips = dlc.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String n = pathname.getName().toLowerCase();
				return pathname.isFile() && n.startsWith("open-ig-") && !n.startsWith("open-ig-upgrade") && n.endsWith(".zip");
			}
		});
		if (normalZips != null) {
			result = scanZips(language, resource, normalZips);
			if (result != null) {
				return result;
			}
		}

		return null;
	}
	/**
	 * Scan a set of zip files for a particular resource.
	 * @param language the language
	 * @param resource the resource
	 * @param dlcZips the zip files
	 * @return the data or null if not found
	 */
	public byte[] scanZips(String language, String resource, File[] dlcZips) {
		for (File f : dlcZips) {
			try {
				ZipFile zf = new ZipFile(f);
				try {
					ZipEntry ze = zf.getEntry(language + "/" + resource);
					if (ze != null) {
						return readEntry(zf, ze);
					}
					ze = zf.getEntry("generic/" + resource);
					if (ze != null) {
						return readEntry(zf, ze);
					}
				} finally {
					zf.close();
				}
			} catch (IOException ex) {
				// ignored
			}
		}
		return null;
	}
	/**
	 * Scan a set of zip files for a particular resource.
	 * @param language the language
	 * @param resource the resource
	 * @param dlcZips the zip files
	 * @return true if exists
	 */
	public boolean scanZipsExist(String language, String resource, File[] dlcZips) {
		for (File f : dlcZips) {
			try {
				ZipFile zf = new ZipFile(f);
				try {
					ZipEntry ze = zf.getEntry(language + "/" + resource);
					if (ze != null) {
						return true;
					}
					ze = zf.getEntry("generic/" + resource);
					if (ze != null) {
						return true;
					}
				} finally {
					zf.close();
				}
			} catch (IOException ex) {
				// ignored
			}
		}
		return false;
	}
	/**
	 * Scan the directories for a specific resource file.
	 * @param language the target language
	 * @param resource the resource path
	 * @param dlcDirs the directories
	 * @return the data or null if not found
	 */
	public byte[] scanDirs(String language, String resource, File[] dlcDirs) {
		for (File f : dlcDirs) {
			File res = new File(f, language + "/" + resource);
			if (res.canRead()) {
				return IOUtils.load(res);
			}
			res = new File(f, "generic/" + resource);
			if (res.canRead()) {
				return IOUtils.load(res);
			}
		}
		return null;
	}
	/**
	 * Scan the directories for a specific resource file.
	 * @param language the target language
	 * @param resource the resource path
	 * @param dlcDirs the directories
	 * @return true if the resource exists
	 */
	public boolean scanDirsExist(String language, String resource, File[] dlcDirs) {
		for (File f : dlcDirs) {
			File res = new File(f, language + "/" + resource);
			if (res.canRead()) {
				return true;
			}
			res = new File(f, "generic/" + resource);
			if (res.canRead()) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Reads a zip entry.
	 * @param zf the zip file
	 * @param ze the zip entry
	 * @return the byte array
	 * @throws IOException on error
	 */
	byte[] readEntry(ZipFile zf, ZipEntry ze) throws IOException {
		InputStream in = zf.getInputStream(ze);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buffer = new byte[8192];
			while (true) {
				int read = in.read(buffer);
				if (read > 0) {
					out.write(buffer, 0, read);
				} else
				if (read < 0) {
					break;
				}
			}
			return out.toByteArray();
		} finally {
			in.close();
		}
	}
	@Override
	public BufferedImage getImage(String resource) {
		byte[] data = getData(projectLanguage, resource);
		if (data != null) {
			try {
				return ImageIO.read(new ByteArrayInputStream(data));
			} catch (IOException ex) {
				// ignored
			}
		}
		return null;
	}
	@Override
	public ImageIcon getIcon(CESeverityIndicator indicator) {
		switch (indicator) {
		case WARNING:
			return warning;
		case ERROR:
			return error;
		default:
			return null;
		}
	}
	@Override
	public String get(String key) {
		if (key == null || key.isEmpty()) {
			return "";
		}
		String text = labels.get(key);
		if (text != null) {
			return text;
		}
		System.err.printf("\t\t<entry key='%s'>%s</entry>%n", XElement.sanitize(key), XElement.sanitize(key));
		labels.put(key, key);
		return key;
	}
	@Override
	public String format(String key, Object... params) {
		String fmt = get(key);
		return String.format(fmt, params);
	}
	@Override
	public String projectLanguage() {
		return projectLanguage;
	}
	@Override
	public String label(String language, String key) {
		if (key == null) {
			return null;
		}
		Map<String, String> lang = mainLabels.get(language);
		if (lang != null) {
			return lang.get(key);
		}
		return null;
	}
	@Override
	public void updateTab(Component c, String title, ImageIcon icon) {
		for (int i = 0; i < tabs.getTabCount(); i++) {
			Component c0 = tabs.getComponentAt(i);
			if (c == c0) {
				if (title != null) {
					tabs.setTitleAt(i, title);
				}
				tabs.setIconAt(i, icon);
				break;
			}
		}
	}
	@Override
	public void addUndo(CEUndoRedoSupport c, String name, XElement oldState, XElement newState) {
		undoManager.addEdit(new CEUndoRedoEntry(c, name, oldState, newState));
		updateUndoRedo();
	}
	@Override
	public void saveXML(String resource, XElement xml) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void saveText(String resource, Iterable<String> lines) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void saveText(String resource, CharSequence text) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void saveData(String resource, byte[] data) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void saveImage(String resource, BufferedImage image) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void delete(String resource) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void addProblem(CESeverityIndicator severity, String message,
			String panel, CEProblemLocator c, XElement description) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void clearProblems(String panel) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String label(String key) {
		return label(projectLanguage, key);
	}
	
	/**
	 * Load all available main labels.
	 */
	void initMainLabels() {
		languages.clear();
		mainLabels.clear();
		languages.addAll(getLanguages());
		for (String lang : languages) {
			Map<String, String> transMap = U.newLinkedHashMap();
			mainLabels.put(lang, transMap);
			
			byte[] langData = getData(lang, "labels.xml");
			if (langData != null) {
				try {
					XElement xlabels = XElement.parseXML(new ByteArrayInputStream(langData));
					for (XElement xe : xlabels.childrenWithName("entry")) {
						String key = xe.get("key");
						String content = xe.content;
						if (key != null && !key.isEmpty() && content != null && !content.isEmpty()) {
							transMap.put(key, content);
						}
					}
				} catch (XMLStreamException ex) {
					// ignored
				}
			}
		}
	}
	@Override
	public List<String> languages() {
		return languages;
	}
	@Override
	public void setLabel(String key, String value) {
		if (key == null || key.isEmpty()) {
			return;
		}
		Map<String, String> lang = mainLabels.get(projectLanguage);
		if (lang == null) {
			lang = U.newLinkedHashMap();
			mainLabels.put(projectLanguage, lang);
		}
		lang.put(key, value);
	}
	@Override
	public File getWorkDir() {
		return workDir;
	}
	@Override
	public boolean exists(String resource) {
		return exists(projectLanguage, resource);
	}
	@Override
	public String mainPlayerRace() {
		return "human"; // FIXME for now
	}
	@Override
	public boolean hasLabel(String key) {
		String lbl = label(key);
		return lbl != null && !lbl.isEmpty();
	}
	/** Update the undo/redo menu. */
	void updateUndoRedo() {
		mnuEditUndo.setEnabled(undoManager.canUndo());
		mnuEditRedo.setEnabled(undoManager.canRedo());
	}
	@Override
	public UndoManager undoManager() {
		return undoManager;
	}
	@Override
	public void undoManagerChanged() {
		updateUndoRedo();
	}
}
