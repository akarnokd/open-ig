/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.launcher.Launcher;
import hu.openig.model.Configuration;
import hu.openig.utils.Exceptions;
import hu.openig.utils.IOUtils;
import hu.openig.utils.XElement;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.xml.stream.XMLStreamException;

/**
 * Utility program to package the executable JAR file and upgrade packs for image+data.
 * @author akarnokd
 */
public final class PackageStuff {
	/** Utility class. */
	private PackageStuff() {
		
	}
	/** The separator. */
	static String sep = File.separator;
	/**
	 * Build the graphics/data patch file.
	 * @param version the version number in the file
	 * @return the digest
	 */
	static String buildImages(String version) {
		String result = "";
		String fileName = "open-ig-images-" + version + ".zip";
		try (ZipOutputStream zout = new ZipOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(fileName), 1024 * 1024))) {
			zout.setLevel(9);
			processDirectory("." + sep + "images" + sep + "", "." + sep + "images", zout, null);
		} catch (IOException ex) {
			Exceptions.add(ex);
		}
		try {
			result += computeDigest(fileName);
		} catch (IOException ex) {
			Exceptions.add(ex);
		}
		return result;
	}
	/**
	 * Build the graphics/data patch file.
	 * @param version the version number in the file
	 * @return the digest
	 */
	static String buildData(String version) {
		String result = "";
		String fileName = "open-ig-upgrade-" + version + "2.zip";
		try (ZipOutputStream zout = new ZipOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(fileName), 1024 * 1024))) {
			zout.setLevel(9);
			processDirectory("." + sep + "data" + sep + "", "." + sep + "data", zout, null);
		} catch (IOException ex) {
			Exceptions.add(ex);
		}
		try {
			result += computeDigest(fileName);
		} catch (IOException ex) {
			Exceptions.add(ex);
		}
		return result;
	}
	/**
	 * Build the game's runnable jar.
	 * @param version the target version
	 * @return the digest
	 */
	static String buildGame(String version) {
		String fileName = "open-ig-" + version + ".jar";
		try (ZipOutputStream zout = new ZipOutputStream(
				new BufferedOutputStream(
						new FileOutputStream(fileName), 1024 * 1024))) {
			zout.setLevel(9);
			processDirectory("." + sep + "bin" + sep + "", "." + sep + "bin", zout, new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					name = name.toLowerCase(Locale.ENGLISH);
					String d = dir.toString().replace('\\', '/');
					if (!d.endsWith("/")) {
						d += "/";
					}
					d += name;
					return !name.contains("splash_medium")
							&& !name.contains("launcher_background")
							&& !d.contains("/launcher");
				}
			});
			addFile("META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF", zout);
		} catch (IOException ex) {
			Exceptions.add(ex);
		}
		try {
			return computeDigest(fileName);
		} catch (IOException ex) {
			Exceptions.add(ex);
		}
		return "";
	}
	/**
	 * Compute the SHA1 digest of the given file.
	 * @param fileName the filename
	 * @return the digest string + the filename
	 * @throws IOException on error
	 */
	static String computeDigest(String fileName) throws IOException {
		try (FileInputStream fis = new FileInputStream(fileName);
            DigestInputStream din = new DigestInputStream(fis, MessageDigest.getInstance("SHA1"))) {
			byte[] buffer = new byte[8192];
			while (true) {
				if (din.read(buffer) < 0) {
					break;
				}
			}
			byte[] digest = din.getMessageDigest().digest();
			StringBuilder sb = new StringBuilder();
			for (byte b : digest) {
				sb.append(String.format("%02X", b & 0xFF));
			}
			sb.append(" ").append(fileName).append("\r\n");
			return sb.toString();
		} catch (NoSuchAlgorithmException ex) {
			Exceptions.add(ex);
		}
		return "";
	}
	/**
	 * Build the launcher file.
	 * @return the digest
	 */
	static String buildLauncher() {
		String fileName = "open-ig-launcher.jar";
		try (ZipOutputStream zout = new ZipOutputStream(
				new BufferedOutputStream(
						new FileOutputStream(fileName), 1024 * 1024))) {
			zout.setLevel(9);
			processDirectory("." + sep + "bin" + sep + "", "." + sep + "bin", zout, new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					String d = dir.toString().replace('\\', '/');
					if (!d.endsWith("/")) {
						d += "/";
					}
					d += name;
					return d.contains("hu/openig/launcher") 
							|| d.contains("hu/openig/utils/Exceptions")
							|| d.contains("hu/openig/utils/XElement")
							|| d.contains("hu/openig/utils/IOUtils")
							|| d.contains("hu/openig/utils/Parallels")
							|| d.contains("hu/openig/utils/ImageUtils")
							|| d.contains("hu/openig/utils/ConsoleWatcher")
							|| d.contains("hu/openig/gfx/checkmark_grayscale.png")
							|| d.contains("hu/openig/gfx/checkmark.png")
							|| d.contains("hu/openig/gfx/button_medium.png")
							|| d.contains("hu/openig/gfx/button_medium_pressed.png")
							|| d.contains("hu/openig/gfx/launcher_background.png")
							|| d.contains("hu/openig/gfx/hungarian.png")
							|| d.contains("hu/openig/gfx/english.png")
							|| d.contains("hu/openig/gfx/german.png")
							|| d.contains("hu/openig/gfx/french.png")
							|| d.contains("hu/openig/gfx/russian.png")
							|| d.contains("hu/openig/gfx/loading.gif")
							|| d.contains("hu/openig/gfx/down.png")
							|| d.contains("hu/openig/ui/IGButton")
							|| d.contains("hu/openig/ui/IGCheckBox")
							|| d.contains("hu/openig/render/RenderTools")
							|| d.contains("hu/openig/render/GenericMediumButton")
							|| d.contains("hu/openig/render/GenericButtonRenderer")
							|| d.contains("hu/openig/core/Func0");
				}
			});
			addFile("META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF.launcher", zout);
		} catch (IOException ex) {
			Exceptions.add(ex);
		}
		try {
			return computeDigest(fileName);
		} catch (IOException ex) {
			Exceptions.add(ex);
		}
		return "";
	}
	/**
	 * Add the given fileName to the zip stream wiht the given entry name.
	 * @param entryName the entry name
	 * @param fileName the file name and path
	 * @param zout the output stream
	 * @throws IOException on error
	 */
	static void addFile(String entryName, String fileName, ZipOutputStream zout)
	throws IOException {
		ZipEntry mf = new ZipEntry(entryName);
		File mfm = new File(fileName);
		if (mfm.canRead()) {
			mf.setSize(mfm.length());
			mf.setTime(mfm.lastModified());
			zout.putNextEntry(mf);
			zout.write(IOUtils.load(mfm));
		} else {
			throw new RuntimeException("File not found: " + mfm.toString());
		}
	}
	/**
	 * Process the contents of the given directory.
	 * @param baseDir the base directory
	 * @param currentDir the current directory
	 * @param zout the output stream
	 * @param filter the optional file filter
	 * @throws IOException on error
	 */
	static void processDirectory(String baseDir, String currentDir, ZipOutputStream zout,
			FilenameFilter filter) throws IOException {
		File[] files = new File(currentDir).listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return !pathname.isHidden();
			}
		});
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					if (!f.getName().equals(".svn")) {
						processDirectory(baseDir, f.getPath(), zout, filter);
					}
				} else {
					String fpath = f.getPath();
					String fpath2 = fpath.substring(baseDir.length());
					
					if (filter == null || filter.accept(f.getParentFile(), f.getName())) {
						System.out.printf("Adding %s as %s%n", fpath, fpath2);
						ZipEntry ze = new ZipEntry(fpath2.replace('\\', '/'));
						ze.setSize(f.length());
						ze.setTime(f.lastModified());
						zout.putNextEntry(ze);
						
						zout.write(IOUtils.load(f));
					}
				}
			}
		}
	}
	/**
	 * Append a result line.
	 * @param area the target text area
	 * @param line the line
	 */
	static void appendLine(final JTextArea area, final String line) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				area.append(line);
			}
		});
	}
	/**
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame f = new JFrame("Build");

                f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                JPanel p = new JPanel();
                p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
                f.getContentPane().add(p);

                final JCheckBox cb1 = new JCheckBox("Build game", true);
                final JCheckBox cb2 = new JCheckBox("Build launcher", false);
                final JCheckBox cb3 = new JCheckBox("Build data", true);
                final JCheckBox cb4 = new JCheckBox("Build images", false);
                final JCheckBox cb5 = new JCheckBox("Modify update.xml", false);

                final JTextArea text = new JTextArea(6, 50);
                text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, cb1.getFont().getSize()));
                text.setColumns(80);
                final JScrollPane sp = new JScrollPane(text);


                final JButton run = new JButton("Run");

                run.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        text.setText("");
                        run.setEnabled(false);
                        final boolean v1 = cb1.isSelected();
                        final boolean v2 = cb2.isSelected();
                        final boolean v3 = cb3.isSelected();
                        final boolean v4 = cb4.isSelected();
                        final boolean v5 = cb5.isSelected();
                        SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                final ConcurrentMap<String, String> checksums = new ConcurrentHashMap<>();
                                final ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
                                if (v1) {
                                    exec.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            String buildGame = buildGame(Configuration.VERSION);
                                            appendLine(text, buildGame);
                                            checksums.put("game", buildGame);
                                        }
                                    });
                                }
                                if (v2) {
                                    exec.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            String buildLauncher = buildLauncher();
                                            appendLine(text, buildLauncher);
                                            checksums.put("launcher", buildLauncher);
                                        }
                                    });
                                }
                                if (v3) {
                                    exec.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                                            String buildData = buildData(sdf.format(new Date()) + "a");
                                            appendLine(text, buildData);
                                            checksums.put("data", buildData);
                                        }
                                    });
                                } 
                                if (v4) {
                                    exec.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                                            String buildImages = buildImages(sdf.format(new Date()) + "a");
                                            appendLine(text, buildImages);
                                            checksums.put("images", buildImages);
                                        }
                                    });
                                }
                                exec.shutdown();
                                exec.awaitTermination(1, TimeUnit.DAYS);
                                if (v5) {
                                    modifyUpdateXML(checksums);
                                }
                                return null;
                            }
                            @Override
                            protected void done() {
                                run.setEnabled(true);
                            }
                        };
                        sw.execute();
                    }
                });
                p.add(cb1);
                p.add(cb2);
                p.add(cb3);
                p.add(cb4);
                p.add(cb5);
                p.add(sp);
                p.add(run);
                f.pack();
                f.setResizable(false);
                f.setLocationRelativeTo(null);
                f.setVisible(true);
            }
        });
	}
	/**
	 * Modify the update.xml according to the changed resources.
	 * @param checksums the checksums
	 */
	static void modifyUpdateXML(Map<String, String> checksums) {
		try {
			XElement update = XElement.parseXML("update.xml");
			
			if (checksums.containsKey("game")) {
				updateJarFile(update, "Game", Configuration.VERSION, checksums.get("game"));
			}
			if (checksums.containsKey("launcher")) {
				updateJarFile(update, "Launcher", Launcher.VERSION, checksums.get("launcher"));
			}
			if (checksums.containsKey("data")) {
				updateZipFile(update, "Game", "open-ig-upgrade-", checksums.get("data"), false);
			}
			if (checksums.containsKey("images")) {
				updateZipFile(update, "Game", "open-ig-images-", checksums.get("images"), true);
			}
			
			if (!checksums.isEmpty()) {
				update.save("update.xml");
			}
		} catch (IOException | XMLStreamException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Update a module's main JAR file.
	 * @param update the XML
	 * @param module the module name
	 * @param version the jar version
	 * @param checksum the sha1 + filename
	 */
	static void updateJarFile(XElement update, 
			String module, 
			String version, 
			String checksum) {
		String sha1 = checksum;
		String name = sha1.substring(41).trim();
		sha1 = sha1.substring(0, 40).trim();
		
		for (XElement xmodules : update.childrenWithName("module")) {
			if (module.equals(xmodules.get("id"))) {
				xmodules.set("version", version);
				
				for (XElement xfile : xmodules.childrenWithName("file")) {
					String url = xfile.get("url");
					if (url.endsWith(".jar")) {
						int idx = url.lastIndexOf('/');
						String prefix = url.substring(0, idx + 1);
						xfile.set("url", prefix + name);
						xfile.set("sha1", sha1);
						return;
					}
				}
			}
		}
		throw new AssertionError("Entry not found");
	}
	/**
	 * Update a module's main JAR file.
	 * @param update the XML
	 * @param module the module name
	 * @param namePrefix the file name's prefix
	 * @param checksum the sha1 + filename
	 * @param removeOld place a remove element?
	 */
	static void updateZipFile(XElement update, 
			String module, 
			String namePrefix, 
			String checksum,
			boolean removeOld) {
		String sha1 = checksum;
		String name = sha1.substring(41).trim();
		sha1 = sha1.substring(0, 40).trim();
		
		for (XElement xmodules : update.childrenWithName("module")) {
			if (module.equals(xmodules.get("id"))) {
				
				for (XElement xfile : xmodules.childrenWithName("file")) {
					String url = xfile.get("url");
					if (url.endsWith(".zip")) {
						int idx = url.lastIndexOf('/');
						String prefix = url.substring(0, idx + 1);
						String currentName = url.substring(idx + 1);
						if (currentName.startsWith(namePrefix)) {
							xfile.set("url", prefix + name);
							xfile.set("sha1", sha1);
							
							if (removeOld && !currentName.equals(name)) {
								boolean found = false;
								for (XElement xremove : xmodules.childrenWithName("remove")) {
									if (currentName.equals(xremove.get("file"))) {
										found = true;
										break;
									}
								}
								if (!found) {
									XElement xremove = xmodules.add("remove");
									xremove.set("file", currentName);
								}
							}
							
							return;
						}
					}
				}
			}
		}
		throw new AssertionError("Entry not found");
	}
}
