/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.xml.stream.XMLStreamException;

import hu.openig.utils.Exceptions;
import hu.openig.utils.XElement;

/**
 * The current user's profile.
 * @author akarnokd, Apr 9, 2011
 */
public class Profile {
    /** The profile's name. */
    public String name;
    /** The acquired achievements. */
    private final Map<String, AchievementProgress> achievements = new HashMap<>();
    /** Set of unlocked videos. */
    private final Set<String> unlockedVideos = new LinkedHashSet<>();
    /**
     * Test if the given achievement is taken.
     * @param name the achievement name
     * @return the status
     */
    public boolean hasAchievement(String name) {
        AchievementProgress ap = achievements.get(name);
        if (ap != null && ap.isComplete()) {
            return true;
        }
        return false;
    }
    /**
     * Unlock a specific video.
     * @param name the resource name of the video
     */
    public void unlockVideo(String name) {
        if (name != null) {
            unlockedVideos.add(name);
        }
    }
    /**
     * Check if a particular video is unlocked.
     * @param name the resource name of the video
     * @return true if the video is unlocked
     */
    public boolean isVideoUnlocked(String name) {
        return unlockedVideos.contains(name);
    }
    /**
     * Returns a list of all unlocked videos.
     * @return the list of unlocked videos
     */
    public List<String> unlockedVideos() {
        return new ArrayList<>(unlockedVideos);
    }
    /**
     * Save the current profile settings.
     * <p>Will reschedule onto the EDT if necessary.</p>
     */
    public void save() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    save();
                }
            });
            return;
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
        for (AchievementProgress s : achievements.values()) {
            XElement xach = xprofile.add("achievement");
            xach.set("id", s.name);
            if (!s.legacy) {
                xach.set("progress", s.progress);
                xach.set("max", s.max);
                xach.set("display-progress", s.displayProgress);
            }
        }
        for (String s : unlockedVideos) {
            xprofile.add("video").set("id", s);
        }
    }
    /**
     * Load the profile.
     * @param xprofile the source XML
     */
    void load(XElement xprofile) {
        achievements.clear();
        for (XElement xa : xprofile.childrenWithName("achievement")) {
            AchievementProgress ap = new AchievementProgress(xa.get("id"));
            if (xa.has("progress") && xa.has("max")) {
                ap.progress = xa.getDouble("progress");
                ap.max = xa.getDouble("max");
                ap.displayProgress = xa.getBoolean("display-progress");
            } else {
                ap.legacy = true;
            }
            achievements.put(ap.name, ap);
        }
        for (XElement xv : xprofile.childrenWithName("video")) {
            unlockedVideos.add(xv.get("id"));
        }
    }
    /**
     * Update the progress on the given achievement and
     * return true if the objective amount has been reached.
     * @param id the id of the achievement
     * @return true if the achievement has reached or surpassed its progress limit
     */
    public AchievementProgress getOrCreateProgress(String id) {
        AchievementProgress ap = achievements.get(id);
        if (ap == null) {
            ap = new AchievementProgress(id);
            achievements.put(ap.name, ap);
        }
        return ap;
    }
    /**
     * Returns the achivement progress if it exists, null otherwise.
     * @param id the achievement id
     * @return the progress or null
     */
    public AchievementProgress getProgress(String id) {
        return achievements.get(id);
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
