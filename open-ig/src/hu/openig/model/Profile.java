/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.utils.Exceptions;
import hu.openig.utils.XElement;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.xml.stream.XMLStreamException;

/**
 * The current user's profile.
 * @author akarnokd, Apr 9, 2011
 */
public class Profile {
	/** The profile's name. */
	public String name;
	/** The acquired achievements. */
	private final Set<String> achievements = new HashSet<>();
	/**
	 * Test if the given achievement is taken.
	 * @param name the achievement name
	 * @return the status
	 */
	public boolean hasAchievement(String name) {
		return achievements.contains(name);
	}
	/**
	 * Save the current profile settings.
	 * <p>Will run on EDT.</p>
	 */
	public void save() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					save();
				}
			});
		}
		XElement xprofile = new XElement("profile");
		try {
			File dir = new File("save/" + name);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
                    Exceptions.add(new IOException("Unable to create directory " + dir));
                }
			}
			save(xprofile);
			xprofile.save(new File(dir, "profile.xml"));
		} catch (IOException ex) {
			Exceptions.add(ex);
		}
	}
	/**
	 * Save the current profile.
	 * @param xprofile the profile XML
	 */
	void save(XElement xprofile) {
		for (String s : achievements) {
			xprofile.add("achievement").set("id", s);
		}
	}
	/**
	 * Load the profile.
	 * @param xprofile the source XML
	 */
	void load(XElement xprofile) {
		achievements.clear();
		for (XElement xa : xprofile.childrenWithName("achievement")) {
			achievements.add(xa.get("id"));
		}
	}
	/**
	 * Grant the achievement in the profile.
	 * <p>Immediately saves the profile on the EDT.</p>
	 * @param id the id of the achievement
	 */
	public void grantAchievement(String id) {
		if (achievements.add(id)) {
			save();
		}
	}
	/**
	 * Load the profile.
	 */
	public void load() {
		try {
			File f = new File("save/" + name + "/profile.xml");
			if (f.canRead()) {
				XElement xprofile = XElement.parseXML(f.getAbsolutePath());
				load(xprofile);
			}
		} catch (XMLStreamException ex) {
			Exceptions.add(ex);
		}
	}
	/**
	 * Prepares a non-existent profile or loads an existing one.
	 */
	public void prepare() {
		File f = new File("save/" + name + "/profile.xml");
		if (f.canRead()) {
			load();
		} else {
			save();
		}
	}
}
