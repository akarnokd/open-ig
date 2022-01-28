/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.utils.PACFile;
import hu.openig.utils.PACFile.PACEntry;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Extract and pair building and technology names from old resources.
 * @author akarnokd, 2012.04.20.
 */
public final class BuildingNames {
    /** Utility class. */
    private BuildingNames() { }
    /**
     * @param args the arguments
     * @throws Exception ignored
     */
    public static void main(String[] args) throws Exception {

        Map<String, PACEntry> names = PACFile.mapByName(PACFile.parseFully("c:/downloads/PCX_GER/TEXT.PAC"));

        List<String> bn = lines(names.get("EPUL_NEV.TXT").data);
        List<String> bi = lines(names.get("EPUL_INF.TXT").data);

        for (int i = 0; i < bn.size(); i++) {
            System.out.println(bn.get(i));
            for (int j = i * 3; j < bi.size() && j < i * 3 + 3; j++) {
                System.out.print(bi.get(j));
            }
            System.out.println();
            System.out.println();
        }
    }
    /**
     * Load lines from the byte data.
     * @param data the data
     * @return the lines
     * @throws IOException ignored
     */
    public static List<String> lines(byte[] data) throws IOException {
        List<String> result = new ArrayList<>();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(data), "ISO-8859-1"));

        String l;

        while ((l = in.readLine()) != null) {
            result.add(l);
        }

        return result;
    }
}
