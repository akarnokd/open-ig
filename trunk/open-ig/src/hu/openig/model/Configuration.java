/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;


import hu.openig.core.Func0;
import hu.openig.utils.Exceptions;
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
	public static final String VERSION = "0.95.117";
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
	/** List of files or directories to be ignored by the resource locator. */
	@LoadSave
	public final List<String> ignore = new ArrayList<String>();
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
	/** Returns the current crash log or null if no such log is available. */
	public Func0<String> crashLog;
	/** Are we in full-screen mode? */
	@LoadSave
	public boolean fullScreen;
	/** Is the window maximized mode? */
	@LoadSave
	public boolean maximized;
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
	/** The research money percent times 10, e.g., 100% == 1000. */
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
	/** Invert the left-right mouse button events. */
	@LoadSave
	@LoadSaveGame
	public boolean swapMouseButtons;
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
	@LoadSaveGame
	public boolean animateInventory = true;
	/** Slow down the game in case of enemy attack? */
	@LoadSave
	@LoadSaveGame
	public boolean slowOnEnemyAttack = true;
	/** Display subtitles. */
	@LoadSave
	@LoadSaveGame
	public boolean subtitles = true;
	/** The time step in minutes for the simulation. */
	@LoadSave
	@LoadSaveGame
	public int timestep = 10;
	/** The user interface scaling factor. */
	@LoadSave
	public int uiScale = 100;
	/** Should the movie be scaled to full screen? */
	@LoadSave
	public boolean movieScale;
	/** Allow skipping cutscenes by mouse click? */
	@LoadSave
	public boolean movieClickSkip;
	/** Show quick research and production buttons? */ 
	@LoadSave
	@LoadSaveGame
	public boolean quickRNP = true;
	/** Scale the secondary, fixed size screens. */
	@LoadSave
	@LoadSaveGame
	public boolean scaleAllScreens = false;
	/** AI should attack every building, not just the defensive ones. */
	@LoadSave
//	@LoadSaveGame
	public boolean aiGroundAttackEverything = false;
	/** AI should attack a mixed set of targets. */
	@LoadSave
//	@LoadSaveGame
	public boolean aiGroundAttackMixed = true;
	/** Force the AI autobuild to ignore static defenses until the economic buildings have been built. */
	@LoadSave
//	@LoadSaveGame
	public boolean autoBuildEconomyFirst = true;
	/** Use standard Java fonts instead of the original bitmap fonts. */
	@LoadSave
//	@LoadSaveGame
	public boolean useStandardFonts = false;
	/** Play the intro videos? */
	@LoadSave
	public boolean intro = true;
	/** Flag from command line to continue with the last game. */
	public boolean continueLastGame;
	/** The tooltip's maximum width. */
	public int tooltipWidth = 600;
	/** Show tooltips? */
	@LoadSave
	public boolean showTooltips = true;
	/** How many technology to remember. */
	@LoadSave
	public int productionHistoryLimit = 10;
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
			Exceptions.add(ex);
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
					String n = f.getName();
					if (f.getType() == Boolean.TYPE) {
						String s = props.getProperty(n);
						if (s != null) {
							f.set(this,  Boolean.valueOf(s));
						}
					} else
					if (f.getType() == Integer.TYPE || f.getType() == Integer.class) {
						String s = props.getProperty(n);
						if (s != null) {
							f.set(this, Integer.valueOf(s));
						}
					} else
					if (f.getType() == String.class) {
						f.set(this, props.getProperty(n, (String)f.get(this)));
					} else
					if (List.class.isAssignableFrom(f.getType())) {
						@SuppressWarnings("unchecked")
						List<String> lst = (List<String>)f.get(this);
						int cnt = Integer.parseInt(props.getProperty(n + "-count", "0"));
						for (int i = 0; i < cnt; i++) {
							lst.add(props.getProperty(n + "-" + i));
						}
					}
				}
			}
		} catch (IllegalAccessException ex) {
			Exceptions.add(ex);
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
					String n = f.getName();
					Object o = f.get(this);
					if (o != null) {
						if (List.class.isAssignableFrom(f.getType())) {
							@SuppressWarnings("unchecked")
							List<String> lst = (List<String>)f.get(this);
							props.setProperty(n + "-count", Integer.toString(lst.size()));
							for (int i = 0; i < lst.size(); i++) {
								props.setProperty(n + "-" + i, lst.get(i));
							}
						} else {
							props.setProperty(f.getName(), o.toString());
						}
					}
				}
			}
		} catch (IllegalAccessException ex) {
			Exceptions.add(ex);
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
			Exceptions.add(ex);
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
		List<String> cont = getContainersAutomatically();
		cont.removeAll(ignore);
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
		File dlc = new File("dlc");
		if (dlc.exists()) {
			File[] dlcs = dlc.listFiles();
			if (dlcs != null) {
				TreeSet<String> upgrades = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
				for (File f : dlcs) {
					if (f.getName().endsWith(".zip") || f.isDirectory()) {
						upgrades.add("dlc/" + f.getName());
					}
				}
				result.addAll(upgrades);
			}
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
			Exceptions.add(ex);
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
			Exceptions.add(ex);
		}
	}
}
