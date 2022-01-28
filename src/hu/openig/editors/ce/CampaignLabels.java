/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.core.Pair;
import hu.openig.model.GameDefinition;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

/**
 * Manages the multiple project labels.
 * @author akarnokd, 2012.12.27.
 */
public class CampaignLabels {
    /** Map from key to language to resource location (path + extension) and the actual text. */
    protected Map<String, Map<String, String[]>> map = new LinkedHashMap<>();
    /** The set of modifiable pair of language and resource location. */
    protected Set<Pair<String, String>> modifiable = new HashSet<>();
    /** The resource name for new or modified read-only entries. */
    public String newEntryLocation;
    /** Indicate that the local file. */
    public boolean newEntryUsed;
    /**
     * Loads the labels associated with the given definition.
     * @param def the definition.
     * @param mgr the resource manager
     */
    public void load(GameDefinition def, CEResourceManager mgr) {
        newEntryUsed = false;
        newEntryLocation = "campaign/ " + def.name + "/labels.xml";
        for (String res : U.startWith(def.labels, "labels.xml")) {
            Map<String, byte[]> data = mgr.getData(res);
            for (Map.Entry<String, byte[]> e : data.entrySet()) {
                String lang = e.getKey();
                if (mgr.canEdit(res, lang)) {
                    modifiable.add(Pair.of(lang, res));
                }
                try {
                    XElement xml = XElement.parseXML(e.getValue());
                    for (XElement xe : xml.childrenWithName("entry")) {
                        String key = xe.get("key", "");
                        String value = xe.content;
                        if (!U.nullOrEmpty(key) && !U.nullOrEmpty(value)) {
                            add(key, lang, res, value);
                        }
                    }
                } catch (XMLStreamException ex) {
                    System.out.println(U.stacktrace(ex));
                }
            }
        }
    }
    /**
     * Save the labels.
     * @param mgr the resource manager
     */
    public void save(CEResourceManager mgr) {
        Map<String, Map<String, XElement>> labelFiles = new HashMap<>();
        for (Map.Entry<String, Map<String, String[]>> e : map.entrySet()) {
            String key = e.getKey();
            for (Map.Entry<String, String[]> e2 : e.getValue().entrySet()) {
                String lang = e2.getKey();
                String res = e2.getValue()[0];
                String value = e2.getValue()[1];

                Map<String, XElement> perLang = labelFiles.get(res);
                if (perLang == null) {
                    perLang = new HashMap<>();
                    labelFiles.put(res, perLang);
                }
                XElement xml = perLang.get(lang);
                if (xml == null) {
                    xml = new XElement("labels");
                    perLang.put(lang, xml);
                }
                XElement xe = xml.add("entry");
                xe.set("key", key);
                xe.content = value;
            }
        }
        for (Map.Entry<String, Map<String, XElement>> e : labelFiles.entrySet()) {
            String res = e.getKey();
            for (Map.Entry<String, XElement> e2 : e.getValue().entrySet()) {
                String lang = e2.getKey();
                XElement xml = e2.getValue();

                if (mgr.canEdit(res, lang)) {
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    try {
                        xml.save(bout);
                    } catch (IOException ex) {
                        // ignored
                    }
                    mgr.saveData(res, lang, bout.toByteArray());
                }
            }
        }
    }
    /**
     * Add an entry.
     * @param key the key
     * @param language the language
     * @param resource the resource
     * @param value the value
     */
    protected void add(String key, String language, String resource, String value) {
        Map<String, String[]> perLang = map.get(key);
        if (perLang == null) {
            perLang = new HashMap<>();
            map.put(key, perLang);
        }
        perLang.put(language, new String[] { resource, value });

    }
    /**
     * Retrieve a translation for a key.
     * @param language the language
     * @param key the key
     * @return the translation or null if not present
     */
    public String get(String language, String key) {
        Map<String, String[]> entry = map.get(key);
        if (entry != null) {
            String[] kv = entry.get(language);
            if (kv != null && kv.length == 2) {
                return kv[1];
            }
        }
        return null;
    }
    /**
     * Save a new key and/or translation.
     * @param language the language
     * @param key the key
     * @param value the value
     */
    public void set(String language, String key, String value) {
        Map<String, String[]> entry = map.get(key);
        if (entry == null) {
            entry = new HashMap<>();
            map.put(key, entry);
        }
        String[] kv = entry.get(language);
        if (kv == null || kv.length != 2 || !modifiable.contains(Pair.of(language, kv[0]))) {
            kv = new String[] { newEntryLocation, value };
            entry.put(language, kv);
            newEntryUsed = true;
        } else {
            kv[1] = value;
        }

    }
    /**
     * Puts a per language translation into the supplied output map.
     * @param key the label key
     * @param out the map from language to translation
     */
    public void putInto(String key, Map<String, String> out) {
        Map<String, String[]> perLang = map.get(key);
        if (perLang != null) {
            for (Map.Entry<String, String[]> e : perLang.entrySet()) {
                out.put(e.getKey(), e.getValue()[1]);
            }
        }
    }
    /** @return the set of available keys. */
    public Set<String> keys() {
        return map.keySet();
    }
}
