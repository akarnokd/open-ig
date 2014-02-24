/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.core.Func1;
import hu.openig.utils.Exceptions;
import hu.openig.utils.IOUtils;
import hu.openig.utils.U;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Keeps track of the game's resources and provides convenience methods to load and
 * save them.
 * @author akarnokd, 2012.12.27.
 */
public class CEResourceManager {
	/** The working directory. */
	protected final File workdir;
	/** A resource name (slashed and with extension) to a language to a parent container. */
	protected final Map<String, Map<String, File>> map = new HashMap<>();
	/** The default directories. */
	static final String[] DEFAULT_DIRECTORIES = { "data", "audio", "images", "video" };
	/**
	 * Check if the File object represents a zip file.
	 */
	public static final Func1<File, Boolean> IS_ZIP = new Func1<File, Boolean>() {
		@Override
		public Boolean invoke(File value) {
			return value.isFile() && value.getName().toLowerCase(Locale.ENGLISH).endsWith(".zip");
		}
	};
	/**
	 * Check if the File object represents a zip file.
	 */
	public static final Func1<File, Boolean> IS_DIR = new Func1<File, Boolean>() {
		@Override
		public Boolean invoke(File value) {
			return value.isDirectory();
		}
	};
	/** The default save directory for new objects. */
	public File saveDir;
	/**
	 * Constructor. Sets the game's working directory.
	 * @param workdir the working directory.
	 */
	public CEResourceManager(File workdir) {
		this.workdir = workdir;
	}
	/**
	 * Scan the files for resources.
	 */
	public void scan() {
		// scan regular zips in the work directory
		List<File> upgrades = new ArrayList<>();
		for (File f : U.listFiles(workdir, IS_ZIP)) {
			if (!f.getName().startsWith("open-ig-upgrade")) {
				processZip(f);
			} else {
				upgrades.add(f);
			}
		}
		// process upgrade zips in filename order.
		Collections.sort(upgrades, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
		for (File f : upgrades) {
			processZip(f);
		}
		// process DLC zips
		for (File f : U.listFiles(new File(workdir, "dlc"), IS_ZIP)) {
			processZip(f);
		}
		// process the default directories
		for (String df : DEFAULT_DIRECTORIES) {
			processDir(new File(workdir, df));
		}
		// process DLC directories
		for (File f : U.listFiles(new File(workdir, "dlc"), IS_DIR)) {
			processDir(f);
		}
	}
	/**
	 * Add a new resource, language and container.
	 * @param resource the resource path with slashes and extension
	 * @param language the language
	 * @param container the container
	 */
	protected void add(String resource, String language, File container) {
		Map<String, File> perLang = map.get(resource);
		if (perLang == null) {
			perLang = new HashMap<>();
			map.put(resource, perLang);
		}
		perLang.put(language, container);
	}
	/**
	 * Process a zip file contents where the first level directory is the language.
	 * @param f the container file
	 */
	protected void processZip(File f) {
		try {
			try (ZipFile zf = new ZipFile(f)) {
				for (ZipEntry ze : U.enumerate(zf.entries())) {
					if (!ze.isDirectory()) {
						String n = ze.getName().replace('\\', '/');
						int idx = n.indexOf('/');
						if (idx > 0) {
							String lang = n.substring(0, idx);
							String res = n.substring(idx + 1);
							add(res, lang, f);
						}
					}
				}
			}
		} catch (IOException ex) {
			// ignored
		}
	}
	/**
	 * Process the contents of the directory. The first level under this directory represent
	 * the languages.
	 * @param dir the directory
	 */
	protected void processDir(File dir) {
		for (File langFile : U.listFiles(dir, IS_DIR)) {
			String lang = langFile.getName().toLowerCase();
			processSubdir(lang, langFile, langFile);
		}
	}
	/**
	 * Process the contents of the subdirectory.
	 * @param language the target language
	 * @param base the base directory
	 * @param parent the current parent directory
	 */
	protected void processSubdir(String language, File base, File parent) {
		for (File f : U.listFiles(parent)) {
			if (f.isFile()) {
				LinkedList<String> pathElements = new LinkedList<>();
				pathElements.add(f.getName());
				File pf = f.getParentFile();
				while (pf != null && !pf.equals(base)) {
					pathElements.addFirst(pf.getName());
					pf = pf.getParentFile();
				}
				String sp = U.join(pathElements, "/");
				add(sp, language, base.getParentFile());
			}
			if (f.isDirectory()) {
				processSubdir(language, base, f);
			}
		}
	}
	/**
	 * Check if the given resource (path and extension) exists.
	 * @param resource the resource
	 * @return true if the resource exists in any language
	 */
	public boolean exists(String resource) {
		return map.containsKey(resource);
	}
	/**
	 * Check if the given resource (path and extension) exists in the given 
	 * language (exact).
	 * @param resource the resource path
	 * @param language the language
	 * @return true if the resource exits
	 */
	public boolean exists(String resource, String language) {
		Map<String, File> res = map.get(resource);
        return res != null && res.containsKey(language);
    }
	/**
	 * @return The set of known resources. 
	 */
	public Set<String> resources() {
		return map.keySet();
	}
	/**
	 * Get a data resource (exact).
	 * @param resource the resource path with extension
	 * @param language the exact language
	 * @return the data or null if the resource does not exist
	 */
	protected byte[] getData(String resource, String language) {
		byte[] data = null;
		Map<String, File> res = map.get(resource);
		if (res != null) {
			File f = res.get(language);
			if (f != null) {
				if (f.isFile() && f.getName().toLowerCase().endsWith(".zip")) {
					try {
						try (ZipFile zf = new ZipFile(f)) {
							ZipEntry ze = zf.getEntry(language + "/" + resource);
							if (ze != null) {
								data = IOUtils.load(zf.getInputStream(ze));
							}
						}
					} catch (IOException ex) {
						// ignored
					}
				} else 
				if (f.isDirectory()) {
					data = IOUtils.load(new File(f, language + "/" + resource));
				}
			}
		}
		return data;
	}
	/**
	 * Loads the data from all available languages.
	 * @param resource the resource (path and extension)
	 * @return the map from language to data
	 */
	public Map<String, byte[]> getData(String resource) {
		Map<String, File> res = map.get(resource);
		Map<String, byte[]> result = new HashMap<>();
		if (res != null) {
			for (String s : res.keySet()) {
				result.put(s, getData(resource, s));
			}
		}
		return result;
	}
	/**
	 * Check if the given resource is in plan, uncompressed container that can be modified.
	 * @param resource the resource (path and extension)
	 * @param language the target language
	 * @return true if the file can be changed
	 */
	public boolean canEdit(String resource, String language) {
		Map<String, File> res = map.get(resource);
		if (res != null) {
			File f = res.get(language);
			if (f != null) {
				return f.isDirectory();
			}
		}
		return false;
	}
	/** @return the working directory. */
	public File workdir() {
		return workdir;
	}
	/**
	 * Retrieves the container file of the given resource.
	 * @param resource the resource.
	 * @param language the language
	 * @return the container file
	 */
	public File getContainer(String resource, String language) {
		Map<String, File> res = map.get(resource);
		if (res != null) {
			return res.get(language);
		}
		return null;
	}
	/**
	 * Test routine.
	 * @param args the arguments.
	 */
	public static void main(String[] args) {
		CEResourceManager mgr = new CEResourceManager(new File("."));
		mgr.scan();
		System.out.println(mgr.map);
	}
	/**
	 * Update a data resource.
	 * @param resource the resource path and extension
	 * @param language the language
	 * @param data the data bytes
	 */
	public void saveData(String resource, String language, byte[] data) {
		if (saveDir == null) {
			Exceptions.add(new AssertionError("No save directory present."));
			return;
		}
		File f = getContainer(resource, language);
		if (f != null && f.isDirectory()) {
			IOUtils.save(new File(f, language + "/" + resource), data);
		} else
		if (f == null) {
			IOUtils.save(new File(saveDir, language + "/" + resource), data);
		}
	}
}
