/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;


import hu.openig.utils.XElement;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

/**
 * The main configuration object with rolling log support.
 * @author akarnokd, 2009.09.25.
 */
public class Configuration {
	/** The version string. */
	public static final String VERSION = "0.94.154";
	/** Annotation for indicating load/save a field. */
	@Retention(RetentionPolicy.RUNTIME)
	@interface LoadSave { }
	/** Annotation for indicating load/save a field into a game save. */
	@Retention(RetentionPolicy.RUNTIME)
	@interface LoadSaveGame { }
	/** The configuration is new. */
	public boolean isNew;
	/** The language code. */
	@LoadSave
	public String language = "hu";
	/** The ordered list of containers. */
	@LoadSave
	public final List<String> containers = new ArrayList<String>();
	/** Disable D3D. */
	@LoadSave
	public boolean disableD3D;
	/** Disable DirectDraw. */
	@LoadSave
	public boolean disableDirectDraw;
	/** Disable OpenGL. */
	@LoadSave
	public boolean disableOpenGL;
	/** Top. */
	@LoadSave
	public Integer top;
	/** Left. */
	@LoadSave
	public Integer left;
	/** Width. */
	@LoadSave
	public Integer width;
	/** Height. */
	@LoadSave
	public Integer height;
	/** The filename. */
	@LoadSave
	private String fileName;
	/** The number of audio channels. */
//	@LoadSave
	public int audioChannels = 16;
	/** The music volume 0-100. */
	@LoadSave
	@LoadSaveGame
	public int musicVolume = 100;
	/** Mute music? */
	@LoadSave
	@LoadSaveGame
	public boolean muteMusic;
	/** The effect volume 0-100. */
	@LoadSave
	@LoadSaveGame
	public int effectVolume = 100;
	/** Mute effect. */
	@LoadSave
	@LoadSaveGame
	public boolean muteEffect;
	/** The effect filter step. */
	@LoadSave
	@LoadSaveGame
	public int effectFilter = 1;
	/** Video volume 0-100. */
	@LoadSave
	@LoadSaveGame
	public int videoVolume = 100;
	/** Mute video. */
	@LoadSave
	@LoadSaveGame
	public boolean muteVideo;
	/** The video filter step. */
	@LoadSave
	@LoadSaveGame
	public int videoFilter = 1;
	/** The debug watcher window. */
	public Closeable watcherWindow;
	/** Are we in full-screen mode? */
	@LoadSave
	public boolean fullScreen;
	/** Is the window maximized mode? */
	@LoadSave
	public boolean maximized;
	/** Automatically find resource files instead of fixed set. */
	@LoadSave
	public boolean autoResources = true;
	/** Reequip tanks after battles. */
	@LoadSave
	@LoadSaveGame
	public boolean reequipTanks = true;
	/** Reequip bombs after battles. */
	@LoadSave
	@LoadSaveGame
	public boolean reequipBombs = true;
	/** Computer voice for screen switches. */
	@LoadSave
	@LoadSaveGame
	public boolean computerVoiceScreen = true;
	/** Computer voice for notifications. */
	@LoadSave
	@LoadSaveGame
	public boolean computerVoiceNotify = true;
	/** Build limit. */
	@LoadSave
	@LoadSaveGame
	public int autoBuildLimit = 20000;
	/** Automatic repair. */
	@LoadSave
	@LoadSaveGame
	public boolean autoRepair;
	/** Play button sounds? */
	@LoadSave
	@LoadSaveGame
	public boolean buttonSounds = true;
	/** Play satellite deploy video? */
	@LoadSave
	@LoadSaveGame
	public boolean satelliteDeploy = true;
	/** The research money percent in 10s of percentages. */
	@LoadSave
	@LoadSaveGame
	public int researchMoneyPercent = 1000;
	/** Automatically determine battle outcome. */
	@LoadSave
	@LoadSaveGame
	public boolean automaticBattle;
	/** Repair limit. */
	@LoadSave
	@LoadSaveGame
	public int autoRepairLimit = 0;
	/** Display the building names and status? */
	@LoadSave
	@LoadSaveGame
	public boolean showBuildingName = true;
	/** Display the union of the radar circles instead of each circle separately? */
	@LoadSave
	@LoadSaveGame
	public boolean radarUnion;
	/** The alpha cache max element count. Zero means disabling the cache. */
	@LoadSave
	public int tileCacheSize = 0;
	/** Classic right-click action control scheme. */
	@LoadSave
	@LoadSaveGame
	public boolean classicControls;
	/** 
	 * Limit on base tile size for tile caching.
	 * <p>Positive value means tiles larger than the given value.
	 * Negative value means tiles smaller than the given absolute value.
	 * Zero means no caching.</p>
	 */
	@LoadSave
	public int tileCacheBaseLimit = -10;
	/** 
	 * Limit on building size for tile caching.
	 * <p>Positive value means tiles larger than the given value.
	 * Negative value means tiles smaller than the given absolute value.
	 * Zero means no caching.</p>
	 */
	@LoadSave
	public int tileCacheBuildingLimit = 10;
	/** Enable the animation of inventory? */
	@LoadSave
	public boolean animateInventory = true;
	/**
	 * Initialize configuration.
	 * @param fileName the filename
	 */
	public Configuration(String fileName) {
		this.fileName = fileName;
	}
	/**
	 * Try to load the configuration file.
	 * @return true if the configuration was loaded successfully
	 */
	public boolean load() {
		File f = new File(fileName);
		isNew = !f.canRead();
		if (!isNew) {
			isNew = !processConfig();
		}
		return !isNew;
	}
	/**
	 * Process configuration.
	 * @return true if the configuration was processed successfully.
	 */
	private boolean processConfig() {
		try {
			Properties props = new Properties();
			FileInputStream fin = new FileInputStream(fileName);
			try {
				props.loadFromXML(fin);
			} finally {
				fin.close();
			}
			loadProperties(props);
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
	}
	/**
	 * Assign properties to fields.
	 * @param props the properties.
	 */
	private void loadProperties(Properties props) {
		try {
			for (Field f : this.getClass().getDeclaredFields()) {
				if (f.isAnnotationPresent(LoadSave.class)) {
					if (f.getType() == Boolean.TYPE) {
						String s = props.getProperty(f.getName());
						if (s != null) {
							f.set(this,  Boolean.valueOf(s));
						}
					} else
					if (f.getType() == Integer.TYPE || f.getType() == Integer.class) {
						String s = props.getProperty(f.getName());
						if (s != null) {
							f.set(this, Integer.valueOf(s));
						}
					} else
					if (f.getType() == String.class) {
						f.set(this, props.getProperty(f.getName(), (String)f.get(this)));
					}
				}
			}
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Save properties.
	 * @param props the properties
	 */
	private void saveProperties(Properties props) {
		try {
			for (Field f : this.getClass().getDeclaredFields()) {
				if (f.isAnnotationPresent(LoadSave.class)) {
					Object o = f.get(this);
					if (o != null) {
						props.setProperty(f.getName(), o.toString());
					}
				}
			}
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Save the configuration.
	 * @return true if the save was successful.
	 */
	public boolean save() {
		try {
			Properties props = new Properties();
			FileOutputStream fout = new FileOutputStream(fileName);
			saveProperties(props);
			try {
				props.storeToXML(fout, "Open Imperium Galactica Configuration");
			} finally {
				fout.close();
			}
			isNew = false;
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
	}
	/**
	 * Creates a new resource locator object based on the current configuration.
	 * Scans the containsers.
	 * @return the resource locator ready to be used
	 */
	public ResourceLocator newResourceLocator() {
		ResourceLocator rl = new ResourceLocator(language);
		List<String> cont = null;
		if (autoResources) {
			cont = getContainersAutomatically();
		}
		if (cont == null || cont.size() == 0) {
			cont = containers;
		}
		rl.setContainers(cont);
		rl.scanResources();
		return rl;
	}
	/**
	 * @return Find the containers automatically
	 */
	public List<String> getContainersAutomatically() {
		List<String> result = new ArrayList<String>();
		if (new File("audio").exists()) {
			result.add("audio");
		}
		if (new File("data").exists()) {
			result.add("data");
		}
		if (new File("images").exists()) {
			result.add("images");
		}
		if (new File("video").exists()) {
			result.add("video");
		}
		File[] files = new File(".").listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().startsWith("open-ig-") && name.toLowerCase().endsWith(".zip");
			}
		});
		TreeSet<String> upgrades = new TreeSet<String>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		if (files != null) {
			for (File f : files) {
				String name = f.getName();
				if (name.toLowerCase().startsWith("open-ig-upgrade-") && name.toLowerCase().endsWith(".zip")) {
					upgrades.add(name);
				} else {
					result.add(f.getName());
				}
			}
		}
		for (String s : upgrades) {
			result.add(0, s);
		}
		
		return result;
	}
	/**
	 * Load game properties from the given game world object.
	 * @param xworld the world object.
	 */
	public void loadProperties(XElement xworld) {
		try {
			for (Field f : this.getClass().getDeclaredFields()) {
				if (f.isAnnotationPresent(LoadSaveGame.class)) {
					if (f.getType() == Boolean.TYPE) {
						if (xworld.has(f.getName())) {
							f.set(this,  Boolean.valueOf(xworld.get(f.getName())));
						}
					} else
					if (f.getType() == Integer.TYPE || f.getType() == Integer.class) {
						if (xworld.has(f.getName())) {
							f.set(this, Integer.valueOf(xworld.get(f.getName())));
						}
					} else
					if (f.getType() == String.class) {
						if (xworld.has(f.getName())) {
							f.set(this,  xworld.get(f.getName()));
						}
					}
				}
			}
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Save game properties from the given game world object.
	 * @param xworld the world object.
	 */
	public void saveProperties(XElement xworld) {
		try {
			for (Field f : this.getClass().getDeclaredFields()) {
				if (f.isAnnotationPresent(LoadSaveGame.class)) {
					xworld.set(f.getName(), f.get(this));
				}
			}
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		}
	}
}
