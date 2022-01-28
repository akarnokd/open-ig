/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.utils.XElement;

import java.util.Collections;
import java.util.Comparator;

/**
 * Sort labels.xml.
 * @author akarnokd, 2012.04.20.
 */
public final class SortLabels {
    /** Utility class. */
    private SortLabels() { }
    /**
     * @param args no arguments
     * @throws Exception ignored
     */
    public static void main(String[] args) throws Exception {

        String fn = "data/es/labels.xml";

        XElement e = XElement.parseXML(fn);

        Collections.sort(e.children(), new Comparator<XElement>() {
            @Override
            public int compare(XElement o1, XElement o2) {
                String s1 = o1.content != null ? o1.content : ""; // o1.get("key");
                String s2 = o2.content != null ? o2.content : ""; // o2.get("key");
                return Integer.compare(s1.length(), s2.length());
            }
        });

        e.save(fn);

    }

}
