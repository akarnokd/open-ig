/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.utils.IOUtils;
import hu.openig.utils.XElement;

import java.io.File;
import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

/**
 * Combine a label with fragments.
 * @author akarnokd, 2012.10.23.
 */
public final class LabelIntegrator {
    /** Utility class. */
    private LabelIntegrator() { }

    /**
     * @param args no arguments
     * @throws Exception ignored
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> labels = new LinkedHashMap<>();
        File dest = new File("data/ru/labels.xml");
        XElement xlabels = XElement.parseXML(dest);
        for (XElement xentry : xlabels.childrenWithName("entry")) {
            String key = xentry.get("key");
            if (!key.isEmpty()) {
                labels.put(key, xentry.content);
            }
        }
        // ----------------------
        String prefix = "c:/Downloads/";
        List<String> files = Arrays.asList("labels08.xml");

        String param = "%,?\\d*(s|d|f|x)";

        for (String f : files) {
            String content = new String(IOUtils.load(prefix + f), "UTF-8");
            XElement xentries;
            try {
                xentries = XElement.parseXML(new StringReader(content));
            } catch (XMLStreamException ex) {
                String data = "<labels>" + content + "</labels>";
                xentries = XElement.parseXML(new StringReader(data));
            }

            for (XElement xentry : xentries.childrenWithName("entry")) {
                String key = xentry.get("key");
                String value = xentry.content;
                String orig = labels.get(key);
                if (orig != null) {
//                    System.out.printf("Update  %s = %s%n", key, value);
//                    System.out.flush();
                    // check for parametrization

                    if (orig.matches(param)) {
                        if (!value.matches(param)) {
                            System.err.println("Missing arguments:");
                            System.err.println("Orig: " + orig);
                            System.err.println("New : " + value);
                            continue;
                        }
                    }

                    labels.put(key, value);
                } else {
                    System.err.printf("Missing %s = %s%n", key, value);
                    System.err.flush();
                }
            }
        }

        // ----------------------
        xlabels.clear();
        for (Map.Entry<String, String> e : labels.entrySet()) {
            XElement xentry = xlabels.add("entry");
            xentry.set("key", e.getKey());
            xentry.content = e.getValue();
        }
        xlabels.save(dest);
    }

}
