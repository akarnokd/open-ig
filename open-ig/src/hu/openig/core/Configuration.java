/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;


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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.SwingUtilities;

/**
 * The main configuration object with rolling log support.
 * @author akarnokd, 2009.09.25.
 */
public class Configuration {
	/** The version string. */
	public static final String VERSION = "0.87";
	/** Annotation for indicating load/save a field. */
	@Retention(RetentionPolicy.RUNTIME)
	@interface LoadSave { }
	/** The log limit. */
	private int logLimit = 512;
	/** The log entries. */
	public final List<LogEntry> logs = new ArrayList<LogEntry>();
	/** The log listeners. */
	public final Set<Act> logListener = new HashSet<Act>();
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
	@LoadSave
	public int audioChannels = 8;
	/** The music volume 0-100. */
	@LoadSave
	public int musicVolume = 100;
	/** Mute music? */
	@LoadSave
	public boolean muteMusic;
	/** The effect volume 0-100. */
	@LoadSave
	public int effectVolume = 100;
	/** Mute effect. */
	@LoadSave
	public boolean muteEffect;
	/** The effect filter step. */
	@LoadSave
	public int effectFilter = 1;
	/** Video volume 0-100. */
	@LoadSave
	public int videoVolume = 100;
	/** Mute video. */
	@LoadSave
	public boolean muteVideo;
	/** The video filter step. */
	@LoadSave
	public int videoFilter = 1;
	/** The debug watcher window. */
	public Closeable watcherWindow;
	/** Automatically find resource files instead of fixed set. */
	@LoadSave
	public boolean autoResources = true;
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
			error(ex);
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
					if (Collection.class.isAssignableFrom(f.getType())) {
						String s = props.getProperty(f.getName() + "-count");
						if (s != null) {
						int count = Integer.parseInt(s);
							if ("containers".equals(f.getName())) {
								for (int i = 0; i < count; i++) {
									s = props.getProperty(f.getName() + "-" + i);
									containers.add(s);
								}
							}
						}
					} else
					if (f.getType() == String.class) {
						f.set(this, props.getProperty(f.getName(), (String)f.get(this)));
					}
				}
			}
		} catch (IllegalAccessException ex) {
			error(ex);
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
					if (Collection.class.isAssignableFrom(f.getType())) {
						Collection<?> c = (Collection<?>)f.get(this);
						if ("containers".equals(f.getName())) {
							Iterator<?> it = c.iterator();
							int i = 0;
							while (it.hasNext()) {
								Object o = it.next();
								props.setProperty(f.getName() + "-" + i, o != null ? o.toString() : "");
								i++;
							}
						}
						props.setProperty(f.getName() + "-count", Integer.toString(c.size()));
					} else {
						Object o = f.get(this);
						if (o != null) {
							props.setProperty(f.getName(), o.toString());
						}
					}
				}
			}
		} catch (IllegalAccessException ex) {
			error(ex);
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
			return false;
		}
	}
	/**
	 * Debug a message.
	 * @param message the message
	 */
	public void debug(String message) {
		log("DEBUG", message, null);
	}
	/** 
	 * Debug a throwable.
	 * @param t the throwable
	 */
	public void debug(Throwable t) {
		log("DEBUG", t.getMessage(), t);
	}
	/**
	 * Debug a message and throwable.
	 * @param message the message
	 * @param t the throwable
	 */
	public void debug(String message, Throwable t) {
		log("DEBUG", message, t);
	}
	/**
	 * Error a message.
	 * @param message the message
	 */
	public void error(String message) {
		log("ERROR", message, null);
	}
	/**
	 * Error a throwable.
	 * @param t a throwable
	 */
	public void error(Throwable t) {
		log("ERROR", t.getMessage(), t);
	}
	/**
	 * Error a message and throwable.
	 * @param message the message
	 * @param t the throwable
	 */
	public void error(String message, Throwable t) {
		log("ERROR", message, t);
	}
	/**
	 * Log a given event.
	 * @param level the level
	 * @param message the message
	 * @param t the throwable
	 */
	public void log(final String level, final String message, final Throwable t) {
		if (SwingUtilities.isEventDispatchThread()) {
			logInternal(level, message, t);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					logInternal(level, message, t);
				}
			});
		}
	}
	/**
	 * Perform the log in the EDT.
	 * @param level the level
	 * @param message the message
	 * @param t the throwable
	 */
	private void logInternal(final String level, final String message,
			final Throwable t) {
		LogEntry e = new LogEntry();
		e.timestamp = System.currentTimeMillis();
		e.severity = level;
		e.message = message;
		e.stackTrace = LogEntry.toString(t);
		if (logs.size() == logLimit) {
			logs.remove(0);
		}
		logs.add(e);
		if (t != null) {
			t.printStackTrace();
		}
		for (Act a : logListener) {
			a.act();
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
}
