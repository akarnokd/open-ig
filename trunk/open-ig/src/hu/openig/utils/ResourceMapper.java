/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.utils;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Scans the current directory and its subdirectories and provides an associative map for files.
 * Used to uniformly access game resources on Windows and *nix systems where case sensitivity could
 * cause problems. Imperium Galactica does not have duplicate filenames in its subdirectories, therefore
 * the mapper only keeps the filename, not the path.
 * @author akarnokd
 */
public class ResourceMapper {
	/** The map of file names to file objects. */
	private Map<String, File> files;
	/** The root directory. */
	private final String root;
	/**
	 * Constructor. Maps the files of the given directory. 
	 * @param root the directory to start the mapping
	 */
	public ResourceMapper(String root) {
		File rf = new File(root);
		this.root = rf.getAbsolutePath().toUpperCase();
		files = new HashMap<String, File>();
		search(rf);
	}
	/**
	 * Search for files within the specified directory and collect them into the files map.
	 * @param dir the directory to search and map
	 */
	private void search(File dir) {
		File[] fileList = dir.listFiles();
		if (fileList != null) {
			for (File f : fileList) {
				if (f.isDirectory()) {
					search(f);
				} else
				if (f.isFile()) {
					String newFile = f.getAbsolutePath().toUpperCase();
					int idx = newFile.indexOf(root);
					if (idx >= 0) {
						newFile = newFile.substring(idx + root.length()).replaceAll("\\\\", "/");
						if (newFile.startsWith("/")) {
							newFile = newFile.substring(1);
						}
						File old = files.put(newFile, f);
						if (old != null) {
							System.err.printf("Duplicate filename: %s (%d) and %s (%d)%n", f.getAbsolutePath(), f.length(), old.getAbsolutePath(), old.length());
						}
					}
				}
			}
		}
	}
	/**
	 * Returns the File object associated with the given general name.
	 * @param filename the filename to get
	 * @return the found file object or null if not present
	 */
	public File get(String filename) {
		return files.get(filename.toUpperCase());
	}
	/**
	 * Returns an unmodifiable set of the available keys in the resource map.
	 * @return the set of available keys
	 */
	public Set<String> keySet() {
		return Collections.unmodifiableSet(files.keySet());
	}
}
