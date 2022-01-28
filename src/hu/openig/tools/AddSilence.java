/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import java.io.RandomAccessFile;
import java.util.Arrays;
/**
 * Add silence to the audio.
 * @author akarnokd, 2012.04.16.
 *
 */
public final class AddSilence {
    /** Utility class. */
    private AddSilence() { }
    /**
     * @param args no arguments
     * @throws Exception ignored
     */
    public static void main(String[] args) throws Exception {
        try (RandomAccessFile raf = new RandomAccessFile("audio/de/messages/douglas_rebel_governor.wav", "rw")) {
            int len = 2205 * 20;
            raf.seek(4);
            int clen = Integer.reverseBytes(raf.readInt());
            raf.seek(4);
            raf.writeInt(Integer.reverseBytes(clen + len));
            raf.seek(0x28);
            int dlen = Integer.reverseBytes(raf.readInt());
            raf.seek(0x28);
            raf.writeInt(Integer.reverseBytes(dlen + len));

            byte[] prev = new byte[len];
            int pc = len;
            Arrays.fill(prev, (byte)0x80);
            byte[] next = new byte[len];
            int nc;
            // replaceloop
            do {
                long p = raf.getFilePointer();
                nc = raf.read(next);
                raf.seek(p);
                raf.write(prev, 0, pc);
                pc = nc;
                // swap buffers
                byte[] tmp = prev;
                prev = next;
                next = tmp;
            } while (nc >= 0);
        }
    }

}
