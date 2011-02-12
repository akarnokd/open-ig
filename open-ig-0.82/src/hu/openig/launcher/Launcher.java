/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.launcher;

import hu.openig.utils.IOUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.GroupLayout.Alignment;

/**
 * @author karnokd, 2010.10.31.
 * @version $Revision 1.0$
 */
public class Launcher extends JFrame {
	/** */
	private static final long serialVersionUID = -5640883678496406236L;
	/** The launcher's version. */
	static final String VERSION = "0.1";
	/** The list of stuff. */
	JPanel listPanel;
	/** The exit buttom. */
	private JButton exit;
	/** The language label. */
	private JLabel lang;
	/** The language combobox. */
	private JComboBox langList;
	/** The update object. */
	LUpdate update;
	/** The current language. */
	public String currentLanguage = "en";
	/** Refresh button. */
	private JButton refresh;
	/** The installed versions of the stuff. */
	final Map<String, String> installedVersions = new HashMap<String, String>();
	/** Construct the launcher window. */
	public Launcher() {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		installedVersions.put("Launcher", VERSION);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				saveInstalledVersions();
			}
		});
		
		setTitle("Launcher " + VERSION);
		
		listPanel = new JPanel();
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.PAGE_AXIS));
		
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		
		exit = new JButton("Exit");
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doExit();
			}
		});
		
		JScrollPane sp = new JScrollPane(listPanel);
		sp.getVerticalScrollBar().setBlockIncrement(3 * 12);
		sp.getVerticalScrollBar().setUnitIncrement(12);
		
		c.add(sp, BorderLayout.CENTER);
		
		JPanel top = new JPanel(new FlowLayout());
		
		lang = new JLabel("Language:");
		langList = new JComboBox(new String[] { "English - Angol", "Hungarian - Magyar" });
		langList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doChangeLanguage();
			}
		});
		
		refresh = new JButton("Refresh");
		refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				downloadUpdateXML();
			}
		});
		
		top.add(lang);
		top.add(langList);
		top.add(new JLabel("   "));
		top.add(refresh);
		top.add(new JLabel(" "));
		top.add(exit);
		
		c.add(top, BorderLayout.NORTH);
		
		setResizable(false);
		setSize(640, 400);
		setLocationRelativeTo(null);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				loadInstalledVersions();
				downloadUpdateXML();
			}
		});
	}
	/** The download progress panel. */
	class DownloadPanel extends JPanel {
		/** */
		private static final long serialVersionUID = -7515872931469391835L;
		/** The statistics label. */
		public JLabel statistics;
		/** The progress bar. */
		public JProgressBar bar;
		/** The cancel button. */
		public JButton cancel;
		/** Constructors. */
		public DownloadPanel() {
			bar = new JProgressBar();
			statistics = new JLabel("...");
			cancel = new JButton("Cancel");
			
			GroupLayout gl = new GroupLayout(this);
			gl.setAutoCreateContainerGaps(true);
			gl.setAutoCreateGaps(true);
			setLayout(gl);
			
			gl.setHorizontalGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup()
					.addComponent(bar)
					.addComponent(statistics)
				)
				.addComponent(cancel)
			);
			gl.setVerticalGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(bar)
					.addComponent(statistics)
				)
				.addComponent(cancel)
			);
		}
	}
	/** Download the update.xml . */
	void downloadUpdateXML() {
		listPanel.removeAll();
		final DownloadPanel dlp = new DownloadPanel();
		listPanel.add(dlp);
		final String fn = "update-" + System.currentTimeMillis() + ".xml"; 
		final Downloader dl = new Downloader("http://open-ig.googlecode.com/svn/trunk/open-ig/update.xml",
				fn, new DownloadCallback() {
			@Override
			public void failed(Throwable exception) {
				dlp.statistics.setText(exception.toString());
			}
			@Override
			public void progress(DownloadProgress progress) {
				if (progress.bytesTotal < 0) {
					dlp.bar.setIndeterminate(true);
				} else {
					dlp.bar.setValue((int)(progress.bytesReceived * 100 / progress.bytesTotal));
				}
			}
			@Override
			public void success(DownloadProgress progress, byte[] sha1) {
				if (progress.bytesTotal < 0 || (progress.bytesReceived == progress.bytesTotal)) {
					doUpdateXMLCompleted(fn);
					new File(fn).delete();
					listPanel.remove(dlp);
					listPanel.revalidate();
					listPanel.repaint();
				}
			}
			@Override
			public void cancelled() {
				listPanel.remove(dlp);
				listPanel.revalidate();
				listPanel.repaint();
				new File(fn).delete();
			}
		});
		dlp.cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dl.cancel(true);
			}
		});
		dl.execute();
	}
	/**
	 * Do something with the received update XML.
	 * @param fileName the local filename
	 */
	void doUpdateXMLCompleted(String fileName) {
		update = new LUpdate();
		byte[] data = IOUtils.load(fileName);
		try {
			update.parse(data);
			doDisplayModules();
		} catch (IOException ex) {
			
		}
	}
	/** Display the modules. */
	void doDisplayModules() {
		final LModule launcherModule = update.getModule("Launcher");
		if (launcherModule != null && launcherModule.compareVersion(VERSION) > 0) {
			final ModulePanel mp = new ModulePanel();
			mp.title.setText("Launcher " + VERSION);
			mp.title.setToolTipText(launcherModule.general.getDescription(currentLanguage).replaceAll("\\s+", " "));
			mp.updateAvailable.setText("Version " + launcherModule.version + ": " + launcherModule.releaseNotes.getDescription(currentLanguage));
			mp.generalInfo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			mp.generalInfo.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					doNavigate(launcherModule.general.url);
				}
			});
			mp.releaseNotes.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					doNavigate(launcherModule.releaseNotes.url);
				}
			});
			
			mp.generalInfo.setToolTipText(launcherModule.general.url);
			mp.releaseNotes.setToolTipText(launcherModule.releaseNotes.url);

			mp.start.setVisible(false);
			mp.update.setVisible(true);
			mp.update.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					updateModule(mp, launcherModule);
				}
			});
			
			
			mp.verify.setVisible(false);
			mp.install.setVisible(false);
			mp.install.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					updateModule(mp, launcherModule);
				}
			});
			mp.remove.setVisible(false);
			mp.updateAvailable.setVisible(true);
			listPanel.add(mp);
		}
		for (final LModule m : update.modules) {
			if (m.id.equals("Launcher")) {
				continue;
			}
			final ModulePanel mp = new ModulePanel();
			mp.title.setText(m.id);
			mp.title.setToolTipText(m.general.getDescription(currentLanguage).replaceAll("\\s+", " "));
			mp.generalInfo.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					doNavigate(m.general.url);
				}
			});
			mp.releaseNotes.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			mp.releaseNotes.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					doNavigate(m.releaseNotes.url);
				}
			});
			
			mp.generalInfo.setToolTipText(m.general.url);
			mp.releaseNotes.setToolTipText(m.releaseNotes.url);
			
			mp.updateAvailable.setText("Version " + m.version + ": " + m.releaseNotes.getDescription(currentLanguage));
			
			setVisibleModuleButtons(m, mp);
			mp.install.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					updateModule(mp, m);
				}
			});
			mp.update.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					updateModule(mp, m);
				}
			});
			mp.start.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					startModule(mp, m);
				}
			});
			mp.remove.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					removeModule(mp, m);
				}
			});
			
			listPanel.add(mp);
		}
		listPanel.revalidate();
		listPanel.repaint();
	}
	/**
	 * Remove the specified module.
	 * @param mp the module panel
	 * @param m the module definition
	 */
	protected void removeModule(ModulePanel mp, LModule m) {
		for (LFile f : m.files) {
			int idx = f.url.lastIndexOf("/");
			File f0 = new File(f.url.substring(idx + 1));
			f0.delete();
		}
		installedVersions.remove(m.id);
		setVisibleModuleButtons(m, mp);
	}
	/**
	 * Set the module button visibility based on the current state.
	 * @param m the module definition
	 * @param mp module panel
	 */
	void setVisibleModuleButtons(final LModule m, final ModulePanel mp) {
		mp.progress.setVisible(false);
		mp.statistics.setVisible(false);
		String iv = installedVersions.get(m.id);
		if (iv == null) {
			mp.title.setText(m.id);
			mp.install.setVisible(true);
			mp.start.setVisible(false);
			mp.verify.setVisible(false);
			mp.update.setVisible(false);
			mp.remove.setVisible(false);
			mp.updateAvailable.setVisible(true);
		} else {
			mp.title.setText(m.id + " " + iv);
			mp.install.setVisible(false);
			mp.start.setVisible(true);
			mp.remove.setVisible(true);
			int c = m.compareVersion(iv); 
			if (c > 0) {
				mp.update.setVisible(true);
				mp.updateAvailable.setVisible(m.compareVersion(iv) > 0);
			} else {
				mp.verify.setVisible(c == 0);
				mp.update.setVisible(false);
				mp.updateAvailable.setVisible(false);
			}
		}
		mp.cancel.setVisible(false);
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
	/** The module panel. */
	class ModulePanel extends JPanel {
		/** */
		private static final long serialVersionUID = 129442212074771400L;
		/** The title line. */
		public JLabel title;
		/** If a new update is available, this label will list its version and short text. */
		public JLabel updateAvailable;
		/** The link that opens the general info page. */
		public JLabel generalInfo;
		/** The link that opens the release notes page. */
		public JLabel releaseNotes;
		/** Start the program. */
		public JButton start;
		/** Update the program. */
		public JButton update;
		/** Verify the program files. */
		public JButton verify;
		/** Install the program. */
		public JButton install;
		/** Remove the program. */
		public JButton remove;
		/** The progressbar. */
		public JProgressBar progress;
		/** The statistics part. */
		public JLabel statistics;
		/** Cancel the installation progress. */
		public JButton cancel;
		/** Constructor. */
		public ModulePanel() {
			setOpaque(true);
			title = new JLabel();
			title.setOpaque(true);
			title.setForeground(Color.WHITE);
			title.setHorizontalAlignment(SwingConstants.CENTER);
			title.setBackground(Color.DARK_GRAY);
			updateAvailable = new JLabel();
			updateAvailable.setOpaque(true);
			updateAvailable.setBackground(Color.BLUE);
			updateAvailable.setForeground(Color.WHITE);
			generalInfo = new JLabel("<html><font color='blue'><u>General information");
			generalInfo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			releaseNotes = new JLabel("<html><font color='blue'><u>Release notes");
			releaseNotes.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			
			start = new JButton("Start");
			update = new JButton("Update");
			verify = new JButton("Verify");
			install = new JButton("Install");
			remove = new JButton("Remove");
			cancel = new JButton("Cancel");
			cancel.setVisible(false);
			
			progress = new JProgressBar();
			progress.setVisible(false);
			statistics = new JLabel();
			statistics.setVisible(false);
			
			GroupLayout gl = new GroupLayout(this);
			this.setLayout(gl);
			
			updateAvailable.setVisible(false);

			JLabel space = new JLabel("    ");
			
			gl.setHorizontalGroup(
				gl.createSequentialGroup()
				.addContainerGap()
				.addGroup(
					gl.createParallelGroup()
					.addComponent(title, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addComponent(updateAvailable, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addGroup(
						gl.createSequentialGroup()
						.addGroup(
							gl.createParallelGroup()
							.addComponent(generalInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(releaseNotes, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						)
						.addComponent(space)
						.addComponent(start)
						.addComponent(update)
						.addComponent(verify)
						.addComponent(install)
						.addComponent(remove)
						.addComponent(cancel)
					)
					.addComponent(progress)
					.addComponent(statistics)
				)
				.addContainerGap()
			);
			gl.setVerticalGroup(
				gl.createSequentialGroup()
				.addContainerGap()
				.addComponent(title)
				.addComponent(updateAvailable)
				.addGroup(
					gl.createParallelGroup(Alignment.CENTER)
					.addComponent(space)
					.addComponent(start)
					.addComponent(update)
					.addComponent(verify)
					.addComponent(install)
					.addComponent(remove)
					.addComponent(cancel)
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(generalInfo)
						.addComponent(releaseNotes)
					)
				)
				.addComponent(progress)
				.addComponent(statistics)
			);
		}
	}
	/** Perform the exit. */
	void doExit() {
		dispose();
	}
	/** Changle language. */
	void doChangeLanguage() {
		switch (langList.getSelectedIndex()) {
		case 1:
			currentLanguage = "hu";
			break;
		default:
			currentLanguage = "en";
		}
	}
	/**
	 * Update the launcher itself.
	 */
	void doUpdateItself() {
		
	}
	/**
	 * Load the installed versions.
	 */
	void loadInstalledVersions() {
		installedVersions.clear();
		Properties props = new Properties();
		try {
			File f = new File("launcher-config.xml");
			if (f.canRead()) {
				FileInputStream fin = new FileInputStream(f);
				try {
					props.loadFromXML(fin);
					for (Map.Entry<Object, Object> e : props.entrySet()) {
						installedVersions.put(e.getKey().toString(), e.getValue().toString());
					}
				} finally {
					fin.close();
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Save the installed versions configuration.
	 */
	void saveInstalledVersions() {
		File f = new File("launcher-config.xml");
		try {
			FileOutputStream fout = new FileOutputStream(f);
			try {
				Properties props = new Properties();
				props.putAll(installedVersions);
				props.storeToXML(fout, "Open Imperium Galactica Launcher Configuration");
			} finally {
				fout.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
//	/**
//	 * Start the installation of the given module.
//	 * @param mp the module panel
//	 * @param m the module definition
//	 */
//	protected void installModule(final ModulePanel mp, final LModule m) {
//		mp.install.setVisible(false);
//		mp.cancel.setVisible(true);
//
//		List<String> localFiles = new ArrayList<String>();
//		long t = System.currentTimeMillis();
//		for (LFile f : m.files) {
//			int idx = f.url.lastIndexOf("/");
//			localFiles.add(f.url.substring(idx + 1) + "." + t);
//		}
//		
//		final Downloader[] currentDownloader = new Downloader[1];
//		
//		downloadLoop(localFiles, mp, m, 0, currentDownloader);
//		
//		mp.cancel.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				currentDownloader[0].cancel(true);
//				mp.cancel.setVisible(false);
//				setVisibleModuleButtons(m, mp);
//			}
//		});
//	}
	/** The file entry. */
	class FileEntry {
		/**
		 * @param localName the local name
		 * @param remoteFile the remote file
		 */
		public FileEntry(String localName, LFile remoteFile) {
			this.localName = localName;
			this.remoteFile = remoteFile;
		}
		/** The local name. */
		public String localName;
		/** The remote file name. */
		public LFile remoteFile;
	}
	/**
	 * Start the installation of the given module.
	 * @param mp the module panel
	 * @param m the module definition
	 */
	protected void updateModule(final ModulePanel mp, final LModule m) {
		mp.install.setVisible(false);
		mp.update.setVisible(false);
		mp.cancel.setVisible(true);

		mp.progress.setVisible(true);
		mp.statistics.setVisible(true);
		
		SwingWorker<Void, Void> pretest = new SwingWorker<Void, Void>() {
			final List<FileEntry> localFiles = new ArrayList<FileEntry>();
			@Override
			protected Void doInBackground() throws Exception {
				long t = System.currentTimeMillis();
				int index = 0;
				for (LFile f : m.files) {
					int idx = f.url.lastIndexOf("/");
					final String lf = f.url.substring(idx + 1);

					File lff = new File(lf);

					try {
						MessageDigest sha1 = MessageDigest.getInstance("SHA1");
						if (lff.canRead()) {
							
							byte[] buffer = new byte[64 * 1024];
							long bytesReceived = 0;
							long bytesTotal = lff.length();
							try {
								
								FileInputStream fin = new FileInputStream(lff);
								do {
									int read = fin.read(buffer);
									if (read > 0) {
										bytesReceived += read;
										final int findex = index;
										final int fsize = m.files.size();
										final long fbytesReceived = bytesReceived;
										final long fbytesTotal = bytesTotal;
										SwingUtilities.invokeLater(new Runnable() {
											@Override 
											public void run() {
												mp.statistics.setText("[" + (findex + 1) + " / " + fsize + "] "
														+ lf + " (" + String.format("%.2f", fbytesReceived / 1024.0 / 1024.0) + " MB)"  
												);
												mp.progress.setValue((int)(100 * fbytesReceived / fbytesTotal));
											};
										});
										sha1.update(buffer, 0, read);
									} else
									if (read < 0) {
										break;
									}
								} while (true);
								byte[] sha1h = sha1.digest();
								byte[] sha1hupdate = LFile.toByteArray(f.sha1);
								if (!Arrays.equals(sha1h, sha1hupdate)) {
									localFiles.add(new FileEntry(lf + "." + t, f));
								}
							} catch (IOException ex) {
								ex.printStackTrace();
							}
						} else {			
							localFiles.add(new FileEntry(lf + "." + t, f));
						}
						index++;
					} catch (NoSuchAlgorithmException ex) {
						ex.printStackTrace();
					}
				}
				return null;
			}
			@Override
			protected void done() {
				if (localFiles.size() == 0) {
					installedVersions.put(m.id, m.version);
					if (m.id.equals("Launcher")) {
						listPanel.remove(mp);
						listPanel.revalidate();
						listPanel.repaint();
					} else {
						setVisibleModuleButtons(m, mp);
					}
					return;
				}
				
				final Downloader[] currentDownloader = new Downloader[1];
				
				downloadLoop(localFiles, mp, m, 0, currentDownloader);
				
				mp.cancel.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						currentDownloader[0].cancel(true);
						mp.cancel.setVisible(false);
						setVisibleModuleButtons(m, mp);
					}
				});
			}
		};
		pretest.execute();
		
	}
	/**
	 * The download loop for each files.
	 * @param localFiles the local files
	 * @param mp the module panel
	 * @param m the module definition
	 * @param index the index to continue
	 * @param currentDownloader the active downloader
	 */
	private void downloadLoop(final List<FileEntry> localFiles, final ModulePanel mp, final LModule m, final int index,
			final Downloader[] currentDownloader) {
		if (index >= localFiles.size()) {
			// last
			downloadCompleted(mp, m, localFiles);
			return;
		}
		mp.progress.setVisible(true);
		mp.statistics.setVisible(true);
		FileEntry fe = localFiles.get(index);;
		LFile f = fe.remoteFile;
		final String lf = fe.localName;
		currentDownloader[0] = new Downloader(f.url, lf, new DownloadCallback() {
			@Override
			public void success(DownloadProgress progress, byte[] sha1) {
				mp.statistics.setText("[" + (index + 1) + " / " + localFiles.size() + "] "
						+ lf + " (" + String.format("%.2f", progress.bytesReceived / 1024.0 / 1024.0) + " MB, " + String.format("%.2f KB/s)", progress.getSpeed() / 1.024)  
				);
				downloadLoop(localFiles, mp, m, index + 1, currentDownloader);
			}
			@Override
			public void progress(DownloadProgress progress) {
				if (progress.bytesTotal < 0) {
					mp.progress.setIndeterminate(true);
				} else {
					mp.progress.setValue((int)(100 * progress.bytesReceived / progress.bytesTotal));
				}
				mp.statistics.setText("[" + (index + 1) + " / " + localFiles.size() + "] "
					+ lf + " (" + String.format("%.2f", progress.bytesReceived / 1024.0 / 1024.0) + " MB, " + String.format("%.2f KB/s)", progress.getSpeed() / 1.024)  
				);
			}
			
			@Override
			public void failed(Throwable exception) {
				exception.printStackTrace();
				mp.cancel.setVisible(false);
				setVisibleModuleButtons(m, mp);
			}
			@Override
			public void cancelled() {
				mp.cancel.setVisible(false);
				// delete temporary files
				for (FileEntry f : localFiles) {
					File f0 = new File(f.localName);
					f0.delete();
				}
				setVisibleModuleButtons(m, mp);
			}
		});
		currentDownloader[0].execute();
	}
	/**
	 * If the download completed successfully, rename. 
	 * @param mp the module panel.
	 * @param m the module
	 * @param localFiles the list of the local file names
	 */
	private void downloadCompleted(ModulePanel mp, LModule m, List<FileEntry> localFiles) {
		installedVersions.put(m.id, m.version);
		if (!m.id.equals("Launcher")) {
			for (int i = 0; i < localFiles.size(); i++) {
				FileEntry fe = localFiles.get(i);
				String s = fe.localName;
				File f = new File(s);
				int idx = s.lastIndexOf(".");
				String s1 = s.substring(0, idx);
				File f2 = new File(s1);
				if (f2.exists()) {
					f2.delete();
				}
				if (!f.renameTo(f2)) {
					System.err.printf("Could not rename %s to %s%n", s, s1);
				}
			}
			for (LRemoveFile rf : m.removeFiles) {
				File f = new File(rf.file);
				f.delete();
			}
			mp.progress.setVisible(false);
			mp.statistics.setVisible(false);
			setVisibleModuleButtons(m, mp);
		} else {
			String ff = "";
			for (FileEntry s : localFiles) {
				if (s.localName.startsWith("open-ig-launcher.jar")) {
					ff = s.localName;
					break;
				}
			}
			ProcessBuilder pb = new ProcessBuilder();
			pb.command(System.getProperty("java.home") + "/bin/java", "-jar", ff, "-selfupdate", ff);
			try {
				pb.start();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			dispose();
		}
	}
	/**
	 * Start the module.
	 * @param mp the module panel.
	 * @param m the module
	 */
	protected void startModule(ModulePanel mp, LModule m) {
		ProcessBuilder pb = new ProcessBuilder();
		if (m.memory > 0) {
			pb.command(System.getProperty("java.home") + "/bin/java", "-Xmx" + m.memory + "M", "-jar", String.format(m.executeFile, installedVersions.get(m.id)));
		} else {
			pb.command(System.getProperty("java.home") + "/bin/java", "-jar", String.format(m.executeFile, installedVersions.get(m.id)));
		}
		try {
			pb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		if (args.length >= 2) {
			if ("-selfupdate".equals(args[0])) {
				// make a copy of self and restart
				File f0 = new File(args[1]);
				File old = new File("open-ig-launcher.jar");
				try {
					while (old.exists() && !old.delete()) {
						Thread.sleep(1000);	
					}
					IOUtils.save(old, IOUtils.load(f0));
					ProcessBuilder pb = new ProcessBuilder();
					pb.command(System.getProperty("java.home") + "/bin/java", "-jar", "open-ig-launcher.jar", "-selfdelete", args[1]);
					pb.start();
					return;
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			} else
			if ("-selfdelete".equals(args[0])) {
				File f0 = new File(args[1]);
				try {
					while (f0.exists() && !f0.delete()) {
						Thread.sleep(1000);	
					}
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Launcher ln = new Launcher();
				ln.setVisible(true);
			}
		});
	}
}
