/*
 * Copyright 2008-2012, David Karnok 
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
import hu.openig.utils.XElement;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author akarnokd, 2012.12.13.
 *
 */
public class CEDataManager {
	/**
	 * A simple directory filter.
	 * @author akarnokd, 2012.11.01.
	 */
	private final class DirFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}
	}
	/** The default directories. */
	String[] defaultDirectories = { "data", "audio", "images", "video" };
	/** The working directory. */
	public File workDir = new File(".");
	/** The languages. */
	public List<String> languages = U.newArrayList();
	/** The master campaign data. */
	public CampaignData campaignData;
	/**
	 * Check if the File object represents a zip file.
	 */
	public static final Func1<File, Boolean> IS_ZIP = new Func1<File, Boolean>() {
		@Override
		public Boolean invoke(File value) {
			return value.isFile() && value.getName().toLowerCase().endsWith(".zip");
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
	/** Initialize the data manager. */
	public void init() {
		
	}
	/** @return scan for all languages. */
	public List<String> getLanguages() {
		Set<String> result = U.newHashSet();
		
		File dlc = new File(workDir, "dlc");
		
		List<File> dirs = U.newArrayList();
		List<File> zips = U.newArrayList();
		// check unpacked dlcs
		File[] dlcDirs = dlc.listFiles(new DirFilter());
		if (dlcDirs != null) {
			dirs.addAll(Arrays.asList(dlcDirs));
		}
		for (String s : defaultDirectories) {
			dirs.add(new File(workDir, s));
		}

		File[] dlcZips = dlc.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".zip");
			}
		});
		if (dlcZips != null) {
			zips.addAll(Arrays.asList(dlcZips));
		}

		File[] normalZips = dlc.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String n = pathname.getName().toLowerCase();
				return pathname.isFile() && n.startsWith("open-ig-") && n.endsWith(".zip");
			}
		});
		if (normalZips != null) {
			zips.addAll(Arrays.asList(normalZips));
		}
		
		for (File f : dirs) {
			File[] fs = f.listFiles(new DirFilter());
			if (fs != null) {
				for (File fss : fs) {
					if (!fss.getName().equals("generic")) {
						result.add(fss.getName());
					}
				}
			}
		}
		
		for (File f : zips) {
			try {
				ZipFile zf = new ZipFile(f);
				try {
					Enumeration<? extends ZipEntry> zes = zf.entries();
					while (zes.hasMoreElements()) {
						ZipEntry ze = zes.nextElement();
						
						String name = ze.getName();
						int idx = name.indexOf('/');
						if (idx >= 0) {
							name = name.substring(0, idx);
						}
						result.add(name);
					}
				} finally {
					zf.close();
				}
				
			} catch (IOException ex) {
				// ignored
			}
		}
		
		return U.newArrayList(result);
	}
	/**
	 * Check if the given resource exists under the language or generic.
	 * @param language the language
	 * @param resource the resource with extension
	 * @return true if exists
	 */
	public boolean exists(String language, String resource) {
		File dlc = new File(workDir, "dlc");
		
		// check unpacked dlcs
		File[] dlcDirs = dlc.listFiles(new DirFilter());
		if (dlcDirs != null) {
			boolean result = scanDirsExist(language, resource, dlcDirs);
			if (result) {
				return true;
			}
		}
		// check dlc zips
		File[] dlcZips = dlc.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".zip");
			}
		});
		if (dlcZips != null) {
			boolean result = scanZipsExist(language, resource, dlcZips);
			if (result) {
				return result;
			}
		}
		// check master directories
		File[] normalDirs = new File[defaultDirectories.length];
		for (int i = 0; i < normalDirs.length; i++) {
			normalDirs[i] = new File(workDir, defaultDirectories[i]);
		}
		
		boolean result = scanDirsExist(language, resource, normalDirs);
		if (result) {
			return result;
		}
		
		// check dlc zips
		File[] upgradeZips = dlc.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String n = pathname.getName().toLowerCase();
				return pathname.isFile() && n.startsWith("open-ig-upgrade") && n.endsWith(".zip");
			}
		});
		if (upgradeZips != null) {
			Arrays.sort(upgradeZips, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					return o2.getName().toLowerCase().compareTo(o1.getName().toLowerCase());
				}
			});
			result = scanZipsExist(language, resource, upgradeZips);
			if (result) {
				return result;
			}
		}

		// check dlc zips
		File[] normalZips = dlc.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String n = pathname.getName().toLowerCase();
				return pathname.isFile() && n.startsWith("open-ig-") && !n.startsWith("open-ig-upgrade") && n.endsWith(".zip");
			}
		});
		if (normalZips != null) {
			result = scanZipsExist(language, resource, normalZips);
			if (result) {
				return result;
			}
		}

		return false;
	}
	/**
	 * Retrieve all language resources.
	 * @param resource the resource path with extension
	 * @return the map from language code to data bytes that actually exist
	 */
	public Map<String, byte[]> getDataAll(String resource) {
		Map<String, byte[]> result = U.newHashMap();
		
		List<File> files = U.newArrayList();
		
		// dlc subdirectories
		files.addAll(U.listFiles(new File(workDir, "dlc")));
		// main directories
		for (String s : defaultDirectories) {
			files.add(new File(workDir, s));
		}
		// dlc zips
		files.addAll(U.listFiles(new File(workDir, "dlc"), IS_ZIP));

		// main zips
		List<File> zips = U.listFiles(new File(workDir, "dlc"), new Func1<File, Boolean>() {
			@Override
			public Boolean invoke(File value) {
				String name = value.getName().toLowerCase();
				return name.startsWith("open-ig-") && name.endsWith(".zip");
			}
		});
		Collections.sort(zips, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
		// keep a single upgrade only
		int u = 0;
		for (int i = zips.size() - 1; i >= 0; i--) {
			File f = zips.get(i);
			if (f.getName().startsWith("open-ig-upgrade")) {
				u++;
				if (u > 1) {
					zips.remove(i);
				}
			}
		}
		
		files.addAll(zips);

		for (File f : files) {
			if (IS_ZIP.invoke(f)) {
				try {
					ZipFile zf = new ZipFile(f);
					try {
						for (ZipEntry ze : U.enumerate(zf.entries())) {
							String langStr = ze.getName();
							int idx = langStr.indexOf('/');
							if (idx > 0) {
								String fileStr = langStr.substring(idx + 1);
								langStr = langStr.substring(0, idx);
								if (fileStr.equals(resource)) {
									if (!result.containsKey(langStr)) {
										result.put(langStr, IOUtils.load(zf.getInputStream(ze)));
									}									
								}
							}
						}
					} finally {
						zf.close();
					}
				} catch (IOException ex) {
					// ignored
				}
			} else {
				for (File langs : U.listFiles(f, IS_DIR)) {
					String langStr = langs.getName();
					File f2 = new File(f, langStr + "/" + resource);
					if (f2.canRead()) {
						if (!result.containsKey(langStr)) {
							result.put(langStr, IOUtils.load(f2));
						}
					}
				}
			}
		}
		
		return result;
	}
	/**
	 * Get a resource for the specified language (or generic).
	 * @param language the language
	 * @param resource the resource name with extension
	 * @return the data bytes or null if not found
	 */
	public byte[] getData(String language, String resource) {
		File dlc = new File(workDir, "dlc");
		
		// check unpacked dlcs
		File[] dlcDirs = dlc.listFiles(new DirFilter());
		if (dlcDirs != null) {
			byte[] result = scanDirs(language, resource, dlcDirs);
			if (result != null) {
				return result;
			}
		}
		// check dlc zips
		File[] dlcZips = dlc.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".zip");
			}
		});
		if (dlcZips != null) {
			byte[] result = scanZips(language, resource, dlcZips);
			if (result != null) {
				return result;
			}
		}
		// check master directories
		File[] normalDirs = new File[defaultDirectories.length];
		for (int i = 0; i < normalDirs.length; i++) {
			normalDirs[i] = new File(workDir, defaultDirectories[i]);
		}
		
		byte[] result = scanDirs(language, resource, normalDirs);
		if (result != null) {
			return result;
		}
		
		// check dlc zips
		File[] upgradeZips = dlc.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String n = pathname.getName().toLowerCase();
				return pathname.isFile() && n.startsWith("open-ig-upgrade") && n.endsWith(".zip");
			}
		});
		if (upgradeZips != null) {
			Arrays.sort(upgradeZips, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					return o2.getName().toLowerCase().compareTo(o1.getName().toLowerCase());
				}
			});
			result = scanZips(language, resource, upgradeZips);
			if (result != null) {
				return result;
			}
		}

		// check dlc zips
		File[] normalZips = dlc.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String n = pathname.getName().toLowerCase();
				return pathname.isFile() && n.startsWith("open-ig-") && !n.startsWith("open-ig-upgrade") && n.endsWith(".zip");
			}
		});
		if (normalZips != null) {
			result = scanZips(language, resource, normalZips);
			if (result != null) {
				return result;
			}
		}

		return null;
	}
	/**
	 * Scan a set of zip files for a particular resource.
	 * @param language the language
	 * @param resource the resource
	 * @param dlcZips the zip files
	 * @return the data or null if not found
	 */
	public byte[] scanZips(String language, String resource, File[] dlcZips) {
		for (File f : dlcZips) {
			try {
				ZipFile zf = new ZipFile(f);
				try {
					ZipEntry ze = zf.getEntry(language + "/" + resource);
					if (ze != null) {
						return readEntry(zf, ze);
					}
					ze = zf.getEntry("generic/" + resource);
					if (ze != null) {
						return readEntry(zf, ze);
					}
				} finally {
					zf.close();
				}
			} catch (IOException ex) {
				// ignored
			}
		}
		return null;
	}
	/**
	 * Scan a set of zip files for a particular resource.
	 * @param language the language
	 * @param resource the resource
	 * @param dlcZips the zip files
	 * @return true if exists
	 */
	public boolean scanZipsExist(String language, String resource, File[] dlcZips) {
		for (File f : dlcZips) {
			try {
				ZipFile zf = new ZipFile(f);
				try {
					ZipEntry ze = zf.getEntry(language + "/" + resource);
					if (ze != null) {
						return true;
					}
					ze = zf.getEntry("generic/" + resource);
					if (ze != null) {
						return true;
					}
				} finally {
					zf.close();
				}
			} catch (IOException ex) {
				// ignored
			}
		}
		return false;
	}
	/**
	 * Scan the directories for a specific resource file.
	 * @param language the target language
	 * @param resource the resource path
	 * @param dlcDirs the directories
	 * @return the data or null if not found
	 */
	public byte[] scanDirs(String language, String resource, File[] dlcDirs) {
		for (File f : dlcDirs) {
			File res = new File(f, language + "/" + resource);
			if (res.canRead()) {
				return IOUtils.load(res);
			}
			res = new File(f, "generic/" + resource);
			if (res.canRead()) {
				return IOUtils.load(res);
			}
		}
		return null;
	}
	/**
	 * Scan the directories for a specific resource file.
	 * @param language the target language
	 * @param resource the resource path
	 * @param dlcDirs the directories
	 * @return true if the resource exists
	 */
	public boolean scanDirsExist(String language, String resource, File[] dlcDirs) {
		for (File f : dlcDirs) {
			File res = new File(f, language + "/" + resource);
			if (res.canRead()) {
				return true;
			}
			res = new File(f, "generic/" + resource);
			if (res.canRead()) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Reads a zip entry.
	 * @param zf the zip file
	 * @param ze the zip entry
	 * @return the byte array
	 * @throws IOException on error
	 */
	byte[] readEntry(ZipFile zf, ZipEntry ze) throws IOException {
		InputStream in = zf.getInputStream(ze);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buffer = new byte[8192];
			while (true) {
				int read = in.read(buffer);
				if (read > 0) {
					out.write(buffer, 0, read);
				} else
				if (read < 0) {
					break;
				}
			}
			return out.toByteArray();
		} finally {
			in.close();
		}
	}
	/**
	 * Perform a copy of the current definition based on the supplied settings.
	 * @param copySettings the copy settings
	 */
	public void copy(Map<DataFiles, CopyOperation> copySettings) {
		String name = campaignData.definition.name;
		File dir = new File(workDir, "dlc/" + name);
		if (!dir.exists() && !dir.mkdirs()) {
			Exceptions.add(new IOException("Could not create directories for " + dir));
		}
		
		File campaignDir = new File(dir, "generic/campaign/" + name);
		if (!campaignDir.exists() && !campaignDir.mkdirs()) {
			Exceptions.add(new IOException("Could not create directories for " + campaignDir));
		}
		
		//********************************************************************
		
		CopyOperation cop = copySettings.get(DataFiles.IMAGE);
		if (cop == CopyOperation.BLANK) {
			campaignData.definition.image = null;
			campaignData.definition.imagePath = null;
		} else
		if (cop == CopyOperation.COPY) {
			Map<String, byte[]> allData = getDataAll(campaignData.definition.imagePath + ".png");
			for (Map.Entry<String, byte[]> e : allData.entrySet()) {
				File f = new File(dir, e.getKey() + "/campaign/" + name);
				if (!f.exists() && !f.mkdirs()) {
					Exceptions.add(new IOException("Could not create directories for " + f));
				}
				IOUtils.save(new File(f, "image.png"), e.getValue());
			}
			campaignData.definition.imagePath = "campaign/" + name + "/image"; 
		}

		//********************************************************************

		cop = copySettings.get(DataFiles.LABELS);
		if (cop == CopyOperation.BLANK) {
			campaignData.definition.labels.clear();
			campaignData.definition.labels.add("campaign/" + name + "/labels");
			try {
				for (String lang : campaignData.definition.titles.keySet()) {
					XElement xml = new XElement("labels");
					campaignData.labels.put(lang, xml);
					xml.save(new File(lang + "/campaign/" + name + "/labels.xml"));
				}
			} catch (IOException ex) {
				Exceptions.add(ex);
			}
		} else
		if (cop == CopyOperation.COPY) {
			List<String> newRefs = U.newArrayList();
			int i = 1;
			for (String ref : campaignData.definition.labels) {
				Map<String, byte[]> allData = getDataAll(ref);
				for (Map.Entry<String, byte[]> e : allData.entrySet()) {
					File f = new File(dir, e.getKey() + "/campaign/" + name);
					if (!f.exists() && !f.mkdirs()) {
						Exceptions.add(new IOException("Could not create directories for " + f));
					}
					IOUtils.save(new File(f, "labels_" + i + ".xml"), e.getValue());
				}
				
				newRefs.add("campaign/" + name + "/labels_" + i);
				
				i++;
			}
			campaignData.definition.labels.clear();
			campaignData.definition.labels.addAll(newRefs);
		}

		//********************************************************************
		//********************************************************************
		//********************************************************************
		//********************************************************************
		//********************************************************************
		//********************************************************************
		//********************************************************************
		//********************************************************************
		//********************************************************************
		//********************************************************************
		//********************************************************************
		//********************************************************************
		//********************************************************************
		//********************************************************************
		//********************************************************************
		
		XElement xdef = new XElement("definition");
		campaignData.definition.save(xdef);
		try {
			xdef.save(new File(campaignDir, "definition.xml"));
		} catch (IOException ex) {
			Exceptions.add(ex);
		}
	}
	public void load() {
		
	}
	public void save() {
		
	}
}
