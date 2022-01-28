/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen;

import hu.openig.utils.Exceptions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * An utility class to manage subtitle retrieval based
 * on elapsed time for movies.
 * @author akarnokd, 2009.09.26.
 */
public class SubtitleManager {
    /** The subtitle entry. */
    public static class SubEntry {
        /** Time start. */
        public long start;
        /** Time end. */
        public long end;
        /** Text. */
        public String text;
        /**
         * Is the time within this entry.
         * @param time the time
         * @return true
         */
        public boolean isIn(long time) {
            return start <= time && time <= end;
        }
    }
    /** The entry list. */
    final List<SubEntry> entries = new ArrayList<>();
    /** The last time. */
    long lastTime;
    /** The last index. */
    int lastIndex;
    /**

     * Constructor.
     * @param in the subtitle data to load
     */
    public SubtitleManager(InputStream in) {
        loadSub(in);
    }
    /**
     * @param in the input stream
     */
    private void loadSub(InputStream in) {
        try (BufferedReader bin = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            String line;
            while ((line = bin.readLine()) != null) {
                if (line.length() > 19) {
                    SubEntry e = new SubEntry(); // 00:00.000-00:00.000
                    e.start = Integer.parseInt(line.substring(0, 2)) * 60 * 1000
                    + Integer.parseInt(line.substring(3, 5)) * 1000
                    + Integer.parseInt(line.substring(6, 9));
                    e.end = Integer.parseInt(line.substring(10, 12)) * 60 * 1000
                    + Integer.parseInt(line.substring(13, 15)) * 1000
                    + Integer.parseInt(line.substring(16, 19));
                    e.text = line.substring(19);
                    entries.add(e);
                }
            }
        } catch (IOException ex) {
            Exceptions.add(ex);
        }
    }
    /**
     * Offset the time parameters by the given amount.
     * @param time the amount
     */
    public void offset(long time) {
        for (SubEntry e : entries) {
            e.start += time;
            e.end += time;
        }
    }
    /**
     * Convert the contents to the SUB format.
     * @param out the output stream
     * @throws IOException on error
     */
    public void toSub(OutputStream out) throws IOException {
        PrintWriter wout = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
        for (SubEntry e : entries) {
            long sh = e.start / 60 / 60 / 1000;
            long sm = (e.start / 60 / 1000) % 60;
            long ss = (e.start / 1000) % 60;
            long sz = (e.start % 1000);

            long eh = e.end / 60 / 60 / 1000;
            long em = (e.end / 60 / 1000) % 60;
            long es = (e.end / 1000) % 60;
            long ez = (e.end % 1000);

            wout.printf("%02d:%02d:%02d.%03d,%02d:%02d:%02d.%03d%n%s%n%n", sh, sm, ss, sz, eh, em, es, ez, e.text);
        }
        wout.flush();
    }
    /**
     * Get a subtitle entry for the given time.
     * @param time the time
     * @return the text or null
     */
    public String get(long time) {
        if (time < lastTime) {
            for (int i = lastIndex; i >= 0; i--) {
                SubEntry e = entries.get(i);
                if (e.isIn(time)) {
                    lastIndex = i;
                    lastTime = time;
                    return e.text;
                }
            }
        } else {
            for (int i = lastIndex; i < entries.size(); i++) {
                SubEntry e = entries.get(i);
                if (e.isIn(time)) {
                    lastIndex = i;
                    lastTime = time;
                    return e.text;
                }
            }
        }
        return null;
    }
    /**
     * Translator program to SUB format.
     * @param args no args
     * @throws Exception on error
     */
    public static void main(String[] args) throws Exception {
        try (OutputStream out = new FileOutputStream("c:/download/campaign_start.sub")) {

            SubtitleManager sm1 = new SubtitleManager(new FileInputStream("data/en/message/campaign_start.sub"));
            sm1.toSub(out);
    //        SubtitleManager sm1 = new SubtitleManager(new FileInputStream("data/en/intro/intro_1.sub"));
    //        sm1.toSub(out);

    //        SubtitleManager sm2 = new SubtitleManager(new FileInputStream("data/en/intro/intro_2.sub"));
    //        sm2.offset(3 * 60 * 1000 + 21 * 1000 + 500);
    //        sm2.toSub(out);
    //
    //        SubtitleManager sm3 = new SubtitleManager(new FileInputStream("data/en/intro/intro_3.sub"));
    //        sm3.offset(3 * 60 * 1000 + 21 * 1000 + 500 + 4 * 60 * 1000 + 50 * 1000 + 888);
    //        sm3.toSub(out);
        }
    }
}
