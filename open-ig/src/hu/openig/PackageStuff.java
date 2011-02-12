/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig;

import hu.openig.core.Configuration;
import hu.openig.editors.MapEditor;
import hu.openig.test.ScreenTester;
import hu.openig.utils.IOUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility program to package the executable JAR file and upgrade packs for image+data.
 * @author karnokd
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
							new FileOutputStream("open-ig-upgrade-" + version + ".zip"), 1024 * 1024));
			try {
				zout.setLevel(9);
				processDirectory(".\\data\\", ".\\data", zout, null);
				processDirectory(".\\images\\", ".\\images", zout, null);
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
				processDirectory(".\\bin\\", ".\\bin", zout, null);
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
						return d.endsWith("hu/openig/launcher") || d.endsWith("hu/openig/utils");
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
		mf.setSize(mfm.length());
		mf.setTime(mfm.lastModified());
		zout.putNextEntry(mf);
		zout.write(IOUtils.load(mfm));
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
		buildGame(Configuration.VERSION);
		buildLauncher();
		buildMapEditor(MapEditor.MAP_EDITOR_JAR_VERSION);
		buildPatch("20100917a");
		buildTestbed(ScreenTester.VERSION);
	}
}
