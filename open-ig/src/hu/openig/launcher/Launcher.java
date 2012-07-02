/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.launcher;

import hu.openig.ui.IGButton;
import hu.openig.utils.ConsoleWatcher;
import hu.openig.utils.IOUtils;
import hu.openig.utils.Parallels;
import hu.openig.utils.XElement;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.jar.JarFile;

import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.xml.stream.XMLStreamException;

/**
 * The improved launcher.
 * @author akarnokd, 2012.01.16.
 */
public class Launcher extends JFrame {
	/** */
	private static final long serialVersionUID = -3873203661572006298L;
	/** The launcher's version. */
	public static final String VERSION = "0.33";
	/**
	 * The update XML to download.
	 */
	public static final String UPDATE_XML = "https://open-ig.googlecode.com/svn/trunk/open-ig/update.xml";
	/** The Game module ID. */
	static final String GAME = "Game";
	/** The Launcher module ID. */
	static final String LAUNCHER = "Launcher";
	/** Background color. */
	final Color backgroundColoring = new Color(0x05034B);
	/** The download buffer size. */
	static final int DOWNLOAD_BUFFER_SIZE = 256 * 1024;
	/** Install the entire game. */
	IGButton install;
	/** Cancel the install/update. */
	IGButton cancel;
	/** Update the game to a new version. */
	IGButton update;
	/** Run the game. */
	IGButton run;
	/** Run the map editor. */
	IGButton mapEditor;
	/** Run the video player. */
	IGButton videoPlayer;
	/** Change startup parameters. */
	IGButton other;
	/** Update launcher. */
	IGButton launcher;
	/** The panel background. */
	BufferedImage background;
	/** The main panel. */
	JPanel mainPanel;
	/** The other settings menu. */
	JPopupMenu otherMenu;
	/** The download progress panel. */
	JPanel progressPanel;
	/** The new version's changes. */
	JLabel changes;
	/** The new version label. */
	JLabel newVersionLabel;
	/** The new version's. */
	JLabel newVersion;
	/** The current version. */
	JLabel currentVersion;
	/** The current version label. */
	JLabel currentVersionLabel;
	/** Menu item. */
	private JMenuItem runSettings;
	/** Menu item. */
	private JMenuItem verify;
	/** Menu item. */
	private JMenuItem uninstall;
	/** Menu item. */
	private JMenuItem releaseNotes;
	/** Menu item. */
	private JMenuItem projectPage;
	/** The large font. */
	private Font fontLarge;
	/** The medium font. */
	private Font fontMedium;
	/** Progress panel item. */
	private JLabel currentActionLabel;
	/** Progress panel item. */
	private JLabel currentFileLabel;
	/** Progress panel item. */
	private JLabel currentFileProgressLabel;
	/** Progress panel item. */
	private JLabel totalFileProgressLabel;
	/** Progress panel item. */
	private JLabel currentAction;
	/** Progress panel item. */
	private JLabel currentFile;
	/** Progress panel item. */
	private JLabel currentFileProgress;
	/** Progress panel item. */
	private JLabel totalFileProgress;
	/** Progress panel item. */
	private JProgressBar fileProgress;
	/** Progress panel item. */
	private JProgressBar totalProgress;
	/** The old config file. */
	final File configOld = new File("launcher-config.xml");
	/** The new config file. */
	final File config = new File("open-ig-launcher-config.xml");
	/** The local update file. */
	final File localUpdate = new File("open-ig-update.xml");
	/** Current language. */
	String language = "en";
	/** The language flag. */
	JComboBox<String> flag;
	/** The available maps. */
	final Map<String, BufferedImage> flags = new LinkedHashMap<String, BufferedImage>();
	/** The online module information. */
	private LUpdate updates;
	/** Not installed constant. */
	static final String NOT_INSTALLED = "0.00.000";
	/** The installed version. */
	String detectedVersion;
	/** The current worker. */
	SwingWorker<?, ?> worker;
	/** The installation directory. */
	File installDir;
	/** The installation directory. */
	File currentDir;
	/** The JVM override. */
	String jvm;
	/** The memory override. */
	Integer memory;
	/** The console watcher. */
	ConsoleWatcher cw;
	/** The foreground color. */
	private Color foreground;
	/** Verzió újraellenőrzése. */
	private JMenuItem recheck;
	/** Creates the GUI. */
	public Launcher() {
		super("Open Imperium Galactica Launcher v" + VERSION);
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				doClose();
				cw.dispose();
			}
		});
		
		// working directory workaround
		
		currentDir = new File(".");
		
		if (!new File(currentDir, "open-ig-launcher.jar").exists()) {
			URL u = getClass().getResource("/hu/openig/gfx/launcher_background.png");
			String p = u.toString().replace('\\', '/');
			int fidx = p.indexOf("file:/");
			int lidx = p.indexOf("open-ig-launcher.jar", fidx);
			if (lidx >= 0 && fidx >= 0) {
				String p2 = p.substring(fidx + 6, lidx);
				while (p2.startsWith("/")) {
					p2 = p2.substring(1);
				}
				currentDir = new File(p2);
			}
		}
		installDir = currentDir;
		
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		
		try {
			URL u = getClass().getResource("/hu/openig/gfx/launcher_background.png");
			if (u != null) {
				background = ImageIO.read(u);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		try {
			URL u = getClass().getResource("/hu/openig/gfx/english.png");
			if (u != null) {
				flags.put("en", ImageIO.read(u));
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		try {
			URL u = getClass().getResource("/hu/openig/gfx/hungarian.png");
			if (u != null) {
				flags.put("hu", ImageIO.read(u));
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		try {
			URL u = getClass().getResource("/hu/openig/gfx/german.png");
			if (u != null) {
				flags.put("de", ImageIO.read(u));
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		mainPanel = new JPanel() {
			/** */
			private static final long serialVersionUID = -8242002641839189095L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D)g;
				if (background != null) {
					Composite save0 = g2.getComposite();
					g2.setComposite(AlphaComposite.SrcOver.derive(0.30f));
					g.drawImage(background, 0, 0, null);
					g2.setComposite(save0);
				}
			}
		};
		mainPanel.setOpaque(true);
		mainPanel.setBackground(backgroundColoring);
		
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addComponent(mainPanel, 0, GroupLayout.PREFERRED_SIZE, 640)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(mainPanel, 0, GroupLayout.PREFERRED_SIZE, 480)
		);
		
		createPanel();
		setResizable(false);
		setSize(640, 480);
		setLocationRelativeTo(null);
		init();
	}
	/**
	 * Returns the label.
	 * @param s the label
	 * @return the translated text
	 */
	String label(String s) {
		if ("hu".equals(language)) {
			if ("Processing %s".equals(s)) { return "%s feldolgozása"; }
			if ("Run settings".equals(s)) { return "Futtatás beállítások"; }
			if ("Browse...".equals(s)) { return "Tallózás..."; }
			if ("Java runtime home:".equals(s)) { return "Java futásidejű környezet:"; }
			if ("Default: %s".equals(s)) { return "Alapértelmezett: %s"; }
			if ("MB".equals(s)) { return "MB"; }
			if ("Memory:".equals(s)) { return "Memória:"; }
			if ("OK".equals(s)) { return "Rendben"; }
			if ("Cancel".equals(s)) { return "Mégsem"; }
			if ("Error".equals(s)) { return "Hiba"; }
			if ("Default: %s MB".equals(s)) { return "Alapértelmezett: %s MB"; }
			if ("Checking existing game files...".equals(s)) { return "Létező játék állományok ellenőrzése..."; }
			if ("Downloading game files...".equals(s)) { return "Játék állományok letöltése..."; }
			if ("Some files were not correctly downloaded. Please try again a bit later.".equals(s)) { return "Néhány állomány nem töltődött le rendesen. Kérem, próbálja meg egy kicsit később."; }
			if ("Do you want to uninstall the game (removes all game files except save)?".equals(s)) { return "Biztosan törölni akarja a játék állományait (a mentések kivételével)?"; }
			if ("Uninstall".equals(s)) { return "Eltávolítás"; }
			if ("The Launcher was not correctly downloaded. Please try again a bit later.".equals(s)) { return "A Launcher nem töltődött le rendesen. Kérem, próbálja meg egy kicsit később."; }
			if ("Error during data download: %s".equals(s)) { return "Hiba történt az adatok letöltése közben: %s"; }
			if ("Could not delete file %s".equals(s)) { return "A(z) %s állomány nem törölhető"; }
			if ("Error while processing file %s: %s".equals(s)) { return "A(z) %s állomány feldolgozása közben hiba történt: %s"; }
			if ("New version available: %s".equals(s)) { return "Új verzió érhető el: %s"; }
			if ("Error while checking files: %s".equals(s)) { return "A(z) állományok ellenörzése közben hiba történt: %s"; }
			if ("Could not access directory %s".equals(s)) { return "A(z) %s könyvtár nem elérhető"; }
			if ("Some files are missing or damaged. Do you wish to repair the install?".equals(s)) { return "Néhány fájl hiányzik vagy megsérült. Kijavítsam a telepítést?"; }
			if ("Copy".equals(s)) { return "Másolás"; }
			if ("Manual navigation".equals(s)) { return "Kézi navigálás"; }
			System.err.println("if (\"" + s + "\".equals(s)) { return \"\"; }");
			return s;
		} else
		if ("de".equals(language)) {
			if ("Processing %s".equals(s)) { return "Verarbeitung von %s"; }
			if ("Run settings".equals(s)) { return "Lauf Einstellungen"; }
			if ("Browse...".equals(s)) { return "Browse..."; }
			if ("Java runtime home:".equals(s)) { return "Java Laufzeit Umgebung:"; }
			if ("Default: %s".equals(s)) { return "Voreinstellung: %s"; }
			if ("MB".equals(s)) { return "MB"; }
			if ("Memory:".equals(s)) { return "Speicher:"; }
			if ("OK".equals(s)) { return "Gut"; }
			if ("Cancel".equals(s)) { return "Lieber nicht"; }
			if ("Error".equals(s)) { return "Fehler"; }
			if ("Default: %s MB".equals(s)) { return "Voreinstellung: %s MB"; }
			if ("Checking existing game files...".equals(s)) { return "Gegenvärtige Spiel Datei Überprüfung..."; }
			if ("Downloading game files...".equals(s)) { return "Spiel Datei Herunterladung..."; }
			if ("Some files were not correctly downloaded. Please try again a bit later.".equals(s)) { return "Einige Daten waren nicht vollständig heruntergeladen. Bitte, versuchen Sie nochmal ein bischen später."; }
			if ("Do you want to uninstall the game (removes all game files except save)?".equals(s)) { return "Sind Sie sicher um der Spiel zu löschen (auser die Spielstande)?"; }
			if ("Uninstall".equals(s)) { return "Deinstallieren"; }
			if ("The Launcher was not correctly downloaded. Please try again a bit later.".equals(s)) { return "Launcher war nicht vollständig heruntergeladen. Bitte, versuchen Sie nochmal ein bischen später."; }
			if ("Error during data download: %s".equals(s)) { return "Problem whärend herunterladung: %s"; }
			if ("Could not delete file %s".equals(s)) { return "Datei %s kan nicht gelöscht werden"; }
			if ("Error while processing file %s: %s".equals(s)) { return "Problem whärend processierung von Datei %s: %s"; }
			if ("New version available: %s".equals(s)) { return "Neue Version verfügbar: %s"; }
			if ("Error while checking files: %s".equals(s)) { return "Problem whärend Überprüfung from Daten: %s"; }
			if ("Could not access directory %s".equals(s)) { return "Mappe %s nicht verfügbar."; }
			if ("Some files are missing or damaged. Do you wish to repair the install?".equals(s)) { return "Daten nicht verfügbar oder fählerhaft. Install reparieren?"; }
			if ("Copy".equals(s)) { return "Kopieren"; }
			if ("Manual navigation".equals(s)) { return "Manuelle Navigation"; }
			System.err.println("if (\"" + s + "\".equals(s)) { return \"\"; }");
			return s;
		}
		return s;
	}
	/**
	 * Format a label.
	 * @param s the label id
	 * @param params the label params
	 * @return the translated text
	 */
	String format(String s, Object... params) {
		return String.format(label(s), params);
	}
	/**
	 * Create the main panel.
	 */
	void createPanel() {
		GroupLayout gl = new GroupLayout(mainPanel);
		mainPanel.setLayout(gl);
		fontLarge = new Font(Font.SANS_SERIF, Font.BOLD, 20);
		fontMedium = new Font(Font.SANS_SERIF, Font.BOLD, 16);
		
		install = new IGButton();
		launcher = new IGButton();
		update = new IGButton();
		run = new IGButton();
		mapEditor = new IGButton();
		videoPlayer = new IGButton();
		other = new IGButton();
		cancel = new IGButton();
		otherMenu = new JPopupMenu();
		changes = new JLabel();
		newVersion = new JLabel();
		currentVersion = new JLabel();
		newVersionLabel = new JLabel();
		currentVersionLabel = new JLabel();
		progressPanel = new JPanel();

		flag = new JComboBox<String>(new String[] { "en", "hu", "de" });
		flag.setRenderer(new DefaultListCellRenderer() {
			/** */
			private static final long serialVersionUID = 5312297135529455789L;

			@Override
			public Component getListCellRendererComponent(JList<?> list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel lbl = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
				
				lbl.setIcon(new ImageIcon(flags.get(lbl.getText())));
				lbl.setHorizontalAlignment(JLabel.CENTER);
				lbl.setVerticalAlignment(JLabel.CENTER);
				lbl.setText("");
				
				return lbl;
			}
		});
		flag.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				language = (String)flag.getSelectedItem();
				doActOnUpdates();
				setLabels();
			}
		});

		JPanel flagPanel = new JPanel();
		flagPanel.setLayout(new BorderLayout());
		flagPanel.add(flag, BorderLayout.WEST);
		flagPanel.setOpaque(false);
		
		install.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doInstall(true);
			}
		});
		update.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doInstall(false);
			}
		});
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (worker != null) {
					worker.cancel(true);
					worker = null;
				}
				showHideProgress(false);
				cancel.setVisible(false);
				detectVersion();
				doActOnUpdates();
			}
		});
		launcher.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doUpdateSelf();
			}
		});
		
		run.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runModule(GAME);
			}
		});
		mapEditor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runModule("MapEditor");
			}
		});
		videoPlayer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runModule("VideoPlayer");
			}
		});
		
		install.setFont(fontMedium);
		update.setFont(fontMedium);
		run.setFont(fontLarge);
		mapEditor.setFont(fontMedium);
		videoPlayer.setFont(fontMedium);
		other.setFont(fontMedium);
		cancel.setFont(fontMedium);
		otherMenu.setFont(fontLarge);
		changes.setFont(fontMedium);
		currentVersion.setFont(fontLarge);
		newVersion.setFont(fontLarge);
		currentVersionLabel.setFont(fontLarge);
		newVersionLabel.setFont(fontLarge);
		launcher.setFont(fontLarge);
		
		install.setVisible(false);
		update.setVisible(false);
		run.setVisible(false);
		mapEditor.setVisible(false);
		videoPlayer.setVisible(false);
		other.setVisible(false);
		cancel.setVisible(false);
		otherMenu.setVisible(false);
		changes.setVisible(false);
		currentVersion.setVisible(false);
		newVersion.setVisible(false);
		currentVersionLabel.setVisible(false);
		newVersionLabel.setVisible(false);
		launcher.setVisible(false);
		progressPanel.setVisible(false);
		flag.setVisible(false);

		foreground = new Color(0xD0D0D0);
		install.setForeground(foreground);
		update.setForeground(foreground);
		run.setForeground(foreground);
		mapEditor.setForeground(foreground);
		videoPlayer.setForeground(foreground);
		cancel.setForeground(foreground);
		other.setForeground(foreground);

		progressPanel.setOpaque(false);
		progressPanel.setBackground(backgroundColoring);
		
		createProgressPanel();

		changes.setForeground(Color.YELLOW);
		currentVersion.setForeground(Color.WHITE);
		newVersion.setForeground(Color.ORANGE);
		currentVersionLabel.setForeground(Color.WHITE);
		newVersionLabel.setForeground(Color.ORANGE);
		launcher.setForeground(Color.PINK);

		runSettings = new JMenuItem();
		verify = new JMenuItem();
		uninstall = new JMenuItem();
		releaseNotes = new JMenuItem();
		projectPage = new JMenuItem();
		recheck = new JMenuItem();
		
		verify.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				detectVersion();
				doActOnUpdates();
				doVerify();
			}
		});
		projectPage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (updates != null) {
					LModule um = updates.getModule(GAME);
					if (um != null) {
						doNavigate(um.general.url);
					}
				}
			}
		});
		releaseNotes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (updates != null) {
					LModule um = updates.getModule(GAME);
					if (um != null) {
						doNavigate(um.releaseNotes.url);
					}
				}
			}
		});
		uninstall.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doUninstall();
			}
		});
		runSettings.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				doSettings();
			}
		});
		recheck.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				detectVersion();
				doUpgrades();
			}
		});
		
		otherMenu.add(recheck);
		otherMenu.addSeparator();
		otherMenu.add(projectPage);
		otherMenu.add(releaseNotes);
		otherMenu.addSeparator();
		otherMenu.add(runSettings);
		otherMenu.add(verify);
		otherMenu.addSeparator();
		otherMenu.add(uninstall);
		
		projectPage.setFont(fontLarge);
		releaseNotes.setFont(fontLarge);
		runSettings.setFont(fontLarge);
		verify.setFont(fontLarge);
		uninstall.setFont(fontLarge);
		recheck.setFont(fontLarge);
		
		other.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				otherMenu.show(other, 0, other.getHeight());
			}
		});
		
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addGap(30)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(flagPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(
					gl.createSequentialGroup()
					.addGroup(
						gl.createParallelGroup()
						.addGroup(
							gl.createSequentialGroup()
							.addComponent(newVersionLabel)
							.addGap(5)
							.addComponent(newVersion)
						)
						.addComponent(changes)
					)
					.addGap(20)
					.addComponent(launcher)
					.addComponent(install)
					.addComponent(update)
				)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(run)
					.addGap(15)
					.addComponent(currentVersionLabel)
					.addGap(5)
					.addComponent(currentVersion)
				)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(mapEditor)
					.addGap(25)
					.addComponent(videoPlayer)
					.addGap(25)
					.addComponent(other)
				)
				.addComponent(progressPanel)
			)
			.addGap(30)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGap(10)
			.addComponent(flagPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addGroup(
					gl.createSequentialGroup()
					.addGroup(
						gl.createParallelGroup(Alignment.BASELINE)
						.addComponent(newVersionLabel)
						.addComponent(newVersion)
					)
					.addComponent(changes)
				)
				.addComponent(launcher)
				.addComponent(install)
				.addComponent(update)
			)
			.addGap(25)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(run)
				.addComponent(currentVersionLabel)
				.addComponent(currentVersion)
			)
			.addGap(25)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(mapEditor)
				.addGap(30)
				.addComponent(videoPlayer)
				.addGap(30)
				.addComponent(other)
			)
			.addGap(50)
			.addComponent(progressPanel)
			.addGap(25)
		);
		
		gl.setHonorsVisibility(progressPanel, false);
		
		setLabels();
	}
	/** Create the progress panel. */
	void createProgressPanel() {
		GroupLayout gl = new GroupLayout(progressPanel);
		progressPanel.setLayout(gl);
		
		currentActionLabel = new JLabel();
		currentFileLabel = new JLabel();
		currentFileProgressLabel = new JLabel();
		totalFileProgressLabel = new JLabel();

		currentAction = new JLabel();
		currentFile = new JLabel();
		currentFileProgress = new JLabel();
		totalFileProgress = new JLabel();

		fileProgress = new JProgressBar();
		fileProgress.setFont(fontMedium);
		fileProgress.setStringPainted(true);
		totalProgress = new JProgressBar();
		totalProgress.setFont(fontMedium);
		totalProgress.setStringPainted(true);
		
		currentActionLabel.setFont(fontLarge);
		currentActionLabel.setForeground(Color.LIGHT_GRAY);
		currentFileLabel.setFont(fontMedium);
		currentFileLabel.setForeground(Color.LIGHT_GRAY);
		currentFileProgressLabel.setFont(fontMedium);
		currentFileProgressLabel.setForeground(Color.LIGHT_GRAY);
		totalFileProgressLabel.setFont(fontMedium);
		totalFileProgressLabel.setForeground(Color.LIGHT_GRAY);
		
		currentAction.setFont(fontLarge);
		currentFile.setFont(fontMedium);
		currentFileProgress.setFont(fontMedium);
		totalFileProgress.setFont(fontMedium);
		currentAction.setForeground(Color.ORANGE);
		currentFile.setForeground(Color.WHITE);
		currentFileProgress.setForeground(Color.WHITE);
		totalFileProgress.setForeground(Color.WHITE);
		fileProgress.setForeground(backgroundColoring);
		totalProgress.setForeground(backgroundColoring);
		
		
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup()
					.addComponent(currentActionLabel)
					.addComponent(currentFileLabel)
					.addComponent(currentFileProgressLabel)
					.addComponent(totalFileProgressLabel)
				)
				.addGap(20)
				.addGroup(
					gl.createParallelGroup()
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(currentAction, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(cancel)
					)
					.addComponent(currentFile)
					.addComponent(currentFileProgress)
					.addComponent(totalFileProgress)
				)
			)
			.addComponent(fileProgress)
			.addComponent(totalProgress)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(currentActionLabel)
				.addComponent(currentAction)
				.addComponent(cancel)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(currentFileLabel)
				.addComponent(currentFile)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(currentFileProgressLabel)
				.addComponent(currentFileProgress)
			)
			.addGap(5)
			.addComponent(fileProgress)
			.addGap(5)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(totalFileProgressLabel)
				.addComponent(totalFileProgress)
			)
			.addGap(5)
			.addComponent(totalProgress)
		);
	}
	/** Set the labels for Hungarian. */
	void setLabels() {
		if ("en".equals(language)) {
			install.setText("Install");
			update.setText("Update");
			cancel.setText("Cancel");
			run.setText("Run Game");
			mapEditor.setText("Map Editor");
			videoPlayer.setText("Video Player");
			other.setText("Other options");
			launcher.setText("Update launcher");
			
			projectPage.setText("Project webpage...");
			releaseNotes.setText("Release notes...");
			runSettings.setText("Run settings...");
			verify.setText("Verify installation");
			uninstall.setText("Uninstall");
			recheck.setText("Check for new version");
	
			currentActionLabel.setText("Action:");
			currentFileLabel.setText("File:");
			currentFileProgressLabel.setText("Progress:");
			totalFileProgressLabel.setText("Total:");
			
			currentVersionLabel.setText("Version:");
			newVersionLabel.setText("New version available:");
		} else
		if ("hu".equals(language)) {
			install.setText("Telepítés");
			update.setText("Frissítés");
			cancel.setText("Mégsem");
			run.setText("Játék futtatása");
			mapEditor.setText("Térképszerkesztő");
			videoPlayer.setText("Videolejátszó");
			other.setText("Egyéb lehetőségek");
			launcher.setText("Indító frissítése");
			
			projectPage.setText("Projekt honlapja...");
			releaseNotes.setText("Verzió jegyzetek...");
			runSettings.setText("Futtatási beállítások...");
			verify.setText("Telepítés ellenőrzése");
			uninstall.setText("Eltávolítás");
			recheck.setText("Új verzió keresése");
	
			currentActionLabel.setText("Művelet:");
			currentFileLabel.setText("Állomány:");
			currentFileProgressLabel.setText("Folyamat:");
			totalFileProgressLabel.setText("Összesen:");

			currentVersionLabel.setText("Verzió:");
			newVersionLabel.setText("Új verzió érhető el:");
		} else
		if ("de".equals(language)) {
			install.setText("Installieren");
			update.setText("Aktualisieren");
			cancel.setText("Lieber nicht");
			run.setText("Spiel starten");
			mapEditor.setText("Karteneditor");
			videoPlayer.setText("Videospieler");
			other.setText("Weitere Optionen");
			launcher.setText("Launcher aktualisieren");
			
			projectPage.setText("Projekt Webseite...");
			releaseNotes.setText("Versionshinweise...");
			runSettings.setText("Lauf Einstellungen...");
			verify.setText("Installation überprüfen");
			uninstall.setText("Deinstallieren");
			recheck.setText("Neue Version suchen");
	
			currentActionLabel.setText("Operation:");
			currentFileLabel.setText("Datei:");
			currentFileProgressLabel.setText("Fortstritt:");
			totalFileProgressLabel.setText("Gesamt:");

			currentVersionLabel.setText("Version:");
			newVersionLabel.setText("Neue Version verfügbar:");
		}
	}
	/**
	 * Close the launcher.
	 */
	void doClose() {
		saveConfig();
		dispose();
		if (worker != null) {
			worker.cancel(true);
			worker = null;
		}
	}
	/**
	 * Main program.
	 * <p>Arguments</p>
	 * <p><b>-selfupdate temporary_file_name</b> 
	 * delete the {@code open-ig-launcher.jar} and 
	 * save the {@codwe temporary_file_name} as
	 * the new {@code open-ig-launcher.jar}.</p> 
	 * <p><b>-selfdelete temporary_file_name</b> 
	 * delete the {@codwe temporary_file_name}.</p> 
	 * @param args see above
	 */
	public static void main(final String[] args) {
		System.setProperty("swing.aatext", "true");
		if (args.length >= 2) {
			if ("-selfupdate".equals(args[0])) {
				doSelfUpdate(args);
				return;
			} else
			if ("-selfdelete".equals(args[0])) {
				doSelfDelete(args);
			}
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Launcher ln = new Launcher();
				ln.cw = new ConsoleWatcher(args, VERSION, ln.language, null);
				ln.setVisible(true);
			}
		});
	}
	/**
	 * Delete the given temp file.
	 * @param args the program arguments
	 */
	static void doSelfDelete(String[] args) {
		File tempName = new File(args[1]);
		try {
			while (tempName.exists() && !tempName.delete()) {
				Thread.sleep(1000);	
			}
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Navigate to the given URL in the default browser.
	 * @param url the URL
	 */
	void doNavigate(String url) {
		try {
			Desktop d = Desktop.getDesktop();
			if (d != null) {
				try {
					d.browse(new URI(url));
				} catch (IOException e) {
				} catch (URISyntaxException e) {
				}
			}
		} catch (UnsupportedOperationException ex) {
			final JDialog d1 = new JDialog(this, ModalityType.APPLICATION_MODAL);
			d1.setTitle(label("Manual navigation"));
			
			JPanel d = new JPanel();
			d1.getContentPane().add(d);
			
			d.setBackground(backgroundColoring);
			final JTextField tf = new JTextField(url, 50);
			tf.setSelectionStart(0);
			tf.setSelectionEnd(url.length());
			tf.setEditable(false);
			tf.setFont(this.fontMedium);
			tf.setForeground(Color.BLACK);
			IGButton btnOk = new IGButton(label("OK"));
			btnOk.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					d1.setVisible(false);
				}
			});
			IGButton btnCopy = new IGButton(label("Copy"));
			btnCopy.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					tf.selectAll();
					tf.copy();
				}
			});
			btnOk.setForeground(foreground);
			btnOk.setFont(fontMedium);
			btnCopy.setForeground(foreground);
			btnCopy.setFont(fontMedium);
			
			
			GroupLayout gl = new GroupLayout(d);
			d.setLayout(gl);
			gl.setAutoCreateContainerGaps(true);
			gl.setAutoCreateGaps(true);
			
			gl.setHorizontalGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(tf)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(btnCopy)
					.addComponent(btnOk)
				)
			);
			gl.setVerticalGroup(
				gl.createSequentialGroup()
				.addComponent(tf)
				.addGroup(
					gl.createParallelGroup(Alignment.BASELINE)
					.addComponent(btnOk)
					.addComponent(btnCopy)
				)
			);
			gl.linkSize(SwingConstants.HORIZONTAL, btnCopy, btnOk);
			
			d1.pack();
			d1.setLocationRelativeTo(this);
			d1.setVisible(true);
		}
	}
	/**
	 * Execute the self update action.
	 * @param args the program arguments.
	 */
	static void doSelfUpdate(String[] args) {
		// make a copy of self and restart
		File tempName = new File(args[1]);
		File old = new File("open-ig-launcher.jar");
		try {
			while (old.exists() && !old.delete()) {
				Thread.sleep(1000);	
			}
			IOUtils.save(old, IOUtils.load(tempName));
			ProcessBuilder pb = new ProcessBuilder();
			pb.command(System.getProperty("java.home") + "/bin/java", 
					"-jar", "open-ig-launcher.jar", "-selfdelete", args[1]);
			Process p = pb.start();
			Parallels.consume(p.getInputStream());
			Parallels.consume(p.getErrorStream());
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Initialize from the configuration.
	 */
	void init() {
		doInitialize();
	}
	/** Initialize the launcher. */
	void doInitialize() {
		showHideProgress(true);
		currentAction.setText("Loading configuration...");
		currentFile.setText("open-ig-launcher-config.xml");
		currentFileProgress.setText("0%");
		totalFileProgress.setText("0%");
		totalFileProgress.setVisible(false);
		fileProgress.setValue(0);
		totalProgress.setValue(0);
		totalProgress.setVisible(false);
		SwingWorker<Void, Void> w = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				// remove old config
				if (configOld.exists()) {
					if (!configOld.delete()) {
						configOld.deleteOnExit();
					}
				}
				loadConfig();
				detectVersion();
				return null;
			}
			@Override
			protected void done() {
				doActOnUpdates();
				setLabels();
				
				doUpgrades();
			}
		};
		w.execute();
	}
	/** Check for upgrades. */
	void doUpgrades() {
		showHideProgress(true);
		currentAction.setText("Checking for upgrades...");
		currentFile.setText(UPDATE_XML);
		currentFileProgress.setText("0%");
		totalFileProgress.setText("0%");
		fileProgress.setValue(0);
		totalProgress.setVisible(false);
		totalFileProgress.setVisible(false);
		totalFileProgressLabel.setVisible(false);
		
		final long ts = System.currentTimeMillis();
		final File updateFile = new File(currentDir, "update.xml." + ts);
		SwingWorker<XElement, Void> w = new SwingWorker<XElement, Void>() {
			@Override
			protected XElement doInBackground() throws Exception {
				LFile lf = new LFile();
				lf.url = UPDATE_XML;
				downloadFiles(Collections.singletonList(lf), ts);
				return XElement.parseXML(updateFile.getAbsolutePath());
			}
			@Override
			protected void done() {
				showHideProgress(false);
				XElement xe = null;
//				File uf = updateFile;
				try {
					xe = get();
					move(updateFile, localUpdate);
					doProcessUpdate(xe, updateFile);
				} catch (ExecutionException ex) {
					if (!(ex.getCause() instanceof IOException)) {
						ex.printStackTrace();
					} else {
						processLocal();
					}
					errorMessage(format("Error during data download: %s", ex.toString()));
				} catch (InterruptedException ex) {
					ex.printStackTrace();
					errorMessage(format("Error during data download: %s", ex.toString()));
				} catch (IOException ex) {
					errorMessage(format("Could not rename update file: %s", ex.toString()));
				}
			}
		};
		w.execute();
	}
	/**
	 * Overwrite the destination file with the source file.
	 * @param src the source file
	 * @param dst the destinationf file
	 * @throws IOException on error
	 */
	void move(File src, File dst) throws IOException {
		IOUtils.save(dst, IOUtils.load(src));
	}
	/**
	 * Process a local update xml.
	 */
	void processLocal() {
		try {
			doProcessUpdate(XElement.parseXML(localUpdate.getAbsolutePath()), null);
		} catch (XMLStreamException ex) {
			
		}
	}
	/**
	 * Display an error message.
	 * @param text the message text
	 */
	void errorMessage(String text) {
		JOptionPane.showMessageDialog(this, text, label("Error"), JOptionPane.ERROR_MESSAGE);
	}
	/**
	 * Process the update.xml file.
	 * @param xml the XML
	 * @param file the file we are processing
	 */
	void doProcessUpdate(XElement xml, File file) {
		currentAction.setText(format("Processing %s", "update.xml"));
		if (file != null) {
			currentFile.setText(file.getName());
		} else {
			currentFile.setText("");
		}
		currentFileProgress.setText("0%");
		totalFileProgress.setText("0%");
		fileProgress.setValue(0);
		totalProgress.setValue(0);
		
		try {
			updates = new LUpdate();
			updates.process(xml);
			
			doActOnUpdates();
			
			currentFileProgress.setText("100%");
			totalFileProgress.setText("100%");
			fileProgress.setValue(100);
			totalProgress.setValue(100);
			if (file != null) {
				if (!file.delete()) {
					errorMessage(format("Could not delete file %s", file));
				}
			}
		} catch (Throwable t) {
			errorMessage(format("Error while processing file %s: %s", file, t));
		} finally {
			showHideProgress(false);
		}
	}
	/**
	 * Set the local file progress.
	 * @param position the current position.
	 * @param size the total size
	 * @param speed the download speed
	 */
	void setFileProgress(final long position, final long size, final double speed) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (size <= 0) {
					fileProgress.setIndeterminate(true);
					currentFileProgress.setText(String.format("?%%, %d KB , %.1f KB / s", position / 1024, speed));
				} else {
					fileProgress.setIndeterminate(false);
					fileProgress.setValue((int)(position * 100 / size));
					currentFileProgress.setText(String.format("%d%%, %d KB / %d KB, %.1f KB / s", position * 100 / size, position / 1024, size / 1024, speed));	
				}
				
			}
		});
	}
	/**
	 * Set the local file progress.
	 * @param count the file count
	 * @param max the max files
	 * @param progress the pain progress number
	 * @param position the current position.
	 * @param size the total size
	 * @param speed the download speed
	 */
	void setTotalProgress(
			final int count,
			final int max,
			final int progress, 
			final long position, 
			final long size, 
			final double speed) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				totalProgress.setValue(progress);
				totalProgress.setIndeterminate(false);
				if (size <= 0) {
					totalFileProgress.setText(String.format("%d / %d, %d KB, %.1f KB / s", count, max, position / 1024, speed));	
				} else {
					totalFileProgress.setText(String.format("%d / %d, %d KB / %d KB , %.1f KB / s", count, max, position / 1024, size / 1024, speed));
				}
				
			}
		});
	}
	/**
	 * Load the configuration.
	 */
	void loadConfig() {
		if (config.canRead()) {
			try {
				XElement cfg = XElement.parseXML(config.getAbsolutePath());
				language = cfg.get("language");
				jvm = cfg.get("jvm", null);
				String m = cfg.get("memory", null);
				if (m != null) {
					memory = Integer.parseInt(m);
				} else {
					memory = null;
				}
				flag.setSelectedItem(language);
				
				
				
			} catch (XMLStreamException ex) {
				ex.printStackTrace();
			}
		}
	}
	/**
	 * Detecting installed version.
	 */
	void detectVersion() {
		detectedVersion = NOT_INSTALLED;
		File[] files = currentDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.startsWith("open-ig-") && name.endsWith(".jar")) {
					int idx = name.indexOf(".jar");
					String v = name.substring(9, idx);
					for (int i = 0; i < v.length(); i++) {
						char c = v.charAt(i);
						if (!Character.isDigit(c) && c != '.') {
							return false;
						}
					}
					return true;
				}
				return false;
			}
		});
		if (files == null || files.length == 0) {
			return;
		}
		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				int idx1 = o1.getName().indexOf(".jar");
				String v1 = o1.getName().substring(8, idx1);
				int idx2 = o2.getName().indexOf(".jar");
				String v2 = o2.getName().substring(8, idx2);
				
				return LModule.compareVersion(v2, v1);
			}
		});
		File top = files[0];
		
		String name = top.getName();
		
		int idx = name.indexOf(".jar");
		detectedVersion = name.substring(8, idx);
		
		for (int i = 1; i < files.length; i++) {
			if (!files[i].delete()) {
				System.err.printf("Could not delete previous file: %s%n", files[i]);
			}
		}
	}
	/**
	 * Act on the downloaded upgrades.
	 */
	void doActOnUpdates() {
		flag.setVisible(true);
		LModule m = updates != null ? updates.getModule(LAUNCHER) : null;
		if (m != null && m.compareVersion(VERSION) > 0) {
			launcher.setVisible(true);
			changes.setText(m.releaseNotes.getDescription(language));
			newVersion.setText(m.version);
			
			install.setVisible(false);
			update.setVisible(false);
			changes.setVisible(true);
			newVersion.setVisible(true);
			newVersionLabel.setVisible(true);
			return;
		}
		LModule g = updates != null ? updates.getModule(GAME) : null;
		if (NOT_INSTALLED.equals(detectedVersion)) {
			currentVersion.setVisible(false);
			currentVersionLabel.setVisible(false);
			run.setVisible(false);
			mapEditor.setVisible(false);
			videoPlayer.setVisible(false);
			other.setVisible(false);
			if (g != null) {
				install.setVisible(true);
				changes.setText(g.releaseNotes.getDescription(language));
				newVersion.setText(g.version);
	
				changes.setVisible(true);
				newVersion.setVisible(true);
				newVersionLabel.setVisible(true);
			}
		} else {
			run.setVisible(true);
			mapEditor.setVisible(true);
			videoPlayer.setVisible(true);
			other.setVisible(true);
			
			install.setVisible(false);
			currentVersion.setText(detectedVersion);
			currentVersion.setVisible(true);
			currentVersionLabel.setVisible(true);
			if (g != null) {
				if (g.compareVersion(detectedVersion) > 0) {
					changes.setText(g.releaseNotes.getDescription(language));
					newVersion.setText(g.version);
	
					newVersion.setVisible(true);
					newVersionLabel.setVisible(true);
					changes.setVisible(true);
					update.setVisible(true);
				} else {
					newVersionLabel.setVisible(false);
					newVersion.setVisible(false);
					changes.setVisible(false);
					update.setVisible(false);
				}
			}
		}
	}
	/**
	 * Install the game.
	 * @param askDir ask for the installation directory?
	 */
	void doInstall(boolean askDir) {
		
		if (askDir) {
			JFileChooser fc = new JFileChooser(installDir);
			fc.setMultiSelectionEnabled(false);
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
				return;
			}
			installDir = fc.getSelectedFile();
		} else {
			installDir = currentDir;
		}
		
		final LModule g = updates.getModule(GAME);
		showHideProgress(true);
		
		currentAction.setText(label("Checking existing game files..."));
		currentFileProgress.setText("0%");
		totalFileProgress.setText("0%");
		fileProgress.setValue(0);
		totalProgress.setValue(0);
		install.setVisible(false);
		update.setVisible(false);
		cancel.setVisible(true);
		totalProgress.setVisible(true);
		totalFileProgress.setVisible(true);
		totalFileProgressLabel.setVisible(true);
		
		worker = new SwingWorker<List<LFile>, Void>() {
			@Override
			protected List<LFile> doInBackground() throws Exception {
				return collectDownloads(g);
			}
			@Override
			protected void done() {
				showHideProgress(false);
				
				worker = null;
				cancel.setVisible(false);
				try {
					doDownload(get());
				} catch (CancellationException ex) {
				} catch (ExecutionException ex) {
					ex.printStackTrace();
					errorMessage(format("Error while checking files: %s", ex));
				} catch (InterruptedException ex) {
					ex.printStackTrace();
					errorMessage(format("Error while checking files: %s", ex));
				}
			}
		};
		worker.execute();
	}
	/**
	 * Verify the game.
	 */
	void doVerify() {
		installDir = currentDir;
		
		final LModule g = updates.getModule(GAME);
		showHideProgress(true);
		currentAction.setText(label("Checking existing game files..."));
		currentFileProgress.setText("0%");
		totalFileProgress.setText("0%");
		fileProgress.setValue(0);
		totalProgress.setValue(0);
		install.setVisible(false);
		update.setVisible(false);
		cancel.setVisible(true);
		totalProgress.setVisible(true);
		totalFileProgress.setVisible(true);
		totalFileProgressLabel.setVisible(true);
		
		worker = new SwingWorker<List<LFile>, Void>() {
			@Override
			protected List<LFile> doInBackground() throws Exception {
				return collectDownloads(g);
			}
			@Override
			protected void done() {
				showHideProgress(false);
				worker = null;
				cancel.setVisible(false);
				try {
					List<LFile> files = get();
					
					if (!files.isEmpty()) {
						if (JOptionPane.showConfirmDialog(
								Launcher.this, 
								format("Some files are missing or damaged. Do you wish to repair the install?"),
								label("Error"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							doDownload(files);
						}
					}
				} catch (CancellationException ex) {
				} catch (ExecutionException ex) {
					ex.printStackTrace();
					errorMessage(format("Error while checking files: %s", ex));
				} catch (InterruptedException ex) {
					ex.printStackTrace();
					errorMessage(format("Error while checking files: %s", ex));
				}
			}
		};
		worker.execute();
	}
	/**
	 * Set the current filename.
	 * @param filename the filename
	 */
	void setCurrentFile(final String filename) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				currentFile.setText(filename);
			}
		});
	}
	/**
	 * Collect the files that are not properly downloaded.
	 * @param g the game module settings.
	 * @return the list of URLs to download
	 * @throws IOException on error
	 */
	List<LFile> collectDownloads(LModule g) throws IOException {
		if (!installDir.exists() && !installDir.mkdirs()) {
			throw new IOException(format("Could not access directory %s", installDir));
		}
		for (LRemoveFile rf : g.removeFiles) {
			File f = new File(rf.file);
			f = new File(installDir, f.getName());
			if (f.canWrite() && !f.delete()) {
				System.out.printf("Could not delete old file: %s%n", f);
			}
		}
		
		List<LFile> toDownload = new ArrayList<LFile>();

		long start = System.currentTimeMillis();
		long position = 0;
		long totalSize = 0;
		for (LFile lf : g.files) {
			File localFile = new File(installDir, lf.name());
			if (localFile.canRead()) {
				totalSize += localFile.length();
			}
		}
		for (int i = 0; i < g.files.size(); i++) {
			if (Thread.currentThread().isInterrupted()) {
				return toDownload;
			}
			
			double speed = position * 1000d / 1024 / (System.currentTimeMillis() - start);
			int progressValue = totalSize > 0 ? (int)(position * 100 / totalSize) : 0;
			setTotalProgress(i + 1, g.files.size(), progressValue, position, totalSize, speed);
			setFileProgress(0, -1, 0);
			
			LFile lf = g.files.get(i);
			String fn = lf.name();
			setCurrentFile(fn);
			
			File localFile = new File(installDir, fn);
			if (localFile.canRead()) {
				long filePos = 0;
				byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
				/** The SHA1 hash computer. */
				try {
					long start1 = System.currentTimeMillis();
					MessageDigest sha1 = MessageDigest.getInstance("SHA1");
					FileInputStream fin = new FileInputStream(localFile);
					try {
						while (!Thread.currentThread().isInterrupted()) {
							int read = fin.read(buffer);
							if (read > 0) {
								filePos += read;
								position += read;
								sha1.update(buffer, 0, read);
								// update local progress
								double speed1 = filePos * 1000d / 1024 / (System.currentTimeMillis() - start1);
								setFileProgress(filePos, localFile.length(), speed1);
								// update global progress
								speed = position * 1000d / 1024 / (System.currentTimeMillis() - start);
								progressValue = totalSize > 0 ? (int)(position * 100 / totalSize) : 0;
								setTotalProgress(i + 1, g.files.size(), progressValue, position, totalSize, speed);
							} else
							if (read < 0) {
								break;
							}
						}
						// update local progress
						double speed1 = filePos * 1000d / 1024 / (System.currentTimeMillis() - start1);
						setFileProgress(filePos, localFile.length(), speed1);
						// update global progress
						speed = position * 1000d / 1024 / (System.currentTimeMillis() - start);
						progressValue = totalSize > 0 ? (int)(position * 100 / totalSize) : 0;
						setTotalProgress(i + 1, g.files.size(), progressValue, position, totalSize, speed);
						
						byte[] sha1h = sha1.digest();
						byte[] sha1hupdate = LFile.toByteArray(lf.sha1);
						if (!Arrays.equals(sha1h, sha1hupdate)) {
							toDownload.add(lf);
						}
					} finally {
						fin.close();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
					toDownload.add(lf);
				} catch (NoSuchAlgorithmException ex) {
					ex.printStackTrace();
					toDownload.add(lf);
				}
			} else {
				toDownload.add(lf);
			}
		}
		
		return toDownload;
	}
	/**
	 * The downloader.
	 * @param files the list of files to download
	 */
	void doDownload(final List<LFile> files) {
		showHideProgress(true);
		
		currentAction.setText(label("Downloading game files..."));
		currentFileProgress.setText("0%");
		totalFileProgress.setText("0%");
		fileProgress.setValue(0);
		totalProgress.setValue(0);
		install.setVisible(false);
		cancel.setVisible(true);
		totalProgress.setVisible(true);
		totalFileProgress.setVisible(true);
		totalFileProgressLabel.setVisible(true);
		
		worker = new SwingWorker<Boolean, Void>() {
			/** Move self to the install directory? */
			boolean moveSelf;
			@Override
			protected Boolean doInBackground() throws Exception {
				boolean result = downloadFiles(files, -1);
				String ca = currentDir.getCanonicalPath();
				String ia = installDir.getCanonicalPath();
				if (!ca.equals(ia)) {
					moveSelf = true;
				}
				return result;
			}
			@Override
			protected void done() {
				showHideProgress(false);

				worker = null;
				cancel.setVisible(false);
				try {
					if (get() == Boolean.FALSE) {
						errorMessage(label("Some files were not correctly downloaded. Please try again a bit later."));
					} else {
						if (moveSelf) {
							doMoveSelfAndRun();
						}
					}
				} catch (CancellationException ex) {
					// ignore
				} catch (ExecutionException ex) {
					ex.printStackTrace();
					errorMessage(format("Error while checking files: %s", ex));
				} catch (InterruptedException ex) {
					ex.printStackTrace();
					errorMessage(format("Error while checking files: %s", ex));
				}
				if (!moveSelf) {
					detectVersion();
					doActOnUpdates();
				}
			}
		};
		worker.execute();
	}
	/**
	 * Move self to the install directory.
	 */
	void doMoveSelfAndRun() {
		File local = new File(currentDir, "open-ig-launcher.jar");
		File localcfg = new File(currentDir, "open-ig-launcher-config.xml");
		if (local.canRead()) {
			File target = new File(installDir, "open-ig-launcher.jar");
			File targetcfg = new File(installDir, "open-ig-launcher-config.xml");
			
			IOUtils.save(target, IOUtils.load(local));

			try {
				Desktop d = Desktop.getDesktop();
				if (d != null) {
					try {
						d.open(target.getParentFile());
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			} catch (UnsupportedOperationException ex) {
				// not supported
			}
			
			ProcessBuilder pb = new ProcessBuilder();
			pb.directory(target.getParentFile());
			pb.command(System.getProperty("java.home") + "/bin/java", 
					"-jar", target.getAbsolutePath(), 
					"-selfdelete", local.getAbsolutePath());
			try {
				pb.start();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
			doClose();
			if (localcfg.canRead()) {
				IOUtils.save(targetcfg, IOUtils.load(localcfg));
				if (!localcfg.delete()) {
					System.err.printf("Could not delete " + localcfg);
				}
			}
		} else {
			System.err.printf("Warning, " + local + " not found");
		}
	}
	/**
	 * Download all files from the list of URLs.
	 * @param files the list of files
	 * @param timestamp the custom timestamp to use for temporary filenames, -1 if not
	 * @return true if all downloaded file checked out okay
	 * @throws IOException on error
	 */
	boolean downloadFiles(List<LFile> files, long timestamp) throws IOException {
		boolean allOk = true;
		long start = System.currentTimeMillis();
		long position = 0;
		for (int i = 0; i < files.size(); i++) {
			if (Thread.currentThread().isInterrupted()) {
				return false;
			}
			
			double speed = position * 1000d / 1024 / (System.currentTimeMillis() - start);
			setTotalProgress(i + 1, files.size(), (i) * 100 / files.size(), position, -1, speed);
			setFileProgress(0, -1, 0);
			
			LFile lf = files.get(i);
			URL u = new URL(lf.url + "?x=" + System.currentTimeMillis());
			String fn = u.getPath();
			int idx = fn.lastIndexOf("/");
			if (idx >= 0) {
				fn = fn.substring(idx + 1);
			}
			if (timestamp >= 0) {
				fn += "." + timestamp;
			}
			setCurrentFile(fn);
			
			File localFile = new File(installDir, fn);
			
			HttpURLConnection conn = (HttpURLConnection)u.openConnection();
			conn.connect();
			try {
				InputStream in = conn.getInputStream();
				long length = conn.getContentLength();
				try {
					long filePos = 0;
					byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
					/** The SHA1 hash computer. */
					try {
						MessageDigest sha1 = MessageDigest.getInstance("SHA1");
						long start1 = System.currentTimeMillis();
						
						FileOutputStream fout = new FileOutputStream(localFile);
						try {
							while (!Thread.currentThread().isInterrupted()) {
								int read = in.read(buffer);
								if (read > 0) {
									filePos += read;
									position += read;
									sha1.update(buffer, 0, read);
									fout.write(buffer, 0, read);
									// update local progress
									double speed1 = filePos * 1000d / 1024 / (System.currentTimeMillis() - start1);
									setFileProgress(filePos, length, speed1);
									// update global progress
									speed = position * 1000d / 1024 / (System.currentTimeMillis() - start);
									
									int pos = (i) * 100 / files.size();
									long fileProgress = length > 0 ? filePos * 100 / length : 0;
									if (fileProgress >= 0) {
										pos += fileProgress / files.size();
									}
									setTotalProgress(i + 1, files.size(), pos, position, -1, speed);
								} else
								if (read < 0) {
									break;
								}
							}
							// update local progress
							double speed1 = filePos * 1000d / 1024 / (System.currentTimeMillis() - start1);
							setFileProgress(filePos, length, speed1);
							// update global progress
							speed = position * 1000d / 1024 / (System.currentTimeMillis() - start);
							setTotalProgress(i + 1, files.size(), (i + 1) * 100 / files.size(), position, -1, speed);
							
							if (timestamp < 0) {
								byte[] sha1h = sha1.digest();
								byte[] sha1hupdate = LFile.toByteArray(lf.sha1);
								if (!Arrays.equals(sha1h, sha1hupdate)) {
									allOk = false;
								}
							}
						} finally {
							fout.close();
						}
					} catch (IOException ex) {
						ex.printStackTrace();
						allOk = false;
					} catch (NoSuchAlgorithmException ex) {
						ex.printStackTrace();
						allOk = false;
					}					
				} finally {
					in.close();
				}
			} finally {
				conn.disconnect();
			}
		}
		if (allOk) {
			removeOldUpgrades();
		}
		return allOk;
	}
	/**
	 * Remove any upgrade packs which are not listed in the module def.
	 */
	void removeOldUpgrades() {
		LModule m = updates != null ? updates.getModule(GAME) : null;
		if (m == null) {
			return;
		}
		final Set<String> curr = new HashSet<String>();
		for  (LFile lf : m.files) {
			int idx = lf.url.indexOf("open-ig-upgrade-");
			if (idx >= 0) {
				int jdx = lf.url.indexOf(".zip", idx);
				if (jdx >= 0) {
					curr.add(lf.url.substring(idx, jdx + 4));
				}
			}
		}
		File[] ugs = currentDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("open-ig-upgrade-") && name.endsWith(".zip")
						&& !curr.contains(name);
			}
		});
		if (ugs == null) {
			return;
		}
		for (File f : ugs) {
			if (!f.delete()) {
				System.err.println("Could not delete old upgrade: " + f);
			}
		}
	}
	/**
	 * Run the module.
	 * @param moduleId the module id to run
	 */
	void runModule(String moduleId) {
		LModule m = updates != null ? updates.getModule(moduleId) : null;
		if (m != null && m.clazz != null) {
			String jar = String.format("%s/open-ig-%s.jar", currentDir.getAbsolutePath(), detectedVersion);
			// extract splash
			File hack = new File(currentDir, "open-ig-splash.png");
			if (!hack.exists()) {
				try {
					JarFile jf = new JarFile(jar);
					try {
						InputStream in = jf.getInputStream(jf.getJarEntry("hu/openig/gfx/OpenIG_Splash.png"));
						try {
							IOUtils.save(hack, IOUtils.load(in));
						} finally {
							in.close();
						}
					} finally {
						jf.close();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			String runJVM = jvm;
			if (runJVM == null) {
				runJVM = System.getProperty("java.home");
			}
			int mem = m.memory;
			if (memory != null) {
				mem = memory;
			}
			ProcessBuilder pb = new ProcessBuilder();
			pb.command(
					runJVM + "/bin/java",
					"-Xmx" + mem + "M",
					String.format("-splash:%s/open-ig-splash.png", currentDir.getAbsolutePath()),
					"-cp", 
					jar,
					m.clazz,
					"-memonce", "-" + language);
			try {
				Process p = pb.start();
				Parallels.consume(p.getInputStream());
				Parallels.consume(p.getErrorStream());
				
			} catch (IOException ex) {
				ex.printStackTrace();
				errorMessage(format("Error running module: %s", ex));
			}
		}
	}
	/**
	 * Uninstall the game.
	 */
	void doUninstall() {
		if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(this, 
				label("Do you want to uninstall the game (removes all game files except save)?"),
				label("Uninstall"), JOptionPane.YES_NO_OPTION)) {
			File[] files = currentDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.startsWith("open-ig-");
				}
			});
			if (files != null) {
				for (File f : files) {
					if (!f.delete()) {
						System.out.printf("Could not delete file: %s%n", f);
					}
				}
			}
		}
	}
	/**
	 * Show or hide the progress panel and enable/disable the other controls.
	 * @param visible the visibility of the panel
	 */
	void showHideProgress(boolean visible) {
		progressPanel.setVisible(visible);

		run.setEnabled(!visible);
		mapEditor.setEnabled(!visible);
		videoPlayer.setEnabled(!visible);
		other.setEnabled(!visible);
	}
	/**
	 * Update the launcher itself.
	 */
	void doUpdateSelf() {
		final LModule m = updates != null ? updates.getModule(LAUNCHER) : null;
		if (m == null) {
			return;
		}

		showHideProgress(true);
		
		currentAction.setText("Updating the Launcher...");
		currentFile.setText("open-ig-launcher.jar");
		currentFileProgress.setText("0%");
		fileProgress.setValue(0);
		totalFileProgress.setVisible(false);
		totalProgress.setVisible(false);
		launcher.setVisible(false);
		cancel.setVisible(true);
		
		final long ts = System.currentTimeMillis();
		worker = new SwingWorker<Boolean, Void>() {
			/** Move self to the install directory? */
			boolean moveSelf;
			@Override
			protected Boolean doInBackground() throws Exception {
				boolean result = downloadFiles(m.files, ts);
				if (!currentDir.getAbsolutePath().equals(installDir.getAbsolutePath())) {
					moveSelf = true;
				}
				return result;
			}
			@Override
			protected void done() {
				showHideProgress(false);
				worker = null;
				cancel.setVisible(false);
				try {
					if (get() == Boolean.FALSE) {
						errorMessage(label("The Launcher was not correctly downloaded. Please try again a bit later."));
					} else {
						if (moveSelf) {
							doMoveSelfAndRun();
						} else {
							doSwapSelf(ts);
						}
					}
				} catch (CancellationException ex) {
					// ignore
				} catch (ExecutionException ex) {
					ex.printStackTrace();
					errorMessage(format("Error while checking files: %s", ex));
				} catch (InterruptedException ex) {
					ex.printStackTrace();
					errorMessage(format("Error while checking files: %s", ex));
				}
			}
		};
		worker.execute();
	}
	/**
	 * Swap itself with the new version of the launcher.
	 * @param ts the downloaded timestamp
	 */
	void doSwapSelf(long ts) {
		String self = String.format("%s/open-ig-launcher.jar.%s", installDir.getAbsolutePath(), ts);
		ProcessBuilder pb = new ProcessBuilder();
		pb.command(System.getProperty("java.home") + "/bin/java", 
				"-jar", self, 
				"-selfupdate", self);
		try {
			pb.start();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		doClose();
	}
	/**
	 * Save the configuration.
	 */
	void saveConfig() {
		XElement cfg = new XElement("open-ig-launcher-config");
		cfg.set("version", VERSION);
		cfg.set("language", language);
		cfg.set("jvm", jvm);
		cfg.set("memory", memory);
		try {
			cfg.save(config);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Display the run settings dialog.
	 */
	void doSettings() {
		final LModule m = updates != null ? updates.getModule(GAME) : null;
		if (m == null) {
			return;
		}
		
		
		
		final JDialog dialog = new JDialog(this, label("Run settings"));
		
		Container c = dialog.getContentPane();
		
		JPanel p = new JPanel();
		p.setBackground(backgroundColoring);
		
		c.add(p);
		
		GroupLayout gl = new GroupLayout(p);
		p.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		final JTextField jvmField = new JTextField(30);
		if (jvm != null) {
			jvmField.setText(jvm);
		}
		IGButton browse = new IGButton(label("Browse..."));
		browse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String path = System.getProperty("java.home");
				if (!jvmField.getText().isEmpty()) {
					path = jvmField.getText();
				}
				JFileChooser fc = new JFileChooser(path);
				fc.setMultiSelectionEnabled(false);
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
					jvmField.setText(fc.getSelectedFile().getAbsolutePath());
				}
				
			}
		});
		final JTextField memField = new JTextField(5);
		if (memory != null) {
			memField.setText(memory.toString());
		}
		
		JLabel jvmLabel = new JLabel(label("Java runtime home:"));
		JLabel jvmLabelNow = new JLabel(format("Default: %s", System.getProperty("java.home")));
		
		JLabel memMb = new JLabel(label("MB"));

		JLabel memLabel = new JLabel(label("Memory:"));
		JLabel memLabelNow = new JLabel(format("Default: %s MB", m.memory));

		IGButton ok = new IGButton(label("OK"));
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (memField.getText().isEmpty()) {
					memory = null;
				} else {
					String o = memField.getText();
					if (!o.isEmpty()) {
						try {
							int mi = Integer.parseInt(o);
							if (mi <= 0) {
								memory = null;
							} else {
								memory = mi;
							}
						} catch (NumberFormatException ex) {
							
						}
					} else {
						memory = null;
					}
				}
				if (jvmField.getText().isEmpty()) {
					jvm = null;
				} else {
					jvm = jvmField.getText();
				}
				dialog.dispose();
			}
		});
		IGButton cancel = new IGButton(label("Cancel"));
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});

		jvmLabel.setFont(fontMedium);
		jvmLabelNow.setFont(fontMedium);
		jvmField.setFont(fontMedium);
		
		memLabelNow.setFont(fontMedium);
		memLabel.setFont(fontMedium);
		memField.setFont(fontMedium);
		memMb.setFont(fontMedium);
		
		jvmLabel.setForeground(foreground);
		jvmLabelNow.setForeground(foreground);
		jvmField.setForeground(Color.BLACK);
		
		memLabelNow.setForeground(foreground);
		memLabel.setForeground(foreground);
		memField.setForeground(Color.BLACK);
		memMb.setForeground(foreground);
		
		ok.setFont(fontMedium);
		cancel.setFont(fontMedium);
		browse.setFont(fontMedium);
		ok.setForeground(foreground);
		cancel.setForeground(foreground);
		browse.setForeground(foreground);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createParallelGroup(Alignment.LEADING)
				.addGroup(
					gl.createSequentialGroup()
					.addGroup(
						gl.createParallelGroup()
						.addComponent(jvmLabel)
						.addComponent(memLabel)
					)
					.addGroup(
						gl.createParallelGroup()
						.addComponent(jvmField)
						.addGroup(
							gl.createSequentialGroup()
							.addComponent(memField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(memMb)
						)
					)
					.addComponent(browse)
				)
				.addGroup(
					gl.createSequentialGroup()
					.addGap(30)
					.addComponent(jvmLabelNow)
				)
				.addGroup(
					gl.createSequentialGroup()
					.addGap(30)
					.addComponent(memLabelNow)
				)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(ok)
				.addComponent(cancel)
			)
		);
		
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(jvmLabel)
				.addComponent(jvmField)
				.addComponent(browse)
			)
			.addComponent(jvmLabelNow)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(memLabel)
				.addComponent(memField)
				.addComponent(memMb)
			)
			.addComponent(memLabelNow)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(ok)
				.addComponent(cancel)
			)
		);
		
		gl.linkSize(SwingConstants.HORIZONTAL, ok, cancel);
		
		dialog.setResizable(false);
		dialog.pack();
		dialog.setLocationRelativeTo(this);
		dialog.setModal(true);
		dialog.setVisible(true);
		
	}
}
