/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.utils.Exceptions;
import hu.openig.utils.IOUtils;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.stream.XMLStreamException;

/**
 * @author akarnokd, 2012.12.13.
 *
 */
public class CEDataManager {
    /** The master campaign data. */
    public CampaignData campaignData;
    /** The resource manager. */
    public CEResourceManager mgr;
    /**
     * Constructor. Initializes the resource manager.
     * @param workdir the working directory
     */
    public CEDataManager(File workdir) {
        mgr = new CEResourceManager(workdir);
        mgr.scan();
    }
    /**
     * Perform a copy of the current definition based on the supplied settings.
     * @param copySettings the copy settings
     */
    public void copy(Map<DataFiles, CopyOperation> copySettings) {
        String name = campaignData.definition.name;
        campaignData.directory = new File(mgr.workdir(), "dlc/" + name);
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
                byte[] data = mgr.getData(original + ".xml", "generic");
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
                byte[] data = mgr.getData(original + ".xml", "generic");
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
                byte[] data = mgr.getData(original + ".xml", "generic");
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
                byte[] data = mgr.getData(original + ".xml", "generic");
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
                byte[] data = mgr.getData(original + ".xml", "generic");
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
                byte[] data = mgr.getData(original + ".xml", "generic");
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
                byte[] data = mgr.getData(original + ".xml", "generic");
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
                byte[] data = mgr.getData(original + ".xml", "generic");
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
                byte[] data = mgr.getData(original + ".xml", "generic");
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
                byte[] data = mgr.getData(original + ".xml", "generic");
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
                byte[] data = mgr.getData(original + ".xml", "generic");
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
                byte[] data = mgr.getData(original + ".xml", "generic");
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
                byte[] data = mgr.getData(original + ".xml", "generic");
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

        mgr.scan();
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
            Map<String, byte[]> galaxies = mgr.getData(campaignData.definition.galaxy + ".xml");
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

                                    byte[] xp = mgr.getData(String.format(pattern, i) + ".xml", "generic");

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
            List<String> newRefs = new ArrayList<>();
            int i = 1;
            for (String ref : campaignData.definition.labels) {
                Map<String, byte[]> allData = mgr.getData(ref);
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
        cop = copySettings.get(DataFiles.DEFAULT_LABELS);
        if (cop == CopyOperation.COPY) {
            Map<String, byte[]> labels2 = mgr.getData("labels.xml");
            if (labels2 != null && !labels2.isEmpty()) {
                for (Map.Entry<String, byte[]> e : labels2.entrySet()) {
                    File f = new File(dir, e.getKey() + "/campaign/" + name);
                    if (!f.exists() && !f.mkdirs()) {
                        Exceptions.add(new IOException("Could not create directories for " + f));
                    }
                    IOUtils.save(new File(f, "labels.xml"), e.getValue());
                }

                campaignData.definition.labels.add("campaign/" + name + "/labels");
            }
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
            Map<String, byte[]> allData = mgr.getData(campaignData.definition.imagePath + ".png");
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

        campaignData.labels = new CampaignLabels();
        campaignData.labels.load(campaignData.definition, mgr);

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

        mgr.saveDir = mgr.getContainer("campaign/" + campaignData.definition.name + "/definition.xml", "generic");
    }
    /**
     * Save the data files.
     */
    public void save() {
        if (campaignData.labels.newEntryUsed) {
            String nel = campaignData.labels.newEntryLocation;
            if (campaignData.definition.labels.contains(nel)) {
                campaignData.definition.labels.add(nel);
            }
        }
        campaignData.definition.save(campaignData.def);
        save("campaign/" + campaignData.definition.name + "/definition", campaignData.def);

        campaignData.labels.save(mgr);

        save(campaignData.definition.galaxy, campaignData.galaxy);
        save(campaignData.definition.players, campaignData.players);
        save(campaignData.definition.planets, campaignData.planets);
        save(campaignData.definition.tech, campaignData.technology);
        save(campaignData.definition.buildings, campaignData.buildings);
        save(campaignData.definition.battle, campaignData.battle);
        save(campaignData.definition.diplomacy, campaignData.diplomacy);
        save(campaignData.definition.bridge, campaignData.bridge);
        save(campaignData.definition.talks, campaignData.talks);
        save(campaignData.definition.walks, campaignData.walks);
        save(campaignData.definition.chats, campaignData.chats);
        save(campaignData.definition.test, campaignData.test);
        save(campaignData.definition.spies, campaignData.spies);
        save(campaignData.definition.scripting, campaignData.scripting);

    }
    /**
     * Load an XML reference.
     * @param reference the reference path
     * @return the XElement
     */
    XElement load(String reference) {
        try {
            byte[] data = mgr.getData(reference + ".xml", "generic");
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
     * @param reference the file reference without extension
     * @param xml the XML to save
     */
    void save(String reference, XElement xml) {
        if (xml == null) {
            int idx = reference.lastIndexOf('/');
            String key = reference;
            if (idx >= 0) {
                key = key.substring(idx + 1);
            }
            xml = new XElement(key);
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            xml.save(bout);
        } catch (IOException ex) {
            // ignored
        }

        mgr.saveData(reference + ".xml", "generic", bout.toByteArray());
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
        byte[] data = mgr.getData(resource, language);
        if (data == null && !language.equals("generic")) {
            data = mgr.getData(resource, "generic");
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
        return campaignData.labels.get(campaignData.projectLanguage, key);
    }
    /**
     * Sets the label for the current project language entry.
     * @param key the key
     * @param value the value
     */
    public void setLabel(String key, String value) {
        campaignData.labels.set(campaignData.projectLanguage, key, value);
    }
    /**
     * Test if a label exists in the project language.
     * @param key the key
     * @return true if the label
     */
    public boolean hasLabel(String key) {
        return !U.nullOrEmpty(campaignData.labels.get(campaignData.projectLanguage, key));
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
        return mgr.exists(reference, campaignData.projectLanguage) || mgr.exists(reference, "generic");
    }
    /** @return the list of supported languages of the campaign. */
    public List<String> languages() {
        return U.newArrayList(campaignData.definition.languages());
    }
    /** @return the definition directory. */
    public File getDefinitionDirectory() {
        return new File(campaignData.directory, "generic/campaign/" + campaignData.definition.name);
    }
    /** @return the working directory. */
    public File workdir() {
        return mgr.workdir();
    }
}
