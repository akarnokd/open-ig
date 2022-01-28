/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.utils.XElement;

import java.util.HashMap;
import java.util.Map;

/**
 * Removes the duplicates from a game label and the main labels.
 * @author akarnokd, 2012.09.17.
 */
public final class RemoveGameLabelDuplicates {
    /** Utility class. */
    private RemoveGameLabelDuplicates() { }

    /**
     * @param args no arguments
     * @throws Exception ignored.
     */
    public static void main(String[] args) throws Exception {
        XElement main = XElement.parseXML("data/hu/labels.xml");
        Map<String, String> mainMap = new HashMap<>();
        for (XElement xe : main.childrenWithName("entry")) {
            if (xe.content != null) {
                mainMap.put(xe.get("key"), xe.content);
            }
        }
        String dlcFile = "dlc/gump891202-racemod-0.1/hu/skirmish/racemod-0.1/labels.xml";
        XElement dlc = XElement.parseXML(dlcFile);
        for (XElement xe3 : dlc.childrenWithName("entry")) {
            if (mainMap.containsKey(xe3.get("key")) && xe3.content != null) {
                dlc.children().remove(xe3);
            }
        }
        dlc.save(dlcFile);
    }

}
