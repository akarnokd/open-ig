/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.model.Configuration;
import hu.openig.utils.IOUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import javax.swing.SwingWorker;

/**
 * Utility program to package the executable JAR file and upgrade packs for image+data.
 * @author akarnokd
 */
public final class PackageStuff {
	/** Utility class. */
	private PackageStuff() {
		
	}
	/**
	 * Build the graphics/data patch file.
	 * @param version the version number in the file
	 */
	static void buildPatch(String version) {
		try {
			ZipOutputStream zout = new ZipOutputStream(
					new BufferedOutputStream(
							new FileOutputStream("open-ig-images-" + version + ".zip"), 1024 * 1024));
			try {
				zout.setLevel(9);
				processDirectory(".\\images\\", ".\\images", zout, null);
//				processDirectory(".\\audio\\", ".\\audio", zout, new FilenameFilter() {
//					@Override
//					public boolean accept(File dir, String name) {
//						String d = dir.toString().replace('\\', '/');
//						return d.contains("/ui") 
//								|| d.contains("/groundwar")
//								|| d.contains("/spacewar") || d.contains("generic")
//								;
//					}
//				});
//				addFile("generic/messages/achilles_check.ani.gz", "video/generic/messages/achilles_check.ani.gz", zout);
//				addFile("hu/messages/achilles_check.wav", "audio/hu/messages/achilles_check.wav", zout);
//				addFile("hu/messages/centronom_check.wav", "audio/hu/messages/centronom_check.wav", zout);
//				addFile("en/messages/achilles_check.wav", "audio/en/messages/achilles_check.wav", zout);
			} finally {
				zout.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		try {
			ZipOutputStream zout = new ZipOutputStream(
					new BufferedOutputStream(
							new FileOutputStream("open-ig-upgrade-" + version + "2.zip"), 1024 * 1024));
			try {
				zout.setLevel(9);
				processDirectory(".\\data\\", ".\\data", zout, null);
			} finally {
				zout.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Build the map editor with the target version number.
	 * @param version the target version number
	 */
	static void buildMapEditor(String version) {
		try {
			ZipOutputStream zout = new ZipOutputStream(
					new BufferedOutputStream(
							new FileOutputStream("open-ig-mapeditor-" + version + ".jar"), 1024 * 1024));
			try {
				zout.setLevel(9);
				processDirectory(".\\bin\\", ".\\bin", zout, null);
				addFile("META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF.mapeditor", zout);
			} finally {
				zout.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Build the game's runnable jar.
	 * @param version the target version
	 */
	static void buildGame(String version) {
		try {
			ZipOutputStream zout = new ZipOutputStream(
					new BufferedOutputStream(
							new FileOutputStream("open-ig-" + version + ".jar"), 1024 * 1024));
			try {
				zout.setLevel(9);
				processDirectory(".\\bin\\", ".\\bin", zout, new FilenameFilter() {
					
					@Override
					public boolean accept(File dir, String name) {
						name = name.toLowerCase();
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
			} finally {
				zout.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Build the launcher file.
	 */
	static void buildLauncher() {
		try {
			ZipOutputStream zout = new ZipOutputStream(
					new BufferedOutputStream(
							new FileOutputStream("open-ig-launcher.jar"), 1024 * 1024));
			try {
				zout.setLevel(9);
				processDirectory(".\\bin\\", ".\\bin", zout, new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						String d = dir.toString().replace('\\', '/');
						if (!d.endsWith("/")) {
							d += "/";
						}
						d += name;
						return d.contains("hu/openig/launcher") 
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
								|| d.contains("hu/openig/gfx/loading.gif")
								|| d.contains("hu/openig/ui/IGButton")
								|| d.contains("hu/openig/render/RenderTools")
								|| d.contains("hu/openig/render/GenericMediumButton")
								|| d.contains("hu/openig/render/GenericButtonRenderer");
					}
				});
				addFile("META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF.launcher", zout);
			} finally {
				zout.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Build the testbed application.
	 * @param version the version
	 */
	static void buildTestbed(String version) {
		try {
			ZipOutputStream zout = new ZipOutputStream(
					new BufferedOutputStream(
							new FileOutputStream("open-ig-testbed-" + version + ".jar"), 1024 * 1024));
			try {
				zout.setLevel(9);
				processDirectory(".\\bin\\", ".\\bin", zout, null);
				addFile("META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF.testbed", zout);
			} finally {
				zout.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
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
					processDirectory(baseDir, f.getPath(), zout, filter);
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
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		
		JFrame f = new JFrame("Build");
		
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		f.getContentPane().add(p);
		
		final JCheckBox cb1 = new JCheckBox("Build game", true);
		final JCheckBox cb2 = new JCheckBox("Build launcher", false);
		final JCheckBox cb3 = new JCheckBox("Build patch", false);
		
		final JButton run = new JButton("Run");
		
		run.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				run.setEnabled(false);
				final boolean v1 = cb1.isSelected();
				final boolean v2 = cb2.isSelected();
				final boolean v3 = cb3.isSelected();
				SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() throws Exception {
						final ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
						if (v1) {
							exec.execute(new Runnable() {
								@Override
								public void run() {
									buildGame(Configuration.VERSION);
								}
							});
						}
						if (v2) {
							exec.execute(new Runnable() {
								@Override
								public void run() {
									buildLauncher();
								}
							});
						}
						if (v3) {
							exec.execute(new Runnable() {
								@Override
								public void run() {
									SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
									buildPatch(sdf.format(new Date()) + "a");
								}
							});
						} 

						exec.shutdown();
						exec.awaitTermination(1, TimeUnit.DAYS);
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
		p.add(run);
		f.pack();
		f.setResizable(false);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}
}
