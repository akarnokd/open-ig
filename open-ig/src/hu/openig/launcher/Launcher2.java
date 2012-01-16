/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.launcher;

import hu.openig.ui.IGButton;
import hu.openig.utils.IOUtils;
import hu.openig.utils.Parallels;
import hu.openig.utils.XElement;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
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
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.xml.stream.XMLStreamException;

/**
 * The improved launcher.
 * @author akarnokd, 2012.01.16.
 */
public class Launcher2 extends JFrame {
	/** */
	private static final long serialVersionUID = -3873203661572006298L;
	/** The launcher's version. */
	public static final String VERSION = "0.20";
	/**
	 * The update XML to download.
	 */
	public static final String UPDATE_XML = "http://open-ig.googlecode.com/svn/trunk/open-ig/update.xml";
	/** The Game module ID. */
	static final String GAME = "Game";
	/** The Launcher module ID. */
	static final String LAUNCHER = "Launcher";
	/** Background color. */
	private static final Color BG = new Color(0x05032B);
	/** The download buffer size. */
	static final int DOWNLOAD_BUFFER_SIZE = 128 * 1024;
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
	/** The new version's. */
	JLabel newVersion;
	/** The current version. */
	JLabel currentVersion;
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
	/** Current language. */
	String language = "en";
	/** The language flag. */
	JLabel flag;
	/** The english flag. */
	BufferedImage enFlag;
	/** The hungarian flag. */
	BufferedImage huFlag;
	/** The current upgrades. */
	private LUpdate updates;
	/** Not installed constant. */
	static final String NOT_INSTALLED = "0.00.000";
	/** The installed version. */
	String detectedVersion;
	/** The current worker. */
	SwingWorker<?, ?> worker;
	/** The cancel action. */
	ActionListener cancelAction;
	/** Creates the GUI. */
	public Launcher2() {
		super("Open Imperium Galactica Launcher v" + VERSION);
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				doClose();
			}
		});
		
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
				enFlag = ImageIO.read(u);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		try {
			URL u = getClass().getResource("/hu/openig/gfx/hungarian.png");
			if (u != null) {
				huFlag = ImageIO.read(u);
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
					g2.setComposite(AlphaComposite.SrcOver.derive(0.3f));
					g.drawImage(background, 0, 0, null);
					g2.setComposite(save0);
				}
			}
		};
		mainPanel.setOpaque(true);
		mainPanel.setBackground(BG);
		
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addComponent(mainPanel, 640, 640, 640)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(mainPanel, 480, 480, 480)
		);
		
		createPanel();
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
		init();
	}
	/**
	 * Returns the label.
	 * @param s the label
	 * @return the translated text
	 */
	String label(String s) {
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
		progressPanel = new JPanel();
		flag = new JLabel();
		
		install.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doInstall();
			}
		});
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (cancelAction != null) {
					cancelAction.actionPerformed(e);
					cancelAction = null;
				}
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
		launcher.setVisible(false);
		progressPanel.setVisible(false);
		flag.setVisible(false);

		Color c = new Color(0xD0D0D0);
		install.setForeground(c);
		update.setForeground(c);
		run.setForeground(c);
		mapEditor.setForeground(c);
		videoPlayer.setForeground(c);
		cancel.setForeground(c);
		other.setForeground(c);

		progressPanel.setOpaque(false);
		progressPanel.setBackground(BG);
		
		createProgressPanel();

		currentVersion.setForeground(Color.WHITE);
		changes.setForeground(Color.YELLOW);
		newVersion.setForeground(Color.ORANGE);
		launcher.setForeground(Color.PINK);

		runSettings = new JMenuItem();
		verify = new JMenuItem();
		uninstall = new JMenuItem();
		releaseNotes = new JMenuItem();
		projectPage = new JMenuItem();
		
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
		flag.setIcon(new ImageIcon(huFlag));
		flag.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
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
				.addComponent(flag, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(currentVersion)
				.addComponent(newVersion)
				.addComponent(changes)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(launcher)
					.addComponent(install)
					.addComponent(update)
					.addComponent(cancel)
				)
				.addComponent(run)
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
			.addComponent(flag)
			.addGap(5)
			.addComponent(newVersion)
			.addComponent(changes)
			.addGap(25)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(launcher)
				.addComponent(install)
				.addComponent(update)
				.addComponent(cancel)
			)
			.addGap(10)
			.addComponent(currentVersion)
			.addGap(10)
			.addComponent(run)
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
		currentAction.setForeground(Color.WHITE);
		currentFile.setForeground(Color.WHITE);
		currentFileProgress.setForeground(Color.WHITE);
		totalFileProgress.setForeground(Color.WHITE);
		
		
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
					.addComponent(currentAction)
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
	
			currentActionLabel.setText("Action:");
			currentFileLabel.setText("File:");
			currentFileProgressLabel.setText("Progress:");
			totalFileProgressLabel.setText("Total:");
		} else {
			install.setText("Telepítés");
			update.setText("Frissítés");
			cancel.setText("Mégsem");
			run.setText("Játék futtatása");
			mapEditor.setText("Térképszerkesztő");
			videoPlayer.setText("Videolejátszó");
			other.setText("Egyéb lehetőségek");
			launcher.setText("Indító frissítése");
			
			projectPage.setText("Projekt honlapja...");
			releaseNotes.setText("Release notes...");
			runSettings.setText("Run settings...");
			verify.setText("Verify installation");
			uninstall.setText("Uninstall");
	
			currentActionLabel.setText("Action:");
			currentFileLabel.setText("File:");
			currentFileProgressLabel.setText("Progress:");
			totalFileProgressLabel.setText("Total:");
		}
	}
	/**
	 * Close the launcher.
	 */
	void doClose() {
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
	public static void main(String[] args) {
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
				Launcher2 ln = new Launcher2();
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
		Desktop d = Desktop.getDesktop();
		if (d != null) {
			try {
				d.browse(new URI(url));
			} catch (IOException e) {
			} catch (URISyntaxException e) {
			}
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
		progressPanel.setVisible(true);
		currentAction.setText("Loading configuration...");
		currentFile.setText(format("open-ig-launcher-config.xml"));
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
					if (!config.delete()) {
						config.deleteOnExit();
					}
				}
				loadConfig();
				detectVersion();
				return null;
			}
			@Override
			protected void done() {
				currentFileProgress.setText("100%");
				totalFileProgress.setText("100%");
				fileProgress.setValue(100);
				totalProgress.setValue(100);
				setLabels();
				doUpgrades();
			}
		};
		w.execute();
	}
	/** Check for upgrades. */
	void doUpgrades() {
		currentAction.setText("Checking for upgrades...");
		currentFile.setText(format("http://open-ig.googlecode.com/.../update.xml"));
		currentFileProgress.setText("0%");
		totalFileProgress.setText("0%");
		fileProgress.setValue(0);
		totalProgress.setValue(0);
		final long ts = System.currentTimeMillis();
		final File updateFile = new File("update.xml." + ts);
		SwingWorker<XElement, Void> w = new SwingWorker<XElement, Void>() {
			@Override
			protected XElement doInBackground() throws Exception {
				downloadFiles(Collections.singletonList(UPDATE_XML), ts);
				return XElement.parseXML(updateFile.getAbsolutePath());
			}
			@Override
			protected void done() {
				progressPanel.setVisible(false);
				try {
					doProcessUpdate(get(), updateFile);
				} catch (ExecutionException ex) {
					ex.printStackTrace();
					errorMessage(format("Error during data download: %s", ex.toString()));
				} catch (InterruptedException ex) {
					ex.printStackTrace();
					errorMessage(format("Error during data download: %s", ex.toString()));
				}
			}
		};
		w.execute();
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
		currentAction.setText(label("Processing update.xml"));
		currentFile.setText(file.getName());
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
			if (!file.delete()) {
				errorMessage(format("Could not delete file %s", file));
			}
		} catch (Throwable t) {
			errorMessage(format("Error while processing file %s: %s", file, t));
		} finally {
			progressPanel.setVisible(false);
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
				if (size < 0) {
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
	 * @param progress the pain progress number
	 * @param position the current position.
	 * @param size the total size
	 * @param speed the download speed
	 */
	void setTotalProgress(final int progress, final long position, final long size, final double speed) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				totalProgress.setValue(progress);
				if (size < 0) {
					totalProgress.setIndeterminate(true);
					totalFileProgress.setText(String.format("?%%, %d KB, %.1f KB / s", position / 1024, speed));	
				} else {
					totalProgress.setIndeterminate(false);
					totalFileProgress.setText(String.format("%d%%, %d KB / %d KB , %.1f KB / s", (position * 100 / size), position / 1024, size / 1024, speed));
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
		File[] files = new File(".").listFiles(new FilenameFilter() {
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
				return o2.getName().compareTo(o1.getName());
			}
		});
		File top = files[0];
		
		String name = top.getName();
		
		int idx = name.indexOf(".jar");
		detectedVersion = name.substring(9, idx);
		
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
		LModule m = updates.getModule(LAUNCHER);
		if (m.compareVersion(VERSION) > 0) {
			launcher.setVisible(true);
			changes.setText(m.releaseNotes.getDescription(language));
			newVersion.setText(format("New version available: %s", m.version));
			
			changes.setVisible(true);
			newVersion.setVisible(true);
		}
		LModule g = updates.getModule(GAME);
		if (NOT_INSTALLED.equals(detectedVersion)) {
			install.setVisible(true);
			changes.setText(g.releaseNotes.getDescription(language));
			newVersion.setText(format("Version available: %s", g.version));

			changes.setVisible(true);
			newVersion.setVisible(true);
		} else {
			run.setVisible(true);
			mapEditor.setVisible(true);
			videoPlayer.setVisible(true);
			other.setVisible(true);
			
			currentVersion.setText(format("Version %s", detectedVersion));
			if (g.compareVersion(detectedVersion) > 0) {
				changes.setText(g.releaseNotes.getDescription(language));
				newVersion.setText(format("New version available: %s", g.version));

				changes.setVisible(true);
				newVersion.setVisible(true);
				update.setVisible(true);
			}
		}
	}
	/**
	 * Install the game.
	 */
	void doInstall() {
		final LModule g = updates.getModule(GAME);
		progressPanel.setVisible(true);
		currentAction.setText(label("Checking existing game files"));
		currentFileProgress.setText("0%");
		totalFileProgress.setText("0%");
		fileProgress.setValue(0);
		totalProgress.setValue(0);
		totalProgress.setMaximum(g.files.size());
		install.setVisible(false);
		cancel.setVisible(true);
		totalProgress.setVisible(true);
		totalFileProgress.setVisible(true);
		totalFileProgressLabel.setVisible(true);
		
		cancelAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (worker != null) {
					worker.cancel(true);
					worker = null;
				}
				install.setVisible(true);
				progressPanel.setVisible(false);
				cancel.setVisible(false);
			}
		};
		worker = new SwingWorker<List<String>, Void>() {
			@Override
			protected List<String> doInBackground() throws Exception {
				return collectDownloads(g);
			}
			@Override
			protected void done() {
				progressPanel.setVisible(false);
				worker = null;
				cancel.setVisible(false);
				try {
					doDownload(get());
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
	List<String> collectDownloads(LModule g) throws IOException {
		
		for (LRemoveFile rf : g.removeFiles) {
			File f = new File(rf.file);
			if (f.canWrite() && !f.delete()) {
				System.out.printf("Could not delete old file: %s%n", f);
			}
		}
		
		List<String> toDownload = new ArrayList<String>();

		long start = System.currentTimeMillis();
		long position = 0;
		for (int i = 0; i < g.files.size(); i++) {
			if (Thread.currentThread().isInterrupted()) {
				return toDownload;
			}
			
			double speed = position * 1000d / 1024 / (System.currentTimeMillis() - start);
			setTotalProgress((i + 1) * 100 / g.files.size(), position, -1, speed);
			setFileProgress(0, -1, 0);
			
			LFile lf = g.files.get(i);
			URL u = new URL(lf.url);
			String fn = u.getFile();
			int idx = fn.lastIndexOf("/");
			if (idx >= 0) {
				fn = fn.substring(idx + 1);
			}
			setCurrentFile(fn);
			
			File localFile = new File(fn);
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
								setTotalProgress((i + 1) * 100 / g.files.size(), position, -1, speed);
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
						setTotalProgress((i + 2) * 100 / g.files.size(), position, -1, speed);
					} finally {
						fin.close();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
					toDownload.add(lf.url);
				} catch (NoSuchAlgorithmException ex) {
					ex.printStackTrace();
					toDownload.add(lf.url);
				}
			} else {
				toDownload.add(lf.url);
			}
		}
		
		return toDownload;
	}
	/**
	 * The downloader.
	 * @param files the list of files to download
	 */
	void doDownload(final List<String> files) {
		progressPanel.setVisible(true);
		currentAction.setText(label("Downloading game files..."));
		currentFileProgress.setText("0%");
		totalFileProgress.setText("0%");
		fileProgress.setValue(0);
		totalProgress.setValue(0);
		totalProgress.setMaximum(files.size());
		install.setVisible(false);
		cancel.setVisible(true);
		totalProgress.setVisible(true);
		totalFileProgress.setVisible(true);
		totalFileProgressLabel.setVisible(true);
		
		cancelAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (worker != null) {
					worker.cancel(true);
					worker = null;
				}
				progressPanel.setVisible(false);
				cancel.setVisible(false);
				doActOnUpdates();
			}
		};
		worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				downloadFiles(files, -1);
				detectVersion();
				return null;
			}
			@Override
			protected void done() {
				progressPanel.setVisible(false);
				worker = null;
				cancel.setVisible(false);
				doActOnUpdates();
			}
		};
		worker.execute();
	}
	/**
	 * Download all files from the list of URLs.
	 * @param files the list of files
	 * @param timestamp the custom timestamp to use for temporary filenames, -1 if not
	 * @throws IOException on error
	 */
	void downloadFiles(List<String> files, long timestamp) throws IOException {
		long start = System.currentTimeMillis();
		long position = 0;
		for (int i = 0; i < files.size(); i++) {
			if (Thread.currentThread().isInterrupted()) {
				return;
			}
			
			double speed = position * 1000d / 1024 / (System.currentTimeMillis() - start);
			setTotalProgress((i + 1) * 100 / files.size(), position, -1, speed);
			setFileProgress(0, -1, 0);
			
			URL u = new URL(files.get(i));
			String fn = u.getFile();
			int idx = fn.lastIndexOf("/");
			if (idx >= 0) {
				fn = fn.substring(idx + 1);
			}
			if (timestamp >= 0) {
				fn += "." + timestamp;
			}
			setCurrentFile(fn);
			
			File localFile = new File(fn);
			
			HttpURLConnection conn = (HttpURLConnection)u.openConnection();
			conn.connect();
			try {
				InputStream in = conn.getInputStream();
				long length = conn.getContentLengthLong();
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
									setTotalProgress(i + 1, position, -1, speed);
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
							setTotalProgress(i + 2, position, -1, speed);
						} finally {
							fout.close();
						}
					} catch (IOException ex) {
						ex.printStackTrace();
					} catch (NoSuchAlgorithmException ex) {
						ex.printStackTrace();
					}					
				} finally {
					in.close();
				}
			} finally {
				conn.disconnect();
			}
		}
	}
}
