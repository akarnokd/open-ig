/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.core.Action0;
import hu.openig.core.Func0;
import hu.openig.editors.BackgroundProgress;
import hu.openig.model.GameDefinition;
import hu.openig.utils.ConsoleWatcher;
import hu.openig.utils.Exceptions;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
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
public class CampaignEditor extends JFrame implements CEContext, CEPanelPreferences {
    /** */
    private static final long serialVersionUID = -4044298769130516091L;
    /** The main version. */
    public static final String VERSION = "0.03";
    /** The configuration file. */
    public static final String CONFIG_FILE = "open-ig-ce-config.xml";
    /** The console watcher. */
    static Closeable consoleWatcher;
    /** The UI language. */
    static String language = "en";
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
    JMenu mnuFileRecent;
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
    /** The data manager. */
    CEDataManager dataManager;
    /** The definitions panel. */
    CEDefinitionPanel definitionsPanel;
    /** The recent entries. */
    final Set<String> recent = new LinkedHashSet<>();
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

        dataManager = new CEDataManager(new File("."));

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
                Exceptions.add(ex);
            }
        }
    }
    /**
     * Load the configuration from the XML.
     * @param xml the XML
     */
    void loadConfigXML(XElement xml) {
        loadWindowState(this, xml.childElement("main-window"));

        loadPreferences(xml);

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
            Exceptions.add(ex);
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

        savePreferences(result);

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
//        Set<String> argSet = U.newSet(args);

//        if (argSet.contains("-en")) {
//            language = "en";
//        } else
//        if (argSet.contains("-hu")) {
// FIXME language = "hu";
//            language = "en";
//        } else {
//            language = "en";
//        }

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
                ce.showStartupDialog();
            }
        });
    }
    /**
     * Initialize the internal components.
     */
    private void initComponents() {
        undoManager = new UndoManager();

        warning = new ImageIcon(CampaignEditor.class.getResource("/hu/openig/gfx/warning.png"));
        error = new ImageIcon(CampaignEditor.class.getResource("/hu/openig/gfx/error.png"));

        labels = new HashMap<>();
        flags = new HashMap<>();

        // fetch labels
        try {
            XElement xlabels = XElement.parseXML(CampaignEditor.class.getResource("ce_labels.xml"));
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
                flags.put(id, new ImageIcon(CampaignEditor.class.getResource(xlang.get("flag"))));
            }
        } catch (XMLStreamException | IOException ex) {
            Exceptions.add(ex);
        }

        ToolTipManager.sharedInstance().setDismissDelay(120000);

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

        updateUndoRedo();
        startupDialog = new CEStartupDialog(this);

        // FIXME disabled menu and toolbars

        mnuFileOpen.setEnabled(false);
        mnuFileSave.setEnabled(false);
        mnuFileSaveAs.setEnabled(false);
        mnuFileImport.setEnabled(false);
        mnuFileExport.setEnabled(false);

        mnuEditCopy.setEnabled(false);
        mnuEditCut.setEnabled(false);
        mnuEditPaste.setEnabled(false);
        mnuEditDelete.setEnabled(false);

        mnuHelpAbout.setEnabled(false);

        toolbarOpen.setEnabled(false);
        toolbarSave.setEnabled(false);
        toolbarImport.setEnabled(false);
        toolbarExport.setEnabled(false);
        toolbarCut.setEnabled(false);
        toolbarCopy.setEnabled(false);
        toolbarPaste.setEnabled(false);
        toolbarRemove.setEnabled(false);
        toolbarSaveAs.setEnabled(false);
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
        URL res = CampaignEditor.class.getResource("/hu/openig/editors/" + graphicsResource);
        if (res != null) {
            ImageIcon icon = new ImageIcon(res);
            result.setIcon(icon);
            inMenu.setIcon(icon);
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
        definitionsPanel = new CEDefinitionPanel(this);
        labelsPanel = new CELabelsPanel(this);
        technologiesPanel = new CETechnologyPanel(this);

        // TODO initialize tabs
        tabs.addTab(get("Definition"), null, definitionsPanel);
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
        tabs.addTab(get("Spies"), null, new JPanel());
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
        mnuFileRecent = new JMenu(get("menu.file.recent"));
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

        mnuEdit.add(mnuEditUndo);
        mnuEdit.add(mnuEditRedo);
        mnuEdit.addSeparator();
        mnuEdit.add(mnuEditCut);
        mnuEdit.add(mnuEditCopy);
        mnuEdit.add(mnuEditPaste);
        mnuEdit.add(mnuEditDelete);

        // -----------------------

        mnuHelpOnline = new JMenuItem(get("menu.help.online"));
        mnuHelpAbout = new JMenuItem(get("menu.help.about"));

        mnuHelp.add(mnuHelpOnline);
        mnuHelp.addSeparator();
        mnuHelp.add(mnuHelpAbout);
        // -----------------------

        mainMenu.add(mnuFile);
        mainMenu.add(mnuEdit);
        mainMenu.add(mnuView);
        mainMenu.add(mnuTools);
        mainMenu.add(mnuHelp);

        // -----------------------
        // ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo

        mnuFileNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showNewDialog();
            }
        });

        mnuFileExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doExit();
            }
        });

        mnuFileSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BackgroundProgress.run(get("saving_project"), get("saving_project"), new Action0() {
                    @Override
                    public void invoke() {
                        save();
                    }
                }, null);
            }
        });

        mnuHelpOnline.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doHelp();
            }
        });
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
    public void addProblem(CESeverityIndicator severity, String message,
            String panel, CEProblemLocator c, XElement description) {
        // TODO Auto-generated method stub

    }
    @Override
    public void clearProblems(String panel) {
        // TODO Auto-generated method stub

    }

    @Override
    public File getWorkDir() {
        return dataManager.workdir();
    }
    /** Update the undo/redo menu. */
    void updateUndoRedo() {
        mnuEditUndo.setEnabled(undoManager.canUndo());
        mnuEditRedo.setEnabled(undoManager.canRedo());

        toolbarUndo.setEnabled(undoManager.canUndo());
        toolbarRedo.setEnabled(undoManager.canRedo());
    }
    @Override
    public UndoManager undoManager() {
        return undoManager;
    }
    @Override
    public void undoManagerChanged() {
        updateUndoRedo();
    }
    /** Display the startup dialog. */
    void showStartupDialog() {
        startupDialog.reset();
        startupDialog.showRecent(true);
        startupDialog.findCampaigns();
        startupDialog.setRecents();
        startupDialog.setVisible(true);
    }
    /** Display the startup dialog. */
    void showNewDialog() {
        startupDialog.reset();
        startupDialog.showRecent(false);
        startupDialog.findCampaigns();
        startupDialog.setVisible(true);
    }
    /**
     * Open the help page.
     */
    void doHelp() {
        try {
            URI u = new URI("https://github.com/akarnokd/open-ig/wiki/Campaign-editor");

            if (Desktop.isDesktopSupported()) {
                Desktop d = Desktop.getDesktop();
                d.browse(u);
            } else {
                JOptionPane.showConfirmDialog(this, u);
            }
        } catch (IOException | URISyntaxException ex) {
            Exceptions.add(ex);
        }
    }
    @Override
    public CampaignData campaignData() {
        return dataManager.campaignData;
    }
    @Override
    public void campaignData(CampaignData newData) {
        this.dataManager.campaignData = newData;
    }
    @Override
    public CEDataManager dataManager() {
        return dataManager;
    }
    @Override
    public void load() {
        definitionsPanel.load();
        labelsPanel.load();
        technologiesPanel.load();

        // TODO Auto-generated method stub
    }
    @Override
    public void addRecent(String s) {
        if (recent.add(s)) {
            updateRecentMenu();
        }
    }
    /**
     * Update the recent menu items.

     */
    public void updateRecentMenu() {
        mnuFileRecent.removeAll();
        for (final String r : recent) {
            JMenuItem e = new JMenuItem();
            e.setText(r);
            mnuFileRecent.add(e);

            e.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    doOpenRecent(r);
                }
            });
        }
    }
    /**
     * Open a recent file.
     * @param f the file
     */
    void doOpenRecent(String f) {
        final CEDataManager dm = dataManager();
        dm.campaignData = new CampaignData();
        File f0 = new File(getWorkDir(), f);
        if (f0.canRead()) {
            dm.campaignData.definition = new GameDefinition();
            try {
                dm.campaignData.definition.parse(XElement.parseXML(f0));

                dm.campaignData.definition.name = f0.getParentFile().getName();

                BackgroundProgress.run(get("startup.opening_recent"), get("startup.opening_recent"),
                    new Action0() {
                        @Override
                        public void invoke() {
                            dm.load();
                        }
                    },
                    new Action0() {
                        @Override
                        public void invoke() {
                            load();
                        }
                    }
                );
            } catch (XMLStreamException ex) {
                Exceptions.add(ex);
            }
        }
    }
    @Override
    public Set<String> getRecent() {
        return recent;
    }
    @Override
    public void loadPreferences(XElement preferences) {
        recent.clear();
        for (XElement xrecents : preferences.childrenWithName("recent-projects")) {
            for (XElement xrecent : xrecents.childrenWithName("recent")) {
                recent.add(xrecent.content);
            }
            updateRecentMenu();
        }
    }
    @Override
    public String preferencesId() {
        return "main";
    }
    @Override
    public void savePreferences(XElement preferences) {
        XElement xrecents = preferences.add("recent-projects");
        for (String r : recent) {
            xrecents.add("recent", r);
        }
    }
    /**
     * Save the current project.
     */
    public void save() {
        if (dataManager.campaignData != null) {
            dataManager.save();
        }
    }
}
