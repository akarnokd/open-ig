/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
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
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream("open-ig-mapeditor-0.4.jar"), 1024 * 1024));
		try {
			processDirectory(".\\bin\\", ".\\bin", zout);
			
			ZipEntry mf = new ZipEntry("META-INF/MANIFEST.MF");
			File mfm = new File("META-INF/MANIFEST.MF.mapeditor");
			mf.setSize(mfm.length());
			mf.setTime(mfm.lastModified());
			zout.putNextEntry(mf);
			zout.write(IOUtils.load(mfm));
		} finally {
			zout.close();
		}
		zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream("open-ig-mapeditor-0.4-src.zip"), 1024 * 1024));
		try {
			processDirectory(".\\src\\", ".\\src", zout);
			
			ZipEntry mf = new ZipEntry("META-INF/MANIFEST.MF");
			File mfm = new File("META-INF/MANIFEST.MF.mapeditor");
			mf.setSize(mfm.length());
			mf.setTime(mfm.lastModified());
			zout.putNextEntry(mf);
			zout.write(IOUtils.load(mfm));
		} finally {
			zout.close();
		}
		zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream("open-ig-upgrade-20100917a.zip"), 16 * 1024 * 1024));
		try {
			processDirectory(".\\data\\", ".\\data", zout);
			processDirectory(".\\images\\", ".\\images", zout);
		} finally {
			zout.close();
		}
	}
	/**
	 * Process the contents of the given directory.
	 * @param baseDir the base directory
	 * @param currentDir the current directory
	 * @param zout the output stream
	 * @throws IOException on error
	 */
	static void processDirectory(String baseDir, String currentDir, ZipOutputStream zout) throws IOException {
		File[] files = new File(currentDir).listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return !pathname.isHidden();
			}
		});
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
//					String fpath = f.getPath();
//					String fpath2 = fpath.substring(baseDir.length());
//					ZipEntry ze = new ZipEntry(fpath2 + "\\");
//					zout.putNextEntry(ze);
					processDirectory(baseDir, f.getPath(), zout);
				} else {
					String fpath = f.getPath();
					String fpath2 = fpath.substring(baseDir.length());
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
