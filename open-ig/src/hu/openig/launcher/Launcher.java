/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.launcher;

import hu.openig.core.Func0;
import hu.openig.ui.IGButton;
import hu.openig.ui.IGCheckBox;
import hu.openig.utils.ConsoleWatcher;
import hu.openig.utils.Exceptions;
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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
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
import javax.swing.JSeparator;
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
	public static final String VERSION = "0.37";
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
	/** Run the game. */
	IGButton continueLast;
	/** Run the map editor. */
	IGButton mapEditor;
	/** Run the video player. */
	IGButton videoPlayer;
	/** Change startup parameters. */
	IGButton other;
	/** Update launcher. */
	IGButton launcher;
	/** The verify button. */
	IGButton verifyBtn;
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
	/** Menu item. */
	JMenuItem selfRepair;
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
	final String config = "open-ig-launcher-config.xml";
	/** The local update file. */
	final String localUpdate = "open-ig-update.xml";
	/** Current language. */
	String language = "en";
	/** The language flag. */
	JComboBox<String> flag;
	/** The available maps. */
	final Map<String, BufferedImage> flags = new LinkedHashMap<String, BufferedImage>();
	/** The labels. */
	final Map<String, Map<String, String>> labels = new HashMap<String, Map<String, String>>();
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
	/** JVM parameters. */
	String jvmParams;
	/** Application parameters. */
	String appParams;
	/** The console watcher. */
	ConsoleWatcher cw;
	/** The foreground color. */
	private Color foreground;
	/** Verzió újraellenőrzése. */
	private JMenuItem recheck;
	/** Run the verification automatically? */
	boolean runVerify;
	/** Set of installed languages. */
	final Set<String> installedLanguages = new HashSet<String>();
	/** Select install languages. */
	JMenuItem selectLanguages;
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
			Exceptions.add(ex);
		}
		
		loadLanguages();
		
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
	 * Load the languages.
	 */
	void loadLanguages() {
		try {
			InputStream in = getClass().getResourceAsStream("launcher_labels.xml");
			try {
				XElement xlabels = XElement.parseXML(in);
				for (XElement xlabel : xlabels.childrenWithName("language")) {
					String id = xlabel.get("id");
					String flag = xlabel.get("flag");
					
					URL url = getClass().getResource(flag);
					
					if (url == null) {
						System.err.println("Can't locate flag: " + flag);
					}
					BufferedImage image = ImageIO.read(url);
					
					flags.put(id, image);
					
					Map<String, String> entries = new HashMap<String, String>();
					labels.put(id, entries);
					
					for (XElement xentry : xlabel.childrenWithName("entry")) {
						entries.put(xentry.get("key"), xentry.content);
					}
				}
			} finally {
				in.close();
			}
		} catch (IOException ex) {
			Exceptions.add(ex);
		} catch (XMLStreamException ex) {
			Exceptions.add(ex);
		}
	}
	/**
	 * Parse a set of command lines.
	 * @param line the line to parse
	 * @return the list of commands
	 */
	List<String> parseCommandLine(String line) {
		List<String> result = new ArrayList<String>();
		line = line.trim();
		StringBuilder b = new StringBuilder();
		boolean escapeSpace = false;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == '"') {
				if (escapeSpace) {
					escapeSpace = false;
					result.add(b.toString());
				} else {
					escapeSpace = true;
					b.setLength(0);
				}
			} else
			if (c != ' ' || escapeSpace) {
				b.append(c);
			} else
			if (c == ' ' && !escapeSpace) {
				result.add(b.toString());
				b.setLength(0);
			}
		}
		if (b.length() > 0) {
			result.add(b.toString());
		}
		
		return result;
	}
	/**
	 * Returns the label.
	 * @param s the label
	 * @return the translated text
	 */
	String label(String s) {
		Map<String, String> m = labels.get(language);
		if (m != null) {
			String t = m.get(s);
			if (t != null) {
				return t;
			}
		}
		System.err.println("\t\t<entry key='" + s + "'>" + s + "</entry>");
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
		verifyBtn = new IGButton();
		run = new IGButton();
		continueLast = new IGButton();
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

		String[] langs = new String[labels.size()];
		int i = 0;
		for (String l : flags.keySet()) {
			langs[i] = l;
			i++;
		}
		flag = new JComboBox<String>(langs);
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
				if (!selectLanguagesToInstall(true)) {
					return;
				}
				doInstall(true);
			}
		});
		update.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doInstall(false);
			}
		});
		verifyBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				detectVersion();
				doActOnUpdates();
				doVerify();
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
				doRunGame();
			}
		});
		continueLast.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doRunGame("-continue");
			}
		});
		mapEditor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runModule("MapEditor", Collections.<String>emptyList());
			}
		});
		videoPlayer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runModule("VideoPlayer", Collections.<String>emptyList());
			}
		});
		
		install.setFont(fontMedium);
		update.setFont(fontMedium);
		verifyBtn.setFont(fontMedium);
		run.setFont(fontLarge);
		continueLast.setFont(fontLarge);
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
		verifyBtn.setVisible(false);
		run.setVisible(false);
		continueLast.setVisible(false);
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
		verifyBtn.setForeground(foreground);
		run.setForeground(foreground);
		continueLast.setForeground(foreground);
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
		selfRepair = new JMenuItem();
		recheck = new JMenuItem();
		selectLanguages = new JMenuItem();
		
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
		selfRepair.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doUpdateSelf();
			}
		});
		
		selectLanguages.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectLanguagesToInstall(false);
			}
		});
		
		otherMenu.add(recheck);
		otherMenu.add(selectLanguages);
		otherMenu.addSeparator();
		otherMenu.add(projectPage);
		otherMenu.add(releaseNotes);
		otherMenu.addSeparator();
		otherMenu.add(runSettings);
		otherMenu.add(verify);
		otherMenu.add(selfRepair);
		otherMenu.addSeparator();
		otherMenu.add(uninstall);
		
		projectPage.setFont(fontLarge);
		releaseNotes.setFont(fontLarge);
		runSettings.setFont(fontLarge);
		verify.setFont(fontLarge);
		uninstall.setFont(fontLarge);
		recheck.setFont(fontLarge);
		selfRepair.setFont(fontLarge);
		selectLanguages.setFont(fontLarge);
		
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
					.addComponent(verifyBtn)
				)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(run)
					.addGap(15)
					.addComponent(currentVersionLabel)
					.addGap(5)
					.addComponent(currentVersion)
					.addGap(15)
					.addComponent(continueLast)
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
				.addComponent(verifyBtn)
			)
			.addGap(25)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(run)
				.addComponent(currentVersionLabel)
				.addComponent(currentVersion)
				.addComponent(continueLast)
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
		install.setText(label("Install"));
		update.setText(label("Update"));
		verifyBtn.setText(label("Verify"));
		cancel.setText(label("Cancel"));
		run.setText(label("Run Game"));
		continueLast.setText(label("Continue"));
		continueLast.setToolTipText(label("Continue from last save."));
		mapEditor.setText(label("Map Editor"));
		videoPlayer.setText(label("Video Player"));
		other.setText(label("Other options"));
		launcher.setText(label("Update launcher"));
		
		projectPage.setText(label("Project webpage..."));
		releaseNotes.setText(label("Release notes..."));
		runSettings.setText(label("Run settings..."));
		verify.setText(label("Verify installation"));
		uninstall.setText(label("Uninstall"));
		recheck.setText(label("Check for new version"));
		selectLanguages.setText(label("Select language packs..."));

		currentActionLabel.setText(label("Action:"));
		currentFileLabel.setText(label("File:"));
		currentFileProgressLabel.setText(label("Progress:"));
		totalFileProgressLabel.setText(label("Total:"));
		
		currentVersionLabel.setText(label("Version:"));
		newVersionLabel.setText(label("New version available:"));
		
		selfRepair.setText(label("Repair launcher"));
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
		final Set<String> argset = new HashSet<String>(Arrays.asList(args));
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final Launcher ln = new Launcher();
				
				Func0<String> languageFn = new Func0<String>() {
					@Override
					public String invoke() {
						return ln.language;
					}
				};
				
				ln.cw = new ConsoleWatcher(args, VERSION, languageFn, null);
				ln.runVerify = argset.contains("-verify");
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
			Exceptions.add(ex);
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
			Exceptions.add(ex);
		} catch (IOException ex) {
			Exceptions.add(ex);
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
					move(updateFile, new File(installDir, localUpdate));
					doProcessUpdate(xe, updateFile);
				} catch (ExecutionException ex) {
					if (!(ex.getCause() instanceof IOException)) {
						Exceptions.add(ex);
					} else {
						processLocal();
					}
					errorMessage(format("Error during data download: %s", ex.toString()));
				} catch (InterruptedException ex) {
					Exceptions.add(ex);
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
			doProcessUpdate(XElement.parseXML(new File(installDir, localUpdate)), null);
		} catch (XMLStreamException ex) {
			
		}
	}
	/**
	 * Check the existence of required files.
	 * @return true if the check succeded
	 */
	boolean checkInstall() {
		Set<String> testFiles = new HashSet<String>(Arrays.asList(
			"generic/options_1.png",
			"generic/campaign/main/definition.xml",
			"generic/ui/achievement.wav",
			"generic/intro/intro_1.ani.gz"
		));
		for (String s : installedLanguages) {
			testFiles.add(s + "/labels.xml");
			testFiles.add(s + "/ui/bar.wav");
			testFiles.add(s + "/bridge/messages_open_level_1.ani.gz");
		}
		try {
			LUpdate u = new LUpdate();
			u.process(XElement.parseXML(new File(installDir, localUpdate)));
			LModule m = u.getModule(GAME);
			for (LFile f : m.files) {
				if (!"generic".equals(f.language) && !installedLanguages.contains(f.language)) {
					continue;
				}
				String fn = f.url;
				int idx = fn.lastIndexOf("/");
				fn = fn.substring(idx + 1);
				
				File f2 = new File(installDir, fn);
				if (!f2.canRead()) {
					return false;
				}
				if (fn.startsWith("open-ig") && fn.endsWith(".zip")) {
					ZipFile zf = new ZipFile(f2);
					try {
						Enumeration<? extends ZipEntry> e = zf.entries();
						while (e.hasMoreElements()) {
							ZipEntry ze = e.nextElement();
							String zn = ze.getName().replace('\\', '/');
							testFiles.remove(zn);
						}
					} finally {
						zf.close();
					}
				}
			}
			return testFiles.isEmpty() && !installedLanguages.isEmpty();
		} catch (IOException ex) {
			// suppress this
		} catch (XMLStreamException ex) {
			Exceptions.add(ex);
		}
		return false;
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
		if (runVerify) {
			runVerify = false;
			doVerify();
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
		File cfg0 = new File(installDir, config);
		if (cfg0.canRead()) {
			try {
				XElement cfg = XElement.parseXML(cfg0.getAbsolutePath());
				language = cfg.get("language");
				jvm = cfg.get("jvm", null);
				String m = cfg.get("memory", null);
				if (m != null) {
					memory = Integer.parseInt(m);
				} else {
					memory = null;
				}
				flag.setSelectedItem(language);
				jvmParams = cfg.get("jvm-params", null);
				appParams = cfg.get("app-params", null);
				
				installedLanguages.clear();
				for (XElement xinst : cfg.childrenWithName("installed-language")) {
					installedLanguages.add(xinst.get("id"));
				}
				
			} catch (XMLStreamException ex) {
				Exceptions.add(ex);
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
			continueLast.setVisible(false);
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
			continueLast.setVisible(true);
			mapEditor.setVisible(true);
			videoPlayer.setVisible(true);
			other.setVisible(true);
			
			install.setVisible(false);
			verifyBtn.setVisible(false);
			
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
					update.setVisible(false);
					if (checkInstall()) {
						newVersion.setVisible(false);
						changes.setVisible(false);
					} else {
						newVersion.setText(label("The install appears to be incomplete or damaged."));
						changes.setText(label("Please click on the verify button."));
						
						changes.setVisible(true);
						newVersion.setVisible(true);
						verifyBtn.setVisible(true);
					}
					
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
		verifyBtn.setVisible(false);
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
					Exceptions.add(ex);
					errorMessage(format("Error while checking files: %s", ex));
				} catch (InterruptedException ex) {
					Exceptions.add(ex);
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
		if (installedLanguages.isEmpty() && !selectLanguagesToInstall(false)) {
			return;
		}
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
		verifyBtn.setVisible(false);
		cancel.setVisible(true);
		verifyBtn.setVisible(false);
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
							return;
						}
					}
					detectVersion();
					doActOnUpdates();
				} catch (CancellationException ex) {
				} catch (ExecutionException ex) {
					Exceptions.add(ex);
					errorMessage(format("Error while checking files: %s", ex));
				} catch (InterruptedException ex) {
					Exceptions.add(ex);
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
			
			// skip files that do not belong to the selected languages
			if (!"generic".equals(lf.language) && !installedLanguages.contains(lf.language)) {
				continue;
			}
			
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
					Exceptions.add(ex);
					toDownload.add(lf);
				} catch (NoSuchAlgorithmException ex) {
					Exceptions.add(ex);
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
		verifyBtn.setVisible(false);
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
					Exceptions.add(ex);
					errorMessage(format("Error while checking files: %s", ex));
				} catch (InterruptedException ex) {
					Exceptions.add(ex);
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
						Exceptions.add(ex);
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
				Exceptions.add(ex);
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
						Exceptions.add(ex);
						allOk = false;
					} catch (NoSuchAlgorithmException ex) {
						Exceptions.add(ex);
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
	 * @param params the additional parameters
	 */
	void runModule(String moduleId, List<String> params) {
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
					Exceptions.add(ex);
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
			List<String> cmdLine = new ArrayList<String>();

			cmdLine.add(runJVM + "/bin/java");
			if (jvmParams != null) {
				cmdLine.addAll(parseCommandLine(jvmParams));
			}
			cmdLine.add("-Xmx" + mem + "M");
			cmdLine.add(String.format("-splash:%s/open-ig-splash.png", currentDir.getAbsolutePath()));
			cmdLine.add("-cp");
			cmdLine.add(jar);
			cmdLine.add(m.clazz);
			cmdLine.add("-memonce");
			cmdLine.add("-" + language);
			cmdLine.addAll(params);
			if (appParams != null) {
				cmdLine.addAll(parseCommandLine(appParams));
			}

			pb.command(cmdLine);
			try {
				Process p = pb.start();
				Parallels.consume(p.getInputStream());
				Parallels.consume(p.getErrorStream());
				
			} catch (IOException ex) {
				Exceptions.add(ex);
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
		continueLast.setEnabled(!visible);
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
		
		currentAction.setText(label("Updating the Launcher..."));
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
					Exceptions.add(ex);
					errorMessage(format("Error while checking files: %s", ex));
				} catch (InterruptedException ex) {
					Exceptions.add(ex);
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
			Exceptions.add(ex);
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
		cfg.set("jvm-params", jvmParams);
		cfg.set("app-params", appParams);
		
		for (String s : installedLanguages) {
			cfg.add("installed-language").set("id", s);
		}
		
		try {
			cfg.save(new File(installDir, config));
		} catch (IOException ex) {
			Exceptions.add(ex);
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
		JLabel jvmVersion = new JLabel(System.getProperty("java.version") + ", " + System.getProperty("os.name") + ", " + System.getProperty("os.arch"));
		
		
		JLabel memMb = new JLabel(label("MB"));

		JLabel memLabel = new JLabel(label("Memory:"));
		JLabel memLabelNow = new JLabel(format("Default: %s MB", m.memory));

		JLabel jvmParamsLabel = new JLabel(format("JVM parameters:"));
		final JTextField jvmParams = new JTextField(this.jvmParams);
		
		JLabel appParamsLabel = new JLabel(format("Game parameters:"));
		final JTextField appParams = new JTextField(this.appParams);
		
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
				
				if (jvmParams.getText().isEmpty()) {
					Launcher.this.jvmParams = null;
				} else {
					Launcher.this.jvmParams = jvmParams.getText();
				}

				if (appParams.getText().isEmpty()) {
					Launcher.this.appParams = null;
				} else {
					Launcher.this.appParams = appParams.getText();
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

		jvmVersion.setForeground(foreground);
		jvmVersion.setFont(fontMedium);

		jvmParamsLabel.setForeground(foreground);
		jvmParamsLabel.setFont(fontMedium);
		jvmParams.setForeground(Color.BLACK);
		jvmParams.setFont(fontMedium);

		appParamsLabel.setForeground(foreground);
		appParamsLabel.setFont(fontMedium);
		appParams.setForeground(Color.BLACK);
		appParams.setFont(fontMedium);

		ok.setFont(fontMedium);
		cancel.setFont(fontMedium);
		browse.setFont(fontMedium);
		ok.setForeground(foreground);
		cancel.setForeground(foreground);
		browse.setForeground(foreground);
		
		JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
		
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
						.addComponent(jvmParamsLabel)
						.addComponent(appParamsLabel)
					)
					.addGroup(
						gl.createParallelGroup()
						.addComponent(jvmField)
						.addGroup(
							gl.createSequentialGroup()
							.addComponent(memField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(memMb)
						)
						.addComponent(jvmParams)
						.addComponent(appParams)
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
					.addComponent(jvmVersion)
				)
				.addGroup(
					gl.createSequentialGroup()
					.addGap(30)
					.addComponent(memLabelNow)
				)
			)
			.addComponent(sep)
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
			.addComponent(jvmVersion)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(memLabel)
				.addComponent(memField)
				.addComponent(memMb)
			)
			.addComponent(memLabelNow)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(jvmParamsLabel)
				.addComponent(jvmParams)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(appParamsLabel)
				.addComponent(appParams)
			)
			.addComponent(sep, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
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
	/**
	 * Run the game or pop-up the first-time configuration window.
	 * @param arguments the additional arguments
	 */
	void doRunGame(String... arguments) {
		File cfg = new File(installDir, "open-ig-config.xml");
		boolean displayPreLaunch = true;
		if (cfg.exists()) {
			try {
				XElement xcfg = XElement.parseXML(cfg.getAbsolutePath());
				for (XElement e : xcfg.children()) {
					if ("intro".equals(e.get("key", "")) && !"true".equals(e.content)) {
						displayPreLaunch = false;
						break;
					}
				}
			} catch (XMLStreamException ex) {
			}
		}
		if (!installedLanguages.contains(language)) {
			if (JOptionPane.showConfirmDialog(this, label(
					"<html>You do not have the language pack installed for the current language.<br>"
					+ "Please change your Launcher language or install the missing language packs.<br>"
					+ "Do you wish to install the missing language packs?")) == JOptionPane.YES_OPTION) {
				selectLanguagesToInstall(false);
			} else {
				return;
			}
		}
		if (displayPreLaunch) {
			final JDialog dlg = new JDialog(this);
			
			dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dlg.setResizable(false);
			
			JLabel mainLabel = new JLabel();
			mainLabel.setForeground(foreground);
			mainLabel.setFont(fontMedium);
			
			JLabel fullScreenDesc = new JLabel();
			fullScreenDesc.setForeground(foreground);
			fullScreenDesc.setFont(fontMedium);
			
			JComboBox<String> fullScreen = new JComboBox<String>();
			fullScreen.setFont(fontMedium);
			
			JLabel movieDesc = new JLabel();
			movieDesc.setForeground(foreground);
			movieDesc.setFont(fontMedium);
			IGCheckBox movie = new IGCheckBox("", fontMedium);
			movie.setForeground(foreground);
			
			JLabel clickDesc = new JLabel();
			clickDesc.setForeground(foreground);
			clickDesc.setFont(fontMedium);
			IGCheckBox click = new IGCheckBox("", fontMedium);
			click.setForeground(foreground);
			
			final AtomicBoolean approve = new AtomicBoolean();
			
			IGButton ok = new IGButton();
			ok.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					approve.set(true);
					dlg.dispose();
				}
			});
			IGButton cancel = new IGButton();
			cancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dlg.dispose();
				}
			});
			ok.setForeground(foreground);
			ok.setFont(fontMedium);
			cancel.setForeground(foreground);
			cancel.setFont(fontMedium);
			
			// -----------------------------------------------------------------

			dlg.setTitle(label("First time launch"));
			ok.setText(label("OK"));
			cancel.setText(label("Cancel"));

			mainLabel.setText(label("First-time explanation"));

			fullScreenDesc.setText(label("Resolution explanation"));

			fullScreen.setModel(new DefaultComboBoxModel<String>(new String[] { 
					label("Classic 640 x 480"),
					label("Maximized"),
					label("Full screen")
			}));

			movieDesc.setText(label("Movie explanation"));
			movie.setText(label("Movie checkbox"));

			clickDesc.setText(label("Click explanation"));
			click.setText(label("Click checkbox"));

			// -----------------------------------------------------------------
			
			JPanel p = new JPanel();
			p.setBackground(backgroundColoring);
			
			GroupLayout gl = new GroupLayout(p);
			gl.setAutoCreateContainerGaps(true);
			gl.setAutoCreateGaps(true);
			p.setLayout(gl);
			
			JSeparator sep0 = new JSeparator(JSeparator.HORIZONTAL);
			JSeparator sep3 = new JSeparator(JSeparator.HORIZONTAL);
			
			gl.setHorizontalGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(mainLabel, 450, 450, 450)
					.addComponent(sep0, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addComponent(fullScreenDesc, 450, 450, 450)
					.addGroup(
						gl.createSequentialGroup()
						.addGap(20)
						.addComponent(fullScreen)
					)
					.addComponent(movieDesc, 450, 450, 450)
					.addGroup(
						gl.createSequentialGroup()
						.addGap(20)
						.addComponent(movie)
					)
					.addComponent(clickDesc, 450, 450, 450)
					.addGroup(
						gl.createSequentialGroup()
						.addGap(20)
						.addComponent(click)
					)
				)
				.addComponent(sep3, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(ok)
					.addComponent(cancel)
				)
			);
			gl.setVerticalGroup(
				gl.createSequentialGroup()
				.addComponent(mainLabel)
				.addComponent(sep0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(fullScreenDesc)
				.addComponent(fullScreen)
				.addGap(20)
				.addComponent(movieDesc)
				.addComponent(movie)
				.addGap(20)
				.addComponent(clickDesc)
				.addComponent(click)
				.addComponent(sep3, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGroup(
					gl.createParallelGroup(Alignment.BASELINE)
					.addComponent(ok)
					.addComponent(cancel)
				)
			);
			
			
			gl.linkSize(SwingConstants.HORIZONTAL, ok, cancel);
			
			dlg.getContentPane().add(p);
			dlg.setModal(true);
			dlg.pack();
			dlg.setLocationRelativeTo(Launcher.this);
			dlg.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentShown(ComponentEvent e) {
					dlg.pack();
					dlg.setLocationRelativeTo(Launcher.this);
				}
			});
			dlg.setVisible(true);
			if (!approve.get()) {
				return;
			}
			List<String> params = new ArrayList<String>();
			
			if (fullScreen.getSelectedIndex() == 1) {
				params.add("-maximized");
			} else
			if (fullScreen.getSelectedIndex() == 2) {
				params.add("-fullscreen");
			}
			if (movie.isSelected()) {
				params.add("-fullscreenvideos");
			}
			if (click.isSelected()) {
				params.add("-clickskip");
			} else {
				params.add("-noclickskip");
			}
			
			params.addAll(Arrays.asList(arguments));
			
			runModule(GAME, params);
		} else {
			runModule(GAME, Arrays.asList(arguments));
		}
	}
	/**
	 * Displays a dialog where the user can select which languages to install.
	 * @param isInstall called by install?
	 * @return true continue with the last option?
	 */
	boolean selectLanguagesToInstall(boolean isInstall) {
		final JDialog dialog = new JDialog(this);
		dialog.setTitle(label("Select language packs to install"));
		dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		final AtomicBoolean okPressed = new AtomicBoolean();
		
		IGButton ok = new IGButton(label("OK"));
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				okPressed.set(true);
				dialog.dispose();
			}
		});
		IGButton cancel = new IGButton(label("Cancel"));
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				okPressed.set(false);
				dialog.dispose();
			}
		});
		
		JPanel p = new JPanel();
		GroupLayout gl = new GroupLayout(p);
		p.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		// create language lines
		SequentialGroup vert = gl.createSequentialGroup();
		
		ParallelGroup pg1 = gl.createParallelGroup();
		ParallelGroup pg2 = gl.createParallelGroup();
		ParallelGroup pg3 = gl.createParallelGroup();

		final JLabel sum = new JLabel();
		sum.setHorizontalAlignment(JLabel.RIGHT);
		sum.setText(String.format("%,.1f MB", 0d));
		
		final List<JCheckBox> cbs = new ArrayList<JCheckBox>();
		final List<Long> szs = new ArrayList<Long>();
		final List<String> lngs = new ArrayList<String>();
		for (String lng : flags.keySet()) {
			
			ParallelGroup pg0 = gl.createParallelGroup(Alignment.CENTER);
			vert.addGroup(pg0);
			
			JLabel  flag = new JLabel(new ImageIcon(flags.get(lng)));
			
			JCheckBox cb = new JCheckBox(label("language-" + lng));
			cb.setOpaque(false);
			
			JLabel sizes = new JLabel();
			sizes.setHorizontalAlignment(JLabel.RIGHT);
			
			pg0.addComponent(flag).addComponent(cb).addComponent(sizes);
			
			pg1.addComponent(flag);
			pg2.addComponent(cb, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE);
			pg3.addComponent(sizes);
			
			long sz = 0;
			for (LFile f : updates.getModule(GAME).files) {
				if (f.language.equals(lng)) {
					sz += f.size;
					try {
						File f2 = new File(installDir, f.name());
						if (f2.canRead()) {
							cb.setSelected(true);
						}
					} catch (MalformedURLException ex) {
						Exceptions.add(ex);
					}
				}
			}
			sizes.setText(String.format("%,.1f MB", sz / 1024d / 1024d));
			
			cbs.add(cb);
			szs.add(sz);
			lngs.add(lng);
		}
		
		for (JCheckBox cb : cbs) {
			cb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					long sz = 0;
					int i = 0;
					for (JCheckBox cb2 : cbs) {
						if (cb2.isSelected()) {
							sz += szs.get(i);
						}
						i++;
					}
					
					sum.setText(String.format("%,.1f MB", sz / 1024d / 1024d));
				}
			});
		}
		
		JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
		JSeparator sep2 = new JSeparator(JSeparator.HORIZONTAL);
		JSeparator sep3 = new JSeparator(JSeparator.HORIZONTAL);
		
		JCheckBox cbUninstall = new JCheckBox(label("Remove files of unselected packages"));
		JCheckBox cbInstall = new JCheckBox(label("Install new packages now"));
		
		cbUninstall.setVisible(!isInstall);
		cbUninstall.setOpaque(false);
		cbInstall.setVisible(!isInstall);
		cbInstall.setOpaque(false);
		// ------------------------------------------
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createParallelGroup(Alignment.LEADING)
				.addGroup(
					gl.createParallelGroup(Alignment.TRAILING)
					.addGroup(
						gl.createSequentialGroup()
						.addGroup(pg1)
						.addGroup(pg2)
						.addGroup(pg3)
					)
					.addComponent(sep)
					.addComponent(sum, 200, 200, 200)
				)
				.addComponent(sep2)
				.addComponent(cbInstall)
				.addComponent(cbUninstall)
			)
			.addComponent(sep3)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(ok)
				.addComponent(cancel)
			)
		);
		
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(vert)
			.addComponent(sep)
			.addComponent(sum)
			.addComponent(sep2)
			.addComponent(cbInstall)
			.addComponent(cbUninstall)
			.addComponent(sep3)
			.addGap(5)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(ok)
				.addComponent(cancel)
			)
		);

		gl.linkSize(SwingConstants.HORIZONTAL, ok, cancel);

		for (Component cmp : p.getComponents()) {
			if ((cmp instanceof IGButton) 
					|| (cmp instanceof JLabel)
					|| (cmp instanceof JCheckBox)) {
				cmp.setFont(fontMedium);
				cmp.setForeground(foreground);
			}
		}
		p.setBackground(backgroundColoring);
		
		Container c = dialog.getContentPane();
		c.add(p, BorderLayout.CENTER);

		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(this);
		dialog.setModal(true);
		dialog.setVisible(true);

		if (okPressed.get()) {
			installedLanguages.clear();
			int i = 0;
			for (JCheckBox cb : cbs) {
				if (cb.isSelected()) {
					installedLanguages.add(lngs.get(i));
				}
				i++;
			}
			
			if (cbUninstall.isSelected()) {
				Set<String> remove = new HashSet<String>();
				i = 0;
				for (JCheckBox cb : cbs) {
					if (!cb.isSelected()) {
						remove.add(lngs.get(i));
					}
					i++;
				}
				for (LFile lf : updates.getModule(GAME).files) {
					if (remove.contains(lf.language)) {
						try {
							File f2 = new File(installDir, lf.name());
							if (f2.canRead() && !f2.delete()) {
								System.err.println("Unable to delete file: " + f2.getAbsolutePath());
							}
						} catch (MalformedURLException ex) {
							Exceptions.add(ex);
						}
					}
				}
				if (!cbInstall.isSelected()) {
					detectVersion();
					doUpgrades();
				}
			}
			if (cbInstall.isSelected()) {
				doInstall(false);
				return false;
			}
		}
		
		return okPressed.get();
	}
}
