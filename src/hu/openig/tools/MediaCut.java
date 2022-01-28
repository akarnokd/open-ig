/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Cut a specific video on given start and length of frames.
 * @author akarnokd, Jan 21, 2012
 */
public final class MediaCut {
    /** Utility class. */
    private MediaCut() { }

    /**
     * Main entry.
     * @param args no arguments
     * @throws Exception on error
     */
    public static void main(String[] args) throws Exception {

        String source = "video/generic/messages/achilles_not_under_attack.ani.gz";
        String dest = "video/generic/messages/achilles_check.ani.gz";

        int framesStart = 0;
        int framesCount = 65;
        try (DataOutputStream gout = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(dest))));
                DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(source), 1024 * 1024), 1024 * 1024))) {
            int w = Integer.reverseBytes(in.readInt());
            int h = Integer.reverseBytes(in.readInt());
            if (in.skipBytes(4) != 4) {
                throw new IOException("File structure error!");
            }
//                final int frames = Integer.reverseBytes(in.readInt());
            int fps1000 = Integer.reverseBytes(in.readInt());
//                double fps = fps1000 / 1000.0;

            gout.writeInt(Integer.reverseBytes(w));
            gout.writeInt(Integer.reverseBytes(h));
            gout.writeInt(Integer.reverseBytes(framesCount));
            gout.writeInt(Integer.reverseBytes(fps1000));

            int[] palette = new int[256];
            byte[] bytebuffer = new byte[w * h];

            int frameIndex = 0;
            boolean paletteWritten = false;
            int paletteLen = 0;
            while (!Thread.currentThread().isInterrupted()) {
                int c = in.read();
                if (c < 0 || c == 'X') {
                    break;
                } else
                if (c == 'P') {
                    paletteWritten = false;
                    paletteLen = in.readUnsignedByte() ;
                    for (int j = 0; j < paletteLen; j++) {
                        int r = in.read() & 0xFF;
                        int g = in.read() & 0xFF;
                        int b = in.read() & 0xFF;
                        palette[j] = 0xFF000000 | (r << 16) | (g << 8) | b;
                    }
                } else
                if (c == 'I') {
                    in.readFully(bytebuffer);
                    if (frameIndex >= framesStart

                            && frameIndex < framesStart + framesCount) {
                        if (!paletteWritten) {
                            paletteWritten = true;
                            gout.write('P');
                            gout.writeByte(paletteLen);
                            for (int j = 0; j < paletteLen; j++) {
                                gout.writeByte((palette[j] & 0xFF0000) >> 16);
                                gout.writeByte((palette[j] & 0xFF00) >> 8);
                                gout.writeByte((palette[j] & 0xFF));
                            }
                        }
                        gout.writeByte('I');
                        gout.write(bytebuffer);
                    }
                    frameIndex++;
                }
            }
        }
    }

}
