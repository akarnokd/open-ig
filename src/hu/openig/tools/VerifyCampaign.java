/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.core.ResourceType;
import hu.openig.model.Configuration;
import hu.openig.model.GameDefinition;
import hu.openig.model.Labels;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResourceLocator;
import hu.openig.model.ResourceLocator.ResourcePlace;
import hu.openig.render.TextRenderer;
import hu.openig.utils.Exceptions;
import hu.openig.utils.XElement;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

/**
 * Verify the consistency of the campaign files and any cross references
 * among them.
 * @author akarnokd, 2012.05.21.
 */
public class VerifyCampaign {
    /** The resource locator. */
    final ResourceLocator rl;
    /** The game. */
    final String game;
    /** The game definition. */
    GameDefinition def;
    /** All language labels. */
    final Map<String, Labels> labels = new HashMap<>();
    /** Base languages. */
    final String[] languages = { "en", "hu", "de", "fr" };
    /**
     * Constructor, initializes the locator.
     * @param cfg the configuration
     * @param game the game
     */
    public VerifyCampaign(Configuration cfg, String game) {
        this.rl = cfg.newResourceLocator();
        this.game = game;
    }
    /**
     * @param args no arguments
     */
    public static void main(String[] args) {
        System.out.println("Initializing.");
        Configuration cfg = new Configuration("open-ig-config.xml");
        cfg.load();

        String[] games = { "campaign/main" /*, "campaign/main2", "skirmish/human" */};

        for (String g : games) {
            System.out.printf("---------%n Checking %s%n", g);

            VerifyCampaign vc = new VerifyCampaign(cfg, g);
            System.out.println("Running");
            vc.run();
            System.out.println("Done.");
        }
        System.out.printf("---------%nALL DONE.%n");
    }
    /**
     * Verify the game files.
     */
    public void run() {
        def = GameDefinition.parse(rl, game);

        verifyLabels();
        verifyChats();
        verifyDiplomacy();
        verifyTech();
        verifyTest();
        verifyTalks();
    }
    /** Cross-check labels. */
    void verifyLabels() {

        TextRenderer tr = new TextRenderer(rl, false, 0);

        for (String lang1 : languages) {
            Labels l1 = new Labels();
            l1.load(getXML(lang1, "labels"));
            Labels l3 = new Labels();
            XElement x1 = getXML(lang1, game + "/labels");

            labels.put(lang1, l1);

            for (Map.Entry<String, String> v : l1.map().entrySet()) {
                if (v.getValue() != null) {
                    for (char c : v.getValue().toCharArray()) {
                        if (!tr.isSupported(c)

                                && c != '[' && c != ']'
                                && c != '$') {
                            System.err.println("Character " + c + " not supported in " + lang1 + " | " + v);
                            break;
                        }
                    }
                }
            }

            for (String lang2 : languages) {
                if (!lang1.equals(lang2)) {
                    Labels l2 = new Labels();

                    l2.load(getXML(lang2, "labels"));

                    crossCheckLabels(lang1, lang2, l1, l2);

                    Labels l4 = new Labels();
                    XElement x2 = getXML(lang2, game + "/labels");
                    if (x1 != null && x2 == null) {
                        System.err.printf("Game label missing: present %s, missing %s%n", lang1, lang2);
                    } else
                    if (x1 != null && x2 != null) {
                        l3.load(x1);
                        l4.load(x2);
                        crossCheckLabels(lang1, lang2, l3, l4);
                    }

                    l1.map().putAll(l3.map());

                }
            }
        }
    }
    /**
     * Check the existence of a label key in all languages.
     * @param key the key
     * @param where where is it needed?
     */
    void checkLabel(String key, String where) {
        StringBuilder langs = new StringBuilder();
        for (String l : languages) {
            if (!labels.get(l).map().containsKey(key)) {
                langs.append(' ').append(l);
            }
        }
        if (langs.length() > 0) {
            System.err.printf("%s %s in languages%s%n", where, key, langs);
            System.err.printf("\t<entry key='%s'></entry>%n", key);
        }
    }
    /**
     * Cross compare two label maps.
     * @param lang1 the first language
     * @param lang2 the second language
     * @param l1 the first label
     * @param l2 the second label
     */
    void crossCheckLabels(String lang1, String lang2, Labels l1, Labels l2) {
        for (String k1 : l1.map().keySet()) {
            if (!l2.map().containsKey(k1)) {
                System.err.printf("Base label missing: present %s, missing %s%n\t<entry key='%s'>%s</entry>%n", lang1, lang2, k1, l1.map().get(k1));
            }
            if ((l1.map().get(k1) == null) != (l2.map().get(k1) == null)) {
                System.err.printf("Empty/non-empty label pair (%s, %s):%n", lang1, lang2);
                System.err.printf("\t<entry key='%s'>%s</entry>%n", k1, l1.map().get(k1));

                System.err.printf("\t<entry key='%s'>%s</entry>%n", k1, l2.map().get(k1));

            }
        }
    }
    /**
     * Return an XML resource.
     * @param language the target language
     * @param path the path
     * @return the xml or null if not present
     */
    XElement getXML(String language, String path) {
        ResourcePlace rp = rl.getExactly(language, path, ResourceType.DATA);
        if (rp != null) {
            try {
                return XElement.parseXML(new ByteArrayInputStream(rp.get()));
            } catch (XMLStreamException ex) {
                Exceptions.add(ex);
            }
        }
        return null;
    }
    /**
     * Verify the correctness of the chat labels.
     */
    void verifyChats() {
        XElement xchats = rl.getXML(def.chats);

        for (XElement xchat : xchats.childrenWithName("chat")) {
            String cid = xchat.get("id");
            for (XElement xnode : xchat.childrenWithName("node")) {
                String nid = xnode.get("id");

                String opt = xnode.get("option", null);

                if (opt != null) {
                    checkLabel(opt, String.format("Missing label: chat '%s' node '%s' option", cid, nid));
                }

                String msg = xnode.get("message");

                checkLabel(msg, String.format("Missing label: chat '%s' node '%s' message", cid, nid));

            }
        }
    }
    /**
     * Check the referred diplomatic labels.
     */
    void verifyDiplomacy() {
        XElement xdipls = rl.getXML(def.diplomacy);
        for (XElement xplayer : xdipls.childrenWithName("player")) {
            String pid = xplayer.get("id");
            for (XElement xneg : xplayer.childrenWithName("negotiate")) {
                String ty = xneg.get("type");
                for (XElement xappr : xneg.childrenWithName("approach")) {
                    String at = xappr.get("type");

                    checkLabel(xappr.content, String.format("Missing label: diplomacy player %s negotiate %s approach %s ", pid, ty, at));
                }
                for (XElement xresp : xneg.childrenWithName("response")) {
                    String at = xresp.get("type");

                    checkLabel(xresp.content, String.format("Missing label: diplomacy player %s negotiate %s response %s ", pid, ty, at));
                }
            }
            for (XElement xneg : xplayer.childrenWithName("call")) {
                String ty = xneg.get("type");
                for (XElement xappr : xneg.childrenWithName("approach")) {
                    String at = xappr.get("type");

                    checkLabel(xappr.content, String.format("Missing label: diplomacy player %s call %s approach %s ", pid, ty, at));
                }
            }
        }
    }
    /** Verify the technology labels. */
    void verifyTech() {
        XElement xtechs = rl.getXML(def.tech);
        XElement xbattle = rl.getXML(def.battle);
        Map<String, XElement> techMap = new HashMap<>();
        for (XElement xtech : xtechs.childrenWithName("item")) {
            String id = xtech.get("id");
            techMap.put(id, xtech);
            String name = xtech.get("name");
            checkLabel(name, String.format("Missing label: tech %s name ", id));
            String longName = xtech.get("long-name");
            checkLabel(longName, String.format("Missing label: tech %s long-name ", id));
            String desc = xtech.get("description");
            checkLabel(desc, String.format("Missing label: tech %s description ", id));

            ResearchSubCategory cat = ResearchSubCategory.valueOf(xtech.get("category"));

            // check space stuff
            if (cat == ResearchSubCategory.SPACESHIPS_BATTLESHIPS
                    || cat == ResearchSubCategory.SPACESHIPS_STATIONS
                    || cat == ResearchSubCategory.SPACESHIPS_CRUISERS
                    || cat == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
                boolean found = false;
                for (XElement xse : xbattle.childrenWithName("space-entities")) {
                    for (XElement xset : xse.childrenWithName("tech")) {
                        if (id.equals(xset.get("id"))) {
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    System.err.printf("Missing battle settings for %s%n", id);
                }
            }
            // check ground stuff
            if (cat == ResearchSubCategory.WEAPONS_TANKS
                    || cat == ResearchSubCategory.WEAPONS_VEHICLES) {
                boolean found = false;
                for (XElement xse : xbattle.childrenWithName("ground-vehicles")) {
                    for (XElement xset : xse.childrenWithName("tech")) {
                        if (id.equals(xset.get("id"))) {
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    System.err.printf("Missing battle settings for %s%n", id);
                }
            }
        }
        for (XElement xtech : techMap.values()) {
            String id = xtech.get("id");
            String[] techRaces = xtech.get("race").split("\\s*,\\s*");
            for (XElement xslot : xtech.childrenWithName("slot")) {
                String[] items = xslot.get("items").split("\\s*,\\s*");
                String sid = xslot.get("id");

                for (String i : items) {
                    XElement xtech2 = techMap.get(i);
                    if (xtech2 != null) {
                        String[] techRaces2 = xtech2.get("race").split("\\s*,\\s*");
                        boolean found = false;
                        for (String r : techRaces) {
                            for (String r2 : techRaces2) {
                                if (r.equals(r2)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                System.err.printf("Slot technology '%s' for tech '%s' and slot '%s' is not constructable by the race '%s'%n", i, id, sid, r);
                            }
                        }
                    } else {
                        System.err.printf("Missing slot technology '%s' for tech '%s' and slot '%s'%n", i, id, sid);
                    }
                }
            }

        }
    }
    /**
     * Verify test labels.
     */
    void verifyTest() {
        XElement xtests = rl.getXML(def.test);
        for (XElement xtest : xtests.childrenWithName("question")) {
            String id = xtest.get("id");
            String qid = xtest.get("label");
            checkLabel(qid, String.format("Missing label: test question %s ", id));
            for (XElement xans : xtest.childrenWithName("answer")) {
                String id2 = xans.get("id");
                String aid = xans.get("label");

                checkLabel(aid, String.format("Missing label: test question %s answer %s", id, id2));
            }
        }
    }
    /**
     * Verify the talk option labels.
     */
    void verifyTalks() {
        XElement xtalks = rl.getXML(def.talks);
        for (XElement xtalk : xtalks.childrenWithName("talk")) {
            String id = xtalk.get("with");
            for (XElement xstate : xtalk.childrenWithName("state")) {
                String sid = xstate.get("id");
                for (XElement xtran : xstate.childrenWithName("transition")) {
                    String tid = xtran.get("id");
                    String txt = xtran.get("text");

                    checkLabel(txt, String.format("Missing label: talk with %s state %s transition %s ", id, sid, tid));
                }
            }
        }
    }
}
