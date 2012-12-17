/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.core.Func1;
import hu.openig.core.Pair;
import hu.openig.utils.Exceptions;
import hu.openig.utils.IOUtils;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
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

import javax.imageio.ImageIO;
import javax.xml.stream.XMLStreamException;

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
		campaignData.directory = new File(workDir, "dlc/" + name);
		File dir = campaignData.directory;
		if (!dir.exists() && !dir.mkdirs()) {
			Exceptions.add(new IOException("Could not create directories for " + dir));
		}
		
		File campaignDir = new File(dir, "generic/campaign/" + name);
		if (!campaignDir.exists() && !campaignDir.mkdirs()) {
			Exceptions.add(new IOException("Could not create directories for " + campaignDir));
		}
		
		//********************************************************************
		
		CopyOperation cop;
		copyImage(copySettings, name, dir);

		//********************************************************************

		copyLabels(copySettings, name, dir);

		//********************************************************************
		
		copyGalaxy(copySettings, name, dir, campaignDir);
		
		//********************************************************************

		cop = copySettings.get(DataFiles.PLAYERS);
		if (cop != CopyOperation.REFERENCE) {
			String fname = "players";
			String original = campaignData.definition.players;
			campaignData.definition.players = "campaign/" + name + "/" + fname;
			
			XElement xml = new XElement(fname);
			if (cop == CopyOperation.COPY) {
				byte[] data = getDataAll(original + ".xml").get("generic");
				try {
					xml = XElement.parseXML(data);
				} catch (XMLStreamException ex) {
					// ignored
				}
			}
			try {
				xml.save(new File(campaignDir, fname + ".xml"));
			} catch (IOException ex) {
				Exceptions.add(ex);
			}
		}
		
		//********************************************************************

		cop = copySettings.get(DataFiles.PLANETS);
		if (cop != CopyOperation.REFERENCE) {
			String fname = "planets";
			String original = campaignData.definition.planets;
			campaignData.definition.planets = "campaign/" + name + "/" + fname;
			
			XElement xml = new XElement(fname);
			if (cop == CopyOperation.COPY) {
				byte[] data = getDataAll(original + ".xml").get("generic");
				try {
					xml = XElement.parseXML(data);
				} catch (XMLStreamException ex) {
					// ignored
				}
			}
			try {
				xml.save(new File(campaignDir, fname + ".xml"));
			} catch (IOException ex) {
				Exceptions.add(ex);
			}
		}

		//********************************************************************

		cop = copySettings.get(DataFiles.TECHNOLOGY);
		if (cop != CopyOperation.REFERENCE) {
			String fname = "tech";
			String original = campaignData.definition.tech;
			campaignData.definition.tech = "campaign/" + name + "/" + fname;
			
			XElement xml = new XElement(fname);
			if (cop == CopyOperation.COPY) {
				byte[] data = getDataAll(original + ".xml").get("generic");
				try {
					xml = XElement.parseXML(data);
				} catch (XMLStreamException ex) {
					// ignored
				}
			}
			try {
				xml.save(new File(campaignDir, fname + ".xml"));
			} catch (IOException ex) {
				Exceptions.add(ex);
			}
		}

		//********************************************************************

		cop = copySettings.get(DataFiles.BUILDINGS);
		if (cop != CopyOperation.REFERENCE) {
			String fname = "buildings";
			String original = campaignData.definition.buildings;
			campaignData.definition.buildings = "campaign/" + name + "/" + fname;
			
			XElement xml = new XElement(fname);
			if (cop == CopyOperation.COPY) {
				byte[] data = getDataAll(original + ".xml").get("generic");
				try {
					xml = XElement.parseXML(data);
				} catch (XMLStreamException ex) {
					// ignored
				}
			}
			try {
				xml.save(new File(campaignDir, fname + ".xml"));
			} catch (IOException ex) {
				Exceptions.add(ex);
			}
		}

		//********************************************************************

		cop = copySettings.get(DataFiles.BATTLE);
		if (cop != CopyOperation.REFERENCE) {
			String fname = "battle";
			String original = campaignData.definition.battle;
			campaignData.definition.battle = "campaign/" + name + "/" + fname;
			
			XElement xml = new XElement(fname);
			if (cop == CopyOperation.COPY) {
				byte[] data = getDataAll(original + ".xml").get("generic");
				try {
					xml = XElement.parseXML(data);
				} catch (XMLStreamException ex) {
					// ignored
				}
			}
			try {
				xml.save(new File(campaignDir, fname + ".xml"));
			} catch (IOException ex) {
				Exceptions.add(ex);
			}
		}

		//********************************************************************

		cop = copySettings.get(DataFiles.DIPLOMACY);
		if (cop != CopyOperation.REFERENCE) {
			String fname = "diplomacy";
			String original = campaignData.definition.diplomacy;
			campaignData.definition.diplomacy = "campaign/" + name + "/" + fname;
			
			XElement xml = new XElement(fname);
			if (cop == CopyOperation.COPY) {
				byte[] data = getDataAll(original + ".xml").get("generic");
				try {
					xml = XElement.parseXML(data);
				} catch (XMLStreamException ex) {
					// ignored
				}
			}
			try {
				xml.save(new File(campaignDir, fname + ".xml"));
			} catch (IOException ex) {
				Exceptions.add(ex);
			}
		}

		//********************************************************************

		cop = copySettings.get(DataFiles.BRIDGE);
		if (cop != CopyOperation.REFERENCE) {
			String fname = "bridge";
			String original = campaignData.definition.bridge;
			campaignData.definition.bridge = "campaign/" + name + "/" + fname;
			
			XElement xml = new XElement(fname);
			if (cop == CopyOperation.COPY) {
				byte[] data = getDataAll(original + ".xml").get("generic");
				try {
					xml = XElement.parseXML(data);
				} catch (XMLStreamException ex) {
					// ignored
				}
			}
			try {
				xml.save(new File(campaignDir, fname + ".xml"));
			} catch (IOException ex) {
				Exceptions.add(ex);
			}
		}

		//********************************************************************

		cop = copySettings.get(DataFiles.TALKS);
		if (cop != CopyOperation.REFERENCE) {
			String fname = "talks";
			String original = campaignData.definition.talks;
			campaignData.definition.talks = "campaign/" + name + "/" + fname;
			
			XElement xml = new XElement(fname);
			if (cop == CopyOperation.COPY) {
				byte[] data = getDataAll(original + ".xml").get("generic");
				try {
					xml = XElement.parseXML(data);
				} catch (XMLStreamException ex) {
					// ignored
				}
			}
			try {
				xml.save(new File(campaignDir, fname + ".xml"));
			} catch (IOException ex) {
				Exceptions.add(ex);
			}
		}

		
		//********************************************************************

		cop = copySettings.get(DataFiles.WALKS);
		if (cop != CopyOperation.REFERENCE) {
			String fname = "walks";
			String original = campaignData.definition.walks;
			campaignData.definition.walks = "campaign/" + name + "/" + fname;
			
			XElement xml = new XElement(fname);
			if (cop == CopyOperation.COPY) {
				byte[] data = getDataAll(original + ".xml").get("generic");
				try {
					xml = XElement.parseXML(data);
				} catch (XMLStreamException ex) {
					// ignored
				}
			}
			try {
				xml.save(new File(campaignDir, fname + ".xml"));
			} catch (IOException ex) {
				Exceptions.add(ex);
			}
		}
		
		//********************************************************************

		cop = copySettings.get(DataFiles.CHATS);
		if (cop != CopyOperation.REFERENCE) {
			String fname = "chats";
			String original = campaignData.definition.chats;
			campaignData.definition.chats = "campaign/" + name + "/" + fname;
			
			XElement xml = new XElement(fname);
			if (cop == CopyOperation.COPY) {
				byte[] data = getDataAll(original + ".xml").get("generic");
				if (data != null) {
					try {
						xml = XElement.parseXML(data);
					} catch (XMLStreamException ex) {
						// ignored
					}
				}
			}
			try {
				xml.save(new File(campaignDir, fname + ".xml"));
			} catch (IOException ex) {
				Exceptions.add(ex);
			}
		}

		//********************************************************************

		cop = copySettings.get(DataFiles.TEST);
		if (cop != CopyOperation.REFERENCE) {
			String fname = "test";
			String original = campaignData.definition.test;
			campaignData.definition.test = "campaign/" + name + "/" + fname;
			
			XElement xml = new XElement(fname);
			if (cop == CopyOperation.COPY) {
				byte[] data = getDataAll(original + ".xml").get("generic");
				try {
					xml = XElement.parseXML(data);
				} catch (XMLStreamException ex) {
					// ignored
				}
			}
			try {
				xml.save(new File(campaignDir, fname + ".xml"));
			} catch (IOException ex) {
				Exceptions.add(ex);
			}
		}

		
		//********************************************************************

		cop = copySettings.get(DataFiles.SPIES);
		if (cop != CopyOperation.REFERENCE) {
			String fname = "spies";
			String original = campaignData.definition.spies;
			campaignData.definition.spies = "campaign/" + name + "/" + fname;
			
			XElement xml = new XElement(fname);
			if (cop == CopyOperation.COPY) {
				byte[] data = getDataAll(original + ".xml").get("generic");
				if (data != null) {
					try {
						xml = XElement.parseXML(data);
					} catch (XMLStreamException ex) {
						// ignored
					}
				}
			}
			try {
				xml.save(new File(campaignDir, fname + ".xml"));
			} catch (IOException ex) {
				Exceptions.add(ex);
			}
		}

		//********************************************************************

		cop = copySettings.get(DataFiles.SCRIPTING);
		if (cop != CopyOperation.REFERENCE) {
			String fname = "scripting";
			String original = campaignData.definition.scripting;
			campaignData.definition.scripting = "campaign/" + name + "/" + fname;
			
			XElement xml = new XElement(fname);
			if (cop == CopyOperation.COPY) {
				byte[] data = getDataAll(original + ".xml").get("generic");
				if (data != null) {
					try {
						xml = XElement.parseXML(data);
					} catch (XMLStreamException ex) {
						// ignored
					}
				}
			}
			try {
				xml.save(new File(campaignDir, fname + ".xml"));
			} catch (IOException ex) {
				Exceptions.add(ex);
			}
		}

		
		//********************************************************************
		
		XElement xdef = new XElement("definition");
		campaignData.definition.save(xdef);
		try {
			xdef.save(new File(campaignDir, "definition.xml"));
		} catch (IOException ex) {
			Exceptions.add(ex);
		}
	}
	/**
	 * Copy the galaxy and surface definitions.
	 * @param copySettings the copy settings
	 * @param name the campaign name
	 * @param dir the base directory of the campaign
	 * @param campaignDir the campaign definition subdirectory
	 */
	public void copyGalaxy(Map<DataFiles, CopyOperation> copySettings,
			String name, File dir, File campaignDir) {
		CopyOperation cop;
		try {
			Map<String, byte[]> galaxies = getDataAll(campaignData.definition.galaxy + ".xml");
			XElement xgalaxy = XElement.parseXML(new ByteArrayInputStream(galaxies.get("generic")));
			
			cop = copySettings.get(DataFiles.SURFACES);
			if (cop != CopyOperation.REFERENCE) {
				File f = new File(dir, "generic/campaign/" + name + "/surfaces");
				if (!f.exists() && !f.mkdirs()) {
					Exceptions.add(new IOException("Could not create directories for " + f));
				}
				
				for (XElement xplanets : xgalaxy.childrenWithName("planets")) {
					for (XElement xplanet : xplanets.childrenWithName("planet")) {
						for (XElement xmap : xplanet.childrenWithName("map")) {
							int start = xmap.getInt("start");
							int end = xmap.getInt("end");
							String pattern = xmap.get("pattern");
							String pattern2 = pattern;
							int pidx = pattern.lastIndexOf('/');
							if (pidx >= 0) {
								pattern2 = pattern.substring(pidx + 1);
							}

							xmap.set("pattern", "campaign/" + name + "/surfaces/" + pattern2);
							
							if (cop == CopyOperation.BLANK) {
								XElement xp = new XElement("map");
								xp.set("version", "1.0");
								XElement xs = xp.add("surface");
								xs.set("width", 1);
								xs.set("height", 1);
								xp.add("buildings");
	
								for (int i = start; i <= end; i++) {
									File out = new File(f, String.format(pattern2, i) + ".xml");
									try {
										xp.save(out);
									} catch (IOException ex) {
										Exceptions.add(ex);
									}
								}
							} else 
							if (cop == CopyOperation.COPY) {
								for (int i = start; i <= end; i++) {
									
									byte[] xp = getDataAll(String.format(pattern, i) + ".xml").get("generic");
									
									File out = new File(f, String.format(pattern2, i) + ".xml");
									IOUtils.save(out, xp);
								}
							}
						}
					}
				}
			}
			
			cop = copySettings.get(DataFiles.GALAXY);
			if (cop != CopyOperation.REFERENCE) {
				if (cop == CopyOperation.BLANK) {
					xgalaxy.clear();
				}
				try {
					xgalaxy.save(new File(campaignDir, "galaxy.xml"));
				} catch (IOException ex) {
					Exceptions.add(ex);
				}
				campaignData.definition.galaxy = "campaign/" + name + "/galaxy";
			}
		} catch (XMLStreamException ex) {
			// ignored
		}
	}
	/**
	 * Copy the labels of the original campaign.
	 * @param copySettings the copy settings
	 * @param name the new campaign name
	 * @param dir the base directory
	 */
	public void copyLabels(Map<DataFiles, CopyOperation> copySettings,
			String name, File dir) {
		CopyOperation cop;
		cop = copySettings.get(DataFiles.LABELS);
		if (cop == CopyOperation.BLANK) {
			campaignData.definition.labels.clear();
			campaignData.definition.labels.add("campaign/" + name + "/labels");
			try {
				for (String lang : campaignData.definition.languages()) {
					XElement xml = new XElement("labels");
					File fp = new File(dir, lang + "/campaign/" + name);
					if (!fp.exists() && !fp.mkdirs()) {
						Exceptions.add(new IOException("Could not create directories for " + fp));
					}
					xml.save(new File(fp, "labels.xml"));
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
	}
	/**
	 * Copy the campaign image of the original campaign.
	 * @param copySettings the copy settings
	 * @param name the new campaign name
	 * @param dir the base directory
	 */
	public void copyImage(Map<DataFiles, CopyOperation> copySettings,
			String name, File dir) {
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
	}
	/**
	 * Load the data files based on the definition.
	 */
	public void load() {
		campaignData.def = load("campaign/" + campaignData.definition.name + "/definition");
		
		campaignData.definition.parse(campaignData.def);
		
		campaignData.labels = U.newHashMap();
		campaignData.labelMap = U.newHashMap();
		
		for (String lang : languages()) {
			campaignData.labelMap.put(lang, U.<String, String>newHashMap());
		}
		List<String> labelRefs = U.newArrayList(campaignData.definition.labels);
		labelRefs.add("labels");
		
		for (String lr : labelRefs) {
			Map<String, byte[]> datas = getDataAll(lr + ".xml");
			for (String lang : campaignData.definition.languages()) {
				byte[] data = datas.get(lang);
				XElement xml = new XElement("labels");
				if (data != null) {
					try {
						xml = XElement.parseXML(data);
					} catch (XMLStreamException ex) {
						// ignored
					}
				}
				campaignData.labels.put(Pair.of(lang, lr), xml);
				for (XElement xe : xml.childrenWithName("entry")) {
					String key = xe.get("key", "");
					Map<String, String> map = campaignData.labelMap.get(key);
					if (map == null) {
						map = U.newHashMap();
						campaignData.labelMap.put(key, map);
					}
					map.put(lang, xe.content);
				}
			}
		}
		
		campaignData.galaxy = load(campaignData.definition.galaxy);
		campaignData.players = load(campaignData.definition.players);
		campaignData.planets = load(campaignData.definition.planets);
		campaignData.technology = load(campaignData.definition.tech);
		campaignData.buildings = load(campaignData.definition.buildings);
		campaignData.battle = load(campaignData.definition.battle);
		campaignData.diplomacy = load(campaignData.definition.diplomacy);
		campaignData.bridge = load(campaignData.definition.bridge);
		campaignData.talks = load(campaignData.definition.talks);
		campaignData.walks = load(campaignData.definition.walks);
		campaignData.chats = load(campaignData.definition.chats);
		campaignData.test = load(campaignData.definition.test);
		campaignData.spies = load(campaignData.definition.spies);
		campaignData.scripting = load(campaignData.definition.scripting);
	}
	/**
	 * Load an XML reference.
	 * @param reference the reference path
	 * @return the XElement
	 */
	XElement load(String reference) {
		try {
			byte[] data = getDataAll(reference + ".xml").get("generic");
			if (data != null) {
				return XElement.parseXML(data);
			}
		} catch (XMLStreamException ex) {
			// ignored
		}
		int idx = reference.lastIndexOf('/');
		String key = reference;
		if (idx >= 0) {
			key = key.substring(idx + 1);
		}
		return new XElement(key);
	}
	/**
	 * Save the data files based on the definition.
	 */
	public void save() {
		// TODO implement
	}
	/**
	 * Returns the image represented by the resource or null if not found.
	 * @param resource the resource name with extension
	 * @return the image or null
	 */
	public BufferedImage getImage(String resource) {
		byte[] data = getData(resource);
		if (data != null) {
			try {
				return ImageIO.read(new ByteArrayInputStream(data));
			} catch (IOException ex) {
				// ignored;
			}
		}
		return null;
	}
	/**
	 * Returns the image represented by the resource or null if not found.
	 * @param language the target language
	 * @param resource the resource name with extension
	 * @return the image or null
	 */
	public BufferedImage getImage(String language, String resource) {
		byte[] data = getData(language, resource);
		if (data != null) {
			try {
				return ImageIO.read(new ByteArrayInputStream(data));
			} catch (IOException ex) {
				// ignored;
			}
		}
		return null;
	}
	/**
	 * Returns a data with the given resource name.
	 * @param resource the resource name with extension
	 * @return the data or null if missing
	 */
	public byte[] getData(String resource) {
		return getData(campaignData.projectLanguage, resource);
	}
	/**
	 * Returns a data with the given resource name.
	 * @param language the target language
	 * @param resource the resource name with extension
	 * @return the data or null if missing
	 */
	public byte[] getData(String language, String resource) {
		Map<String, byte[]> datas = getDataAll(resource);
		byte[] data = datas.get(language);
		if (data == null) {
			data = datas.get("generic");
		}
		return data;
	}
	/**
	 * Returns an XML with the given resource name.
	 * @param resource the resource name with extension
	 * @return the data or null if missing
	 */
	public XElement getXML(String resource) {
		byte[] data = getData(resource);
		if (data != null) {
			try {
				return XElement.parseXML(data);
			} catch (XMLStreamException ex) {
				// ignored;
			}
		}
		return null;
	}
	/**
	 * Returns a campaign label.
	 * @param key the key
	 * @return the label or null if not found
	 */
	public String label(String key) {
		Map<String, String> perLang = campaignData.labelMap.get(key);
		if (perLang != null) {
			return perLang.get(campaignData.projectLanguage);
		}
		return null;
	}
	/**
	 * Sets the label for the current project language entry.
	 * @param key the key
	 * @param value the value
	 */
	public void setLabel(String key, String value) {
		Map<String, String> perLang = campaignData.labelMap.get(key);
		if (perLang == null) {
			perLang = U.newHashMap();
			campaignData.labelMap.put(key, perLang);
		}
		perLang.put(campaignData.projectLanguage, value);
		
		
	}
	/**
	 * Test if a label exists in the project language.
	 * @param key the key
	 * @return true if the label
	 */
	public boolean hasLabel(String key) {
		Map<String, String> perLang = campaignData.labelMap.get(key);
		if (perLang != null) {
			return perLang.containsKey(campaignData.projectLanguage);
		}
		return false;
	}
	/**
	 * @return the ID of the main player.
	 */
	public String mainPlayer() {
		for (XElement xplayer : campaignData.players.childrenWithName("player")) {
			if ("true".equals(xplayer.get("user", ""))) {
				return xplayer.get("id", "");
			}
		}
		return null;
	}
	/** @return the race of the main player. */
	public String mainPlayerRace() {
		for (XElement xplayer : campaignData.players.childrenWithName("player")) {
			if ("true".equals(xplayer.get("user", ""))) {
				return xplayer.get("race", "");
			}
		}
		return null;
	}
	/**
	 * Check if the given reference exists.
	 * @param reference the reference with extension
	 * @return true
	 */
	public boolean exists(String reference) {
		Map<String, byte[]> data = getDataAll(reference);
		return data.containsKey(campaignData.projectLanguage) || data.containsKey("generic");
	}
	/** @return the list of supported languages of the campaign. */
	public List<String> languages() {
		return U.newArrayList(campaignData.definition.languages());
	}
	/** @return the definition directory. */
	public File getDefinitionDirectory() {
		return new File(campaignData.directory, "generic/campaign/" + campaignData.definition.name);
	}
}
