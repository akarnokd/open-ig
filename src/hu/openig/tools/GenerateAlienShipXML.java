/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Generate the XML for the new alien ships.
 * @author akarnokd, 2014-07-29
 */
public final class GenerateAlienShipXML {
    /** Tool class. */
    private GenerateAlienShipXML() { }
    /**
     * Entry point.
     * @param args no arguments
     * @throws Exception ignored
     */
    public static void main(String[] args) throws Exception {
        String[] races = {
            "dargslan",
            "morgath",
            "ecalep",
            "sullep",
            "ychom",
            "dribs"
        };

        String[] file = {
            "tech", "battle", "kamikaze", "label"

        };

        System.out.println("------------");
        for (String f : file) {
            Files.deleteIfExists(Paths.get("/temp/" + f + ".txt"));
            String str = new String(Files.readAllBytes(Paths.get("/temp/" + f + "_template.txt")), "UTF-8");

            for (String race : races) {

                String str2 = replaceAll(str, "{race}", race);
                str2 = replaceAll(str2, "{Race}", race.substring(0, 1).toUpperCase() + race.substring(1));

                Files.write(Paths.get("/temp/" + f + ".txt"), str2.getBytes("UTF-8"), StandardOpenOption.APPEND, StandardOpenOption.CREATE);

            }
            System.out.println("------------");
        }

    }
    /**
     * Replaces all occurrences of {@code what} with {@code with} in {@code src}
     * (non regexp).
     * @param src the source string
     * @param what the thing to replace
     * @param with the replacement
     * @return the string with replaced parts
     */
    public static String replaceAll(String src, String what, String with) {
        StringBuilder b = new StringBuilder();
        int idx0 = 0;
        int idx = src.indexOf(what, idx0);
        while (idx >= 0) {
            b.append(src, idx0, idx);
            b.append(with);
            idx0 = idx + what.length();
            idx = src.indexOf(what, idx0);
        }
        b.append(src, idx0, src.length());
        return b.toString();
    }
}
